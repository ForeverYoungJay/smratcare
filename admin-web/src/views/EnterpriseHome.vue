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
        <div class="section-actions governance-tools">
          <a-button type="primary" @click="openProfileConfig">运营配置中心</a-button>
          <a-button @click="exportProfileConfig">导出当前配置(JSON)</a-button>
          <a-button @click="downloadProfileConfigTemplate">下载配置模板</a-button>
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
        <span>维护人：{{ pageMaintainer }}</span>
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

    <a-modal
      v-model:open="profileConfigOpen"
      title="运营配置中心"
      width="920px"
      :confirmLoading="profileConfigSaving"
      ok-text="保存并生效"
      cancel-text="取消"
      @ok="saveProfileConfig"
    >
      <a-alert
        type="info"
        show-icon
        style="margin-bottom: 12px;"
        message="用于运营同事更新机构简介、公示证照、联系方式、值班与发布清单；保存后立即生效并写入浏览器本地。"
      />
      <a-textarea v-model:value="profileConfigDraft" :rows="20" />
      <div class="config-modal-actions">
        <a-button size="small" @click="formatProfileConfigDraft">JSON格式化</a-button>
        <a-button size="small" @click="reloadProfileConfigDraft">从当前页面重载</a-button>
        <a-button size="small" @click="importProfileConfigFromFile">导入JSON文件</a-button>
        <a-button size="small" danger @click="resetProfileConfig">恢复默认配置</a-button>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import dayjs from 'dayjs'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { enterpriseProfile, type EnterpriseProfile } from '../constants/enterpriseProfile'
import { useUserStore } from '../stores/user'
import { getToken } from '../utils/auth'

const router = useRouter()
const userStore = useUserStore()
const PROFILE_OVERRIDE_STORAGE_KEY = 'enterprise_profile_override_v1'
const hasToken = computed(() => Boolean(getToken()))
const profileConfigOpen = ref(false)
const profileConfigSaving = ref(false)
const profileConfigDraft = ref('')
const profile = reactive<EnterpriseProfile>(buildRuntimeProfile())
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

function deepClone<T>(value: T): T {
  return JSON.parse(JSON.stringify(value))
}

function isPlainObject(value: unknown): value is Record<string, any> {
  return Object.prototype.toString.call(value) === '[object Object]'
}

function mergeProfile(base: any, patch: any): any {
  if (!isPlainObject(base) || !isPlainObject(patch)) {
    return patch == null ? base : patch
  }
  const merged: Record<string, any> = { ...base }
  Object.keys(patch).forEach((key) => {
    const baseValue = merged[key]
    const patchValue = patch[key]
    if (Array.isArray(patchValue)) {
      merged[key] = patchValue
      return
    }
    if (isPlainObject(baseValue) && isPlainObject(patchValue)) {
      merged[key] = mergeProfile(baseValue, patchValue)
      return
    }
    merged[key] = patchValue
  })
  return merged
}

function loadProfileOverride(): Partial<EnterpriseProfile> {
  const raw = localStorage.getItem(PROFILE_OVERRIDE_STORAGE_KEY)
  if (!raw) return {}
  try {
    const parsed = JSON.parse(raw)
    return isPlainObject(parsed) ? parsed : {}
  } catch (_error) {
    return {}
  }
}

function buildRuntimeProfile(): EnterpriseProfile {
  const base = deepClone(enterpriseProfile)
  const override = loadProfileOverride()
  return mergeProfile(base, override)
}

function replaceProfile(nextProfile: EnterpriseProfile) {
  Object.keys(profile).forEach((key) => {
    delete (profile as any)[key]
  })
  Object.assign(profile, nextProfile)
}

function buildOpsConfigPayload(source: EnterpriseProfile) {
  return {
    publishMeta: source.publishMeta,
    contact: source.contact,
    legal: source.legal,
    compliance: source.compliance,
    contentChecklist: source.contentChecklist,
    departmentDutyBoard: source.departmentDutyBoard,
    newsList: source.newsList,
    announcements: source.announcements
  }
}

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

