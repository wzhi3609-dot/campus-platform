# 校园互助交流平台

面向全校师生的综合互助平台，支持问答、二手交易、论坛、失物招领四大模块。学生与教师分开注册，所有用户和帖子需管理员审核后方可使用。

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2.5 |
| 语言 | Java 21 (GraalVM) |
| 数据库 | H2（开发）/ MySQL（生产） |
| 缓存 | Redis + Spring Cache（`@Cacheable`，生产环境自动启用） |
| ORM | Spring Data JPA |
| 安全 | Spring Security + JWT (jjwt 0.12) |
| 参数校验 | Bean Validation |
| AOP | Spring AOP（自动操作日志） |
| API 文档 | Knife4j / Swagger |
| 限流 | 自定义 IP 级别速率限制 |
| XSS 防御 | Jsoup HTML 白名单过滤 |
| 文件上传 | MIME 类型 + 扩展名双重校验 |
| 定时任务 | `@EnableScheduling`（预留） |
| 富文本编辑 | VueQuill (Quill) |
| 图表 | ECharts / vue-echarts |
| 前端框架 | Vue 3 (Composition API) |
| 构建工具 | Vite |
| UI 库 | Element Plus（按需引入） |
| 状态管理 | Pinia |
| HTTP 客户端 | Axios |
| 后端构建 | Maven |

---

## 项目结构

```
campus-platform/
├── docs/                             # 文档
│   ├── database.md                   # 数据库设计文档
│   └── init.sql                      # MySQL 初始化脚本
├── backend/                          # Spring Boot 后端
│   ├── pom.xml
│   ├── start.bat                     # Windows 启动脚本
│   └── src/main/java/com/campus/
│       ├── CampusApplication.java    # 启动入口
│       ├── admin/
│       │   └── AdminController.java  # 管理员 API（用户审批/帖子审批）
│       ├── common/
│       │   ├── Result.java           # 统一响应体
│       │   ├── exception/            # 全局异常处理
│       │   └── security/             # JWT 认证过滤器 & Token 提供者
│       ├── config/
│       │   ├── SecurityConfig.java   # Spring Security 配置（CORS 可配置）
│       │   ├── RedisConfig.java      # Redis 缓存配置
│       │   └── DataSeeder.java       # 默认管理员种子数据
│       ├── user/                     # 用户模块（注册/登录/审批）
│       ├── question/                 # 问答模块
│       ├── trade/                    # 二手交易模块
│       ├── post/                     # 论坛模块（富文本发帖/审核）
│       └── lostfound/               # 失物招领模块
│
├── frontend/                         # Vue 3 前端
│   ├── package.json
│   ├── vite.config.js                # 开发代理配置
│   ├── start.bat                     # Windows 启动脚本
│   └── src/
│       ├── App.vue                   # 主布局 + 导航栏
│       ├── main.js                   # 入口（注册 Element Plus / Router / Pinia）
│       ├── api/index.js              # Axios 封装 + JWT 拦截器
│       ├── router/index.js           # 前端路由
│       ├── stores/auth.js            # 用户认证状态管理
│       └── views/
│           ├── auth/                 # 登录 + 注册 + 忘记密码
│           ├── home/                 # 首页
│           ├── questions/            # 问答列表/详情/提问
│           ├── trades/               # 二手列表/详情/发布
│           ├── forum/                # 论坛列表/详情/发帖（富文本、搜索）
│           ├── lostfound/            # 失物招领列表/发布（搜索）
│           ├── admin/                # 管理后台（审核 + 仪表盘 + 趋势图）
│           ├── profile/              # 个人中心
│           └── NotFound.vue          # 404 页面
```

---

## 功能列表

### 一、用户系统

| # | 功能 | 说明 |
|---|------|------|
| 1.1 | 学生注册 | 填写学号、姓名、密码、手机号（中国手机号校验）、邮箱 |
| 1.2 | 教师注册 | 填写工号、姓名、密码、手机号、邮箱 |
| 1.3 | Tab 切换注册 | 注册页面「学生注册」/「教师注册」两个 Tab，表单联动切换 |
| 1.4 | 学生登录 | 凭学号 + 密码登录 |
| 1.5 | 教师登录 | 凭工号 + 密码登录 |
| 1.6 | Tab 切换登录 | 登录页面「学生登录」/「教师登录」两个 Tab |
| 1.7 | JWT 认证 | 登录后返回 Bearer Token，前端 Axios 拦截器自动携带 |
| 1.8 | 身份标签 | 导航栏显示「学生」/「教师」标签和姓名 |
| 1.9 | 用户审核 | 注册后 `enabled=false`，未通过管理员审批无法登录 |
| 1.10 | 个人中心 | 查看自己的身份、学号/工号、姓名、邮箱、手机号 |
| 1.11 | 忘记密码 | 输入注册邮箱 + 学号/工号，验证后返回新密码 |
| 1.12 | 修改密码 | 登录后提供原密码即可修改 |

