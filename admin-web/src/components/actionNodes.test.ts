import { describe, expect, it } from 'vitest'
import { Comment, Fragment, h } from 'vue'
import { actionNodes, splitActions } from './actionNodes'

describe('business action layout preserves controls', () => {
  it('keeps every event, disabled flag, and original vnode when moving secondary actions', () => {
    const click = () => {}
    const nodes = [h('button', { disabled: true, onClick: click }, '编辑'), h('button', { type: 'primary', onClick: click }, '新增'), h('button', { danger: true, onClick: click }, '删除'), h('input', { type: 'file' })]
    const result = splitActions(nodes)
    expect(result.visible).toEqual([nodes[0], nodes[1]])
    expect(result.more).toEqual([nodes[2], nodes[3]])
    expect(result.more[0]).toBe(nodes[2])
    expect(result.visible[0].props?.disabled).toBe(true)
    expect(result.more[0].props?.onClick).toBe(click)
  })
  it('keeps reset beside query even when export precedes it', () => {
    const nodes = [h('button', { type: 'primary' }, '查询'), h('button', '导出'), h('button', '重置')]
    expect(splitActions(nodes).visible).toEqual([nodes[0], nodes[2]])
  })
  it('flattens only transparent spaces/fragments and retains upload/dropdown/permission owners', () => {
    const upload = h({ name: 'AUpload' }, { beforeUpload: () => false }, { default: () => h('button', '导入') })
    const protectedSpace = h({ name: 'ASpace' }, {}, { default: () => h('button', '管理员操作') })
    protectedSpace.dirs = [{ dir: {}, instance: null, value: false, oldValue: undefined, arg: undefined, modifiers: {} }]
    const space = h({ name: 'ASpace' }, {}, { default: () => [upload, protectedSpace] })
    expect(actionNodes([h(Comment), h(Fragment, [space])])).toEqual([upload, protectedSpace])
  })
})
