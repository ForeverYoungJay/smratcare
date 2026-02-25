<template>
  <PageContainer title="新增老人" subTitle="录入老人档案">
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
        <a-form-item label="合同号" name="contractNo">
          <a-input v-model:value="form.contractNo" />
        </a-form-item>
        <a-form-item label="初始余额(押金)" name="depositAmount">
          <a-input-number v-model:value="form.depositAmount" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="护理等级" name="careLevel">
          <a-select v-model:value="form.careLevel" placeholder="请选择护理等级">
            <a-select-option value="A">A</a-select-option>
            <a-select-option value="B">B</a-select-option>
            <a-select-option value="C">C</a-select-option>
            <a-select-option value="D">D</a-select-option>
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
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { createElder } from '../../api/elder'
import { admitElder } from '../../api/elderLifecycle'
import { getBedList, getBuildingList, getFloorList, getRoomList } from '../../api/bed'
import type { AdmissionRequest, BedItem, BuildingItem, ElderCreateRequest, ElderItem, FloorItem, RoomItem } from '../../types/api'

const router = useRouter()
const formRef = ref<FormInstance>()
const saving = ref(false)

const form = reactive<ElderCreateRequest & { contractNo?: string; depositAmount?: number }>({
  fullName: '',
  idCardNo: '',
  phone: '',
  homeAddress: '',
  gender: undefined,
  birthDate: undefined,
  admissionDate: undefined,
  contractNo: '',
  depositAmount: undefined,
  careLevel: '',
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
    .filter((b) => !b.elderId && (b.status === 1 || b.status === undefined))
    .map((b) => ({ label: b.bedNo, value: b.id }))
)

const rules: FormRules = {
  fullName: [{ required: true, message: '请输入姓名' }],
  status: [{ required: true, message: '请选择状态' }]
}

function back() {
  router.push('/elder/list')
}

async function submit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    saving.value = true
    const shouldAdmit = !!form.admissionDate
    const payload: ElderCreateRequest = {
      fullName: form.fullName,
      idCardNo: form.idCardNo,
      phone: form.phone,
      homeAddress: form.homeAddress,
      gender: form.gender,
      birthDate: form.birthDate,
      admissionDate: form.admissionDate,
      careLevel: form.careLevel,
      status: form.status,
      remark: form.remark
    }
    payload.bedId = form.bedId
    payload.bedStartDate = form.bedStartDate
    if (payload.bedId && !payload.bedStartDate) {
      payload.bedStartDate = form.admissionDate || new Date().toISOString().slice(0, 10)
    }
    const created: ElderItem = await createElder(payload)
    if (shouldAdmit && created?.id) {
      const admission: AdmissionRequest = {
        elderId: created.id,
        admissionDate: form.admissionDate || new Date().toISOString().slice(0, 10),
        contractNo: form.contractNo || undefined,
        depositAmount: form.depositAmount,
        bedId: form.bedId,
        bedStartDate: form.bedStartDate
      }
      if (admission.bedId && !admission.bedStartDate) {
        admission.bedStartDate = admission.admissionDate
      }
      await admitElder(admission)
    }
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

onMounted(loadAssets)
</script>
