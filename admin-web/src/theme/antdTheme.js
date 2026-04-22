import { theme as antdTheme } from 'ant-design-vue';
export const antTheme = {
    algorithm: antdTheme.defaultAlgorithm,
    token: {
        colorPrimary: '#136cb5',
        colorSuccess: '#2f9b66',
        colorWarning: '#d98922',
        colorError: '#d64c5f',
        colorInfo: '#136cb5',
        colorLink: '#136cb5',
        colorText: '#12314d',
        colorTextSecondary: '#5f7b95',
        colorTextTertiary: '#86a0b7',
        colorBorder: '#d8e5ef',
        colorBorderSecondary: '#e5eef5',
        colorSplit: '#eaf1f6',
        colorBgBase: '#f4f8fb',
        colorBgLayout: '#f4f8fb',
        colorBgContainer: '#ffffff',
        colorBgElevated: '#ffffff',
        colorFillSecondary: '#eff5fa',
        colorFillTertiary: '#f6fafc',
        colorFillQuaternary: '#f9fbfd',
        colorPrimaryBg: '#e7f3ff',
        colorPrimaryBgHover: '#d7ebff',
        colorPrimaryBorder: '#b6d8f5',
        colorSuccessBg: '#edf9f2',
        colorWarningBg: '#fff4e7',
        colorErrorBg: '#fff0f2',
        fontFamily: "'HarmonyOS Sans SC', 'PingFang SC', 'Microsoft YaHei', 'Noto Sans SC', system-ui, -apple-system, sans-serif",
        fontSize: 14,
        fontSizeSM: 13,
        fontSizeLG: 15,
        lineHeight: 1.5,
        lineWidth: 1,
        controlHeight: 38,
        controlHeightLG: 44,
        borderRadius: 14,
        borderRadiusLG: 20,
        borderRadiusSM: 10,
        boxShadow: '0 18px 40px rgba(18, 49, 77, 0.08)',
        boxShadowSecondary: '0 10px 24px rgba(18, 49, 77, 0.06)'
    },
    components: {
        Layout: {
            headerBg: 'rgba(255,255,255,0.92)',
            siderBg: '#f7fafc',
            bodyBg: '#f4f8fb',
            triggerBg: '#f7fafc',
            triggerColor: '#12314d'
        },
        Menu: {
            itemBg: 'transparent',
            itemColor: '#58748d',
            itemHoverColor: '#12314d',
            itemHoverBg: '#edf6fd',
            itemSelectedColor: '#12314d',
            itemSelectedBg: '#e4f2fd',
            subMenuItemBg: 'transparent',
            activeBarBorderWidth: 0
        },
        Table: {
            headerBg: '#f6fafc',
            headerColor: '#12314d',
            rowHoverBg: '#f7fbfe',
            borderColor: '#e3edf4',
            headerBorderRadius: 14
        },
        Card: {
            borderRadiusLG: 22,
            headerBg: 'transparent'
        },
        Button: {
            borderRadius: 12,
            fontWeight: 600,
            controlHeight: 38
        },
        Input: {
            borderRadius: 12,
            colorBgContainer: '#ffffff'
        },
        Select: {
            borderRadius: 12
        },
        DatePicker: {
            borderRadius: 12
        },
        Tag: {
            borderRadiusSM: 999
        },
        Tabs: {
            cardBg: '#f4f8fb',
            itemSelectedColor: '#12314d',
            itemActiveColor: '#136cb5',
            inkBarColor: '#136cb5'
        },
        Pagination: {
            itemActiveBg: '#e7f3ff',
            itemSize: 34
        },
        Breadcrumb: {
            lastItemColor: '#43617c',
            itemColor: '#7890a6',
            separatorColor: '#a0b4c5'
        },
        Modal: {
            borderRadiusLG: 24
        },
        Drawer: {
            colorBgElevated: '#f8fbfd'
        },
        Alert: {
            borderRadiusLG: 18
        },
        Form: {
            labelColor: '#43617c'
        },
        Segmented: {
            trackBg: '#edf4f9',
            itemSelectedBg: '#ffffff'
        }
    }
};
