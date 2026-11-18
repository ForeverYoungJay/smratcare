#!/usr/bin/env python3
"""全链路业务冒烟：咨询 → 参观 → 合同 → 评估 → 入住费用 → 入住 → 签约 → 护理/体征/医嘱 → 账单收款 → 退住结算。

每一步用对应岗位的演示账号（密码 123456）调用真实接口，任何一步失败即停止并打印原因。
注意：只在本地/测试库运行。测试数据以「流程测试」开头；「生成本月账单」是全机构操作，会同时生成其他长者的本月账单。
机构未配置床位费/护理费时账单为 0，收款步骤会跳过。
用法：python3 scripts/business_flow_e2e.py [--base http://localhost:8080] [--skip-pay 验证欠费不能退住]
"""
import json
import sys
import time
import urllib.parse
import urllib.request
from datetime import date, datetime, timedelta

BASE = sys.argv[sys.argv.index('--base') + 1] if '--base' in sys.argv else 'http://localhost:8080'
TODAY = date.today()
MONTH = TODAY.strftime('%Y-%m')
TAG = '流程测试' + datetime.now().strftime('%H%M%S')
_tokens = {}


def call(user, method, path, body=None, allow=()):
    if user not in _tokens:
        _tokens[user] = None
        _tokens[user] = call(user, 'POST', '/api/auth/login', {'username': user, 'password': '123456'})['token']
    req = urllib.request.Request(BASE + urllib.parse.quote(path, safe='/?=&:%-_.~'), method=method, data=None if body is None else json.dumps(body).encode(),
                                 headers={'Content-Type': 'application/json'})
    if _tokens[user]:
        req.add_header('Authorization', 'Bearer ' + _tokens[user])
    try:
        with urllib.request.urlopen(req) as resp:
            raw = resp.read().decode() or 'null'
    except urllib.error.HTTPError as err:
        raw = err.read().decode()
    data = json.loads(raw)
    if isinstance(data, dict) and 'code' in data and 'data' in data:
        if data['code'] != 0:
            if any(a in str(data.get('message')) for a in allow):
                return {'_skipped': data.get('message')}
            raise SystemExit(f'✗ {user} {method} {path}\n  {data.get("message")}')
        return data['data']
    return data


def step(name, value=None):
    print(f'✓ {name}' + (f'  → {value}' if value is not None else ''))
    return value


