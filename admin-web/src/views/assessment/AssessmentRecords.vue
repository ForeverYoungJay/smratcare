<template>
  <PageContainer :title="title" :subTitle="subTitle">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item :label="pageConfig.keywordLabel">
          <ElderNameAutocomplete
            v-if="useElderKeywordSelect"
            v-model:value="query.keyword"
            :placeholder="`${pageConfig.keywordLabel}(编号)`"
            width="220px"
            @select="onSearch"
          />
          <a-input v-else v-model:value="query.keyword" :placeholder="pageConfig.keywordPlaceholder" allow-clear />
        </a-form-item>
        <a-form-item v-if="props.assessmentType === 'ARCHIVE'" label="老人">
          <a-select
            v-model:value="query.elderId"
            allow-clear
            show-search
            :filter-option="false"
            :options="elderOptions"
            :loading="elderLoading"
            style="width: 220px"
            placeholder="请输入老人姓名/拼音首字母"
            @search="searchElderOptions"
            @focus="() => !elderOptions.length && loadElderOptions()"
          />
        </a-form-item>
        <a-form-item v-if="pageConfig.showStatusFilter" label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 140px">
            <a-select-option value="DRAFT">草稿</a-select-option>
            <a-select-option value="COMPLETED">已完成</a-select-option>
            <a-select-option value="ARCHIVED">已归档</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item v-if="pageConfig.showArchiveTypeFilter" label="评估类型">
          <a-select v-model:value="query.archiveType" allow-clear style="width: 160px" placeholder="请选择评估类型">
            <a-select-option v-for="item in assessmentTypeOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item v-if="pageConfig.showContentKeyword" label="评估内容">
          <a-input v-model:value="query.contentKeyword" placeholder="请输入评估内容" allow-clear />
        </a-form-item>
        <a-form-item v-if="pageConfig.showDateFilter" :label="pageConfig.dateLabel">
          <a-range-picker v-model:value="query.dateRange" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="onSearch">搜索</a-button>
            <a-button @click="reset">清空</a-button>
            <a-button @click="copyQueryLink">复制查询链接</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-alert
      v-if="lifecycleContext.active"
      type="info"
      show-icon
      style="margin-top: 12px"
      :message="lifecycleContext.message"
    >
      <template #description>
        <a-space wrap>
          <a-tag color="blue">场景：入住状态变更联动</a-tag>
          <a-button type="link" size="small" @click="router.push('/elder/status-change')">返回状态变更中心</a-button>
          <a-button type="link" size="small" @click="router.push('/elder/admission-processing?source=lifecycle&scene=status-change')">去入住办理核验</a-button>
        </a-space>
      </template>
    </a-alert>

    <a-row v-if="!isAdmissionAssessment" :gutter="[12, 12]" style="margin-top: 16px">
      <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="总记录" :value="summary.totalCount || 0" /></a-card></a-col>
      <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="草稿" :value="summary.draftCount || 0" /></a-card></a-col>
      <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="已完成" :value="summary.completedCount || 0" /></a-card></a-col>
      <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="已归档" :value="summary.archivedCount || 0" /></a-card></a-col>
      <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="复评逾期" :value="summary.reassessOverdueCount || 0" /></a-card></a-col>
      <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="高风险等级" :value="summary.highRiskCount || 0" /></a-card></a-col>
    </a-row>
    <a-alert
      v-if="!isAdmissionAssessment && ((summary.reassessOverdueCount || 0) > 0 || (summary.highRiskCount || 0) > 0)"
      type="warning"
      show-icon
      style="margin-top: 12px"
      :message="`风险提醒：复评逾期 ${summary.reassessOverdueCount || 0} 条，高风险等级 ${summary.highRiskCount || 0} 条。`"
    />
    <a-card v-if="props.assessmentType === 'ARCHIVE'" class="card-elevated" :bordered="false" style="margin-top: 12px">
      <a-tabs v-model:activeKey="archiveStatusTab" @change="onArchiveStatusTabChange">
        <a-tab-pane key="ALL" :tab="`总记录 (${summary.totalCount || 0})`" />
        <a-tab-pane key="DRAFT" :tab="`草稿 (${summary.draftCount || 0})`" />
        <a-tab-pane key="COMPLETED" :tab="`已完成 (${summary.completedCount || 0})`" />
        <a-tab-pane key="ARCHIVED" :tab="`已归档 (${summary.archivedCount || 0})`" />
      </a-tabs>
    </a-card>
    <LifecycleStageBar
      v-if="isAdmissionAssessment"
      title="评估-入住联动阶段"
      :subject="admissionLifecycleSubject"
      :stage="admissionLifecycleStage"
      :hint="admissionLifecycleHint"
      style="margin-top: 12px"
    />
    <a-card
      v-if="isAdmissionAssessment"
      class="card-elevated"
      :bordered="false"
      style="margin-top: 12px;"
    >
      <template #title>待评估合同（请先勾选合同再新增入住评估）</template>
      <template #extra>
        <a-space size="small">
          <a-tag color="gold">仅用于新建入住评估</a-tag>
          <a-button type="primary" :disabled="!selectedPendingContract" @click="openForm()">
            {{ pageConfig.createButtonText }}
          </a-button>
        </a-space>
      </template>
      <a-table
        size="small"
        row-key="contractNo"
        :loading="pendingAdmissionLoading"
        :data-source="pendingAdmissionContracts"
        :columns="pendingAdmissionColumns"
        :pagination="false"
        :row-selection="pendingAdmissionRowSelection"
        :locale="{ emptyText: '暂无待评估合同' }"
        :custom-row="pendingAdmissionCustomRow"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'flowStage'">
            {{ pendingFlowStageText(record.flowStage) }}
          </template>
        </template>
      </a-table>
      <a-alert
        style="margin-top: 10px;"
        type="info"
        show-icon
        :message="selectedPendingContract ? `当前已选合同：${selectedPendingContract.contractNo || '-'} / 老人：${selectedPendingContract.elderName || '-'}` : '未勾选合同：请先勾选一条待评估合同后再新增入住评估'"
      />
    </a-card>

    <a-card v-if="!isAdmissionAssessment" class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <a-alert
        v-if="isAdmissionAssessment"
        style="margin-bottom: 10px"
        type="info"
        show-icon
        :message="selectedPendingContract
          ? `当前新建评估将使用合同 ${selectedPendingContract.contractNo || '-'}；下方“入住评估记录表”勾选仅用于批量处理，不影响新建合同选择。`
          : '提示：上方合同表用于“新建入住评估”，下方记录表用于“批量处理/导出/查看历史”。'"
      />
      <div class="table-actions">
        <a-space wrap size="small">
          <a-button v-if="pageConfig.showCreateButton && !isAdmissionAssessment" type="primary" @click="openForm()">
            {{ pageConfig.createButtonText }}
          </a-button>
          <a-dropdown
            v-if="pageConfig.actions.includes('view') || pageConfig.actions.includes('edit')"
            trigger="click"
          >
            <a-button>单条操作</a-button>
            <template #overlay>
              <a-menu @click="onSingleActionMenuClick">
                <a-menu-item key="view" :disabled="!pageConfig.actions.includes('view') || selectedRowKeys.length !== 1">
                  查看勾选
                </a-menu-item>
                <a-menu-item key="edit" :disabled="!pageConfig.actions.includes('edit') || selectedRowKeys.length !== 1">
                  编辑勾选
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
          <a-dropdown trigger="click">
            <a-button :disabled="selectedRowKeys.length === 0">批量处理</a-button>
            <template #overlay>
              <a-menu @click="onBatchActionMenuClick">
                <a-menu-item key="assign">指派评估人</a-menu-item>
                <a-menu-item key="next-date">设置复评日期</a-menu-item>
                <a-menu-divider />
                <a-menu-item key="mark-completed">标记完成</a-menu-item>
                <a-menu-item key="mark-archived">批量归档</a-menu-item>
                <a-menu-item
                  v-if="pageConfig.showBatchDelete && canDeleteAssessment"
                  key="delete"
                  danger
                >
                  删除勾选
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
          <a-dropdown trigger="click">
            <a-button>勾选管理</a-button>
            <template #overlay>
              <a-menu @click="onSelectionActionMenuClick">
                <a-menu-item key="select-filter">按筛选全选(<=2000)</a-menu-item>
                <a-menu-item key="toggle-page" :disabled="rows.length === 0">
                  {{ selectedRowKeys.length === rows.length && rows.length > 0 ? '取消全选当前页' : '全选当前页' }}
                </a-menu-item>
                <a-menu-item key="clear-selection" :disabled="selectedRowKeys.length === 0">清空勾选</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
          <a-dropdown trigger="click">
            <a-button>导出</a-button>
            <template #overlay>
              <a-menu @click="onExportActionMenuClick">
                <a-menu-item key="selected-excel" :disabled="selectedRowKeys.length === 0">导出勾选Excel</a-menu-item>
                <a-menu-item key="selected-pdf" :disabled="selectedRowKeys.length === 0">下载勾选PDF</a-menu-item>
                <a-menu-item v-if="pageConfig.showExportButton" key="all-excel">导出Excel</a-menu-item>
                <a-menu-item v-if="props.assessmentType === 'ARCHIVE'" key="page-excel">下载本页Excel</a-menu-item>
                <a-menu-item v-if="props.assessmentType === 'ARCHIVE'" key="page-pdf">下载本页PDF</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
          <a-divider type="vertical" />
          <a-button :disabled="!selectedSingleRecord" @click="goSelectedInHospitalOverview">在院总览</a-button>
          <a-button :disabled="!selectedSingleRecord" @click="goSelectedContractsInvoices">合同票据</a-button>
          <a-button :disabled="!selectedSingleRecord" @click="goSelectedAdmissionProcessing">入住办理</a-button>
        </a-space>
        <a-tag color="blue">
          已勾选 {{ selectedRowKeys.length }} 条{{ selectedByFilter ? '（含跨页筛选）' : '' }}
        </a-tag>
      </div>
      <a-alert
        v-if="selectedRowKeys.length > 0"
        style="margin-bottom: 12px"
        type="info"
        show-icon
        :message="selectedSummaryText"
      />
      <a-alert
        v-if="lastBatchAction"
        style="margin-bottom: 12px"
        type="success"
        show-icon
        :message="lastBatchAction"
      />
      <a-table
        :data-source="rows"
        :columns="tableColumns"
        :loading="loading"
        :pagination="false"
        :row-key="assessmentRowKey"
        :row-selection="rowSelection"
        :row-class-name="tableRowClassName"
        :custom-row="tableCustomRow"
        :locale="{ emptyText: '暂无数据' }"
        :scroll="{ x: 1300, y: 520 }"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'index'">
            {{ index + 1 + (query.pageNo - 1) * query.pageSize }}
          </template>
          <template v-else-if="column.key === 'assessmentType'">
            {{ assessmentTypeLabel(record.assessmentTypeLabel || record.assessmentType) }}
          </template>
          <template v-else-if="column.key === 'elderName'">
            {{ resolveAssessmentElderName(record) }}
          </template>
          <template v-else-if="column.key === 'gender'">
            {{ record.genderLabel || (record.gender === 1 ? '男' : record.gender === 2 ? '女' : '-') }}
          </template>
          <template v-else-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">{{ record.statusLabel || statusLabel(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'completedFlag'">
            {{ record.status === 'COMPLETED' || record.status === 'ARCHIVED' ? '是' : '否' }}
          </template>
          <template v-else-if="column.key === 'orgName'">
            {{ orgDisplayName(record) }}
          </template>
          <template v-else-if="column.key === 'assessmentCode'">
            {{ record.archiveNo || record.id || '-' }}
          </template>
          <template v-else-if="column.key === 'assessmentContent'">
            {{ record.levelCode || '-' }}
          </template>
          <template v-else-if="column.key === 'assessmentTime'">
            {{ record.updateTime || record.assessmentDate || '-' }}
          </template>
          <template v-else-if="column.key === 'assessmentPlace'">
            {{ record.source || '-' }}
          </template>
          <template v-else-if="column.key === 'score'">
            {{ record.score ?? '-' }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button v-if="pageConfig.actions.includes('assign')" type="link" @click="assignRecord(record)">指派</a-button>
              <a-button v-if="pageConfig.actions.includes('edit')" type="link" @click="openForm(record)">编辑</a-button>
              <a-button v-if="pageConfig.actions.includes('view')" type="link" @click="openForm(record, true)">查看</a-button>
              <a-button
                v-if="record.status === 'COMPLETED' || record.status === 'ARCHIVED'"
                type="link"
                @click="printReport(record)"
              >
                打印报告
              </a-button>
              <a-button
                v-if="pageConfig.actions.includes('delete') && canDeleteAssessment"
                type="link"
                danger
                @click="remove(record.id)"
              >
                删除
              </a-button>
            </a-space>
          </template>
          <template v-else>
            {{ displayCell(record, column.dataIndex) }}
          </template>
        </template>
      </a-table>
      <a-pagination
        style="margin-top: 16px; text-align: right;"
        :current="query.pageNo"
        :page-size="query.pageSize"
        :total="total"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>

    <a-modal
      v-model:open="open"
      :title="formReadonly ? title + '查看' : title + '登记'"
      width="680px"
      :confirm-loading="submitting"
      :ok-button-props="{ disabled: formReadonly }"
      :ok-text="formReadonly ? '已查看' : admissionModalConfirmText"
      cancel-text="关闭"
      @ok="submit"
      @cancel="() => (open = false)"
    >
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="老人" name="elderId">
              <a-select
                v-model:value="form.elderId"
                :disabled="formReadonly"
                allow-clear
                show-search
                :filter-option="false"
                :options="elderOptions"
                :loading="elderLoading"
                placeholder="请输入老人姓名/拼音首字母"
                @search="searchElderOptions"
                @focus="() => !elderOptions.length && loadElderOptions()"
                @change="onElderChange"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item v-if="!isAdmissionAssessment" label="评估等级">
              <a-input
                v-model:value="form.levelCode"
                placeholder="如 A/B/C 或 轻/中/重"
                :disabled="formReadonly"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="评估日期" name="assessmentDate">
              <a-date-picker v-model:value="form.assessmentDate" value-format="YYYY-MM-DD" style="width: 100%" :disabled="formReadonly" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="下次评估日期">
              <a-date-picker
                v-model:value="form.nextAssessmentDate"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                :disabled="formReadonly"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="16">
            <a-form-item v-if="!isAdmissionAssessment" label="量表模板">
              <a-space direction="vertical" style="width: 100%">
                <a-select v-model:value="form.templateId" allow-clear placeholder="可选，选择后可自动算分" :disabled="formReadonly">
                  <a-select-option v-for="tpl in templates" :key="tpl.id" :value="tpl.id">
                    {{ tpl.templateName }} ({{ tpl.templateCode }})
                  </a-select-option>
                </a-select>
                <a-button type="link" style="padding: 0;" @click="openTemplateManager">上传/自定义量表模板</a-button>
              </a-space>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item v-if="!isAdmissionAssessment" label="自动评分">
              <a-switch v-model:checked="scoreAutoChecked" :disabled="formReadonly" @change="onScoreAutoChange" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item v-if="isAdmissionAssessment" label="老年人能力评估表（完整版）">
          <a-alert
            type="info"
            show-icon
            message="共 26 项，系统按已填写项目得分归一化为 90 分制，并自动计算能力等级；1-8 项如厕相关动作需关注“排泄前解裤、排后清洁、穿回裤子”的完整性；21项意识水平为0时按重度失能处理。"
            style="margin-bottom: 12px"
          />
          <a-space direction="vertical" style="width: 100%">
            <a-card v-for="group in admissionAbilityGroups" :key="group.title" size="small" :title="group.title">
              <a-space direction="vertical" style="width: 100%">
                <a-card v-for="item in group.items" :key="item.id" size="small">
                  <div style="font-weight: 600; margin-bottom: 6px;">{{ item.id }}. {{ item.title }}</div>
                  <div style="color: rgba(0,0,0,0.65); margin-bottom: 8px;">指标说明：{{ item.description }}</div>
                  <a-select
                    :data-admission-item-id="item.id"
                    v-model:value="admissionScores[item.id]"
                    :options="admissionOptionSelectOptions(item)"
                    :disabled="formReadonly"
                    allow-clear
                    style="width: 100%; margin-bottom: 8px;"
                    placeholder="请选择分值及对应能力描述"
                    @change="onAdmissionScoreFilled(item.id, $event)"
                  />
                  <div :id="`admission-score-input-wrap-${item.id}`">
                    <a-input-number
                      v-model:value="admissionScores[item.id]"
                      :min="admissionItemMin(item)"
                      :max="admissionItemMax(item)"
                      :precision="0"
                      :disabled="formReadonly"
                      style="width: 100%; margin-bottom: 8px;"
                      :placeholder="`请输入分值(${admissionItemMin(item)}~${admissionItemMax(item)})`"
                      @change="onAdmissionScoreFilled(item.id, $event)"
                      @update:value="onAdmissionScoreFilled(item.id, $event)"
                      @pressEnter="onAdmissionScoreFilled(item.id, admissionScores[item.id])"
                    />
                  </div>
                  <div style="color: rgba(0,0,0,0.6); font-size: 12px;">
                    可选分值：
                    <span v-for="(opt, idx) in item.options" :key="`${item.id}-hint-${idx}`">
                      {{ opt.score }}分{{ idx < item.options.length - 1 ? ' / ' : '' }}
                    </span>
                  </div>
                </a-card>
              </a-space>
            </a-card>
            <a-card size="small" title="总分与等级">
              <a-row :gutter="16">
                <a-col :span="8">原始得分：{{ admissionRawScore }} / {{ admissionMaxRawScore }}</a-col>
                <a-col :span="8">90分制总分：{{ admissionTotalScore }}</a-col>
                <a-col :span="8">能力等级：{{ admissionLevelText }}</a-col>
              </a-row>
              <div style="margin-top: 8px; color: rgba(0,0,0,0.65);">
                等级规则：0级(90)，1级(66-89)，2级(46-65)，3级(30-45)，4级(0-29)
              </div>
            </a-card>
          </a-space>
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="评估分值">
              <a-input-number v-model:value="form.score" :min="0" :max="999" :step="0.5" style="width: 100%" :disabled="formReadonly" />
            </a-form-item>
          </a-col>
          <a-col v-if="isAdmissionAssessment" :span="12">
            <a-form-item label="评估等级（能力等级）">
              <a-input :value="admissionLevelText === '待完成评分' ? '' : admissionLevelText" disabled placeholder="自动计算" />
            </a-form-item>
          </a-col>
          <a-col v-else :span="12">
            <a-form-item label="评估状态" name="status">
              <a-select v-model:value="form.status" :disabled="formReadonly">
                <a-select-option value="DRAFT">草稿</a-select-option>
                <a-select-option value="COMPLETED">已完成</a-select-option>
                <a-select-option value="ARCHIVED">已归档</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row v-if="isAdmissionAssessment" :gutter="16">
          <a-col :span="12">
            <a-form-item label="评估状态" name="status">
              <a-select v-model:value="form.status" :disabled="formReadonly">
                <a-select-option value="DRAFT">草稿</a-select-option>
                <a-select-option value="COMPLETED">已完成</a-select-option>
                <a-select-option value="ARCHIVED">已归档</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="评估人">
              <a-input v-model:value="form.assessorName" :disabled="formReadonly" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="档案编号">
              <a-input
                v-model:value="form.archiveNo"
                :disabled="formReadonly || isAdmissionAssessment"
                :placeholder="isAdmissionAssessment ? '系统自动回填合同编号' : ''"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="评估结论">
          <a-textarea v-model:value="form.resultSummary" :rows="3" placeholder="简要结论" :disabled="formReadonly" />
        </a-form-item>
        <a-form-item label="护理建议">
          <a-textarea v-model:value="form.suggestion" :rows="3" placeholder="后续护理建议" :disabled="formReadonly" />
        </a-form-item>
        <a-form-item v-if="!isAdmissionAssessment" label="量表明细(JSON)">
          <a-textarea
            v-model:value="form.detailJson"
            :rows="4"
            placeholder='如 {"orientation":6,"registration":5}'
            :disabled="formReadonly"
          />
          <div style="margin-top: 8px;">
            <a-space>
              <a-upload :before-upload="beforeDetailJsonUpload" :show-upload-list="false" accept=".json,.txt" :disabled="formReadonly">
                <a-button size="small" :disabled="formReadonly">上传明细文件</a-button>
              </a-upload>
              <a-button size="small" :disabled="formReadonly" @click="previewScore">试算分值</a-button>
              <span class="hint">选择模板并填写明细后，可自动计算分值和等级。</span>
            </a-space>
          </div>
        </a-form-item>
      </a-form>
    </a-modal>
    <a-modal
      v-model:open="batchAssignOpen"
      title="批量指派评估人"
      :confirm-loading="batchAssignSubmitting"
      ok-text="确认指派"
      cancel-text="取消"
      @ok="submitBatchAssign"
    >
      <a-form layout="vertical">
        <a-form-item label="评估人姓名" required>
          <a-input
            v-model:value="batchAssignAssessorName"
            placeholder="请输入评估人姓名"
            :maxlength="32"
          />
        </a-form-item>
        <a-alert
          type="info"
          show-icon
          :message="`将更新 ${selectedRowKeys.length} 条勾选记录的评估人`"
        />
      </a-form>
    </a-modal>
    <a-modal
      v-model:open="batchNextDateOpen"
      title="批量设置复评日期"
      :confirm-loading="batchNextDateSubmitting"
      ok-text="确认设置"
      cancel-text="取消"
      @ok="submitBatchNextDate"
    >
      <a-form layout="vertical">
        <a-form-item label="下次评估日期" required>
          <a-date-picker
            v-model:value="batchNextAssessmentDate"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </a-form-item>
        <a-alert
          type="info"
          show-icon
          :message="`将更新 ${selectedRowKeys.length} 条勾选记录的复评日期`"
        />
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { hasMinisterOrHigher } from '../../utils/roleAccess'
import PageContainer from '../../components/PageContainer.vue'
import LifecycleStageBar from '../../components/LifecycleStageBar.vue'
import ElderNameAutocomplete from '../../components/ElderNameAutocomplete.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { copyText } from '../../utils/clipboard'
import { lifecycleStageHint, normalizeLifecycleStage } from '../../utils/lifecycleStage'
import { getContractPage } from '../../api/marketing'
import {
  getAssessmentRecordPage,
  getAssessmentRecordSummary,
  getAssessmentRecordIds,
  createAssessmentRecord,
  updateAssessmentRecord,
  deleteAssessmentRecord,
  batchDeleteAssessmentRecord,
  batchAssignAssessmentRecord,
  batchUpdateAssessmentNextDate,
  batchUpdateAssessmentRecordStatus,
  exportAssessmentRecord,
  getAssessmentRecordReport,
  getAssessmentTemplateList,
  previewAssessmentScore
} from '../../api/assessment'
import type {
  AssessmentBatchOperationResult,
  AssessmentRecord,
  AssessmentRecordSummary,
  AssessmentType,
  AssessmentScaleTemplate,
  CrmContractItem,
  PageResult
} from '../../types'

const props = defineProps<{
  title: string
  subTitle: string
  assessmentType: AssessmentType
}>()
const route = useRoute()
const router = useRouter()
const lifecycleContext = computed(() => {
  const source = String(route.query.source || '').trim().toLowerCase()
  const scene = String(route.query.scene || '').trim().toLowerCase()
  const active = source === 'lifecycle' || scene === 'status-change'
  return {
    active,
    message: active ? '当前评估页面由状态变更联动进入，建议优先补齐高风险评估并回流入住办理。' : ''
  }
})
const userStore = useUserStore()
const assessmentRouteSignature = ref('')
const skipNextAssessmentRouteWatch = ref(false)
const ASSESSMENT_ROUTE_KEYS = [
  'assessArchiveType',
  'assessContentKeyword',
  'assessDateEnd',
  'assessDateStart',
  'assessElderId',
  'assessKeyword',
  'assessPageNo',
  'assessPageSize',
  'assessStatus'
] as const
const ASSESSMENT_ROUTE_LEGACY_KEYS = ['archiveType', 'contentKeyword', 'dateFrom', 'dateTo', 'keyword', 'pageNo', 'pageSize', 'status'] as const
const ASSESSMENT_ROUTE_OBSERVE_KEYS = ['residentId', 'elderId', 'elderName'] as const

const loading = ref(false)
const rows = ref<AssessmentRecord[]>([])
const total = ref(0)
const summary = reactive<AssessmentRecordSummary>({
  totalCount: 0,
  draftCount: 0,
  completedCount: 0,
  archivedCount: 0,
  reassessOverdueCount: 0,
  highRiskCount: 0
})
const templates = ref<AssessmentScaleTemplate[]>([])
const {
  elderOptions,
  elderLoading,
  searchElders: loadElderOptions,
  findElderName,
  ensureSelectedElder
} = useElderOptions({ pageSize: 30 })

type ActionType = 'assign' | 'edit' | 'view' | 'delete'

interface TableColumnDef {
  title: string
  key: string
  dataIndex?: string
  width?: number
  fixed?: 'left' | 'right'
}

interface PageConfig {
  keywordLabel: string
  keywordPlaceholder: string
  dateLabel: string
  showStatusFilter: boolean
  showDateFilter: boolean
  showArchiveTypeFilter: boolean
  showContentKeyword: boolean
  showCreateButton: boolean
  showBatchDelete: boolean
  showExportButton: boolean
  createButtonText: string
  actions: ActionType[]
  columns: TableColumnDef[]
}

interface AdmissionScoreOption {
  score: number
  label: string
}

interface AdmissionAbilityItem {
  id: number
  title: string
  description: string
  options: AdmissionScoreOption[]
}

interface AdmissionAbilityGroup {
  title: string
  items: AdmissionAbilityItem[]
}

const commonAbilityOptions: AdmissionScoreOption[] = [
  { score: 4, label: '独立完成，不需要他人协助' },
  { score: 3, label: '在他人指导或提示下完成' },
  { score: 2, label: '需要他人协助，但以自身完成为主' },
  { score: 1, label: '主要依靠他人协助，自身能给予配合' },
  { score: 0, label: '完全依赖他人协助，且不能给予配合' }
]

const admissionAbilityGroups: AdmissionAbilityGroup[] = [
  {
    title: '一、自理能力指标（1-8）',
    items: [
      { id: 1, title: '进食', description: '使用餐具将食物送入口中并完成吞咽。', options: commonAbilityOptions },
      { id: 2, title: '修饰', description: '洗脸、刷牙、梳头、刮脸、剪指（趾）甲等。', options: commonAbilityOptions },
      { id: 3, title: '洗澡', description: '清洗和擦干身体。', options: commonAbilityOptions },
      { id: 4, title: '穿脱上衣', description: '穿脱上身衣服、系扣、拉链等。', options: commonAbilityOptions },
      { id: 5, title: '穿脱裤子和鞋袜', description: '穿脱裤子、鞋袜等。', options: commonAbilityOptions },
      {
        id: 6,
        title: '小便控制',
        description: '控制和排出尿液的能力。',
        options: [
          { score: 4, label: '可自行控制排尿，排尿次数与控制均正常' },
          { score: 3, label: '白天可控制，夜间排尿增多或控制较差，或自行使用尿布/尿垫' },
          { score: 2, label: '白天大部分时间可控，偶有尿失禁（每天<1次且每周>1次）' },
          { score: 1, label: '白天大部分时间不能控制，夜间失禁明显或需大量协助使用辅助用品' },
          { score: 0, label: '小便失禁完全不能控制排尿或留置导尿管' }
        ]
      },
      {
        id: 7,
        title: '大便控制',
        description: '控制和排出粪便的能力。',
        options: [
          { score: 4, label: '可自行正常控制大便排出' },
          { score: 3, label: '偶发便秘/大便失禁（每周<1次）或自行使用辅助用品' },
          { score: 2, label: '经常便秘/大便失禁（每周>1次）或他人少量协助' },
          { score: 1, label: '大部分时间出现便秘/失禁（每天>1次）或他人大量协助' },
          { score: 0, label: '严重便秘或完全依赖他人协助排便和清洁皮肤' }
        ]
      },
      { id: 8, title: '如厕', description: '上厕所排泄大小便，并清洁身体。', options: commonAbilityOptions }
    ]
  },
  {
    title: '二、基础运动能力指标（9-12）',
    items: [
      { id: 9, title: '床上体位转换', description: '卧床翻身及坐起、躺下。', options: commonAbilityOptions },
      { id: 10, title: '床椅转移', description: '从坐位到站位，再从站位到坐位的转换。', options: commonAbilityOptions },
      {
        id: 11,
        title: '平地行走',
        description: '双脚交互方式在地面行走（含使用助行器）。',
        options: [
          { score: 4, label: '独立平地行走约50m，不需协助，无摔倒风险' },
          { score: 3, label: '平地步行约50m存在摔倒风险，需监护/指导或辅助工具' },
          { score: 2, label: '步行时需要他人大量扶持协助' },
          { score: 1, label: '步行能力极差，短距离亦需持续扶持协助' },
          { score: 0, label: '完全不能步行' }
        ]
      },
      {
        id: 12,
        title: '上下楼梯',
        description: '连续上下10～15个台阶。',
        options: [
          { score: 3, label: '可独立上下楼梯，不需要协助' },
          { score: 2, label: '在他人指导或提示下完成' },
          { score: 1, label: '需要他人协助，但以自身完成为主' },
          { score: 0, label: '主要或完全依赖他人协助' }
        ]
      }
    ]
  },
  {
    title: '三、精神状态指标（13-18）',
    items: [
      {
        id: 13,
        title: '时间定向',
        description: '知道并确认时间（年、月、日或季节）的能力。',
        options: [
          { score: 4, label: '知道并确认时间，可相差一天' },
          { score: 3, label: '时间观念下降，年月日或星期几不能全部分辨，误差两天以上' },
          { score: 2, label: '时间观念较差，年月日不清，可知上/下半年或季节' },
          { score: 1, label: '时间观念很差，仅可知上午/下午或白天/夜间' },
          { score: 0, label: '无任何时间观念' }
        ]
      },
      {
        id: 14,
        title: '空间定向',
        description: '知道并确认空间位置的能力。',
        options: [
          { score: 4, label: '可在日常生活范围内单独外出' },
          { score: 3, label: '不能单独外出，但知道日常生活所在地具体地址' },
          { score: 2, label: '不能单独外出，但知道较多相关地址信息' },
          { score: 1, label: '不能单独外出，仅知道较少地址信息' },
          { score: 0, label: '不能单独外出，无空间观念' }
        ]
      },
      {
        id: 15,
        title: '人物定向',
        description: '知道并确认人物的能力。',
        options: [
          { score: 4, label: '认识长期共同生活者，可称呼并知道关系' },
          { score: 3, label: '能认识大部分共同生活者，能称呼或知关系' },
          { score: 2, label: '能认识部分亲人或照护者，能称呼或知关系' },
          { score: 1, label: '仅认识自己或极少数亲人/照护者' },
          { score: 0, label: '不认识任何人' }
        ]
      },
      {
        id: 16,
        title: '记忆能力',
        description: '短时、近事和远事记忆能力。',
        options: [
          { score: 4, label: '记忆与社会年龄相符，回忆完整' },
          { score: 3, label: '轻度记忆紊乱，5分钟回忆3词仅0~1个' },
          { score: 2, label: '中度记忆紊乱，近期记忆明显受损' },
          { score: 1, label: '重度记忆紊乱，难以回忆熟悉人物事件' },
          { score: 0, label: '记忆完全紊乱，不能正确回忆既往事物' }
        ]
      },
      {
        id: 17,
        title: '理解能力',
        description: '理解语言信息和非语言信息的能力。',
        options: [
          { score: 4, label: '能正常且完全理解他人的话' },
          { score: 3, label: '能理解他人的话，但需要更多时间' },
          { score: 2, label: '理解有明显困难，需要反复解释' },
          { score: 1, label: '仅能理解简单指令，复杂内容难以理解' },
          { score: 0, label: '基本无法理解语言和非语言信息' }
        ]
      },
      {
        id: 18,
        title: '表达能力',
        description: '表达自己思想和需求的能力。',
        options: [
          { score: 4, label: '能正常表达自己的想法' },
          { score: 3, label: '能表达自己的需求，但需要增加时间' },
          { score: 2, label: '表达有困难，需频繁重复或简化口头表达' },
          { score: 1, label: '表达有严重困难，需要大量他人帮助' },
          { score: 0, label: '完全不能表达需求' }
        ]
      }
    ]
  },
  {
    title: '四、精神状态指标和评分（19-21）',
    items: [
      {
        id: 19,
        title: '攻击行为',
        description: '身体或语言攻击行为（近一个月情况），如踢/推/咬/抓/摔东西，骂人/威胁/尖叫等。',
        options: [
          { score: 1, label: '未出现' },
          { score: 0, label: '近一个月内出现过攻击行为' }
        ]
      },
      {
        id: 20,
        title: '抑郁症状',
        description: '存在情绪低落、兴趣减退、活力减退，或妄想幻觉、自杀念头/行为。',
        options: [
          { score: 1, label: '未出现' },
          { score: 0, label: '近一个月内出现过负性情绪' }
        ]
      },
      {
        id: 21,
        title: '意识水平',
        description: '机体对自身和周围环境刺激做出应答反应的能力程度。',
        options: [
          { score: 2, label: '神智清醒，对周围环境能做出正确反应' },
          { score: 1, label: '嗜睡或意识模糊，可唤醒并短暂正确应答' },
          { score: 0, label: '昏睡或昏迷，对一般刺激无反应或仅短暂反应' }
        ]
      }
    ]
  },
  {
    title: '五、感知觉与社会参与指标和评分（22-26）',
    items: [
      {
        id: 22,
        title: '视力',
        description: '感受光线并感受物体大小、形状的能力，在个体的最好矫正视力下进行评估。',
        options: [
          { score: 2, label: '视力正常' },
          { score: 1, label: '看清楚大字体，但看不清报上的标准字体，视力有限，看不清报纸的大标题，但能辨别物体。' },
          { score: 0, label: '只能看到光颜色和形状；完全失明' }
        ]
      },
      {
        id: 23,
        title: '听力',
        description: '能辨别声音的方位、音调、音量和音质的能力，可借助平时使用的助听设备等。',
        options: [
          { score: 2, label: '听力正常' },
          { score: 1, label: '在轻声说话或说话距离超过两米时听不清，正常交流时有困难，需在安静的环境或大声说话才能听见' },
          { score: 0, label: '讲话者大声说话或说话很慢才能部分听见；完全失聪' }
        ]
      },
      {
        id: 24,
        title: '执行日常事务',
        description: '计划安排并完成日常事务，包括但不限于；洗衣服；小金额购物；服药管理等。',
        options: [
          { score: 4, label: '能完全独立计划安排和完成日常事务，无需协助' },
          { score: 3, label: '在计划安排和完成日常事务时，需要他人监护或指导' },
          { score: 2, label: '在计划安排和完成日常事务时需要少量协助' },
          { score: 1, label: '在计划安排和完成日常事务时需要大量协助' },
          { score: 0, label: '完全依赖他人，进行日常事务' }
        ]
      },
      {
        id: 25,
        title: '使用交通工具外出',
        description: '使用交通工具外出能力。',
        options: [
          { score: 2, label: '能自己骑车或搭乘公共交通工具外出' },
          { score: 1, label: '能自己搭乘出租车，但不会搭乘其他公共交通工具外出' },
          { score: 0, label: '当有人协助或陪伴，可搭乘公共交通工具外出/只能在他人协助下搭乘出租车或私家车外出，完全不能出门或外出；完全需要协助' }
        ]
      },
      {
        id: 26,
        title: '社会交往',
        description: '参与社会与人际交往能力。',
        options: [
          { score: 4, label: '参与社会在社会环境有一定的适应能力，待人接物恰当' },
          { score: 3, label: '能适应单纯环境主动接触他人见面时能让人发现智力问题，不能理解隐喻语' },
          { score: 2, label: '脱离社会，被动接触不主动待他人，谈话中很多不适词句容易上当受骗' },
          { score: 1, label: '与他人接触内容不清楚表情不恰当' },
          { score: 0, label: '不能与人接触交往' }
        ]
      }
    ]
  }
]

const assessmentTypeOptions = [
  { value: 'ADMISSION', label: '入住评估' },
  { value: 'REGISTRATION', label: '评估登记' },
  { value: 'CONTINUOUS', label: '持续评估' },
  { value: 'ARCHIVE', label: '评估档案' },
  { value: 'OTHER_SCALE', label: '其他量表评估' },
  { value: 'COGNITIVE', label: '认知能力评估' },
  { value: 'SELF_CARE', label: '自理能力评估' }
]

const pageConfigMap: Record<AssessmentType, PageConfig> = {
  ADMISSION: {
    keywordLabel: '老人姓名',
    keywordPlaceholder: '请输入老人姓名',
    dateLabel: '评估日期',
    showStatusFilter: false,
    showDateFilter: false,
    showArchiveTypeFilter: false,
    showContentKeyword: false,
    showCreateButton: true,
    showBatchDelete: false,
    showExportButton: false,
    createButtonText: '新建入住评估',
    actions: ['assign', 'edit', 'view', 'delete'],
    columns: [
      { title: '序号', key: 'index', width: 70, fixed: 'left' },
      { title: '老人姓名', key: 'elderName', dataIndex: 'elderName', width: 130 },
      { title: '评估人', key: 'assessorName', dataIndex: 'assessorName', width: 120 },
      { title: '评估日期', key: 'assessmentDate', dataIndex: 'assessmentDate', width: 130 },
      { title: '是否完成评估', key: 'completedFlag', width: 130 },
      { title: '评估结果', key: 'resultSummary', dataIndex: 'resultSummary', width: 220 },
      { title: '评估得分', key: 'score', dataIndex: 'score', width: 100 },
      { title: '所属机构', key: 'orgName', width: 140 },
      { title: '指派人', key: 'assigneeName', dataIndex: 'assessorName', width: 120 },
      { title: '操作', key: 'action', width: 220, fixed: 'right' }
    ]
  },
  REGISTRATION: {
    keywordLabel: '用户姓名',
    keywordPlaceholder: '请输入用户姓名',
    dateLabel: '评估日期',
    showStatusFilter: false,
    showDateFilter: false,
    showArchiveTypeFilter: false,
    showContentKeyword: false,
    showCreateButton: true,
    showBatchDelete: false,
    showExportButton: false,
    createButtonText: '新增',
    actions: ['edit', 'view', 'delete'],
    columns: [
      { title: '序号', key: 'index', width: 70, fixed: 'left' },
      { title: '用户姓名', key: 'elderName', dataIndex: 'elderName', width: 120 },
      { title: '评估类型', key: 'assessmentType', dataIndex: 'assessmentType', width: 140 },
      { title: '评估结果', key: 'resultSummary', dataIndex: 'resultSummary', width: 220 },
      { title: '评估人', key: 'assessorName', dataIndex: 'assessorName', width: 120 },
      { title: '评估地点', key: 'assessmentPlace', width: 140 },
      { title: '评估日期', key: 'assessmentDate', dataIndex: 'assessmentDate', width: 130 },
      { title: '所属机构', key: 'orgName', width: 140 },
      { title: '操作', key: 'action', width: 180, fixed: 'right' }
    ]
  },
  CONTINUOUS: {
    keywordLabel: '老人姓名',
    keywordPlaceholder: '请输入老人姓名',
    dateLabel: '评估日期',
    showStatusFilter: false,
    showDateFilter: false,
    showArchiveTypeFilter: false,
    showContentKeyword: false,
    showCreateButton: true,
    showBatchDelete: true,
    showExportButton: false,
    createButtonText: '新增',
    actions: ['edit'],
    columns: [
      { title: '序号', key: 'index', width: 70, fixed: 'left' },
      { title: '老人姓名', key: 'elderName', dataIndex: 'elderName', width: 130 },
      { title: '评估人', key: 'assessorName', dataIndex: 'assessorName', width: 120 },
      { title: '评估日期', key: 'assessmentDate', dataIndex: 'assessmentDate', width: 130 },
      { title: '是否完成评估', key: 'completedFlag', width: 130 },
      { title: '评估结果', key: 'resultSummary', dataIndex: 'resultSummary', width: 220 },
      { title: '评估得分', key: 'score', dataIndex: 'score', width: 100 },
      { title: '所属机构', key: 'orgName', width: 140 },
      { title: '操作', key: 'action', width: 120, fixed: 'right' }
    ]
  },
  ARCHIVE: {
    keywordLabel: '老人姓名',
    keywordPlaceholder: '请输入老人姓名',
    dateLabel: '评估日期',
    showStatusFilter: false,
    showDateFilter: true,
    showArchiveTypeFilter: true,
    showContentKeyword: false,
    showCreateButton: false,
    showBatchDelete: false,
    showExportButton: true,
    createButtonText: '新增',
    actions: ['view'],
    columns: [
      { title: '序号', key: 'index', width: 70, fixed: 'left' },
      { title: '老人姓名', key: 'elderName', dataIndex: 'elderName', width: 130 },
      { title: '状态', key: 'status', dataIndex: 'status', width: 110 },
      { title: '性别', key: 'gender', dataIndex: 'gender', width: 90 },
      { title: '年龄', key: 'age', dataIndex: 'age', width: 90 },
      { title: '家庭地址', key: 'address', dataIndex: 'address', width: 260 },
      { title: '评估类型', key: 'assessmentType', dataIndex: 'assessmentType', width: 140 },
      { title: '评估日期', key: 'assessmentDate', dataIndex: 'assessmentDate', width: 130 },
      { title: '评估结论', key: 'resultSummary', dataIndex: 'resultSummary', width: 220 },
      { title: '所属机构', key: 'orgName', width: 140 },
      { title: '操作', key: 'action', width: 100, fixed: 'right' }
    ]
  },
  OTHER_SCALE: {
    keywordLabel: '老人姓名',
    keywordPlaceholder: '请输入老人姓名',
    dateLabel: '评估日期',
    showStatusFilter: false,
    showDateFilter: false,
    showArchiveTypeFilter: false,
    showContentKeyword: true,
    showCreateButton: true,
    showBatchDelete: false,
    showExportButton: true,
    createButtonText: '新增',
    actions: ['edit', 'delete', 'view'],
    columns: [
      { title: '序号', key: 'index', width: 70, fixed: 'left' },
      { title: '评估编码', key: 'assessmentCode', width: 180 },
      { title: '老人姓名', key: 'elderName', dataIndex: 'elderName', width: 120 },
      { title: '性别', key: 'gender', dataIndex: 'gender', width: 90 },
      { title: '年龄', key: 'age', dataIndex: 'age', width: 90 },
      { title: '手机号', key: 'phone', dataIndex: 'phone', width: 130 },
      { title: '评估内容', key: 'assessmentContent', width: 180 },
      { title: '评估时间', key: 'assessmentTime', width: 170 },
      { title: '评估人', key: 'assessorName', dataIndex: 'assessorName', width: 120 },
      { title: '评估结果', key: 'resultSummary', dataIndex: 'resultSummary', width: 180 },
      { title: '所属机构', key: 'orgName', width: 140 },
      { title: '操作', key: 'action', width: 180, fixed: 'right' }
    ]
  },
  COGNITIVE: {
    keywordLabel: '老人姓名',
    keywordPlaceholder: '请输入老人姓名',
    dateLabel: '评估日期',
    showStatusFilter: false,
    showDateFilter: true,
    showArchiveTypeFilter: false,
    showContentKeyword: false,
    showCreateButton: true,
    showBatchDelete: false,
    showExportButton: false,
    createButtonText: '新增',
    actions: ['edit', 'view', 'delete'],
    columns: [
      { title: '序号', key: 'index', width: 70, fixed: 'left' },
      { title: '老人姓名', key: 'elderName', dataIndex: 'elderName', width: 130 },
      { title: '性别', key: 'gender', dataIndex: 'gender', width: 90 },
      { title: '年龄', key: 'age', dataIndex: 'age', width: 90 },
      { title: '家庭地址', key: 'address', dataIndex: 'address', width: 260 },
      { title: '评估日期', key: 'assessmentDate', dataIndex: 'assessmentDate', width: 130 },
      { title: '评估结果', key: 'resultSummary', dataIndex: 'resultSummary', width: 220 },
      { title: '所属机构', key: 'orgName', width: 140 },
      { title: '操作', key: 'action', width: 100, fixed: 'right' }
    ]
  },
  SELF_CARE: {
    keywordLabel: '老人姓名',
    keywordPlaceholder: '请输入老人姓名',
    dateLabel: '评估日期',
    showStatusFilter: false,
    showDateFilter: true,
    showArchiveTypeFilter: false,
    showContentKeyword: false,
    showCreateButton: true,
    showBatchDelete: false,
    showExportButton: false,
    createButtonText: '新增',
    actions: ['edit', 'view', 'delete'],
    columns: [
      { title: '序号', key: 'index', width: 70, fixed: 'left' },
      { title: '老人姓名', key: 'elderName', dataIndex: 'elderName', width: 130 },
      { title: '性别', key: 'gender', dataIndex: 'gender', width: 90 },
      { title: '年龄', key: 'age', dataIndex: 'age', width: 90 },
      { title: '家庭地址', key: 'address', dataIndex: 'address', width: 260 },
      { title: '评估日期', key: 'assessmentDate', dataIndex: 'assessmentDate', width: 130 },
      { title: '评估结果', key: 'resultSummary', dataIndex: 'resultSummary', width: 220 },
      { title: '所属机构', key: 'orgName', width: 140 },
      { title: '操作', key: 'action', width: 100, fixed: 'right' }
    ]
  }
}

const pageConfig = computed(() => pageConfigMap[props.assessmentType])
const useElderKeywordSelect = computed(() => {
  const label = String(pageConfig.value.keywordLabel || '')
  return label.includes('老人') || label.includes('长者')
})
const isAdmissionAssessment = computed(() => props.assessmentType === 'ADMISSION')
const admissionScoreItemList = admissionAbilityGroups.flatMap((group) => group.items)
const admissionMaxRawScore = admissionScoreItemList.reduce((sum, item) => sum + Math.max(...item.options.map((opt) => opt.score)), 0)
const admissionScores = reactive<Record<number, number | undefined>>({})

const admissionRawScore = computed(() =>
  admissionScoreItemList.reduce((sum, item) => sum + (Number(admissionScores[item.id]) || 0), 0)
)

const admissionTotalScore = computed(() => {
  if (!admissionMaxRawScore) return 0
  const normalized = (admissionRawScore.value / admissionMaxRawScore) * 90
  return Number(normalized.toFixed(1))
})

const admissionLevelText = computed(() => {
  if (admissionMissingItems.value.length > 0) {
    return '待完成评分'
  }
  return resolveAbilityLevel(admissionTotalScore.value)
})
const admissionMissingItems = computed(() =>
  admissionScoreItemList.filter((item) => {
    const current = admissionScores[item.id]
    return (current === undefined || current === null) && Math.max(...item.options.map((opt) => opt.score)) > 0
  })
)
const admissionResidentIdFromRoute = computed(() => firstRouteQueryText(route.query.residentId || route.query.elderId))
const admissionContractNoFromRoute = computed(() => String(route.query.contractNo || '').trim())
const admissionElderNameFromRoute = computed(() => String(route.query.elderName || '').trim())
const admissionAutoOpenMode = computed(() => String(route.query.mode || 'new'))
const admissionShouldAutoOpen = computed(() => String(route.query.autoOpen || '') === '1')
const admissionCloseLoopEnabled = computed(() => String(route.query.closeLoop || route.query.returnToAdmission || '') === '1')
const admissionRouteHandled = ref(false)
const routeElderId = computed(() => {
  const raw = firstRouteQueryText(route.query.residentId || route.query.elderId)
  return raw || undefined
})
const archiveStatusTab = ref<'ALL' | 'DRAFT' | 'COMPLETED' | 'ARCHIVED'>('ALL')

const query = reactive({
  keyword: '',
  elderId: undefined as string | number | undefined,
  contentKeyword: '',
  archiveType: undefined as AssessmentType | undefined,
  status: undefined as string | undefined,
  dateRange: undefined as string[] | undefined,
  pageNo: 1,
  pageSize: 10
})

function firstRouteQueryText(value: unknown) {
  if (Array.isArray(value)) return firstRouteQueryText(value[0])
  if (value == null) return ''
  return String(value).trim()
}

function parsePositiveInt(value: unknown, fallback: number) {
  const parsed = Number(firstRouteQueryText(value))
  if (!Number.isFinite(parsed) || parsed <= 0) return fallback
  return Math.round(parsed)
}

function normalizeAssessmentStatus(value: unknown): 'DRAFT' | 'COMPLETED' | 'ARCHIVED' | undefined {
  const text = firstRouteQueryText(value).toUpperCase()
  if (text === 'DRAFT' || text === 'COMPLETED' || text === 'ARCHIVED') {
    return text
  }
  return undefined
}

function normalizeAssessmentArchiveType(value: unknown): AssessmentType | undefined {
  const text = firstRouteQueryText(value).toUpperCase()
  if (!text) return undefined
  if (assessmentTypeOptions.some((item) => item.value === text)) {
    return text as AssessmentType
  }
  return undefined
}

function flattenAssessmentRouteQuery(source: Record<string, unknown>) {
  return Object.entries(source || {}).reduce<Record<string, string>>((acc, [key, value]) => {
    const text = firstRouteQueryText(value)
    if (!text) return acc
    acc[key] = text
    return acc
  }, {})
}

function buildAssessmentRouteSignature(source: Record<string, unknown>) {
  const keys = [...ASSESSMENT_ROUTE_KEYS, ...ASSESSMENT_ROUTE_LEGACY_KEYS, ...ASSESSMENT_ROUTE_OBSERVE_KEYS]
  return keys
    .sort()
    .map((key) => `${key}:${firstRouteQueryText(source[key])}`)
    .join('|')
}

function resetAssessmentQueryState() {
  query.keyword = ''
  query.elderId = undefined
  query.contentKeyword = ''
  query.archiveType = undefined
  query.status = undefined
  query.dateRange = undefined
  query.pageNo = 1
  query.pageSize = 10
  archiveStatusTab.value = 'ALL'
}

function applyAssessmentQueryFromRoute() {
  resetAssessmentQueryState()

  const legacyKeyword = firstRouteQueryText(route.query.keyword || route.query.elderName)
  const keyword = firstRouteQueryText(route.query.assessKeyword) || legacyKeyword
  if (keyword) query.keyword = keyword

  const elderId = firstRouteQueryText(route.query.assessElderId || route.query.elderId || route.query.residentId)
  if (elderId && props.assessmentType === 'ARCHIVE') {
    query.elderId = elderId
    ensureSelectedElder(elderId)
  }

  const contentKeyword = firstRouteQueryText(route.query.assessContentKeyword || route.query.contentKeyword)
  if (contentKeyword) query.contentKeyword = contentKeyword

  const archiveType = normalizeAssessmentArchiveType(route.query.assessArchiveType || route.query.archiveType)
  if (archiveType && props.assessmentType === 'ARCHIVE') {
    query.archiveType = archiveType
  }

  const status = normalizeAssessmentStatus(route.query.assessStatus || route.query.status)
  if (status) {
    query.status = status
    if (props.assessmentType === 'ARCHIVE') {
      archiveStatusTab.value = status
    }
  }

  const dateStart = firstRouteQueryText(route.query.assessDateStart || route.query.dateFrom)
  const dateEnd = firstRouteQueryText(route.query.assessDateEnd || route.query.dateTo)
  if (dateStart && dateEnd) {
    query.dateRange = [dateStart, dateEnd]
  }

  query.pageNo = parsePositiveInt(route.query.assessPageNo || route.query.pageNo, 1)
  query.pageSize = parsePositiveInt(route.query.assessPageSize || route.query.pageSize, 10)
}

function buildAssessmentRouteQuery() {
  const nextQuery: Record<string, string> = {}
  Object.entries(flattenAssessmentRouteQuery(route.query as Record<string, unknown>)).forEach(([key, value]) => {
    if ((ASSESSMENT_ROUTE_KEYS as readonly string[]).includes(key)) return
    if ((ASSESSMENT_ROUTE_LEGACY_KEYS as readonly string[]).includes(key)) return
    nextQuery[key] = value
  })

  if (query.keyword) nextQuery.assessKeyword = query.keyword
  if (props.assessmentType === 'ARCHIVE' && query.elderId) nextQuery.assessElderId = String(query.elderId)
  if (query.contentKeyword) nextQuery.assessContentKeyword = query.contentKeyword
  if (props.assessmentType === 'ARCHIVE' && query.archiveType) nextQuery.assessArchiveType = String(query.archiveType)

  const normalizedStatus = normalizeAssessmentStatus(query.status)
  if (pageConfig.value.showStatusFilter && normalizedStatus) {
    nextQuery.assessStatus = normalizedStatus
  }
  if (query.dateRange?.[0]) nextQuery.assessDateStart = query.dateRange[0]
  if (query.dateRange?.[1]) nextQuery.assessDateEnd = query.dateRange[1]
  nextQuery.assessPageNo = String(parsePositiveInt(query.pageNo, 1))
  nextQuery.assessPageSize = String(parsePositiveInt(query.pageSize, 10))
  return nextQuery
}

function hasSameAssessmentRouteQuery(nextQuery: Record<string, string>) {
  const currentQuery = flattenAssessmentRouteQuery(route.query as Record<string, unknown>)
  const currentKeys = Object.keys(currentQuery)
  const nextKeys = Object.keys(nextQuery)
  if (currentKeys.length !== nextKeys.length) return false
  return nextKeys.every((key) => currentQuery[key] === nextQuery[key])
}

async function syncAssessmentQueryToRoute() {
  const nextQuery = buildAssessmentRouteQuery()
  if (hasSameAssessmentRouteQuery(nextQuery)) return
  skipNextAssessmentRouteWatch.value = true
  assessmentRouteSignature.value = buildAssessmentRouteSignature(nextQuery)
  await router.replace({ path: route.path, query: nextQuery })
}

async function copyQueryLink() {
  const href = router.resolve({ path: route.path, query: buildAssessmentRouteQuery() }).href
  const fullUrl = /^https?:\/\//i.test(href) ? href : `${window.location.origin}${href}`
  const copied = await copyText(fullUrl)
  if (copied) {
    message.success('查询链接已复制')
    return
  }
  message.warning('复制失败，请手动复制地址栏链接')
}

const open = ref(false)
const formReadonly = ref(false)
const formRef = ref<FormInstance>()
const selectedRowKeys = ref<string[]>([])
const batchAssignOpen = ref(false)
const batchAssignSubmitting = ref(false)
const batchAssignAssessorName = ref('')
const batchNextDateOpen = ref(false)
const batchNextDateSubmitting = ref(false)
const batchNextAssessmentDate = ref<string>('')
const lastBatchAction = ref('')
const changedRowIdSet = ref<Set<number>>(new Set())
const selectedByFilter = ref(false)
const form = reactive<Partial<AssessmentRecord>>({
  elderId: undefined,
  assessmentType: props.assessmentType,
  templateId: undefined,
  elderName: '',
  levelCode: '',
  score: undefined,
  assessmentDate: '',
  nextAssessmentDate: undefined,
  status: 'COMPLETED',
  assessorName: '',
  archiveNo: '',
  resultSummary: '',
  suggestion: '',
  detailJson: '',
  scoreAuto: 0
})
const submitting = ref(false)
const scoreAutoChecked = ref(false)
const pendingAdmissionLoading = ref(false)
const pendingAdmissionContracts = ref<CrmContractItem[]>([])
const selectedPendingContractNo = ref('')

const rules: FormRules = {
  elderId: [{ required: true, message: '请选择老人' }],
  assessmentDate: [{ required: true, message: '请选择评估日期' }],
  status: [{ required: true, message: '请选择状态' }]
}

const tableColumns = computed(() => pageConfig.value.columns)
const canDeleteAssessment = computed(() => {
  const roles = (userStore.roles || []).map((item) => String(item || '').toUpperCase())
  return hasMinisterOrHigher(roles)
})
const selectedPendingContract = computed(() =>
  pendingAdmissionContracts.value.find((item) => item.contractNo === selectedPendingContractNo.value)
)
const pendingAdmissionColumns = [
  { title: '合同编号', dataIndex: 'contractNo', key: 'contractNo', width: 180 },
  { title: '老人姓名', dataIndex: 'elderName', key: 'elderName', width: 140 },
  { title: '流程阶段', dataIndex: 'flowStage', key: 'flowStage', width: 120 },
  { title: '状态', dataIndex: 'contractStatus', key: 'contractStatus', width: 120 },
  { title: '签约时间', dataIndex: 'contractSignedAt', key: 'contractSignedAt', width: 170 }
]
const pendingAdmissionRowSelection = computed(() => ({
  type: 'radio' as const,
  selectedRowKeys: selectedPendingContractNo.value ? [selectedPendingContractNo.value] : [],
  onChange: (keys: Array<string | number>) => {
    const picked = String(keys?.[0] || '').trim()
    if (!picked) return
    choosePendingContract(picked).catch(() => {})
  }
}))
const selectedAdmissionRecord = computed(() => {
  if (selectedRowKeys.value.length !== 1) return null
  const key = Number(selectedRowKeys.value[0])
  return rows.value.find((item) => Number(item.id) === key) || null
})
const admissionLifecycleStage = computed(() => {
  if (!isAdmissionAssessment.value) return 'PENDING_ASSESSMENT'
  if (selectedPendingContract.value?.flowStage) {
    return normalizeLifecycleStage(selectedPendingContract.value.flowStage, selectedPendingContract.value.contractStatus)
  }
  const selected = selectedAdmissionRecord.value
  if (selected) {
    return selected.status === 'DRAFT'
      ? 'PENDING_ASSESSMENT'
      : 'PENDING_BED_SELECT'
  }
  return 'PENDING_ASSESSMENT'
})
const admissionLifecycleSubject = computed(() => {
  if (!isAdmissionAssessment.value) return ''
  if (selectedPendingContract.value) {
    return `合同 ${selectedPendingContract.value.contractNo || '-'} / 长者 ${resolveAssessmentElderName(selectedPendingContract.value as any)}`
  }
  if (selectedAdmissionRecord.value) {
    return `评估记录 ${selectedAdmissionRecord.value.archiveNo || selectedAdmissionRecord.value.id || '-'} / 长者 ${resolveAssessmentElderName(selectedAdmissionRecord.value as any)}`
  }
  return '未勾选待评估合同，建议先在待评估合同列表中选择一条。'
})
const admissionLifecycleHint = computed(() => {
  if (!isAdmissionAssessment.value) return ''
  if (!selectedPendingContract.value) {
    return '请先在待评估合同列表中勾选合同，再新建入住评估。'
  }
  if (selectedAdmissionRecord.value?.status === 'DRAFT') {
    return '评估仍是草稿，建议先补全评分与建议后再推进。'
  }
  return lifecycleStageHint(admissionLifecycleStage.value)
})
const admissionModalConfirmText = computed(() => {
  if (formReadonly.value) return '已查看'
  if (isAdmissionAssessment.value && admissionCloseLoopEnabled.value) return '保存并回流入住办理'
  return '保存'
})

function assessmentRowKey(record: AssessmentRecord) {
  return String(record?.id ?? '').trim()
}
const rowSelection = computed(() => {
  return {
    preserveSelectedRowKeys: true,
    selectedRowKeys: selectedRowKeys.value,
    onChange: (keys: Array<number | string>) => {
      selectedRowKeys.value = keys
        .map((item) => String(item ?? '').trim())
        .filter((item) => item.length > 0)
      if (selectedByFilter.value && selectedRowKeys.value.length <= rows.value.length) {
        selectedByFilter.value = false
      }
      if (isAdmissionAssessment.value && selectedRowKeys.value.length === 1) {
        const selected = rows.value.find((item) => assessmentRowKey(item) === selectedRowKeys.value[0])
        const contractNo = String(selected?.archiveNo || '').trim()
        if (contractNo && pendingAdmissionContracts.value.some((item) => item.contractNo === contractNo)) {
          selectedPendingContractNo.value = contractNo
        }
      }
    }
  }
})
const selectedRowsInCurrentPage = computed(() => {
  if (!selectedRowKeys.value.length || !rows.value.length) return []
  const idSet = new Set(selectedRowKeys.value)
  return rows.value.filter((item) => idSet.has(assessmentRowKey(item)))
})
const selectedSingleRecord = computed(() => {
  if (selectedRowKeys.value.length !== 1) return null
  const targetKey = selectedRowKeys.value[0]
  return rows.value.find((item) => assessmentRowKey(item) === targetKey) || null
})
const selectedSummaryText = computed(() => {
  const selectedCount = selectedRowKeys.value.length
  if (!selectedCount) {
    return '未勾选评估记录'
  }
  const selectedLabels = selectedRowsInCurrentPage.value
    .slice(0, 8)
    .map((item) => `${resolveAssessmentElderName(item as any)}（${item.archiveNo || item.id || '-'}）`)
  const hiddenCount = selectedRowsInCurrentPage.value.length - selectedLabels.length
  const outOfPageCount = selectedCount - selectedRowsInCurrentPage.value.length
  let text = `当前页已勾选：${selectedLabels.join('、') || '当前页暂无已勾选项'}`
  if (hiddenCount > 0) {
    text += `；另有 ${hiddenCount} 条未展开`
  }
  if (outOfPageCount > 0) {
    text += `；另有 ${outOfPageCount} 条在其他页`
  }
  return text
})

const tableCustomRow = (record: AssessmentRecord) => {
  if (props.assessmentType !== 'ARCHIVE') {
    return {}
  }
  return {
    style: { cursor: 'pointer' },
    onClick: (event: MouseEvent) => {
      const target = event.target as HTMLElement | null
      if (target?.closest('button') || target?.closest('a')) {
        return
      }
      openForm(record, true)
    }
  }
}

const tableRowClassName = (record: AssessmentRecord) => {
  const id = Number(record.id)
  if (!Number.isFinite(id)) {
    return ''
  }
  return changedRowIdSet.value.has(id) ? 'row-batch-updated' : ''
}

function statusLabel(status?: string) {
  if (status === 'DRAFT') return '草稿'
  if (status === 'ARCHIVED') return '已归档'
  return '已完成'
}

function statusColor(status?: string) {
  if (status === 'DRAFT') return 'orange'
  if (status === 'ARCHIVED') return 'blue'
  return 'green'
}

function normalizeArchiveStatusByTab() {
  if (props.assessmentType !== 'ARCHIVE') {
    return pageConfig.value.showStatusFilter ? query.status : undefined
  }
  if (archiveStatusTab.value === 'ALL') {
    return undefined
  }
  return archiveStatusTab.value
}

function onArchiveStatusTabChange(tab: string) {
  archiveStatusTab.value = (tab as 'ALL' | 'DRAFT' | 'COMPLETED' | 'ARCHIVED') || 'ALL'
  query.status = archiveStatusTab.value === 'ALL' ? undefined : archiveStatusTab.value
  query.pageNo = 1
  selectedByFilter.value = false
  syncAssessmentQueryToRoute()
    .then(() => fetchData())
    .catch(() => {})
}

function assessmentTypeLabel(type?: AssessmentType | string) {
  if (!type) return '-'
  if (typeof type === 'string' && type.includes('评估')) return type
  const matched = assessmentTypeOptions.find((item) => item.value === type)
  return matched?.label || '-'
}

function pendingFlowStageText(stage?: string) {
  if (stage === 'PENDING_ASSESSMENT') return '待评估'
  if (stage === 'PENDING_BED_SELECT') return '待办理入住'
  if (stage === 'PENDING_SIGN') return '待签署'
  if (stage === 'SIGNED') return '已签署'
  return stage || '-'
}

function displayCell(record: Record<string, any>, key?: string) {
  if (!key) return '-'
  if (key === 'elderName') {
    return resolveAssessmentElderName(record)
  }
  const value = record[key]
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  return value
}

function resolveAssessmentElderName(record: Record<string, any>) {
  const rawName = String(record?.elderName || '').trim()
  if (rawName && !/^\d+$/.test(rawName)) return rawName
  const elderId = String(record?.elderId || '').trim()
  if (elderId) {
    const fromCache = String(findElderName(elderId as any) || '').trim()
    if (fromCache) return fromCache
  }
  return '未命名长者'
}

function orgDisplayName(record: AssessmentRecord & Record<string, any>) {
  if (record.orgName) return record.orgName
  if (record.orgId) return `机构${record.orgId}`
  return '-'
}

function buildQueryParams() {
  const assessmentTypeForQuery = props.assessmentType === 'ARCHIVE'
    ? query.archiveType
    : props.assessmentType
  const keyword = query.contentKeyword || query.keyword
  return {
    assessmentType: assessmentTypeForQuery,
    status: normalizeArchiveStatusByTab(),
    elderId: props.assessmentType === 'ARCHIVE' ? query.elderId : routeElderId.value,
    keyword,
    dateFrom: query.dateRange?.[0],
    dateTo: query.dateRange?.[1]
  }
}

async function loadPendingAdmissionContracts() {
  if (!isAdmissionAssessment.value) return
  pendingAdmissionLoading.value = true
  try {
    const elderIdFromRoute = firstRouteQueryText(route.query.residentId || route.query.elderId)
    const page: PageResult<CrmContractItem> = await getContractPage({
      pageNo: 1,
      pageSize: 200,
      flowStage: 'PENDING_ASSESSMENT',
      currentOwnerDept: 'ASSESSMENT',
      elderId: elderIdFromRoute || undefined
    })
    pendingAdmissionContracts.value = page.list || []
    const routeContractNo = admissionContractNoFromRoute.value
    if (routeContractNo && pendingAdmissionContracts.value.some((item) => item.contractNo === routeContractNo)) {
      selectedPendingContractNo.value = routeContractNo
      return
    }
    if (selectedPendingContractNo.value
      && pendingAdmissionContracts.value.some((item) => item.contractNo === selectedPendingContractNo.value)) {
      return
    }
    selectedPendingContractNo.value = ''
  } finally {
    pendingAdmissionLoading.value = false
  }
}

async function choosePendingContract(contractNo?: string) {
  const normalized = String(contractNo || '').trim()
  if (!normalized) return
  const target = pendingAdmissionContracts.value.find((item) => item.contractNo === normalized)
  if (!target) return
  selectedRowKeys.value = []
  selectedByFilter.value = false
  selectedPendingContractNo.value = normalized
  const nextQuery: Record<string, any> = { ...route.query, contractNo: normalized }
  if (target.elderId) nextQuery.residentId = target.elderId
  if (target.elderName) nextQuery.elderName = target.elderName
  const nextSignature = buildAssessmentRouteSignature(nextQuery)
  if (nextSignature === buildAssessmentRouteSignature(route.query as Record<string, unknown>)) {
    return
  }
  skipNextAssessmentRouteWatch.value = true
  assessmentRouteSignature.value = nextSignature
  await router.replace({ path: route.path, query: nextQuery })
}

function pendingAdmissionCustomRow(record: CrmContractItem) {
  return {
    onClick: () => choosePendingContract(record.contractNo || '').catch(() => {})
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params = {
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      ...buildQueryParams()
    }
    const [res, sum] = await Promise.all([
      getAssessmentRecordPage(params),
      getAssessmentRecordSummary(params)
    ])
    rows.value = res.list
    total.value = res.total
    Object.assign(summary, sum || {})
    if (isAdmissionAssessment.value) {
      await loadPendingAdmissionContracts()
    }
  } finally {
    loading.value = false
  }
}

async function fetchTemplates() {
  templates.value = await getAssessmentTemplateList({ assessmentType: props.assessmentType })
}

function onSearch() {
  query.pageNo = 1
  selectedByFilter.value = false
  syncAssessmentQueryToRoute()
    .then(() => fetchData())
    .catch(() => {})
}

function reset() {
  resetAssessmentQueryState()
  selectedRowKeys.value = []
  selectedByFilter.value = false
  syncAssessmentQueryToRoute()
    .then(() => fetchData())
    .catch(() => {})
}

function admissionItemMin(item: AdmissionAbilityItem) {
  return Math.min(...item.options.map((opt) => opt.score))
}

function admissionItemMax(item: AdmissionAbilityItem) {
  return Math.max(...item.options.map((opt) => opt.score))
}

function admissionOptionSelectOptions(item: AdmissionAbilityItem) {
  return item.options.map((opt) => ({
    value: opt.score,
    label: `${opt.score}分 - ${opt.label}`
  }))
}

function nextAdmissionItemId(currentItemId: number) {
  const index = admissionScoreItemList.findIndex((item) => item.id === currentItemId)
  if (index < 0 || index >= admissionScoreItemList.length - 1) return undefined
  return admissionScoreItemList[index + 1].id
}

async function focusAdmissionItemInput(itemId?: number) {
  if (!itemId) return
  await nextTick()
  const root = document.getElementById(`admission-score-input-wrap-${itemId}`) as HTMLElement | null
  if (!root) return
  const input = root.querySelector('input') as HTMLInputElement | null
  if (!input || input.disabled) return
  input.focus()
  input.select?.()
}

function onAdmissionScoreFilled(itemId: number, value: unknown) {
  if (formReadonly.value || !isAdmissionAssessment.value) return
  if (value === null || value === undefined || value === '') return
  const currentItem = admissionScoreItemList.find((item) => item.id === itemId)
  if (!currentItem) return
  const numericValue = Number(value)
  if (!Number.isFinite(numericValue)) return
  if (numericValue < admissionItemMin(currentItem) || numericValue > admissionItemMax(currentItem)) return
  const nextId = nextAdmissionItemId(itemId)
  if (!nextId) return
  focusAdmissionItemInput(nextId)
}

function onPageChange(page: number) {
  query.pageNo = page
  syncAssessmentQueryToRoute()
    .then(() => fetchData())
    .catch(() => {})
}

function onPageSizeChange(current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  syncAssessmentQueryToRoute()
    .then(() => fetchData())
    .catch(() => {})
}

async function searchElderOptions(keyword: string) {
  await loadElderOptions(keyword)
}

async function resolveContractNoByElder(elderId?: number | string) {
  if (!elderId) return ''
  const normalizedElderId = String(elderId).trim()
  if (!normalizedElderId) return ''
  try {
    const page: PageResult<CrmContractItem> = await getContractPage({
      pageNo: 1,
      pageSize: 20,
      elderId: normalizedElderId
    })
    const list = page.list || []
    const active = list.find((item) => item.contractNo && item.status !== 'VOID')
    return (active || list.find((item) => item.contractNo))?.contractNo || ''
  } catch {
    return ''
  }
}

async function onElderChange(elderId?: number | string) {
  form.elderName = findElderName(elderId as any)
  if (!elderId || formReadonly.value) return
  if (admissionContractNoFromRoute.value) {
    form.archiveNo = admissionContractNoFromRoute.value
    return
  }
  const contractNo = await resolveContractNoByElder(elderId)
  if (contractNo) {
    form.archiveNo = contractNo
  }
}

function resolveAbilityLevel(totalScore: number) {
  if (Number(admissionScores[21]) === 0) {
    return '3级：重度失能（意识水平）'
  }
  if (totalScore >= 90) return '0级：能力完好'
  if (totalScore >= 66) return '1级：轻度失能'
  if (totalScore >= 46) return '2级：中度失能'
  if (totalScore >= 30) return '3级：重度失能'
  return '4级：完全失能'
}

function resetAdmissionScores() {
  admissionScoreItemList.forEach((item) => {
    admissionScores[item.id] = item.options.length === 1 && item.options[0].score === 0 ? 0 : undefined
  })
}

function loadAdmissionScoresFromDetail(detailJson?: string) {
  resetAdmissionScores()
  if (!detailJson) return
  try {
    const parsed = JSON.parse(detailJson)
    const mapped = parsed?.admissionAbilityScores || {}
    admissionScoreItemList.forEach((item) => {
      if (mapped[item.id] !== undefined && mapped[item.id] !== null) {
        const parsedScore = Number(mapped[item.id])
        admissionScores[item.id] = Number.isFinite(parsedScore) ? parsedScore : undefined
      }
    })
  } catch {
    // ignore invalid legacy json
  }
}

function buildAdmissionDetailJson() {
  const scoreMap: Record<string, number> = {}
  admissionScoreItemList.forEach((item) => {
    const val = admissionScores[item.id]
    if (val !== undefined) {
      scoreMap[String(item.id)] = Number(val)
    }
  })
  return JSON.stringify({
    template: '老年人能力评估表（完整版）',
    scoreSystem: '90分制（按已填写项目归一化）',
    admissionAbilityScores: scoreMap,
    rawScore: admissionRawScore.value,
    normalizedScore: admissionTotalScore.value,
    level: admissionLevelText.value
  })
}

function assignRecord(record: AssessmentRecord) {
  openForm(record)
  message.info('请在弹窗中调整评估人后保存，即可完成指派')
}

function openForm(record?: AssessmentRecord, readonly = false) {
  if (!record && isAdmissionAssessment.value && !selectedPendingContract.value) {
    message.warning('请先勾选一条待评估合同，再新增入住评估')
    return
  }
  formReadonly.value = readonly
  if (record) {
    Object.assign(form, record)
    form.scoreAuto = record.scoreAuto === 1 ? 1 : 0
    ensureSelectedElder(record.elderId, record.elderName)
    scoreAutoChecked.value = record.scoreAuto === 1
    if (isAdmissionAssessment.value) {
      const recordContractNo = String(record.archiveNo || '').trim()
      if (recordContractNo && pendingAdmissionContracts.value.some((item) => item.contractNo === recordContractNo)) {
        selectedPendingContractNo.value = recordContractNo
      }
      loadAdmissionScoresFromDetail(record.detailJson)
      form.score = admissionTotalScore.value
      form.levelCode = resolveAbilityLevel(admissionTotalScore.value).split('：')[0]
    }
  } else {
    const defaultStatus = isAdmissionAssessment.value ? 'DRAFT' : 'COMPLETED'
    Object.assign(form, {
      id: undefined,
      elderId: undefined,
      assessmentType: props.assessmentType,
      templateId: undefined,
      elderName: '',
      levelCode: '',
      score: undefined,
      assessmentDate: new Date().toISOString().slice(0, 10),
      nextAssessmentDate: undefined,
      status: defaultStatus,
      assessorName: '',
      archiveNo: '',
      resultSummary: '',
      suggestion: '',
      detailJson: '',
      scoreAuto: 0
    })
    scoreAutoChecked.value = false
    if (isAdmissionAssessment.value) {
      resetAdmissionScores()
      form.score = admissionTotalScore.value
      form.levelCode = admissionLevelText.value === '待完成评分' ? '' : admissionLevelText.value.split('：')[0]
    }
    if (routeElderId.value) {
      form.elderId = routeElderId.value as any
      const routeElderName = findElderName(routeElderId.value as any)
      if (routeElderName) {
        form.elderName = routeElderName
      }
      ensureSelectedElder(routeElderId.value, routeElderName || undefined)
    }
    if (isAdmissionAssessment.value && selectedPendingContract.value) {
      const contract = selectedPendingContract.value
      if (contract.contractNo) {
        form.archiveNo = contract.contractNo
      }
      if (contract.elderName) {
        form.elderName = contract.elderName
      }
      if (contract.elderId) {
        form.elderId = contract.elderId as any
        ensureSelectedElder(contract.elderId as any, contract.elderName || undefined)
      }
    }
  }
  open.value = true
}

function onScoreAutoChange(checked: boolean) {
  form.scoreAuto = checked ? 1 : 0
}

watch(
  () => admissionTotalScore.value,
  (val) => {
    if (!isAdmissionAssessment.value || !open.value) return
    form.score = val
  }
)

watch(
  () => admissionLevelText.value,
  (val) => {
    if (!isAdmissionAssessment.value || !open.value) return
    if (admissionMissingItems.value.length > 0) {
      form.levelCode = ''
      return
    }
    form.levelCode = val.split('：')[0]
    if (!form.resultSummary) {
      form.resultSummary = val
    }
  }
)

async function previewScore() {
  if (!form.templateId) {
    message.warning('请先选择量表模板')
    return
  }
  if (!form.detailJson) {
    message.warning('请先填写量表明细JSON')
    return
  }
  const result = await previewAssessmentScore({
    templateId: form.templateId,
    detailJson: form.detailJson
  })
  form.score = result.score
  if (result.levelCode) {
    form.levelCode = result.levelCode
  }
  message.success('试算完成')
}

function openTemplateManager() {
  router.push('/elder/assessment/template')
}

function beforeDetailJsonUpload(file: File) {
  const reader = new FileReader()
  reader.onload = () => {
    const content = String(reader.result || '')
    if (!content.trim()) {
      message.warning('上传文件内容为空')
      return
    }
    form.detailJson = content
    message.success('量表明细已导入')
  }
  reader.onerror = () => {
    message.error('读取文件失败，请重试')
  }
  reader.readAsText(file, 'utf-8')
  return false
}

async function submit() {
  if (formReadonly.value) {
    open.value = false
    return
  }
  if (form.nextAssessmentDate && form.assessmentDate && form.nextAssessmentDate < form.assessmentDate) {
    message.warning('下次评估日期不能早于评估日期')
    return
  }
  if (!isAdmissionAssessment.value && form.scoreAuto !== 0) {
    if (!form.templateId) {
      message.warning('自动评分开启时必须选择量表模板')
      return
    }
    if (!form.detailJson) {
      message.warning('自动评分开启时必须填写量表明细JSON')
      return
    }
  }
  if (!isAdmissionAssessment.value && !form.elderId) {
    message.warning('请选择老人')
    return
  }
  await formRef.value?.validate()
  if (isAdmissionAssessment.value) {
    if (admissionMissingItems.value.length > 0) {
      message.warning(`请完成所有评分项，当前仍有 ${admissionMissingItems.value.length} 项未评分`)
      return
    }
    const outOfRangeItem = admissionScoreItemList.find((item) => {
      const val = Number(admissionScores[item.id])
      return val < admissionItemMin(item) || val > admissionItemMax(item)
    })
    if (outOfRangeItem) {
      message.warning(`第${outOfRangeItem.id}项分值超出范围，请按提示输入`)
      return
    }
    form.score = admissionTotalScore.value
    form.levelCode = resolveAbilityLevel(admissionTotalScore.value).split('：')[0]
    form.resultSummary = form.resultSummary || resolveAbilityLevel(admissionTotalScore.value)
    form.detailJson = buildAdmissionDetailJson()
    form.scoreAuto = 0
    form.templateId = undefined
    const admissionContractNo = String(
      selectedPendingContract.value?.contractNo
      || admissionContractNoFromRoute.value
      || form.archiveNo
      || ''
    ).trim()
    if ((form.status === 'COMPLETED' || form.status === 'ARCHIVED') && admissionContractNo) {
      form.archiveNo = admissionContractNo
    }
    if (admissionCloseLoopEnabled.value && form.status === 'DRAFT') {
      form.status = 'COMPLETED'
      message.info('闭环模式已自动将评估状态改为“已完成”')
    }
  }
  submitting.value = true
  try {
    const payload = {
      elderId: form.elderId,
      assessmentType: props.assessmentType,
      templateId: form.templateId,
      elderName: findElderName(form.elderId) || form.elderName,
      levelCode: form.levelCode,
      score: form.score,
      assessmentDate: form.assessmentDate,
      nextAssessmentDate: form.nextAssessmentDate,
      status: form.status,
      assessorName: form.assessorName,
      archiveNo: form.archiveNo,
      resultSummary: form.resultSummary,
      suggestion: form.suggestion,
      detailJson: form.detailJson,
      scoreAuto: form.scoreAuto,
      source: 'MANUAL'
    }
    if (form.id) {
      await updateAssessmentRecord(form.id, payload)
      message.success(isAdmissionAssessment.value ? '入住评估更新成功' : '更新成功')
    } else {
      await createAssessmentRecord(payload)
      message.success(isAdmissionAssessment.value ? '入住评估登记成功' : '登记成功')
    }
    open.value = false
    if (isAdmissionAssessment.value && admissionCloseLoopEnabled.value) {
      const contractNo = String(payload.archiveNo || admissionContractNoFromRoute.value || '').trim()
      const residentId = String(payload.elderId || admissionResidentIdFromRoute.value || '').trim()
      const params = new URLSearchParams()
      if (contractNo) params.set('contractNo', contractNo)
      if (residentId) params.set('residentId', residentId)
      message.success('评估完成，正在自动回流入住办理')
      const queryString = params.toString()
      const targetPath = queryString ? `/elder/admission-processing?${queryString}` : '/elder/admission-processing'
      await router.push(targetPath)
      return
    }
    fetchData()
  } finally {
    submitting.value = false
  }
}

function safeHtml(value?: string | number | null) {
  const text = value === null || value === undefined ? '' : String(value)
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

async function printReport(record: AssessmentRecord) {
  const report = await getAssessmentRecordReport(Number(record.id))
  if (!report) {
    message.warning('未找到评估报告数据')
    return
  }
  const popup = window.open('', '_blank', 'width=900,height=760')
  if (!popup) {
    message.warning('浏览器拦截了打印窗口，请允许弹窗后重试')
    return
  }
  popup.document.write(`
    <html>
      <head>
        <title>评估报告-${report.reportNo || report.recordId}</title>
        <style>
          body { font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", sans-serif; padding: 24px; color:#222; }
          .title { font-size: 22px; font-weight: 700; margin-bottom: 14px; }
          .meta { display:grid; grid-template-columns: 1fr 1fr; gap:8px 16px; margin-bottom:16px; font-size:14px; }
          .block { border:1px solid #ddd; padding:12px; margin-bottom:12px; border-radius:6px; }
          .label { font-weight:600; margin-bottom:8px; }
          .pre { white-space: pre-wrap; word-break: break-word; line-height: 1.7; }
        </style>
      </head>
      <body>
        <div class="title">入住评估报告</div>
        <div class="meta">
          <div>报告编号：${safeHtml(report.reportNo || '-')}</div>
          <div>记录ID：${safeHtml(report.recordId || '-')}</div>
          <div>老人姓名：${safeHtml(report.elderName || '-')}</div>
          <div>评估类型：${safeHtml(report.assessmentTypeLabel || report.assessmentType || '-')}</div>
          <div>评估日期：${safeHtml(report.assessmentDate || '-')}</div>
          <div>评估人：${safeHtml(report.assessorName || '-')}</div>
          <div>评估状态：${safeHtml(report.reportStatus || '-')}</div>
          <div>完成时间：${safeHtml(report.completedTime || '-')}</div>
          <div>分值：${safeHtml(report.score ?? '-')}</div>
          <div>等级：${safeHtml(report.levelCode || '-')}</div>
        </div>
        <div class="block">
          <div class="label">评估结论</div>
          <div class="pre">${safeHtml(report.resultSummary || '-')}</div>
        </div>
        <div class="block">
          <div class="label">护理建议</div>
          <div class="pre">${safeHtml(report.suggestion || '-')}</div>
        </div>
        <div class="block">
          <div class="label">评分明细</div>
          <div class="pre">${safeHtml(report.detailJson || '-')}</div>
        </div>
      </body>
    </html>
  `)
  popup.document.close()
  popup.focus()
  popup.print()
}

async function autoOpenAdmissionFromRoute() {
  if (!isAdmissionAssessment.value || admissionRouteHandled.value || !admissionShouldAutoOpen.value) {
    return
  }
  const residentId = admissionResidentIdFromRoute.value
  const contractNoFromRoute = admissionContractNoFromRoute.value
  const elderNameFromRoute = admissionElderNameFromRoute.value
  admissionRouteHandled.value = true

  const mode = admissionAutoOpenMode.value
  let draft: AssessmentRecord | undefined
  let latest: AssessmentRecord | undefined
  if (residentId) {
    const res: PageResult<AssessmentRecord> = await getAssessmentRecordPage({
      pageNo: 1,
      pageSize: 20,
      assessmentType: 'ADMISSION',
      elderId: residentId
    })
    const list = res.list || []
    draft = list.find((item) => item.status === 'DRAFT')
    latest = list[0]
  }

  if (mode === 'continue' && draft) {
    openForm(draft)
    if (!form.archiveNo && contractNoFromRoute) {
      form.archiveNo = contractNoFromRoute
    }
    return
  }

  if (mode === 'continue' && latest) {
    openForm(latest)
    if (!form.archiveNo && contractNoFromRoute) {
      form.archiveNo = contractNoFromRoute
    }
    return
  }

  openForm()
  form.status = 'DRAFT'
  form.assessmentDate = form.assessmentDate || new Date().toISOString().slice(0, 10)
  if (residentId) {
    form.elderId = residentId
    const elderName = findElderName(residentId) || elderNameFromRoute || ''
    form.elderName = elderName
    ensureSelectedElder(residentId, elderName || undefined)
  } else if (elderNameFromRoute) {
    form.elderName = elderNameFromRoute
  }
  if (contractNoFromRoute) {
    form.archiveNo = contractNoFromRoute
  } else if (residentId) {
    const autoContractNo = await resolveContractNoByElder(residentId)
    if (autoContractNo) {
      form.archiveNo = autoContractNo
    }
  }
}

function remove(id: number) {
  if (!canDeleteAssessment.value) {
    message.warning('仅主管及以上角色可删除评估记录')
    return
  }
  Modal.confirm({
    title: '确认删除该评估记录？',
    onOk: async () => {
      await deleteAssessmentRecord(id)
      message.success('删除成功')
      fetchData()
    }
  })
}

function removeBatch() {
  if (!canDeleteAssessment.value) {
    message.warning('仅主管及以上角色可批量删除')
    return
  }
  if (!selectedRowKeys.value.length) return
  Modal.confirm({
    title: `确认删除选中的 ${selectedRowKeys.value.length} 条评估记录？`,
    onOk: async () => {
      const result = await batchDeleteAssessmentRecord(selectedRowKeys.value.map((item) => Number(item)))
      handleBatchResultFeedback(result, '批量删除评估记录')
      fetchData()
    }
  })
}

async function exportCsv() {
  const blob = await exportAssessmentRecord({
    ...buildQueryParams()
  })
  const link = document.createElement('a')
  const url = URL.createObjectURL(new Blob([blob], { type: 'text/csv;charset=utf-8;' }))
  link.href = url
  link.download = `${title}_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  message.success('导出成功')
}

function getSelectedRows() {
  const idSet = new Set(selectedRowKeys.value)
  return rows.value.filter((item) => idSet.has(assessmentRowKey(item)))
}

function toggleSelectAll() {
  if (!rows.value.length) {
    selectedRowKeys.value = []
    selectedByFilter.value = false
    return
  }
  if (selectedRowKeys.value.length === rows.value.length) {
    selectedRowKeys.value = []
    selectedByFilter.value = false
    return
  }
  selectedRowKeys.value = rows.value
    .map((item) => assessmentRowKey(item))
    .filter((item) => item.length > 0)
  selectedByFilter.value = false
}

async function selectByFilter() {
  const ids = await getAssessmentRecordIds({
    ...buildQueryParams(),
    limit: 2000
  })
  const selected = (ids || [])
    .map((item) => String(item ?? '').trim())
    .filter((item) => item.length > 0)
  selectedRowKeys.value = selected
  selectedByFilter.value = selected.length > rows.value.length
  if (!selected.length) {
    message.warning('当前筛选条件下暂无可勾选记录')
    return
  }
  message.success(`已按筛选条件勾选 ${selected.length} 条（最多2000条）`)
}

function clearSelection() {
  selectedRowKeys.value = []
  selectedByFilter.value = false
}

function markLastBatchAction(text: string) {
  const timeText = new Date().toLocaleString()
  lastBatchAction.value = `${timeText}：${text}`
}

function handleBatchResultFeedback(result: AssessmentBatchOperationResult, successText: string) {
  const successCount = Number(result?.successCount || 0)
  const failedCount = Number(result?.failedCount || 0)
  const successIds = Array.isArray(result?.successIds) ? result.successIds : []
  const failedIds = Array.from(new Set(
    (result?.failures || [])
      .map((item) => String(item?.id ?? '').trim())
      .filter((item) => item.length > 0)
  ))
  changedRowIdSet.value = new Set(
    successIds
      .map((item) => Number(item))
      .filter((item) => Number.isFinite(item))
  )
  selectedRowKeys.value = failedIds
  selectedByFilter.value = failedIds.length > rows.value.length
  if (successCount > 0) {
    markLastBatchAction(`${successText}，成功 ${successCount} 条`)
  }
  if (successCount > 0 && failedCount === 0) {
    message.success(`批量处理成功，共 ${successCount} 条`)
    return
  }
  if (successCount > 0 && failedCount > 0) {
    message.warning(`部分成功：成功 ${successCount} 条，失败 ${failedCount} 条`)
    showBatchFailureDetails(result)
    return
  }
  message.error(`批量处理失败，共 ${failedCount} 条失败`)
  showBatchFailureDetails(result)
}

function showBatchFailureDetails(result: AssessmentBatchOperationResult) {
  const lines = (result?.failures || [])
    .slice(0, 50)
    .map((item) => {
      const idText = item?.id ? `ID=${item.id}` : 'ID=未知'
      const reason = String(item?.reason || '未知错误')
      return `${idText}：${reason}`
    })
  if (!lines.length) {
    return
  }
  const content = lines.join('\n')
  Modal.info({
    title: '批量处理失败明细',
    width: 680,
    content
  })
}

function markSelectedStatus(status: 'COMPLETED' | 'ARCHIVED') {
  if (!selectedRowKeys.value.length) {
    message.warning('请先勾选要处理的评估记录')
    return
  }
  const actionText = status === 'ARCHIVED' ? '归档' : '标记完成'
  Modal.confirm({
    title: `确认将勾选的 ${selectedRowKeys.value.length} 条记录${actionText}？`,
    onOk: async () => {
      const result = await batchUpdateAssessmentRecordStatus(
        selectedRowKeys.value.map((item) => Number(item)),
        status
      )
      handleBatchResultFeedback(
        result,
        `批量状态更新为${status === 'ARCHIVED' ? '已归档' : '已完成'}`
      )
      await fetchData()
    }
  })
}

function openBatchAssignModal() {
  if (!selectedRowKeys.value.length) {
    message.warning('请先勾选要指派的评估记录')
    return
  }
  const selected = getSelectedRows()
  const firstAssessor = selected.find((item) => String(item.assessorName || '').trim())?.assessorName || ''
  batchAssignAssessorName.value = firstAssessor
  batchAssignOpen.value = true
}

async function submitBatchAssign() {
  const assessorName = String(batchAssignAssessorName.value || '').trim()
  if (!assessorName) {
    message.warning('请输入评估人姓名')
    return
  }
  if (!selectedRowKeys.value.length) {
    batchAssignOpen.value = false
    return
  }
  batchAssignSubmitting.value = true
  try {
    const result = await batchAssignAssessmentRecord(
      selectedRowKeys.value.map((item) => Number(item)),
      assessorName
    )
    handleBatchResultFeedback(result, `批量指派评估人：${assessorName}`)
    batchAssignOpen.value = false
    await fetchData()
  } finally {
    batchAssignSubmitting.value = false
  }
}

function openBatchNextDateModal() {
  if (!selectedRowKeys.value.length) {
    message.warning('请先勾选要设置复评日期的评估记录')
    return
  }
  const selected = getSelectedRows()
  batchNextAssessmentDate.value = String(selected.find((item) => item.nextAssessmentDate)?.nextAssessmentDate || '')
  batchNextDateOpen.value = true
}

async function submitBatchNextDate() {
  const nextDate = String(batchNextAssessmentDate.value || '').trim()
  if (!nextDate) {
    message.warning('请选择下次评估日期')
    return
  }
  if (!selectedRowKeys.value.length) {
    batchNextDateOpen.value = false
    return
  }
  batchNextDateSubmitting.value = true
  try {
    const result = await batchUpdateAssessmentNextDate(
      selectedRowKeys.value.map((item) => Number(item)),
      nextDate
    )
    handleBatchResultFeedback(result, `批量设置复评日期：${nextDate}`)
    batchNextDateOpen.value = false
    await fetchData()
  } finally {
    batchNextDateSubmitting.value = false
  }
}

function exportSelectedCsv() {
  const selectedRows = getSelectedRows()
  if (!selectedRows.length) {
    message.warning('请先勾选要导出的评估记录')
    return
  }
  const headers = ['评估编码', '老人姓名', '评估类型', '评估日期', '状态', '评估结果', '分值', '评估人', '所属机构']
  const csvEscape = (value: unknown) => `"${String(value ?? '').replace(/"/g, '""')}"`
  const lines = selectedRows.map((item) => ([
    item.archiveNo || item.id || '-',
    resolveAssessmentElderName(item as any),
    assessmentTypeLabel(item.assessmentTypeLabel || item.assessmentType),
    item.assessmentDate || '-',
    item.statusLabel || statusLabel(item.status),
    item.resultSummary || '-',
    item.score ?? '-',
    item.assessorName || '-',
    orgDisplayName(item as any)
  ].map(csvEscape).join(',')))
  const content = ['\uFEFF' + headers.join(','), ...lines].join('\n')
  const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${title}_勾选_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  message.success(`已导出 ${selectedRows.length} 条记录`)
}

