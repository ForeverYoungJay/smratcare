# 阿里云 ECS 部署指南（Docker Compose）

适用当前仓库：`/Users/yangjiyi/Downloads/smratcare`

## 1. 阿里云资源准备
- 1 台 ECS（建议 `2C4G` 起，系统 Ubuntu 22.04）
- 安全组仅放行：`22`、`80`（如需 HTTPS 再放行 `443`）
- 绑定公网 IP

## 2. 服务器安装 Docker
```bash
sudo apt update
sudo apt install -y ca-certificates curl gnupg
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo \"$VERSION_CODENAME\") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo usermod -aG docker $USER
newgrp docker
```

## 3. 上传代码并准备环境变量
```bash
cd /opt
sudo git clone <你的仓库地址> smartcare
sudo chown -R $USER:$USER /opt/smartcare
cd /opt/smartcare
cp .env.aliyun.example .env.prod
```

编辑 `.env.prod`，替换为真实强密码。

## 4. 启动生产容器
```bash
cd /opt/smartcare
docker compose --env-file .env.prod -f docker-compose.aliyun.yml up -d --build
```

查看状态：
```bash
docker compose -f docker-compose.aliyun.yml ps
docker logs -f smartcare-backend
```

## 5. 验证服务
```bash
curl -s -X POST http://127.0.0.1/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"123456"}'
```

如果 ECS 公网 IP 是 `47.x.x.x`，浏览器访问：`http://47.x.x.x`

## 6. HTTPS（推荐）
建议在 ECS 上再加一层 Nginx + Certbot，把 `443` 反代到 `127.0.0.1:80`，并开启自动续签。

## 7. 生产注意事项
- 当前 `docker-compose.aliyun.yml` 仅初始化数据库结构：`1_schema.sql`，不会导入 `2_data.sql/3_pagination_mock.sql` 等测试数据。
- `JWT_SECRET` 必须替换成 32 位以上随机字符串。
- 不要在安全组放开 `3306/6379/8080`。
- 上传文件目录在宿主机：`/opt/smartcare/output/uploads`，请纳入备份。
- 首次部署后建议立即修改默认管理员密码。

## 8. 更新发布
```bash
cd /opt/smartcare
git pull
docker compose --env-file .env.prod -f docker-compose.aliyun.yml up -d --build
```

## 9. 回滚（按 commit/tag）
```bash
cd /opt/smartcare
git checkout <commit_or_tag>
docker compose --env-file .env.prod -f docker-compose.aliyun.yml up -d --build
```
