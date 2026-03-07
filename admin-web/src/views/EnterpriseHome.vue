<template>
  <div class="enterprise-home" :class="{ 'reading-mode': readingMode }">
    <header class="topbar">
      <div class="container topbar-inner">
        <div class="brand" @click="scrollTo('top')">
          <div class="brand-logo">{{ profile.shortName.slice(0, 1) }}</div>
          <div class="brand-text">
            <div class="name">{{ profile.name }}</div>
            <div class="desc">{{ profile.slogan }}</div>
          </div>
        </div>
        <a-button class="mobile-menu-btn" @click="openMobileMenu">栏目导航</a-button>
        <nav class="nav-list">
          <a v-for="item in navItems" :key="item.id" @click="scrollTo(item.id)">{{ item.label }}</a>
        </nav>
        <div class="top-actions">
          <a-button type="primary" @click="goAdmin">进入管理后台</a-button>
          <a-button @click="goLogin">员工登录</a-button>
        </div>
      </div>
    </header>

    <a-drawer v-model:open="mobileMenuOpen" title="站点导航" placement="right" :width="280">
      <div class="mobile-nav-list">
        <a-button v-for="item in navItems" :key="item.id" block @click="handleMobileNav(item.id)">{{ item.label }}</a-button>
      </div>
    </a-drawer>

    <div class="floating-actions">
      <a-button type="primary" @click="goAdmin">后台入口</a-button>
      <a-button @click="openConsultModal">预约参观</a-button>
      <a-button @click="toggleReadingMode">{{ readingMode ? '标准模式' : '阅读模式' }}</a-button>
      <a-button @click="openMapNavigation">到院导航</a-button>
      <a-button @click="scrollTo('top')">回到顶部</a-button>
    </div>

    <section id="top" class="hero">
      <div class="hero-mask" />
      <div class="container hero-content">
        <div class="hero-badge">企业首页</div>
        <div v-if="contentRiskCount > 0" class="risk-badge">内容待完善 {{ contentRiskCount }} 项</div>
        <h1>{{ profile.heroTitle }}</h1>
        <p>{{ profile.heroDesc }}</p>
        <div class="hero-actions">
          <a-button type="primary" size="large" @click="goAdmin">进入管理后台</a-button>
          <a-button size="large" @click="scrollTo('service-system')">查看服务体系</a-button>
          <a-button size="large" @click="openVrCommunity">VR看社区</a-button>
          <a-button size="large" @click="openJoinUs">加入我们</a-button>
          <a-button size="large" @click="openConsultModal">立即预约</a-button>
        </div>
      </div>
    </section>

    <section class="section stats">
      <div class="container">
        <div class="announcement-strip">
          <div class="announcement-title">公告栏</div>
          <div class="announcement-list">
            <div
              v-for="item in profile.announcements"
              :key="item.title"
              class="announcement-item"
              :class="`announcement-${item.level}`"
            >
              <span class="announcement-date">{{ item.date }}</span>
              <span>{{ item.title }}</span>
            </div>
          </div>
        </div>
        <a-row :gutter="[16, 16]">
          <a-col v-for="item in profile.quickStats" :key="item.label" :xs="12" :md="6">
            <a-card :bordered="false" class="stat-card">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-label">{{ item.label }}</div>
              <div v-if="item.tip" class="stat-tip">{{ item.tip }}</div>
            </a-card>
          </a-col>
        </a-row>
        <div class="freshness-tip" :class="{ 'freshness-warning': contentFreshnessDays > 30 }">
          内容新鲜度：最近更新距今 {{ contentFreshnessDays }} 天，{{ governanceStatusText }}
        </div>
      </div>
    </section>

    <section class="section section-alt guide-section">
      <div class="container">
        <div class="section-title">
          <h2>智能导览</h2>
          <p>输入关键词，快速定位服务、公示、联系方式等内容模块。</p>
        </div>
        <div class="guide-bar">
          <a-input
            v-model:value="guideKeyword"
            placeholder="例如：记忆照护、资质证照、预约参观、联系方式"
            allow-clear
            @pressEnter="runGuideSearch"
          />
          <a-button type="primary" @click="runGuideSearch">快速定位</a-button>
        </div>
        <div class="guide-result">
          <a-tag
            v-for="item in guideResults"
            :key="item.id"
            color="blue"
            class="guide-tag"
            @click="jumpToGuideTarget(item.id)"
          >
            {{ item.label }}
          </a-tag>
          <span v-if="guideKeyword && guideResults.length === 0" class="guide-empty">未命中，建议尝试“服务/公示/联系/活动”。</span>
        </div>
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
            <a-card :bordered="false" class="card">
              <h3>使命</h3>
              <p>{{ profile.mission }}</p>
            </a-card>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-card :bordered="false" class="card">
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
        <div class="news-toolbar">
          <a-input
            v-model:value="newsKeyword"
            allow-clear
            placeholder="搜索新闻标题/内容"
            style="max-width: 320px;"
          />
          <a-segmented v-model:value="activeNewsTag" :options="newsTagOptions" />
        </div>
        <div class="news-list">
          <a-card v-for="item in filteredNewsList" :key="item.title" :bordered="false" class="news-card">
            <div class="news-meta">
              <span class="news-tag">{{ item.tag }}</span>
              <span class="news-date">{{ item.date }}</span>
            </div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.desc }}</p>
          </a-card>
        </div>
        <a-empty v-if="filteredNewsList.length === 0" description="未找到匹配新闻，请调整筛选条件" />
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

    <section id="service-packages" class="section">
      <div class="container">
        <div class="section-title">
          <h2>服务套餐</h2>
          <p>按长者状态匹配服务方案，费用区间公开透明，可到院面评后细化。</p>
        </div>
        <a-row :gutter="[16, 16]">
          <a-col v-for="item in profile.servicePackages" :key="item.name" :xs="24" :md="8">
            <a-card :bordered="false" class="card package-card">
              <h3>{{ item.name }}</h3>
              <p class="package-suitable">适用人群：{{ item.suitableFor }}</p>
              <ul class="package-list">
                <li v-for="highlight in item.highlights" :key="highlight">{{ highlight }}</li>
              </ul>
              <div class="package-fee">{{ item.monthlyFeeRange }}</div>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </section>

    <section id="care-matcher" class="section section-alt">
      <div class="container">
        <div class="section-title">
          <h2>智能照护方案推荐</h2>
          <p>基于年龄、自理能力、认知状态给出初步建议（演示版，可作为到院咨询前参考）。</p>
        </div>
        <a-card :bordered="false" class="card matcher-card">
          <div class="matcher-form">
            <a-select v-model:value="careMatcher.ageGroup" :options="ageGroupOptions" style="width: 220px;" placeholder="选择年龄段" />
            <a-select
              v-model:value="careMatcher.selfCareLevel"
              :options="selfCareOptions"
              style="width: 220px;"
              placeholder="选择自理能力"
            />
            <a-select
              v-model:value="careMatcher.cognitiveState"
              :options="cognitiveOptions"
              style="width: 220px;"
              placeholder="选择认知状态"
            />
            <a-button type="primary" @click="generateCareRecommendation">生成建议</a-button>
            <a-button @click="resetCareMatcher">重置</a-button>
          </div>
          <a-alert
            v-if="careRecommendation"
            type="info"
            show-icon
            :message="careRecommendation.title"
            :description="careRecommendation.desc"
            style="margin-top: 14px;"
          />
        </a-card>
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
        <a-table :dataSource="profile.cityTiers" :pagination="false" rowKey="tier" size="middle" class="tier-table">
          <a-table-column title="分档" data-index="tier" key="tier" />
          <a-table-column title="城市/区域" data-index="cityGroup" key="cityGroup" />
          <a-table-column title="服务模型" data-index="model" key="model" />
        </a-table>
      </div>
    </section>

    <section id="compliance" class="section">
      <div class="container">
        <div class="section-title">
          <h2>机构公示</h2>
          <p>真实机构信息配置化：机构简介、资质证照、地图导航、图片版权说明。</p>
        </div>
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :md="8">
            <a-card :bordered="false" class="card">
              <h3>机构简介公示</h3>
              <ul class="compliance-list">
                <li v-for="item in profile.compliance.organizationInfo" :key="item">{{ item }}</li>
              </ul>
            </a-card>
          </a-col>
          <a-col :xs="24" :md="16">
            <a-card :bordered="false" class="card">
              <h3>资质证照公示</h3>
              <a-table :dataSource="profile.compliance.qualifications" :pagination="false" rowKey="name" size="small">
                <a-table-column title="证照名称" data-index="name" key="name" />
                <a-table-column title="编号" data-index="no" key="no" />
                <a-table-column title="签发单位" data-index="issuer" key="issuer" />
                <a-table-column title="有效期" data-index="validUntil" key="validUntil" />
              </a-table>
            </a-card>
          </a-col>
        </a-row>
        <a-row :gutter="[16, 16]" style="margin-top: 12px;">
          <a-col :xs="24" :md="12">
            <a-card :bordered="false" class="card">
              <h3>地图与到院导航</h3>
              <p>{{ profile.compliance.map.serviceRadius }}</p>
              <p class="map-note">{{ profile.compliance.map.note }}</p>
              <div class="section-actions">
                <a-button @click="openMapNavigation">打开导航</a-button>
              </div>
            </a-card>
          </a-col>
          <a-col :xs="24" :md="12">
            <a-card :bordered="false" class="card">
              <h3>图片版权说明</h3>
              <ul class="compliance-list">
                <li>素材来源：{{ profile.compliance.mediaRights.photoSource }}</li>
                <li>版权归属：{{ profile.compliance.mediaRights.copyrightOwner }}</li>
                <li>授权范围：{{ profile.compliance.mediaRights.authorizedScope }}</li>
                <li>最近核验：{{ profile.compliance.mediaRights.lastVerifiedAt }}</li>
                <li>联系邮箱：{{ profile.compliance.mediaRights.contact }}</li>
              </ul>
            </a-card>
          </a-col>
        </a-row>
        <div class="section-actions compliance-actions">
          <a-button @click="copyComplianceSummary">复制公示摘要</a-button>
          <a-button @click="downloadComplianceSnapshot">下载公示快照(JSON)</a-button>
          <a-button @click="copyOpsTodo">复制运营待办</a-button>
          <a-button @click="downloadAgencyBrief">下载机构资料包</a-button>
        </div>
        <div class="ops-status">内容治理状态：当前识别到 <b>{{ contentRiskCount }}</b> 个占位/示例项。</div>
      </div>
    </section>

    <section id="ops-governance" class="section section-alt">
      <div class="container">
        <div class="section-title">
          <h2>运营治理看板</h2>
          <p>面向运营同事的内容发布清单与部门值班信息，支持按清单更新，无需开发介入。</p>
        </div>
        <div class="governance-metrics">
          <div class="metric-box">
            <div class="metric-label">治理评分</div>
            <div class="metric-value">{{ governanceScore }}</div>
            <div class="metric-tip">综合占位风险、时效、清单完成度</div>
          </div>
          <div class="metric-box">
            <div class="metric-label">清单完成率</div>
            <div class="metric-value">{{ checklistCompletionRate }}%</div>
            <div class="metric-tip">已完成 {{ checklistDoneCount }} / {{ profile.contentChecklist.length }}</div>
          </div>
          <div class="metric-box">
            <div class="metric-label">证照预警</div>
            <div class="metric-value">{{ expiringQualificationCount }}</div>
            <div class="metric-tip">180天内到期证照数</div>
          </div>
        </div>
        <a-row :gutter="[16, 16]">
          <a-col :xs="24" :lg="14">
            <a-card :bordered="false" class="card">
              <h3>内容发布清单</h3>
              <a-table :dataSource="profile.contentChecklist" :pagination="false" rowKey="category" size="small">
                <a-table-column title="清单项" data-index="category" key="category" width="150" />
                <a-table-column title="维护部门" data-index="owner" key="owner" width="140" />
                <a-table-column title="状态" key="status" width="110">
                  <template #default="{ record }">
                    <a-tag :color="checklistStatusColor(record.status)">{{ record.status }}</a-tag>
                  </template>
                </a-table-column>
                <a-table-column title="最近更新" data-index="updatedAt" key="updatedAt" width="130" />
              </a-table>
            </a-card>
          </a-col>
          <a-col :xs="24" :lg="10">
            <a-card :bordered="false" class="card">
              <h3>六部门值班联络</h3>
              <div class="duty-board">
                <div v-for="item in profile.departmentDutyBoard" :key="item.department" class="duty-item">
                  <div class="duty-head">
                    <span class="duty-name">{{ item.department }}</span>
                    <span class="duty-hours">{{ item.dutyHours }}</span>
                  </div>
                  <div class="duty-scope">{{ item.serviceScope }}</div>
                  <div class="duty-contact">值班电话：{{ item.contact }}</div>
                  <div class="duty-actions">
                    <a-button size="small" @click="copyDutyContact(item.department, item.contact)">复制电话</a-button>
                  </div>
                </div>
              </div>
            </a-card>
          </a-col>
        </a-row>
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

    <section class="section section-alt appointment-strip">
      <div class="container appointment-inner">
        <div>
          <h3>想了解是否适合家人入住？</h3>
          <p>支持电话咨询、线上初评、到院参观三步服务，最快当天给出照护建议。</p>
        </div>
        <div class="appointment-actions">
          <a-button type="primary" size="large" @click="openConsultModal">立即预约</a-button>
          <a-button size="large" @click="openMapNavigation">查看路线</a-button>
          <a-button size="large" @click="openConsultRecordsModal">查看预约记录（{{ consultRecordCount }}）</a-button>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="container">
        <div class="section-title">
          <h2>常见问题</h2>
          <p>围绕参观、入住、照护与费用的高频问题解答。</p>
        </div>
        <div class="faq-search">
          <a-input
            v-model:value="faqKeyword"
            allow-clear
            placeholder="搜索问题或答案关键词（如：入住、费用、失能）"
            style="max-width: 420px;"
          />
        </div>
        <a-collapse :bordered="false" class="faq-panel">
          <a-collapse-panel v-for="item in filteredFaq" :key="item.question" :header="item.question">
            <p class="faq-answer">{{ item.answer }}</p>
          </a-collapse-panel>
        </a-collapse>
        <a-empty v-if="filteredFaq.length === 0" description="未命中FAQ，请换个关键词再试" />
      </div>
    </section>

    <section id="family-voice" class="section section-alt">
      <div class="container">
        <div class="section-title">
          <h2>家属评价</h2>
          <p>来自真实家属的反馈，持续改进服务质量与沟通体验。</p>
        </div>
        <a-row :gutter="[16, 16]">
          <a-col v-for="item in profile.testimonials" :key="`${item.name}-${item.date}`" :xs="24" :md="8">
            <a-card :bordered="false" class="card testimonial-card">
              <p class="testimonial-content">“{{ item.content }}”</p>
              <div class="testimonial-meta">
                <span>{{ item.name }} · {{ item.relation }}</span>
                <span>{{ item.date }}</span>
              </div>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </section>

    <footer class="footer">
      <div class="container footer-inner">
        <span>© {{ currentYear }} {{ profile.name }}</span>
        <span>{{ profile.slogan }}</span>
        <span>最近更新：{{ profile.publishMeta.lastUpdated }}</span>
        <span>维护人：{{ profile.publishMeta.maintainer }}</span>
        <span>下次复核：{{ profile.publishMeta.nextReviewDate }}</span>
        <span v-if="profile.legal.icp">{{ profile.legal.icp }}</span>
        <span v-if="profile.legal.publicSecurityRecord">{{ profile.legal.publicSecurityRecord }}</span>
      </div>
    </footer>

    <a-modal
      v-model:open="consultModalOpen"
      title="预约咨询"
      :confirmLoading="consultSubmitting"
      ok-text="提交预约"
      cancel-text="取消"
      @ok="submitConsult"
    >
      <a-form ref="consultFormRef" :model="consultForm" layout="vertical">
        <a-form-item label="联系人" name="name" :rules="[{ required: true, message: '请输入联系人姓名' }]">
          <a-input v-model:value="consultForm.name" placeholder="例如：王女士" />
        </a-form-item>
        <a-form-item label="联系电话" name="phone" :rules="[{ required: true, message: '请输入联系电话' }]">
          <a-input v-model:value="consultForm.phone" placeholder="例如：13800000000" />
        </a-form-item>
        <a-form-item label="关注需求" name="need">
          <a-input v-model:value="consultForm.need" placeholder="例如：失能照护、记忆照护、短住体验" />
        </a-form-item>
        <a-form-item label="优先对接部门" name="department">
          <a-select
            v-model:value="consultForm.department"
            allow-clear
            placeholder="选择后可优先分派（可不选）"
            :options="profile.departmentDutyBoard.map((item) => ({ label: item.department, value: item.department }))"
          />
        </a-form-item>
        <a-form-item label="期望到院日期" name="visitDate">
          <a-date-picker v-model:value="consultForm.visitDate" style="width: 100%;" />
        </a-form-item>
      </a-form>
      <div class="consult-tip">提交后将保存在本地浏览器（演示环境），并提示你电话联系院方确认到院时间。</div>
      <div class="consult-actions">
        <a-button size="small" @click="exportConsultRecords">导出预约记录(JSON)</a-button>
        <a-button size="small" danger @click="clearConsultRecords">清空预约记录</a-button>
      </div>
    </a-modal>

    <a-modal v-model:open="consultRecordsModalOpen" title="预约记录管理" width="760px" :footer="null">
      <div class="consult-summary">
        <a-tag color="blue">总记录 {{ consultRecordCount }}</a-tag>
        <a-tag color="green">今日新增 {{ consultTodayCount }}</a-tag>
        <a-tag color="purple">最多选择部门：{{ consultTopDepartment }}</a-tag>
      </div>
      <div class="consult-record-actions">
        <a-button size="small" @click="exportConsultRecords">导出JSON</a-button>
        <a-button size="small" @click="exportConsultRecordsCsv">导出CSV</a-button>
        <a-button size="small" danger @click="clearConsultRecords">清空全部</a-button>
      </div>
      <a-table :dataSource="consultRecords" :pagination="{ pageSize: 6 }" rowKey="createdAt" size="small">
        <a-table-column title="提交时间" data-index="createdAt" key="createdAt" width="160" />
        <a-table-column title="联系人" data-index="name" key="name" width="110" />
        <a-table-column title="联系电话" data-index="phone" key="phone" width="130" />
        <a-table-column title="对接部门" data-index="department" key="department" width="120" />
        <a-table-column title="意向日期" data-index="visitDate" key="visitDate" width="120" />
        <a-table-column title="需求" data-index="need" key="need" />
      </a-table>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import dayjs from 'dayjs'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { enterpriseProfile as profile } from '../constants/enterpriseProfile'
