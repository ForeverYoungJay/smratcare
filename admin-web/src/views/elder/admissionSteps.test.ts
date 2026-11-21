import { describe, expect, it } from 'vitest'
import { assetsFromRooms, completionPercent, isAssessmentDone, isBedAvailable, isContractValid, missingForConfirm, resolveAdmissionSteps } from './admissionSteps'

const none = { identityReady: false, assessmentDone: false, contractValid: false, feeConfirmed: false, bedReady: false, admitted: false }

describe('admissionSteps', () => {
  it('marks the first unfinished step as current and keeps later finished steps done', () => {
    const steps = resolveAdmissionSteps({ ...none, identityReady: true, contractValid: true })
    expect(steps.map((step) => step.state)).toEqual(['done', 'current', 'done', 'pending', 'pending', 'pending'])
    expect(completionPercent({ ...none, identityReady: true, contractValid: true })).toBe(33)
  })

  it('treats an admitted resident as fully done', () => {
    expect(resolveAdmissionSteps({ ...none, admitted: true }).every((step) => step.state === 'done')).toBe(true)
    expect(completionPercent({ ...none, admitted: true })).toBe(100)
  })

  it('lists every missing gate before confirmation', () => {
    expect(missingForConfirm({ ...none, assessmentDone: true, bedReady: true })).toEqual(['入住登记', '合同签订', '费用办理'])
    expect(missingForConfirm({ ...none, identityReady: true, assessmentDone: true, contractValid: true, feeConfirmed: true, bedReady: true })).toEqual([])
  })

  it('accepts only non-void contracts past the assessment stage', () => {
    expect(isContractValid(null)).toBe(false)
    expect(isContractValid({ flowStage: 'PENDING_ASSESSMENT' })).toBe(false)
    expect(isContractValid({ flowStage: 'PENDING_BED_SELECT', status: 'VOID' })).toBe(false)
    expect(isContractValid({ flowStage: 'PENDING_BED_SELECT', status: 'PENDING_APPROVAL' })).toBe(true)
    expect(isAssessmentDone([{ status: 'DRAFT' }], 'PENDING_ASSESSMENT')).toBe(false)
    expect(isAssessmentDone([{ status: 'COMPLETED' }], 'PENDING_ASSESSMENT')).toBe(true)
    expect(isAssessmentDone([], 'PENDING_SIGN')).toBe(true)
  })

  it('allows only free beds or the bed already held by this resident', () => {
    expect(isBedAvailable(undefined)).toBe(false)
    expect(isBedAvailable({ status: 1 })).toBe(true)
    expect(isBedAvailable({ status: 3 })).toBe(false)
    expect(isBedAvailable({ elderId: 7, status: 2 }, '7')).toBe(true)
    expect(isBedAvailable({ elderId: 8, status: 2 }, '7')).toBe(false)
  })
})

describe('assetsFromRooms', () => {
  it('derives unique buildings and floors from rooms', () => {
    const { buildings, floors } = assetsFromRooms([
      { id: 'r1', buildingId: 'b1', building: '松鹤楼', floorId: 'f2', floorNo: '2F' },
      { id: 'r2', buildingId: 'b1', building: '松鹤楼', floorId: 'f2', floorNo: '2F' },
      { id: 'r3', buildingId: 'b2', building: '康宁楼', floorId: 'f1', floorNo: '1F' },
      { id: 'r4' }
    ] as never)
    expect(buildings).toEqual([{ id: 'b1', name: '松鹤楼' }, { id: 'b2', name: '康宁楼' }])
    expect(floors).toEqual([{ id: 'f2', buildingId: 'b1', floorNo: '2F' }, { id: 'f1', buildingId: 'b2', floorNo: '1F' }])
  })
})
