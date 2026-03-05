<template>
  <div class="enterprise-home">
    <header class="topbar">
      <div class="container topbar-inner">
        <div class="brand" @click="scrollTo('top')">
          <div class="brand-logo">{{ profile.shortName.slice(0, 1) }}</div>
          <div class="brand-text">
            <div class="name">{{ profile.name }}</div>
            <div class="desc">{{ profile.slogan }}</div>
          </div>
        </div>
        <nav class="nav-list">
          <a @click="scrollTo('service-system')">服务体系</a>
          <a @click="scrollTo('resident-system')">龟峰居民</a>
          <a @click="scrollTo('community')">社区动态</a>
          <a @click="scrollTo('news')">新闻资讯</a>
          <a @click="scrollTo('activities')">居民活动</a>
          <a @click="scrollTo('about-us')">关于我们</a>
          <a @click="scrollTo('join-us')">加入我们</a>
          <a @click="scrollTo('cooperation')">业务合作</a>
          <a @click="scrollTo('city-tier')">城市分档</a>
          <a @click="scrollTo('contact')">联系我们</a>
        </nav>
        <div class="top-actions">
          <a-button type="primary" @click="goAdmin">进入管理后台</a-button>
          <a-button @click="goLogin">员工登录</a-button>
        </div>
      </div>
    </header>

    <section id="top" class="hero">
      <div class="hero-mask" />
      <div class="container hero-content">
        <div class="hero-badge">企业首页</div>
        <h1>{{ profile.heroTitle }}</h1>
        <p>{{ profile.heroDesc }}</p>
        <div class="hero-actions">
          <a-button type="primary" size="large" @click="goAdmin">进入管理后台</a-button>
          <a-button size="large" @click="scrollTo('service-system')">查看服务体系</a-button>
          <a-button size="large" @click="openVrCommunity">VR看社区</a-button>
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
          <h2>企业介绍</h2>
          <p>{{ profile.subtitle }}</p>
        </div>
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :md="12">
            <a-card :bordered="false" class="card intro">
              <h3>使命</h3>
              <p>{{ profile.mission }}</p>
            </a-card>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-card :bordered="false" class="card intro">
              <h3>愿景</h3>
              <p>{{ profile.vision }}</p>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </section>

    <section id="service-system" class="section section-alt">
      <div class="container">
        <div class="section-title">
          <h2>服务体系</h2>
          <p>活力生活、专业护理、记忆照护、康复医院、生命关怀五大模块协同。</p>
        </div>
        <a-row :gutter="[16, 16]">
          <a-col v-for="item in profile.serviceSystem" :key="item.title" :xs="24" :md="12" :lg="8">
            <a-card :bordered="false" class="card">
              <h3>{{ item.title }}</h3>
              <p>{{ item.desc }}</p>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </section>

    <section id="resident-system" class="section">
      <div class="container">
        <div class="section-title">
          <h2>龟峰居民</h2>
          <p>居民体系、价值再创造、风采与故事，形成“有归属感”的社区生活。</p>
        </div>
        <a-row :gutter="[16, 16]">
          <a-col v-for="item in profile.residentSystem" :key="item.title" :xs="24" :md="12">
            <a-card :bordered="false" class="card">
              <h3>{{ item.title }}</h3>
              <p>{{ item.desc }}</p>
            </a-card>
          </a-col>
        </a-row>

        <div class="sub-title">居民专题</div>
        <a-row :gutter="[16, 16]">
          <a-col v-for="item in profile.residentHighlights" :key="item.title" :xs="24" :md="12" :lg="6">
            <a-card :bordered="false" class="card compact">
              <h3>{{ item.title }}</h3>
              <p>{{ item.desc }}</p>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </section>

    <section id="community" class="section section-alt">
      <div class="container">
        <div class="section-title">
          <h2>社区动态</h2>
          <p>新闻资讯、居民活动、服务更新。</p>
        </div>
        <div class="news-list">
          <a-card v-for="item in profile.communityUpdates" :key="item.title" :bordered="false" class="news-card">
            <h3>{{ item.title }}</h3>
            <p>{{ item.desc }}</p>
          </a-card>
        </div>
      </div>
    </section>

    <section id="news" class="section">
      <div class="container">
        <div class="section-title">
          <h2>新闻资讯</h2>
          <p>关注机构公告、健康科普和养老服务最新动态。</p>
        </div>
        <div class="news-list">
          <a-card v-for="item in profile.newsList" :key="item.title" :bordered="false" class="news-card">
            <div class="news-meta">
              <span class="news-tag">{{ item.tag }}</span>
              <span class="news-date">{{ item.date }}</span>
            </div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.desc }}</p>
          </a-card>
        </div>
      </div>
    </section>

    <section id="activities" class="section section-alt">
      <div class="container">
        <div class="section-title">
          <h2>居民活动</h2>
          <p>以活动促交流，以参与促健康，打造有温度的院内生活。</p>
        </div>
        <a-row :gutter="[16, 16]">
          <a-col v-for="item in profile.residentActivities" :key="item.title" :xs="24" :md="8">
            <a-card :bordered="false" class="card">
              <h3>{{ item.title }}</h3>
              <p>{{ item.desc }}</p>
              <div class="activity-meta">
                <span>{{ item.schedule }}</span>
                <span>{{ item.location }}</span>
              </div>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </section>

    <section class="section">
      <div class="container">
        <div class="section-title">
          <h2>入住流程</h2>
          <p>从预约咨询到周期复评，流程透明，方案清晰。</p>
        </div>
        <div class="flow-list">
          <div v-for="(item, index) in profile.admissionFlow" :key="item" class="flow-item">
            <div class="flow-index">{{ index + 1 }}</div>
            <div class="flow-text">{{ item }}</div>
          </div>
        </div>
      </div>
    </section>

    <section id="about-us" class="section section-alt">
      <div class="container">
        <div class="section-title">
          <h2>关于我们</h2>
          <p>企业介绍、联系我们、加入我们。</p>
        </div>
        <a-row :gutter="[16, 16]">
          <a-col v-for="item in profile.aboutUs" :key="item.title" :xs="24" :md="8">
            <a-card :bordered="false" class="card">
              <h3>{{ item.title }}</h3>
              <p>{{ item.desc }}</p>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </section>

    <section id="join-us" class="section">
      <div class="container">
        <div class="section-title">
          <h2>加入我们</h2>
          <p>欢迎认同“专业、耐心、尊重”的你，加入龟峰颐养团队。</p>
        </div>
        <a-row :gutter="[16, 16]">
          <a-col v-for="item in profile.jobs" :key="item.title" :xs="24" :md="8">
            <a-card :bordered="false" class="card">
              <h3>{{ item.title }}</h3>
              <p>{{ item.requirement }}</p>
              <div class="job-dept">{{ item.department }}</div>
            </a-card>
          </a-col>
        </a-row>
        <div class="section-actions">
          <a-button type="primary" @click="openJoinUs">投递简历</a-button>
        </div>
      </div>
    </section>

    <section id="cooperation" class="section">
      <div class="container">
        <div class="section-title">
          <h2>业务合作</h2>
          <p>面向医疗、康复、供应链、保险、公益组织开放生态合作。</p>
        </div>
        <a-row :gutter="[16, 16]">
          <a-col v-for="item in profile.cooperation" :key="item.title" :xs="24" :md="12">
            <a-card :bordered="false" class="card">
              <h3>{{ item.title }}</h3>
              <p>{{ item.desc }}</p>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </section>

    <section id="city-tier" class="section section-alt">
      <div class="container">
        <div class="section-title">
          <h2>城市分档</h2>
          <p>按城市与区域特征，配置差异化养老服务供给。</p>
        </div>
        <a-table
          :dataSource="profile.cityTiers"
          :pagination="false"
          rowKey="tier"
          size="middle"
          class="tier-table"
        >
          <a-table-column title="分档" data-index="tier" key="tier" />
          <a-table-column title="城市/区域" data-index="cityGroup" key="cityGroup" />
          <a-table-column title="服务模型" data-index="model" key="model" />
        </a-table>
      </div>
    </section>

    <section id="contact" class="section">
      <div class="container">
        <div class="contact-card">
          <h2>联系我们</h2>
          <p>欢迎预约到院参观，获取个性化养老照护方案。</p>
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

