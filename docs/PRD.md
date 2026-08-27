# 个人博客网站 PRD（产品需求文档）

- 版本：v1.0
- 状态：已评审确认
- 技术栈：Vue 3 + Spring Boot 3.x + MyBatis-Plus + MySQL 8 + Redis

---

## 1. 项目概述

### 1.1 背景与目标

构建一个前后端分离的个人博客网站，作者可以在线撰写、发布文章，访客可浏览文章、按分类/标签/时间归档检索内容，注册用户可评论、点赞并投稿自己的文章（经管理员审核后发布）。

### 1.2 产品定位

个人技术/生活博客平台，轻量、自托管、数据完全自持。第一版目标为**本地完整跑通全链路**，不涉及部署上线。

### 1.3 用户角色

| 角色 | 说明 |
|---|---|
| 游客（未登录） | 浏览前台公开内容 |
| 注册用户（USER） | 浏览、评论、点赞、撰写并投稿自己的文章 |
| 管理员（ADMIN） | 拥有全部权限，含文章审核发布、内容与用户管理 |

---

## 2. 确认的决策清单（头脑风暴结论）

| 决策点 | 结论 |
|---|---|
| 前台渲染 | 纯 SPA（Vue 3），动态 meta 做基本 SEO 补救 |
| 用户体系 | 管理员 + 开放注册，角色权限（ADMIN / USER） |
| 发文权限 | 注册用户只能管理**自己的**文章；发布需管理员审核；不允许多人协作 |
| 评论 | 自建评论系统，**必须登录**才能评论，游客只读 |
| 验证码 | 注册 / 登录 / 评论均不引入验证码 |
| 站内搜索 | MySQL 8 FULLTEXT 全文索引（ngram 中文分词） |
| 后端版本 | Spring Boot 3.x + JDK 17 |
| 部署 | 第一版不部署，本地跑通（Docker 部署留作后续） |
| 图片存储 | 上传到本地磁盘指定目录 + Spring MVC 虚拟路径映射访问 |
| Redis | 引入，仅用于：① 阅读量/点赞计数 ② 热点数据缓存 |
| 前台 UI | 组件库 Naive UI（前台与后台统一使用，不再引入第二套组件库） |
| 评论区管理 | 评论直接可见，管理员可删除，用户可删自己的评论 |

---

## 3. 权限矩阵

| 功能 | 游客 | 注册用户 | 管理员 |
|---|:---:|:---:|:---:|
| 浏览文章 / 分类 / 标签 / 归档 / 搜索 | ✅ | ✅ | ✅ |
| 查看评论 | ✅ | ✅ | ✅ |
| 发表评论 / 删除自己的评论 | ❌ | ✅ | ✅ |
| 点赞 / 取消点赞 | ❌ | ✅ | ✅ |
| 写文章（存草稿） | ❌ | ✅ | ✅ |
| 提交审核 / 编辑删除自己的文章 | ❌ | ✅（仅自己的） | ✅（全部） |
| 审核通过 / 驳回 / 下架文章 | ❌ | ❌ | ✅ |
| 分类 / 标签管理 | ❌ | ❌ | ✅ |
| 评论管理（删除） | ❌ | ❌ | ✅ |
| 用户管理（禁用/启用） | ❌ | ❌ | ✅ |
| 友链 / 站点设置管理 | ❌ | ❌ | ✅ |
| 文章图片上传 | ❌ | ✅（自己的文章配图） | ✅ |

---

## 4. 功能需求

### 4.1 前台模块（访客 / 登录用户）

| 功能 | 说明 | 优先级 |
|---|---|---|
| 首页文章列表 | 分页展示已发布文章（按发布时间倒序，置顶优先），卡片含封面、摘要、分类、标签、阅读量、点赞数 | P0 |
| 文章详情 | Markdown 渲染 + 代码高亮；展示作者、发布时间、分类、标签、阅读量；支持点赞；下方评论区 | P0 |
| 分类页 / 标签页 | 按分类或标签筛选文章 | P0 |
| 归档 | 按年/月时间轴展示文章列表 | P0 |
| 站内搜索 | 输入关键词全文检索（标题+正文），高亮关键词，分页结果 | P0 |
| 关于我 | 静态页面，内容来自站点配置 | P1 |
| 友链 | 展示友链列表 | P1 |
| 评论区 | 登录用户发表评论，支持楼中楼回复（一级评论 + 回复，parent_id 树形），分页加载 | P0 |
| 点赞 | 登录用户点赞/取消，同一用户对同一文章只能点赞一次 | P0 |
| SEO 基础 | 动态 title / description / keywords / og 标签；静态 robots.txt | P1 |

