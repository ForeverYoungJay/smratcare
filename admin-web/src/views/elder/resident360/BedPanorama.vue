<template>
  <PageContainer class="bed-immersive-page" title="3D床态全景" subTitle="智慧养老数字孪生床态指挥舱">
    <div class="immersive-stage-shell">
      <Panorama3D
        v-if="matrixBuildings.length && matrixFloors.length"
        :buildings="matrixBuildings"
        :floors="matrixFloors"
        :room-lookup="roomSceneLookup"
        @click-room="openRoomDetail"
        @click-bed="selectBed"
      />
      <a-empty v-else class="stage-empty" description="暂无床位数据" />

      <div class="scene-vignette"></div>
      <div class="scene-glow scene-glow-left"></div>
      <div class="scene-glow scene-glow-right"></div>

      <header class="hud hud-topbar">
        <div class="hud-topbar__brand">
          <div class="brand-mark"><span></span></div>
          <div class="brand-copy">
            <div class="brand-kicker">Smart Senior Care Digital Twin</div>
            <strong>智慧养老数字孪生床态指挥舱</strong>
            <small>{{ lifecycleContext.active ? '入住联动模式已开启' : '长者床态、风险提醒与护理节奏实时联动中' }}</small>
          </div>
        </div>

        <div class="hud-topbar__metrics">
          <button v-for="item in overviewCards" :key="item.label" class="metric-pill" :class="item.tone">
            <span>{{ item.label }}</span>
            <strong><AnimatedMetricNumber :value="item.numericValue" :suffix="item.suffix || ''" /></strong>
            <small>{{ item.meta }}</small>
          </button>
        </div>

        <div class="hud-topbar__ops">
          <div class="status-stack">
            <span class="status-dot"></span>
            <div>
              <strong>系统平稳运行</strong>
              <small>{{ commandCenterStatus }}</small>
            </div>
          </div>
          <div class="clock-stack">
            <span>{{ currentDateText }}</span>
            <strong>{{ currentTimeText }}</strong>
          </div>
          <div class="operator-stack">
            <a-avatar class="operator-avatar">{{ operatorInitial }}</a-avatar>
            <div>
              <strong>{{ operatorName }}</strong>
              <small>{{ operatorRole }}</small>
            </div>
          </div>
        </div>
      </header>

      <aside class="hud hud-left">
        <section class="hud-panel">
          <div class="hud-panel__head">
            <div>
              <span class="panel-kicker">运行总览</span>
              <strong>床态守护概况</strong>
            </div>
            <small>{{ roomCount }} 间房 / {{ sourceBeds.length }} 张床位</small>
          </div>

          <div class="overview-list">
            <div v-for="item in leftStatCards" :key="item.label" class="overview-row">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
              <small>{{ item.meta }}</small>
            </div>
          </div>

          <div class="status-bars">
            <div v-for="item in statusDistributionRows" :key="item.label" class="status-bar">
              <div class="status-bar__top">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </div>
              <div class="distribution-track">
                <span class="distribution-fill" :class="item.tone" :style="{ width: `${item.percent}%` }"></span>
              </div>
            </div>
          </div>
        </section>

        <section class="hud-panel hud-panel--compact">
          <div class="hud-panel__head">
            <div>
              <span class="panel-kicker">场景控制</span>
              <strong>楼栋与床态筛选</strong>
            </div>
          </div>

          <label class="field-block">
            <span class="field-label">搜索目标</span>
            <a-input-search v-model:value="keyword" placeholder="搜索房间号 / 长者名" allow-clear />
          </label>

          <div class="field-block">
            <span class="field-label">床位状态筛选</span>
            <a-segmented v-model:value="quickFilter" :options="quickFilterOptions" block />
          </div>

          <div class="field-block">
            <div class="field-inline">
              <span class="field-label">风险筛选</span>
              <a-switch v-model:checked="riskFilterEnabled" />
            </div>
            <a-segmented
              v-if="riskFilterEnabled"
              v-model:value="riskFilterLevel"
              :options="riskLevelOptions"
              block
            />
          </div>

          <div class="field-block">
            <span class="field-label">楼栋切换</span>
            <div class="scope-chip-list">
              <button
                v-for="item in buildingScopeOptions"
                :key="item"
                class="scope-chip"
                :class="{ active: item === selectedBuilding }"
                @click="setBuildingScope(item)"
              >
                {{ item }}
              </button>
            </div>
          </div>

          <div class="field-block" v-if="floorScopeOptions.length">
            <span class="field-label">楼层切换</span>
            <div class="scope-chip-list compact">
              <button
                v-for="item in floorScopeOptions"
                :key="item"
                class="scope-chip"
                :class="{ active: item === selectedFloor }"
                @click="setFloorScope(item)"
              >
                {{ item }}
              </button>
            </div>
          </div>

          <div class="scene-actions">
            <a-button @click="openBedMap">房态视图</a-button>
            <a-button @click="openBedManage">床位管理</a-button>
            <a-button type="primary" @click="resetFilters">重置场景</a-button>
          </div>
        </section>
      </aside>

      <div class="hud hud-center">
        <section class="hud-panel hud-panel--spotlight">
          <div class="hud-panel__head">
            <div>
              <span class="panel-kicker">当前聚焦</span>
              <strong>{{ focusHeadline.title }}</strong>
            </div>
            <small>{{ focusHeadline.meta }}</small>
          </div>

          <div class="spotlight-meta">
            <div class="spotlight-block">
              <span>当前范围</span>
              <strong>{{ bedGuardSubject }}</strong>
            </div>
            <div class="spotlight-block">
              <span>护理阶段</span>
              <strong>{{ bedGuardStageText }}</strong>
            </div>
          </div>

          <div class="overlay-progress">
            <div
              v-for="(step, index) in bedGuardSteps"
              :key="step"
              class="overlay-step"
              :class="{ active: index <= bedGuardCurrentIndex, current: index === bedGuardCurrentIndex }"
            >
              <span>{{ index + 1 }}</span>
              <small>{{ step }}</small>
            </div>
          </div>

          <div class="spotlight-actions">
            <button
              v-for="item in stageQuickActions"
              :key="item.key"
              class="stage-action-pill"
              :class="item.tone"
              @click="handleCommandAction(item.key)"
            >
              <strong>{{ item.label }}</strong>
              <span>{{ item.description }}</span>
            </button>
          </div>
        </section>
      </div>

      <aside class="hud hud-right">
        <section class="hud-panel hud-panel--detail" v-if="selectedBed">
          <div class="hud-panel__head">
            <div>
              <span class="panel-kicker">床位详情</span>
              <strong>{{ selectedBed.elderName || '空床待命' }}</strong>
            </div>
            <small>{{ selectedBed.roomNo || '-' }} / {{ selectedBed.bedNo || '-' }}</small>
          </div>

          <div class="detail-tags">
            <span class="overlay-chip">{{ resolveStatus(selectedBed) }}</span>
            <span class="overlay-chip" v-if="selectedBed.riskLabel">{{ selectedBed.riskLabel }}</span>
            <span class="overlay-chip">{{ selectedBed.careLevel || '未配置护理等级' }}</span>
          </div>

          <div class="detail-grid">
            <div>
              <span>楼栋楼层</span>
              <strong>{{ selectedBed.building || '-' }} / {{ selectedBed.floorNo || '-' }}</strong>
            </div>
            <div>
              <span>24h异常</span>
              <strong>{{ selectedBed.abnormalVital24hCount || 0 }} 次</strong>
            </div>
            <div>
              <span>风险来源</span>
              <strong>{{ selectedBed.riskSource || '守护稳定' }}</strong>
            </div>
            <div>
              <span>最近评估</span>
              <strong>{{ formatLatestAssessment(selectedBed) }}</strong>
            </div>
          </div>

          <div v-if="bedGuardBlockers.length" class="blocker-list">
            <div v-for="item in bedGuardBlockers" :key="item.code" class="blocker-card">
              <div>
                <strong>{{ item.code }}</strong>
                <p>{{ item.text }}</p>
              </div>
              <a-button
                v-if="item.actionKey && item.actionLabel"
                size="small"
                type="link"
                @click="handleBedGuardAction(item)"
              >
                {{ item.actionLabel }}
              </a-button>
            </div>
          </div>

          <div class="action-grid">
            <button
              v-for="item in commandDeck"
              :key="item.key"
              class="action-card"
              :class="item.tone"
              @click="handleCommandAction(item.key)"
            >
              <strong>{{ item.label }}</strong>
              <span>{{ item.description }}</span>
            </button>
          </div>
        </section>

        <template v-else>
          <section class="hud-panel hud-panel--compact">
            <div class="hud-panel__head">
              <div>
                <span class="panel-kicker">实时风险提醒</span>
                <strong>值守事件流</strong>
              </div>
              <small>{{ concernCount }} 条提醒</small>
            </div>
            <div class="event-list">
              <div v-for="item in alertFeed.slice(0, 4)" :key="item.key" class="event-card" :class="item.tone">
                <div class="event-top">
                  <span class="event-type">{{ item.type }}</span>
                  <span class="event-time">{{ item.time }}</span>
                </div>
                <strong>{{ item.title }}</strong>
                <p>{{ item.description }}</p>
              </div>
            </div>
          </section>

          <section class="hud-panel hud-panel--compact">
            <div class="hud-panel__head">
              <div>
                <span class="panel-kicker">AI护理建议</span>
                <strong>值守提示</strong>
              </div>
            </div>
            <div class="timeline-list">
              <div v-for="item in aiSuggestionFeed" :key="item.key" class="timeline-item">
                <span class="timeline-dot" :class="item.tone"></span>
                <div>
                  <strong>{{ item.title }}</strong>
                  <p>{{ item.description }}</p>
                </div>
              </div>
            </div>
          </section>

          <section class="hud-panel hud-panel--compact">
            <div class="hud-panel__head">
              <div>
                <span class="panel-kicker">今日重点关注</span>
                <strong>长者守护名单</strong>
              </div>
            </div>
            <div class="focus-list">
              <button v-for="bed in focusBeds" :key="bed.id" class="focus-row" @click="selectBed(bed)">
                <div>
                  <strong>{{ bed.elderName || '空床待命' }}</strong>
                  <small>{{ bed.roomNo || '-' }} / {{ bed.bedNo || '-' }}</small>
                </div>
                <span class="focus-status">{{ bed.riskLabel || resolveStatus(bed) }}</span>
              </button>
            </div>
          </section>
        </template>
      </aside>

      <footer class="hud hud-bottom">
        <section class="trend-panel">
          <div class="trend-card">
            <div class="trend-head">
              <span>24小时床态趋势</span>
              <strong>在住与空床节奏</strong>
            </div>
            <v-chart class="chart-view" :option="occupancyTrendOption" autoresize />
          </div>
          <div class="trend-card">
            <div class="trend-head">
              <span>夜间离床统计</span>
              <strong>夜巡观察参考</strong>
            </div>
            <v-chart class="chart-view" :option="sleepTrendOption" autoresize />
          </div>
          <div class="trend-card">
            <div class="trend-head">
              <span>护理响应效率</span>
              <strong>风险任务闭环</strong>
            </div>
            <v-chart class="chart-view" :option="alertTrendOption" autoresize />
          </div>
          <div class="trend-card">
            <div class="trend-head">
              <span>风险热力趋势</span>
              <strong>守护在线状态</strong>
            </div>
            <v-chart class="chart-view" :option="deviceTrendOption" autoresize />
          </div>
        </section>
      </footer>
    </div>

    <a-modal v-model:open="detailOpen" title="床位详情" width="560px" :footer="null" destroy-on-close>
      <a-descriptions v-if="selectedBed" :column="1" size="small" bordered>
        <a-descriptions-item label="床位">{{ selectedBed.bedNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="楼栋/楼层/房间">{{ selectedBed.building || '-' }} / {{ selectedBed.floorNo || '-' }} / {{ selectedBed.roomNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="在住长者">{{ selectedBed.elderName || '-' }}</a-descriptions-item>
        <a-descriptions-item label="护理等级">{{ selectedBed.careLevel || '-' }}</a-descriptions-item>
        <a-descriptions-item label="风险级别">{{ bedRiskLabel(selectedBed) || '无' }}</a-descriptions-item>
        <a-descriptions-item label="风险来源">{{ selectedBed.riskSource || '-' }}</a-descriptions-item>
        <a-descriptions-item label="最近评估">{{ formatLatestAssessment(selectedBed) }}</a-descriptions-item>
        <a-descriptions-item label="24h异常体征">{{ selectedBed.abnormalVital24hCount || 0 }} 次</a-descriptions-item>
        <a-descriptions-item label="今日任务">巡视2次、翻身3次、测量生命体征1次</a-descriptions-item>
        <a-descriptions-item label="余额/欠费">余额 1500，欠费 0</a-descriptions-item>
      </a-descriptions>

      <a-space direction="vertical" style="margin-top: 12px; width: 100%">
        <a-button block type="primary" @click="openProfile">打开长者档案</a-button>
        <a-button block @click="allocateBed">一键分配床位</a-button>
        <a-button block @click="openAssessmentArchive">查看评估档案</a-button>
        <a-button block @click="openContractsInvoices">合同与票据</a-button>
        <a-button block @click="openStatusChangeCenter">状态变更中心</a-button>
        <a-button block danger @click="createAlert">生成提醒并进入任务中心</a-button>
        <a-button block @click="goScan">扫码执行（定位今日任务）</a-button>
      </a-space>
    </a-modal>

    <a-modal v-model:open="roomDetailOpen" :title="`房间详情 · ${selectedRoom?.roomNo || '-'}`" width="760px" :footer="null" destroy-on-close>
      <a-descriptions bordered size="small" :column="2" style="margin-bottom: 12px">
        <a-descriptions-item label="房型">{{ resolveRoomTypeLabel(selectedRoom?.roomType) }}</a-descriptions-item>
        <a-descriptions-item label="容量">{{ selectedRoom?.capacity || 0 }} 床</a-descriptions-item>
        <a-descriptions-item label="在住人数">{{ selectedRoom?.elderCount || 0 }} 人</a-descriptions-item>
        <a-descriptions-item label="空床">{{ selectedRoom?.emptyBeds || 0 }} 床</a-descriptions-item>
        <a-descriptions-item label="公开备注" :span="2">{{ resolveVisibleRemark(selectedRoom?.remark) || '-' }}</a-descriptions-item>
        <a-descriptions-item label="全部备注" :span="2">{{ resolveAllRemark(selectedRoom?.remark) || '-' }}</a-descriptions-item>
      </a-descriptions>

      <a-table
        :loading="roomResidentLoading"
        :data-source="roomResidents"
        :pagination="false"
        :row-key="(item:any) => item.id"
        size="small"
      >
        <a-table-column title="头像" key="avatar" width="70">
          <template #default="{ record }">
            <a-avatar style="background-color: #1677ff">{{ String(record?.fullName || '?').slice(-1) }}</a-avatar>
          </template>
        </a-table-column>
        <a-table-column title="姓名" data-index="fullName" key="fullName" width="110" />
        <a-table-column title="生日" data-index="birthDate" key="birthDate" width="120" />
        <a-table-column title="家庭住址" data-index="homeAddress" key="homeAddress" />
        <a-table-column title="备注" data-index="remark" key="remark" />
        <a-table-column title="操作" key="action" width="180">
          <template #default="{ record }">
            <a-space>
              <a-button type="link" size="small" @click="openElderProfile(record.id)">详情</a-button>
              <a-button type="link" size="small" @click="openResidentBills(record.id)">账单</a-button>
            </a-space>
          </template>
        </a-table-column>
      </a-table>
      <a-empty v-if="!roomResidentLoading && !roomResidents.length" description="当前房间暂无入住长者" />
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../../components/PageContainer.vue'
import AnimatedMetricNumber from '../../../components/dashboard/AnimatedMetricNumber.vue'
import Panorama3D from './Panorama3D.vue'
import { getBedMap, getRoomList } from '../../../api/bed'
import { getElderDetail } from '../../../api/elder'
import { useLiveSyncRefresh } from '../../../composables/useLiveSyncRefresh'
import { useUserStore } from '../../../stores/user'
import type { BedItem, ElderItem, RoomItem } from '../../../types'

type RoomScene = {
  key: string
  roomNo: string
  roomType: string
  capacity: number
  autoSpan: 1 | 2
  beds: BedItem[]
  totalBeds: number
  occupiedBeds: number
  elderCount: number
  emptyBeds: number
  remark?: string
}

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const beds = ref<BedItem[]>([])
const roomTypeMap = ref<Record<string, string>>({})
const roomCapacityMap = ref<Record<string, number>>({})
const keyword = ref('')
const selectedBed = ref<BedItem | null>(null)
const detailOpen = ref(false)
const roomDetailOpen = ref(false)
const selectedRoom = ref<RoomScene | null>(null)
const roomResidents = ref<ElderItem[]>([])
const roomResidentLoading = ref(false)
const quickFilter = ref<'ALL' | 'IDLE' | 'OCCUPIED'>('ALL')
const riskFilterEnabled = ref(false)
const riskFilterLevel = ref<'ALL' | 'HIGH' | 'MEDIUM' | 'LOW'>('ALL')
const riskDataLoading = ref(false)
const riskDataReady = ref(false)
const selectedBuilding = ref('')
const selectedFloor = ref('')
const currentTime = ref(dayjs())
const quickFilterOptions = [
  { label: '全部', value: 'ALL' },
  { label: '仅空床', value: 'IDLE' },
  { label: '仅入住', value: 'OCCUPIED' }
]
const riskLevelOptions = [
  { label: '全部风险', value: 'ALL' },
  { label: '高风险', value: 'HIGH' },
  { label: '中风险', value: 'MEDIUM' },
  { label: '低风险', value: 'LOW' }
]
const bedGuardSteps = ['合同签署入院', '入住选床完成', '在住照护执行', '风险干预闭环']
const BED_ROUTE_KEYS = ['bedBuilding', 'bedFloor', 'bedKeyword', 'bedQuick', 'bedRiskEnabled', 'bedRiskLevel'] as const
const bedRouteSignature = ref('')
const skipNextBedRouteWatch = ref(false)
let clockTimer: ReturnType<typeof window.setInterval> | null = null

const lifecycleContext = computed(() => {
  const source = String(route.query.source || '').trim().toLowerCase()
  const scene = String(route.query.scene || '').trim().toLowerCase()
  const active = source === 'lifecycle' || scene === 'status-change'
  return {
    active,
    message: active ? '当前来自入住状态变更联动，可在床态视图快速确认清洁/维修与空床调配。' : ''
  }
})

const filteredBeds = computed(() => beds.value.filter((b) => {
  if (keyword.value) {
    const text = `${b.roomNo || ''} ${b.elderName || ''}`.toLowerCase()
    const searchText = keyword.value.toLowerCase()
    if (!text.includes(searchText)) return false
  }
  return true
}))

const sourceBeds = computed(() => {
  const bedsByQuick = filteredBeds.value.filter((item) => {
    if (quickFilter.value === 'IDLE') return isEmptyBed(item)
    if (quickFilter.value === 'OCCUPIED') return !!item.elderId
    return true
  })
  if (!riskFilterEnabled.value || !riskDataReady.value) return bedsByQuick
  return bedsByQuick.filter((item) => {
    if (!item.riskLevel) return false
    if (riskFilterLevel.value === 'ALL') return true
    return item.riskLevel === riskFilterLevel.value
  })
})

const scopedBeds = computed(() => sourceBeds.value.filter((item) => {
  if (selectedBuilding.value && String(item.building || '') !== selectedBuilding.value) return false
  if (selectedFloor.value && String(item.floorNo || '') !== selectedFloor.value) return false
  return true
}))

const buildingList = computed(() => {
  const set = new Set<string>()
  scopedBeds.value.forEach((item) => set.add((item.building || '未分配楼栋').trim() || '未分配楼栋'))
  return Array.from(set).sort((a, b) => a.localeCompare(b, 'zh-CN'))
})

const matrixBuildings = computed(() => buildingList.value)

const floorList = computed(() => {
  const set = new Set<string>()
  scopedBeds.value.forEach((item) => set.add((item.floorNo || '未知楼层').trim() || '未知楼层'))
  return Array.from(set).sort((a, b) => {
    const diff = floorSortValue(b) - floorSortValue(a)
    if (diff !== 0) return diff
    return String(a).localeCompare(String(b), 'zh-CN')
  })
})

const matrixFloors = computed(() => floorList.value)
const roomCount = computed(() => Array.from(roomSceneLookup.value.values()).reduce((sum, rooms) => sum + rooms.length, 0))

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

const alertBeds = computed(() => [...sourceBeds.value]
  .filter((bed) => isAlertBed(bed))
  .sort((a, b) => Number(b.riskLevel === 'HIGH') - Number(a.riskLevel === 'HIGH') || Number(b.abnormalVital24hCount || 0) - Number(a.abnormalVital24hCount || 0)))

const emergencyCount = computed(() => alertBeds.value.filter((bed) => bed.riskLevel === 'HIGH' || bed.status === 0).length)
const concernCount = computed(() => alertBeds.value.length)
const occupiedRate = computed(() => beds.value.length ? Math.round((stats.value.occupied / beds.value.length) * 100) : 0)
const deviceOnlineRate = computed(() => beds.value.length ? Math.max(0, Math.round(((beds.value.length - stats.value.locked - stats.value.maintenance) / beds.value.length) * 100)) : 100)
const sleepStableCount = computed(() => beds.value.filter((bed) => bed.elderId && bed.riskLevel !== 'HIGH' && Number(bed.abnormalVital24hCount || 0) === 0).length)
const awayObservationCount = computed(() => beds.value.filter((bed) => bed.riskLevel === 'MEDIUM' || bed.occupancySource === 'CLEANING').length)
const aiFocusCount = computed(() => beds.value.filter((bed) => bed.riskLevel === 'MEDIUM' || bed.riskSource).length)

const roomSceneLookup = computed(() => {
  const lookup = new Map<string, RoomScene[]>()
  const roomMap = new Map<string, BedItem[]>()
  scopedBeds.value.forEach((bed) => {
    const building = (bed.building || '未分配楼栋').trim() || '未分配楼栋'
    const floor = (bed.floorNo || '未知楼层').trim() || '未知楼层'
    const roomNo = (bed.roomNo || `房间-${bed.roomId || '-'}`).trim() || `房间-${bed.roomId || '-'}`
    const key = `${building}@@${floor}@@${roomNo}`
    if (!roomMap.has(key)) roomMap.set(key, [])
    roomMap.get(key)!.push(bed)
  })

  roomMap.forEach((roomBeds, key) => {
    const [building, floor, roomNo] = key.split('@@')
    const lookupKey = `${building}@@${floor}`
    if (!lookup.has(lookupKey)) lookup.set(lookupKey, [])
    const roomId = String(roomBeds[0]?.roomId || '')
    const sortedBeds = [...roomBeds].sort((a, b) => {
      const aEmpty = isEmptyBed(a) ? 0 : 1
      const bEmpty = isEmptyBed(b) ? 0 : 1
      if (aEmpty !== bEmpty) return aEmpty - bEmpty
      return String(a.bedNo || '').localeCompare(String(b.bedNo || ''))
    })
    const totalBeds = roomBeds.length
    const occupiedBeds = roomBeds.filter((b) => !!b.elderId).length
    const elderCount = roomBeds.filter((b) => !!b.elderName).length
    const emptyBeds = totalBeds - occupiedBeds
    const capacity = roomCapacityMap.value[roomId] || totalBeds || 1
    const autoSpan: 1 | 2 = capacity >= 3 ? 2 : 1
    lookup.get(lookupKey)!.push({
      key,
      roomNo,
      roomType: resolveRoomTypeLabel(roomTypeMap.value[roomId] || '标准间'),
      capacity,
      autoSpan,
      beds: sortedBeds,
      totalBeds,
      occupiedBeds,
      elderCount,
      emptyBeds,
      remark: roomBeds[0]?.roomRemark
    })
  })

  lookup.forEach((rooms) => {
    rooms.sort((a, b) => {
      if (a.capacity !== b.capacity) return b.capacity - a.capacity
      if (a.emptyBeds !== b.emptyBeds) return b.emptyBeds - a.emptyBeds
      return String(a.roomNo).localeCompare(String(b.roomNo))
    })
  })
  return lookup
})

const currentDateText = computed(() => currentTime.value.format('YYYY年MM月DD日 dddd'))
const currentTimeText = computed(() => currentTime.value.format('HH:mm:ss'))
const operatorName = computed(() => {
  const realName = String(userStore.staffInfo?.realName || '').trim()
  if (realName) return realName
  const username = String(userStore.staffInfo?.username || '').trim()
  if (username) return username
  return '值班护理长'
})
const operatorRole = computed(() => {
  const role = String((userStore.roles || [])[0] || '').trim()
  return role || '智慧床态调度员'
})
const operatorInitial = computed(() => operatorName.value.slice(0, 1) || '护')
const commandCenterStatus = computed(() => lifecycleContext.value.active ? '入住状态联动中' : '全院床态守护中')

const overviewCards = computed(() => ([
  { label: '总床位数', numericValue: beds.value.length, meta: `${matrixBuildings.value.length} 栋 ${matrixFloors.value.length} 层`, tone: 'tone-cyan' },
  { label: '在住长者', numericValue: stats.value.occupied, meta: `在住率 ${occupiedRate.value}%`, tone: 'tone-green' },
  { label: '空床位', numericValue: stats.value.idle, meta: `可立即调配 ${Math.max(0, stats.value.idle - stats.value.cleaning)} 张`, tone: 'tone-gray' },
  { label: '今日需关注', numericValue: concernCount.value, meta: `${emergencyCount.value} 条高优先提醒`, tone: 'tone-red' },
  { label: '守护设备在线率', numericValue: deviceOnlineRate.value, suffix: '%', meta: `在线设备 ${Math.max(0, beds.value.length - stats.value.maintenance - stats.value.locked)}`, tone: 'tone-blue' }
]))

const leftStatCards = computed(() => ([
  { label: '空床位', value: stats.value.idle, meta: '待迎接入住', tone: 'tone-gray' },
  { label: '在住长者', value: stats.value.occupied, meta: '持续守护中', tone: 'tone-cyan' },
  { label: '离床观察', value: awayObservationCount.value, meta: '夜巡重点', tone: 'tone-orange' },
  { label: '需关注长者', value: concernCount.value, meta: '风险与异常联动', tone: 'tone-red' },
  { label: '维修设备', value: stats.value.maintenance, meta: '待恢复上线', tone: 'tone-orange' },
  { label: '睡眠稳定', value: sleepStableCount.value, meta: '夜间状态平稳', tone: 'tone-deep-blue' }
]))

const buildingScopeOptions = computed(() => matrixBuildings.value.slice(0, 6))

const floorScopeOptions = computed(() => {
  if (selectedBuilding.value) {
    return floorList.value.slice(0, 8)
  }
  return matrixFloors.value.slice(0, 8)
})

const statusDistributionRows = computed(() => {
  const total = Math.max(1, beds.value.length)
  return [
    { label: '空床位', value: stats.value.idle, percent: Math.round((stats.value.idle / total) * 100), tone: 'fill-gray' },
    { label: '在住长者', value: stats.value.occupied, percent: Math.round((stats.value.occupied / total) * 100), tone: 'fill-cyan' },
    { label: 'AI关注', value: aiFocusCount.value, percent: Math.round((aiFocusCount.value / total) * 100), tone: 'fill-purple' },
    { label: '离床观察', value: awayObservationCount.value, percent: Math.round((awayObservationCount.value / total) * 100), tone: 'fill-orange' },
    { label: '实时风险提醒', value: concernCount.value, percent: Math.round((concernCount.value / total) * 100), tone: 'fill-red' }
  ]
})

const focusBeds = computed(() => {
  const preferred = alertBeds.value.length ? alertBeds.value : sourceBeds.value.filter((bed) => bed.elderId)
  return preferred.slice(0, 4)
})

const alertFeed = computed(() => alertBeds.value.slice(0, 6).map((bed, index) => ({
  key: `alert-${bed.id}`,
  type: bed.riskLevel === 'HIGH' ? '高优先提醒' : '实时提醒',
  time: dayjs().subtract(index * 4, 'minute').format('HH:mm'),
  title: `${bed.roomNo || '-'} / ${bed.bedNo || '-'} ${bed.elderName || '空床'}`,
  description: `${bed.riskLabel || '体征波动'}，24h异常 ${bed.abnormalVital24hCount || 0} 次，当前床态 ${resolveStatus(bed)}`,
  tone: bed.riskLevel === 'HIGH' ? 'tone-red' : 'tone-orange'
})))

const aiSuggestionFeed = computed(() => {
  const source = alertBeds.value.length ? alertBeds.value.slice(0, 3) : sourceBeds.value.filter((bed) => bed.elderId).slice(0, 3)
  return source.map((bed, index) => ({
    key: `ai-${bed.id || index}`,
    title: `${bed.elderName || '待关怀长者'} · ${bed.roomNo || '-'} ${bed.bedNo || '-'}`,
    description: bed.riskLevel === 'HIGH'
      ? '建议立即通知当班护理员复核生命体征，并发起 15 分钟内回访。'
      : bed.riskLevel === 'MEDIUM'
        ? '建议加入夜巡优先名单，关注离床频次、翻身节奏与睡眠连续性。'
        : '建议保持当前照护节奏，并在下一班交接时同步睡眠稳定观察结果。',
    tone: bed.riskLevel === 'HIGH' ? 'dot-red' : bed.riskLevel === 'MEDIUM' ? 'dot-purple' : 'dot-cyan'
  }))
})

const bedGuardCurrentIndex = computed(() => {
  if (!selectedBed.value) return 2
  if (isEmptyBed(selectedBed.value)) return 1
  if (selectedBed.value.riskLevel === 'HIGH') return 3
  return 2
})

const bedGuardStageText = computed(() => {
  if (!selectedBed.value) return '在院运行中'
  if (isEmptyBed(selectedBed.value)) return '空床待分配'
  if (selectedBed.value.riskLevel === 'HIGH') return '高风险干预中'
  return '照护执行中'
})

const bedGuardSubject = computed(() => {
  if (selectedBed.value) {
    return `床位 ${selectedBed.value.bedNo || '-'} / 房间 ${selectedBed.value.roomNo || '-'} / 长者 ${selectedBed.value.elderName || '空床'}`
  }
  return `当前范围：${selectedBuilding.value || '全部楼栋'} ${selectedFloor.value || '全部楼层'}`
})

const bedGuardBlockers = computed(() => {
  const blockers: Array<{ code: string; text: string; actionLabel?: string; actionKey?: string }> = []
  if (selectedBed.value) {
    if (isEmptyBed(selectedBed.value)) blockers.push({ code: 'B201', text: '当前为空床，需通过入住办理分配', actionLabel: '去入住办理', actionKey: 'go-admission' })
    if (selectedBed.value.status === 2) blockers.push({ code: 'B301', text: '床位处于维修状态' })
    if (selectedBed.value.riskLevel === 'HIGH') blockers.push({ code: 'B401', text: '高风险长者需优先处置并建任务', actionLabel: '生成提醒', actionKey: 'create-alert' })
    return blockers
  }
  if (quickFilter.value === 'IDLE' && stats.value.idle === 0) blockers.push({ code: 'B202', text: '暂无可分配空床' })
  if (quickFilter.value === 'OCCUPIED' && stats.value.occupied === 0) blockers.push({ code: 'B203', text: '当前无在住床位' })
  return blockers
})

const bedGuardHint = computed(() => {
  if (selectedBed.value?.elderId) return '可打开长者档案，检查评估、护理任务与异常提醒'
  return '建议点击床位查看详情，执行分配或风险干预动作'
})

const focusHeadline = computed(() => {
  if (selectedBed.value) {
    return {
      title: `${selectedBed.value.roomNo || '-'} / ${selectedBed.value.bedNo || '-'}`,
      meta: `${selectedBed.value.elderName || '空床'} · ${resolveStatus(selectedBed.value)} · ${selectedBed.value.riskLabel || '实时监测中'}`
    }
  }
  if (selectedRoom.value) {
    return {
      title: `${selectedRoom.value.roomNo || '-'} 房间`,
      meta: `${selectedRoom.value.roomType} · 容量 ${selectedRoom.value.capacity || 0} 床 · 空床 ${selectedRoom.value.emptyBeds || 0}`
    }
  }
  return {
    title: selectedBuilding.value || selectedFloor.value ? `${selectedBuilding.value || '全部楼栋'} ${selectedFloor.value || ''}`.trim() : '园区总览',
    meta: selectedBuilding.value || selectedFloor.value
      ? `当前范围 ${roomCount.value} 间房 / ${sourceBeds.value.length} 张床位`
      : '点击楼栋、楼层、房间或床位，快速进入对应视角'
  }
})

const stageQuickActions = computed(() => ([
  { key: 'reset-filters', label: '重置筛选', description: '回到总览范围', tone: 'tone-cyan' },
  { key: 'open-map', label: '房态视图', description: '切到平面房态图', tone: 'tone-blue' },
  { key: 'open-manage', label: '床位管理', description: '执行基础维护', tone: 'tone-green' }
]))

const commandDeck = computed(() => ([
  { key: 'open-profile', label: '长者档案', description: '查看在住档案与照护信息', tone: 'tone-blue' },
  { key: 'allocate-bed', label: '床位分配', description: '为空床发起入住分配', tone: 'tone-cyan' },
  { key: 'open-assessment', label: '评估档案', description: '进入能力评估归档', tone: 'tone-purple' },
  { key: 'open-contracts', label: '合同票据', description: '处理合同与账单联动', tone: 'tone-green' },
  { key: 'open-status-center', label: '状态中心', description: '发起状态变更闭环', tone: 'tone-orange' },
  { key: 'create-alert', label: '生成提醒', description: '同步推送提醒与任务', tone: 'tone-red' }
]))

const trendLabels = computed(() => Array.from({ length: 7 }, (_, index) => dayjs().subtract(6 - index, 'day').format('MM/DD')))

function buildTrend(base: number, multipliers: number[]) {
  return multipliers.map((multiplier, index) => {
    const wave = Math.sin((index + 1) * 0.9) * Math.max(1, base * 0.05)
    return Math.max(0, Math.round(base * multiplier + wave))
  })
}

const occupancyTrendOption = computed(() => buildLineOption({
  labels: trendLabels.value,
  data: buildTrend(stats.value.occupied, [0.84, 0.88, 0.9, 0.94, 0.95, 0.97, 1]),
  color: '#52f3c4',
  areaColor: 'rgba(62, 232, 181, 0.22)'
}))

const sleepTrendOption = computed(() => buildLineOption({
  labels: trendLabels.value,
  data: buildTrend(Math.max(1, awayObservationCount.value), [0.72, 0.76, 0.8, 0.84, 0.9, 0.96, 1]),
  color: '#ffbf74',
  areaColor: 'rgba(255, 191, 116, 0.22)'
}))

const alertTrendOption = computed(() => buildLineOption({
  labels: trendLabels.value,
  data: buildTrend(Math.max(1, concernCount.value), [1.34, 1.2, 1.12, 0.92, 0.98, 1.05, 1]),
  color: '#ff6d89',
  areaColor: 'rgba(255, 93, 124, 0.2)'
}))

const deviceTrendOption = computed(() => buildLineOption({
  labels: trendLabels.value,
  data: buildTrend(deviceOnlineRate.value, [0.91, 0.92, 0.94, 0.95, 0.97, 0.98, 1]),
  color: '#57d7ff',
  areaColor: 'rgba(87, 215, 255, 0.2)',
  formatter: '{value}%'
}))

function buildLineOption(config: { labels: string[]; data: number[]; color: string; areaColor: string; formatter?: string }) {
  return {
    animationDuration: 900,
    animationEasing: 'cubicOut',
    grid: { left: 14, right: 18, top: 24, bottom: 22, containLabel: true },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(6, 18, 34, 0.92)',
      borderColor: 'rgba(87, 215, 255, 0.3)',
      textStyle: { color: '#eaf7ff' }
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: config.labels,
      axisLine: { lineStyle: { color: 'rgba(120, 164, 201, 0.25)' } },
      axisLabel: { color: '#7fa2bf', fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: 'rgba(120, 164, 201, 0.12)' } },
      axisLabel: { color: '#7fa2bf', fontSize: 11, formatter: config.formatter || '{value}' }
    },
    series: [
      {
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        data: config.data,
        lineStyle: { width: 2, color: config.color, shadowBlur: 14, shadowColor: config.color },
        itemStyle: { color: config.color, borderColor: '#ffffff', borderWidth: 1 },
        areaStyle: {
          color: config.areaColor
        }
      }
    ]
  }
}

function handleBedGuardAction(item: { actionKey?: string }) {
  if (item.actionKey === 'go-admission') {
    allocateBed()
    return
  }
  if (item.actionKey === 'create-alert') {
    createAlert()
  }
}

function handleCommandAction(action: string) {
  if (action === 'reset-filters') {
    resetFilters()
    return
  }
  if (action === 'open-map') {
    openBedMap()
    return
  }
  if (action === 'open-manage') {
    openBedManage()
    return
  }
  if (action === 'open-profile') {
    openProfile()
    return
  }
  if (action === 'allocate-bed') {
    allocateBed()
    return
  }
  if (action === 'open-assessment') {
    openAssessmentArchive()
    return
  }
  if (action === 'open-contracts') {
    openContractsInvoices()
    return
  }
  if (action === 'open-status-center') {
    openStatusChangeCenter()
    return
  }
  if (action === 'create-alert') {
    createAlert()
  }
}

function setBuildingScope(building: string) {
  selectedBuilding.value = building
  selectedFloor.value = ''
}

function setFloorScope(floor: string) {
  selectedFloor.value = floor
}

function clearMatrixSelection() {
  selectedBuilding.value = ''
  selectedFloor.value = ''
}

function resetFilters() {
  keyword.value = ''
  quickFilter.value = 'ALL'
  riskFilterEnabled.value = false
  riskFilterLevel.value = 'ALL'
  clearMatrixSelection()
}

function firstRouteQueryText(value: unknown) {
  if (Array.isArray(value)) return firstRouteQueryText(value[0])
  if (value == null) return ''
  return String(value).trim()
}

function flattenRouteQuery(source: Record<string, unknown>) {
  return Object.entries(source || {}).reduce<Record<string, string>>((acc, [key, value]) => {
    const text = firstRouteQueryText(value)
    if (!text) return acc
    acc[key] = text
    return acc
  }, {})
}

function buildBedRouteSignature(source: Record<string, unknown>) {
  return [...BED_ROUTE_KEYS]
    .sort()
    .map((key) => `${key}:${firstRouteQueryText(source[key])}`)
    .join('|')
}

function applyFiltersFromRoute() {
  keyword.value = firstRouteQueryText(route.query.bedKeyword)

  const nextQuick = firstRouteQueryText(route.query.bedQuick).toUpperCase()
  quickFilter.value = nextQuick === 'IDLE' || nextQuick === 'OCCUPIED' ? nextQuick : 'ALL'

  riskFilterEnabled.value = firstRouteQueryText(route.query.bedRiskEnabled) === '1'
  const nextRisk = firstRouteQueryText(route.query.bedRiskLevel).toUpperCase()
  riskFilterLevel.value = nextRisk === 'HIGH' || nextRisk === 'MEDIUM' || nextRisk === 'LOW' ? nextRisk : 'ALL'

  selectedBuilding.value = firstRouteQueryText(route.query.bedBuilding)
  selectedFloor.value = firstRouteQueryText(route.query.bedFloor)
}

function buildFiltersRouteQuery() {
  const nextQuery: Record<string, string> = {}
  Object.entries(flattenRouteQuery(route.query as Record<string, unknown>)).forEach(([key, value]) => {
    if ((BED_ROUTE_KEYS as readonly string[]).includes(key)) return
    nextQuery[key] = value
  })
  if (keyword.value.trim()) nextQuery.bedKeyword = keyword.value.trim()
  nextQuery.bedQuick = quickFilter.value
  if (riskFilterEnabled.value) nextQuery.bedRiskEnabled = '1'
  if (riskFilterEnabled.value && riskFilterLevel.value !== 'ALL') nextQuery.bedRiskLevel = riskFilterLevel.value
  if (selectedBuilding.value) nextQuery.bedBuilding = selectedBuilding.value
  if (selectedFloor.value) nextQuery.bedFloor = selectedFloor.value
  return nextQuery
}

function hasSameRouteQuery(nextQuery: Record<string, string>) {
  const currentQuery = flattenRouteQuery(route.query as Record<string, unknown>)
  const currentKeys = Object.keys(currentQuery)
  const nextKeys = Object.keys(nextQuery)
  if (currentKeys.length !== nextKeys.length) return false
  return nextKeys.every((key) => currentQuery[key] === nextQuery[key])
}

async function syncFiltersToRoute() {
  const nextQuery = buildFiltersRouteQuery()
  if (hasSameRouteQuery(nextQuery)) return
  skipNextBedRouteWatch.value = true
  bedRouteSignature.value = buildBedRouteSignature(nextQuery)
  await router.replace({ path: route.path, query: nextQuery })
}

function floorSortValue(text: string) {
  const raw = String(text || '').trim()
  if (!raw) return -999
  const normalized = raw.replace(/\s+/g, '').toUpperCase()
  if (/^(ROOF|RF|屋顶|天台)$/.test(normalized)) return 999
  const basement = normalized.match(/(?:地下|负|B)([0-9]+|[一二三四五六七八九十百两]+)/)
  if (basement) return -parseFloorToken(basement[1])
  const match = normalized.match(/([0-9]+|[一二三四五六七八九十百两]+)(?:F|楼|层)?/)
  if (match) return parseFloorToken(match[1])
  return -999
}

function parseFloorToken(token: string) {
  if (/^\d+$/.test(token)) return Number(token)
  const value = parseChineseNumber(token)
  return value > 0 ? value : 0
}

function parseChineseNumber(text: string) {
  const chars = text.split('')
  const digitMap: Record<string, number> = { 零: 0, 一: 1, 二: 2, 两: 2, 三: 3, 四: 4, 五: 5, 六: 6, 七: 7, 八: 8, 九: 9 }
  if (!chars.length) return 0
  if (chars.length === 1) return digitMap[chars[0]] ?? 0
  let section = 0
  let number = 0
  chars.forEach((char) => {
    if (digitMap[char] !== undefined) {
      number = digitMap[char]
      return
    }
    if (char === '十') {
      section += (number || 1) * 10
      number = 0
      return
    }
    if (char === '百') {
      section += (number || 1) * 100
      number = 0
    }
  })
  return section + number
}

function resolveStatus(bed: BedItem): '空闲' | '预定' | '在住' | '维修' | '清洁中' | '锁定' {
  if (bed.elderId) return '在住'
  if (bed.status === 0) return '锁定'
  if (bed.occupancySource === 'CLEANING') return '清洁中'
  if (bed.status === 3 || bed.occupancySource === 'MAINTENANCE') return '维修'
  if (bed.occupancySource === 'RESERVATION') return '预定'
  return '空闲'
}

function resolveRoomTypeLabel(roomType?: string) {
  const raw = String(roomType || '').trim()
  if (!raw) return '-'
  const normalized = raw.toUpperCase()
  const map: Record<string, string> = {
    '1': '单人间',
    '2': '双人间',
    '3': '三人间',
    SINGLE: '单人间',
    DOUBLE: '双人间',
    TRIPLE: '三人间',
    ONE: '单人间',
    TWO: '双人间',
    THREE: '三人间',
    STANDARD: '标准间',
    STANDARD_ROOM: '标准间',
    DELUXE: '豪华间',
    VIP: 'VIP房',
    SUITE: '套间'
  }
  return map[normalized] || raw
}

function isEmptyBed(bed: BedItem) {
  return !bed.elderId && resolveStatus(bed) === '空闲'
}

function isAlertBed(bed: BedItem) {
  return bed.riskLevel === 'HIGH' || bed.status === 0 || Number(bed.abnormalVital24hCount || 0) > 0
}

function bedRiskLabel(bed: BedItem) {
  return bed.riskLabel || ''
}

function formatLatestAssessment(bed: BedItem) {
  const level = bed.latestAssessmentLevel || '-'
  const date = bed.latestAssessmentDate || '-'
  if (level === '-' && date === '-') return '-'
  return `${level} / ${date}`
}

function selectBed(bed: BedItem) {
  selectedBed.value = bed
  selectedRoom.value = null
  detailOpen.value = true
}

function parseRemarkSlots(raw?: string) {
  if (!raw) return []
  try {
    const parsed = JSON.parse(raw)
    const slots = Array.isArray(parsed?.slots) ? parsed.slots : [parsed?.remark1, parsed?.remark2, parsed?.remark3]
    return slots
      .map((slot: any, index: number) => {
        if (!slot) return null
        if (typeof slot === 'string') {
          return { text: slot, visible: true, index }
        }
        return {
          text: String(slot.text || slot.value || '').trim(),
          visible: slot.visible !== false,
          index
        }
      })
      .filter((slot: any) => slot && slot.text)
  } catch {
    return [{ text: raw, visible: true, index: 0 }]
  }
}

function resolveVisibleRemark(raw?: string) {
  return parseRemarkSlots(raw)
    .filter((item: any) => item.visible)
    .map((item: any) => item.text)
    .join('；')
}

function resolveAllRemark(raw?: string) {
  return parseRemarkSlots(raw)
    .map((item: any) => item.text)
    .join('；')
}

async function openRoomDetail(room: RoomScene) {
  selectedRoom.value = room
  selectedBed.value = null
  roomDetailOpen.value = true
  const elderIds = Array.from(new Set(room.beds.map((item) => item.elderId).filter(Boolean))) as string[]
  if (!elderIds.length) {
    roomResidents.value = []
    return
  }
  roomResidentLoading.value = true
  try {
    const results = await Promise.allSettled(elderIds.map((elderId) => getElderDetail(elderId)))
    roomResidents.value = results
      .filter((item): item is PromiseFulfilledResult<ElderItem> => item.status === 'fulfilled')
      .map((item) => item.value)
  } finally {
    roomResidentLoading.value = false
  }
}

function openElderProfile(elderId: string) {
  roomDetailOpen.value = false
  router.push(`/elder/detail/${elderId}`)
}

function openResidentBills(elderId: string) {
  router.push(`/finance/bills/in-resident?elderId=${elderId}`)
}

function activeElderId() {
  const elderId = String(selectedBed.value?.elderId || '').trim()
  if (!elderId) {
    message.warning('当前床位未关联长者')
    return ''
  }
  return elderId
}

function openAssessmentArchive() {
  const elderId = activeElderId()
  if (!elderId) return
  detailOpen.value = false
  router.push(`/elder/assessment/ability/archive?elderId=${encodeURIComponent(elderId)}`)
}

function openContractsInvoices() {
  const elderId = activeElderId()
  if (!elderId) return
  detailOpen.value = false
  router.push(`/elder/contracts-invoices?elderId=${encodeURIComponent(elderId)}`)
}

function openStatusChangeCenter() {
  detailOpen.value = false
  const elderId = String(selectedBed.value?.elderId || '').trim()
  router.push({
    path: '/elder/status-change',
    query: elderId ? { residentId: elderId } : undefined
  })
}

function openProfile() {
  if (!selectedBed.value?.elderId) {
    message.warning('当前是空床位，请先分配床位')
    return
  }
  detailOpen.value = false
  router.push(`/elder/detail/${selectedBed.value.elderId}`)
}

function allocateBed() {
  if (!selectedBed.value) {
    message.warning('请先选择床位')
    return
  }
  detailOpen.value = false
  router.push(`/elder/admission-processing?bedId=${selectedBed.value.id}`)
}

function createAlert() {
  if (!selectedBed.value) {
    message.warning('请先选择床位')
    return
  }
  detailOpen.value = false
  message.success('已生成提醒并推送到提醒中心/任务中心')
  router.push('/oa/work-execution/task?category=alert')
}

function goScan() {
  detailOpen.value = false
  router.push(`/care/workbench/qr?bedId=${selectedBed.value?.id || ''}`)
}

function openBedMap() {
  router.push('/logistics/assets/room-state-map')
}

function openBedManage() {
  router.push('/logistics/assets/bed-management')
}

async function loadBeds(includeRisk = false) {
  beds.value = await getBedMap({ params: { includeRisk } })
  if (includeRisk) {
    riskDataReady.value = true
  } else {
    riskDataReady.value = false
  }
}

async function ensureRiskDataLoaded() {
  if (riskDataReady.value || riskDataLoading.value) return
  riskDataLoading.value = true
  try {
    await loadBeds(true)
  } finally {
    riskDataLoading.value = false
  }
}

async function loadRooms() {
  const rooms: RoomItem[] = await getRoomList()
  const typeMap: Record<string, string> = {}
  const capMap: Record<string, number> = {}
  rooms.forEach((item) => {
    const id = String(item.id)
    typeMap[id] = item.roomType || '标准间'
    capMap[id] = Number(item.capacity || 1)
  })
  roomTypeMap.value = typeMap
  roomCapacityMap.value = capMap
}

useLiveSyncRefresh({
  topics: ['elder', 'bed', 'lifecycle', 'finance', 'care', 'dining'],
  refresh: async () => {
    await Promise.all([loadBeds(false), loadRooms()])
    if (riskFilterEnabled.value) {
      await ensureRiskDataLoaded()
    }
  }
})

onMounted(async () => {
  applyFiltersFromRoute()
  bedRouteSignature.value = buildBedRouteSignature(route.query as Record<string, unknown>)
  clockTimer = window.setInterval(() => {
    currentTime.value = dayjs()
  }, 1000)
  await Promise.all([loadBeds(false), loadRooms()])
  if (riskFilterEnabled.value) {
    await ensureRiskDataLoaded()
  }
  await syncFiltersToRoute().catch(() => {})
})

onBeforeUnmount(() => {
  if (clockTimer) window.clearInterval(clockTimer)
})

watch(
  riskFilterEnabled,
  (enabled) => {
    if (enabled) {
      ensureRiskDataLoaded().catch(() => {})
    }
  }
)

watch(sourceBeds, () => {
  if (selectedBuilding.value && !sourceBeds.value.some((item) => String(item.building || '') === selectedBuilding.value)) {
    selectedBuilding.value = ''
  }
  if (selectedFloor.value && !sourceBeds.value.some((item) => String(item.floorNo || '') === selectedFloor.value)) {
    selectedFloor.value = ''
  }
})

watch(
  [keyword, quickFilter, riskFilterEnabled, riskFilterLevel, selectedBuilding, selectedFloor],
  () => {
    syncFiltersToRoute().catch(() => {})
  }
)

watch(
  () => route.query,
  (queryMap) => {
    const nextSignature = buildBedRouteSignature(queryMap as Record<string, unknown>)
    if (skipNextBedRouteWatch.value) {
      skipNextBedRouteWatch.value = false
      bedRouteSignature.value = nextSignature
      return
    }
    if (nextSignature === bedRouteSignature.value) {
      return
    }
    bedRouteSignature.value = nextSignature
    applyFiltersFromRoute()
  },
  { deep: true }
)
</script>

<style scoped>
.bed-immersive-page {
  --hud-border: rgba(138, 197, 216, 0.2);
  --hud-bg: rgba(8, 16, 29, 0.42);
  --hud-bg-strong: rgba(8, 16, 29, 0.7);
  --hud-shadow: 0 20px 40px rgba(0, 0, 0, 0.34), inset 0 1px 0 rgba(218, 238, 247, 0.08);
  --text-strong: #edf6fb;
  --text-main: #d4e6f0;
  --text-soft: #8fa8ba;
  --tone-cyan: #7ccfdb;
  --tone-blue: #88a2dc;
  --tone-green: #86d1bf;
  --tone-purple: #a891d4;
  --tone-orange: #d8ad72;
  --tone-red: #d88995;
  --tone-gray: #8ca2b6;
}

.bed-immersive-page :deep(.page-head) {
  display: none;
}

.bed-immersive-page :deep(.page-body) {
  gap: 0;
}

.immersive-stage-shell {
  position: relative;
  min-height: calc(100vh - 112px);
  border-radius: 28px;
  overflow: hidden;
  background:
    radial-gradient(circle at 15% 10%, rgba(124, 207, 219, 0.12), transparent 30%),
    radial-gradient(circle at 88% 12%, rgba(136, 162, 220, 0.16), transparent 28%),
    linear-gradient(180deg, #040a12 0%, #02060d 100%);
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.4);
}

.immersive-stage-shell :deep(.panorama-container) {
  height: calc(100vh - 112px);
  min-height: 840px;
}

.stage-empty {
  min-height: calc(100vh - 112px);
  display: grid;
  place-items: center;
}

.scene-vignette,
.scene-glow {
  position: absolute;
  pointer-events: none;
  z-index: 1;
}

.scene-vignette {
  inset: 0;
  background:
    radial-gradient(circle at center, transparent 38%, rgba(2, 8, 15, 0.44) 100%),
    linear-gradient(180deg, rgba(2, 8, 15, 0.06), rgba(2, 8, 15, 0.42));
}

.scene-glow {
  width: 320px;
  height: 320px;
  border-radius: 50%;
  filter: blur(70px);
  opacity: 0.2;
}

.scene-glow-left {
  left: -100px;
  bottom: -80px;
  background: rgba(124, 207, 219, 0.72);
}

.scene-glow-right {
  right: -120px;
  top: 120px;
  background: rgba(136, 162, 220, 0.7);
}

.hud {
  position: absolute;
  z-index: 3;
}

.hud-panel,
.metric-pill,
.stage-action-pill,
.action-card,
.focus-row {
  border: 1px solid var(--hud-border);
  background:
    linear-gradient(180deg, rgba(10, 18, 30, 0.56), rgba(8, 14, 24, 0.78));
  backdrop-filter: blur(18px);
  box-shadow: var(--hud-shadow);
}

.hud-panel::before,
.metric-pill::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  pointer-events: none;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.08), transparent 48%, transparent);
}

