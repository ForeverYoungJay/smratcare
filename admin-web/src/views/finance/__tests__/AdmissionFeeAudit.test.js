import { beforeEach, describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import AdmissionFeeAudit from '../AdmissionFeeAudit.vue';
const getAdmissionFeeAuditPage = vi.fn().mockResolvedValue({ list: [], total: 0, pageNo: 1, pageSize: 10 });
const reviewAdmissionFeeAudit = vi.fn().mockResolvedValue(null);
vi.mock('../../../api/financeFee', () => ({
    getAdmissionFeeAuditPage,
    createAdmissionFeeAudit: vi.fn().mockResolvedValue(null),
    reviewAdmissionFeeAudit
}));
vi.mock('../../../api/elder', () => ({
    getElderPage: vi.fn().mockResolvedValue({ list: [], total: 0, pageNo: 1, pageSize: 20 })
}));
const message = {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn()
};
const modalConfirm = vi.fn();
vi.mock('ant-design-vue', () => ({
    message,
    Modal: { confirm: modalConfirm },
    Input: { TextArea: { name: 'TextArea' } }
}));
const globalStubs = {
    PageContainer: { template: '<div><slot /></div>' },
    SearchForm: { template: '<div><slot /><slot name="extra" /></div>' },
    DataTable: { template: '<div />' },
    'a-form': { template: '<form><slot /></form>' },
    'a-form-item': { template: '<div><slot /></div>' },
    'a-input': { template: '<input />' },
    'a-select': { template: '<select><slot /></select>' },
    'a-input-number': { template: '<input />' },
    'a-button': { template: '<button><slot /></button>' },
    'a-modal': { template: '<div><slot /></div>' },
    'a-space': { template: '<div><slot /></div>' },
    'a-tag': { template: '<span><slot /></span>' }
};
const waitTick = async () => {
    await Promise.resolve();
    await Promise.resolve();
};
describe('AdmissionFeeAudit', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        getAdmissionFeeAuditPage.mockResolvedValue({ list: [], total: 0, pageNo: 1, pageSize: 10 });
    });
    it('requires review remark when rejecting', async () => {
        const wrapper = mount(AdmissionFeeAudit, { global: { stubs: globalStubs } });
        await waitTick();
        const vm = wrapper.vm;
        vm.review({ id: 1001, status: 'PENDING' }, 'REJECTED');
        const config = modalConfirm.mock.calls[0][0];
        await expect(config.onOk()).rejects.toBeUndefined();
        expect(message.error).toHaveBeenCalledWith('驳回时请填写审核备注');
        expect(reviewAdmissionFeeAudit).not.toHaveBeenCalled();
    });
    it('submits review remark when rejecting with reason', async () => {
        const wrapper = mount(AdmissionFeeAudit, { global: { stubs: globalStubs } });
        await waitTick();
        const vm = wrapper.vm;
        vm.review({ id: 1002, status: 'PENDING' }, 'REJECTED');
        const config = modalConfirm.mock.calls[0][0];
        config.content.props.onChange({ target: { value: '资料不全' } });
        await config.onOk();
        expect(reviewAdmissionFeeAudit).toHaveBeenCalledWith(1002, {
            status: 'REJECTED',
            reviewRemark: '资料不全'
        });
    });
});