const pageMaintainer = computed(() => {
  const realName = String(userStore.staffInfo?.realName || '').trim()
  if (realName) return realName
  const username = String(userStore.staffInfo?.username || '').trim()
  if (username) return username
  return profile.publishMeta.maintainer
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
  reloadProfileConfigDraft()
  document.title = `${profile.name} - 企业首页`
  const cachedReadingMode = localStorage.getItem('enterprise_home_reading_mode')
  if (cachedReadingMode === '1') readingMode.value = true
})

function reloadProfileConfigDraft() {
  profileConfigDraft.value = JSON.stringify(buildOpsConfigPayload(profile), null, 2)
}

function openProfileConfig() {
  reloadProfileConfigDraft()
  profileConfigOpen.value = true
}

function formatProfileConfigDraft() {
  try {
    profileConfigDraft.value = JSON.stringify(JSON.parse(profileConfigDraft.value || '{}'), null, 2)
  } catch (_error) {
    message.warning('JSON 格式不正确，无法格式化。')
  }
}

function saveProfileConfig() {
  profileConfigSaving.value = true
  try {
    const parsed = JSON.parse(profileConfigDraft.value || '{}')
    if (!isPlainObject(parsed)) {
      message.warning('配置内容必须是 JSON 对象。')
      return
    }
    const updater = pageMaintainer.value
    const publishMetaPatch = isPlainObject(parsed.publishMeta) ? parsed.publishMeta : {}
    publishMetaPatch.maintainer = updater
    parsed.publishMeta = publishMetaPatch
    const merged = mergeProfile(deepClone(enterpriseProfile), parsed) as EnterpriseProfile
    replaceProfile(merged)
    localStorage.setItem(PROFILE_OVERRIDE_STORAGE_KEY, JSON.stringify(parsed))
    profileConfigOpen.value = false
    message.success(`运营配置已保存并生效，维护人已更新为：${updater}`)
  } catch (_error) {
    message.error('配置保存失败，请检查 JSON 结构。')
  } finally {
    profileConfigSaving.value = false
  }
}

function exportProfileConfig() {
  const payload = buildOpsConfigPayload(profile)
  const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `enterprise-ops-config-${dayjs().format('YYYYMMDD-HHmmss')}.json`
  a.click()
  URL.revokeObjectURL(url)
}

function downloadProfileConfigTemplate() {
  const template = {
    publishMeta: {
      lastUpdated: '2026-03-08',
      maintainer: '品牌运营组',
      nextReviewDate: '2026-04-15'
    },
    contact: {
      phone: '0793-5899001',
      hotlineTip: '工作日 08:00-18:00',
      email: 'service@yiyang-guifeng-care.cn',
      address: '江西省上饶市弋阳县龟峰大道88号',
      visitingTime: '周一至周日 08:00-18:00'
    },
    contentChecklist: [
      { category: '机构简介', owner: '品牌运营组', status: '已完成', updatedAt: '2026-03-08' },
      { category: '资质证照', owner: '行政人事部', status: '待核验', updatedAt: '2026-03-08' }
    ],
    departmentDutyBoard: [
      { department: '护理部', serviceScope: '24小时护理响应', contact: '0793-5899002', dutyHours: '7×24' }
    ]
  }
  const blob = new Blob([JSON.stringify(template, null, 2)], { type: 'application/json;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'enterprise-ops-config-template.json'
  a.click()
  URL.revokeObjectURL(url)
}

function importProfileConfigFromFile() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'application/json,.json'
  input.onchange = () => {
    const file = input.files?.[0]
    if (!file) return
    const reader = new FileReader()
    reader.onload = () => {
      const text = String(reader.result || '')
      try {
        profileConfigDraft.value = JSON.stringify(JSON.parse(text), null, 2)
        message.success('配置文件已导入，请确认后保存。')
      } catch (_error) {
        message.error('导入失败：文件不是有效 JSON。')
      }
    }
    reader.readAsText(file, 'utf-8')
  }
  input.click()
}

function resetProfileConfig() {
  localStorage.removeItem(PROFILE_OVERRIDE_STORAGE_KEY)
  replaceProfile(deepClone(enterpriseProfile))
  reloadProfileConfigDraft()
  message.success('已恢复默认配置。')
}

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
  router.push('/admin')
}

