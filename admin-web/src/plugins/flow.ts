import type { App } from 'vue'
import { defineComponent, h, onMounted, onBeforeUnmount, ref } from 'vue'
import LogicFlow from '@logicflow/core'
import '@logicflow/core/dist/index.css'
import '@logicflow/extension/dist/index.css'

export const FlowDesigner = defineComponent({
  name: 'FlowDesigner',
  props: {
    data: { type: Object, default: () => ({ nodes: [], edges: [] }) }
  },
  emits: ['update:data'],
  setup(props, { emit }) {
    const containerRef = ref<HTMLDivElement | null>(null)
    let lf: LogicFlow | null = null

    onMounted(() => {
      if (!containerRef.value) return
      lf = new LogicFlow({
        container: containerRef.value,
        grid: true,
        background: { color: '#f8fafc' }
      })
      lf.render(props.data as any)
      lf.on('graph:updated', ({ data }) => {
        emit('update:data', data)
      })
    })

    onBeforeUnmount(() => {
      if (lf) {
        lf.destroy()
        lf = null
      }
    })

    return () => h('div', { ref: containerRef, style: 'height: 360px; border: 1px solid #e5e7eb; border-radius: 8px;' })
  }
})

export function setupFlow(app: App) {
  app.component('FlowDesigner', FlowDesigner)
}
