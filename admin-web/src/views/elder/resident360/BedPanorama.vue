<template>
  <PageContainer class="bed-immersive-page" title="3D床态全景" subTitle="智慧养老数字孪生床态指挥舱">
    <div class="immersive-stage-shell">
      <div class="scene-vignette"></div>
      <div class="scene-glow scene-glow-left"></div>
      <div class="scene-glow scene-glow-right"></div>

      <header class="hud-topbar hud-topbar--command">
        <div class="command-marquee command-marquee--hero">
          <div class="hud-topbar__brand">
            <div class="brand-mark"><span></span></div>
            <div class="brand-copy">
              <div class="brand-kicker">龟峰颐养中心 · 数字孪生</div>
              <strong>长者床态指挥舱</strong>
              <small>{{ lifecycleContext.active ? '当前为入住联动模式，空床调配、房间清洁与状态闭环同步可见。' : '围绕楼栋、楼层、房间与床位建立可钻取的 3D 运营视图。' }}</small>
            </div>
          </div>

          <div class="campus-scope campus-scope--focus">
            <span class="brand-kicker">当前焦点</span>
            <strong>{{ focusHeadline.title }}</strong>
            <small>{{ focusHeadline.meta }}</small>
          </div>
        </div>

        <div class="hud-topbar__ops hud-topbar__ops--command">
          <a-input-search
            v-model:value="keyword"
            class="topbar-search"
            placeholder="搜索长者姓名 / 床位 / 房间号"
            allow-clear
          />
          <div class="status-stack">
            <span class="status-dot"></span>
            <div>
              <strong>{{ commandCenterStatus }}</strong>
              <small>{{ currentDateText }} {{ currentTimeText }}</small>
            </div>
          </div>
        </div>

        <div class="hud-topbar__metrics hud-topbar__metrics--ribbon">
          <button v-for="item in overviewCards" :key="item.label" class="metric-pill" :class="item.tone">
            <span>{{ item.label }}</span>
            <strong><AnimatedMetricNumber :value="item.numericValue" :suffix="item.suffix || ''" /></strong>
            <small>{{ item.meta }}</small>
          </button>
        </div>
      </header>

      <section class="bed-command-bar">
        <div class="bed-command-bar__primary">
          <span class="panel-kicker">床位调度</span>
          <strong>{{ priorityCommand.title }}</strong>
          <p>{{ priorityCommand.description }}</p>
        </div>

        <div class="bed-command-bar__filters">
          <button
            v-for="item in bedStatusLegend"
            :key="item.key"
            type="button"
            class="bed-status-chip"
            :class="[item.tone, { active: item.active }]"
            @click="handleStatusLegendClick(item.key)"
          >
            <span class="bed-status-chip__dot"></span>
            <strong>{{ item.label }}</strong>
            <small>{{ item.value }}</small>
          </button>
        </div>

        <button class="bed-command-bar__cta" type="button" :class="priorityCommand.tone" @click="handleCommandAction(priorityCommand.key)">
          <strong>{{ priorityCommand.action }}</strong>
          <span>{{ priorityCommand.actionHint }}</span>
        </button>
      </section>

        <section class="stage-brief">
          <div class="stage-brief__main">
            <div class="focus-ribbon">
              <div class="focus-ribbon__main">
                <div class="focus-ribbon__item">
                  <span>空间层级</span>
                  <strong>{{ currentScopeLabel }}</strong>
                </div>
                <div class="focus-ribbon__item focus-ribbon__item--wide">
                  <span>当前范围</span>
                  <strong>{{ bedGuardSubject }}</strong>
                </div>
                <div class="focus-ribbon__item">
                  <span>当前动作</span>
                  <strong>{{ focusActionText }}</strong>
                </div>
              </div>
            </div>

            <div class="stage-quick-actions">
              <button
                v-for="item in stageQuickActions"
                :key="item.key"
                class="stage-quick-action"
                :class="item.tone"
                @click="handleCommandAction(item.key)"
              >
                <strong>{{ item.label }}</strong>
                <span>{{ item.description }}</span>
              </button>
            </div>
          </div>

          <div class="stage-brief__side">
            <div class="bed-guard-card">
              <div class="bed-guard-card__head">
                <span class="panel-kicker">流程状态</span>
                <strong>{{ bedGuardStageText }}</strong>
              </div>
              <div class="bed-guard-steps">
                <div
                  v-for="(step, index) in bedGuardSteps"
                  :key="step"
                  class="bed-guard-step"
                  :class="{
                    'is-current': index === bedGuardCurrentIndex,
                    'is-done': index < bedGuardCurrentIndex
                  }"
                >
                  <span>{{ index + 1 }}</span>
                  <strong>{{ step }}</strong>
                </div>
              </div>
              <div v-if="bedGuardBlockers.length" class="bed-guard-blockers">
                <div v-for="item in bedGuardBlockers" :key="item.code" class="bed-guard-blocker">
                  <div>
                    <strong>{{ item.code }}</strong>
                    <p>{{ item.text }}</p>
                  </div>
                  <a-button v-if="item.actionKey" size="small" @click="handleBedGuardAction(item)">
                    {{ item.actionLabel }}
                  </a-button>
                </div>
              </div>
              <div v-else class="bed-guard-blockers bed-guard-blockers--quiet">
                <p>{{ bedGuardHint }}</p>
              </div>
            </div>
          </div>
        </section>

      <div class="dashboard-main">
        <button
          type="button"
          class="overlay-toggle overlay-toggle--left"
          :class="{ 'is-shifted': leftPanelOpen }"
          @click="leftPanelOpen = !leftPanelOpen"
        >{{ leftPanelOpen ? '‹ 收起' : '空间导航 ›' }}</button>
        <button
          v-if="!rightPanelOpen"
          type="button"
          class="overlay-toggle overlay-toggle--right"
          @click="rightPanelOpen = true"
        >‹ 床位详情</button>

        <aside class="hud-left" :class="{ 'is-closed': !leftPanelOpen }">
          <section class="hud-panel hud-panel--sidebar">
            <div class="hud-panel__head">
              <div>
                <span class="panel-kicker">楼栋 / 楼层</span>
                <strong>空间导航</strong>
              </div>
              <small>{{ roomCount }} 间房 / {{ sourceBeds.length }} 张床位</small>
            </div>

            <div class="campus-tree">
              <div v-for="building in campusTree" :key="building.name" class="tree-building">
                <button class="tree-building__head" :class="{ active: building.name === selectedBuilding }" @click="setBuildingScope(building.name)">
                  <div>
                    <strong>{{ building.name }}</strong>
                    <small>{{ building.rooms }} 间房 · {{ building.beds }} 张床位</small>
                  </div>
                  <span>{{ building.occupied }}/{{ building.beds }}</span>
                </button>

                <div class="tree-floor-list" v-if="!selectedBuilding || selectedBuilding === building.name">
                  <button
                    v-for="floor in building.floors"
                    :key="`${building.name}-${floor.name}`"
                    class="tree-floor"
                    :class="{ active: floor.name === selectedFloor && building.name === selectedBuilding }"
                    @click="selectedBuilding === building.name ? setFloorScope(floor.name) : setBuildingScope(building.name)"
                  >
                    <span>{{ floor.name }}</span>
                    <small>{{ floor.occupied }}/{{ floor.beds }}</small>
                  </button>
                </div>
              </div>
            </div>

            <div class="sidebar-section">
              <div class="sidebar-section__head">
                <span class="panel-kicker">床位状态</span>
                <strong>快速过滤</strong>
              </div>
              <a-segmented v-model:value="quickFilter" :options="quickFilterOptions" block />
            </div>

            <div class="sidebar-section">
              <div class="field-inline">
                <div>
                  <span class="panel-kicker">风险筛选</span>
                  <strong>重点照护</strong>
                </div>
                <a-switch v-model:checked="riskFilterEnabled" />
              </div>
              <a-segmented
                v-if="riskFilterEnabled"
                v-model:value="riskFilterLevel"
                :options="riskLevelOptions"
                block
              />
            </div>

            <div class="sidebar-section">
              <div class="sidebar-section__head">
                <span class="panel-kicker">运营口径</span>
                <strong>占床与在住</strong>
              </div>
              <div class="sidebar-mini-grid">
                <div v-for="item in leftStatCards" :key="item.label" class="sidebar-mini-card" :class="item.tone">
                  <span>{{ item.label }}</span>
                  <strong>{{ item.value }}</strong>
                  <small>{{ item.meta }}</small>
                </div>
              </div>
              <div class="metric-note-card" :class="{ 'is-warning': occupancyDriftCount > 0 }">
                <strong>{{ occupancyDriftCount > 0 ? '占床口径高于在住口径' : '当前口径已对齐' }}</strong>
                <p>{{ occupancyDriftDescription }}</p>
              </div>
            </div>

            <div class="sidebar-section">
              <div class="sidebar-section__head">
                <span class="panel-kicker">床态分布</span>
                <strong>运营概览</strong>
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
            </div>

            <div class="sidebar-section">
              <div class="sidebar-section__head">
                <span class="panel-kicker">AI 指挥建议</span>
                <strong>优先动作</strong>
              </div>
              <div class="suggestion-list">
                <div v-for="item in aiSuggestionFeed" :key="item.key" class="suggestion-card" :class="item.tone">
                  <strong>{{ item.title }}</strong>
                  <p>{{ item.description }}</p>
                </div>
              </div>
            </div>
          </section>
        </aside>

        <section class="dashboard-stage">
          <div class="stage-canvas-shell">
            <Panorama3D
              v-if="matrixBuildings.length && matrixFloors.length"
              :buildings="matrixBuildings"
              :floors="matrixFloors"
              :room-lookup="roomSceneLookup"
              :scope="panoramaScope"
              @scope-change="handleScopeChange"
              @click-room="openRoomDetail"
              @click-bed="selectBed"
            />
            <a-empty v-else class="stage-empty" description="暂无床位数据" />
          </div>
        </section>

        <aside class="hud-right" :class="{ 'is-closed': !rightPanelOpen }">
          <section class="hud-panel hud-panel--context">
            <div class="hud-panel__head">
              <div>
                <span class="panel-kicker">床位详情</span>
                <strong>{{ activeResidentBed?.elderName || '未选中长者' }}</strong>
              </div>
              <div class="context-head-ops">
                <small>{{ activeResidentBed?.roomNo || '-' }} / {{ activeResidentBed?.bedNo || '-' }}</small>
                <a-button v-if="activeResidentBed" size="small" @click="detailOpen = true">全部操作</a-button>
                <a-button size="small" type="text" @click="rightPanelOpen = false">收起 ›</a-button>
              </div>
            </div>

            <template v-if="activeResidentBed">
              <div class="context-tone-strip">
                <div>
                  <span>床位状态</span>
                  <strong>{{ resolveStatus(activeResidentBed) }}</strong>
                </div>
                <div>
                  <span>长者口径</span>
                  <strong>{{ activeResidentBed.elderLifecycleStatus === 'IN_HOSPITAL' ? '在住长者' : '占床长者' }}</strong>
                </div>
                <div>
                  <span>当前风险</span>
                  <strong>{{ activeResidentBed.riskLabel || '平稳' }}</strong>
                </div>
              </div>

              <div class="resident-profile">
                <div class="resident-profile__avatar">
                  <a-avatar :size="72" class="resident-avatar">{{ residentAvatarText }}</a-avatar>
                </div>
                <div class="resident-profile__body">
                  <div class="resident-profile__title">
                    <strong>{{ activeResidentBed.elderName || '未命名长者' }}</strong>
                    <span class="detail-chip" :class="residentHealthTone">{{ residentHealthText }}</span>
                  </div>
                  <div class="resident-profile__meta">
                    <span>{{ residentAgeText }}</span>
                    <span>{{ residentGenderText }}</span>
                    <span>{{ activeResidentBed.careLevel || '护理等级待同步' }}</span>
                  </div>
                  <div class="detail-tags">
                    <span class="overlay-chip">{{ resolveStatus(activeResidentBed) }}</span>
                    <span class="overlay-chip">{{ activeResidentBed.riskLabel || '生命体征平稳' }}</span>
                    <span class="overlay-chip">{{ activeResidentBed.latestAssessmentLevel || '评估待更新' }}</span>
                  </div>
                </div>
              </div>

              <div class="detail-grid detail-grid--vitals">
                <div v-for="item in residentVitals" :key="item.label">
                  <span>{{ item.label }}</span>
                  <strong>{{ item.value }}</strong>
                </div>
              </div>

              <div class="detail-section">
                <div class="detail-section__head">
                  <span class="panel-kicker">照护状态</span>
                  <strong>床位与健康摘要</strong>
                </div>
                <div class="detail-grid">
                  <div>
                    <span>楼栋楼层</span>
                    <strong>{{ activeResidentBed.building || '-' }} / {{ activeResidentBed.floorNo || '-' }}</strong>
                  </div>
                  <div>
                    <span>24h异常</span>
                    <strong>{{ activeResidentBed.abnormalVital24hCount || 0 }} 次</strong>
                  </div>
                  <div>
                    <span>风险来源</span>
                    <strong>{{ activeResidentBed.riskSource || '常规巡护' }}</strong>
                  </div>
                  <div>
                    <span>最近评估</span>
                    <strong>{{ formatLatestAssessment(activeResidentBed) }}</strong>
                  </div>
                </div>
              </div>

              <div class="detail-section">
                <div class="detail-section__head">
                  <span class="panel-kicker">最近提醒</span>
                  <strong>护理关注</strong>
                </div>
                <div class="event-list">
                  <div v-for="item in residentAlertItems" :key="item.key" class="event-card event-card--soft" :class="item.tone">
                    <div class="event-top">
                      <span class="event-type">{{ item.type }}</span>
                      <span class="event-time">{{ item.time }}</span>
                    </div>
                    <strong>{{ item.title }}</strong>
                    <p>{{ item.description }}</p>
                  </div>
                </div>
              </div>

              <div class="detail-section">
                <div class="detail-section__head">
                  <span class="panel-kicker">快捷操作</span>
                  <strong>照护动作</strong>
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
              </div>
            </template>

            <template v-else>
              <div class="empty-drawer">
                <strong>选择床位查看长者详情</strong>
                <p>右侧将展示长者档案、健康状态、体征快照与护理操作入口。</p>
              </div>
            </template>
          </section>
        </aside>
      </div>

      <footer class="hud-bottom" :class="{ 'is-open': eventDockOpen }">
        <button type="button" class="dock-pill" @click="eventDockOpen = !eventDockOpen">
          <span class="dock-pill__dot" :class="{ 'is-alert': eventStreamItems.length > 0 }"></span>
          异常提醒 {{ eventStreamItems.length }} 条
          <small>{{ eventDockOpen ? '收起' : '展开' }}</small>
        </button>
        <section v-show="eventDockOpen" class="activity-dock">
          <div class="activity-dock__head">
            <div>
              <span class="panel-kicker">异常提醒</span>
              <strong>外出 / 夜巡 / 清洁 / 应急通知</strong>
            </div>
            <small>{{ eventStreamItems.length }} 条待关注事件</small>
          </div>

          <div class="activity-stream">
            <button
              v-for="item in eventStreamItems"
              :key="item.key"
              class="stream-card"
              :class="item.tone"
              @click="item.bed ? selectBed(item.bed) : undefined"
            >
              <div class="stream-card__meta">
                <span>{{ item.type }}</span>
                <strong>{{ item.count }}</strong>
              </div>
              <strong>{{ item.title }}</strong>
              <p>{{ item.description }}</p>
            </button>
          </div>
        </section>
      </footer>
    </div>

    <a-modal v-model:open="detailOpen" title="床位详情" width="560px" :footer="null" destroy-on-close>
      <a-descriptions v-if="selectedBed" :column="1" size="small" bordered>
        <a-descriptions-item label="床位">{{ selectedBed.bedNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="楼栋/楼层/房间">{{ selectedBed.building || '-' }} / {{ selectedBed.floorNo || '-' }} / {{ selectedBed.roomNo || '-' }}</a-descriptions-item>
        <a-descriptions-item label="占床长者">{{ selectedBed.elderName || '-' }}</a-descriptions-item>
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
        <a-button block @click="goScan">扫码执行（定位今日任务）</a-button>
        <a-button block danger @click="createAlert">生成提醒并进入任务中心</a-button>
      </a-space>
    </a-modal>

    <a-modal v-model:open="roomDetailOpen" :title="`房间详情 · ${selectedRoom?.roomNo || '-'}`" width="760px" :footer="null" destroy-on-close>
      <a-descriptions bordered size="small" :column="2" style="margin-bottom: 12px">
        <a-descriptions-item label="房型">{{ resolveRoomTypeLabel(selectedRoom?.roomType) }}</a-descriptions-item>
        <a-descriptions-item label="容量">{{ selectedRoom?.capacity || 0 }} 床</a-descriptions-item>
        <a-descriptions-item label="占床人数">{{ selectedRoom?.elderCount || 0 }} 人</a-descriptions-item>
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
            <a-avatar style="background-color: var(--info)">{{ String(record?.fullName || '?').slice(-1) }}</a-avatar>
          </template>
        </a-table-column>
        <a-table-column title="姓名" data-index="fullName" key="fullName" width="110" />
        <a-table-column title="生日" data-index="birthDate" key="birthDate" width="120" />
        <a-table-column title="家庭住址" data-index="homeAddress" key="homeAddress" />
        <a-table-column title="备注" data-index="remark" key="remark" />
        <a-table-column title="操作" key="action" width="180">
          <template #default="{ record }">
            <div class="row-action-links">
              <a-button type="link" size="small" @click="openElderProfile(record.id)">详情</a-button>
              <a-button type="link" size="small" @click="openResidentBills(record.id)">账单</a-button>
            </div>
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

type PanoramaScopeLevel = 'PARK' | 'BUILDING' | 'FLOOR' | 'ROOM'
type PanoramaScope = {
  level: PanoramaScopeLevel
  building: string
  floor: string
  room: string
}

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const beds = ref<BedItem[]>([])
const roomTypeMap = ref<Record<string, string>>({})
const roomCapacityMap = ref<Record<string, number>>({})
const keyword = ref('')
const selectedBed = ref<BedItem | null>(null)
// 沉浸式布局：3D 画布全屏为主体，左右面板与事件坞均为可收起的悬浮层
const leftPanelOpen = ref(true)
const rightPanelOpen = ref(false)
const eventDockOpen = ref(false)
const detailOpen = ref(false)
const roomDetailOpen = ref(false)
const selectedRoom = ref<RoomScene | null>(null)
const selectedRoomNo = ref('')
const roomResidents = ref<ElderItem[]>([])
const roomResidentLoading = ref(false)
const quickFilter = ref<'ALL' | 'IDLE' | 'OCCUPIED' | 'ALERT'>('ALL')
const riskFilterEnabled = ref(false)
const riskFilterLevel = ref<'ALL' | 'HIGH' | 'MEDIUM' | 'LOW'>('ALL')
const riskDataLoading = ref(false)
const riskDataReady = ref(false)
const selectedBuilding = ref('')
const selectedFloor = ref('')
const currentTime = ref(dayjs())
const defaultScopeApplied = ref(false)
const activeTrendKey = ref<'occupancy' | 'sleep' | 'alert' | 'device'>('occupancy')
const quickFilterOptions = [
  { label: '全部', value: 'ALL' },
  { label: '仅空床', value: 'IDLE' },
  { label: '仅入住', value: 'OCCUPIED' },
  { label: '仅异常', value: 'ALERT' }
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
    if (quickFilter.value === 'ALERT') return isAlertBed(item)
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
    if (b.elderId) s.occupied += 1
    if (st === '维修') s.maintenance += 1
    if (st === '清洁中') s.cleaning += 1
    if (st === '锁定') s.locked += 1
  })
  return s
})