import { getToken } from '../utils/auth'

const router = useRouter()
const hasToken = computed(() => Boolean(getToken()))
const currentYear = new Date().getFullYear()
const mobileMenuOpen = ref(false)
const consultModalOpen = ref(false)
const consultRecordsModalOpen = ref(false)
const consultSubmitting = ref(false)
const consultFormRef = ref<FormInstance>()
const consultForm = ref({
  name: '',
  phone: '',
  need: '',
  department: '',
  visitDate: null as any
})
const consultVersion = ref(0)
const newsKeyword = ref('')
const activeNewsTag = ref('全部')
const guideKeyword = ref('')
const faqKeyword = ref('')
const readingMode = ref(false)
const careMatcher = ref({
  ageGroup: '',
  selfCareLevel: '',
  cognitiveState: ''
})
const careRecommendation = ref<{ title: string; desc: string } | null>(null)
const navItems = [
  { id: 'service-system', label: '服务体系' },
  { id: 'resident-system', label: '龟峰居民' },
  { id: 'community', label: '社区动态' },
  { id: 'news', label: '新闻资讯' },
  { id: 'activities', label: '居民活动' },
  { id: 'service-packages', label: '服务套餐' },
  { id: 'care-matcher', label: '照护推荐' },
  { id: 'about-us', label: '关于我们' },
  { id: 'join-us', label: '加入我们' },
  { id: 'cooperation', label: '业务合作' },
  { id: 'city-tier', label: '城市分档' },
  { id: 'compliance', label: '机构公示' },
  { id: 'ops-governance', label: '运营治理' },
  { id: 'family-voice', label: '家属评价' },
  { id: 'contact', label: '联系我们' }
] as const

