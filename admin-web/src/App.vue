<template>
  <a-config-provider :locale="zhCN" :theme="antTheme">
    <router-view />
  </a-config-provider>
</template>

<script setup lang="ts">
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import { antTheme } from './theme/antdTheme'
</script>