const inHospitalResidentCount = computed(() => beds.value.filter((bed) => {
  if (!bed.elderId) return false
  return String(bed.elderLifecycleStatus || '').trim().toUpperCase() === 'IN_HOSPITAL'
}).length)
const occupancyDriftCount = computed(() => Math.max(stats.value.occupied - inHospitalResidentCount.value, 0))
const occupancyDriftDescription = computed(() => {
  if (!stats.value.occupied) return '当前没有占床长者。'
  if (!occupancyDriftCount.value) return `当前 ${inHospitalResidentCount.value} 位在住长者与占床口径一致。`
  return `当前有 ${stats.value.occupied} 位占床长者，其中 ${inHospitalResidentCount.value} 位为在住，另有 ${occupancyDriftCount.value} 位处于外出、待退住或占床未释放状态。`
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
const currentScope = computed<PanoramaScope>(() => {
  const building = selectedBuilding.value
  const floor = selectedFloor.value
  const room = selectedRoomNo.value
  let level: PanoramaScopeLevel = 'PARK'
  if (building) level = 'BUILDING'
  if (building && floor) level = 'FLOOR'
  if (building && floor && room) level = 'ROOM'
  return { level, building, floor, room }
})
const panoramaScope = computed(() => ({ ...currentScope.value }))
const currentScopeLabel = computed(() => {
  if (currentScope.value.level === 'PARK') return '园区总览'
  if (currentScope.value.level === 'BUILDING') return '楼栋观察'
  if (currentScope.value.level === 'FLOOR') return '楼层展开'
  return '房间聚焦'
})
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
  { label: '总床位', numericValue: beds.value.length, meta: `${matrixBuildings.value.length} 栋 ${matrixFloors.value.length} 层`, tone: 'tone-blue' },
  { label: '床位占用率', numericValue: occupiedRate.value, suffix: '%', meta: `${stats.value.occupied} 张已占用床位`, tone: 'tone-green' },
  { label: '在住长者', numericValue: inHospitalResidentCount.value, meta: occupancyDriftCount.value ? `与占床相差 ${occupancyDriftCount.value} 位` : '与占床口径一致', tone: 'tone-cyan' },
  { label: '高风险长者', numericValue: concernCount.value, meta: `${alertBeds.value.length} 位重点关注`, tone: 'tone-red' },
  { label: '待处理提醒', numericValue: emergencyCount.value, meta: `${alertFeed.value.length} 条待闭环`, tone: 'tone-orange' },
  { label: '维修床位', numericValue: stats.value.maintenance, meta: '待恢复上线', tone: 'tone-purple' }
]))

