import request, { fetchPage } from '../utils/request';
export function getHealthDrugDictionaryPage(params) {
    return fetchPage('/api/health/drug-dictionary/page', params);
}
export function createHealthDrugDictionary(data) {
    return request.post('/api/health/drug-dictionary', data);
}
export function updateHealthDrugDictionary(id, data) {
    return request.put(`/api/health/drug-dictionary/${id}`, data);
}
export function deleteHealthDrugDictionary(id) {
    return request.delete(`/api/health/drug-dictionary/${id}`);
}
export function getHealthMedicationDepositPage(params) {
    return fetchPage('/api/health/medication/deposit/page', params);
}
export function createHealthMedicationDeposit(data) {
    return request.post('/api/health/medication/deposit', data);
}
export function updateHealthMedicationDeposit(id, data) {
    return request.put(`/api/health/medication/deposit/${id}`, data);
}
export function deleteHealthMedicationDeposit(id) {
    return request.delete(`/api/health/medication/deposit/${id}`);
}
export function getHealthMedicationSettingPage(params) {
    return fetchPage('/api/health/medication/setting/page', params);
}
export function createHealthMedicationSetting(data) {
    return request.post('/api/health/medication/setting', data);
}
export function updateHealthMedicationSetting(id, data) {
    return request.put(`/api/health/medication/setting/${id}`, data);
}
export function deleteHealthMedicationSetting(id) {
    return request.delete(`/api/health/medication/setting/${id}`);
}
export function getHealthMedicationRegistrationPage(params) {
    return fetchPage('/api/health/medication/registration/page', params);
}
export function getHealthMedicationRegistrationSummary(params) {
    return request.get('/api/health/medication/registration/summary', { params });
}
export function createHealthMedicationRegistration(data) {
    return request.post('/api/health/medication/registration', data);
}
export function updateHealthMedicationRegistration(id, data) {
    return request.put(`/api/health/medication/registration/${id}`, data);
}
export function deleteHealthMedicationRegistration(id) {
    return request.delete(`/api/health/medication/registration/${id}`);
}
export function getHealthMedicationRemainingPage(params) {
    return fetchPage('/api/health/medication/remaining/page', params);
}
export function getHealthMedicationTaskPage(params) {
    return fetchPage('/api/health/medication/task/page', params);
}
export function getHealthArchivePage(params) {
    return fetchPage('/api/health/archive/page', params);
}
export function createHealthArchive(data) {
    return request.post('/api/health/archive', data);
}
export function updateHealthArchive(id, data) {
    return request.put(`/api/health/archive/${id}`, data);
}
export function deleteHealthArchive(id) {
    return request.delete(`/api/health/archive/${id}`);
}
export function getHealthDataRecordPage(params) {
    return fetchPage('/api/health/data/page', params);
}
export function getHealthDataSummary(params) {
    return request.get('/api/health/data/summary', { params });
}
export function createHealthDataRecord(data) {
    return request.post('/api/health/data', data);
}
export function updateHealthDataRecord(id, data) {
    return request.put(`/api/health/data/${id}`, data);
}
export function deleteHealthDataRecord(id) {
    return request.delete(`/api/health/data/${id}`);
}
export function getHealthInspectionPage(params) {
    return fetchPage('/api/health/inspection/page', params);
}
export function getHealthInspectionSummary(params) {
    return request.get('/api/health/inspection/summary', { params });
}
export function createHealthInspection(data) {
    return request.post('/api/health/inspection', data);
}
export function updateHealthInspection(id, data) {
    return request.put(`/api/health/inspection/${id}`, data);
}
export function deleteHealthInspection(id) {
    return request.delete(`/api/health/inspection/${id}`);
}
export function getHealthNursingLogPage(params) {
    return fetchPage('/api/health/nursing-log/page', params);
}
export function getHealthNursingLogSummary(params) {
    return request.get('/api/health/nursing-log/summary', { params });
}
export function createHealthNursingLog(data) {
    return request.post('/api/health/nursing-log', data);
}
export function updateHealthNursingLog(id, data) {
    return request.put(`/api/health/nursing-log/${id}`, data);
}
export function deleteHealthNursingLog(id) {
    return request.delete(`/api/health/nursing-log/${id}`);
}
