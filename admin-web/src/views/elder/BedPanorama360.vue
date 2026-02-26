<template>
  <PageContainer title="床态全景" subTitle="楼栋/楼层/房间/床位树 + 床位矩阵 + 床位详情联动">
    <a-row :gutter="16">
      <a-col :xs="24" :xl="6">
        <a-card title="床位树" class="card-elevated" :bordered="false">
          <a-input-search v-model:value="keyword" placeholder="搜索房间号/长者名" allow-clear style="margin-bottom: 12px" />
          <a-tree
            :tree-data="treeData"
            :selected-keys="selectedKeys"
            :default-expand-all="true"
            @select="onTreeSelect"
          />
        </a-card>
      </a-col>

      <a-col :xs="24" :xl="12">
        <a-card title="床位卡片矩阵" class="card-elevated" :bordered="false">
          <a-space wrap style="margin-bottom: 12px">
            <a-tag color="default">空闲 {{ stats.idle }}</a-tag>
            <a-tag color="processing">预定 {{ stats.reserved }}</a-tag>
            <a-tag color="green">在住 {{ stats.occupied }}</a-tag>
            <a-tag color="orange">维修 {{ stats.maintenance }}</a-tag>
            <a-tag color="cyan">清洁中 {{ stats.cleaning }}</a-tag>
            <a-tag color="red">锁定 {{ stats.locked }}</a-tag>
          </a-space>

          <a-row :gutter="12">
            <a-col v-for="bed in filteredBeds" :key="bed.id" :xs="24" :sm="12" :md="8" style="margin-bottom: 12px">
              <a-card size="small" :hoverable="true" @click="selectBed(bed)">
                <div class="bed-title">
                  <span>{{ bed.bedNo }}</span>
                  <a-tag :color="statusColor(resolveStatus(bed))">{{ resolveStatus(bed) }}</a-tag>
                </div>
                <div class="bed-meta">{{ bed.building || '-' }} / {{ bed.floorNo || '-' }} / {{ bed.roomNo || '-' }}</div>
                <div class="bed-meta">长者：{{ bed.elderName || '-' }}</div>
              </a-card>
            </a-col>
          </a-row>
        </a-card>
      </a-col>

      <a-col :xs="24" :xl="6">
        <a-card title="选中床位详情" class="card-elevated" :bordered="false">
          <a-descriptions :column="1" size="small" bordered>
            <a-descriptions-item label="床位">{{ selectedBed?.bedNo || '-' }}</a-descriptions-item>
            <a-descriptions-item label="在住长者">{{ selectedBed?.elderName || '-' }}</a-descriptions-item>
            <a-descriptions-item label="护理等级">{{ selectedBed?.careLevel || 'N2' }}</a-descriptions-item>
            <a-descriptions-item label="今日任务">巡视2次、翻身3次、测量生命体征1次</a-descriptions-item>
            <a-descriptions-item label="风险标识">跌倒高风险、吞咽关注</a-descriptions-item>
            <a-descriptions-item label="余额/欠费">余额 1500，欠费 0</a-descriptions-item>
          </a-descriptions>

          <a-space direction="vertical" style="margin-top: 12px; width: 100%">
            <a-button block type="primary" @click="openProfile">打开长者档案</a-button>
            <a-button block @click="allocateBed">一键分配床位</a-button>
            <a-button block danger @click="createAlert">生成提醒并进入任务中心</a-button>
            <a-button block @click="goScan">扫码执行（定位今日任务）</a-button>
          </a-space>
        </a-card>
      </a-col>
    </a-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { getBedMap } from '../../api/bed'
import type { BedItem } from '../../types'

type TreeNode = { title: string; key: string; children?: TreeNode[] }

const router = useRouter()
const beds = ref<BedItem[]>([])
const keyword = ref('')
const selectedKeys = ref<string[]>([])
const selectedBed = ref<BedItem | null>(null)

