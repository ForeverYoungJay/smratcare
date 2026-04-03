<template>
  <div class="page-container">
    <a-result status="403" title="403" sub-title="没有权限访问该页面，请联系管理员开通对应功能权限。">
      <template #extra>
        <a-space>
          <a-button @click="goEnterpriseHome">返回企业首页</a-button>
          <a-button type="primary" @click="goPortal">返回工作台</a-button>
          <a-button @click="contactAdmin">联系管理员</a-button>
        </a-space>
      </template>
    </a-result>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { enterpriseProfile } from '../constants/enterpriseProfile'

const router = useRouter()

function goPortal() {
  router.push('/workbench')
}

function goEnterpriseHome() {
  router.push('/home')
}

function contactAdmin() {
  const phone = enterpriseProfile.contact.phone || ''
  const onlyDigits = phone.replace(/[^\d-]/g, '')
  if (onlyDigits) {
    window.location.href = `tel:${onlyDigits}`
    return
  }
  const email = enterpriseProfile.contact.email || ''
  if (email) {
    window.location.href = `mailto:${email}`
  }
}
</script>
