import { mount, flushPromises } from '@vue/test-utils'
import { reactive } from 'vue'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import TaskBoard from './TaskBoard.vue'

const api = vi.hoisted(() => ({ getTaskPage: vi.fn() }))
const route = reactive({ query: { date: '2026-11-16', residentId: '9007199254740993' } as Record<string, string> })
const push = vi.hoisted(() => vi.fn())
const access = vi.hoisted(() => ({ allowed: true }))
vi.mock('../../../api/care', () => api)
vi.mock('vue-router', () => ({ useRoute: () => route, useRouter: () => ({ push, replace: vi.fn() }) }))
vi.mock('ant-design-vue', () => ({ message: { error: vi.fn() } }))
vi.mock('../../../stores/user', () => ({ useUserStore: () => ({ roles: [], pagePermissions: [], permissions: [] }) }))
vi.mock('../../../utils/routeAccess', () => ({ resolveRouteAccess: () => ({ canAccess: access.allowed }) }))
const task = (id: number) => ({ taskDailyId: id, elderId: '9007199254740993', elderName: '张阿姨', roomNo: '101',
  taskName: '照护', status: 'PENDING', planTime: '2026-11-16 08:00' })
function mountPage() {
  return mount(TaskBoard, { global: { stubs: {
    PageContainer: { template: '<div><slot /><slot name="extra" /></div>' },
    StatefulBlock: { template: '<div><slot /></div>' }, 'a-card': { template: '<div><slot /></div>' }, 'a-table': true,
    ...Object.fromEntries(['date-picker', 'button', 'space', 'statistic', 'col', 'row', 'alert', 'radio-button', 'radio-group', 'tag'].map(name => [`a-${name}`, true]))
  } } })
}
describe('complete care task board', () => {
  beforeEach(() => { vi.clearAllMocks(); access.allowed = true; route.query = { date: '2026-11-16', residentId: '9007199254740993' } })
  it('loads every page and carries resident, date and task identity to execution', async () => {
    api.getTaskPage.mockImplementation(({ pageNo }) => Promise.resolve({
      list: pageNo === 1 ? Array.from({ length: 200 }, (_, i) => task(i + 1)) : [task(201)], total: 201
    }))
    const wrapper = mountPage(); await flushPromises()
    const vm = wrapper.vm as any
    expect(api.getTaskPage).toHaveBeenCalledTimes(2)
    expect(vm.rows).toHaveLength(201)
    vm.goTask(vm.rows[200])
    expect(push).toHaveBeenCalledWith({ path: '/care/today', query: {
      residentId: '9007199254740993', elderId: '9007199254740993', taskId: '201', date: '2026-11-16'
    } })
    route.query.contractNo = 'CT-1'
    vm.goTask(vm.rows[200])
    expect(push).toHaveBeenLastCalledWith(expect.objectContaining({ query: expect.objectContaining({ contractNo: 'CT-1' }) }))
    vm.goTask({ ...vm.rows[200], elderId: '42' })
    expect(push.mock.calls[push.mock.calls.length - 1]?.[0].query.contractNo).toBeUndefined()
    wrapper.unmount()
  })
  it('updates the date from deep links and clears stale data on failure', async () => {
    api.getTaskPage.mockResolvedValue({ list: [task(1)], total: 1 })
    const wrapper = mountPage(); await flushPromises()
    api.getTaskPage.mockRejectedValue(new Error('连接失败'))
    route.query.date = '2026-11-17'
    await flushPromises()
    const vm = wrapper.vm as any
    expect(api.getTaskPage).toHaveBeenLastCalledWith(expect.objectContaining({ date: '2026-11-17' }))
    expect(vm.rows).toEqual([])
    expect(vm.errorMessage).toBe('连接失败')
    wrapper.unmount()
  })
  it('does not publish a partial board if a later page is missing', async () => {
    api.getTaskPage.mockResolvedValueOnce({ list: [task(1)], total: 2 }).mockResolvedValueOnce({ list: [], total: 2 })
    const wrapper = mountPage(); await flushPromises()
    const vm = wrapper.vm as any
    expect(vm.rows).toEqual([])
    expect(vm.errorMessage).toContain('任务数据不完整')
    wrapper.unmount()
  })
  it('does not navigate to task execution after access is revoked', async () => {
    api.getTaskPage.mockResolvedValue({ list: [task(1)], total: 1 })
    const wrapper = mountPage(); await flushPromises()
    access.allowed = false
    const vm = wrapper.vm as any
    vm.goTask(task(1))
    expect(push).not.toHaveBeenCalled()
    wrapper.unmount()
  })
})
