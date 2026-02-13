<template>
  <PageContainer title="退院办理" subTitle="退院结算与释放床位">
    <a-card class="card-elevated" :bordered="false">
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-form-item label="老人" name="elderId">
          <a-select v-model:value="form.elderId" placeholder="请选择老人">
            <a-select-option v-for="item in elders" :key="item.id" :value="item.id">{{ item.fullName }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="退院日期" name="dischargeDate">
          <a-date-picker v-model:value="form.dischargeDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="结算金额" name="settleAmount">
          <a-input-number v-model:value="form.settleAmount" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="原因" name="reason">
          <a-input v-model:value="form.reason" />
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
import PageContainer from '../../components/PageContainer.vue'
import { getElderPage } from '../../api/elder'
import { dischargeElder } from '../../api/elderLifecycle'
import type { DischargeRequest, ElderItem, PageResult } from '../../types'

const formRef = ref<FormInstance>()
const form = reactive<DischargeRequest>({ elderId: 0, dischargeDate: '' })
const submitting = ref(false)
const elders = ref<ElderItem[]>([])

const rules: FormRules = {
  elderId: [{ required: true, message: '请选择老人' }],
  dischargeDate: [{ required: true, message: '请选择退院日期' }]
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
    await dischargeElder(form)
    message.success('退院办理成功')
  } catch {
    message.error('提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(loadElders)
</script>