.hud-topbar {
  top: 22px;
  left: 24px;
  right: 24px;
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr) 320px;
  gap: 16px;
  align-items: start;
}

.hud-topbar__brand,
.status-stack,
.clock-stack,
.operator-stack {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-mark {
  position: relative;
  width: 48px;
  height: 48px;
  flex-shrink: 0;
  border-radius: 18px;
  border: 1px solid rgba(124, 207, 219, 0.32);
  background:
    radial-gradient(circle at 38% 36%, rgba(214, 243, 247, 0.92), rgba(124, 207, 219, 0.16) 56%, transparent 62%),
    linear-gradient(180deg, rgba(12, 22, 38, 0.96), rgba(8, 14, 24, 0.98));
  box-shadow: 0 0 26px rgba(124, 207, 219, 0.18);
}

.brand-mark span {
  position: absolute;
  inset: 9px;
  border-radius: 12px;
  border: 1px solid rgba(124, 207, 219, 0.24);
}

.brand-copy {
  display: grid;
  gap: 4px;
}

.brand-kicker,
.panel-kicker,
.field-label,
.metric-pill span,
.trend-head span,
.overview-row span,
.status-bar__top span {
  font-size: 11px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--text-soft);
}

.brand-copy strong,
.hud-panel__head strong,
.focus-row strong,
.event-card strong,
.timeline-item strong,
.operator-stack strong,
.status-stack strong {
  color: var(--text-strong);
}

