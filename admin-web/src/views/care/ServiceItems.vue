<template>
  <PageContainer title="服务项目库" subTitle="服务项目标准化维护">
    <a-card :bordered="false" class="card-elevated">
      <div class="table-actions">
        <a-space>
          <a-button type="primary" @click="openModal()">新增服务项目</a-button>
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

    <a-modal v-model:open="modalOpen" title="服务项目" :confirm-loading="submitting" @ok="submit">
      <a-form ref="formRef" layout="vertical" :model="form" :rules="rules">
        <a-form-item name="name" label="服务名称" required>
          <a-input v-model:value="form.name" />
        </a-form-item>
        <a-form-item name="category" label="分类">
          <a-input v-model:value="form.category" />
        </a-form-item>
        <a-form-item name="defaultDuration" label="默认时长(分钟)">
          <a-input-number v-model:value="form.defaultDuration" :min="1" style="width: 100%" />
        </a-form-item>
        <a-form-item name="defaultPoints" label="默认积分/费用">
          <a-input-number v-model:value="form.defaultPoints" :min="0" style="width: 100%" />
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
import { getServiceItemPage, createServiceItem, updateServiceItem, deleteServiceItem } from '../../api/standard'
import type { ServiceItem, PageResult } from '../../types'

const list = ref<ServiceItem[]>([])
const loading = ref(false)
const modalOpen = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive<Partial<ServiceItem>>({ enabled: 1 })
const page = reactive({ pageNo: 1, pageSize: 10, total: 0 })

const rules = {
  name: [{ required: true, message: '请输入服务名称' }]
}

const columns = [
  { title: '服务名称', dataIndex: 'name', key: 'name' },
  { title: '分类', dataIndex: 'category', key: 'category', width: 140 },
  { title: '默认时长', dataIndex: 'defaultDuration', key: 'defaultDuration', width: 120 },
  { title: '默认积分', dataIndex: 'defaultPoints', key: 'defaultPoints', width: 120 },
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

function openModal(record?: ServiceItem) {
  Object.assign(form, record || { name: '', category: '', defaultDuration: 10, defaultPoints: 0, enabled: 1, remark: '' })
  modalOpen.value = true
}

async function submit() {
  try {
    await formRef.value?.validate()
    submitting.value = true
    if (form.id) {
      await updateServiceItem(form.id, form)
    } else {
      await createServiceItem(form)
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
    title: '确认删除该服务项目？',
    onOk: async () => {
      await deleteServiceItem(id)
      message.success('已删除')
      load()
    }
  })
}

async function load() {
  loading.value = true
  try {
    const res: PageResult<ServiceItem> = await getServiceItemPage({
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