function exportSelectedPdf() {
  const selectedRows = getSelectedRows()
  if (!selectedRows.length) {
    message.warning('请先勾选要导出的评估记录')
    return
  }
  const popup = window.open('', '_blank', 'width=1220,height=820')
  if (!popup) {
    message.warning('浏览器拦截了弹窗，请允许后重试')
    return
  }
  const rowsHtml = selectedRows.map((item, index) => `
    <tr>
      <td>${index + 1}</td>
      <td>${safeHtml(item.archiveNo || item.id || '-')}</td>
      <td>${safeHtml(resolveAssessmentElderName(item as any))}</td>
      <td>${safeHtml(assessmentTypeLabel(item.assessmentTypeLabel || item.assessmentType))}</td>
      <td>${safeHtml(item.assessmentDate || '-')}</td>
      <td>${safeHtml(item.statusLabel || statusLabel(item.status))}</td>
      <td>${safeHtml(item.score ?? '-')}</td>
      <td>${safeHtml(item.assessorName || '-')}</td>
      <td>${safeHtml(item.resultSummary || '-')}</td>
    </tr>
  `).join('')
  popup.document.write(`
    <html>
      <head>
        <title>评估记录勾选导出</title>
        <style>
          body { font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", sans-serif; padding: 16px; color: #222; }
          h2 { margin: 0 0 10px 0; }
          table { width: 100%; border-collapse: collapse; font-size: 12px; }
          th, td { border: 1px solid #ddd; padding: 6px 8px; text-align: left; vertical-align: top; }
          th { background: #f5f5f5; }
        </style>
      </head>
      <body>
        <h2>评估记录（勾选导出）</h2>
        <div style="margin-bottom:8px;">导出时间：${safeHtml(new Date().toLocaleString())}</div>
        <table>
          <thead>
            <tr>
              <th>序号</th><th>评估编码</th><th>老人姓名</th><th>评估类型</th><th>评估日期</th><th>状态</th><th>分值</th><th>评估人</th><th>评估结果</th>
            </tr>
          </thead>
          <tbody>${rowsHtml}</tbody>
        </table>
      </body>
    </html>
  `)
  popup.document.close()
  popup.focus()
  popup.print()
}