### 二、问答互助

| # | 功能 | 说明 |
|---|------|------|
| 2.1 | 发布问题 | 标题 + 内容 + 标签（逗号分隔） |
| 2.2 | 回答问题 | 在问题详情页下方提交回答 |
| 2.3 | 采纳答案 | 提问者可采纳某条回答，问题标记为已解决 |
| 2.4 | 问题列表 | 分页展示，显示标题、标签、回答数、浏览数、时间 |
| 2.5 | 关键词搜索 | 按标题或内容搜索 |
| 2.6 | 浏览计数 | 每次查看详情自动 +1 |
| 2.7 | 删除问题 | 提问者可删除自己的问题 |

### 三、二手交易

| # | 功能 | 说明 |
|---|------|------|
| 3.1 | 发布商品 | 标题、描述、价格、分类、联系人、电话、位置 |
| 3.2 | 分类筛选 | 教材 / 电子产品 / 生活用品 / 服饰 / 体育 / 其他 |
| 3.3 | 关键词搜索 | 按标题或描述搜索 |
| 3.4 | 商品列表 | 卡片网格布局，显示标题、价格、分类、时间 |
| 3.5 | 商品详情 | 完整信息展示，含联系方式 |
| 3.6 | 状态管理 | 商品状态：在售 / 已下架，发布者可更新 |
| 3.7 | 删除商品 | 发布者可删除 |
| 3.8 | 浏览计数 | 每次查看详情自动 +1 |

### 四、论坛

| # | 功能 | 说明 |
|---|------|------|
| 4.1 | 富文本发帖 | 集成 VueQuill 编辑器，支持标题/加粗/列表/图片/引用等 |
| 4.2 | 帖子审核 | 发布后状态为 `PENDING`，管理员通过后才公开 |
| 4.3 | 帖子列表 | 仅展示已通过的帖子，卡片列表 + 分页 |
| 4.4 | 帖子详情 | 渲染 HTML 内容，显示浏览数，加载状态骨架 |
| 4.5 | 删除帖子 | 发帖者可删除 |
| 4.6 | 浏览计数 | 每次查看详情自动 +1 |
| 4.7 | 关键词搜索 | 按标题搜索帖子 |
| 4.8 | 举报帖子 | 非作者可举报，管理员在后台处理 |

### 五、失物招领

| # | 功能 | 说明 |
|---|------|------|
| 5.1 | 发布寻物 | 类型选「寻物」+ 标题、描述、地点、联系方式 |
| 5.2 | 发布招领 | 类型选「招领」+ 标题、描述、地点、联系方式 |
| 5.3 | 类型筛选 | 「全部」/「寻物」/「招领」Radio 切换 |
| 5.4 | 标记解决 | 发布者可标记为已解决 |
| 5.5 | 关键词搜索 | 按标题搜索失物招领信息 |
| 5.6 | 举报信息 | 非发布者可举报，管理员在后台处理 |

### 六、管理后台

| # | 功能 | 说明 |
|---|------|------|
| 6.1 | 用户审核 | 查看待审批用户列表（姓名、身份、学号/工号、邮箱、手机号、注册时间） |
| 6.2 | 审批用户 | 点击「通过」启用账号，点击「驳回」删除用户 |
| 6.3 | 帖子审核 | 查看待审批帖子列表（标题、内容预览、发布时间） |
| 6.4 | 审批帖子 | 点击「通过」公开帖子，点击「驳回」拒绝帖子 |
| 6.5 | 权限控制 | 导航栏仅管理员可见「管理后台」按钮；路由守卫校验 ADMIN 角色 |
| 6.6 | 数据统计 | 仪表盘展示用户/问题/二手/帖子/失物招领总数及待审数 |
| 6.7 | 趋势图表 | 近 7 天用户注册与发帖折线趋势图（ECharts LineChart） |
| 6.8 | 举报处理 | 查看待处理举报列表，标记已处理 |

### 七、安全与基础设施

