<template>
  <PageContainer title="官网配置中心" subTitle="按栏目维护官网内容，不再使用 JSON 配置">
    <a-alert
      type="info"
      show-icon
      style="margin-bottom: 12px"
      message="运营方式已调整为分栏目维护"
      description="机构介绍、机构动态、生活娱乐、资质公示、联系方式、招聘合作分别单独维护。点击上方栏目按钮进入对应子页面，直接按表单填写即可。"
    />

    <a-row :gutter="[12, 12]" style="margin-bottom: 12px">
      <a-col :xs="24" :md="6">
        <a-card size="small">
          <div class="summary-label">当前栏目</div>
          <div class="summary-value">{{ activeSectionLabel }}</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="6">
        <a-card size="small">
          <div class="summary-label">最近更新</div>
          <div class="summary-value">{{ profile.publishMeta?.lastUpdated || '-' }}</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="6">
        <a-card size="small">
          <div class="summary-label">维护人</div>
          <div class="summary-value">{{ pageMaintainer }}</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :md="6">
        <a-card size="small">
          <div class="summary-label">预约留言数</div>
          <div class="summary-value">{{ consultRecords.length }}</div>
        </a-card>
      </a-col>
    </a-row>

    <a-card :bordered="false" class="card-elevated">
      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="profile" tab="官网内容配置">
          <div class="section-nav">
            <a-button
              v-for="item in sectionOptions"
              :key="item.key"
              :type="activeSection === item.key ? 'primary' : 'default'"
              @click="activeSection = item.key"
            >
              {{ item.label }}
            </a-button>
          </div>

          <a-card size="small" class="section-card">
            <template #title>
              <div class="section-card-title">
                <span>{{ activeSectionLabel }}</span>
                <a-tag color="blue">{{ activeSectionDescription }}</a-tag>
              </div>
            </template>

            <template v-if="activeSection === 'intro'">
              <a-form layout="vertical">
                <a-row :gutter="[12, 0]">
                  <a-col :xs="24" :md="12">
                    <a-form-item label="机构全称">
                      <a-input v-model:value="profile.name" />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="12">
                    <a-form-item label="机构简称">
                      <a-input v-model:value="profile.shortName" />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="12">
                    <a-form-item label="宣传口号">
                      <a-input v-model:value="profile.slogan" />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="12">
                    <a-form-item label="副标题">
                      <a-input v-model:value="profile.subtitle" />
                    </a-form-item>
                  </a-col>
                </a-row>

                <a-form-item label="首页主标题">
                  <a-textarea v-model:value="profile.heroTitle" :rows="2" />
                </a-form-item>
                <a-form-item label="首页主描述">
                  <a-textarea v-model:value="profile.heroDesc" :rows="3" />
                </a-form-item>
                <a-row :gutter="[12, 0]">
                  <a-col :xs="24" :md="12">
                    <a-form-item label="机构使命">
                      <a-textarea v-model:value="profile.mission" :rows="3" />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="12">
                    <a-form-item label="机构愿景">
                      <a-textarea v-model:value="profile.vision" :rows="3" />
                    </a-form-item>
                  </a-col>
                </a-row>

                <div class="editor-block">
                  <div class="editor-head">
                    <div>
                      <div class="editor-title">机构介绍模块</div>
                      <div class="editor-tip">用于官网介绍页、关于我们和服务体系展示。</div>
                    </div>
                    <a-button size="small" @click="addAboutUs">新增介绍卡片</a-button>
                  </div>
                  <div v-for="(item, index) in profile.aboutUs" :key="`about-${index}`" class="editable-item">
                    <a-row :gutter="[12, 0]">
                      <a-col :xs="24" :md="8">
                        <a-form-item label="标题">
                          <a-input v-model:value="item.title" />
                        </a-form-item>
                      </a-col>
                      <a-col :xs="24" :md="14">
                        <a-form-item label="内容">
                          <a-textarea v-model:value="item.desc" :rows="2" />
                        </a-form-item>
                      </a-col>
                      <a-col :xs="24" :md="2" class="item-action-col">
                        <a-button danger type="link" @click="removeListItem(profile.aboutUs, index)">删除</a-button>
                      </a-col>
                    </a-row>
                  </div>
                </div>

                <div class="editor-block">
                  <div class="editor-head">
                    <div>
                      <div class="editor-title">服务体系</div>
                      <div class="editor-tip">展示医养服务、照护能力和特色服务。</div>
                    </div>
                    <a-button size="small" @click="addServiceSystem">新增服务项</a-button>
                  </div>
                  <div v-for="(item, index) in profile.serviceSystem" :key="`service-${index}`" class="editable-item">
                    <a-row :gutter="[12, 0]">
                      <a-col :xs="24" :md="8">
                        <a-form-item label="服务名称">
                          <a-input v-model:value="item.title" />
                        </a-form-item>
                      </a-col>
                      <a-col :xs="24" :md="14">
                        <a-form-item label="服务说明">
                          <a-textarea v-model:value="item.desc" :rows="2" />
                        </a-form-item>
                      </a-col>
                      <a-col :xs="24" :md="2" class="item-action-col">
                        <a-button danger type="link" @click="removeListItem(profile.serviceSystem, index)">删除</a-button>
                      </a-col>
                    </a-row>
                  </div>
                </div>
              </a-form>
            </template>

            <template v-else-if="activeSection === 'news'">
              <div class="editor-block">
                <div class="editor-head">
                  <div>
                    <div class="editor-title">机构动态快讯</div>
                    <div class="editor-tip">用于官网首页和机构动态页的醒目内容。</div>
                  </div>
                  <a-button size="small" @click="addCommunityUpdate">新增动态</a-button>
                </div>
                <div v-for="(item, index) in profile.communityUpdates" :key="`update-${index}`" class="editable-item">
                  <a-row :gutter="[12, 0]">
                    <a-col :xs="24" :md="8">
                      <a-form-item label="标题">
                        <a-input v-model:value="item.title" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="14">
                      <a-form-item label="简介">
                        <a-textarea v-model:value="item.desc" :rows="2" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="2" class="item-action-col">
                      <a-button danger type="link" @click="removeListItem(profile.communityUpdates, index)">删除</a-button>
                    </a-col>
                  </a-row>
                </div>
              </div>

              <div class="editor-block">
                <div class="editor-head">
                  <div>
                    <div class="editor-title">新闻资讯</div>
                    <div class="editor-tip">适合发布机构新闻、政策通知和阶段成果。</div>
                  </div>
                  <a-button size="small" @click="addNewsItem">新增新闻</a-button>
                </div>
                <div v-for="(item, index) in profile.newsList" :key="`news-${index}`" class="editable-item">
                  <a-row :gutter="[12, 0]">
                    <a-col :xs="24" :md="7">
                      <a-form-item label="标题">
                        <a-input v-model:value="item.title" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="5">
                      <a-form-item label="日期">
                        <a-date-picker v-model:value="item.date" value-format="YYYY-MM-DD" style="width: 100%" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="4">
                      <a-form-item label="标签">
                        <a-input v-model:value="item.tag" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="6">
                      <a-form-item label="简介">
                        <a-textarea v-model:value="item.desc" :rows="2" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="2" class="item-action-col">
                      <a-button danger type="link" @click="removeListItem(profile.newsList, index)">删除</a-button>
                    </a-col>
                  </a-row>
                </div>
              </div>

              <div class="editor-block">
                <div class="editor-head">
                  <div>
                    <div class="editor-title">公告栏</div>
                    <div class="editor-tip">适合公示提醒、节假日通知和重要活动公告。</div>
                  </div>
                  <a-button size="small" @click="addAnnouncement">新增公告</a-button>
                </div>
                <div v-for="(item, index) in profile.announcements" :key="`announce-${index}`" class="editable-item">
                  <a-row :gutter="[12, 0]">
                    <a-col :xs="24" :md="8">
                      <a-form-item label="公告标题">
                        <a-input v-model:value="item.title" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="6">
                      <a-form-item label="日期">
                        <a-date-picker v-model:value="item.date" value-format="YYYY-MM-DD" style="width: 100%" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="6">
                      <a-form-item label="级别">
                        <a-select v-model:value="item.level" :options="announcementLevelOptions" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="4" class="item-action-col">
                      <a-button danger type="link" @click="removeListItem(profile.announcements, index)">删除</a-button>
                    </a-col>
                  </a-row>
                </div>
              </div>
            </template>

            <template v-else-if="activeSection === 'activities'">
              <div class="editor-block">
                <div class="editor-head">
                  <div>
                    <div class="editor-title">生活娱乐活动</div>
                    <div class="editor-tip">维护长者活动安排、兴趣课程和生活亮点。</div>
                  </div>
                  <a-button size="small" @click="addResidentActivity">新增活动</a-button>
                </div>
                <div v-for="(item, index) in profile.residentActivities" :key="`activity-${index}`" class="editable-item">
                  <a-row :gutter="[12, 0]">
                    <a-col :xs="24" :md="6">
                      <a-form-item label="活动名称">
                        <a-input v-model:value="item.title" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="5">
                      <a-form-item label="活动时间">
                        <a-input v-model:value="item.schedule" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="5">
                      <a-form-item label="地点">
                        <a-input v-model:value="item.location" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="6">
                      <a-form-item label="简介">
                        <a-textarea v-model:value="item.desc" :rows="2" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="2" class="item-action-col">
                      <a-button danger type="link" @click="removeListItem(profile.residentActivities, index)">删除</a-button>
                    </a-col>
                  </a-row>
                </div>
              </div>

              <div class="editor-block">
                <div class="editor-head">
                  <div>
                    <div class="editor-title">生活亮点板块</div>
                    <div class="editor-tip">适合展示社区特色、文娱看点和居民风采。</div>
                  </div>
                  <a-button size="small" @click="addResidentHighlight">新增亮点</a-button>
                </div>
                <div v-for="(item, index) in profile.residentHighlights" :key="`highlight-${index}`" class="editable-item">
                  <a-row :gutter="[12, 0]">
                    <a-col :xs="24" :md="8">
                      <a-form-item label="标题">
                        <a-input v-model:value="item.title" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="14">
                      <a-form-item label="内容">
                        <a-textarea v-model:value="item.desc" :rows="2" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="2" class="item-action-col">
                      <a-button danger type="link" @click="removeListItem(profile.residentHighlights, index)">删除</a-button>
                    </a-col>
                  </a-row>
                </div>
              </div>

              <div class="editor-block">
                <div class="editor-head">
                  <div>
                    <div class="editor-title">家属评价</div>
                    <div class="editor-tip">官网可信度展示，建议保留真实日期和关系描述。</div>
                  </div>
                  <a-button size="small" @click="addTestimonial">新增评价</a-button>
                </div>
                <div v-for="(item, index) in profile.testimonials" :key="`test-${index}`" class="editable-item">
                  <a-row :gutter="[12, 0]">
                    <a-col :xs="24" :md="5">
                      <a-form-item label="姓名">
                        <a-input v-model:value="item.name" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="5">
                      <a-form-item label="关系">
                        <a-input v-model:value="item.relation" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="4">
                      <a-form-item label="日期">
                        <a-date-picker v-model:value="item.date" value-format="YYYY-MM-DD" style="width: 100%" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="8">
                      <a-form-item label="评价内容">
                        <a-textarea v-model:value="item.content" :rows="2" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="2" class="item-action-col">
                      <a-button danger type="link" @click="removeListItem(profile.testimonials, index)">删除</a-button>
                    </a-col>
                  </a-row>
                </div>
              </div>
            </template>

            <template v-else-if="activeSection === 'contact'">
              <a-form layout="vertical">
                <a-row :gutter="[12, 0]">
                  <a-col :xs="24" :md="8">
                    <a-form-item label="联系电话">
                      <a-input v-model:value="profile.contact.phone" />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="8">
                    <a-form-item label="客服邮箱">
                      <a-input v-model:value="profile.contact.email" />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="8">
                    <a-form-item label="来访时间">
                      <a-input v-model:value="profile.contact.visitingTime" />
                    </a-form-item>
                  </a-col>
                </a-row>
                <a-form-item label="电话提示">
                  <a-input v-model:value="profile.contact.hotlineTip" />
                </a-form-item>
                <a-form-item label="机构地址">
                  <a-input v-model:value="profile.contact.address" />
                </a-form-item>
                <a-row :gutter="[12, 0]">
                  <a-col :xs="24" :md="8">
                    <a-form-item label="VR看社区链接">
                      <a-input v-model:value="profile.vrCommunityUrl" />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="8">
                    <a-form-item label="加入我们链接">
                      <a-input v-model:value="profile.joinUsUrl" />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="8">
                    <a-form-item label="下次复核日期">
                      <a-date-picker v-model:value="profile.publishMeta.nextReviewDate" value-format="YYYY-MM-DD" style="width: 100%" />
                    </a-form-item>
                  </a-col>
                </a-row>
                <a-row :gutter="[12, 0]">
                  <a-col :xs="24" :md="12">
                    <a-form-item label="ICP备案号">
                      <a-input v-model:value="profile.legal.icp" />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="12">
                    <a-form-item label="公安备案号">
                      <a-input v-model:value="profile.legal.publicSecurityRecord" />
                    </a-form-item>
                  </a-col>
                </a-row>
                <a-form-item label="机构公示信息">
                  <a-textarea v-model:value="organizationInfoDraft" :rows="5" placeholder="每行一条，例如：成立时间 / 床位数 / 服务对象" />
                  <div class="editor-tip">每行一条，保存时会自动拆分成官网展示项。</div>
                </a-form-item>
              </a-form>
            </template>

            <template v-else-if="activeSection === 'compliance'">
              <a-form layout="vertical">
                <a-row :gutter="[12, 0]">
                  <a-col :xs="24" :md="4">
                    <a-form-item label="服务半径">
                      <a-input v-model:value="profile.compliance.map.serviceRadius" />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="8">
                    <a-form-item label="导航链接">
                      <a-input v-model:value="profile.compliance.map.navigationUrl" />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="12">
                    <a-form-item label="地图说明">
                      <a-input v-model:value="profile.compliance.map.note" />
                    </a-form-item>
                  </a-col>
                </a-row>
                <a-row :gutter="[12, 0]">
                  <a-col :xs="24" :md="6">
                    <a-form-item label="图片来源">
                      <a-input v-model:value="profile.compliance.mediaRights.photoSource" />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="6">
                    <a-form-item label="版权归属">
                      <a-input v-model:value="profile.compliance.mediaRights.copyrightOwner" />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="6">
                    <a-form-item label="授权范围">
                      <a-input v-model:value="profile.compliance.mediaRights.authorizedScope" />
                    </a-form-item>
                  </a-col>
                  <a-col :xs="24" :md="6">
                    <a-form-item label="核验日期">
                      <a-date-picker v-model:value="profile.compliance.mediaRights.lastVerifiedAt" value-format="YYYY-MM-DD" style="width: 100%" />
                    </a-form-item>
                  </a-col>
                </a-row>
                <a-form-item label="版权联系人">
                  <a-input v-model:value="profile.compliance.mediaRights.contact" />
                </a-form-item>
              </a-form>

              <div class="editor-block">
                <div class="editor-head">
                  <div>
                    <div class="editor-title">资质公示</div>
                    <div class="editor-tip">建议用于医疗合作、养老资质、消防验收等公示信息。</div>
                  </div>
                  <a-button size="small" @click="addQualification">新增资质</a-button>
                </div>
                <div v-for="(item, index) in profile.compliance.qualifications" :key="`qualification-${index}`" class="editable-item">
                  <a-row :gutter="[12, 0]">
                    <a-col :xs="24" :md="4">
                      <a-form-item label="资质名称">
                        <a-input v-model:value="item.name" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="4">
                      <a-form-item label="证照编号">
                        <a-input v-model:value="item.no" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="4">
                      <a-form-item label="发证单位">
                        <a-input v-model:value="item.issuer" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="4">
                      <a-form-item label="有效期">
                        <a-date-picker v-model:value="item.validUntil" value-format="YYYY-MM-DD" style="width: 100%" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="6">
                      <a-form-item label="公示链接">
                        <a-input v-model:value="item.publicUrl" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="2" class="item-action-col">
                      <a-button danger type="link" @click="removeListItem(profile.compliance.qualifications, index)">删除</a-button>
                    </a-col>
                  </a-row>
                </div>
              </div>
            </template>

            <template v-else-if="activeSection === 'jobs'">
              <div class="editor-block">
                <div class="editor-head">
                  <div>
                    <div class="editor-title">招聘岗位</div>
                    <div class="editor-tip">用于企业官网“加入我们”栏目。</div>
                  </div>
                  <a-button size="small" @click="addJob">新增岗位</a-button>
                </div>
                <div v-for="(item, index) in profile.jobs" :key="`job-${index}`" class="editable-item">
                  <a-row :gutter="[12, 0]">
                    <a-col :xs="24" :md="5">
                      <a-form-item label="岗位名称">
                        <a-input v-model:value="item.title" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="5">
                      <a-form-item label="所属部门">
                        <a-input v-model:value="item.department" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="12">
                      <a-form-item label="任职要求">
                        <a-textarea v-model:value="item.requirement" :rows="2" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="2" class="item-action-col">
                      <a-button danger type="link" @click="removeListItem(profile.jobs, index)">删除</a-button>
                    </a-col>
                  </a-row>
                </div>
              </div>

              <div class="editor-block">
                <div class="editor-head">
                  <div>
                    <div class="editor-title">合作说明</div>
                    <div class="editor-tip">可用于校企合作、医疗合作、志愿者合作等栏目。</div>
                  </div>
                  <a-button size="small" @click="addCooperation">新增合作项</a-button>
                </div>
                <div v-for="(item, index) in profile.cooperation" :key="`cooperation-${index}`" class="editable-item">
                  <a-row :gutter="[12, 0]">
                    <a-col :xs="24" :md="8">
                      <a-form-item label="合作标题">
                        <a-input v-model:value="item.title" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="14">
                      <a-form-item label="合作说明">
                        <a-textarea v-model:value="item.desc" :rows="2" />
                      </a-form-item>
                    </a-col>
                    <a-col :xs="24" :md="2" class="item-action-col">
                      <a-button danger type="link" @click="removeListItem(profile.cooperation, index)">删除</a-button>
                    </a-col>
                  </a-row>
                </div>
              </div>
            </template>

            <div class="section-actions">
              <a-button @click="reloadProfile">重新载入当前配置</a-button>
              <a-button danger @click="resetProfileConfig">恢复默认内容</a-button>
              <a-button type="primary" :loading="profileConfigSaving" @click="saveProfileConfig">保存并生效</a-button>
            </div>
          </a-card>
        </a-tab-pane>

        <a-tab-pane key="consult" tab="预约留言记录">
          <div class="consult-actions">
            <a-tag color="blue">总记录 {{ consultRecords.length }}</a-tag>
            <a-tag color="green">今日新增 {{ consultTodayCount }}</a-tag>
            <a-tag color="purple">热门部门 {{ consultTopDepartment }}</a-tag>
            <a-button size="small" @click="exportConsultRecordsCsv">导出CSV</a-button>
            <a-button size="small" danger @click="clearConsultRecords">清空全部</a-button>
          </div>
          <a-table :data-source="consultRecords" :pagination="{ pageSize: 8 }" row-key="createdAt" size="small">
            <a-table-column title="提交时间" data-index="createdAt" key="createdAt" width="170" />
            <a-table-column title="联系人" data-index="name" key="name" width="120" />
            <a-table-column title="联系电话" data-index="phone" key="phone" width="140" />
            <a-table-column title="对接部门" data-index="department" key="department" width="140" />
            <a-table-column title="意向日期" data-index="visitDate" key="visitDate" width="130" />
            <a-table-column title="需求" data-index="need" key="need" />
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { enterpriseProfile, type EnterpriseProfile } from '../../constants/enterpriseProfile'
import { useUserStore } from '../../stores/user'

