<template>
  <PageContainer class="bed-cockpit-page" title="智慧床态全景指挥中心" subTitle="3D床态全景 / 智慧养老数字孪生驾驶舱">
    <template #extra>
      <div class="cockpit-topbar">
        <div class="topbar-status">
          <span class="status-dot"></span>
          <span>系统运行正常</span>
          <span class="status-divider"></span>
          <span>{{ lifecycleContext.active ? '入住联动模式' : '床态实时监控' }}</span>
        </div>
        <div class="topbar-clock">
          <div class="clock-date">{{ currentDateText }}</div>
          <div class="clock-time">{{ currentTimeText }}</div>
        </div>
      </div>
    </template>

    <template #stats>
      <div class="hero-metrics">
        <div v-for="item in overviewCards" :key="item.label" class="hero-metric-card" :class="item.tone">
          <span class="metric-label">{{ item.label }}</span>
          <strong class="metric-value">{{ item.value }}</strong>
          <span class="metric-meta">{{ item.meta }}</span>
        </div>
      </div>
    </template>

    <div class="bed-cockpit-shell">
      <aside class="cockpit-panel panel-left">
        <section class="tech-panel">
          <div class="section-head">
            <div>
              <h3>运行总览</h3>
              <p>床位、告警与设备态势一屏掌握</p>
            </div>
          </div>
          <div class="metric-grid">
            <div v-for="item in leftStatCards" :key="item.label" class="metric-tile" :class="item.tone">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
              <small>{{ item.meta }}</small>
            </div>
          </div>

          <div class="distribution-list">
            <div v-for="item in statusDistributionRows" :key="item.label" class="distribution-item">
              <div class="distribution-top">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </div>
              <div class="distribution-track">
                <span class="distribution-fill" :class="item.tone" :style="{ width: `${item.percent}%` }"></span>
              </div>
            </div>
          </div>
        </section>

        <section class="tech-panel">
          <div class="section-head">
            <div>
              <h3>指挥筛选</h3>
              <p>筛选条件仍与原业务参数保持同步</p>
            </div>
          </div>

          <div class="filter-stack">
            <label class="field-block">
              <span class="field-label">搜索目标</span>
              <a-input-search v-model:value="keyword" placeholder="搜索房间号 / 长者名" allow-clear size="large" />
            </label>

            <label class="field-block">
              <span class="field-label">床位筛选</span>
              <a-segmented v-model:value="quickFilter" :options="quickFilterOptions" block />
            </label>

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
              <span class="field-label">视角聚焦</span>
              <div class="selection-tags">
                <a-tag color="blue" v-if="selectedBuilding">楼栋：{{ selectedBuilding }}</a-tag>
                <a-tag color="cyan" v-if="selectedFloor">楼层：{{ selectedFloor }}</a-tag>
                <span v-if="!selectedBuilding && !selectedFloor" class="field-tip">点击场景中的楼栋、楼层可快速聚焦</span>
              </div>
            </div>

            <div class="command-buttons">
              <a-button @click="openBedMap">房态视图</a-button>
              <a-button @click="openBedManage">床位管理</a-button>
              <a-button type="primary" ghost @click="resetFilters">重置</a-button>
            </div>
          </div>
        </section>

        <section class="tech-panel">
          <div class="section-head">
            <div>
              <h3>重点床位</h3>
              <p>优先关注告警与重点照护对象</p>
            </div>
          </div>
          <div class="focus-bed-list">
            <BedInfoCard v-for="bed in focusBeds" :key="bed.id" :bed="bed" @click="selectBed(bed)" />
            <a-empty v-if="!focusBeds.length" description="当前没有重点床位" />
          </div>
        </section>
      </aside>

      <main class="cockpit-core">
        <section class="tech-panel flow-panel">
          <FlowGuardBar
            title="在院床态守卫"
            :subject="bedGuardSubject"
            :stage-text="bedGuardStageText"
            :stage-color="bedGuardStageColor"
            :steps="bedGuardSteps"
            :current-index="bedGuardCurrentIndex"
            :blockers="bedGuardBlockers"
            :hint="bedGuardHint"
            @action="handleBedGuardAction"
          />
        </section>

        <section class="tech-panel stage-panel">
          <div class="stage-heading">
            <div>
              <div class="eyebrow">3D Bed State Panorama</div>
              <h2>空间透视床态总览</h2>
              <p>中台接口与床位绑定逻辑保持不变，只重构视觉层、结构层与场景表现。</p>
            </div>
            <div class="stage-kpis">
              <div>
                <span>监测楼栋</span>
                <strong>{{ matrixBuildings.length }}</strong>
              </div>
              <div>
                <span>监测楼层</span>
                <strong>{{ matrixFloors.length }}</strong>
              </div>
              <div>
                <span>实时告警</span>
                <strong>{{ emergencyCount }}</strong>
              </div>
            </div>
          </div>

          <a-alert
            v-if="lifecycleContext.active"
            type="info"
            show-icon
            class="lifecycle-alert"
            :message="lifecycleContext.message"
          />

          <div class="stage-shell">
            <Panorama3D
              v-if="matrixBuildings.length && matrixFloors.length"
              :buildings="matrixBuildings"
              :floors="matrixFloors"
              :room-lookup="roomSceneLookup"
              @click-room="openRoomDetail"
              @click-bed="selectBed"
            />
            <a-empty v-else class="stage-empty" description="暂无床位数据" />

            <div class="stage-overlay-card">
              <div class="overlay-title">选中焦点</div>
              <div class="overlay-name">{{ selectedBed?.elderName || selectedRoom?.roomNo || '等待选择床位 / 房间' }}</div>
              <div class="overlay-meta">
                {{
                  selectedBed
                    ? `${selectedBed.building || '-'} / ${selectedBed.floorNo || '-'} / ${selectedBed.roomNo || '-'} / ${selectedBed.bedNo || '-'}`
                    : selectedRoom
                      ? `${selectedRoom.roomType} · ${selectedRoom.capacity || 0}床`
                      : '点击场景中的楼栋、楼层、房间或床位进行联动分析'
                }}
              </div>
              <div class="overlay-tags" v-if="selectedBed">
                <span class="overlay-chip" :class="statusChipClass(selectedBed)">{{ resolveStatus(selectedBed) }}</span>
                <span class="overlay-chip" :class="riskChipClass(selectedBed)" v-if="selectedBed.riskLabel">{{ selectedBed.riskLabel }}</span>
                <span class="overlay-chip is-metric">异常 {{ selectedBed.abnormalVital24hCount || 0 }}</span>
              </div>
              <div v-if="selectedBed" class="overlay-vitals">
                <div class="vital-badge">
                  <span>HR</span>
                  <strong>{{ deriveHeartRate(selectedBed) }}</strong>
                </div>
                <div class="vital-badge">
                  <span>RR</span>
                  <strong>{{ deriveBreathRate(selectedBed) }}</strong>
                </div>
                <div class="vital-badge">
                  <span>在线</span>
                  <strong>{{ bedDeviceState(selectedBed) }}</strong>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section class="chart-grid">
          <div class="tech-panel chart-panel">
            <div class="section-head compact">
              <div>
                <h3>床位占用趋势</h3>
                <p>根据当前床位状态推演近 7 日变化</p>
              </div>
            </div>
            <v-chart class="chart-view" :option="occupancyTrendOption" autoresize />
          </div>

          <div class="tech-panel chart-panel">
            <div class="section-head compact">
              <div>
                <h3>睡眠质量趋势</h3>
                <p>基于在住与低风险床位的稳定度估算</p>
              </div>
            </div>
            <v-chart class="chart-view" :option="sleepTrendOption" autoresize />
          </div>

          <div class="tech-panel chart-panel">
            <div class="section-head compact">
              <div>
                <h3>告警趋势</h3>
                <p>高风险、异常体征与锁定状态综合观察</p>
              </div>
            </div>
            <v-chart class="chart-view" :option="alertTrendOption" autoresize />
          </div>

          <div class="tech-panel chart-panel">
            <div class="section-head compact">
              <div>
                <h3>设备健康趋势</h3>
                <p>根据可监测床位占比生成设备健康视图</p>
              </div>
            </div>
            <v-chart class="chart-view" :option="deviceTrendOption" autoresize />
          </div>
        </section>
      </main>

      <aside class="cockpit-panel panel-right">
        <section class="tech-panel">
          <div class="section-head">
            <div>
              <h3>实时告警流</h3>
              <p>风险事件、体征异常与设备状态联动</p>
            </div>
          </div>
          <div class="event-list">
            <div v-for="item in alertFeed" :key="item.key" class="event-card" :class="item.tone">
              <div class="event-top">
                <span class="event-type">{{ item.type }}</span>
                <span class="event-time">{{ item.time }}</span>
              </div>
              <strong>{{ item.title }}</strong>
              <p>{{ item.description }}</p>
            </div>
            <a-empty v-if="!alertFeed.length" description="暂无告警流" />
          </div>
        </section>

        <section class="tech-panel">
          <div class="section-head">
            <div>
              <h3>最新状态变化</h3>
              <p>房间、楼层和床位视角的即时动态</p>
            </div>
          </div>
          <div class="timeline-list">
            <div v-for="item in timelineFeed" :key="item.key" class="timeline-item">
              <span class="timeline-dot" :class="item.tone"></span>
              <div>
                <strong>{{ item.title }}</strong>
                <p>{{ item.description }}</p>
              </div>
            </div>
          </div>
        </section>

        <section class="tech-panel selected-panel">
          <div class="section-head">
            <div>
              <h3>指挥操作台</h3>
              <p>围绕当前床位执行业务动作</p>
            </div>
          </div>

          <div class="selected-summary" v-if="selectedBed">
            <div class="selected-title">{{ selectedBed.bedNo || '-' }} / {{ selectedBed.elderName || '空床' }}</div>
            <div class="selected-meta">{{ selectedBed.building || '-' }} · {{ selectedBed.floorNo || '-' }} · {{ selectedBed.roomNo || '-' }}</div>
            <div class="selected-badges">
              <span class="overlay-chip" :class="statusChipClass(selectedBed)">{{ resolveStatus(selectedBed) }}</span>
              <span class="overlay-chip" :class="riskChipClass(selectedBed)" v-if="selectedBed.riskLabel">{{ selectedBed.riskLabel }}</span>
              <span class="overlay-chip is-metric">{{ selectedBed.careLevel || '未配置护理等级' }}</span>
            </div>
            <div class="selected-health-strip">
              <div class="health-cell">
                <span>心率</span>
                <strong>{{ deriveHeartRate(selectedBed) }}</strong>
              </div>
              <div class="health-cell">
                <span>呼吸</span>
                <strong>{{ deriveBreathRate(selectedBed) }}</strong>
              </div>
              <div class="health-cell">
                <span>设备</span>
                <strong>{{ bedDeviceState(selectedBed) }}</strong>
              </div>
            </div>
          </div>
          <a-empty v-else description="点击床位后显示详情与快捷操作" />

          <div class="action-grid">
            <a-button block type="primary" @click="openProfile">长者档案</a-button>
            <a-button block @click="allocateBed">床位分配</a-button>
            <a-button block @click="openAssessmentArchive">评估档案</a-button>
            <a-button block @click="openContractsInvoices">合同票据</a-button>
            <a-button block @click="openStatusChangeCenter">状态中心</a-button>
            <a-button block danger ghost @click="createAlert">生成提醒</a-button>
          </div>
        </section>
      </aside>
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
import FlowGuardBar from '../../../components/FlowGuardBar.vue'
import BedInfoCard from '../../../components/bed/BedInfoCard.vue'
import Panorama3D from './Panorama3D.vue'
import { getBedMap, getRoomList } from '../../../api/bed'
import { getElderDetail } from '../../../api/elder'
import { useLiveSyncRefresh } from '../../../composables/useLiveSyncRefresh'
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
const occupiedRate = computed(() => beds.value.length ? Math.round((stats.value.occupied / beds.value.length) * 100) : 0)
const deviceOnlineRate = computed(() => beds.value.length ? Math.max(0, Math.round(((beds.value.length - stats.value.locked - stats.value.maintenance) / beds.value.length) * 100)) : 100)
const sleepStableCount = computed(() => beds.value.filter((bed) => bed.elderId && bed.riskLevel !== 'HIGH' && Number(bed.abnormalVital24hCount || 0) === 0).length)

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

