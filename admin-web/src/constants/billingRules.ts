const rules: Record<string, { label: string; unit: string }> = {
  BED_FEE_MONTHLY: { label: '床位费（月）', unit: '元/月' },
  MEAL_FEE_MONTHLY: { label: '餐费（月）', unit: '元/月' },
  MEAL_FEE_PER_DAY: { label: '餐费（日）', unit: '元/天' },
  ALLOC_ELECTRIC_BASE_PRICE: { label: '电费单价', unit: '元/度' },
  ALLOC_ELECTRIC_FREE_KWH_PER_PERSON: { label: '每人每月免费电量', unit: '度/月' },
  ALLOC_WATER_BASE_PRICE: { label: '水费基础价格', unit: '元' }
}

export const billingRuleOptions = Object.entries(rules).map(([value, rule]) => ({ value, label: rule.label }))
export function billingRuleLabel(key: string, remark?: string) { return rules[key]?.label || remark || key }
export function billingRuleUnit(key: string) { return rules[key]?.unit || '按业务规则' }
export function isBusinessBillingRule(key: string) { return key in rules }
