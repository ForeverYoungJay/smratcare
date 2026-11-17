import { expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import { defineComponent, h, ref } from 'vue'
import { completionGuideKey, useCompletionGuide, type CompletionGuide } from './useCompletionGuide'

it('ignores a late save response after its originating page has unmounted', () => {
  const state = ref<CompletionGuide | null>(null)
  let complete!: ReturnType<typeof useCompletionGuide>
  const host = defineComponent({ setup() { complete = useCompletionGuide(); return () => h('div') } })
  const wrapper = mount(host, { global: { provide: { [completionGuideKey as symbol]: state } } })
  complete('已保存', [])
  expect(state.value?.title).toBe('已保存')
  state.value = null
  wrapper.unmount()
  complete('迟到的结果', [])
  expect(state.value).toBeNull()
})
