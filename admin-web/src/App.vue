<template>
  <a-config-provider :locale="zhCN" :theme="theme">
    <router-view />
  </a-config-provider>
</template>

<script setup lang="ts">
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import { theme as antdTheme } from 'ant-design-vue'

const theme = {
  algorithm: antdTheme.defaultAlgorithm,
  token: {
    colorPrimary: '#1f8fbe',
    colorSuccess: '#3bb273',
    colorWarning: '#f0a54a',
    colorError: '#de5b63',
    colorInfo: '#1f8fbe',
    colorText: '#173854',
    colorTextSecondary: '#6d8aa3',
    colorBorder: '#d7e7f2',
    colorSplit: '#e6f0f7',
    colorBgBase: '#f4f8fb',
    colorBgContainer: '#ffffff',
    colorFillSecondary: '#eff6fb',
    colorFillTertiary: '#f7fbfe',
    borderRadius: 14,
    borderRadiusLG: 18,
    boxShadow: '0 14px 32px rgba(73, 130, 178, 0.12)'
  },
  components: {
    Layout: {
      headerBg: 'rgba(255,255,255,0.92)',
      siderBg: '#f7fbfe',
      triggerBg: '#f7fbfe',
      triggerColor: '#173854'
    },
    Menu: {
      itemBg: 'transparent',
      itemColor: '#4f6f89',
      itemHoverColor: '#173854',
      itemHoverBg: '#ebf6fb',
      itemSelectedColor: '#173854',
      itemSelectedBg: '#dff3fa',
      subMenuItemBg: 'transparent'
    },
    Table: {
      headerBg: '#f2f8fc',
      headerColor: '#173854',
      rowHoverBg: '#f7fbfe',
      borderColor: '#e3eef6'
    },
    Button: {
      borderRadius: 12,
      fontWeight: 600
    },
    Input: {
      borderRadius: 12
    },
    Select: {
      borderRadius: 12
    },
    Card: {
      borderRadiusLG: 20
    }
  }
}
</script>
