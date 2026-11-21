# 管理员与基础配置：功能、页面信息与排版盘点

日期：2026-11-15。基于当前工作区源码，未改业务页面。

## 阅读口径

“页面入口”按有页面组件的非隐藏路由统计；同一组件可承载不同场景。隐藏兼容入口见兼容清单。以下字段为源码声明的可显示信息，并非同一账号、同一状态下同时出现；弹层内容与默认列表分开记录。动态列、权限、数据状态仍以运行时为准。

本分册记录 20 个重点入口；其中 `/system` 虽在路由标记隐藏，仍是系统管理员默认首页，特意纳入。包含管理员与共享页面交叉，不可与其他分册数量相加。

## 页面总表

| 页面 | 地址 | 功能及主要信息 | 排版结构 | 重复关联 |
|---|---|---|---|---|
| 老人类别 | /base-config/elder-type | 维护「老人类别」字典分组（ELDER_CATEGORY）：关键词/状态筛选；配置名称、编码、排序、状态、备注；新增编辑、批量启停及导入预检。特定分组另含房间属性/功能码/默认床位数等条件字段。 | 页头 → 配置分类页签/筛选 → 配置表格 → 新增编辑弹层/导入预检。 | — |
| 客户标签 | /base-config/marketing/customer-tag | 维护「客户标签」字典分组（MARKETING_CUSTOMER_TAG）：关键词/状态筛选；配置名称、编码、排序、状态、备注；新增编辑、批量启停及导入预检。特定分组另含房间属性/功能码/默认床位数等条件字段。 | 页头 → 配置分类页签/筛选 → 配置表格 → 新增编辑弹层/导入预检。 | — |
| 来源渠道 | /base-config/marketing/source-channel | 维护「来源渠道」字典分组（MARKETING_SOURCE_CHANNEL）：关键词/状态筛选；配置名称、编码、排序、状态、备注；新增编辑、批量启停及导入预检。特定分组另含房间属性/功能码/默认床位数等条件字段。 | 页头 → 配置分类页签/筛选 → 配置表格 → 新增编辑弹层/导入预检。 | — |
| 评估 | /base-config/assessment | 维护「评估」字典分组（ASSESSMENT_TYPE）：关键词/状态筛选；配置名称、编码、排序、状态、备注；新增编辑、批量启停及导入预检。特定分组另含房间属性/功能码/默认床位数等条件字段。 | 页头 → 配置分类页签/筛选 → 配置表格 → 新增编辑弹层/导入预检。 | — |
| 活动 | /base-config/activity | 维护「活动」字典分组（ACTIVITY_TYPE）：关键词/状态筛选；配置名称、编码、排序、状态、备注；新增编辑、批量启停及导入预检。特定分组另含房间属性/功能码/默认床位数等条件字段。 | 页头 → 配置分类页签/筛选 → 配置表格 → 新增编辑弹层/导入预检。 | — |
| 维修分类 | /base-config/community/maintenance-category | 维护「维修分类」字典分组（COMMUNITY_REPAIR_CATEGORY）：关键词/状态筛选；配置名称、编码、排序、状态、备注；新增编辑、批量启停及导入预检。特定分组另含房间属性/功能码/默认床位数等条件字段。 | 页头 → 配置分类页签/筛选 → 配置表格 → 新增编辑弹层/导入预检。 | — |
| 任务类型设置 | /base-config/community/task-type-settings | 维护「任务类型设置」字典分组（COMMUNITY_TASK_TYPE）：关键词/状态筛选；配置名称、编码、排序、状态、备注；新增编辑、批量启停及导入预检。特定分组另含房间属性/功能码/默认床位数等条件字段。 | 页头 → 配置分类页签/筛选 → 配置表格 → 新增编辑弹层/导入预检。 | — |
| 退住费用设置 | /base-config/discharge-fee-settings | 维护「退住费用设置」字典分组（DISCHARGE_FEE_CONFIG）：关键词/状态筛选；配置名称、编码、排序、状态、备注；新增编辑、批量启停及导入预检。特定分组另含房间属性/功能码/默认床位数等条件字段。 | 页头 → 配置分类页签/筛选 → 配置表格 → 新增编辑弹层/导入预检。 | — |
| 费用 | /base-config/fee | 维护「费用」字典分组（FEE_TYPE）：关键词/状态筛选；配置名称、编码、排序、状态、备注；新增编辑、批量启停及导入预检。特定分组另含房间属性/功能码/默认床位数等条件字段。 | 页头 → 配置分类页签/筛选 → 配置表格 → 新增编辑弹层/导入预检。 | — |
| 退款原因 | /base-config/refund-reason | 维护「退款原因」字典分组（REFUND_REASON）：关键词/状态筛选；配置名称、编码、排序、状态、备注；新增编辑、批量启停及导入预检。特定分组另含房间属性/功能码/默认床位数等条件字段。 | 页头 → 配置分类页签/筛选 → 配置表格 → 新增编辑弹层/导入预检。 | — |
| 试住套餐 | /base-config/trial-package | 维护「试住套餐」字典分组（TRIAL_STAY_PACKAGE）：关键词/状态筛选；配置名称、编码、排序、状态、备注；新增编辑、批量启停及导入预检。特定分组另含房间属性/功能码/默认床位数等条件字段。 | 页头 → 配置分类页签/筛选 → 配置表格 → 新增编辑弹层/导入预检。 | — |
| 官网配置中心 | /system/site-config | 官网六类内容编辑：介绍、动态、生活娱乐、联系、资质、招聘合作；大量重复条目编辑；另有预约留言表格。 | 页头 → 官网内容/预约留言主页签 → 内容分类页签 → 多段纵向表单与可增删条目；留言用表格。 | — |
| 机构信息 | /system/org-info | 机构编码、名称、类型、联系人、电话、地址、启停；查询和编辑。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 角色与权限 | /system/role | 角色名称、部门、上级领导角色、页面权限、状态；搜索、编辑和删除。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 部门管理 | /system/department | 部门名称、状态；查询、新增、编辑、删除。 | 筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 经营总览 | /portal | 经营总览：入住/服务/回款/风险、收费押金电费、生日、最近操作、收入趋势、健康安全预警、快捷入口与协同待办。 | 数据表格 → 弹窗/抽屉 → 图表；表格列/操作由脚本或子组件定义。 | D20 |
| 系统管理首页 | /system | 系统治理首页：角色权限、部门、机构、安全策略及两类审计入口；建议维护顺序与职责边界，不是六部门业务首页。 | 页头 → 治理引导 → 配置入口卡片 → 建议顺序与职责说明。 | — |
| 敏感数据审计 | /stats/sensitive-access-audit | 时间、操作人、角色、动作、数据类别、对象、字段、用途、结果、IP | 数据表格；表格列/操作由脚本或子组件定义。 | — |
| 安全策略配置 | /stats/security-policy | 开启后，用户名密码校验通过还需短信验证码（发送到员工手机号）才能登录、开启后，长者/家属的身份证、手机号等在接口返回时自动打码（豁免角色可看全文） | 由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。 | — |
| 导出审计 | /stats/export-audit | 时间、操作人、角色、模块、范围、用途、行数、状态、导出时间、IP | 数据表格；表格列/操作由脚本或子组件定义。 | — |

