<template>
  <PageContainer title="合同与票据" subTitle="合同、票据、账单按同一合同号联动">
    <a-row :gutter="16">
      <a-col :xs="24" :lg="14">
        <a-card title="附件中心" class="card-elevated" :bordered="false">
          <a-space wrap style="margin-bottom: 12px">
            <a-button @click="downloadBundle">下载清单</a-button>
            <a-button @click="goContractManagement">查看合同详情</a-button>
            <a-button @click="go('/finance/bill')">前往费用账单</a-button>
          </a-space>
          <a-alert
            type="info"
            show-icon
            message="合同签署与附件上传统一在【营销管理-合同签约】中完成，本页面仅做已签署合同与票据只读展示。"
            style="margin-bottom: 12px"
          />
          <StatefulBlock :loading="loading" :error="errorMessage" :empty="!rows.length" empty-text="暂无附件" @retry="loadLinkage">
            <a-table :columns="columns" :data-source="rows" row-key="id" :pagination="false" />
          </StatefulBlock>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="10">
        <a-card title="结构化字段" class="card-elevated" :bordered="false" style="margin-bottom: 16px">
          <StatefulBlock :loading="loading" :error="errorMessage" @retry="loadLinkage">
            <a-descriptions :column="1" size="small" bordered>
              <a-descriptions-item label="合同号">{{ linkage?.contractNo || '-' }}</a-descriptions-item>
              <a-descriptions-item label="签约状态">{{ linkage?.contractStatus || '-' }}</a-descriptions-item>
              <a-descriptions-item label="签约时间">{{ linkage?.contractSignedAt || '-' }}</a-descriptions-item>
              <a-descriptions-item label="合同有效期止">{{ linkage?.contractExpiryDate || '-' }}</a-descriptions-item>
              <a-descriptions-item label="入住日期">{{ linkage?.admissionDate || '-' }}</a-descriptions-item>
              <a-descriptions-item label="押金">{{ formatAmount(linkage?.depositAmount) }}</a-descriptions-item>
            </a-descriptions>
          </StatefulBlock>
        </a-card>

        <a-card title="票据联动摘要" class="card-elevated" :bordered="false">
          <StatefulBlock :loading="loading" :error="errorMessage" @retry="loadLinkage">
            <a-descriptions :column="1" size="small" bordered>
              <a-descriptions-item label="账单数量">{{ linkage?.billCount ?? 0 }}</a-descriptions-item>
              <a-descriptions-item label="账单总额">{{ formatAmount(linkage?.billTotalAmount) }}</a-descriptions-item>
              <a-descriptions-item label="已收金额">{{ formatAmount(linkage?.billPaidAmount) }}</a-descriptions-item>
              <a-descriptions-item label="未收金额">{{ formatAmount(linkage?.billOutstandingAmount) }}</a-descriptions-item>
              <a-descriptions-item label="附件数量">{{ linkage?.attachmentCount ?? 0 }}</a-descriptions-item>
            </a-descriptions>
          </StatefulBlock>
        </a-card>
      </a-col>
    </a-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../../components/PageContainer.vue'
import StatefulBlock from '../../../components/StatefulBlock.vue'
import { getContractLinkageByElder } from '../../../api/marketing'
import type { ContractAttachmentItem, ContractLinkageSummary } from '../../../types'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const errorMessage = ref('')
const linkage = ref<ContractLinkageSummary>()

const columns = [
  { title: '类型', dataIndex: 'type', key: 'type', width: 120 },
  { title: '文件名', dataIndex: 'name', key: 'name' },
  { title: '文件链接', dataIndex: 'url', key: 'url', width: 260 },
  { title: '大小', dataIndex: 'size', key: 'size', width: 120 },
  { title: '上传时间', dataIndex: 'time', key: 'time', width: 180 }
]

const rows = computed(() => {
  const attachments = linkage.value?.attachments || []
  return attachments.map((item) => ({
    id: item.id,
    type: normalizeType(item),
    name: item.fileName,
    url: item.fileUrl,
    size: formatFileSize(item.fileSize),
    time: item.createTime || '-'
  }))
})

function normalizeType(item: ContractAttachmentItem) {
  const fileName = (item.fileName || '').toLowerCase()
  if (fileName.includes('发票') || fileName.includes('invoice') || fileName.includes('receipt')) {
    return '收据/发票'
  }
  if (fileName.includes('合同') || fileName.includes('contract')) {
    return '合同'
  }
  return item.fileType || '附件'
}

function go(path: string) {
  router.push(path)
}

function goContractManagement() {
  if (linkage.value?.contractNo) {
    router.push(`/marketing/contract-management?contractNo=${encodeURIComponent(linkage.value.contractNo)}`)
    return
  }
  router.push('/marketing/contract-management')
}

function downloadBundle() {
  const content = rows.value
    .map((item) => `${item.type},${item.name},${item.url || '-'},${item.size},${item.time}`)
    .join('\n')
  const csv = `类型,文件名,文件链接,大小,上传时间\n${content}`
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = '合同与票据-附件清单.csv'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

function formatFileSize(size?: number) {
  if (!size || size <= 0) return '-'
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / (1024 * 1024)).toFixed(1)} MB`
}

function formatAmount(value?: number) {
  const amount = value ?? 0
  return amount.toFixed(2)
}

async function loadLinkage() {
  const residentId = Number(route.query.residentId || route.query.elderId)
  if (!residentId) {
    linkage.value = undefined
    return
  }
  loading.value = true
  errorMessage.value = ''
  try {
    linkage.value = await getContractLinkageByElder(residentId)
  } catch (error: any) {
    errorMessage.value = error?.message || '加载合同与票据失败'
    linkage.value = undefined
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

onMounted(loadLinkage)
</script>
