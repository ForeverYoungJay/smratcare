<template>
  <PageContainer :title="pageTitle" subTitle="统一维护老人、营销、入住、活动、社区及费用等基础字典">
    <a-card class="card-elevated" :bordered="false">
      <a-tabs v-if="showTabs" v-model:activeKey="activeGroup" @change="onGroupChange">
        <a-tab-pane v-for="item in displayGroups" :key="item.code" :tab="item.label" />
      </a-tabs>

      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" placeholder="名称/编码" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 140px">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button type="primary" ghost @click="openCreate">新增配置项</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <a-table :data-source="rows" :columns="columns" :loading="loading" :pagination="false" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 1 ? 'green' : 'default'">{{ record.status === 1 ? '启用' : '停用' }}</a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" @click="openEdit(record)">编辑</a-button>
              <a-button type="link" danger @click="remove(record)">删除</a-button>
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

    <a-modal
      v-model:open="editorOpen"
      :title="editingId ? '编辑基础数据' : '新增基础数据'"
      @ok="submit"
      :confirm-loading="submitting"
      width="520px"
    >
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-form-item label="所属分组" name="configGroup">
          <a-select v-model:value="form.configGroup" :disabled="!!editingId">
            <a-select-option v-for="item in groups" :key="item.code" :value="item.code">{{ item.label }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="配置名称" name="itemName">
          <a-input v-model:value="form.itemName" placeholder="请输入名称" />
        </a-form-item>
        <a-form-item label="配置编码" name="itemCode">
          <a-input v-model:value="form.itemCode" placeholder="如 CHANNEL_WECHAT" />
        </a-form-item>
        <a-form-item label="排序">
          <a-input-number v-model:value="form.sortNo" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="form.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">停用</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="form.remark" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message, Modal } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import {
  createBaseConfigItem,
  deleteBaseConfigItem,
  getBaseConfigGroups,
  getBaseConfigItemPage,
  updateBaseConfigItem
} from '../../api/baseConfig'
import type { BaseConfigGroupOption, BaseConfigItem, BaseConfigItemPayload, PageResult } from '../../types'

const props = withDefaults(defineProps<{
  title?: string
  groupCode?: string
}>(), {
  title: '基础数据配置',
  groupCode: ''
})

const loading = ref(false)
const submitting = ref(false)
const editorOpen = ref(false)
const formRef = ref<FormInstance>()
const editingId = ref<number>()

const groups = ref<BaseConfigGroupOption[]>([])
const activeGroup = ref('')
const rows = ref<BaseConfigItem[]>([])
const total = ref(0)

const query = reactive({
  keyword: '',
  status: undefined as number | undefined,
  pageNo: 1,
  pageSize: 10
})

const form = reactive<BaseConfigItemPayload>({
  configGroup: '',
  itemCode: '',
  itemName: '',
  status: 1,
  sortNo: 0,
  remark: ''
})

const rules: FormRules = {
  configGroup: [{ required: true, message: '请选择分组' }],
  itemName: [{ required: true, message: '请输入配置名称' }],
  itemCode: [
    { required: true, message: '请输入配置编码' },
    { pattern: /^[A-Za-z0-9_]+$/, message: '仅支持字母/数字/下划线' }
  ],
  status: [{ required: true, message: '请选择状态' }]
}

const columns = computed(() => [
  { title: '分组', dataIndex: 'configGroup', key: 'configGroup', width: 180 },
  { title: '配置名称', dataIndex: 'itemName', key: 'itemName', width: 180 },
  { title: '配置编码', dataIndex: 'itemCode', key: 'itemCode', width: 180 },
  { title: '排序', dataIndex: 'sortNo', key: 'sortNo', width: 100 },
  { title: '状态', key: 'status', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '操作', key: 'action', width: 140 }
])

const pageTitle = computed(() => props.title || '基础数据配置')

const showTabs = computed(() => !props.groupCode)

const displayGroups = computed(() => {
  if (!props.groupCode) {
    return groups.value
  }
  return groups.value.filter((item) => item.code === props.groupCode)
})

async function loadGroups() {
  groups.value = await getBaseConfigGroups()
  if (props.groupCode) {
    activeGroup.value = props.groupCode
    return
  }
  if (!activeGroup.value && groups.value.length > 0) {
    activeGroup.value = groups.value[0].code
  }
}

async function fetchData() {
  if (!activeGroup.value) {
    rows.value = []
    total.value = 0
    return
  }
  loading.value = true
  try {
    const res: PageResult<BaseConfigItem> = await getBaseConfigItemPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      configGroup: activeGroup.value,
      keyword: query.keyword || undefined,
      status: query.status
    })
    rows.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function onGroupChange() {
  query.pageNo = 1
  fetchData()
}

function reset() {
  query.keyword = ''
  query.status = undefined
  query.pageNo = 1
  fetchData()
}

function openCreate() {
  editingId.value = undefined
  form.configGroup = activeGroup.value
  form.itemName = ''
  form.itemCode = ''
  form.status = 1
  form.sortNo = 0
  form.remark = ''
  editorOpen.value = true
}

function openEdit(row: BaseConfigItem) {
  editingId.value = row.id
  form.configGroup = row.configGroup
  form.itemName = row.itemName
  form.itemCode = row.itemCode
  form.status = row.status
  form.sortNo = row.sortNo
  form.remark = row.remark || ''
  editorOpen.value = true
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editingId.value) {
      await updateBaseConfigItem(editingId.value, form)
      message.success('更新成功')
    } else {
      await createBaseConfigItem(form)
      message.success('创建成功')
    }
    editorOpen.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

function remove(row: BaseConfigItem) {
  Modal.confirm({
    title: `确认删除【${row.itemName}】吗？`,
    onOk: async () => {
      await deleteBaseConfigItem(row.id)
      message.success('删除成功')
      fetchData()
    }
  })
}

function onPageChange(page: number) {
  query.pageNo = page
  fetchData()
}

function onPageSizeChange(_current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  fetchData()
}

onMounted(async () => {
  await loadGroups()
  await fetchData()
})

watch(
  () => props.groupCode,
  (value) => {
    if (value) {
      activeGroup.value = value
      query.pageNo = 1
      fetchData()
    }
  }
)
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}
</style>
