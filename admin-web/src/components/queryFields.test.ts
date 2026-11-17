import { describe, expect, it } from 'vitest'
import { h, Fragment } from 'vue'
import { commonQueryFields } from './queryFields'

describe('common queries', () => {
  it('keeps identity and period ahead of secondary filters without recreating fields', () => {
    const status = h('div', { label: '风险等级' }), keyword = h('div', { label: '关键词' }), date = h('div', { label: '日期' })
    const nodes = [status, keyword, date]
    expect(commonQueryFields(nodes, '/finance/reconcile')).toEqual([keyword, date, status])
    expect(nodes[0]).toBe(status)
    expect(commonQueryFields([h(Fragment, nodes)], '/finance/reconcile')[0]).toBe(keyword)
  })
  it('prefers employee and department queries for HR while retaining all other conditions', () => {
    const nodes = ['状态', '部门', '员工姓名', '日期'].map(label => h('div', { label }))
    expect(commonQueryFields(nodes, '/hr/profile').map(n => n.props?.label)).toEqual(['员工姓名', '部门', '日期', '状态'])
  })
})