### 4.2 用户模块

| 功能 | 说明 | 优先级 |
|---|---|---|
| 注册 | 用户名 + 邮箱 + 密码（BCrypt 加密），用户名/邮箱唯一 | P0 |
| 登录 / 登出 | JWT 认证，Token 有效期可配置 | P0 |
| 个人中心 | 查看/修改昵称、头像、邮箱；修改密码 | P1 |
| 我的文章 | 查看自己所有文章（草稿/待审核/已发布/已驳回） | P0 |
| 写文章 / 编辑 | Vditor 编辑器，Markdown 写作，支持上传配图（含封面） | P0 |
| 提交审核 | 草稿或编辑后可提交审核，进入待审核状态 | P0 |

### 4.3 后台管理模块（仅 ADMIN）

| 功能 | 说明 | 优先级 |
|---|---|---|
| 仪表盘 | 文章数、评论数、用户数、总阅读量、近 7 日发布趋势 | P1 |
| 文章管理 | 全量文章列表，按状态/作者/分类筛选；审核通过 / 驳回（填写原因）/ 下架；编辑、删除任意文章 | P0 |
| 分类管理 | 分类 CRUD（名称、排序） | P0 |
| 标签管理 | 标签 CRUD | P0 |
| 评论管理 | 全站评论列表、删除评论 | P1 |
| 用户管理 | 用户列表、禁用/启用账号 | P1 |
| 友链管理 | 友链 CRUD（名称、URL、Logo、简介、排序、启停） | P1 |
| 站点设置 | 博客名、副标题、头像、简介、关于我内容、公告、ICP 备案号等 | P1 |

### 4.4 功能优先级总览

- **P0（第一版必须）**：注册登录、文章前台展示与详情、写作与审核流、分类标签、评论、点赞、阅读量、FULLTEXT 搜索、图片上传
- **P1（第一版完成）**：归档、友链、站点设置、后台仪表盘、用户/评论管理、SEO meta、个人中心
- **P2（后续迭代，可选）**：暗色模式、RSS 订阅、文章图片管理页、邮箱找回密码、文章导出 Markdown

---

## 5. 核心流程

### 5.1 文章发布审核流程

```
注册用户写文章（保存为 DRAFT 草稿）
        │ 提交审核
        ▼
PENDING 待审核（用户可编辑/撤回/删除）
        │ 管理员审核
        ├── 通过 ──► PUBLISHED 已发布（前台可见）
        └── 驳回 ──► REJECTED 已驳回（填写驳回原因，作者可见并可修改后重新提交）

管理员发文：可直接创建并发布（跳过审核）。
已发布文章可由管理员下架（回到 DRAFT）。
```

- 状态枚举：`DRAFT(草稿) / PENDING(待审核) / PUBLISHED(已发布) / REJECTED(已驳回)`
- 前台只展示 `PUBLISHED` 的文章
- 文章实体带 `publish_time`（实际发布时间）用于列表排序与归档

### 5.2 评论流程

1. 游客可查看评论；发表评论需登录。
2. 评论默认直接可见（不引入审核），管理员可在后台删除；作者可删除自己的评论。
3. 删除评论时，其子回复一并逻辑删除。
4. 接口层做基础频率限制（如同一用户每分钟最多 5 条），防止简单刷屏。

### 5.3 阅读量 / 点赞计数（Redis）

