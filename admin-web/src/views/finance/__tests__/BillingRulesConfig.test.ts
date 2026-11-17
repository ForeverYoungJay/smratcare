import { mount, flushPromises } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import dayjs from 'dayjs'
import BillingRulesConfig from '../BillingRulesConfig.vue'

const api = vi.hoisted(() => ({ getFinanceBillingConfig: vi.fn(), upsertFinanceBillingConfig: vi.fn(),
  batchUpsertFinanceBillingConfig: vi.fn(), getFinanceConfigImpactPreview: vi.fn() }))
const confirm = vi.hoisted(() => vi.fn().mockResolvedValue(true))
const warning = vi.hoisted(() => vi.fn())
vi.mock('../../../api/finance', () => api)
vi.mock('../../../utils/actionConfirm', () => ({ confirmAction: confirm }))
vi.mock('../../../stores/user', () => ({ useUserStore: () => ({ roles: ['FINANCE_MINISTER'] }) }))
vi.mock('vue-router', () => ({ useRouter: () => ({ push: vi.fn() }) }))
vi.mock('ant-design-vue', () => ({ message: { warning, success: vi.fn(), info: vi.fn() } }))

function mountPage() {
  return mount(BillingRulesConfig, { global: { stubs: {
    PageContainer: { template: '<div><slot /></div>' }, QueryBar: { template: '<div><slot /><slot name="actions" /></div>' },
    ActionToolbar: { template: '<div><slot /></div>' }, CareLevelFeeEditor: true,
    'a-card': { template: '<div><slot /></div>' }, 'a-modal': true,
    'vxe-table': true, 'vxe-column': true,
    ...Object.fromEntries(['date-picker', 'form-item', 'input', 'button', 'space', 'col', 'row', 'alert', 'tag', 'select', 'checkbox', 'input-number', 'switch', 'form'].map(name => [`a-${name}`, true]))
  } } })
}
describe('business billing rules', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    api.getFinanceBillingConfig.mockResolvedValue([{ configKey: 'BED_FEE_MONTHLY', configValue: 2000, effectiveMonth: '2026-11', status: 0 }])
    api.getFinanceConfigImpactPreview.mockResolvedValue({ activeBillCount: 2, activeElderCount: 1, impactedItems: [], riskTips: [] })
    confirm.mockResolvedValue(true)
  })
  it('keeps a disabled rule disabled and previews the month being saved', async () => {
    const wrapper = mountPage(); await flushPromises()
    const vm = wrapper.vm as any
    vm.openEdit(vm.rows[0])
    vm.form.effectiveMonth = dayjs('2027-01')
    await vm.submitEdit()
    expect(api.getFinanceConfigImpactPreview).toHaveBeenCalledWith({ month: '2027-01', configKeys: ['BED_FEE_MONTHLY'] })
    expect(api.upsertFinanceBillingConfig).toHaveBeenCalledWith(expect.objectContaining({ status: 0, effectiveMonth: '2027-01', configKey: 'BED_FEE_MONTHLY' }))
    expect(vm.query.month.format('YYYY-MM')).toBe('2027-01')
    wrapper.unmount()
  })
  it('rejects an empty or negative standard before confirming a save', async () => {
    const wrapper = mountPage(); await flushPromises()
    const vm = wrapper.vm as any
    vm.openEdit(vm.rows[0])
    for (const value of [null, -1, Number.NaN]) { vm.form.configValue = value; await vm.submitEdit() }
    expect(api.upsertFinanceBillingConfig).not.toHaveBeenCalled()
    expect(confirm).not.toHaveBeenCalled()
    expect(warning).toHaveBeenCalled()
    wrapper.unmount()
  })
  it('searches Chinese project names and only adds missing engine-supported defaults', async () => {
    const wrapper = mountPage(); await flushPromises()
    const vm = wrapper.vm as any
    vm.query.keyword = '床位'
    expect(vm.filteredRows).toHaveLength(1)
    await vm.applyTemplate('STANDARD')
    const items = api.batchUpsertFinanceBillingConfig.mock.calls[0][0].items
    expect(items.map((item: { configKey: string }) => item.configKey)).toEqual(['MEAL_FEE_MONTHLY', 'MEAL_FEE_PER_DAY'])
    expect(items.some((item: { configKey: string }) => item.configKey.startsWith('BILL_'))).toBe(false)
    wrapper.unmount()
  })
})