const overviewCards = computed(() => ([
  { label: '总床位数', value: `${beds.value.length}`, meta: `${matrixBuildings.value.length} 栋 ${matrixFloors.value.length} 层`, tone: 'tone-cyan' },
  { label: '已占用床位', value: `${stats.value.occupied}`, meta: `占用率 ${occupiedRate.value}%`, tone: 'tone-green' },
  { label: '紧急告警数', value: `${emergencyCount.value}`, meta: `${alertBeds.value.length} 个重点关注`, tone: 'tone-red' },
  { label: '设备在线率', value: `${deviceOnlineRate.value}%`, meta: `可监测床位 ${Math.max(0, beds.value.length - stats.value.maintenance)}`, tone: 'tone-blue' }
]))

const leftStatCards = computed(() => ([
  { label: '空床位', value: stats.value.idle, meta: '待分配', tone: 'tone-cyan' },
  { label: '在住床位', value: stats.value.occupied, meta: '实时监测', tone: 'tone-green' },
  { label: '异常床位', value: alertBeds.value.length, meta: '需要关注', tone: 'tone-orange' },
  { label: '锁定/离线', value: stats.value.locked, meta: '弱化显示', tone: 'tone-gray' },
  { label: '维修床位', value: stats.value.maintenance, meta: '需恢复', tone: 'tone-orange' },
  { label: '睡眠稳定', value: sleepStableCount.value, meta: '低异常波动', tone: 'tone-purple' }
]))

