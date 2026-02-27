# 测试说明（TESTING）

## 测试栈
- JUnit 5 + Spring Boot Test
- H2 内存数据库（`src/test/resources/application-test.yml`）
- Flyway H2 迁移目录：`src/test/resources/db/migration/h2`

## 执行方式
在仓库根目录运行：
```bash
mvn test
```

仅执行单个测试类：
```bash
mvn -Dtest=AuthSecurityTest test
```

## 当前测试范围（按代码现状）
- 认证与权限：`AuthSecurityTest`、`FamilyAuthTest`
- 长者与入住流转：`ElderServiceTest`、`ElderResidenceFlowTest`
- 护理与健康：`CareTaskServiceTest`、`HealthWorkflowIntegrationTest`、`VitalSignServiceTest`
- 商城与物资中心：`StoreOrderServiceTest`、`InventoryFlowTest`、`InventoryAlertTest`
- 财务与费用：`FinancePaymentTest`、`FinanceReconcileTest`、`FeeManagementServiceTest`、`FeeManagementControllerTest`
- 评估与统计：`AssessmentApiIntegrationTest`、`StatisticsControllerTest`、`MarketingReportServiceTest`
- 其它模块：`VisitModuleTest`、`DiseaseRuleServiceTest` 等

## 前置依赖
- 后端单元/集成测试默认使用 H2，不依赖本地 MySQL。
- 若测试用例涉及 Redis 行为，请先确认本地 `localhost:6379` 可用。

## 建议流程
1. 提交前执行 `mvn test`。
2. 涉及 Flyway 迁移时，额外执行 `./scripts/check_flyway_versions.sh`。
3. 联调改动后，补充关键接口冒烟（登录、核心业务写接口）。
