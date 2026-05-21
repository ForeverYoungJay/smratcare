import { mount, flushPromises } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import BedPanorama from './BedPanorama.vue'
import { emitLiveSync, inferLiveSyncTopics } from '../../../utils/liveSync'

const { getBedMap, getRoomList } = vi.hoisted(() => ({
  getBedMap: vi.fn(),
  getRoomList: vi.fn()
}))

vi.mock('../../../api/bed', () => ({
  getBedMap,
  getRoomList
}))

vi.mock('vue-router', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-router')>()
  return {
    ...actual,
    useRouter: () => ({
      push: vi.fn(),
      replace: vi.fn()
    }),
    useRoute: () => ({
      path: '/elder/bed-panorama',
      query: {}
    })
  }
})

vi.mock('ant-design-vue', () => ({
  message: {
    warning: vi.fn(),
    success: vi.fn()
  }
}))

vi.mock('./Panorama3D.vue', () => ({
  default: {
    name: 'Panorama3D',
    props: ['scope'],
    emits: ['scope-change', 'click-room', 'click-bed'],
    template: '<div class="panorama3d-stub" :data-scope="JSON.stringify(scope)"></div>'
  }
}))

vi.mock('../../../stores/user', () => ({
  useUserStore: () => ({
    staffInfo: {
      realName: '系统管理员',
      username: 'admin'
    },
    roles: ['ADMIN']
  })
}))

describe('BedPanorama live linkage', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    getBedMap.mockResolvedValue([
      { id: 1, roomId: 101, bedNo: '01', roomNo: 'A101', building: 'A栋', floorNo: '1F', status: 1 }
    ])
    getRoomList.mockResolvedValue([
      { id: 101, roomNo: 'A101', roomType: 'STANDARD', capacity: 2 }
    ])
  })

  afterEach(() => {
    vi.clearAllMocks()
    vi.useRealTimers()
  })

  it('refreshes panorama when management updates room/bed data', async () => {
    mount(BedPanorama, {
      global: {
        stubs: {
          PageContainer: { template: '<div><slot /></div>' },
          'a-avatar': { template: '<div><slot /></div>' },
          'a-button': { template: '<button><slot /></button>' },
          'a-empty': { template: '<div><slot /></div>' },
          'a-input-search': { template: '<input />' },
          'a-modal': { template: '<div><slot /></div>' },
          'a-space': { template: '<div><slot /></div>' },
          'a-switch': { template: '<button />' },
          'a-segmented': { template: '<div />' },
          'a-descriptions': { template: '<div><slot /></div>' },
          'a-descriptions-item': { template: '<div><slot /></div>' },
          'a-table': { template: '<div><slot /></div>' },
          'a-table-column': { template: '<div><slot /></div>' },
          'v-chart': { template: '<div class="chart-stub" />' },
          AnimatedMetricNumber: { props: ['value', 'suffix'], template: '<span>{{ value }}{{ suffix }}</span>' }
        }
      }
    })
    await flushPromises()

    expect(getBedMap).toHaveBeenCalledTimes(1)
    expect(getRoomList).toHaveBeenCalledTimes(1)

    emitLiveSync({
      topics: inferLiveSyncTopics('/api/room/1001'),
      method: 'PUT',
      url: '/api/room/1001',
      at: Date.now()
    })

    vi.advanceTimersByTime(600)
    await flushPromises()

    expect(getBedMap).toHaveBeenCalledTimes(2)
    expect(getRoomList).toHaveBeenCalledTimes(2)
  })

  it('does not refresh panorama for unrelated live-sync topics', async () => {
    mount(BedPanorama, {
      global: {
        stubs: {
          PageContainer: { template: '<div><slot /></div>' },
          'a-avatar': { template: '<div><slot /></div>' },
          'a-button': { template: '<button><slot /></button>' },
          'a-empty': { template: '<div><slot /></div>' },
          'a-input-search': { template: '<input />' },
          'a-modal': { template: '<div><slot /></div>' },
          'a-space': { template: '<div><slot /></div>' },
          'a-switch': { template: '<button />' },
          'a-segmented': { template: '<div />' },
          'a-descriptions': { template: '<div><slot /></div>' },
          'a-descriptions-item': { template: '<div><slot /></div>' },
          'a-table': { template: '<div><slot /></div>' },
          'a-table-column': { template: '<div><slot /></div>' },
          'v-chart': { template: '<div class="chart-stub" />' },
          AnimatedMetricNumber: { props: ['value', 'suffix'], template: '<span>{{ value }}{{ suffix }}</span>' }
        }
      }
    })
    await flushPromises()

    expect(getBedMap).toHaveBeenCalledTimes(1)
    expect(getRoomList).toHaveBeenCalledTimes(1)

    emitLiveSync({
      topics: ['oa'],
      method: 'PUT',
      url: '/api/oa/todo/1',
      at: Date.now()
    })

    vi.advanceTimersByTime(600)
    await flushPromises()

    expect(getBedMap).toHaveBeenCalledTimes(1)
    expect(getRoomList).toHaveBeenCalledTimes(1)
  })

  it('syncs page scope when panorama emits scope-change', async () => {
    const wrapper = mount(BedPanorama, {
      global: {
        stubs: {
          PageContainer: { template: '<div><slot /></div>' },
          'a-avatar': { template: '<div><slot /></div>' },
          'a-button': { template: '<button><slot /></button>' },
          'a-empty': { template: '<div><slot /></div>' },
          'a-input-search': { template: '<input />' },
          'a-modal': { template: '<div><slot /></div>' },
          'a-space': { template: '<div><slot /></div>' },
          'a-switch': { template: '<button />' },
          'a-segmented': { template: '<div />' },
          'a-descriptions': { template: '<div><slot /></div>' },
          'a-descriptions-item': { template: '<div><slot /></div>' },
          'a-table': { template: '<div><slot /></div>' },
          'a-table-column': { template: '<div><slot /></div>' },
          'v-chart': { template: '<div class="chart-stub" />' },
          AnimatedMetricNumber: { props: ['value', 'suffix'], template: '<span>{{ value }}{{ suffix }}</span>' }
        }
      }
    })
    await flushPromises()

    const panorama = wrapper.findComponent({ name: 'Panorama3D' })
    panorama.vm.$emit('scope-change', {
      level: 'FLOOR',
      building: 'A栋',
      floor: '1F',
      room: ''
    })
    await flushPromises()

    expect(wrapper.text()).toContain('A栋 / 1F')
    expect(wrapper.text()).toContain('楼层展开')

    panorama.vm.$emit('scope-change', {
      level: 'PARK',
      building: '',
      floor: '',
      room: ''
    })
    await flushPromises()

    expect(wrapper.text()).toContain('全部楼栋 / 全部楼层')
    expect(wrapper.text()).toContain('园区总览')
  })
})
