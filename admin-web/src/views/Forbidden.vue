<template>
  <div class="page-container">
    <a-result status="403" title="403" sub-title="没有权限访问该页面，请联系管理员开通对应功能权限。">
      <template #extra>
        <a-space>
          <a-button @click="goEnterpriseHome">返回企业首页</a-button>
          <a-button type="primary" @click="goHome">返回我的首页</a-button>
          <a-button @click="contactAdmin">联系管理员</a-button>
        </a-space>
      </template>
    </a-result>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { enterpriseProfile } from '../constants/enterpriseProfile'
import { resolveDefaultHome } from '../access/policy'
import { getRoles } from '../utils/auth'

const router = useRouter()

// 不能写死 /portal：一线员工无权访问经营总览，点了会再吃一次 403
function goHome() {
  router.push(resolveDefaultHome(getRoles()))
}

function goEnterpriseHome() {
  window.location.assign('/')
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
