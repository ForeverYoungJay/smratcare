<template>
  <component :is="embedded ? 'div' : PageContainer" v-bind="embedded ? {} : { title: '编辑老人', subTitle: '更新老人档案' }">
    <a-card class="card-elevated" :bordered="false">
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical" style="max-width: 760px">
        <a-form-item label="姓名" name="fullName">
          <a-input v-model:value="form.fullName" />
        </a-form-item>
        <a-form-item label="老人编号" name="elderCode">
          <a-input v-model:value="form.elderCode" />
        </a-form-item>
        <a-form-item label="档案来源">
          <a-tag color="blue">{{ sourceTypeText(form.sourceType) }}</a-tag>
        </a-form-item>
        <a-form-item label="身份证号" name="idCardNo">
          <a-input v-model:value="form.idCardNo" />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="form.phone" />
        </a-form-item>
        <a-form-item label="家庭地址" name="homeAddress">
          <a-input v-model:value="form.homeAddress" />
        </a-form-item>
        <a-form-item label="性别" name="gender">
          <a-select v-model:value="form.gender" placeholder="请选择">
            <a-select-option :value="1">男</a-select-option>
            <a-select-option :value="2">女</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="出生日期" name="birthDate">
          <a-date-picker v-model:value="form.birthDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="入院日期" name="admissionDate">
          <a-date-picker v-model:value="form.admissionDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="历史合同附件">
          <a-space>
            <a-upload :show-upload-list="false" :before-upload="beforeUploadHistoricalContract">
              <a-button :loading="uploadingHistoricalContract">上传文件</a-button>
            </a-upload>
            <a-typography-text type="secondary">{{ fileLabel(form.historicalContractFileUrl, '未上传') }}</a-typography-text>
            <a-button v-if="form.historicalContractFileUrl" type="link" @click="openFile(form.historicalContractFileUrl)">查看</a-button>
          </a-space>
        </a-form-item>
        <a-form-item label="护理等级" name="careLevel">
          <a-select v-model:value="form.careLevel" :options="careLevelOptions" placeholder="请选择护理等级（护理部「护理等级」中维护）" />
        </a-form-item>
        <a-form-item label="责任护理员" name="responsibleStaffId">
          <a-select
            v-model:value="form.responsibleStaffId"
            allow-clear
            show-search
            :filter-option="false"
            :options="staffOptions"
            :loading="staffLoading"
            placeholder="输入姓名/工号搜索"
            @search="searchStaff"
            @focus="() => searchStaff('')"
          />
        </a-form-item>
        <a-form-item label="风险预担" name="riskPrecommit">
          <a-select v-model:value="form.riskPrecommit" placeholder="请选择突发风险处置优先策略" allow-clear>
            <a-select-option value="RESCUE_FIRST">第一时间抢救</a-select-option>
            <a-select-option value="NOTIFY_FAMILY_FIRST">第一时间通知家属</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="楼栋" name="buildingId">
          <a-select v-model:value="assetSelect.buildingId" allow-clear placeholder="选择楼栋" :disabled="form.status !== 1">
            <a-select-option v-for="item in buildingOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="楼层" name="floorId">
          <a-select v-model:value="assetSelect.floorId" allow-clear placeholder="选择楼层" :disabled="form.status !== 1">
            <a-select-option v-for="item in floorOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="房间" name="roomId">
          <a-select v-model:value="assetSelect.roomId" allow-clear placeholder="选择房间" :disabled="form.status !== 1">
            <a-select-option v-for="item in roomOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="床位" name="bedId">
          <a-space direction="vertical" style="width: 100%">
            <a-select v-model:value="form.bedId" allow-clear placeholder="选择床位" :disabled="form.status !== 1">
              <a-select-option v-for="item in bedOptions" :key="item.value" :value="item.value">
                {{ item.label }}
              </a-select-option>
            </a-select>
            <a-button v-if="hasAssignedBed" type="link" style="padding-left: 0" :disabled="form.status !== 1" @click="clearBedSelection">
              清空当前床位
            </a-button>
          </a-space>
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-select v-model:value="form.status" placeholder="请选择">
            <a-select-option :value="1">在院</a-select-option>
            <a-select-option :value="2">请假</a-select-option>
            <a-select-option :value="3">离院</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-textarea v-model:value="form.remark" :rows="3" />
        </a-form-item>
      </a-form>
      <a-space>
        <a-button @click="back">返回</a-button>
        <a-button type="primary" :loading="saving" @click="submit">保存</a-button>
      </a-space>
    </a-card>
  </component>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { useStaffOptions } from '../../composables/useStaffOptions'
