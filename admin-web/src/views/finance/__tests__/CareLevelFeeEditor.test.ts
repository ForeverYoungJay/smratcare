import { mount, flushPromises } from '@vue/test-utils'
import { beforeEach, expect, it, vi } from 'vitest'
import CareLevelFeeEditor from '../components/CareLevelFeeEditor.vue'

const api = vi.hoisted(() => ({ getBillingCareLevelFees: vi.fn(), getBillingFeeHistory: vi.fn(),
  getFinanceConfigImpactPreview: vi.fn(), upsertBillingCareLevelFee: vi.fn() }))
const user = vi.hoisted(() => ({ staffInfo: { orgId: '1' } }))
vi.mock('../../../api/finance', () => api)
vi.mock('../../../api/nursing', () => ({ getCareLevelNameOptions: vi.fn().mockResolvedValue([{ value: 'L2', label: 'L2' }]) }))
vi.mock('../../../stores/user', () => ({ useUserStore: () => user }))
vi.mock('../../../utils/actionConfirm', () => ({ confirmAction: vi.fn().mockResolvedValue(true) }))
vi.mock('ant-design-vue', () => ({ message: { success: vi.fn(), error: vi.fn() } }))
const fee = { id: '4', orgId: '1', careLevel: 'L2', feeMonthly: 1800, effectiveMonth: '2026-11', status: 1 }
function mountEditor() {
  return mount(CareLevelFeeEditor, { props: { month: '2026-11' }, global: { stubs: {
    'a-modal': { template: '<div><slot /></div>' }, 'a-form': { template: '<form><slot /></form>', methods: { validate: () => Promise.resolve(), clearValidate: () => {} } },
    StatefulBlock: true,
    ...Object.fromEntries(['alert', 'select', 'table', 'divider', 'row', 'col', 'form-item', 'input', 'input-number', 'switch', 'card', 'space', 'button', 'date-picker', 'radio-group', 'radio-button', 'descriptions', 'descriptions-item'].map(name => [`a-${name}`, true]))
  } } })
}
beforeEach(() => {
  vi.clearAllMocks(); user.staffInfo.orgId = '1'
  api.getBillingCareLevelFees.mockResolvedValue([fee])
  api.getBillingFeeHistory.mockResolvedValue([{ configKey: 'BED_FEE_MONTHLY', configValue: 1800, effectiveMonth: '2026-01', status: 1 }])
  api.getFinanceConfigImpactPreview.mockResolvedValue({ activeBillCount: 10, activeElderCount: 2 })
})
it('saves zero-valued, disabled care fees through the existing institution-scoped endpoint', async () => {
  const wrapper = mountEditor(); await flushPromises()
  const vm = wrapper.vm as any
  vm.edit(fee); vm.form.status = 0; vm.form.feeMonthly = 0
  await vm.save()
  expect(api.upsertBillingCareLevelFee).toHaveBeenCalledWith(expect.objectContaining({ orgId: '1', careLevel: 'L2', feeMonthly: 0, status: 0, effectiveMonth: '2026-11' }))
  wrapper.unmount()
})
it('does not save if impact preview fails or no institution is available', async () => {
  const wrapper = mountEditor(); await flushPromises()
  const vm = wrapper.vm as any
  vm.edit(fee)
  api.getFinanceConfigImpactPreview.mockRejectedValue(new Error('预览失败'))
  await vm.save()
  expect(api.upsertBillingCareLevelFee).not.toHaveBeenCalled()
  user.staffInfo.orgId = ''
  await vm.load()
  expect(vm.error).toContain('未获取到当前机构')
  await vm.save()
  expect(api.upsertBillingCareLevelFee).not.toHaveBeenCalled()
  wrapper.unmount()
})
