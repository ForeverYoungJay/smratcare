<template>
  <PageContainer title="新建老人" subTitle="补录平台启用前已入住长者，保留历史合同与资料，不走营销流程">
    <a-card class="card-elevated" :bordered="false">
      <a-alert
        type="info"
        show-icon
        style="margin-bottom: 16px"
        message="新建老人用于补录平台启用前已入住长者，可直接上传历史资料并办理入住，不依赖营销线索或 CRM 合同流程。"
      />
      <a-alert
        v-if="duplicateWarnings.length"
        type="warning"
        show-icon
        style="margin-bottom: 16px"
        message="发现可能重复的老人档案"
        :description="duplicateWarningText"
      />
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical" style="max-width: 920px">
        <a-form-item label="姓名" name="fullName">
          <a-input v-model:value="form.fullName" @blur="refreshDuplicateWarnings" />
        </a-form-item>
        <a-form-item label="身份证号" name="idCardNo">
          <a-input v-model:value="form.idCardNo" @blur="refreshDuplicateWarnings" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :xs="24" :md="12">
            <a-form-item label="身份证识别">
              <a-input :value="formDerivedBirthDate ? '已识别' : '未识别'" readonly />
            </a-form-item>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-form-item label="识别生日">
              <a-input :value="formDerivedBirthDate || '-'" readonly />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="form.phone" @blur="refreshDuplicateWarnings" />
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
          <a-date-picker v-model:value="form.birthDate" value-format="YYYY-MM-DD" style="width: 100%" @change="refreshDuplicateWarnings" />
        </a-form-item>
        <a-form-item label="入院日期" name="admissionDate">
          <a-date-picker v-model:value="form.admissionDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="历史合同号" name="contractNo">
          <a-input
            v-model:value="form.contractNo"
            placeholder="请输入平台启用前已签署的历史/线下合同号"
          />
        </a-form-item>
        <a-form-item label="历史合同附件">
          <a-space>
            <a-upload :show-upload-list="false" :before-upload="beforeUploadHistoricalContract">
              <a-button :loading="uploading.historicalContract">上传文件</a-button>
            </a-upload>
            <a-typography-text type="secondary">{{ fileLabel(form.historicalContractFileUrl, '未上传') }}</a-typography-text>
            <a-button v-if="form.historicalContractFileUrl" type="link" @click="openFile(form.historicalContractFileUrl)">查看</a-button>
          </a-space>
        </a-form-item>
        <a-form-item label="初始余额(押金)" name="depositAmount">
          <a-input-number v-model:value="form.depositAmount" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="护理等级" name="careLevel">
          <a-input v-model:value="form.careLevel" placeholder="如：一级护理/二级护理" />
        </a-form-item>
        <a-form-item label="风险预担" name="riskPrecommit">
          <a-select v-model:value="form.riskPrecommit" placeholder="请选择突发风险处置优先策略" allow-clear>
            <a-select-option value="RESCUE_FIRST">第一时间抢救</a-select-option>
            <a-select-option value="NOTIFY_FAMILY_FIRST">第一时间通知家属</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="基础疾病信息">
          <a-select
            v-model:value="selectedDiseaseIds"
            mode="multiple"
            allow-clear
            show-search
            :options="diseaseOptions"
            placeholder="请选择基础疾病"
          />
        </a-form-item>
        <div style="margin: 8px 0 16px; display: flex; justify-content: space-between; align-items: center;">
          <div>
            <div style="font-weight: 600;">家属信息</div>
            <div style="color: rgba(0, 0, 0, 0.45); font-size: 12px;">常用联系人集中录入，手机号与身份证号要求完整。</div>
          </div>
          <a-button size="small" type="dashed" @click="addFamilyDraftRow">新增家属</a-button>
        </div>
        <div class="family-editor-list">
          <div v-for="item in familyDraftRows" :key="item.key" class="family-card family-card-editable">
            <div class="family-card-toolbar">
              <a-tag :color="item.isPrimary ? 'green' : 'default'">{{ item.isPrimary ? '主联系人' : '普通联系人' }}</a-tag>
              <a-button type="link" danger @click="removeFamilyDraftRow(item.key)">删除</a-button>
            </div>
            <a-row :gutter="[12, 4]">
              <a-col :xs="24" :md="10">
                <a-form-item label="姓名" class="compact-form-item">
                  <a-input v-model:value="item.realName" placeholder="姓名" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="8">
                <a-form-item label="关系" class="compact-form-item">
                  <a-select
                    v-model:value="item.relation"
                    :options="familyRelationOptions"
                    allow-clear
                    placeholder="请选择关系"
                  />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="6">
                <a-form-item label="主联系人" class="compact-form-item">
                  <a-switch v-model:checked="item.isPrimary" @change="() => setPrimaryFamilyDraftRow(item.key)" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="手机号" class="compact-form-item">
                  <a-input v-model:value="item.phone" placeholder="手机号" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="身份证号" class="compact-form-item">
                  <a-input v-model:value="item.idCardNo" placeholder="身份证号" />
                </a-form-item>
              </a-col>
            </a-row>
          </div>
          <a-empty v-if="!familyDraftRows.length" :image="null" description="暂无家属信息，可直接新增录入" />
        </div>
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
          <a-select v-model:value="form.bedId" allow-clear placeholder="选择床位" :disabled="form.status !== 1">
            <a-select-option v-for="item in bedOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="床位开始日期" name="bedStartDate">
          <a-date-picker v-model:value="form.bedStartDate" value-format="YYYY-MM-DD" style="width: 100%" :disabled="form.status !== 1" />
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
import dayjs from 'dayjs'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { bindFamily, createElder, getElderPage, updateElderDiseases, uploadElderFile } from '../../api/elder'
import { admitElder } from '../../api/elderLifecycle'
import { upsertFamilyUser } from '../../api/family'
import { getBedList, getBuildingList, getFloorList, getRoomList } from '../../api/bed'
import { getDiseaseList } from '../../api/store'
import type { AdmissionRequest, BedItem, BuildingItem, ElderCreateRequest, ElderItem, FloorItem, Id, RoomItem } from '../../types'