function goAdmin() {
  router.push(hasToken.value ? '/portal' : '/admin')
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
    `维护人：${pageMaintainer.value}`,
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
    maintainer: pageMaintainer.value,
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
    `- 维护人：${pageMaintainer.value}`,
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
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap');

.enterprise-home {
  min-height: 100vh;
  color: #1a202c;
  background: #f0f4f8;
  background-image: 
    radial-gradient(at 0% 0%, hsla(210,100%,94%,1) 0, transparent 50%), 
    radial-gradient(at 50% 0%, hsla(220,100%,96%,1) 0, transparent 50%), 
    radial-gradient(at 100% 0%, hsla(230,100%,94%,1) 0, transparent 50%);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  letter-spacing: -0.01em;
}

.container {
  width: min(1200px, calc(100vw - 40px));
  margin: 0 auto;
}

.topbar {
  position: fixed;
  inset-inline: 0;
  top: 0;
  z-index: 50;
  background: rgba(13, 25, 52, 0.45);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.topbar:hover {
  background: rgba(13, 25, 52, 0.7);
}

.topbar-inner {
  height: 76px;
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 24px;
  align-items: center;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.brand:hover {
  transform: scale(1.02);
}

.brand-logo {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  color: #0b2e75;
  font-weight: 800;
  font-size: 20px;
  background: linear-gradient(135deg, #ffffff, #e2edff);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.brand-text .name {
  font-size: 18px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 0.5px;
}

.brand-text .desc {
  margin-top: 2px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.75);
}

.nav-list {
  display: flex;
  justify-content: center;
  gap: 20px;
  font-size: 14px;
  font-weight: 500;
}

.nav-list a {
  color: rgba(255, 255, 255, 0.85);
  text-decoration: none;
  cursor: pointer;
  padding: 6px 0;
  position: relative;
  transition: color 0.3s ease;
}

.nav-list a::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 0;
  height: 2px;
  background: #60a5fa;
  transition: width 0.3s ease;
  border-radius: 2px;
}

.nav-list a:hover {
  color: #ffffff;
}

.nav-list a:hover::after {
  width: 100%;
}

.top-actions {
  display: flex;
  gap: 12px;
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
  right: 24px;
  bottom: 30px;
  z-index: 40;
  display: grid;
  gap: 10px;
}

.hero {
  position: relative;
  min-height: 720px;
  padding: 180px 0 100px;
  background:
    linear-gradient(135deg, rgba(13, 33, 76, 0.88) 0%, rgba(25, 74, 168, 0.78) 50%, rgba(46, 110, 222, 0.72) 100%),
    url('../assets/home.jpg') center / cover no-repeat;
  display: flex;
  align-items: center;
}

.hero::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 120px;
  background: linear-gradient(to top, #f0f4f8, transparent);
  pointer-events: none;
}

.hero-mask {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 20% 30%, rgba(255, 255, 255, 0.15), transparent 50%);
  pointer-events: none;
}

.hero-content {
  position: relative;
  color: #fff;
  z-index: 10;
}

.hero-badge {
  display: inline-block;
  margin-bottom: 20px;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 1px;
  border-radius: 999px;
  padding: 6px 16px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  text-transform: uppercase;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.risk-badge {
  display: inline-block;
  margin-left: 10px;
  margin-bottom: 20px;
  font-size: 12px;
  font-weight: 600;
  border-radius: 999px;
  padding: 6px 14px;
  color: #9a3412;
  background: rgba(255, 237, 213, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 237, 213, 1);
  box-shadow: 0 4px 12px rgba(234, 88, 12, 0.15);
}

.hero h1 {
  margin: 0;
  font-size: clamp(40px, 6vw, 64px);
  font-weight: 800;
  line-height: 1.15;
  white-space: pre-line;
  text-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  background: linear-gradient(180deg, #ffffff 0%, #e2edff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero p {
  margin: 24px 0 0;
  max-width: 800px;
  font-size: 18px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.85);
  font-weight: 400;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.hero-actions {
  margin-top: 40px;
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.hero-actions .ant-btn {
  height: 48px;
  padding: 0 28px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 24px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.3s cubic-bezier(0.165, 0.84, 0.44, 1);
}

.hero-actions .ant-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
}

.section {
  padding: 80px 0;
  position: relative;
}

.section-alt {
  background: linear-gradient(180deg, rgba(234, 240, 255, 0.6) 0%, rgba(245, 248, 255, 0.2) 100%);
}

.section-title {
  text-align: center;
  margin-bottom: 48px;
}

.section-title h2 {
  margin: 0;
  font-size: 36px;
  font-weight: 800;
  color: #1a202c;
  letter-spacing: -0.5px;
}

.section-title p {
  margin: 12px auto 0;
  color: #4a5568;
  font-size: 16px;
  max-width: 600px;
  line-height: 1.6;
}

/* Glassmorphism Cards */
.stat-card,
.card,
.news-card,
.metric-box,
.flow-item,
.duty-item,
.contact-card,
.appointment-inner,
.faq-panel :deep(.ant-collapse-item) {
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 10px 30px rgba(31, 38, 135, 0.03);
  transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
  position: relative;
  overflow: hidden;
}

.stat-card:hover,
.card:hover,
.news-card:hover,
.metric-box:hover,
.flow-item:hover,
.contact-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 20px 40px rgba(31, 38, 135, 0.08);
  background: rgba(255, 255, 255, 0.9);
  border-color: rgba(255, 255, 255, 1);
}

/* decorative gradients inside cards */
.card::before, .stat-card::before, .news-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #3b82f6, #8b5cf6);
  opacity: 0;
  transition: opacity 0.4s ease;
}

.card:hover::before, .stat-card:hover::before, .news-card:hover::before {
  opacity: 1;
}

.stats {
  margin-top: -80px;
  position: relative;
  z-index: 20;
}

.stat-card {
  text-align: center;
  padding: 30px 20px;
}

.stat-value {
  font-size: 36px;
  font-weight: 800;
  background: linear-gradient(135deg, #1e3a8a, #3b82f6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.stat-label {
  margin-top: 10px;
  color: #4a5568;
  font-weight: 600;
  font-size: 15px;
}

.stat-tip {
  margin-top: 6px;
  color: #718096;
  font-size: 12px;
}

.freshness-tip {
  margin-top: 20px;
  padding: 14px 20px;
  border-radius: 12px;
  color: #1e3a8a;
  background: rgba(219, 234, 254, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.5);
  font-size: 14px;
  font-weight: 500;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
  display: inline-block;
}

.freshness-warning {
  color: #9a3412;
  background: rgba(255, 237, 213, 0.8);
  border: 1px solid rgba(255, 237, 213, 1);
}

.guide-section {
  padding-top: 60px;
}

.guide-bar {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  align-items: center;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.guide-bar .ant-input-affix-wrapper {
  border-radius: 20px;
  padding: 8px 16px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.02);
  border: 1px solid rgba(255,255,255,0.8);
  background: rgba(255,255,255,0.7);
  backdrop-filter: blur(10px);
}

.guide-bar .ant-btn {
  border-radius: 20px;
  height: 42px;
  padding: 0 24px;
}

.guide-result {
  margin-top: 16px;
  min-height: 28px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
}

.guide-tag {
  cursor: pointer;
  border-radius: 12px;
  padding: 4px 12px;
  font-size: 13px;
  border: none;
  background: rgba(59, 130, 246, 0.1);
  color: #2563eb;
  transition: all 0.2s;
}

.guide-tag:hover {
  background: #3b82f6;
  color: #fff;
}

.guide-empty {
  color: #718096;
  font-size: 13px;
}

.announcement-strip {
  margin-bottom: 24px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.9);
  box-shadow: 0 12px 30px rgba(31, 38, 135, 0.05);
  padding: 16px 20px;
}

.announcement-title {
  font-size: 14px;
  color: #1e3a8a;
  font-weight: 700;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.announcement-title::before {
  content: '📢';
}

.announcement-list {
  display: grid;
  gap: 8px;
}

.announcement-item {
  border-radius: 10px;
  padding: 10px 14px;
  font-size: 14px;
  display: flex;
  align-items: center;
  transition: background 0.2s;
}

.announcement-item:hover {
  filter: brightness(0.98);
}

.announcement-date {
  margin-right: 12px;
  color: #718096;
  font-size: 13px;
  font-weight: 500;
}

.announcement-info { background: rgba(239, 246, 255, 0.8); }
.announcement-success { background: rgba(240, 253, 244, 0.8); }
.announcement-warning { background: rgba(255, 251, 235, 0.8); }

.card h3,
.news-card h3 {
  margin: 0;
  color: #1a202c;
  font-size: 20px;
  font-weight: 700;
}

.card p,
.news-card p {
  margin: 12px 0 0;
  color: #4a5568;
  line-height: 1.7;
  font-size: 15px;
}

.news-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
}

.news-toolbar {
  margin: 0 auto 30px;
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
}

.news-toolbar .ant-input-affix-wrapper {
  border-radius: 20px;
}

.news-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  align-items: center;
}

