import { expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { ref, reactive } from 'vue'
import { createMemoryHistory, createRouter } from 'vue-router'
import CompletionGuide from './CompletionGuide.vue'
import { completionGuideKey } from '../composables/useCompletionGuide'

const user = reactive({ roles: ['HR_MINISTER'], pagePermissions: ['/hr'], permissions: [] })
vi.mock('../stores/user', () => ({ useUserStore: () => user }))
it('shows only authorized next steps and responds to revoked access', async () => {
  const router = createRouter({ history: createMemoryHistory(), routes: [
    { path: '/', component: {} }, { path: '/hr/profile/contracts', component: {}, meta: { roles: ['HR_MINISTER'] } },
    { path: '/system/role', component: {}, meta: { roles: ['SYS_ADMIN'] } }
  ] })
  await router.push('/')
  const guide = ref({ title: '员工档案已保存', actions: [{ label: '维护合同', path: '/hr/profile/contracts' }, { label: '维护权限', path: '/system/role' }] })
  const wrapper = mount(CompletionGuide, { global: { plugins: [router], provide: { [completionGuideKey as symbol]: guide },
    stubs: { ASpace: { template: '<div><slot /></div>' }, AButton: { template: '<button><slot /></button>' } } } })
  expect(wrapper.text()).toContain('维护合同')
  expect(wrapper.text()).not.toContain('维护权限')
  user.pagePermissions = ['/workbench']
  await wrapper.vm.$nextTick()
  expect(wrapper.text()).not.toContain('维护合同')
  await wrapper.find('button').trigger('click')
  expect(guide.value).toBeNull()
  wrapper.unmount()
})