const router = useRouter()
const formRef = ref<FormInstance>()
const saving = ref(false)
const formDerivedBirthDate = ref('')

const form = reactive<ElderCreateRequest & { contractNo?: string; depositAmount?: number }>({
  fullName: '',
  idCardNo: '',
  phone: '',
  homeAddress: '',
  medicalInsuranceCopyUrl: '',
  householdCopyUrl: '',
  medicalRecordFileUrl: '',
  historicalContractFileUrl: '',
  sourceType: 'HISTORICAL_IMPORT',
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
  medicalRecord: false,
  historicalContract: false
})
const duplicateWarnings = ref<Array<{ elderId: string; reasons: string[]; label: string }>>([])
const selectedDiseaseIds = ref<number[]>([])
const diseaseOptions = ref<Array<{ label: string; value: number }>>([])
const familyDraftRows = ref<Array<{
  key: string
  familyUserId?: string
  realName: string
  relation: string
  phone: string
  idCardNo: string
  isPrimary: boolean
}>>([])
const familyRelationOptions = [
  { label: '配偶', value: '配偶' },
  { label: '子女', value: '子女' },
  { label: '孙辈', value: '孙辈' },
  { label: '兄弟姐妹', value: '兄弟姐妹' },
  { label: '亲属', value: '亲属' },
  { label: '监护人', value: '监护人' },
  { label: '朋友', value: '朋友' },
  { label: '其他', value: '其他' }
]

const buildings = ref<BuildingItem[]>([])
const floors = ref<FloorItem[]>([])
const rooms = ref<RoomItem[]>([])
const beds = ref<BedItem[]>([])

const assetSelect = reactive({
  buildingId: undefined as Id | undefined,
  floorId: undefined as Id | undefined,
  roomId: undefined as Id | undefined
})

function trimText(value?: string) {
  return String(value || '').trim()
}

