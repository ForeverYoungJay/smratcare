<template>
  <a-modal
    :open="open"
    :footer="null"
    :width="720"
    class="global-search-palette"
    centered
    title="全局搜索"
    @cancel="$emit('close')"
  >
    <a-input
      ref="inputRef"
      :value="keyword"
      allow-clear
      autofocus
      placeholder="搜索页面、常用动作或最近访问"
      size="large"
      @input="$emit('update:keyword', ($event.target as HTMLInputElement).value)"
      @keydown="onKeydown"
    />
    <div class="global-search-palette__hint">支持模糊匹配（如“护任”可找到“护理任务”），多个关键词用空格分隔；↑ ↓ 选择，Enter 打开</div>

    <div class="global-search-palette__body">
      <section v-if="actionItems.length" class="global-search-palette__section">
        <div class="global-search-palette__section-title">常用动作</div>
        <button
          v-for="item in actionItems"
          :key="item.key"
          type="button"
          class="global-search-palette__item"
          :class="{ 'is-active': item === activeItem }"
          @click="$emit('select', item)"
        >
          <div>
            <strong>{{ item.title }}</strong>
            <span>{{ item.description }}</span>
          </div>
          <small>{{ item.group }}</small>
        </button>
      </section>

      <section v-if="pageItems.length" class="global-search-palette__section">
        <div class="global-search-palette__section-title">页面</div>
        <button
          v-for="item in pageItems"
          :key="item.key"
          type="button"
          class="global-search-palette__item"
          :class="{ 'is-active': item === activeItem }"
          @click="$emit('select', item)"
        >
          <div>
            <strong>{{ item.title }}</strong>
            <span>{{ item.description }}</span>
          </div>
          <small>{{ item.group }}</small>
        </button>
      </section>

      <section v-if="recentItems.length" class="global-search-palette__section">
        <div class="global-search-palette__section-title">最近访问</div>
        <button
          v-for="item in recentItems"
          :key="item.key"
          type="button"
          class="global-search-palette__item"
          :class="{ 'is-active': item === activeItem }"
          @click="$emit('select', item)"
        >
          <div>
            <strong>{{ item.title }}</strong>
            <span>{{ item.description }}</span>
          </div>
          <small>{{ item.group }}</small>
        </button>
      </section>

      <a-empty v-if="!actionItems.length && !pageItems.length && !recentItems.length" description="没有匹配结果" />
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue'

export type SearchPaletteItem = {
  key: string
  title: string
  description: string
  group: string
  path?: string
  action?: string
  keywords?: string
}

const props = defineProps<{
  actionItems: SearchPaletteItem[]
  keyword: string
  open: boolean
  pageItems: SearchPaletteItem[]
  recentItems: SearchPaletteItem[]
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'select', item: SearchPaletteItem): void
  (e: 'update:keyword', value: string): void
}>()

const allItems = computed(() => [...props.actionItems, ...props.pageItems, ...props.recentItems])
const activeIndex = ref(0)
const activeItem = computed(() => allItems.value[activeIndex.value])
watch(allItems, () => { activeIndex.value = 0 })
// autofocus 只在首次挂载时生效，之后每次打开都要手动聚焦
const inputRef = ref<{ focus: () => void } | null>(null)
watch(() => props.open, (open) => { if (open) nextTick(() => inputRef.value?.focus()) })

function onKeydown(event: KeyboardEvent) {
  if (event.isComposing) return
  const total = allItems.value.length
  if (!total) return
  if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
    event.preventDefault()
    activeIndex.value = (activeIndex.value + (event.key === 'ArrowDown' ? 1 : total - 1)) % total
    nextTick(() => document.querySelector('.global-search-palette__item.is-active')?.scrollIntoView({ block: 'nearest' }))
  } else if (event.key === 'Enter' && activeItem.value) {
    event.preventDefault()
    emit('select', activeItem.value)
  }
}
</script>

<style scoped>
.global-search-palette :deep(.ant-modal-content) {
  border-radius: 24px;
}

.global-search-palette__body {
  display: grid;
  gap: 18px;
  margin-top: 18px;
  max-height: 60vh;
  overflow-y: auto;
}

.global-search-palette__section {
  display: grid;
  gap: 8px;
}

.global-search-palette__section-title {
  color: var(--muted-2);
  font-size: 12px;
  font-weight: 800;
}

.global-search-palette__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  width: 100%;
  padding: 12px 14px;
  border: 1px solid rgba(211, 223, 232, 0.9);
  border-radius: 16px;
  background: #fff;
  text-align: left;
  cursor: pointer;
}

.global-search-palette__hint {
  margin-top: 8px;
  color: var(--muted-2);
  font-size: 12px;
}
.global-search-palette__item:hover,
.global-search-palette__item.is-active {
  border-color: rgba(var(--primary-rgb), 0.3);
  background: linear-gradient(180deg, #ffffff 0%, var(--primary-soft) 160%);
  box-shadow: var(--shadow-xs);
}

.global-search-palette__item div {
  display: grid;
  gap: 3px;
}

.global-search-palette__item strong {
  color: var(--ink);
}

.global-search-palette__item span,
.global-search-palette__item small {
  color: var(--muted);
  font-size: 12px;
}
</style>