def main():
    t = lambda h: f'{TODAY}T{h}:00'
    # 上次中途失败的测试长者可能还占着床，先解绑
    for b in call('nursing_minister', 'GET', '/api/bed/list'):
        if b.get('elderId') and call('nursing_minister', 'GET', f"/api/elder/{b['elderId']}")['fullName'].startswith('流程测试'):
            call('nursing_minister', 'POST', f"/api/elder/{b['elderId']}/unbindBed", {'reason': '清理流程测试残留'})
    # 1 市场：咨询、跟进、参观
    lead = call('marketing_emp', 'POST', '/api/crm/leads', {
        'name': TAG + '家属', 'phone': '139' + datetime.now().strftime('%H%M%S%f')[:8], 'elderName': TAG + '长者',
        'consultType': '电话', 'remark': '关注半自理照护和单人间费用'})
    lead_id = step('市场员工 新建咨询客户', lead['id'])
    call('marketing_emp', 'POST', f'/api/crm/leads/{lead_id}/followups',
         {'method': '电话', 'followupResult': '有意向', 'followupContent': '约明天参观'})
    step('市场员工 记录跟进')
    appt = call('marketing_emp', 'POST', '/api/crm/visit-appointments',
                {'leadId': lead_id, 'appointmentTime': t('10:00'), 'visitorName': TAG + '家属'})
    appt_id = appt['id'] if isinstance(appt, dict) else appt
    call('marketing_emp', 'POST', f'/api/crm/visit-appointments/{appt_id}/arrive')
    call('marketing_emp', 'POST', f'/api/crm/visit-appointments/{appt_id}/complete', {'visitResult': '满意，准备签约'})
    step('市场员工 参观预约 → 到访 → 完成', appt_id)

    # 2 合同草稿
    contract = call('marketing_emp', 'POST', '/api/crm/contracts', {
        'leadId': lead_id, 'elderName': TAG + '长者', 'name': TAG + '家属', 'contractType': 'LONG_TERM',
        'referenceMonthlyFee': 5000, 'flowStage': 'PENDING_ASSESSMENT', 'currentOwnerDept': 'ASSESSMENT', 'status': 'DRAFT'})
    contract_id, contract_no = contract['id'], contract['contractNo']
    step('市场员工 建合同草稿', contract_no)

    # 3 护理部：建档 + 入住评估
    elder = call('nursing_minister', 'POST', '/api/elder',
                 {'fullName': TAG + '长者', 'gender': 1, 'birthDate': '1945-01-01', 'status': 2})
    elder_id = step('护理部长 建长者档案', elder['id'] if isinstance(elder, dict) else elder)
    call('nursing_minister', 'POST', '/api/assessment/records', {
        'elderId': elder_id, 'elderName': TAG + '长者', 'assessmentType': 'ADMISSION', 'assessmentDate': str(TODAY),
        'archiveNo': contract_no, 'levelCode': 'L2', 'score': 60, 'resultSummary': '半自理，建议二级护理'})
    stage = call('marketing_emp', 'GET', f'/api/crm/contracts/{contract_id}')['flowStage']
    step('护理部长 入住评估（合同阶段应为待选床）', stage)
    if stage != 'PENDING_BED_SELECT':
        raise SystemExit('✗ 评估完成后合同没有进入待选床')

    # 4 财务：入住费用审核
    audit = call('finance_emp', 'POST', '/api/finance/fee/admission-audit',
                 {'elderId': elder_id, 'totalAmount': 8000, 'depositAmount': 3000, 'remark': '首期'})
    audit_id = audit['id'] if isinstance(audit, dict) else audit
    call('finance_minister', 'PUT', f'/api/finance/fee/admission-audit/{audit_id}/review', {'status': 'APPROVED'})
    step('财务员工 提交入住费用 → 财务部长审核通过', audit_id)

    # 5 入住办理：选空床
    beds = call('marketing_emp', 'GET', '/api/bed/list')
    bed = next(b for b in beds if b.get('status') == 1 and not b.get('elderId'))
    call('marketing_emp', 'POST', '/api/elder/lifecycle/admit', {
        'elderId': elder_id, 'admissionDate': str(TODAY), 'contractNo': contract_no, 'bedId': bed['id'],
        'bedStartDate': str(TODAY), 'depositAmount': 3000, 'occupancyMode': 'BED'})
    step('市场员工 入住办理（分床、收押金）', bed.get('bedNo') or bed['id'])

    # 6 市场部长：审批并最终签署
    call('marketing_minister', 'POST', f'/api/crm/contracts/{contract_id}/move-to-pending-sign')
    call('marketing_minister', 'POST', f'/api/crm/contracts/{contract_id}/finalize', {'remark': '家属到场签署'})
    signed = call('marketing_minister', 'GET', f'/api/crm/contracts/{contract_id}')
    status = (signed.get('status'), signed.get('flowStage'))
    step('市场部长 最终签署合同', status)

    # 7 护理：派任务、扫码执行、记体征
    nurse = call('nursing_emp', 'GET', '/api/auth/me')
    nurse_id = (nurse.get('staffInfo') or nurse).get('id')
    task_id = call('nursing_minister', 'POST', '/api/care/tasks/create',
                   {'elderId': elder_id, 'taskName': '翻身拍背', 'planTime': t('10:00'), 'staffId': nurse_id})
    task_id = task_id['id'] if isinstance(task_id, dict) else task_id
    bed_now = next(b for b in call('marketing_emp', 'GET', '/api/bed/list') if str(b['id']) == str(bed['id']))
    done = call('nursing_emp', 'POST', '/api/care/tasks/execute',
                {'taskDailyId': task_id, 'staffId': nurse_id, 'bedQrCode': bed_now.get('bedQrCode'), 'remark': '完成'})
    step('护理员 扫床头码执行护理任务', done.get('status') if isinstance(done, dict) else done)
    call('nursing_emp', 'POST', '/api/vital/record',
         {'elderId': elder_id, 'type': 'BP', 'valueJson': {'sbp': 132, 'dbp': 84}, 'measuredAt': t('09:00')})
    step('护理员 记录血压')

    # 8 医务：开医嘱
    order = call('medical_minister', 'POST', '/api/medical/order', {
        'elderId': elder_id, 'content': '阿司匹林 100mg qd', 'orderType': 'LONG_TERM', 'category': 'DRUG',
        'dosage': '100mg', 'frequency': 'QD', 'startTime': t('08:00')})
    step('医务部长 开长期医嘱', order)

    # 9 财务：出账、收款
    gen = call('finance_emp', 'POST', f'/api/bill/generate?month={MONTH}')
    step('财务员工 生成本月账单', {k: gen.get(k) for k in ('generatedCount', 'skippedCount')})
    bills = call('finance_emp', 'GET', f'/api/bill/page?elderId={elder_id}&month={MONTH}&pageNo=1&pageSize=5')['records']
    if not bills:
        raise SystemExit('✗ 已签约入住的长者没有生成本月账单')
    bill = bills[0]
    step('本月账单', f"应收 {bill.get('totalAmount')} / 未收 {bill.get('outstandingAmount')}")
    if float(bill.get('outstandingAmount') or 0) > 0 and '--skip-pay' not in sys.argv:
        paid = call('finance_emp', 'POST', f"/api/bill/{bill['id']}/pay", {
            'amount': float(bill['outstandingAmount']), 'method': 'CASH',
            'paidAt': f'{TODAY} 10:30:00', 'remark': '前台现金'})
        step('财务员工 收款登记，剩余未收', paid.get('outstandingAmount') if isinstance(paid, dict) else paid)

    # 10 退住
    planned = str(TODAY + timedelta(days=7))
    apply_ = call('nursing_emp', 'POST', '/api/elder/lifecycle/discharge-apply',
                  {'elderId': elder_id, 'plannedDischargeDate': planned, 'reason': '回家休养'})
    apply_id = apply_['id'] if isinstance(apply_, dict) else apply_
    call('nursing_minister', 'PUT', f'/api/elder/lifecycle/discharge-apply/{apply_id}/review',
         {'status': 'APPROVED', 'reviewRemark': '同意'})
    for s in ('CARE_HANDOVER', 'ITEM_HANDOVER'):
        call('nursing_minister', 'PUT', f'/api/elder/lifecycle/discharge-apply/{apply_id}/progress', {'step': s})
    step('护理员 申请退住 → 护理部长审批 → 护理/物品交接', apply_id)
    daudit = call('finance_emp', 'POST', '/api/finance/fee/discharge-audit',
                  {'elderId': elder_id, 'dischargeApplyId': apply_id, 'payableAmount': 1200})
    daudit_id = daudit['id'] if isinstance(daudit, dict) else daudit
    call('finance_minister', 'PUT', f'/api/finance/fee/discharge-audit/{daudit_id}/review', {'status': 'APPROVED'})
    sett = call('finance_minister', 'GET', f'/api/finance/fee/discharge-settlement/page?elderId={elder_id}&pageNo=1&pageSize=5')['records'][0]
    for body in ({'action': 'FRONTDESK_APPROVE'}, {'action': 'NURSING_APPROVE'},
                 {'action': 'FINANCE_REFUND', 'signerName': '财务部长'}):
        call('finance_minister', 'POST', f"/api/finance/fee/discharge-settlement/{sett['id']}/confirm", body)
    call('nursing_minister', 'PUT', f'/api/elder/lifecycle/discharge-apply/{apply_id}/progress', {'step': 'ARCHIVE'})
    step('财务 退住结算（前台、护理签字 → 押金抵扣退款）→ 归档', sett['id'])

    final = call('nursing_minister', 'GET', f'/api/elder/{elder_id}')
    bed_after = next(b for b in call('marketing_emp', 'GET', '/api/bed/list') if str(b['id']) == str(bed['id']))
    step('结果：长者状态（3=已退住）', final.get('status'))
    step('结果：床位是否释放', not bed_after.get('elderId'))
    ended = call('marketing_minister', 'GET', f'/api/crm/contracts/{contract_id}')
    step('结果：合同状态', (ended.get('status'), ended.get('contractStatus')))
    if ended.get('status') != 'TERMINATED':
        raise SystemExit('✗ 退住后合同没有失效')
    print(f'\n全流程通过。测试数据标记：{TAG}，elderId={elder_id} leadId={lead_id} contractId={contract_id}')


if __name__ == '__main__':
    t0 = time.time()
    main()
    print(f'用时 {time.time() - t0:.1f}s')
