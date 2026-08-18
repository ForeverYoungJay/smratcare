<template>
  <PageContainer :title="`房间详情 · ${room?.roomNo || ''}`" :sub-title="roomSubTitle">
    <a-card class="card-elevated" :bordered="false">
      <div class="room-head">
        <a-descriptions :column="4" size="small">
          <a-descriptions-item label="楼栋">{{ room?.building || '-' }}</a-descriptions-item>
          <a-descriptions-item label="楼层">{{ room?.floorNo || '-' }}</a-descriptions-item>
          <a-descriptions-item label="房型">{{ room?.roomType || '-' }}</a-descriptions-item>
          <a-descriptions-item label="朝向">{{ orientationText(room?.orientation) }}</a-descriptions-item>
          <a-descriptions-item label="床位数">{{ room?.capacity ?? '-' }}</a-descriptions-item>
          <a-descriptions-item label="启用状态">
            <a-tag :color="room?.status === 0 ? 'default' : 'green'">
              {{ room?.status === 0 ? '未启用' : '启用中' }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="在住人数">{{ residents.length }} 人</a-descriptions-item>
        </a-descriptions>
        <a-button type="primary" ghost @click="openRoomEdit">编辑房间信息</a-button>
      </div>
    </a-card>

    <a-row :gutter="16" style="margin-top: 16px;">
      <a-col :xs="24" :md="7" :xl="6">
        <a-card class="card-elevated" title="在住长者" :bordered="false">
          <a-spin :spinning="loading">
            <div v-if="residents.length" class="resident-picker">
              <button
                v-for="item in residents"
                :key="String(item.elderId)"
                type="button"
                class="resident-picker__item"
                :class="{ 'is-active': String(item.elderId) === String(activeElderId) }"
                @click="selectElder(item.elderId)"
              >
                <strong>{{ item.elderName }}</strong>
                <small>{{ item.bedNo || '未分配床位' }}{{ item.careLevel ? ' · ' + item.careLevel : '' }}</small>
                <span v-if="Number(item.outstandingAmount || 0) > 0" class="resident-picker__badge">
                  欠费 {{ Number(item.outstandingAmount).toFixed(2) }}
                </span>
              </button>
            </div>
            <a-empty v-else description="该房间当前没有在住长者" />
          </a-spin>
        </a-card>
      </a-col>

      <a-col :xs="24" :md="17" :xl="18">
        <a-card class="card-elevated" :bordered="false">
          <a-empty v-if="!activeElderId" description="请从左侧选择一位长者" />
          <a-tabs v-else v-model:activeKey="activeTab">
            <a-tab-pane key="admission" tab="入住档案">
              <a-spin :spinning="elderLoading">
                <a-descriptions bordered :column="2" size="small" title="基本信息">
                  <a-descriptions-item label="姓名">{{ elder?.fullName || '-' }}</a-descriptions-item>
                  <a-descriptions-item label="编号">{{ elder?.elderCode || '-' }}</a-descriptions-item>
                  <a-descriptions-item label="性别">{{ genderText(elder?.gender) }}</a-descriptions-item>
                  <a-descriptions-item label="出生日期">{{ elder?.birthDate || '-' }}</a-descriptions-item>
                  <a-descriptions-item label="入住日期">{{ elder?.admissionDate || '-' }}</a-descriptions-item>
                  <a-descriptions-item label="护理等级">{{ elder?.careLevel || '-' }}</a-descriptions-item>
                  <a-descriptions-item label="联系电话">{{ elder?.phone || '-' }}</a-descriptions-item>
                  <a-descriptions-item label="身份证">{{ elder?.idCardNo || '-' }}</a-descriptions-item>
                  <a-descriptions-item label="家庭住址" :span="2">{{ elder?.homeAddress || '-' }}</a-descriptions-item>
                </a-descriptions>

                <div class="block-title">家属</div>
                <vxe-table border stripe :data="families" max-height="220">
                  <vxe-column field="familyName" title="家属" width="140">
                    <template #default="{ row }">{{ row.familyName || row.fullName || row.name || '-' }}</template>
                  </vxe-column>
                  <vxe-column field="relation" title="关系" width="120" />
                  <vxe-column field="phone" title="联系电话" width="160" />
                  <vxe-column field="isPrimary" title="主联系人" width="110">
                    <template #default="{ row }">{{ row.isPrimary ? '是' : '否' }}</template>
                  </vxe-column>
                </vxe-table>
                <a-empty v-if="!families.length" description="暂无绑定家属" />

                <div class="block-title">缴费摘要</div>
                <a-row :gutter="[12, 12]">
                  <a-col :xs="12" :md="6">
                    <a-statistic title="本月应收" :value="Number(billSummary.total)" :precision="2" suffix="元" />
                  </a-col>
                  <a-col :xs="12" :md="6">
                    <a-statistic title="本月已收" :value="Number(billSummary.paid)" :precision="2" suffix="元" />
                  </a-col>
                  <a-col :xs="12" :md="6">
                    <a-statistic
                      title="本月欠费"
                      :value="Number(billSummary.outstanding)"
                      :precision="2"
                      suffix="元"
                      :value-style="Number(billSummary.outstanding) > 0 ? { color: '#cf1322' } : undefined"
                    />
                  </a-col>
                  <a-col :xs="12" :md="6">
                    <a-statistic
                      title="押金在押余额"
                      :value="Number(depositSummary.balance)"
                      :precision="2"
                      suffix="元"
                    />
                    <div class="stat-hint">应缴差额 {{ Number(depositSummary.shortfall).toFixed(2) }} 元</div>
                  </a-col>
                </a-row>
              </a-spin>
            </a-tab-pane>

            <a-tab-pane key="health" tab="身体情况">
              <a-spin :spinning="healthLoading">
                <a-descriptions bordered :column="2" size="small">
                  <a-descriptions-item label="护理等级">{{ elder?.careLevel || '-' }}</a-descriptions-item>
                  <a-descriptions-item label="血型">{{ healthArchive?.bloodType || '-' }}</a-descriptions-item>
                  <a-descriptions-item label="过敏史" :span="2">{{ healthArchive?.allergyHistory || '暂无记录' }}</a-descriptions-item>
                  <a-descriptions-item label="慢性病" :span="2">{{ healthArchive?.chronicDisease || '暂无记录' }}</a-descriptions-item>
                  <a-descriptions-item label="体检小结" :span="2">{{ healthArchive?.checkReportSummary || '暂无记录' }}</a-descriptions-item>
                  <a-descriptions-item label="近期就诊" :span="2">{{ healthArchive?.recentMedicalVisit || '暂无记录' }}</a-descriptions-item>
                  <a-descriptions-item label="康复记录" :span="2">{{ healthArchive?.rehabilitationRecord || '暂无记录' }}</a-descriptions-item>
                </a-descriptions>
                <div class="block-tip">
                  身体情况来自健康档案，需要修改请到
                  <a-button type="link" size="small" style="padding: 0" @click="router.push('/health/archive')">
                    健康档案
                  </a-button>
                  维护。
                </div>
              </a-spin>
            </a-tab-pane>

            <a-tab-pane key="files" tab="电子文件存档">
              <div class="files-toolbar">
                <a-radio-group v-model:value="fileCategory" button-style="solid" size="small" @change="loadArchives">
                  <a-radio-button value="">全部</a-radio-button>
                  <a-radio-button value="CONTRACT">合同</a-radio-button>
                  <a-radio-button value="MEDICAL">医疗</a-radio-button>
                  <a-radio-button value="CERTIFICATE">证件</a-radio-button>
                  <a-radio-button value="OTHER">其他</a-radio-button>
                </a-radio-group>
                <a-space>
                  <a-select v-model:value="uploadCategory" style="width: 120px" size="small">
                    <a-select-option value="CONTRACT">合同</a-select-option>
                    <a-select-option value="MEDICAL">医疗</a-select-option>
                    <a-select-option value="CERTIFICATE">证件</a-select-option>
                    <a-select-option value="OTHER">其他</a-select-option>
                  </a-select>
                  <a-upload :before-upload="beforeUpload" :show-upload-list="false">
                    <a-button type="primary" size="small" :loading="uploading">上传归档</a-button>
                  </a-upload>
                </a-space>
              </div>
              <vxe-table border stripe show-overflow="title" :loading="archiveLoading" :data="archives" max-height="380">
                <vxe-column field="category" title="分类" width="100">
                  <template #default="{ row }">
                    <a-tag :color="categoryColor(row.category)">{{ categoryText(row.category) }}</a-tag>
                  </template>
                </vxe-column>
                <vxe-column field="fileName" title="文件名" min-width="220" />
                <vxe-column field="uploadedAt" title="归档时间" width="170" />
                <vxe-column field="remark" title="备注" min-width="140" />
                <vxe-column title="操作" width="140" fixed="right">
                  <template #default="{ row }">
                    <a-space>
                      <a-button type="link" size="small" @click="openFile(row)">查看</a-button>
                      <a-button type="link" size="small" danger @click="removeArchive(row)">删除</a-button>
                    </a-space>
                  </template>
                </vxe-column>
              </vxe-table>
              <a-empty v-if="!archiveLoading && !archives.length" description="该分类下还没有归档文件" />
            </a-tab-pane>
          </a-tabs>
        </a-card>
      </a-col>
    </a-row>

    <a-modal
      v-model:open="roomEditOpen"
      title="编辑房间信息"
      :confirm-loading="roomSaving"
      @ok="submitRoomEdit"
    >
      <a-form layout="vertical" :model="roomForm">
        <a-form-item label="房型">
          <a-input v-model:value="roomForm.roomType" allow-clear placeholder="如：双人间" />
        </a-form-item>
        <a-form-item label="朝向">
          <a-select v-model:value="roomForm.orientation" allow-clear placeholder="选择朝向">
            <a-select-option value="SOUTH">南向</a-select-option>
            <a-select-option value="NORTH">北向</a-select-option>
            <a-select-option value="EAST">东向</a-select-option>
            <a-select-option value="WEST">西向</a-select-option>
          </a-select>
          <span class="stat-hint">朝向决定平面图里房间落在走廊哪一侧</span>
        </a-form-item>
        <a-form-item label="床位数">
          <a-input-number v-model:value="roomForm.capacity" :min="0" :precision="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="启用状态">
          <a-switch v-model:checked="roomForm.enabled" checked-children="启用" un-checked-children="停用" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import { getRoomList, updateRoom } from '../../api/bed'
import { getElderDetail, uploadElderFile } from '../../api/elder'
import { getFamilyRelations } from '../../api/family'
import { getBillPage } from '../../api/bill'
import { getFloorPlan, type FloorPlanResident, type FloorPlanRoom } from '../../api/floorPlan'
import {
  createElderFileArchive,
  deleteElderFileArchive,
  getElderFileArchives,
  type ElderFileArchiveItem
} from '../../api/elderFileArchive'
import { getDepositTransactions } from '../../api/deposit'
import request from '../../utils/request'
import type { Id, RoomItem } from '../../types'

const route = useRoute()
const router = useRouter()

const roomId = computed(() => String(route.query.roomId || ''))
const loading = ref(false)
const room = ref<RoomItem | null>(null)
const planRoom = ref<FloorPlanRoom | null>(null)
const residents = ref<FloorPlanResident[]>([])
const activeElderId = ref<Id | undefined>(undefined)
const activeTab = ref('admission')

const elderLoading = ref(false)
const elder = ref<any | null>(null)
const families = ref<any[]>([])
const billSummary = reactive({ total: 0, paid: 0, outstanding: 0 })
const depositSummary = reactive({ balance: 0, shortfall: 0 })

const healthLoading = ref(false)
const healthArchive = ref<any | null>(null)

const archiveLoading = ref(false)
const archives = ref<ElderFileArchiveItem[]>([])
const fileCategory = ref('')
const uploadCategory = ref('CONTRACT')
const uploading = ref(false)

const roomEditOpen = ref(false)
const roomSaving = ref(false)
const roomForm = reactive({
  roomType: '',
  orientation: undefined as string | undefined,
  capacity: 0,
  enabled: true
})

const roomSubTitle = computed(() =>
  planRoom.value
    ? `${planRoom.value.planStatusText} · 床位 ${planRoom.value.occupiedBeds}/${planRoom.value.totalBeds}`
    : '左侧选择在住长者，右侧查看入住档案、身体情况与电子文件'
)

function orientationText(orientation?: string) {
  switch (orientation) {
    case 'SOUTH':
      return '南向'
    case 'NORTH':
      return '北向'
    case 'EAST':
      return '东向'
    case 'WEST':
      return '西向'
    default:
      return '未标注'
  }
}

function genderText(gender?: number) {
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '-'
}

function categoryText(category: string) {
  switch (category) {
    case 'CONTRACT':
      return '合同'
    case 'MEDICAL':
      return '医疗'
    case 'CERTIFICATE':
      return '证件'
    default:
      return '其他'
  }
}

function categoryColor(category: string) {
  switch (category) {
    case 'CONTRACT':
      return 'blue'
    case 'MEDICAL':
      return 'red'
    case 'CERTIFICATE':
      return 'green'
    default:
      return 'default'
  }
}

async function loadRoom() {
  if (!roomId.value) return
  loading.value = true
  try {
    const [rooms, plan] = await Promise.all([
      getRoomList().catch(() => [] as RoomItem[]),
      getFloorPlan(dayjs().format('YYYY-MM')).catch(() => null)
    ])
    room.value = (rooms || []).find((item) => String(item.id) === roomId.value) || null
    const flatRooms = (plan?.buildings || [])
      .flatMap((building) => building.floors)
      .flatMap((floor) => [...floor.southRooms, ...floor.northRooms, ...floor.otherRooms])
    planRoom.value = flatRooms.find((item) => String(item.roomId) === roomId.value) || null
    residents.value = planRoom.value?.residents || []
    if (residents.value.length) {
      selectElder(residents.value[0].elderId)
    } else {
      activeElderId.value = undefined
    }
  } finally {
    loading.value = false
  }
}

function selectElder(elderId: Id) {
  activeElderId.value = elderId
  loadElder(elderId)
  loadHealth(elderId)
  loadArchives()
}

async function loadElder(elderId: Id) {
  elderLoading.value = true
  try {
    const [detail, relations, bills] = await Promise.all([
      getElderDetail(elderId).catch(() => null),
      getFamilyRelations(elderId).catch(() => []),
      getBillPage({ pageNo: 1, pageSize: 12, elderId, month: dayjs().format('YYYY-MM') }).catch(() => null)
    ])
    elder.value = detail
    families.value = Array.isArray(relations) ? relations : []
    const rows = bills?.list || []
    billSummary.total = rows.reduce((sum: number, item: any) => sum + Number(item.totalAmount || 0), 0)
    billSummary.paid = rows.reduce((sum: number, item: any) => sum + Number(item.paidAmount || 0), 0)
    billSummary.outstanding = rows.reduce((sum: number, item: any) => sum + Number(item.outstandingAmount || 0), 0)
    await loadDeposit(elderId)
  } finally {
    elderLoading.value = false
  }
}

async function loadDeposit(elderId: Id) {
  try {
    const account: any = await request.get(`/api/finance/deposit/${elderId}`)
    depositSummary.balance = Number(account?.balanceAmount || 0)
    depositSummary.shortfall = Number(account?.shortfallAmount || 0)
    // 顺手确认流水接口可用，避免详情页里出现「有余额但查不到流水」的错觉
    await getDepositTransactions(elderId).catch(() => [])
  } catch {
    depositSummary.balance = 0
    depositSummary.shortfall = 0
  }
}

async function loadHealth(elderId: Id) {
  healthLoading.value = true
  try {
    const res: any = await request.get('/api/health/archive/page', {
      params: { pageNo: 1, pageSize: 1, elderId }
    })
    const list = res?.records || res?.list || []
    healthArchive.value = list[0] || null
  } catch {
    healthArchive.value = null
  } finally {
    healthLoading.value = false
  }
}

async function loadArchives() {
  if (!activeElderId.value) return
  archiveLoading.value = true
  try {
    archives.value = await getElderFileArchives(activeElderId.value, fileCategory.value || undefined)
  } finally {
    archiveLoading.value = false
  }
}

async function beforeUpload(file: File) {
  if (!activeElderId.value) {
    message.warning('请先选择长者')
    return false
  }
  uploading.value = true
  try {
    const uploaded: any = await uploadElderFile(file, 'elder-file-archive')
    const fileUrl = uploaded?.url || uploaded?.fileUrl || uploaded?.path
    if (!fileUrl) {
      message.error('上传成功但未拿到文件地址，请重试')
      return false
    }
    await createElderFileArchive({
      elderId: activeElderId.value,
      category: uploadCategory.value,
      fileName: file.name,
      fileUrl,
      fileSize: file.size
    })
    message.success('已归档')
    await loadArchives()
  } finally {
    uploading.value = false
  }
  return false
}

function openFile(row: ElderFileArchiveItem) {
  if (!row.fileUrl) return
  window.open(row.fileUrl, '_blank', 'noopener')
}

function removeArchive(row: ElderFileArchiveItem) {
  Modal.confirm({
    title: `删除归档文件「${row.fileName}」？`,
    content: '仅移除归档记录，不影响已上传的文件本体。',
    okType: 'danger',
    onOk: async () => {
      await deleteElderFileArchive(row.id)
      message.success('已删除')
      await loadArchives()
    }
  })
}

function openRoomEdit() {
  roomForm.roomType = room.value?.roomType || ''
  roomForm.orientation = room.value?.orientation || undefined
  roomForm.capacity = Number(room.value?.capacity || 0)
  roomForm.enabled = room.value?.status !== 0
  roomEditOpen.value = true
}

async function submitRoomEdit() {
  if (!room.value || roomSaving.value) return
  roomSaving.value = true
  try {
    await updateRoom(room.value.id, {
      ...room.value,
      roomType: roomForm.roomType.trim() || undefined,
      orientation: roomForm.orientation,
      capacity: Number(roomForm.capacity || 0),
      status: roomForm.enabled ? 1 : 0
    })
    message.success('房间信息已更新')
    roomEditOpen.value = false
    await loadRoom()
  } finally {
    roomSaving.value = false
  }
}

watch(roomId, () => loadRoom())

onMounted(loadRoom)
</script>

<style scoped>
.room-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.resident-picker {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.resident-picker__item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--border-soft, #eee);
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
  text-align: left;
}

.resident-picker__item.is-active {
  border-color: #52c41a;
  background: rgba(82, 196, 26, 0.08);
}

.resident-picker__item small {
  color: var(--text-tertiary, #999);
  font-size: 12px;
}

.resident-picker__badge {
  margin-top: 2px;
  font-size: 12px;
  color: #cf1322;
}

.block-title {
  margin: 18px 0 10px;
  font-size: 14px;
  font-weight: 600;
}

.block-tip,
.stat-hint {
  margin-top: 8px;
  font-size: 12px;
  color: var(--text-tertiary, #999);
}

.files-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
}
</style>
