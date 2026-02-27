import { describe, it, expect, vi, beforeEach } from 'vitest';
import { defineComponent } from 'vue';
import { mount } from '@vue/test-utils';
import DischargeSettlement from '../DischargeSettlement.vue';
const getDischargeSettlementPage = vi.fn().mockResolvedValue({ list: [], total: 0, pageNo: 1, pageSize: 10 });
vi.mock('../../../api/financeFee', () => ({
    getDischargeSettlementPage,
    createDischargeSettlement: vi.fn().mockResolvedValue(null),
    confirmDischargeSettlement: vi.fn().mockResolvedValue(null)
}));
vi.mock('../../../api/elder', () => ({
    getElderPage: vi.fn().mockResolvedValue({ list: [], total: 0, pageNo: 1, pageSize: 20 })
}));
vi.mock('../../../api/baseConfig', () => ({
    getBaseConfigItemList: vi.fn().mockResolvedValue([])
}));
vi.mock('ant-design-vue', () => ({
    message: { success: vi.fn(), error: vi.fn(), warning: vi.fn() }
}));
const SearchFormStub = defineComponent({
    emits: ['search', 'reset'],
    template: `
    <div>
      <slot />
      <button class="search-btn" @click="$emit('search')">search</button>
      <button class="reset-btn" @click="$emit('reset')">reset</button>
      <slot name="extra" />
    </div>
  `
});
const InputStub = defineComponent({
    props: { value: { type: String, default: '' } },
    emits: ['update:value'],
    template: `<input class="text-input" :value="value" @input="$emit('update:value', $event.target.value)" />`
});
const ButtonStub = defineComponent({
    emits: ['click'],
    template: `<button class="a-button" @click="$emit('click')"><slot /></button>`
});
const globalStubs = {
    PageContainer: { template: '<div><slot /></div>' },
    SearchForm: SearchFormStub,
    DataTable: { template: '<div />' },
    'a-form': { template: '<form><slot /></form>' },
    'a-form-item': { template: '<div><slot /></div>' },
    'a-input': InputStub,
    'a-select': { template: '<select><slot /></select>' },
    'a-select-option': { template: '<option><slot /></option>' },
    'a-input-number': { template: '<input />' },
    'a-textarea': { template: '<textarea />' },
    'a-button': ButtonStub,
    'a-modal': { template: '<div><slot /></div>' },
    'a-tag': { template: '<span><slot /></span>' }
};
const waitTick = async () => {
    await Promise.resolve();
    await Promise.resolve();
};
describe('DischargeSettlement', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        getDischargeSettlementPage.mockResolvedValue({ list: [], total: 0, pageNo: 1, pageSize: 10 });
    });
    it('passes keyword to backend search api', async () => {
        const wrapper = mount(DischargeSettlement, {
            global: { stubs: globalStubs }
        });
        await waitTick();
        expect(getDischargeSettlementPage).toHaveBeenCalledTimes(1);
        expect(getDischargeSettlementPage).toHaveBeenLastCalledWith(expect.objectContaining({ keyword: undefined }));
        await wrapper.find('input.text-input').setValue('张三');
        await wrapper.find('button.search-btn').trigger('click');
        await waitTick();
        expect(getDischargeSettlementPage).toHaveBeenCalledTimes(2);
        expect(getDischargeSettlementPage).toHaveBeenLastCalledWith(expect.objectContaining({ keyword: '张三' }));
    });
});
