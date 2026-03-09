import { mount, flushPromises } from '@vue/test-utils';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import BedMap from './Map.vue';
import { emitLiveSync, inferLiveSyncTopics } from '../../utils/liveSync';
const getBedMap = vi.fn();
const getBaseConfigItemList = vi.fn();
vi.mock('../../api/bed', () => ({
    getBedMap
}));
vi.mock('../../api/baseConfig', () => ({
    getBaseConfigItemList
}));
vi.mock('../../api/elder', () => ({
    getElderDetail: vi.fn()
}));
vi.mock('vue-router', () => ({
    useRouter: () => ({
        push: vi.fn()
    })
}));
vi.mock('ant-design-vue', () => ({
    message: {
        warning: vi.fn(),
        success: vi.fn()
    }
}));
describe('BedMap live linkage', () => {
    beforeEach(() => {
        vi.useFakeTimers();
        getBedMap.mockResolvedValue([
            { id: 1, roomId: 101, bedNo: '01', roomNo: 'A101', building: 'A栋', floorNo: '1F', status: 1 }
        ]);
        getBaseConfigItemList.mockResolvedValue([]);
    });
    afterEach(() => {
        vi.clearAllMocks();
        vi.useRealTimers();
    });
    it('refreshes bed map when room data changes in management', async () => {
        mount(BedMap, {
            global: {
                stubs: {
                    PageContainer: { template: '<div><slot /></div>' }
                }
            }
        });
        await flushPromises();
        expect(getBedMap).toHaveBeenCalledTimes(1);
        expect(getBaseConfigItemList).toHaveBeenCalledTimes(3);
        emitLiveSync({
            topics: inferLiveSyncTopics('/api/room/1001'),
            method: 'PUT',
            url: '/api/room/1001',
            at: Date.now()
        });
        vi.advanceTimersByTime(600);
        await flushPromises();
        expect(getBedMap).toHaveBeenCalledTimes(2);
        expect(getBaseConfigItemList).toHaveBeenCalledTimes(6);
    });
    it('ignores unrelated live-sync topics', async () => {
        mount(BedMap, {
            global: {
                stubs: {
                    PageContainer: { template: '<div><slot /></div>' }
                }
            }
        });
        await flushPromises();
        expect(getBedMap).toHaveBeenCalledTimes(1);
        emitLiveSync({
            topics: ['oa'],
            method: 'PUT',
            url: '/api/oa/todo/1',
            at: Date.now()
        });
        vi.advanceTimersByTime(600);
        await flushPromises();
        expect(getBedMap).toHaveBeenCalledTimes(1);
    });
});