function viewSelected() {
  if (selectedRowKeys.value.length !== 1) {
    message.warning('请勾选一条记录后查看')
    return
  }
  const selected = getSelectedRows()[0]
  if (!selected) {
    message.warning('当前页未找到勾选记录，请刷新后重试')
    return
  }
  openForm(selected, true)
}

function editSelected() {
  if (selectedRowKeys.value.length !== 1) {
    message.warning('请勾选一条记录后编辑')
    return
  }
  const selected = getSelectedRows()[0]
  if (!selected) {
    message.warning('当前页未找到勾选记录，请刷新后重试')
    return
  }
  openForm(selected)
}

function onSingleActionMenuClick({ key }: { key: string }) {
  if (key === 'view') {
    viewSelected()
    return
  }
  if (key === 'edit') {
    editSelected()
  }
}

function onBatchActionMenuClick({ key }: { key: string }) {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请先勾选记录')
    return
  }
  if (key === 'assign') {
    openBatchAssignModal()
    return
  }
  if (key === 'next-date') {
    openBatchNextDateModal()
    return
  }
  if (key === 'mark-completed') {
    markSelectedStatus('COMPLETED')
    return
  }
  if (key === 'mark-archived') {
    markSelectedStatus('ARCHIVED')
    return
  }
  if (key === 'delete' && pageConfig.value.showBatchDelete && canDeleteAssessment.value) {
    removeBatch()
  }
}

