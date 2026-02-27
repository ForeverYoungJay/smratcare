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
