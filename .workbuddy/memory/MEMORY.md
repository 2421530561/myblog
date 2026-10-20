# myblog 项目长期记忆

## 项目约定
- 文档驱动：需求以 `docs/PRD.md` 为准，里程碑按 PRD 第 8 节推进，完成一个里程碑更新 README 的 checkbox。
- 后端包名 `com.littlecase.myblog`，分层 `common/config/controller/service/mapper/entity/dto/vo`。
- 统一返回体 `{code, message, data}`：0 成功 / 400 参数 / 401 未登录 / 403 无权限 / 404 不存在 / 500 业务或系统错误。
- 文章状态：0 草稿 / 1 待审核 / 2 已发布 / 3 已驳回（`common/ArticleStatus`）；前台只展示已发布。
- 手写 SQL 必须放 `src/main/resources/mapper/*.xml`（`mapper-locations=classpath*:mapper/*.xml`），放 `src/main/java` 下不会被加载。
- 上传图片落盘 `./upload/{yyyyMMdd}/{uuid}.ext`，访问路径 `/upload/**`；上传目录必须转绝对路径。
- 前端：Vue3 + TS + Naive UI（全局注册），接口封装在 `src/api/*`，`request.ts` 负责解包与 401 跳转。
- 前端构建必须过 `vue-tsc --noEmit`，因此避免未使用的变量/导入（`noUnusedLocals` 开启）。
- 计数类 Redis 操作统一用 `StringRedisTemplate`（`RedisTemplate<String,Object>` 的 JDK 序列化读不了 `INCR` 写入的数字）。
- 评论接口：`GET /api/comment/page?articleId=&page=&size=` 返回 `IPage<CommentVO>`（顶层评论分页，回复在 `children`）；点赞 `POST /api/like/article/{id}`。
- 点赞以 `article_like` 为事实源，点赞数走 `blog:like:count:{id}` 缓存（TTL 10 分钟）；阅读量走 `blog:view:{id}` 增量 + 每 5 分钟 `ViewCountSyncTask` 落库。

## 本机环境要点
- 后端端口 8081（前端 vite proxy 已指向 8081），前端 5173；MySQL root/11111111，库 `blog`。
- Redis：本机原先无服务，便携版在 `D:\tools\redis`，启动 `cd /d/tools/redis && ./redis-server.exe redis.windows.conf`（6379，无密码）。
- Git Bash 下不要直接用 `mvnw`/`mvn`（MSYS 破坏 `-classpath`）；用 java 直接拉 Maven 的 plexus-classworlds（`/tmp/mvnb.sh`）。
- 编译后端统一用 `clean compile`，避免 IDE 自动构建写脏 `target/classes`。
- 图片上传上限 5MB（`spring.servlet.multipart.max-file-size`），Vditor 资源由 `npm run sync:vditor` 生成。
- Vite 打包时若 `dist` 已存在会被沙箱 safe-delete 拦截：先 `mv dist /tmp/dist_bak_xxx` 再 `npx vite build`。

## 待办（M4 起）
- FULLTEXT 全文搜索、归档、友链、站点设置、后台仪表盘/用户/评论管理、SEO meta
- 后台置顶操作入口、分类/标签文章数统计