import { getElderDetail, updateElder, uploadElderFile } from '../../api/elder'
import { getCareLevelNameOptions } from '../../api/nursing'
import { getBedList, getBuildingList, getFloorList, getRoomList } from '../../api/bed'
import type { BedItem, BuildingItem, ElderItem, FloorItem, RoomItem, Id } from '../../types/api'

// embedded：在长者列表的右侧抽屉里使用，elderId 由父组件传入，保存/取消改为通知父组件
const props = defineProps<{ embedded?: boolean; elderId?: Id }>()
const emit = defineEmits<{ saved: []; cancel: [] }>()
const route = useRoute()
const router = useRouter()
const { staffOptions, staffLoading, searchStaff, ensureSelectedStaff } = useStaffOptions()
const formRef = ref<FormInstance>()
const saving = ref(false)
const uploadingHistoricalContract = ref(false)
const currentElderId = computed(() => String(props.elderId ?? route.params.id ?? ''))
const originalBedId = ref<Id | undefined>(undefined)

// responsibleStaffId/Name 由后端同期补充到 ElderItem，类型未落地前用本地交叉类型
const form = reactive<Partial<ElderItem> & { responsibleStaffId?: Id; responsibleStaffName?: string }>({
  fullName: '',
  elderCode: '',
  idCardNo: '',
  phone: '',
  homeAddress: '',
  gender: undefined,
  birthDate: undefined,
  admissionDate: undefined,
  sourceType: undefined,
  historicalContractFileUrl: '',
  careLevel: '',
  responsibleStaffId: undefined,
  riskPrecommit: undefined,
  status: 1,
  remark: '',
  bedId: undefined,
  bedStartDate: undefined
})

const buildings = ref<BuildingItem[]>([])
const floors = ref<FloorItem[]>([])
const rooms = ref<RoomItem[]>([])
const beds = ref<BedItem[]>([])

const assetSelect = reactive({
  buildingId: undefined as Id | undefined,
  floorId: undefined as Id | undefined,
  roomId: undefined as Id | undefined
})
const syncingAssetSelection = ref(false)

const buildingOptions = computed(() =>
  buildings.value.map((b) => ({ label: b.name, value: b.id }))
)
const floorOptions = computed(() =>
  floors.value
    .filter((f) => (assetSelect.buildingId ? f.buildingId === assetSelect.buildingId : true))
    .map((f) => ({ label: f.floorNo, value: f.id }))
)
const roomOptions = computed(() =>
  rooms.value
    .filter((r) => (assetSelect.floorId ? r.floorId === assetSelect.floorId : true))
    .map((r) => ({ label: r.roomNo, value: r.id }))
)
const bedOptions = computed(() =>
  beds.value
    .filter((b) => (assetSelect.roomId ? b.roomId === assetSelect.roomId : true))
    .filter((b) => (!b.elderId && (b.status === 1 || b.status === undefined)) || String(b.elderId || '') === currentElderId.value)
    .map((b) => ({ label: b.bedNo, value: b.id }))
)
const hasAssignedBed = computed(() => Boolean(form.bedId || originalBedId.value))

const rules: FormRules = {
  fullName: [{ required: true, message: '请输入姓名' }],
  elderCode: [{ required: true, message: '请输入老人编号' }],
  status: [{ required: true, message: '请选择状态' }]
}

function sourceTypeText(value?: string) {
  if (value === 'HISTORICAL_IMPORT') return '历史补录'
  if (value === 'MARKETING_CONTRACT') return '合同流程'
  return '未标记'
}

