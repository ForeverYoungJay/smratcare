import { describe, expect, it } from 'vitest';
import { buildThresholdPreviewRows } from './dashboardThresholdPreview';
describe('dashboardThresholdPreview', () => {
    it('marks trigger state with threshold rules', () => {
        const rows = buildThresholdPreviewRows({
            abnormalCount: 4,
            inventoryCount: 3,
            bedOccupancyRate: 96.2,
            revenueGrowthRate: -8.2
        }, {
            abnormalTaskThreshold: 3,
            inventoryAlertThreshold: 10,
            bedOccupancyThreshold: 95,
            revenueDropThreshold: 5
        });
        expect(rows.map((item) => item.triggered)).toEqual([true, false, true, true]);
    });
    it('sanitizes invalid numeric values', () => {
        const rows = buildThresholdPreviewRows({
            abnormalCount: Number.NaN,
            inventoryCount: Number.POSITIVE_INFINITY,
            bedOccupancyRate: Number.NaN,
            revenueGrowthRate: Number.NaN
        }, {
            abnormalTaskThreshold: 0,
            inventoryAlertThreshold: 0,
            bedOccupancyThreshold: 0,
            revenueDropThreshold: 0
        });
        expect(rows).toEqual([
            {
                key: 'abnormal',
                title: '护理异常任务',
                currentText: '0 条',
                thresholdText: '>= 1 条',
                triggered: false
            },
            {
                key: 'inventory',
                title: '库存预警',
                currentText: '0 项',
                thresholdText: '>= 1 项',
                triggered: false
            },
            {
                key: 'bed',
                title: '床位高占用',
                currentText: '0.00%',
                thresholdText: '>= 1%',
                triggered: false
            },
            {
                key: 'revenue',
                title: '收入环比下滑',
                currentText: '0.00%',
                thresholdText: '<= -1%',
                triggered: false
            }
        ]);
    });
});