function resolveBirthDateAndAgeFromIdCard(idCardNo?: string) {
  const normalized = trimText(idCardNo)
  const now = dayjs()
  const toValidBirthDate = (value: string) => {
    const d = dayjs(value)
    if (!d.isValid()) return null
    if (d.format('YYYY-MM-DD') !== value) return null
    if (d.isAfter(now)) return null
    return d
  }
  if (/^\d{17}[\dXx]$/.test(normalized)) {
    const birthDate = toValidBirthDate(`${normalized.slice(6, 10)}-${normalized.slice(10, 12)}-${normalized.slice(12, 14)}`)
    return birthDate ? birthDate.format('YYYY-MM-DD') : ''
  }
  if (/^\d{15}$/.test(normalized)) {
    const birthDate = toValidBirthDate(`19${normalized.slice(6, 8)}-${normalized.slice(8, 10)}-${normalized.slice(10, 12)}`)
    return birthDate ? birthDate.format('YYYY-MM-DD') : ''
  }
  return ''
}

async function validateIdCardNo(_rule: unknown, value?: string) {
  const text = trimText(value)
  if (!text) {
    return Promise.resolve()
  }
  if (!/^(?:\d{15}|\d{17}[\dXx])$/.test(text)) {
    return Promise.reject(new Error('身份证号格式不正确'))
  }
  return Promise.resolve()
}

async function validatePhone(_rule: unknown, value?: string) {
  const text = trimText(value)
  if (!text) {
    return Promise.resolve()
  }
  if (!/^1\d{10}$/.test(text)) {
    return Promise.reject(new Error('手机号格式不正确'))
  }
  return Promise.resolve()
}

async function validateBirthDate(_rule: unknown, value?: string) {
  if (!value) {
    return Promise.resolve()
  }
  if (dayjs(value).isAfter(dayjs(), 'day')) {
    return Promise.reject(new Error('出生日期不能晚于今天'))
  }
  return Promise.resolve()
}

async function validateAdmissionDate(_rule: unknown, value?: string) {
  if (!value) {
    return Promise.reject(new Error('请填写入院日期'))
  }
  if (value && form.birthDate && dayjs(value).isBefore(dayjs(form.birthDate), 'day')) {
    return Promise.reject(new Error('入院日期不能早于出生日期'))
  }
  return Promise.resolve()
}

async function validateContractNo(_rule: unknown, value?: string) {
  const text = trimText(value)
  if (!text) {
    return Promise.reject(new Error('请填写历史合同号'))
  }
  return Promise.resolve()
}

async function validateBedId(_rule: unknown, value?: Id) {
  if (!value) {
    return Promise.resolve()
  }
  if (form.status !== 1) {
    return Promise.reject(new Error('仅在院状态可分配床位'))
  }
  return Promise.resolve()
}

async function validateBedStartDate(_rule: unknown, value?: string) {
  if (!form.bedId) {
    return Promise.resolve()
  }
  if (!value) {
    return Promise.reject(new Error('选择床位时必须填写床位开始日期'))
  }
  if (form.admissionDate && dayjs(value).isBefore(dayjs(form.admissionDate), 'day')) {
    return Promise.reject(new Error('床位开始日期不能早于入院日期'))
  }
  return Promise.resolve()
}

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
const duplicateWarningText = computed(() =>
  duplicateWarnings.value
    .map((item) => `${item.label}（命中：${item.reasons.join('、')}）`)
    .join('；')
)

const rules: FormRules = {
  fullName: [{ required: true, message: '请输入姓名' }],
  idCardNo: [{ validator: validateIdCardNo, trigger: 'blur' }],
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  birthDate: [{ validator: validateBirthDate, trigger: 'change' }],
  admissionDate: [{ validator: validateAdmissionDate, trigger: 'change' }],
  contractNo: [{ validator: validateContractNo, trigger: 'blur' }],
  bedId: [{ validator: validateBedId, trigger: 'change' }],
  bedStartDate: [{ validator: validateBedStartDate, trigger: 'change' }],
  status: [{ required: true, message: '请选择状态' }]
}

function back() {
  router.push('/elder/list')
}

