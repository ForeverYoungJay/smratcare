import { onMounted, onUnmounted } from 'vue'
import { subscribeLiveSync, type LiveSyncTopic } from '../utils/liveSync'

interface UseLiveSyncRefreshOptions {
  topics: LiveSyncTopic[]
  refresh: () => void | Promise<void>
  debounceMs?: number
}

export function useLiveSyncRefresh(options: UseLiveSyncRefreshOptions) {
  const debounceMs = options.debounceMs ?? 500
  let timer: number | undefined

  const triggerRefresh = () => {
    if (timer) window.clearTimeout(timer)
    timer = window.setTimeout(() => {
      options.refresh()
    }, debounceMs)
  }

  let unsubscribe = () => {}
  onMounted(() => {
    unsubscribe = subscribeLiveSync((payload) => {
      const hit = payload.topics.some((topic) => options.topics.includes(topic))
      if (hit) triggerRefresh()
    })
  })

  onUnmounted(() => {
    unsubscribe()
    if (timer) window.clearTimeout(timer)
  })
}

