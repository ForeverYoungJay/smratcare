<template>
  <PageContainer title="菜单预览" subTitle="当前用户可见菜单树">
    <a-card class="card-elevated" :bordered="false">
      <a-descriptions size="small" :column="1">
        <a-descriptions-item label="当前角色">
          <a-space wrap>
            <a-tag v-for="role in roles" :key="role" color="blue">{{ role }}</a-tag>
          </a-space>
        </a-descriptions-item>
      </a-descriptions>
      <pre class="menu-json">{{ menuJson }}</pre>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import { useUserStore } from '../../stores/user'
import { getMenuTree } from '../../layouts/menu'

const userStore = useUserStore()
const roles = computed(() => userStore.roles || [])
const menuJson = computed(() => JSON.stringify(getMenuTree(roles.value), null, 2))
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
