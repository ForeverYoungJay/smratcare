export type LiveSyncTopic =
  | 'elder'
  | 'bed'
  | 'lifecycle'
  | 'finance'
  | 'care'
  | 'health'
  | 'dining'
  | 'marketing'
  | 'oa'
  | 'system'

export interface LiveSyncPayload {
  topics: LiveSyncTopic[]
  method: string
  url: string
  at: number
}

const LIVE_SYNC_EVENT = 'smratcare:live-sync'
const CHANNEL_NAME = 'smratcare-live-sync'

const topicRules: Array<{ pattern: RegExp; topics: LiveSyncTopic[] }> = [
  { pattern: /^\/api\/elder\//, topics: ['elder'] },
  { pattern: /^\/api\/elder\/lifecycle\//, topics: ['elder', 'lifecycle', 'bed', 'finance', 'care', 'dining'] },
  { pattern: /^\/api\/bed\//, topics: ['bed', 'elder'] },
  { pattern: /^\/api\/asset\//, topics: ['bed'] },
  { pattern: /^\/api\/finance\//, topics: ['finance', 'elder'] },
  { pattern: /^\/api\/care\//, topics: ['care', 'elder'] },
  { pattern: /^\/api\/health\//, topics: ['health', 'elder'] },
  { pattern: /^\/api\/dining\//, topics: ['dining', 'elder'] },
  { pattern: /^\/api\/marketing\//, topics: ['marketing', 'elder'] },
  { pattern: /^\/api\/oa\//, topics: ['oa'] }
]

function isBrowser() {
  return typeof window !== 'undefined'
}

function normalizeUrl(url: string) {
  if (!url) return ''
  const clean = url.replace(/^https?:\/\/[^/]+/i, '')
  return clean.split('?')[0] || clean
}

export function inferLiveSyncTopics(url: string): LiveSyncTopic[] {
  const cleanUrl = normalizeUrl(url)
  const topics = new Set<LiveSyncTopic>()
  topicRules.forEach((rule) => {
    if (rule.pattern.test(cleanUrl)) {
      rule.topics.forEach((topic) => topics.add(topic))
    }
  })
  if (!topics.size) topics.add('system')
  return Array.from(topics)
}

export function emitLiveSync(payload: LiveSyncPayload) {
  if (!isBrowser()) return
  window.dispatchEvent(new CustomEvent<LiveSyncPayload>(LIVE_SYNC_EVENT, { detail: payload }))
  if (typeof BroadcastChannel !== 'undefined') {
    const channel = new BroadcastChannel(CHANNEL_NAME)
    channel.postMessage(payload)
    channel.close()
  }
}

export function subscribeLiveSync(listener: (payload: LiveSyncPayload) => void) {
  if (!isBrowser()) return () => {}

  const onWindowEvent = (event: Event) => {
    const detail = (event as CustomEvent<LiveSyncPayload>).detail
    if (detail) listener(detail)
  }
  window.addEventListener(LIVE_SYNC_EVENT, onWindowEvent)

  let channel: BroadcastChannel | null = null
  if (typeof BroadcastChannel !== 'undefined') {
    channel = new BroadcastChannel(CHANNEL_NAME)
    channel.onmessage = (event) => {
      const payload = event.data as LiveSyncPayload
      if (payload) listener(payload)
    }
  }

  return () => {
    window.removeEventListener(LIVE_SYNC_EVENT, onWindowEvent)
    if (channel) channel.close()
  }
}

