<template>
  <PageContainer title="护工小组" subTitle="护工编组与班组负责人管理">
    <a-card :bordered="false" class="card-elevated">
      <a-form layout="inline" @submit.prevent>
        <a-form-item label="关键字">
          <a-input v-model:value="query.keyword" placeholder="小组名称" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.enabled" :options="enabledOptions" allow-clear style="width: 140px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="search">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button type="primary" @click="openModal()">新增小组</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-table
        row-key="id"
        :columns="columns"
        :data-source="rows"
        :loading="loading"
        :pagination="pagination"
        @change="onTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'leaderStaffName'">
            {{ record.leaderStaffName || '-' }}
          </template>
          <template v-else-if="column.key === 'memberCount'">
            {{ record.memberStaffIds?.length || 0 }}
          </template>
          <template v-else-if="column.key === 'enabled'">
            <a-tag :color="record.enabled === 1 ? 'green' : 'default'">{{ record.enabled === 1 ? '启用' : '停用' }}</a-tag>
          </template>
          <template v-else-if="column.key === 'actions'">
            <a-space>
              <a @click="openModal(record)">编辑</a>
              <a class="danger-text" @click="remove(record.id)">删除</a>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="modalOpen" title="护工小组" :confirm-loading="submitting" @ok="submit">
      <a-form ref="formRef" layout="vertical" :model="form" :rules="rules">
        <a-form-item label="小组名称" name="name" required>
          <a-input v-model:value="form.name" />
        </a-form-item>
        <a-form-item label="组长" name="leaderStaffId">
          <a-select
            v-model:value="form.leaderStaffId"
            show-search
            allow-clear
            :filter-option="false"
            placeholder="输入姓名搜索"
            :options="staffOptions"
            @search="searchStaff"
          />
        </a-form-item>
        <a-form-item label="成员" name="memberStaffIds">
          <a-select
            v-model:value="form.memberStaffIds"
            mode="multiple"
            show-search
            :filter-option="false"
            placeholder="选择小组成员"
            :options="staffOptions"
            @search="searchStaff"
          />
        </a-form-item>
        <a-form-item label="启用状态" name="enabled">
          <a-switch v-model:checked="form.enabled" :checked-value="1" :un-checked-value="0" />
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-textarea v-model:value="form.remark" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getStaffPage } from '../../api/rbac'
import { createCaregiverGroup, deleteCaregiverGroup, getCaregiverGroupPage, updateCaregiverGroup } from '../../api/nursing'
import type { CaregiverGroupItem, PageResult, StaffItem } from '../../types'

const rows = ref<CaregiverGroupItem[]>([])
const loading = ref(false)
const modalOpen = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const staffOptions = ref<Array<{ label: string; value: number }>>([])

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0,
  keyword: undefined as string | undefined,
  enabled: undefined as number | undefined
})

const form = reactive<Partial<CaregiverGroupItem>>({
  name: '',
  leaderStaffId: undefined,
  memberStaffIds: [],
  enabled: 1,
  remark: ''
})

const enabledOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
]

const rules = {
  name: [{ required: true, message: '请输入小组名称', trigger: 'blur' }]
}

const columns = [
  { title: '小组名称', dataIndex: 'name', key: 'name' },
  { title: '组长', key: 'leaderStaffName', width: 140 },
  { title: '成员数', key: 'memberCount', width: 100 },
  { title: '状态', key: 'enabled', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'actions', width: 140 }
]

const pagination = computed(() => ({
  current: query.pageNo,
  pageSize: query.pageSize,
  total: query.total,
  showSizeChanger: true
}))

function resetForm() {
  form.id = undefined
  form.name = ''
  form.leaderStaffId = undefined
  form.memberStaffIds = []
  form.enabled = 1
  form.remark = ''
}

function openModal(record?: CaregiverGroupItem) {
  resetForm()
  if (record) {
    Object.assign(form, {
      id: record.id,
      name: record.name,
      leaderStaffId: record.leaderStaffId,
      memberStaffIds: record.memberStaffIds || [],
      enabled: record.enabled ?? 1,
      remark: record.remark || ''
    })
  }
  modalOpen.value = true
}

async function loadStaffOptions(keyword?: string) {
  const res: PageResult<StaffItem> = await getStaffPage({ pageNo: 1, pageSize: 50, keyword })
  staffOptions.value = res.list.map((item) => ({ label: item.realName || item.username, value: item.id }))
}

async function searchStaff(keyword: string) {
  await loadStaffOptions(keyword)
}

async function submit() {
  try {
    await formRef.value?.validate()
    submitting.value = true
    const payload: Partial<CaregiverGroupItem> = {
      name: form.name,
      leaderStaffId: form.leaderStaffId,
      memberStaffIds: form.memberStaffIds || [],
      enabled: form.enabled ?? 1,
      remark: form.remark
    }
    if (form.id) {
      await updateCaregiverGroup(form.id, payload)
    } else {
      await createCaregiverGroup(payload)
    }
    message.success('保存成功')
    modalOpen.value = false
    await load()
  } finally {
    submitting.value = false
  }
}

async function remove(id: number) {
  Modal.confirm({
    title: '确认删除该护工小组？',
    onOk: async () => {
      await deleteCaregiverGroup(id)
      message.success('删除成功')
      await load()
    }
  })
}

function onTableChange(pag: { current?: number; pageSize?: number }) {
  query.pageNo = pag.current || 1
  query.pageSize = pag.pageSize || 10
  load()
}

function search() {
  query.pageNo = 1
  load()
}

function reset() {
  query.keyword = undefined
  query.enabled = undefined
  query.pageNo = 1
  load()
}

async function load() {
  loading.value = true
  try {
    const res: PageResult<CaregiverGroupItem> = await getCaregiverGroupPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword,
      enabled: query.enabled
    })
    rows.value = res.list
    query.total = res.total
  } finally {
    loading.value = false
  }
}

load()
loadStaffOptions()
</script>

<style scoped>
.danger-text {
  color: #ef4444;
}
</style>
