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
    // jsdom 的 Blob 未实现 arrayBuffer()，用 FileReader 兜底真实读取字节（Response 会把 Blob 变成字符串）
    if (typeof Blob.prototype.arrayBuffer !== 'function') {
      ;(Blob.prototype as unknown as { arrayBuffer: () => Promise<ArrayBuffer> }).arrayBuffer = function (this: Blob) {
        return new Promise<ArrayBuffer>((resolve, reject) => {
          const reader = new FileReader()
          reader.onload = () => resolve(reader.result as ArrayBuffer)
          reader.onerror = () => reject(reader.error)
          reader.readAsArrayBuffer(this)
        })
      }
    }
    // jsdom 环境下 URL.createObjectURL / revokeObjectURL 默认不存在，先补桩再 spyOn
    if (typeof URL.createObjectURL !== 'function') {
      ;(URL as unknown as { createObjectURL: () => string }).createObjectURL = () => ''
    }
    if (typeof URL.revokeObjectURL !== 'function') {
      ;(URL as unknown as { revokeObjectURL: () => void }).revokeObjectURL = () => {}
    }
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
