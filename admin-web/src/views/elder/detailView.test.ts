import { describe, expect, it } from 'vitest'
import {
  archiveCompleteness,
  buildArchiveChecks,
  buildContractDocuments,
  buildDetailAlerts,
  contractDocFilterFromQuery,
  contractStatusText,
  filterContractDocuments,
  normalizeDetailTab,
  residentActionLinks
} from './detailView'

describe('elder detail view helpers', () => {
  it('maps legacy and unknown tab query values', () => {
    expect(normalizeDetailTab('family')).toBe('family')
    expect(normalizeDetailTab('disease')).toBe('health')
    expect(normalizeDetailTab(['contract'])).toBe('contract')
    expect(normalizeDetailTab('nope')).toBe('base')
    expect(normalizeDetailTab(undefined)).toBe('base')
  })

  it('builds alerts only from real data', () => {
    expect(buildDetailAlerts(null, null)).toEqual([])
    const alerts = buildDetailAlerts(
      { elderId: '1', riskTags: ['跌倒风险'], adjustmentRequired: true, suggestedCareLevel: '一级护理' },
      { abnormalVital24hCount: 2, abnormalInspectionOpenCount: 0, openIncidentCount: 0, pendingCareTaskCount: 1, latestCvdRiskLevel: 'HIGH' }
    )
    expect(alerts.map((item) => item.text)).toEqual([
      '跌倒风险',
      '心血管风险评估：高风险',
      '近 24 小时体征异常 2 次',
      '今日待执行护理任务 1 项',
      '评估建议调整为一级护理'
    ])
    expect(alerts[0].level).toBe('danger')
    expect(alerts[3].level).toBe('warning')
  })

  it('ignores zero counts serialized as strings', () => {
    const risk = { abnormalVital24hCount: '0', openIncidentCount: '0', abnormalInspectionOpenCount: '1', pendingCareTaskCount: '0' } as unknown as Parameters<typeof buildDetailAlerts>[1]
    expect(buildDetailAlerts(null, risk).map((item) => item.text)).toEqual(['巡检异常待处理 1 条'])
  })

  it('derives archive checks from contract linkage', () => {
    expect(buildArchiveChecks(null)).toEqual([])
    const checks = buildArchiveChecks({ hasIdAttachment: true, hasContractAttachment: false })
    expect(checks.map((item) => item.ok)).toEqual([true, false, false])
  })

  it('labels contract status and links actions straight to the merged pages', () => {
    expect(contractStatusText('signed')).toBe('已签署')
    expect(contractStatusText('')).toBe('--')
    expect(contractStatusText('CUSTOM')).toBe('CUSTOM')
    const links = Object.fromEntries(residentActionLinks('42').map((item) => [item.key, item]))
    expect(Object.keys(links)).toHaveLength(6)
    expect(Object.values(links).every((item) => item.query.elderId === '42')).toBe(true)
    expect(links.outing).toMatchObject({ path: '/elder/bed-panorama', query: { action: 'outing', recordType: 'OUTING' } })
    expect(links.medical).toMatchObject({ path: '/elder/bed-panorama', query: { action: 'medical', recordType: 'MEDICAL_OUTING' } })
    expect(links.bed).toMatchObject({ path: '/elder/bed-panorama', query: { elderId: '42' } })
    expect(links.fee).toMatchObject({ path: '/elder/status-change/discharge-settlement', query: { tab: 'fee' } })
    expect(links.discharge).toMatchObject({ path: '/elder/status-change/discharge-settlement', query: { tab: 'discharge', create: '1' } })
  })

  it('reads legacy docKind / docKeyword query', () => {
    expect(contractDocFilterFromQuery({ docKind: 'contract', docKeyword: ' 合同 ' })).toEqual({ kind: 'CONTRACT', keyword: '合同' })
    expect(contractDocFilterFromQuery({ docKind: 'BAD' })).toEqual({ kind: 'ALL', keyword: '' })
  })

  it('merges historical contract, attachments and assessment reports then filters', () => {
    const docs = buildContractDocuments(
      {
        elderId: '7',
        attachments: [
          { id: '1', leadId: '1', attachmentType: 'household', fileName: '户口.pdf', fileSize: 2048 },
          { id: '2', leadId: '1', attachmentType: 'weird', fileName: 'x.png' }
        ]
      },
      { id: '7', historicalContractFileUrl: '/f/%E5%90%88%E5%90%8C.pdf', admissionDate: '2024-01-01' } as never,
      { unassignedReports: [{ recordId: 9, assessmentType: 'ADMISSION', levelCode: 'L2', score: 80 }] }
    )
    expect(docs.map((d) => [d.typeCode, d.type, d.name])).toEqual([
      ['CONTRACT', '历史合同附件', '合同.pdf'],
      ['HOUSEHOLD', '户口复印件', '户口.pdf'],
      ['OTHER', '其他附件', 'x.png'],
      ['ASSESSMENT_REPORT', '入住评估', '入住评估报告-L2-80分']
    ])
    expect(docs[1].size).toBe('2.0 KB')
    expect(docs[3].route).toBe('/elder/assessment/ability/archive?elderId=7')
    expect(filterContractDocuments(docs, 'ASSESSMENT_REPORT', '').map((d) => d.id)).toEqual(['report-9'])
    expect(filterContractDocuments(docs, 'ALL', '户口').map((d) => d.id)).toEqual(['att-1'])
    expect(buildContractDocuments(null, null, null)).toEqual([])
  })

  it('prefers backend archive score and falls back to flags', () => {
    expect(archiveCompleteness(null)).toBeNull()
    expect(archiveCompleteness({ archiveScore: 120, archiveLevel: 'HIGH' })).toEqual({ score: 100, level: '高' })
    expect(archiveCompleteness({ hasContractAttachment: true, hasIdAttachment: true })).toEqual({ score: 70, level: '中' })
  })
})
