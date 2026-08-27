# 个人博客（myblog）

基于 [docs/PRD.md](docs/PRD.md) 搭建的前后端分离个人博客。

- **前端** `blog-frontend/`：Vue 3 + TypeScript + Vite + Naive UI
- **后端**（本仓库根目录）：Spring Boot 4 + MyBatis-Plus + MySQL 8（Redis 后续里程碑接入）

> 当前状态：**M0 项目框架（脚手架）**。接口与业务代码均为占位 / TODO，随学习路线逐步实现。

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

## 学习路线（对应 PRD 第 8 节里程碑）

### M1 工程与认证
- [ ] MyBatis-Plus 基础：以 `user` 表为例写 Entity / Mapper / Service
- [ ] 注册接口：参数校验（jakarta validation）+ BCrypt 加密
- [ ] 登录接口 + JWT（启用 `pom.xml` 中被注释的 Sa-Token 依赖）
- [ ] 登录页 / 注册页真实表单接入（替换占位页）

### M2 文章链路
- [ ] article 实体与前台列表 / 详情接口（分页插件已配好）
- [ ] 分类 / 标签 CRUD
- [ ] Vditor 编辑器接入 + 图片上传（虚拟路径映射已配好）
- [ ] 草稿 → 提交审核 → 审核发布 / 驳回状态机

### M3 互动
- [ ] 评论（登录校验、楼中楼回复）
- [ ] 点赞（唯一约束防重复 + Redis 计数缓存）
- [ ] 阅读量（Redis 增量 + 定时落库，启用 Redis 依赖）

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
