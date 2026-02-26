# 阿里云 ECS 部署指南（Docker Compose）

适用文件：`docker-compose.aliyun.yml`

## 1. 服务器准备
- ECS 建议：`2C4G` 起，Ubuntu 22.04
- 安全组放行：`22`、`80`（如启用 HTTPS 再放行 `443`）
- 代码目录示例：`/opt/smartcare`

## 2. 安装 Docker
```bash
sudo apt update
sudo apt install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo \"$VERSION_CODENAME\") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo usermod -aG docker $USER
newgrp docker
```

## 3. 拉取代码并配置环境变量
```bash
cd /opt
git clone <repo-url> smartcare
cd smartcare
cp .env.aliyun.example .env.prod
```

编辑 `.env.prod`：
- `DB_USER`
- `DB_PASSWORD`
- `MYSQL_ROOT_PASSWORD`
- `JWT_SECRET`

## 4. 启动服务
```bash
docker compose --env-file .env.prod -f docker-compose.aliyun.yml up -d --build
```

查看状态：
```bash
docker compose -f docker-compose.aliyun.yml ps
docker logs -f smartcare-backend
```

## 5. 验证
容器编排中前端 Nginx 暴露 `80`，并反代 `/api` 到后端。

登录验证：
```bash
curl -s -X POST http://127.0.0.1/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"123456"}'
```

返回应包含：
- `code=0`
- `data.token` 非空
- `data.roles` 为数组

浏览器访问：`http://<ECS公网IP>`

## 6. 数据初始化说明
- `docker-compose.aliyun.yml` 中 MySQL 仅挂载 `docker/mysql/init/1_schema.sql`。
- 如需初始业务数据，请在部署后按需手动导入，不建议导入测试数据到生产。

## 7. 更新发布
```bash
cd /opt/smartcare
git pull
docker compose --env-file .env.prod -f docker-compose.aliyun.yml up -d --build
```

## 8. 回滚
```bash
cd /opt/smartcare
git checkout <tag-or-commit>
docker compose --env-file .env.prod -f docker-compose.aliyun.yml up -d --build
```

## 9. 生产建议
- 使用 HTTPS（Nginx + 证书）
- 对 `output/uploads` 做持久化与备份
- 开启容器日志采集与告警
