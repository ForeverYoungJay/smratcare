<template>
  <PageContainer title="库存预警" subTitle="低库存与临期预警中心">
    <a-card class="card-elevated" :bordered="false">
      <a-space>
        <a-button @click="exportSuggestions">导出采购建议</a-button>
        <a-upload :show-upload-list="false" :before-upload="beforeUploadPhoto">
          <a-button :loading="uploading">上传库存拍照单品/合计图</a-button>
        </a-upload>
        <a-button @click="fetchAll">刷新</a-button>
      </a-space>
      <div v-if="photoAttachments.length" class="attach-list">
        <div v-for="(item, idx) in photoAttachments" :key="`${item.url}-${idx}`" class="attach-item">
          <a :href="item.url" target="_blank" rel="noopener noreferrer">{{ item.name }}</a>
          <a-button type="link" danger size="small" @click="removePhoto(idx)">删除</a-button>
        </div>
      </div>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <a-tabs v-model:activeKey="activeKey">
        <a-tab-pane key="low" tab="低库存预警">
          <a-table
            :columns="lowColumns"
            :data-source="lowRows"
            row-key="productId"
            :loading="loading"
            :pagination="false"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'actions'">
                <a-space>
                  <a @click="toStock(record)">查看库存</a>
                  <a @click="toPurchase(record)">发起采购</a>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-tab-pane>
        <a-tab-pane key="expiry" tab="临期预警">
          <a-table
            :columns="expiryColumns"
            :data-source="expiryRows"
            row-key="batchId"
            :loading="loading"
            :pagination="false"
          />
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { exportCsv } from '../../utils/export'
import { getInventoryAlerts, getInventoryExpiryAlerts } from '../../api/materialCenter'
import { uploadOaFile } from '../../api/oa'
import type { InventoryAlertItem, InventoryExpiryAlertItem } from '../../types'

const activeKey = ref('low')
const router = useRouter()
const loading = ref(false)
const uploading = ref(false)
const lowRows = ref<InventoryAlertItem[]>([])
const expiryRows = ref<InventoryExpiryAlertItem[]>([])
const photoAttachments = ref<Array<{ name: string; url: string }>>([])
const attachmentStorageKey = 'inventory-alert-photo-attachments'

const lowColumns = [
  { title: '商品名称', dataIndex: 'productName' },
  { title: '商品ID', dataIndex: 'productId', width: 120 },
  { title: '安全库存', dataIndex: 'safetyStock', width: 120 },
  { title: '当前库存', dataIndex: 'currentStock', width: 120 },
  {
    title: '建议采购数量',
    key: 'suggestQty',
    width: 140,
    customRender: ({ record }: { record: InventoryAlertItem }) => {
      const current = Number(record.currentStock || 0)
      const safety = Number(record.safetyStock || 0)
      return Math.max(safety - current, 0)
    }
  },
  { title: '操作', key: 'actions', width: 180 }
]

const expiryColumns = [
  { title: '商品名称', dataIndex: 'productName' },
  { title: '批次号', dataIndex: 'batchNo', width: 160 },
  { title: '库存数量', dataIndex: 'quantity', width: 120 },
  { title: '有效期', dataIndex: 'expireDate', width: 140 },
  { title: '距离到期(天)', dataIndex: 'daysToExpire', width: 140 }
]

async function fetchAll() {
  loading.value = true
  try {
    lowRows.value = await getInventoryAlerts()
    expiryRows.value = await getInventoryExpiryAlerts()
  } finally {
    loading.value = false
  }
}

function exportSuggestions() {
  const data = [
    ...lowRows.value.map((r) => ({
      类型: '低库存',
      商品名称: r.productName,
      商品ID: r.productId,
      安全库存: r.safetyStock,
      当前库存: r.currentStock,
      建议采购数量: Math.max(Number(r.safetyStock || 0) - Number(r.currentStock || 0), 0)
    })),
    ...expiryRows.value.map((r) => ({
      类型: '临期',
      商品名称: r.productName,
      批次号: r.batchNo,
      库存数量: r.quantity,
      有效期: r.expireDate,
      距离到期: r.daysToExpire
    }))
  ]
  exportCsv(data, '库存预警清单')
}

function loadAttachments() {
  try {
    const cached = JSON.parse(localStorage.getItem(attachmentStorageKey) || '[]')
    photoAttachments.value = Array.isArray(cached) ? cached.filter((item) => item?.url) : []
  } catch {
    photoAttachments.value = []
  }
}

function saveAttachments() {
  localStorage.setItem(attachmentStorageKey, JSON.stringify(photoAttachments.value))
}

async function beforeUploadPhoto(file: File) {
  uploading.value = true
  try {
    const uploaded = await uploadOaFile(file, 'inventory-alert-photo')
    const url = uploaded?.fileUrl || ''
    if (!url) {
      message.error('上传失败：未返回文件地址')
      return false
    }
    photoAttachments.value.unshift({
      name: uploaded.originalFileName || uploaded.fileName || file.name,
      url
    })
    saveAttachments()
    message.success('库存图片已上传')
  } catch (error: any) {
    message.error(error?.message || '上传失败')
  } finally {
    uploading.value = false
  }
  return false
}

function removePhoto(index: number) {
  photoAttachments.value.splice(index, 1)
  saveAttachments()
}

function suggestQty(record: InventoryAlertItem) {
  return Math.max(Number(record.safetyStock || 0) - Number(record.currentStock || 0), 1)
}

function toStock(record: InventoryAlertItem) {
  router.push({
    path: '/logistics/storage/stock-query',
    query: {
      productId: record.productId,
      keyword: record.productName || '',
      lowStockOnly: '1',
      source: 'inventory_alert'
    }
  })
}

function toPurchase(record: InventoryAlertItem) {
  router.push({
    path: '/logistics/storage/purchase',
    query: {
      autoCreate: '1',
      productId: record.productId,
      quantity: suggestQty(record),
      source: 'inventory_alert'
    }
  })
}

onMounted(() => {
  loadAttachments()
  fetchAll()
})
</script>

<style scoped>
.attach-list {
  margin-top: 10px;
  display: grid;
  gap: 6px;
}
.attach-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 4px 8px;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
}
</style>
