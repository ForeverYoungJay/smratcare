<template>
  <div class="enterprise-home">
    <header class="topbar">
      <div class="container topbar-inner">
        <div class="brand">
          <div class="brand-logo">{{ profile.shortName.slice(0, 1) }}</div>
          <div class="brand-text">
            <div class="name">{{ profile.name }}</div>
            <div class="desc">{{ profile.slogan }}</div>
          </div>
        </div>
        <div class="top-actions">
          <a-button type="primary" @click="goAdmin">进入管理后台</a-button>
          <a-button @click="goLogin">员工登录</a-button>
        </div>
      </div>
    </header>

    <section class="hero">
      <div class="hero-mask" />
      <div class="container hero-content">
        <div class="hero-badge">企业首页</div>
        <h1>{{ profile.heroTitle }}</h1>
        <p>{{ profile.heroDesc }}</p>
        <div class="hero-actions">
          <a-button type="primary" size="large" @click="goAdmin">进入管理后台</a-button>
          <a-button size="large" @click="scrollTo('services')">服务项目</a-button>
          <a-button size="large" @click="scrollTo('admission')">入住流程</a-button>
          <a-button size="large" @click="scrollTo('contact')">联系我们</a-button>
        </div>
      </div>
    </section>

    <section class="section stats">
      <div class="container">
        <a-row :gutter="[16, 16]">
          <a-col v-for="item in profile.quickStats" :key="item.label" :xs="12" :md="6">
            <a-card :bordered="false" class="stat-card">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-label">{{ item.label }}</div>
              <div v-if="item.tip" class="stat-tip">{{ item.tip }}</div>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </section>

    <section class="section">
      <div class="container">
        <div class="section-title">
          <h2>机构介绍</h2>
          <p>{{ profile.subtitle }}</p>
        </div>
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :md="12">
            <a-card :bordered="false" class="intro-card">
              <h3>我们的使命</h3>
              <p>{{ profile.mission }}</p>
            </a-card>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-card :bordered="false" class="intro-card">
              <h3>我们的愿景</h3>
              <p>{{ profile.vision }}</p>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </section>

    <section id="services" class="section section-alt">
      <div class="container">
        <div class="section-title">
          <h2>核心服务</h2>
          <p>围绕长者全周期照护需求，提供专业化、个性化、可持续服务。</p>
        </div>
        <a-row :gutter="[16, 16]">
          <a-col v-for="item in profile.serviceGroups" :key="item.title" :xs="24" :md="12" :lg="8">
            <a-card :bordered="false" class="card">
              <h3>{{ item.title }}</h3>
              <p>{{ item.desc }}</p>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </section>

    <section class="section">
      <div class="container">
        <div class="section-title">
          <h2>服务优势</h2>
          <p>让机构管理更稳，让照护服务更细，让家属沟通更顺畅。</p>
        </div>
        <a-row :gutter="[16, 16]">
          <a-col v-for="item in profile.strengths" :key="item.title" :xs="24" :md="12">
            <a-card :bordered="false" class="card">
              <h3>{{ item.title }}</h3>
              <p>{{ item.desc }}</p>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </section>

    <section class="section section-alt">
      <div class="container">
        <div class="section-title">
          <h2>照护理念</h2>
          <p>坚持“以长者为中心”，实现安全、尊严与品质并重。</p>
        </div>
        <a-row :gutter="[16, 16]">
          <a-col v-for="item in profile.carePrinciples" :key="item.title" :xs="24" :md="12" :lg="6">
            <a-card :bordered="false" class="card compact">
              <h3>{{ item.title }}</h3>
              <p>{{ item.desc }}</p>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </section>

    <section class="section">
      <div class="container">
        <div class="section-title">
          <h2>院内环境</h2>
          <p>营造安全、舒适、温馨的适老化生活空间。</p>
        </div>
        <a-row :gutter="[16, 16]">
          <a-col v-for="item in profile.environment" :key="item.title" :xs="24" :md="8">
            <a-card :bordered="false" class="card">
              <h3>{{ item.title }}</h3>
              <p>{{ item.desc }}</p>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </section>

    <section id="admission" class="section section-alt">
      <div class="container">
        <div class="section-title">
          <h2>入住流程</h2>
          <p>流程透明、评估先行、方案清晰。</p>
        </div>
        <div class="flow-list">
          <div v-for="(item, index) in profile.admissionFlow" :key="item" class="flow-item">
            <div class="flow-index">{{ index + 1 }}</div>
            <div class="flow-text">{{ item }}</div>
          </div>
        </div>
      </div>
    </section>

    <section id="contact" class="section">
      <div class="container">
        <div class="contact-card">
          <h2>联系我们</h2>
          <p>欢迎预约到院参观，获取个性化养老照护方案建议。</p>
          <ul>
            <li><span>咨询电话：</span>{{ profile.contact.phone }} {{ profile.contact.hotlineTip }}</li>
            <li><span>联系邮箱：</span>{{ profile.contact.email }}</li>
            <li><span>机构地址：</span>{{ profile.contact.address }}</li>
            <li><span>参观时段：</span>{{ profile.contact.visitingTime }}</li>
          </ul>
          <div class="contact-actions">
            <a-button type="primary" @click="goAdmin">进入管理后台</a-button>
            <a-button @click="goLogin">后台登录</a-button>
          </div>
        </div>
      </div>
    </section>

    <footer class="footer">
      <div class="container footer-inner">
        <span>© {{ currentYear }} {{ profile.name }}</span>
        <span>{{ profile.slogan }}</span>
        <span v-if="profile.legal.icp">{{ profile.legal.icp }}</span>
        <span v-if="profile.legal.publicSecurityRecord">{{ profile.legal.publicSecurityRecord }}</span>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { enterpriseProfile as profile } from '../constants/enterpriseProfile'
