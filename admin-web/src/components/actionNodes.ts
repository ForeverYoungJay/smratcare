import { Comment, Fragment, Text, isVNode, type VNode } from 'vue'

function childrenOf(node: VNode): unknown[] {
  if (Array.isArray(node.children)) return node.children
  if (typeof node.children === 'string') return [node.children]
  const slot = (node.children as { default?: () => unknown } | null)?.default
  const result = typeof slot === 'function' ? slot() : []
  return Array.isArray(result) ? result : [result]
}

export function actionNodes(nodes: unknown[]): VNode[] {
  return nodes.flatMap((node) => {
    if (!isVNode(node) || node.type === Comment || (node.type === Text && !String(node.children || '').trim())) return []
    const name = typeof node.type === 'object' ? (node.type as { name?: string }).name : ''
    // Keep permission directives and refs on their original owner.
    if (!node.dirs?.length && !node.ref && (node.type === Fragment || name === 'ASpace' || name === 'AFormItem')) {
      return actionNodes(childrenOf(node))
    }
    return [node]
  })
}

export function splitActions(nodes: VNode[]) {
  const controls = actionNodes(nodes)
  if (controls.length <= 2) return { visible: controls, more: [] }
  const primary = controls.findIndex((node) => node.props?.type === 'primary')
  const first = primary >= 0 ? primary : 0
  const label = (node: VNode) => {
    const name = typeof node.type === 'object' ? (node.type as { name?: string }).name : ''
    if (node.type !== 'button' && name !== 'AButton') return ''
    return childrenOf(node).map((child) => typeof child === 'string' ? child : isVNode(child) && typeof child.children === 'string' ? child.children : '').join('').replace(/\s/g, '')
  }
  const reset = controls.findIndex((node, index) => index !== first && /^(重置|清空|清除条件)$/.test(label(node)))
  const second = reset >= 0 ? reset : controls.findIndex((node, index) => index !== first && !node.props?.danger)
  return {
    visible: controls.filter((_, index) => index === first || index === second),
    more: controls.filter((_, index) => index !== first && index !== second)
  }
}