function onSelectionActionMenuClick({ key }: { key: string }) {
  if (key === 'select-filter') {
    selectByFilter()
    return
  }
  if (key === 'toggle-page') {
    toggleSelectAll()
    return
  }
  if (key === 'clear-selection') {
    clearSelection()
  }
}

function onExportActionMenuClick({ key }: { key: string }) {
  if (key === 'selected-excel') {
    exportSelectedCsv()
    return
  }
  if (key === 'selected-pdf') {
    exportSelectedPdf()
    return
  }
  if (key === 'all-excel') {
    exportCsv()
    return
  }
  if (key === 'page-excel') {
    exportCurrentPageCsv()
    return
  }
  if (key === 'page-pdf') {
    exportCurrentPagePdf()
  }
}

function selectedRecordElderContext() {
  const selected = selectedSingleRecord.value
  if (!selected) {
    message.warning('请先勾选当前页 1 条记录')
    return null
  }
  const elderId = Number(selected.elderId || 0)
  const elderName = String(selected.elderName || '').trim()
  if (!elderId) {
    message.warning('该评估记录未关联长者ID')
    return null
  }
  return {
    elderId,
    elderName,
    contractNo: String(selected.archiveNo || '').trim()
  }
}

function goSelectedInHospitalOverview() {
  const context = selectedRecordElderContext()
  if (!context) return
  router.push({
    path: '/elder/in-hospital-overview',
    query: {
      residentId: String(context.elderId),
      elderName: context.elderName || undefined
    }
  })
}