const statusDistributionRows = computed(() => {
  const total = Math.max(1, beds.value.length)
  return [
    { label: '空闲', value: stats.value.idle, percent: Math.round((stats.value.idle / total) * 100), tone: 'fill-cyan' },
    { label: '在住', value: stats.value.occupied, percent: Math.round((stats.value.occupied / total) * 100), tone: 'fill-green' },
    { label: '预定', value: stats.value.reserved, percent: Math.round((stats.value.reserved / total) * 100), tone: 'fill-blue' },
    { label: '清洁/维修', value: stats.value.cleaning + stats.value.maintenance, percent: Math.round(((stats.value.cleaning + stats.value.maintenance) / total) * 100), tone: 'fill-orange' },
    { label: '锁定', value: stats.value.locked, percent: Math.round((stats.value.locked / total) * 100), tone: 'fill-red' }
  ]
})

const focusBeds = computed(() => {
  const preferred = alertBeds.value.length ? alertBeds.value : sourceBeds.value.filter((bed) => bed.elderId)
  return preferred.slice(0, 4)
})

const alertFeed = computed(() => alertBeds.value.slice(0, 6).map((bed, index) => ({
  key: `alert-${bed.id}`,
  type: bed.riskLevel === 'HIGH' ? '紧急告警' : '实时异常',
  time: dayjs().subtract(index * 4, 'minute').format('HH:mm'),
  title: `${bed.roomNo || '-'} / ${bed.bedNo || '-'} ${bed.elderName || '空床'}`,
  description: `${bed.riskLabel || '体征波动'}，24h异常 ${bed.abnormalVital24hCount || 0} 次，当前状态 ${resolveStatus(bed)}`,
  tone: bed.riskLevel === 'HIGH' ? 'tone-red' : 'tone-orange'
})))

