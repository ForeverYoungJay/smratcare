<template>
  <div class="stateful-wrap">
    <template v-if="loading">
      <div class="stateful-panel">
        <a-skeleton active :paragraph="{ rows: rows }" />
      </div>
    </template>
    <template v-else-if="error">
      <div class="stateful-panel">
        <a-result status="error" :title="errorTitle" :sub-title="error">
          <template #extra>
            <a-button type="primary" size="small" @click="$emit('retry')">重试</a-button>
          </template>
        </a-result>
      </div>
    </template>
    <template v-else-if="empty">
      <div class="stateful-panel empty-wrap">
        <!-- 传了 filterModel 就能区分「被筛掉了」和「本来就没有」，两者的下一步完全不同 -->
        <TableEmpty
          :filtered="isFilteredEmpty"
          :title="emptyText === '暂无数据' ? undefined : emptyText"
          @clear-filters="onClearFilters"
        />
      </div>
    </template>
    <template v-else>
      <slot />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import TableEmpty from './TableEmpty.vue'
import { clearFilters, countActiveFilters } from '../utils/filterState'

const props = withDefaults(
  defineProps<{
    loading?: boolean
    error?: string
    empty?: boolean
    emptyText?: string
    rows?: number
    errorTitle?: string
    /** 传入页面的查询表单对象后，空状态会区分「被筛掉了」和「本来就没有」 */
    filterModel?: Record<string, any> | null
  }>(),
  {
    loading: false,
    error: '',
    empty: false,
    emptyText: '暂无数据',
    rows: 4,
    errorTitle: '加载失败'
  }
)

const emit = defineEmits<{ (e: 'retry'): void; (e: 'clear-filters'): void }>()

const isFilteredEmpty = computed(() => countActiveFilters(props.filterModel) > 0)

/**
 * 先真正清掉筛选条件再让页面重查。
 * 不能复用页面的「清空」按钮：那个是恢复初始值，而初始值本身常常就是筛选条件。
 */
function onClearFilters() {
  clearFilters(props.filterModel)
  emit('clear-filters')
}
</script>

<style scoped>
.stateful-wrap {
  min-height: 64px;
}

.stateful-panel {
  padding: 12px;
}
</style>
