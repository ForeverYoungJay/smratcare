<template>
  <PageContainer title="床态全景" subTitle="3D 楼栋视图：楼栋/楼层/房间/房型/床位联动">
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
        <a-card title="3D 床位全景" class="card-elevated" :bordered="false">
          <a-space wrap style="margin-bottom: 12px">
            <a-tag color="default">空闲 {{ stats.idle }}</a-tag>
            <a-tag color="processing">预定 {{ stats.reserved }}</a-tag>
            <a-tag color="green">在住 {{ stats.occupied }}</a-tag>
            <a-tag color="orange">维修 {{ stats.maintenance }}</a-tag>
            <a-tag color="cyan">清洁中 {{ stats.cleaning }}</a-tag>
            <a-tag color="red">锁定 {{ stats.locked }}</a-tag>
          </a-space>

          <div class="city-scene">
            <div v-for="building in buildingScenes" :key="building.key" class="building-shell">
              <div class="building-head">
                <div class="building-name">{{ building.name }}</div>
                <div class="building-kpi">{{ building.floors.length }} 层 · {{ building.roomCount }} 间 · {{ building.bedCount }} 床</div>
              </div>
              <div class="building-stack">
                <div v-for="floor in building.floors" :key="floor.key" class="floor-deck">
                  <div class="floor-axis">{{ floor.label }}</div>
                  <div class="room-lane">
                    <div v-for="room in floor.rooms" :key="room.key" class="room-cube">
                      <div class="room-title">{{ room.roomNo }}</div>
                      <div class="room-type">{{ room.roomType }}</div>
                      <div class="room-meta">{{ room.occupiedBeds }}/{{ room.totalBeds }} 床 · {{ room.elderCount }} 人</div>
                      <div class="bed-grid">
                        <button
                          v-for="bed in room.beds"
                          :key="bed.id"
                          type="button"
                          class="bed-pill"
                          :class="statusClass(resolveStatus(bed))"
                          @click="selectBed(bed)"
                        >
                          {{ bed.bedNo }}
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :xl="6">
        <a-card title="选中床位详情" class="card-elevated" :bordered="false">
          <a-empty v-if="!selectedBed" description="请先点击中间 3D 视图中的床位" />
          <template v-else>
            <a-descriptions :column="1" size="small" bordered>
              <a-descriptions-item label="床位">{{ selectedBed.bedNo || '-' }}</a-descriptions-item>
              <a-descriptions-item label="楼栋/楼层/房间">{{ selectedBed.building || '-' }} / {{ selectedBed.floorNo || '-' }} / {{ selectedBed.roomNo || '-' }}</a-descriptions-item>
              <a-descriptions-item label="在住长者">{{ selectedBed.elderName || '-' }}</a-descriptions-item>
              <a-descriptions-item label="护理等级">{{ selectedBed.careLevel || 'N2' }}</a-descriptions-item>
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
          </template>
        </a-card>
      </a-col>
    </a-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../../components/PageContainer.vue'
import { getBedMap, getRoomList } from '../../../api/bed'
import { useLiveSyncRefresh } from '../../../composables/useLiveSyncRefresh'
import type { BedItem, RoomItem } from '../../../types'

type TreeNode = { title: string; key: string; children?: TreeNode[] }
type RoomScene = {
  key: string
  roomNo: string
  roomType: string
  beds: BedItem[]
  totalBeds: number
  occupiedBeds: number
  elderCount: number
}
type FloorScene = { key: string; label: string; sort: number; rooms: RoomScene[] }
type BuildingScene = { key: string; name: string; floors: FloorScene[]; roomCount: number; bedCount: number }

const router = useRouter()
const beds = ref<BedItem[]>([])
const roomTypeMap = ref<Record<number, string>>({})
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

