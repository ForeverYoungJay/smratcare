import { describe, expect, it, vi, beforeEach } from 'vitest'
import { shallowMount } from '@vue/test-utils'
import EnterpriseHome from '../EnterpriseHome.vue'

vi.mock('ant-design-vue', async (importOriginal) => {
  const actual = await importOriginal<typeof import('ant-design-vue')>()
  return {
    ...actual,
    message: {
      success: vi.fn(),
      error: vi.fn(),
      warning: vi.fn(),
      info: vi.fn()
    }
  }
})

describe('EnterpriseHome', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('renders homepage content', () => {
    const wrapper = shallowMount(EnterpriseHome, {
      global: {
        stubs: {
          teleport: true
        }
      }
    })

    expect(wrapper.text()).toContain('山水有情，颐养有爱')
    expect(wrapper.text()).toContain('预约参观')
    expect(wrapper.get('a[href="/admin"]').text()).toContain('后台管理')
    expect(wrapper.text()).toContain('专业团队与安全保障')
  })
})
