<template>
  <PageContainer :title="quickArchiveMode ? '档案一键生成' : '新增老人'" :subTitle="quickArchiveMode ? '上传现有资料并直接办理入住（跳过营销合同流程）' : '录入老人档案'">
    <a-card class="card-elevated" :bordered="false">
      <a-alert
        v-if="quickArchiveMode"
        type="info"
        show-icon
        style="margin-bottom: 16px"
        message="快捷建档模式：可直接上传医保/户口/病历资料并办理入住，不需要营销合同。"
      />
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
        <a-form-item label="医保复印件">
          <a-space>
            <a-upload :show-upload-list="false" :before-upload="beforeUploadMedicalInsurance">
              <a-button :loading="uploading.medicalInsurance">上传文件</a-button>
            </a-upload>
            <a-typography-text type="secondary">{{ fileLabel(form.medicalInsuranceCopyUrl, '未上传') }}</a-typography-text>
            <a-button v-if="form.medicalInsuranceCopyUrl" type="link" @click="openFile(form.medicalInsuranceCopyUrl)">查看</a-button>
          </a-space>
        </a-form-item>
        <a-form-item label="户口本复印件">
          <a-space>
            <a-upload :show-upload-list="false" :before-upload="beforeUploadHousehold">
              <a-button :loading="uploading.household">上传文件</a-button>
            </a-upload>
            <a-typography-text type="secondary">{{ fileLabel(form.householdCopyUrl, '未上传') }}</a-typography-text>
            <a-button v-if="form.householdCopyUrl" type="link" @click="openFile(form.householdCopyUrl)">查看</a-button>
          </a-space>
        </a-form-item>
        <a-form-item label="既往病历资料">
          <a-space>
            <a-upload :show-upload-list="false" :before-upload="beforeUploadMedicalRecord">
              <a-button :loading="uploading.medicalRecord">上传文件</a-button>
            </a-upload>
            <a-typography-text type="secondary">{{ fileLabel(form.medicalRecordFileUrl, '未上传') }}</a-typography-text>
            <a-button v-if="form.medicalRecordFileUrl" type="link" @click="openFile(form.medicalRecordFileUrl)">查看</a-button>
          </a-space>
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
        <a-form-item v-if="!quickArchiveMode" label="合同号" name="contractNo">
          <a-input v-model:value="form.contractNo" placeholder="营销链路下可填写，触发生命周期入住记录" />
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
import { createElder, uploadElderFile } from '../../api/elder'
import { admitElder } from '../../api/elderLifecycle'
import { getBedList, getBuildingList, getFloorList, getRoomList } from '../../api/bed'
import type { AdmissionRequest, BedItem, BuildingItem, ElderCreateRequest, ElderItem, FloorItem, RoomItem } from '../../types/api'

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const saving = ref(false)
const quickArchiveMode = computed(() => String(route.query.quickArchive || '') === '1')

const form = reactive<ElderCreateRequest & { contractNo?: string; depositAmount?: number }>({
  fullName: '',
  idCardNo: '',
  phone: '',
  homeAddress: '',
  medicalInsuranceCopyUrl: '',
  householdCopyUrl: '',
  medicalRecordFileUrl: '',
  gender: undefined,
  birthDate: undefined,
  admissionDate: undefined,
  contractNo: '',
  depositAmount: undefined,
  careLevel: '',
  riskPrecommit: undefined,
  status: 1,
  remark: '',
  bedId: undefined,
  bedStartDate: undefined
})
const uploading = reactive({
  medicalInsurance: false,
  household: false,
  medicalRecord: false
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
    const normalizedContractNo = String(form.contractNo || '').trim()
    const shouldAdmit = !quickArchiveMode.value && !!form.admissionDate && !!normalizedContractNo
    const payload: ElderCreateRequest = {
      fullName: form.fullName,
      idCardNo: form.idCardNo,
      phone: form.phone,
      homeAddress: form.homeAddress,
      medicalInsuranceCopyUrl: form.medicalInsuranceCopyUrl,
      householdCopyUrl: form.householdCopyUrl,
      medicalRecordFileUrl: form.medicalRecordFileUrl,
      gender: form.gender,
      birthDate: form.birthDate,
      admissionDate: form.admissionDate,
      careLevel: form.careLevel,
      riskPrecommit: form.riskPrecommit,
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
        contractNo: normalizedContractNo || undefined,
        depositAmount: form.depositAmount,
        bedId: form.bedId,
        bedStartDate: form.bedStartDate
      }
      if (admission.bedId && !admission.bedStartDate) {
        admission.bedStartDate = admission.admissionDate
      }
      try {
        await admitElder(admission)
      } catch (admitError: any) {
        message.warning(admitError?.message || '老人档案已创建，但自动办理入住失败，请到“入住办理”继续处理')
      }
    } else if (form.admissionDate && form.bedId) {
      message.success('已按直办入住完成建档与床位分配')
    }
    message.success('保存成功')
    router.push('/elder/list')
  } catch (error: any) {
    message.error(error?.message || error?.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function uploadArchiveFile(field: 'medicalInsuranceCopyUrl' | 'householdCopyUrl' | 'medicalRecordFileUrl', loadingKey: 'medicalInsurance' | 'household' | 'medicalRecord', file: File) {
  uploading[loadingKey] = true
  try {
    const uploaded = await uploadElderFile(file, `elder-${field}`)
    const url = uploaded?.fileUrl || ''
    if (!url) {
      message.error('上传失败：未返回文件地址')
      return false
    }
    form[field] = url
    message.success('上传成功')
  } catch (error: any) {
    message.error(error?.message || '上传失败')
  } finally {
    uploading[loadingKey] = false
  }
  return false
}

function beforeUploadMedicalInsurance(file: File) {
  return uploadArchiveFile('medicalInsuranceCopyUrl', 'medicalInsurance', file)
}

function beforeUploadHousehold(file: File) {
  return uploadArchiveFile('householdCopyUrl', 'household', file)
}

function beforeUploadMedicalRecord(file: File) {
  return uploadArchiveFile('medicalRecordFileUrl', 'medicalRecord', file)
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
