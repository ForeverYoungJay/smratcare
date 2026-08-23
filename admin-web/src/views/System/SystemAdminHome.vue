<template>
  <PageContainer
    title="系统管理首页"
    subTitle="集中管理角色权限、组织信息和系统安全配置，不承载护理、医务、收费等日常业务操作"
    mode="showcase"
    kicker="系统治理"
  >
    <template #meta>
      <a-space wrap>
        <StatusTag text="系统管理员视图" tone="pending" />
        <StatusTag text="配置与业务分离" tone="normal" />
      </a-space>
    </template>

    <section class="system-command">
      <div>
        <span>ACCESS · ORGANIZATION · SECURITY</span>
        <h2>先确认授权边界，再维护组织与安全策略。</h2>
        <p>这里只呈现当前账号真正有权进入的系统能力。未授权页面不会出现在入口中，直接访问仍由统一路由守卫拦截。</p>
      </div>
      <StatusTag :text="`${visibleActions.length} 个授权入口`" tone="normal" />
    </section>

    <section class="system-action-grid" aria-label="系统管理快捷入口">
      <button v-for="action in visibleActions" :key="action.path" type="button" @click="open(action.path)">
        <span aria-hidden="true">{{ action.code }}</span>
        <strong>{{ action.title }}</strong>
        <p>{{ action.description }}</p>
        <em>进入配置 →</em>
      </button>
    </section>

    <div class="system-columns">
      <WorkbenchModuleCard title="建议管理顺序" eyebrow="配置流程">
        <ol class="system-steps">
          <li><strong>01</strong><div><b>角色与权限</b><span>先确认岗位可见页面与操作范围，避免越权或漏权。</span></div></li>
          <li><strong>02</strong><div><b>组织与部门</b><span>再维护机构、部门和人员归属，确保业务数据边界准确。</span></div></li>
          <li><strong>03</strong><div><b>安全策略与审计</b><span>最后复核敏感访问、导出和安全规则，保留可追溯记录。</span></div></li>
        </ol>
      </WorkbenchModuleCard>

      <WorkbenchModuleCard title="使用边界" eyebrow="安全提醒">
        <a-alert
          type="warning"
          show-icon
          message="系统管理员不处理部门日常业务"
          description="护理、医务、财务、后勤、人事和市场的执行入口不会在本页展示；需要业务协作时应使用对应业务岗位账号。"
        />
        <div class="system-boundary">
          <span>菜单与搜索</span><strong>仅显示授权页面</strong>
          <span>直接访问</span><strong>无权限进入 403</strong>
          <span>旧地址</span><strong>继续按兼容映射跳转</strong>
        </div>
      </WorkbenchModuleCard>
    </div>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatusTag from '../../components/smartcare/StatusTag.vue'
import WorkbenchModuleCard from '../../components/smartcare/WorkbenchModuleCard.vue'
import { useUserStore } from '../../stores/user'
import { resolveRouteAccess } from '../../utils/routeAccess'

const router = useRouter()
const userStore = useUserStore()
const actions = [
  { code: 'RBAC', title: '角色与权限', description: '维护角色页面范围和操作授权。', path: '/system/role' },
  { code: 'DEPT', title: '部门管理', description: '维护部门结构与岗位归属。', path: '/system/department' },
  { code: 'ORG', title: '机构信息', description: '核对机构基础信息和主体资料。', path: '/system/org-info' },
  { code: 'SAFE', title: '安全策略', description: '配置访问规则和敏感操作保护。', path: '/stats/security-policy' },
  { code: 'AUDIT', title: '敏感访问审计', description: '检查敏感数据访问和异常记录。', path: '/stats/sensitive-access-audit' },
  { code: 'EXPORT', title: '导出审计', description: '追踪数据导出及责任账号。', path: '/stats/export-audit' }
]

function canAccess(path: string) {
  return resolveRouteAccess(router, userStore.roles || [], path, userStore.pagePermissions || [], userStore.permissions || []).canAccess
}

const visibleActions = computed(() => actions.filter((action) => canAccess(action.path)))

function open(path: string) {
  if (!canAccess(path)) {
    message.warning('当前账号暂无该配置权限')
    return
  }
  router.push(path)
}
</script>

<style scoped>
.system-command { display: flex; align-items: flex-start; justify-content: space-between; gap: 24px; padding: 24px; border: 1px solid #cfd9df; border-left: 6px solid #315b70; background: #f2f6f8; }
.system-command span { color: #60717a; font-size: 12px; font-weight: 700; letter-spacing: .1em; }
.system-command h2 { margin: 7px 0; color: #253a45; font-size: 22px; }
.system-command p { max-width: 760px; margin: 0; color: #5e6c73; font-size: 14px; line-height: 1.7; }
.system-action-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-top: 16px; }
.system-action-grid button { display: grid; min-height: 156px; padding: 18px; text-align: left; border: 1px solid #d8e0e4; border-top: 4px solid #315b70; border-radius: 4px; background: #fff; cursor: pointer; }
.system-action-grid button:hover { border-color: #315b70; background: #f7f9fa; }
.system-action-grid button:focus-visible { outline: 3px solid rgba(49, 91, 112, .28); outline-offset: 2px; }
.system-action-grid span { color: #73848c; font-size: 12px; font-weight: 700; letter-spacing: .08em; }
.system-action-grid strong { margin-top: 10px; color: #273a44; font-size: 18px; }
.system-action-grid p { margin: 7px 0; color: #65737a; font-size: 14px; line-height: 1.6; }
.system-action-grid em { align-self: end; color: #315b70; font-size: 13px; font-style: normal; font-weight: 600; }
.system-columns { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-top: 16px; }
.system-steps { display: grid; gap: 10px; margin: 0; padding: 0; list-style: none; }
.system-steps li { display: grid; grid-template-columns: 48px 1fr; gap: 10px; padding: 13px; border: 1px solid #e0e5e7; background: #fafbfb; }
.system-steps li > strong { color: #315b70; font-size: 20px; }
.system-steps b, .system-steps span { display: block; }
.system-steps b { color: #2d3d45; font-size: 14px; }
.system-steps span { margin-top: 4px; color: #69767c; font-size: 14px; line-height: 1.5; }
.system-boundary { display: grid; grid-template-columns: 110px 1fr; gap: 10px 14px; margin-top: 18px; padding: 16px; border: 1px solid #e0e5e7; background: #fafbfb; font-size: 14px; }
.system-boundary span { color: #728087; }
.system-boundary strong { color: #34474f; }
@media (max-width: 1000px) { .system-action-grid { grid-template-columns: repeat(2, 1fr); } .system-columns { grid-template-columns: 1fr; } }
@media (max-width: 680px) { .system-command { display: grid; } .system-action-grid { grid-template-columns: 1fr; } }
</style>
