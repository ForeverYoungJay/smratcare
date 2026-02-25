export interface BaseConfigGroupOption {
  code: string
  label: string
}

export interface BaseConfigItem {
  id: number
  configGroup: string
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
