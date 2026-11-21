import { describe, expect, it } from 'vitest'
import { fuzzyFilter, fuzzyTokenScore } from './fuzzySearch'

const pages = [
  { title: '护理任务', extra: '护理中心 / 护理任务 hulirenwu' },
  { title: '任务中心', extra: '后勤 / 任务中心' },
  { title: '老人档案', extra: '长者管理 / 老人档案 elder' },
  { title: '账单管理', extra: '财务 / 账单管理 bill' }
]
const run = (query: string) => fuzzyFilter(pages, query, (item) => item).map((item) => item.title)

describe('fuzzySearch', () => {
  it('matches characters in order even with gaps', () => {
    expect(run('护任')).toEqual(['护理任务'])
  })

  it('ranks title prefix above title contains above extra-only hits', () => {
    expect(run('任务')).toEqual(['任务中心', '护理任务'])
    expect(fuzzyTokenScore('任务', '任务中心')).toBeGreaterThan(fuzzyTokenScore('任务', '护理任务'))
  })

  it('requires every space-separated word and ignores case', () => {
    expect(run('财务 BILL')).toEqual(['账单管理'])
    expect(run('财务 elder')).toEqual([])
  })

  it('rejects characters scattered far apart in long text', () => {
    expect(fuzzyTokenScore('护任', '护理任务')).toBeGreaterThan(0)
    expect(fuzzyTokenScore('护任', '护理计划、巡视执行、交接与异常跟踪任务')).toBe(0)
  })

  it('returns everything for an empty query and nothing for a miss', () => {
    expect(run('  ')).toHaveLength(4)
    expect(run('排班')).toEqual([])
  })
})
