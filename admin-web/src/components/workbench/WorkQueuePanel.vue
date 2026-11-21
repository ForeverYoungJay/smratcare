<template>
  <section class="work-queue" :aria-label="title">
    <h2>{{ title }}</h2>
    <div class="work-queue__list">
      <button v-for="item in visibleItems" :key="item.path" type="button" @click="open(item.path)">
        <span><strong>{{ item.label }}</strong><small v-if="item.hint">{{ item.hint }}</small></span>
        <span class="work-queue__value">{{ item.value == null ? '查看' : item.value }} <span aria-hidden="true">›</span></span>
      </button>
      <a-empty v-if="!visibleItems.length" description="当前账号暂无可办理事项" />
    </div>
  </section>
</template>
<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { resolveRouteAccess } from '../../utils/routeAccess'
const props = withDefaults(defineProps<{ title?: string; items: Array<{ label: string; path: string; hint?: string; value?: number | string }> }>(), { title: '优先办理' })
const router = useRouter()
const user = useUserStore()
const allowed = (path: string) => resolveRouteAccess(router, user.roles, path, user.pagePermissions, user.permissions).canAccess
const visibleItems = computed(() => props.items.filter((item) => allowed(item.path)))
function open(path: string) { if (allowed(path)) router.push(path) }
</script>
<style scoped>
.work-queue { margin-top: 18px; }
.work-queue h2 { margin: 0 0 12px; color: var(--text-primary, #213b2e); font-size: 17px; }
.work-queue__list { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; }
.work-queue button { display: flex; align-items: center; justify-content: space-between; gap: 14px; min-height: 76px; padding: 14px 18px; border: 1px solid var(--border); border-radius: 10px; background: var(--surface, #fff); text-align: left; cursor: pointer; }
.work-queue button:hover { border-color: var(--primary, #27785c); background: var(--surface-hover, #f4f8f5); }
.work-queue button:focus-visible { outline: 3px solid var(--primary, #27785c); outline-offset: 2px; }
.work-queue strong { display: block; font-size: 15px; color: var(--text-primary, #213b2e); }
.work-queue small { display: block; margin-top: 4px; color: var(--text-secondary, #61756b); font-size: 13px; }
.work-queue__value { flex-shrink: 0; color: var(--primary, #27785c); font-size: 17px; font-weight: 600; }
@media (max-width: 760px) { .work-queue__list { grid-template-columns: 1fr; } }
</style>
