import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import MonthlyAllocation from '../MonthlyAllocation.vue'

const createMonthlyAllocation = vi.hoisted(() => vi.fn().mockResolvedValue(null))

vi.mock('../../../api/financeFee', () => ({
  getMonthlyAllocationPage: vi.fn().mockResolvedValue({ list: [], total: 0, pageNo: 1, pageSize: 10 }),
  createMonthlyAllocation
}))

const message = vi.hoisted(() => ({
  success: vi.fn(),
  error: vi.fn(),
  warning: vi.fn()
}))

vi.mock('ant-design-vue', () => ({
  message
}))

const globalStubs = {
  PageContainer: { template: '<div><slot /></div>' },
  SearchForm: { template: '<div><slot /><slot name="extra" /></div>' },
  DataTable: { template: '<div />' },
  'a-form': { template: '<form><slot /></form>' },
  'a-form-item': { template: '<div><slot /></div>' },
  'a-date-picker': { template: '<input />' },
  'a-input': { template: '<input />' },
  'a-input-number': { template: '<input />' },
  'a-select': { template: '<select><slot /></select>' },
  'a-button': { template: '<button><slot /></button>' },
  'a-modal': { template: '<div><slot /></div>' }
}

const waitTick = async () => {
  await Promise.resolve()
  await Promise.resolve()
}

describe('MonthlyAllocation', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('calculates avg amount with two decimals', async () => {
    const wrapper = mount(MonthlyAllocation, { global: { stubs: globalStubs } })
    await waitTick()

    const vm = wrapper.vm as any
    expect(vm.resolveAvgAmount({ totalAmount: 100, targetCount: 3 })).toBe('33.33')
    expect(vm.resolveAvgAmount({ totalAmount: 100, targetCount: 0 })).toBe('--')
    expect(vm.resolveAvgAmount({ avgAmount: 12.345 })).toBe('12.35')
  })

  it('blocks create when no elder selected', async () => {
    const wrapper = mount(MonthlyAllocation, { global: { stubs: globalStubs } })
    await waitTick()

    const vm = wrapper.vm as any
    vm.createForm.allocationMonth = '2026-02'
    vm.createForm.allocationName = '保洁费'
    vm.createForm.totalAmount = 99
    vm.createForm.elderIds = []
    await vm.submitCreate()

    expect(message.error).toHaveBeenCalledWith('请至少选择一位分摊老人')
    expect(createMonthlyAllocation).not.toHaveBeenCalled()
  })
})