| # | 功能 | 说明 |
|---|------|------|
| 7.1 | 密码加密 | BCrypt 加密存储 |
| 7.2 | JWT 携带角色 | Token 中包含用户角色，Spring Security 据此鉴权 |
| 7.3 | 全局异常处理 | 统一 `Result` 响应格式，覆盖 400/401/403/405/413/500 异常并记录日志 |
| 7.4 | 参数校验 | 后端 `@Valid` + `@Pattern` 校验，前端表单预检 |
| 7.5 | CORS 跨域 | 通过 `app.cors.allowed-origins` 配置允许的前端域名 |
| 7.6 | 数据库双模式 | 默认 H2（`ddl-auto: update` 持久化），可选 MySQL（生产） |
| 7.7 | 默认管理员 | 首次启动自动创建管理员账号（ADMIN001 / admin123） |
| 7.8 | Redis 缓存 | `@EnableCaching` 激活，首页统计和公告自动缓存，写入自动失效 |
| 7.9 | 404 页面 | 前端捕获所有无效路径，显示友好 404 页面 |

---

## 快速启动

### 环境要求
- JDK 21+
- Node.js 18+
- Maven 3.8+

### 启动后端（开发模式，H2 内存库）
```bash
cd backend
mvn package -DskipTests
java -jar target/campus-platform-1.0.0.jar
```
服务运行在 `http://localhost:8080`
H2 控制台：`http://localhost:8080/h2-console`

### 启动后端（生产模式，MySQL）
```bash
# 1. 确保 MySQL 已运行
# 2. 创建数据库（自动或手动 CREATE DATABASE campus;）
# 3. 修改 application-mysql.yml 中的用户名和密码
cd backend
mvn package -DskipTests
java -jar target/campus-platform-1.0.0.jar --spring.profiles.active=mysql
```

### 启动前端
```bash
cd frontend
npm install
npm run dev
```
服务运行在 `http://localhost:5173`，已配置代理转发 `/api` 到后端

### 默认管理员账号
| 字段 | 值 |
|------|-----|
| 身份 | 教师 |
| 工号 | ADMIN001 |
| 密码 | admin123 |

---

## 一键部署（Docker）

```bash
docker-compose up -d
```

包含 MySQL + Redis + 后端 + 前端 Nginx，启动后访问 `http://localhost`。

---

## API 文档

启动后端后访问 Knife4j 文档：`http://localhost:8080/doc.html`

---

## 开发历史

### v2.1.0 — 质量完善版
- Redis 缓存正式激活（`@EnableCaching` + `RedisConfig` + 首页/公告缓存）
- 全局异常处理覆盖 401/405/413 等，500 异常记录完整日志
- 文件上传增加 MIME 类型校验，所有异常改用 `BusinessException`
- CORS 域名改为 `app.cors.allowed-origins` 可配置
- H2 开发模式改为 `ddl-auto: update` 持久化数据
- 前端 admin 路由增加 `requiresRole` 角色守卫
- 新增忘记密码页面，登录页增加入口链接
- 论坛列表与失物招领列表增加关键词搜索
- 论坛帖子与失物招领详情增加举报功能
- 仪表盘增加近 7 天趋势折线图（LineChart）
- 详情页增加 loading 骨架状态
- 空 catch 块全部添加默认值处理
- 新增 404 页面与路由
- 401 重定向改用 `router.push` 代替 `location.href`
- `App.vue` 定时器添加 `onUnmounted` 清理
- 删除未使用的 `HelloWorld.vue` 和空组件目录
- 后端增加 `PostRepository`/`LostFoundItemRepository` 时间范围查询方法

### v2.0.0 — 毕业设计完善版
- XSS 防护（Jsoup HTML 白名单过滤）
- 点赞去重（Like 表 + toggle 模式）
- 登录/注册 IP 级别速率限制
- 图片上传（支持多图，前端 el-upload 组件）
- 管理后台数据统计仪表盘（ECharts 图表）
- 评论系统扩展到问答和二手交易
- 举报功能（用户举报 + 管理员处理）
- 密码重置（邮箱验证）
- 认证边界修复（写操作强制登录）
- Loading 状态修复
- Element Plus 按需引入
- Knife4j API 文档
- Redis 缓存支持
- Logback 日志框架（按天滚动）
- Docker 一键部署
- 单元测试（JUnit 5 + Mockito）

### v1.0.0 — 初始版本
- 搭建 Spring Boot + Vue 3 项目骨架
- 实现 JWT 认证
- 实现问答、二手交易、失物招领三大模块基本 CRUD

