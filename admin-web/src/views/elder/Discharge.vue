<template>
  <PageContainer title="退院办理" subTitle="退院结算与释放床位">
    <a-alert
      v-if="highlightApplyId"
      style="margin-bottom: 16px;"
      type="success"
      show-icon
      :message="`已由申请(${highlightApplyId})触发自动退住，当前页面可用于补充人工登记`"
    />
    <a-card class="card-elevated" :bordered="false">
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-form-item label="老人" name="elderId">
          <a-select
            v-model:value="form.elderId"
            show-search
            :filter-option="false"
            :options="elderOptions"
            :loading="elderLoading"
            placeholder="请输入老人姓名/拼音首字母"
            @search="searchElders"
            @focus="() => !elderOptions.length && searchElders('')"
          />
        </a-form-item>
        <a-form-item label="退院日期" name="dischargeDate">
          <a-date-picker v-model:value="form.dischargeDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="结算金额" name="settleAmount">
          <a-input-number v-model:value="form.settleAmount" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="原因" name="reason">
          <a-select v-model:value="form.reason" allow-clear placeholder="请选择退住费用设置">
            <a-select-option v-for="item in dischargeFeeConfigOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-input v-model:value="form.remark" />
        </a-form-item>
      </a-form>
      <div style="text-align: right;">
        <a-button type="primary" :loading="submitting" @click="submit">提交退院</a-button>
      </div>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import { useRoute } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { getBaseConfigItemList } from '../../api/baseConfig'
import { useElderOptions } from '../../composables/useElderOptions'
import { dischargeElder } from '../../api/elderLifecycle'
import type { BaseConfigItem, DischargeRequest } from '../../types'

const formRef = ref<FormInstance>()
const form = reactive<DischargeRequest>({ elderId: 0, dischargeDate: '' })
const submitting = ref(false)
const route = useRoute()
const highlightApplyId = ref('')
const dischargeFeeConfigOptions = ref<{ label: string; value: string }[]>([])
const { elderOptions, elderLoading, searchElders } = useElderOptions({ pageSize: 80 })

const rules: FormRules = {
  elderId: [{ required: true, message: '请选择老人' }],
  dischargeDate: [{ required: true, message: '请选择退院日期' }]
}

async function loadDischargeFeeConfigOptions() {
  const options = await getBaseConfigItemList({ configGroup: 'DISCHARGE_FEE_CONFIG', status: 1 })
  dischargeFeeConfigOptions.value = (options || []).map((item: BaseConfigItem) => ({
    label: item.itemName,
    value: item.itemName
  }))
}

async function submit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true
    await dischargeElder(form)
    message.success('退院办理成功')
  } catch {
    message.error('提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  highlightApplyId.value = String(route.query.highlightApplyId || '')
  loadDischargeFeeConfigOptions()
  searchElders('')
})
</script>