- **阅读量**：文章详情接口每次访问 `INCR blog:view:{articleId}`；定时任务（每 5 分钟）将 Redis 增量累加写回 MySQL 并清零。前台展示 = MySQL 存量 + Redis 当前增量。
- **点赞**：以 MySQL `article_like` 表为准（唯一约束防重复点赞），点赞数读取顺序：Redis 缓存 `blog:like:count:{articleId}` → 未命中则查库并回填（TTL 10 分钟）；点赞/取消时同步更新 Redis 计数。

---

## 6. 非功能需求

| 类别 | 要求 |
|---|---|
| 性能 | 列表/详情接口 P95 < 300ms；热点数据走 Redis 缓存；图片走 Nginx/静态资源不经过 Java 业务链路 |
| 安全 | 密码 BCrypt；JWT 签名与过期校验；后台接口按角色鉴权；XSS：前端 DOMPurify 清洗 + 后端入库前再次清洗；SQL 注入：MyBatis-Plus 预编译 + 全文检索参数化；上传类型白名单（jpg/png/webp/gif）、单文件 ≤ 5MB、文件名 UUID 重命名；登录失败与评论接口基础限流 |
| 健壮性 | 统一异常处理与统一返回体；参数校验（jakarta validation）；逻辑删除（deleted 字段） |
| 数据 | MySQL 字符集 utf8mb4；关键表时间戳字段；定时任务异常不影响主链路 |
| 兼容 | 前端现代浏览器（Chrome/Edge/Safari/Firefox 近两年版本） |

---

## 7. 技术方案

### 7.1 总体架构

```
浏览器 (Vue 3 SPA，前台+后台同一应用，/admin 路由区隔)
        │  Axios（统一拦截器：携带 JWT、统一错误提示）
        ▼
Spring Boot 3 后端（RESTful /api/**，Knife4j 接口文档）
   ├── Sa-Token（登录态 + 角色权限 @SaCheckRole）
   ├── MyBatis-Plus（ORM、分页插件、逻辑删除、代码生成器）
   ├── Redis（计数、热点缓存，Lettuce 客户端）
   └── 本地磁盘存储（/upload/** 虚拟路径映射）
        │
        ▼
MySQL 8（FULLTEXT ngram 中文全文索引）
```

### 7.2 前端技术栈（blog-frontend/）

| 项 | 选型 |
|---|---|
| 框架 | Vue 3（Composition API + `<script setup>`）+ TypeScript |
| 构建 | Vite |
| 路由 | Vue Router 4（前台布局 + 后台布局 + 路由守卫） |
| 状态 | Pinia（用户态、站点配置） |
| HTTP | Axios（拦截器统一处理 Token 与错误码） |
| UI | Naive UI（前台 + 后台统一使用） |
| 图标 | xicons（@vicons/ionicons5 等） |
| Markdown 编辑 | Vditor（分屏预览 / 所见即所得） |
| Markdown 渲染 | markdown-it + highlight.js + DOMPurify（防 XSS） |
| SEO | @unhead/vue 动态 title/description/keywords/og |

### 7.3 后端技术栈（blog-backend/）

| 项 | 选型 |
|---|---|
| 语言/运行时 | Java 17（JDK 17） |
| 框架 | Spring Boot 3.x |
| ORM | MyBatis-Plus 3.5.x（`mybatis-plus-spring-boot3-starter`） |
| 认证授权 | Sa-Token（`sa-token-spring-boot3-starter`，`@SaCheckRole` 注解式权限） |
| 缓存 | spring-boot-starter-data-redis（Lettuce） |
| 接口文档 | Knife4j（`knife4j-openapi3-jakarta-spring-boot-starter`） |
| 数据库驱动 | mysql-connector-j |
| 校验 | jakarta validation |
| 构建 | Maven |

> 选型说明：认证未采用 Spring Security 而是 Sa-Token —— 个人博客角色模型简单（两级），Sa-Token 上手快、注解式权限清晰，可显著降低配置复杂度；如后续需要复杂 OAuth2 可平滑替换。

### 7.4 数据库设计（9 张表）

