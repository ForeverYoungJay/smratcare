import type { App } from 'vue'
import { defineComponent, h, onBeforeUnmount, ref, watch } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'

export const RichTextEditor = defineComponent({
  name: 'RichTextEditor',
  props: {
    modelValue: { type: String, default: '' }
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const editorRef = ref<any>(null)
    const value = ref(props.modelValue)

    watch(
      () => props.modelValue,
      (val) => {
        if (val !== value.value) value.value = val
      }
    )

    const onChange = (editor: any) => {
      emit('update:modelValue', editor.getHtml())
    }

    onBeforeUnmount(() => {
      if (editorRef.value) {
        editorRef.value.destroy()
      }
    })

    return () =>
      h('div', { class: 'rich-editor' }, [
        h(Toolbar as any, { editor: editorRef.value, defaultConfig: {} }),
        h(Editor as any, {
          modelValue: value.value,
          'onUpdate:modelValue': (val: string) => (value.value = val),
          onOnChange: onChange,
          onOnCreated: (editor: any) => (editorRef.value = editor),
          defaultConfig: { placeholder: '请输入内容' },
          style: 'height: 240px; border: 1px solid #e5e7eb;'
        })
      ])
  }
})

export function setupEditor(app: App) {
  app.component('RichTextEditor', RichTextEditor)
}
