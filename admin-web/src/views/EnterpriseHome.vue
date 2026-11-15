<template>
  <div id="top" class="site" :class="{ 'large-text': largeText }">
    <a class="skip-link" href="#main-content">跳到主要内容</a>
    <header class="site-header">
      <div class="utility-bar">
        <div class="container utility-inner">
          <span>用爱守护长者的幸福晚年｜让每一位长者都拥有尊严、有温度、有品质的生活</span>
          <div><a :href="`tel:${phoneDigits}`">☎ {{ profile.contact.phone }}</a><span>⌖ {{ profile.contact.address }}</span><button type="button" @click="toggleLargeText">{{ largeText ? '标准字号' : '大字模式' }}</button><a class="admin-entry" href="/admin">后台管理 ↗</a></div>
        </div>
      </div>
      <div class="container nav-row">
        <a class="brand" href="#top" aria-label="弋阳龟峰颐养中心首页">
          <img :src="brandLogoUrl" alt="" /><span><strong>弋阳龟峰颐养中心</strong><small>山水相伴 · 颐养有爱</small></span>
        </a>
        <nav class="desktop-nav" aria-label="主导航"><a v-for="item in navItems" :key="item.href" :href="item.href">{{ item.label }}</a></nav>
        <a class="nav-cta" href="/contact.html">预约参观</a>
        <button class="menu-button" type="button" :aria-expanded="menuOpen" aria-controls="mobile-nav" @click="menuOpen = !menuOpen"><span></span><span></span><span></span><b class="sr-only">打开导航</b></button>
      </div>
      <nav v-if="menuOpen" id="mobile-nav" class="mobile-nav" aria-label="移动端导航"><a v-for="item in navItems" :key="item.href" :href="item.href" @click="menuOpen = false">{{ item.label }}</a><a href="/contact.html">预约参观</a><a href="/admin">后台管理</a></nav>
    </header>

    <main id="main-content">
      <section class="hero">
        <img class="hero-landscape" :src="images.guifeng" alt="龟峰山水风光" referrerpolicy="no-referrer" />
        <div class="hero-wash"></div>
        <div class="container hero-content">
          <div class="hero-copy">
            <p class="hero-seal">弋阳 · 龟峰</p>
            <h1>山水有情，颐养有爱</h1>
            <h2>用专业守护健康，用温情陪伴岁月</h2>
            <p>在山水之间，安享幸福晚年</p>
            <div class="hero-actions"><a class="button button-gold" href="/contact.html">▣ 预约参观 <b>→</b></a><a class="button button-line" :href="`tel:${phoneDigits}`">☎ 电话咨询</a></div>
          </div>
          <div class="hero-portrait"><img :src="images.activity" alt="长者参与颐养活动" referrerpolicy="no-referrer" /><span>依龟峰山水<br />享颐养人生</span></div>
        </div>
      </section>

      <section id="about" class="about-section section-pattern">
        <div class="container about-grid">
          <figure class="about-photo"><img :src="images.guifeng" alt="龟峰山水风光" referrerpolicy="no-referrer" /><figcaption>山水福地</figcaption></figure>
          <div class="about-copy"><p class="eyebrow">认识龟峰颐养中心</p><h2>依山傍水，颐养天年</h2><p>弋阳龟峰颐养中心立足本地养老服务需求，以专业照护为基础，以长者尊严、健康与生活感受为中心，营造安全、舒适、温馨的颐养环境。</p><p>生活照护、健康管理、康复支持、营养膳食与文化娱乐协同，让晚年生活既有保障，也有温度。</p><a class="small-button" href="/about.html">了解更多 →</a></div>
          <div class="about-features"><article v-for="item in featureCards" :key="item.title"><span>{{ item.icon }}</span><div><h3>{{ item.title }}</h3><p>{{ item.desc }}</p></div></article></div>
        </div>
      </section>

      <section id="services" class="content-section section-pattern">
        <div class="container">
          <SectionTitle title="我们的养老服务" subtitle="全方位 · 个性化 · 有温度的养老服务" />
          <div class="photo-card-grid service-grid">
            <a v-for="item in services" :key="item.title" class="photo-card service-card" :href="item.href"><div class="card-head"><span>{{ item.icon }}</span><div><h3>{{ item.title }}</h3><p>{{ item.sub }}</p></div></div><img :src="item.image" :alt="item.title" loading="lazy" referrerpolicy="no-referrer" /><div class="card-caption">{{ item.desc }}</div></a>
          </div>
        </div>
      </section>

      <section id="environment" class="content-section environment-section section-pattern">
        <div class="container">
          <div class="section-title-row"><SectionTitle title="环境与房型" subtitle="依山傍水、舒适宜居，打造高品质颐养环境" /><a href="/about.html">查看更多 →</a></div>
          <div class="photo-card-grid environment-grid">
            <article v-for="item in environments" :key="item.title" class="photo-card"><img :src="item.image" :alt="item.title" loading="lazy" referrerpolicy="no-referrer" /><div class="image-label"><h3>{{ item.title }}</h3><p>{{ item.desc }}</p></div></article>
          </div>
        </div>
      </section>

      <section id="life" class="content-section section-pattern">
        <div class="container">
          <div class="section-title-row"><SectionTitle title="颐养生活纪实" subtitle="丰富多彩的文化生活，让每一天都充满阳光与笑容" /><a href="#news">查看更多 →</a></div>
          <div class="photo-card-grid life-grid">
            <article v-for="item in lifeMoments" :key="item.title" class="photo-card"><img :src="item.image" :alt="item.title" loading="lazy" referrerpolicy="no-referrer" /><div class="image-label"><h3>{{ item.title }}</h3><p>{{ item.desc }}</p></div></article>
          </div>
          <p class="temporary-note">当前使用公开网络图片作为版式示意，后续可统一替换为机构授权实拍。</p>
        </div>
      </section>

      <section id="team" class="team-section">
        <div class="container team-grid">
          <div class="team-copy"><h2>专业团队与安全保障</h2><p>用专业和爱心，守护每一位长者的幸福晚年</p><a class="small-button" href="/services.html">了解更多 →</a></div>
          <img :src="images.team" alt="养老照护专业团队" loading="lazy" referrerpolicy="no-referrer" />
          <div class="team-features"><article v-for="item in safeguards" :key="item.title"><span>{{ item.icon }}</span><h3>{{ item.title }}</h3><p>{{ item.desc }}</p></article></div>
        </div>
      </section>

      <section id="news" class="content-section news-section">
        <div class="container">
          <div class="section-title-row compact"><div><h2>新闻动态</h2><p>关注龟峰颐养中心，了解更多养老资讯</p></div><a href="/about.html">查看更多 →</a></div>
          <div class="news-grid"><article v-for="(item, index) in latestNews" :key="item.title"><img :src="newsImages[index]" :alt="item.title" loading="lazy" referrerpolicy="no-referrer" /><div><small>{{ item.tag }}</small><h3>{{ item.title }}</h3><p>{{ item.desc }}</p><time>{{ item.date }}</time></div></article></div>
        </div>
      </section>

      <section class="visit-banner"><img :src="images.guifeng" alt="龟峰山水" loading="lazy" referrerpolicy="no-referrer" /><div class="visit-shade"></div><div class="container visit-inner"><div><p>预约参观　开启美好颐养生活</p><span>欢迎您和家人实地参观，亲身感受龟峰颐养中心的温馨环境与专业服务</span></div><div><a class="button button-gold" href="/contact.html">▣ 预约参观 →</a><a class="button button-light" :href="`tel:${phoneDigits}`">☎ 电话咨询 {{ profile.contact.phone }}</a></div></div></section>
    </main>

    <footer class="site-footer"><div class="container footer-grid"><div class="footer-brand"><img :src="brandLogoUrl" alt="" /><div><strong>弋阳龟峰颐养中心</strong><span>山水相伴　颐养有爱</span></div><p>让每一位长者都拥有尊严、有温度、有品质的生活</p></div><div class="footer-contact"><a :href="`tel:${phoneDigits}`">☎ {{ profile.contact.phone }}</a><span>咨询时间：{{ profile.contact.hotlineTip }}</span><span>⌖ {{ profile.contact.address }}</span></div><div class="qr-placeholder"><b>官方微信</b><span>后续放置二维码</span></div></div><div class="container footer-bottom"><span>© {{ currentYear }} 弋阳龟峰颐养中心</span><span>服务内容与开放情况以机构实际公示为准</span><span v-if="profile.legal.icp">{{ profile.legal.icp }}</span></div></footer>
    <div class="mobile-action-bar"><a :href="`tel:${phoneDigits}`">电话咨询</a><a href="/contact.html">预约参观</a></div>
  </div>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, ref } from 'vue'
