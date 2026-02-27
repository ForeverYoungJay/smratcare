<template>
  <PageContainer :title="title" :subTitle="subTitle">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item :label="pageConfig.keywordLabel">
          <a-input v-model:value="query.keyword" :placeholder="pageConfig.keywordPlaceholder" allow-clear />
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
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-row :gutter="[12, 12]" style="margin-top: 16px">
      <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="总记录" :value="summary.totalCount || 0" /></a-card></a-col>
      <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="草稿" :value="summary.draftCount || 0" /></a-card></a-col>
      <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="已完成" :value="summary.completedCount || 0" /></a-card></a-col>
      <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="已归档" :value="summary.archivedCount || 0" /></a-card></a-col>
      <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="复评逾期" :value="summary.reassessOverdueCount || 0" /></a-card></a-col>
      <a-col :xs="12" :lg="4"><a-card class="card-elevated" :bordered="false" size="small"><a-statistic title="高风险等级" :value="summary.highRiskCount || 0" /></a-card></a-col>
    </a-row>
    <a-alert
      v-if="(summary.reassessOverdueCount || 0) > 0 || (summary.highRiskCount || 0) > 0"
      type="warning"
      show-icon
      style="margin-top: 12px"
      :message="`风险提醒：复评逾期 ${summary.reassessOverdueCount || 0} 条，高风险等级 ${summary.highRiskCount || 0} 条。`"
    />

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <div class="table-actions">
        <a-space>
          <a-button v-if="pageConfig.showCreateButton" type="primary" @click="openForm()">
            {{ pageConfig.createButtonText }}
          </a-button>
          <a-button v-if="pageConfig.showBatchDelete" danger :disabled="selectedRowKeys.length === 0" @click="removeBatch">
            删除
          </a-button>
          <a-button v-if="pageConfig.showExportButton" @click="exportCsv">导出Excel</a-button>
        </a-space>
      </div>
      <a-table
        :data-source="rows"
        :columns="tableColumns"
        :loading="loading"
        :pagination="false"
        row-key="id"
        :row-selection="rowSelection"
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
              <a-button v-if="pageConfig.actions.includes('delete')" type="link" danger @click="remove(record.id)">删除</a-button>
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
      :ok-text="formReadonly ? '已查看' : '保存'"
      cancel-text="关闭"
      @ok="submit"
      @cancel="() => (open = false)"
    >
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="老人姓名" name="elderId">
              <a-select
                v-model:value="form.elderId"
                :disabled="formReadonly"
                allow-clear
                show-search
                :filter-option="false"
                placeholder="请选择老人"
                @search="searchElderOptions"
                @focus="() => loadElderOptions()"
                @change="onElderChange"
              >
                <a-select-option v-for="item in elderOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="评估等级">
              <a-input v-model:value="form.levelCode" placeholder="如 A/B/C 或 轻/中/重" :disabled="formReadonly" />
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
              <a-select v-model:value="form.templateId" allow-clear placeholder="可选，选择后可自动算分" :disabled="formReadonly">
                <a-select-option v-for="tpl in templates" :key="tpl.id" :value="tpl.id">
                  {{ tpl.templateName }} ({{ tpl.templateCode }})
                </a-select-option>
              </a-select>
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
                    v-model:value="admissionScores[item.id]"
                    placeholder="请选择评分"
                    :disabled="formReadonly"
                    style="width: 100%; margin-bottom: 8px;"
                  >
                    <a-select-option v-for="opt in item.options" :key="`${item.id}-${opt.score}`" :value="opt.score">
                      {{ opt.score }}分：{{ opt.label }}
                    </a-select-option>
                  </a-select>
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
              <a-space style="margin-top: 12px;">
                <a-button :disabled="formReadonly || !canDownloadAdmissionReport" @click="downloadAdmissionReport">
                  下载评估报告
                </a-button>
              </a-space>
            </a-card>
          </a-space>
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="评估分值">
              <a-input-number v-model:value="form.score" :min="0" :max="999" :step="0.5" style="width: 100%" :disabled="formReadonly" />
            </a-form-item>
          </a-col>
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
              <a-input v-model:value="form.archiveNo" :disabled="formReadonly" />
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
              <a-button size="small" :disabled="formReadonly" @click="previewScore">试算分值</a-button>
              <span class="hint">选择模板并填写明细后，可自动计算分值和等级。</span>
            </a-space>
          </div>
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import { useRoute } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import {
  getAssessmentRecordPage,
  getAssessmentRecordSummary,
  createAssessmentRecord,
  updateAssessmentRecord,
  deleteAssessmentRecord,
  batchDeleteAssessmentRecord,
  exportAssessmentRecord,
  getAssessmentTemplateList,
  previewAssessmentScore
} from '../../api/assessment'
import type { AssessmentRecord, AssessmentRecordSummary, AssessmentType, AssessmentScaleTemplate, PageResult } from '../../types'

