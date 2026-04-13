<template>
  <PageContainer title="审批流程" subTitle="请假/加班/报销/采购/收入证明/物资申请/用章审批/营销方案审批">
    <template #stats>
      <div class="approval-overview-grid">
        <div class="overview-card">
          <span>审批总单</span>
          <strong>{{ summary.totalCount || 0 }}</strong>
          <small>覆盖当前筛选口径与工作视角</small>
        </div>
        <div class="overview-card">
          <span>待审批</span>
          <strong>{{ summary.pendingCount || 0 }}</strong>
          <small>优先处理待我审批与超时任务</small>
        </div>
        <div class="overview-card">
          <span>超时待审</span>
          <strong>{{ summary.timeoutPendingCount || 0 }}</strong>
          <small>需要尽快推进，避免流程积压</small>
        </div>
        <div class="overview-card">
          <span>批量可处理</span>
          <strong>{{ canApproveFlow ? selectedPendingCount : selectedCount }}</strong>
          <small>{{ canApproveFlow ? '已选待审批记录可直接批量处理' : '当前账号以我的申请与跟进为主' }}</small>
        </div>
      </div>
    </template>

    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="关键字">
        <a-input v-model:value="query.keyword" placeholder="标题/申请人/备注" allow-clear style="width: 240px" />
      </a-form-item>
      <a-form-item label="类型">
        <a-select v-model:value="query.type" :options="typeOptions" allow-clear style="width: 160px" />
      </a-form-item>
      <a-form-item v-if="canApproveFlow" label="审批角色">
        <a-select v-model:value="query.currentApproverRole" :options="approverRoleOptions" allow-clear style="width: 180px" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select
          v-model:value="query.status"
          :options="statusOptions"
          :disabled="isStatusLocked"
          :placeholder="statusSelectPlaceholder"
          allow-clear
          style="width: 160px"
        />
      </a-form-item>
      <a-form-item label="视角">
        <a-radio-group v-model:value="approvalScope" button-style="solid" size="small">
          <a-radio-button value="MY">我的申请</a-radio-button>
          <a-radio-button v-if="canApproveFlow" value="PENDING_REVIEW">待我审批</a-radio-button>
          <a-radio-button v-if="canApproveFlow" value="ALL">全部审批</a-radio-button>
        </a-radio-group>
      </a-form-item>
      <a-form-item label="自动刷新">
        <a-switch v-model:checked="autoRefresh" />
      </a-form-item>
      <a-form-item label="高级筛选">
        <a-space wrap>
          <a-checkbox v-model:checked="query.urgedOnly">仅看已催办</a-checkbox>
          <a-checkbox v-model:checked="query.overdueOnly">仅看超时</a-checkbox>
        </a-space>
      </a-form-item>
      <template #extra>
        <div class="approval-commandbar">
          <div class="command-group">
            <span class="action-group-title">快捷操作</span>
            <a-space wrap>
              <a-button type="primary" @click="openCreate">新增审批</a-button>
              <a-button @click="downloadExport">导出CSV</a-button>
            </a-space>
          </div>
          <div class="command-group">
            <span class="action-group-title">单条处理</span>
            <a-space wrap>
              <a-button :disabled="!selectedSingleRecord || !isSelectedSinglePending" @click="editSelected">编辑</a-button>
              <a-button v-if="canApproveFlow" :disabled="!selectedSingleRecord || !isSelectedSinglePending" @click="approveSelected">同意</a-button>
              <a-button v-if="canApproveFlow" :disabled="!selectedSingleRecord || !isSelectedSinglePending" @click="rejectSelected">驳回</a-button>
              <a-button :disabled="!selectedSingleRecord || !isSelectedSingleRejected" @click="resubmitSelected">驳回后重提</a-button>
              <a-button :disabled="!selectedSingleRecord" @click="openSelectedTimeline">查看时间线</a-button>
              <a-button :disabled="!selectedSingleRecord" danger @click="removeSelected">删除</a-button>
            </a-space>
          </div>
          <div class="command-group">
            <span class="action-group-title">批量处理</span>
            <a-space wrap>
              <a-button v-if="canApproveFlow" :disabled="selectedPendingCount === 0" @click="batchApprove">批量同意</a-button>
              <a-button v-if="canApproveFlow" :disabled="selectedPendingCount === 0" @click="batchReject">批量驳回</a-button>
              <a-button v-if="canApproveFlow" :disabled="selectedPendingCount === 0" @click="batchUrge">批量催办</a-button>
              <a-button :disabled="selectedCount === 0" danger @click="batchRemove">批量删除</a-button>
            </a-space>
            <div class="command-group-meta">
              <a-tag color="blue">已勾选 {{ selectedCount }} 条</a-tag>
              <span class="selection-tip">{{ canApproveFlow ? '批量审批仅对待审批生效' : '当前账号仅可发起和编辑本人申请' }}</span>
            </div>
          </div>
        </div>
      </template>
    </SearchForm>

    <section class="surface-toolbar approval-toolbar">
      <div class="surface-toolbar-title">
        <strong>审批工作台</strong>
        <span>当前视角为 {{ approvalScopeLabel }}，优先处理超时待审、催办单和需要你立即决策的事项。</span>
      </div>
      <a-space wrap>
        <a-tag color="processing">视角 {{ approvalScopeLabel }}</a-tag>
        <a-tag color="gold">已催办 {{ urgedRecordCount }}</a-tag>
        <a-tag color="volcano">在途超时 {{ overduePendingCount }}</a-tag>
        <span class="selection-pill">已勾选 {{ selectedCount }} 条</span>
      </a-space>
    </section>

    <a-alert
      v-if="lifecycleContext.active"
      type="info"
      show-icon
      style="margin-bottom: 12px"
      :message="lifecycleContext.message"
    >
      <template #description>
        <a-space wrap>
          <a-tag color="blue">场景：入住状态变更联动</a-tag>
          <a-button type="link" size="small" @click="applySummaryFilter('timeoutPending')">查看超时待审</a-button>
          <a-button type="link" size="small" @click="applySummaryFilter('pending')">查看待我审批</a-button>
        </a-space>
      </template>
    </a-alert>

    <StatefulBlock :loading="summaryLoading" :error="summaryError" :empty="false" @retry="fetchData">
      <a-space v-if="canApproveFlow" wrap style="margin-bottom: 10px">
        <a-tag :color="!query.currentApproverRole ? 'blue' : 'default'" style="cursor: pointer" @click="setApproverRole(undefined)">
          全部角色
        </a-tag>
        <a-tag v-for="item in approverRoleOptions" :key="item.value" :color="query.currentApproverRole === item.value ? 'blue' : 'default'" style="cursor: pointer" @click="setApproverRole(item.value)">
          {{ item.label }}
        </a-tag>
      </a-space>
      <a-row :gutter="[12, 12]" style="margin-bottom: 12px">
        <a-col :xs="12" :lg="3">
          <a-card class="card-elevated summary-filter-card" :class="{ active: activeSummaryFilter === 'total' }" :bordered="false" size="small" @click="applySummaryFilter('total')">
            <a-statistic title="总单数" :value="summary.totalCount || 0" />
          </a-card>
        </a-col>
        <a-col :xs="12" :lg="3">
          <a-card class="card-elevated summary-filter-card" :class="{ active: activeSummaryFilter === 'pending' }" :bordered="false" size="small" @click="applySummaryFilter('pending')">
            <a-statistic title="待审批" :value="summary.pendingCount || 0" />
          </a-card>
        </a-col>
        <a-col :xs="12" :lg="3">
          <a-card class="card-elevated summary-filter-card" :class="{ active: activeSummaryFilter === 'approved' }" :bordered="false" size="small" @click="applySummaryFilter('approved')">
            <a-statistic title="已通过" :value="summary.approvedCount || 0" />
          </a-card>
        </a-col>
        <a-col :xs="12" :lg="3">
          <a-card class="card-elevated summary-filter-card" :class="{ active: activeSummaryFilter === 'rejected' }" :bordered="false" size="small" @click="applySummaryFilter('rejected')">
            <a-statistic title="已驳回" :value="summary.rejectedCount || 0" />
          </a-card>
        </a-col>
        <a-col :xs="12" :lg="3">
          <a-card class="card-elevated summary-filter-card" :class="{ active: activeSummaryFilter === 'timeoutPending' }" :bordered="false" size="small" @click="applySummaryFilter('timeoutPending')">
            <a-statistic title="超时待审" :value="summary.timeoutPendingCount || 0" />
          </a-card>
        </a-col>
        <a-col :xs="12" :lg="3">
          <a-card class="card-elevated summary-filter-card" :class="{ active: activeSummaryFilter === 'leavePending' }" :bordered="false" size="small" @click="applySummaryFilter('leavePending')">
            <a-statistic title="请假待审" :value="summary.leavePendingCount || 0" />
          </a-card>
        </a-col>
        <a-col :xs="12" :lg="3">
          <a-card class="card-elevated summary-filter-card" :class="{ active: activeSummaryFilter === 'reimbursePending' }" :bordered="false" size="small" @click="applySummaryFilter('reimbursePending')">
            <a-statistic title="报销待审" :value="summary.reimbursePendingCount || 0" />
          </a-card>
        </a-col>
        <a-col :xs="12" :lg="3">
          <a-card class="card-elevated summary-filter-card" :class="{ active: activeSummaryFilter === 'purchasePending' }" :bordered="false" size="small" @click="applySummaryFilter('purchasePending')">
            <a-statistic title="采购待审" :value="summary.purchasePendingCount || 0" />
          </a-card>
        </a-col>
        <a-col :xs="12" :lg="3">
          <a-card class="card-elevated summary-filter-card" :class="{ active: activeSummaryFilter === 'marketingPlanPending' }" :bordered="false" size="small" @click="applySummaryFilter('marketingPlanPending')">
            <a-statistic title="营销待审" :value="summary.marketingPlanPendingCount || 0" />
          </a-card>
        </a-col>
        <a-col :xs="12" :lg="3">
          <a-card class="card-elevated summary-filter-card" :class="{ active: activeSummaryFilter === 'urged' }" :bordered="false" size="small" @click="applySummaryFilter('urged')">
            <a-statistic title="已催办" :value="urgedRecordCount" />
          </a-card>
        </a-col>
        <a-col :xs="12" :lg="3">
          <a-card class="card-elevated summary-filter-card" :class="{ active: activeSummaryFilter === 'overdue' }" :bordered="false" size="small" @click="applySummaryFilter('overdue')">
            <a-statistic title="在途超时" :value="overduePendingCount" />
          </a-card>
        </a-col>
      </a-row>
      <a-alert
        v-if="(summary.timeoutPendingCount || 0) > 0"
        type="warning"
        show-icon
        style="margin-bottom: 12px"
        :message="`审批提醒：存在 ${summary.timeoutPendingCount || 0} 条超时待审批单。`"
      />
    </StatefulBlock>

    <StatefulBlock :loading="loading" :error="tableError" :empty="!loading && !tableError && rows.length === 0" empty-text="暂无审批记录" @retry="fetchData">
      <DataTable
        rowKey="id"
        :row-selection="rowSelection"
        :columns="columns"
        :data-source="rows"
        :row-class-name="approvalRowClassName"
        :loading="false"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #toolbar>
          <div class="table-head">
            <div>
              <strong>审批单列表</strong>
              <span>支持批量审批、时间线查看、驳回后重提和角色责任追踪。</span>
            </div>
            <a-space wrap>
              <a-tag color="processing">第 {{ pagination.current }} / {{ totalPageCount }} 页</a-tag>
              <a-tag color="default">共 {{ pagination.total }} 条</a-tag>
            </a-space>
          </div>
        </template>
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'approvalType'">
            {{ typeLabel(record.approvalType) }}
          </template>
          <template v-else-if="column.key === 'title'">
            <div class="approval-title-cell">
              <strong>{{ record.title || '-' }}</strong>
              <span>{{ approvalPeriodText(record) }}</span>
            </div>
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="record.status === 'APPROVED' ? 'green' : record.status === 'REJECTED' ? 'red' : 'orange'">
              {{ statusLabel(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'currentNode'">
            {{ currentNode(record) }}
          </template>
          <template v-else-if="column.key === 'currentApprover'">
            <div class="approver-cell">
              <a-tag :color="approverStatusColor(record)">{{ approverStatusText(record) }}</a-tag>
              <div class="approver-name">{{ currentApprover(record) }}</div>
            </div>
            <div class="urge-meta">{{ urgeMetaText(record) }}</div>
          </template>
          <template v-else-if="column.key === 'approvalRounds'">
            {{ approvalRoundCount(record) }}
          </template>
          <template v-else-if="column.key === 'amount'">
            <span class="approval-amount">{{ formatApprovalAmount(record.amount) }}</span>
          </template>
          <template v-else-if="column.key === 'approvalOpinion'">
            {{ approvalOpinion(record) }}
          </template>
          <template v-else-if="column.key === 'annualLeaveCount'">
            {{ record.approvalType === 'LEAVE' && record.status === 'APPROVED' ? annualLeaveCount(record) : '-' }}
          </template>
        </template>
      </DataTable>
    </StatefulBlock>

    <a-drawer
      v-model:open="timelineOpen"
      :title="`审批时间线 · ${timelineRecord?.title || ''}`"
      width="520"
      placement="right"
    >
      <a-timeline>
        <a-timeline-item v-for="item in timelineItems" :key="item.key" :color="item.color">
          <div class="timeline-title">{{ item.title }}</div>
          <div class="timeline-desc">{{ item.desc }}</div>
          <div class="timeline-time">{{ item.timeText }}</div>
        </a-timeline-item>
      </a-timeline>
    </a-drawer>

    <a-modal v-model:open="editOpen" title="审批" @ok="submit" :confirm-loading="saving" width="700px">
      <a-form layout="vertical">
        <a-form-item label="类型" required>
          <a-select v-model:value="form.approvalType" :options="typeOptions" />
        </a-form-item>
        <a-form-item label="标题" required>
          <a-input v-model:value="form.title" />
        </a-form-item>
        <a-form-item label="申请人" required>
          <a-input v-model:value="form.applicantName" disabled />
          <div class="upload-hint">申请人固定为当前登录人，系统自动带入并锁定。</div>
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="金额">
              <a-input-number v-model:value="form.amount" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态">
              <a-select v-model:value="form.status" :options="editableStatusOptions" disabled />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="开始时间">
              <a-date-picker v-model:value="form.startTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="结束时间">
              <a-date-picker v-model:value="form.endTime" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-alert
          show-icon
          type="info"
          style="margin-bottom: 12px"
          :message="workflowTitle"
          :description="workflowHint"
        />
        <a-steps size="small" :current="Math.max(0, workflowSteps.length - 1)" style="margin-bottom: 12px">
          <a-step v-for="step in workflowSteps" :key="step" :title="step" />
        </a-steps>
        <template v-if="form.approvalType === 'LEAVE'">
          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item label="请假类型">
                <a-select v-model:value="workflowForm.leaveType">
                  <a-select-option value="ANNUAL">年假</a-select-option>
                  <a-select-option value="SICK">病假</a-select-option>
                  <a-select-option value="PERSONAL">事假</a-select-option>
                  <a-select-option value="COMPENSATORY">调休</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="是否影响排班">
                <a-switch v-model:checked="workflowForm.shiftAffected" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="交接人">
                <a-input v-model:value="workflowForm.handoverName" placeholder="填写交接同事" />
              </a-form-item>
            </a-col>
          </a-row>
          <a-alert
            type="warning"
            show-icon
            style="margin-bottom: 12px"
            :message="`系统自动计算请假天数：${leaveDays} 天`"
            :description="needDeanApproval ? '超过 3 天，已自动追加院长审批节点。' : '3 天及以内，按部门主管 + 人事流程。'"
          />
          <a-alert
            type="info"
            show-icon
            message="管理制度：龟峰颐养中心请假申请须知"
            style="margin-bottom: 12px"
          />
          <a-card size="small" class="leave-policy-card" style="margin-bottom: 12px">
            <a-typography-paragraph>
              为规范管理，员工请假须提前在系统提交申请，注明事由及起止时间，经审批通过后方可休假。3天以内由部门主管审批，3天以上报中心负责人审批。紧急情况应先行报备，并于返岗后1个工作日内补办手续。
            </a-typography-paragraph>
            <a-typography-paragraph>
              事假须提供相关证明材料；病假须提交三级甲等及以上医院出具的就诊病例或医嘱单；婚假须为在职期间依法登记结婚，并提供结婚证复印件。产假、护理假按国家及地方规定执行，需提交医院证明；直系亲属去世可申请丧假，并提供相关证明材料。
            </a-typography-paragraph>
            <a-typography-paragraph>
              所有材料须真实有效，未履行审批手续擅自离岗者按旷工处理。请提前做好工作交接。
            </a-typography-paragraph>
          </a-card>
          <a-space style="margin-bottom: 10px" wrap>
            <a-button size="small" @click="openPolicy('/oa/knowledge?category=制度规范')">查看管理制度</a-button>
            <a-button size="small" @click="openPolicy('/oa/document?folder=请假制度')">查看制度文档</a-button>
          </a-space>
          <a-form-item label="请假证明上传">
            <a-upload :show-upload-list="false" :before-upload="beforeUploadLeaveProof">
              <a-button :loading="proofUploading">上传证明</a-button>
            </a-upload>
            <div v-if="leaveProofs.length" class="proof-list">
              <div v-for="(file, idx) in leaveProofs" :key="`${file.url}-${idx}`" class="proof-item">
                <a :href="file.url" target="_blank" rel="noopener noreferrer">{{ file.name }}</a>
                <a-button type="link" danger size="small" @click="removeLeaveProof(idx)">删除</a-button>
              </div>
            </div>
          </a-form-item>
          <a-form-item>
            <a-checkbox v-model:checked="leavePolicyAcknowledged">
              我已阅读并同意制度
            </a-checkbox>
          </a-form-item>
        </template>
        <template v-if="form.approvalType === 'REIMBURSE' || form.approvalType === 'OVERTIME'">
          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item label="报销类别">
                <a-select v-model:value="workflowForm.reimburseCategory">
                  <a-select-option value="TRAVEL">差旅</a-select-option>
                  <a-select-option value="PURCHASE">采购</a-select-option>
                  <a-select-option value="ACTIVITY">活动费用</a-select-option>
                  <a-select-option value="OVERTIME">加班</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="是否有预算">
                <a-switch v-model:checked="workflowForm.hasBudget" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="发票张数">
                <a-input-number v-model:value="workflowForm.invoiceCount" :min="0" style="width: 100%" />
              </a-form-item>
            </a-col>
          </a-row>
        </template>
        <template v-if="form.approvalType === 'PURCHASE'">
          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item label="采购品类">
                <a-select v-model:value="workflowForm.purchaseCategory">
                  <a-select-option value="FOOD">食材</a-select-option>
                  <a-select-option value="MEDICAL">医疗耗材</a-select-option>
                  <a-select-option value="CARE">护理用品</a-select-option>
                  <a-select-option value="ASSET">固定资产</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="仓库是否已核库存">
                <a-switch v-model:checked="workflowForm.stockChecked" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="是否超预算">
                <a-switch v-model:checked="workflowForm.overBudget" />
              </a-form-item>
            </a-col>
          </a-row>
        </template>
        <template v-if="form.approvalType === 'MATERIAL_APPLY'">
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="物资类型">
                <a-select v-model:value="workflowForm.materialCategory">
                  <a-select-option value="CARE">护理用品</a-select-option>
                  <a-select-option value="OFFICE">办公用品</a-select-option>
                  <a-select-option value="MEDICAL">医疗耗材</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="低库存自动触发采购">
                <a-switch v-model:checked="workflowForm.autoPurchaseWhenLowStock" />
              </a-form-item>
            </a-col>
          </a-row>
        </template>
        <template v-if="form.approvalType === 'INCOME_PROOF' || form.approvalType === 'BANK_CARD'">
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="证明编号">
                <a-input v-model:value="workflowForm.proofNo" placeholder="系统可自动回填编号" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="行政盖章方式">
                <a-select v-model:value="workflowForm.sealMode">
                  <a-select-option value="ONLINE">电子章</a-select-option>
                  <a-select-option value="OFFLINE">线下盖章</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </a-row>
        </template>
        <template v-if="form.approvalType === 'POINTS_CASH'">
          <a-alert
            type="warning"
            show-icon
            style="margin-bottom: 12px"
            message="积分兑现金规则：每 300 积分可兑换 10 元，需院长审批后自动扣减积分。"
          />
          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item label="兑换积分">
                <a-input-number v-model:value="workflowForm.redeemPoints" :min="300" :step="300" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item label="可兑现金额(元)">
                <a-input-number :value="redeemCash" disabled style="width: 100%" />
              </a-form-item>
            </a-col>
          </a-row>
        </template>
        <a-form-item label="附件上传">
          <a-upload :show-upload-list="false" :before-upload="beforeUploadCommonProof">
            <a-button :loading="proofUploading">上传附件</a-button>
          </a-upload>
          <div v-if="commonProofs.length" class="proof-list">
            <div v-for="(file, idx) in commonProofs" :key="`${file.url}-${idx}`" class="proof-item">
              <a :href="file.url" target="_blank" rel="noopener noreferrer">{{ file.name }}</a>
              <a-button type="link" danger size="small" @click="removeCommonProof(idx)">删除</a-button>
            </div>
          </div>
        </a-form-item>
        <a-divider orientation="left" plain>驳回后补充材料（支持二次提交）</a-divider>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="事件证明人（院内）">
              <a-input v-model:value="workflowForm.insideWitness" placeholder="姓名 + 联系方式" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="事件证明人（院外）">
              <a-input v-model:value="workflowForm.outsideWitness" placeholder="姓名 + 联系方式" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="补充证明上传">
          <a-upload :show-upload-list="false" :before-upload="beforeUploadSupplementProof">
            <a-button :loading="proofUploading">上传补充证明</a-button>
          </a-upload>
          <div v-if="supplementProofs.length" class="proof-list">
            <div v-for="(file, idx) in supplementProofs" :key="`${file.url}-${idx}`" class="proof-item">
              <a :href="file.url" target="_blank" rel="noopener noreferrer">{{ file.name }}</a>
              <a-button type="link" danger size="small" @click="removeSupplementProof(idx)">删除</a-button>
            </div>
          </div>
        </a-form-item>
        <a-form-item label="扩展表单(JSON，可选)">
          <a-textarea v-model:value="form.formData" :rows="2" placeholder='可留空，系统会自动生成流程字段' />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="form.remark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { useUserStore } from '../../stores/user'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { hasMinisterOrHigher } from '../../utils/roleAccess'
import {
  getApprovalSummary,
  getApprovalPage,
  createApproval,
  updateApproval,
  approveApproval,
  rejectApproval,
  deleteApproval,
  batchApproveApproval,
  batchRejectApproval,
  batchDeleteApproval,
  exportApproval,
  uploadOaFile,
  createOaTask
} from '../../api/oa'
import type { OaApproval, OaApprovalSummary } from '../../types'

type ProofFile = { name: string; url: string }

const userStore = useUserStore()
const loading = ref(false)
const tableError = ref('')
const summaryLoading = ref(false)
const summaryError = ref('')
const rows = ref<OaApproval[]>([])
const route = useRoute()
const query = reactive({
  keyword: '',
  type: undefined as string | undefined,
  currentApproverRole: undefined as string | undefined,
  status: undefined as string | undefined,
  urgedOnly: false,
  overdueOnly: false,
  pageNo: 1,
  pageSize: 10
})
const approvalScope = ref<'MY' | 'PENDING_REVIEW' | 'ALL'>('MY')
const activeSummaryFilter = ref<
  | 'total'
  | 'pending'
  | 'approved'
  | 'rejected'
  | 'timeoutPending'
  | 'leavePending'
  | 'reimbursePending'
  | 'purchasePending'
  | 'marketingPlanPending'
  | 'urged'
  | 'overdue'
>('total')
const autoRefresh = ref(true)
let autoRefreshTimer: number | undefined
const AUTO_REFRESH_INTERVAL_MS = 60 * 1000
const focusApprovalId = ref('')
const focusedOnce = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const selectedRowKeys = ref<string[]>([])
const summary = reactive<OaApprovalSummary>({
  totalCount: 0,
  pendingCount: 0,
  approvedCount: 0,
  rejectedCount: 0,
  timeoutPendingCount: 0,
  leavePendingCount: 0,
  reimbursePendingCount: 0,
  purchasePendingCount: 0
})

const columns = [
  { title: '类型', dataIndex: 'approvalType', key: 'approvalType', width: 120 },
  { title: '标题', dataIndex: 'title', key: 'title', width: 200 },
  { title: '申请人', dataIndex: 'applicantName', key: 'applicantName', width: 120 },
  { title: '当前环节', key: 'currentNode', width: 130 },
  { title: '当前审批人', key: 'currentApprover', width: 190 },
  { title: '审批轮次', key: 'approvalRounds', width: 96 },
  { title: '金额', dataIndex: 'amount', key: 'amount', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '审批意见', key: 'approvalOpinion', width: 220 },
  { title: '年度请假次数', key: 'annualLeaveCount', width: 130 }
]

const editOpen = ref(false)
const timelineOpen = ref(false)
const timelineRecord = ref<OaApproval | null>(null)
const saving = ref(false)
const proofUploading = ref(false)
const leaveProofs = ref<ProofFile[]>([])
const commonProofs = ref<ProofFile[]>([])
const supplementProofs = ref<ProofFile[]>([])
const leavePolicyAcknowledged = ref(false)
const form = reactive({
  id: undefined as string | undefined,
  approvalType: 'LEAVE',
  title: '',
  applicantName: '',
  amount: undefined as number | undefined,
  startTime: undefined as any,
  endTime: undefined as any,
  formData: '',
  status: 'PENDING',
  remark: ''
})
const workflowForm = reactive({
  leaveType: 'ANNUAL',
  shiftAffected: false,
  handoverName: '',
  reimburseCategory: 'TRAVEL',
  hasBudget: true,
  invoiceCount: 0,
  purchaseCategory: 'FOOD',
  stockChecked: false,
  overBudget: false,
  materialCategory: 'CARE',
  autoPurchaseWhenLowStock: true,
  proofNo: '',
  sealMode: 'ONLINE',
  insideWitness: '',
  outsideWitness: '',
  redeemPoints: 300
})

const typeOptions = [
  { label: '请假', value: 'LEAVE' },
  { label: '加班', value: 'OVERTIME' },
  { label: '报销', value: 'REIMBURSE' },
  { label: '采购', value: 'PURCHASE' },
  { label: '收入证明', value: 'INCOME_PROOF' },
  { label: '银行卡申请', value: 'BANK_CARD' },
  { label: '积分兑现金', value: 'POINTS_CASH' },
  { label: '物资申领', value: 'MATERIAL_APPLY' },
  { label: '用章申请', value: 'OFFICIAL_SEAL' },
  { label: '换班申请', value: 'SHIFT_CHANGE' },
  { label: '营销方案审批', value: 'MARKETING_PLAN' }
]
const approverRoleOptions = [
  { label: '部门主管', value: 'DEPT_MANAGER' },
  { label: '人事', value: 'HR' },
  { label: '财务', value: 'FINANCE' },
  { label: '院长', value: 'DEAN' },
  { label: '仓库', value: 'WAREHOUSE' },
  { label: '行政', value: 'ADMIN_OFFICE' },
  { label: '采购', value: 'PURCHASING' },
  { label: '归档', value: 'ARCHIVE' }
]
const statusOptions = [
  { label: '待审批', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' }
]
const isStatusLocked = computed(() => approvalScope.value === 'PENDING_REVIEW')
const statusSelectPlaceholder = computed(() => (isStatusLocked.value ? '待我审批视角固定为待审批' : '请选择状态'))
const editableStatusOptions = [{ label: '待审批', value: 'PENDING' }]
const lifecycleContext = computed(() => {
  const source = String(route.query.source || '').trim().toLowerCase()
  const scene = String(route.query.scene || '').trim().toLowerCase()
  const active = source === 'lifecycle' || scene === 'status-change'
  return {
    active,
    message: active ? '已按状态变更联动场景应用审批筛选，优先处理超时待审单。' : ''
  }
})
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((item) => String(item))
  }
}))
const selectedRecords = computed(() => rows.value.filter((item) => selectedRowKeys.value.includes(String(item.id))))
const selectedCount = computed(() => selectedRowKeys.value.length)
const selectedSingleRecord = computed(() => (selectedRecords.value.length === 1 ? selectedRecords.value[0] : null))
const isSelectedSinglePending = computed(() => selectedSingleRecord.value?.status === 'PENDING')
const isSelectedSingleRejected = computed(() => selectedSingleRecord.value?.status === 'REJECTED')
const selectedPendingCount = computed(() => selectedRecords.value.filter((item) => item.status === 'PENDING').length)
const selectedPendingIds = computed(() =>
  selectedRecords.value.filter((item) => item.status === 'PENDING').map((item) => String(item.id))
)
const urgedRecordCount = computed(() => rows.value.filter((item) => Number(parseFormData(item.formData)?.urgeCount || 0) > 0).length)
const overduePendingCount = computed(() => rows.value.filter((item) => isApprovalOverdue(item)).length)
const totalPageCount = computed(() => Math.max(1, Math.ceil(Number(pagination.total || 0) / Number(pagination.pageSize || 10))))
const approvalScopeLabel = computed(() => {
  if (approvalScope.value === 'PENDING_REVIEW') return '待我审批'
  if (approvalScope.value === 'ALL') return '全部审批'
  return '我的申请'
})
const timelineItems = computed(() => {
  const record = timelineRecord.value
  if (!record) return []
  const parsed = parseFormData(record.formData)
  const items: Array<{
    key: string
    title: string
    desc: string
    timeText: string
    color: string
    sortTime: number
    order: number
  }> = []
  const submittedAt = approvalSubmittedAt(record)
  const activeSince = approvalCurrentNodeEnteredAt(record)
  const finishedAt = approvalFinishedAt(record)
  const now = dayjs()
  const pushItem = (
    key: string,
    title: string,
    desc: string,
    rawTime: string | undefined,
    color: string,
    fallbackOrder: number
  ) => {
    const time = rawTime ? dayjs(rawTime) : null
    items.push({
      key,
      title,
      desc,
      timeText: formatTimelineTime(rawTime),
      color,
      sortTime: time && time.isValid() ? time.valueOf() : Number.MAX_SAFE_INTEGER,
      order: fallbackOrder
    })
  }
  items.push({
    key: 'created',
    title: '申请创建',
    desc: `${record.applicantName || '-'} 提交 ${typeLabel(record.approvalType)} 申请`,
    timeText: formatTimelineTime(submittedAt?.format('YYYY-MM-DDTHH:mm:ss')),
    color: 'blue',
    sortTime: submittedAt && submittedAt.isValid() ? submittedAt.valueOf() : 0,
    order: 0
  })
  const urgeCount = Number(parsed?.urgeCount || 0)
  const approverHistory = Array.isArray(parsed?.approverHistory) ? parsed.approverHistory : []
  approverHistory.slice(-6).forEach((entry: any, index: number) => {
    if (!entry) return
    pushItem(
      `approver-${index}-${entry?.at || ''}`,
      '责任人变更',
      `${entry?.from || '未分配'} → ${entry?.to || '未分配'}`,
      entry?.at,
      'gold',
      20 + index
    )
  })
  const urgeHistory = Array.isArray(parsed?.urgeHistory) ? parsed.urgeHistory : []
  urgeHistory.slice(-6).forEach((entry: any, index: number) => {
    if (!entry) return
    pushItem(
      `urge-history-${index}-${entry?.at || ''}`,
      '催办动作',
      `${entry?.by || '-'} 催办${entry?.count ? `（第${entry.count}次）` : ''}`,
      entry?.at,
      'orange',
      40 + index
    )
  })
  if (urgeCount > 0 && urgeHistory.length === 0) {
    pushItem(
      'urge',
      `催办记录（${urgeCount}次）`,
      parsed?.lastUrgedBy ? `最近催办人：${parsed.lastUrgedBy}` : '已发生催办',
      parsed?.lastUrgedAt,
      'orange',
      60
    )
  }
  const approvalRounds = Array.isArray(parsed?.approvalRounds) ? parsed.approvalRounds : []
  approvalRounds.slice(-8).forEach((entry: any, index: number) => {
    if (!entry) return
    const action = String(entry?.action || '').toUpperCase()
    const actionText = action === 'APPROVED' ? '通过' : action === 'REJECTED' ? '驳回' : action || '处理'
    pushItem(
      `round-${index}-${entry?.at || ''}`,
      `第${Number(entry?.round || index + 1)}轮审批 · ${actionText}`,
      `${entry?.nodeName || '未知节点'} · ${entry?.approverName || '-'}${entry?.remark ? `：${entry.remark}` : ''}`,
      entry?.at,
      action === 'REJECTED' ? 'red' : 'green',
      80 + index
    )
  })
  if (parsed?.lastApprovalRemark && approvalRounds.length === 0) {
    pushItem(
      'remark',
      '审批意见更新',
      parsed.lastApprovalRemark,
      parsed.lastApprovalAt,
      'cyan',
      100
    )
  }
  const notifyHistory = Array.isArray(parsed?.notifyHistory) ? parsed.notifyHistory : []
  notifyHistory.slice(-8).forEach((entry: any, index: number) => {
    if (!entry) return
    const roleText = approverRoleLabel(entry?.toRole)
    const nameText = entry?.toName ? String(entry.toName).trim() : ''
    const approverText = nameText && roleText && roleText !== '待分配'
      ? `${nameText}（${roleText}）`
      : nameText || roleText || '待分配'
    pushItem(
      `notify-${index}-${entry?.at || ''}`,
      '自动同步审批人',
      `${approverText}${entry?.message ? `：${entry.message}` : ''}`,
      entry?.at,
      'blue',
      120 + index
    )
  })
  if (record.status === 'PENDING' && activeSince && activeSince.isValid()) {
    const pendingHours = Math.max(0, now.diff(activeSince, 'hour'))
    const overdue = pendingHours > 48
    items.push({
      key: 'pending-duration',
      title: overdue ? '审批超时预警' : '在途处理时长',
      desc: overdue ? `已超时 ${pendingHours - 48} 小时（当前环节共 ${pendingHours} 小时）` : `当前环节已在途 ${pendingHours} 小时`,
      timeText: `从 ${formatTimelineTime(activeSince.format('YYYY-MM-DDTHH:mm:ss'))} 开始计时`,
      color: overdue ? 'red' : 'blue',
      sortTime: Number.MAX_SAFE_INTEGER - 2,
      order: 1000
    })
  }
  if (record.status === 'APPROVED') {
    items.push({
      key: 'approved',
      title: '审批通过',
      desc: record.remark || '流程结束',
      timeText: formatTimelineTime(finishedAt?.format('YYYY-MM-DDTHH:mm:ss')),
      color: 'green',
      sortTime: finishedAt && finishedAt.isValid() ? finishedAt.valueOf() : Number.MAX_SAFE_INTEGER - 3,
      order: 1100
    })
  }
  if (record.status === 'REJECTED') {
    items.push({
      key: 'rejected',
      title: '审批驳回',
      desc: record.remark || '已驳回，待重提',
      timeText: formatTimelineTime(finishedAt?.format('YYYY-MM-DDTHH:mm:ss')),
      color: 'red',
      sortTime: finishedAt && finishedAt.isValid() ? finishedAt.valueOf() : Number.MAX_SAFE_INTEGER - 3,
      order: 1100
    })
  }
  if (submittedAt && submittedAt.isValid()) {
    const durationEnd = finishedAt && finishedAt.isValid() ? finishedAt : now
    const durationHours = Math.max(0, durationEnd.diff(submittedAt, 'hour'))
    items.push({
      key: 'duration',
      title: '流程总耗时',
      desc: `${durationHours} 小时`,
      timeText: finishedAt && finishedAt.isValid()
        ? `截至 ${formatTimelineTime(finishedAt.format('YYYY-MM-DDTHH:mm:ss'))}`
        : '仍在处理中',
      color: 'purple',
      sortTime: Number.MAX_SAFE_INTEGER - 1,
      order: 1200
    })
  }
  return items
    .sort((a, b) => {
      if (a.sortTime !== b.sortTime) return a.sortTime - b.sortTime
      return a.order - b.order
    })
    .map(({ sortTime, order, ...item }) => item)
})
const canApproveFlow = computed(() => {
  const roles = (userStore.roles || []).map((item) => String(item || '').toUpperCase())
  return hasMinisterOrHigher(roles)
})