const ageGroupOptions = [
  { label: '60-69岁', value: '60-69' },
  { label: '70-79岁', value: '70-79' },
  { label: '80岁及以上', value: '80+' }
]

const selfCareOptions = [
  { label: '可自理', value: 'self' },
  { label: '部分协助', value: 'partial' },
  { label: '高度依赖', value: 'depend' }
]

const cognitiveOptions = [
  { label: '认知正常', value: 'normal' },
  { label: '轻度认知下降', value: 'mild' },
  { label: '中重度认知障碍', value: 'severe' }
]

function loadConsultRecords() {
  const raw = localStorage.getItem('enterprise_consult_records')
  const list = raw ? JSON.parse(raw) : []
  return Array.isArray(list) ? list : []
}

const consultRecords = computed(() => {
  consultVersion.value
  return loadConsultRecords()
})

const consultRecordCount = computed(() => consultRecords.value.length)

const contentRiskCount = computed(() => {
  const payload = JSON.stringify(profile)
  const matches = payload.match(/示例|XXXXXXXX|"#"/g)
  return matches ? matches.length : 0
})

const contentFreshnessDays = computed(() => {
  const date = dayjs(profile.publishMeta.lastUpdated)
  if (!date.isValid()) return 999
  return Math.max(dayjs().diff(date, 'day'), 0)
})

