<template>
  <PageContainer title="套餐任务生成" subTitle="按老人套餐生成护理任务">
    <a-card :bordered="false" class="card-elevated">
      <a-form layout="vertical">
        <a-form-item label="日期" required>
          <a-date-picker v-model:value="form.date" value-format="YYYY-MM-DD" style="width: 240px" />
        </a-form-item>
        <a-form-item label="覆盖生成">
          <a-switch v-model:checked="form.force" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" :loading="loading" @click="submit">生成任务</a-button>
        </a-form-item>
      </a-form>
      <a-alert v-if="lastCount !== null" :message="`已生成 ${lastCount} 条任务`" type="success" show-icon />
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { generateTasksByPackage } from '../../api/standard'

const loading = ref(false)
const lastCount = ref<number | null>(null)
const form = reactive({ date: '', force: false })

async function submit() {
  if (!form.date) {
    message.warning('请选择日期')
    return
  }
  loading.value = true
  try {
    const count = await generateTasksByPackage({ date: form.date, force: form.force })
    lastCount.value = Number(count)
    message.success('任务已生成')
  } catch {
    message.error('生成失败')
  } finally {
    loading.value = false
  }
}
</script>
