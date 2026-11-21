import { message } from 'ant-design-vue'

/**
 * 同一次请求失败常常被弹两次：
 * request.ts 的响应拦截器弹一次，页面自己的 catch 再 message.error 一次，
 * 两条文案一模一样，叠在一起像是出了两个错。
 *
 * 全站有几百处 catch，逐个改不现实，在出口处对短时间内的重复文案去重。
 */
const DEDUPE_WINDOW_MS = 1200

function textOf(content: unknown): string {
  if (typeof content === 'string') return content
  if (content && typeof content === 'object' && 'content' in content) {
    return String((content as { content?: unknown }).content ?? '')
  }
  return ''
}

export function setupMessageDedupe() {
  const levels = ['error', 'warning'] as const
  levels.forEach((level) => {
    const original = message[level].bind(message)
    let lastText = ''
    let lastAt = 0
    // @ts-expect-error 运行时替换 antd 的全局提示方法
    message[level] = (content: unknown, ...rest: unknown[]) => {
      const text = textOf(content)
      const now = Date.now()
      if (text && text === lastText && now - lastAt < DEDUPE_WINDOW_MS) {
        return () => {}
      }
      lastText = text
      lastAt = now
      // @ts-expect-error 透传原始参数
      return original(content, ...rest)
    }
  })
}