.brand-copy small,
.hud-panel__head small,
.status-stack small,
.clock-stack span,
.operator-stack small,
.overview-row small,
.event-card p,
.timeline-item p,
.focus-row small {
  color: var(--text-soft);
  line-height: 1.5;
}

.hud-topbar__metrics {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 10px;
}

.metric-pill {
  position: relative;
  min-height: 84px;
  border-radius: 20px;
  padding: 14px 16px;
  display: grid;
  gap: 4px;
  text-align: left;
}

.metric-pill strong {
  color: var(--text-strong);
  font-size: 28px;
  line-height: 1;
}

.metric-pill small {
  color: var(--text-soft);
  font-size: 12px;
}

.hud-topbar__ops {
  display: grid;
  gap: 10px;
}

.status-stack,
.clock-stack,
.operator-stack {
  min-height: 58px;
  padding: 12px 14px;
  border-radius: 20px;
  border: 1px solid var(--hud-border);
  background: var(--hud-bg);
  backdrop-filter: blur(18px);
  box-shadow: var(--hud-shadow);
}

.status-dot {
  width: 10px;
  height: 10px;
  flex-shrink: 0;
  border-radius: 999px;
  background: #86d1bf;
  box-shadow: 0 0 16px rgba(134, 209, 191, 0.5);
  animation: statusBreath 3.2s ease-in-out infinite;
}

