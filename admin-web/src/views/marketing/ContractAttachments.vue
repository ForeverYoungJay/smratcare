<template>
  <PageContainer title="合同附件" sub-title="合同附件集中查看：合同、病历、医保、户口等">
    <MarketingQuickNav parent-path="/marketing/contracts" />
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline">
        <a-form-item label="合同编号">
          <a-input v-model:value="query.contractNo" allow-clear />
        </a-form-item>
        <a-form-item label="长者姓名">
          <a-input v-model:value="query.elderName" allow-clear placeholder="请输入 长者姓名" style="width: 220px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadContracts">查询</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px">
      <MarketingListToolbar :selected-count="selectedCount" tip="可批量删除附件">
        <a-space>
          <a-button :disabled="!hasSingleSelection" @click="openSelectedAttachments">查看附件</a-button>
          <a-button :disabled="selectedCount === 0" @click="exportSelectedAttachmentIndex">导出所选附件索引</a-button>
          <a-button @click="exportAllContractIndex">导出合同索引</a-button>
        </a-space>
      </MarketingListToolbar>
      <a-table
        :columns="contractColumns"
        :data-source="contracts"
        :loading="loadingContracts"
        row-key="id"
        :row-selection="rowSelection"
        :pagination="false"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-button type="link" @click="openAttachments(record)">查看附件</a-button>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="attachmentOpen" title="附件清单" width="860px" :footer="null">
      <a-table :columns="attachmentColumns" :data-source="attachments" :loading="loadingAttachments" row-key="id" :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'preview'">
            <a-button type="link" size="small" @click="previewAttachment(record)">预览</a-button>
          </template>
          <template v-else-if="column.key === 'fileUrl'">
            <a :href="resolveAttachmentUrl(record.fileUrl)" target="_blank" rel="noopener noreferrer" style="color: #1677ff">
              {{ record.fileName }}
            </a>
          </template>
          <template v-else-if="column.key === 'attachmentType'">
            {{ attachmentTypeLabel(record.attachmentType) }}
          </template>
          <template v-else-if="column.key === 'fileSize'">
            {{ formatSize(record.fileSize) }}
          </template>
        </template>
      </a-table>
    </a-modal>

    <a-modal v-model:open="previewOpen" :title="previewTitle" width="960px" :footer="null">
      <a-image
        v-if="previewKind === 'image' && previewUrl"
        :src="previewUrl"
        style="max-width: 100%"
      />
      <iframe
        v-else-if="previewUrl"
        :src="previewUrl"
        style="width: 100%; height: 72vh; border: 0"
      />
      <a-empty v-else description="附件链接缺失，暂时无法预览" />
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import MarketingQuickNav from './components/MarketingQuickNav.vue'
import MarketingListToolbar from './components/MarketingListToolbar.vue'
import { getContractAttachments, getContractPage } from '../../api/marketing'
import { exportCsv } from '../../utils/export'
import type { ContractAttachmentItem, CrmContractItem, PageResult } from '../../types'

const query = reactive({
  contractNo: '',
  elderName: ''
})
const contracts = ref<CrmContractItem[]>([])
const attachments = ref<ContractAttachmentItem[]>([])
const loadingContracts = ref(false)
const loadingAttachments = ref(false)
const attachmentOpen = ref(false)
const previewOpen = ref(false)
const previewUrl = ref('')
const previewTitle = ref('附件预览')
const previewKind = ref<'image' | 'iframe'>('iframe')
const selectedRowKeys = ref<string[]>([])
const selectedContracts = ref<CrmContractItem[]>([])

const contractColumns = [
  { title: '合同编号', dataIndex: 'contractNo', key: 'contractNo', width: 180 },
  { title: '长者姓名', dataIndex: 'elderName', key: 'elderName', width: 140 },
  { title: '联系电话', dataIndex: 'elderPhone', key: 'elderPhone', width: 150 },
  { title: '流程阶段', dataIndex: 'flowStage', key: 'flowStage', width: 140 },
  { title: '附件', key: 'action', width: 120 }
]
const attachmentColumns = [
  { title: '预览', dataIndex: 'preview', key: 'preview', width: 90 },
  { title: '类型', dataIndex: 'attachmentType', key: 'attachmentType', width: 140 },
  { title: '文件名', dataIndex: 'fileName', key: 'fileName', width: 260 },
  { title: '文件链接', dataIndex: 'fileUrl', key: 'fileUrl', width: 300 },
  { title: '大小', dataIndex: 'fileSize', key: 'fileSize', width: 100 },
  { title: '上传时间', dataIndex: 'createTime', key: 'createTime', width: 170 }
]
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: Array<string | number>, rows: CrmContractItem[]) => {
    selectedRowKeys.value = keys.map((item) => String(item))
    selectedContracts.value = rows
  }
}))
const selectedCount = computed(() => selectedRowKeys.value.length)
const hasSingleSelection = computed(() => selectedContracts.value.length === 1)

