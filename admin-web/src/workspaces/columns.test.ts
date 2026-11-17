import { describe, expect, it } from 'vitest'
import { compactWorkspaceColumns, emphasizeColumns } from './columns'
describe('compact business columns', () => {
  it('keeps settlement money, risk, status and operations while moving signing metadata to full view', () => {
    const keys = ['detailNo', 'elderName', 'feeItem', 'payableAmount', 'fromDepositAmount', 'refundAmount', 'supplementAmount', 'riskLevel', 'frontdeskSignedTime', 'status', 'action']
    const columns = keys.map(key => ({ key, title: key === 'riskLevel' ? '风险等级' : key }))
    const result = compactWorkspaceColumns(columns, '/finance/discharge/settlement').map(c => c.key)
    expect(result).toEqual(['elderName', 'payableAmount', 'fromDepositAmount', 'refundAmount', 'supplementAmount', 'riskLevel', 'status', 'action'])
    expect(columns).toHaveLength(11)
  })
  it('always preserves warning/status/action columns in unknown tables', () => {
    const columns = Array.from({ length: 12 }, (_, i) => ({ key: `col${i}`, title: `列${i}` }))
    columns.push({ key: 'risk', title: '超时风险' }, { key: 'status', title: '状态' }, { key: 'action', title: '操作' })
    const result = compactWorkspaceColumns(columns, '/unknown')
    expect(result.slice(-3)).toEqual(columns.slice(-3))
    expect(result.length).toBeLessThan(columns.length)
  })
})

it('emphasizes money and identity without changing column callbacks or dropping clinical fields', () => {
  const render = () => 'original'
  const columns = [{ key: 'elderName', className: 'existing', customRender: render }, { key: 'amount' }, { key: 'dose' }, { key: 'status' }]
  const result = emphasizeColumns(columns)
  expect(result.map(c => c.key)).toEqual(columns.map(c => c.key))
  expect(result[0].className).toContain('existing business-column--identity')
  expect(result[0].customRender).toBe(render)
  expect(result[1].className).toContain('money')
  expect(columns[0].className).toBe('existing')
})
