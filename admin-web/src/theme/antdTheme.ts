import { theme as antdTheme } from 'ant-design-vue'

export const antTheme = {
  algorithm: antdTheme.defaultAlgorithm,
  token: {
    colorPrimary: '#21705F',
    colorSuccess: '#3B9A68',
    colorWarning: '#DE9B3D',
    colorError: '#C9504B',
    colorInfo: '#3D7FA6',
    colorLink: '#21705F',
    colorText: '#22332E',
    colorTextSecondary: '#5C6F69',
    colorTextTertiary: '#8A9A94',
    colorBorder: '#E4E7E0',
    colorBorderSecondary: '#EDEFE9',
    colorSplit: '#EDEFE9',
    colorBgBase: '#F7F6F2',
    colorBgLayout: '#F7F6F2',
    colorBgContainer: '#ffffff',
    colorBgElevated: '#ffffff',
    colorFillSecondary: '#F1F3EE',
    colorFillTertiary: '#F5F6F2',
    colorFillQuaternary: '#FAFAF7',
    colorPrimaryBg: '#E8F3EF',
    colorPrimaryBgHover: '#DCEDE6',
    colorPrimaryBorder: '#BBD9CF',
    colorPrimaryHover: '#2E8A72',
    colorPrimaryActive: '#185C4E',
    colorSuccessBg: '#EBF6F0',
    colorWarningBg: '#FBF3E4',
    colorErrorBg: '#FAECEB',
    colorInfoBg: '#EAF2F7',
    fontFamily: "'HarmonyOS Sans SC', 'PingFang SC', 'Microsoft YaHei', 'Noto Sans SC', system-ui, -apple-system, sans-serif",
    fontSize: 14,
    fontSizeSM: 13,
    fontSizeLG: 15,
    lineHeight: 1.5,
    lineWidth: 1,
    controlHeight: 37,
    controlHeightLG: 42,
    borderRadius: 10,
    borderRadiusLG: 14,
    borderRadiusSM: 8,
    boxShadow: '0 6px 16px rgba(34, 51, 46, 0.07)',
    boxShadowSecondary: '0 1px 2px rgba(34, 51, 46, 0.05)'
  },
  components: {
    Layout: {
      headerBg: 'rgba(255,255,255,0.95)',
      siderBg: '#FCFCFA',
      bodyBg: '#F7F6F2',
      triggerBg: '#FCFCFA',
      triggerColor: '#22332E'
    },
    Menu: {
      itemBg: 'transparent',
      itemColor: '#5C6F69',
      itemHoverColor: '#22332E',
      itemHoverBg: '#F1F3EE',
      itemSelectedColor: '#185C4E',
      itemSelectedBg: '#E8F3EF',
      subMenuItemBg: 'transparent',
      activeBarBorderWidth: 0
    },
    Table: {
      headerBg: '#F3F5F0',
      headerColor: '#22332E',
      rowHoverBg: '#F7F9F5',
      borderColor: '#EDEFE9',
      headerBorderRadius: 10
    },
    Card: {
      borderRadiusLG: 14,
      headerBg: 'transparent'
    },
    Button: {
      borderRadius: 10,
      fontWeight: 600,
      controlHeight: 37
    },
    Input: {
      borderRadius: 10,
      colorBgContainer: '#ffffff'
    },
    Select: {
      borderRadius: 10
    },
    DatePicker: {
      borderRadius: 10
    },
    Tag: {
      borderRadiusSM: 999
    },
    Tabs: {
      cardBg: '#F3F5F0',
      itemSelectedColor: '#185C4E',
      itemActiveColor: '#21705F',
      inkBarColor: '#21705F'
    },
    Pagination: {
      itemActiveBg: '#E8F3EF',
      itemSize: 34
    },
    Breadcrumb: {
      lastItemColor: '#42544E',
      itemColor: '#8A9A94',
      separatorColor: '#A9B6B0'
    },
    Modal: {
      borderRadiusLG: 16
    },
    Drawer: {
      colorBgElevated: '#FCFCFA'
    },
    Alert: {
      borderRadiusLG: 12
    },
    Form: {
      labelColor: '#42544E'
    },
    Segmented: {
      trackBg: '#F1F3EE',
      itemSelectedBg: '#ffffff'
    }
  }
}
