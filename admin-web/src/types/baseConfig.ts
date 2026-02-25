export interface BaseConfigGroupOption {
  code: string
  label: string
}

export interface BaseConfigItem {
  id: number
  configGroup: string
  configGroupLabel?: string
  itemCode: string
  itemName: string
  status: number
  sortNo: number
  remark?: string
}

export interface BaseConfigItemPayload {
  configGroup: string
  itemCode: string
  itemName: string
  status: number
  sortNo?: number
  remark?: string
}

export interface BaseConfigImportItem {
  itemCode: string
  itemName: string
  status?: number
  sortNo?: number
  remark?: string
}

export interface BaseConfigImportErrorItem {
  rowNo: number
  itemCode?: string
  itemName?: string
  status?: number
  sortNo?: number
  remark?: string
  errorMessage: string
}

export interface BaseConfigImportResult {
  preview?: boolean
  totalCount: number
  successCount: number
  createCount?: number
  updateCount?: number
  failCount: number
  errors?: string[]
  errorItems?: BaseConfigImportErrorItem[]
}