const governanceStatusText = computed(() => {
  if (contentFreshnessDays.value <= 15) return '内容维护状态优秀'
  if (contentFreshnessDays.value <= 30) return '内容维护状态正常'
  return '建议尽快复核内容并更新'
})

const checklistDoneCount = computed(() => profile.contentChecklist.filter((item) => item.status === '已完成').length)

const checklistCompletionRate = computed(() => {
  if (!profile.contentChecklist.length) return 0
  return Math.round((checklistDoneCount.value / profile.contentChecklist.length) * 100)
})

const expiringQualificationCount = computed(() => {
  const deadline = dayjs().add(180, 'day')
  return profile.compliance.qualifications.filter((item) => {
    const validUntil = item.validUntil || ''
    if (validUntil.includes('长期')) return false
    const dateMatch = validUntil.match(/\d{4}-\d{2}-\d{2}/)
    if (!dateMatch) return false
    const date = dayjs(dateMatch[0])
    return date.isValid() && date.isBefore(deadline)
  }).length
})

const governanceScore = computed(() => {
  const freshnessPenalty = contentFreshnessDays.value > 30 ? Math.min(20, contentFreshnessDays.value - 30) : 0
  const checklistPenalty = Math.max(0, 30 - checklistCompletionRate.value / 3)
  const placeholderPenalty = Math.min(contentRiskCount.value * 4, 20)
  const qualificationPenalty = Math.min(expiringQualificationCount.value * 3, 10)
  const score = 100 - freshnessPenalty - checklistPenalty - placeholderPenalty - qualificationPenalty
  return Math.max(Math.round(score), 0)
})

