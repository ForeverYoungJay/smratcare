export const DINING_MESSAGES = {
    requiredDishName: '请输入菜品名称',
    invalidDishPrice: '单价不能小于0',
    requiredRecipeName: '请输入食谱名称',
    requiredMealType: '请选择餐次',
    requiredDishSelection: '请至少选择一个菜品',
    requiredRecipeFields: '请填写完整信息',
    requiredOrderMealDish: '请先填写老人、餐次和菜品',
    requiredOrderFields: '请填写完整点餐信息',
    requiredOverrideReason: '请输入放行申请原因',
    noNeedOverride: '当前无需申请放行',
    requiredOverrideId: '风险预检未通过，请先申请放行审批并填写审批ID',
    overrideAppliedPrefix: '放行申请已提交，审批单ID:',
    requiredPrepZoneFields: '请填写分区编码和名称',
    invalidPrepCapacity: '备餐能力不能小于0',
    requiredDeliveryAreaFields: '请填写区域编码和名称',
    requiredMealOrder: '请选择点餐单'
};
export const DINING_MEAL_TYPES = {
    breakfast: 'BREAKFAST',
    lunch: 'LUNCH',
    dinner: 'DINNER',
    snack: 'SNACK',
    unknown: 'UNKNOWN'
};
export const DINING_STATUS = {
    enabled: 'ENABLED',
    disabled: 'DISABLED',
    created: 'CREATED',
    preparing: 'PREPARING',
    delivering: 'DELIVERING',
    delivered: 'DELIVERED',
    cancelled: 'CANCELLED',
    pending: 'PENDING',
    failed: 'FAILED',
    approved: 'APPROVED',
    rejected: 'REJECTED'
};
export const DINING_MEAL_TYPE_OPTIONS = [
    { label: '早餐', value: DINING_MEAL_TYPES.breakfast },
    { label: '午餐', value: DINING_MEAL_TYPES.lunch },
    { label: '晚餐', value: DINING_MEAL_TYPES.dinner },
    { label: '加餐', value: DINING_MEAL_TYPES.snack }
];
export const DINING_ENABLE_STATUS_OPTIONS = [
    { label: '启用', value: DINING_STATUS.enabled },
    { label: '停用', value: DINING_STATUS.disabled }
];
export const DINING_ORDER_STATUS_OPTIONS = [
    { label: '已创建', value: DINING_STATUS.created },
    { label: '备餐中', value: DINING_STATUS.preparing },
    { label: '配送中', value: DINING_STATUS.delivering },
    { label: '已送达', value: DINING_STATUS.delivered },
    { label: '已取消', value: DINING_STATUS.cancelled }
];
export const DINING_DELIVERY_STATUS_OPTIONS = [
    { label: '待送达', value: DINING_STATUS.pending },
    { label: '已送达', value: DINING_STATUS.delivered },
    { label: '送达失败', value: DINING_STATUS.failed }
];
export function getDiningMealTypeLabel(value) {
    return DINING_MEAL_TYPE_OPTIONS.find((item) => item.value === value)?.label || value || '-';
}
export function getDiningOrderStatusLabel(value) {
    return DINING_ORDER_STATUS_OPTIONS.find((item) => item.value === value)?.label || value || '-';
}
export function getDiningEnableStatusLabel(value) {
    return DINING_ENABLE_STATUS_OPTIONS.find((item) => item.value === value)?.label || value || '-';
}
export function getDiningDeliveryStatusLabel(value) {
    return DINING_DELIVERY_STATUS_OPTIONS.find((item) => item.value === value)?.label || value || '-';
}
export function getDiningOrderStatusColor(value) {
    if (value === DINING_STATUS.delivered)
        return 'green';
    if (value === DINING_STATUS.cancelled)
        return 'red';
    if (value === DINING_STATUS.delivering)
        return 'blue';
    return 'orange';
}
export function getDiningDeliveryStatusColor(value) {
    if (value === DINING_STATUS.delivered)
        return 'green';
    if (value === DINING_STATUS.failed)
        return 'red';
    return 'orange';
}