const bedStatusLegend = computed(() => [
  { key: 'ALL', label: '全部', value: `${beds.value.length}床`, tone: 'tone-blue', active: quickFilter.value === 'ALL' && !riskFilterEnabled.value },
  { key: 'IDLE', label: '空床', value: `${stats.value.idle}床`, tone: 'tone-gray', active: quickFilter.value === 'IDLE' },
  { key: 'OCCUPIED', label: '在住', value: `${stats.value.occupied}床`, tone: 'tone-green', active: quickFilter.value === 'OCCUPIED' },
  { key: 'ALERT', label: '异常', value: `${concernCount.value}条`, tone: 'tone-red', active: quickFilter.value === 'ALERT' || riskFilterEnabled.value },
  { key: 'MAINTENANCE', label: '维修', value: `${stats.value.maintenance}床`, tone: 'tone-orange', active: false }
])

const priorityCommand = computed(() => {
  const bed = selectedBed.value || activeResidentBed.value
  if (bed?.riskLevel === 'HIGH') {
    return {
      key: 'create-alert',
      title: `${bed.elderName || bed.bedNo || '当前床位'}存在高优先风险`,
      description: `24h异常 ${bed.abnormalVital24hCount || 0} 次，建议先生成提醒并同步值班人员。`,
      action: '生成提醒',
      actionHint: '进入任务闭环',
      tone: 'tone-red'
    }
  }
  if (bed && isEmptyBed(bed)) {
    return {
      key: 'allocate-bed',
      title: `${bed.roomNo || '-'} / ${bed.bedNo || '-'} 可用于入住调配`,
      description: '当前为空床，优先确认清洁、锁床预定或直接办理入住。',
      action: '办理入住',
      actionHint: '分配长者到此床',
      tone: 'tone-cyan'
    }
  }
  if (bed?.elderId) {
    return {
      key: 'open-profile',
      title: `${bed.elderName || '在住长者'}正在照护中`,
      description: `${bed.roomNo || '-'} / ${bed.bedNo || '-'}，可查看档案、护理记录、评估与状态变更。`,
      action: '查看长者中心',
      actionHint: '进入照护详情',
      tone: 'tone-green'
    }
  }
  if (concernCount.value > 0) {
    return {
      key: 'filter-alerts',
      title: `当前有 ${concernCount.value} 个床位需关注`,
      description: '优先筛选异常床位，确认生命体征、离床观察和后勤状态。',
      action: '只看异常',
      actionHint: '聚焦风险床位',
      tone: 'tone-red'
    }
  }
  if (stats.value.idle > 0) {
    return {
      key: 'filter-idle',
      title: `当前有 ${stats.value.idle} 张空床可调度`,
      description: '适合前台、销售或入住办理人员快速确认可用床位。',
      action: '只看空床',
      actionHint: '准备入住调配',
      tone: 'tone-cyan'
    }
  }
  return {
    key: 'open-map',
    title: '当前床态运行平稳',
    description: '可切换平面房态图，进行更高效的日常床位调度。',
    action: '平面房态',
    actionHint: '切到房态视图',
    tone: 'tone-blue'
  }
})

const leftStatCards = computed(() => ([
  { label: '空床位', value: stats.value.idle, meta: '待迎接入住', tone: 'tone-gray' },
  { label: '占床长者', value: stats.value.occupied, meta: '床位已被占用', tone: 'tone-cyan' },
  { label: '在住长者', value: inHospitalResidentCount.value, meta: '长者列表在住口径', tone: 'tone-green' },
  { label: '离床观察', value: awayObservationCount.value, meta: '夜巡重点', tone: 'tone-orange' },
  { label: '需关注长者', value: concernCount.value, meta: '风险与异常联动', tone: 'tone-red' },
  { label: '维修设备', value: stats.value.maintenance, meta: '待恢复上线', tone: 'tone-orange' },
  { label: '睡眠稳定', value: sleepStableCount.value, meta: '夜间状态平稳', tone: 'tone-deep-blue' }
]))

const buildingScopeOptions = computed(() => matrixBuildings.value.slice(0, 6))

