# 智养云 备份与容灾操作手册（Backup & DR Runbook）

> 版本：v1.0　编制日期：2026-07-06
> 适用范围：智养云生产/预生产环境 MySQL 8 数据库的备份、恢复、恢复演练，以及上线前安全配置检查。
> 关联：`docs/PLATFORM_ENHANCEMENT_MASTER_PLAN.md` 领域 F（合规与数据安全）；脚本位于 `scripts/db_backup.sh`、`scripts/db_restore_drill.sh`。

---

## 1. 备份策略

| 项 | 策略 |
|----|------|
| 备份方式 | `mysqldump --single-transaction`（InnoDB 一致性快照，不锁业务表） |
| 频率 | 每日 1 次（建议凌晨 01:30，避开 01:10 驾驶舱快照与 02:40 日志清理任务） |
| 本地保留 | 近 14 份（`RETAIN_COUNT` 可调） |
| 异地容灾 | 备份产物上传对象存储（OSS/S3，脚本内有注释占位），建议异地 Bucket + 版本控制 + 30 天以上生命周期 |
| 加密 | 备份落盘目录限 `600/700` 权限；上传对象存储开启服务端加密（SSE） |
| 演练 | 每月至少 1 次恢复演练（`db_restore_drill.sh`），演练记录留档备查（等保测评项） |

## 2. 日常备份

```bash
# 手动执行一次备份
DB_HOST=127.0.0.1 DB_PORT=3307 DB_USER=root DB_PASSWORD='***' DB_NAME=zhiyangyun \
BACKUP_DIR=/var/backups/zhiyangyun ./scripts/db_backup.sh
```

crontab 配置（生产建议放 `/etc/cron.d/smartcare-backup`）：

```cron
# 每日 01:30 数据库备份（密码建议放 /etc/smartcare/backup.env，权限 600，通过 . 引入）
30 1 * * * root . /etc/smartcare/backup.env && /opt/smartcare/scripts/db_backup.sh >> /var/log/smartcare/db_backup.log 2>&1
```

备份成功判定：日志出现 `备份完成` 且 `gzip -t` 未报错；建议接入监控（日志关键字或备份文件 mtime 超过 26 小时告警）。

## 3. 恢复演练（每月）

```bash
# 使用最近一份备份恢复到临时库 zhiyangyun_drill_YYYYMMDD_HHMMSS 并校验
DB_PASSWORD='***' ./scripts/db_restore_drill.sh

# 指定备份文件、演练后保留临时库供人工抽查
DB_PASSWORD='***' KEEP_DRILL_DB=1 ./scripts/db_restore_drill.sh /var/backups/zhiyangyun/zhiyangyun_20260706_013000.sql.gz
```

脚本自动校验：

1. 恢复库表数量 >= 源库表数量的 95%（可用 `MIN_TABLE_COUNT` 覆盖）；
2. 关键表（`org` / `staff` / `elder` / `flyway_schema_history`）行数抽查输出。

演练完成后在运维台账记录：日期、备份文件、表数、耗时、执行人、结论。

## 4. 真实恢复流程（事故场景）

1. **止血**：停止应用写入（下线应用或置维护页），防止脏数据扩散；
2. **定位备份**：选择事故时间点之前最近的备份文件，先跑一次 `db_restore_drill.sh <file>` 在临时库验证完整性；
3. **恢复**：
   ```bash
   # 恢复到新库再切换（推荐，可回退），不要直接覆盖原库
   mysql -h... -uroot -p -e "CREATE DATABASE zhiyangyun_restore DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
   gunzip -c <backup.sql.gz> | mysql -h... -uroot -p zhiyangyun_restore
   ```
4. **校验**：核对 `flyway_schema_history` 最后版本与应用期望版本一致；抽查业务关键表；
5. **切换**：修改应用 `DB_NAME`（或库改名）指向恢复库，启动应用回归冒烟；
6. **复盘**：记录 RPO（数据丢失窗口 = 事故时间 - 备份时间）与 RTO，评估是否需要加密备份频率或开启 binlog 增量。

> 如需把 RPO 压到分钟级：在每日全量之外开启 binlog（`log_bin`），恢复时“全量 + binlog 重放到时间点”。

## 5. 生产上线必改项清单（密钥与默认值风险）

以下默认值仅供本地开发，**生产环境必须替换/关闭**（多数在 `.env` / 环境变量中覆盖）：

| 配置项 | 当前默认值 | 风险 | 生产要求 |
|--------|-----------|------|----------|
| `JWT_SECRET`（`security.jwt.secret`） | `smartcare-local-dev-jwt-secret-32-bytes`（`.env.example` 与 `application.yml` 兜底值相同） | 任何人可伪造登录令牌 | 必改为 ≥32 位随机串；`SecurityStartupValidator` 在 prod profile 下会拒绝默认值/短密钥启动 |
| `DB_PASSWORD` / `MYSQL_ROOT_PASSWORD` | `app123456` / `root`（`.env.example`） | 弱口令 | 必改强口令，限制来源 IP |
| `app.family.sms-code.debug-return-code` | `true` | 接口响应直接回显短信验证码，2FA/家属登录形同虚设 | 必须 `false` |
| `app.family.sms-code.provider` | `mock`（验证码固定 `1234`） | 同上 | 必须切换为 `aliyun` 并配置 AK/SK、签名与模板 |
| `app.family.sms-code.code-salt` | `smartcare-family-sms` | 验证码哈希可被离线碰撞 | 必改随机盐 |
| `app.family.wechat-pay.skip-notify-signature-verify` | `true` | 支付回调不验签，可伪造支付成功 | 必须 `false`，并配置平台证书 |
| `app.family.wechat-pay.*` / `wechat-notify.*` 密钥 | 空字符串 | 未配置即启用会失败或被绕过 | 生产启用前配置商户号/证书/APIv3 Key，密钥走密钥托管（KMS/环境变量），禁止入库入 git |
| `VITE_ENABLE_DEMO_LOGIN` | 未设置（DEV 下显示演示账号面板） | 演示账号/默认密码泄露入口 | 生产构建不得设置为 `true` |
| `DEMO_SEED_ENABLED` | `false` | 演示种子数据 | 生产保持 `false` |
| `zhiyangyun.staff-mobile.demo-fallback` | `false` | 演示兜底数据 | 生产保持 `false` |

上线前检查命令（快速 grep 默认值）：

```bash
grep -RnE "smartcare-local-dev-jwt-secret|app123456|debug-return-code: true|skip-notify-signature-verify: true" \
  .env src/main/resources/application*.yml || echo "OK: 未发现默认密钥/危险开关"
```

## 6. 与日志留存策略的关系

- 审计日志（`audit_log`）、敏感访问日志（`compliance_sensitive_access_log`）、登录日志（`auth_login_log`）、导出留痕（`compliance_export_log`）由定时任务 `ComplianceLogRetentionTask`（默认每日 02:40）按机构策略清理，**硬编码下限 180 天**（等保 2.0 要求 ≥6 个月）；
- 留存天数在管理端「安全策略配置」页按机构调整（`compliance_security_policy.log_retention_days`，默认 400 天）；
- 数据库备份保留策略与日志留存互补：日志清理前的数据仍可通过历史备份追溯。
