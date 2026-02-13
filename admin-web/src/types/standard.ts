export interface ServiceItem {
  id: number
  name: string
  category?: string
  defaultDuration?: number
  defaultPoints?: number
  enabled?: number
  remark?: string
}

export interface CarePackage {
  id: number
  name: string
  careLevel?: string
  cycleDays?: number
  enabled?: number
  remark?: string
}

export interface CarePackageItem {
  id: number
  packageId: number
  itemId: number
  frequencyPerDay?: number
  enabled?: number
  sortNo?: number
  remark?: string
  itemName?: string
}

export interface ElderPackage {
  id: number
  elderId: number
  elderName?: string
  packageId: number
  packageName?: string
  startDate: string
  endDate?: string
  status?: number
  remark?: string
}

export interface GenerateTaskRequest {
  date: string
  force?: boolean
}
