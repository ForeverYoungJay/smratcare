import { Modal } from 'ant-design-vue'

export interface ActionConfirmOptions {
  title: string
  content?: string
  impactItems?: string[]
  okText?: string
  cancelText?: string
  danger?: boolean
}

export function confirmAction(options: ActionConfirmOptions): Promise<boolean> {
  const {
    title,
    content,
    impactItems = [],
    okText = '确认',
    cancelText = '取消',
    danger = false
  } = options
  const impactHtml = impactItems.length
    ? `<div style="margin-top:8px;"><div style="margin-bottom:4px;color:#64748b;">影响范围：</div><ul style="padding-left:18px;margin:0;">${impactItems
        .map(item => `<li style="margin:2px 0;">${item}</li>`)
        .join('')}</ul></div>`
    : ''
  return new Promise(resolve => {
    Modal.confirm({
      title,
      content: content
        ? `${content}${impactHtml ? `<br/>${impactHtml}` : ''}`
        : impactHtml || undefined,
      okText,
      cancelText,
      okType: danger ? 'danger' : 'primary',
      onOk: () => resolve(true),
      onCancel: () => resolve(false)
    })
  })
}
