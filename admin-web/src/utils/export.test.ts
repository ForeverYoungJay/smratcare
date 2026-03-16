import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { exportCsvByRequest } from './export'

vi.mock('./auth', () => ({
  getToken: () => 'test-token'
}))

describe('export utils', () => {
  const originalFetch = global.fetch
  let clickSpy: ReturnType<typeof vi.spyOn>
  let createObjectURLSpy: ReturnType<typeof vi.spyOn>
  let revokeObjectURLSpy: ReturnType<typeof vi.spyOn>

  beforeEach(() => {
    clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {})
    createObjectURLSpy = vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:test')
    revokeObjectURLSpy = vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => {})
  })

  afterEach(() => {
    global.fetch = originalFetch
    clickSpy.mockRestore()
    createObjectURLSpy.mockRestore()
    revokeObjectURLSpy.mockRestore()
    vi.clearAllMocks()
  })

  it('does not prepend a second BOM when CSV already contains one', async () => {
    let downloadedBlob: Blob | undefined
    createObjectURLSpy.mockImplementation((blob: Blob | MediaSource) => {
      downloadedBlob = blob as Blob
      return 'blob:test'
    })
    global.fetch = vi.fn().mockResolvedValue(
      new Response(new Blob([new Uint8Array([0xef, 0xbb, 0xbf, 0x61])], { type: 'text/csv;charset=utf-8;' }), {
        status: 200,
        headers: { 'content-disposition': 'attachment; filename="report.csv"' }
      })
    ) as typeof fetch

    await exportCsvByRequest('/api/export')

    const bytes = new Uint8Array(await downloadedBlob!.arrayBuffer())
    expect(Array.from(bytes)).toEqual([0xef, 0xbb, 0xbf, 0x61])
  })

  it('prepends BOM when CSV does not contain one', async () => {
    let downloadedBlob: Blob | undefined
    createObjectURLSpy.mockImplementation((blob: Blob | MediaSource) => {
      downloadedBlob = blob as Blob
      return 'blob:test'
    })
    global.fetch = vi.fn().mockResolvedValue(
      new Response(new Blob(['a'], { type: 'text/csv;charset=utf-8;' }), {
        status: 200,
        headers: { 'content-disposition': 'attachment; filename="report.csv"' }
      })
    ) as typeof fetch

    await exportCsvByRequest('/api/export')

    const bytes = new Uint8Array(await downloadedBlob!.arrayBuffer())
    expect(Array.from(bytes)).toEqual([0xef, 0xbb, 0xbf, 0x61])
  })
})
