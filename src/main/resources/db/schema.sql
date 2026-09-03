-- ============================================================
-- 个人博客数据库初始化脚本（对应 PRD 附录 A 及 7.4）
-- 环境：MySQL 8.0+
-- 建表顺序（存在外键依赖）：user → category → tag → article
--        → article_tag → comment → article_like → friend_link → site_config
-- ============================================================

CREATE DATABASE IF NOT EXISTS `blog` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `blog`;

-- ------------------------------------------------------------
-- 1. 用户表 user
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`    VARCHAR(50)  NOT NULL COMMENT '用户名（唯一）',
  `password`    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt 加密）',
  `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
  `email`       VARCHAR(100) NOT NULL COMMENT '邮箱（唯一）',
  `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像 URL',
  `role`        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN / USER',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0=禁用 / 1=正常',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 / 1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ------------------------------------------------------------
-- 2. 分类表 category
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `category` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(50) NOT NULL COMMENT '分类名称（唯一）',
  `sort`        INT         NOT NULL DEFAULT 0 COMMENT '排序（越小越靠前）',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- ------------------------------------------------------------
-- 3. 标签表 tag
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tag` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(50) NOT NULL COMMENT '标签名称（唯一）',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- ------------------------------------------------------------
-- 4. 文章表 article
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `article` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`       BIGINT UNSIGNED NOT NULL COMMENT '作者 ID（关联 user.id）',
  `category_id`   BIGINT UNSIGNED DEFAULT NULL COMMENT '分类 ID（关联 category.id，可空）',
  `title`         VARCHAR(200) NOT NULL COMMENT '标题',
  `summary`       VARCHAR(500) DEFAULT NULL COMMENT '摘要（为空时自动截取正文前 150 字）',
  `content`       LONGTEXT     NOT NULL COMMENT 'Markdown 原文',
  `content_html`  LONGTEXT     DEFAULT NULL COMMENT '渲染后 HTML（发布时生成）',
  `cover`         VARCHAR(255) DEFAULT NULL COMMENT '封面图 URL',
  `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0=草稿 / 1=待审核 / 2=已发布 / 3=已驳回',
  `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '驳回原因',
  `view_count`    INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '阅读量（MySQL 存量，Redis 存增量）',
  `like_count`    INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数',
  `is_top`        TINYINT      NOT NULL DEFAULT 0 COMMENT '是否置顶：0=否 / 1=是',
  `publish_time`  DATETIME     DEFAULT NULL COMMENT '实际发布时间（列表排序/归档用）',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 / 1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_status`     (`user_id`, `status`),
  KEY `idx_category_status` (`category_id`, `status`),
  KEY `idx_status_publish`  (`status`, `publish_time`),
  FULLTEXT KEY `ft_title_content` (`title`, `content`) WITH PARSER ngram COMMENT '标题+正文中文全文索引',
  CONSTRAINT `fk_article_user`     FOREIGN KEY (`user_id`)     REFERENCES `user` (`id`),
  CONSTRAINT `fk_article_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章表';

-- ------------------------------------------------------------
-- 5. 文章-标签关联表 article_tag
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `article_tag` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `article_id` BIGINT UNSIGNED NOT NULL COMMENT '文章 ID（关联 article.id）',
  `tag_id`     BIGINT UNSIGNED NOT NULL COMMENT '标签 ID（关联 tag.id）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`),
  KEY `idx_tag` (`tag_id`),
  CONSTRAINT `fk_article_tag_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`),
  CONSTRAINT `fk_article_tag_tag`     FOREIGN KEY (`tag_id`)     REFERENCES `tag` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章-标签关联表';

-- ------------------------------------------------------------
-- 6. 评论表 comment（楼中楼回复）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `comment` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `article_id`  BIGINT UNSIGNED NOT NULL COMMENT '所属文章 ID（关联 article.id）',
  `user_id`     BIGINT UNSIGNED NOT NULL COMMENT '评论用户 ID（关联 user.id）',
  `parent_id`   BIGINT UNSIGNED DEFAULT NULL COMMENT '父评论 ID（NULL=顶层评论，关联 comment.id）',
  `content`     VARCHAR(1000) NOT NULL COMMENT '评论内容',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0=隐藏 / 1=正常',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=正常 / 1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_article_status` (`article_id`, `status`, `deleted`),
  KEY `idx_user`           (`user_id`),
  KEY `idx_parent`         (`parent_id`),
  CONSTRAINT `fk_comment_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`),
  CONSTRAINT `fk_comment_user`    FOREIGN KEY (`user_id`)    REFERENCES `user` (`id`),
  CONSTRAINT `fk_comment_parent`  FOREIGN KEY (`parent_id`)  REFERENCES `comment` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ------------------------------------------------------------
-- 7. 点赞记录表 article_like
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `article_like` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `article_id`  BIGINT UNSIGNED NOT NULL COMMENT '文章 ID（关联 article.id）',
  `user_id`     BIGINT UNSIGNED NOT NULL COMMENT '点赞用户 ID（关联 user.id）',
  `create_time` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_user` (`article_id`, `user_id`),
  KEY `idx_user` (`user_id`),
  CONSTRAINT `fk_like_article` FOREIGN KEY (`article_id`) REFERENCES `article` (`id`),
  CONSTRAINT `fk_like_user`    FOREIGN KEY (`user_id`)    REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞记录表（唯一约束防止重复点赞）';

-- ------------------------------------------------------------
-- 8. 友链表 friend_link
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `friend_link` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`        VARCHAR(50)  NOT NULL COMMENT '站点名称',
  `url`         VARCHAR(255) NOT NULL COMMENT '站点地址',
  `logo`        VARCHAR(255) DEFAULT NULL COMMENT 'Logo URL',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '简介',
  `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序（越小越靠前）',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0=隐藏 / 1=展示',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='友链表';

-- ------------------------------------------------------------
-- 9. 站点配置表 site_config（KV 结构）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `site_config` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `config_key`   VARCHAR(50) NOT NULL COMMENT '配置键（唯一）',
  `config_value` TEXT        DEFAULT NULL COMMENT '配置值',
  `remark`       VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站点配置表';

-- 初始化基础站点配置
INSERT INTO `site_config` (`config_key`, `config_value`, `remark`) VALUES
('site_name', '我的博客', '博客名称'),
('slogan', '记录与分享', '副标题');

-- ------------------------------------------------------------
-- 提示：管理员账号在 M1 实现注册接口后创建（注册成功后手动把 role 改为 ADMIN），
-- 或通过 SQL 插入 BCrypt 加密后的密码哈希。
-- ------------------------------------------------------------