function openJoinUs() {
  if (profile.joinUsUrl === '#') {
    scrollTo('contact')
    return
  }
  window.open(profile.joinUsUrl, '_blank', 'noopener,noreferrer')
}

function openVrCommunity() {
  if (!profile.vrCommunityUrl || profile.vrCommunityUrl === '#') {
    scrollTo('community')
    return
  }
  window.open(profile.vrCommunityUrl, '_blank', 'noopener,noreferrer')
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
  width: min(1200px, calc(100vw - 36px));
  margin: 0 auto;
}

.topbar {
  position: fixed;
  inset-inline: 0;
  top: 0;
  z-index: 30;
  backdrop-filter: blur(8px);
  background: rgba(13, 44, 109, 0.58);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.topbar-inner {
  height: 72px;
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 18px;
  align-items: center;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
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
  font-size: 17px;
  line-height: 1.2;
  font-weight: 700;
  color: #ffffff;
}

.brand-text .desc {
  margin-top: 2px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.82);
}

.nav-list {
  display: flex;
  justify-content: center;
  gap: 14px;
  font-size: 14px;
}

.nav-list a {
  color: rgba(255, 255, 255, 0.94);
  text-decoration: none;
  cursor: pointer;
}

.top-actions {
  display: flex;
  gap: 10px;
}

