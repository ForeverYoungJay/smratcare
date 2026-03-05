export function mapHealthExportRows(items, columns) {
    return items.map((item) => {
        const row = {};
        columns.forEach((column) => {
            row[column.title] = column.value(item);
        });
        return row;
    });
}
export const medicationRegistrationExportColumns = [
    { key: 'elderName', title: '老人姓名', value: (item) => item.elderName || '' },
    { key: 'drugName', title: '药品名称', value: (item) => item.drugName || '' },
    { key: 'registerTime', title: '登记时间', value: (item) => item.registerTime || '' },
    { key: 'dosageTaken', title: '用药量', value: (item) => item.dosageTaken ?? '' },
    { key: 'unit', title: '单位', value: (item) => item.unit || '' },
    { key: 'nurseName', title: '执行护士', value: (item) => item.nurseName || '' },
    { key: 'remark', title: '备注', value: (item) => item.remark || '' }
];
export const inspectionExportColumns = [
    { key: 'elderName', title: '老人姓名', value: (item) => item.elderName || '' },
    { key: 'inspectionDate', title: '巡检日期', value: (item) => item.inspectionDate || '' },
    { key: 'inspectionItem', title: '巡检项目', value: (item) => item.inspectionItem || '' },
    { key: 'result', title: '巡检结果', value: (item) => item.result || '' },
    { key: 'status', title: '状态', value: (item) => item.status || '' },
    { key: 'inspectorName', title: '巡检人', value: (item) => item.inspectorName || '' },
    { key: 'followUpAction', title: '跟进措施', value: (item) => item.followUpAction || '' },
    { key: 'attachmentUrls', title: '附件', value: (item) => item.attachmentUrls || '' },
    { key: 'otherNote', title: '其他说明', value: (item) => item.otherNote || '' },
    { key: 'remark', title: '备注', value: (item) => item.remark || '' }
];
export const nursingLogExportColumns = [
    { key: 'elderName', title: '老人姓名', value: (item) => item.elderName || '' },
    { key: 'logTime', title: '日志时间', value: (item) => item.logTime || '' },
    { key: 'logType', title: '日志类型', value: (item) => item.logType || '' },
    { key: 'content', title: '日志内容', value: (item) => item.content || '' },
    { key: 'staffName', title: '记录人', value: (item) => item.staffName || '' },
    { key: 'status', title: '状态', value: (item) => item.status || '' },
    { key: 'sourceInspectionId', title: '来源巡检ID', value: (item) => item.sourceInspectionId ?? '' },
    { key: 'remark', title: '备注', value: (item) => item.remark || '' }
];
export const drugDictionaryExportColumns = [
    { key: 'drugCode', title: '编码', value: (item) => item.drugCode || '' },
    { key: 'drugName', title: '名称', value: (item) => item.drugName || '' },
    { key: 'specification', title: '规格', value: (item) => item.specification || '' },
    { key: 'unit', title: '单位', value: (item) => item.unit || '' },
    { key: 'manufacturer', title: '厂家', value: (item) => item.manufacturer || '' },
    { key: 'category', title: '分类', value: (item) => item.category || '' },
    { key: 'remark', title: '备注', value: (item) => item.remark || '' }
];
export const drugDepositExportColumns = [
    { key: 'elderName', title: '老人', value: (item) => item.elderName || '' },
    { key: 'drugName', title: '药品', value: (item) => item.drugName || '' },
    { key: 'depositDate', title: '缴存日期', value: (item) => item.depositDate || '' },
    { key: 'quantity', title: '数量', value: (item) => item.quantity ?? '' },
    { key: 'unit', title: '单位', value: (item) => item.unit || '' },
    { key: 'expireDate', title: '到期日期', value: (item) => item.expireDate || '' },
    { key: 'depositorName', title: '缴存人', value: (item) => item.depositorName || '' },
    { key: 'remark', title: '备注', value: (item) => item.remark || '' }
];
export const medicationSettingExportColumns = [
    { key: 'elderName', title: '老人', value: (item) => item.elderName || '' },
    { key: 'drugName', title: '药品', value: (item) => item.drugName || '' },
    { key: 'dosage', title: '剂量', value: (item) => item.dosage || '' },
    { key: 'frequency', title: '频次', value: (item) => item.frequency || '' },
    { key: 'medicationTime', title: '用药时段', value: (item) => item.medicationTime || '' },
    { key: 'period', title: '起止日期', value: (item) => `${item.startDate || '-'} ~ ${item.endDate || '-'}` },
    { key: 'minRemainQty', title: '最小阈值', value: (item) => item.minRemainQty ?? '' },
    { key: 'remark', title: '备注', value: (item) => item.remark || '' }
];
export const medicationRemainingExportColumns = [
    { key: 'elderName', title: '老人', value: (item) => item.elderName || '' },
    { key: 'drugName', title: '药品', value: (item) => item.drugName || '' },
    { key: 'depositQty', title: '缴存总量', value: (item) => item.depositQty ?? '' },
    { key: 'usedQty', title: '已用总量', value: (item) => item.usedQty ?? '' },
    { key: 'remainQty', title: '剩余用量', value: (item) => item.remainQty ?? '' },
    { key: 'minRemainQty', title: '最小阈值', value: (item) => item.minRemainQty ?? '' },
    { key: 'lowStock', title: '状态', value: (item) => Number(item.lowStock) === 1 ? '预警' : '正常' }
];
export const archiveExportColumns = [
    { key: 'elderName', title: '老人', value: (item) => item.elderName || '' },
    { key: 'bloodType', title: '血型', value: (item) => item.bloodType || '' },
    { key: 'chronicDisease', title: '慢病史', value: (item) => item.chronicDisease || '' },
    { key: 'allergyHistory', title: '过敏史', value: (item) => item.allergyHistory || '' },
    { key: 'emergencyContact', title: '紧急联系人', value: (item) => item.emergencyContact || '' },
    { key: 'emergencyPhone', title: '联系电话', value: (item) => item.emergencyPhone || '' }
];
export const healthDataExportColumns = [
    { key: 'elderName', title: '老人', value: (item) => item.elderName || '' },
    { key: 'dataType', title: '类型', value: (item) => item.dataType || '' },
    { key: 'dataValue', title: '数据值', value: (item) => item.dataValue || '' },
    { key: 'measuredAt', title: '采集时间', value: (item) => item.measuredAt || '' },
    { key: 'source', title: '来源', value: (item) => item.source || '' },
    { key: 'abnormalFlag', title: '状态', value: (item) => item.abnormalFlag ? '异常' : '正常' },
    { key: 'remark', title: '备注', value: (item) => item.remark || '' }
];