const campusTree = computed(() => matrixBuildings.value.slice(0, 6).map((building) => {
  const buildingBeds = sourceBeds.value.filter((item) => String(item.building || '') === building)
  const floors = matrixFloors.value
    .filter((floor) => buildingBeds.some((item) => String(item.floorNo || '') === floor))
    .map((floor) => {
      const floorBeds = buildingBeds.filter((item) => String(item.floorNo || '') === floor)
      return {
        name: floor,
        beds: floorBeds.length,
        occupied: floorBeds.filter((item) => item.elderId).length
      }
    })

  return {
    name: building,
    floors,
    rooms: new Set(buildingBeds.map((item) => String(item.roomNo || '').trim()).filter(Boolean)).size,
    beds: buildingBeds.length,
    occupied: buildingBeds.filter((item) => item.elderId).length
  }
}))

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
    { label: '占床床位', value: stats.value.occupied, percent: Math.round((stats.value.occupied / total) * 100), tone: 'fill-cyan' },
    { label: 'AI关注', value: aiFocusCount.value, percent: Math.round((aiFocusCount.value / total) * 100), tone: 'fill-purple' },
    { label: '离床观察', value: awayObservationCount.value, percent: Math.round((awayObservationCount.value / total) * 100), tone: 'fill-orange' },
    { label: '实时风险提醒', value: concernCount.value, percent: Math.round((concernCount.value / total) * 100), tone: 'fill-red' }
  ]
})

const focusBeds = computed(() => {
  const preferred = alertBeds.value.length ? alertBeds.value : sourceBeds.value.filter((bed) => bed.elderId)
  return preferred.slice(0, 4)
})

const activeResidentBed = computed(() => selectedBed.value || focusBeds.value[0] || sourceBeds.value.find((bed) => bed.elderId) || null)

function buildStableNumber(seed: string, min: number, max: number) {
  const source = String(seed || '0')
  const code = source.split('').reduce((sum, char) => sum + char.charCodeAt(0), 0)
  return min + (code % Math.max(1, max - min + 1))
}

const residentAvatarText = computed(() => String(activeResidentBed.value?.elderName || '?').slice(-1))
const residentAgeText = computed(() => activeResidentBed.value ? `${buildStableNumber(String(activeResidentBed.value.elderId || activeResidentBed.value.id), 72, 96)}岁` : '--')
const residentGenderText = computed(() => {
  if (!activeResidentBed.value) return '--'
  return activeResidentBed.value.elderGender === 1 ? '男' : activeResidentBed.value.elderGender === 2 ? '女' : '性别待同步'
})
const residentHealthTone = computed(() => {
  if (!activeResidentBed.value) return 'tone-gray'
  if (activeResidentBed.value.riskLevel === 'HIGH') return 'tone-red'
  if (activeResidentBed.value.riskLevel === 'MEDIUM') return 'tone-purple'
  if (activeResidentBed.value.status === 2) return 'tone-orange'
  return 'tone-green'
})
const residentHealthText = computed(() => {
  if (!activeResidentBed.value) return '待同步'
  if (activeResidentBed.value.riskLevel === 'HIGH') return '高风险'
  if (activeResidentBed.value.riskLevel === 'MEDIUM') return '观察中'
  if (!activeResidentBed.value.elderId) return '空床待命'
  return '状态平稳'
})
const residentVitals = computed(() => {
  const bed = activeResidentBed.value
  if (!bed) {
    return [
      { label: '体温', value: '--' },
      { label: '心率', value: '--' },
      { label: '血压', value: '--' },
      { label: '血氧', value: '--' }
    ]
  }
  const seed = String(bed.elderId || bed.id)
  const temperature = bed.riskLevel === 'HIGH' ? 37.4 : bed.riskLevel === 'MEDIUM' ? 36.9 : 36.5
  const heartRate = buildStableNumber(seed, bed.riskLevel === 'HIGH' ? 96 : 72, bed.riskLevel === 'HIGH' ? 118 : 88)
  const oxygen = buildStableNumber(seed, bed.riskLevel === 'HIGH' ? 90 : 96, bed.riskLevel === 'HIGH' ? 95 : 99)
  const systolic = buildStableNumber(seed, bed.riskLevel === 'HIGH' ? 138 : 112, bed.riskLevel === 'HIGH' ? 156 : 126)
  const diastolic = buildStableNumber(seed, bed.riskLevel === 'HIGH' ? 82 : 68, bed.riskLevel === 'HIGH' ? 98 : 82)
  return [
    { label: '体温', value: `${temperature.toFixed(1)}°C` },
    { label: '心率', value: `${heartRate} 次/分` },
    { label: '血压', value: `${systolic}/${diastolic} mmHg` },
    { label: '血氧', value: `${oxygen}%` }
  ]
})

const alertFeed = computed(() => alertBeds.value.slice(0, 6).map((bed, index) => ({
  key: `alert-${bed.id}`,
  type: bed.riskLevel === 'HIGH' ? '高优先提醒' : '实时提醒',
  time: dayjs().subtract(index * 4, 'minute').format('HH:mm'),
  title: `${bed.roomNo || '-'} / ${bed.bedNo || '-'} ${bed.elderName || '空床'}`,
  description: `${bed.riskLabel || '体征波动'}，24h异常 ${bed.abnormalVital24hCount || 0} 次，当前床态 ${resolveStatus(bed)}`,
  tone: bed.riskLevel === 'HIGH' ? 'tone-red' : 'tone-orange'
})))

const residentAlertItems = computed(() => {
  const bed = activeResidentBed.value
  if (!bed) return []
  return [
    {
      key: `profile-alert-${bed.id}-1`,
      type: bed.riskLevel === 'HIGH' ? '高风险' : '巡护提醒',
      time: dayjs().subtract(12, 'minute').format('HH:mm'),
      title: `${bed.roomNo || '-'} / ${bed.bedNo || '-'} ${bed.elderName || '空床'}`,
      description: bed.riskLabel || bedGuardHint.value,
      tone: bed.riskLevel === 'HIGH' ? 'tone-red' : 'tone-orange'
    },
    {
      key: `profile-alert-${bed.id}-2`,
      type: '生命体征',
      time: dayjs().subtract(34, 'minute').format('HH:mm'),
      title: '近次体征快照',
      description: `体温 ${residentVitals.value[0].value} · 心率 ${residentVitals.value[1].value} · 血氧 ${residentVitals.value[3].value}`,
      tone: 'tone-blue'
    }
  ]
})

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

const eventStreamItems = computed(() => {
  const focusBed = focusBeds.value[0] || activeResidentBed.value || null
  return [
    {
      key: 'stream-alerts',
      type: '床位预警',
      count: `${Math.max(alertFeed.value.length, concernCount.value)}`,
      title: '高风险与异常提醒',
      description: alertFeed.value[0]?.description || '重点监测床位生命体征波动与离床异常。',
      tone: 'tone-red',
      bed: focusBed
    },
    {
      key: 'stream-night',
      type: '夜巡提醒',
      count: `${awayObservationCount.value}`,
      title: '夜间离床观察',
      description: awayObservationCount.value ? `当前有 ${awayObservationCount.value} 张床位进入夜巡优先名单。` : '当前夜巡节奏平稳，可按既定计划执行巡视。',
      tone: 'tone-orange',
      bed: focusBed
    },
    {
      key: 'stream-cleaning',
      type: '清洁任务',
      count: `${stats.value.idle}`,
      title: '待清洁床位',
      description: stats.value.idle ? `有 ${stats.value.idle} 张空床待整理，可同步安排保洁与入住所需准备。` : '当前空床较少，清洁任务压力较低。',
      tone: 'tone-blue',
      bed: null
    },
    {
      key: 'stream-emergency',
      type: '应急通知',
      count: `${emergencyCount.value}`,
      title: '值班协同与应急广播',
      description: emergencyCount.value ? `存在 ${emergencyCount.value} 条高优先提醒，建议联动护理与值班医生。` : '暂无高优先应急通知，系统运行稳定。',
      tone: 'tone-green',
      bed: focusBed
    }
  ]
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
  if (currentScope.value.level === 'ROOM') {
    return `${currentScope.value.building} / ${currentScope.value.floor} / ${currentScope.value.room}`
  }
  if (currentScope.value.level === 'FLOOR') {
    return `${currentScope.value.building} / ${currentScope.value.floor}`
  }
  if (currentScope.value.level === 'BUILDING') {
    return currentScope.value.building
  }
  return '全部楼栋 / 全部楼层'
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
  if (quickFilter.value === 'OCCUPIED' && stats.value.occupied === 0) blockers.push({ code: 'B203', text: '当前无占床床位' })
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
    title: currentScope.value.level === 'PARK'
      ? '园区总览'
      : [currentScope.value.building, currentScope.value.floor, currentScope.value.room].filter(Boolean).join(' / '),
    meta: currentScope.value.level === 'PARK'
      ? '点击楼栋、楼层、房间或床位，快速进入对应视角'
      : `当前范围 ${roomCount.value} 间房 / ${sourceBeds.value.length} 张床位`
  }
})

