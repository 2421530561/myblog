# 个人博客（myblog）

基于 [docs/PRD.md](docs/PRD.md) 搭建的前后端分离个人博客。

- **前端** `blog-frontend/`：Vue 3 + TypeScript + Vite + Naive UI
- **后端**（本仓库根目录）：Spring Boot 3.4 + MyBatis-Plus + MySQL 8 + Redis（计数缓存）
- **互动**：评论（楼中楼）、点赞、阅读量（Redis 增量 + 定时落库）

> 当前状态：**M3 互动已完成**（M1 认证、M2 文章链路、M3 评论/点赞/阅读量全部跑通）。
> 验收记录见 [docs/M2-验收报告.md](docs/M2-验收报告.md)、[docs/M3-验收报告.md](docs/M3-验收报告.md)。

## 目录结构

```
myblog/
├── docs/
│   └── PRD.md                          # 产品需求文档
├── blog-frontend/                      # 前端工程（Vue 3 + TS + Vite）
│   └── src/
│       ├── api/                        # axios 封装与接口定义
│       ├── components/                 # 通用组件
│       ├── layouts/                    # FrontLayout（前台）/ AdminLayout（后台）
│       ├── router/                     # 路由 + 登录/角色守卫
│       ├── stores/                     # Pinia：user（用户态）、app（站点配置）
│       └── views/
│           ├── front/                  # 前台页面
│           └── admin/                  # 后台页面
├── pom.xml                             # 后端 Maven 配置（Spring Boot 4.1.1）
├── mvnw / mvnw.cmd                     # Maven Wrapper（无需本机装 Maven）
└── src/
    ├── main/
    │   ├── java/com/littlecase/myblog/
    │   │   ├── common/                 # 统一返回体、业务异常、全局异常处理
    │   │   ├── config/                 # MyBatis-Plus、WebMvc（虚拟路径映射）
    │   │   ├── controller/             # REST 控制器（占位 + TODO）
    │   │   └── entity/ mapper/ service/ dto/ vo/   # 空目录，里程碑中逐个填充
    │   └── resources/
    │       ├── application.yml         # 端口 8080、数据源、MyBatis-Plus 配置
    │       ├── db/schema.sql           # 数据库建表脚本（9 张表）
    │       └── mapper/                 # 手写 SQL（全文检索等）
    └── test/                           # 测试
```

## 如何运行（M0 验证）

### 1. 准备数据库

- 本地安装 MySQL 8，创建库并执行建表脚本：

  ```bash
  mysql -uroot -p < src/main/resources/db/schema.sql
  ```

- 修改 `src/main/resources/application.yml` 中的数据库账号密码（默认 root/root）。

### 2. 启动后端（Windows 用 `mvnw.cmd`）

```bash
mvnw spring-boot:run
```

验证：浏览器访问 `http://localhost:8080/api/health`，应返回 `{ "code": 0, "message": "ok", "data": "blog backend is running" }`。

### 3. 启动前端

```bash
cd blog-frontend
npm install
npm run dev
```

打开 `http://localhost:5173`，应看到前台页面（首页 / 归档 / 关于 / 友链 + 登录注册入口）。

> Vditor 的运行时资源（`lute.min.js`、highlight.js 等）默认从 CDN 加载，离线会白屏。
> `npm run dev` / `npm run build` 会先执行 `scripts/sync-vditor.mjs`，把所需资源同步到
> `public/vditor/dist`（已在 .gitignore 中），编辑器通过 `cdn: '/vditor'` 走本地资源。

### 常见问题

- **`./mvnw` 报 “找不到或无法加载主类 org.codehaus.plexus.classworlds.launcher.Launcher”**
  Git Bash 下 MSYS 会改写 `-classpath` 参数导致启动失败。设置好 `JAVA_HOME` 后用 IDE 启动，
  或直接用 `java` 拉起 Maven（参见 docs/M2-验收报告.md 的“本地启动备忘”）。
