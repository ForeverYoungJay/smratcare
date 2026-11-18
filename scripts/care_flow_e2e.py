#!/usr/bin/env python3
"""照护与协同业务线冒烟：家属（注册 → 绑定长者 → 预约探视 → 门岗核验 → 充值 → 看账单）、
医护（用药设置 → 服药任务 → 临时医嘱执行 → 查房 → 异常体征 → 护理事件上报到关闭）、
OA（用章审批 → 任务派发验收 → 公告发布与已读回执）。

依赖开发配置的短信模拟（debug-return-code）。用法：python3 scripts/care_flow_e2e.py [--base http://localhost:8080]
只在本地/测试库运行；数据以「流程测试」开头，清理见 scripts/business_flow_cleanup.sql。
"""
import json
import urllib.request
from datetime import datetime, timedelta

from business_flow_e2e import BASE, TAG, TODAY, _tokens, call, step


def ident(value):
    return value['id'] if isinstance(value, dict) else value


def public(path, body):
    req = urllib.request.Request(BASE + path, method='POST', data=json.dumps(body).encode(),
                                 headers={'Content-Type': 'application/json'})
    with urllib.request.urlopen(req) as resp:
        data = json.loads(resp.read().decode())
    if data.get('code') != 0:
        raise SystemExit(f'✗ {path}\n  {data.get("message")}')
    return data['data']


