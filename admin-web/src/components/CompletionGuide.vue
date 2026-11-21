<template>
  <section v-if="guide" class="completion-guide" role="status" aria-live="polite">
    <div><strong>{{ guide.title }}</strong><span>可以继续办理下一步，或留在当前页面。</span></div>
    <a-space wrap>
      <a-button v-for="action in allowedActions" :key="action.path" @click="navigate(action.path)">{{ action.label }}</a-button>
      <a-button type="text" aria-label="关闭办理结果" @click="guide = null">关闭</a-button>
    </a-space>
  </section>
</template>
<script setup lang="ts">
import { computed, inject } from 'vue'
import { useRouter } from 'vue-router'
import { completionGuideKey } from '../composables/useCompletionGuide'
import { useUserStore } from '../stores/user'
import { resolveRouteAccess } from '../utils/routeAccess'
const guide = inject(completionGuideKey)!
const router = useRouter()
const user = useUserStore()
const allowedActions = computed(() => (guide.value?.actions || []).filter(action => {
  const route = router.resolve(action.path)
  return route.matched.length && !route.matched.some(record => record.name === 'NotFound') &&
    resolveRouteAccess(router, user.roles || [], action.path, user.pagePermissions || [], user.permissions || []).canAccess
}))
function navigate(path: string) {
  if (allowedActions.value.some(action => action.path === path)) void router.push(path)
}
</script>
<style scoped>
.completion-guide { display:flex;align-items:center;justify-content:space-between;gap:12px;flex-wrap:wrap;padding:12px 16px;margin-bottom:12px;border:1px solid var(--border);border-left:3px solid var(--primary);border-radius:8px;background:var(--surface); }
.completion-guide strong,.completion-guide span { display:block; }
.completion-guide span { margin-top:4px;font-size:13px;color:var(--muted); }
</style>