- **改了代码但行为没变 / 报 `Unresolved compilation problem`**
  IDE 的 Java 自动构建会往 `target/classes` 写入半成品 class，Maven 增量编译会因时间戳判定
  “已最新”而跳过。执行一次 `clean compile` 即可。

## 学习路线（对应 PRD 第 8 节里程碑）

### M1 工程与认证
- [ ] MyBatis-Plus 基础：以 `user` 表为例写 Entity / Mapper / Service
- [ ] 注册接口：参数校验（jakarta validation）+ BCrypt 加密
- [ ] 登录接口 + JWT（启用 `pom.xml` 中被注释的 Sa-Token 依赖）
- [ ] 登录页 / 注册页真实表单接入（替换占位页）

### M2 文章链路
- [x] article 实体与前台列表 / 详情接口（分页插件已配好）
- [x] 分类 / 标签 CRUD
- [x] Vditor 编辑器接入 + 图片上传（虚拟路径映射已配好）
- [x] 草稿 → 提交审核 → 审核发布 / 驳回状态机

> M2 关键实现说明：
> - 状态枚举见 `common/ArticleStatus`（0 草稿 / 1 待审核 / 2 已发布 / 3 已驳回），流转规则见 `ArticleServiceImpl`
> - `content_html` 在**发布时**由 `MarkdownUtil`（commonmark + GFM 表格/删除线）渲染，并经 `HtmlSanitizer`（jsoup 白名单）清洗
> - Sa-Token 注解鉴权需手动注册 `SaInterceptor`，角色数据源需实现 `StpInterface` —— 见 `config/SaTokenConfig`、`config/StpInterfaceImpl`
> - 上传的图片落盘到 `./upload/{yyyyMMdd}/{uuid}.ext`，经 `/upload/**` 虚拟路径访问（上传目录已转绝对路径）

### M3 互动
- [x] 评论（登录校验、楼中楼回复、作者/管理员删除、每分钟 5 条限流）
- [x] 点赞（唯一约束防重复 + Redis 计数缓存 TTL 10 分钟）
- [x] 阅读量（Redis 增量 + 每 5 分钟定时落库）
- [x] 前台文章详情页评论区 + 点赞按钮

> M3 关键实现说明：
> - 阅读量：详情接口只 `INCR blog:view:{id}`，`config/ViewCountSyncTask` 每 5 分钟累加进 `article.view_count` 后删 key；展示值 = DB 存量 + Redis 增量
> - 点赞：`article_like` 表为唯一事实源（`uk_article_user`），点赞数按 PRD 走 `blog:like:count:{id}` 缓存（TTL 10 分钟，未命中查库回填），Redis 故障自动降级查库
> - 评论：顶层评论分页（新的在前），回复统一挂在所属顶层评论下展示并带“回复 @某人”；删除时递归逻辑删除全部后代
> - Redis 不可用时评论/阅读量/点赞均降级，不影响主链路（评论限流放行、阅读量直接写库、点赞数查库）
> - 本地 Redis 端口 6379；`application.yml` 的 `spring.data.redis` 即连接配置

### M4 检索与管理
- [ ] FULLTEXT 全文搜索（手写 Mapper XML，返回高亮字段）
- [ ] 归档、友链、站点设置、后台管理页面
- [ ] SEO meta（@unhead/vue）

### M5 打磨
- [ ] 限流、异常兜底、自测用例、完善 README

> 建议按顺序推进：每完成一个里程碑在对应 checkbox 打勾，遇到卡点随时提问。

## 技术选型备注（Boot 4 适配）

- Spring Boot 4.1.1（JDK 17）为本机 start.spring.io 生成版本，PRD 原计划 Boot 3.x；Boot 4 生态兼容已查证：
  - MyBatis-Plus：使用 `mybatis-plus-spring-boot4-starter` 3.5.14（官方支持 Boot 4）
  - Sa-Token：使用 `sa-token-spring-boot4-starter` 1.45.0（官方适配 Boot 4）
  - Knife4j 接口文档：Boot 4 需用社区分支 knife4j-next（M4 启用时再确认版本）
- 相关依赖在 `pom.xml` 中以注释形式预留，按里程碑逐个启用。
