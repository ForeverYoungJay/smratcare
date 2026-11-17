import { Comment, Fragment, isVNode, type VNode } from 'vue'

// Reorder the original nodes, keeping models, directives and handlers attached.
export function commonQueryFields(nodes: VNode[], path: string): VNode[] {
  const fields = nodes.flatMap(node => node.type === Fragment && Array.isArray(node.children)
    ? commonQueryFields(node.children.filter(isVNode), path) : node.type === Comment ? [] : [node])
  const labels = path.startsWith('/hr') ? [/姓名|员工|关键/, /部门|月份|日期/]
    : /\/(finance|medical|health|care|elder)/.test(path) ? [/长者|老人|患者|姓名|关键/, /账期|月份|日期|时间/]
      : path.startsWith('/marketing') ? [/姓名|电话|关键/, /负责人|跟进人|日期/]
        : [/关键|名称|姓名/, /状态|部门|日期/]
  const rank = (node: VNode) => {
    const label = String(node.props?.label || '')
    const index = labels.findIndex(pattern => pattern.test(label))
    return index < 0 ? 2 : index
  }
  return fields.map((node, index) => ({ node, index })).sort((a, b) => rank(a.node) - rank(b.node) || a.index - b.index).map(item => item.node)
}
