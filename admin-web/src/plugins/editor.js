import { defineComponent, h, onBeforeUnmount, ref, watch } from 'vue';
import { Editor, Toolbar } from '@wangeditor/editor-for-vue';
import '@wangeditor/editor/dist/css/style.css';
export const RichTextEditor = defineComponent({
    name: 'RichTextEditor',
    props: {
        modelValue: { type: String, default: '' }
    },
    emits: ['update:modelValue'],
    setup(props, { emit }) {
        const editorRef = ref(null);
        const value = ref(props.modelValue);
        watch(() => props.modelValue, (val) => {
            if (val !== value.value)
                value.value = val;
        });
        const onChange = (editor) => {
            emit('update:modelValue', editor.getHtml());
        };
        onBeforeUnmount(() => {
            if (editorRef.value) {
                editorRef.value.destroy();
            }
        });
        return () => h('div', { class: 'rich-editor' }, [
            h(Toolbar, { editor: editorRef.value, defaultConfig: {} }),
            h(Editor, {
                modelValue: value.value,
                'onUpdate:modelValue': (val) => (value.value = val),
                onOnChange: onChange,
                onOnCreated: (editor) => (editorRef.value = editor),
                defaultConfig: { placeholder: '请输入内容' },
                style: 'height: 240px; border: 1px solid #e5e7eb;'
            })
        ]);
    }
});
export function setupEditor(app) {
    app.component('RichTextEditor', RichTextEditor);
}
