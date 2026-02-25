# 智养云（智慧养老OA）数据库说明

## 假设
- 多租户 `tenant_id` 在 M1 资产与房间管理阶段先对楼栋/楼层/房间/床位与审计日志表落地，`tenant_id` 取当前 `org_id`；其它业务表在后续模块迭代中逐步补齐。
- 若数据库已存在基础表结构，Flyway 采用 `baselineVersion=0` 进行迁移，V1 会补齐 M1 所需字段与表。
- 入院办理会自动初始化老人财务账户与商城积分账户；护理套餐需在入院后由业务人员选择配置（未自动分配默认套餐）。

## 数据库结构
- 以总库为准：`zhi_yang_yun_schema.sql`
- 子模块不再单独维护 SQL

## 使用方式
- 初始化数据库时直接执行 `zhi_yang_yun_schema.sql`

## 本地环境初始化（Docker + 后端）
### 1) 启动数据库与缓存
```
docker compose up -d
```

### 2) 初始化数据库结构与测试数据（UTF-8）
```
docker exec -i smartcare-mysql mysql -uroot -proot zhiyangyun < docker/mysql/init/1_schema.sql
docker exec -i smartcare-mysql mysql --default-character-set=utf8mb4 -uroot -proot zhiyangyun < docker/mysql/init/2_data.sql
```

如需重置数据（测试环境）：
```
docker exec -it smartcare-mysql mysql -uroot -proot -e "
SET FOREIGN_KEY_CHECKS=0;
TRUNCATE TABLE zhiyangyun.room;
TRUNCATE TABLE zhiyangyun.bed;
TRUNCATE TABLE zhiyangyun.elder;
TRUNCATE TABLE zhiyangyun.org;
TRUNCATE TABLE zhiyangyun.department;
TRUNCATE TABLE zhiyangyun.role;
TRUNCATE TABLE zhiyangyun.staff;
TRUNCATE TABLE zhiyangyun.staff_role;
TRUNCATE TABLE zhiyangyun.care_task_template;
TRUNCATE TABLE zhiyangyun.care_task_daily;
TRUNCATE TABLE zhiyangyun.care_task_execute_log;
TRUNCATE TABLE zhiyangyun.disease;
TRUNCATE TABLE zhiyangyun.product_tag;
TRUNCATE TABLE zhiyangyun.disease_forbidden_tag;
TRUNCATE TABLE zhiyangyun.elder_disease;
TRUNCATE TABLE zhiyangyun.product;
TRUNCATE TABLE zhiyangyun.elder_points_account;
TRUNCATE TABLE zhiyangyun.inventory_batch;
TRUNCATE TABLE zhiyangyun.vital_threshold_config;
SET FOREIGN_KEY_CHECKS=1;
"
```

### 3) 启动后端
```
mvn spring-boot:run
```

### 4) 冒烟验证（示例）
登录：
```
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"123456"}'
```

注意：
- MySQL 端口使用 `3307`（见 `docker-compose.yml`），应用已配置为 `127.0.0.1:3307`。
- 如需重新初始化容器数据卷：`docker compose down -v` 后再执行上述步骤。

## 常见问题排查
### 1) 3306 端口冲突
如果本机已有 MySQL 占用 3306，Docker MySQL 会冲突。当前已将 Docker 映射到 3307：
```
docker compose ps
```
确认 MySQL 端口为 `3307:3306`，并确保 `application.yml` 中 JDBC 指向 `127.0.0.1:3307`。

### 2) 中文乱码
通常是导入数据时字符集不一致导致。请使用 UTF‑8 导入：
```
docker exec -i smartcare-mysql mysql --default-character-set=utf8mb4 -uroot -proot zhiyangyun < docker/mysql/init/2_data.sql
```
如果已导入过，请先清表再导入（见上面的“重置数据”步骤）。

### 3) root 账号无法连接
如果出现 `Access denied for user 'root'@'localhost'`：
```
docker exec smartcare-mysql mysql -uroot -proot -e "SELECT 1;"
```
若此命令失败，说明容器初始化时 root 密码不是 `root`，需要重建容器数据卷：
```
docker compose down -v
docker compose up -d
```

## OpenAPI 导出
启动服务后可通过以下接口导出 OpenAPI：
- JSON：`http://localhost:8080/v3/api-docs`
- YAML：`http://localhost:8080/v3/api-docs.yaml`

示例导出（JSON）：
```
curl -s http://localhost:8080/v3/api-docs -o openapi.json
```

## Postman 闭环测试
已提供 Postman 集合与环境：
- `postman_zhiyangyun_e2e.collection.json`
- `postman_zhiyangyun_e2e.environment.json`

使用步骤：
1) Postman → Import → 选择上述两个 JSON  
2) 选择环境 `zhiyangyun-local`  
3) 先运行 `Auth/Login`（会自动写入 `token`）  
4) 按顺序运行其它请求完成业务闭环测试

## 部署（生产）
参考：
- `src/main/resources/application-prod.yml`
- `docker-compose.prod.yml`
- `DEPLOYMENT_CHECKLIST.md`

已验证部署成功（登录接口返回 `code=0`）。

## Flyway 版本冲突检查
多人并行开发时，建议在提交前执行：
```
./scripts/check_flyway_versions.sh
```
如果输出 `Duplicate Flyway versions found`，需要先统一迁移版本号后再合并。
