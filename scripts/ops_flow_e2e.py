#!/usr/bin/env python3
"""内部运营业务线冒烟：人事（入职 → 打卡 → 请假审批 → 工资批次 → 财务付款 → 工资条 → 积分申诉 → 离职检查）
和后勤（报修 → 采购入库 → 领用出库审批 → 盘点 → 设备 → 订餐 → 消防巡查）。

用法：python3 scripts/ops_flow_e2e.py [--base http://localhost:8080] [--only hr|logistics]
只在本地/测试库运行；数据以「流程测试」开头，工资批次期间固定为 2099-01。
"""
import json
import sys
from datetime import timedelta

from business_flow_e2e import TAG, TODAY, call, step

PERIOD = '2099-01'  # 测试专用工资期间，避免占用真实月份


def ident(value):
    return value['id'] if isinstance(value, dict) else value


def hr():
    print('— 人事线 —')
    phone = '137' + TAG[-6:] + '01'
    dup = call('hr_minister', 'GET', f'/api/admin/hr/staff/duplicate-check?phone={phone}&name={TAG}员工')
    step('人事部长 入职查重（应无重复）', len(dup))
    dept = call('hr_minister', 'GET', '/api/admin/departments/options')['records'][0]
    staff = call('hr_minister', 'POST', '/api/admin/staff', {
        'username': 'flow' + TAG[-6:], 'password': '123456', 'realName': TAG + '员工', 'staffNo': 'FLOW' + TAG[-6:],
        'departmentId': dept.get('value') or dept.get('id'), 'phone': phone, 'status': 1})
    staff_id = step('人事部长 新员工入职建账号', ident(staff))
    call('hr_minister', 'POST', '/api/admin/hr/profile',
         {'staffId': staff_id, 'jobTitle': '护理员', 'employmentType': 'FULL_TIME', 'hireDate': str(TODAY)})
    role = next(r for r in call('hr_minister', 'GET', '/api/admin/roles')['records'] if r.get('roleCode') == 'NURSING_EMPLOYEE')
    call('hr_minister', 'POST', '/api/admin/staff-roles/add', {'staffId': staff_id, 'roleId': role['id']})
    step('人事部长 完善人事档案、分配护理员角色')

    for action in ('IN', 'OUT'):
        r = call('nursing_emp', 'POST', f'/api/attendance/punch?action={action}', allow=('今日已完成',))
        step(f'护理员 打卡 {action}', r.get('_skipped') if isinstance(r, dict) and '_skipped' in r else '成功')

    start = TODAY + timedelta(days=2)
    leave = call('nursing_emp', 'POST', '/api/oa/approval', {
        'approvalType': 'LEAVE', 'title': TAG + '事假', 'startTime': f'{start}T09:00:00', 'endTime': f'{start}T18:00:00',
        'formData': json.dumps({'policyAcknowledged': True, 'leaveType': 'PERSONAL'})})
    leave_id = ident(leave)
    approvers = ['nursing_minister', 'hr_minister', 'hr_director']
    for _ in range(4):
        cur = next(r for r in call('hr_director', 'GET', '/api/oa/approval/page?pageNo=1&pageSize=50')['records']
                   if str(r['id']) == str(leave_id))
        if cur.get('status') not in ('PENDING', 'IN_PROGRESS', 'PROCESSING', None):
            break
        for who in approvers:
            r = call(who, 'PUT', f'/api/oa/approval/{leave_id}/approve?remark=同意', allow=('指派', '仅管理者', '无权'))
            if not (isinstance(r, dict) and '_skipped' in r):
                break
    step('护理员 请假 → 逐级审批', cur.get('status'))

    batch = call('hr_minister', 'POST', '/api/admin/hr/payroll/batches', {'period': PERIOD})
    batch_id = ident(batch)
    lines = call('hr_minister', 'GET', f'/api/admin/hr/payroll/batches/{batch_id}/lines')
    line = next(l for l in lines if str(l['staffId']) == str(staff_id))
    call('hr_minister', 'PUT', f"/api/admin/hr/payroll/lines/{line['id']}", {
        'baseAmount': 5000, 'performanceAmount': 800, 'allowanceAmount': 300,
        'overtimeAmount': 0, 'deductionAmount': 0, 'personalAmount': 500})
    call('hr_minister', 'POST', f'/api/admin/hr/payroll/batches/{batch_id}/submit', {})
    self_approve = call('hr_minister', 'POST', f'/api/admin/hr/payroll/batches/{batch_id}/approve', {}, allow=('核算人',))
    if '_skipped' not in (self_approve or {}):
        raise SystemExit('✗ 核算人审核了自己的工资批次')
    call('hr_director', 'POST', f'/api/admin/hr/payroll/batches/{batch_id}/approve', {})
    call('finance_emp', 'POST', f'/api/admin/hr/payroll/batches/{batch_id}/bank-submit', {})
    call('finance_emp', 'POST', f'/api/admin/hr/payroll/batches/{batch_id}/mark-paid', {'paymentRef': 'BANK-' + TAG})
    step('工资：人事核算 → 自审被拒 → 人事总监审核 → 财务提交银行 → 登记付款', f'{len(lines)} 人')
    mine = call('flow' + TAG[-6:], 'GET', '/api/hr/payroll/my')
    slip = next((m for m in mine if m.get('period') == PERIOD), None)
    step('新员工 查看本人工资条（实发）', slip and slip['line'].get('netAmount'))
    if not slip or float(slip['line'].get('netAmount') or 0) != 5600:
        raise SystemExit('✗ 工资条实发应为 5600')

    call('hr_minister', 'POST', '/api/admin/hr/points/adjust',
         {'staffId': staff_id, 'changeType': 'DEDUCT', 'changePoints': 5, 'remark': TAG + '迟到扣分'}, allow=('insufficient',))
    call('hr_minister', 'POST', '/api/admin/hr/points/adjust',
         {'staffId': staff_id, 'changeType': 'EARN', 'changePoints': 10, 'remark': TAG + '表扬'})
    logs = call('flow' + TAG[-6:], 'GET', '/api/hr/points/my')
    logs = logs.get('records', logs.get('logs', logs)) if isinstance(logs, dict) else logs
    log = next(l for l in logs if TAG in (l.get('remark') or ''))
    appeal = call('flow' + TAG[-6:], 'POST', '/api/hr/appeals',
                  {'targetType': 'POINTS_LOG', 'targetId': log['id'], 'reason': '当天已请假'})
    call('hr_minister', 'POST', f'/api/admin/hr/appeals/{ident(appeal)}/decide', {'approve': True, 'note': '属实'})
    step('新员工 积分申诉 → 人事部长受理（自动冲正）', ident(appeal))

    checklist = call('hr_minister', 'GET', f'/api/admin/hr/staff/{staff_id}/offboarding-checklist')
    step('离职检查清单', {c['key']: c['done'] for c in checklist})
    call('hr_minister', 'POST', f'/api/admin/hr/terminate?staffId={staff_id}&leaveDate={TODAY}&leaveReason=流程测试')
    step('人事部长 办理离职')


