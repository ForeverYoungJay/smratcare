# 企业首页内容治理清单（运营可直接维护）

> 目标：让运营同事无需改页面结构，只改配置即可更新企业首页内容。  
> 配置文件：`admin-web/src/constants/enterpriseProfile.ts`

## 1. 每周更新（建议）

- `newsList`：新闻资讯（标题、日期、标签、摘要）
- `communityUpdates`：社区动态
- `residentActivities`：居民活动排期

## 2. 每月更新（建议）

- `quickStats`：核心运营指标口径
- `jobs`：招聘岗位与要求
- `cooperation`：业务合作方向

## 3. 变更即更新（强制）

- `contact`：电话、邮箱、地址、参访时段
- `compliance.organizationInfo`：机构公示信息
- `compliance.qualifications`：资质证照编号、签发单位、有效期
- `compliance.map.navigationUrl`：真实地图导航链接
- `compliance.mediaRights`：图片来源/版权归属/授权范围/核验时间
- `legal.icp`、`legal.publicSecurityRecord`：备案信息

## 4. 发布前检查（运营勾选）

- [ ] 机构名称、Slogan、联系方式与官方对外口径一致
- [ ] 新闻日期真实有效，无“未来日期”误填
- [ ] 证照编号、有效期已核验并可追溯
- [ ] 地图导航链接可打开并定位正确
- [ ] 首页图片版权信息与素材授权一致
- [ ] “进入管理后台 / 员工登录”跳转正常

## 5. 一键检查脚本（运营/产品可执行）

```bash
./scripts/enterprise_home_content_check.sh
```

可检查：

- 关键字段是否存在
- 是否仍有 `#` 占位链接
- 是否残留“示例/XXXXXX”占位文案
- 联系方式字段格式是否异常
- 资质条目是否过少

## 6. 运营待办生成（可复制到周报）

```bash
./scripts/enterprise_ops_todo.sh
```

输出包含：

- 最近更新 / 下次复核 / 维护人
- 占位风险项数量
- 本周建议处理清单

## 7. 时效巡检（避免内容过期）

```bash
./scripts/enterprise_home_freshness_check.sh
```

输出包含：

- 最近更新时间距今天数
- 下次复核日期是否逾期
- 最新新闻新鲜度（天数）
- 综合风险结论

## 8. 风险提醒

- 示例字段（如“示例地址”“示例编号”）上线前必须替换为真实信息。
- 对外公示内容建议保留“最后更新日期”，降低信息过期风险。

## 9. 预约咨询记录管理（前端本地）

企业首页“立即预约”弹窗提交后，会将记录保存到浏览器本地（`localStorage`）：

- 键名：`enterprise_consult_records`
- 用途：演示环境收集咨询意向，不替代正式 CRM 入库

弹窗内提供：

- 导出预约记录（JSON）
- 清空预约记录

建议上线后将该流程对接后端线索接口，避免多终端数据丢失。