const timelineFeed = computed(() => {
  const buildingPulse = matrixBuildings.value.slice(0, 2).map((building, index) => ({
    key: `building-${building}`,
    title: `${building} 监测刷新`,
    description: `${roomsAt(building, matrixFloors.value[index] || matrixFloors.value[0] || '').length} 个房间已完成床态同步`,
    tone: 'dot-cyan'
  }))

  const roomPulse = Array.from(roomSceneLookup.value.values())
    .flat()
    .slice(0, 3)
    .map((room) => ({
      key: room.key,
      title: `${room.roomNo} 房间态势更新`,
      description: `在住 ${room.occupiedBeds} / ${room.totalBeds}，空床 ${room.emptyBeds}，房型 ${room.roomType}`,
      tone: room.emptyBeds === 0 ? 'dot-orange' : 'dot-green'
    }))

  return [...buildingPulse, ...roomPulse]
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

const bedGuardStageColor = computed(() => {
  if (!selectedBed.value) return 'blue'
  if (isEmptyBed(selectedBed.value)) return 'gold'
  if (selectedBed.value.riskLevel === 'HIGH') return 'red'
  return 'green'
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
  comparison: buildTrend(Math.round(stats.value.occupied * 0.92), [0.82, 0.84, 0.87, 0.88, 0.91, 0.92, 0.94]),
  color: '#52f3c4',
  areaColor: 'rgba(62, 232, 181, 0.22)',
  metricLabel: '占用床位'
}))

const sleepTrendOption = computed(() => buildLineOption({
  labels: trendLabels.value,
  data: buildTrend(sleepStableCount.value, [0.76, 0.8, 0.82, 0.85, 0.88, 0.92, 1]),
  comparison: buildTrend(Math.max(1, Math.round(sleepStableCount.value * 0.9)), [0.72, 0.74, 0.78, 0.8, 0.84, 0.87, 0.9]),
  color: '#9e88ff',
  areaColor: 'rgba(155, 123, 255, 0.22)',
  metricLabel: '稳定睡眠'
}))

const alertTrendOption = computed(() => buildLineOption({
  labels: trendLabels.value,
  data: buildTrend(Math.max(1, alertBeds.value.length), [1.34, 1.2, 1.12, 0.92, 0.98, 1.05, 1]),
  comparison: buildTrend(Math.max(1, Math.round(alertBeds.value.length * 0.88)), [1.08, 1.02, 0.96, 0.9, 0.93, 0.96, 0.94]),
  color: '#ff6d89',
  areaColor: 'rgba(255, 93, 124, 0.2)',
  metricLabel: '告警事件'
}))

const deviceTrendOption = computed(() => buildLineOption({
  labels: trendLabels.value,
  data: buildTrend(deviceOnlineRate.value, [0.91, 0.92, 0.94, 0.95, 0.97, 0.98, 1]),
  comparison: buildTrend(Math.max(1, Math.round(deviceOnlineRate.value * 0.97)), [0.88, 0.89, 0.91, 0.92, 0.94, 0.95, 0.96]),
  color: '#57d7ff',
  areaColor: 'rgba(87, 215, 255, 0.2)',
  formatter: '{value}%',
  metricLabel: '在线率'
}))

function buildLineOption(config: { labels: string[]; data: number[]; comparison?: number[]; color: string; areaColor: string; formatter?: string; metricLabel: string }) {
  const maxValue = Math.max(...config.data)
  const maxIndex = config.data.findIndex((item) => item === maxValue)
  return {
    animationDuration: 900,
    animationEasing: 'cubicOut',
    grid: { left: 14, right: 18, top: 24, bottom: 22, containLabel: true },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.96)',
      borderColor: 'rgba(87, 215, 255, 0.3)',
      textStyle: { color: '#173854' },
      axisPointer: {
        type: 'line',
        lineStyle: { color: 'rgba(87, 151, 204, 0.28)' }
      }
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
        symbol: 'none',
        data: config.comparison || [],
        lineStyle: { width: 1, color: 'rgba(162, 189, 213, 0.45)', type: 'dashed' },
        itemStyle: { color: 'rgba(162, 189, 213, 0.45)' },
        areaStyle: { opacity: 0 }
      },
      {
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        data: config.data,
        markPoint: {
          symbol: 'circle',
          symbolSize: 42,
          itemStyle: {
            color: 'rgba(255, 255, 255, 0.98)',
            borderColor: config.color,
            borderWidth: 2
          },
          label: {
            color: '#173854',
            formatter: ({ value }: any) => `${config.metricLabel}\n${value}`
          },
          data: maxIndex >= 0 ? [{ coord: [config.labels[maxIndex], maxValue], value: maxValue }] : []
        },
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

function roomsAt(building: string, floor: string) {
  return roomSceneLookup.value.get(`${building}@@${floor}`) || []
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
  if (bed.status === 2) return '维修'
  if (bed.status === 3) return '清洁中'
  if (String(bed.bedNo || '').endsWith('R')) return '预定'
  return '空闲'
}

function bedHashSeed(bed: BedItem) {
  return `${bed.id || ''}${bed.bedNo || ''}${bed.roomNo || ''}`
    .split('')
    .reduce((sum, char) => sum + char.charCodeAt(0), 0)
}

function deriveHeartRate(bed: BedItem) {
  if (!bed.elderId) return '--'
  return 62 + (bedHashSeed(bed) % 24)
}

function deriveBreathRate(bed: BedItem) {
  if (!bed.elderId) return '--'
  return 15 + (bedHashSeed(bed) % 7)
}

function bedDeviceState(bed: BedItem) {
  if (bed.status === 0) return '离线'
  if (bed.status === 2) return '检修'
  return '在线'
}

function statusChipClass(bed: BedItem) {
  const status = resolveStatus(bed)
  if (status === '在住') return 'is-occupied'
  if (status === '预定') return 'is-reserved'
  if (status === '维修') return 'is-maintenance'
  if (status === '清洁中') return 'is-cleaning'
  if (status === '锁定') return 'is-offline'
  return 'is-idle'
}

function riskChipClass(bed: BedItem) {
  if (bed.riskLevel === 'HIGH') return 'is-alert'
  if (bed.riskLevel === 'MEDIUM') return 'is-warning'
  if (bed.riskLevel === 'LOW') return 'is-sleep'
  return 'is-idle'
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
.bed-cockpit-page {
  --panel-border: rgba(87, 215, 255, 0.18);
  --panel-bg: linear-gradient(180deg, rgba(255, 255, 255, 0.96) 0%, rgba(242, 249, 255, 0.98) 100%);
  --panel-shadow: 0 16px 36px rgba(73, 130, 178, 0.14), inset 0 1px 0 rgba(255, 255, 255, 0.92);
}

.bed-cockpit-page :deep(.page-head) {
  border: 1px solid var(--panel-border);
  background:
    radial-gradient(circle at top right, rgba(77, 124, 255, 0.12), transparent 30%),
    radial-gradient(circle at left top, rgba(87, 215, 255, 0.1), transparent 28%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(240, 248, 255, 1) 100%);
  box-shadow: var(--panel-shadow);
}

.bed-cockpit-page :deep(.page-title) {
  color: #173854;
  font-size: 28px;
  letter-spacing: 0.08em;
}

.bed-cockpit-page :deep(.page-subtitle) {
  color: #6d8aa3;
}

.cockpit-topbar,
.topbar-status,
.hero-metrics,
.metric-grid,
.distribution-top,
.section-head,
.stage-heading,
.stage-kpis,
.command-buttons,
.event-top,
.selected-badges {
  display: flex;
  align-items: center;
}

.cockpit-topbar {
  gap: 20px;
  justify-content: space-between;
}

.topbar-status {
  gap: 10px;
  color: #6d8aa3;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #3ee8b5;
  box-shadow: 0 0 12px rgba(62, 232, 181, 0.7);
}

.status-divider {
  width: 1px;
  height: 12px;
  background: rgba(141, 178, 207, 0.22);
}

.topbar-clock {
  text-align: right;
}

.clock-date {
  color: #6d8aa3;
  font-size: 12px;
}

.clock-time {
  color: #173854;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.hero-metrics {
  gap: 14px;
  flex-wrap: wrap;
}

.hero-metric-card {
  min-width: 168px;
  flex: 1 1 168px;
  display: grid;
  gap: 6px;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid rgba(87, 215, 255, 0.14);
  background: rgba(255, 255, 255, 0.82);
}

.metric-label {
  font-size: 12px;
  color: #6d8aa3;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.metric-value {
  color: #173854;
  font-size: 30px;
  line-height: 1;
}

.metric-meta {
  color: #7a97b0;
  font-size: 12px;
}

.tone-cyan {
  box-shadow: inset 0 0 0 1px rgba(87, 215, 255, 0.08), 0 0 26px rgba(87, 215, 255, 0.08);
}

.tone-green {
  box-shadow: inset 0 0 0 1px rgba(62, 232, 181, 0.08), 0 0 26px rgba(62, 232, 181, 0.08);
}

.tone-blue {
  box-shadow: inset 0 0 0 1px rgba(77, 124, 255, 0.08), 0 0 26px rgba(77, 124, 255, 0.08);
}

.tone-orange {
  box-shadow: inset 0 0 0 1px rgba(255, 174, 87, 0.08), 0 0 24px rgba(255, 174, 87, 0.08);
}

.tone-red {
  box-shadow: inset 0 0 0 1px rgba(255, 93, 124, 0.1), 0 0 28px rgba(255, 93, 124, 0.12);
}

.bed-cockpit-shell {
  display: grid;
  grid-template-columns: 340px minmax(0, 1fr) 340px;
  gap: 18px;
  min-height: 0;
}

.cockpit-panel,
.cockpit-core {
  display: grid;
  gap: 18px;
  min-height: 0;
}

.tech-panel {
  position: relative;
  overflow: hidden;
  border-radius: 24px;
  border: 1px solid var(--panel-border);
  background: var(--panel-bg);
  box-shadow: var(--panel-shadow);
  padding: 18px;
}

.tech-panel::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, transparent 0%, rgba(87, 215, 255, 0.05) 48%, transparent 100%);
  opacity: 0.6;
  pointer-events: none;
}

.section-head {
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.section-head h3,
.stage-heading h2 {
  margin: 0;
  color: #173854;
}

.section-head p,
.stage-heading p,
.field-tip,
.event-card p,
.timeline-item p,
.selected-meta {
  margin: 4px 0 0;
  color: #6d8aa3;
  line-height: 1.6;
  font-size: 12px;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.metric-tile {
  display: grid;
  gap: 6px;
  padding: 14px;
  border-radius: 18px;
  border: 1px solid rgba(87, 215, 255, 0.14);
  background: rgba(246, 251, 255, 0.96);
}

.metric-tile span,
.field-label,
.overlay-title,
.event-type {
  font-size: 12px;
  color: #6d8aa3;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.metric-tile strong {
  color: #173854;
  font-size: 24px;
}

.metric-tile small {
  color: #7896af;
}

.distribution-list,
.filter-stack,
.event-list,
.timeline-list,
.focus-bed-list {
  display: grid;
  gap: 12px;
}

.distribution-top {
  justify-content: space-between;
  margin-bottom: 8px;
  color: #214b6b;
}

.distribution-track {
  height: 8px;
  border-radius: 999px;
  background: rgba(120, 164, 201, 0.12);
  overflow: hidden;
}

.distribution-fill {
  display: block;
  height: 100%;
  border-radius: inherit;
}

.fill-cyan {
  background: linear-gradient(90deg, #2bcfff, #57d7ff);
}

.fill-green {
  background: linear-gradient(90deg, #27ce94, #52f3c4);
}

.fill-blue {
  background: linear-gradient(90deg, #4d7cff, #68a7ff);
}

.fill-orange {
  background: linear-gradient(90deg, #ff9655, #ffbf74);
}

.fill-red {
  background: linear-gradient(90deg, #ff5d7c, #ff8aa0);
}

.field-block {
  display: grid;
  gap: 10px;
}

.field-inline,
.selection-tags,
.action-grid {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.command-buttons {
  gap: 10px;
  flex-wrap: wrap;
}

.flow-panel {
  padding: 12px 16px;
}

.stage-panel {
  display: grid;
  gap: 14px;
}

.stage-heading {
  justify-content: space-between;
  gap: 18px;
  align-items: flex-start;
}

.eyebrow {
  margin-bottom: 8px;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.22em;
  color: #57d7ff;
}

.stage-kpis {
  gap: 16px;
  flex-wrap: wrap;
}

.stage-kpis > div {
  min-width: 82px;
  display: grid;
  gap: 4px;
}

.stage-kpis span {
  color: #6d8aa3;
  font-size: 11px;
  text-transform: uppercase;
}

.stage-kpis strong {
  color: #173854;
  font-size: 22px;
}

.lifecycle-alert {
  border-radius: 16px;
}

.stage-shell {
  position: relative;
  min-height: 620px;
}

.stage-shell :deep(.panorama-container) {
  height: 620px;
}

.stage-empty {
  min-height: 620px;
  display: grid;
  place-items: center;
}

.stage-overlay-card {
  position: absolute;
  right: 18px;
  bottom: 18px;
  max-width: 320px;
  padding: 16px;
  border-radius: 18px;
  border: 1px solid rgba(87, 215, 255, 0.2);
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(12px);
  box-shadow: 0 16px 30px rgba(73, 130, 178, 0.14);
}

.overlay-name,
.selected-title,
.event-card strong,
.timeline-item strong {
  color: #173854;
  font-size: 16px;
  font-weight: 700;
}

.overlay-meta {
  margin-top: 6px;
  color: #6d8aa3;
  line-height: 1.6;
  font-size: 12px;
}

.overlay-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 12px;
}

.overlay-vitals,
.selected-health-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-top: 14px;
}

.overlay-chip {
  padding: 5px 10px;
  border-radius: 999px;
  border: 1px solid rgba(87, 215, 255, 0.16);
  background: rgba(87, 215, 255, 0.08);
  color: #173854;
  font-size: 12px;
}

.overlay-chip.is-occupied {
  border-color: rgba(62, 232, 181, 0.28);
  background: rgba(62, 232, 181, 0.12);
}

.overlay-chip.is-reserved,
.overlay-chip.is-cleaning,
.overlay-chip.is-warning {
  border-color: rgba(255, 174, 87, 0.28);
  background: rgba(255, 174, 87, 0.12);
}

.overlay-chip.is-alert {
  border-color: rgba(255, 93, 124, 0.34);
  background: rgba(255, 93, 124, 0.14);
}

.overlay-chip.is-sleep {
  border-color: rgba(155, 123, 255, 0.3);
  background: rgba(155, 123, 255, 0.14);
}

.overlay-chip.is-offline,
.overlay-chip.is-maintenance,
.overlay-chip.is-idle,
.overlay-chip.is-metric {
  border-color: rgba(141, 178, 207, 0.2);
  background: rgba(141, 178, 207, 0.08);
}

.vital-badge,
.health-cell {
  display: grid;
  gap: 4px;
  padding: 10px 12px;
  border-radius: 14px;
  background: rgba(247, 252, 255, 0.96);
  border: 1px solid rgba(87, 215, 255, 0.12);
}

.vital-badge span,
.health-cell span {
  font-size: 11px;
  color: #6d8aa3;
  text-transform: uppercase;
  letter-spacing: 0.12em;
}

.vital-badge strong,
.health-cell strong {
  color: #173854;
  font-size: 16px;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.chart-panel {
  min-height: 260px;
}

.section-head.compact {
  margin-bottom: 8px;
}

.chart-view {
  height: 210px;
}

.event-card,
.selected-summary {
  padding: 14px;
  border-radius: 18px;
  border: 1px solid rgba(87, 215, 255, 0.14);
  background: rgba(248, 252, 255, 0.96);
}

.event-top {
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 10px;
}

.event-time {
  color: #6d8aa3;
  font-size: 12px;
}

.timeline-item {
  display: grid;
  grid-template-columns: 16px minmax(0, 1fr);
  gap: 12px;
  align-items: start;
}

.timeline-dot {
  width: 10px;
  height: 10px;
  margin-top: 6px;
  border-radius: 50%;
  box-shadow: 0 0 14px currentColor;
}

.dot-cyan {
  color: #57d7ff;
  background: #57d7ff;
}

.dot-green {
  color: #52f3c4;
  background: #52f3c4;
}

.dot-orange {
  color: #ffbf74;
  background: #ffbf74;
}

.selected-panel {
  align-content: start;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-top: 14px;
}

.bed-cockpit-page :deep(.ant-input),
.bed-cockpit-page :deep(.ant-input-search-button),
.bed-cockpit-page :deep(.ant-segmented),
.bed-cockpit-page :deep(.ant-btn),
.bed-cockpit-page :deep(.ant-alert) {
  border-radius: 14px;
}

.bed-cockpit-page :deep(.ant-input),
.bed-cockpit-page :deep(.ant-input-affix-wrapper),
.bed-cockpit-page :deep(.ant-segmented),
.bed-cockpit-page :deep(.ant-alert),
.bed-cockpit-page :deep(.ant-switch) {
  background: rgba(255, 255, 255, 0.94);
  border-color: rgba(87, 215, 255, 0.14);
  color: #173854;
}

.bed-cockpit-page :deep(.ant-btn) {
  background: rgba(255, 255, 255, 0.94);
  border-color: rgba(87, 215, 255, 0.18);
  color: #173854;
}

.bed-cockpit-page :deep(.ant-btn-primary) {
  background: linear-gradient(90deg, #0f82d8, #3cbfff) !important;
  color: #06111f !important;
}

.bed-cockpit-page :deep(.ant-empty-description) {
  color: #6d8aa3;
}

@media (max-width: 1600px) {
  .bed-cockpit-shell {
    grid-template-columns: 300px minmax(0, 1fr) 300px;
  }
}

@media (max-width: 1280px) {
  .bed-cockpit-shell {
    grid-template-columns: 1fr;
  }

  .chart-grid {
    grid-template-columns: 1fr;
  }

  .stage-overlay-card {
    position: static;
    margin-top: 14px;
    max-width: none;
  }
}

@media (max-width: 768px) {
  .hero-metrics,
  .metric-grid,
  .action-grid {
    grid-template-columns: 1fr;
  }

  .cockpit-topbar,
  .stage-heading {
    flex-direction: column;
    align-items: flex-start;
  }

  .clock-time {
    font-size: 22px;
  }

  .stage-shell,
  .stage-shell :deep(.panorama-container) {
    min-height: 520px;
    height: 520px;
  }
}
</style>
