<template>
  <PageContainer title="菜单预览" subTitle="当前用户可见菜单树">
    <a-card class="card-elevated" :bordered="false">
      <a-row :gutter="[12, 12]">
        <a-col :xs="12" :lg="6">
          <a-statistic title="角色数" :value="roles.length" />
        </a-col>
        <a-col :xs="12" :lg="6">
          <a-statistic title="菜单节点" :value="menuNodeCount" />
        </a-col>
        <a-col :xs="12" :lg="6">
          <a-statistic title="可访问页面" :value="menuLeafCount" />
        </a-col>
        <a-col :xs="12" :lg="6">
          <a-statistic title="当前过滤命中" :value="filteredLeafCount" />
        </a-col>
      </a-row>
      <a-divider />
      <a-descriptions size="small" :column="1">
        <a-descriptions-item label="当前角色">
          <a-space wrap>
            <a-tag v-for="role in roles" :key="role" color="blue">{{ role }}</a-tag>
          </a-space>
        </a-descriptions-item>
        <a-descriptions-item label="关键词过滤">
          <a-input v-model:value="keyword" allow-clear placeholder="输入菜单名或路由片段" style="max-width: 320px" />
        </a-descriptions-item>
      </a-descriptions>
      <pre class="menu-json">{{ filteredMenuJson }}</pre>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import { useUserStore } from '../../stores/user'
import { getMenuTree } from '../../layouts/menu'

const userStore = useUserStore()
const roles = computed(() => userStore.roles || [])
const keyword = ref('')
const menuTree = computed(() => getMenuTree(roles.value))

const menuNodeCount = computed(() => {
  const walk = (items: any[]): number =>
    (items || []).reduce((sum, item) => sum + 1 + walk(item.children || []), 0)
  return walk(menuTree.value || [])
})

const menuLeafCount = computed(() => {
  const walk = (items: any[]): number =>
    (items || []).reduce((sum, item) => {
      if (!item.children || !item.children.length) return sum + 1
      return sum + walk(item.children)
    }, 0)
  return walk(menuTree.value || [])
})

const filteredTree = computed(() => {
  const text = keyword.value.trim().toLowerCase()
  if (!text) return menuTree.value
  const walk = (items: any[]): any[] =>
    (items || [])
      .map((item) => {
        const children = walk(item.children || [])
        const selfHit = String(item.label || '').toLowerCase().includes(text)
          || String(item.path || '').toLowerCase().includes(text)
        if (selfHit || children.length) {
          return { ...item, children }
        }
        return null
      })
      .filter(Boolean)
  return walk(menuTree.value || [])
})

const filteredLeafCount = computed(() => {
  const walk = (items: any[]): number =>
    (items || []).reduce((sum, item) => {
      if (!item.children || !item.children.length) return sum + 1
      return sum + walk(item.children)
    }, 0)
  return walk(filteredTree.value || [])
})

const filteredMenuJson = computed(() => JSON.stringify(filteredTree.value, null, 2))
</script>

<style scoped>
.menu-json {
  margin-top: 12px;
  background: #0f172a;
  color: #e2e8f0;
  padding: 16px;
  border-radius: 10px;
  font-size: 12px;
  line-height: 1.5;
  overflow: auto;
}
</style>