## 逐页字段、操作和实现依据

每页给出筛选、列表列、弹层字段和动作。脚本列定义按变量名列出：其中可能包含导出列、子表列或非当前场景列，不能把它们相加作为首屏列数。静态文字用于补充指标/分区信息；动态表达式保留原名，便于回查。

### 1. 老人类别 `/base-config/elder-type`

维护「老人类别」字典分组（ELDER_CATEGORY）：关键词/状态筛选；配置名称、编码、排序、状态、备注；新增编辑、批量启停及导入预检。特定分组另含房间属性/功能码/默认床位数等条件字段。

- 导航归属：基础数据配置 > 老人类别。
- 路由角色约束：[["SYS_ADMIN", "ADMIN"], ["ADMIN"]]。
- 场景参数：{"title": "老人类别", "groupCode": "ELDER_CATEGORY"}。
- 排版：页头 → 配置分类页签/筛选 → 配置表格 → 新增编辑弹层/导入预检。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/systemRoutes.ts](/Users/yang/smartcare/admin-web/src/router/systemRoutes.ts)。

**页面：[admin-web/src/views/base-config/Index.vue](/Users/yang/smartcare/admin-web/src/views/base-config/Index.vue)**

- 筛选/主页面表单：关键词、状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：max-height: 260px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：基础数据配置、分组、配置名称、配置编码、排序、状态、备注、操作、存在导入失败项，是否下载错误文件？。
- 页签/分区：item.label。
- 指标和说明静态文字：当前分组：、可维护分组：、已勾选： 项、当前分组记录、便于快速掌握该字典规模、当前页启用项、优先确认实际生效配置、当前页停用项、减少误用历史配置、待批量处理、支持启停和导出前集中确认。
- 弹层标题：editingId ? '编辑基础数据' : '新增基础数据'、导入预校验结果。
- 弹层表单字段：所属分组、配置名称、配置编码、房间属性、功能码、默认床位数、排序、状态、备注。
- 可读操作文案：查询、重置、新增配置项、批量启用、批量停用、一键补全默认项、同步到床态全景、导出CSV、导入模板、导入CSV/错误文件、编辑、删除、下载错误文件、上传修复后错误文件。

### 2. 客户标签 `/base-config/marketing/customer-tag`

维护「客户标签」字典分组（MARKETING_CUSTOMER_TAG）：关键词/状态筛选；配置名称、编码、排序、状态、备注；新增编辑、批量启停及导入预检。特定分组另含房间属性/功能码/默认床位数等条件字段。

- 导航归属：基础数据配置 > 营销 > 客户标签。
- 路由角色约束：[["SYS_ADMIN", "ADMIN"], ["ADMIN"], ["ADMIN"]]。
- 场景参数：{"title": "客户标签", "groupCode": "MARKETING_CUSTOMER_TAG"}。
- 排版：页头 → 配置分类页签/筛选 → 配置表格 → 新增编辑弹层/导入预检。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/systemRoutes.ts](/Users/yang/smartcare/admin-web/src/router/systemRoutes.ts)。

**页面：[admin-web/src/views/base-config/Index.vue](/Users/yang/smartcare/admin-web/src/views/base-config/Index.vue)**

- 筛选/主页面表单：关键词、状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：max-height: 260px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：基础数据配置、分组、配置名称、配置编码、排序、状态、备注、操作、存在导入失败项，是否下载错误文件？。
- 页签/分区：item.label。
- 指标和说明静态文字：当前分组：、可维护分组：、已勾选： 项、当前分组记录、便于快速掌握该字典规模、当前页启用项、优先确认实际生效配置、当前页停用项、减少误用历史配置、待批量处理、支持启停和导出前集中确认。
- 弹层标题：editingId ? '编辑基础数据' : '新增基础数据'、导入预校验结果。
- 弹层表单字段：所属分组、配置名称、配置编码、房间属性、功能码、默认床位数、排序、状态、备注。
- 可读操作文案：查询、重置、新增配置项、批量启用、批量停用、一键补全默认项、同步到床态全景、导出CSV、导入模板、导入CSV/错误文件、编辑、删除、下载错误文件、上传修复后错误文件。

### 3. 来源渠道 `/base-config/marketing/source-channel`

