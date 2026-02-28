<template>
  <PageContainer title="编辑老人" subTitle="更新老人档案">
    <a-card class="card-elevated" :bordered="false">
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical" style="max-width: 760px">
        <a-form-item label="姓名" name="fullName">
          <a-input v-model:value="form.fullName" />
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
        <a-form-item label="护理等级" name="careLevel">
          <a-select v-model:value="form.careLevel" placeholder="请选择护理等级">
            <a-select-option value="A">A</a-select-option>
            <a-select-option value="B">B</a-select-option>
            <a-select-option value="C">C</a-select-option>
            <a-select-option value="D">D</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="风险预担" name="riskPrecommit">
          <a-select v-model:value="form.riskPrecommit" placeholder="请选择突发风险处置优先策略" allow-clear>
            <a-select-option value="RESCUE_FIRST">第一时间抢救</a-select-option>
            <a-select-option value="NOTIFY_FAMILY_FIRST">第一时间通知家属</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="楼栋" name="buildingId">
          <a-select v-model:value="assetSelect.buildingId" allow-clear placeholder="选择楼栋">
            <a-select-option v-for="item in buildingOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="楼层" name="floorId">
          <a-select v-model:value="assetSelect.floorId" allow-clear placeholder="选择楼层">
            <a-select-option v-for="item in floorOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="房间" name="roomId">
          <a-select v-model:value="assetSelect.roomId" allow-clear placeholder="选择房间">
            <a-select-option v-for="item in roomOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="床位" name="bedId">
          <a-select v-model:value="form.bedId" allow-clear placeholder="选择床位">
            <a-select-option v-for="item in bedOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
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
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getElderDetail, updateElder } from '../../api/elder'
import { getBedList, getBuildingList, getFloorList, getRoomList } from '../../api/bed'
import type { BedItem, BuildingItem, ElderItem, FloorItem, RoomItem } from '../../types/api'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const saving = ref(false)
const currentElderId = computed(() => Number(route.params.id))

const form = reactive<Partial<ElderItem>>({
  fullName: '',
  elderCode: '',
  idCardNo: '',
  phone: '',
  homeAddress: '',
  gender: undefined,
  birthDate: undefined,
  admissionDate: undefined,
  careLevel: '',
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
  buildingId: undefined as number | undefined,
  floorId: undefined as number | undefined,
  roomId: undefined as number | undefined
})

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
    .filter((b) => (!b.elderId && (b.status === 1 || b.status === undefined)) || b.elderId === currentElderId.value)
    .map((b) => ({ label: b.bedNo, value: b.id }))
)

const rules: FormRules = {
  fullName: [{ required: true, message: '请输入姓名' }],
  status: [{ required: true, message: '请选择状态' }]
}

function back() {
  router.push('/elder/list')
}

async function load() {
  const id = Number(route.params.id)
  if (!id) return
  try {
    const data = await getElderDetail(id)
    Object.assign(form, data)
  } catch {
    message.error('加载失败')
  }
}

async function submit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    saving.value = true
    if (form.bedId && !form.bedStartDate) {
      form.bedStartDate = form.admissionDate || new Date().toISOString().slice(0, 10)
    }
    await updateElder(Number(route.params.id), form)
    message.success('保存成功')
    router.push('/elder/list')
  } catch {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
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

function syncAssetSelectionByBed(bedId?: number) {
  if (!bedId) return
  const bed = beds.value.find((b) => b.id === bedId)
  if (!bed) return
  const room = rooms.value.find((r) => r.id === bed.roomId)
  if (!room) return
  assetSelect.roomId = room.id
  assetSelect.floorId = room.floorId
  assetSelect.buildingId = room.buildingId
}

watch(
  () => assetSelect.buildingId,
  () => {
    assetSelect.floorId = undefined
    assetSelect.roomId = undefined
    form.bedId = undefined
  }
)
watch(
  () => assetSelect.floorId,
  () => {
    assetSelect.roomId = undefined
    form.bedId = undefined
  }
)
watch(
  () => assetSelect.roomId,
  () => {
    form.bedId = undefined
  }
)

onMounted(async () => {
  await Promise.all([load(), loadAssets()])
  syncAssetSelectionByBed(form.bedId as number | undefined)
})
</script>