| 表 | 关键字段 | 说明 |
|---|---|---|
| `user` | id, username(唯一), password(BCrypt), nickname, email(唯一), avatar, role(ADMIN/USER), status(0禁用/1正常), create_time, update_time, deleted | 用户 |
| `article` | id, user_id(作者), category_id, title, summary, content(Markdown 原文), content_html(渲染后HTML，发布时生成), cover, status(DRAFT/PENDING/PUBLISHED/REJECTED), reject_reason, view_count, like_count, is_top, publish_time, create_time, update_time, deleted | 文章；FULLTEXT 索引(title, content) WITH PARSER ngram |
| `category` | id, name(唯一), sort, create_time | 分类 |
| `tag` | id, name(唯一), create_time | 标签 |
| `article_tag` | id, article_id, tag_id, 唯一索引(article_id, tag_id) | 文章-标签多对多 |
| `comment` | id, article_id, user_id, parent_id(NULL为顶层，外键级联删除子回复), content, status, create_time, deleted | 评论（楼中楼） |
| `article_like` | id, article_id, user_id, create_time, 唯一索引(article_id, user_id) | 点赞记录（防重复） |
| `friend_link` | id, name, url, logo, description, sort, status, create_time | 友链 |
| `site_config` | id, config_key(唯一), config_value, remark | 站点配置（KV 形式，灵活扩展） |

关键设计说明：
- **全文索引**：MySQL 8 使用默认 `ngram` 解析器支持中文分词；通过自定义 Mapper XML 写 `MATCH(title, content) AGAINST (#{keyword} IN BOOLEAN MODE)`（MyBatis-Plus Wrapper 不支持 MATCH AGAINST，需手写 SQL）。
- **审核流**：`article.status` 四态 + `reject_reason`，配合权限注解控制审核接口仅 ADMIN 可调。
- **阅读量**：MySQL 存存量，Redis 存增量，定时批量回写。

### 7.5 接口规范（RESTful，前缀 /api）

- 统一返回体：`{ "code": 0, "message": "ok", "data": ... }`，分页返回 `{ records, total, current, size }`
- 认证：`POST /api/auth/register`、`POST /api/auth/login`，登录后返回 JWT，请求头 `Authorization: Bearer <token>`
- 核心接口（示例）：
  - 文章：`GET /api/article/page`（列表）、`GET /api/article/{id}`（详情，阅读量+1）、`GET /api/article/archives`（归档）、`GET /api/article/search?keyword=`（全文检索）
  - 写作：`POST /api/article`、`PUT /api/article/{id}`、`POST /api/article/{id}/submit`（提交审核）
  - 审核：`POST /api/admin/article/{id}/publish`、`POST /api/admin/article/{id}/reject`（带原因）
  - 评论：`GET /api/comment/page?articleId=`、`POST /api/comment`、`DELETE /api/comment/{id}`
  - 点赞：`POST /api/article/{id}/like`、`DELETE /api/article/{id}/like`、`GET /api/article/{id}/like/status`
  - 上传：`POST /api/upload/image`
  - 站点：`GET /api/config/public`（前台展示用）、`PUT /api/admin/config`
- 文件访问：上传落盘到 `./upload/{yyyyMMdd}/uuid.ext`，Spring MVC 虚拟路径映射 `/upload/**` → 磁盘目录；本地开发由 Vite proxy 转发 `/upload` 到后端

**搜索接口请求/响应示例（含高亮字段）**

请求：

```
GET /api/article/search?keyword=Redis&page=1&size=10
```