function goSelectedContractsInvoices() {
  const context = selectedRecordElderContext()
  if (!context) return
  router.push({
    path: '/elder/contracts-invoices',
    query: {
      elderId: String(context.elderId),
      elderName: context.elderName || undefined,
      contractNo: context.contractNo || undefined
    }
  })
}

function goSelectedAdmissionProcessing() {
  const context = selectedRecordElderContext()
  if (!context) return
  router.push({
    path: '/elder/admission-processing',
    query: {
      residentId: String(context.elderId),
      elderName: context.elderName || undefined,
      contractNo: context.contractNo || undefined
    }
  })
}

function exportCurrentPageCsv() {
  if (!rows.value.length) {
    message.warning('当前页无可导出记录')
    return
  }
  const headers = ['评估编码', '老人姓名', '评估类型', '评估日期', '状态', '评估结果', '分值', '评估人', '所属机构']
  const csvEscape = (value: unknown) => `"${String(value ?? '').replace(/"/g, '""')}"`
  const lines = rows.value.map((item) => ([
    item.archiveNo || item.id || '-',
    resolveAssessmentElderName(item as any),
    assessmentTypeLabel(item.assessmentTypeLabel || item.assessmentType),
    item.assessmentDate || '-',
    item.statusLabel || statusLabel(item.status),
    item.resultSummary || '-',
    item.score ?? '-',
    item.assessorName || '-',
    orgDisplayName(item as any)
  ].map(csvEscape).join(',')))
  const content = ['\uFEFF' + headers.join(','), ...lines].join('\n')
  const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `评估档案_本页_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  message.success('本页Excel导出成功')
}

function exportCurrentPagePdf() {
  if (!rows.value.length) {
    message.warning('当前页无可导出记录')
    return
  }
  const popup = window.open('', '_blank', 'width=1120,height=780')
  if (!popup) {
    message.warning('浏览器拦截了弹窗，请允许后重试')
    return
  }
  const rowsHtml = rows.value.map((item, index) => `
    <tr>
      <td>${index + 1}</td>
      <td>${safeHtml(item.archiveNo || item.id || '-')}</td>
      <td>${safeHtml(resolveAssessmentElderName(item as any))}</td>
      <td>${safeHtml(assessmentTypeLabel(item.assessmentTypeLabel || item.assessmentType))}</td>
      <td>${safeHtml(item.assessmentDate || '-')}</td>
      <td>${safeHtml(item.statusLabel || statusLabel(item.status))}</td>
      <td>${safeHtml(item.resultSummary || '-')}</td>
      <td>${safeHtml(item.score ?? '-')}</td>
      <td>${safeHtml(item.assessorName || '-')}</td>
      <td>${safeHtml(orgDisplayName(item as any))}</td>
    </tr>
  `).join('')
  popup.document.write(`
    <html>
      <head>
        <title>评估档案本页导出</title>
        <style>
          body { font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", sans-serif; padding: 16px; color: #222; }
          h2 { margin: 0 0 10px 0; }
          table { width: 100%; border-collapse: collapse; font-size: 12px; }
          th, td { border: 1px solid #ddd; padding: 6px 8px; text-align: left; vertical-align: top; }
          th { background: #f5f5f5; }
        </style>
      </head>
      <body>
        <h2>评估档案（本页）</h2>
        <div style="margin-bottom:8px;">导出时间：${safeHtml(new Date().toLocaleString())}</div>
        <table>
          <thead>
            <tr>
              <th>序号</th><th>评估编码</th><th>老人姓名</th><th>评估类型</th><th>评估日期</th><th>状态</th><th>评估结果</th><th>分值</th><th>评估人</th><th>所属机构</th>
            </tr>
          </thead>
          <tbody>${rowsHtml}</tbody>
        </table>
      </body>
    </html>
  `)
  popup.document.close()
  popup.focus()
  popup.print()
}

onMounted(async () => {
  applyAssessmentQueryFromRoute()
  assessmentRouteSignature.value = buildAssessmentRouteSignature(route.query as Record<string, unknown>)
  await Promise.all([fetchTemplates(), fetchData(), loadElderOptions()])
  await autoOpenAdmissionFromRoute()
  await syncAssessmentQueryToRoute().catch(() => {})
})

watch(
  () => route.query,
  (queryMap) => {
    const nextSignature = buildAssessmentRouteSignature(queryMap as Record<string, unknown>)
    if (skipNextAssessmentRouteWatch.value) {
      skipNextAssessmentRouteWatch.value = false
      assessmentRouteSignature.value = nextSignature
      return
    }
    if (nextSignature === assessmentRouteSignature.value) {
      return
    }
    assessmentRouteSignature.value = nextSignature
    applyAssessmentQueryFromRoute()
    fetchData().catch(() => {})
  },
  { deep: true }
)

useLiveSyncRefresh({
  topics: ['elder', 'health', 'oa', 'hr'],
  refresh: () => {
    if (loading.value) return
    fetchData().catch(() => {})
  },
  debounceMs: 900
})
</script>

<style scoped>
.search-bar {
  display: flex;
  flex-wrap: wrap;
  row-gap: 8px;
}

.table-actions {
  margin-bottom: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.hint {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}

:deep(.row-batch-updated > td) {
  background: #f6ffed !important;
}

:deep(.ant-table-selection-column .ant-checkbox .ant-checkbox-inner) {
  background: #fff;
  border-color: #8c8c8c;
}

:deep(.ant-table-selection-column .ant-checkbox-checked .ant-checkbox-inner) {
  background: #1677ff;
  border-color: #1677ff;
}

:deep(.ant-table-selection-column .ant-checkbox-checked .ant-checkbox-inner::after) {
  border-color: #fff;
}
</style>
