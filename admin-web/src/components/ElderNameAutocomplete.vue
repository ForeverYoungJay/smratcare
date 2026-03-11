<template>
  <div ref="hostRef">
    <a-auto-complete
      :value="innerValue"
      :options="autoCompleteOptions"
      :placeholder="placeholder || '请输入长者姓名/拼音首字母'"
      :allow-clear="allowClear"
      :style="mergedStyle"
      @update:value="onUpdateValue"
      @search="onSearch"
      @select="onSelect"
      @focus="onFocus"
      @clear="onClear"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useElderOptions } from '../composables/useElderOptions'

const props = withDefaults(defineProps<{
  value?: string
  placeholder?: string
  width?: string
  allowClear?: boolean
  inHospitalOnly?: boolean
  signedOnly?: boolean
}>(), {
  value: '',
  placeholder: '',
  width: '',
  allowClear: true,
  inHospitalOnly: false,
  signedOnly: false
})

const emit = defineEmits<{
  (event: 'update:value', value: string): void
  (event: 'select', payload: { elderId: string; elderName: string }): void
}>()

const innerValue = ref(String(props.value || ''))
const hostRef = ref<HTMLElement | null>(null)
const { elderOptions, searchElders } = useElderOptions({
  pageSize: 80,
  inHospitalOnly: props.inHospitalOnly,
  signedOnly: props.signedOnly
})

watch(
  () => props.value,
  (value) => {
    const next = String(value || '')
    if (next !== innerValue.value) {
      innerValue.value = next
    }
  }
)

const autoCompleteOptions = computed(() =>
  elderOptions.value.map((item) => ({
    value: item.name,
    label: item.label,
    key: String(item.value)
  }))
)

const mergedStyle = computed(() => {
  if (props.width && props.width.trim()) {
    return { width: props.width.trim() }
  }
  return undefined
})

function onUpdateValue(value: string) {
  innerValue.value = String(value || '')
  emit('update:value', innerValue.value)
}

function onSearch(value: string) {
  onUpdateValue(value)
  searchElders(value || '').catch(() => {})
}

function onSelect(value: string, option: any) {
  const elderId = String(option?.key || '')
  const elderName = String(value || '').trim()
  innerValue.value = elderName
  emit('update:value', elderName)
  if (elderId) {
    emit('select', { elderId, elderName })
  }
  // Bubble a native event so parent search forms can auto-trigger query.
  hostRef.value?.dispatchEvent(new CustomEvent('elder-autosearch', {
    bubbles: true,
    detail: { elderId, elderName }
  }))
}

function onFocus() {
  if (!elderOptions.value.length) {
    searchElders(innerValue.value || '').catch(() => {})
  }
}

function onClear() {
  innerValue.value = ''
  emit('update:value', '')
}
</script>
