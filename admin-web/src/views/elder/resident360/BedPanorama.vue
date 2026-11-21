<template>
  <PageContainer class="bed-panorama rm-page" title="床位与状态管理" kicker="RESIDENT MANAGEMENT / 05" sub-title="床位可视化、分床调床、外出、住院、返院统一管理">
    <template #extra>
      <span class="bed-panorama__updated">数据更新时间：{{ refreshedAt || '--' }}</span>
      <a-segmented v-model:value="view" :options="VIEW_OPTIONS" />
      <a-button type="primary" :loading="loading" @click="loadData">
        <template #icon><ReloadOutlined /></template>
        刷新
      </a-button>
    </template>

    <section class="bed-kpis">
      <button v-for="item in kpis" :key="item.key" type="button" class="bed-kpi" :class="`bed-kpi--${item.tone}`" @click="item.status && setStatusFilter(item.status)">
        <span class="bed-kpi__icon"><component :is="item.icon" /></span>
        <span class="bed-kpi__body">
          <span class="bed-kpi__label">{{ item.label }}</span>
          <strong class="bed-kpi__value">{{ item.value }}<small>{{ item.unit }}</small></strong>
        </span>
      </button>
    </section>

    <section v-if="view === 'grid'" class="bed-layout">
      <aside class="bed-card bed-filters">
        <h3 class="bed-card__title"><FilterOutlined /> 筛选条件</h3>
        <label class="bed-field">
          <span>楼栋</span>
          <a-select v-model:value="building" :options="buildingOptions" placeholder="选择楼栋" />
        </label>
        <label class="bed-field">
          <span>楼层</span>
          <a-select v-model:value="floor" :options="floorOptions" placeholder="选择楼层" />
        </label>
        <label class="bed-field">
          <span>房型</span>
          <a-select v-model:value="roomTypeFilter" :options="roomTypeOptions" />
        </label>
        <label class="bed-field">
          <span>床位状态</span>
          <a-select v-model:value="statusFilter" :options="statusOptions" />
        </label>
        <label class="bed-field">
          <span>护理风险</span>
          <a-select v-model:value="riskFilter" :options="riskOptions" />
        </label>
        <label class="bed-field">
          <span>关键字搜索</span>
          <a-input v-model:value="keyword" allow-clear placeholder="长者姓名 / 房间号 / 床号" />
        </label>
        <a-button block @click="resetFilters"><template #icon><UndoOutlined /></template>重置</a-button>
      </aside>

      <main class="bed-card bed-floor">
        <header class="bed-floor__head">
          <div class="bed-floor__title">
            <h2>{{ building || '--' }} · {{ floor || '--' }}</h2>
            <span>（共{{ floorRooms.length }}个房间 · {{ floorBeds.length }}张床位 · 已入住{{ floorCounts.occupied }} · 空床{{ floorCounts.idle }} · 预留{{ floorCounts.reserved }} · 清洁中{{ floorCounts.cleaning }} · 维修中{{ floorCounts.maintenance }}）</span>
          </div>
          <div class="bed-floor__switch">
            <a-button :disabled="floorIndex <= 0" aria-label="上一层" @click="stepFloor(-1)"><template #icon><LeftOutlined /></template></a-button>
            <span>切换楼层：</span>
            <a-select v-model:value="floor" :options="floorOptions" class="bed-floor__select" />
            <a-button :disabled="floorIndex >= floorOptions.length - 1" aria-label="下一层" @click="stepFloor(1)"><template #icon><RightOutlined /></template></a-button>
          </div>
        </header>

        <a-spin :spinning="loading">
          <div v-if="visibleRooms.length" class="bed-rooms">
            <article v-for="room in visibleRooms" :key="room.key" class="bed-room" :class="{ 'is-selected': room.key === selectedRoomKey }">
              <button type="button" class="bed-room__head" :aria-pressed="room.key === selectedRoomKey" @click="selectedRoomKey = room.key === selectedRoomKey ? '' : room.key">
                <strong>{{ room.roomNo }}房</strong>
                <span>{{ room.roomType }}</span>
                <a-tag :color="room.occupied >= room.beds.length ? 'green' : 'default'" class="bed-room__count">{{ room.occupied }}/{{ room.beds.length }}</a-tag>
              </button>
              <button
                v-for="item in room.visibleBeds"
                :key="item.id"
                type="button"
                class="bed-slot"
                :class="[`bed-slot--${bedStatus(item)}`, { 'is-focused': focusElderId && String(item.elderId) === focusElderId }]"
                @click="openBed(item)"
              >
                <span class="bed-slot__top">
                  <strong>{{ bedLabel(item) }}</strong>
                  <a-tag :color="BED_STATUS_META[bedStatus(item)].tagColor">{{ BED_STATUS_META[bedStatus(item)].label }}</a-tag>
                  <a-tag v-if="residentAwayLabel(item)" color="gold">{{ residentAwayLabel(item) }}</a-tag>
                </span>
                <template v-if="item.elderId">
                  <span class="bed-slot__resident">
                    <span class="bed-slot__avatar" :class="{ 'bed-slot__avatar--female': item.elderGender === 2 }">{{ String(item.elderName || '?').slice(-1) }}</span>
                    <span>
                      <strong>{{ item.elderName || '未命名长者' }}</strong>
                      <small>{{ residentMeta(item) }}</small>
                    </span>
                  </span>
                  <span v-if="bedCaregiver(item, caregivers)" class="bed-slot__staff">{{ bedCaregiver(item, caregivers)?.label }}：{{ bedCaregiver(item, caregivers)?.name }}</span>
                  <span v-if="bedRiskTags(item).length" class="bed-slot__risks">
                    <span v-for="tag in bedRiskTags(item)" :key="tag" class="bed-slot__risk"><WarningOutlined /> {{ tag }}</span>
                  </span>
                </template>
                <span v-else class="bed-slot__empty">{{ emptyBedHint(item) }}</span>
              </button>
            </article>
          </div>
          <a-empty v-else class="bed-floor__empty" :description="beds.length ? '当前楼层没有符合条件的床位' : '暂无床位数据'" />
        </a-spin>
      </main>

      <aside class="bed-side">
        <section v-if="selectedRoom" class="bed-card bed-room-panel">
          <h3 class="bed-card__title">
            <HomeOutlined /> 选中房间与快捷操作
            <a-button type="text" size="small" class="bed-room-panel__close" aria-label="取消选中房间" @click="selectedRoomKey = ''"><template #icon><CloseOutlined /></template></a-button>
          </h3>
          <div class="bed-room-panel__head">
            <strong>{{ selectedRoom.roomNo }} 室</strong>
            <span v-if="selectedRoomIdle.length" class="rm-pill rm-pill--success">可分配 {{ selectedRoomIdle.length }} 床</span>
          </div>
          <p class="bed-room-panel__meta">{{ [building, floor, selectedRoom.roomType, orientationLabel(roomOrientations[String(selectedRoom.beds[0]?.roomId)])].filter(Boolean).join(' · ') }}</p>
          <div v-for="item in selectedRoom.beds" :key="item.id" class="bed-room-panel__bed">
            <button type="button" class="rm-link" @click="openBed(item)">{{ bedLabel(item) }}</button>
            <span>
              <strong v-if="item.elderName">{{ item.elderName }} · </strong>
              <span class="rm-pill" :class="`rm-pill--${STATUS_PILL[bedStatus(item)]}`">{{ residentAwayLabel(item) || BED_STATUS_META[bedStatus(item)].label }}</span>
            </span>
          </div>
          <p v-if="selectedRoom.beds[0]?.roomRemark" class="bed-room-panel__meta">房间备注：{{ selectedRoom.beds[0]?.roomRemark }}</p>
          <div class="bed-drawer__actions">
            <a-button type="primary" :disabled="!selectedRoomIdle.length" @click="openAction('assign', selectedRoom.beds)"><template #icon><PlusOutlined /></template>安排床位</a-button>
            <a-button :disabled="!selectedRoom.occupied" @click="openAction('transfer', selectedRoom.beds)"><template #icon><SwapOutlined /></template>调床</a-button>
          </div>
          <h4 class="rm-section-title bed-room-panel__section">在院状态快捷办理</h4>
          <button v-for="kind in RESIDENCE_ACTIONS" :key="kind" type="button" class="bed-side__row" :disabled="!selectedRoom.occupied" @click="openAction(kind, selectedRoom.beds)">
            <span>{{ ACTION_TITLES[kind] }}</span>
            <RightOutlined />
          </button>
        </section>

        <section class="bed-card">
          <h3 class="bed-card__title"><InfoCircleOutlined /> 床位状态图例</h3>
          <button v-for="key in STATUS_KEYS" :key="key" type="button" class="bed-legend" :class="{ 'is-active': statusFilter === key }" @click="setStatusFilter(key)">
            <span class="bed-legend__dot" :class="`bed-legend__dot--${key}`"></span>
            <span>{{ BED_STATUS_META[key].label }}</span>
            <span class="bed-legend__count">（{{ counts[key] }}张）</span>
          </button>
        </section>

        <section class="bed-card">
          <h3 class="bed-card__title"><BellOutlined /> 护理提醒（今日） <em>{{ reminderTotal }} 条</em></h3>
          <template v-if="careSummary">
            <button v-for="item in reminderItems" :key="item.key" type="button" class="bed-side__row" @click="router.push(item.path)">
              <span>{{ item.label }}</span>
              <span>{{ item.value }} 项 <RightOutlined /></span>
            </button>
          </template>
          <p v-else class="bed-side__muted">护理任务数据暂不可用</p>
        </section>

        <section class="bed-card">
          <h3 class="bed-card__title"><CheckSquareOutlined /> 待处理事项 <em>{{ pendingTotal }} 项</em></h3>
          <button v-for="item in pendingItems" :key="item.status" type="button" class="bed-side__row" @click="setStatusFilter(item.status)">
            <span>{{ item.label }}</span>
            <span>{{ item.value }} 张 <RightOutlined /></span>
          </button>
        </section>

        <section class="bed-card bed-card--alert">
          <h3 class="bed-card__title"><AlertOutlined /> 异常预警 <em>{{ alertTotal }} 项</em></h3>
          <button v-for="item in alertItems" :key="item.key" type="button" class="bed-side__row" @click="item.action()">
            <span><i class="bed-side__bullet" :class="`bed-side__bullet--${item.tone}`"></i>{{ item.label }}</span>
            <span>{{ item.value }} {{ item.unit }} <RightOutlined /></span>
          </button>
        </section>
      </aside>
    </section>

    <section v-else class="bed-card">
      <header class="rm-card__head">
        <h3 class="rm-card__title">外出 / 住院记录</h3>
        <span class="rm-card__sub">外出请假与外出就医（住院）登记；外出、住院期间床位保持占用</span>
        <a-space class="rm-card__extra" wrap>
          <a-button v-if="recordType !== 'OUTING'" @click="exportMedicalRecords">导出就医记录</a-button>
          <a-button @click="openAction('outing', beds)">外出请假</a-button>
          <a-button type="primary" @click="openAction('medical', beds)">住院登记</a-button>
        </a-space>
      </header>
      <div class="bed-records__filters">
        <a-segmented v-model:value="recordType" :options="RECORD_TYPES" />
        <a-select v-model:value="recordStatus" :options="RECORD_STATUS_OPTIONS" class="bed-records__status" aria-label="记录状态" />
        <a-select
          v-model:value="recordElderId"
          allow-clear
          show-search
          :filter-option="false"
          :options="elderOptions"
          :loading="elderLoading"
          placeholder="全部长者"
          class="bed-records__elder"
          aria-label="长者"
          @search="searchElders"
          @focus="() => !elderOptions.length && searchElders('')"
        />
        <a-input v-model:value="recordKeyword" allow-clear placeholder="长者 / 医院 / 科室 / 诊断 / 原因" class="bed-records__keyword" />
        <a-range-picker v-model:value="recordRange" value-format="YYYY-MM-DD" :placeholder="['外出开始', '外出结束']" />
      </div>
      <a-table :data-source="visibleRecords" :columns="recordColumns" :loading="recordsLoading" row-key="key" size="middle" :pagination="{ pageSize: 20 }">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'kind'">{{ record.kind === 'medical' ? '外出就医 / 住院' : '外出请假' }}</template>
          <template v-else-if="column.key === 'status'">
            <span class="rm-pill" :class="record.canReturn ? 'rm-pill--warning' : record.status === 'RETURNED' ? 'rm-pill--success' : ''">{{ record.statusLabel }}</span>
            <span v-if="isAwayOverdue(record as AwayRecord, now)" class="rm-pill rm-pill--danger">逾期未返</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space :size="8">
              <a-popconfirm v-if="record.canApprove" :title="`批准 ${record.elderName} 的外出申请？`" @confirm="confirmApprove(record as AwayRecord)">
                <button type="button" class="rm-link">批准</button>
              </a-popconfirm>
              <a-popconfirm v-if="record.canReturn" :title="`确认 ${record.elderName} 已返院？`" @confirm="confirmReturn(record as AwayRecord)">
                <button type="button" class="rm-link">返院</button>
              </a-popconfirm>
              <a v-if="record.leaveNoteImageUrl" :href="record.leaveNoteImageUrl" target="_blank" rel="noopener noreferrer" class="rm-link">请假条</a>
              <a-popconfirm :title="`删除 ${record.elderName} 的这条记录？删除后不可恢复`" ok-text="删除" :ok-button-props="{ danger: true }" @confirm="confirmDelete(record as AwayRecord)">
                <button type="button" class="rm-link bed-records__delete">删除</button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
      <footer class="bed-records__links">
        <span>跨部门联动：</span>
        <button v-for="item in LINKAGE_LINKS" :key="item.label" type="button" class="rm-link" :title="item.hint" @click="router.push(item.path)">{{ item.label }}</button>
      </footer>
    </section>

    <section class="bed-card bed-today">
      <div class="bed-today__title">
        <CalendarOutlined />
        <span><strong>今日动态</strong><small>{{ todayLabel }}</small></span>
      </div>
      <button v-for="item in activityItems" :key="item.key" type="button" class="bed-today__item" :class="`bed-today__item--${item.key}`" @click="item.path ? router.push(item.path) : showAwayRecords()">
        <span class="bed-today__icon"><component :is="item.icon" /></span>
        <span class="bed-today__body">
          <span>{{ item.label }} <strong>{{ item.value ?? '--' }}</strong> 人</span>
          <small>{{ item.hint }}</small>
        </span>
        <RightOutlined />
      </button>
    </section>

    <a-drawer v-model:open="drawerOpen" :title="activeBed ? `${bedLabel(activeBed)} 床位详情` : '床位详情'" width="380">
      <template v-if="activeBed">
        <a-descriptions :column="1" size="small" bordered>
          <a-descriptions-item label="位置">{{ activeBed.building || '-' }} / {{ activeBed.floorNo || '-' }} / {{ activeBed.roomNo || '-' }}</a-descriptions-item>
          <a-descriptions-item label="床位状态">{{ BED_STATUS_META[bedStatus(activeBed)].label }}<template v-if="residentAwayLabel(activeBed)">（{{ residentAwayLabel(activeBed) }}）</template></a-descriptions-item>
          <a-descriptions-item label="长者">{{ activeBed.elderName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="性别 / 年龄">{{ activeBed.elderId ? residentMeta(activeBed, false) : '-' }}</a-descriptions-item>
          <a-descriptions-item :label="bedCaregiver(activeBed, caregivers)?.label || '护理员'">{{ bedCaregiver(activeBed, caregivers)?.name || '-' }}</a-descriptions-item>
          <a-descriptions-item label="护理等级">{{ activeBed.careLevel || '-' }}</a-descriptions-item>
          <a-descriptions-item label="风险">{{ bedRiskTags(activeBed).join('、') || '-' }}</a-descriptions-item>
          <a-descriptions-item label="最近评估">{{ [activeBed.latestAssessmentLevel, activeBed.latestAssessmentDate].filter(Boolean).join(' / ') || '-' }}</a-descriptions-item>
          <a-descriptions-item label="占用说明">{{ activeBed.occupancyNote || '-' }}</a-descriptions-item>
        </a-descriptions>
        <div class="bed-drawer__actions">
          <a-button v-if="activeBed.elderId" type="primary" @click="router.push(`/elder/detail/${activeBed.elderId}`)">长者档案</a-button>
          <a-button v-else-if="bedStatus(activeBed) === 'idle'" type="primary" @click="router.push(`/elder/admission-processing?bedId=${activeBed.id}`)">办理入住</a-button>
          <a-button @click="router.push('/logistics/assets/bed-management')">床位管理</a-button>
        </div>
        <div class="bed-drawer__actions">
          <template v-if="activeBed.elderId">
            <a-button v-if="residentAwayLabel(activeBed) === '外出'" @click="openAction('return', [activeBed])">返院确认</a-button>
            <template v-else>
              <a-button @click="openAction('outing', [activeBed])">外出请假</a-button>
              <a-button @click="openAction('medical', [activeBed])">住院登记</a-button>
            </template>
            <a-button @click="openAction('transfer', [activeBed])">调床</a-button>
          </template>
          <a-button v-else-if="bedStatus(activeBed) === 'idle'" @click="openAction('assign', [activeBed])">安排床位</a-button>
        </div>
      </template>
    </a-drawer>

    <a-drawer v-model:open="actionOpen" :title="actionKind ? ACTION_TITLES[actionKind] : ''" width="420" destroy-on-close>
      <a-form ref="actionFormRef" :model="actionForm" layout="vertical">
        <a-form-item label="长者" name="elderId" :rules="[{ required: true, message: '请选择长者' }]">
          <a-select v-model:value="actionForm.elderId" :options="actionElderOptions" :loading="elderOptionsLoading" show-search option-filter-prop="label" placeholder="选择长者" :not-found-content="actionKind === 'return' ? '该范围内没有外出中的长者' : '暂无可选长者'" />
        </a-form-item>
        <template v-if="actionKind === 'transfer' || actionKind === 'assign'">
          <a-form-item :label="actionKind === 'transfer' ? '目标床位（仅空床）' : '床位（仅空床）'" name="bedId" :rules="[{ required: true, message: '请选择空床' }]">
            <a-select v-model:value="actionForm.bedId" :options="idleBedOptions" show-search option-filter-prop="label" placeholder="选择空床" not-found-content="当前没有空床" />
          </a-form-item>
          <a-form-item label="生效日期" name="startDate" :rules="[{ required: true, message: '请选择生效日期' }]">
            <a-date-picker v-model:value="actionForm.startDate" value-format="YYYY-MM-DD" style="width: 100%" />
          </a-form-item>
        </template>
        <template v-else-if="actionKind === 'outing' || actionKind === 'medical'">
          <a-form-item :label="actionKind === 'medical' ? '入院日期' : '外出日期'" name="outingDate" :rules="[{ required: true, message: '请选择日期' }]">
            <a-date-picker v-model:value="actionForm.outingDate" value-format="YYYY-MM-DD" style="width: 100%" />
          </a-form-item>
          <a-form-item label="预计返院时间" name="expectedReturnTime">
            <a-date-picker v-model:value="actionForm.expectedReturnTime" show-time value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
          </a-form-item>
          <template v-if="actionKind === 'medical'">
            <a-form-item label="医院" name="hospitalName" :rules="[{ required: true, message: '请填写医院' }]">
              <a-input v-model:value="actionForm.hospitalName" />
            </a-form-item>
            <a-form-item label="科室" name="department"><a-input v-model:value="actionForm.department" /></a-form-item>
            <a-form-item label="诊断" name="diagnosis"><a-input v-model:value="actionForm.diagnosis" /></a-form-item>
          </template>
          <a-form-item label="陪同人" name="companion"><a-input v-model:value="actionForm.companion" /></a-form-item>
          <a-form-item :label="actionKind === 'medical' ? '就医原因' : '外出事由'" name="reason" :rules="[{ required: true, message: '请填写原因' }]">
            <a-textarea v-model:value="actionForm.reason" :rows="2" />
          </a-form-item>
          <a-form-item v-if="actionKind === 'outing'" label="请假条照片">
            <a-space>
              <a-upload :show-upload-list="false" :before-upload="uploadLeaveNote" accept="image/*">
                <a-button :loading="leaveNoteUploading">拍照 / 上传请假条</a-button>
              </a-upload>
              <a v-if="actionForm.leaveNoteImageUrl" :href="actionForm.leaveNoteImageUrl" target="_blank" rel="noopener noreferrer">查看已上传</a>
            </a-space>
          </a-form-item>
        </template>
        <template v-else-if="actionKind === 'return'">
          <a-form-item label="外出记录">
            <a-spin :spinning="openRecordLoading">
              <span v-if="openRecord">{{ openRecord.kind === 'medical' ? '外出就医 / 住院' : '外出请假' }} · {{ openRecord.outingDate }}{{ openRecord.place ? ` · ${openRecord.place}` : '' }}</span>
              <span v-else class="bed-side__muted">{{ actionForm.elderId ? '未找到进行中的外出 / 住院记录' : '请先选择长者' }}</span>
            </a-spin>
          </a-form-item>
        </template>
        <a-form-item v-if="actionKind === 'return' || actionKind === 'outing' || actionKind === 'medical'" label="备注" name="remark">
          <a-input v-model:value="actionForm.remark" />
        </a-form-item>
      </a-form>
      <p v-if="actionKind === 'outing' || actionKind === 'medical'" class="bed-side__muted">外出、住院期间床位保持占用，不会释放。</p>
      <template #footer>
        <div class="bed-drawer__footer">
          <a-button @click="actionOpen = false">取消</a-button>
          <a-button type="primary" :loading="actionSaving" :disabled="actionKind === 'return' && !openRecord" @click="submitAction">确认办理</a-button>
        </div>
      </template>
    </a-drawer>

    <p class="rm-note"><SafetyCertificateOutlined /> 分床、调床仅可选择空床，后端同时校验床位占用；外出、住院不释放床位，返院后恢复在院状态。</p>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch, type Component } from 'vue'
import dayjs from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import { message, type FormInstance } from 'ant-design-vue'
import {
  AlertOutlined, BellOutlined, CalendarOutlined, CarOutlined, CheckSquareOutlined, CloseOutlined, PlusOutlined, SafetyCertificateOutlined, ExportOutlined, FilterOutlined, ImportOutlined, SwapOutlined, HeartOutlined, HomeOutlined, InfoCircleOutlined, LeftOutlined,
  PieChartOutlined, ReloadOutlined, RightOutlined, ScheduleOutlined, SkinOutlined, TeamOutlined,
  ToolOutlined, UndoOutlined, WarningOutlined
} from '@ant-design/icons-vue'
import PageContainer from '../../../components/PageContainer.vue'
import { getBedMap, getRoomList } from '../../../api/bed'
import { getTaskSummary, getTodayTasks } from '../../../api/care'
import { getChangeLogs } from '../../../api/elderLifecycle'
import { assignBed, getElderPage, uploadElderFile } from '../../../api/elder'
import {
  approveOuting, createMedicalOuting, createOuting, deleteMedicalOuting, deleteOuting, exportMedicalOuting, getMedicalOutingPage, getOutingPage, getResidenceStatusSummary, returnMedicalOuting, returnOuting
} from '../../../api/elderResidence'
import { useElderOptions } from '../../../composables/useElderOptions'
import { useLiveSyncRefresh } from '../../../composables/useLiveSyncRefresh'
import type { BedItem, CareTaskSummary, Id, MedicalOutingItem, MedicalOutingStatus, OutingItem } from '../../../types'
import '../residentUi.css'
import { ageFromBirthDate, BED_STATUS_META, bedCaregiver, bedLabel, countBedTransfers, filterAwayRecords, isAwayOverdue, mergeAwayRecords, orientationLabel, parsePanoramaQuery, type AwayRecord, type AwayRecordType, type AwayStatusFilter, primaryCaregivers, bedRiskTags, bedStatus, floorSortValue, groupRooms, residentAwayLabel, roomTypeLabel, type BedStatusKey } from './bedPanoramaModel'

const STATUS_KEYS = Object.keys(BED_STATUS_META) as BedStatusKey[]
const router = useRouter()
const route = useRoute()
const pagePath = route.path

const beds = ref<BedItem[]>([])
const roomTypes = ref<Record<string, string>>({})
const roomOrientations = ref<Record<string, string>>({})
const loading = ref(false)
const refreshedAt = ref('')
const building = ref<string>()
const floor = ref<string>()
const roomTypeFilter = ref('ALL')
const statusFilter = ref<'ALL' | BedStatusKey>('ALL')
const riskFilter = ref<'ALL' | 'RISK'>('ALL')
const keyword = ref('')
const drawerOpen = ref(false)
const caregivers = ref<Record<string, string>>({})
const careSummary = ref<CareTaskSummary | null>(null)
const activity = ref<{ admitted?: number; admittedYesterday?: number; discharged?: number; dischargedYesterday?: number; transferred?: number; away?: number }>({})
const todayLabel = dayjs().format('YYYY-MM-DD（ddd）')
const activeBed = ref<BedItem | null>(null)

const statusOptions = [{ label: '全部状态', value: 'ALL' }, ...STATUS_KEYS.map((key) => ({ label: BED_STATUS_META[key].label, value: key }))]
const riskOptions = [{ label: '全部长者', value: 'ALL' }, { label: '仅重点关注', value: 'RISK' }]

const buildingOf = (bed: BedItem) => String(bed.building || '').trim() || '未分配楼栋'
const floorOf = (bed: BedItem) => String(bed.floorNo || '').trim() || '未知楼层'
const isFocusResident = (bed: BedItem) => !!bed.elderId && bedRiskTags(bed).length > 0

const counts = computed(() => {
  const result = Object.fromEntries(STATUS_KEYS.map((key) => [key, 0])) as Record<BedStatusKey, number>
  beds.value.forEach((bed) => { result[bedStatus(bed)] += 1 })
  return result
})
const focusCount = computed(() => beds.value.filter(isFocusResident).length)
const vitalAbnormalCount = computed(() => beds.value.filter((bed) => bed.elderId && Number(bed.abnormalVital24hCount || 0) > 0).length)
const awayCount = computed(() => beds.value.filter((bed) => residentAwayLabel(bed)).length)
const occupancyRate = computed(() => beds.value.length ? ((counts.value.occupied / beds.value.length) * 100).toFixed(1) : '0.0')

const kpis = computed<Array<{ key: string; label: string; value: string | number; unit: string; icon: Component; tone: string; status?: BedStatusKey }>>(() => [
  { key: 'total', label: '总床位', value: beds.value.length, unit: '张', icon: HomeOutlined, tone: 'blue' },
  { key: 'occupied', label: '已入住', value: counts.value.occupied, unit: '张', icon: TeamOutlined, tone: 'green', status: 'occupied' },
  { key: 'idle', label: '空床', value: counts.value.idle, unit: '张', icon: SkinOutlined, tone: 'teal', status: 'idle' },
  { key: 'reserved', label: '预留', value: counts.value.reserved, unit: '张', icon: ScheduleOutlined, tone: 'orange', status: 'reserved' },
  { key: 'cleaning', label: '清洁中', value: counts.value.cleaning, unit: '张', icon: SkinOutlined, tone: 'purple', status: 'cleaning' },
  { key: 'maintenance', label: '维修中', value: counts.value.maintenance, unit: '张', icon: ToolOutlined, tone: 'amber', status: 'maintenance' },
  { key: 'rate', label: '入住率', value: occupancyRate.value, unit: '%', icon: PieChartOutlined, tone: 'green' },
  { key: 'focus', label: '重点关注', value: focusCount.value, unit: '人', icon: HeartOutlined, tone: 'red' }
])

const buildingOptions = computed(() => Array.from(new Set(beds.value.map(buildingOf)))
  .sort((a, b) => a.localeCompare(b, 'zh-CN', { numeric: true }))
  .map((value) => ({ label: value, value })))
const floorOptions = computed(() => Array.from(new Set(beds.value.filter((bed) => buildingOf(bed) === building.value).map(floorOf)))
  .sort((a, b) => floorSortValue(a) - floorSortValue(b))
  .map((value) => ({ label: value, value })))
const floorIndex = computed(() => floorOptions.value.findIndex((item) => item.value === floor.value))
const roomTypeOptions = computed(() => [
  { label: '全部房型', value: 'ALL' },
  ...Array.from(new Set(beds.value.map((bed) => roomTypeLabel(roomTypes.value[String(bed.roomId)] || bed.roomType)))).map((value) => ({ label: value, value }))
])

const floorBeds = computed(() => beds.value.filter((bed) => buildingOf(bed) === building.value && floorOf(bed) === floor.value))
const floorRooms = computed(() => groupRooms(floorBeds.value, roomTypes.value))
const floorCounts = computed(() => {
  const result = Object.fromEntries(STATUS_KEYS.map((key) => [key, 0])) as Record<BedStatusKey, number>
  floorBeds.value.forEach((bed) => { result[bedStatus(bed)] += 1 })
  return result
})

function matchesKeyword(bed: BedItem) {
  const text = keyword.value.trim().toLowerCase()
  return !text || `${bed.elderName || ''} ${bed.roomNo || ''} ${bed.roomNo || ''}-${bed.bedNo || ''}`.toLowerCase().includes(text)
}

function matchesFilters(bed: BedItem) {
  if (statusFilter.value !== 'ALL' && bedStatus(bed) !== statusFilter.value) return false
  if (riskFilter.value === 'RISK' && !isFocusResident(bed)) return false
  return matchesKeyword(bed)
}

const visibleRooms = computed(() => floorRooms.value
  .filter((room) => roomTypeFilter.value === 'ALL' || room.roomType === roomTypeFilter.value)
  .map((room) => ({ ...room, visibleBeds: room.beds.filter(matchesFilters) }))
  .filter((room) => room.visibleBeds.length))

const pendingItems = computed(() => [
  { label: '待清洁床位', value: counts.value.cleaning, status: 'cleaning' as const },
  { label: '待维修床位', value: counts.value.maintenance, status: 'maintenance' as const },
  { label: '待确认预留', value: counts.value.reserved, status: 'reserved' as const },
  { label: '锁定床位', value: counts.value.locked, status: 'locked' as const }
])
const pendingTotal = computed(() => pendingItems.value.reduce((sum, item) => sum + item.value, 0))
const alertItems = computed(() => [
  { key: 'focus', label: '高风险长者', value: focusCount.value, unit: '人', tone: 'red', action: () => { riskFilter.value = 'RISK' } },
  { key: 'vital', label: '24 小时体征异常', value: vitalAbnormalCount.value, unit: '人', tone: 'orange', action: () => { riskFilter.value = 'RISK' } },
  { key: 'away', label: '外出 / 待退住', value: awayCount.value, unit: '人', tone: 'gold', action: () => setStatusFilter('occupied') }
])
const alertTotal = computed(() => alertItems.value.reduce((sum, item) => sum + item.value, 0))

function residentMeta(bed: BedItem, withCareLevel = true) {
  const age = ageFromBirthDate(bed.elderBirthDate, new Date())
  const gender = bed.elderGender === 1 ? '男' : bed.elderGender === 2 ? '女' : '性别未录入'
  return [gender, age == null ? '' : `${age}岁`, withCareLevel ? bed.careLevel : ''].filter(Boolean).join(' · ')
}

const reminderItems = computed(() => [
  { key: 'pending', label: '待执行护理任务', value: Number(careSummary.value?.pendingCount || 0), path: '/care/today' },
  { key: 'overdue', label: '已逾期护理任务', value: Number(careSummary.value?.overdueCount || 0), path: '/care/today' },
  { key: 'exception', label: '执行异常任务', value: Number(careSummary.value?.exceptionCount || 0), path: '/care/exception' },
  { key: 'unassigned', label: '未分配护理员', value: Number(careSummary.value?.unassignedCount || 0), path: '/care/today' }
])
const reminderTotal = computed(() => reminderItems.value.reduce((sum, item) => sum + item.value, 0))

function compareHint(today?: number, yesterday?: number) {
  if (today == null || yesterday == null) return '较昨日 --'
  const diff = today - yesterday
  return `较昨日 ${diff > 0 ? '+' : ''}${diff}`
}

const activityItems = computed(() => [
  { key: 'admit', label: '入院', value: activity.value.admitted, hint: compareHint(activity.value.admitted, activity.value.admittedYesterday), icon: ImportOutlined, path: '/elder/change-log' },
  { key: 'discharge', label: '出院', value: activity.value.discharged, hint: compareHint(activity.value.discharged, activity.value.dischargedYesterday), icon: ExportOutlined, path: '/elder/change-log' },
  { key: 'transfer', label: '转床', value: activity.value.transferred, hint: '床位调整 / 护理需要', icon: SwapOutlined, path: '/elder/change-log' },
  { key: 'away', label: '外出中', value: activity.value.away, hint: '请假外出 / 外出就医', icon: CarOutlined, path: '' }
])

function emptyBedHint(bed: BedItem) {
  const status = bedStatus(bed)
  if (bed.occupancyNote) return bed.occupancyNote
  if (status === 'reserved') return bed.lockExpiresAt ? `预留至 ${dayjs(bed.lockExpiresAt).format('MM-DD HH:mm')}` : '已预留'
  if (status === 'cleaning') return '退床清洁中'
  if (status === 'maintenance') return '设备维修中'
  if (status === 'locked') return '床位已锁定'
  return '暂未入住'
}

function setStatusFilter(status: BedStatusKey) {
  statusFilter.value = statusFilter.value === status ? 'ALL' : status
}

function stepFloor(step: number) {
  floor.value = floorOptions.value[floorIndex.value + step]?.value ?? floor.value
}

function resetFilters() {
  roomTypeFilter.value = 'ALL'
  statusFilter.value = 'ALL'
  riskFilter.value = 'ALL'
  keyword.value = ''
}

function openBed(bed: BedItem) {
  activeBed.value = bed
  drawerOpen.value = true
}

// 楼栋变化或当前楼层失效时，落到该楼栋最低楼层
watch(floorOptions, (options) => {
  if (!options.some((item) => item.value === floor.value)) floor.value = options[0]?.value
})

// 搜索命中不在当前楼层时，跳到第一个命中的楼层
watch(keyword, () => {
  if (!keyword.value.trim() || floorBeds.value.some(matchesKeyword)) return
  const hit = beds.value.find(matchesKeyword)
  if (!hit) return
  building.value = buildingOf(hit)
  floor.value = floorOf(hit)
})

function dayRange(offset: number) {
  const day = dayjs().add(offset, 'day')
  return { startTime: day.startOf('day').format('YYYY-MM-DDTHH:mm:ss'), endTime: day.endOf('day').format('YYYY-MM-DDTHH:mm:ss') }
}

async function countChanges(changeType: string, offset: number) {
  const page = await getChangeLogs({ changeType, pageNo: 1, pageSize: 1, ...dayRange(offset) })
  return page.total
}

// 这些数据各自有权限，取不到的显示 --，不影响床位图
async function loadActivity() {
  const today = dayjs().format('YYYY-MM-DD')
  const value = <T,>(result: PromiseSettledResult<T>) => (result.status === 'fulfilled' ? result.value : undefined)
  const [admitted, admittedYesterday, discharged, dischargedYesterday, transfers, residence, summary, tasks] = await Promise.allSettled([
    countChanges('ADMISSION', 0),
    countChanges('ADMISSION', -1),
    countChanges('DISCHARGE', 0),
    countChanges('DISCHARGE', -1),
    getChangeLogs({ changeType: 'BED_CHANGE', pageNo: 1, pageSize: 500, ...dayRange(0) }),
    getResidenceStatusSummary({ silent403: true }),
    getTaskSummary({ date: today }, { silent403: true }),
    getTodayTasks({ date: today }, { silent403: true })
  ])
  const residenceValue = value(residence)
  activity.value = {
    admitted: value(admitted),
    admittedYesterday: value(admittedYesterday),
    discharged: value(discharged),
    dischargedYesterday: value(dischargedYesterday),
    transferred: transfers.status === 'fulfilled' ? countBedTransfers(transfers.value.list) : undefined,
    away: residenceValue ? Number(residenceValue.outingCount || 0) + Number(residenceValue.medicalOutingCount || 0) : undefined
  }
  careSummary.value = value(summary) ?? null
  caregivers.value = tasks.status === 'fulfilled' ? primaryCaregivers(tasks.value.list) : {}
}

async function loadData() {
  loadActivity().catch(() => {})
  loading.value = true
  try {
    const [bedList, rooms] = await Promise.all([getBedMap({ params: { includeRisk: true } }), getRoomList()])
    beds.value = bedList
    roomTypes.value = Object.fromEntries(rooms.map((room) => [String(room.id), room.roomType || '']))
    roomOrientations.value = Object.fromEntries(rooms.map((room) => [String(room.id), room.orientation || '']))
    if (!buildingOptions.value.some((item) => item.value === building.value)) building.value = buildingOptions.value[0]?.value
    refreshedAt.value = dayjs().format('YYYY-MM-DD HH:mm:ss')
  } finally {
    loading.value = false
  }
  if (view.value === 'records') loadRecords()
}

// ---- 选中房间、外出/住院记录、在院状态快捷办理 ----
type ActionKind = 'outing' | 'medical' | 'return' | 'transfer' | 'assign'
const ACTION_TITLES: Record<ActionKind, string> = { outing: '外出请假', medical: '住院登记', return: '返院确认', transfer: '调床', assign: '安排床位' }
const RESIDENCE_ACTIONS: ActionKind[] = ['outing', 'medical', 'return']
const STATUS_PILL: Record<BedStatusKey, string> = { occupied: 'info', idle: 'success', reserved: 'warning', cleaning: 'info', maintenance: 'danger', locked: 'danger' }
const VIEW_OPTIONS = [{ label: '床位网格', value: 'grid' }, { label: '外出 / 住院记录', value: 'records' }]
const RECORD_TYPES = [{ label: '全部类型', value: 'ALL' }, { label: '外出请假', value: 'OUTING' }, { label: '外出就医 / 住院', value: 'MEDICAL_OUTING' }]
const RECORD_STATUS_OPTIONS = [
  { label: '全部状态', value: 'ALL' }, { label: '待审批', value: 'APPLIED' }, { label: '外出中', value: 'OUT' }, { label: '已返院', value: 'RETURNED' }, { label: '已取消', value: 'CANCELLED' }
]
// 原「入住状态变更」页的跨部门任务入口
const LINKAGE_LINKS = [
  { label: '综合任务中心', hint: '状态变更触发的待办与告警任务', path: '/oa/work-execution/task?source=lifecycle&scene=status-change&category=alert&quick=overdue&status=OPEN' },
  { label: '医护任务中心', hint: '就医外出后联动的医护处置', path: '/medical-care/unified-task-center?source=lifecycle&scene=status-change&onlyOverdue=1&sortBy=RISK_SCORE&sortDirection=DESC' },
  { label: '后勤任务中心', hint: '床位清洁与维修任务', path: '/logistics/task-center?source=lifecycle&scene=status-change&view=DUTY&tab=maintenance&lifecycleFocus=maintenance&overdueOnly=1' }
]
const recordColumns = [
  { title: '类型', key: 'kind', width: 130 },
  { title: '长者', dataIndex: 'elderName', width: 100 },
  { title: '外出日期', dataIndex: 'outingDate', width: 120 },
  { title: '预计返院', dataIndex: 'expectedReturnTime', customRender: ({ text }: { text?: string }) => formatTime(text) },
  { title: '实际返院', dataIndex: 'actualReturnTime', customRender: ({ text }: { text?: string }) => formatTime(text) },
  { title: '医院 / 陪同', dataIndex: 'place', customRender: ({ text }: { text?: string }) => text || '--' },
  { title: '原因', dataIndex: 'reason', ellipsis: true, customRender: ({ text }: { text?: string }) => text || '--' },
  { title: '状态', key: 'status', width: 100 },
  { title: '操作', key: 'action', width: 180 }
]

const view = ref<'grid' | 'records'>('grid')
const selectedRoomKey = ref('')
const selectedRoom = computed(() => floorRooms.value.find((room) => room.key === selectedRoomKey.value))
const selectedRoomIdle = computed(() => (selectedRoom.value?.beds || []).filter((item) => bedStatus(item) === 'idle'))

const recordType = ref<AwayRecordType>('ALL')
const recordStatus = ref<AwayStatusFilter>('OUT')
const recordElderId = ref<Id>()
const recordKeyword = ref('')
const recordRange = ref<[string, string]>()
const recordsLoading = ref(false)
const awayRecords = ref<AwayRecord[]>([])
const now = ref(new Date())
const visibleRecords = computed(() => filterAwayRecords(awayRecords.value, { keyword: recordKeyword.value, from: recordRange.value?.[0], to: recordRange.value?.[1] }))
const focusElderId = ref('')
const { elderOptions, elderLoading, searchElders, ensureSelectedElder } = useElderOptions({ pageSize: 80 })

// 旧外出登记 / 外出就医 / 变更总览页重定向过来时带的筛选；从具体长者进来默认看他的全部记录
function applyQueryFilters(query: ReturnType<typeof parsePanoramaQuery>) {
  view.value = query.view
  recordType.value = query.recordType
  recordStatus.value = query.status || (query.recordType !== 'ALL' || query.elderId ? 'ALL' : 'OUT')
  recordElderId.value = query.elderId || undefined
  focusElderId.value = query.elderId
  if (query.elderId) ensureSelectedElder(query.elderId)
}
applyQueryFilters(parsePanoramaQuery(route.query))

// 定位到长者所在楼层并高亮；action 是一次性参数，用完从地址栏去掉，刷新不会重复打开
function applyQueryFocus() {
  const query = parsePanoramaQuery(route.query)
  const bed = query.elderId ? beds.value.find((item) => String(item.elderId) === query.elderId) : undefined
  if (bed) {
    building.value = buildingOf(bed)
    floor.value = floorOf(bed)
    ensureSelectedElder(query.elderId, bed.elderName)
  }
  if (!query.action) return
  openAction(query.action, bed ? [bed] : beds.value)
  if (query.elderId) {
    if (actionElderOptions.value.some((item) => String(item.value) === query.elderId)) actionForm.elderId = query.elderId
    else message.warning(bed ? '该长者当前外出中或待退住，不能重复登记' : '未找到该长者的在住床位，请手动选择')
  }
  const rest = { ...route.query }
  delete rest.action
  router.replace({ path: route.path, query: rest })
}

function showAwayRecords() {
  view.value = 'records'
  recordType.value = 'ALL'
  recordStatus.value = 'OUT'
}

function formatTime(value?: string) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '--'
}