const newsTagOptions = computed(() => {
  const tags = Array.from(new Set(profile.newsList.map((item) => item.tag)))
  return ['全部', ...tags]
})

const filteredNewsList = computed(() => {
  const keyword = newsKeyword.value.trim().toLowerCase()
  return profile.newsList.filter((item) => {
    const tagMatch = activeNewsTag.value === '全部' || item.tag === activeNewsTag.value
    if (!tagMatch) return false
    if (!keyword) return true
    return `${item.title} ${item.desc} ${item.tag}`.toLowerCase().includes(keyword)
  })
})

const filteredFaq = computed(() => {
  const keyword = faqKeyword.value.trim().toLowerCase()
  if (!keyword) return profile.faq
  return profile.faq.filter((item) => `${item.question} ${item.answer}`.toLowerCase().includes(keyword))
})

const consultTodayCount = computed(() => {
  const today = dayjs().format('YYYY-MM-DD')
  return consultRecords.value.filter((item) => String(item.createdAt || '').startsWith(today)).length
})

const consultTopDepartment = computed(() => {
  const countMap = new Map<string, number>()
  consultRecords.value.forEach((item) => {
    const key = item.department || '未指定'
    countMap.set(key, (countMap.get(key) || 0) + 1)
  })
  const first = Array.from(countMap.entries()).sort((a, b) => b[1] - a[1])[0]
  return first ? `${first[0]}（${first[1]}）` : '暂无'
})

const guideResults = computed(() => {
  const keyword = guideKeyword.value.trim()
  if (!keyword) return navItems.slice(0, 6)
  const aliasMap = [
    { id: 'service-system', label: '服务体系', aliases: ['服务', '护理', '记忆', '康复'] },
    { id: 'resident-system', label: '龟峰居民', aliases: ['居民', '故事', '组织'] },
    { id: 'community', label: '社区动态', aliases: ['社区', '动态'] },
    { id: 'news', label: '新闻资讯', aliases: ['新闻', '资讯', '公告'] },
    { id: 'activities', label: '居民活动', aliases: ['活动', '社团'] },
    { id: 'compliance', label: '机构公示', aliases: ['证照', '资质', '公示', '版权'] },
    { id: 'ops-governance', label: '运营治理', aliases: ['运营', '清单', '值班'] },
    { id: 'contact', label: '联系我们', aliases: ['联系', '电话', '地址', '地图'] }
  ]
  return aliasMap.filter((item) => `${item.label}${item.aliases.join('')}`.includes(keyword))
})

onMounted(() => {
  document.title = `${profile.name} - 企业首页`
  const cachedReadingMode = localStorage.getItem('enterprise_home_reading_mode')
  if (cachedReadingMode === '1') readingMode.value = true
})

