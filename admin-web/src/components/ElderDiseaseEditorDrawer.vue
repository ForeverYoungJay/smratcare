<template>
  <a-drawer
    :open="open"
    :title="drawerTitle"
    width="520"
    destroy-on-close
    @close="closeDrawer"
  >
    <a-spin :spinning="loading">
      <a-alert
        type="info"
        show-icon
        style="margin-bottom: 16px"
        message="基础疾病会联动商城禁忌、配餐风险与医护提醒。"
      />

      <a-form layout="vertical">
        <a-form-item label="当前长者">
          <a-input :value="elderName || '-'" disabled />
        </a-form-item>
        <a-form-item label="已选基础疾病">
          <a-space v-if="selectedLabels.length" wrap>
            <a-tag v-for="item in selectedLabels" :key="item" color="blue">{{ item }}</a-tag>
          </a-space>
          <a-empty v-else description="尚未维护基础疾病" />
        </a-form-item>
        <a-form-item label="选择疾病">
          <a-select
            v-model:value="selectedDiseaseIds"
            mode="multiple"
            allow-clear
            show-search
            :options="diseaseOptions"
            placeholder="请选择基础疾病"
          />
        </a-form-item>
      </a-form>
    </a-spin>

    <template #extra>
      <a-space>
        <a-button @click="openDiseaseRuleConfig">疾病字典/规则</a-button>
        <a-button @click="closeDrawer">取消</a-button>
        <a-button type="primary" :loading="saving" @click="submit">保存</a-button>
      </a-space>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { updateElderDiseases } from '../api/elder'
import { useElderDiseaseCatalog } from '../composables/useElderDiseaseCatalog'
import type { Id } from '../types'

const props = defineProps<{
  open: boolean
  elderId?: Id
  elderName?: string
  title?: string
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'saved', payload: { elderId?: Id; diseaseIds: number[]; diseaseLabels: string[] }): void
}>()

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const selectedDiseaseIds = ref<number[]>([])
const selectedLabels = computed(() => selectedDiseaseIds.value
  .map((item) => diseaseNameMap.value[item] || `疾病#${item}`))
const drawerTitle = computed(() => props.title || '维护基础疾病')
const { diseaseOptions, diseaseNameMap, ensureDiseaseCatalogLoaded, loadDiseaseState } = useElderDiseaseCatalog()

async function loadCurrentState() {
  if (!props.open || !props.elderId) {
    selectedDiseaseIds.value = []
    return
  }
  loading.value = true
  try {
    await ensureDiseaseCatalogLoaded()
    const state = await loadDiseaseState(props.elderId)
    selectedDiseaseIds.value = state.ids
  } catch (error: any) {
    message.error(error?.message || '加载基础疾病失败')
    selectedDiseaseIds.value = []
  } finally {
    loading.value = false
  }
}

function closeDrawer() {
  emit('update:open', false)
}

function openDiseaseRuleConfig() {
  router.push('/logistics/commerce/risk')
}

async function submit() {
  if (!props.elderId) {
    message.warning('请先选择长者')
    return
  }
  saving.value = true
  try {
    await updateElderDiseases(props.elderId, selectedDiseaseIds.value)
    message.success('基础疾病已更新')
    emit('saved', {
      elderId: props.elderId,
      diseaseIds: selectedDiseaseIds.value,
      diseaseLabels: selectedLabels.value
    })
    closeDrawer()
  } catch (error: any) {
    message.error(error?.message || '保存基础疾病失败')
  } finally {
    saving.value = false
  }
}

watch(
  () => [props.open, props.elderId],
  () => {
    loadCurrentState().catch(() => {})
  },
  { immediate: true }
)
</script>