维护「来源渠道」字典分组（MARKETING_SOURCE_CHANNEL）：关键词/状态筛选；配置名称、编码、排序、状态、备注；新增编辑、批量启停及导入预检。特定分组另含房间属性/功能码/默认床位数等条件字段。

- 导航归属：基础数据配置 > 营销 > 来源渠道。
- 路由角色约束：[["SYS_ADMIN", "ADMIN"], ["ADMIN"], ["ADMIN"]]。
- 场景参数：{"title": "来源渠道", "groupCode": "MARKETING_SOURCE_CHANNEL"}。
- 排版：页头 → 配置分类页签/筛选 → 配置表格 → 新增编辑弹层/导入预检。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/systemRoutes.ts](/Users/yang/smartcare/admin-web/src/router/systemRoutes.ts)。

**页面：[admin-web/src/views/base-config/Index.vue](/Users/yang/smartcare/admin-web/src/views/base-config/Index.vue)**

- 筛选/主页面表单：关键词、状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：max-height: 260px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：基础数据配置、分组、配置名称、配置编码、排序、状态、备注、操作、存在导入失败项，是否下载错误文件？。
- 页签/分区：item.label。
- 指标和说明静态文字：当前分组：、可维护分组：、已勾选： 项、当前分组记录、便于快速掌握该字典规模、当前页启用项、优先确认实际生效配置、当前页停用项、减少误用历史配置、待批量处理、支持启停和导出前集中确认。
- 弹层标题：editingId ? '编辑基础数据' : '新增基础数据'、导入预校验结果。
- 弹层表单字段：所属分组、配置名称、配置编码、房间属性、功能码、默认床位数、排序、状态、备注。
- 可读操作文案：查询、重置、新增配置项、批量启用、批量停用、一键补全默认项、同步到床态全景、导出CSV、导入模板、导入CSV/错误文件、编辑、删除、下载错误文件、上传修复后错误文件。

### 4. 评估 `/base-config/assessment`

维护「评估」字典分组（ASSESSMENT_TYPE）：关键词/状态筛选；配置名称、编码、排序、状态、备注；新增编辑、批量启停及导入预检。特定分组另含房间属性/功能码/默认床位数等条件字段。

- 导航归属：基础数据配置 > 评估。
- 路由角色约束：[["SYS_ADMIN", "ADMIN"], ["ADMIN"]]。
- 场景参数：{"title": "评估", "groupCode": "ASSESSMENT_TYPE"}。
- 排版：页头 → 配置分类页签/筛选 → 配置表格 → 新增编辑弹层/导入预检。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/systemRoutes.ts](/Users/yang/smartcare/admin-web/src/router/systemRoutes.ts)。

**页面：[admin-web/src/views/base-config/Index.vue](/Users/yang/smartcare/admin-web/src/views/base-config/Index.vue)**

- 筛选/主页面表单：关键词、状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：max-height: 260px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：基础数据配置、分组、配置名称、配置编码、排序、状态、备注、操作、存在导入失败项，是否下载错误文件？。
- 页签/分区：item.label。
- 指标和说明静态文字：当前分组：、可维护分组：、已勾选： 项、当前分组记录、便于快速掌握该字典规模、当前页启用项、优先确认实际生效配置、当前页停用项、减少误用历史配置、待批量处理、支持启停和导出前集中确认。
- 弹层标题：editingId ? '编辑基础数据' : '新增基础数据'、导入预校验结果。
- 弹层表单字段：所属分组、配置名称、配置编码、房间属性、功能码、默认床位数、排序、状态、备注。
- 可读操作文案：查询、重置、新增配置项、批量启用、批量停用、一键补全默认项、同步到床态全景、导出CSV、导入模板、导入CSV/错误文件、编辑、删除、下载错误文件、上传修复后错误文件。

### 5. 活动 `/base-config/activity`

维护「活动」字典分组（ACTIVITY_TYPE）：关键词/状态筛选；配置名称、编码、排序、状态、备注；新增编辑、批量启停及导入预检。特定分组另含房间属性/功能码/默认床位数等条件字段。

- 导航归属：基础数据配置 > 活动。
- 路由角色约束：[["SYS_ADMIN", "ADMIN"], ["ADMIN"]]。
- 场景参数：{"title": "活动", "groupCode": "ACTIVITY_TYPE"}。
- 排版：页头 → 配置分类页签/筛选 → 配置表格 → 新增编辑弹层/导入预检。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/systemRoutes.ts](/Users/yang/smartcare/admin-web/src/router/systemRoutes.ts)。

**页面：[admin-web/src/views/base-config/Index.vue](/Users/yang/smartcare/admin-web/src/views/base-config/Index.vue)**

- 筛选/主页面表单：关键词、状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：max-height: 260px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：基础数据配置、分组、配置名称、配置编码、排序、状态、备注、操作、存在导入失败项，是否下载错误文件？。
- 页签/分区：item.label。
- 指标和说明静态文字：当前分组：、可维护分组：、已勾选： 项、当前分组记录、便于快速掌握该字典规模、当前页启用项、优先确认实际生效配置、当前页停用项、减少误用历史配置、待批量处理、支持启停和导出前集中确认。
- 弹层标题：editingId ? '编辑基础数据' : '新增基础数据'、导入预校验结果。
- 弹层表单字段：所属分组、配置名称、配置编码、房间属性、功能码、默认床位数、排序、状态、备注。
- 可读操作文案：查询、重置、新增配置项、批量启用、批量停用、一键补全默认项、同步到床态全景、导出CSV、导入模板、导入CSV/错误文件、编辑、删除、下载错误文件、上传修复后错误文件。

### 6. 维修分类 `/base-config/community/maintenance-category`

维护「维修分类」字典分组（COMMUNITY_REPAIR_CATEGORY）：关键词/状态筛选；配置名称、编码、排序、状态、备注；新增编辑、批量启停及导入预检。特定分组另含房间属性/功能码/默认床位数等条件字段。

