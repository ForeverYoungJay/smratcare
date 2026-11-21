import { describe, expect, it } from 'vitest'
import { resolveAdmissionFlowSteps, withAdmissionContext } from './admissionFlow'

describe('resolveAdmissionFlowSteps', () => {
  it('marks the contract node as current while waiting for signature', () => {
    expect(resolveAdmissionFlowSteps('PENDING_SIGN').map((item) => item.state))
      .toEqual(['done', 'done', 'done', 'current', 'pending'])
  })

  it('marks every node complete after admission', () => {
    expect(resolveAdmissionFlowSteps('SIGNED', true).every((item) => item.state === 'done')).toBe(true)
  })

  it('preserves the target view and safely carries resident, contract and bed identity', () => {
    const path = withAdmissionContext('/marketing/contract-signing?openAttachment=1&view=signing', {
      elderId: '9007199254740993', contractNo: '合同 A&B/#1', bedId: '200', leadId: '300'
    })
    const params = new URLSearchParams(path.split('?')[1])
    expect(params.get('view')).toBe('signing')
    expect(params.get('openAttachment')).toBe('1')
    expect(params.get('elderId')).toBe('9007199254740993')
    expect(params.get('residentId')).toBe('9007199254740993')
    expect(params.get('contractNo')).toBe('合同 A&B/#1')
    expect(params.get('bedId')).toBe('200')
  })

  it('does not append empty identity fields', () => {
    expect(withAdmissionContext('/elder/admission', {})).toBe('/elder/admission')
    expect(withAdmissionContext('/elder/admission', { elderId: ' ', contractNo: ' ' })).toBe('/elder/admission')
  })
})
