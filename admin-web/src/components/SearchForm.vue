<template>
  <section class="search-form-card card-elevated">
    <div class="search-form-head">
      <div class="search-copy">
        <strong>筛选条件</strong>
        <span>优先输入关键词，再按状态或时间缩小范围。</span>
      </div>
      <div class="search-actions">
        <a-button type="primary" html-type="submit" @click="onSearch">搜索</a-button>
        <a-button @click="onReset">清空</a-button>
      </div>
    </div>

    <a-form
      :model="model"
      layout="inline"
      @finish="onSearch"
      @elder-autosearch="onAutoSearch"
      class="search-form"
    >
      <slot />
      <slot name="extra" />
    </a-form>
  </section>
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

function onAutoSearch() {
  emit('search')
}

function onReset() {
  Object.keys(props.model).forEach((k) => (props.model[k] = initialModel[k]))
  emit('reset')
}
</script>

<style scoped>
.search-form-card {
  padding: 16px 18px;
}

.search-form-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e8f0f6;
}

.search-copy {
  display: grid;
  gap: 4px;
}

.search-copy strong {
  color: #173854;
  font-size: 15px;
}

.search-copy span {
  color: #6d8aa3;
  font-size: 12px;
}

.search-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 12px;
}

.search-form :deep(.ant-form-item) {
  margin-inline-end: 0;
  margin-bottom: 0;
}

@media (max-width: 768px) {
  .search-form-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .search-actions {
    width: 100%;
  }

  .search-actions :deep(.ant-btn) {
    flex: 1;
  }

  .search-form :deep(.ant-form-item) {
    width: 100%;
  }
}
</style>