async function submit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    validateFamilyDraftRows()
    await refreshDuplicateWarnings()
    saving.value = true
    const normalizedContractNo = String(form.contractNo || '').trim()
    const shouldAdmit = !!form.admissionDate && !!normalizedContractNo
    let successMessage = '保存成功'
    const payload: ElderCreateRequest = {
      fullName: trimText(form.fullName),
      idCardNo: trimText(form.idCardNo) || undefined,
      phone: trimText(form.phone) || undefined,
      homeAddress: trimText(form.homeAddress) || undefined,
      medicalInsuranceCopyUrl: form.medicalInsuranceCopyUrl,
      householdCopyUrl: form.householdCopyUrl,
      medicalRecordFileUrl: form.medicalRecordFileUrl,
      historicalContractFileUrl: form.historicalContractFileUrl,
      sourceType: form.sourceType,
      gender: form.gender,
      birthDate: form.birthDate,
      admissionDate: form.admissionDate,
      careLevel: form.careLevel,
      riskPrecommit: form.riskPrecommit,
      status: form.status,
      remark: trimText(form.remark) || undefined
    }
    payload.bedId = form.bedId
    payload.bedStartDate = form.bedStartDate
    const created: ElderItem = await createElder(payload)
    if (created?.id && selectedDiseaseIds.value.length > 0) {
      await updateElderDiseases(created.id, selectedDiseaseIds.value)
    }
    if (created?.id && familyDraftRows.value.length > 0) {
      for (let i = 0; i < familyDraftRows.value.length; i += 1) {
        const familyRow = familyDraftRows.value[i]
        const user = await upsertFamilyUser({
          realName: trimText(familyRow.realName),
          phone: trimText(familyRow.phone),
          idCardNo: trimText(familyRow.idCardNo),
          status: 1
        })
        const familyUserId = String(user?.id || familyRow.familyUserId || '').trim()
        if (!familyUserId) continue
        await bindFamily({
          elderId: created.id,
          familyUserId,
          relation: trimText(familyRow.relation) || '家属',
          isPrimary: familyRow.isPrimary ? 1 : 0
        })
      }
    }
    if (shouldAdmit && created?.id) {
      const admission: AdmissionRequest = {
        elderId: created.id,
        admissionDate: form.admissionDate || new Date().toISOString().slice(0, 10),
        contractNo: normalizedContractNo || undefined,
        depositAmount: form.depositAmount,
        bedId: form.bedId,
        bedStartDate: form.bedStartDate,
        allowMissingContractRecord: true
      }
      try {
        await admitElder(admission)
        successMessage = '已完成历史入住补录'
      } catch (admitError: any) {
        message.warning(
          admitError?.message
            || '老人档案已创建，但自动办理入住失败，请到“入住办理”继续处理'
        )
      }
    }
    message.success(successMessage)
    router.push('/elder/list')
  } catch (error: any) {
    message.error(error?.message || error?.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function uploadArchiveFile(
  field: 'medicalInsuranceCopyUrl' | 'householdCopyUrl' | 'medicalRecordFileUrl' | 'historicalContractFileUrl',
  loadingKey: 'medicalInsurance' | 'household' | 'medicalRecord' | 'historicalContract',
  file: File
) {
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

function beforeUploadHistoricalContract(file: File) {
  return uploadArchiveFile('historicalContractFileUrl', 'historicalContract', file)
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

function addFamilyDraftRow() {
  familyDraftRows.value.push({
    key: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
    realName: '',
    relation: '',
    phone: '',
    idCardNo: '',
    isPrimary: familyDraftRows.value.length === 0
  })
}

function removeFamilyDraftRow(key: string) {
  familyDraftRows.value = familyDraftRows.value.filter((item) => item.key !== key)
  if (!familyDraftRows.value.some((item) => item.isPrimary) && familyDraftRows.value.length) {
    familyDraftRows.value[0].isPrimary = true
  }
}

function setPrimaryFamilyDraftRow(key: string) {
  familyDraftRows.value.forEach((item) => {
    item.isPrimary = item.key === key
  })
}

function validateFamilyDraftRows() {
  if (familyDraftRows.value.length === 0) return
  const invalidRow = familyDraftRows.value.find((item) =>
    !trimText(item.realName) || !trimText(item.phone) || !trimText(item.idCardNo))
  if (invalidRow) {
    throw new Error('家属信息需填写姓名、手机号和身份证号')
  }
  const invalidPhoneRow = familyDraftRows.value.find((item) => !/^1\d{10}$/.test(trimText(item.phone)))
  if (invalidPhoneRow) {
    throw new Error(`家属手机号格式不正确：${trimText(invalidPhoneRow.realName) || '未命名家属'}`)
  }
  const invalidIdCardRow = familyDraftRows.value.find((item) => !/^(\d{15}|\d{17}[\dXx])$/.test(trimText(item.idCardNo)))
  if (invalidIdCardRow) {
    throw new Error(`家属身份证号格式不正确：${trimText(invalidIdCardRow.realName) || '未命名家属'}`)
  }
}

async function refreshDuplicateWarnings() {
  const idCardNo = trimText(form.idCardNo)
  const phone = trimText(form.phone)
  const fullName = trimText(form.fullName)
  const birthDate = trimText(form.birthDate)
  if (!idCardNo && !phone && !(fullName && birthDate)) {
    duplicateWarnings.value = []
    return
  }
  try {
    const [idCardPage, phonePage, fullNamePage] = await Promise.all([
      idCardNo ? getElderPage({ pageNo: 1, pageSize: 10, idCardNo }) : Promise.resolve({ list: [] as ElderItem[] }),
      phone ? getElderPage({ pageNo: 1, pageSize: 20, keyword: phone }) : Promise.resolve({ list: [] as ElderItem[] }),
      fullName ? getElderPage({ pageNo: 1, pageSize: 20, fullName }) : Promise.resolve({ list: [] as ElderItem[] })
    ])
    const warningMap = new Map<string, { elderId: string; label: string; reasons: string[] }>()
    const collect = (rows: ElderItem[], reason: string, matcher: (row: ElderItem) => boolean) => {
      rows.forEach((row) => {
        const elderId = String(row.id || '').trim()
        if (!elderId || !matcher(row)) return
        const current = warningMap.get(elderId) || {
          elderId,
          label: `${row.fullName || '未命名'}${row.elderCode ? `（${row.elderCode}）` : ''}`,
          reasons: []
        }
        if (!current.reasons.includes(reason)) current.reasons.push(reason)
        warningMap.set(elderId, current)
      })
    }
    collect(idCardPage.list || [], '身份证号相同', (row) => trimText(row.idCardNo) === idCardNo)
    collect(phonePage.list || [], '手机号相同', (row) => trimText(row.phone) === phone)
    collect(fullNamePage.list || [], '姓名和出生日期相同', (row) => trimText(row.fullName) === fullName && trimText(row.birthDate) === birthDate)
    duplicateWarnings.value = Array.from(warningMap.values()).slice(0, 5)
  } catch {
    duplicateWarnings.value = []
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

async function loadDiseaseOptions() {
  try {
    const list = await getDiseaseList()
    diseaseOptions.value = (list || []).map((item: any) => ({
      label: String(item.diseaseName || item.name || `疾病#${item.id}`),
      value: Number(item.id)
    }))
  } catch {
    diseaseOptions.value = []
  }
}

watch(
  () => form.idCardNo,
  (value) => {
    const parsedBirthDate = resolveBirthDateAndAgeFromIdCard(value)
    formDerivedBirthDate.value = parsedBirthDate
    if (parsedBirthDate && !form.birthDate) {
      form.birthDate = parsedBirthDate
    }
  },
  { immediate: true }
)
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
watch(
  () => form.bedId,
  () => {
    if (!form.bedId) {
      form.bedStartDate = undefined
    }
  }
)
watch(
  () => form.status,
  (status) => {
    if (status === 1) {
      return
    }
    assetSelect.buildingId = undefined
    assetSelect.floorId = undefined
    assetSelect.roomId = undefined
    form.bedId = undefined
    form.bedStartDate = undefined
  }
)

onMounted(() => {
  loadAssets()
  loadDiseaseOptions()
})
</script>

<style scoped>
.family-editor-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.family-card {
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  padding: 14px 16px 4px;
  background: #fafafa;
}

.family-card-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.compact-form-item {
  margin-bottom: 10px;
}
</style>
