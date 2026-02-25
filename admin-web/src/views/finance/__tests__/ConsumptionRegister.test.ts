import { describe, it, expect, vi, beforeEach } from 'vitest'
import { defineComponent } from 'vue'
import { mount } from '@vue/test-utils'
import ConsumptionRegister from '../ConsumptionRegister.vue'

const getConsumptionPage = vi.fn().mockResolvedValue({ list: [], total: 0, pageNo: 1, pageSize: 10 })

vi.mock('../../../api/financeFee', () => ({
  getConsumptionPage,
  createConsumption: vi.fn().mockResolvedValue(null)
}))

vi.mock('../../../api/elder', () => ({
  getElderPage: vi.fn().mockResolvedValue({ list: [], total: 0, pageNo: 1, pageSize: 20 })
}))

const message = {
  success: vi.fn(),
  error: vi.fn(),
  warning: vi.fn()
}

vi.mock('ant-design-vue', () => ({
  message
}))

const SearchFormStub = defineComponent({
  emits: ['search', 'reset'],
  template: `
    <div>
      <slot />
      <button class="search-btn" @click="$emit('search')">search</button>
      <button class="reset-btn" @click="$emit('reset')">reset</button>
      <slot name="extra" />
    </div>
  `
})

const DatePickerStub = defineComponent({
  props: { value: { type: [String, Object], default: undefined } },
  emits: ['update:value'],
  template: `<input class="date-picker" :value="value" @input="$emit('update:value', $event.target.value)" />`
})

const InputStub = defineComponent({
  props: { value: { type: String, default: '' } },
  emits: ['update:value'],
  template: `<input class="text-input" :value="value" @input="$emit('update:value', $event.target.value)" />`
})

const ButtonStub = defineComponent({
  emits: ['click'],
  template: `<button class="a-button" @click="$emit('click')"><slot /></button>`
})

const globalStubs = {
  PageContainer: { template: '<div><slot /></div>' },
  SearchForm: SearchFormStub,
  DataTable: { template: '<div />' },
  'a-form': { template: '<form><slot /></form>' },
  'a-form-item': { template: '<div><slot /></div>' },
  'a-input': InputStub,
  'a-date-picker': DatePickerStub,
  'a-input-number': { template: '<input />' },
  'a-select': { template: '<select><slot /></select>' },
  'a-button': ButtonStub,
  'a-modal': { template: '<div><slot /></div>' }
}

const waitTick = async () => {
  await Promise.resolve()
  await Promise.resolve()
}

describe('ConsumptionRegister', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    getConsumptionPage.mockResolvedValue({ list: [], total: 0, pageNo: 1, pageSize: 10 })
  })

  it('blocks invalid date range and does not request page api', async () => {
    const wrapper = mount(ConsumptionRegister, {
      global: { stubs: globalStubs }
    })
    await waitTick()
    expect(getConsumptionPage).toHaveBeenCalledTimes(1)

    const dateInputs = wrapper.findAll('input.date-picker')
    await dateInputs[0].setValue('2026-02-20')
    await dateInputs[1].setValue('2026-02-01')
    await wrapper.find('button.search-btn').trigger('click')
    await waitTick()

    expect(message.error).toHaveBeenCalledWith('开始日期不能晚于结束日期')
    expect(getConsumptionPage).toHaveBeenCalledTimes(1)
  })
})
