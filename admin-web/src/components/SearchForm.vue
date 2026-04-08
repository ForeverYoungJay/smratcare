<template>
  <section class="search-form-card card-elevated">
    <div class="search-form-head">
      <div class="search-copy">
        <div class="search-copy__eyebrow">{{ title }}</div>
        <strong>{{ heading }}</strong>
        <span>{{ description }}</span>
        <div class="search-copy__meta">
          <span class="search-chip">已启用 {{ activeFieldCount }} 个筛选条件</span>
          <span class="search-chip search-chip--ghost">支持回车快速查询</span>
        </div>
      </div>
      <div class="search-side">
        <div class="search-extra" v-if="$slots.extra">
          <slot name="extra" />
        </div>
        <div class="search-actions">
          <a-button type="primary" html-type="submit" @click="onSearch">{{ searchText }}</a-button>
          <a-button @click="onReset">{{ resetText }}</a-button>
        </div>
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
    </a-form>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'

const props = withDefaults(
  defineProps<{
    model: Record<string, any>
    title?: string
    description?: string
    searchText?: string
    resetText?: string
  }>(),
  {
    title: 'Operational Filters',
    description: '优先输入关键词，再按状态、时间或组织范围逐步缩小结果。',
    searchText: '查询',
    resetText: '清空'
  }
)
const emit = defineEmits<{ (e: 'search'): void; (e: 'reset'): void }>()
const initialModel: Record<string, any> = {}

const activeFieldCount = computed(() =>
  Object.values(props.model || {}).filter((value) => {
    if (Array.isArray(value)) return value.length > 0
    if (typeof value === 'string') return value.trim().length > 0
    return value !== undefined && value !== null && value !== ''
  }).length
)
const heading = computed(() => (activeFieldCount.value > 0 ? '当前已按条件筛选' : '筛选条件'))

function cloneValue<T>(value: T): T {
  if (value === undefined || value === null || typeof value !== 'object') return value
  if (typeof structuredClone === 'function') return structuredClone(value)
  return JSON.parse(JSON.stringify(value))
}

onMounted(() => {
  Object.keys(props.model).forEach((key) => {
    initialModel[key] = cloneValue(props.model[key])
  })
})

function onSearch() {
  emit('search')
}

function onAutoSearch() {
  emit('search')
}

function onReset() {
  Object.keys(props.model).forEach((key) => {
    props.model[key] = cloneValue(initialModel[key])
  })
  emit('reset')
}
</script>

<style scoped>
.search-form-card {
  padding: 18px 20px;
}

.search-form-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid #e5eef5;
}

.search-copy {
  display: grid;
  gap: 6px;
}

.search-copy__eyebrow {
  color: #7f96aa;
  font-size: 11px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  font-weight: 700;
}

.search-copy strong {
  color: #12314d;
  font-size: 16px;
}

.search-copy span {
  color: #5f7b95;
  font-size: 12px;
  line-height: 1.6;
}

.search-copy__meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.search-chip {
  display: inline-flex;
  align-items: center;
  min-height: 26px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(19, 108, 181, 0.1);
  color: #136cb5;
  font-size: 12px;
  font-weight: 700;
}

.search-chip--ghost {
  background: rgba(95, 123, 149, 0.08);
  color: #4f6a84;
}

.search-side {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
}

.search-extra {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.search-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
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

  .search-side,
  .search-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .search-actions :deep(.ant-btn) {
    flex: 1;
  }

  .search-form :deep(.ant-form-item) {
    width: 100%;
  }
}
</style>
