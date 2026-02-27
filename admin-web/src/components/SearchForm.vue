<template>
  <a-card class="card-elevated search-form-card" :bordered="false">
    <a-form :model="model" layout="inline" @finish="onSearch" class="search-form">
      <slot />
      <slot name="extra" />
      <a-form-item>
        <a-space>
          <a-button type="primary" html-type="submit">搜索</a-button>
          <a-button @click="onReset">清空</a-button>
        </a-space>
      </a-form-item>
    </a-form>
  </a-card>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'

const props = defineProps<{ model: Record<string, any> }>()
const emit = defineEmits<{ (e: 'search'): void; (e: 'reset'): void }>()
const initialModel: Record<string, any> = {}

onMounted(() => {
  Object.keys(props.model).forEach((key) => {
    initialModel[key] = props.model[key]
  })
})

function onSearch() {
  emit('search')
}

function onReset() {
  Object.keys(props.model).forEach((k) => (props.model[k] = initialModel[k]))
  emit('reset')
}
</script>

<style scoped>
.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 10px;
}

@media (max-width: 768px) {
  .search-form :deep(.ant-form-item) {
    width: 100%;
    margin-inline-end: 0;
  }
}
</style>