const PROFILE_OVERRIDE_STORAGE_KEY = 'enterprise_profile_override_v1'
const CONSULT_RECORD_STORAGE_KEY = 'enterprise_consult_records'

type SectionKey = 'intro' | 'news' | 'activities' | 'contact' | 'compliance' | 'jobs'

const sectionOptions: Array<{ key: SectionKey; label: string; description: string }> = [
  { key: 'intro', label: '机构介绍', description: '维护首页品牌介绍、机构使命、服务体系和关于我们。' },
  { key: 'news', label: '机构动态', description: '维护新闻资讯、动态快讯和公告栏。' },
  { key: 'activities', label: '生活娱乐', description: '维护长者活动、生活亮点和家属评价。' },
  { key: 'contact', label: '联系方式', description: '维护电话、地址、来访时间、VR看社区等基础信息。' },
  { key: 'compliance', label: '资质公示', description: '维护机构资质、地图信息和图片版权说明。' },
  { key: 'jobs', label: '招聘合作', description: '维护招聘岗位和合作说明。' }
]

const announcementLevelOptions = [
  { label: '一般', value: 'info' },
  { label: '重要', value: 'warning' },
  { label: '喜讯', value: 'success' }
]

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('profile')
const activeSection = ref<SectionKey>('intro')
const profileConfigSaving = ref(false)
const profile = reactive<EnterpriseProfile>(buildRuntimeProfile())
const consultVersion = ref(0)
const organizationInfoDraft = ref('')

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
  } catch {
    return {}
  }
}