// shortcut: 每类只取最近 200 条，记录多到需要翻页时再改成服务端分页
async function loadRecords() {
  recordsLoading.value = true
  try {
    const status = recordStatus.value === 'ALL' ? undefined : recordStatus.value
    const elderId = recordElderId.value || undefined
    // 外出就医没有待审批 / 已取消状态
    const withMedical = recordType.value !== 'OUTING' && (!status || status === 'OUT' || status === 'RETURNED')
    const [outings, medicals] = await Promise.all([
      recordType.value === 'MEDICAL_OUTING' ? [] as OutingItem[] : getOutingPage({ pageNo: 1, pageSize: 200, elderId, status }).then((page) => page.list),
      withMedical ? getMedicalOutingPage({ pageNo: 1, pageSize: 200, elderId, status: status as MedicalOutingStatus | undefined }).then((page) => page.list) : [] as MedicalOutingItem[]
    ])
    awayRecords.value = mergeAwayRecords(outings, medicals)
    now.value = new Date()
  } finally {
    recordsLoading.value = false
  }
}

watch([view, recordType, recordStatus, recordElderId], () => { if (view.value === 'records') loadRecords() })

// 已在本页时再从菜单 / 旧地址跳进来（组件复用，不会重新 mounted）
watch(() => route.query, () => {
  if (route.path !== pagePath || !Object.keys(route.query).length) return
  applyQueryFilters(parsePanoramaQuery(route.query))
  applyQueryFocus()
})

