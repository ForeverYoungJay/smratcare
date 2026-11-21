<template>
  <a-modal :open="true" title="护理等级收费" :footer="null" width="820px" :mask-closable="false" @cancel="emit('close')">
    <a-alert type="info" show-icon message="按护理等级与生效月份设置月费；护理等级取自护理部「护理等级」字典，与长者档案一致。护理费在账单中按实际计费天数折算。" />
    <a-alert v-if="!loading && !careLevelOptions.length" type="warning" show-icon style="margin-top: 8px" message="护理部尚未维护护理等级，请先在「护理等级」中添加，再回来设置月费。" />
    <StatefulBlock :loading="loading" :error="error" @retry="load">
      <a-table :columns="columns" :data-source="fees" row-key="id" :scroll="{ x: 620 }" :pagination="{ pageSize: 5 }" size="small">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">{{ Number(record.status) === 1 ? '启用' : '停用' }}</template>
          <template v-else-if="column.key === 'actions'"><a-button type="link" size="small" @click="edit(record)">编辑</a-button></template>
        </template>
      </a-table>
    </StatefulBlock>
    <a-divider>维护护理月费</a-divider>
    <a-form ref="formRef" :model="form" :rules="rules" layout="vertical" @finish="save">
      <a-row :gutter="16">
        <a-col :xs="24" :sm="12"><a-form-item label="护理等级" name="careLevel"><a-select v-model:value="form.careLevel" :options="careLevelOptions" :disabled="editing" placeholder="选择护理部维护的护理等级" /></a-form-item></a-col>
        <a-col :xs="24" :sm="12"><a-form-item label="生效月份" name="effectiveMonth"><a-date-picker v-model:value="form.effectiveMonth" :allow-clear="false" picker="month" style="width:100%" /></a-form-item></a-col>
        <a-col :xs="24" :sm="12"><a-form-item label="护理月费（元/月）" name="feeMonthly"><a-input-number v-model:value="form.feeMonthly" :min="0" :precision="2" style="width:100%" /></a-form-item></a-col>
        <a-col :xs="24" :sm="12"><a-form-item label="状态"><a-switch v-model:checked="form.status" :checked-value="1" :un-checked-value="0" checked-children="启用" un-checked-children="停用" /></a-form-item></a-col>
      </a-row>
      <a-form-item label="备注"><a-input v-model:value="form.remark" /></a-form-item>
      <a-card size="small" title="收费参考试算" style="margin-bottom:16px">
        <a-space wrap>
          <a-radio-group v-model:value="quoteMode" aria-label="参考计费周期"><a-radio-button value="month">整月</a-radio-button><a-radio-button value="days">按入住天数</a-radio-button></a-radio-group>
          <a-input-number v-if="quoteMode === 'days'" v-model:value="quoteDays" aria-label="计费天数" :min="1" :max="form.effectiveMonth?.daysInMonth()" :precision="0" addon-after="天" />
        </a-space>
        <a-descriptions v-if="quote && form.status === 1" :column="1" size="small" style="margin-top:12px">
          <a-descriptions-item label="床位费">{{ quote.bed.toFixed(2) }} 元</a-descriptions-item>
          <a-descriptions-item label="护理费">{{ quote.nursing.toFixed(2) }} 元</a-descriptions-item>
          <a-descriptions-item label="餐费">{{ quote.meal.toFixed(2) }} 元</a-descriptions-item>
          <a-descriptions-item label="参考合计">{{ quote.total.toFixed(2) }} 元（{{ quote.chargeableDays }} 天）</a-descriptions-item>
        </a-descriptions>
        <p v-else>请填写有效的月费、月份和计费天数，并启用当前护理标准。</p>
        <p style="margin:8px 0 0;color:var(--muted)">采用该账期适用的床位、餐费标准与上方护理月费草稿；缺少标准按零试算。试住或短住可按天参考，不含独立套餐优惠、消费、分摊及减免，以正式账单为准。</p>
      </a-card>
      <a-space><a-button type="primary" html-type="submit" :loading="saving" :disabled="!!error || loading">保存护理月费</a-button><a-button @click="reset">新增其他等级</a-button></a-space>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { getBillingCareLevelFees, getBillingFeeHistory, getFinanceConfigImpactPreview, upsertBillingCareLevelFee } from '../../../api/finance'
