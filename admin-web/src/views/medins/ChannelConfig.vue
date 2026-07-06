<template>
  <PageContainer title="医保渠道配置" subTitle="各地医保平台差异化接入（密钥以引用方式管理，不落明文）">
    <a-alert
      type="info"
      show-icon
      message="渠道配置为医保适配层载体：内置 MOCK 渠道供联调验收；正式环境按地区新增渠道并实现对应适配器，密钥仅存密钥库键名引用。"
      style="margin-bottom: 16px"
    />
    <div style="margin-bottom: 12px; text-align: right">
      <a-button type="primary" @click="openEdit()">新增渠道</a-button>
    </div>
    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="false">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'channel'">
          <a-tag :color="record.channel === 'MOCK' ? 'cyan' : 'purple'">{{ channelText(record.channel) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'scope'">
          <a-tag :color="record.orgId ? 'blue' : 'default'">{{ record.orgId ? '本机构' : '全局默认' }}</a-tag>
        </template>
        <template v-else-if="column.key === 'enabled'">
          <a-tag :color="record.enabled ? 'green' : 'default'">{{ record.enabled ? '启用' : '停用' }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <div class="row-action-links">
            <a-button type="link" size="small" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" size="small" @click="doToggle(record)">{{ record.enabled ? '停用' : '启用' }}</a-button>
          </div>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="editOpen" :title="editForm.id ? '编辑渠道' : '新增渠道'" :confirm-loading="saving" @ok="submitEdit">
      <a-form layout="vertical">
        <a-form-item label="渠道编码" required>
          <a-input v-model:value="editForm.channel" placeholder="如 MOCK 或各地医保平台编码" :disabled="!!editForm.id" />
        </a-form-item>
        <a-form-item label="地区编码">
          <a-input v-model:value="editForm.regionCode" placeholder="行政区划编码，如 310000" />
        </a-form-item>
        <a-form-item label="接口地址">
          <a-input v-model:value="editForm.endpoint" placeholder="医保平台 endpoint" />
        </a-form-item>
        <a-form-item label="AppID">
          <a-input v-model:value="editForm.appId" />
        </a-form-item>
        <a-form-item label="密钥引用">
          <a-input v-model:value="editForm.secretRef" placeholder="密钥库键名，如 ref:medins.xxx.secret（禁止填明文密钥）" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="editForm.remark" :rows="2" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import DataTable from '../../components/DataTable.vue'
import { getMedinsChannels, saveMedinsChannel, toggleMedinsChannel } from '../../api/medins'
import type { MedinsChannelConfig } from '../../api/medins'
import type { Id } from '../../types'

const loading = ref(false)
const saving = ref(false)
const rows = ref<MedinsChannelConfig[]>([])

const columns = [
  { title: '渠道', key: 'channel', width: 120 },
  { title: '范围', key: 'scope', width: 100 },
  { title: '地区编码', dataIndex: 'regionCode', key: 'regionCode', width: 110 },
  { title: '接口地址', dataIndex: 'endpoint', key: 'endpoint', ellipsis: true },
  { title: 'AppID', dataIndex: 'appId', key: 'appId', width: 150 },
  { title: '密钥引用', dataIndex: 'secretRef', key: 'secretRef', width: 170, ellipsis: true },
  { title: '状态', key: 'enabled', width: 80 },
  { title: '操作', key: 'action', width: 130 }
]

const editOpen = ref(false)
const editForm = reactive({
  id: undefined as Id | undefined,
  channel: '',
  regionCode: '',
  endpoint: '',
  appId: '',
  secretRef: '',
  remark: ''
})

function channelText(c?: string) {
  return c === 'MOCK' ? 'MOCK 联调' : c || '-'
}

function openEdit(record?: MedinsChannelConfig) {
  editForm.id = record?.id
  editForm.channel = record?.channel || ''
  editForm.regionCode = record?.regionCode || ''
  editForm.endpoint = record?.endpoint || ''
  editForm.appId = record?.appId || ''
  editForm.secretRef = record?.secretRef || ''
  editForm.remark = record?.remark || ''
  editOpen.value = true
}

async function submitEdit() {
  if (!editForm.channel) {
    message.error('请填写渠道编码')
    return
  }
  saving.value = true
  try {
    await saveMedinsChannel({
      id: editForm.id,
      channel: editForm.channel,
      regionCode: editForm.regionCode || undefined,
      endpoint: editForm.endpoint || undefined,
      appId: editForm.appId || undefined,
      secretRef: editForm.secretRef || undefined,
      remark: editForm.remark || undefined
    })
    message.success('渠道配置已保存')
    editOpen.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

async function doToggle(record: MedinsChannelConfig) {
  await toggleMedinsChannel(record.id, !record.enabled)
  message.success(record.enabled ? '已停用' : '已启用')
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    rows.value = (await getMedinsChannels()) || []
  } finally {
    loading.value = false
  }
}

loadData()
</script>
