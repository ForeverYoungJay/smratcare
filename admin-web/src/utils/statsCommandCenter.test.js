import { beforeEach, describe, expect, it, vi } from 'vitest';
import { listStatsFeedback, listStatsReportTemplates, loadStatsPanelConfig, saveStatsFeedback, saveStatsPanelConfig, saveStatsReportTemplate } from './statsCommandCenter';
describe('statsCommandCenter utils', () => {
    beforeEach(() => {
        localStorage.clear();
        vi.useFakeTimers();
        vi.setSystemTime(new Date('2026-03-15T10:00:00'));
    });
    it('saves and restores report templates', () => {
        saveStatsReportTemplate('consumption', {
            name: '财务复核版',
            summary: '近6个月 + Top10',
            columns: ['elderName', 'amount'],
            payload: { from: '2026-01', to: '2026-06' }
        });
        const rows = listStatsReportTemplates('consumption');
        expect(rows).toHaveLength(1);
        expect(rows[0].columns).toEqual(['elderName', 'amount']);
    });
    it('saves and restores panel config', () => {
        saveStatsPanelConfig('dashboard', ['total_revenue', 'bed_occupancy_rate']);
        expect(loadStatsPanelConfig('dashboard')).toEqual(['total_revenue', 'bed_occupancy_rate']);
    });
    it('saves feedback records', () => {
        saveStatsFeedback('elder-info', {
            title: '高龄占比异常',
            detail: '某机构高龄占比明显偏低，请复核老人年龄结构。',
            urgency: 'HIGH',
            scope: '老人画像统计协同包'
        });
        const rows = listStatsFeedback('elder-info');
        expect(rows).toHaveLength(1);
        expect(rows[0].status).toBe('OPEN');
        expect(rows[0].urgency).toBe('HIGH');
    });
});
