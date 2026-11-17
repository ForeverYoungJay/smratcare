<script lang="ts">
import { defineComponent, Fragment, h, inject } from 'vue'
import { Button, Popover, Space } from 'ant-design-vue'
import { splitActions } from './actionNodes'

export default defineComponent({
  name: 'ActionToolbar',
  setup(_props, { slots, attrs }) {
    const enabled = inject('business-page-layout', false)
    return () => {
      const nodes = slots.default?.() || []
      if (!enabled) return h(Fragment, nodes)
      const { visible, more } = splitActions(nodes)
      return h(Space, { ...attrs, wrap: true, class: ['business-actions', attrs.class] }, { default: () => [
        ...visible,
        ...(more.length ? [h(Popover, { trigger: 'click', placement: 'bottomRight' }, {
          default: () => h(Button, { 'aria-label': '更多操作' }, () => '更多操作'),
          content: () => h('div', { class: 'business-actions__more', 'aria-label': '其他操作' }, more)
        })] : [])
      ] })
    }
  }
})
</script>
<style>
.business-actions { align-items:center; }
.business-actions__more { display:flex; flex-wrap:wrap; align-items:center; gap:8px; max-width:min(440px, calc(100vw - 48px)); }
.business-actions__more > * { max-width:100%; }
.business-actions__more .ant-btn { min-height:36px; }
</style>
