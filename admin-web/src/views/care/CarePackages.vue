<template>
  <PageContainer title="护理套餐" subTitle="套餐与护理等级配置">
    <a-card :bordered="false" class="card-elevated">
      <div class="table-actions">
        <a-space>
          <a-button type="primary" @click="openModal()">新增套餐</a-button>
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

    <a-modal v-model:open="modalOpen" title="护理套餐" :confirm-loading="submitting" @ok="submit">
      <a-form ref="formRef" layout="vertical" :model="form" :rules="rules">
        <a-form-item name="name" label="套餐名称" required>
          <a-input v-model:value="form.name" />
        </a-form-item>
        <a-form-item name="careLevel" label="护理等级">
          <a-input v-model:value="form.careLevel" placeholder="如 A/B/C" />
        </a-form-item>
        <a-form-item name="cycleDays" label="周期(天)">
          <a-input-number v-model:value="form.cycleDays" :min="1" style="width: 100%" />
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
import { getCarePackagePage, createCarePackage, updateCarePackage, deleteCarePackage } from '../../api/standard'
import type { CarePackage, PageResult } from '../../types'

const list = ref<CarePackage[]>([])
const loading = ref(false)
const modalOpen = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive<Partial<CarePackage>>({ enabled: 1, cycleDays: 1 })
const page = reactive({ pageNo: 1, pageSize: 10, total: 0 })

const rules = {
  name: [{ required: true, message: '请输入套餐名称' }]
}

const columns = [
  { title: '套餐名称', dataIndex: 'name', key: 'name' },
  { title: '护理等级', dataIndex: 'careLevel', key: 'careLevel', width: 140 },
  { title: '周期(天)', dataIndex: 'cycleDays', key: 'cycleDays', width: 120 },
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

function openModal(record?: CarePackage) {
  Object.assign(form, record || { name: '', careLevel: '', cycleDays: 1, enabled: 1, remark: '' })
  modalOpen.value = true
}

async function submit() {
  try {
    await formRef.value?.validate()
    submitting.value = true
    if (form.id) {
      await updateCarePackage(form.id, form)
    } else {
      await createCarePackage(form)
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
    title: '确认删除该套餐？',
    onOk: async () => {
      await deleteCarePackage(id)
      message.success('已删除')
      load()
    }
  })
}

async function load() {
  loading.value = true
  try {
    const res: PageResult<CarePackage> = await getCarePackagePage({
      pageNo: page.pageNo,
      pageSize: page.pageSize
    })
    list.value = res.list
    page.total = res.total
  } finally {
    loading.value = false
  }
}

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
