import request, { fetchPage } from '../utils/request'
import type {
  HealthDrugDictionary,
  HealthMedicationDeposit,
  HealthMedicationSetting,
  HealthMedicationRegistration,
  HealthMedicationTask,
  HealthMedicationRemainingItem,
  HealthArchive,
  HealthDataRecord,
  HealthInspection,
  HealthNursingLog
} from '../types'

export function getHealthDrugDictionaryPage(params: any) {
  return fetchPage<HealthDrugDictionary>('/api/health/drug-dictionary/page', params)
}

export function createHealthDrugDictionary(data: Partial<HealthDrugDictionary>) {
  return request.post<HealthDrugDictionary>('/api/health/drug-dictionary', data)
}

export function updateHealthDrugDictionary(id: number, data: Partial<HealthDrugDictionary>) {
  return request.put<HealthDrugDictionary>(`/api/health/drug-dictionary/${id}`, data)
}

export function deleteHealthDrugDictionary(id: number) {
  return request.delete<void>(`/api/health/drug-dictionary/${id}`)
}

export function getHealthMedicationDepositPage(params: any) {
  return fetchPage<HealthMedicationDeposit>('/api/health/medication/deposit/page', params)
}

export function createHealthMedicationDeposit(data: Partial<HealthMedicationDeposit>) {
  return request.post<HealthMedicationDeposit>('/api/health/medication/deposit', data)
}

export function updateHealthMedicationDeposit(id: number, data: Partial<HealthMedicationDeposit>) {
  return request.put<HealthMedicationDeposit>(`/api/health/medication/deposit/${id}`, data)
}

export function deleteHealthMedicationDeposit(id: number) {
  return request.delete<void>(`/api/health/medication/deposit/${id}`)
}

export function getHealthMedicationSettingPage(params: any) {
  return fetchPage<HealthMedicationSetting>('/api/health/medication/setting/page', params)
}

export function createHealthMedicationSetting(data: Partial<HealthMedicationSetting>) {
  return request.post<HealthMedicationSetting>('/api/health/medication/setting', data)
}

export function updateHealthMedicationSetting(id: number, data: Partial<HealthMedicationSetting>) {
  return request.put<HealthMedicationSetting>(`/api/health/medication/setting/${id}`, data)
}

export function deleteHealthMedicationSetting(id: number) {
  return request.delete<void>(`/api/health/medication/setting/${id}`)
}

export function getHealthMedicationRegistrationPage(params: any) {
  return fetchPage<HealthMedicationRegistration>('/api/health/medication/registration/page', params)
}

export function createHealthMedicationRegistration(data: Partial<HealthMedicationRegistration>) {
  return request.post<HealthMedicationRegistration>('/api/health/medication/registration', data)
}

export function updateHealthMedicationRegistration(id: number, data: Partial<HealthMedicationRegistration>) {
  return request.put<HealthMedicationRegistration>(`/api/health/medication/registration/${id}`, data)
}

export function deleteHealthMedicationRegistration(id: number) {
  return request.delete<void>(`/api/health/medication/registration/${id}`)
}

export function getHealthMedicationRemainingPage(params: any) {
  return fetchPage<HealthMedicationRemainingItem>('/api/health/medication/remaining/page', params)
}

export function getHealthMedicationTaskPage(params: any) {
  return fetchPage<HealthMedicationTask>('/api/health/medication/task/page', params)
}

export function getHealthArchivePage(params: any) {
  return fetchPage<HealthArchive>('/api/health/archive/page', params)
}

export function createHealthArchive(data: Partial<HealthArchive>) {
  return request.post<HealthArchive>('/api/health/archive', data)
}

export function updateHealthArchive(id: number, data: Partial<HealthArchive>) {
  return request.put<HealthArchive>(`/api/health/archive/${id}`, data)
}

export function deleteHealthArchive(id: number) {
  return request.delete<void>(`/api/health/archive/${id}`)
}

export function getHealthDataRecordPage(params: any) {
  return fetchPage<HealthDataRecord>('/api/health/data/page', params)
}

export function createHealthDataRecord(data: Partial<HealthDataRecord>) {
  return request.post<HealthDataRecord>('/api/health/data', data)
}

export function updateHealthDataRecord(id: number, data: Partial<HealthDataRecord>) {
  return request.put<HealthDataRecord>(`/api/health/data/${id}`, data)
}

export function deleteHealthDataRecord(id: number) {
  return request.delete<void>(`/api/health/data/${id}`)
}

export function getHealthInspectionPage(params: any) {
  return fetchPage<HealthInspection>('/api/health/inspection/page', params)
}

export function createHealthInspection(data: Partial<HealthInspection>) {
  return request.post<HealthInspection>('/api/health/inspection', data)
}

export function updateHealthInspection(id: number, data: Partial<HealthInspection>) {
  return request.put<HealthInspection>(`/api/health/inspection/${id}`, data)
}

export function deleteHealthInspection(id: number) {
  return request.delete<void>(`/api/health/inspection/${id}`)
}

export function getHealthNursingLogPage(params: any) {
  return fetchPage<HealthNursingLog>('/api/health/nursing-log/page', params)
}

export function createHealthNursingLog(data: Partial<HealthNursingLog>) {
  return request.post<HealthNursingLog>('/api/health/nursing-log', data)
}

export function updateHealthNursingLog(id: number, data: Partial<HealthNursingLog>) {
  return request.put<HealthNursingLog>(`/api/health/nursing-log/${id}`, data)
}

export function deleteHealthNursingLog(id: number) {
  return request.delete<void>(`/api/health/nursing-log/${id}`)
}