function doReturn(record: AwayRecord, remark?: string) {
  return record.kind === 'medical' ? returnMedicalOuting(record.id, { remark }) : returnOuting(record.id, { remark })
}

async function confirmApprove(record: AwayRecord) {
  await approveOuting(record.id)
  message.success(`${record.elderName} 的外出申请已批准`)
  loadData()
}

async function confirmDelete(record: AwayRecord) {
  await (record.kind === 'medical' ? deleteMedicalOuting(record.id) : deleteOuting(record.id))
  message.success('记录已删除')
  loadData()
}

function exportMedicalRecords() {
  const status = recordStatus.value === 'OUT' || recordStatus.value === 'RETURNED' ? recordStatus.value : undefined
  return exportMedicalOuting({ elderId: recordElderId.value || undefined, status, keyword: recordKeyword.value.trim() || undefined })
}

async function confirmReturn(record: AwayRecord) {
  await doReturn(record)
  message.success(`${record.elderName} 已确认返院`)
  loadData()
}

const actionOpen = ref(false)
const actionKind = ref<ActionKind>()
const actionScope = ref<BedItem[]>([])
const actionSaving = ref(false)
const actionFormRef = ref<FormInstance>()
const actionForm = reactive({
  elderId: undefined as Id | undefined,
  bedId: undefined as Id | undefined,
  startDate: '',
  outingDate: '',
  expectedReturnTime: undefined as string | undefined,
  hospitalName: '',
  department: '',
  diagnosis: '',
  companion: '',
  reason: '',
  leaveNoteImageUrl: '',
  remark: ''
})
const leaveNoteUploading = ref(false)
const unassignedElders = ref<Array<{ label: string; value: Id }>>([])
const elderOptionsLoading = ref(false)
const openRecord = ref<AwayRecord | null>(null)
const openRecordLoading = ref(false)

