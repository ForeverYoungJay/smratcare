import { describe, expect, it } from 'vitest'
import { pickDischargeStep } from './dischargeSteps'

describe('discharge flow step', () => {
  it('opens the requested step and falls back to settlement', () => {
    expect(pickDischargeStep('review')).toBe('review')
    expect(pickDischargeStep('sync')).toBe('sync')
    expect(pickDischargeStep(undefined)).toBe('settle')
    expect(pickDischargeStep('nope')).toBe('settle')
  })
})
