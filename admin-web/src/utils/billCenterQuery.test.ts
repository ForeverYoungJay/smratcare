import { describe, expect, it } from 'vitest'
import {
  buildBillCenterRouteQuery,
  buildBillCenterRouteSignature,
  parseBillCenterRouteState
} from './billCenterQuery'

describe('billCenterQuery utils', () => {
  it('parses route query with defaults and lockScene guard', () => {
    const state = parseBillCenterRouteState(
      {
        billMonth: '2026-03',
        billKeyword: '王阿姨',
        billPayMethod: 'wechat',
        billScene: 'resident',
        billPageNo: '5',
        billPageSize: '20'
      },
      {
        lockScene: true,
        defaultScene: 'ADMISSION',
        defaultMonth: '2026-02'
      }
    )
    expect(state).toEqual({
      month: '2026-03',
      keyword: '王阿姨',
      payMethod: 'WECHAT',
      scene: 'ADMISSION',
      pageNo: 5,
      pageSize: 20
    })
  })

  it('builds route query and keeps unmanaged query values', () => {
    const query = buildBillCenterRouteQuery(
      {
        month: '2026-03',
        keyword: ' 李奶奶 ',
        payMethod: 'alipay',
        scene: 'RESIDENT',
        pageNo: 2,
        pageSize: 50
      },
      {
        fromResident: '1',
        billMonth: '2025-12'
      },
      {
        lockScene: false
      }
    )
    expect(query).toEqual({
      fromResident: '1',
      billMonth: '2026-03',
      billKeyword: '李奶奶',
      billPayMethod: 'ALIPAY',
      billScene: 'RESIDENT',
      billPageNo: '2',
      billPageSize: '50'
    })
  })

  it('builds stable signature from managed route keys', () => {
    const signature = buildBillCenterRouteSignature({
      billPageSize: '10',
      billPageNo: '1',
      billKeyword: 'A'
    })
    expect(signature).toBe(
      'billKeyword:A|billMonth:|billPageNo:1|billPageSize:10|billPayMethod:|billScene:'
    )
  })
})