import { getToken } from '../utils/auth'

const router = useRouter()
const hasToken = computed(() => Boolean(getToken()))
const currentYear = new Date().getFullYear()

onMounted(() => {
  document.title = `${profile.name} - 企业首页`
})

function goLogin() {
  router.push('/login')
}

function goAdmin() {
  router.push(hasToken.value ? '/portal' : '/login')
}

function scrollTo(id: string) {
  const target = document.getElementById(id)
  if (!target) return
  target.scrollIntoView({ behavior: 'smooth', block: 'start' })
}
</script>

<style scoped>
.enterprise-home {
  min-height: 100vh;
  color: #1f2d3d;
  background: #f5f8ff;
}

.container {
  width: min(1180px, calc(100vw - 40px));
  margin: 0 auto;
}

.topbar {
  position: fixed;
  inset-inline: 0;
  top: 0;
  z-index: 20;
  backdrop-filter: blur(8px);
  background: rgba(13, 44, 109, 0.58);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.topbar-inner {
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-logo {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  color: #0d3a91;
  font-weight: 700;
  background: linear-gradient(135deg, #ffffff, #deebff);
}

.brand-text .name {
  font-size: 18px;
  line-height: 1.2;
  font-weight: 700;
  color: #ffffff;
}

.brand-text .desc {
  margin-top: 2px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.82);
}

.top-actions {
  display: flex;
  gap: 10px;
}

.hero {
  position: relative;
  min-height: 620px;
  padding: 140px 0 90px;
  background: linear-gradient(120deg, #0b2f79 0%, #1c58bb 55%, #3b84f3 100%);
}

.hero-mask {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 15% 18%, rgba(255, 255, 255, 0.2), rgba(255, 255, 255, 0) 40%),
    radial-gradient(circle at 90% 12%, rgba(255, 255, 255, 0.14), rgba(255, 255, 255, 0) 30%);
}

.hero-content {
  position: relative;
  color: #ffffff;
}

.hero-badge {
  display: inline-block;
  margin-bottom: 16px;
  font-size: 13px;
  letter-spacing: 1px;
  border-radius: 999px;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.16);
}

.hero h1 {
  margin: 0;
  color: #ffffff;
  font-size: clamp(34px, 6vw, 54px);
  line-height: 1.2;
  white-space: pre-line;
}

.hero p {
  margin: 20px 0 0;
  max-width: 860px;
  font-size: 17px;
  line-height: 1.9;
  color: rgba(255, 255, 255, 0.92);
}

.hero-actions {
  margin-top: 28px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.section {
  padding: 62px 0;
}

.section-alt {
  background: #edf3ff;
}

.section-title {
  margin-bottom: 20px;
}

.section-title h2 {
  margin: 0;
  font-size: 30px;
  color: #1d2f4f;
}

.section-title p {
  margin: 8px 0 0;
  color: #5d6f8f;
}

.stats {
  margin-top: -72px;
  position: relative;
  z-index: 2;
}

.stat-card {
  border-radius: 15px;
  box-shadow: 0 10px 28px rgba(20, 55, 117, 0.15);
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a55b6;
}

.stat-label {
  margin-top: 6px;
  color: #4f627f;
  font-weight: 600;
}

.stat-tip {
  margin-top: 4px;
  color: #8392aa;
  font-size: 12px;
}

.intro-card,
.card {
  height: 100%;
  border-radius: 14px;
}

.card.compact p {
  min-height: 84px;
}

.intro-card h3,
.card h3 {
  margin: 0;
  color: #24395e;
}

.intro-card p,
.card p {
  margin: 10px 0 0;
  color: #667890;
  line-height: 1.85;
}

.flow-list {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.flow-item {
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 8px 20px rgba(35, 73, 139, 0.1);
  padding: 16px 14px;
}

.flow-index {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-weight: 700;
  color: #ffffff;
  background: #2f74df;
}

.flow-text {
  margin-top: 10px;
  color: #385173;
  font-weight: 600;
}

.contact-card {
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 10px 25px rgba(26, 66, 139, 0.11);
  padding: 30px;
}

.contact-card h2 {
  margin: 0;
  color: #1f3459;
}

.contact-card p {
  margin: 10px 0 14px;
  color: #6a7c97;
}

.contact-card ul {
  list-style: none;
  margin: 0;
  padding: 0;
}

.contact-card li {
  margin: 8px 0;
  color: #3d4f67;
}

.contact-card li span {
  font-weight: 700;
  color: #1d355f;
}

.contact-actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.footer {
  margin-top: 36px;
  padding: 20px 0;
  background: #0d2f6e;
  color: rgba(255, 255, 255, 0.9);
}

.footer-inner {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  flex-wrap: wrap;
}

@media (max-width: 900px) {
  .topbar-inner {
    height: 64px;
  }

  .brand-text .name {
    font-size: 16px;
  }

  .brand-text .desc {
    display: none;
  }

  .hero {
    min-height: 560px;
    padding-top: 112px;
  }

  .stats {
    margin-top: -52px;
  }

  .flow-list {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 680px) {
  .container {
    width: calc(100vw - 24px);
  }

  .top-actions :deep(.ant-btn) {
    padding-inline: 11px;
  }

  .section {
    padding: 50px 0;
  }

  .contact-card {
    padding: 20px;
  }

  .flow-list {
    grid-template-columns: 1fr;
  }
}
</style>
