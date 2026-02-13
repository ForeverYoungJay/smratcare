<template>
  <div class="portal-page">
    <div class="portal-hero card-elevated">
      <div>
        <div class="hero-title">信息门户</div>
        <div class="hero-subtitle">一站式入口，快速进入各业务模块</div>
      </div>
      <div class="hero-meta">
        <div class="meta-item">
          <div class="meta-label">当前登录</div>
          <div class="meta-value">{{ userStore.staffInfo?.realName || '管理员' }}</div>
        </div>
        <div class="meta-item">
          <div class="meta-label">角色</div>
          <div class="meta-value">{{ roleLabel }}</div>
        </div>
      </div>
    </div>

    <div v-for="group in menuGroups" :key="group.key" class="portal-group">
      <div class="group-title">{{ group.label }}</div>
      <div class="group-grid">
        <div
          v-for="item in group.items"
          :key="item.path"
          class="portal-tile"
          :style="{ background: item.color }"
          @click="go(item.path)"
        >
          <div class="tile-icon">{{ item.abbr }}</div>
          <div class="tile-title">{{ item.label }}</div>
          <div class="tile-desc">{{ group.label }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { getMenuTree } from '../layouts/menu'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const palette = [
  '#2b5db8',
  '#7a3db8',
  '#1f7dcf',
  '#e4572e',
  '#118a7e',
  '#6d4af5',
  '#f59e0b',
  '#4c6ef5',
  '#0ea5e9',
  '#ef4444',
  '#10b981',
  '#6366f1'
]

const roleLabel = computed(() => {
  const roles = userStore.roles || []
  if (roles.length === 0) return '普通用户'
  return roles.join(' / ')
})

const menuGroups = computed(() => {
  const roles = userStore.roles || []
  const tree = getMenuTree(roles)
  let colorIndex = 0
  const rootLeaves = tree
    .filter((node) => !node.children && node.path)
    .map((node) => {
      const color = palette[colorIndex % palette.length]
      colorIndex += 1
      return {
        label: node.label,
        path: node.path as string,
        color,
        abbr: String(node.label).slice(0, 1)
      }
    })

  const groups = tree
    .filter((group) => group.children && group.children.length > 0)
    .map((group) => ({
      key: group.key,
      label: group.label,
      items: (group.children || [])
        .filter((child) => !!child.path)
        .map((child) => {
          const color = palette[colorIndex % palette.length]
          colorIndex += 1
          return {
            label: child.label,
            path: child.path as string,
            color,
            abbr: String(child.label).slice(0, 1)
          }
        })
    }))

  if (rootLeaves.length > 0) {
    groups.unshift({
      key: 'quick',
      label: '常用入口',
      items: rootLeaves
    })
  }

  return groups
})

function go(path: string) {
  router.push(path)
}
</script>

<style scoped>
.portal-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.portal-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
}

.hero-title {
  font-size: 20px;
  font-weight: 700;
}

.hero-subtitle {
  margin-top: 6px;
  color: var(--muted);
  font-size: 13px;
}

.hero-meta {
  display: flex;
  gap: 24px;
}

.meta-item {
  text-align: right;
}

.meta-label {
  font-size: 12px;
  color: var(--muted);
}

.meta-value {
  font-size: 14px;
  font-weight: 600;
}

.portal-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.group-title {
  font-weight: 600;
  font-size: 15px;
  color: #1f2937;
}

.group-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
}

.portal-tile {
  color: #fff;
  padding: 16px;
  border-radius: 12px;
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
  min-height: 110px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.portal-tile:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.tile-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.2);
  display: grid;
  place-items: center;
  font-weight: 700;
  font-size: 18px;
}

.tile-title {
  margin-top: 10px;
  font-size: 14px;
  font-weight: 600;
}

.tile-desc {
  font-size: 12px;
  opacity: 0.8;
}

@media (max-width: 768px) {
  .portal-hero {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .hero-meta {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
