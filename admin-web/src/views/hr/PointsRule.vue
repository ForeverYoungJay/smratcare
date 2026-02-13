<template>
  <PageContainer title="积分规则" subTitle="按护理模板/评分/异常配置">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <template #extra>
        <a-button type="primary" @click="openDrawer()">新增规则</a-button>
      </template>
    </SearchForm>

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 1 ? 'green' : 'default'">
            {{ record.status === 1 ? '启用' : '停用' }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'templateName'">
          <span>{{ record.templateName || '默认规则' }}</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a @click="openDrawer(record)">编辑</a>
            <a @click="remove(record)">删除</a>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-drawer v-model:open="drawerOpen" :title="drawerTitle" width="520">
      <a-form :model="form" layout="vertical">
        <a-form-item label="护理模板">
          <a-select
            v-model:value="form.templateId"
            allow-clear
            :options="templateOptions"
            placeholder="留空表示默认规则"
          />
        </a-form-item>
        <a-form-item label="基础积分">
          <a-input-number v-model:value="form.basePoints" style="width: 100%" />
        </a-form-item>
        <a-form-item label="评分权重">
          <a-input-number v-model:value="form.scoreWeight" style="width: 100%" :step="0.1" />
        </a-form-item>
        <a-form-item label="可疑扣减">
          <a-input-number v-model:value="form.suspiciousPenalty" style="width: 100%" />
        </a-form-item>
        <a-form-item label="失败积分">
          <a-input-number v-model:value="form.failPoints" style="width: 100%" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="3" />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-space>
          <a-button @click="drawerOpen = false">取消</a-button>
          <a-button type="primary" :loading="saving" @click="submit">保存</a-button>
        </a-space>
      </template>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getPointsRulePage, upsertPointsRule, deletePointsRule } from '../../api/hr'
import { getTemplatePage } from '../../api/care'
import type { StaffPointsRule, CareTaskTemplate, PageResult } from '../../types'

const query = reactive({ pageNo: 1, pageSize: 10 })
const rows = ref<StaffPointsRule[]>([])
const loading = ref(false)
const saving = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '模板', dataIndex: 'templateName', key: 'templateName', width: 180 },
  { title: '基础积分', dataIndex: 'basePoints', key: 'basePoints', width: 120 },
  { title: '评分权重', dataIndex: 'scoreWeight', key: 'scoreWeight', width: 120 },
  { title: '可疑扣减', dataIndex: 'suspiciousPenalty', key: 'suspiciousPenalty', width: 120 },
  { title: '失败积分', dataIndex: 'failPoints', key: 'failPoints', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 140 }
]

const drawerOpen = ref(false)
const form = reactive<Partial<StaffPointsRule>>({ status: 1 })
const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]
const templates = ref<CareTaskTemplate[]>([])
const templateOptions = computed(() =>
  templates.value.map((t) => ({ label: t.taskName, value: t.id }))
)

const drawerTitle = computed(() => (form.id ? '编辑规则' : '新增规则'))

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<StaffPointsRule> = await getPointsRulePage(query)
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } catch {
    rows.value = []
  } finally {
    loading.value = false
  }
}

async function loadTemplates() {
  const res: PageResult<CareTaskTemplate> = await getTemplatePage({ pageNo: 1, pageSize: 200 })
  templates.value = res.list
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function onReset() {
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openDrawer(record?: StaffPointsRule) {
  Object.keys(form).forEach((k) => ((form as any)[k] = undefined))
  Object.assign(form, record || { status: 1 })
  drawerOpen.value = true
}

async function submit() {
  saving.value = true
  try {
    await upsertPointsRule(form)
    message.success('保存成功')
    drawerOpen.value = false
    fetchData()
  } catch {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function remove(record: StaffPointsRule) {
  if (!record.id) return
  try {
    await deletePointsRule(record.id)
    message.success('删除成功')
    fetchData()
  } catch {
    message.error('删除失败')
  }
}

loadTemplates()
fetchData()
</script>
