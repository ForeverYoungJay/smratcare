<template>
  <section class="search-form-card">
    <QueryBar :model="model" @finish="onSearch" @elder-autosearch="onAutoSearch">
      <slot />
      <template #actions>
        <a-button type="primary" html-type="submit">{{ searchText }}</a-button>
        <a-button @click="onReset">{{ resetText }}</a-button>
        <ActionToolbar v-if="$slots.extra"><slot name="extra" /></ActionToolbar>
      </template>
    </QueryBar>
    <div v-if="activeFieldCount || description" class="query-context">
      <span v-if="activeFieldCount">已启用 {{ activeFieldCount }} 个条件</span>
      <span v-if="description">{{ description }}</span>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import QueryBar from './QueryBar.vue'
import ActionToolbar from './ActionToolbar.vue'
import dayjs from 'dayjs'
import { countActiveFilters } from '../utils/filterState'

const props = withDefaults(
  defineProps<{
    model: Record<string, any>
    title?: string
    description?: string
    searchText?: string
    resetText?: string
  }>(),
  {
    title: '快速筛选',
    // 默认不再常驻说明长句（125 处调用全站重复出现会被视觉过滤且挤占首屏）；页面确有特殊口径时可显式传入
    description: '',
    searchText: '查询',
    resetText: '清空'
  }
)
const emit = defineEmits<{ (e: 'search'): void; (e: 'reset'): void }>()
const initialModel: Record<string, any> = {}
const activeFieldCount = computed(() => countActiveFilters(props.model))

function cloneValue<T>(value: T): T {
  if (value === undefined || value === null || typeof value !== 'object') return value
  if (dayjs.isDayjs(value)) return value.clone() as T
  if (Array.isArray(value)) return value.map((item) => cloneValue(item)) as T
  if (value instanceof Date) return new Date(value.getTime()) as T
  if (typeof structuredClone === 'function') {
    try {
      return structuredClone(value)
    } catch {
      // Fall through to the safer recursive clone below for proxied or non-cloneable values.
    }
  }
  if (Object.prototype.toString.call(value) === '[object Object]') {
    const cloned: Record<string, any> = {}
    Object.keys(value as Record<string, any>).forEach((key) => {
      cloned[key] = cloneValue((value as Record<string, any>)[key])
    })
    return cloned as T
  }
  try {
    return JSON.parse(JSON.stringify(value))
  } catch {
    return value
  }
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
.search-form-card { padding:0; border:0; background:none; box-shadow:none; }
.query-context { display:flex; flex-wrap:wrap; gap:12px; margin:6px 0 0; color:var(--muted); font-size:13px; }
</style>