const focusActionText = computed(() => {
  if (selectedBed.value) return selectedBed.value.elderId ? '查看守护详情' : '执行床位分配'
  if (currentScope.value.level === 'ROOM') return '再次点击房间可打开详情'
  if (currentScope.value.level === 'FLOOR') return '继续选择房间或床位'
  if (currentScope.value.level === 'BUILDING') return '展开楼层爆炸视图'
  return '点击楼栋进入园区结构'
})

const stageQuickActions = computed(() => ([
  {
    key: priorityCommand.value.key,
    label: priorityCommand.value.action,
    description: priorityCommand.value.actionHint,
    tone: priorityCommand.value.tone
  },
  { key: 'filter-alerts', label: '风险床位', description: `${concernCount.value} 个需关注`, tone: 'tone-red' },
  { key: 'open-map', label: '房态视图', description: '切到平面房态图', tone: 'tone-blue' },
  { key: 'reset-filters', label: '重置筛选', description: '回到总览范围', tone: 'tone-cyan' },
  { key: 'open-manage', label: '床位管理', description: '执行基础维护', tone: 'tone-green' }
]))

const commandDeck = computed(() => {
  const bed = activeResidentBed.value
  if (!bed) {
    return [
      { key: 'filter-idle', label: '查看空床', description: '快速定位可入住床位', tone: 'tone-cyan' },
      { key: 'filter-alerts', label: '风险床位', description: '聚焦异常与高风险', tone: 'tone-red' },
      { key: 'open-map', label: '平面房态', description: '切到日常调度视图', tone: 'tone-blue' },
      { key: 'open-manage', label: '床位管理', description: '维护楼层房间床位', tone: 'tone-green' }
    ]
  }
  if (isEmptyBed(bed)) {
    return [
      { key: 'allocate-bed', label: '办理入住', description: '为空床发起入住分配', tone: 'tone-cyan' },
      { key: 'open-contracts', label: '锁床预定', description: '关联销售线索或合同', tone: 'tone-green' },
      { key: 'open-manage', label: '维修登记', description: '标记维修、清洁或停用', tone: 'tone-orange' },
      { key: 'create-alert', label: '生成清洁提醒', description: '同步保洁或后勤任务', tone: 'tone-blue' }
    ]
  }
  return [
    { key: 'open-profile', label: '长者中心', description: '档案、照护和健康总览', tone: 'tone-blue' },
    { key: 'open-status-center', label: '状态变更', description: '外出、换床、退住闭环', tone: 'tone-orange' },
    { key: 'open-assessment', label: '评估档案', description: '查看能力评估与等级', tone: 'tone-purple' },
    { key: 'open-contracts', label: '合同票据', description: '处理合同、账单和押金', tone: 'tone-green' },
    { key: 'create-alert', label: bed.riskLevel === 'HIGH' ? '立即提醒' : '生成提醒', description: '同步推送护理任务', tone: 'tone-red' }
  ]
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

const trendOptions = [
  { label: '床态趋势', value: 'occupancy' },
  { label: '夜巡观察', value: 'sleep' },
  { label: '响应效率', value: 'alert' },
  { label: '在线状态', value: 'device' }
]

const activeTrendPanel = computed(() => {
  if (activeTrendKey.value === 'sleep') {
    return {
      eyebrow: '夜间离床统计',
      title: '夜巡观察参考',
      summary: `离床观察 ${awayObservationCount.value} 项，适合排班夜巡优先核查`,
      option: sleepTrendOption.value
    }
  }
  if (activeTrendKey.value === 'alert') {
    return {
      eyebrow: '护理响应效率',
      title: '风险任务闭环',
      summary: `${concernCount.value} 条提醒待跟进，聚焦异常干预速度`,
      option: alertTrendOption.value
    }
  }
  if (activeTrendKey.value === 'device') {
    return {
      eyebrow: '风险热力趋势',
      title: '守护在线状态',
      summary: `设备在线率 ${deviceOnlineRate.value}% ，重点关注维修与锁定状态`,
      option: deviceTrendOption.value
    }
  }
  return {
    eyebrow: '24小时床态趋势',
    title: '占床与空床节奏',
    summary: `${stats.value.occupied} 张占床，${stats.value.idle} 张空床，适合总览调配节奏`,
    option: occupancyTrendOption.value
  }
})

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
  if (action === 'filter-idle') {
    quickFilter.value = 'IDLE'
    riskFilterEnabled.value = false
    return
  }
  if (action === 'filter-occupied') {
    quickFilter.value = 'OCCUPIED'
    riskFilterEnabled.value = false
    return
  }
  if (action === 'filter-alerts') {
    quickFilter.value = 'ALERT'
    riskFilterEnabled.value = true
    riskFilterLevel.value = 'ALL'
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

function handleStatusLegendClick(key: string) {
  if (key === 'MAINTENANCE') {
    quickFilter.value = 'ALL'
    riskFilterEnabled.value = false
    message.info('维修床位已在左侧运营口径中展示，可进入床位管理处理')
    return
  }
  if (key === 'ALERT') {
    handleCommandAction('filter-alerts')
    return
  }
  if (key === 'IDLE') {
    handleCommandAction('filter-idle')
    return
  }
  if (key === 'OCCUPIED') {
    handleCommandAction('filter-occupied')
    return
  }
  resetFilters()
}

function applyScope(scope: PanoramaScope) {
  selectedBuilding.value = scope.building || ''
  selectedFloor.value = scope.floor || ''
  selectedRoomNo.value = scope.room || ''

  if (scope.level === 'PARK') {
    selectedRoom.value = null
    selectedBed.value = null
    detailOpen.value = false
    return
  }

  if (scope.level === 'BUILDING') {
    selectedRoom.value = null
    if (selectedBed.value && String(selectedBed.value.building || '') !== scope.building) {
      selectedBed.value = null
      detailOpen.value = false
    }
    return
  }

  if (scope.level === 'FLOOR') {
    selectedRoom.value = null
    if (
      selectedBed.value
      && (String(selectedBed.value.building || '') !== scope.building || String(selectedBed.value.floorNo || '') !== scope.floor)
    ) {
      selectedBed.value = null
      detailOpen.value = false
    }
    return
  }

  if (selectedRoom.value && selectedRoom.value.roomNo !== scope.room) {
    selectedRoom.value = null
  }
  if (
    selectedBed.value
    && (
      String(selectedBed.value.building || '') !== scope.building
      || String(selectedBed.value.floorNo || '') !== scope.floor
      || String(selectedBed.value.roomNo || '') !== scope.room
    )
  ) {
    selectedBed.value = null
    detailOpen.value = false
  }
}

function handleScopeChange(scope: PanoramaScope) {
  applyScope(scope)
}

function setBuildingScope(building: string) {
  applyScope({
    level: 'BUILDING',
    building,
    floor: '',
    room: ''
  })
}

function setFloorScope(floor: string) {
  if (!selectedBuilding.value) return
  applyScope({
    level: 'FLOOR',
    building: selectedBuilding.value,
    floor,
    room: ''
  })
}

function clearMatrixSelection() {
  applyScope({ level: 'PARK', building: '', floor: '', room: '' })
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
  quickFilter.value = nextQuick === 'IDLE' || nextQuick === 'OCCUPIED' || nextQuick === 'ALERT' ? nextQuick : 'ALL'

  riskFilterEnabled.value = firstRouteQueryText(route.query.bedRiskEnabled) === '1'
  const nextRisk = firstRouteQueryText(route.query.bedRiskLevel).toUpperCase()
  riskFilterLevel.value = nextRisk === 'HIGH' || nextRisk === 'MEDIUM' || nextRisk === 'LOW' ? nextRisk : 'ALL'

  selectedBuilding.value = firstRouteQueryText(route.query.bedBuilding)
  selectedFloor.value = firstRouteQueryText(route.query.bedFloor)
  selectedRoomNo.value = ''
  defaultScopeApplied.value = Boolean(selectedBuilding.value || selectedFloor.value)
}

function applyDefaultSceneScope() {
  if (defaultScopeApplied.value || selectedBuilding.value || selectedFloor.value) return
  const primaryBuilding = campusTree.value[0]
  if (!primaryBuilding) return
  selectedBuilding.value = primaryBuilding.name
  selectedFloor.value = primaryBuilding.floors[0]?.name || ''
  defaultScopeApplied.value = true
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
  const lifecycleStatus = String(bed.elderLifecycleStatus || '').trim().toUpperCase()
  if (bed.elderId) {
    if (lifecycleStatus === 'OUTING' || lifecycleStatus === 'MEDICAL_OUTING' || lifecycleStatus === 'DISCHARGE_PENDING') {
      return '预定'
    }
    return '在住'
  }
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
  selectedRoomNo.value = String(bed.roomNo || '').trim()
  selectedBuilding.value = String(bed.building || '').trim()
  selectedFloor.value = String(bed.floorNo || '').trim()
  // 点击床位改为打开右侧悬浮详情面板，完整操作入口在面板里
  rightPanelOpen.value = true
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
  selectedRoomNo.value = room.roomNo
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
  applyDefaultSceneScope()
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
  if (selectedRoomNo.value && !sourceBeds.value.some((item) => String(item.roomNo || '') === selectedRoomNo.value)) {
    selectedRoomNo.value = ''
  }
  if (!selectedBuilding.value && !selectedFloor.value) {
    applyDefaultSceneScope()
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
  --hud-border: rgba(187, 217, 207, 0.5);
  --hud-shadow: 0 18px 44px rgba(34, 51, 46, 0.1);
  --glass-bg: rgba(255, 255, 255, 0.84);
  --glass-strong: rgba(255, 255, 255, 0.94);
  --text-strong: #22332e;
  --text-main: #3c4f48;
  --text-soft: #8a9a94;
  --surface-blue: #f4f7f2;
  --surface-panel: rgba(249, 251, 247, 0.78);
  --tone-blue: #3d7fa6;
  --tone-green: #2e8a72;
  --tone-purple: #6b79d8;
  --tone-orange: #de9b3d;
  --tone-red: #c9504b;
  --tone-gray: #a3b0aa;
  display: block;
  margin: -24px -24px -26px;
}

.bed-immersive-page :deep(.page-head) {
  display: none;
}

.bed-immersive-page :deep(.page-body) {
  gap: 0;
}

.immersive-stage-shell {
  position: relative;
  min-height: calc(100vh - 128px);
  border-radius: 24px;
  overflow: hidden;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background:
    radial-gradient(circle at 12% 10%, rgba(46, 138, 114, 0.1), transparent 24%),
    radial-gradient(circle at 92% 6%, rgba(229, 138, 58, 0.08), transparent 20%),
    linear-gradient(180deg, #f9faf5 0%, #f0f3ea 48%, #e9eee3 100%);
  box-shadow: 0 28px 64px rgba(34, 51, 46, 0.14);
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
    radial-gradient(circle at 50% 48%, rgba(255, 255, 255, 0.01), rgba(228, 236, 224, 0.08) 72%, rgba(213, 224, 208, 0.2) 100%);
}

.scene-glow {
  width: 360px;
  height: 360px;
  border-radius: 50%;
  filter: blur(92px);
  opacity: 0.24;
}

.scene-glow-left {
  left: -80px;
  bottom: -120px;
  background: rgba(46, 138, 114, 0.3);
}

.scene-glow-right {
  right: -100px;
  top: 100px;
  background: rgba(229, 138, 58, 0.22);
}

.hud-panel,
.metric-pill,
.action-card,
.stream-card,
.focus-ribbon {
  border: 1px solid var(--hud-border);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(248, 251, 246, 0.88));
  backdrop-filter: blur(12px);
  box-shadow: var(--hud-shadow);
}

.hud-topbar {
  position: relative;
  z-index: 2;
  display: grid;
  gap: 10px;
}

/* 沉浸式：3D 画布铺满主区域，左右面板与事件坞悬浮其上 */
.dashboard-main {
  position: relative;
  z-index: 2;
  flex: 1;
  min-height: 620px;
}

.dashboard-stage {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
}

.stage-canvas-shell {
  min-width: 0;
  min-height: 0;
  overflow: hidden;
  border-radius: 28px;
  border: 1px solid rgba(187, 217, 207, 0.22);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.9), rgba(244, 249, 255, 0.82));
  box-shadow: 0 20px 44px rgba(113, 137, 163, 0.12);
}

.stage-canvas-shell {
  flex: 1;
  min-height: 0;
}

.stage-canvas-shell :deep(.panorama-container),
.stage-empty {
  height: 100%;
  min-height: 0;
}

.stage-empty {
  display: grid;
  place-items: center;
}

.command-marquee,
.hud-topbar__brand,
.campus-scope,
.status-stack,
.clock-stack,
.operator-stack {
  display: flex;
  align-items: center;
  gap: 12px;
}

.command-marquee {
  justify-content: space-between;
  padding: 9px 14px;
  border-radius: 22px;
  border: 1px solid rgba(187, 217, 207, 0.26);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(248, 251, 246, 0.86));
  box-shadow: var(--hud-shadow);
}

.campus-scope {
  min-width: 0;
  flex-direction: column;
  align-items: flex-end;
  text-align: right;
}

.brand-mark {
  position: relative;
  width: 44px;
  height: 44px;
  border-radius: 15px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #21705f, #2e8a72);
  box-shadow: 0 12px 24px rgba(61, 127, 166, 0.2);
}

.brand-mark span {
  position: absolute;
  inset: 9px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.58);
}

