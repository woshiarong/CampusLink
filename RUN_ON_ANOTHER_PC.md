# 跨电脑运行环境配置指南

本文用于把项目迁移到另一台电脑并成功运行（Windows/macOS/Linux）。

## 1. 先清理不需要携带的文件

不需要拷贝到新电脑的目录：

- `frontend/node_modules`
- `frontend/dist`
- `backend/target`
- `.idea`
- `.vscode`

项目根目录已配置 `.gitignore`，这些目录不会影响源码运行。

> 若你是直接拷贝文件夹（不是 git clone），建议先执行 `scripts/cleanup-portable.ps1`（Windows）进行清理。

## 2. 运行环境要求

- JDK 17
- Maven 3.8+
- Node.js 18+（建议 20 LTS）
- MySQL 8.x

## 3. 数据库初始化

在 MySQL 中执行以下 SQL（按顺序）：

1. `database/init.sql`
2. `database/V2_admin_schema.sql`
3. `database/V3_user_features.sql`

创建数据库示例：

```sql
CREATE DATABASE campus_forum DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

## 4. 后端配置

后端配置文件：`backend/src/main/resources/application.yml`

该文件已使用环境变量，不含硬编码绝对路径。可按需设置：

- `SERVER_PORT`（默认 `8080`）
- `DB_HOST`（默认 `localhost`）
- `DB_PORT`（默认 `3306`）
- `DB_NAME`（默认 `campus_forum`）
- `DB_USER`（默认 `root`）
- `DB_PASSWORD`（默认 `root`）
- `JWT_SECRET_KEY`（建议改成你自己的随机串）

Windows PowerShell 示例：

```powershell
$env:DB_HOST="127.0.0.1"
$env:DB_PORT="3306"
$env:DB_NAME="campus_forum"
$env:DB_USER="root"
$env:DB_PASSWORD="你的密码"
$env:JWT_SECRET_KEY="your-jwt-secret"
```

## 5. 启动后端

```bash
cd backend
mvn spring-boot:run
```

默认地址：`http://localhost:8080`

## 6. 启动前端

```bash
cd frontend
npm install
npm run dev
```

默认地址：`http://localhost:5173`

前端通过 Vite 代理访问后端 `/api`，保持前后端同时运行即可。

## 7. 常见问题排查

- **数据库连不上**：确认 MySQL 已启动，账号密码和库名正确。
- **端口冲突**：改 `SERVER_PORT` 或 Vite 端口。
- **上传文件路径**：后端使用相对路径配置（`forum.upload-dir`），不依赖本机绝对路径。
- **通知/消息不显示**：确认后端已重启（新增接口或服务变更后需重启）。

## 8. 建议迁移方式

优先使用 git：

```bash
git clone <your-repo-url>
```

然后按第 5、6 步重新安装依赖并启动。

