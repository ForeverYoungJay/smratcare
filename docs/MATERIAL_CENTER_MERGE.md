# 物资中心合并与联动方案（已落地）

## 1. 合并结果
前端菜单从三块口径收敛为两块：
- `物资中心`（原 物资管理 + 库存管理）
- `商城`（原 商城与库存，库存能力已剥离到物资中心）

本次变更后：
- `物资中心`承载主数据、采购、调拨、入出库、盘点、预警、库存金额。
- 旧路由`/inventory/*`保留兼容，统一重定向到`/material/*`，避免历史链接失效。

## 2. 主数据与台账模型（后端现状）
统一数据模型已在 `store` 域中闭环：
- 主数据：`product`（SKU/物资）、`material_warehouse`（仓库）、`material_supplier`（供应商）
- 库存余额：`inventory_batch`
- 库存流水：`inventory_log`
- 业务单据：`material_purchase_order(_item)`、`material_transfer_order(_item)`
- 盘点：`inventory_adjustment`

## 3. API 合并口径（接口字段按当前代码）

### 3.1 物资主数据与单据（`/api/material`）
- 仓库：`GET /warehouse/page`、`POST /warehouse`、`PUT /warehouse/{id}`、`DELETE /warehouse/{id}`
- 供应商：`GET /supplier/page`、`POST /supplier`、`PUT /supplier/{id}`、`DELETE /supplier/{id}`
- 采购：`GET /purchase/page`、`GET /purchase/{id}`、`POST /purchase`、`PUT /purchase/{id}`、`POST /purchase/{id}/approve|complete|cancel`
- 调拨：`GET /transfer/page`、`GET /transfer/{id}`、`POST /transfer`、`PUT /transfer/{id}`、`POST /transfer/{id}/complete|cancel`
- 库存金额：`GET /stock/amount?dimension=PRODUCT|WAREHOUSE|CATEGORY`

### 3.2 库存执行与预警（`/api/inventory`）
- 执行：`POST /inbound`、`POST /outbound`、`POST /adjust`
- 查询：`GET /batch/page`、`GET /log/page`、`GET /inbound/page`、`GET /outbound/page`
- 盘点：`GET /adjustment/page`、`GET /adjustment/diff-report`
- 预警：`GET /alerts`、`GET /expiry-alerts`

说明：`/api/inventory`保留为库存执行域，`/api/material`保留为物资主数据与单据域，两者共用同一套库存余额与流水表。

### 3.3 物资中心聚合（`/api/material-center`）
- 总览：`GET /overview?expiryDays=30`
  - 返回：`lowStockCount`、`expiryAlertCount`、`materialPurchaseDraftCount`、`materialTransferDraftCount`

## 4. 与其他模块联动（当前代码已存在）
- OA 门户：统计低库存预警、采购草稿、调拨草稿（`OaPortalController`）
- 运营看板：库存预警指标（`DashboardController`）
- 账单：商城消费计入月账单（`BillServiceImpl` 中 `SHOP` 项）
- 审批：支持 `MATERIAL_APPLY` 物资申请审批类型（`OaApprovalController`）

## 5. 前端路由映射（兼容）
- `/inventory/overview` -> `/material/stock-query`
- `/inventory/inbound` -> `/material/inbound`
- `/inventory/outbound` -> `/material/outbound`
- `/inventory/alerts` -> `/material/alerts`
- `/inventory/adjustments` -> `/material/stock-check`

## 6. 前端 API 收敛（已完成）
- 已新增统一前端 API：`admin-web/src/api/materialCenter.ts`
- 已将物资中心页面从 `material.ts` / `inventory.ts` 切换到 `materialCenter.ts`
- 已删除冗余文件：`admin-web/src/api/material.ts`、`admin-web/src/api/inventory.ts`

## 7. 后续建议（可选）
- 可继续在后端新增聚合接口 `GET /api/material-center/overview`，把库存预警、采购草稿、调拨草稿做成单接口返回，减少前端多次请求。
