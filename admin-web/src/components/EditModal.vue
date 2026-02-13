<template>
  <a-modal
    :open="open"
    :title="title"
    :confirm-loading="confirmLoading"
    @update:open="(val) => emit('update:open', val)"
    @ok="handleOk"
  >
    <a-form ref="formRef" :model="model" :rules="rules" layout="vertical">
      <slot />
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps<{
  open: boolean
  title: string
  model: Record<string, any>
  rules?: Record<string, any>
  confirmLoading?: boolean
  onSubmit: (model: Record<string, any>) => Promise<any>
}>()

const emit = defineEmits<{ (e: 'update:open', value: boolean): void }>()

const formRef = ref()

async function handleOk() {
  if (!formRef.value) return
  await formRef.value.validate()
  await props.onSubmit(props.model)
}
</script>
