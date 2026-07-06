import { h } from 'vue'
import type { ItemType } from 'ant-design-vue'
import {
  AppstoreOutlined,
  ApartmentOutlined,
  AccountBookOutlined,
  AlertOutlined,
  BarChartOutlined,
  DatabaseOutlined,
  FireOutlined,
  HomeOutlined,
  MedicineBoxOutlined,
  SafetyCertificateOutlined,
  SettingOutlined,
  TeamOutlined,
  ToolOutlined
} from '@ant-design/icons-vue'
import type { MenuItem } from './menu'

export type NavSectionKey = 'entry' | 'care' | 'operations' | 'compliance' | 'support' | 'system'

export const NAV_SECTION_LABELS: Record<NavSectionKey, string> = {
  entry: '工作入口',
  care: '长者服务',
  operations: '经营运营',
  compliance: '安全与监管',
  support: '行政后勤',
  system: '系统配置'
}

type NavMeta = {
  section: NavSectionKey
  order: number
  pinned?: boolean
}

export const routeNavMeta: Record<string, NavMeta> = {
  '/portal': { section: 'entry', order: 10, pinned: true },
  '/function-map': { section: 'entry', order: 15, pinned: true },
  '/workbench': { section: 'entry', order: 20, pinned: true },
  '/elder': { section: 'care', order: 30, pinned: true },
  '/medical-care': { section: 'care', order: 40, pinned: true },
  '/marketing': { section: 'operations', order: 50, pinned: true },
  '/finance': { section: 'operations', order: 60, pinned: true },
  '/stats': { section: 'operations', order: 70, pinned: true },
  '/fire': { section: 'compliance', order: 80, pinned: true },
  '/ltci': { section: 'compliance', order: 85, pinned: true },
  '/logistics': { section: 'support', order: 90, pinned: true },
  '/oa': { section: 'support', order: 100, pinned: true },
  '/hr': { section: 'support', order: 110, pinned: true },
  '/base-config': { section: 'system', order: 120, pinned: true },
  '/system': { section: 'system', order: 130, pinned: true }
}

const iconRegistry = {
  HomeOutlined,
  AppstoreOutlined,
  TeamOutlined,
  MedicineBoxOutlined,
  AccountBookOutlined,
  BarChartOutlined,
  ApartmentOutlined,
  ToolOutlined,
  DatabaseOutlined,
  SettingOutlined,
  SafetyCertificateOutlined,
  SafetyOutlined: FireOutlined,
  FundProjectionScreenOutlined: AlertOutlined
} as const

function normalizeMenuPath(item: MenuItem) {
  if (item.path) return item.path
  if (item.children?.length) {
    return normalizeMenuPath(item.children[0])
  }
  return item.key
}

function resolveIconNode(iconName?: string) {
  if (!iconName) return undefined
  const IconComponent = iconRegistry[iconName as keyof typeof iconRegistry]
  if (!IconComponent) return undefined
  return h(IconComponent)
}

function mapMenuNode(item: MenuItem): ItemType {
  return {
    key: item.path || item.key,
    icon: resolveIconNode(item.icon),
    label: item.label,
    title: item.desc ? `${item.label} — ${item.desc}` : item.label,
    children: item.children?.map(mapMenuNode)
  }
}

export function buildGroupedMenuItems(items: MenuItem[]): ItemType[] {
  const grouped = new Map<NavSectionKey, MenuItem[]>()
  ;(Object.keys(NAV_SECTION_LABELS) as NavSectionKey[]).forEach((key) => grouped.set(key, []))

  const sortedItems = [...items].sort((left, right) => {
    const leftMeta = routeNavMeta[normalizeMenuPath(left)] || { section: 'support' as NavSectionKey, order: 999 }
    const rightMeta = routeNavMeta[normalizeMenuPath(right)] || { section: 'support' as NavSectionKey, order: 999 }
    return leftMeta.order - rightMeta.order
  })

  sortedItems.forEach((item) => {
    const meta = routeNavMeta[normalizeMenuPath(item)] || { section: 'support' as NavSectionKey, order: 999 }
    grouped.get(meta.section)?.push(item)
  })

  return (Object.keys(NAV_SECTION_LABELS) as NavSectionKey[])
    .map((section) => {
      const sectionItems = grouped.get(section) || []
      if (!sectionItems.length) return null
      return {
        type: 'group',
        key: `group-${section}`,
        label: NAV_SECTION_LABELS[section],
        children: sectionItems.map(mapMenuNode)
      } satisfies ItemType
    })
    .filter(Boolean) as ItemType[]
}