- 导航归属：基础数据配置 > 社区 > 维修分类。
- 路由角色约束：[["SYS_ADMIN", "ADMIN"], ["ADMIN"], ["ADMIN"]]。
- 场景参数：{"title": "维修分类", "groupCode": "COMMUNITY_REPAIR_CATEGORY"}。
- 排版：页头 → 配置分类页签/筛选 → 配置表格 → 新增编辑弹层/导入预检。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/systemRoutes.ts](/Users/yang/smartcare/admin-web/src/router/systemRoutes.ts)。

**页面：[admin-web/src/views/base-config/Index.vue](/Users/yang/smartcare/admin-web/src/views/base-config/Index.vue)**

- 筛选/主页面表单：关键词、状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：max-height: 260px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：基础数据配置、分组、配置名称、配置编码、排序、状态、备注、操作、存在导入失败项，是否下载错误文件？。
- 页签/分区：item.label。
- 指标和说明静态文字：当前分组：、可维护分组：、已勾选： 项、当前分组记录、便于快速掌握该字典规模、当前页启用项、优先确认实际生效配置、当前页停用项、减少误用历史配置、待批量处理、支持启停和导出前集中确认。
- 弹层标题：editingId ? '编辑基础数据' : '新增基础数据'、导入预校验结果。
- 弹层表单字段：所属分组、配置名称、配置编码、房间属性、功能码、默认床位数、排序、状态、备注。
- 可读操作文案：查询、重置、新增配置项、批量启用、批量停用、一键补全默认项、同步到床态全景、导出CSV、导入模板、导入CSV/错误文件、编辑、删除、下载错误文件、上传修复后错误文件。

### 7. 任务类型设置 `/base-config/community/task-type-settings`

维护「任务类型设置」字典分组（COMMUNITY_TASK_TYPE）：关键词/状态筛选；配置名称、编码、排序、状态、备注；新增编辑、批量启停及导入预检。特定分组另含房间属性/功能码/默认床位数等条件字段。

- 导航归属：基础数据配置 > 社区 > 任务类型设置。
- 路由角色约束：[["SYS_ADMIN", "ADMIN"], ["ADMIN"], ["ADMIN"]]。
- 场景参数：{"title": "任务类型设置", "groupCode": "COMMUNITY_TASK_TYPE"}。
- 排版：页头 → 配置分类页签/筛选 → 配置表格 → 新增编辑弹层/导入预检。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/systemRoutes.ts](/Users/yang/smartcare/admin-web/src/router/systemRoutes.ts)。

**页面：[admin-web/src/views/base-config/Index.vue](/Users/yang/smartcare/admin-web/src/views/base-config/Index.vue)**

- 筛选/主页面表单：关键词、状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：max-height: 260px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：基础数据配置、分组、配置名称、配置编码、排序、状态、备注、操作、存在导入失败项，是否下载错误文件？。
- 页签/分区：item.label。
- 指标和说明静态文字：当前分组：、可维护分组：、已勾选： 项、当前分组记录、便于快速掌握该字典规模、当前页启用项、优先确认实际生效配置、当前页停用项、减少误用历史配置、待批量处理、支持启停和导出前集中确认。
- 弹层标题：editingId ? '编辑基础数据' : '新增基础数据'、导入预校验结果。
- 弹层表单字段：所属分组、配置名称、配置编码、房间属性、功能码、默认床位数、排序、状态、备注。
- 可读操作文案：查询、重置、新增配置项、批量启用、批量停用、一键补全默认项、同步到床态全景、导出CSV、导入模板、导入CSV/错误文件、编辑、删除、下载错误文件、上传修复后错误文件。

### 8. 退住费用设置 `/base-config/discharge-fee-settings`

维护「退住费用设置」字典分组（DISCHARGE_FEE_CONFIG）：关键词/状态筛选；配置名称、编码、排序、状态、备注；新增编辑、批量启停及导入预检。特定分组另含房间属性/功能码/默认床位数等条件字段。

- 导航归属：基础数据配置 > 退住费用设置。
- 路由角色约束：[["SYS_ADMIN", "ADMIN"], ["ADMIN"]]。
- 场景参数：{"title": "退住费用设置", "groupCode": "DISCHARGE_FEE_CONFIG"}。
- 排版：页头 → 配置分类页签/筛选 → 配置表格 → 新增编辑弹层/导入预检。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/systemRoutes.ts](/Users/yang/smartcare/admin-web/src/router/systemRoutes.ts)。

**页面：[admin-web/src/views/base-config/Index.vue](/Users/yang/smartcare/admin-web/src/views/base-config/Index.vue)**

- 筛选/主页面表单：关键词、状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：max-height: 260px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：基础数据配置、分组、配置名称、配置编码、排序、状态、备注、操作、存在导入失败项，是否下载错误文件？。
- 页签/分区：item.label。
- 指标和说明静态文字：当前分组：、可维护分组：、已勾选： 项、当前分组记录、便于快速掌握该字典规模、当前页启用项、优先确认实际生效配置、当前页停用项、减少误用历史配置、待批量处理、支持启停和导出前集中确认。
- 弹层标题：editingId ? '编辑基础数据' : '新增基础数据'、导入预校验结果。
- 弹层表单字段：所属分组、配置名称、配置编码、房间属性、功能码、默认床位数、排序、状态、备注。
- 可读操作文案：查询、重置、新增配置项、批量启用、批量停用、一键补全默认项、同步到床态全景、导出CSV、导入模板、导入CSV/错误文件、编辑、删除、下载错误文件、上传修复后错误文件。

### 9. 费用 `/base-config/fee`