const actionElderOptions = computed(() => {
  if (actionKind.value === 'assign') return unassignedElders.value
  return actionScope.value
    .filter((item) => item.elderId && (actionKind.value === 'return' ? residentAwayLabel(item) === '外出' : actionKind.value === 'transfer' || !residentAwayLabel(item)))
    .map((item) => ({ label: `${bedLabel(item)} ${item.elderName || ''}`, value: item.elderId as Id }))
})

// 排他：调床/分床目标只列当前空床（预留、清洁、维修、锁定都不可选），后端 assignBed 也会拒绝已占用床位
const idleBedOptions = computed(() => beds.value
  .filter((item) => bedStatus(item) === 'idle')
  .map((item) => ({ label: [buildingOf(item), floorOf(item), bedLabel(item), roomTypeLabel(roomTypes.value[String(item.roomId)] || item.roomType)].join(' · '), value: item.id })))

// shortcut: 未分床长者从前 500 条档案里筛选，长者规模更大时改为后端过滤
async function loadUnassignedElders() {
  elderOptionsLoading.value = true
  try {
    const page = await getElderPage({ pageNo: 1, pageSize: 500 })
    unassignedElders.value = page.list
      .filter((item) => !item.bedId && !['DISCHARGED', 'DECEASED'].includes(String(item.lifecycleStatus || '').toUpperCase()))
      .map((item) => ({ label: `${item.fullName || '未命名长者'}${item.elderCode ? ` (${item.elderCode})` : ''}`, value: item.id }))
  } finally {
    elderOptionsLoading.value = false
  }
}

