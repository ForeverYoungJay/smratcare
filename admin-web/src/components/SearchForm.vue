<template>
  <a-card class="card-elevated" :bordered="false">
    <a-form :model="model" layout="inline" @finish="onSearch" class="search-form">
      <slot />
      <slot name="extra" />
      <a-form-item>
        <a-space>
          <a-button type="primary" html-type="submit">查询</a-button>
          <a-button @click="onReset">重置</a-button>
        </a-space>
      </a-form-item>
    </a-form>
  </a-card>
</template>

<script setup lang="ts">
const props = defineProps<{ model: Record<string, any> }>()
const emit = defineEmits<{ (e: 'search'): void; (e: 'reset'): void }>()

function onSearch() {
  emit('search')
}

function onReset() {
  Object.keys(props.model).forEach((k) => (props.model[k] = undefined))
  emit('reset')
}
</script>

<style scoped>
.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
</style>