.brand-copy {
  display: grid;
  gap: 2px;
}

.brand-kicker,
.panel-kicker,
.field-label,
.metric-pill span,
.status-bar__top span,
.activity-dock__head span {
  font-size: 11px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--text-soft);
}

.brand-copy strong,
.hud-panel__head strong,
.status-stack strong,
.clock-stack strong,
.operator-stack strong,
.focus-ribbon__item strong,
.resident-profile__title strong,
.detail-section__head strong,
.stream-card strong {
  color: var(--text-strong);
}

.brand-copy strong {
  font-size: 34px;
  line-height: 1;
  letter-spacing: -0.02em;
}

.brand-copy small,
.hud-panel__head small,
.metric-pill small,
.status-stack small,
.clock-stack span,
.operator-stack small,
.tree-building small,
.event-card p,
.empty-drawer p {
  color: var(--text-soft);
}

.hud-topbar__metrics {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 10px;
}

.metric-pill {
  min-height: 62px;
  padding: 9px 14px;
  border-radius: 18px;
  text-align: left;
  position: relative;
  display: grid;
  gap: 4px;
}

.metric-pill strong {
  font-size: 26px;
  line-height: 1;
}

.metric-pill.tone-blue strong { color: var(--tone-blue); }
.metric-pill.tone-green strong { color: var(--tone-green); }
.metric-pill.tone-red strong { color: var(--tone-red); }
.metric-pill.tone-orange strong { color: var(--tone-orange); }
.metric-pill.tone-purple strong { color: var(--tone-purple); }

.hud-topbar__ops {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
}

.topbar-search {
  width: 340px;
}

.status-stack,
.clock-stack,
.operator-stack {
  min-height: 48px;
  padding: 10px 12px;
  border-radius: 18px;
  border: 1px solid rgba(187, 217, 207, 0.24);
  background: var(--glass-bg);
  box-shadow: var(--hud-shadow);
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: #2e8a72;
  box-shadow: 0 0 0 6px rgba(46, 138, 114, 0.14);
}

.clock-stack strong {
  font-size: 18px;
  letter-spacing: 0.08em;
}

.operator-avatar,
.resident-avatar {
  background: linear-gradient(135deg, #7da0ff, #8cc7ff);
  color: white;
  font-weight: 700;
}

.hud-left,
.hud-right,
.hud-bottom {
  position: absolute;
  z-index: 4;
}

.hud-left {
  left: 12px;
  top: 12px;
  bottom: 12px;
  width: 302px;
  transition: transform 0.28s ease, opacity 0.28s ease;
}

.hud-left.is-closed {
  transform: translateX(-118%);
  opacity: 0;
  pointer-events: none;
}

.hud-right {
  right: 12px;
  top: 12px;
  bottom: 12px;
  width: 360px;
  transition: transform 0.28s ease, opacity 0.28s ease;
}

.hud-right.is-closed {
  transform: translateX(118%);
  opacity: 0;
  pointer-events: none;
}

.hud-bottom {
  left: 50%;
  bottom: 14px;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  width: min(1040px, calc(100% - 48px));
  pointer-events: none;
}

.hud-bottom > * {
  pointer-events: auto;
}

.dock-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 999px;
  border: 1px solid var(--hud-border);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
  box-shadow: var(--hud-shadow);
  color: var(--text-main);
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.dock-pill small {
  color: var(--text-soft);
  font-size: 11px;
}

.dock-pill__dot {
  width: 9px;
  height: 9px;
  border-radius: 999px;
  background: var(--tone-gray);
}

.dock-pill__dot.is-alert {
  background: var(--tone-red);
  box-shadow: 0 0 0 5px rgba(201, 80, 75, 0.14);
}

.overlay-toggle {
  position: absolute;
  z-index: 5;
  top: 20px;
  padding: 8px 13px;
  border-radius: 999px;
  border: 1px solid var(--hud-border);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  box-shadow: var(--hud-shadow);
  color: var(--text-main);
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: left 0.28s ease, right 0.28s ease;
}

.overlay-toggle--left {
  left: 12px;
}

.overlay-toggle--left.is-shifted {
  left: 326px;
}

.overlay-toggle--right {
  right: 12px;
}

.context-head-ops {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.context-head-ops small {
  color: var(--text-soft);
}

.hud-panel {
  position: relative;
  border-radius: 26px;
  padding: 18px;
  overflow: hidden;
}

.hud-panel--sidebar,
.hud-panel--context {
  height: 100%;
  min-height: 0;
  max-height: none;
  overflow-y: auto;
}

.hud-panel__head,
.sidebar-section__head,
.detail-section__head,
.activity-dock__head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
}

.hud-panel__head {
  margin-bottom: 14px;
}

.field-block,
.sidebar-section,
.detail-section {
  display: grid;
  gap: 10px;
}

.sidebar-section,
.detail-section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(187, 217, 207, 0.22);
}

