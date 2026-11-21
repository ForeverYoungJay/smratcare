import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import Antd from 'ant-design-vue'
import VideoManagement from './VideoManagement.vue'

const mocks = vi.hoisted(() => ({
  roles: ['LOGISTICS_MINISTER'],
  overview: vi.fn(), cameras: vi.fn(), saveCamera: vi.fn(), savePolicy: vi.fn(), records: vi.fn()
}))
vi.mock('../../utils/auth', () => ({ getRoles: () => mocks.roles }))
vi.mock('../../utils/request', () => ({ default: { post: vi.fn() } }))
vi.mock('../../api/logisticsVideo', () => ({
  getVideoOverview: mocks.overview, getVideoCameras: mocks.cameras, saveVideoCamera: mocks.saveCamera,
  saveVideoPolicy: mocks.savePolicy, getVideoRecordings: mocks.records,
  holdVideoRecording: vi.fn(), getVideoPlayback: vi.fn(), getVideoContent: vi.fn()
}))

let wrapper: ReturnType<typeof mount> | undefined
beforeEach(() => {
  mocks.roles = ['LOGISTICS_MINISTER']
  mocks.overview.mockResolvedValue({ cameraCount: 0, enabledCount: 0, missingCount: 0, cameraHealth: [],
    policy: { provider: 'UNDECIDED', retentionDays: 90, autoCleanup: false },
    storageConfigured: false, maxSegmentBytes: 10485760, estimatedTb: 0, uploadMbps: 0, checkedAt: '2026-11-15T10:00:00' })
  mocks.cameras.mockResolvedValue({ list: [], total: 0 })
  mocks.records.mockResolvedValue({ list: [], total: 0 })
  window.matchMedia = vi.fn().mockReturnValue({ matches: false, addListener: vi.fn(), removeListener: vi.fn() })
  vi.stubGlobal('ResizeObserver', class { observe() {} unobserve() {} disconnect() {} })
})
afterEach(() => { wrapper?.unmount(); document.body.innerHTML = ''; vi.clearAllMocks() })

describe('VideoManagement', () => {
  it('opens the camera form through its visible action and switches to storage policy', async () => {
    wrapper = mount(VideoManagement, { attachTo: document.body, global: { plugins: [Antd] } })
    await flushPromises()
    const add = wrapper.findAll('button').find(button => button.text() === '新增摄像头')!
    await add.trigger('click')
    await flushPromises()
    expect(document.body.textContent).toContain('采集网关通道标识')
    expect(document.querySelector('[role="dialog"]')).not.toBeNull()
    const close = document.querySelector<HTMLButtonElement>('.ant-modal-close')!
    close.click()
    await flushPromises()
    const policy = wrapper.findAll('[role="tab"]').find(item => item.text() === '存储与留存策略')!
    await policy.trigger('click')
    await flushPromises()
    expect(wrapper.text()).toContain('本地专用存储')
    expect(wrapper.text()).toContain('阿里云 OSS')
  })

  it('hides recording access and mutation actions from logistics employees', async () => {
    mocks.roles = ['LOGISTICS_EMPLOYEE']
    wrapper = mount(VideoManagement, { attachTo: document.body, global: { plugins: [Antd] } })
    await flushPromises()
    expect(wrapper.findAll('button').some(button => button.text() === '新增摄像头')).toBe(false)
    expect(wrapper.findAll('[role="tab"]').some(tab => tab.text() === '录像查询与保全')).toBe(false)
    expect(mocks.records).not.toHaveBeenCalled()
  })
})
