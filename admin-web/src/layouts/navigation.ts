import { h } from 'vue'
import type { ItemType } from 'ant-design-vue'
import {
  AppstoreOutlined,
  ApartmentOutlined,
  AccountBookOutlined,
  AlertOutlined,
  AuditOutlined,
  BarChartOutlined,
  CheckSquareOutlined,
  DatabaseOutlined,
  FireOutlined,
  HomeOutlined,
  MedicineBoxOutlined,
  ScheduleOutlined,
  SafetyCertificateOutlined,
  SettingOutlined,
  TeamOutlined,
  ToolOutlined,
  UserOutlined
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

const iconRegistry = {
  HomeOutlined,
  AppstoreOutlined,
  TeamOutlined,
  MedicineBoxOutlined,
  AccountBookOutlined,
  BarChartOutlined,
  CheckSquareOutlined,
  AuditOutlined,
  ScheduleOutlined,
  UserOutlined,
  AlertOutlined,
  ApartmentOutlined,
  ToolOutlined,
  DatabaseOutlined,
  SettingOutlined,
  SafetyCertificateOutlined,
  SafetyOutlined: FireOutlined,
  FundProjectionScreenOutlined: AlertOutlined
} as const

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
    return (left.navOrder ?? 999) - (right.navOrder ?? 999)
  })

  sortedItems.forEach((item) => {
    grouped.get(item.navSection || 'support')?.push(item)
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
