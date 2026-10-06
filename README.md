# DND 角色生成器

基于 D&D（龙与地下城）规则的角色卡自动生成器，支持随机自动生成与玩家手动创建角色，并可导出 xlsx 人物卡。

## 技术栈

- 后端：Spring Boot 3 + Spring Data JPA
- 前端：Vue 3 + Vite
- 数据库：MySQL

## 功能特性

- 随机自动生成角色 / 玩家手动创建角色
- 属性投骰公式自定义（`adb`、`adbdc`、`adbdcde`）
- 种族、亚种、职业、子职、背景、阵营、姓名等完整角色流程生成
- 人物卡以 xlsx 格式导出

## 目录结构

```
dndhero/
├── backend/          # Spring Boot 后端
│   └── src/main/
│       ├── java/com/dndhero/
│       │   ├── controller/   # REST 接口
│       │   ├── model/        # 实体 / DTO
│       │   ├── repository/   # JPA 仓库
│       │   ├── service/      # 业务逻辑
│       │   └── util/         # 骰子解析等工具
│       └── resources/
│           └── application.yml
├── frontend/         # Vue 3 + Vite 前端
│   └── src/
│       ├── api/          # 后端接口封装
│       └── components/   # 角色卡等组件
└── _localhost-*.dump.sql   # MySQL 数据库结构及数据导出
```

## 环境要求

- JDK 17+（推荐 21）
- Maven 3.6+
- Node.js 18+
- MySQL 8.x

## 数据库初始化

1. 创建数据库 `dnd2`：

```sql
CREATE DATABASE dnd2 DEFAULT CHARACTER SET utf8mb4;
```

2. 导入仓库根目录的数据库导出文件：

```bash
mysql -uroot -p dnd2 < _localhost-*.dump.sql
```

3. 在 [backend/src/main/resources/application.yml](backend/src/main/resources/application.yml) 中修改数据库连接账号密码（默认 `root` / `123`）。

## 启动方式

### 后端（默认端口 8080）

```bash
cd backend
mvn spring-boot:run
```

或打包后运行：

```bash
mvn package -DskipTests
java -jar target/dndhero-backend-1.0.0.jar
```

### 前端（默认端口 5173）

```bash
cd frontend
npm install
npm run dev
```

浏览器访问 http://localhost:5173 ，前端已将 `/api` 请求代理到后端 `8080` 端口。

## 特殊骰子表达式

`a`、`b`、`c`、`e` 为数字：

| 表达式 | 含义 |
| --- | --- |
| `adb` | a 次随机，每次在 1~b 之间，求和 |
| `adbdc` | a 次随机，每次在 1~b 之间，去掉最小 c 个，求和 |
| `adbdcde` | a 次随机，每次在 1~b 之间，去掉最小 c 个和最大 e 个，求和 |

默认属性公式为 `5d6d1d1`。