.news-tag {
  background: rgba(59, 130, 246, 0.1);
  color: #2563eb;
  border-radius: 999px;
  padding: 4px 12px;
  font-size: 12px;
  font-weight: 600;
}

.news-date {
  color: #a0aec0;
  font-size: 13px;
  font-weight: 500;
}

.package-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.package-suitable {
  color: #2b6cb0;
  font-weight: 600;
  font-size: 14px;
  background: rgba(235, 248, 255, 0.6);
  padding: 6px 12px;
  border-radius: 8px;
  display: inline-block;
}

.package-list {
  margin: 16px 0 auto;
  padding-left: 20px;
  color: #4a5568;
  line-height: 1.8;
  flex-grow: 1;
}

.package-fee {
  margin-top: 20px;
  color: #2563eb;
  font-weight: 800;
  font-size: 20px;
  padding-top: 16px;
  border-top: 1px dashed rgba(0,0,0,0.1);
}

.matcher-card {
  padding: 24px;
}

.matcher-form {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
}

.matcher-form .ant-select-selector {
  border-radius: 12px !important;
}

.matcher-form .ant-btn {
  border-radius: 12px;
}

.activity-meta {
  margin-top: 16px;
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #718096;
  font-weight: 500;
  padding-top: 12px;
  border-top: 1px solid rgba(0,0,0,0.05);
}