import { getCareLevelNameOptions } from '../../../api/nursing'
import type { BillingCareLevelFee, FinanceBillingConfigEntry } from '../../../types'
import { useUserStore } from '../../../stores/user'
import { confirmAction } from '../../../utils/actionConfirm'
import StatefulBlock from '../../../components/StatefulBlock.vue'
import { billingQuote } from '../../../utils/billingQuote'

const props = defineProps<{ month: string }>()
const emit = defineEmits<{ close: [] }>()
const user = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false), saving = ref(false), editing = ref(false)
const careLevelOptions = ref<Array<{ value: string; label: string }>>([])
const error = ref('')
const fees = ref<BillingCareLevelFee[]>([])
const baseFees = ref<FinanceBillingConfigEntry[]>([])
const quoteMode = ref('month'), quoteDays = ref<number | null>(1)
const form = reactive({ careLevel: '', feeMonthly: 0, effectiveMonth: dayjs(props.month), status: 1, remark: '' })
const quote = computed(() => loading.value || error.value || !form.effectiveMonth?.isValid() ? null
  : billingQuote(baseFees.value, form.effectiveMonth.format('YYYY-MM'), form.feeMonthly, quoteMode.value === 'days' ? Number(quoteDays.value) : undefined))
const rules = {
  careLevel: [{ required: true, whitespace: true, message: '请输入护理等级' }],
  feeMonthly: [{ required: true, type: 'number' as const, min: 0, message: '请输入不小于零的月费' }],
  effectiveMonth: [{ required: true, message: '请选择生效月份' }]
}
const columns = [
  { title: '护理等级', dataIndex: 'careLevel' }, { title: '月费（元）', dataIndex: 'feeMonthly' },
  { title: '生效月份', dataIndex: 'effectiveMonth' }, { title: '状态', key: 'status' }, { title: '操作', key: 'actions' }
]
function reset() {
  editing.value = false
  Object.assign(form, { careLevel: '', feeMonthly: 0, effectiveMonth: dayjs(props.month), status: 1, remark: '' })
  formRef.value?.clearValidate()
}
function edit(row: BillingCareLevelFee) {
  editing.value = true
  Object.assign(form, { ...row, feeMonthly: Number(row.feeMonthly), status: Number(row.status), effectiveMonth: dayjs(row.effectiveMonth) })
  formRef.value?.clearValidate()
}
async function load() {
  const orgId = user.staffInfo?.orgId
  if (!orgId) { error.value = '未获取到当前机构，请重新登录后重试'; return }
  loading.value = true
  error.value = ''
  try {
    const [careFees, history, levels] = await Promise.all([getBillingCareLevelFees(orgId), getBillingFeeHistory(orgId), getCareLevelNameOptions()])
    careLevelOptions.value = levels
    fees.value = careFees.sort((a, b) => b.effectiveMonth.localeCompare(a.effectiveMonth) || a.careLevel.localeCompare(b.careLevel, 'zh-CN'))
    baseFees.value = history
  } catch (err: any) {
    fees.value = []
    baseFees.value = []
    error.value = err?.message || '护理收费加载失败，请重试'
  } finally { loading.value = false }
}
async function save() {
  if (saving.value || error.value || loading.value) return
  await formRef.value?.validate()
  if (!user.staffInfo?.orgId || !form.careLevel.trim() || !Number.isFinite(form.feeMonthly) || form.feeMonthly < 0 || !form.effectiveMonth?.isValid()) return
  saving.value = true
  try {
    const month = form.effectiveMonth.format('YYYY-MM')
    const preview = await getFinanceConfigImpactPreview({ month, configKeys: [] })
    const confirmed = await confirmAction({ title: '确认保存护理月费？',
      content: `${form.careLevel.trim()}：${form.feeMonthly.toFixed(2)} 元/月，生效月份 ${month}。同等级同月份会更新已有标准。`,
      impactItems: [`当前账期 ${preview.activeBillCount} 条账单、${preview.activeElderCount} 位长者；此统计不是差额试算。`, '已有账单不会由本页面自动重算。'], okText: '确认保存' })
    if (!confirmed) return
    await upsertBillingCareLevelFee({ orgId: user.staffInfo.orgId, careLevel: form.careLevel.trim(), feeMonthly: form.feeMonthly,
      effectiveMonth: month, status: form.status, remark: form.remark })
    message.success('护理月费已保存')
    await load()
    reset()
  } catch (err: any) { message.error(err?.message || '护理月费保存失败') }
  finally { saving.value = false }
}
onMounted(load)
</script>