响应（统一返回体；`data.records` 为结果列表）：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "records": [
      {
        "id": 12,
        "title": "基于 Redis 的博客缓存设计",
        "snippet": "本文介绍 Redis 缓存在个人博客中的应用，包括计数与热点缓存方案……",
        "highlight": {
          "title": "基于 <em class=\"hl\">Redis</em> 的博客缓存设计",
          "snippet": "本文介绍 <em class=\"hl\">Redis</em> 缓存在个人博客中的应用，包括计数与热点缓存方案……"
        },
        "categoryName": "后端",
        "tags": ["Redis", "缓存"],
        "cover": "/upload/20250101/abc.webp",
        "viewCount": 328,
        "likeCount": 45,
        "publishTime": "2025-01-01 10:00:00"
      }
    ],
    "total": 3,
    "current": 1,
    "size": 10
  }
}
```

**高亮字段返回格式约定**：

1. `title` / `snippet`：返回**原始纯文本**（不做任何标记），供列表常规展示（`v-text`）。
2. `highlight.title` / `highlight.snippet`：返回**高亮后的 HTML 片段**，命中关键词以 `<em class="hl">关键词</em>` 包裹；多个关键词逐个包裹，英文匹配忽略大小写。
3. `snippet` 与 `highlight.snippet`：默认截取正文中命中关键词的上下文片段（前后各约 60 字）；无命中上下文时取摘要前 150 字。
4. 生成方式：MySQL FULLTEXT（`MATCH...AGAINST`）只负责**检索匹配**，本身不提供 snippet 与高亮；后端从 Markdown 原文提取纯文本 → HTML 转义 → 按关键词正则包裹 `<em class="hl">` 后返回。
5. 前端渲染 `highlight` 字段时使用 `v-html`，并对 HTML 做 DOMPurify 二次清洗；`.hl` 高亮样式（如黄色底色）由前端全局 CSS 定义。

### 7.6 Redis 缓存设计

| Key | 用途 | TTL / 策略 |
|---|---|---|
| `blog:view:{articleId}` | 阅读量增量计数 | 定时任务每 5 分钟回写 MySQL 后清零 |
| `blog:like:count:{articleId}` | 点赞数缓存 | 10 分钟，点赞/取消时同步更新 |
| `blog:cache:siteConfig` | 站点配置 | 10 分钟，后台修改时主动删除 |
| `blog:cache:categories` / `blog:cache:tags` | 分类/标签列表 | 10 分钟，后台修改时主动删除 |
| `blog:cache:homeList:{page}` | 首页文章列表 | 10 分钟，审核发布/下架时主动删除 |

### 7.7 工程结构规划

```
myblog/
├── docs/
│   └── PRD.md                        # 本文档
├── blog-frontend/                    # Vue 3 + TS + Vite
│   └── src/
│       ├── api/                      # axios 封装与接口定义
│       ├── components/
│       ├── layouts/                  # FrontLayout（前台）/ AdminLayout（后台）
│       ├── router/                   # 路由 + 登录守卫
│       ├── stores/                   # Pinia：user、app（站点配置）
│       ├── utils/
│       └── views/
│           ├── front/                # 首页/详情/分类/标签/归档/搜索/关于/友链/登录注册/个人中心
│           └── admin/                # 仪表盘/文章/分类/标签/评论/用户/友链/站点设置
└── blog-backend/                     # Spring Boot 3 + Maven
    ├── pom.xml
    └── src/main/
        ├── java/com/blog/
        │   ├── config/               # WebMvc(虚拟路径映射/拦截器)、Redis、MyBatisPlus、SaToken、Knife4j
        │   ├── common/               # 统一返回体、异常、常量、状态枚举
        │   ├── controller/           # auth/article/category/tag/comment/like/upload/admin
        │   ├── service/
        │   ├── mapper/
        │   ├── entity/ dto/ vo/
        │   └── utils/
        └── resources/
            ├── application.yml       # 端口 8080、数据源、Redis、上传目录、JWT 配置
            └── mapper/               # 手写 SQL（全文检索等）
