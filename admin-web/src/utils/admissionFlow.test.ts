import { describe, expect, it } from 'vitest'
import { resolveAdmissionFlowSteps } from './admissionFlow'

describe('resolveAdmissionFlowSteps', () => {
  it('marks the contract node as current while waiting for signature', () => {
    expect(resolveAdmissionFlowSteps('PENDING_SIGN').map((item) => item.state))
      .toEqual(['done', 'done', 'done', 'current', 'pending'])
  })

  it('marks every node complete after admission', () => {
    expect(resolveAdmissionFlowSteps('SIGNED', true).every((item) => item.state === 'done')).toBe(true)
  })
})