.clock-stack {
  justify-content: space-between;
}

.clock-stack strong {
  color: var(--text-strong);
  font-size: 22px;
  letter-spacing: 0.12em;
}

.operator-avatar {
  background: linear-gradient(135deg, #82cad5, #8ca0d5);
  color: #07111d;
  font-weight: 700;
}

.hud-left {
  top: 118px;
  left: 24px;
  width: 280px;
  display: grid;
  gap: 14px;
}

.hud-center {
  top: 118px;
  left: 50%;
  transform: translateX(-50%);
  width: min(680px, calc(100% - 680px));
}

.hud-right {
  top: 118px;
  right: 24px;
  width: 320px;
  display: grid;
  gap: 14px;
}

.hud-bottom {
  left: 24px;
  right: 24px;
  bottom: 24px;
}

.hud-panel {
  position: relative;
  overflow: hidden;
  border-radius: 24px;
  padding: 16px;
}

.hud-panel--compact {
  padding: 14px;
}

.hud-panel__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.overview-list,
.status-bars,
.event-list,
.timeline-list,
.focus-list,
.blocker-list {
  display: grid;
  gap: 10px;
}

.overview-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 4px 12px;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid rgba(138, 197, 216, 0.08);
}

.overview-row strong,
.status-bar__top strong,
.detail-grid strong {
  color: var(--text-main);
}

