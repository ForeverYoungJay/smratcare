<template>
  <PageContainer title="CRM线索" subTitle="潜在客户管理">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="关键字">
          <a-input v-model:value="query.keyword" placeholder="姓名/手机号" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 140px">
            <a-select-option :value="0">新建</a-select-option>
            <a-select-option :value="1">跟进中</a-select-option>
            <a-select-option :value="2">已签约</a-select-option>
            <a-select-option :value="3">流失</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="来源渠道">
          <a-select v-model:value="query.source" allow-clear style="width: 160px">
            <a-select-option v-for="item in sourceOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="客户标签">
          <a-select v-model:value="query.customerTag" allow-clear style="width: 160px">
            <a-select-option v-for="item in customerTagOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">查询</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <div class="table-actions">
        <a-space>
          <a-button type="primary" @click="openForm()">新增线索</a-button>
        </a-space>
      </div>
      <a-table
        :data-source="rows"
        :columns="columns"
        :loading="loading"
        :pagination="false"
        row-key="id"
        :scroll="{ y: 520 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusTag(record.status)">{{ statusLabel(record.status) }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" @click="openForm(record)">编辑</a-button>
              <a-button type="link" danger @click="remove(record.id)">删除</a-button>
            </a-space>
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

    <a-modal v-model:open="open" title="线索信息" width="420px" :confirm-loading="submitting" @ok="submit" @cancel="() => (open = false)">
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-form-item label="姓名" name="name">
          <a-input v-model:value="form.name" />
        </a-form-item>
        <a-form-item label="电话" name="phone">
          <a-input v-model:value="form.phone" />
        </a-form-item>
        <a-form-item label="来源" name="source">
          <a-select v-model:value="form.source" allow-clear>
            <a-select-option v-for="item in sourceOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="客户标签" name="customerTag">
          <a-select v-model:value="form.customerTag" allow-clear>
            <a-select-option v-for="item in customerTagOptions" :key="item.value" :value="item.value">{{ item.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select v-model:value="form.status">
            <a-select-option :value="0">新建</a-select-option>
            <a-select-option :value="1">跟进中</a-select-option>
            <a-select-option :value="2">已签约</a-select-option>
            <a-select-option :value="3">流失</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="下次跟进" name="nextFollowDate">
          <a-date-picker v-model:value="form.nextFollowDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-input v-model:value="form.remark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getBaseConfigItemList } from '../../api/baseConfig'
import { getCrmLeadPage, createCrmLead, updateCrmLead, deleteCrmLead } from '../../api/crm'
import type { BaseConfigItem, CrmLeadItem, PageResult } from '../../types'

const loading = ref(false)
const rows = ref<CrmLeadItem[]>([])
const total = ref(0)
const sourceOptions = ref<{ label: string; value: string }[]>([])
const customerTagOptions = ref<{ label: string; value: string }[]>([])

const query = reactive({
  keyword: '',
  status: undefined as number | undefined,
  source: undefined as string | undefined,
  customerTag: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const open = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<Partial<CrmLeadItem>>({ status: 0 })
const submitting = ref(false)

const rules: FormRules = {
  name: [{ required: true, message: '请输入姓名' }]
}

const columns = [
  { title: '姓名', dataIndex: 'name', key: 'name', width: 140 },
  { title: '电话', dataIndex: 'phone', key: 'phone', width: 140 },
  { title: '来源', dataIndex: 'source', key: 'source', width: 140 },
  { title: '客户标签', dataIndex: 'customerTag', key: 'customerTag', width: 140 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '下次跟进', dataIndex: 'nextFollowDate', key: 'nextFollowDate', width: 140 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'action', width: 200 }
]

function statusLabel(status?: number) {
  if (status === 0) return '新建'
  if (status === 1) return '跟进中'
  if (status === 2) return '已签约'
  if (status === 3) return '流失'
  return '-'
}

function statusTag(status?: number) {
  if (status === 2) return 'green'
  if (status === 3) return 'red'
  if (status === 1) return 'orange'
  return 'default'
}

async function loadDictOptions() {
  const [sourceItems, tagItems] = await Promise.all([
    getBaseConfigItemList({ configGroup: 'MARKETING_SOURCE_CHANNEL', status: 1 }),
    getBaseConfigItemList({ configGroup: 'MARKETING_CUSTOMER_TAG', status: 1 })
  ])
  sourceOptions.value = (sourceItems || []).map((item: BaseConfigItem) => ({ label: item.itemName, value: item.itemName }))
  customerTagOptions.value = (tagItems || []).map((item: BaseConfigItem) => ({ label: item.itemName, value: item.itemName }))
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<CrmLeadItem> = await getCrmLeadPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword,
      status: query.status,
      source: query.source,
      customerTag: query.customerTag
    })
    rows.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function reset() {
  query.keyword = ''
  query.status = undefined
  query.source = undefined
  query.customerTag = undefined
  query.pageNo = 1
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

function openForm(row?: CrmLeadItem) {
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { id: undefined, name: '', phone: '', source: '', status: 0, nextFollowDate: '', remark: '' })
    form.customerTag = ''
  }
  open.value = true
}

async function submit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true
    if (form.id) {
      await updateCrmLead(form.id, form)
    } else {
      await createCrmLead(form)
    }
    message.success('保存成功')
    open.value = false
    await fetchData()
  } catch {
    message.error('保存失败')
  } finally {
    submitting.value = false
  }
}

async function remove(id: number) {
  Modal.confirm({
    title: '提示',
    content: '确认删除该线索吗？',
    async onOk() {
      await deleteCrmLead(id)
      message.success('已删除')
      await fetchData()
    }
  })
}

onMounted(async () => {
  await loadDictOptions()
  await fetchData()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}
.table-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}
</style>
