<template>
  <PageContainer title="送餐记录" subTitle="记录送餐结果与送达状态">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="日期范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 140px" />
      </a-form-item>
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="订单号/区域/送餐人" allow-clear />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新增送餐记录</a-button>
      </template>
    </SearchForm>

    <DataTable rowKey="id" :columns="columns" :data-source="rows" :loading="loading" :pagination="pagination" @change="handleTableChange">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="getDiningDeliveryStatusColor(record.status)">
            {{ statusLabel(record.status) }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" @click="openQr(record)">签收码</a-button>
            <a-button type="link" @click="openEdit(record)">编辑</a-button>
            <a-button type="link" danger @click="remove(record)">删除</a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal v-model:open="qrOpen" title="送餐签收二维码" :footer="null" width="460px">
      <div class="qr-modal">
        <img v-if="qrDataUrl" :src="qrDataUrl" alt="送餐签收二维码" class="qr-image" />
        <a-typography-text v-if="qrText" copyable>{{ qrText }}</a-typography-text>
        <a-typography-text type="secondary">这里展示的是当前长者已有的床位二维码，若未绑定床位则回退为老人二维码。</a-typography-text>
      </div>
    </a-modal>

    <a-modal v-model:open="editOpen" title="送餐记录" :confirm-loading="saving" @ok="submit">
      <a-form layout="vertical">
        <a-form-item label="点餐单" required>
          <a-select
            v-model:value="form.mealOrderId"
            show-search
            :filter-option="false"
            :options="mealOrderOptions"
            placeholder="请输入订单号或老人姓名搜索"
            @search="searchMealOrders"
            @change="onMealOrderChange"
          />
        </a-form-item>
        <a-form-item label="订单号" required>
          <a-input v-model:value="form.orderNo" readonly />
        </a-form-item>
        <a-form-item label="送餐区域">
          <a-select
            v-model:value="form.deliveryAreaId"
            :options="deliveryAreaOptions"
            allow-clear
            placeholder="请选择送餐区域"
            @change="onDeliveryAreaChange"
          />
        </a-form-item>
        <a-form-item label="送餐人">
          <a-input v-model:value="form.deliveredByName" />
        </a-form-item>
        <a-form-item label="送达时间">
          <a-date-picker v-model:value="form.deliveredAt" show-time style="width: 100%" />
        </a-form-item>
        <a-form-item label="床头码签收时间">
          <a-date-picker v-model:value="form.signedAt" show-time style="width: 100%" />
        </a-form-item>
        <a-form-item label="床头码扫码回传时间">
          <a-date-picker v-model:value="form.qrScanAt" show-time style="width: 100%" />
        </a-form-item>
        <a-form-item label="签收图片">
          <a-upload :before-upload="beforeUploadSignoffImage" :show-upload-list="false" accept="image/*" multiple>
            <a-button :loading="uploadingSignoffImage">上传签收图片</a-button>
          </a-upload>
          <a-space v-if="form.signoffImageUrls.length" wrap style="margin-top: 8px">
            <a-tag v-for="(url, index) in form.signoffImageUrls" :key="`${url}_${index}`" closable @close.prevent="removeSignoffImage(index)">
              <a :href="url" target="_blank" rel="noopener noreferrer">图片{{ index + 1 }}</a>
            </a-tag>
          </a-space>
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="form.status" :options="statusOptions" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="form.remark" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import dayjs from 'dayjs'
import QRCode from 'qrcode'
import { message } from 'ant-design-vue'
import type { Dayjs } from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { uploadOaFile } from '../../api/oa'
import {
  DINING_DELIVERY_STATUS_OPTIONS,
  DINING_MESSAGES,
  DINING_STATUS,
  getDiningDeliveryStatusColor,
  getDiningDeliveryStatusLabel
} from '../../constants/dining'
import {
  getDiningDeliveryRecordPage,
  createDiningDeliveryRecord,
  updateDiningDeliveryRecord,
  deleteDiningDeliveryRecord,
  generateDiningDeliverySignoffQr,
  getDiningMealOrderPage,
  getDiningDeliveryAreaList
} from '../../api/dining'
import type { DiningDeliveryRecord, DiningMealOrder, Id, PageResult } from '../../types'

const statusOptions = DINING_DELIVERY_STATUS_OPTIONS

const loading = ref(false)
const rows = ref<DiningDeliveryRecord[]>([])
const mealOrderOptions = ref<{ label: string; value: Id; orderNo: string; deliveryAreaId?: Id; deliveryAreaName?: string }[]>([])
const deliveryAreaOptions = ref<{ label: string; value: Id; name: string }[]>([])
const query = reactive({
  range: undefined as [Dayjs, Dayjs] | undefined,
  status: undefined as string | undefined,
  keyword: '',
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const editOpen = ref(false)
const saving = ref(false)
const uploadingSignoffImage = ref(false)
const qrOpen = ref(false)
const qrText = ref('')
const qrDataUrl = ref('')
const form = reactive({
  id: undefined as Id | undefined,
  mealOrderId: undefined as Id | undefined,
  orderNo: '',
  deliveryAreaId: undefined as Id | undefined,
  deliveryAreaName: '',
  deliveredByName: '',
  deliveredAt: undefined as Dayjs | undefined,
  signedAt: undefined as Dayjs | undefined,
  qrScanAt: undefined as Dayjs | undefined,
  signoffImageUrls: [] as string[],
  status: DINING_STATUS.pending,
  remark: ''
})

const columns = [
  { title: '点餐单ID', dataIndex: 'mealOrderId', key: 'mealOrderId', width: 120 },
  { title: '订单号', dataIndex: 'orderNo', key: 'orderNo', width: 150 },
  { title: '送餐区域', dataIndex: 'deliveryAreaName', key: 'deliveryAreaName', width: 130 },
  { title: '送餐人', dataIndex: 'deliveredByName', key: 'deliveredByName', width: 120 },
  { title: '送达时间', dataIndex: 'deliveredAt', key: 'deliveredAt', width: 180 },
  { title: '签收时间', dataIndex: 'signedAt', key: 'signedAt', width: 180 },
  { title: '签收图片', key: 'signoffImageUrls', width: 110, customRender: ({ record }: any) => `${record.signoffImageUrls?.length || 0}张` },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '操作', key: 'action', width: 190 }
]

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNo: query.pageNo, pageSize: query.pageSize, keyword: query.keyword, status: query.status }
    if (query.range) {
      params.dateFrom = query.range[0].format('YYYY-MM-DD')
      params.dateTo = query.range[1].format('YYYY-MM-DD')
    }
    const res: PageResult<DiningDeliveryRecord> = await getDiningDeliveryRecordPage(params)
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally {
    loading.value = false
  }
}