.hero {
  position: relative;
  min-height: 630px;
  padding: 140px 0 95px;
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
  margin: 18px 0 0;
  max-width: 900px;
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

.sub-title {
  margin: 24px 0 12px;
  color: #314e7c;
  font-weight: 700;
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

.card {
  height: 100%;
  border-radius: 14px;
}

.card.compact p {
  min-height: 88px;
}

.card h3 {
  margin: 0;
  color: #24395e;
}

.card p {
  margin: 10px 0 0;
  color: #667890;
  line-height: 1.8;
}

.news-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.news-card {
  border-radius: 14px;
}

.news-card h3 {
  margin: 0;
  color: #23447c;
}

.news-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.news-tag {
  background: #e8f1ff;
  color: #2b63bf;
  border-radius: 999px;
  padding: 2px 10px;
  font-size: 12px;
}

.news-date {
  color: #8496b4;
  font-size: 12px;
}

.news-card p {
  margin: 10px 0 0;
  color: #6a7f9e;
  line-height: 1.8;
}

.flow-list {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
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

.tier-table :deep(.ant-table) {
  border-radius: 12px;
  overflow: hidden;
}

.section-actions {
  margin-top: 14px;
}

.activity-meta {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  gap: 8px;
  color: #6b7f9f;
  font-size: 12px;
}

.job-dept {
  margin-top: 12px;
  display: inline-block;
  color: #2d63bb;
  background: #e7f0ff;
  border-radius: 999px;
  padding: 2px 10px;
  font-size: 12px;
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

@media (max-width: 1080px) {
  .topbar-inner {
    grid-template-columns: 1fr auto;
    grid-template-areas:
      'brand actions'
      'nav nav';
    height: auto;
    padding: 10px 0;
  }

  .brand {
    grid-area: brand;
  }

  .top-actions {
    grid-area: actions;
  }

  .nav-list {
    grid-area: nav;
    justify-content: flex-start;
    flex-wrap: wrap;
    gap: 10px 16px;
    margin-top: 8px;
  }

  .hero {
    padding-top: 170px;
  }

  .news-list {
    grid-template-columns: 1fr;
  }

  .flow-list {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .container {
    width: calc(100vw - 24px);
  }

  .brand-text .desc {
    display: none;
  }

  .top-actions :deep(.ant-btn) {
    padding-inline: 10px;
  }

  .hero {
    min-height: 560px;
    padding-top: 175px;
  }

  .stats {
    margin-top: -52px;
  }

  .section {
    padding: 50px 0;
  }

  .flow-list {
    grid-template-columns: 1fr 1fr;
  }

  .contact-card {
    padding: 20px;
  }
}

@media (max-width: 520px) {
  .flow-list {
    grid-template-columns: 1fr;
  }
}
</style>
