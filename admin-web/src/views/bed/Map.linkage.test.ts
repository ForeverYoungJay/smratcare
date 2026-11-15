import { mount, flushPromises } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import BedMap from './Map.vue'
import { emitLiveSync, inferLiveSyncTopics } from '../../utils/liveSync'
import { resetBedMapDatasetState } from '../../composables/useBedMapDataset'

const getBedMap = vi.hoisted(() => vi.fn())
const getRoomList = vi.hoisted(() => vi.fn())
const getBaseConfigItemList = vi.hoisted(() => vi.fn())
const currentRoles = vi.hoisted(() => ({ value: ['LOGISTICS_MINISTER'] as string[] }))

vi.mock('./components/FloorPlanBoard.vue', () => ({
  default: {
    name: 'FloorPlanBoard',
    template: '<div class="floor-plan-board-stub"></div>'
  }
}))

vi.mock('../../api/bed', () => ({
  getBedMap,
  getRoomList
}))

vi.mock('../../utils/auth', () => ({ getRoles: () => currentRoles.value }))

vi.mock('../../api/baseConfig', () => ({
  getBaseConfigItemList
}))

vi.mock('../../api/elder', () => ({
  getElderDetail: vi.fn()
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn(),
    replace: vi.fn()
  }),
  useRoute: () => ({
    query: {},
    params: {},
    path: '/logistics/assets/room-state-map',
    fullPath: '/logistics/assets/room-state-map'
  })
}))

vi.mock('ant-design-vue', () => ({
  message: {
    warning: vi.fn(),
    success: vi.fn()
  }
}))

describe('BedMap live linkage', () => {
  beforeEach(() => {
    resetBedMapDatasetState()
    currentRoles.value = ['LOGISTICS_MINISTER']
    vi.useFakeTimers()
    getBedMap.mockResolvedValue([
      { id: 1, roomId: 101, bedNo: '01', roomNo: 'A101', building: 'A栋', floorNo: '1F', status: 1 }
    ])
    getBaseConfigItemList.mockResolvedValue([])
    getRoomList.mockResolvedValue([])
  })

  afterEach(() => {
    vi.clearAllMocks()
    vi.useRealTimers()
  })

  it('refreshes bed map when room data changes in management', async () => {
    mount(BedMap, {
      global: {
        stubs: {
          PageContainer: { template: '<div><slot /></div>' },
          // a-table-column 的作用域插槽在未注册组件下会以 undefined 入参渲染，显式 stub 避免误报
          'a-table': true,
          'a-table-column': true
        }
      }
    })
    await flushPromises()

    expect(getBedMap).toHaveBeenCalledTimes(1)
    expect(getBaseConfigItemList).toHaveBeenCalledTimes(3)

    emitLiveSync({
      topics: inferLiveSyncTopics('/api/room/1001'),
      method: 'PUT',
      url: '/api/room/1001',
      at: Date.now()
    })

    vi.advanceTimersByTime(600)
    await flushPromises()

    expect(getBedMap).toHaveBeenCalledTimes(2)
    expect(getBaseConfigItemList).toHaveBeenCalledTimes(6)
  })

  it('ignores unrelated live-sync topics', async () => {
    mount(BedMap, {
      global: {
        stubs: {
          PageContainer: { template: '<div><slot /></div>' },
          // a-table-column 的作用域插槽在未注册组件下会以 undefined 入参渲染，显式 stub 避免误报
          'a-table': true,
          'a-table-column': true
        }
      }
    })
    await flushPromises()

    expect(getBedMap).toHaveBeenCalledTimes(1)

    emitLiveSync({
      topics: ['oa'],
      method: 'PUT',
      url: '/api/oa/todo/1',
      at: Date.now()
    })

    vi.advanceTimersByTime(600)
    await flushPromises()

    expect(getBedMap).toHaveBeenCalledTimes(1)
  })

  it('does not request manager-only residence config for logistics employees', async () => {
    currentRoles.value = ['LOGISTICS_EMPLOYEE']
    mount(BedMap, {
      global: { stubs: { PageContainer: { template: '<div><slot /></div>' }, 'a-table': true, 'a-table-column': true } }
    })
    await flushPromises()
    expect(getBedMap).toHaveBeenCalledOnce()
    expect(getRoomList).toHaveBeenCalledOnce()
    expect(getBaseConfigItemList).not.toHaveBeenCalled()
  })
})
