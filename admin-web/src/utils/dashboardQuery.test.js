import { describe, expect, it } from 'vitest';
import { buildDashboardShareQuery, buildThresholdQuery, buildWindowValue, parseDashboardRouteFilters, parseThresholdQuery, toThresholdSnapshot } from './dashboardQuery';
describe('dashboardQuery utils', () => {
    it('clamps and rounds threshold query values', () => {
        const parsed = parseThresholdQuery({
            abnormalTaskThreshold: '0',
            inventoryAlertThreshold: '100000',
            bedOccupancyThreshold: '88.2',
            revenueDropThreshold: '-5'
        });
        expect(parsed).toEqual({
            abnormalTaskThreshold: 1,
            inventoryAlertThreshold: 9999,
            bedOccupancyThreshold: 88,
            revenueDropThreshold: 1
        });
    });
    it('parses dashboard filters from window query', () => {
        const filters = parseDashboardRouteFilters({
            metricVersion: 'v2026.03',
            window: '2025-10_2026-03'
        });
        expect(filters).toEqual({
            metricVersion: 'v2026.03',
            from: '2025-10',
            to: '2026-03'
        });
    });
    it('uses explicit from/to filters over window query', () => {
        const filters = parseDashboardRouteFilters({
            from: '2025-11',
            to: '2026-02',
            window: '2025-01_2025-12'
        });
        expect(filters).toEqual({
            from: '2025-11',
            to: '2026-02'
        });
    });
    it('builds sanitized threshold query payload', () => {
        const query = buildThresholdQuery({
            abnormalTaskThreshold: 8.9,
            inventoryAlertThreshold: 12,
            bedOccupancyThreshold: 101,
            revenueDropThreshold: 0
        });
        expect(query).toEqual({
            abnormalTaskThreshold: '9',
            inventoryAlertThreshold: '12',
            bedOccupancyThreshold: '100',
            revenueDropThreshold: '1'
        });
    });
    it('builds dashboard share query with window and metric version', () => {
        const query = buildDashboardShareQuery({
            metricVersion: '  m-v1  ',
            from: '2025-09',
            to: '2026-02',
            threshold: toThresholdSnapshot({
                abnormalTaskThreshold: 6,
                inventoryAlertThreshold: 9,
                bedOccupancyThreshold: 94,
                revenueDropThreshold: 4
            })
        });
        expect(query).toEqual({
            metricVersion: 'm-v1',
            abnormalTaskThreshold: '6',
            inventoryAlertThreshold: '9',
            bedOccupancyThreshold: '94',
            revenueDropThreshold: '4',
            window: '2025-09_2026-02'
        });
    });
    it('returns empty window when month text is invalid', () => {
        expect(buildWindowValue('2025-13', '2026-03')).toBe('');
        expect(buildWindowValue('2025-12', '2026/03')).toBe('');
    });
});
