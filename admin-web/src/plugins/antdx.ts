import type { App } from 'vue'
import { defineComponent, h, ref } from 'vue'
import { Bubble } from 'ant-design-x-vue'

export const AdxChat = defineComponent({
  name: 'AdxChat',
  setup() {
    const input = ref('')
    const messages = ref([
      { id: 1, role: 'assistant', content: '欢迎使用智能助手。' }
    ])

    const send = () => {
      if (!input.value.trim()) return
      const text = input.value.trim()
      messages.value.push({ id: Date.now(), role: 'user', content: text })
      input.value = ''
      setTimeout(() => {
        messages.value.push({ id: Date.now() + 1, role: 'assistant', content: `收到：${text}` })
      }, 600)
    }

    return () =>
      h('div', { class: 'adx-chat' }, [
        h('div', { class: 'adx-messages' },
          messages.value.map((msg) =>
            h(Bubble as any, {
              key: msg.id,
              content: msg.content,
              placement: msg.role === 'user' ? 'end' : 'start',
              variant: msg.role === 'user' ? 'filled' : 'outlined',
              type: msg.role === 'user' ? 'primary' : 'default'
            })
          )
        ),
        h('div', { class: 'adx-input' }, [
          h('input', {
            value: input.value,
            onInput: (e: any) => (input.value = e.target.value),
            placeholder: '输入消息...'
          }),
          h('button', { onClick: send }, '发送')
        ])
      ])
  }
})

export function setupAntdx(app: App) {
  app.component('AdxBubble', Bubble)
  app.component('AdxChat', AdxChat)
}