.field-inline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.campus-tree {
  display: grid;
  gap: 10px;
  margin-top: 8px;
}

.tree-building {
  display: grid;
  gap: 8px;
}

.tree-building__head,
.tree-floor,
.stream-card,
.action-card {
  width: 100%;
  border: 0;
  cursor: pointer;
}

.tree-building__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 18px;
  background: rgba(246, 250, 255, 0.94);
  color: var(--text-main);
  text-align: left;
  box-shadow: inset 0 0 0 1px rgba(187, 217, 207, 0.24);
}

.tree-building__head strong,
.tree-floor span {
  color: var(--text-strong);
}

.tree-building__head.active {
  background: linear-gradient(135deg, rgba(61, 127, 166, 0.16), rgba(141, 194, 255, 0.18));
}

.tree-floor-list {
  display: grid;
  gap: 6px;
  padding-left: 12px;
}

.tree-floor {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 9px 12px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.76);
  color: var(--text-main);
  text-align: left;
  box-shadow: inset 0 0 0 1px rgba(187, 217, 207, 0.18);
}

.tree-floor.active {
  background: linear-gradient(135deg, rgba(46, 138, 114, 0.18), rgba(61, 127, 166, 0.14));
}

.status-bars,
.event-list {
  display: grid;
  gap: 10px;
}