```

### 7.8 开发环境

| 项 | 版本 |
|---|---|
| Node.js | 18+ |
| JDK | 17 |
| Maven | 3.8+ |
| MySQL | 8.0+（本地或 Docker） |
| Redis | 6+（本地或 Docker） |
| 前端开发端口 | 5173（Vite，proxy `/api`、`/upload` → 8080） |
| 后端端口 | 8080 |

---

## 8. 迭代规划（本地开发里程碑）

| 里程碑 | 内容 | 验收标准 |
|---|---|---|
| M1 工程与认证 | 前后端工程初始化、数据库建表、注册/登录/JWT、Sa-Token 角色权限框架、统一返回体 | 可注册登录，游客/用户/管理员接口权限隔离生效 |
| M2 文章链路 | 前台列表/详情、分类标签、Vditor 写作、草稿/提交审核/审核发布全流程、图片上传与虚拟路径映射 | 用户投稿→管理员审核→前台可见全流程跑通 |
| M3 互动 | 评论（登录+楼中楼+删除）、点赞、阅读量计数（Redis 增量 + 定时落库） | 互动功能完整，计数最终一致 |
| M4 检索与管理 | FULLTEXT 全文搜索、归档、友链、站点设置、后台仪表盘/用户/评论管理、SEO meta | P0+P1 功能全部可用 |
| M5 打磨 | 统一交互细节、异常兜底、限流、自测用例、README | 本地全链路稳定运行 |

---

## 9. 风险与待定事项

| 风险/待定 | 说明与应对 |
|---|---|
| ngram 中文分词限制 | 默认 token 粒度 2 字，单字/过短关键词可能检索不到；PRD 接受此限制，必要时可调整 ngram_token_size |
| 无验证码的注册滥用 | 第一版接受该决策；如需加固，后续可在注册接口引入图形验证码或邮箱验证 |
| 审核流复杂度 | 已确认纳入；通过状态机枚举 + 状态变更校验简化实现 |
| Redis 本地依赖 | 开发环境需本机安装 Redis（或 Docker 起一个），application.yml 提供默认连接配置 |
| 组件库统一 | 前台后台统一 Naive UI，避免双组件库体积与风格分裂（已确认） |
| TypeScript | 前端默认使用 TS；如个人偏好 JS 可降级，不影响后端设计 |

---

## 10. 假设与约定

1. 文章内容以 Markdown 原文存储，展示时渲染为 HTML（发布时生成 `content_html` 缓存渲染结果）。
2. 文章摘要可手填，为空时自动截取正文前 150 字。
3. 封面图可选，无封面时前台使用占位样式。
4. 评论仅支持"一级评论 + 回复"两级结构。
5. 管理员发文直接发布，不走审核；管理员可审核/下架任何文章。
6. 被禁用用户无法登录、无法评论，历史内容保留。
7. 本地开发前端通过 Vite proxy 转发 `/api` 与 `/upload`，生产静态资源托管方式后续迭代再定。

---

## 附录 A：数据库 DDL（核心表）

> 环境：MySQL 8.0+，引擎 InnoDB，字符集 utf8mb4 / 排序规则 utf8mb4_unicode_ci。
> 建表顺序（存在外键依赖）：`user` → `category` → `article` → `comment` → `article_like`。
> 通用约定：
> 1. 主键统一 `BIGINT UNSIGNED` 自增；
> 2. 删除统一走**逻辑删除**（`deleted` 字段，0=正常 / 1=已删除），外键级联仅在物理删除时兜底触发；
> 3. 时间字段统一 `DATETIME`：`create_time` 默认当前时间，`update_time` 随更新自动刷新；
> 4. 全文索引使用 MySQL 8 内置 `ngram` 解析器（支持中文分词，默认 token 粒度 2 字）。

```sql
-- ------------------------------------------------------------
-- 1. 用户表 user
-- ------------------------------------------------------------
CREATE TABLE `user` (
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
-- 2. 文章表 article（依赖 user、category）
-- ------------------------------------------------------------
CREATE TABLE `article` (
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
-- 3. 评论表 comment（依赖 article、user；楼中楼回复）
-- ------------------------------------------------------------
CREATE TABLE `comment` (
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
-- 4. 点赞记录表 article_like（依赖 article、user）
-- ------------------------------------------------------------
CREATE TABLE `article_like` (
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
```

**补充说明**：

1. **逻辑删除与唯一索引的冲突**：`user.username` / `user.email` 的唯一索引在记录被逻辑删除后仍占用该值，会导致"删除用户后无法用同名重新注册"。服务层处理：注册校验时排除已删除记录（`WHERE deleted = 0`），或在唯一索引上扩展为 `(username, deleted)` 复合唯一索引。PRD 默认采用**服务层校验**方案。
2. 其余表（`category`、`tag`、`article_tag`、`friend_link`、`site_config`）结构简单，遵循上述通用约定，在 M1 里程碑随代码初始化脚本一并生成，不在此附录展开。
