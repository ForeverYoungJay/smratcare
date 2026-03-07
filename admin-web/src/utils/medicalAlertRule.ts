import { getMedicalAlertRuleConfig } from '../api/medicalCare'
import type { MedicalAlertRuleConfig } from '../types'

export const MEDICAL_ALERT_RULE_STORAGE_KEY = 'medical_alert_rules_v1'

export const defaultMedicalAlertRules: MedicalAlertRuleConfig = {
  medicationHighDosageThreshold: 10,
  overdueHoursThreshold: 12,
  abnormalInspectionRequirePhoto: 1,
  handoverAutoFillConfirmTime: 1,
  autoCreateNursingLogFromInspection: 1,
  autoRaiseTaskFromAbnormal: 1,
  autoCarryResidentContext: 1,
  handoverRiskKeywords: '异常,风险,事故,上报'
}

let syncingPromise: Promise<MedicalAlertRuleConfig> | null = null

export function normalizeMedicalAlertRules(value: any): MedicalAlertRuleConfig {
  const parsed = value || {}
  const highDosage = Number(parsed.medicationHighDosageThreshold)
  const overdueHours = Number(parsed.overdueHoursThreshold)
  return {
    medicationHighDosageThreshold: Number.isFinite(highDosage) && highDosage > 0 ? highDosage : defaultMedicalAlertRules.medicationHighDosageThreshold,
    overdueHoursThreshold: Number.isFinite(overdueHours) && overdueHours > 0 ? overdueHours : defaultMedicalAlertRules.overdueHoursThreshold,
    abnormalInspectionRequirePhoto: Number(parsed.abnormalInspectionRequirePhoto) === 0 ? 0 : 1,
    handoverAutoFillConfirmTime: Number(parsed.handoverAutoFillConfirmTime) === 0 ? 0 : 1,
    autoCreateNursingLogFromInspection: Number(parsed.autoCreateNursingLogFromInspection) === 0 ? 0 : 1,
    autoRaiseTaskFromAbnormal: Number(parsed.autoRaiseTaskFromAbnormal) === 0 ? 0 : 1,
    autoCarryResidentContext: Number(parsed.autoCarryResidentContext) === 0 ? 0 : 1,
    handoverRiskKeywords: String(parsed.handoverRiskKeywords || defaultMedicalAlertRules.handoverRiskKeywords)
  }
}

export function readMedicalAlertRulesFromStorage() {
  try {
    const text = localStorage.getItem(MEDICAL_ALERT_RULE_STORAGE_KEY)
    if (!text) {
      return { ...defaultMedicalAlertRules }
    }
    return normalizeMedicalAlertRules(JSON.parse(text))
  } catch (_error) {
    return { ...defaultMedicalAlertRules }
  }
}

export function getMedicationHighDosageThreshold() {
  return readMedicalAlertRulesFromStorage().medicationHighDosageThreshold || defaultMedicalAlertRules.medicationHighDosageThreshold
}

export function isAbnormalInspectionPhotoRequired() {
  return Number(readMedicalAlertRulesFromStorage().abnormalInspectionRequirePhoto) !== 0
}

export function isAutoCarryResidentContextEnabled() {
  return Number(readMedicalAlertRulesFromStorage().autoCarryResidentContext) !== 0
}

export function getHandoverRiskKeywords() {
  const raw = readMedicalAlertRulesFromStorage().handoverRiskKeywords || defaultMedicalAlertRules.handoverRiskKeywords
  const list = String(raw)
    .split(/[，,]/g)
    .map((item) => item.trim())
    .filter((item) => !!item)
  return list.length ? list : String(defaultMedicalAlertRules.handoverRiskKeywords).split(/[，,]/g)
}

export function writeMedicalAlertRulesToStorage(value: Partial<MedicalAlertRuleConfig>) {
  const merged = normalizeMedicalAlertRules({ ...readMedicalAlertRulesFromStorage(), ...(value || {}) })
  localStorage.setItem(MEDICAL_ALERT_RULE_STORAGE_KEY, JSON.stringify(merged))
  return merged
}

export async function syncMedicalAlertRules(force = false) {
  if (!force && syncingPromise) {
    return syncingPromise
  }
  syncingPromise = getMedicalAlertRuleConfig()
    .then((data) => writeMedicalAlertRulesToStorage(data || defaultMedicalAlertRules))
    .catch(() => readMedicalAlertRulesFromStorage())
    .finally(() => {
      syncingPromise = null
    })
  return syncingPromise
}
