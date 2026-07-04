<template>
  <div class="module-hub">
    <!-- Hero：模块名 + 一句话职责 + 常用直达 -->
    <section class="hub-hero card-elevated">
      <div class="hub-hero__main">
        <div class="hub-hero__eyebrow">业务导航</div>
        <h1 class="hub-hero__title">{{ moduleLabel }}</h1>
        <p v-if="moduleDesc" class="hub-hero__desc">{{ moduleDesc }}</p>
        <div class="hub-hero__stats">
          <span class="hub-chip">{{ groupCount }} 个分区</span>
          <span class="hub-chip">{{ pageCount }} 项功能</span>
        </div>
      </div>
      <div v-if="quickEntries.length" class="hub-hero__quick">
        <div class="hub-hero__quick-label">常用入口</div>
        <div class="hub-hero__quick-list">
          <button
            v-for="entry in quickEntries"
            :key="entry.path"
            type="button"
            class="hub-quick-btn"
            @click="go(entry.path)"
          >
            {{ entry.label }}
          </button>
        </div>
      </div>
    </section>

    <!-- 模块实时指标（由具体模块通过 #metrics 注入，可选） -->
    <section v-if="$slots.metrics" class="hub-metrics">
      <slot name="metrics" />
    </section>

    <a-empty v-if="!groups.length" description="该模块暂无可访问的功能" />

    <!-- 分组功能磁贴 -->
    <section v-for="group in groups" :key="group.key" class="hub-group">
      <div class="hub-group__head">
        <span class="hub-group__dot"></span>
        <h2 class="hub-group__title">{{ group.label }}</h2>
        <span v-if="group.desc" class="hub-group__desc">{{ group.desc }}</span>
      </div>
      <div class="hub-tiles">
        <button
          v-for="page in group.pages"
          :key="page.path"
          type="button"
          class="hub-tile"
          @click="go(page.path)"
        >
          <span class="hub-tile__name">{{ page.label }}</span>
          <span v-if="page.desc" class="hub-tile__desc">{{ page.desc }}</span>
          <span class="hub-tile__arrow" aria-hidden="true">→</span>
        </button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getMenuTree, type MenuItem } from '../layouts/menu'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

type Tile = { label: string; desc?: string; path: string }
type Group = { key: string; label: string; desc?: string; pages: Tile[] }

const fullMenu = computed<MenuItem[]>(() =>
  getMenuTree(userStore.roles || [], userStore.pagePermissions || [], { focused: false })
)

// 当前一级模块（用路由首段匹配菜单顶层项）
const currentModule = computed<MenuItem | undefined>(() => {
  const seg = `/${String(route.path || '').split('/').filter(Boolean)[0] || ''}`
  return fullMenu.value.find((m) => m.path === seg || m.key === seg.slice(1)) || fullMenu.value.find((m) => (m.path || '').startsWith(seg))
})

const moduleLabel = computed(() => currentModule.value?.label || '业务导航')
const moduleDesc = computed(() => currentModule.value?.desc || '')

function collectLeaves(items: MenuItem[] = []): Tile[] {
  return items.flatMap((item) => {
    const self: Tile[] = item.path && !item.children?.length ? [{ label: item.label, desc: item.desc, path: item.path }] : []
    return [...self, ...collectLeaves(item.children)]
  })
}

// 一级模块下的“分区”：直接子项若还有子项则成组，否则并入“常用功能”
const groups = computed<Group[]>(() => {
  const children = currentModule.value?.children || []
  const result: Group[] = []
  const looseTiles: Tile[] = []
  children.forEach((child) => {
    if (child.children?.length) {
      result.push({
        key: child.key,
        label: child.label,
        desc: child.desc,
        pages: collectLeaves(child.children)
      })
    } else if (child.path) {
      looseTiles.push({ label: child.label, desc: child.desc, path: child.path })
    }
  })
  if (looseTiles.length) result.unshift({ key: '__loose__', label: '常用功能', pages: looseTiles })
  return result.filter((g) => g.pages.length)
})

const pageCount = computed(() => groups.value.reduce((sum, g) => sum + g.pages.length, 0))
const groupCount = computed(() => groups.value.length)

// 常用入口：取前 4 个功能作为快捷直达
const quickEntries = computed<Tile[]>(() => groups.value.flatMap((g) => g.pages).slice(0, 4))

function go(path?: string) {
  if (path) router.push(path)
}
</script>

<style scoped>
.module-hub {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* Hero */
.hub-hero {
  position: relative;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
  padding: 28px 28px 26px 32px;
  overflow: hidden;
  background:
    radial-gradient(120% 140% at 0% 0%, rgba(var(--primary-rgb), 0.10), transparent 60%),
    var(--surface);
}

.hub-hero::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 5px;
  background: var(--primary-gradient);
}

.hub-hero__eyebrow {
  font-size: 12px;
  letter-spacing: 2px;
  color: var(--primary);
  font-weight: 600;
  margin-bottom: 8px;
}

.hub-hero__title {
  margin: 0;
  font-size: 30px;
  line-height: 1.1;
  color: var(--ink);
  font-weight: 700;
}

.hub-hero__desc {
  margin: 10px 0 0;
  max-width: 620px;
  color: var(--muted);
  font-size: 14px;
  line-height: 1.6;
}

.hub-hero__stats {
  display: flex;
  gap: 8px;
  margin-top: 16px;
}

.hub-chip {
  padding: 4px 12px;
  border-radius: 999px;
  background: var(--primary-soft);
  color: var(--primary-strong);
  font-size: 12px;
  font-weight: 600;
}

.hub-hero__quick {
  min-width: 200px;
}

.hub-hero__quick-label {
  font-size: 12px;
  color: var(--muted-2);
  margin-bottom: 10px;
}

.hub-hero__quick-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.hub-quick-btn {
  text-align: left;
  padding: 9px 14px;
  border: 1px solid var(--border-strong);
  border-radius: var(--radius-sm);
  background: var(--surface);
  color: var(--ink-soft);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.hub-quick-btn:hover {
  border-color: rgba(var(--primary-rgb), 0.5);
  color: var(--primary-strong);
  background: var(--primary-soft);
}

/* 分组 */
.hub-group__head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 12px;
}

.hub-group__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary);
  transform: translateY(-1px);
}

.hub-group__title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--ink);
}

.hub-group__desc {
  font-size: 12px;
  color: var(--muted-2);
}

.hub-tiles {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 12px;
}

.hub-tile {
  position: relative;
  text-align: left;
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 16px 18px;
  border: 1px solid var(--border-strong);
  border-radius: var(--radius-md);
  background: var(--surface);
  box-shadow: var(--shadow-xs);
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease, border-color 0.15s ease;
}

.hub-tile:hover {
  transform: translateY(-2px);
  border-color: rgba(var(--primary-rgb), 0.45);
  box-shadow: var(--shadow-md);
}

.hub-tile__name {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink);
}

.hub-tile__desc {
  font-size: 12px;
  color: var(--muted);
  line-height: 1.5;
}

.hub-tile__arrow {
  position: absolute;
  top: 14px;
  right: 16px;
  color: var(--muted-2);
  opacity: 0;
  transform: translateX(-4px);
  transition: all 0.15s ease;
}

.hub-tile:hover .hub-tile__arrow {
  opacity: 1;
  transform: translateX(0);
  color: var(--primary);
}

@media (max-width: 768px) {
  .hub-hero {
    padding: 22px 18px 20px 22px;
  }
  .hub-hero__title {
    font-size: 24px;
  }
}
</style>
