# 测试记录（TESTING）

## 环境
- Java: 17
- Spring Boot: 3.2.6
- MySQL: 8.x（Docker）
- Redis: 7.2（Docker）
- 端口：MySQL 3307 → 容器 3306；应用 8080

## 初始化步骤
1) 启动容器
```
docker compose up -d
```

2) 初始化结构与数据（UTF-8）
```
docker exec -i smartcare-mysql mysql -uroot -proot zhiyangyun < docker/mysql/init/1_schema.sql
docker exec -i smartcare-mysql mysql --default-character-set=utf8mb4 -uroot -proot zhiyangyun < docker/mysql/init/2_data.sql
```

3) 启动后端
```
mvn spring-boot:run
```

## 测试执行
- 单元/集成测试：
```
mvn test
```
- Postman E2E 集合：
  - `postman_zhiyangyun_e2e.collection.json`
  - `postman_zhiyangyun_e2e.environment.json`

## 结果
- 全部测试通过（BUILD SUCCESS）
- 冒烟流程通过：登录、护理任务、商城禁忌预览/下单、探视、生命体征、账单生成

## 生产部署验证
- 部署方式：`docker-compose.prod.yml`
- 结果：后端容器启动成功，登录接口返回 `code=0`

## 常见问题
- 3306 端口冲突：改用 3307（见 `docker-compose.yml` 和 `application.yml`）
- 中文乱码：使用 `--default-character-set=utf8mb4` 导入数据
- 账号认证失败：确保测试数据哈希为 `$2a$` 格式
