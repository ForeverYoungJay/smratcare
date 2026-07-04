<template>
  <PageContainer title="长护险待遇管理" subTitle="参保登记 · 待遇核定 · 护理服务记录 · 服务包">
    <template #extra>
      <a-space>
        <a-button @click="openInsured">参保登记</a-button>
        <a-button @click="openBenefit">待遇核定</a-button>
        <a-button type="primary" @click="openRecord">录入服务记录</a-button>
      </a-space>
    </template>

    <a-card title="护理服务包" size="small">
      <DataTable rowKey="id" :columns="pkgColumns" :data-source="packages" :loading="loading" :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'price'">{{ yuan(record.price) }} 元</template>
          <template v-else-if="column.key === 'fundCovered'">
            <a-tag :color="record.fundCovered ? 'green' : 'default'">{{ record.fundCovered ? '基金覆盖' : '自费' }}</a-tag>
          </template>
        </template>
      </DataTable>
    </a-card>

    <!-- 参保登记 -->
    <a-modal v-model:open="insuredOpen" title="参保登记" :confirm-loading="saving" @ok="submitInsured">
      <a-form layout="vertical">
        <a-form-item label="长者" required>
          <a-select v-model:value="insuredForm.elderId" show-search :filter-option="false" :options="elderOptions"
            placeholder="输入姓名搜索" @search="searchElders" @focus="() => !elderOptions.length && searchElders('')" />
        </a-form-item>
        <a-form-item label="长护险参保号" required>
          <a-input v-model:value="insuredForm.insuredNo" />
        </a-form-item>
        <a-form-item label="身份证号（脱敏存储）">
          <a-input v-model:value="insuredForm.idCard" placeholder="选填" />
        </a-form-item>
        <a-form-item label="参保城市编码">
          <a-input v-model:value="insuredForm.cityCode" placeholder="选填" />
        </a-form-item>
        <a-form-item label="起始日期">
          <a-date-picker v-model:value="insuredForm.startDate" style="width: 100%" value-format="YYYY-MM-DD" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 待遇核定 -->
    <a-modal v-model:open="benefitOpen" title="待遇核定" :confirm-loading="saving" @ok="submitBenefit" width="640px">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="参保记录ID" required>
              <a-input-number v-model:value="benefitForm.insuredId" style="width: 100%" placeholder="参保登记返回的ID" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="长者" required>
              <a-select v-model:value="benefitForm.elderId" show-search :filter-option="false" :options="elderOptions"
                placeholder="输入姓名搜索" @search="searchElders" @focus="() => !elderOptions.length && searchElders('')" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="失能等级">
              <a-select v-model:value="benefitForm.disabilityLevel" allow-clear>
                <a-select-option v-for="lv in [0,1,2,3,4,5]" :key="lv" :value="lv">{{ lv }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="待遇类型">
              <a-select v-model:value="benefitForm.benefitType" :options="benefitTypeOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="关联评估ID">
              <a-input-number v-model:value="benefitForm.assessmentId" style="width: 100%" placeholder="选填" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="日限额（元/天）" required>
              <a-input-number v-model:value="benefitForm.dailyQuotaYuan" :min="0" :precision="2" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="统筹支付比例（%）" required>
              <a-input-number v-model:value="benefitForm.payRatioPct" :min="0" :max="100" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="生效开始">
              <a-date-picker v-model:value="benefitForm.validStart" style="width: 100%" value-format="YYYY-MM-DD" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="生效结束">
              <a-date-picker v-model:value="benefitForm.validEnd" style="width: 100%" value-format="YYYY-MM-DD" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <!-- 服务记录 -->
    <a-modal v-model:open="recordOpen" title="录入护理服务记录" :confirm-loading="saving" @ok="submitRecord" width="600px">
      <a-form layout="vertical">
        <a-form-item label="长者" required>
          <a-select v-model:value="recordForm.elderId" show-search :filter-option="false" :options="elderOptions"
            placeholder="输入姓名搜索" @search="searchElders" @focus="() => !elderOptions.length && searchElders('')" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="服务日期" required>
              <a-date-picker v-model:value="recordForm.serviceDate" style="width: 100%" value-format="YYYY-MM-DD" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="服务包">
              <a-select v-model:value="recordForm.packageId" allow-clear
                :options="packages.map(p => ({ label: p.packageName, value: p.id }))" placeholder="选填" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="服务项目">
              <a-input v-model:value="recordForm.itemName" placeholder="如 清洁照护" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="费用（元）" required>
              <a-input-number v-model:value="recordForm.feeYuan" :min="0" :precision="2" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import DataTable from '../../components/DataTable.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import {
  getLtciPackages,
  registerLtciInsured,
  grantLtciBenefit,
  addLtciServiceRecord
} from '../../api/ltci'
import type { Id, LtciServicePackage } from '../../types'

const loading = ref(false)
const saving = ref(false)
const packages = ref<LtciServicePackage[]>([])
const { elderOptions, searchElders } = useElderOptions({ pageSize: 50 })