维护「费用」字典分组（FEE_TYPE）：关键词/状态筛选；配置名称、编码、排序、状态、备注；新增编辑、批量启停及导入预检。特定分组另含房间属性/功能码/默认床位数等条件字段。

- 导航归属：基础数据配置 > 费用。
- 路由角色约束：[["SYS_ADMIN", "ADMIN"], ["ADMIN"]]。
- 场景参数：{"title": "费用", "groupCode": "FEE_TYPE"}。
- 排版：页头 → 配置分类页签/筛选 → 配置表格 → 新增编辑弹层/导入预检。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/systemRoutes.ts](/Users/yang/smartcare/admin-web/src/router/systemRoutes.ts)。

**页面：[admin-web/src/views/base-config/Index.vue](/Users/yang/smartcare/admin-web/src/views/base-config/Index.vue)**

- 筛选/主页面表单：关键词、状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：max-height: 260px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：基础数据配置、分组、配置名称、配置编码、排序、状态、备注、操作、存在导入失败项，是否下载错误文件？。
- 页签/分区：item.label。
- 指标和说明静态文字：当前分组：、可维护分组：、已勾选： 项、当前分组记录、便于快速掌握该字典规模、当前页启用项、优先确认实际生效配置、当前页停用项、减少误用历史配置、待批量处理、支持启停和导出前集中确认。
- 弹层标题：editingId ? '编辑基础数据' : '新增基础数据'、导入预校验结果。
- 弹层表单字段：所属分组、配置名称、配置编码、房间属性、功能码、默认床位数、排序、状态、备注。
- 可读操作文案：查询、重置、新增配置项、批量启用、批量停用、一键补全默认项、同步到床态全景、导出CSV、导入模板、导入CSV/错误文件、编辑、删除、下载错误文件、上传修复后错误文件。

### 10. 退款原因 `/base-config/refund-reason`

维护「退款原因」字典分组（REFUND_REASON）：关键词/状态筛选；配置名称、编码、排序、状态、备注；新增编辑、批量启停及导入预检。特定分组另含房间属性/功能码/默认床位数等条件字段。

- 导航归属：基础数据配置 > 退款原因。
- 路由角色约束：[["SYS_ADMIN", "ADMIN"], ["ADMIN"]]。
- 场景参数：{"title": "退款原因", "groupCode": "REFUND_REASON"}。
- 排版：页头 → 配置分类页签/筛选 → 配置表格 → 新增编辑弹层/导入预检。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/systemRoutes.ts](/Users/yang/smartcare/admin-web/src/router/systemRoutes.ts)。

**页面：[admin-web/src/views/base-config/Index.vue](/Users/yang/smartcare/admin-web/src/views/base-config/Index.vue)**

- 筛选/主页面表单：关键词、状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：max-height: 260px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：基础数据配置、分组、配置名称、配置编码、排序、状态、备注、操作、存在导入失败项，是否下载错误文件？。
- 页签/分区：item.label。
- 指标和说明静态文字：当前分组：、可维护分组：、已勾选： 项、当前分组记录、便于快速掌握该字典规模、当前页启用项、优先确认实际生效配置、当前页停用项、减少误用历史配置、待批量处理、支持启停和导出前集中确认。
- 弹层标题：editingId ? '编辑基础数据' : '新增基础数据'、导入预校验结果。
- 弹层表单字段：所属分组、配置名称、配置编码、房间属性、功能码、默认床位数、排序、状态、备注。
- 可读操作文案：查询、重置、新增配置项、批量启用、批量停用、一键补全默认项、同步到床态全景、导出CSV、导入模板、导入CSV/错误文件、编辑、删除、下载错误文件、上传修复后错误文件。

### 11. 试住套餐 `/base-config/trial-package`

维护「试住套餐」字典分组（TRIAL_STAY_PACKAGE）：关键词/状态筛选；配置名称、编码、排序、状态、备注；新增编辑、批量启停及导入预检。特定分组另含房间属性/功能码/默认床位数等条件字段。

- 导航归属：基础数据配置 > 试住套餐。
- 路由角色约束：[["SYS_ADMIN", "ADMIN"], ["ADMIN"]]。
- 场景参数：{"title": "试住套餐", "groupCode": "TRIAL_STAY_PACKAGE"}。
- 排版：页头 → 配置分类页签/筛选 → 配置表格 → 新增编辑弹层/导入预检。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/systemRoutes.ts](/Users/yang/smartcare/admin-web/src/router/systemRoutes.ts)。

**页面：[admin-web/src/views/base-config/Index.vue](/Users/yang/smartcare/admin-web/src/views/base-config/Index.vue)**

- 筛选/主页面表单：关键词、状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 表格列绑定：columns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：max-height: 260px。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：基础数据配置、分组、配置名称、配置编码、排序、状态、备注、操作、存在导入失败项，是否下载错误文件？。
- 页签/分区：item.label。
- 指标和说明静态文字：当前分组：、可维护分组：、已勾选： 项、当前分组记录、便于快速掌握该字典规模、当前页启用项、优先确认实际生效配置、当前页停用项、减少误用历史配置、待批量处理、支持启停和导出前集中确认。
- 弹层标题：editingId ? '编辑基础数据' : '新增基础数据'、导入预校验结果。
- 弹层表单字段：所属分组、配置名称、配置编码、房间属性、功能码、默认床位数、排序、状态、备注。
- 可读操作文案：查询、重置、新增配置项、批量启用、批量停用、一键补全默认项、同步到床态全景、导出CSV、导入模板、导入CSV/错误文件、编辑、删除、下载错误文件、上传修复后错误文件。

### 12. 官网配置中心 `/system/site-config`

官网六类内容编辑：介绍、动态、生活娱乐、联系、资质、招聘合作；大量重复条目编辑；另有预约留言表格。

