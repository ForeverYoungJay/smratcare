<template>
  <PageContainer title="合同签约" sub-title="签约登记并同步入院、押金与床位信息">
    <a-row :gutter="16">
      <a-col :xs="24" :lg="8">
        <a-card class="card-elevated" :bordered="false" title="待签约线索">
          <a-statistic :value="reservationCount" />
          <div class="meta-text">来自销售跟进中的预订客户</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="16">
        <a-card class="card-elevated" :bordered="false" title="签约办理">
          <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item label="关联预订客户">
                  <a-select v-model:value="selectedLeadId" allow-clear placeholder="可选">
                    <a-select-option v-for="item in reservationLeads" :key="item.id" :value="item.id">
                      {{ item.name }}{{ item.phone ? `（${item.phone}）` : '' }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="老人" name="elderId">
                  <a-select v-model:value="form.elderId" show-search :filter-option="filterElder" placeholder="选择老人">
                    <a-select-option v-for="item in elders" :key="item.id" :value="item.id">
                      {{ item.fullName }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>

            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item label="合同号" name="contractNo">
                  <a-input v-model:value="form.contractNo" placeholder="如 HT2026020001" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="入院日期" name="admissionDate">
                  <a-date-picker v-model:value="form.admissionDate" value-format="YYYY-MM-DD" style="width: 100%" />
                </a-form-item>
              </a-col>
            </a-row>

            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item label="押金金额" name="depositAmount">
                  <a-input-number v-model:value="form.depositAmount" :min="0" style="width: 100%" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="床位">
                  <a-select v-model:value="form.bedId" allow-clear placeholder="可选，签约后分配">
                    <a-select-option v-for="item in bedOptions" :key="item.value" :value="item.value">
                      {{ item.label }}
                    </a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>

            <a-form-item label="备注">
              <a-textarea v-model:value="form.remark" :rows="3" />
            </a-form-item>
          </a-form>
          <div class="actions">
            <a-space>
              <a-button @click="reset">重置</a-button>
              <a-button type="primary" :loading="submitting" @click="submit">确认签约</a-button>
            </a-space>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import { getBedList } from '../../api/bed'
import { getElderPage } from '../../api/elder'
import { admitElder } from '../../api/elderLifecycle'
import { getLeadPage, updateCrmLead } from '../../api/marketing'
import type { AdmissionRequest, BedItem, CrmLeadItem, ElderItem, PageResult } from '../../types'

const formRef = ref<FormInstance>()
const submitting = ref(false)
const selectedLeadId = ref<number | undefined>()
const reservationLeads = ref<CrmLeadItem[]>([])
const elders = ref<ElderItem[]>([])
const beds = ref<BedItem[]>([])

const form = reactive<AdmissionRequest>({
  elderId: 0,
  admissionDate: dayjs().format('YYYY-MM-DD'),
  contractNo: '',
  depositAmount: 0,
  bedId: undefined,
  bedStartDate: undefined,
  remark: ''
})

const rules: FormRules = {
  elderId: [{ required: true, message: '请选择老人' }],
  admissionDate: [{ required: true, message: '请选择入院日期' }],
  contractNo: [{ required: true, message: '请输入合同号' }]
}

const reservationCount = computed(() => reservationLeads.value.length)
const bedOptions = computed(() => beds.value
  .filter((item) => !item.elderId && (item.status === 1 || item.status === undefined))
  .map((item) => ({ value: item.id, label: `${item.roomNo || item.roomId}-${item.bedNo}` })))

function filterElder(input: string, option: any) {
  return String(option?.children || '').toLowerCase().includes(input.toLowerCase())
}

function generateContractNo() {
  const date = dayjs().format('YYYYMMDD')
  const rand = Math.floor(1000 + Math.random() * 9000)
  return `HT${date}${rand}`
}

function reset() {
  form.elderId = 0
  form.admissionDate = dayjs().format('YYYY-MM-DD')
  form.contractNo = generateContractNo()
  form.depositAmount = 0
  form.bedId = undefined
  form.bedStartDate = undefined
  form.remark = ''
  selectedLeadId.value = undefined
}

async function submit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true
    if (form.bedId && !form.bedStartDate) {
      form.bedStartDate = form.admissionDate
    }
    await admitElder(form)
    if (selectedLeadId.value) {
      const lead = reservationLeads.value.find((item) => item.id === selectedLeadId.value)
      if (lead) {
        await updateCrmLead(lead.id, {
          ...lead,
          status: 2,
          contractSignedFlag: 1,
          contractSignedAt: dayjs().format('YYYY-MM-DDTHH:mm:ss'),
          contractNo: form.contractNo,
          remark: `${lead.remark || ''} 已签约`
        } as any)
      }
    }
    message.success('签约成功，已完成入院登记')
    await loadData()
    reset()
  } catch {
    message.error('签约失败')
  } finally {
    submitting.value = false
  }
}

async function loadData() {
  const [leadPage, elderPage, bedList] = await Promise.all([
    getLeadPage({ pageNo: 1, pageSize: 300, status: 2 }),
    getElderPage({ pageNo: 1, pageSize: 300 }),
    getBedList()
  ]) as [PageResult<CrmLeadItem>, PageResult<ElderItem>, BedItem[]]
  reservationLeads.value = leadPage.list || []
  elders.value = elderPage.list || []
  beds.value = bedList || []
}

watch(selectedLeadId, (id) => {
  if (!id || form.contractNo) return
  form.contractNo = generateContractNo()
})

onMounted(async () => {
  reset()
  await loadData()
})
</script>

<style scoped>
.meta-text {
  margin-top: 8px;
  color: var(--muted);
  font-size: 12px;
}

.actions {
  text-align: right;
}
</style>
