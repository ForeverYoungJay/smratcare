import { Modal } from 'ant-design-vue'

export function useConfirmDelete() {
  return (onOk: () => void, content = '确认删除该记录？') => {
    Modal.confirm({
      title: '删除确认',
      content,
      okType: 'danger',
      onOk
    })
  }
}