- 导航归属：系统管理 > 官网配置中心。
- 路由角色约束：[["DIRECTOR", "SYS_ADMIN", "ADMIN", "HR_MINISTER"], ["ADMIN", "HR_MINISTER"]]。
- 场景参数：无路由 props。
- 排版：页头 → 官网内容/预约留言主页签 → 内容分类页签 → 多段纵向表单与可增删条目；留言用表格。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/systemRoutes.ts](/Users/yang/smartcare/admin-web/src/router/systemRoutes.ts)。

**页面：[admin-web/src/views/System/SiteConfigCenter.vue](/Users/yang/smartcare/admin-web/src/views/System/SiteConfigCenter.vue)**

- 筛选/主页面表单：机构全称、机构简称、宣传口号、副标题、首页主标题、首页主描述、机构使命、机构愿景、标题、内容、服务名称、服务说明、简介、日期、标签、公告标题、级别、活动名称、活动时间、地点、姓名、关系、评价内容、联系电话、客服邮箱、来访时间、电话提示、机构地址、VR看社区链接、加入我们链接、下次复核日期、ICP备案号、公安备案号、机构公示信息、服务半径、导航链接、地图说明、图片来源、版权归属、授权范围、核验日期、版权联系人、资质名称、证照编号、发证单位、有效期、公示链接、岗位名称、所属部门、任职要求、合作标题、合作说明。
- 模板直接声明的表格列：提交时间、联系人、联系电话、对接部门、意向日期、需求。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：机构介绍、机构动态、生活娱乐、联系方式、资质公示、招聘合作、一般、重要、喜讯、新介绍模块、新服务项、新动态、新新闻、新公告、新活动、新亮点、家属姓名、新资质、新岗位、新合作方向。
- 页签/分区：官网内容配置、预约留言记录。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：新增介绍卡片、删除、新增服务项、新增动态、新增新闻、新增公告、新增活动、新增亮点、新增评价、新增资质、新增岗位、新增合作项、重新载入当前配置、恢复默认内容、保存并生效、导出CSV、清空全部。

### 13. 机构信息 `/system/org-info`

机构编码、名称、类型、联系人、电话、地址、启停；查询和编辑。

