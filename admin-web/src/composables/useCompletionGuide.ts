import { inject, onActivated, onDeactivated, onScopeDispose, ref, type InjectionKey, type Ref } from 'vue'

export interface CompletionGuide { title: string; actions: { label: string; path: string }[] }
export const completionGuideKey: InjectionKey<Ref<CompletionGuide | null>> = Symbol('completion-guide')
export function useCompletionGuide() {
  const state = inject(completionGuideKey, ref<CompletionGuide | null>(null))
  let active = true
  onActivated(() => { active = true })
  onDeactivated(() => { active = false })
  onScopeDispose(() => { active = false })
  return (title: string, actions: CompletionGuide['actions']) => { if (active) state.value = { title, actions } }
}
