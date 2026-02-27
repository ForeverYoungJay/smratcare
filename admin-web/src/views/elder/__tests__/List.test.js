import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import List from '../List.vue';
vi.mock('../../../api/elder', () => ({
    getElderPage: vi.fn().mockResolvedValue({ list: [], total: 0, pageNo: 1, pageSize: 10 }),
    assignBed: vi.fn().mockResolvedValue(null),
    unbindBed: vi.fn().mockResolvedValue(null),
    bindFamily: vi.fn().mockResolvedValue(null)
}));
vi.mock('../../../api/bed', () => ({
    getBedList: vi.fn().mockResolvedValue([])
}));
vi.mock('qrcode', () => ({
    default: { toDataURL: vi.fn().mockResolvedValue('data:image/png;base64,') },
    toDataURL: vi.fn().mockResolvedValue('data:image/png;base64,')
}));
vi.mock('ant-design-vue', () => ({
    message: { success: vi.fn(), error: vi.fn(), warning: vi.fn() },
    Modal: { confirm: vi.fn() }
}));
const globalStubs = {
    PageContainer: { template: '<div><slot /></div>' },
    'a-card': { template: '<div><slot /></div>' },
    'a-form': { template: '<form><slot /></form>' },
    'a-form-item': { template: '<div><slot /></div>' },
    'a-input': { template: '<input />' },
    'a-select': { template: '<select><slot /></select>' },
    'a-select-option': { template: '<option><slot /></option>' },
    'a-space': { template: '<div><slot /></div>' },
    'a-button': { template: '<button><slot /></button>' },
    'a-table': { template: '<table><slot /></table>' },
    'a-pagination': { template: '<div />' },
    'a-modal': { template: '<div><slot /></div>' },
    'a-date-picker': { template: '<input />' },
    'a-input-number': { template: '<input />' },
    'a-switch': { template: '<input />' },
    'a-tag': { template: '<span><slot /></span>' }
};
describe('Elder List', () => {
    it('renders and loads data', async () => {
        const wrapper = mount(List, {
            global: {
                stubs: globalStubs
            }
        });
        expect(wrapper.exists()).toBe(true);
    });
});