function statusLabel(status?: string) {
  return getDiningDeliveryStatusLabel(status)
}

function handleTableChange(pag: any) {
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

function onReset() {
  query.range = undefined
  query.status = undefined
  query.keyword = ''
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

async function loadDeliveryAreaOptions() {
  const list = await getDiningDeliveryAreaList({})
  deliveryAreaOptions.value = (list || []).map((item: any) => ({
    label: `${item.areaCode}-${item.areaName}`,
    value: String(item.id),
    name: item.areaName
  }))
}

async function searchMealOrders(keyword = '') {
  const page = await getDiningMealOrderPage({ pageNo: 1, pageSize: 30, keyword })
  mealOrderOptions.value = (page.list || []).map((item: DiningMealOrder) => ({
    label: `${item.orderNo} / ${item.elderName || ''}`,
    value: String(item.id),
    orderNo: item.orderNo,
    deliveryAreaId: item.deliveryAreaId,
    deliveryAreaName: item.deliveryAreaName
  }))
}

function onMealOrderChange(value: Id | undefined) {
  const selected = mealOrderOptions.value.find((item) => item.value === value)
  form.orderNo = selected?.orderNo || ''
  form.deliveryAreaId = undefined
  form.deliveryAreaName = ''
  if (selected?.deliveryAreaId) {
    form.deliveryAreaId = selected.deliveryAreaId
    form.deliveryAreaName = selected.deliveryAreaName || ''
  }
}

function onDeliveryAreaChange(value: Id | undefined) {
  const selected = deliveryAreaOptions.value.find((item) => item.value === value)
  form.deliveryAreaName = selected?.name || ''
}

async function openCreate() {
  form.id = undefined
  form.mealOrderId = undefined
  form.orderNo = ''
  form.deliveryAreaId = undefined
  form.deliveryAreaName = ''
  form.deliveredByName = ''
  form.deliveredAt = undefined
  form.signedAt = undefined
  form.qrScanAt = undefined
  form.signoffImageUrls = []
  form.status = DINING_STATUS.pending
  form.remark = ''
  await Promise.all([searchMealOrders(), loadDeliveryAreaOptions()])
  editOpen.value = true
}

async function openEdit(record: DiningDeliveryRecord) {
  form.id = record.id
  form.mealOrderId = record.mealOrderId
  form.orderNo = record.orderNo
  form.deliveryAreaId = record.deliveryAreaId
  form.deliveryAreaName = record.deliveryAreaName || ''
  form.deliveredByName = record.deliveredByName || ''
  form.deliveredAt = record.deliveredAt ? dayjs(record.deliveredAt) : undefined
  form.signedAt = record.signedAt ? dayjs(record.signedAt) : undefined
  form.qrScanAt = record.qrScanAt ? dayjs(record.qrScanAt) : undefined
  form.signoffImageUrls = Array.isArray(record.signoffImageUrls) ? [...record.signoffImageUrls] : []
  form.status = record.status || DINING_STATUS.pending
  form.remark = record.remark || ''
  await Promise.all([searchMealOrders(record.orderNo || ''), loadDeliveryAreaOptions()])
  editOpen.value = true
}

async function submit() {
  if (!form.mealOrderId || !form.orderNo) {
    message.error(DINING_MESSAGES.requiredMealOrder)
    return
  }
  saving.value = true
  try {
    const payload = {
      mealOrderId: form.mealOrderId,
      orderNo: form.orderNo,
      deliveryAreaId: form.deliveryAreaId,
      deliveryAreaName: form.deliveryAreaName,
      deliveredByName: form.deliveredByName.trim() || undefined,
      deliveredAt: form.deliveredAt ? dayjs(form.deliveredAt).format('YYYY-MM-DD HH:mm:ss') : undefined,
      signedAt: form.signedAt ? dayjs(form.signedAt).format('YYYY-MM-DD HH:mm:ss') : undefined,
      qrScanAt: form.qrScanAt ? dayjs(form.qrScanAt).format('YYYY-MM-DD HH:mm:ss') : undefined,
      signoffImageUrls: form.signoffImageUrls,
      status: form.status,
      remark: form.remark.trim() || undefined
    }
    if (form.id) {
      await updateDiningDeliveryRecord(form.id, payload)
    } else {
      await createDiningDeliveryRecord(payload)
    }
    editOpen.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

async function remove(record: DiningDeliveryRecord) {
  await deleteDiningDeliveryRecord(record.id)
  fetchData()
}

async function openQr(record: DiningDeliveryRecord) {
  const payload = await generateDiningDeliverySignoffQr(record.id)
  qrText.value = payload.qrContent || payload.qrToken || ''
  qrDataUrl.value = qrText.value ? await QRCode.toDataURL(qrText.value) : ''
  qrOpen.value = true
}

async function beforeUploadSignoffImage(file: File) {
  uploadingSignoffImage.value = true
  try {
    const uploaded = await uploadOaFile(file, 'dining-delivery-signoff')
    const url = String(uploaded?.fileUrl || '').trim()
    if (!url) {
      message.error('上传失败：未返回图片地址')
      return false
    }
    if (!form.signoffImageUrls.includes(url)) {
      form.signoffImageUrls = [...form.signoffImageUrls, url]
    }
    if (!form.signedAt) {
      form.signedAt = dayjs()
    }
    if (!form.qrScanAt) {
      form.qrScanAt = dayjs()
    }
    message.success('签收图片上传成功')
  } finally {
    uploadingSignoffImage.value = false
  }
  return false
}

function removeSignoffImage(index: number) {
  form.signoffImageUrls = form.signoffImageUrls.filter((_, currentIndex) => currentIndex !== index)
}

searchMealOrders()
loadDeliveryAreaOptions()
fetchData()
</script>

<style scoped>
.qr-modal {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.qr-image {
  width: 220px;
  height: 220px;
}
</style>
