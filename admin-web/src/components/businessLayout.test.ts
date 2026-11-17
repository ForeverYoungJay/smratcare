import { describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { defineComponent, h, nextTick, ref, withDirectives } from 'vue'
import { Button, Form, Input, Space } from 'ant-design-vue'
import QueryBar from './QueryBar.vue'
import ActionToolbar from './ActionToolbar.vue'
import SearchForm from './SearchForm.vue'
import DataTable from './DataTable.vue'
import { permission } from '../directives/permission'

vi.mock('../utils/auth', () => ({ getRoles: () => ['HR_EMPLOYEE'] }))

vi.stubGlobal('matchMedia', () => ({ matches: false, addListener: () => {}, removeListener: () => {}, addEventListener: () => {}, removeEventListener: () => {} }))
const global = { provide: { 'business-page-layout': true }, components: { AForm: Form, AFormItem: Form.Item, AButton: Button, AInput: Input } }
describe('unified query/action interactions', () => {
  it('expands filters without changing data and forwards the original query/reset events', async () => {
    const query = { keyword: '张', month: '2026-11', status: 'PENDING' }
    const wrapper = mount(SearchForm, { props: { model: query }, global, attachTo: document.body, slots: { default: () => [
      h(Form.Item, { label: '姓名' }, () => h(Input)), h(Form.Item, { label: '月份' }, () => h(Input)), h(Form.Item, { label: '状态' }, () => h(Input))
    ] } })
    await nextTick()
    const more = wrapper.findAll('button').find(button => button.text() === '更多筛选')!
    await more.trigger('click')
    expect(more.attributes('aria-expanded')).toBe('true')
    expect(query.status).toBe('PENDING')
    wrapper.findAll('button').find(button => button.text().replace(/\s/g, '') === '查询')!.element.click()
    await flushPromises()
    expect(wrapper.emitted('search')).toHaveLength(1)
    query.keyword = '李'
    await wrapper.findAll('button').find(button => button.text().replace(/\s/g, '') === '清空')!.trigger('click')
    expect(query.keyword).toBe('张')
    expect(wrapper.emitted('reset')).toHaveLength(1)
    wrapper.unmount()
  })
  it('preserves form submission and autocomplete events on QueryBar', async () => {
    const finish = vi.fn(), autocomplete = vi.fn()
    const wrapper = mount(QueryBar, { props: { model: {}, onFinish: finish }, attrs: { onElderAutosearch: autocomplete }, global })
    await wrapper.find('form').trigger('submit')
    await flushPromises()
    expect(finish).toHaveBeenCalledTimes(1)
    wrapper.findComponent(Form).vm.$emit('elder-autosearch')
    expect(autocomplete).toHaveBeenCalledTimes(1)
    wrapper.unmount()
  })
  it('updates disabled actions reactively and keeps secondary handlers', async () => {
    const enabled = ref(false), remove = vi.fn()
    const host = defineComponent({ setup: () => () => h(ActionToolbar, {}, { default: () => [
      h(Button, { type: 'primary' }, () => '新增'), h(Button, { disabled: !enabled.value }, () => '编辑'), h(Button, { danger: true, onClick: remove }, () => '删除')
    ] }) })
    const wrapper = mount(host, { global, attachTo: document.body })
    expect(wrapper.findAll('button').find(b => b.text().replace(/\s/g, '') === '编辑')?.attributes('disabled')).toBeDefined()
    enabled.value = true; await nextTick()
    expect(wrapper.findAll('button').find(b => b.text().replace(/\s/g, '') === '编辑')?.attributes('disabled')).toBeUndefined()
    await wrapper.findAll('button').find(b => b.text() === '更多操作')!.trigger('click')
    await new Promise(resolve => setTimeout(resolve, 180))
    const button = Array.from(document.querySelectorAll('button')).find(b => b.textContent?.replace(/\s/g, '') === '删除')!
    button.click(); expect(remove).toHaveBeenCalledTimes(1)
    wrapper.unmount()
  })
  it('keeps permission directives effective inside secondary actions', async () => {
    const host = defineComponent({ setup: () => () => h(ActionToolbar, {}, { default: () => [
      h(Button, { type: 'primary' }, () => '查询'), h(Button, {}, () => '重置'),
      withDirectives(h(Space, {}, { default: () => h(Button, {}, () => '越权操作') }), [[permission, ['SYS_ADMIN']]])
    ] }) })
    const wrapper = mount(host, { global, attachTo: document.body })
    await wrapper.findAll('button').find(b => b.text() === '更多操作')!.trigger('click')
    await new Promise(resolve => setTimeout(resolve, 180))
    expect(Array.from(document.querySelectorAll('button')).some(b => b.textContent === '越权操作')).toBe(false)
    wrapper.unmount()
  })

  it('clears selection through the existing parent handler without mutating keys', async () => {
    const change = vi.fn(), selectedRowKeys = [42]
    const wrapper = mount(DataTable, {
      props: { columns: [{ key: 'name', title: '姓名' }], dataSource: [], rowSelection: { selectedRowKeys, onChange: change } },
      global: { ...global, provide: { ...global.provide, 'function-workspace-embedded': true }, stubs: { ATable: true, TableEmpty: true } }
    })
    expect(wrapper.get('[role="status"]').text()).toContain('已选 1 条')
    await wrapper.find('button').trigger('click')
    expect(change).toHaveBeenCalledWith([], [])
    expect(selectedRowKeys).toEqual([42])
    wrapper.unmount()
  })

})