.flow-list {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 16px;
}

.flow-item {
  padding: 24px 16px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.flow-index {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-weight: 800;
  color: #fff;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  font-size: 16px;
  box-shadow: 0 4px 10px rgba(37, 99, 235, 0.3);
  margin-bottom: 16px;
}

.flow-text {
  color: #2d3748;
  font-weight: 700;
  font-size: 15px;
}

.job-dept {
  margin-top: 16px;
  display: inline-block;
  color: #2b6cb0;
  background: rgba(235, 248, 255, 0.8);
  border-radius: 8px;
  padding: 6px 12px;
  font-size: 13px;
  font-weight: 600;
}

.compliance-list {
  margin: 12px 0 0;
  padding-left: 20px;
  color: #4a5568;
  line-height: 2;
}

.map-note {
  font-size: 13px;
  color: #718096;
  margin-top: 8px;
}

.section-actions {
  margin-top: 24px;
}

.governance-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
  margin: 0 0 24px;
}

.metric-box {
  padding: 24px;
}

.metric-label {
  color: #718096;
  font-size: 14px;
  font-weight: 600;
}

.metric-value {
  margin-top: 8px;
  font-size: 32px;
  color: #1e3a8a;
  font-weight: 800;
}

.metric-tip {
  margin-top: 4px;
  color: #a0aec0;
  font-size: 13px;
}

