import { ref } from 'vue'

/**
 * 提交防重锁：把“提交中”状态与重入守卫收进一个组合式。
 *
 * antd 的 confirm-loading 只在重渲染后禁用按钮，渲染前落地的连击仍会重复触发
 * 提交函数（曾在电子病历上实测三连击产生 3 条重复记录）。用法：
 *
 *   const { locked: saving, run: submitLocked } = useSubmitLock()
 *   async function submit() {
 *     await submitLocked(async () => {
 *       await api(...)
 *     })
 *   }
 *   // 模板：<a-modal :confirm-loading="saving" @ok="submit">
 *
 * run() 在锁被占用时直接返回 undefined（静默丢弃重复触发）。
 */
export function useSubmitLock() {
  const locked = ref(false)

  async function run<T>(task: () => Promise<T>): Promise<T | undefined> {
    if (locked.value) return undefined
    locked.value = true
    try {
      return await task()
    } finally {
      locked.value = false
    }
  }

  return { locked, run }
}