def main():
    now = datetime.now()
    elder_id = ident(call('nursing_minister', 'POST', '/api/elder',
                          {'fullName': TAG + '长者', 'gender': 2, 'birthDate': '1940-05-01', 'status': 1}))
    step('护理部长 建在住长者档案', elder_id)

    print('— 家属线 —')
    phone = '136' + TAG[-6:] + '02'
    code = public('/api/auth/family/sms-code/send', {'orgId': 1, 'phone': phone, 'scene': 'REGISTER'})['debugCode']
    reg = public('/api/auth/family/register',
                 {'orgId': 1, 'phone': phone, 'verifyCode': code, 'password': 'flow123456', 'realName': TAG + '家属'})
    _tokens['family'] = reg['token']
    family_id = step('家属 短信验证注册', reg.get('familyUserId'))
    call('hr_minister', 'POST', '/api/admin/family/relations/bind',
         {'familyUserId': family_id, 'elderId': elder_id, 'relation': '子女', 'isPrimary': 1})
    step('人事部长 绑定家属与长者')
    visit_at = now + timedelta(minutes=40)
    if visit_at.date() == now.date():
        booking = call('family', 'POST', '/api/family/visit/book',
                       {'elderId': elder_id, 'visitTime': visit_at.strftime('%Y-%m-%dT%H:%M:00'), 'visitTimeSlot': 'PM'})
        call('logistics_emp', 'POST', '/api/guard/visit/checkin',
             {'bookingId': booking['id'], 'visitCode': booking['visitCode']})
        step('家属 预约探视 → 门岗凭探视码登记到访', booking['id'])
    else:
        step('探视：已接近午夜，当天无法预约，跳过')
    call('family', 'POST', '/api/family/payment/recharge',
         {'elderId': elder_id, 'amount': 100, 'method': 'OFFLINE', 'remark': TAG + '线下充值'})
    summary = call('family', 'GET', f"/api/family/payment/summary?elderId={elder_id}&month={TODAY.strftime('%Y-%m')}")
    step('家属 线下充值 100 → 查看费用概览', {k: summary.get(k) for k in list(summary)[:4]} if isinstance(summary, dict) else summary)

    print('— 医护线 —')
    call('nursing_minister', 'POST', '/api/health/medication/setting', {
        'elderId': elder_id, 'drugName': TAG + '阿司匹林', 'dosage': '1片', 'frequency': 'CUSTOM',
        'medicationTime': (now + timedelta(minutes=5)).strftime('%H:%M'), 'startDate': str(TODAY)})
    call('nursing_minister', 'POST', '/api/health/medication/task/generate-today')
    tasks = call('nursing_emp', 'GET', f'/api/health/medication/task/page?pageNo=1&pageSize=10&elderId={elder_id}&taskDate={TODAY}')['records']
    if not tasks:
        raise SystemExit('✗ 用药设置后没有生成今日服药任务')
    call('nursing_emp', 'PUT', f"/api/health/medication/task/{tasks[0]['id']}/complete",
         {'dosageTaken': '1', 'unit': '片', 'nurseName': '护理员', 'remark': '已服'})
    step('护理部长 用药设置 → 生成服药任务 → 护理员确认服药', len(tasks))

    order = ident(call('medical_minister', 'POST', '/api/medical/order', {
        'elderId': elder_id, 'content': TAG + '测血糖', 'orderType': 'TEMPORARY', 'category': 'CHECK',
        'startTime': now.strftime('%Y-%m-%dT%H:%M:00')}))
    execs = call('nursing_emp', 'GET', f'/api/medical/order/executions/page?pageNo=1&pageSize=10&elderId={elder_id}&status=PENDING')['records']
    if not execs:
        raise SystemExit('✗ 临时医嘱没有生成执行单')
    call('nursing_emp', 'POST', '/api/medical/order/executions/sign',
         {'executionId': execs[0]['id'], 'status': 'DONE', 'result': '空腹 6.1'})
    step('医务部长 开临时医嘱 → 护理员签执行', order)
    call('medical_minister', 'POST', '/api/medical/rounds/records', {
        'elderId': elder_id, 'findings': TAG + '血压偏高', 'handleOpinion': '加测血压', 'resultLevel': 'ABNORMAL'})
    step('医务部长 查房记录（异常）')
    vital = call('nursing_emp', 'POST', '/api/vital/record', {
        'elderId': elder_id, 'type': 'BP', 'valueJson': {'sbp': 182, 'dbp': 112}, 'measuredAt': now.strftime('%Y-%m-%dT%H:%M:00')})
    abnormal = call('nursing_emp', 'GET', '/api/vital/abnormal/today')
    rows = abnormal.get('records', abnormal) if isinstance(abnormal, dict) else abnormal
    step('护理员 录入血压 182/112，今日异常列表是否出现',
         any(str(r.get('elderId')) == str(elder_id) for r in (rows or [])))

    incident = ident(call('nursing_emp', 'POST', '/api/nursing/incidents', {
        'elderId': elder_id, 'incidentType': 'FALL', 'severity': 'HIGH',
        'occurredAt': now.strftime('%Y-%m-%dT%H:%M:00'), 'description': TAG + '起夜跌倒，无明显外伤'}))
    for action, who in (('ACCEPT', 'nursing_emp'), ('PROCESS', 'nursing_emp'), ('SUBMIT_REVIEW', 'nursing_emp')):
        call(who, 'POST', f'/api/nursing/incidents/{incident}/actions', {'action': action, 'note': '已处置并观察'})
    early = call('nursing_emp', 'POST', f'/api/nursing/incidents/{incident}/actions', {'action': 'CLOSE', 'note': '关闭'},
                 allow=('权限', '无权', '仅'))
    if '_skipped' not in (early if isinstance(early, dict) else {}):
        raise SystemExit('✗ 护理员自己关闭了跌倒事件')
    call('nursing_minister', 'POST', f'/api/nursing/incidents/{incident}/actions', {'action': 'CLOSE', 'note': '复核通过'})
    step('护理员 上报跌倒 → 接手 → 处理 → 提交复核（自行关闭被拒）→ 护理部长关闭', incident)

    print('— OA 线 —')
    seal = ident(call('nursing_emp', 'POST', '/api/oa/approval', {'approvalType': 'OFFICIAL_SEAL', 'title': TAG + '用章申请'}))
    denied = call('nursing_emp', 'PUT', f'/api/oa/approval/{seal}/approve?remark=ok', allow=('仅管理者', '权限'))
    if '_skipped' not in (denied if isinstance(denied, dict) else {}):
        raise SystemExit('✗ 员工审批了自己的申请')
    call('nursing_minister', 'PUT', f'/api/oa/approval/{seal}/approve?remark=同意')
    step('护理员 用章申请（自批被拒）→ 护理部长审批', seal)

    me = call('nursing_emp', 'GET', '/api/auth/me')
    nurse_id = (me.get('staffInfo') or me).get('id')
    task = ident(call('nursing_minister', 'POST', '/api/oa/task', {
        'title': TAG + '整理护理记录', 'assigneeId': nurse_id,
        'startTime': now.strftime('%Y-%m-%dT%H:%M:00'), 'endTime': (now + timedelta(hours=4)).strftime('%Y-%m-%dT%H:%M:00')}))
    call('nursing_emp', 'PUT', f'/api/oa/task/{task}/submit')
    call('nursing_minister', 'PUT', f'/api/oa/task/{task}/accept?pass=true')
    step('护理部长 派任务 → 护理员提交 → 部长验收', task)

    notice = ident(call('hr_minister', 'POST', '/api/oa/notice',
                        {'title': TAG + '消防演练通知', 'content': '周五下午消防演练', 'audience': 'STAFF'}))
    call('hr_minister', 'PUT', f'/api/oa/notice/{notice}/publish')
    for who in ('nursing_emp', 'logistics_emp'):
        call(who, 'POST', f'/api/oa/notice/{notice}/read')
    reads = call('hr_minister', 'GET', f'/api/oa/notice/{notice}/reads')
    step('人事部长 发布公告 → 两人已读，回执', f"{reads.get('readCount')}/{reads.get('targetCount')}")
    if reads.get('readCount') != 2:
        raise SystemExit('✗ 已读回执人数应为 2')
    print(f'\n通过。测试数据标记：{TAG}')


if __name__ == '__main__':
    main()