const props = defineProps<{
  title: string
  subTitle: string
  assessmentType: AssessmentType
}>()
const route = useRoute()

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
    createButtonText: '新增',
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
    showExportButton: false,
    createButtonText: '新增',
    actions: ['view'],
    columns: [
      { title: '序号', key: 'index', width: 70, fixed: 'left' },
      { title: '老人姓名', key: 'elderName', dataIndex: 'elderName', width: 130 },
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
    showCreateButton: false,
    showBatchDelete: false,
    showExportButton: false,
    createButtonText: '新增',
    actions: [],
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
    showCreateButton: false,
    showBatchDelete: false,
    showExportButton: false,
    createButtonText: '新增',
    actions: [],
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

const admissionLevelText = computed(() => resolveAbilityLevel(admissionTotalScore.value))
const admissionMissingItems = computed(() =>
  admissionScoreItemList.filter((item) => admissionScores[item.id] === undefined && Math.max(...item.options.map((opt) => opt.score)) > 0)
)
const canDownloadAdmissionReport = computed(() => admissionMissingItems.value.length === 0)
const admissionResidentIdFromRoute = computed(() => Number(route.query.residentId || 0))
const admissionAutoOpenMode = computed(() => String(route.query.mode || 'new'))
const admissionShouldAutoOpen = computed(() => String(route.query.autoOpen || '') === '1')
const admissionRouteHandled = ref(false)

const query = reactive({
  keyword: '',
  contentKeyword: '',
  archiveType: undefined as AssessmentType | undefined,
  status: undefined as string | undefined,
  dateRange: undefined as string[] | undefined,
  pageNo: 1,
  pageSize: 10
})

const open = ref(false)
const formReadonly = ref(false)
const formRef = ref<FormInstance>()
const selectedRowKeys = ref<number[]>([])
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
  scoreAuto: 1
})
const submitting = ref(false)
const scoreAutoChecked = ref(true)

const rules: FormRules = {
  elderId: [{ required: true, message: '请选择老人' }],
  assessmentDate: [{ required: true, message: '请选择评估日期' }],
  status: [{ required: true, message: '请选择状态' }]
}

const tableColumns = computed(() => pageConfig.value.columns)
const rowSelection = computed(() => {
  if (!pageConfig.value.showBatchDelete) {
    return undefined
  }
  return {
    selectedRowKeys: selectedRowKeys.value,
    onChange: (keys: number[]) => {
      selectedRowKeys.value = keys
    }
  }
})

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

function assessmentTypeLabel(type?: AssessmentType | string) {
  if (!type) return '-'
  if (typeof type === 'string' && type.includes('评估')) return type
  const matched = assessmentTypeOptions.find((item) => item.value === type)
  return matched?.label || '-'
}

function displayCell(record: Record<string, any>, key?: string) {
  if (!key) return '-'
  const value = record[key]
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  return value
}

function orgDisplayName(record: AssessmentRecord & Record<string, any>) {
  if (record.orgName) return record.orgName
  if (record.orgId) return `机构${record.orgId}`
  return '-'
}

async function fetchData() {
  loading.value = true
  try {
    const assessmentTypeForQuery = props.assessmentType === 'ARCHIVE'
      ? query.archiveType
      : props.assessmentType
    const keyword = query.contentKeyword || query.keyword
    const params = {
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      assessmentType: assessmentTypeForQuery,
      status: pageConfig.value.showStatusFilter ? query.status : undefined,
      keyword,
      dateFrom: query.dateRange?.[0],
      dateTo: query.dateRange?.[1]
    }
    const [res, sum] = await Promise.all([
      getAssessmentRecordPage(params),
      getAssessmentRecordSummary(params)
    ])
    rows.value = res.list
    total.value = res.total
    Object.assign(summary, sum || {})
  } finally {
    loading.value = false
  }
}

async function fetchTemplates() {
  templates.value = await getAssessmentTemplateList({ assessmentType: props.assessmentType })
}

function onSearch() {
  query.pageNo = 1
  fetchData()
}

function reset() {
  query.keyword = ''
  query.contentKeyword = ''
  query.archiveType = undefined
  query.status = undefined
  query.dateRange = undefined
  query.pageNo = 1
  selectedRowKeys.value = []
  fetchData()
}

function onPageChange(page: number) {
  query.pageNo = page
  fetchData()
}

function onPageSizeChange(current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  fetchData()
}

async function searchElderOptions(keyword: string) {
  await loadElderOptions(keyword)
}