- 导航归属：系统管理 > 机构信息。
- 路由角色约束：[["DIRECTOR", "SYS_ADMIN", "ADMIN", "HR_MINISTER"], ["DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/systemRoutes.ts](/Users/yang/smartcare/admin-web/src/router/systemRoutes.ts)。

**页面：[admin-web/src/views/System/OrgInfo.vue](/Users/yang/smartcare/admin-web/src/views/System/OrgInfo.vue)**

- 筛选/主页面表单：关键词。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：机构编码、机构名称、机构类型、联系人、联系电话、状态、操作。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：启用、停用、机构编码、机构名称、机构类型、联系人、联系电话、状态、操作。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：drawerTitle。
- 弹层表单字段：机构编码、机构名称、机构类型、联系人、联系电话、地址、状态。
- 可读操作文案：新增机构、编辑、删除、取消、保存。

### 14. 角色与权限 `/system/role`

角色名称、部门、上级领导角色、页面权限、状态；搜索、编辑和删除。

- 导航归属：系统管理 > 角色与权限。
- 路由角色约束：[["DIRECTOR", "SYS_ADMIN", "ADMIN", "HR_MINISTER"], ["SYS_ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/systemRoutes.ts](/Users/yang/smartcare/admin-web/src/router/systemRoutes.ts)。

**页面：[admin-web/src/views/System/RoleManage.vue](/Users/yang/smartcare/admin-web/src/views/System/RoleManage.vue)**

- 筛选/主页面表单：关键词、所属部门、状态。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：角色名称、所属部门、上级领导角色、页面权限、状态、操作。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：启用、停用、角色名称、所属部门、上级领导角色、页面权限、状态、操作。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：角色总数、启用、预设推荐、当前页角色、用于快速核对组织分工、启用角色、可参与当前权限与监管链、停用角色、建议确认是否仍需保留、编辑中权限、当前已勾选页面权限数、已选 个页面。
- 弹层标题：drawerTitle。
- 弹层表单字段：角色名称、所属部门、上级领导角色、状态、页面权限。
- 可读操作文案：新增角色、全部、编辑、删除、套用推荐预设、清空页面权限、取消、保存。

### 15. 部门管理 `/system/department`

部门名称、状态；查询、新增、编辑、删除。

- 导航归属：系统管理 > 部门管理。
- 路由角色约束：[["DIRECTOR", "SYS_ADMIN", "ADMIN", "HR_MINISTER"], ["HR_MINISTER", "DIRECTOR", "SYS_ADMIN", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：筛选卡片 → 弹窗/抽屉 → 数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/systemRoutes.ts](/Users/yang/smartcare/admin-web/src/router/systemRoutes.ts)。

**页面：[admin-web/src/views/System/DepartmentManage.vue](/Users/yang/smartcare/admin-web/src/views/System/DepartmentManage.vue)**

- 筛选/主页面表单：关键词。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：部门名称、状态、操作。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：启用、停用、部门名称、状态、操作、确认删除该部门吗？。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：部门总数、启用、停用、当前页部门、用于维护组织基础结构、启用部门、可继续用于档案和账号归属、停用部门、建议核对是否仍需保留、快捷入口、2、支持跳转档案中心与账号设置。
- 弹层标题：drawerTitle。
- 弹层表单字段：部门名称、状态。
- 可读操作文案：新增部门、编辑、档案中心、更多、取消、保存。

### 16. 经营总览 `/portal`

经营总览：入住/服务/回款/风险、收费押金电费、生日、最近操作、收入趋势、健康安全预警、快捷入口与协同待办。

- 导航归属：经营总览。
- 路由角色约束：[["DIRECTOR", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格 → 弹窗/抽屉 → 图表；表格列/操作由脚本或子组件定义。
- 关联重复项：D20。
- 路由依据：[admin-web/src/router/sharedRoutes.ts](/Users/yang/smartcare/admin-web/src/router/sharedRoutes.ts)。

**页面：[admin-web/src/views/Portal.vue](/Users/yang/smartcare/admin-web/src/views/Portal.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `quickActions`：新增长者、新增护理记录、处理预警、创建工单、收费登记、数据报表。
- 脚本列 `birthdayColumns`：长者姓名、下次生日、剩余天数、届时年龄、房间 / 床位、操作。
- 表格列绑定：birthdayColumns（计算列定义请回查源码）。
- CSS布局声明（含响应式分支及局部图表）：grid-template-columns: repeat(2, minmax(0, 1fr))、grid-template-columns: repeat(4, minmax(0, 1fr))、grid-template-columns: minmax(0, 1.25fr) minmax(360px, 0.75fr)、grid-template-columns: minmax(0, 1.15fr) minmax(0, 0.85fr)、grid-template-columns: repeat(3, minmax(0, 1fr))、height: 1.7、height: 1.2、min-height: 132px、height: 1.6、height: 1、height: 4px、grid-template-columns: minmax(0, 0.82fr) minmax(0, 1.18fr) auto、height: 34px、height: 240px、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：入住率、服务完成、本月回款、账单、押金、护理费统一跟踪、未闭环风险、体检异常、未闭环事故、维修超时工单、入住转化、合同收费、人力排班、家属体验、照护执行 SLA、健康复核、安全事件、后勤保障、咨询到入住、能力评估、护理计划执行、健康用药、家属连接、经营复盘、智能风险预警、AI护理摘要、IoT设备接入、体检异常 · 未闭环事故 · 维修超时工单、待处理事项、在住 / 空床、在住长者 / 空置床位、本月收入 / 欠费、本月收费与回款，可下钻欠费与结算、今日护理任务、护理执行与巡诊跟踪、员工在岗数、排班与人力在岗、设备在线率、待设备遥测接入、高风险长者、重点关注跌倒、离床、异常波动等高风险对象。、体检异常未闭环、聚焦体温、血压、血氧等异常与跟进中记录。、跌倒 / 离床 / 呼叫预警、行为类预警统一收束到处置视线，不再散落在不同模块。、异常生命体征、待处理预警列表、护理记录、计划与执行、待审批事项、行政、财务、人事审批、待处理维修 / 后勤工单、维修、巡检与后勤保障、待回访客户、咨询线索与营销回访、新增长者、新增护理记录、处理预警、创建工单、收费登记、数据报表、长者姓名、下次生日、剩余天数、届时年龄、房间 / 床位、操作、近期生日关怀节奏平稳、房间号、床位号、开放待办、进行中任务、任务推进与跨部门执行、今日日程、个人、部门与协同安排、生日待办、关怀类提醒与后续处理、逾期待办、待我审批、生日关怀待办、协同日程。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：员工入口、先进入个人工作台、打卡、待办、审批、日程和岗位任务都从个人视角开始处理。、管理入口、再看机构经营与风险、院长、主管和管理员适合从经营首页或月报继续下钻。、本月经营闭环、入住、回款、人效与客户转化、风险闭环、护理、健康、后勤与审批 SLA。
- 弹层标题：birthdayModalTitle。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：进入工作台、刷新首页、经营月报、待办中心、我的日程、打印名单、进入完整生日页面。

### 17. 系统管理首页 `/system`

系统治理首页：角色权限、部门、机构、安全策略及两类审计入口；建议维护顺序与职责边界，不是六部门业务首页。

- 导航归属：系统管理 > 系统管理首页。
- 路由角色约束：[["DIRECTOR", "SYS_ADMIN", "ADMIN", "HR_MINISTER"]]。
- 场景参数：无路由 props。
- 排版：页头 → 治理引导 → 配置入口卡片 → 建议顺序与职责说明。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/systemRoutes.ts](/Users/yang/smartcare/admin-web/src/router/systemRoutes.ts)。

**页面：[admin-web/src/views/System/SystemAdminHome.vue](/Users/yang/smartcare/admin-web/src/views/System/SystemAdminHome.vue)**

- 筛选/主页面表单：未在该层模板中声明。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `actions`：角色与权限、部门管理、机构信息、安全策略、敏感访问审计、导出审计。
- CSS布局声明（含响应式分支及局部图表）：height: 1.7、grid-template-columns: repeat(3, 1fr)、min-height: 156px、height: 1.6、grid-template-columns: 1fr 1fr、grid-template-columns: 48px 1fr、height: 1.5、grid-template-columns: 110px 1fr、grid-template-columns: repeat(2, 1fr)、grid-template-columns: 1fr。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：角色与权限、部门管理、机构信息、安全策略、敏感访问审计、导出审计。
- 页签/分区：先确认授权边界，再维护组织与安全策略。。
- 指标和说明静态文字：ACCESS · ORGANIZATION · SECURITY、这里只呈现当前账号真正有权进入的系统能力。未授权页面不会出现在入口中，直接访问仍由统一路由守卫拦截。、01、先确认岗位可见页面与操作范围，避免越权或漏权。、02、再维护机构、部门和人员归属，确保业务数据边界准确。、03、最后复核敏感访问、导出和安全规则，保留可追溯记录。、菜单与搜索、仅显示授权页面、直接访问、无权限进入 403、旧地址、继续按兼容映射跳转。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：未在该层模板中声明。

### 18. 敏感数据审计 `/stats/sensitive-access-audit`

主要展示信息见以下模板分区。

- 导航归属：统计分析 > 敏感数据审计。
- 路由角色约束：[["NURSING_EMPLOYEE", "MEDICAL_EMPLOYEE", "FINANCE_EMPLOYEE", "LOGISTICS_EMPLOYEE", "HR_EMPLOYEE", "MARKETING_EMPLOYEE", "NURSING_MINISTER", "MEDICAL_MINISTER", "FINANCE_MINISTER", "LOGISTICS_MINISTER", "HR_MINISTER", "MARKETING_MINISTER", "DIRECTOR", "ADMIN", "SYS_ADMIN"], ["SYS_ADMIN", "DIRECTOR", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/analyticsRoutes.ts](/Users/yang/smartcare/admin-web/src/router/analyticsRoutes.ts)。

**页面：[admin-web/src/views/compliance/SensitiveAccessAudit.vue](/Users/yang/smartcare/admin-web/src/views/compliance/SensitiveAccessAudit.vue)**

- 筛选/主页面表单：数据类别、动作、操作人ID、日期。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：时间、操作人、角色、动作、数据类别、对象、字段、用途、结果、IP。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：时间、操作人、角色、动作、数据类别、对象、字段、用途、结果。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、查询、重置。

### 19. 安全策略配置 `/stats/security-policy`

主要展示信息见以下模板分区。

- 导航归属：统计分析 > 安全策略配置。
- 路由角色约束：[["NURSING_EMPLOYEE", "MEDICAL_EMPLOYEE", "FINANCE_EMPLOYEE", "LOGISTICS_EMPLOYEE", "HR_EMPLOYEE", "MARKETING_EMPLOYEE", "NURSING_MINISTER", "MEDICAL_MINISTER", "FINANCE_MINISTER", "LOGISTICS_MINISTER", "HR_MINISTER", "MARKETING_MINISTER", "DIRECTOR", "ADMIN", "SYS_ADMIN"], ["SYS_ADMIN"]]。
- 场景参数：无路由 props。
- 排版：由页内表单、列表、卡片或业务子组件组成；详见下方分区及源码。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/analyticsRoutes.ts](/Users/yang/smartcare/admin-web/src/router/analyticsRoutes.ts)。

**页面：[admin-web/src/views/compliance/SecurityPolicyConfig.vue](/Users/yang/smartcare/admin-web/src/views/compliance/SecurityPolicyConfig.vue)**

- 筛选/主页面表单：管理端双因子登录（2FA）、仅对以下角色启用 2FA（不选 = 全部角色）、密码有效期（天，0 = 不启用）、登录失败锁定次数（0 = 不启用）、锁定时长（分钟）、会话超时（分钟，0 = 使用系统默认）、敏感字段脱敏展示、脱敏豁免角色（可查看全文）、日志留存天数。
- 模板直接声明的表格列：未在该层模板中声明。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：系统管理员 SYS_ADMIN、管理员 ADMIN、院长/管理层 DIRECTOR、医务部长 MEDICAL_MINISTER、护理部长 NURSING_MINISTER、财务部长 FINANCE_MINISTER、后勤部长 LOGISTICS_MINISTER、市场部长 MARKETING_MINISTER、人事部长 HR_MINISTER、医务员工 MEDICAL_EMPLOYEE、护理员工 NURSING_EMPLOYEE、财务员工 FINANCE_EMPLOYEE、后勤员工 LOGISTICS_EMPLOYEE、市场员工 MARKETING_EMPLOYEE、人事员工 HR_EMPLOYEE。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：开启后，用户名密码校验通过还需短信验证码（发送到员工手机号）才能登录、开启后，长者/家属的身份证、手机号等在接口返回时自动打码（豁免角色可看全文）。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：保存策略。

### 20. 导出审计 `/stats/export-audit`

主要展示信息见以下模板分区。

- 导航归属：统计分析 > 导出审计。
- 路由角色约束：[["NURSING_EMPLOYEE", "MEDICAL_EMPLOYEE", "FINANCE_EMPLOYEE", "LOGISTICS_EMPLOYEE", "HR_EMPLOYEE", "MARKETING_EMPLOYEE", "NURSING_MINISTER", "MEDICAL_MINISTER", "FINANCE_MINISTER", "LOGISTICS_MINISTER", "HR_MINISTER", "MARKETING_MINISTER", "DIRECTOR", "ADMIN", "SYS_ADMIN"], ["SYS_ADMIN", "DIRECTOR", "ADMIN"]]。
- 场景参数：无路由 props。
- 排版：数据表格；表格列/操作由脚本或子组件定义。
- 关联重复项：未单独判为重复。
- 路由依据：[admin-web/src/router/analyticsRoutes.ts](/Users/yang/smartcare/admin-web/src/router/analyticsRoutes.ts)。

**页面：[admin-web/src/views/compliance/ExportAudit.vue](/Users/yang/smartcare/admin-web/src/views/compliance/ExportAudit.vue)**

- 筛选/主页面表单：模块、状态、日期。
- 模板直接声明的表格列：未在该层模板中声明。
- 脚本列 `columns`：时间、操作人、角色、模块、范围、用途、行数、状态、导出时间、IP。
- 表格列绑定：columns（计算列定义请回查源码）。
- 动态配置中的字段/指标/操作文案（各条件分支合并登记）：时间、操作人、角色、模块、范围、用途、行数、状态、导出时间。
- 页签/分区：未在该层模板中声明。
- 指标和说明静态文字：未在该层模板中声明。
- 弹层标题：未在该层模板中声明。
- 弹层表单字段：未在该层模板中声明。
- 可读操作文案：刷新、查询、重置。

## 相关重复项

### D20 多个首页/驾驶舱

- 判断：导航和指标层层重复。
- 依据：总览、个人、部门、行政、驾驶舱重复承担入口和摘要；口径及受众并非完全一样。
- 后续整理方向：明确每类首页唯一职责，摘要点击下钻而非再次进入导航。
