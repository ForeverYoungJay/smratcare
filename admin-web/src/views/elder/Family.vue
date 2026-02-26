<template>
  <PageContainer title="家属管理" subTitle="家属档案与关联关系">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-form">
        <a-form-item label="姓名">
          <a-input v-model:value="query.realName" placeholder="姓名" allow-clear />
        </a-form-item>
        <a-form-item label="电话">
          <a-input v-model:value="query.phone" placeholder="手机号" allow-clear />
        </a-form-item>
        <a-form-item label="关联老人">
          <a-input v-model:value="query.elderName" placeholder="老人姓名" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 120px">
            <a-select-option :value="1">正常</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">搜索</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <div class="table-actions">
        <a-space>
          <a-button @click="fetchData">刷新</a-button>
        </a-space>
      </div>

      <a-table
        :data-source="rows"
        :columns="columns"
        :loading="loading"
        :pagination="false"
        row-key="id"
        @change="onTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'default'">{{ record.status === 1 ? '正常' : '停用' }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" @click="openBind(record)">绑定老人</a-button>
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

    <a-modal v-model:open="bindOpen" title="绑定老人" width="420px" @ok="submitBind" @cancel="() => (bindOpen = false)">
      <a-form ref="bindFormRef" :model="bindForm" :rules="bindRules" layout="vertical">
        <a-form-item label="老人" name="elderId">
          <a-select
            v-model:value="bindForm.elderId"
            allow-clear
            show-search
            :filter-option="false"
            placeholder="选择老人"
            @search="searchElders"
          >
            <a-select-option v-for="item in elderOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="关系" name="relation">
          <a-input v-model:value="bindForm.relation" placeholder="如 子女/配偶" />
        </a-form-item>
        <a-form-item label="主联系人">
          <a-switch v-model:checked="bindForm.isPrimary" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import { getFamilyUserPage } from '../../api/family'
import { bindFamily } from '../../api/elder'
import type { FamilyBindRequest, FamilyUserItem, PageResult } from '../../types/api'

const loading = ref(false)
const rows = ref<FamilyUserItem[]>([])
const total = ref(0)

const query = reactive({
  realName: '',
  phone: '',
  elderName: '',
  status: undefined as number | undefined,
  sortBy: undefined as string | undefined,
  sortOrder: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const columns = [
  { title: '姓名', dataIndex: 'realName', key: 'realName', width: 160, sorter: true },
  { title: '电话', dataIndex: 'phone', key: 'phone', width: 160, sorter: true },
  { title: '身份证', dataIndex: 'idCardNo', key: 'idCardNo', width: 180 },
  { title: '关联人数', dataIndex: 'elderCount', key: 'elderCount', width: 120, sorter: true },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100, sorter: true },
  { title: '操作', key: 'action', width: 180, fixed: 'right' as const }
]

const bindOpen = ref(false)
const bindFormRef = ref<FormInstance>()
const bindForm = reactive<FamilyBindRequest>({ familyUserId: 0, elderId: 0, relation: '', isPrimary: false })
const bindRules: FormRules = {
  elderId: [{ required: true, message: '请选择老人' }],
  relation: [{ required: true, message: '请输入关系' }]
}
const { elderOptions, searchElders: searchElderOptions } = useElderOptions({ pageSize: 200 })

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<FamilyUserItem> = await getFamilyUserPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      realName: query.realName,
      phone: query.phone,
      elderName: query.elderName,
      status: query.status,
      sortBy: query.sortBy,
      sortOrder: query.sortOrder
    })
    rows.value = res.list
    total.value = res.total
  } catch {
    rows.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function searchElders(val: string) {
  await searchElderOptions(val)
}

function reset() {
  query.realName = ''
  query.phone = ''
  query.elderName = ''
  query.status = undefined
  query.sortBy = undefined
  query.sortOrder = undefined
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

function onTableChange(_: any, __: any, sorter: any) {
  if (Array.isArray(sorter)) sorter = sorter[0]
  query.sortBy = sorter?.field || undefined
  query.sortOrder = sorter?.order === 'ascend' ? 'asc' : sorter?.order === 'descend' ? 'desc' : undefined
  fetchData()
}

function openBind(row: FamilyUserItem) {
  bindForm.familyUserId = row.id
  bindForm.elderId = 0
  bindForm.relation = ''
  bindForm.isPrimary = false
  bindOpen.value = true
}

async function submitBind() {
  if (!bindFormRef.value) return
  try {
    await bindFormRef.value.validate()
    await bindFamily(bindForm)
    message.success('绑定成功')
    bindOpen.value = false
  } catch {
    message.error('绑定失败')
  }
}

onMounted(async () => {
  await searchElderOptions('')
  await fetchData()
})
</script>

<style scoped>
.search-form {
  row-gap: 12px;
}
.table-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}
</style>
