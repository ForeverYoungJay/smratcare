<template>
  <PageContainer title="上报渠道配置" subTitle="民政/医保对接渠道（密钥以引用方式管理，不落明文）">
    <a-alert
      type="info"
      show-icon
      message="渠道配置为对接适配层载体：字段映射改配置不改代码；正式环境须替换 endpoint 与密钥引用，并在网关做加签与回调白名单。"
      style="margin-bottom: 16px"
    />
    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="false">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'channel'">
          <a-tag :color="record.channel === 'MZ_JINMIN' ? 'geekblue' : 'purple'">{{ channelText(record.channel) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'enabled'">
          <a-tag :color="record.enabled ? 'green' : 'default'">{{ record.enabled ? '启用' : '停用' }}</a-tag>
        </template>
        <template v-else-if="column.key === 'fieldMapping'">
          <a-button type="link" size="small" @click="viewMapping(record)">查看映射</a-button>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="mappingOpen" title="字段映射（本地字段 → 上报字段）" :footer="null" width="560px">
      <a-descriptions :column="1" size="small" bordered v-if="mappingList.length">
        <a-descriptions-item v-for="m in mappingList" :key="m.local" :label="m.local">{{ m.report }}</a-descriptions-item>
      </a-descriptions>
      <a-empty v-else description="未配置字段映射" />
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import DataTable from '../../components/DataTable.vue'
import { getGovChannels } from '../../api/govreport'
import type { GovChannelConfig } from '../../types'

const loading = ref(false)
const rows = ref<GovChannelConfig[]>([])

const columns = [
  { title: '渠道', key: 'channel', width: 120 },
  { title: '城市编码', dataIndex: 'cityCode', key: 'cityCode', width: 120 },
  { title: '接口地址', dataIndex: 'endpoint', key: 'endpoint', ellipsis: true },
  { title: 'AppID', dataIndex: 'appId', key: 'appId', width: 140 },
  { title: '映射版本', dataIndex: 'fieldMappingVersion', key: 'fieldMappingVersion', width: 120 },
  { title: '字段映射', key: 'fieldMapping', width: 110 },
  { title: '状态', key: 'enabled', width: 90 }
]

const mappingOpen = ref(false)
const mappingList = ref<{ local: string; report: string }[]>([])

function channelText(c?: string) {
  return c === 'MZ_JINMIN' ? '民政金民' : c === 'YB_MEDICAL' ? '医保结算' : c || '-'
}

function viewMapping(record: GovChannelConfig) {
  mappingList.value = []
  if (record.fieldMappingJson) {
    try {
      const obj = JSON.parse(record.fieldMappingJson) as Record<string, string>
      mappingList.value = Object.entries(obj).map(([local, report]) => ({ local, report }))
    } catch {
      mappingList.value = []
    }
  }
  mappingOpen.value = true
}

async function loadData() {
  loading.value = true
  try {
    rows.value = (await getGovChannels()) || []
  } finally {
    loading.value = false
  }
}

loadData()
</script>
