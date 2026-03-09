import { mount, flushPromises } from '@vue/test-utils';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import BedPanorama from './BedPanorama.vue';
import { emitLiveSync, inferLiveSyncTopics } from '../../../utils/liveSync';
const getBedMap = vi.fn();
const getRoomList = vi.fn();
vi.mock('../../../api/bed', () => ({
    getBedMap,
    getRoomList
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
describe('BedPanorama live linkage', () => {
    beforeEach(() => {
        vi.useFakeTimers();
        getBedMap.mockResolvedValue([
            { id: 1, roomId: 101, bedNo: '01', roomNo: 'A101', building: 'A栋', floorNo: '1F', status: 1 }
        ]);
        getRoomList.mockResolvedValue([
            { id: 101, roomNo: 'A101', roomType: 'STANDARD', capacity: 2 }
        ]);
    });
    afterEach(() => {
        vi.clearAllMocks();
        vi.useRealTimers();
    });
    it('refreshes panorama when management updates room/bed data', async () => {
        mount(BedPanorama, {
            global: {
                stubs: {
                    PageContainer: { template: '<div><slot /></div>' }
                }
            }
        });
        await flushPromises();
        expect(getBedMap).toHaveBeenCalledTimes(1);
        expect(getRoomList).toHaveBeenCalledTimes(1);
        emitLiveSync({
            topics: inferLiveSyncTopics('/api/room/1001'),
            method: 'PUT',
            url: '/api/room/1001',
            at: Date.now()
        });
        vi.advanceTimersByTime(600);
        await flushPromises();
        expect(getBedMap).toHaveBeenCalledTimes(2);
        expect(getRoomList).toHaveBeenCalledTimes(2);
    });
    it('does not refresh panorama for unrelated live-sync topics', async () => {
        mount(BedPanorama, {
            global: {
                stubs: {
                    PageContainer: { template: '<div><slot /></div>' }
                }
            }
        });
        await flushPromises();
        expect(getBedMap).toHaveBeenCalledTimes(1);
        expect(getRoomList).toHaveBeenCalledTimes(1);
        emitLiveSync({
            topics: ['oa'],
            method: 'PUT',
            url: '/api/oa/todo/1',
            at: Date.now()
        });
        vi.advanceTimersByTime(600);
        await flushPromises();
        expect(getBedMap).toHaveBeenCalledTimes(1);
        expect(getRoomList).toHaveBeenCalledTimes(1);
    });
});
