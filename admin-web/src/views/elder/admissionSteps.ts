import type { BuildingItem, FloorItem, RoomItem } from '../../types'
// 入住办理中心六步：完成情况全部由既有业务数据推导（档案/附件、入住评估、合同阶段、入住费用审核、床位、在住状态）。

export type AdmissionStepKey = 'register' | 'assessment' | 'contract' | 'fee' | 'bed' | 'confirm'
export type AdmissionStepState = 'done' | 'current' | 'pending'

export interface AdmissionFacts {
  identityReady: boolean
  assessmentDone: boolean
  contractValid: boolean
  feeConfirmed: boolean
  bedReady: boolean
  admitted: boolean
}

export const ADMISSION_STEPS: ReadonlyArray<{ key: AdmissionStepKey; label: string; owner: string; fact: keyof AdmissionFacts }> = [
  { key: 'register', label: '入住登记', owner: '市场部', fact: 'identityReady' },
  { key: 'assessment', label: '健康评估', owner: '护理部 / 医务部', fact: 'assessmentDone' },
  { key: 'contract', label: '合同签订', owner: '市场部', fact: 'contractValid' },
  { key: 'fee', label: '费用办理', owner: '财务部', fact: 'feeConfirmed' },
  { key: 'bed', label: '床位分配', owner: '后勤部 / 护理部', fact: 'bedReady' },
  { key: 'confirm', label: '入住确认', owner: '护理部', fact: 'admitted' }
]

/** 合同有效：未作废且已过入住评估阶段（与后端 admit 的合同校验一致；终签在入住后由市场部完成）。 */
export function isContractValid(contract?: { status?: string; flowStage?: string } | null): boolean {
  if (!contract || contract.status === 'VOID') return false
  const stage = String(contract.flowStage || '').toUpperCase()
  return stage === 'PENDING_BED_SELECT' || stage === 'PENDING_SIGN' || stage === 'SIGNED'
}

/** 入住评估完成：存在非草稿的入住评估记录，或合同已被评估流转到下一阶段。 */
export function isAssessmentDone(records: Array<{ status?: string }>, flowStage?: string): boolean {
  if (records.some((item) => item.status && item.status !== 'DRAFT')) return true
  return isContractValid({ flowStage })
}

/** 床位可用：空闲且未停用，或本就由该长者占用。 */
export function isBedAvailable(bed: { elderId?: unknown; status?: number } | undefined, elderId?: unknown): boolean {
  if (!bed) return false
  if (bed.elderId) return String(bed.elderId) === String(elderId || '')
  return bed.status === 1 || bed.status === undefined
}

export function resolveAdmissionSteps(facts: AdmissionFacts) {
  const currentIndex = facts.admitted ? -1 : ADMISSION_STEPS.findIndex((step) => !facts[step.fact])
  return ADMISSION_STEPS.map((step, index) => ({
    ...step,
    state: (facts.admitted || facts[step.fact]
      ? 'done'
      : index === currentIndex ? 'current' : 'pending') as AdmissionStepState
  }))
}

/** 入住确认前必须满足的条件（入住确认自身除外），返回缺失项名称。 */
export function missingForConfirm(facts: AdmissionFacts): string[] {
  return ADMISSION_STEPS.filter((step) => step.key !== 'confirm' && !facts[step.fact]).map((step) => step.label)
}

export function completionPercent(facts: AdmissionFacts): number {
  const done = ADMISSION_STEPS.filter((step) => facts.admitted || facts[step.fact]).length
  return Math.round((done / ADMISSION_STEPS.length) * 100)
}

/** 没有楼栋/楼层接口权限时，用房间上自带的楼栋、楼层信息补出选项，保证能选床 */
export function assetsFromRooms(rooms: RoomItem[]): { buildings: BuildingItem[]; floors: FloorItem[] } {
  const buildings = new Map<string, BuildingItem>()
  const floors = new Map<string, FloorItem>()
  rooms.forEach((room) => {
    if (room.buildingId == null) return
    const buildingKey = String(room.buildingId)
    if (!buildings.has(buildingKey)) buildings.set(buildingKey, { id: room.buildingId, name: room.building || buildingKey })
    if (room.floorId != null && !floors.has(String(room.floorId))) {
      floors.set(String(room.floorId), { id: room.floorId, buildingId: room.buildingId, floorNo: room.floorNo || String(room.floorId) })
    }
  })
  return { buildings: [...buildings.values()], floors: [...floors.values()] }
}