function buildRuntimeProfile(): EnterpriseProfile {
  return mergeProfile(deepClone(enterpriseProfile), loadProfileOverride())
}

function replaceProfile(nextProfile: EnterpriseProfile) {
  Object.keys(profile).forEach((key) => {
    delete (profile as Record<string, unknown>)[key]
  })
  Object.assign(profile, nextProfile)
  syncOrganizationInfoDraft()
}

const activeSectionMeta = computed(() => sectionOptions.find((item) => item.key === activeSection.value) || sectionOptions[0])
const activeSectionLabel = computed(() => activeSectionMeta.value.label)
const activeSectionDescription = computed(() => activeSectionMeta.value.description)

const pageMaintainer = computed(() => {
  const realName = String(userStore.staffInfo?.realName || '').trim()
  if (realName) return realName
  const username = String(userStore.staffInfo?.username || '').trim()
  if (username) return username
  return String(profile.publishMeta?.maintainer || '运营管理员')
})

const consultRecords = computed(() => {
  consultVersion.value
  const raw = localStorage.getItem(CONSULT_RECORD_STORAGE_KEY)
  if (!raw) return []
  try {
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
})

const consultTodayCount = computed(() => {
  const today = dayjs().format('YYYY-MM-DD')
  return consultRecords.value.filter((item) => String(item.createdAt || '').startsWith(today)).length
})

const consultTopDepartment = computed(() => {
  const counter = new Map<string, number>()
  consultRecords.value.forEach((item) => {
    const key = String(item.department || '未指定')
    counter.set(key, (counter.get(key) || 0) + 1)
  })
  const top = Array.from(counter.entries()).sort((a, b) => b[1] - a[1])[0]
  return top ? `${top[0]}（${top[1]}）` : '暂无'
})

function syncOrganizationInfoDraft() {
  organizationInfoDraft.value = Array.isArray(profile.compliance?.organizationInfo)
    ? profile.compliance.organizationInfo.join('\n')
    : ''
}

function normalizeDraftFields() {
  profile.publishMeta.maintainer = pageMaintainer.value
  profile.publishMeta.lastUpdated = dayjs().format('YYYY-MM-DD HH:mm')
  profile.compliance.organizationInfo = organizationInfoDraft.value
    .split('\n')
    .map((item) => item.trim())
    .filter(Boolean)
}

function reloadProfile() {
  replaceProfile(buildRuntimeProfile())
  message.success('已重新载入当前官网配置')
}

function saveProfileConfig() {
  profileConfigSaving.value = true
  try {
    normalizeDraftFields()
    localStorage.setItem(PROFILE_OVERRIDE_STORAGE_KEY, JSON.stringify(deepClone(profile)))
    replaceProfile(buildRuntimeProfile())
    message.success(`${activeSectionLabel.value}已保存并生效`)
  } catch {
    message.error('保存失败，请稍后重试')
  } finally {
    profileConfigSaving.value = false
  }
}

function resetProfileConfig() {
  localStorage.removeItem(PROFILE_OVERRIDE_STORAGE_KEY)
  replaceProfile(deepClone(enterpriseProfile))
  message.success('已恢复默认官网配置')
}

function addAboutUs() {
  profile.aboutUs.push({ title: '新介绍模块', desc: '请填写内容简介' })
}

function addServiceSystem() {
  profile.serviceSystem.push({ title: '新服务项', desc: '请填写服务说明' })
}

function addCommunityUpdate() {
  profile.communityUpdates.push({ title: '新动态', desc: '请填写动态简介' })
}

function addNewsItem() {
  profile.newsList.push({ title: '新新闻', date: dayjs().format('YYYY-MM-DD'), tag: '机构动态', desc: '请填写新闻简介' })
}

function addAnnouncement() {
  profile.announcements.push({ title: '新公告', date: dayjs().format('YYYY-MM-DD'), level: 'info' })
}

function addResidentActivity() {
  profile.residentActivities.push({
    title: '新活动',
    schedule: '每周三 15:00',
    location: '多功能活动室',
    desc: '请填写活动说明'
  })
}

function addResidentHighlight() {
  profile.residentHighlights.push({ title: '新亮点', desc: '请填写内容简介' })
}

function addTestimonial() {
  profile.testimonials.push({
    name: '家属姓名',
    relation: '家属',
    content: '请填写评价内容',
    date: dayjs().format('YYYY-MM-DD')
  })
}

function addQualification() {
  profile.compliance.qualifications.push({
    name: '新资质',
    no: '',
    issuer: '',
    validUntil: dayjs().format('YYYY-MM-DD'),
    publicUrl: ''
  })
}

function addJob() {
  profile.jobs.push({ title: '新岗位', department: '所属部门', requirement: '请填写岗位要求' })
}

function addCooperation() {
  profile.cooperation.push({ title: '新合作方向', desc: '请填写合作说明' })
}

function removeListItem(list: unknown[], index: number) {
  list.splice(index, 1)
}

function exportConsultRecordsCsv() {
  const header = ['提交时间', '联系人', '联系电话', '对接部门', '意向日期', '需求']
  const rows = consultRecords.value.map((item) => [
    item.createdAt || '',
    item.name || '',
    item.phone || '',
    item.department || '',
    item.visitDate || '',
    item.need || ''
  ])
  const csv = [header, ...rows]
    .map((cols) => cols.map((cell) => `"${String(cell || '').replace(/"/g, '""')}"`).join(','))
    .join('\n')
  const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `enterprise-consult-records-${dayjs().format('YYYYMMDD-HHmmss')}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

function clearConsultRecords() {
  localStorage.removeItem(CONSULT_RECORD_STORAGE_KEY)
  consultVersion.value += 1
  message.success('预约留言记录已清空')
}

function syncTabFromRoute() {
  const tab = String(route.query.tab || 'profile')
  activeTab.value = tab === 'consult' ? 'consult' : 'profile'
}

function syncSectionFromRoute() {
  const section = String(route.query.section || 'intro') as SectionKey
  activeSection.value = sectionOptions.some((item) => item.key === section) ? section : 'intro'
}

watch(activeTab, (value) => {
  if (String(route.query.tab || 'profile') === value) return
  router.replace({ query: { ...route.query, tab: value } })
})

watch(activeSection, (value) => {
  if (String(route.query.section || 'intro') === value) return
  router.replace({ query: { ...route.query, section: value } })
})

watch(() => route.query.tab, syncTabFromRoute)
watch(() => route.query.section, syncSectionFromRoute)

onMounted(() => {
  syncTabFromRoute()
  syncSectionFromRoute()
  syncOrganizationInfoDraft()
})
</script>

<style scoped>
.summary-label {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}

.summary-value {
  margin-top: 6px;
  font-size: 20px;
  font-weight: 600;
}

.section-nav {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 14px;
}

.section-card {
  border-radius: 18px;
  background:
    linear-gradient(135deg, rgba(244, 249, 255, 0.92), rgba(255, 255, 255, 0.98)),
    radial-gradient(circle at top right, rgba(35, 110, 199, 0.12), transparent 42%);
}

.section-card-title {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.section-actions,
.consult-actions {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.editor-block {
  margin-top: 18px;
  padding: 16px;
  border: 1px solid rgba(18, 52, 88, 0.08);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.78);
}

.editor-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.editor-title {
  font-size: 16px;
  font-weight: 600;
  color: #183b5b;
}

.editor-tip {
  margin-top: 4px;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}

.editable-item {
  padding: 12px 12px 2px;
  border: 1px dashed rgba(38, 88, 140, 0.18);
  border-radius: 14px;
  background: rgba(247, 250, 255, 0.85);
}

.editable-item + .editable-item {
  margin-top: 12px;
}

.item-action-col {
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .item-action-col {
    justify-content: flex-start;
    margin-top: -8px;
    margin-bottom: 8px;
  }
}
</style>