function onElderChange(elderId?: number) {
  form.elderName = findElderName(elderId)
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
        admissionScores[item.id] = Number(mapped[item.id])
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

function downloadAdmissionReport() {
  if (!canDownloadAdmissionReport.value) {
    message.warning(`还有 ${admissionMissingItems.value.length} 项未评分，无法下载`)
    return
  }
  const elderName = findElderName(form.elderId) || form.elderName || '未命名长者'
  const lines: string[] = []
  lines.push('老年人能力评估报告（入住评估）')
  lines.push(`老人姓名：${elderName}`)
  lines.push(`评估日期：${form.assessmentDate || '-'}`)
  lines.push(`评估人：${form.assessorName || '-'}`)
  lines.push(`总分（90分制）：${admissionTotalScore.value}`)
  lines.push(`能力等级：${admissionLevelText.value}`)
  lines.push('')
  admissionAbilityGroups.forEach((group) => {
    lines.push(group.title)
    group.items.forEach((item) => {
      const score = admissionScores[item.id]
      const selected = item.options.find((opt) => opt.score === score)
      lines.push(`${item.id}. ${item.title}：${score ?? '-'}分 ${selected ? `(${selected.label})` : ''}`)
      lines.push(`  指标说明：${item.description}`)
    })
    lines.push('')
  })
  lines.push('等级划分：0级(90)，1级(66-89)，2级(46-65)，3级(30-45)，4级(0-29)')
  const blob = new Blob([lines.join('\n')], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `入住评估报告_${elderName}_${form.assessmentDate || new Date().toISOString().slice(0, 10)}.txt`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  message.success('评估报告已下载')
}

function assignRecord(record: AssessmentRecord) {
  openForm(record)
  message.info('请在弹窗中调整评估人后保存，即可完成指派')
}

function openForm(record?: AssessmentRecord, readonly = false) {
  formReadonly.value = readonly
  if (record) {
    Object.assign(form, record)
    form.scoreAuto = record.scoreAuto === 0 ? 0 : 1
    ensureSelectedElder(record.elderId, record.elderName)
    scoreAutoChecked.value = record.scoreAuto !== 0
    if (isAdmissionAssessment.value) {
      loadAdmissionScoresFromDetail(record.detailJson)
      form.score = admissionTotalScore.value
      form.levelCode = resolveAbilityLevel(admissionTotalScore.value).split('：')[0]
    }
  } else {
    Object.assign(form, {
      id: undefined,
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
      scoreAuto: 1
    })
    scoreAutoChecked.value = true
    if (isAdmissionAssessment.value) {
      resetAdmissionScores()
      form.score = 0
      form.levelCode = '4级'
    }
  }
  open.value = true
}

function onScoreAutoChange(checked: boolean) {
  form.scoreAuto = checked ? 1 : 0
}

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
  await formRef.value?.validate()
  if (isAdmissionAssessment.value) {
    if (admissionMissingItems.value.length > 0) {
      message.warning(`请完成所有评分项，当前仍有 ${admissionMissingItems.value.length} 项未评分`)
      return
    }
    form.score = admissionTotalScore.value
    form.levelCode = resolveAbilityLevel(admissionTotalScore.value).split('：')[0]
    form.resultSummary = form.resultSummary || resolveAbilityLevel(admissionTotalScore.value)
    form.detailJson = buildAdmissionDetailJson()
    form.scoreAuto = 0
    form.templateId = undefined
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
      message.success('更新成功')
    } else {
      await createAssessmentRecord(payload)
      message.success('登记成功')
    }
    open.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

async function autoOpenAdmissionFromRoute() {
  if (!isAdmissionAssessment.value || admissionRouteHandled.value || !admissionShouldAutoOpen.value) {
    return
  }
  const residentId = admissionResidentIdFromRoute.value
  if (!residentId) {
    return
  }
  admissionRouteHandled.value = true

  const mode = admissionAutoOpenMode.value
  const res: PageResult<AssessmentRecord> = await getAssessmentRecordPage({
    pageNo: 1,
    pageSize: 20,
    assessmentType: 'ADMISSION',
    elderId: residentId
  })
  const list = res.list || []
  const draft = list.find((item) => item.status === 'DRAFT')
  const latest = list[0]

  if (mode === 'continue' && draft) {
    openForm(draft)
    return
  }

  if (mode === 'continue' && latest) {
    openForm(latest)
    return
  }

  openForm()
  form.elderId = residentId
  form.elderName = findElderName(residentId) || ''
  ensureSelectedElder(residentId, form.elderName || `长者${residentId}`)
  form.status = 'DRAFT'
}

function remove(id: number) {
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
  if (!selectedRowKeys.value.length) return
  Modal.confirm({
    title: `确认删除选中的 ${selectedRowKeys.value.length} 条评估记录？`,
    onOk: async () => {
      await batchDeleteAssessmentRecord(selectedRowKeys.value.map((item) => Number(item)))
      message.success('批量删除成功')
      selectedRowKeys.value = []
      fetchData()
    }
  })
}

async function exportCsv() {
  const assessmentTypeForQuery = props.assessmentType === 'ARCHIVE'
    ? query.archiveType
    : props.assessmentType
  const blob = await exportAssessmentRecord({
    assessmentType: assessmentTypeForQuery,
    status: pageConfig.value.showStatusFilter ? query.status : undefined,
    keyword: query.contentKeyword || query.keyword,
    dateFrom: query.dateRange?.[0],
    dateTo: query.dateRange?.[1]
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

onMounted(async () => {
  await Promise.all([fetchTemplates(), fetchData(), loadElderOptions()])
  await autoOpenAdmissionFromRoute()
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
}

.hint {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}
</style>