function attachmentTypeLabel(type?: string) {
  if (type === 'MEDICAL_RECORD') return '病历资料'
  if (type === 'MEDICAL_INSURANCE') return '医保复印件'
  if (type === 'HOUSEHOLD') return '户口复印件'
  if (type === 'CONTRACT') return '合同附件'
  return '其他附件'
}

function formatSize(size?: number) {
  const bytes = Number(size || 0)
  if (!bytes) return '-'
  if (bytes < 1024) return `${bytes}B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)}KB`
  return `${(bytes / 1024 / 1024).toFixed(1)}MB`
}

function resolveAttachmentUrl(fileUrl?: string) {
  const text = String(fileUrl || '').trim()
  if (!text) return ''
  if (/^https?:\/\//i.test(text)) return text
  try {
    return new URL(text, window.location.origin).toString()
  } catch (_error) {
    return text
  }
}

function isImageAttachment(record: ContractAttachmentItem) {
  const fileType = String(record.fileType || '').toLowerCase()
  if (fileType.startsWith('image/')) return true
  const fileUrl = String(record.fileUrl || '').toLowerCase()
  return ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.bmp'].some((suffix) => fileUrl.includes(suffix))
}

function isIframePreviewable(record: ContractAttachmentItem) {
  if (isImageAttachment(record)) return true
  const fileType = String(record.fileType || '').toLowerCase()
  if (fileType.includes('pdf') || fileType.startsWith('text/')) return true
  const fileUrl = String(record.fileUrl || '').toLowerCase()
  return ['.pdf', '.txt', '.md', '.json', '.csv', '.log'].some((suffix) => fileUrl.includes(suffix))
}

function previewAttachment(record: ContractAttachmentItem) {
  const url = resolveAttachmentUrl(record.fileUrl)
  if (!url) {
    message.warning('附件链接缺失，暂时无法预览')
    return
  }
  if (isIframePreviewable(record)) {
    previewTitle.value = record.fileName || '附件预览'
    previewKind.value = isImageAttachment(record) ? 'image' : 'iframe'
    previewUrl.value = url
    previewOpen.value = true
    return
  }
  window.open(url, '_blank', 'noopener,noreferrer')
  message.info('该文件将通过浏览器新窗口打开，若浏览器不支持预览会直接下载')
}

async function loadContracts() {
  loadingContracts.value = true
  try {
    const page: PageResult<CrmContractItem> = await getContractPage({
      pageNo: 1,
      pageSize: 200,
      contractNo: query.contractNo || undefined,
      elderName: query.elderName || undefined
    })
    contracts.value = (page.list || []).map((item) => ({ ...item, id: String(item.id) }))
  } finally {
    loadingContracts.value = false
  }
}

async function openAttachments(record: CrmContractItem) {
  loadingAttachments.value = true
  attachmentOpen.value = true
  try {
    attachments.value = (await getContractAttachments(String(record.id))) || []
  } finally {
    loadingAttachments.value = false
  }
}

function openSelectedAttachments() {
  if (selectedContracts.value.length !== 1) {
    message.info('请先勾选 1 条合同')
    return
  }
  openAttachments(selectedContracts.value[0])
}

async function exportSelectedAttachmentIndex() {
  if (!selectedContracts.value.length) {
    message.info('请先勾选合同')
    return
  }
  const result = await Promise.all(
    selectedContracts.value.map(async (contract) => {
      const list = await getContractAttachments(String(contract.id))
      return (list || []).map((item) => ({
        合同编号: contract.contractNo || '-',
        长者姓名: contract.elderName || '-',
        附件类型: attachmentTypeLabel(item.attachmentType),
        文件名: item.fileName || '-',
        文件链接: item.fileUrl || '-',
        上传时间: item.createTime || '-'
      }))
    })
  )
  const flatRows = result.flat()
  if (!flatRows.length) {
    message.warning('所选合同暂无附件可导出')
    return
  }
  exportCsv(flatRows, `contract-attachments-selected-${Date.now()}.csv`)
  message.success(`已导出 ${flatRows.length} 条附件索引`)
}

function exportAllContractIndex() {
  exportCsv(
    contracts.value.map((item) => ({
      合同编号: item.contractNo || '-',
      长者姓名: item.elderName || '-',
      联系电话: item.elderPhone || '-',
      流程阶段: item.flowStage || '-'
    })),
    `contract-index-${Date.now()}.csv`
  )
  message.success('合同索引已导出')
}

function reset() {
  query.contractNo = ''
  query.elderName = ''
  selectedRowKeys.value = []
  selectedContracts.value = []
  loadContracts()
}

loadContracts()
</script>
