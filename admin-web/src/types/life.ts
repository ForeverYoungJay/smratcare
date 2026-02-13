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
