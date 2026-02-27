<template>
  <PageContainer title="营销方案" sub-title="营销话术学习、机构政策沉淀与执行追踪">
    <template #extra>
      <a-space>
        <a-input-search
          v-model:value="query.keyword"
          allow-clear
          placeholder="搜索标题/摘要/责任部门"
          style="width: 280px"
          @search="fetchData"
        />
        <a-select v-model:value="query.status" allow-clear placeholder="状态筛选" style="width: 140px" @change="fetchData">
          <a-select-option value="ACTIVE">启用</a-select-option>
          <a-select-option value="INACTIVE">停用</a-select-option>
        </a-select>
        <a-button type="primary" @click="openCreate">新增方案</a-button>
      </a-space>
    </template>

    <a-tabs v-model:activeKey="activeModule" class="plan-tabs" @change="onModuleChange">
      <a-tab-pane key="SPEECH" tab="营销话术库">
        <a-row :gutter="[16, 16]">
          <a-col v-for="item in speechCards" :key="item.id" :xs="24" :md="12" :xl="8">
            <a-card class="card-elevated plan-card" :bordered="false" hoverable>
              <template #title>
                <div class="plan-card-title">
                  <span>{{ item.title }}</span>
                  <a-tag :color="item.status === 'ACTIVE' ? 'green' : 'default'">
                    {{ item.status === 'ACTIVE' ? '启用' : '停用' }}
                  </a-tag>
                </div>
              </template>
              <div class="plan-card-summary">{{ item.summary || '暂无摘要，建议补充核心话术场景。' }}</div>
              <div class="plan-card-meta">
                <span>优先级：{{ item.priority }}</span>
                <span>生效：{{ item.effectiveDate || '--' }}</span>
              </div>
              <div class="plan-card-actions">
                <a-button type="link" size="small" @click="openPreview(item)">查看内容</a-button>
                <a-button type="link" size="small" @click="openEdit(item)">编辑</a-button>
                <a-popconfirm title="确认删除该话术吗？" @confirm="onDelete(item.id)">
                  <a-button type="link" danger size="small">删除</a-button>
                </a-popconfirm>
              </div>
            </a-card>
          </a-col>
        </a-row>
        <a-empty v-if="!speechCards.length && !loading" description="暂无话术，请先新增" />
      </a-tab-pane>

      <a-tab-pane key="POLICY" tab="季度运营政策">
        <a-card class="card-elevated" :bordered="false">
          <a-table
            :loading="loading"
            :data-source="policyRows"
            :columns="policyColumns"
            :pagination="pagination"
            row-key="id"
            @change="onTableChange"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'status'">
                <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'default'">
                  {{ record.status === 'ACTIVE' ? '启用' : '停用' }}
                </a-tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space>
                  <a-button type="link" size="small" @click="openPreview(record)">查看</a-button>
                  <a-button type="link" size="small" @click="openEdit(record)">编辑</a-button>
                  <a-popconfirm title="确认删除该政策吗？" @confirm="onDelete(record.id)">
                    <a-button type="link" size="small" danger>删除</a-button>
                  </a-popconfirm>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-tab-pane>
    </a-tabs>

    <a-modal
      v-model:open="modalOpen"
      :title="editingId ? '编辑营销方案' : '新增营销方案'"
      :confirm-loading="submitLoading"
      width="700px"
      @ok="submitForm"
    >
      <a-form ref="formRef" :model="form" :rules="formRules" layout="vertical">
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="模块类型" name="moduleType">
              <a-select v-model:value="form.moduleType" :disabled="Boolean(editingId)">
                <a-select-option value="SPEECH">营销话术</a-select-option>
                <a-select-option value="POLICY">季度政策</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="状态" name="status">
              <a-select v-model:value="form.status">
                <a-select-option value="ACTIVE">启用</a-select-option>
                <a-select-option value="INACTIVE">停用</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="标题" name="title">
          <a-input v-model:value="form.title" :maxlength="128" show-count />
        </a-form-item>
        <a-form-item label="摘要" name="summary">
          <a-input v-model:value="form.summary" :maxlength="255" show-count />
        </a-form-item>
        <a-row :gutter="12" v-if="form.moduleType === 'POLICY'">
          <a-col :span="8">
            <a-form-item label="季度" name="quarterLabel">
              <a-input v-model:value="form.quarterLabel" placeholder="如 2026 Q1" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="责任部门" name="owner">
              <a-input v-model:value="form.owner" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="生效日期" name="effectiveDate">
              <a-date-picker v-model:value="form.effectiveDate" style="width: 100%" value-format="YYYY-MM-DD" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item v-if="form.moduleType === 'POLICY'" label="运营目标" name="target">
          <a-input v-model:value="form.target" />
        </a-form-item>
        <a-form-item label="优先级" name="priority">
          <a-input-number v-model:value="form.priority" :min="1" :max="999" style="width: 100%" />
        </a-form-item>
        <a-form-item label="内容详情" name="content">
          <a-textarea v-model:value="form.content" :rows="5" placeholder="可填写详细策略、执行要点和常见异议回答" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-drawer v-model:open="previewOpen" title="方案详情" width="520">
      <a-descriptions :column="1" bordered size="small" v-if="previewRecord">
        <a-descriptions-item label="类型">{{ moduleLabel(previewRecord.moduleType) }}</a-descriptions-item>
        <a-descriptions-item label="标题">{{ previewRecord.title }}</a-descriptions-item>
        <a-descriptions-item label="摘要">{{ previewRecord.summary || '--' }}</a-descriptions-item>
        <a-descriptions-item label="季度">{{ previewRecord.quarterLabel || '--' }}</a-descriptions-item>
        <a-descriptions-item label="目标">{{ previewRecord.target || '--' }}</a-descriptions-item>
        <a-descriptions-item label="部门">{{ previewRecord.owner || '--' }}</a-descriptions-item>
        <a-descriptions-item label="生效日">{{ previewRecord.effectiveDate || '--' }}</a-descriptions-item>
        <a-descriptions-item label="内容">
          <div class="preview-content">{{ previewRecord.content || '暂无详细内容' }}</div>
        </a-descriptions-item>
      </a-descriptions>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance, TablePaginationConfig } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import {
  createMarketingPlan,
  deleteMarketingPlan,
  getMarketingPlanPage,
  updateMarketingPlan
} from '../../api/marketing'
import type { MarketingPlanItem, MarketingPlanPayload, MarketingPlanQuery } from '../../types'

