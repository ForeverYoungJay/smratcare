# Family 旧接口下线计划

## 目标范围
- 旧接口：`POST /api/family/bindElder`
- 旧接口：`GET /api/family/my-elders`
- 替代接口：`POST /api/family/bindings`、`GET /api/family/bindings`

## 当前状态（已实施）
- 旧接口已标记 `@Deprecated(forRemoval = true, since = "2026-03")`。
- 后台管理端绑定入口已迁移至 `POST /api/admin/family/relations/bind`，不再依赖旧家属接口。
- 调用旧接口时，响应头返回：
  - `Deprecation: true`
  - `Sunset: <app.family.legacy-api-sunset-date>`
  - `Link: </api/family/bindings>; rel="successor-version"`
  - `X-Deprecated-Reason: Use family aggregation APIs`
- 调用日志会输出告警，便于统计存量调用方。

## 渐进下线机制
- 配置项：`app.family.legacy-api-enabled`
  - `true`：允许调用（保留废弃提示）
  - `false`：直接返回 `410 Gone`
- 配置项：`app.family.legacy-api-sunset-date`
  - 达到该日期后，旧接口自动返回 `410 Gone`（即使 `legacy-api-enabled=true`）

## 建议节奏
1. 第 1 阶段（迁移窗口）：保持 `legacy-api-enabled=true`，收集调用日志并推动客户端改造。
2. 第 2 阶段（冻结窗口）：将 `legacy-api-enabled=false` 于灰度环境验证。
3. 第 3 阶段（正式下线）：生产置 `legacy-api-enabled=false`，并在下个版本移除控制器方法。

## 验收标准
- 家属端小程序与第三方接入均改用 `/api/family/bindings`。
- 管理端（admin-web）已切换到 `POST /api/admin/family/relations/bind`。
- 旧接口 7 天内无有效调用日志。
- `410 Gone` 下线行为在测试与生产一致。