const pkgColumns = [
  { title: '编码', dataIndex: 'packageCode', key: 'packageCode', width: 140 },
  { title: '名称', dataIndex: 'packageName', key: 'packageName' },
  { title: '适用等级', dataIndex: 'levelScope', key: 'levelScope', width: 120 },
  { title: '价格', key: 'price', width: 120 },
  { title: '基金', key: 'fundCovered', width: 100 }
]
const benefitTypeOptions = [
  { label: '机构护理', value: 'INSTITUTION' },
  { label: '居家护理', value: 'HOME' },
  { label: '辅具', value: 'DEVICE' }
]

const insuredOpen = ref(false)
const benefitOpen = ref(false)
const recordOpen = ref(false)

const insuredForm = reactive({
  elderId: undefined as Id | undefined,
  insuredNo: '',
  idCard: '',
  cityCode: '',
  startDate: undefined as string | undefined
})
const benefitForm = reactive({
  insuredId: undefined as number | undefined,
  elderId: undefined as Id | undefined,
  disabilityLevel: undefined as number | undefined,
  benefitType: 'INSTITUTION',
  assessmentId: undefined as number | undefined,
  dailyQuotaYuan: undefined as number | undefined,
  payRatioPct: 70,
  validStart: undefined as string | undefined,
  validEnd: undefined as string | undefined
})
const recordForm = reactive({
  elderId: undefined as Id | undefined,
  serviceDate: dayjs().format('YYYY-MM-DD'),
  packageId: undefined as Id | undefined,
  itemName: '',
  feeYuan: undefined as number | undefined
})

function yuan(fen?: number) {
  return fen == null ? '0.00' : (Number(fen) / 100).toFixed(2)
}

async function loadPackages() {
  loading.value = true
  try {
    packages.value = (await getLtciPackages()) || []
  } finally {
    loading.value = false
  }
}

function openInsured() {
  insuredForm.elderId = undefined
  insuredForm.insuredNo = ''
  insuredForm.idCard = ''
  insuredForm.cityCode = ''
  insuredForm.startDate = undefined
  insuredOpen.value = true
}
async function submitInsured() {
  if (!insuredForm.elderId || !insuredForm.insuredNo) {
    message.error('请填写长者与参保号')
    return
  }
  saving.value = true
  try {
    const id = await registerLtciInsured({
      elderId: insuredForm.elderId,
      insuredNo: insuredForm.insuredNo,
      idCard: insuredForm.idCard || undefined,
      cityCode: insuredForm.cityCode || undefined,
      startDate: insuredForm.startDate
    })
    message.success(`参保登记成功，参保记录ID：${id}`)
    insuredOpen.value = false
  } finally {
    saving.value = false
  }
}

function openBenefit() {
  benefitForm.insuredId = undefined
  benefitForm.elderId = undefined
  benefitForm.disabilityLevel = undefined
  benefitForm.benefitType = 'INSTITUTION'
  benefitForm.assessmentId = undefined
  benefitForm.dailyQuotaYuan = undefined
  benefitForm.payRatioPct = 70
  benefitForm.validStart = undefined
  benefitForm.validEnd = undefined
  benefitOpen.value = true
}
async function submitBenefit() {
  if (!benefitForm.insuredId || !benefitForm.elderId || benefitForm.dailyQuotaYuan == null) {
    message.error('请填写参保记录、长者与日限额')
    return
  }
  saving.value = true
  try {
    await grantLtciBenefit({
      insuredId: benefitForm.insuredId as unknown as Id,
      elderId: benefitForm.elderId,
      assessmentId: (benefitForm.assessmentId as unknown as Id) || undefined,
      disabilityLevel: benefitForm.disabilityLevel,
      benefitType: benefitForm.benefitType,
      dailyQuota: Math.round((benefitForm.dailyQuotaYuan || 0) * 100),
      payRatio: Number(((benefitForm.payRatioPct || 0) / 100).toFixed(4)),
      validStart: benefitForm.validStart,
      validEnd: benefitForm.validEnd
    })
    message.success('待遇核定成功')
    benefitOpen.value = false
  } finally {
    saving.value = false
  }
}

function openRecord() {
  recordForm.elderId = undefined
  recordForm.serviceDate = dayjs().format('YYYY-MM-DD')
  recordForm.packageId = undefined
  recordForm.itemName = ''
  recordForm.feeYuan = undefined
  recordOpen.value = true
}
async function submitRecord() {
  if (!recordForm.elderId || recordForm.feeYuan == null) {
    message.error('请填写长者与费用')
    return
  }
  saving.value = true
  try {
    await addLtciServiceRecord({
      elderId: recordForm.elderId,
      serviceDate: recordForm.serviceDate,
      packageId: recordForm.packageId,
      itemName: recordForm.itemName || undefined,
      fee: Math.round((recordForm.feeYuan || 0) * 100)
    })
    message.success('服务记录已录入')
    recordOpen.value = false
  } finally {
    saving.value = false
  }
}

loadPackages()
searchElders('')
</script>