const activeModule = ref<'SPEECH' | 'POLICY'>('SPEECH')
const loading = ref(false)
const submitLoading = ref(false)
const modalOpen = ref(false)
const previewOpen = ref(false)
const previewRecord = ref<MarketingPlanItem>()
const editingId = ref<number>()
const formRef = ref<FormInstance>()

const query = reactive<MarketingPlanQuery>({
  pageNo: 1,
  pageSize: 10,
  moduleType: 'SPEECH'
})

const pagination = reactive<TablePaginationConfig>({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`
})

const rows = ref<MarketingPlanItem[]>([])

const policyColumns = [
  { title: '季度', dataIndex: 'quarterLabel', key: 'quarterLabel', width: 120 },
  { title: '标题', dataIndex: 'title', key: 'title', width: 200 },
  { title: '运营目标', dataIndex: 'target', key: 'target', width: 260 },
  { title: '责任部门', dataIndex: 'owner', key: 'owner', width: 140 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '操作', key: 'action', width: 160, fixed: 'right' }
]

const form = reactive({
  moduleType: 'SPEECH' as 'SPEECH' | 'POLICY',
  title: '',
  summary: '',
  content: '',
  quarterLabel: '',
  target: '',
  owner: '',
  priority: 50,
  status: 'ACTIVE' as 'ACTIVE' | 'INACTIVE',
  effectiveDate: undefined as string | undefined
})

const formRules = {
  moduleType: [{ required: true, message: '请选择模块类型' }],
  title: [{ required: true, message: '请输入标题' }],
  status: [{ required: true, message: '请选择状态' }],
  priority: [{ required: true, type: 'number', message: '请输入优先级' }]
}

const speechCards = computed(() => rows.value)
const policyRows = computed(() => rows.value)

async function fetchData() {
  loading.value = true
  try {
    const params: MarketingPlanQuery = {
      pageNo: pagination.current || 1,
      pageSize: pagination.pageSize || 10,
      moduleType: activeModule.value,
      status: query.status,
      keyword: query.keyword?.trim()
    }
    const page = await getMarketingPlanPage(params)
    rows.value = page.list || []
    pagination.total = page.total || 0
  } catch (error: any) {
    message.error(error?.message || '加载营销方案失败')
  } finally {
    loading.value = false
  }
}

function onModuleChange(tab: string) {
  activeModule.value = tab as 'SPEECH' | 'POLICY'
  pagination.current = 1
  fetchData()
}

function onTableChange(pag: TablePaginationConfig) {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 10
  fetchData()
}

function openCreate() {
  editingId.value = undefined
  form.moduleType = activeModule.value
  form.title = ''
  form.summary = ''
  form.content = ''
  form.quarterLabel = ''
  form.target = ''
  form.owner = ''
  form.priority = 50
  form.status = 'ACTIVE'
  form.effectiveDate = undefined
  modalOpen.value = true
}

function openEdit(item: MarketingPlanItem) {
  editingId.value = item.id
  form.moduleType = item.moduleType
  form.title = item.title || ''
  form.summary = item.summary || ''
  form.content = item.content || ''
  form.quarterLabel = item.quarterLabel || ''
  form.target = item.target || ''
  form.owner = item.owner || ''
  form.priority = item.priority || 50
  form.status = item.status || 'ACTIVE'
  form.effectiveDate = item.effectiveDate
  modalOpen.value = true
}

function openPreview(item: MarketingPlanItem) {
  previewRecord.value = item
  previewOpen.value = true
}

async function submitForm() {
  try {
    await formRef.value?.validate()
    const payload: MarketingPlanPayload = {
      moduleType: form.moduleType,
      title: form.title.trim(),
      summary: trimOrUndefined(form.summary),
      content: trimOrUndefined(form.content),
      quarterLabel: trimOrUndefined(form.quarterLabel),
      target: trimOrUndefined(form.target),
      owner: trimOrUndefined(form.owner),
      priority: form.priority,
      status: form.status,
      effectiveDate: form.effectiveDate
    }
    submitLoading.value = true
    if (editingId.value) {
      await updateMarketingPlan(editingId.value, payload)
      message.success('更新成功')
    } else {
      await createMarketingPlan(payload)
      message.success('新增成功')
    }
    modalOpen.value = false
    fetchData()
  } catch (error: any) {
    if (error?.errorFields) return
    message.error(error?.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}

async function onDelete(id: number) {
  try {
    await deleteMarketingPlan(id)
    message.success('删除成功')
    if (rows.value.length === 1 && (pagination.current || 1) > 1) {
      pagination.current = (pagination.current || 1) - 1
    }
    fetchData()
  } catch (error: any) {
    message.error(error?.message || '删除失败')
  }
}

function moduleLabel(moduleType: string) {
  return moduleType === 'POLICY' ? '季度政策' : '营销话术'
}

function trimOrUndefined(value?: string) {
  const trimmed = value?.trim()
  return trimmed ? trimmed : undefined
}

onMounted(fetchData)
</script>

<style scoped>
.plan-tabs {
  margin-top: 6px;
}

.plan-card {
  min-height: 210px;
}

.plan-card-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.plan-card-summary {
  min-height: 60px;
  color: var(--muted);
  line-height: 1.6;
}

.plan-card-meta {
  display: flex;
  justify-content: space-between;
  color: var(--muted);
  font-size: 12px;
  margin-top: 8px;
}

.plan-card-actions {
  margin-top: 6px;
  display: flex;
  justify-content: flex-end;
}

.preview-content {
  white-space: pre-wrap;
  line-height: 1.7;
}
</style>
