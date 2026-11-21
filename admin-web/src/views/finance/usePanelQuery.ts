import { computed } from 'vue'
import { useRouter } from 'vue-router'

/**
 * 合并后的旧页面（收款流水、日结交班、账户流水、预存充值）以 ?panel= 在主页面的抽屉里打开。
 * 读写用真实路由（含工作区的 view 参数），嵌入页拿到的 useRoute() 不含 view，直接 replace 会切走标签。
 */
export function usePanelQuery() {
  const router = useRouter()
  const panel = computed(() => {
    const value = router.currentRoute.value.query.panel
    return typeof value === 'string' ? value : ''
  })
  function close(...extraKeys: string[]) {
    const query = { ...router.currentRoute.value.query }
    for (const key of ['panel', ...extraKeys]) delete query[key]
    router.replace({ query })
  }
  return { panel, close }
}