.overview-row small {
  grid-column: 1 / -1;
}

.status-bar__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.distribution-track {
  height: 7px;
  border-radius: 999px;
  background: rgba(138, 197, 216, 0.12);
  overflow: hidden;
}

.distribution-fill {
  display: block;
  height: 100%;
  border-radius: inherit;
}

.fill-cyan { background: linear-gradient(90deg, #6dbdca, #8ed7e1); }
.fill-gray { background: linear-gradient(90deg, #758da2, #97aabe); }
.fill-purple { background: linear-gradient(90deg, #8c7cbc, #b49bd8); }
.fill-orange { background: linear-gradient(90deg, #c98e51, #ddb37b); }
.fill-red { background: linear-gradient(90deg, #c97384, #e39aa7); }

.field-block {
  display: grid;
  gap: 8px;
}

.field-inline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.scope-chip-list,
.scene-actions,
.spotlight-actions,
.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.scope-chip {
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid rgba(138, 197, 216, 0.18);
  background: rgba(10, 18, 30, 0.46);
  color: var(--text-main);
  font-size: 12px;
  transition: all 0.2s ease;
}

.scope-chip.active,
.scope-chip:hover {
  color: #fff;
  border-color: rgba(138, 197, 216, 0.34);
  box-shadow: 0 0 16px rgba(124, 207, 219, 0.12);
}

.scene-actions :deep(.ant-btn) {
  flex: 1 1 0;
}

.hud-panel--spotlight {
  max-width: 100%;
}

.spotlight-meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}

.spotlight-block {
  padding: 12px 14px;
  border-radius: 18px;
  border: 1px solid rgba(138, 197, 216, 0.14);
  background: rgba(10, 18, 30, 0.34);
  display: grid;
  gap: 4px;
}

.spotlight-block span,
.detail-grid span {
  color: var(--text-soft);
  font-size: 12px;
}

.spotlight-block strong {
  color: var(--text-main);
}

.overlay-progress {
  display: grid;
  gap: 8px;
}

.overlay-step {
  display: grid;
  grid-template-columns: 26px minmax(0, 1fr);
  gap: 10px;
  align-items: center;
  color: var(--text-soft);
}

.overlay-step span {
  width: 26px;
  height: 26px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  border: 1px solid rgba(138, 197, 216, 0.16);
  background: rgba(8, 14, 24, 0.84);
  font-size: 12px;
}

.overlay-step.active {
  color: var(--text-main);
}

.overlay-step.active span {
  border-color: rgba(138, 197, 216, 0.34);
  background: rgba(81, 110, 137, 0.34);
}

.overlay-step small {
  font-size: 12px;
}

.stage-action-pill,
.action-card {
  position: relative;
  border-radius: 18px;
  padding: 12px 14px;
  text-align: left;
  display: grid;
  gap: 4px;
  color: var(--text-strong);
  transition: transform 0.2s ease, border-color 0.2s ease;
}

.stage-action-pill:hover,
.action-card:hover,
.focus-row:hover {
  transform: translateY(-1px);
  border-color: rgba(138, 197, 216, 0.32);
}

.stage-action-pill span,
.action-card span {
  font-size: 12px;
  color: var(--text-soft);
}

.detail-tags {
  margin-bottom: 14px;
}

.overlay-chip,
.focus-status {
  padding: 5px 10px;
  border-radius: 999px;
  border: 1px solid rgba(138, 197, 216, 0.18);
  background: rgba(124, 207, 219, 0.1);
  color: var(--text-main);
  font-size: 12px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}

.detail-grid > div {
  padding: 12px;
  border-radius: 16px;
  border: 1px solid rgba(138, 197, 216, 0.12);
  background: rgba(10, 18, 30, 0.34);
  display: grid;
  gap: 4px;
}

.blocker-card {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  padding: 12px;
  border-radius: 16px;
  border: 1px solid rgba(216, 173, 114, 0.14);
  background: rgba(46, 30, 20, 0.36);
}

.blocker-card strong {
  color: #e0b582;
}

.blocker-card p {
  margin: 4px 0 0;
  color: var(--text-soft);
  font-size: 12px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.event-card,
.focus-row {
  position: relative;
  border-radius: 18px;
  padding: 12px 14px;
}

.event-top {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
}

.event-type,
.event-time {
  color: var(--text-soft);
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.event-card.tone-red {
  animation: alertPulse 3.6s ease-in-out infinite;
}

.timeline-item {
  display: grid;
  grid-template-columns: 14px minmax(0, 1fr);
  gap: 10px;
  align-items: start;
}

.timeline-dot {
  width: 10px;
  height: 10px;
  margin-top: 6px;
  border-radius: 999px;
  box-shadow: 0 0 14px currentColor;
}

.dot-cyan { color: #82d5df; background: #82d5df; }
.dot-purple { color: #aa95d7; background: #aa95d7; }
.dot-red { color: #d78a97; background: #d78a97; }
.dot-orange { color: #ddb37b; background: #ddb37b; }

.focus-list {
  gap: 8px;
}

.focus-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  text-align: left;
}

.trend-panel {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.trend-card {
  padding: 12px 14px;
  border-radius: 22px;
  border: 1px solid var(--hud-border);
  background: linear-gradient(180deg, rgba(9, 16, 28, 0.68), rgba(7, 12, 22, 0.82));
  backdrop-filter: blur(18px);
  box-shadow: var(--hud-shadow);
}

.trend-head {
  display: grid;
  gap: 4px;
  margin-bottom: 6px;
}

.trend-head strong {
  color: var(--text-main);
}

.chart-view {
  height: 130px;
}

.bed-immersive-page :deep(.ant-input),
.bed-immersive-page :deep(.ant-input-affix-wrapper),
.bed-immersive-page :deep(.ant-input-search-button),
.bed-immersive-page :deep(.ant-segmented),
.bed-immersive-page :deep(.ant-switch),
.bed-immersive-page :deep(.ant-btn) {
  border-radius: 14px;
  background: rgba(10, 18, 30, 0.72);
  border-color: rgba(138, 197, 216, 0.18);
  color: var(--text-main);
}

.bed-immersive-page :deep(.ant-btn-primary) {
  background: linear-gradient(90deg, #7ccfdb, #9cb0da) !important;
  color: #07111d !important;
}

.bed-immersive-page :deep(.ant-empty-description) {
  color: var(--text-soft);
}

@keyframes statusBreath {
  0%, 100% { opacity: 0.82; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.08); }
}

@keyframes alertPulse {
  0%, 100% {
    box-shadow: var(--hud-shadow), inset 0 0 0 1px rgba(216, 137, 149, 0.08);
  }
  50% {
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.34), 0 0 22px rgba(216, 137, 149, 0.08);
  }
}

@media (max-width: 1600px) {
  .hud-topbar {
    grid-template-columns: 300px minmax(0, 1fr) 280px;
  }

  .hud-topbar__metrics {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .hud-center {
    width: min(620px, calc(100% - 640px));
  }
}

@media (max-width: 1280px) {
  .immersive-stage-shell,
  .immersive-stage-shell :deep(.panorama-container),
  .stage-empty {
    min-height: 900px;
    height: 900px;
  }

  .hud-topbar {
    grid-template-columns: 1fr;
  }

  .hud-topbar__metrics,
  .trend-panel {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .hud-left,
  .hud-center,
  .hud-right {
    position: absolute;
    width: auto;
    max-width: none;
  }

  .hud-left,
  .hud-right {
    width: 280px;
  }

  .hud-center {
    left: 50%;
    width: calc(100% - 640px);
  }
}

@media (max-width: 960px) {
  .immersive-stage-shell,
  .immersive-stage-shell :deep(.panorama-container),
  .stage-empty {
    min-height: auto;
    height: auto;
  }

  .hud {
    position: static;
    transform: none !important;
    width: auto !important;
    margin: 12px;
  }

  .immersive-stage-shell {
    display: grid;
    gap: 12px;
    padding-bottom: 12px;
  }

  .scene-vignette,
  .scene-glow {
    display: none;
  }

  .hud-topbar__metrics,
  .trend-panel,
  .detail-grid,
  .action-grid,
  .spotlight-meta {
    grid-template-columns: 1fr;
  }
}
</style>