const workflowStepsMap: Record<string, string[]> = {
  LEAVE: ['员工发起申请', '部门主管审批', '人事确认', '必要时院长审批', '自动归档'],
  OVERTIME: ['申请人提交', '部门主管', '财务审核', '院长审批', '出纳付款', '归档'],
  REIMBURSE: ['申请人提交', '部门主管', '财务审核', '院长审批', '出纳付款', '归档'],
  PURCHASE: ['申请人', '部门主管', '仓库确认库存', '财务确认预算', '院长审批', '采购执行', '入库验收'],
  MATERIAL_APPLY: ['申请人', '部门主管', '仓库审核', '出库确认', '归档'],
  INCOME_PROOF: ['员工申请', '人事审核', '财务确认收入', '行政盖章', '归档'],
  BANK_CARD: ['员工申请', '人事审核', '财务确认收入', '行政盖章', '归档'],
  POINTS_CASH: ['员工申请', '院长审批', '自动扣减积分', '财务发放现金', '归档'],
  OFFICIAL_SEAL: ['申请人提交', '行政审核', '院长审批', '盖章归档'],
  SHIFT_CHANGE: ['申请人提交', '被换班员工确认', '人事审批', '部门主管审批', '归档'],
  MARKETING_PLAN: ['营销经理提交方案', '院长审批', '审批意见回写方案', '发布执行']
}
const workflowHintMap: Record<string, string> = {
  LEAVE: '请假时间系统自动计算，超过3天会自动追加院长审批提醒。',
  OVERTIME: '加班/报销需校验预算、发票合规性与是否重复报销。',
  REIMBURSE: '报销单会进入财务核验，异常项需补充票据和证明人。',
  PURCHASE: '采购审批通过后建议同步生成采购单和入库单。',
  MATERIAL_APPLY: '领用会触发库存扣减，低于安全库存时建议自动触发采购流程。',
  INCOME_PROOF: '系统应自动生成标准模板并记录证明编号，电子归档至人事档案。',
  BANK_CARD: '银行卡办理同收入证明流程，支持追加辅助证明材料。',
  POINTS_CASH: '按 300 积分 = 10 元自动换算，审批通过后自动扣减积分余额。',
  OFFICIAL_SEAL: '请明确用途、对象、时间，避免跨用途盖章风险。',
  SHIFT_CHANGE: '换班申请需先由被换班员工确认，同意后再进入人事与主管审批，最终自动更新双方排班与日程。',
  MARKETING_PLAN: '营销方案提交后会同步到营销管理的审批状态，并支持发布/驳回意见联动。'
}
const workflowSteps = computed(() => workflowStepsMap[form.approvalType] || ['申请', '审批', '归档'])
const workflowTitle = computed(() => {
  const item = typeOptions.find((option) => option.value === form.approvalType)
  return `流程模板：${item?.label || '审批'}`
})
const workflowHint = computed(() => workflowHintMap[form.approvalType] || '按基础审批流程执行。')
const leaveDays = computed(() => {
  if (!form.startTime || !form.endTime) return 0
  const diffHours = Math.max(0, dayjs(form.endTime).diff(dayjs(form.startTime), 'hour', true))
  return Math.max(1, Math.ceil(diffHours / 24))
})
const needDeanApproval = computed(() => leaveDays.value > 3)
const redeemCash = computed(() => Math.floor(Number(workflowForm.redeemPoints || 0) / 300) * 10)