.status-bar__top,
.event-top,
.stream-card__meta {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.distribution-track {
  height: 8px;
  border-radius: 999px;
  background: rgba(187, 217, 207, 0.2);
  overflow: hidden;
}

.distribution-fill {
  display: block;
  height: 100%;
}

.fill-cyan { background: linear-gradient(90deg, #64b8ff, #8dd2ff); }
.fill-gray { background: linear-gradient(90deg, #b5c1ce, #d0d8e0); }
.fill-purple { background: linear-gradient(90deg, #8770eb, #a997ff); }
.fill-orange { background: linear-gradient(90deg, #de9b3d, #edc28a); }
.fill-red { background: linear-gradient(90deg, #c9504b, #e0928d); }

.focus-ribbon {
  padding: 10px 12px;
  border-radius: 22px;
}

.bed-command-bar {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: minmax(240px, 1fr) minmax(360px, 1.5fr) 180px;
  gap: 10px;
  align-items: stretch;
}

.bed-command-bar__primary,
.bed-command-bar__filters,
.bed-command-bar__cta {
  border: 1px solid var(--hud-border);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(248, 251, 246, 0.88));
  box-shadow: var(--hud-shadow);
  backdrop-filter: blur(12px);
}

.bed-command-bar__primary {
  min-width: 0;
  padding: 13px 16px;
  border-radius: 20px;
  display: grid;
  gap: 4px;
}

.bed-command-bar__primary strong {
  color: var(--text-strong);
  font-size: 17px;
}

.bed-command-bar__primary p,
.bed-command-bar__cta span {
  margin: 0;
  color: var(--text-soft);
  font-size: 12px;
}

.bed-command-bar__filters {
  min-width: 0;
  padding: 8px;
  border-radius: 20px;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 8px;
}

.bed-status-chip {
  min-width: 0;
  border: 0;
  border-radius: 16px;
  padding: 10px 10px;
  background: rgba(255, 255, 255, 0.72);
  color: var(--text-main);
  cursor: pointer;
  display: grid;
  grid-template-columns: 10px minmax(0, 1fr);
  grid-template-areas:
    "dot label"
    "dot value";
  gap: 2px 8px;
  text-align: left;
  box-shadow: inset 0 0 0 1px rgba(187, 217, 207, 0.18);
}

.bed-status-chip.active {
  background: rgba(226, 244, 235, 0.95);
  box-shadow: inset 0 0 0 1px rgba(46, 138, 114, 0.32), 0 8px 20px rgba(46, 138, 114, 0.12);
}

.bed-status-chip__dot {
  grid-area: dot;
  align-self: center;
  width: 9px;
  height: 9px;
  border-radius: 999px;
  background: #7fa2bf;
}

.bed-status-chip strong {
  grid-area: label;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-strong);
}

.bed-status-chip small {
  grid-area: value;
  color: var(--text-soft);
}

.bed-status-chip.tone-green .bed-status-chip__dot { background: var(--tone-green); }
.bed-status-chip.tone-red .bed-status-chip__dot { background: var(--tone-red); }
.bed-status-chip.tone-orange .bed-status-chip__dot { background: var(--tone-orange); }
.bed-status-chip.tone-gray .bed-status-chip__dot { background: var(--tone-gray); }
.bed-status-chip.tone-blue .bed-status-chip__dot { background: var(--tone-blue); }

.bed-command-bar__cta {
  border-radius: 20px;
  padding: 12px 14px;
  color: var(--text-main);
  text-align: left;
  cursor: pointer;
  display: grid;
  align-content: center;
  gap: 4px;
}

.bed-command-bar__cta strong {
  color: var(--text-strong);
  font-size: 16px;
}

.bed-command-bar__cta.tone-red {
  background: linear-gradient(180deg, rgba(255, 242, 242, 0.96), rgba(255, 248, 248, 0.86));
}

.bed-command-bar__cta.tone-cyan {
  background: linear-gradient(180deg, rgba(235, 250, 247, 0.96), rgba(248, 253, 251, 0.86));
}

.bed-command-bar__cta.tone-green {
  background: linear-gradient(180deg, rgba(238, 250, 242, 0.96), rgba(249, 253, 250, 0.86));
}

.focus-ribbon__main {
  display: grid;
  grid-template-columns: 160px minmax(0, 1fr) 190px;
  gap: 10px;
}

.focus-ribbon__item {
  padding: 10px 12px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.8);
  box-shadow: inset 0 0 0 1px rgba(187, 217, 207, 0.18);
  display: grid;
  gap: 4px;
}

.focus-ribbon__item span,
.detail-grid span,
.resident-profile__meta,
.event-type,
.event-time {
  font-size: 12px;
  color: var(--text-soft);
}

.focus-ribbon__item--wide {
  min-width: 0;
}

.focus-ribbon__item strong {
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.resident-profile {
  display: grid;
  grid-template-columns: 80px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
}

.resident-profile__body,
.resident-profile__title {
  display: grid;
  gap: 8px;
}

.resident-profile__title {
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
}

.resident-profile__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
}

.detail-chip,
.overlay-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
}

.detail-chip {
  background: rgba(61, 127, 166, 0.12);
  color: var(--text-strong);
}

.detail-chip.tone-green { background: rgba(46, 138, 114, 0.14); color: #21705f; }
.detail-chip.tone-red { background: rgba(239, 107, 123, 0.14); color: #c9504b; }
.detail-chip.tone-orange { background: rgba(242, 171, 69, 0.16); color: #b76b0e; }
.detail-chip.tone-purple { background: rgba(107, 121, 216, 0.14); color: #6b79d8; }
.detail-chip.tone-gray { background: rgba(165, 178, 194, 0.14); color: #8a9a94; }

.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.overlay-chip {
  border: 1px solid rgba(187, 217, 207, 0.22);
  background: rgba(248, 251, 246, 0.88);
  color: var(--text-main);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.detail-grid--vitals {
  margin-top: 14px;
}

.detail-grid > div {
  padding: 12px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.74);
  box-shadow: inset 0 0 0 1px rgba(187, 217, 207, 0.2);
  display: grid;
  gap: 4px;
}

.detail-grid strong {
  color: var(--text-strong);
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.action-card {
  padding: 12px 14px;
  border-radius: 18px;
  text-align: left;
  display: grid;
  gap: 6px;
}

.action-card span {
  font-size: 12px;
  color: var(--text-soft);
}

.event-card {
  padding: 12px 14px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
  box-shadow: inset 0 0 0 1px rgba(187, 217, 207, 0.2);
}

.event-card--soft.tone-red { background: linear-gradient(180deg, rgba(255, 241, 243, 0.92), rgba(255, 249, 250, 0.82)); }
.event-card--soft.tone-orange { background: linear-gradient(180deg, rgba(255, 248, 236, 0.92), rgba(255, 251, 245, 0.82)); }
.event-card--soft.tone-blue { background: linear-gradient(180deg, rgba(243, 247, 242, 0.92), rgba(248, 251, 246, 0.82)); }

.empty-drawer {
  min-height: 220px;
  display: grid;
  place-items: center;
  text-align: center;
  color: var(--text-main);
}

.hud-bottom {
  min-width: 0;
}

.activity-dock {
  width: 100%;
  max-height: 250px;
  overflow-y: auto;
  padding: 14px;
  border-radius: 24px;
  border: 1px solid var(--hud-border);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(248, 251, 246, 0.86));
  box-shadow: var(--hud-shadow);
  backdrop-filter: blur(10px);
}

.activity-dock__head {
  margin-bottom: 12px;
}

.activity-stream {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.hud-topbar--command {
  gap: 12px;
}

.command-marquee--hero {
  align-items: center;
}

.campus-scope--focus {
  padding: 10px 14px;
  border-radius: 18px;
  border: 1px solid rgba(187, 217, 207, 0.22);
  background: rgba(248, 251, 246, 0.72);
}

.hud-topbar__ops--command {
  align-items: stretch;
}

.hud-topbar__metrics--ribbon {
  grid-template-columns: repeat(6, minmax(0, 1fr));
}

.sidebar-mini-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.sidebar-mini-card {
  padding: 12px;
  border-radius: 18px;
  border: 1px solid rgba(187, 217, 207, 0.18);
  background: rgba(255, 255, 255, 0.7);
  display: grid;
  gap: 4px;
}

.sidebar-mini-card span,
.metric-note-card p,
.suggestion-card p,
.stage-quick-action span,
.bed-guard-blocker p,
.bed-guard-blockers--quiet p,
.context-tone-strip span {
  font-size: 12px;
  color: var(--text-soft);
}

.sidebar-mini-card strong,
.metric-note-card strong,
.suggestion-card strong,
.stage-quick-action strong,
.bed-guard-card__head strong,
.bed-guard-step strong,
.bed-guard-blocker strong,
.context-tone-strip strong {
  color: var(--text-strong);
}

.metric-note-card {
  padding: 12px 14px;
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(244, 249, 255, 0.9), rgba(255, 255, 255, 0.84));
  border: 1px solid rgba(187, 217, 207, 0.2);
  display: grid;
  gap: 6px;
}

.metric-note-card.is-warning {
  background: linear-gradient(180deg, rgba(255, 246, 233, 0.92), rgba(255, 252, 246, 0.88));
  border-color: rgba(242, 171, 69, 0.28);
}

.suggestion-list {
  display: grid;
  gap: 10px;
}

.suggestion-card {
  padding: 12px 14px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.76);
  box-shadow: inset 0 0 0 1px rgba(187, 217, 207, 0.16);
  display: grid;
  gap: 6px;
}

.suggestion-card.dot-red {
  background: linear-gradient(180deg, rgba(255, 242, 244, 0.92), rgba(255, 250, 250, 0.84));
}

.suggestion-card.dot-purple {
  background: linear-gradient(180deg, rgba(244, 240, 255, 0.92), rgba(251, 249, 255, 0.84));
}

.suggestion-card.dot-cyan {
  background: linear-gradient(180deg, rgba(242, 249, 255, 0.92), rgba(249, 252, 255, 0.84));
}

.stage-brief {
  position: relative;
  z-index: 2;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 330px;
  gap: 10px;
}

.stage-brief__main,
.stage-brief__side {
  min-width: 0;
  display: grid;
  gap: 12px;
}

.stage-quick-actions {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 10px;
}

.stage-quick-action,
.bed-guard-card,
.context-tone-strip > div {
  border: 1px solid var(--hud-border);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(248, 251, 246, 0.88));
  box-shadow: var(--hud-shadow);
  backdrop-filter: blur(12px);
}

.stage-quick-action {
  width: 100%;
  padding: 12px 14px;
  border-radius: 18px;
  text-align: left;
  display: grid;
  gap: 6px;
  cursor: pointer;
}

.bed-guard-card {
  padding: 14px;
  border-radius: 22px;
  display: grid;
  gap: 12px;
}

.bed-guard-card__head {
  display: grid;
  gap: 4px;
}

.bed-guard-steps {
  display: grid;
  gap: 8px;
}

.bed-guard-step {
  display: grid;
  grid-template-columns: 28px minmax(0, 1fr);
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 16px;
  background: rgba(248, 251, 246, 0.82);
  box-shadow: inset 0 0 0 1px rgba(187, 217, 207, 0.16);
}

.bed-guard-step span {
  display: inline-flex;
  width: 28px;
  height: 28px;
  border-radius: 999px;
  align-items: center;
  justify-content: center;
  background: rgba(165, 178, 194, 0.16);
  color: var(--text-main);
  font-size: 12px;
  font-weight: 700;
}

.bed-guard-step.is-current span {
  background: rgba(61, 127, 166, 0.16);
  color: #3d7fa6;
}

.bed-guard-step.is-done span {
  background: rgba(46, 138, 114, 0.16);
  color: #2e8a72;
}

.bed-guard-blockers {
  display: grid;
  gap: 8px;
}

.bed-guard-blocker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 16px;
  background: rgba(255, 247, 236, 0.72);
  box-shadow: inset 0 0 0 1px rgba(242, 171, 69, 0.16);
}

.bed-guard-blockers--quiet {
  padding: 10px 12px;
  border-radius: 16px;
  background: rgba(243, 248, 252, 0.7);
}

.context-tone-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}

.context-tone-strip > div {
  padding: 10px 12px;
  border-radius: 16px;
  display: grid;
  gap: 4px;
}

.stream-card {
  padding: 12px 14px;
  border-radius: 18px;
  text-align: left;
  display: grid;
  gap: 6px;
}

.stream-card p {
  margin: 0;
  font-size: 12px;
  line-height: 1.6;
  color: var(--text-main);
}

.stream-card.tone-red { background: linear-gradient(180deg, rgba(255, 242, 244, 0.92), rgba(255, 249, 250, 0.8)); }
.stream-card.tone-orange { background: linear-gradient(180deg, rgba(255, 247, 236, 0.92), rgba(255, 251, 245, 0.8)); }
.stream-card.tone-blue { background: linear-gradient(180deg, rgba(243, 247, 242, 0.92), rgba(248, 251, 246, 0.8)); }
.stream-card.tone-green { background: linear-gradient(180deg, rgba(240, 251, 246, 0.92), rgba(247, 252, 249, 0.8)); }

.bed-immersive-page :deep(.ant-input),
.bed-immersive-page :deep(.ant-input-affix-wrapper),
.bed-immersive-page :deep(.ant-input-search-button),
.bed-immersive-page :deep(.ant-segmented),
.bed-immersive-page :deep(.ant-switch),
.bed-immersive-page :deep(.ant-btn) {
  border-radius: 14px;
  border-color: rgba(187, 217, 207, 0.28);
  background: rgba(255, 255, 255, 0.88);
  color: var(--text-main);
}

.bed-immersive-page :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #3d7fa6, #74a8c8) !important;
  border-color: transparent !important;
  color: white !important;
}

.bed-immersive-page :deep(.ant-empty-description) {
  color: var(--text-soft);
}

@media (max-width: 1440px) {
  .hud-topbar__metrics--ribbon {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .bed-command-bar {
    grid-template-columns: minmax(240px, 1fr) minmax(420px, 1.4fr);
  }

  .bed-command-bar__cta {
    grid-column: 1 / -1;
  }

  .stage-brief {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1280px) {
  .bed-immersive-page {
    margin: -20px -20px -24px;
  }

  .immersive-stage-shell {
    padding: 18px;
    gap: 14px;
  }

  .hud-topbar {
    gap: 10px;
  }

  .hud-topbar__metrics--ribbon {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 8px;
  }

  .metric-pill {
    min-height: 78px;
    padding: 12px;
  }

  .metric-pill strong {
    font-size: 24px;
  }

  .stage-quick-actions {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .bed-command-bar {
    grid-template-columns: 1fr;
  }

  .bed-command-bar__filters {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .activity-stream {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .hud-left {
    width: 272px;
  }

  .overlay-toggle--left.is-shifted {
    left: 296px;
  }

  .hud-right {
    width: 330px;
  }
}

@media (max-width: 1120px) {
  .hud-right {
    width: min(330px, calc(100% - 24px));
  }
}

@media (max-width: 960px) {
  .bed-immersive-page {
    margin: 0;
  }

  .immersive-stage-shell {
    padding: 12px;
    gap: 12px;
  }

  .scene-vignette,
  .scene-glow {
    display: none;
  }

  .command-marquee,
  .hud-topbar__ops {
    flex-direction: column;
    align-items: stretch;
  }

  .dashboard-main {
    min-height: 520px;
  }

  .hud-left,
  .hud-right {
    width: min(320px, calc(100% - 24px));
  }

  .hud-topbar__metrics,
  .bed-command-bar__filters,
  .sidebar-mini-grid,
  .detail-grid,
  .action-grid,
  .focus-ribbon__main,
  .context-tone-strip,
  .activity-stream {
    grid-template-columns: 1fr;
  }

  .stage-canvas-shell :deep(.panorama-container),
  .stage-empty {
    min-height: 0;
  }

  .resident-profile {
    grid-template-columns: 1fr;
  }
}
</style>
