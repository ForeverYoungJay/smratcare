<template>
  <PageContainer title="套餐明细" subTitle="维护套餐内的服务项">
    <a-card :bordered="false" class="card-elevated">
      <div class="table-actions">
        <a-space>
          <a-select
            v-model:value="query.packageId"
            allow-clear
            placeholder="选择套餐"
            style="width: 220px"
            @change="load"
          >
            <a-select-option v-for="pkg in packageOptions" :key="pkg.id" :value="pkg.id">
              {{ pkg.name }}
            </a-select-option>
          </a-select>
          <a-button type="primary" @click="openModal()">新增明细</a-button>
        </a-space>
      </div>
      <a-table
        row-key="id"
        :columns="columns"
        :data-source="list"
        :loading="loading"
        :pagination="pagination"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'enabled'">
            <a-tag :color="record.enabled === 1 ? 'green' : 'default'">
              {{ record.enabled === 1 ? '启用' : '停用' }}
            </a-tag>
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

    <a-modal v-model:open="modalOpen" title="套餐明细" :confirm-loading="submitting" @ok="submit">
      <a-form ref="formRef" layout="vertical" :model="form" :rules="rules">
        <a-form-item name="packageId" label="所属套餐" required>
          <a-select v-model:value="form.packageId" placeholder="请选择套餐">
            <a-select-option v-for="pkg in packageOptions" :key="pkg.id" :value="pkg.id">
              {{ pkg.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item name="itemId" label="服务项目" required>
          <a-select v-model:value="form.itemId" placeholder="请选择服务项">
            <a-select-option v-for="item in itemOptions" :key="item.id" :value="item.id">
              {{ item.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item name="frequencyPerDay" label="每日频次">
          <a-input-number v-model:value="form.frequencyPerDay" :min="1" style="width: 100%" />
        </a-form-item>
        <a-form-item name="sortNo" label="排序">
          <a-input-number v-model:value="form.sortNo" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item name="enabled" label="启用状态">
          <a-switch v-model:checked="form.enabled" :checked-value="1" :un-checked-value="0" />
        </a-form-item>
        <a-form-item name="remark" label="备注">
          <a-input v-model:value="form.remark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import {
  getPackageItemPage,
  createPackageItem,
  updatePackageItem,
  deletePackageItem,
  listCarePackages,
  listServiceItems
} from '../../api/standard'
import type { CarePackage, CarePackageItem, ServiceItem, PageResult } from '../../types'

const list = ref<CarePackageItem[]>([])
const loading = ref(false)
const modalOpen = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive<Partial<CarePackageItem>>({ enabled: 1, frequencyPerDay: 1, sortNo: 0 })
const page = reactive({ pageNo: 1, pageSize: 10, total: 0 })
const query = reactive({ packageId: undefined as number | undefined })

const packageOptions = ref<CarePackage[]>([])
const itemOptions = ref<ServiceItem[]>([])

const rules = {
  packageId: [{ required: true, message: '请选择套餐' }],
  itemId: [{ required: true, message: '请选择服务项' }]
}

const columns = [
  { title: '服务项目', dataIndex: 'itemName', key: 'itemName' },
  { title: '频次/天', dataIndex: 'frequencyPerDay', key: 'frequencyPerDay', width: 120 },
  { title: '排序', dataIndex: 'sortNo', key: 'sortNo', width: 100 },
  { title: '状态', key: 'enabled', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'actions', width: 160 }
]

const pagination = {
  current: page.pageNo,
  pageSize: page.pageSize,
  total: page.total,
  showSizeChanger: true,
  onChange: (current: number, size: number) => {
    page.pageNo = current
    page.pageSize = size
    load()
  }
}

function openModal(record?: CarePackageItem) {
  Object.assign(form, record || { packageId: query.packageId, itemId: undefined, frequencyPerDay: 1, enabled: 1, sortNo: 0, remark: '' })
  modalOpen.value = true
}

async function submit() {
  try {
    await formRef.value?.validate()
    submitting.value = true
    if (form.id) {
      await updatePackageItem(form.id, form)
    } else {
      await createPackageItem(form)
    }
    message.success('保存成功')
    modalOpen.value = false
    load()
  } catch {
    message.error('保存失败')
  } finally {
    submitting.value = false
  }
}

async function remove(id: number) {
  Modal.confirm({
    title: '确认删除该明细？',
    onOk: async () => {
      await deletePackageItem(id)
      message.success('已删除')
      load()
    }
  })
}

async function loadOptions() {
  packageOptions.value = await listCarePackages()
  itemOptions.value = await listServiceItems()
}

async function load() {
  loading.value = true
  try {
    const res: PageResult<CarePackageItem> = await getPackageItemPage({
      pageNo: page.pageNo,
      pageSize: page.pageSize,
      packageId: query.packageId
    })
    list.value = res.list
    page.total = res.total
  } finally {
    loading.value = false
  }
}

loadOptions()
load()
</script>

<style scoped>
.table-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}
.danger-text {
  color: #ef4444;
}
</style>