function openAction(kind: ActionKind, scope: BedItem[]) {
  actionKind.value = kind
  actionScope.value = scope
  Object.assign(actionForm, {
    elderId: undefined,
    bedId: kind === 'assign' ? scope.find((item) => bedStatus(item) === 'idle')?.id : undefined,
    startDate: dayjs().format('YYYY-MM-DD'),
    outingDate: dayjs().format('YYYY-MM-DD'),
    expectedReturnTime: undefined,
    hospitalName: '',
    department: '',
    diagnosis: '',
    companion: '',
    reason: '',
    leaveNoteImageUrl: '',
    remark: ''
  })
  if (kind !== 'assign' && actionElderOptions.value.length === 1) actionForm.elderId = actionElderOptions.value[0].value
  openRecord.value = null
  actionOpen.value = true
  if (kind === 'assign') loadUnassignedElders().catch(() => {})
}

// 返院确认：找该长者进行中的外出 / 住院记录
watch(() => [actionKind.value, actionForm.elderId], async () => {
  openRecord.value = null
  const elderId = actionForm.elderId
  if (actionKind.value !== 'return' || !elderId) return
  openRecordLoading.value = true
  try {
    const [outings, medicals] = await Promise.all([
      getOutingPage({ pageNo: 1, pageSize: 1, elderId, status: 'OUT' }),
      getMedicalOutingPage({ pageNo: 1, pageSize: 1, elderId, status: 'OUT' })
    ])
    if (actionForm.elderId === elderId) openRecord.value = mergeAwayRecords(outings.list, medicals.list)[0] ?? null
  } finally {
    openRecordLoading.value = false
  }
})

