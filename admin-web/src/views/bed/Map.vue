<template>
  <PageContainer title="房态图" subTitle="床位状态一览">
    <a-card class="card-elevated" :bordered="false">
      <a-form :model="query" layout="inline" class="search-bar">
        <a-form-item label="床位号">
          <a-input v-model:value="query.bedNo" placeholder="床位号" allow-clear />
        </a-form-item>
        <a-form-item label="房间号">
          <a-input v-model:value="query.roomNo" placeholder="房间号" allow-clear />
        </a-form-item>
        <a-form-item label="老人">
          <a-input v-model:value="query.elderName" placeholder="姓名" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 120px">
            <a-select-option :value="1">空床</a-select-option>
            <a-select-option :value="2">入住</a-select-option>
            <a-select-option :value="3">维修</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="排序">
          <a-select v-model:value="query.sortBy" style="width: 140px">
            <a-select-option value="bedNo">床位号</a-select-option>
            <a-select-option value="roomNo">房间号</a-select-option>
            <a-select-option value="status">状态</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-select v-model:value="query.sortOrder" style="width: 120px">
            <a-select-option value="asc">升序</a-select-option>
            <a-select-option value="desc">降序</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="applySearch">查询</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-row :gutter="16">
        <a-col v-for="bed in pagedBeds" :key="bed.id" :xs="24" :sm="12" :md="8" :lg="6">
          <a-card class="bed-card" :hoverable="true" @click="openBed(bed)">
            <div class="bed-title">
              <span>{{ bed.bedNo }}</span>
              <a-tag :color="statusTag(bed.status, bed.elderId)">{{ statusText(bed.status, bed.elderId) }}</a-tag>
            </div>
            <div class="bed-meta">老人：{{ bed.elderName || '-' }}</div>
            <div class="bed-meta">护理等级：{{ bed.careLevel || '-' }}</div>
          </a-card>
        </a-col>
      </a-row>

      <a-pagination
        style="margin-top: 16px; text-align: right;"
        :current="query.pageNo"
        :page-size="query.pageSize"
        :total="filteredBeds.length"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>

    <a-modal v-model:open="detailOpen" title="床位详情" width="420px" @ok="printBedQr" ok-text="打印" @cancel="() => (detailOpen = false)">
      <a-descriptions bordered :column="1" size="small">
        <a-descriptions-item label="床位">{{ current?.roomNo || current?.roomId }}-{{ current?.bedNo }}</a-descriptions-item>
        <a-descriptions-item label="老人">{{ current?.elderName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="护理等级">{{ current?.careLevel || '-' }}</a-descriptions-item>
        <a-descriptions-item label="今日任务完成率">{{ completionRate }}%</a-descriptions-item>
      </a-descriptions>
      <div class="qr-preview" v-if="qrDataUrl">
        <img :src="qrDataUrl" alt="qr" />
      </div>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import QRCode from 'qrcode'
import PageContainer from '../../components/PageContainer.vue'
import { getBedMap } from '../../api/bed'
import type { BedItem } from '../../types/api'

const beds = ref<BedItem[]>([])
const detailOpen = ref(false)
const current = ref<BedItem | null>(null)
const completionRate = ref(0)
const qrDataUrl = ref('')

const query = reactive({
  bedNo: '',
  roomNo: '',
  elderName: '',
  status: undefined as number | undefined,
  sortBy: 'bedNo',
  sortOrder: 'asc',
  pageNo: 1,
  pageSize: 12
})

const filteredBeds = computed(() => {
  const list = beds.value.filter((bed) => {
    if (query.bedNo && !String(bed.bedNo || '').includes(query.bedNo)) return false
    if (query.roomNo && !String(bed.roomNo || '').includes(query.roomNo)) return false
    if (query.elderName && !String(bed.elderName || '').includes(query.elderName)) return false
    if (query.status && bed.status !== query.status) return false
    return true
  })
  const factor = query.sortOrder === 'asc' ? 1 : -1
  return list.sort((a: any, b: any) => {
    const av = a[query.sortBy as keyof BedItem] ?? ''
    const bv = b[query.sortBy as keyof BedItem] ?? ''
    if (av === bv) return 0
    return av > bv ? factor : -factor
  })
})

const pagedBeds = computed(() => {
  const start = (query.pageNo - 1) * query.pageSize
  return filteredBeds.value.slice(start, start + query.pageSize)
})

function statusText(status?: number, elderId?: number) {
  if (status === 2) return '占用'
  if (status === 3) return '维修'
  if (elderId) return '入住'
  return '空床'
}

function statusTag(status?: number, elderId?: number) {
  if (status === 2) return 'orange'
  if (status === 3) return 'red'
  if (elderId) return 'blue'
  return 'green'
}

async function load() {
  try {
    beds.value = await getBedMap()
  } catch {
    beds.value = []
  }
}

function applySearch() {
  query.pageNo = 1
}

function reset() {
  query.bedNo = ''
  query.roomNo = ''
  query.elderName = ''
  query.status = undefined
  query.sortBy = 'bedNo'
  query.sortOrder = 'asc'
  query.pageNo = 1
}

function onPageChange(page: number) {
  query.pageNo = page
}

function onPageSizeChange(current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
}

async function openBed(bed: BedItem) {
  current.value = bed
  completionRate.value = Math.floor(Math.random() * 40) + 60
  detailOpen.value = true
  qrDataUrl.value = await QRCode.toDataURL(bed.bedQrCode || `BED:${bed.id}`)
}

function printBedQr() {
  if (!qrDataUrl.value) {
    message.warning('未生成二维码')
    return
  }
  const win = window.open('', '_blank')
  if (!win) return
  win.document.write(`<img src="${qrDataUrl.value}" style="width:220px;height:220px"/>`)
  win.print()
  win.close()
}

onMounted(load)
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}
.bed-card {
  margin-bottom: 16px;
  cursor: pointer;
}
.bed-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}
.bed-meta {
  color: var(--muted);
  font-size: 12px;
  margin-top: 6px;
}
.qr-preview {
  display: grid;
  justify-items: center;
  margin-top: 12px;
}
</style>