function statusLabel(status?: string) {
  if (status === 'APPROVED') return '已通过'
  if (status === 'REJECTED') return '已驳回'
  return '待审批'
}

function typeLabel(type?: string) {
  const value = String(type || '').toUpperCase()
  const option = typeOptions.find((item) => item.value === value)
  return option?.label || type || '-'
}

function approvalPeriodText(record: OaApproval) {
  const start = record.startTime ? dayjs(record.startTime).format('MM-DD HH:mm') : ''
  const end = record.endTime ? dayjs(record.endTime).format('MM-DD HH:mm') : ''
  if (start && end) {
    return `${start} - ${end}`
  }
  if (start) {
    return `提交时间 ${start}`
  }
  return '待补充时间范围'
}

function formatApprovalAmount(value?: number | null) {
  if (value == null || Number.isNaN(Number(value))) {
    return '-'
  }
  return `${Number(value).toFixed(2)} 元`
}

function formatTimelineTime(value?: string) {
  if (!value) return '时间未知'
  const time = dayjs(String(value).replace(' ', 'T'))
  if (!time.isValid()) return '时间未知'
  return time.format('YYYY-MM-DD HH:mm')
}

function parseApprovalMoment(value?: string) {
  if (!value) return null
  const time = dayjs(String(value).replace(' ', 'T'))
  return time.isValid() ? time : null
}