async function uploadLeaveNote(file: File) {
  leaveNoteUploading.value = true
  try {
    actionForm.leaveNoteImageUrl = (await uploadElderFile(file, 'elder-outing-leave-note'))?.fileUrl || ''
    message.success('请假条上传成功')
  } finally {
    leaveNoteUploading.value = false
  }
  return false
}

async function submitAction() {
  try {
    await actionFormRef.value?.validate()
  } catch {
    return
  }
  const kind = actionKind.value
  const form = actionForm
  if (!kind || form.elderId == null) return
  actionSaving.value = true
  try {
    if (kind === 'transfer' || kind === 'assign') {
      await assignBed(form.elderId, form.bedId as Id, form.startDate)
    } else if (kind === 'outing') {
      await createOuting({ elderId: form.elderId, outingDate: form.outingDate, expectedReturnTime: form.expectedReturnTime, companion: form.companion, reason: form.reason, leaveNoteImageUrl: form.leaveNoteImageUrl || undefined, remark: form.remark })
    } else if (kind === 'medical') {
      await createMedicalOuting({
        elderId: form.elderId, outingDate: form.outingDate, expectedReturnTime: form.expectedReturnTime, hospitalName: form.hospitalName,
        department: form.department, diagnosis: form.diagnosis, companion: form.companion, reason: form.reason, remark: form.remark
      })
    } else if (openRecord.value) {
      await doReturn(openRecord.value, form.remark || undefined)
    }
    message.success(`${ACTION_TITLES[kind]}已办理`)
    actionOpen.value = false
    drawerOpen.value = false
    loadData()
  } finally {
    actionSaving.value = false
  }
}

