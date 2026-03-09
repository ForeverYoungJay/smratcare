import { describe, expect, it } from 'vitest';
import { LOGISTICS_WORKBENCH_QUERY_DEFAULTS, buildLogisticsWorkbenchRouteQuery, normalizeLogisticsWorkbenchQuery, parseLogisticsWorkbenchQueryPatch } from './logisticsWorkbenchQuery';
describe('logisticsWorkbenchQuery utils', () => {
    it('normalizes and clamps all fields', () => {
        const query = normalizeLogisticsWorkbenchQuery({
            windowDays: 0,
            expiryDays: 9999,
            overdueDays: 2.8,
            maintenanceDueDays: -4
        });
        expect(query).toEqual({
            windowDays: 1,
            expiryDays: 365,
            overdueDays: 3,
            maintenanceDueDays: 1
        });
    });
    it('parses query patch only when values are valid numbers', () => {
        const patch = parseLogisticsWorkbenchQueryPatch({
            mode: 'duty',
            windowDays: '14',
            expiryDays: 'abc',
            overdueDays: '3',
            maintenanceDueDays: '60'
        });
        expect(patch).toEqual({
            windowDays: 14,
            overdueDays: 3,
            maintenanceDueDays: 60
        });
    });
    it('builds route query with extra fields', () => {
        const query = buildLogisticsWorkbenchRouteQuery({
            windowDays: 7,
            expiryDays: 15,
            overdueDays: 1,
            maintenanceDueDays: 20
        }, {
            mode: 'duty',
            tab: 'maintenance'
        });
        expect(query).toEqual({
            windowDays: '7',
            expiryDays: '15',
            overdueDays: '1',
            maintenanceDueDays: '20',
            mode: 'duty',
            tab: 'maintenance'
        });
    });
    it('falls back to defaults when query is empty', () => {
        expect(normalizeLogisticsWorkbenchQuery()).toEqual(LOGISTICS_WORKBENCH_QUERY_DEFAULTS);
    });
});
