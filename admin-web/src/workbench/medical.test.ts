import { beforeEach, describe, expect, it, vi } from 'vitest'
import { resolveWorkbenchProfile } from './model'

vi.mock('../api/health', () => ({ getHealthMedicationRegistrationPage: vi.fn() }))
vi.mock('../api/medicalCare', () => ({ getMedicalCareWorkbenchSummary: vi.fn(), getMedicalUnifiedTaskPage: vi.fn() }))

import { getHealthMedicationRegistrationPage } from '../api/health'
import { getMedicalCareWorkbenchSummary, getMedicalUnifiedTaskPage } from '../api/medicalCare'
import { buildMedicalDepartmentDetail, loadMedicalWorkbench, medicalModuleLabel, medicalTaskStatusLabel } from './medical'

describe('medical workbench adapter', () => {
  beforeEach(() => vi.clearAllMocks())

  it('does not load department summaries for a medical employee', async () => {
    vi.mocked(getHealthMedicationRegistrationPage).mockResolvedValue({ list: [], total: 0, pageNo: 1, pageSize: 6 })
    const result = await loadMedicalWorkbench(resolveWorkbenchProfile(['MEDICAL_EMPLOYEE']), { date: '2026-08-23', username: 'doctor_a' })
    expect(getHealthMedicationRegistrationPage).toHaveBeenCalledWith(expect.objectContaining({ nurseName: 'doctor_a' }))
    expect(getMedicalCareWorkbenchSummary).not.toHaveBeenCalled()
    expect(getMedicalUnifiedTaskPage).not.toHaveBeenCalled()
    expect(result.detail.scope).toBe('SELF_SAFE')
    expect(result.detail.personalTaskDataAvailable).toBe(false)
  })

  it('requires a stable employee identity before loading personal records', async () => {
    await expect(loadMedicalWorkbench(resolveWorkbenchProfile(['MEDICAL_EMPLOYEE']), { date: '2026-08-23' })).rejects.toThrow('缺少用户名')
  })

  it('builds the minister queue and risk residents from real department data', () => {
    const detail = buildMedicalDepartmentDetail({ keyResidents: [{ elderId: '9', elderName: '张奶奶', riskLevel: 'HIGH', keyRiskFactors: '血压异常' }] }, [
      { id: 'ORDER_1', module: 'ORDER', residentId: '9', residentName: '张奶奶', taskTitle: '执行医嘱', priority: 'HIGH', status: 'PENDING', overdue: true }
    ])
    expect(detail.scope).toBe('DEPARTMENT')
    expect(detail.tasks[0]).toMatchObject({ moduleLabel: '医嘱用药', overdue: true })
    expect(detail.riskResidents[0]).toMatchObject({ name: '张奶奶', riskLabel: '高风险' })
  })

  it('uses explicit business labels', () => {
    expect(medicalModuleLabel('INSPECTION')).toBe('巡诊巡检')
    expect(medicalTaskStatusLabel('ABNORMAL')).toBe('异常待处理')
  })
})