def logistics():
    print('— 后勤线 —')
    order = call('nursing_emp', 'POST', '/api/life/maintenance',
                 {'reporterName': '护理员', 'issueType': '水电', 'description': TAG + '卫生间漏水', 'priority': 'HIGH'})
    oid = ident(order)
    call('logistics_emp', 'PUT', f'/api/life/maintenance/{oid}', {
        'reporterName': '护理员', 'issueType': '水电', 'description': TAG + '卫生间漏水', 'priority': 'HIGH',
        'assigneeName': '维修工', 'status': 'PROCESSING'})
    early = call('nursing_emp', 'PUT', f'/api/life/maintenance/{oid}/complete', allow=('权限',))
    if '_skipped' not in (early if isinstance(early, dict) else {}):
        raise SystemExit('✗ 报修人自己把维修单标记完工')
    call('logistics_emp', 'PUT', f'/api/life/maintenance/{oid}/complete')
    step('护理员 报修 → 后勤接单 →（报修人自行完工被拒）→ 后勤完工', oid)

    product = ident(call('logistics_emp', 'POST', '/api/store/product',
                         {'productName': TAG + '纸尿裤', 'unit': '包', 'category': 'CARE'}))
    wh = ident(call('logistics_emp', 'POST', '/api/material/warehouse',
                    {'warehouseCode': 'WH' + TAG[-6:], 'warehouseName': TAG + '仓'}))
    sup = ident(call('logistics_emp', 'POST', '/api/material/supplier',
                     {'supplierCode': 'SP' + TAG[-6:], 'supplierName': TAG + '供应商'}))
    po = ident(call('logistics_emp', 'POST', '/api/material/purchase', {
        'warehouseId': wh, 'supplierId': sup, 'items': [{'productId': product, 'quantity': 100, 'unitPrice': 12.5}]}))
    call('logistics_minister', 'POST', f'/api/material/purchase/{po}/approve')
    call('logistics_emp', 'POST', f'/api/material/purchase/{po}/complete')
    step('后勤 建物资/仓库/供应商 → 采购单 → 部长审批 → 入库', po)

    def stock():
        page = call('logistics_emp', 'GET', f'/api/inventory/batch/page?pageNo=1&pageSize=50&productId={product}')
        return sum(int(b.get('quantity') or 0) for b in page['records'] if str(b.get('productId')) == str(product))
    step('入库后库存', stock())

    sheet = call('nursing_minister', 'POST', '/api/inventory/outbound/sheet', {
        'receiverName': '护理部', 'applyDept': '护理部',
        'items': [{'productId': product, 'warehouseId': wh, 'quantity': 5, 'reason': '领用'}]},
        allow=('无权限', '权限'))
    if '_skipped' in sheet:
        sheet = call('logistics_emp', 'POST', '/api/inventory/outbound/sheet', {
            'receiverName': '护理部', 'applyDept': '护理部',
            'items': [{'productId': product, 'warehouseId': wh, 'quantity': 5, 'reason': '领用'}]})
    sheet_id = ident(sheet)
    early = call('logistics_emp', 'POST', f'/api/inventory/outbound/sheet/confirm?id={sheet_id}', allow=('审批',))
    if '_skipped' not in (early if isinstance(early, dict) else {}):
        raise SystemExit('✗ 领用单未审批就出库了')
    appr = ident(call('logistics_emp', 'POST', '/api/oa/approval', {
        'approvalType': 'MATERIAL_APPLY', 'title': TAG + '出库审批',
        'formData': json.dumps({'outboundSheetId': str(sheet_id)})}))
    for _ in range(5):
        r = call('logistics_minister', 'PUT', f'/api/oa/approval/{appr}/approve?remark=同意', allow=('指派', '已', '状态'))
        if '_skipped' in (r if isinstance(r, dict) else {}):
            r2 = call('admin', 'PUT', f'/api/oa/approval/{appr}/approve?remark=同意', allow=('已', '状态'))
            if '_skipped' in (r2 if isinstance(r2, dict) else {}):
                break
    step('领用出库：领用单 →（未审批直接出库被拒）→ OA 逐级审批 → 自动出库，库存', stock())
    if stock() != 95:
        raise SystemExit('✗ 审批完成后库存应为 95')

    call('logistics_emp', 'POST', '/api/inventory/adjust',
         {'productId': product, 'warehouseId': wh, 'adjustType': 'LOSS', 'adjustQty': 1, 'reason': '盘点'})
    step('盘点盘亏 1，库存', stock())

    eq = ident(call('logistics_emp', 'POST', '/api/logistics/equipment', {
        'equipmentCode': 'EQ' + TAG[-6:], 'equipmentName': TAG + '电梯', 'location': '1号楼',
        'nextMaintainedAt': f'{TODAY + timedelta(days=10)}T09:00:00'}))
    call('logistics_emp', 'PUT', f'/api/logistics/equipment/{eq}/generate-maintenance')
    step('设备建档 → 生成保养工单', eq)

    dish = ident(call('logistics_emp', 'POST', '/api/life/dining/dish',
                      {'dishName': TAG + '清蒸鱼', 'unitPrice': 15, 'mealType': 'LUNCH', 'status': 'ENABLED'}))
    elders = call('nursing_minister', 'GET', '/api/elder/page?pageNo=1&pageSize=20&status=1')['records']
    elder = next(e for e in elders if not e['fullName'].startswith('流程测试'))
    meal = call('logistics_emp', 'POST', '/api/life/dining/order', {
        'elderId': elder['id'], 'orderDate': str(TODAY + timedelta(days=1)), 'mealType': 'LUNCH',
        'dishNames': TAG + '清蒸鱼', 'dishIds': str(dish), 'totalAmount': 15}, allow=('风险',))
    if '_skipped' in meal:
        step('订餐被风险拦截（长者有饮食禁忌）', meal['_skipped'])
    else:
        mid = ident(meal)
        for st in ('PREPARING', 'DELIVERING', 'DELIVERED'):
            call('logistics_emp', 'PUT', f'/api/life/dining/order/{mid}/status?status={st}')
        step('订餐 → 备餐 → 配送 → 送达', mid)

    fire = ident(call('logistics_emp', 'POST', '/api/fire/records', {
        'recordType': 'DAY_PATROL', 'title': TAG + '日间巡查', 'inspectorName': '后勤员',
        'checkTime': f'{TODAY}T10:00:00', 'location': '1号楼', 'issueDescription': '灭火器过期'}))
    early = call('logistics_minister', 'PUT', f'/api/fire/records/{fire}/close', allow=('处置',))
    if '_skipped' not in (early if isinstance(early, dict) else {}):
        raise SystemExit('✗ 未填处置措施也能关闭消防问题')
    call('logistics_emp', 'PUT', f'/api/fire/records/{fire}', {
        'recordType': 'DAY_PATROL', 'title': TAG + '日间巡查', 'inspectorName': '后勤员',
        'checkTime': f'{TODAY}T10:00:00', 'location': '1号楼', 'issueDescription': '灭火器过期', 'actionTaken': '已更换'})
    call('logistics_minister', 'PUT', f'/api/fire/records/{fire}/close')
    step('消防巡查发现问题 → 未整改不能关闭 → 填写整改 → 部长关闭', fire)


if __name__ == '__main__':
    only = sys.argv[sys.argv.index('--only') + 1] if '--only' in sys.argv else None
    if only in (None, 'hr'):
        hr()
    if only in (None, 'logistics'):
        logistics()
    print(f'\n通过。测试数据标记：{TAG}')
