import { beforeEach, describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import MonthlyAllocation from '../MonthlyAllocation.vue';
const createMonthlyAllocation = vi.fn().mockResolvedValue(null);
vi.mock('../../../api/financeFee', () => ({
    getMonthlyAllocationPage: vi.fn().mockResolvedValue({ list: [], total: 0, pageNo: 1, pageSize: 10 }),
    createMonthlyAllocation
}));
const message = {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn()
};
vi.mock('ant-design-vue', () => ({
    message
}));
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
};
const waitTick = async () => {
    await Promise.resolve();
    await Promise.resolve();
};
describe('MonthlyAllocation', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });
    it('calculates avg amount with two decimals', async () => {
        const wrapper = mount(MonthlyAllocation, { global: { stubs: globalStubs } });
        await waitTick();
        const vm = wrapper.vm;
        expect(vm.calcAvgAmount(100, 3)).toBe('33.33');
        expect(vm.calcAvgAmount(100, 0)).toBe('--');
    });
    it('blocks create when amount positive and targetCount zero', async () => {
        const wrapper = mount(MonthlyAllocation, { global: { stubs: globalStubs } });
        await waitTick();
        const vm = wrapper.vm;
        vm.createForm.allocationMonth = '2026-02';
        vm.createForm.allocationName = '保洁费';
        vm.createForm.totalAmount = 99;
        vm.createForm.targetCount = 0;
        await vm.submitCreate();
        expect(message.error).toHaveBeenCalledWith('总金额大于0时，目标人数必须大于0');
        expect(createMonthlyAllocation).not.toHaveBeenCalled();
    });
});