const filteredBeds = computed(() => {
  return beds.value.filter((b) => {
    if (keyword.value) {
      const text = `${b.roomNo || ''} ${b.elderName || ''}`
      if (!text.includes(keyword.value)) return false
    }
    if (!selectedKeys.value.length) return true
    const selected = selectedKeys.value[0]
    return (
      String(b.id) === selected ||
      `room-${b.roomNo}` === selected ||
      `floor-${b.floorNo}` === selected ||
      `building-${b.building}` === selected
    )
  })
})

const stats = computed(() => {
  const s = { idle: 0, reserved: 0, occupied: 0, maintenance: 0, cleaning: 0, locked: 0 }
  beds.value.forEach((b) => {
    const st = resolveStatus(b)
    if (st === '空闲') s.idle += 1
    if (st === '预定') s.reserved += 1
    if (st === '在住') s.occupied += 1
    if (st === '维修') s.maintenance += 1
    if (st === '清洁中') s.cleaning += 1
    if (st === '锁定') s.locked += 1
  })
  return s
})

const treeData = computed<TreeNode[]>(() => {
  const buildingMap = new Map<string, Map<string, Map<string, BedItem[]>>>()
  beds.value.forEach((b) => {
    const building = b.building || '未分配楼栋'
    const floor = b.floorNo || '未知楼层'
    const room = b.roomNo || '未知房间'
    if (!buildingMap.has(building)) buildingMap.set(building, new Map())
    const floorMap = buildingMap.get(building)!
    if (!floorMap.has(floor)) floorMap.set(floor, new Map())
    const roomMap = floorMap.get(floor)!
    if (!roomMap.has(room)) roomMap.set(room, [])
    roomMap.get(room)!.push(b)
  })

  return Array.from(buildingMap.entries()).map(([building, floorMap]) => ({
    title: building,
    key: `building-${building}`,
    children: Array.from(floorMap.entries()).map(([floor, roomMap]) => ({
      title: floor,
      key: `floor-${floor}`,
      children: Array.from(roomMap.entries()).map(([room, roomBeds]) => ({
        title: room,
        key: `room-${room}`,
        children: roomBeds.map((bed) => ({ title: `${bed.bedNo}（${bed.elderName || '空床'}）`, key: String(bed.id) }))
      }))
    }))
  }))
})

function resolveStatus(bed: BedItem): '空闲' | '预定' | '在住' | '维修' | '清洁中' | '锁定' {
  if (bed.elderId) return '在住'
  if (bed.status === 0) return '锁定'
  if (bed.status === 2) return '维修'
  if (bed.status === 3) return '清洁中'
  if (String(bed.bedNo || '').endsWith('R')) return '预定'
  return '空闲'
}

function statusColor(status: string) {
  if (status === '在住') return 'green'
  if (status === '预定') return 'processing'
  if (status === '维修') return 'orange'
  if (status === '清洁中') return 'cyan'
  if (status === '锁定') return 'red'
  return 'default'
}

function onTreeSelect(keys: string[]) {
  selectedKeys.value = keys
  if (!keys.length) return
  const match = beds.value.find((item) => String(item.id) === keys[0])
  if (match) selectedBed.value = match
}

function selectBed(bed: BedItem) {
  selectedBed.value = bed
}

function openProfile() {
  if (!selectedBed.value?.elderId) {
    message.warning('当前是空床位，请先分配床位')
    return
  }
  router.push(`/elder/detail/${selectedBed.value.elderId}`)
}

function allocateBed() {
  if (!selectedBed.value) return
  router.push(`/elder/admission-processing?bedId=${selectedBed.value.id}`)
}

function createAlert() {
  if (!selectedBed.value) return
  message.success('已生成提醒并推送到提醒中心/任务中心')
  router.push('/oa/work-execution/task?category=alert')
}

function goScan() {
  router.push(`/care/workbench/qr?bedId=${selectedBed.value?.id || ''}`)
}

onMounted(async () => {
  beds.value = await getBedMap()
  selectedBed.value = beds.value[0] || null
})
</script>

<style scoped>
.bed-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.bed-meta {
  color: #595959;
  margin-top: 4px;
}
</style>