function openMobileMenu() {
  mobileMenuOpen.value = true
}

function handleMobileNav(id: string) {
  mobileMenuOpen.value = false
  scrollTo(id)
}

function runGuideSearch() {
  if (!guideKeyword.value.trim()) {
    message.info('请输入关键词后再搜索。')
    return
  }
  if (!guideResults.value.length) {
    message.warning('未匹配到栏目，请尝试“服务、公示、联系、活动”等关键词。')
    return
  }
  jumpToGuideTarget(guideResults.value[0].id)
}

function jumpToGuideTarget(id: string) {
  scrollTo(id)
}

function toggleReadingMode() {
  readingMode.value = !readingMode.value
  localStorage.setItem('enterprise_home_reading_mode', readingMode.value ? '1' : '0')
}

function generateCareRecommendation() {
  const { ageGroup, selfCareLevel, cognitiveState } = careMatcher.value
  if (!ageGroup || !selfCareLevel || !cognitiveState) {
    message.warning('请先完整选择年龄、自理能力和认知状态。')
    return
  }
  if (cognitiveState === 'severe') {
    careRecommendation.value = {
      title: '建议：记忆照护计划 + 家属协同沟通',
      desc: '推荐优先评估记忆照护专区，结合行为支持、情绪安抚与多学科干预，提升安全性与生活稳定性。'
    }
    return
  }
  if (selfCareLevel === 'depend') {
    careRecommendation.value = {
      title: '建议：专业护理计划',
      desc: '推荐分级护理+夜间巡护+慢病管理组合，先进行到院综合评估后确定护理等级与服务频次。'
    }
    return
  }
  if (selfCareLevel === 'partial' || ageGroup === '80+') {
    careRecommendation.value = {
      title: '建议：安心颐养计划（增强版）',
      desc: '推荐生活照料、营养管理、康复训练与健康随访组合，保障日常支持与功能维持。'
    }
    return
  }
  careRecommendation.value = {
    title: '建议：安心颐养计划（标准版）',
    desc: '推荐以活力生活与健康促进为核心，定期复评并根据身体状态动态调整服务内容。'
  }
}

function resetCareMatcher() {
  careMatcher.value = { ageGroup: '', selfCareLevel: '', cognitiveState: '' }
  careRecommendation.value = null
}

function goLogin() {
  router.push('/login?redirect=/portal')
}

function goAdmin() {
  router.push(hasToken.value ? '/portal' : '/login?redirect=/portal')
}

function openMapNavigation() {
  const url = profile.compliance.map.navigationUrl
  if (!url || url === '#') {
    scrollTo('contact')
    return
  }
  window.open(url, '_blank', 'noopener,noreferrer')
}

function openConsultModal() {
  consultModalOpen.value = true
}

function openConsultRecordsModal() {
  consultRecordsModalOpen.value = true
}

async function submitConsult() {
  if (!consultFormRef.value) return
  try {
    await consultFormRef.value.validate()
  } catch (_error) {
    return
  }
  consultSubmitting.value = true
  try {
    const payload = {
      ...consultForm.value,
      visitDate: consultForm.value.visitDate ? dayjs(consultForm.value.visitDate).format('YYYY-MM-DD') : '',
      createdAt: dayjs().format('YYYY-MM-DD HH:mm:ss')
    }
    const list = loadConsultRecords()
    list.unshift(payload)
    localStorage.setItem('enterprise_consult_records', JSON.stringify(list.slice(0, 200)))
    consultVersion.value += 1
    consultModalOpen.value = false
    consultForm.value = { name: '', phone: '', need: '', department: '', visitDate: null }
    message.success('预约信息已记录，请电话联系院方确认。')
    openAppointment()
  } finally {
    consultSubmitting.value = false
  }
}

function openAppointment() {
  const phone = profile.contact.phone || ''
  const onlyDigits = phone.replace(/[^\d-]/g, '')
  if (!onlyDigits) {
    scrollTo('contact')
    return
  }
  window.location.href = `tel:${onlyDigits}`
}

