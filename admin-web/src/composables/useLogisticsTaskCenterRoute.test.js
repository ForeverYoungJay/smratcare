import { describe, expect, it } from 'vitest';
import { firstTaskCenterQueryValue, flattenTaskCenterRouteQuery, normalizeTaskCenterDensityMode, normalizeTaskCenterLifecycleFocus, normalizeTaskCenterOverdueOnly, normalizeTaskCenterTab, normalizeTaskCenterViewMode, summaryQuerySignature } from './useLogisticsTaskCenterRoute';
describe('useLogisticsTaskCenterRoute helpers', () => {
    it('parses first query value safely', () => {
        expect(firstTaskCenterQueryValue(['maintenance', 'delivery'])).toBe('maintenance');
        expect(firstTaskCenterQueryValue(undefined)).toBe('');
        expect(firstTaskCenterQueryValue('  duty  ')).toBe('duty');
    });
    it('flattens route query and drops empty values', () => {
        const result = flattenTaskCenterRouteQuery({
            tab: 'cleaning',
            density: ['dense'],
            empty: '',
            skip: undefined
        });
        expect(result).toEqual({
            tab: 'cleaning',
            density: 'dense'
        });
    });
    it('normalizes tab/view/density values with fallback', () => {
        expect(normalizeTaskCenterTab('delivery')).toBe('delivery');
        expect(normalizeTaskCenterTab('unknown')).toBe('cleaning');
        expect(normalizeTaskCenterViewMode('duty')).toBe('DUTY');
        expect(normalizeTaskCenterViewMode('x')).toBe('ALL');
        expect(normalizeTaskCenterDensityMode('dense')).toBe('dense');
        expect(normalizeTaskCenterDensityMode('x')).toBe('normal');
        expect(normalizeTaskCenterLifecycleFocus('maintenance')).toBe('maintenance');
        expect(normalizeTaskCenterLifecycleFocus('abc')).toBe('');
        expect(normalizeTaskCenterOverdueOnly('1')).toBe(true);
        expect(normalizeTaskCenterOverdueOnly('true')).toBe(true);
        expect(normalizeTaskCenterOverdueOnly('0')).toBe(false);
    });
    it('builds stable signature for summary query', () => {
        expect(summaryQuerySignature({})).toBe('');
        expect(summaryQuerySignature({ windowDays: 14, overdueDays: 1 })).toBe('14-30-1-30');
    });
});