const buildingScenes = computed<BuildingScene[]>(() => {
  const buildingMap = new Map<string, Map<string, Map<string, BedItem[]>>>()
  filteredBeds.value.forEach((bed) => {
    const building = (bed.building || '未分配楼栋').trim() || '未分配楼栋'
    const floor = (bed.floorNo || '未知楼层').trim() || '未知楼层'
    const roomNo = (bed.roomNo || `房间-${bed.roomId || '-'}`).trim() || `房间-${bed.roomId || '-'}`

    if (!buildingMap.has(building)) buildingMap.set(building, new Map())
    const floorMap = buildingMap.get(building)!
    if (!floorMap.has(floor)) floorMap.set(floor, new Map())
    const roomMap = floorMap.get(floor)!
    if (!roomMap.has(roomNo)) roomMap.set(roomNo, [])
    roomMap.get(roomNo)!.push(bed)
  })

  return Array.from(buildingMap.entries()).map(([building, floorMap]) => {
    const floors: FloorScene[] = Array.from(floorMap.entries())
      .map(([floorNo, roomMap]) => {
        const rooms: RoomScene[] = Array.from(roomMap.entries()).map(([roomNo, roomBeds]) => {
          const totalBeds = roomBeds.length
          const occupiedBeds = roomBeds.filter((b) => !!b.elderId).length
          const elderCount = roomBeds.filter((b) => !!b.elderName).length
          const roomType = roomTypeMap.value[Number(roomBeds[0]?.roomId || 0)] || '标准间'
          return {
            key: `${building}-${floorNo}-${roomNo}`,
            roomNo,
            roomType,
            beds: roomBeds,
            totalBeds,
            occupiedBeds,
            elderCount
          }
        })

        const sort = Number(String(floorNo).replace(/[^0-9.-]/g, ''))
        return {
          key: `${building}-${floorNo}`,
          label: floorNo,
          sort: Number.isNaN(sort) ? 0 : sort,
          rooms
        }
      })
      .sort((a, b) => b.sort - a.sort)

    return {
      key: building,
      name: building,
      floors,
      roomCount: floors.reduce((sum, floor) => sum + floor.rooms.length, 0),
      bedCount: floors.reduce((sum, floor) => sum + floor.rooms.reduce((s, room) => s + room.totalBeds, 0), 0)
    }
  })
})

function resolveStatus(bed: BedItem): '空闲' | '预定' | '在住' | '维修' | '清洁中' | '锁定' {
  if (bed.elderId) return '在住'
  if (bed.status === 0) return '锁定'
  if (bed.status === 2) return '维修'
  if (bed.status === 3) return '清洁中'
  if (String(bed.bedNo || '').endsWith('R')) return '预定'
  return '空闲'
}

function statusClass(status: string) {
  if (status === '在住') return 'is-occupied'
  if (status === '预定') return 'is-reserved'
  if (status === '维修') return 'is-maintenance'
  if (status === '清洁中') return 'is-cleaning'
  if (status === '锁定') return 'is-locked'
  return 'is-idle'
}

function onTreeSelect(keys: string[]) {
  selectedKeys.value = keys
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

async function loadBeds() {
  beds.value = await getBedMap()
}

async function loadRooms() {
  const rooms: RoomItem[] = await getRoomList()
  const map: Record<number, string> = {}
  rooms.forEach((item) => {
    map[item.id] = item.roomType || '标准间'
  })
  roomTypeMap.value = map
}

useLiveSyncRefresh({
  topics: ['elder', 'bed', 'lifecycle', 'finance', 'care', 'dining'],
  refresh: () => {
    loadBeds()
    loadRooms()
  }
})

onMounted(async () => {
  await Promise.all([loadBeds(), loadRooms()])
})
</script>

<style scoped>
.city-scene {
  display: grid;
  gap: 14px;
}

.building-shell {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: linear-gradient(160deg, #f8fbff 0%, #ffffff 70%);
  padding: 12px;
}

.building-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 10px;
}

.building-name {
  font-weight: 700;
  color: #1f2937;
}

.building-kpi {
  color: #6b7280;
  font-size: 12px;
}

.building-stack {
  display: grid;
  gap: 10px;
}

.floor-deck {
  display: grid;
  grid-template-columns: 56px 1fr;
  gap: 8px;
  align-items: start;
}

.floor-axis {
  font-weight: 600;
  color: #475569;
  background: #eef2ff;
  border-radius: 8px;
  text-align: center;
  padding: 8px 4px;
}

.room-lane {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 10px;
}

.room-cube {
  border: 1px solid #dbeafe;
  border-radius: 10px;
  background: #ffffff;
  padding: 8px;
  box-shadow: 0 5px 12px rgba(15, 23, 42, 0.05);
}

.room-title {
  font-weight: 600;
  color: #0f172a;
}

.room-type {
  font-size: 12px;
  color: #2563eb;
}

.room-meta {
  font-size: 12px;
  color: #6b7280;
  margin: 4px 0 6px;
}

.bed-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.bed-pill {
  border: 0;
  border-radius: 8px;
  padding: 4px 8px;
  font-size: 12px;
  cursor: pointer;
}

.is-idle {
  background: #f3f4f6;
  color: #111827;
}

.is-reserved {
  background: #dbeafe;
  color: #1d4ed8;
}

.is-occupied {
  background: #dcfce7;
  color: #166534;
}

.is-maintenance {
  background: #ffedd5;
  color: #9a3412;
}

.is-cleaning {
  background: #cffafe;
  color: #0e7490;
}

.is-locked {
  background: #fee2e2;
  color: #991b1b;
}
</style>
