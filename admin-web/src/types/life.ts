export interface MealPlan {
  id: number
  planDate: string
  mealType: string
  menu: string
  calories?: number
  remark?: string
}

export interface ActivityEvent {
  id: number
  title: string
  eventDate: string
  startTime?: string
  endTime?: string
  location?: string
  organizer?: string
  content?: string
  status?: string
  remark?: string
}

export interface IncidentReport {
  id: number
  elderId?: number
  elderName?: string
  reporterName: string
  incidentTime: string
  incidentType: string
  level?: string
  description: string
  actionTaken?: string
  status?: string
}

export type IncidentLevel = 'NORMAL' | 'MAJOR'
export type IncidentStatus = 'OPEN' | 'CLOSED'

export interface IncidentQuery {
  pageNo?: number
  pageSize?: number
  keyword?: string
  elderName?: string
  incidentType?: string
  reporterName?: string
  level?: IncidentLevel
  status?: IncidentStatus
  incidentTimeStart?: string
  incidentTimeEnd?: string
}

export interface HealthBasicRecord {
  id: number
  elderId: number
  elderName?: string
  recordDate: string
  heightCm?: number
  weightKg?: number
  bmi?: number
  bloodPressure?: string
  heartRate?: number
  remark?: string
}

export interface BirthdayReminder {
  elderId: number
  elderName: string
  birthDate?: string
  nextBirthday?: string
  daysUntil?: number
  ageOnNextBirthday?: number
  roomNo?: string
  bedNo?: string
}

export interface RoomCleaningTask {
  id: number
  roomId: number
  roomNo?: string
  cleanerName?: string
  planDate: string
  cleanedAt?: string
  status?: string
  remark?: string
}

export interface MaintenanceRequest {
  id: number
  roomId?: number
  roomNo?: string
  reporterName: string
  assigneeName?: string
  issueType: string
  description: string
  priority?: string
  status?: string
  reportedAt?: string
  completedAt?: string
  remark?: string
}