function approvalSubmittedAt(record: OaApproval) {
  const parsed = parseFormData(record.formData)
  return parseApprovalMoment(parsed?.submittedAt || record.createTime || record.startTime)
}

function approvalCurrentNodeEnteredAt(record: OaApproval) {
  const parsed = parseFormData(record.formData)
  const currentNodeEnteredAt = parseApprovalMoment(parsed?.currentNodeEnteredAt)
  if (currentNodeEnteredAt) return currentNodeEnteredAt
  const notifyHistory = Array.isArray(parsed?.notifyHistory) ? parsed.notifyHistory : []
  for (let i = notifyHistory.length - 1; i >= 0; i -= 1) {
    const time = parseApprovalMoment(notifyHistory[i]?.at)
    if (time) return time
  }
  return approvalSubmittedAt(record)
}

function approvalFinishedAt(record: OaApproval) {
  const parsed = parseFormData(record.formData)
  return parseApprovalMoment(parsed?.lastApprovalAt || record.updateTime || record.endTime)
}

async function fetchData() {
  loading.value = true
  summaryLoading.value = true
  tableError.value = ''
  summaryError.value = ''
  try {
    const applicantId = resolveApplicantIdParam()
    const effectiveStatus = approvalScope.value === 'PENDING_REVIEW' ? 'PENDING' : query.status
    const params = {
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: effectiveStatus,
      type: query.type,
      currentApproverRole: query.currentApproverRole,
      applicantId,
      pendingMine: approvalScope.value === 'PENDING_REVIEW',
      keyword: query.keyword || undefined
    }
    const [res, sum] = await Promise.all([
      getApprovalPage(params),
      getApprovalSummary(params)
    ])
    const rawRows = (res.list || []).map((item) => ({
      ...item,
      id: String(item.id)
    }))
    rows.value = rawRows.filter((item) => {
      if (approvalScope.value === 'PENDING_REVIEW' && !isCurrentUserApprover(item)) return false
      const parsed = parseFormData(item.formData)
      if (query.urgedOnly && Number(parsed?.urgeCount || 0) <= 0) return false
      if (query.overdueOnly && !isApprovalOverdue(item)) return false
      return true
    })
    pagination.total = res.total || res.list.length
    selectedRowKeys.value = []
    if (focusApprovalId.value && !focusedOnce.value) {
      const matched = rows.value.find((item) => String(item.id) === String(focusApprovalId.value))
      if (matched) {
        selectedRowKeys.value = [String(matched.id)]
        timelineRecord.value = matched
        timelineOpen.value = true
        focusedOnce.value = true
      }
    }
    Object.assign(summary, sum || {})
  } catch (error: any) {
    const text = error?.message || '加载失败，请稍后重试'
    tableError.value = text
    summaryError.value = text
  } finally {
    loading.value = false
    summaryLoading.value = false
  }
}