function back() {
  if (props.embedded) emit('cancel')
  else router.push('/elder/list')
}

function clearBedSelection() {
  form.bedId = undefined
  form.bedStartDate = undefined
}

async function load() {
  const id = currentElderId.value
  if (!id) return
  try {
    const data = await getElderDetail(id)
    Object.assign(form, data)
    form.bedId = data.bedId ?? data.currentBed?.id
    originalBedId.value = form.bedId
    if (form.responsibleStaffId != null) {
      form.responsibleStaffId = String(form.responsibleStaffId)
      ensureSelectedStaff(form.responsibleStaffId, form.responsibleStaffName)
    }
  } catch {
    message.error('加载失败')
  }
}

async function submit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    if (saving.value) return
    saving.value = true
    const payload: typeof form & { clearBed?: boolean } = { ...form }
    if (payload.bedId && !payload.bedStartDate) {
      payload.bedStartDate = payload.admissionDate || new Date().toISOString().slice(0, 10)
    }
    if (!payload.bedId) {
      payload.bedStartDate = undefined
      if (originalBedId.value) {
        payload.clearBed = true
      }
    }
    await updateElder(currentElderId.value, payload)
    message.success('保存成功')
    if (props.embedded) emit('saved')
    else router.push('/elder/list')
  } catch (error: any) {
    message.error(error?.message || error?.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function beforeUploadHistoricalContract(file: File) {
  uploadingHistoricalContract.value = true
  try {
    const uploaded = await uploadElderFile(file, 'elder-historical-contract')
    const url = uploaded?.fileUrl || ''
    if (!url) {
      message.error('上传失败：未返回文件地址')
      return false
    }
    form.historicalContractFileUrl = url
    message.success('上传成功')
  } catch (error: any) {
    message.error(error?.message || '上传失败')
  } finally {
    uploadingHistoricalContract.value = false
  }
  return false
}

function fileLabel(url?: string, fallback = '') {
  if (!url) return fallback
  const text = String(url).split('/').pop() || url
  return decodeURIComponent(text)
}

function openFile(url?: string) {
  if (!url) return
  window.open(url, '_blank')
}

async function loadBeds() {
  try {
    beds.value = await getBedList()
  } catch {
    beds.value = []
  }
}

async function loadAssets() {
  try {
    const [buildingList, floorList, roomList, bedList] = await Promise.all([
      getBuildingList(),
      getFloorList(),
      getRoomList(),
      getBedList()
    ])
    buildings.value = buildingList
    floors.value = floorList
    rooms.value = roomList
    beds.value = bedList
  } catch {
    buildings.value = []
    floors.value = []
    rooms.value = []
    beds.value = []
  }
}

async function syncAssetSelectionByBed(bedId?: Id) {
  if (!bedId) return
  const bed = beds.value.find((b) => String(b.id || '') === String(bedId || ''))
  if (!bed) return
  const room = rooms.value.find((r) => String(r.id || '') === String(bed.roomId || ''))
  if (!room) return
  syncingAssetSelection.value = true
  assetSelect.roomId = room.id
  assetSelect.floorId = room.floorId
  assetSelect.buildingId = room.buildingId
  await nextTick()
  syncingAssetSelection.value = false
}

watch(
  () => assetSelect.buildingId,
  () => {
    if (syncingAssetSelection.value) return
    assetSelect.floorId = undefined
    assetSelect.roomId = undefined
    form.bedId = undefined
  }
)
watch(
  () => assetSelect.floorId,
  () => {
    if (syncingAssetSelection.value) return
    assetSelect.roomId = undefined
    form.bedId = undefined
  }
)
watch(
  () => assetSelect.roomId,
  () => {
    if (syncingAssetSelection.value) return
    form.bedId = undefined
  }
)

const careLevelOptions = ref<Array<{ value: string; label: string }>>([])

onMounted(async () => {
  await Promise.all([load(), loadAssets()])
  syncAssetSelectionByBed(form.bedId as Id | undefined)
  careLevelOptions.value = await getCareLevelNameOptions(form.careLevel || undefined)
})
</script>