### 学生/教师身份分离
- User 实体增加 `userType`（STUDENT/TEACHER）、`studentId`、`teacherId` 字段
- 注册页面 Tab 切换学生/教师
- 登录页面 Tab 切换，凭学号/工号登录
- 前端展示身份标签

### 手机号必填 + 校验
- 注册时手机号改为必填
- 后端 `@Pattern(regexp = "^1[3-9]\\d{9}$")` 校验
- 前端表单提交前校验

### 审核系统
- 注册后 `enabled=false`，登录拦截提示"账号待管理员审核"
- 新增 `AdminController`：`GET /api/admin/users/pending`、`PUT approve`、`PUT reject`
- JWT Token 携带角色信息，Spring Security 权限拦截
- 启动时种子数据创建默认管理员（admin/ADMIN001）
- 前端管理后台页面，导航栏管理员可见"管理后台"按钮

### 论坛模块
- 新增 `Post` 实体（支持富文本 HTML 内容）
- 发帖 → PENDING → 管理员审批 → APPROVED → 公开可见
- 安装 VueQuill 实现富文本编辑
- 前端论坛列表/详情/发帖页面
- 管理后台新增"帖子审核"Tab
- SecurityConfig 放行 `/api/posts/**` 公开访问

### MySQL 支持 + 数据库文档
- 添加 MySQL Connector 依赖
- 创建 `application-mysql.yml` 配置
- 支持 `--spring.profiles.active=mysql` 切换
- 编写完整数据库设计文档 `docs/database.md`
- 生成 MySQL 初始化脚本 `docs/init.sql`

---

## API 接口概览

### 认证
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 注册 |
| POST | `/api/auth/login` | 登录 |
| POST | `/api/auth/reset-password` | 忘记密码（验证邮箱 + 学号/工号） |
| GET | `/api/users/me` | 当前用户信息 |

### 问答
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/questions` | 问题列表 |
| POST | `/api/questions` | 提问 |
| GET | `/api/questions/{id}` | 问题详情 |
| POST | `/api/questions/{id}/answers` | 回答 |
| GET | `/api/questions/{id}/answers` | 回答列表 |
| POST | `/api/questions/{id}/answers/{aid}/accept` | 采纳答案 |

### 二手交易
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/trades` | 商品列表 |
| POST | `/api/trades` | 发布商品 |
| GET | `/api/trades/{id}` | 商品详情 |
| PUT | `/api/trades/{id}/status` | 更新状态 |
| DELETE | `/api/trades/{id}` | 删除商品 |

### 论坛
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/posts` | 已通过帖子列表（支持 `?keyword=` 搜索） |
| POST | `/api/posts` | 发帖（需登录） |
| GET | `/api/posts/{id}` | 帖子详情 |
| DELETE | `/api/posts/{id}` | 删除帖子 |

### 失物招领
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/lost-found` | 列表（支持 `?type=&keyword=` 筛选搜索） |
| POST | `/api/lost-found` | 发布 |
| GET | `/api/lost-found/{id}` | 详情 |
| PUT | `/api/lost-found/{id}/resolve` | 标记解决 |
| DELETE | `/api/lost-found/{id}` | 删除 |

### 管理后台
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/users/pending` | 待审核用户 |
| PUT | `/api/admin/users/{id}/approve` | 通过用户 |
| PUT | `/api/admin/users/{id}/reject` | 驳回用户 |
| GET | `/api/admin/posts/pending` | 待审核帖子 |
| PUT | `/api/admin/posts/{id}/approve` | 通过帖子 |
| PUT | `/api/admin/posts/{id}/reject` | 驳回帖子 |
| GET | `/api/admin/dashboard` | 数据统计仪表盘 |
| GET | `/api/admin/dashboard/timeline` | 近 7 天趋势数据 |
| GET | `/api/admin/reports/pending` | 待处理举报 |
| PUT | `/api/admin/reports/{id}/resolve` | 处理举报 |

---

## 数据库设计

详见 [docs/database.md](docs/database.md)

**6 张表概要：**

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| `users` | 用户表 | username, user_type, student_id, teacher_id, enabled |
| `questions` | 问题表 | title, content, tags, user_id, resolved |
| `answers` | 回答表 | content, question_id, user_id, accepted |
| `trade_items` | 二手交易表 | title, price, category, status, user_id |
| `posts` | 论坛帖表 | title, content(HTML), user_id, status |
| `lost_found_items` | 失物招领表 | type, title, location, user_id |

建表脚本：[docs/init.sql](docs/init.sql)
