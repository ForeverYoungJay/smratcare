import { describe, expect, it } from 'vitest';
import { MEDICAL_WORKBENCH_QUERY_DEFAULTS, buildMedicalWorkbenchRouteQuery, normalizeMedicalWorkbenchQuery, parseMedicalWorkbenchQueryPatch } from './medicalWorkbenchQuery';
describe('medicalWorkbenchQuery utils', () => {
    it('normalizes and clamps numeric values', () => {
        const query = normalizeMedicalWorkbenchQuery({
            incidentWindowDays: 0,
            overdueHours: 999,
            topResidentLimit: 2.4,
            riskResidentLookbackDays: 5
        });
        expect(query).toEqual({
            incidentWindowDays: 1,
            overdueHours: 72,
            topResidentLimit: 2,
            riskResidentLookbackDays: 7
        });
    });
    it('parses route patch with date and status', () => {
        const patch = parseMedicalWorkbenchQueryPatch({
            date: '2026-03-09',
            status: 'pending',
            incidentWindowDays: '14',
            overdueHours: 'abc',
            topResidentLimit: '8',
            riskResidentLookbackDays: '365',
            elderId: '12'
        });
        expect(patch).toEqual({
            date: '2026-03-09',
            status: 'PENDING',
            incidentWindowDays: 14,
            topResidentLimit: 8,
            riskResidentLookbackDays: 365,
            elderId: '12'
        });
    });
    it('builds route query payload with extras', () => {
        const payload = buildMedicalWorkbenchRouteQuery({
            date: '2026-03-09',
            incidentWindowDays: 30,
            overdueHours: 6,
            topResidentLimit: 10,
            riskResidentLookbackDays: 120
        }, { mode: 'duty' });
        expect(payload).toEqual({
            date: '2026-03-09',
            incidentWindowDays: '30',
            overdueHours: '6',
            topResidentLimit: '10',
            riskResidentLookbackDays: '120',
            mode: 'duty'
        });
    });
    it('uses numeric defaults when empty', () => {
        expect(normalizeMedicalWorkbenchQuery()).toEqual(MEDICAL_WORKBENCH_QUERY_DEFAULTS);
    });
});
