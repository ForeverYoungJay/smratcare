<template>
  <PageContainer title="入院办理" subTitle="办理入院与合同信息">
    <a-card class="card-elevated" :bordered="false">
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-form-item label="老人" name="elderId">
          <a-select v-model:value="form.elderId" placeholder="请选择老人">
            <a-select-option v-for="item in elders" :key="item.id" :value="item.id">{{ item.fullName }}</a-select-option>
          </a-select>
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
        <a-form-item label="备注" name="remark">
          <a-input v-model:value="form.remark" />
        </a-form-item>
      </a-form>
      <div style="text-align: right;">
        <a-button type="primary" :loading="submitting" @click="submit">提交入院</a-button>
      </div>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getElderPage } from '../../api/elder'
import { getBedList, getBuildingList, getFloorList, getRoomList } from '../../api/bed'
import { admitElder } from '../../api/elderLifecycle'
import type { AdmissionRequest, BedItem, BuildingItem, ElderItem, FloorItem, PageResult, RoomItem } from '../../types'

const formRef = ref<FormInstance>()
const form = reactive<AdmissionRequest>({ elderId: 0, admissionDate: '', bedId: undefined, bedStartDate: undefined })
const submitting = ref(false)
const elders = ref<ElderItem[]>([])
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
  elderId: [{ required: true, message: '请选择老人' }],
  admissionDate: [{ required: true, message: '请选择入院日期' }]
}

async function loadElders() {
  const res: PageResult<ElderItem> = await getElderPage({ pageNo: 1, pageSize: 200 })
  elders.value = res.list
}

async function submit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true
    if (form.bedId && !form.bedStartDate) {
      form.bedStartDate = form.admissionDate || new Date().toISOString().slice(0, 10)
    }
    await admitElder(form)
    message.success('入院办理成功')
  } catch {
    message.error('提交失败')
  } finally {
    submitting.value = false
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

onMounted(() => {
  loadElders()
  loadAssets()
})
</script>
