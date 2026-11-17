import { describe, expect, it } from 'vitest'
import { canConvertTrial, intentLevelText, resolveAdmissionTab, toConvertedTrialRequest, trialStatusColor, trialStatusText } from './admissionTrial'

describe('admissionTrial', () => {
  it('resolves the tab from route query', () => {
    expect(resolveAdmissionTab('trial')).toBe('trial')
    expect(resolveAdmissionTab(['trial'])).toBe('trial')
    expect(resolveAdmissionTab(undefined)).toBe('admission')
    expect(resolveAdmissionTab('other')).toBe('admission')
  })

  it('only converts registered or finished trials', () => {
    expect(canConvertTrial('REGISTERED')).toBe(true)
    expect(canConvertTrial('FINISHED')).toBe(true)
    expect(canConvertTrial('CONVERTED')).toBe(false)
    expect(canConvertTrial('CANCELLED')).toBe(false)
    expect(canConvertTrial(undefined)).toBe(false)
  })

  it('maps labels with -- fallback and legacy intent text', () => {
    expect(trialStatusText('CONVERTED')).toBe('已转签约')
    expect(trialStatusText()).toBe('--')
    expect(trialStatusColor('CANCELLED')).toBe('red')
    expect(trialStatusColor('FINISHED')).toBe('default')
    expect(intentLevelText('HIGH')).toBe('高')
    expect(intentLevelText('很高')).toBe('很高')
  })

  it('keeps the record and only flips status when converting', () => {
    const req = toConvertedTrialRequest({ id: '1', elderId: '9', trialStartDate: '2026-11-01', trialEndDate: '2026-11-07', status: 'REGISTERED', remark: '家属满意' })
    expect(req).toMatchObject({ elderId: '9', trialStartDate: '2026-11-01', trialEndDate: '2026-11-07', status: 'CONVERTED', remark: '家属满意', intentLevel: 'MEDIUM' })
  })
})