function isApprovalOverdue(item: OaApproval) {
  if (item.status !== 'PENDING') return false
  const activeSince = approvalCurrentNodeEnteredAt(item)
  if (!activeSince || !activeSince.isValid()) return false
  return dayjs().diff(activeSince, 'hour') > 48
}

function approvalRowClassName(record: OaApproval) {
  return isApprovalOverdue(record) ? 'is-urgent-row' : ''
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function onReset() {
  query.keyword = ''
  query.status = undefined
  query.type = undefined
  query.currentApproverRole = undefined
  query.urgedOnly = false
  query.overdueOnly = false
  activeSummaryFilter.value = 'total'
  query.pageNo = 1
  pagination.current = 1
  approvalScope.value = canApproveFlow.value ? 'ALL' : 'MY'
  fetchData()
}

function setApproverRole(role?: string) {
  query.currentApproverRole = role
  activeSummaryFilter.value = 'total'
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function applySummaryFilter(filterKey: typeof activeSummaryFilter.value) {
  activeSummaryFilter.value = filterKey
  query.pageNo = 1
  pagination.current = 1
  if (filterKey === 'total') {
    query.type = undefined
    query.status = approvalScope.value === 'PENDING_REVIEW' ? 'PENDING' : undefined
    query.urgedOnly = false
    query.overdueOnly = false
    fetchData()
    return
  }
  if (filterKey === 'pending') {
    query.status = 'PENDING'
    query.type = undefined
    query.urgedOnly = false
    query.overdueOnly = false
    fetchData()
    return
  }
  if (filterKey === 'approved') {
    query.status = 'APPROVED'
    query.type = undefined
    query.urgedOnly = false
    query.overdueOnly = false
    fetchData()
    return
  }
  if (filterKey === 'rejected') {
    query.status = 'REJECTED'
    query.type = undefined
    query.urgedOnly = false
    query.overdueOnly = false
    fetchData()
    return
  }
  if (filterKey === 'timeoutPending' || filterKey === 'overdue') {
    query.status = 'PENDING'
    query.type = undefined
    query.urgedOnly = false
    query.overdueOnly = true
    fetchData()
    return
  }
  if (filterKey === 'leavePending') {
    query.status = 'PENDING'
    query.type = 'LEAVE'
    query.urgedOnly = false
    query.overdueOnly = false
    fetchData()
    return
  }
  if (filterKey === 'reimbursePending') {
    query.status = 'PENDING'
    query.type = 'REIMBURSE'
    query.urgedOnly = false
    query.overdueOnly = false
    fetchData()
    return
  }
  if (filterKey === 'purchasePending') {
    query.status = 'PENDING'
    query.type = 'PURCHASE'
    query.urgedOnly = false
    query.overdueOnly = false
    fetchData()
    return
  }
  if (filterKey === 'marketingPlanPending') {
    query.status = 'PENDING'
    query.type = 'MARKETING_PLAN'
    query.urgedOnly = false
    query.overdueOnly = false
    fetchData()
    return
  }
  if (filterKey === 'urged') {
    query.urgedOnly = true
    query.overdueOnly = false
    fetchData()
  }
}

function stopAutoRefreshTimer() {
  if (autoRefreshTimer !== undefined) {
    window.clearInterval(autoRefreshTimer)
    autoRefreshTimer = undefined
  }
}

function startAutoRefreshTimer() {
  if (!autoRefresh.value || autoRefreshTimer !== undefined) {
    return
  }
  autoRefreshTimer = window.setInterval(() => {
    if (loading.value || summaryLoading.value || document.visibilityState !== 'visible') return
    fetchData()
  }, AUTO_REFRESH_INTERVAL_MS)
}

function resolveApplicantIdParam() {
  const myId = userStore.staffInfo?.id
  if (!canApproveFlow.value) {
    return myId
  }
  if (approvalScope.value === 'MY') {
    return myId
  }
  return undefined
}

function resetWorkflowForm() {
  workflowForm.leaveType = 'ANNUAL'
  workflowForm.shiftAffected = false
  workflowForm.handoverName = ''
  workflowForm.reimburseCategory = 'TRAVEL'
  workflowForm.hasBudget = true
  workflowForm.invoiceCount = 0
  workflowForm.purchaseCategory = 'FOOD'
  workflowForm.stockChecked = false
  workflowForm.overBudget = false
  workflowForm.materialCategory = 'CARE'
  workflowForm.autoPurchaseWhenLowStock = true
  workflowForm.proofNo = ''
  workflowForm.sealMode = 'ONLINE'
  workflowForm.insideWitness = ''
  workflowForm.outsideWitness = ''
  workflowForm.redeemPoints = 300
}

function parseFormData(raw?: string) {
  if (!raw || !raw.trim()) return {} as Record<string, any>
  try {
    const parsed = JSON.parse(raw)
    return parsed && typeof parsed === 'object' ? parsed : {}
  } catch {
    return {}
  }
}

function approvalOpinion(record: OaApproval) {
  const parsed = parseFormData(record.formData)
  return parsed?.lastApprovalRemark || record.remark || '-'
}

function annualLeaveCount(record: OaApproval) {
  const parsed = parseFormData(record.formData)
  const count = Number(parsed?.annualLeaveApprovedCount || 0)
  return Number.isFinite(count) && count > 0 ? count : '-'
}

function approvalRoundCount(record: OaApproval) {
  const parsed = parseFormData(record.formData)
  const rounds = Array.isArray(parsed?.approvalRounds) ? parsed.approvalRounds : []
  const count = rounds.length
  return count > 0 ? `${count}轮` : '-'
}

function currentNode(record: OaApproval) {
  const parsed = parseFormData(record.formData)
  if (record.status === 'APPROVED') return '流程完成'
  if (record.status === 'REJECTED') return '已驳回'
  return parsed?.currentNodeName || parsed?.currentStep || '待审批'
}

function currentApprover(record: OaApproval) {
  const parsed = parseFormData(record.formData)
  if (record.status === 'APPROVED') return '-'
  if (record.status === 'REJECTED') return '-'
  const approverNames = currentApproverDisplayNames(record)
  const approverName = approverNames.join(' / ') || currentApproverNameOnly(record)
  const role = parsed?.currentApproverRole ? String(parsed.currentApproverRole).trim() : ''
  if (approverName) {
    return approverName
  }
  return approverRoleLabel(role)
}

function currentApproverDisplayNames(record: OaApproval) {
  const parsed = parseFormData(record.formData)
  const displayNames = Array.isArray(parsed?.currentApproverDisplayNames)
    ? parsed.currentApproverDisplayNames
        .map((item: any) => String(item || '').trim())
        .filter((item: string) => !!item)
    : []
  return Array.from(new Set(displayNames))
}

function currentApproverNameOnly(record: OaApproval) {
  const displayNames = currentApproverDisplayNames(record)
  if (displayNames.length) {
    return displayNames.join(' / ')
  }
  const parsed = parseFormData(record.formData)
  const value = parsed?.currentApproverName || parsed?.nextApproverName
  return value ? String(value).trim() : ''
}

function approverRoleLabel(role?: string) {
  const value = String(role || '').toUpperCase()
  if (!value) return '待分配'
  const matched = approverRoleOptions.find((item) => String(item.value || '').toUpperCase() === value)
  return matched?.label || '待分配'
}

function approverStatusText(record: OaApproval) {
  const parsed = parseFormData(record.formData)
  if (record.status === 'APPROVED') return '流程完成'
  if (record.status === 'REJECTED') return '已驳回'
  const approverId = String(parsed?.currentApproverId || '').trim()
  const approverIds = Array.isArray(parsed?.currentApproverIds)
    ? parsed.currentApproverIds.map((item: any) => String(item || '').trim()).filter((item: string) => !!item)
    : []
  const role = String(parsed?.currentApproverRole || '').trim()
  const approverName = currentApprover(record)
  const currentStaffId = String(userStore.staffInfo?.id || '')
  if (approverIds.length > 0) {
    if (approverIds.includes(currentStaffId)) return '当前轮到我'
    return approverName ? `可由 ${approverName} 任一处理` : '任一候选人可审批'
  }
  if (approverId) {
    if (approverId === currentStaffId) return '当前轮到我'
    return approverName ? `已指派给 ${approverName}` : role ? `已指派给${approverRoleLabel(role)}` : '已指派'
  }
  if (role) {
    return approverName ? `待 ${approverName} 处理` : `待${approverRoleLabel(role)}处理`
  }
  return '待分配'
}

function approverStatusColor(record: OaApproval) {
  const text = approverStatusText(record)
  if (text === '流程完成') return 'green'
  if (text === '已驳回') return 'red'
  if (text === '当前轮到我') return 'blue'
  if (text.startsWith('可由')) return 'cyan'
  if (text.startsWith('已指派')) return 'gold'
  if (text.startsWith('待')) return 'purple'
  return 'default'
}

function urgeMetaText(record: OaApproval) {
  const parsed = parseFormData(record.formData)
  const count = Number(parsed?.urgeCount || 0)
  const timeText = parsed?.lastUrgedAt ? dayjs(parsed.lastUrgedAt).format('MM-DD HH:mm') : ''
  const byText = parsed?.lastUrgedBy ? ` by ${parsed.lastUrgedBy}` : ''
  if (count <= 0) return '未催办'
  return `催办${count}次${timeText ? ` · 最近 ${timeText}${byText}` : ''}`
}

function applyWorkflowData(parsed: Record<string, any>) {
  workflowForm.leaveType = parsed.leaveType || 'ANNUAL'
  workflowForm.shiftAffected = Boolean(parsed.shiftAffected)
  workflowForm.handoverName = parsed.handoverName || ''
  workflowForm.reimburseCategory = parsed.reimburseCategory || 'TRAVEL'
  workflowForm.hasBudget = parsed.hasBudget !== undefined ? Boolean(parsed.hasBudget) : true
  workflowForm.invoiceCount = Number(parsed.invoiceCount || 0)
  workflowForm.purchaseCategory = parsed.purchaseCategory || 'FOOD'
  workflowForm.stockChecked = Boolean(parsed.stockChecked)
  workflowForm.overBudget = Boolean(parsed.overBudget)
  workflowForm.materialCategory = parsed.materialCategory || 'CARE'
  workflowForm.autoPurchaseWhenLowStock = parsed.autoPurchaseWhenLowStock !== undefined ? Boolean(parsed.autoPurchaseWhenLowStock) : true
  workflowForm.proofNo = parsed.proofNo || ''
  workflowForm.sealMode = parsed.sealMode || 'ONLINE'
  workflowForm.insideWitness = parsed.insideWitness || ''
  workflowForm.outsideWitness = parsed.outsideWitness || ''
  workflowForm.redeemPoints = Number(parsed.redeemPoints || 300)
  leavePolicyAcknowledged.value = Boolean(parsed.policyAcknowledged)
  leaveProofs.value = Array.isArray(parsed.leaveProofs) ? parsed.leaveProofs.filter((item: any) => item?.url) : []
  commonProofs.value = Array.isArray(parsed.commonProofs) ? parsed.commonProofs.filter((item: any) => item?.url) : []
  supplementProofs.value = Array.isArray(parsed.supplementProofs) ? parsed.supplementProofs.filter((item: any) => item?.url) : []
}

function openCreate() {
  form.id = undefined
  form.approvalType = (query.type || 'LEAVE') as string
  form.title = ''
  form.applicantName = userStore.staffInfo?.realName || userStore.staffInfo?.username || ''
  form.amount = undefined
  form.startTime = undefined
  form.endTime = undefined
  form.formData = ''
  form.status = 'PENDING'
  form.remark = ''
  leavePolicyAcknowledged.value = false
  leaveProofs.value = []
  commonProofs.value = []
  supplementProofs.value = []
  resetWorkflowForm()
  editOpen.value = true
}

function openEdit(record: OaApproval) {
  form.id = String(record.id)
  form.approvalType = record.approvalType
  form.title = record.title
  form.applicantName = record.applicantName
  form.amount = record.amount
  form.startTime = record.startTime ? dayjs(record.startTime) : undefined
  form.endTime = record.endTime ? dayjs(record.endTime) : undefined
  form.formData = record.formData || ''
  form.status = record.status || 'PENDING'
  form.remark = record.remark || ''
  applyWorkflowData(parseFormData(record.formData))
  editOpen.value = true
}

function resubmit(record: OaApproval) {
  openCreate()
  form.approvalType = record.approvalType
  form.title = `【重提】${record.title || ''}`
  form.applicantName = userStore.staffInfo?.realName || record.applicantName || ''
  form.amount = record.amount
  form.startTime = record.startTime ? dayjs(record.startTime) : undefined
  form.endTime = record.endTime ? dayjs(record.endTime) : undefined
  form.remark = `驳回后重提：${record.remark || '已补充材料'}`
  applyWorkflowData(parseFormData(record.formData))
  editOpen.value = true
}

function buildWorkflowData() {
  const parsed = parseFormData(form.formData)
  parsed.leaveType = workflowForm.leaveType
  parsed.shiftAffected = workflowForm.shiftAffected
  parsed.handoverName = workflowForm.handoverName
  parsed.reimburseCategory = workflowForm.reimburseCategory
  parsed.hasBudget = workflowForm.hasBudget
  parsed.invoiceCount = workflowForm.invoiceCount
  parsed.purchaseCategory = workflowForm.purchaseCategory
  parsed.stockChecked = workflowForm.stockChecked
  parsed.overBudget = workflowForm.overBudget
  parsed.materialCategory = workflowForm.materialCategory
  parsed.autoPurchaseWhenLowStock = workflowForm.autoPurchaseWhenLowStock
  parsed.proofNo = workflowForm.proofNo
  parsed.sealMode = workflowForm.sealMode
  parsed.insideWitness = workflowForm.insideWitness
  parsed.outsideWitness = workflowForm.outsideWitness
  parsed.leaveProofs = leaveProofs.value
  parsed.commonProofs = commonProofs.value
  parsed.supplementProofs = supplementProofs.value
  parsed.policyAcknowledged = leavePolicyAcknowledged.value
  parsed.leaveDays = leaveDays.value
  parsed.needDeanApproval = needDeanApproval.value
  if (form.approvalType === 'POINTS_CASH') {
    parsed.staffId = userStore.staffInfo?.id
    parsed.redeemPoints = workflowForm.redeemPoints
    parsed.redeemCash = redeemCash.value
    parsed.exchangeRate = '300:10'
  }
  return JSON.stringify(parsed)
}

async function submit() {
  if (!form.title.trim()) {
    message.warning('请填写标题')
    return
  }
  form.applicantName = userStore.staffInfo?.realName || userStore.staffInfo?.username || form.applicantName
  if (form.approvalType === 'LEAVE' && !leavePolicyAcknowledged.value) {
    message.warning('请先勾选“我已阅读并同意制度”')
    return
  }
  if (form.approvalType === 'POINTS_CASH') {
    if (!workflowForm.redeemPoints || workflowForm.redeemPoints < 300 || workflowForm.redeemPoints % 300 !== 0) {
      message.warning('积分兑现金需按 300 的整数倍填写')
      return
    }
    if (redeemCash.value <= 0) {
      message.warning('当前积分不足以兑换现金')
      return
    }
  }
  const payload = {
    approvalType: form.approvalType,
    title: form.title,
    applicantId: userStore.staffInfo?.id,
    applicantName: userStore.staffInfo?.realName || userStore.staffInfo?.username || form.applicantName,
    amount: form.approvalType === 'POINTS_CASH' ? redeemCash.value : form.amount,
    startTime: form.startTime ? dayjs(form.startTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
    endTime: form.endTime ? dayjs(form.endTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
    formData: buildWorkflowData(),
    status: form.status,
    remark: form.remark
  }
  saving.value = true
  try {
    if (form.id) {
      await updateApproval(form.id, payload)
    } else {
      await createApproval(payload)
    }
    editOpen.value = false
    await fetchData()
    message.success('审批单已保存')
  } catch (error: any) {
    message.error(error?.message || '审批单保存失败')
  } finally {
    saving.value = false
  }
}

function isCurrentUserApprover(record: OaApproval) {
  const parsed = parseFormData(record.formData)
  const approverId = String(parsed?.currentApproverId || '').trim()
  const approverIds = Array.isArray(parsed?.currentApproverIds)
    ? parsed.currentApproverIds.map((item: any) => String(item || '').trim()).filter((item: string) => !!item)
    : []
  const approverRole = String(parsed?.currentApproverRole || '').toUpperCase()
  const roles = (userStore.roles || []).map((item) => String(item || '').toUpperCase())
  if (roles.includes('ADMIN') || roles.includes('SYS_ADMIN') || roles.includes('DIRECTOR')) return true
  if (approverIds.length > 0) {
    return approverIds.includes(String(userStore.staffInfo?.id || ''))
  }
  if (approverId) {
    return approverId === String(userStore.staffInfo?.id || '')
  }
  if (!approverRole) return canApproveFlow.value
  if (approverRole === 'DEPT_MANAGER' || approverRole === 'MANAGER') {
    return hasMinisterOrHigher(roles)
  }
  if (approverRole === 'DEAN') {
    return roles.includes('DEAN') || roles.includes('DIRECTOR')
  }
  if (approverRole === 'HR') {
    return roles.some((role) => role.includes('HR') || role.includes('PERSONNEL')) || hasMinisterOrHigher(roles)
  }
  if (approverRole === 'FINANCE') {
    return roles.some((role) => role.includes('FINANCE') || role.includes('CASHIER') || role.includes('ACCOUNTANT')) || hasMinisterOrHigher(roles)
  }
  if (approverRole === 'WAREHOUSE') {
    return roles.some((role) => role.includes('WAREHOUSE') || role.includes('STORE')) || hasMinisterOrHigher(roles)
  }
  if (approverRole === 'ADMIN_OFFICE') {
    return roles.some((role) => role.includes('ADMIN') || role.includes('OFFICE')) || hasMinisterOrHigher(roles)
  }
  if (approverRole === 'PURCHASING') {
    return roles.some((role) => role.includes('PURCHASE') || role.includes('PROCUREMENT')) || hasMinisterOrHigher(roles)
  }
  return canApproveFlow.value
}

async function uploadProof(target: { value: ProofFile[] }, file: File, bizType: string, successText: string) {
  proofUploading.value = true
  try {
    const uploaded = await uploadOaFile(file, bizType)
    const url = uploaded?.fileUrl || ''
    if (!url) {
      message.error('上传失败：未返回文件地址')
      return false
    }
    target.value.push({
      name: uploaded.originalFileName || uploaded.fileName || file.name,
      url
    })
    message.success(successText)
  } catch (error: any) {
    message.error(error?.message || '上传失败')
  } finally {
    proofUploading.value = false
  }
  return false
}

async function beforeUploadLeaveProof(file: File) {
  return uploadProof(leaveProofs, file, 'oa-leave-proof', '请假证明上传成功')
}

async function beforeUploadCommonProof(file: File) {
  return uploadProof(commonProofs, file, 'oa-approval-proof', '审批附件上传成功')
}

async function beforeUploadSupplementProof(file: File) {
  return uploadProof(supplementProofs, file, 'oa-approval-supplement', '补充证明上传成功')
}

function removeLeaveProof(index: number) {
  leaveProofs.value.splice(index, 1)
}

function removeCommonProof(index: number) {
  commonProofs.value.splice(index, 1)
}

function removeSupplementProof(index: number) {
  supplementProofs.value.splice(index, 1)
}

function openPolicy(path: string) {
  window.open(path, '_blank')
}

async function approve(record: OaApproval) {
  if (record.status !== 'PENDING') return
  try {
    await approveApproval(String(record.id), '同意')
    message.success('审批已通过')
    await fetchData()
  } catch (error: any) {
    message.error(error?.message || '审批失败')
  }
}

async function reject(record: OaApproval) {
  if (record.status !== 'PENDING') return
  const remark = window.prompt('请输入驳回原因', '请补充材料后重提')
  if (remark === null) return
  if (!remark.trim()) {
    message.warning('驳回原因不能为空')
    return
  }
  try {
    await rejectApproval(String(record.id), remark.trim())
    message.success('已驳回')
    await fetchData()
  } catch (error: any) {
    message.error(error?.message || '驳回失败')
  }
}

async function remove(record: OaApproval) {
  try {
    await deleteApproval(String(record.id))
    message.success('已删除')
    await fetchData()
  } catch (error: any) {
    message.error(error?.message || '删除失败')
  }
}

async function urge(record: OaApproval, silent = false) {
  if (record.status !== 'PENDING') {
    message.info('仅待审批流程支持催办')
    return
  }
  try {
    const parsed = parseFormData(record.formData)
    const urgeCount = Number(parsed?.urgeCount || 0) + 1
    const lastUrgedAt = dayjs().format('YYYY-MM-DDTHH:mm:ss')
    const lastUrgedBy = userStore.staffInfo?.realName || userStore.staffInfo?.username || '系统用户'
    const currentApproverName = currentApproverNameOnly(record)
    const previousApprover = String(parsed?.currentApproverName || parsed?.nextApproverName || '')
    const nextApprover = currentApproverName === '-' ? '' : currentApproverName
    const approverHistory = Array.isArray(parsed?.approverHistory) ? parsed.approverHistory.slice(-20) : []
    if (nextApprover && previousApprover !== nextApprover) {
      approverHistory.push({
        from: previousApprover || '未分配',
        to: nextApprover,
        at: lastUrgedAt
      })
    }
    const urgeHistory = Array.isArray(parsed?.urgeHistory) ? parsed.urgeHistory.slice(-30) : []
    urgeHistory.push({
      by: lastUrgedBy,
      at: lastUrgedAt,
      count: urgeCount
    })
    const nextFormData = JSON.stringify({
      ...parsed,
      urgeCount,
      lastUrgedAt,
      lastUrgedBy,
      currentApproverName: nextApprover || parsed?.currentApproverName,
      approverHistory,
      urgeHistory
    })
    await updateApproval(String(record.id), {
      approvalType: String(record.approvalType || 'LEAVE'),
      title: String(record.title || '审批催办'),
      applicantId: record.applicantId,
      applicantName: String(record.applicantName || userStore.staffInfo?.realName || userStore.staffInfo?.username || '系统用户'),
      amount: record.amount,
      startTime: record.startTime,
      endTime: record.endTime,
      formData: nextFormData
    })
    await createOaTask({
      title: `催办审批：${record.title || record.approvalType || ''}`,
      description: `审批ID：${record.id}，申请人：${record.applicantName || '-'}，${urgeMetaText({
        ...record,
        formData: nextFormData
      } as OaApproval)}`,
      priority: 'HIGH',
      calendarType: 'WORK',
      urgency: 'EMERGENCY',
      status: 'OPEN',
      assigneeName: currentApproverNameOnly(record) || undefined
    })
    if (!silent) message.success('催办已发送')
    await fetchData()
  } catch (error: any) {
    if (!silent) message.error(error?.message || '催办失败')
    throw error
  }
}

async function batchUrge() {
  const list = selectedRecords.value.filter((item) => item.status === 'PENDING')
  if (!list.length) {
    message.info('请先勾选待审批记录')
    return
  }
  let successCount = 0
  for (const item of list) {
    try {
      // eslint-disable-next-line no-await-in-loop
      await urge(item, true)
      successCount += 1
    } catch {}
  }
  if (successCount === 0) {
    message.error('批量催办失败')
    return
  }
  message.success(`批量催办完成，共处理 ${successCount} 条`)
}

function openTimeline(record: OaApproval) {
  timelineRecord.value = record
  timelineOpen.value = true
}

function openSelectedTimeline() {
  const record = requireSingleSelection('查看时间线')
  if (!record) return
  openTimeline(record)
}

function requireSingleSelection(action: string) {
  if (!selectedSingleRecord.value) {
    message.info(`请先勾选 1 条审批后再${action}`)
    return null
  }
  return selectedSingleRecord.value
}

function editSelected() {
  const record = requireSingleSelection('编辑')
  if (!record) return
  if (record.status !== 'PENDING') {
    message.warning('仅待审批状态可编辑')
    return
  }
  if (!canApproveFlow.value && String(record.applicantId || '') !== String(userStore.staffInfo?.id || '')) {
    message.warning('仅可编辑本人申请')
    return
  }
  openEdit(record)
}

async function approveSelected() {
  const record = requireSingleSelection('通过')
  if (!record) return
  await approve(record)
}

async function rejectSelected() {
  const record = requireSingleSelection('驳回')
  if (!record) return
  await reject(record)
}

function resubmitSelected() {
  const record = requireSingleSelection('驳回后重提')
  if (!record) return
  if (record.status !== 'REJECTED') {
    message.warning('仅已驳回审批支持重提')
    return
  }
  resubmit(record)
}

async function removeSelected() {
  const record = requireSingleSelection('删除')
  if (!record) return
  if (!canApproveFlow.value && String(record.applicantId || '') !== String(userStore.staffInfo?.id || '')) {
    message.warning('仅可删除本人申请')
    return
  }
  await remove(record)
}

async function batchApprove() {
  if (!canApproveFlow.value) {
    message.warning('仅管理者可执行审批')
    return
  }
  if (selectedPendingIds.value.length === 0) {
    message.info('勾选项中没有“待审批”记录')
    return
  }
  const affected = await batchApproveApproval(selectedPendingIds.value)
  message.success(`批量通过完成，共处理 ${affected || 0} 条`)
  fetchData()
}

async function batchReject() {
  if (!canApproveFlow.value) {
    message.warning('仅管理者可执行审批')
    return
  }
  if (selectedPendingIds.value.length === 0) {
    message.info('勾选项中没有“待审批”记录')
    return
  }
  const remark = window.prompt('请输入批量驳回原因', '批量未通过，请补充材料后重提')
  if (remark === null) return
  if (!remark.trim()) {
    message.warning('批量驳回原因不能为空')
    return
  }
  const affected = await batchRejectApproval(selectedPendingIds.value, remark.trim())
  message.success(`批量驳回完成，共处理 ${affected || 0} 条`)
  fetchData()
}

async function batchRemove() {
  if (selectedRowKeys.value.length === 0) return
  const affected = await batchDeleteApproval(selectedRowKeys.value)
  message.success(`批量删除完成，共处理 ${affected || 0} 条`)
  fetchData()
}

async function downloadExport() {
  const applicantId = resolveApplicantIdParam()
  const effectiveStatus = approvalScope.value === 'PENDING_REVIEW' ? 'PENDING' : query.status
  const blob = await exportApproval({
    keyword: query.keyword || undefined,
    type: query.type,
    currentApproverRole: query.currentApproverRole,
    status: effectiveStatus,
    applicantId,
    pendingMine: approvalScope.value === 'PENDING_REVIEW'
  })
  const href = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = href
  link.download = `oa-approval-${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(href)
}

watch(approvalScope, () => {
  query.pageNo = 1
  pagination.current = 1
  activeSummaryFilter.value = 'total'
  if (approvalScope.value === 'PENDING_REVIEW') {
    query.status = 'PENDING'
  }
  fetchData()
})

watch(autoRefresh, (enabled) => {
  if (enabled) {
    startAutoRefreshTimer()
    return
  }
  stopAutoRefreshTimer()
})

watch(
  () => [query.urgedOnly, query.overdueOnly],
  () => {
    query.pageNo = 1
    pagination.current = 1
    fetchData()
  }
)

onMounted(() => {
  applyRouteFilters()
  fetchData()
  startAutoRefreshTimer()
  if (String(route.query.quick || '') === '1') {
    openCreate()
  }
})

watch(
  () => route.query,
  () => {
    applyRouteFilters()
    fetchData()
    if (String(route.query.quick || '') === '1') {
      openCreate()
    }
  }
)

function applyRouteFilters() {
  const type = String(route.query.type || '').toUpperCase()
  const approverRole = String(route.query.currentApproverRole || '').toUpperCase()
  const status = String(route.query.status || '').toUpperCase()
  const scope = String(route.query.scope || '').toUpperCase()
  const keyword = String(route.query.keyword || '').trim()
  const focusId = String(route.query.focusId || '').trim()
  const urged = String(route.query.urged || '').toLowerCase()
  const overdue = String(route.query.overdue || '').toLowerCase()
  const source = String(route.query.source || '').toLowerCase()
  const scene = String(route.query.scene || '').toLowerCase()
  const category = String(route.query.category || '').toLowerCase()
  const quick = String(route.query.quick || '').toLowerCase()
  const lifecycleScene = source === 'lifecycle' || scene === 'status-change'
  activeSummaryFilter.value = 'total'
  approvalScope.value = canApproveFlow.value ? 'ALL' : 'MY'
  if (type && typeOptions.some((item) => item.value === type)) {
    query.type = type
    form.approvalType = type
  } else if (!type) {
    query.type = undefined
  }
  if (approverRole && approverRoleOptions.some((item) => item.value === approverRole)) {
    query.currentApproverRole = approverRole
  } else if (!approverRole) {
    query.currentApproverRole = undefined
  }
  if (status && statusOptions.some((item) => item.value === status)) {
    query.status = status
  } else if (!status) {
    query.status = undefined
  }
  focusApprovalId.value = focusId
  if (focusId) {
    query.keyword = focusId
    focusedOnce.value = false
  } else if (keyword) {
    query.keyword = keyword
  } else if (!keyword) {
    query.keyword = ''
  }
  query.urgedOnly = urged === '1' || urged === 'true' || quick === 'urged'
  query.overdueOnly = overdue === '1' || overdue === 'true'
  if (quick === 'overdue' || quick === 'risk' || quick === 'alert' || category === 'alert') {
    query.overdueOnly = true
  }

  const explicitMyScope = scope === 'MY' || route.query.my === '1'
  const explicitPendingScope = (scope === 'PENDING_REVIEW' || route.query.pending === '1') && canApproveFlow.value
  if (explicitMyScope) {
    approvalScope.value = 'MY'
  }
  if (explicitPendingScope) {
    approvalScope.value = 'PENDING_REVIEW'
    query.status = 'PENDING'
  }
  if (lifecycleScene && !explicitMyScope && !explicitPendingScope) {
    approvalScope.value = canApproveFlow.value ? 'PENDING_REVIEW' : 'MY'
    if (!status) {
      query.status = 'PENDING'
    }
  }
}

onUnmounted(() => {
  stopAutoRefreshTimer()
})

useLiveSyncRefresh({
  topics: ['oa'],
  refresh: () => {
    if (loading.value || summaryLoading.value) return
    fetchData().catch(() => {})
  },
  debounceMs: 800
})
</script>

<style scoped>
.approval-overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.overview-card {
  display: grid;
  gap: 6px;
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid #dce9f2;
  background: rgba(255, 255, 255, 0.86);
}

.overview-card span,
.table-head span {
  color: #6d8aa3;
  font-size: 12px;
}

.overview-card strong {
  color: #173854;
  font-size: 24px;
}

.overview-card small,
.approval-title-cell span,
.upload-hint,
.selection-tip,
.urge-meta,
.timeline-time {
  color: #6d8aa3;
  font-size: 12px;
}

.approval-commandbar {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  flex-wrap: wrap;
}

.command-group {
  display: grid;
  gap: 8px;
  min-width: 0;
  padding: 12px 14px;
  border: 1px solid #e5eef5;
  border-radius: 14px;
  background: rgba(247, 251, 254, 0.88);
}

.command-group-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.approval-toolbar {
  margin-top: 2px;
}

.selection-pill {
  padding: 6px 12px;
  border-radius: 999px;
  background: #eef8fc;
  color: #1b6282;
  font-size: 12px;
  font-weight: 700;
}

.table-head,
.approval-title-cell,
.approver-cell {
  display: flex;
}

.table-head {
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.table-head strong {
  display: block;
  color: #173854;
  font-size: 16px;
}

.approval-title-cell,
.approver-cell {
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.approval-title-cell strong,
.approver-name,
.approval-amount {
  color: #173854;
  font-weight: 700;
}

.proof-list {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.action-group-title {
  font-size: 12px;
  color: #5e788f;
  font-weight: 700;
}

.summary-filter-card {
  cursor: pointer;
  transition: all 0.2s ease;
}

.summary-filter-card:hover {
  transform: translateY(-1px);
}

.summary-filter-card.active {
  box-shadow: 0 0 0 1px #1677ff inset;
  background: #e6f4ff;
}

.upload-hint {
  margin-top: 6px;
}

.leave-policy-card :deep(.ant-typography) {
  margin-bottom: 8px;
}

.proof-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 4px 8px;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
}

.urge-meta {
  margin-top: 2px;
}

.timeline-title {
  font-weight: 600;
  color: #173854;
}

.timeline-desc {
  margin-top: 2px;
  color: #4f6f86;
}

.timeline-time {
  margin-top: 2px;
}

:deep(.is-urgent-row > td) {
  background: rgba(255, 247, 230, 0.82);
}

@media (max-width: 1200px) {
  .approval-overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .approval-overview-grid {
    grid-template-columns: 1fr;
  }

  .approval-commandbar,
  .table-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .command-group {
    width: 100%;
  }
}
</style>