function exportConsultRecords() {
  const list = loadConsultRecords()
  if (!list.length) {
    message.info('暂无预约记录可导出。')
    return
  }
  const blob = new Blob([JSON.stringify(list, null, 2)], { type: 'application/json;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `consult-records-${dayjs().format('YYYYMMDD-HHmmss')}.json`
  a.click()
  URL.revokeObjectURL(url)
}

function exportConsultRecordsCsv() {
  const list = loadConsultRecords()
  if (!list.length) {
    message.info('暂无预约记录可导出。')
    return
  }
  const headers = ['提交时间', '联系人', '联系电话', '对接部门', '意向日期', '需求']
  const rows = list.map((item) => [
    item.createdAt || '',
    item.name || '',
    item.phone || '',
    item.department || '',
    item.visitDate || '',
    item.need || ''
  ])
  const csv = [headers, ...rows]
    .map((row) => row.map((cell) => `"${String(cell).replace(/"/g, '""')}"`).join(','))
    .join('\n')
  const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `consult-records-${dayjs().format('YYYYMMDD-HHmmss')}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

function clearConsultRecords() {
  localStorage.removeItem('enterprise_consult_records')
  consultVersion.value += 1
  consultRecordsModalOpen.value = false
  message.success('预约记录已清空。')
}

async function copyComplianceSummary() {
  const summary = [
    `机构：${profile.name}`,
    `最近更新：${profile.publishMeta.lastUpdated}`,
    `维护人：${profile.publishMeta.maintainer}`,
    `地址：${profile.contact.address}`,
    `电话：${profile.contact.phone}`,
    `资质：${profile.compliance.qualifications.map((item) => item.name).join('、')}`
  ].join('\n')
  try {
    await navigator.clipboard.writeText(summary)
    message.success('公示摘要已复制。')
  } catch (_error) {
    message.warning('当前环境不支持自动复制。')
  }
}

function downloadComplianceSnapshot() {
  const payload = {
    name: profile.name,
    updatedAt: profile.publishMeta.lastUpdated,
    maintainer: profile.publishMeta.maintainer,
    contact: profile.contact,
    legal: profile.legal,
    compliance: profile.compliance
  }
  const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `enterprise-compliance-${profile.publishMeta.lastUpdated}.json`
  a.click()
  URL.revokeObjectURL(url)
}

function downloadAgencyBrief() {
  const content = [
    `# ${profile.name} 机构资料包`,
    '',
    `- 宣传语：${profile.slogan}`,
    `- 机构定位：${profile.subtitle}`,
    `- 联系电话：${profile.contact.phone}`,
    `- 联系邮箱：${profile.contact.email}`,
    `- 机构地址：${profile.contact.address}`,
    `- 最近更新：${profile.publishMeta.lastUpdated}`,
    '',
    '## 核心服务体系',
    ...profile.serviceSystem.map((item) => `- ${item.title}：${item.desc}`)
  ].join('\n')
  const blob = new Blob([content], { type: 'text/markdown;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `agency-brief-${dayjs().format('YYYYMMDD')}.md`
  a.click()
  URL.revokeObjectURL(url)
}

async function copyOpsTodo() {
  const lines = [
    `【${profile.name} 运营更新待办】`,
    `- 最近更新：${profile.publishMeta.lastUpdated}`,
    `- 下次复核：${profile.publishMeta.nextReviewDate}`,
    `- 维护人：${profile.publishMeta.maintainer}`,
    `- 风险项数量：${contentRiskCount.value}`,
    `- 待更新：新闻资讯、活动排期、资质有效期、地图导航链接、版权核验日期`
  ]
  try {
    await navigator.clipboard.writeText(lines.join('\n'))
    message.success('运营待办已复制。')
  } catch (_error) {
    message.warning('当前环境不支持自动复制。')
  }
}

function openJoinUs() {
  if (!profile.joinUsUrl || profile.joinUsUrl === '#') {
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

function checklistStatusColor(status: string) {
  if (status === '已完成') return 'success'
  if (status === '待更新') return 'warning'
  return 'processing'
}

async function copyDutyContact(department: string, contact: string) {
  try {
    await navigator.clipboard.writeText(`${department}：${contact}`)
    message.success(`${department}电话已复制。`)
  } catch (_error) {
    message.warning('当前环境不支持自动复制。')
  }
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
  font-weight: 700;
  color: #fff;
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

.mobile-menu-btn {
  display: none;
}

.mobile-nav-list {
  display: grid;
  gap: 10px;
}

.floating-actions {
  position: fixed;
  right: 18px;
  bottom: 24px;
  z-index: 28;
  display: grid;
  gap: 8px;
}

.hero {
  position: relative;
  min-height: 630px;
  padding: 140px 0 95px;
  background:
    linear-gradient(120deg, rgba(11, 47, 121, 0.82) 0%, rgba(28, 88, 187, 0.72) 55%, rgba(59, 132, 243, 0.68) 100%),
    url('../assets/home.jpg') center / cover no-repeat;
}

.hero-mask {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 15% 18%, rgba(255, 255, 255, 0.2), rgba(255, 255, 255, 0) 40%);
}

.hero-content {
  position: relative;
  color: #fff;
}

.hero-badge {
  display: inline-block;
  margin-bottom: 16px;
  font-size: 13px;
  border-radius: 999px;
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.16);
}

.risk-badge {
  display: inline-block;
  margin-left: 8px;
  margin-bottom: 16px;
  font-size: 12px;
  border-radius: 999px;
  padding: 6px 10px;
  color: #7a2b00;
  background: rgba(255, 241, 224, 0.9);
}

.hero h1 {
  margin: 0;
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

.freshness-tip {
  margin-top: 14px;
  padding: 10px 12px;
  border-radius: 10px;
  color: #30537f;
  background: rgba(232, 243, 255, 0.9);
  font-size: 13px;
}

.freshness-warning {
  color: #7a3411;
  background: #fff4e8;
}

.guide-section {
  padding-top: 46px;
}

.governance-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin: 0 0 14px;
}

.metric-box {
  background: #fff;
  border-radius: 12px;
  padding: 14px;
}

.metric-label {
  color: #5f7391;
  font-size: 12px;
}

.metric-value {
  margin-top: 6px;
  font-size: 28px;
  color: #2156b0;
  font-weight: 700;
}

.metric-tip {
  margin-top: 2px;
  color: #8a99b0;
  font-size: 12px;
}

.guide-bar {
  margin-top: 14px;
  display: flex;
  gap: 10px;
  align-items: center;
}

.guide-result {
  margin-top: 12px;
  min-height: 24px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}

.guide-tag {
  cursor: pointer;
}

.guide-empty {
  color: #8596b2;
  font-size: 13px;
}

.announcement-strip {
  margin-bottom: 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 10px 20px rgba(21, 55, 118, 0.12);
  padding: 12px 14px;
}

.announcement-title {
  font-size: 13px;
  color: #2d4a79;
  font-weight: 700;
  margin-bottom: 8px;
}

.announcement-list {
  display: grid;
  gap: 6px;
}

.announcement-item {
  border-radius: 10px;
  padding: 7px 10px;
  font-size: 13px;
}

.announcement-date {
  margin-right: 8px;
  color: #7f92af;
}

.announcement-info {
  background: #edf5ff;
}

.announcement-success {
  background: #edf9f1;
}

.announcement-warning {
  background: #fff5e9;
}

.stat-card,
.card,
.news-card {
  border-radius: 14px;
}

.stat-card {
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

.card h3,
.news-card h3 {
  margin: 0;
  color: #24395e;
}

.card p,
.news-card p {
  margin: 10px 0 0;
  color: #667890;
  line-height: 1.8;
}

.news-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.news-toolbar {
  margin: 14px 0;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

.news-meta {
  display: flex;
  justify-content: space-between;
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

.package-card {
  height: 100%;
}

.package-suitable {
  color: #3e5f8d;
  font-weight: 600;
}

.package-list {
  margin: 10px 0 0;
  padding-left: 18px;
  color: #647995;
  line-height: 1.7;
}

.package-fee {
  margin-top: 12px;
  color: #1f58b2;
  font-weight: 700;
}

.matcher-card {
  padding: 2px;
}

.matcher-form {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

.activity-meta {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #6b7f9f;
}

.flow-list {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
}

.flow-item {
  border-radius: 12px;
  background: #fff;
  padding: 16px 14px;
}

.flow-index {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-weight: 700;
  color: #fff;
  background: #2f74df;
}

.flow-text {
  margin-top: 10px;
  color: #385173;
  font-weight: 600;
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

.compliance-list {
  margin: 10px 0 0;
  padding-left: 18px;
  color: #667890;
  line-height: 1.9;
}

.map-note {
  font-size: 13px;
  color: #8a99b0;
}

.section-actions {
  margin-top: 14px;
}

.compliance-actions,
.consult-actions,
.consult-record-actions,
.appointment-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.ops-status {
  margin-top: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  background: rgba(235, 245, 255, 0.75);
  color: #35547d;
  font-size: 13px;
}

.duty-board {
  display: grid;
  gap: 10px;
}

.duty-item {
  border-radius: 10px;
  padding: 12px;
  background: #f4f8ff;
}

.duty-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.duty-name {
  font-weight: 700;
  color: #294777;
}

.duty-hours {
  font-size: 12px;
  color: #6882a6;
}

.duty-scope {
  margin-top: 6px;
  color: #4c6283;
  font-size: 13px;
}

.duty-contact {
  margin-top: 4px;
  color: #2458ae;
  font-size: 13px;
}

.duty-actions {
  margin-top: 8px;
}

.contact-card {
  border-radius: 16px;
  background: #fff;
  padding: 30px;
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

.appointment-strip {
  padding: 36px 0;
}

.appointment-inner {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: center;
  background: #fff;
  border-radius: 14px;
  padding: 20px 24px;
}

.faq-panel :deep(.ant-collapse-item) {
  border: none;
  margin-bottom: 10px;
  background: #fff;
  border-radius: 12px;
}

.faq-search {
  margin: 10px 0 12px;
}

.faq-answer {
  margin: 0;
  color: #5c7292;
  line-height: 1.8;
}

.testimonial-card {
  height: 100%;
}

.testimonial-content {
  margin: 0;
  color: #415d84;
  line-height: 1.9;
}

.testimonial-meta {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  color: #7d91ad;
  font-size: 12px;
}

.consult-tip {
  margin-top: 6px;
  color: #8a98ad;
  font-size: 12px;
}

.consult-summary {
  margin-bottom: 10px;
  display: flex;
  gap: 8px;
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

.reading-mode {
  font-size: 17px;
}

.reading-mode .card p,
.reading-mode .news-card p,
.reading-mode .faq-answer {
  font-size: 16px;
  line-height: 2;
}

.reading-mode .testimonial-content {
  font-size: 16px;
  line-height: 2;
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

  .mobile-menu-btn {
    display: inline-flex;
    grid-area: actions;
    justify-self: end;
    margin-left: 8px;
  }

  .nav-list {
    display: none;
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

  .guide-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .governance-metrics {
    grid-template-columns: 1fr;
  }

  .matcher-form {
    flex-direction: column;
    align-items: stretch;
  }
}

@media (max-width: 760px) {
  .container {
    width: calc(100vw - 24px);
  }

  .floating-actions {
    right: 12px;
    bottom: 18px;
  }

  .flow-list {
    grid-template-columns: 1fr 1fr;
  }

  .appointment-inner {
    flex-direction: column;
    align-items: flex-start;
    padding: 18px;
  }
}

@media (max-width: 520px) {
  .flow-list {
    grid-template-columns: 1fr;
  }

  .floating-actions {
    display: none;
  }
}
</style>
