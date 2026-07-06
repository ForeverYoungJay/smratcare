<template>
  <PageContainer title="全部功能地图" subTitle="按业务板块浏览全站功能，新人快速了解“每个页面是干嘛的”">
    <template #extra>
      <a-input
        v-model:value="keyword"
        allow-clear
        placeholder="搜索功能：如 收钱 / 找床 / 请假"
        style="width: 260px"
      />
    </template>

    <a-empty v-if="!visibleGroups.length" description="没有匹配的功能" />

    <section v-for="group in visibleGroups" :key="group.section" class="fmap-group">
      <div class="fmap-group__title">{{ group.label }}</div>
      <div class="fmap-grid">
        <a-card
          v-for="mod in group.modules"
          :key="mod.key"
          class="card-elevated fmap-module"
          :bordered="false"
        >
          <div class="fmap-module__head" :class="{ 'is-link': !!mod.path }" @click="mod.path && go(mod.path)">
            <strong>{{ mod.label }}</strong>
            <span v-if="mod.desc" class="fmap-module__desc">{{ mod.desc }}</span>
          </div>
          <ul class="fmap-pages">
            <li
              v-for="page in mod.pages"
              :key="page.key"
              class="fmap-page"
              @click="page.path && go(page.path)"
            >
              <span class="fmap-page__name">{{ page.label }}</span>
              <span v-if="page.desc" class="fmap-page__desc">{{ page.desc }}</span>
            </li>
          </ul>
        </a-card>
      </div>
    </section>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import PageContainer from '../components/PageContainer.vue'
import { getMenuTree, type MenuItem } from '../layouts/menu'
import { NAV_SECTION_LABELS, type NavSectionKey } from '../layouts/navigation'
import { routeNavMeta } from '../layouts/navigation'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const keyword = ref('')

type FlatPage = { key: string; label: string; desc?: string; path?: string; aliases?: string[] }
type FlatModule = FlatPage & { pages: FlatPage[] }
type Group = { section: NavSectionKey; label: string; modules: FlatModule[] }

// 全量菜单（不聚焦），作为功能地图数据源
const fullMenu = computed<MenuItem[]>(() =>
  getMenuTree(userStore.roles || [], userStore.pagePermissions || [], { focused: false })
)

function flattenChildren(items: MenuItem[] = []): FlatPage[] {
  return items.flatMap((item) => {
    const self: FlatPage[] = item.path
      ? [{ key: item.key, label: item.label, desc: item.desc, path: item.path, aliases: item.aliases }]
      : []
    return [...self, ...flattenChildren(item.children)]
  })
}

function sectionOf(item: MenuItem): NavSectionKey {
  const path = item.path || `/${item.key}`
  return (routeNavMeta[path]?.section as NavSectionKey) || 'support'
}

const groups = computed<Group[]>(() => {
  const bySection = new Map<NavSectionKey, FlatModule[]>()
  ;(Object.keys(NAV_SECTION_LABELS) as NavSectionKey[]).forEach((key) => bySection.set(key, []))

  fullMenu.value.forEach((mod) => {
    const module: FlatModule = {
      key: mod.key,
      label: mod.label,
      desc: mod.desc,
      path: mod.path,
      aliases: mod.aliases,
      pages: flattenChildren(mod.children)
    }
    bySection.get(sectionOf(mod))?.push(module)
  })

  return (Object.keys(NAV_SECTION_LABELS) as NavSectionKey[])
    .map((section) => ({ section, label: NAV_SECTION_LABELS[section], modules: bySection.get(section) || [] }))
    .filter((g) => g.modules.length)
})

function matchPage(page: FlatPage, kw: string) {
  return `${page.label} ${page.desc || ''} ${(page.aliases || []).join(' ')}`.toLowerCase().includes(kw)
}

const visibleGroups = computed<Group[]>(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return groups.value
  return groups.value
    .map((g) => ({
      ...g,
      modules: g.modules
        .map((mod) => {
          const moduleHit = matchPage(mod, kw)
          const pages = moduleHit ? mod.pages : mod.pages.filter((p) => matchPage(p, kw))
          return { ...mod, pages }
        })
        .filter((mod) => matchPage(mod, kw) || mod.pages.length)
    }))
    .filter((g) => g.modules.length)
})

function go(path?: string) {
  if (path) router.push(path)
}
</script>

<style scoped>
.fmap-group {
  margin-bottom: 24px;
}

.fmap-group__title {
  font-size: 13px;
  font-weight: 600;
  color: var(--muted);
  margin: 4px 0 12px;
  letter-spacing: 0.5px;
}

.fmap-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.fmap-module__head {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding-bottom: 10px;
  margin-bottom: 8px;
  border-bottom: 1px solid var(--border-soft);
}

.fmap-module__head.is-link {
  cursor: pointer;
}

.fmap-module__head strong {
  font-size: 15px;
  color: var(--ink);
}

.fmap-module__desc {
  font-size: 12px;
  color: var(--muted);
  line-height: 1.5;
}

.fmap-pages {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.fmap-page {
  display: flex;
  align-items: baseline;
  gap: 8px;
  padding: 6px 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s ease;
}

.fmap-page:hover {
  background: var(--primary-soft);
}

.fmap-page__name {
  font-size: 13px;
  color: var(--ink-soft);
  white-space: nowrap;
}

.fmap-page__desc {
  font-size: 12px;
  color: var(--muted-2);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
