import { describe, expect, it } from 'vitest';
import { runTaskBatch } from './useLogisticsTaskCenterBatch';
describe('useLogisticsTaskCenterBatch', () => {
    it('aggregates success, failure and receipt correctly', async () => {
        const rows = [
            { id: 1, fail: false },
            { id: 2, fail: true },
            { id: 3, fail: false }
        ];
        const steps = [];
        let tick = 0;
        const result = await runTaskBatch({
            action: '批量测试',
            rows,
            getItemId: (row) => row.id,
            execute: async (row) => {
                if (row.fail) {
                    throw new Error('failed');
                }
            },
            parseErrorDetail: () => ({
                reason: 'failed',
                code: 'E500',
                path: '/mock/path',
                retryable: true
            }),
            onStep: (ok) => {
                steps.push(ok);
            },
            now: () => `T${++tick}`
        });
        expect(steps).toEqual([true, false, true]);
        expect(result.successIds).toEqual([1, 3]);
        expect(result.failedIds).toEqual([2]);
        expect(result.failures).toEqual([
            {
                at: 'T3',
                action: '批量测试',
                itemId: 2,
                reason: 'failed',
                code: 'E500',
                path: '/mock/path',
                retryable: true
            }
        ]);
        expect(result.receipt).toEqual({
            action: '批量测试',
            startAt: 'T1',
            finishAt: 'T5',
            total: 3,
            success: 2,
            failed: 1
        });
    });
});
