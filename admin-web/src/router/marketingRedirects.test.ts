import { describe, expect, it } from 'vitest'
import { legacyMarketingRedirect } from './marketingRedirects'

describe('legacyMarketingRedirect', () => {
  it('maps old lead and follow-up pages to customer tabs', () => {
    expect(legacyMarketingRedirect('leads/all')).toEqual({ path: '/marketing/customers', query: {} })
    expect(legacyMarketingRedirect('interactions/overdue')).toEqual({ path: '/marketing/customers', query: { tab: 'followup', view: 'overdue' } })
    expect(legacyMarketingRedirect('leads/blacklist')).toEqual({ path: '/marketing/customers', query: { tab: 'invalid', scenario: 'blacklist' } })
  })

  it('keeps filters from the old address but lets the mapped view win', () => {
    expect(legacyMarketingRedirect('reservation/expiring', { elderName: '张', tab: 'x' })).toEqual({
      path: '/marketing/visits', query: { elderName: '张', tab: 'reservation', scenario: 'expiring_lock' }
    })
  })

  it('sends the plan page to the operations plans tab', () => {
    expect(legacyMarketingRedirect('plan')).toEqual({ path: '/marketing/operations', query: { tab: 'plans' } })
  })
})
