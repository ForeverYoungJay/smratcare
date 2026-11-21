import { ref } from 'vue'
import { getShiftTemplateList } from '../api/nursing'
import type { ShiftTemplateItem } from '../types'

export interface ShiftCodeOption {
  label: string
  value: string
}

/**
 * 班次编码字典。
 *
 * 交接班、排班、班次模板四处原本都是自由文本，各自给不同的 placeholder
 * （「如 MORNING」/ 无提示 /「DAY / NIGHT / A1」），结果同一个班在不同页面
 * 被填成不同编码，之后按班次编码筛选永远查不到。统一从班次模板取值。
 */
const cache = { options: null as ShiftCodeOption[] | null }

function toOption(item: ShiftTemplateItem): ShiftCodeOption {
  const code = String(item.shiftCode || '').trim()
  const time = item.startTime && item.endTime ? ` ${item.startTime}-${item.endTime}` : ''
  const name = String(item.name || '').trim()
  return {
    value: code,
    label: name ? `${name}（${code}${time}）` : `${code}${time}`
  }
}

export function useShiftCodeOptions() {
  const shiftCodeOptions = ref<ShiftCodeOption[]>(cache.options ? [...cache.options] : [])
  const shiftCodeLoading = ref(false)

  async function loadShiftCodes(force = false) {
    if (!force && cache.options) {
      shiftCodeOptions.value = [...cache.options]
      return shiftCodeOptions.value
    }
    shiftCodeLoading.value = true
    try {
      const rows = (await getShiftTemplateList({ enabled: 1 })) || []
      const seen = new Set<string>()
      const options: ShiftCodeOption[] = []
      rows.forEach((item) => {
        const code = String(item.shiftCode || '').trim()
        if (!code || seen.has(code)) return
        seen.add(code)
        options.push(toOption(item))
      })
      cache.options = options
      shiftCodeOptions.value = [...options]
    } catch {
      // 字典拉取失败不阻断页面：已有值仍然显示，用户可以照常查询
    } finally {
      shiftCodeLoading.value = false
    }
    return shiftCodeOptions.value
  }

  /** 历史记录里的编码可能已经不在模板里了，补进选项，避免下拉显示空白 */
  function ensureShiftCode(code?: string | null) {
    const value = String(code || '').trim()
    if (!value) return
    if (shiftCodeOptions.value.some((item) => item.value === value)) return
    shiftCodeOptions.value = [{ value, label: `${value}（历史编码）` }, ...shiftCodeOptions.value]
  }

  return { shiftCodeOptions, shiftCodeLoading, loadShiftCodes, ensureShiftCode }
}