import brandLogoUrl from '../assets/guifeng-logo.png'
import { enterpriseProfile as profile } from '../constants/enterpriseProfile'

const SectionTitle = defineComponent({
  props: { title: { type: String, required: true }, subtitle: { type: String, required: true } },
  setup(props) { return () => h('div', { class: 'section-title' }, [h('h2', props.title), h('p', props.subtitle)]) }
})

const currentYear = new Date().getFullYear()
const largeText = ref(false)
const menuOpen = ref(false)
const phoneDigits = computed(() => profile.contact.phone.replace(/\D/g, ''))
const images = {
  guifeng: 'https://navigatechina.com/content/uploadfile/202411/4f061730514288.jpg',
  care: 'https://images.unsplash.com/photo-1584515933487-779824d29309?auto=format&fit=crop&w=1400&q=82',
  elder: 'https://images.unsplash.com/photo-1543333995-a78aea2eee50?auto=format&fit=crop&w=1200&q=82',
  companion: 'https://images.unsplash.com/photo-1559234938-b60fff04894d?auto=format&fit=crop&w=1200&q=82',
  activity: 'https://images.unsplash.com/photo-1773227060944-dc7b00b09ae2?auto=format&fit=crop&w=1200&q=82',
  bedroom: 'https://images.unsplash.com/photo-1760072513376-67a46aab0fd1?auto=format&fit=crop&w=1200&q=82',
  food: 'https://images.unsplash.com/photo-1578167731266-cabac4159bed?auto=format&fit=crop&w=1200&q=82',
  living: 'https://images.unsplash.com/photo-1600210492486-724fe5c67fb0?auto=format&fit=crop&w=1200&q=82',
  lounge: 'https://images.unsplash.com/photo-1600566753190-17f0baa2a6c3?auto=format&fit=crop&w=1200&q=82',
  corridor: 'https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&w=1200&q=82',
  team: 'https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?auto=format&fit=crop&w=1600&q=82'
}
const navItems = [{ label: '首页', href: '#top' }, { label: '关于我们', href: '#about' }, { label: '养老服务', href: '#services' }, { label: '环境设施', href: '#environment' }, { label: '长者生活', href: '#life' }, { label: '新闻动态', href: '#news' }, { label: '入住指南', href: '/pricing.html' }, { label: '联系我们', href: '/contact.html' }]
const featureCards = [{ icon: '院', title: '服务理念', desc: '以人为本，尊重、关爱、专业' }, { icon: '山', title: '舒适环境', desc: '山水相伴，宜居宜养' }, { icon: '心', title: '贴心照护', desc: '用爱陪伴，让晚年更温暖' }, { icon: '盾', title: '健康支持', desc: '专业守护，全方位健康管理' }]
const services = [
  { icon: '护', title: '生活照护', sub: '日常起居照料', desc: '让生活更安心', image: images.care, href: '/services.html' },
  { icon: '康', title: '健康管理', sub: '定期体检 · 健康评估', desc: '慢病管理', image: images.team, href: '/services.html' },
  { icon: '复', title: '康复支持', sub: '专业康复训练', desc: '提升生活能力', image: images.elder, href: '/rehabilitation.html' },
  { icon: '膳', title: '营养膳食', sub: '科学营养搭配', desc: '健康美味可口', image: images.food, href: '/services.html' },
  { icon: '乐', title: '文娱活动', sub: '丰富多彩的文化生活', desc: '乐享晚年时光', image: images.activity, href: '#life' },
  { icon: '安', title: '安全保障', sub: '智能化管理', desc: '全方位安全守护', image: images.corridor, href: '/services.html' }
]
const environments = [{ title: '舒适房型', desc: '温馨舒适，多种房型选择', image: images.bedroom }, { title: '活动空间', desc: '宽敞明亮，丰富多彩', image: images.living }, { title: '营养餐厅', desc: '健康营养，美味可口', image: images.food }, { title: '园林环境', desc: '依山傍水，空气清新', image: images.guifeng }, { title: '无障碍设施', desc: '贴心设计，安全便利', image: images.corridor }]
const lifeMoments = [{ title: '书画雅集', desc: '修身养性，乐在其中', image: images.elder }, { title: '品茗时光', desc: '闲话家常，享受悠闲', image: images.companion }, { title: '康复锻炼', desc: '强身健体，活力常在', image: images.activity }, { title: '生日庆祝', desc: '温馨相伴，快乐加倍', image: images.care }, { title: '家属探访', desc: '亲情相聚，幸福时光', image: images.lounge }]
const safeguards = [{ icon: '♙', title: '专业护理', desc: '持证上岗，经验丰富' }, { icon: '◴', title: '24小时守护', desc: '全天候照护服务' }, { icon: '⌘', title: '温暖陪伴', desc: '用爱心和耐心守护' }]
const latestNews = computed(() => profile.newsList.slice(0, 3))
const newsImages = [images.guifeng, images.companion, images.team]
function toggleLargeText() { largeText.value = !largeText.value }
</script>

<style scoped>@import '../styles/enterprise-home.less';</style>