.ops-status {
  margin-top: 16px;
  padding: 12px 16px;
  border-radius: 12px;
  background: rgba(239, 246, 255, 0.8);
  color: #1e3a8a;
  font-size: 14px;
  font-weight: 500;
}

.duty-board {
  display: grid;
  gap: 12px;
}

.duty-item {
  padding: 16px;
  background: rgba(255,255,255,0.4);
}

.duty-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.duty-name {
  font-weight: 800;
  color: #1a202c;
  font-size: 15px;
}

.duty-hours {
  font-size: 12px;
  color: #718096;
  background: rgba(0,0,0,0.05);
  padding: 2px 8px;
  border-radius: 999px;
}

.duty-scope {
  margin-top: 8px;
  color: #4a5568;
  font-size: 14px;
  line-height: 1.5;
}

.duty-contact {
  margin-top: 10px;
  color: #2563eb;
  font-size: 14px;
  font-weight: 600;
}

.contact-card {
  padding: 40px;
}

.contact-card ul {
  list-style: none;
  margin: 24px 0;
  padding: 0;
}

.contact-card li {
  margin: 12px 0;
  color: #4a5568;
  font-size: 16px;
}

.contact-card li span {
  font-weight: 700;
  color: #2d3748;
  display: inline-block;
  width: 90px;
}

.appointment-strip {
  padding: 48px 0;
  background: linear-gradient(90deg, #1e3a8a, #3b82f6);
}

.appointment-inner {
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.appointment-inner h3 {
  color: #fff;
  font-size: 24px;
  margin: 0;
}

.appointment-inner p {
  color: rgba(255, 255, 255, 0.8);
  margin: 8px 0 0;
  font-size: 15px;
}

.faq-search {
  margin: 0 auto 30px;
  display: flex;
  justify-content: center;
}

.faq-search .ant-input-affix-wrapper {
  border-radius: 20px;
}

.faq-panel :deep(.ant-collapse-item) {
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
}

.faq-panel :deep(.ant-collapse-header) {
  padding: 16px 20px !important;
}

.faq-answer {
  margin: 0;
  color: #4a5568;
  line-height: 1.8;
  font-weight: 400;
  font-size: 15px;
}

.testimonial-content {
  margin: 0;
  color: #2d3748;
  line-height: 1.9;
  font-size: 15px;
  font-style: italic;
}

.testimonial-meta {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
  color: #718096;
  font-size: 13px;
  font-weight: 600;
  padding-top: 16px;
  border-top: 1px solid rgba(0,0,0,0.05);
}

.footer {
  margin-top: 60px;
  padding: 40px 0;
  background: #0f172a;
  color: rgba(255, 255, 255, 0.6);
}

.footer-inner {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
  font-size: 14px;
}

.footer-inner span {
  position: relative;
}

.footer-inner span:not(:last-child)::after {
  content: '·';
  position: absolute;
  right: -12px;
  color: rgba(255,255,255,0.3);
}

.reading-mode {
  font-size: 18px;
}

.reading-mode .card p,
.reading-mode .news-card p,
.reading-mode .faq-answer {
  font-size: 17px;
  line-height: 2;
}

.reading-mode .testimonial-content {
  font-size: 17px;
  line-height: 2;
}

@media (max-width: 1080px) {
  .topbar-inner {
    grid-template-columns: 1fr auto;
    grid-template-areas:
      'brand actions'
      'nav nav';
    height: auto;
    padding: 12px 0;
  }

  .mobile-menu-btn {
    display: inline-flex;
    grid-area: actions;
    justify-self: end;
    margin-left: 12px;
  }

  .nav-list {
    display: none;
  }

  .hero {
    padding-top: 200px;
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
    width: calc(100vw - 32px);
  }

  .floating-actions {
    right: 16px;
    bottom: 24px;
  }

  .flow-list {
    grid-template-columns: 1fr 1fr;
  }

  .appointment-inner {
    flex-direction: column;
    align-items: flex-start;
    padding: 24px;
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
