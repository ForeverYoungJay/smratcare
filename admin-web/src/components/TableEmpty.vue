<template>
  <div class="empty-wrap">
    <a-empty>
      <template #description>
        <div class="empty-copy">
          <strong class="empty-copy__title">{{ resolvedTitle }}</strong>
          <span class="empty-copy__description">{{ resolvedDescription }}</span>
        </div>
      </template>
      <div v-if="filtered || $slots.default" class="empty-extra">
        <!-- 被筛掉时最有用的下一步就是清空筛选，不需要每个页面各写一遍 -->
        <a-button v-if="filtered" size="small" @click="emit('clear-filters')">清空筛选条件</a-button>
        <slot />
      </div>
    </a-empty>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

/**
 * 表格空状态。
 *
 * 「本来就没有数据」和「被筛选条件挡掉了」对用户是两件完全不同的事，
 * 必须给出不同的下一步。DataTable 和仍在用裸 a-table 的页面共用这一份实现。
 */
const props = defineProps<{
  filtered?: boolean
  title?: string
  description?: string
}>()

const emit = defineEmits<{ (e: 'clear-filters'): void }>()

const resolvedTitle = computed(() => {
  if (props.filtered) return '没有符合筛选条件的记录'
  return props.title || '这里还没有记录'
})

const resolvedDescription = computed(() => {
  if (props.filtered) return '换个条件试试，或者清空筛选查看全部数据。'
  return props.description || '新建后会出现在这个列表里。'
})
</script>

<style scoped>
.empty-wrap {
  padding: 12px 0;
}

.empty-copy {
  display: grid;
  gap: 6px;
}

.empty-copy__title {
  color: var(--text-strong, #22332e);
  font-size: 15px;
}

.empty-copy__description {
  color: var(--muted, #5c6f69);
  font-size: 12px;
  line-height: 1.7;
}

.empty-extra {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 14px;
}
</style>