useLiveSyncRefresh({ topics: ['elder', 'bed', 'lifecycle'], refresh: loadData })
onMounted(async () => {
  await loadData()
  applyQueryFocus()
})
</script>

<style scoped>
.bed-panorama {
  --bp-green: #21705f;
  --bp-green-soft: #e8f3ef;
  --bp-border: #e3ebe7;
  --bp-text: #1f2d28;
  --bp-muted: #6b7a74;
}

.bed-panorama__updated {
  color: var(--bp-muted);
  font-size: 13px;
}

.bed-card {
  background: #fff;
  border: 1px solid var(--bp-border);
  border-radius: 12px;
  padding: 16px;
}

.bed-card__title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 600;
  color: var(--bp-text);
}

.bed-card__title em {
  margin-left: auto;
  font-style: normal;
  color: #d4380d;
}

.bed-kpis {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.bed-kpi {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: #fff;
  border: 1px solid var(--bp-border);
  border-radius: 12px;
  text-align: left;
  cursor: pointer;
  font: inherit;
}

.bed-kpi:hover {
  border-color: var(--bp-green);
}

.bed-kpi__icon {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  flex: none;
  border-radius: 10px;
  font-size: 20px;
  background: var(--kpi-soft);
  color: var(--kpi-color);
}

.bed-kpi__body {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.bed-kpi__label {
  white-space: nowrap;
  color: var(--bp-muted);
  font-size: 13px;
}

.bed-kpi__value {
  font-size: 24px;
  line-height: 1.2;
  color: var(--bp-text);
}

.bed-kpi__value small {
  margin-left: 2px;
  font-size: 12px;
  font-weight: 400;
  color: var(--bp-muted);
}

.bed-kpi--blue { --kpi-color: #2f6fd6; --kpi-soft: #e8f0fc; }
.bed-kpi--green { --kpi-color: #21705f; --kpi-soft: #e8f3ef; }
.bed-kpi--teal { --kpi-color: #3a9a8a; --kpi-soft: #e6f4f1; }
.bed-kpi--orange { --kpi-color: #d48806; --kpi-soft: #fff5e1; }
.bed-kpi--purple { --kpi-color: #7a5af5; --kpi-soft: #f1edff; }
.bed-kpi--amber { --kpi-color: #e8590c; --kpi-soft: #fff0e6; }
.bed-kpi--red { --kpi-color: #cf1322; --kpi-soft: #fff1f0; }

.bed-layout {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr) 280px;
  gap: 16px;
  align-items: start;
}

.bed-filters {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.bed-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: var(--bp-text);
  font-size: 13px;
}

.bed-floor__head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 12px;
  margin-bottom: 16px;
  border-bottom: 1px solid var(--bp-border);
}

.bed-floor__title {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 8px;
  min-width: 0;
}

.bed-floor__title h2 {
  margin: 0;
  font-size: 20px;
  color: var(--bp-green);
}

.bed-floor__title span {
  color: var(--bp-muted);
  font-size: 13px;
}

.bed-floor__switch {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.bed-floor__select {
  width: 110px;
}

.bed-floor__empty {
  padding: 48px 0;
}

.bed-rooms {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
}

.bed-room {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px;
  border: 1px solid var(--bp-border);
  border-radius: 10px;
  background: #fafcfb;
}

.bed-room__head {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 0;
  border: 0;
  background: none;
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.bed-room.is-selected {
  border-color: var(--bp-green);
  box-shadow: 0 0 0 2px var(--bp-green-soft);
}

.bed-room-panel__close {
  margin-left: auto;
}

.bed-room-panel__head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bed-room-panel__head strong {
  font-size: 18px;
  color: var(--bp-text);
}

.bed-room-panel__meta {
  margin: 4px 0 8px;
  color: var(--bp-muted);
  font-size: 12px;
}

.bed-room-panel__bed {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 6px 0;
  border-bottom: 1px dashed var(--bp-border);
  font-size: 13px;
}

.bed-room-panel__section {
  margin-top: 16px;
}

.bed-side__row:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.bed-drawer__footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.bed-room__head strong {
  font-size: 16px;
  color: var(--bp-text);
}

.bed-room__head span {
  color: var(--bp-muted);
  font-size: 12px;
}

.bed-room__count {
  margin: 0 0 0 auto;
}

.bed-slot {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 100%;
  padding: 8px 10px;
  border: 1px solid var(--slot-border);
  border-radius: 8px;
  background: var(--slot-bg);
  text-align: left;
  cursor: pointer;
  font: inherit;
  color: var(--bp-text);
}

.bed-slot:hover {
  box-shadow: 0 2px 8px rgba(31, 45, 40, 0.12);
}

.bed-slot--occupied { --slot-bg: #f0f8f4; --slot-border: #cfe6db; }
.bed-slot--idle { --slot-bg: #f6f7f7; --slot-border: #e3e6e5; }
.bed-slot--reserved { --slot-bg: #fff8e8; --slot-border: #f5dfaa; }
.bed-slot--cleaning { --slot-bg: #eef4ff; --slot-border: #c9dafa; }
.bed-slot--maintenance { --slot-bg: #fff1f0; --slot-border: #f6c6c3; }
.bed-slot--locked { --slot-bg: #f5f0ff; --slot-border: #dccff9; }

.bed-slot__top {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}

.bed-slot__top :deep(.ant-tag) {
  margin: 0;
}

.bed-slot__resident {
  display: flex;
  align-items: center;
  gap: 8px;
}

.bed-slot__resident > span:last-child {
  display: flex;
  flex-direction: column;
}

.bed-slot__resident small {
  color: var(--bp-muted);
  font-size: 12px;
}

.bed-slot__avatar {
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  flex: none;
  border-radius: 50%;
  background: #dbeaf7;
  color: #2f6fd6;
  font-weight: 600;
}

.bed-slot__avatar--female {
  background: #fbe4ec;
  color: #c41d7f;
}

.bed-slot__risks {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.bed-slot__risk {
  color: #cf1322;
  font-size: 12px;
}

.bed-slot__empty {
  color: var(--bp-muted);
  font-size: 12px;
}

.bed-side {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.bed-legend,
.bed-side__row {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 6px 8px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  text-align: left;
  cursor: pointer;
  font: inherit;
  font-size: 13px;
  color: var(--bp-text);
}

.bed-legend:hover,
.bed-side__row:hover,
.bed-legend.is-active {
  background: var(--bp-green-soft);
}

.bed-legend__count {
  margin-left: auto;
  color: var(--bp-muted);
}

.bed-side__row {
  justify-content: space-between;
}

.bed-side__row > span:last-child {
  color: var(--bp-muted);
}

.bed-legend__dot {
  width: 12px;
  height: 12px;
  border-radius: 3px;
}

.bed-legend__dot--occupied { background: #52a37e; }
.bed-legend__dot--idle { background: #b8c2be; }
.bed-legend__dot--reserved { background: #f0b429; }
.bed-legend__dot--cleaning { background: #4c8df6; }
.bed-legend__dot--maintenance { background: #f5574a; }
.bed-legend__dot--locked { background: #8b6cf0; }

.bed-side__bullet {
  display: inline-block;
  width: 8px;
  height: 8px;
  margin-right: 8px;
  border-radius: 50%;
}

.bed-side__bullet--red { background: #f5574a; }
.bed-side__bullet--orange { background: #fa8c16; }
.bed-side__bullet--gold { background: #f0b429; }

.bed-drawer__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
}

.bed-slot__staff,
.bed-side__muted {
  color: var(--bp-muted);
  font-size: 12px;
}

.bed-side__muted {
  margin: 0;
  padding: 6px 8px;
}

.bed-today {
  display: grid;
  grid-template-columns: 180px repeat(4, minmax(0, 1fr));
  gap: 12px;
  align-items: center;
  margin-top: 16px;
}

.bed-today__title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 22px;
  color: var(--bp-green);
}

.bed-today__title span {
  display: flex;
  flex-direction: column;
}

.bed-today__title strong {
  font-size: 17px;
  color: var(--bp-text);
}

.bed-today__title small {
  font-size: 12px;
  color: var(--bp-muted);
}

.bed-today__item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid var(--bp-border);
  border-radius: 10px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  font: inherit;
  color: var(--bp-muted);
}

.bed-today__item:hover {
  border-color: var(--bp-green);
}

.bed-today__icon {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  flex: none;
  border-radius: 50%;
  font-size: 20px;
  background: var(--act-soft);
  color: var(--act-color);
}

.bed-today__body {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-width: 0;
  color: var(--bp-text);
}

.bed-today__body strong {
  font-size: 20px;
}

.bed-today__body small {
  color: var(--bp-muted);
  font-size: 12px;
}

.bed-today__item--admit { --act-color: #21705f; --act-soft: #e8f3ef; }
.bed-today__item--discharge { --act-color: #2f6fd6; --act-soft: #e8f0fc; }
.bed-today__item--transfer { --act-color: #7a5af5; --act-soft: #f1edff; }
.bed-today__item--away { --act-color: #d48806; --act-soft: #fff5e1; }

@media (max-width: 1280px) {
  .bed-layout {
    grid-template-columns: 220px minmax(0, 1fr);
  }

  .bed-today {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .bed-today__title {
    grid-column: 1 / -1;
  }

  .bed-side {
    grid-column: 1 / -1;
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  }
}

@media (max-width: 900px) {
  .bed-layout,
  .bed-today {
    grid-template-columns: minmax(0, 1fr);
  }
}

.bed-slot.is-focused {
  outline: 3px solid #faad14;
  outline-offset: 2px;
}

.bed-records__filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  margin-bottom: 12px;
}

.bed-records__status { width: 130px; }
.bed-records__elder,
.bed-records__keyword { width: 220px; }
.bed-records__delete { color: #cf1322; }

.bed-records__links {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 16px;
  margin-top: 12px;
  color: var(--bp-muted);
  font-size: 13px;
}
</style>
