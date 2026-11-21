import { describe, expect, it } from 'vitest'
import { legacyResidentRedirect } from './residentRedirects'

describe('legacyResidentRedirect', () => {
  it('sends outing records to the bed page records view with create intent', () => {
    expect(legacyResidentRedirect('outing', { elderId: '7', openCreate: '1' })).toEqual({
      path: '/elder/bed-panorama',
      query: { view: 'records', recordType: 'OUTING', elderId: '7', action: 'outing' }
    })
    expect(legacyResidentRedirect('medical-outing', {})).toEqual({
      path: '/elder/bed-panorama',
      query: { view: 'records', recordType: 'MEDICAL_OUTING' }
    })
  })

  it('folds discharge apply and death register into the discharge tab', () => {
    expect(legacyResidentRedirect('discharge-apply', { residentId: '9', openCreate: '1' })).toEqual({
      path: '/elder/status-change/discharge-settlement',
      query: { tab: 'discharge', elderId: '9', create: '1' }
    })
    expect(legacyResidentRedirect('death-register', {})).toEqual({
      path: '/elder/status-change/discharge-settlement',
      query: { tab: 'discharge', departure: 'DEATH' }
    })
  })

  it('opens contracts on the elder detail when an elder is known', () => {
    expect(legacyResidentRedirect('contracts-invoices', { docElderId: '3' })).toEqual({ path: '/elder/detail/3', query: { tab: 'contract' } })
    expect(legacyResidentRedirect('contracts-invoices', {})).toEqual({ path: '/elder/status-change/discharge-settlement', query: { tab: 'contract' } })
  })

  it('maps trial stay and the old overview', () => {
    expect(legacyResidentRedirect('trial-stay', { elderId: '5' })).toEqual({ path: '/elder/admission-processing', query: { tab: 'trial', elderId: '5' } })
    expect(legacyResidentRedirect('center', { residentId: '5' })).toEqual({ path: '/elder/bed-panorama', query: { elderId: '5' } })
  })
})
