package com.zhiyangyun.care.family.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.auth.entity.Org;
import com.zhiyangyun.care.auth.mapper.OrgMapper;
import com.zhiyangyun.care.bill.entity.BillMonthly;
import com.zhiyangyun.care.bill.model.BillDetailResponse;
import com.zhiyangyun.care.bill.model.BillItemDetail;
import com.zhiyangyun.care.bill.service.BillService;
import com.zhiyangyun.care.bill.mapper.BillMonthlyMapper;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.entity.ElderFamily;
import com.zhiyangyun.care.elder.entity.ElderProfile;
import com.zhiyangyun.care.elder.entity.FamilyUser;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderMedicalOutingRecord;
import com.zhiyangyun.care.elder.entity.lifecycle.ElderOutingRecord;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.ElderFamilyMapper;
import com.zhiyangyun.care.elder.mapper.ElderMapper;
import com.zhiyangyun.care.elder.mapper.FamilyUserMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderMedicalOutingRecordMapper;
import com.zhiyangyun.care.elder.mapper.lifecycle.ElderOutingRecordMapper;
import com.zhiyangyun.care.family.config.FamilyPortalProperties;
import com.zhiyangyun.care.family.entity.FamilyPortalState;
import com.zhiyangyun.care.family.entity.FamilyRechargeOrder;
import com.zhiyangyun.care.family.mapper.FamilyPortalStateMapper;
import com.zhiyangyun.care.family.mapper.FamilyRechargeOrderMapper;
import com.zhiyangyun.care.family.model.FamilyNotifyCommand;
import com.zhiyangyun.care.family.model.FamilyPortalModels;
import com.zhiyangyun.care.family.service.FamilyPortalService;
import com.zhiyangyun.care.family.service.FamilyWechatNotifyService;
import com.zhiyangyun.care.finance.entity.ElderAccount;
import com.zhiyangyun.care.finance.entity.ElderAccountLog;
import com.zhiyangyun.care.finance.mapper.ElderAccountLogMapper;
import com.zhiyangyun.care.finance.mapper.ElderAccountMapper;
import com.zhiyangyun.care.health.entity.HealthDataRecord;
import com.zhiyangyun.care.health.entity.HealthNursingLog;
import com.zhiyangyun.care.health.mapper.HealthDataRecordMapper;
import com.zhiyangyun.care.health.mapper.HealthNursingLogMapper;
import com.zhiyangyun.care.life.entity.MealPlan;
import com.zhiyangyun.care.life.mapper.MealPlanMapper;
import com.zhiyangyun.care.model.CareTaskTodayItem;
import com.zhiyangyun.care.nursing.entity.ServiceBooking;
import com.zhiyangyun.care.nursing.mapper.ServiceBookingMapper;
import com.zhiyangyun.care.oa.entity.OaAlbum;
import com.zhiyangyun.care.oa.entity.OaNotice;
import com.zhiyangyun.care.oa.entity.OaSuggestion;
import com.zhiyangyun.care.oa.entity.OaTodo;
import com.zhiyangyun.care.oa.mapper.OaAlbumMapper;
import com.zhiyangyun.care.oa.mapper.OaNoticeMapper;
import com.zhiyangyun.care.oa.mapper.OaSuggestionMapper;
import com.zhiyangyun.care.oa.mapper.OaTodoMapper;
import com.zhiyangyun.care.assessment.entity.AssessmentRecord;
import com.zhiyangyun.care.assessment.mapper.AssessmentRecordMapper;
import com.zhiyangyun.care.standard.entity.ServiceItem;
import com.zhiyangyun.care.standard.mapper.ServiceItemMapper;
import com.zhiyangyun.care.store.entity.InventoryBatch;
import com.zhiyangyun.care.store.entity.OrderItem;
import com.zhiyangyun.care.store.entity.Product;
import com.zhiyangyun.care.store.entity.StoreOrder;
import com.zhiyangyun.care.store.mapper.InventoryBatchMapper;
import com.zhiyangyun.care.store.mapper.OrderItemMapper;
import com.zhiyangyun.care.store.mapper.ProductMapper;
import com.zhiyangyun.care.store.mapper.StoreOrderMapper;
import com.zhiyangyun.care.store.model.ForbiddenReason;
import com.zhiyangyun.care.store.model.OrderCancelRequest;
import com.zhiyangyun.care.store.model.OrderDetailResponse;
import com.zhiyangyun.care.store.model.OrderPreviewRequest;
import com.zhiyangyun.care.store.model.OrderPreviewResponse;
import com.zhiyangyun.care.store.model.OrderRefundRequest;
import com.zhiyangyun.care.store.model.OrderSubmitResponse;
import com.zhiyangyun.care.store.service.StoreOrderService;
import com.zhiyangyun.care.service.CareTaskService;
import com.zhiyangyun.care.visit.model.VisitBookRequest;
import com.zhiyangyun.care.visit.model.VisitBookingResponse;
import com.zhiyangyun.care.visit.service.VisitService;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class FamilyPortalServiceImpl implements FamilyPortalService {
  private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_DATE;
  private static final DateTimeFormatter HHMM_FMT = DateTimeFormatter.ofPattern("HH:mm");
  private static final Pattern URL_PATTERN = Pattern.compile("https?://[^\\s,;]+", Pattern.CASE_INSENSITIVE);

  private static final String METRIC_BP = "BP";
  private static final String METRIC_TEMP = "TEMP";
  private static final String METRIC_HR = "HR";
  private static final String METRIC_SUGAR = "SUGAR";

  private static final String STATE_MESSAGE_READ = "MESSAGE_READ";
  private static final String STATE_ALBUM_LIKE = "ALBUM_LIKE";
  private static final String STATE_ALBUM_COMMENT = "ALBUM_COMMENT";
  private static final String STATE_AUTO_PAY = "AUTO_PAY";
  private static final String STATE_PROFILE = "PROFILE";
  private static final String STATE_NOTIFICATION = "NOTIFICATION";
  private static final String STATE_AFFECTION = "AFFECTION";
  private static final String STATE_SECURITY = "SECURITY";
  private static final String STATE_COMMUNICATION = "COMMUNICATION";
  private static final String STATE_PAYMENT_ALERT = "PAYMENT_ALERT";
  private static final String STATE_TODO_ACTION = "TODO_ACTION";

  private static final String ORDER_STATUS_INIT = "INIT";
  private static final String ORDER_STATUS_PREPAY_CREATED = "PREPAY_CREATED";
  private static final String ORDER_STATUS_PAID = "PAID";
  private static final String ORDER_STATUS_CLOSED = "CLOSED";
  private static final String ORDER_STATUS_FAILED = "FAILED";
  private static final Duration WECHAT_PLATFORM_CERT_CACHE_TTL = Duration.ofHours(6);
  private static final Duration WECHAT_NOTIFY_REPLAY_TTL = Duration.ofMinutes(15);
  private static final int WECHAT_NOTIFY_REPLAY_CACHE_MAX_SIZE = 8192;

  private final ElderFamilyMapper elderFamilyMapper;
  private final ElderMapper elderMapper;
  private final FamilyUserMapper familyUserMapper;
  private final BedMapper bedMapper;
  private final RoomMapper roomMapper;
  private final ElderOutingRecordMapper elderOutingRecordMapper;
  private final ElderMedicalOutingRecordMapper elderMedicalOutingRecordMapper;
  private final HealthDataRecordMapper healthDataRecordMapper;
  private final HealthNursingLogMapper healthNursingLogMapper;
  private final MealPlanMapper mealPlanMapper;
  private final OaNoticeMapper oaNoticeMapper;
  private final OaAlbumMapper oaAlbumMapper;
  private final OaSuggestionMapper oaSuggestionMapper;
  private final OaTodoMapper oaTodoMapper;
  private final AssessmentRecordMapper assessmentRecordMapper;
  private final BillMonthlyMapper billMonthlyMapper;
  private final BillService billService;
  private final ElderAccountMapper elderAccountMapper;
  private final ElderAccountLogMapper elderAccountLogMapper;
  private final ServiceItemMapper serviceItemMapper;
  private final ServiceBookingMapper serviceBookingMapper;
  private final ProductMapper productMapper;
  private final InventoryBatchMapper inventoryBatchMapper;
  private final StoreOrderMapper storeOrderMapper;
  private final OrderItemMapper orderItemMapper;
  private final StoreOrderService storeOrderService;
  private final VisitService visitService;
  private final CareTaskService careTaskService;
  private final OrgMapper orgMapper;
  private final FamilyPortalStateMapper familyPortalStateMapper;
  private final FamilyRechargeOrderMapper familyRechargeOrderMapper;
  private final FamilyPortalProperties familyPortalProperties;
  private final FamilyWechatNotifyService familyWechatNotifyService;
  private final PasswordEncoder passwordEncoder;
  private final ObjectMapper objectMapper;
  private final RestTemplate restTemplate;
  private final Map<String, PlatformCertificateHolder> wechatPlatformCertCache = new ConcurrentHashMap<>();
  private final Map<String, Long> wechatNotifyReplayCache = new ConcurrentHashMap<>();
  private volatile LocalDateTime wechatPlatformCertSyncedAt;

  public FamilyPortalServiceImpl(ElderFamilyMapper elderFamilyMapper,
      ElderMapper elderMapper,
      FamilyUserMapper familyUserMapper,
      BedMapper bedMapper,
      RoomMapper roomMapper,
      ElderOutingRecordMapper elderOutingRecordMapper,
      ElderMedicalOutingRecordMapper elderMedicalOutingRecordMapper,
      HealthDataRecordMapper healthDataRecordMapper,
      HealthNursingLogMapper healthNursingLogMapper,
      MealPlanMapper mealPlanMapper,
      OaNoticeMapper oaNoticeMapper,
      OaAlbumMapper oaAlbumMapper,
      OaSuggestionMapper oaSuggestionMapper,
      OaTodoMapper oaTodoMapper,
      AssessmentRecordMapper assessmentRecordMapper,
      BillMonthlyMapper billMonthlyMapper,
      BillService billService,
      ElderAccountMapper elderAccountMapper,
      ElderAccountLogMapper elderAccountLogMapper,
      ServiceItemMapper serviceItemMapper,
      ServiceBookingMapper serviceBookingMapper,
      ProductMapper productMapper,
      InventoryBatchMapper inventoryBatchMapper,
      StoreOrderMapper storeOrderMapper,
      OrderItemMapper orderItemMapper,
      StoreOrderService storeOrderService,
      VisitService visitService,
      CareTaskService careTaskService,
      OrgMapper orgMapper,
      FamilyPortalStateMapper familyPortalStateMapper,
      FamilyRechargeOrderMapper familyRechargeOrderMapper,
      FamilyPortalProperties familyPortalProperties,
      FamilyWechatNotifyService familyWechatNotifyService,
      PasswordEncoder passwordEncoder,
      ObjectMapper objectMapper) {
    this.elderFamilyMapper = elderFamilyMapper;
    this.elderMapper = elderMapper;
    this.familyUserMapper = familyUserMapper;
    this.bedMapper = bedMapper;
    this.roomMapper = roomMapper;
    this.elderOutingRecordMapper = elderOutingRecordMapper;
    this.elderMedicalOutingRecordMapper = elderMedicalOutingRecordMapper;
    this.healthDataRecordMapper = healthDataRecordMapper;
    this.healthNursingLogMapper = healthNursingLogMapper;
    this.mealPlanMapper = mealPlanMapper;
    this.oaNoticeMapper = oaNoticeMapper;
    this.oaAlbumMapper = oaAlbumMapper;
    this.oaSuggestionMapper = oaSuggestionMapper;
    this.oaTodoMapper = oaTodoMapper;
    this.assessmentRecordMapper = assessmentRecordMapper;
    this.billMonthlyMapper = billMonthlyMapper;
    this.billService = billService;
    this.elderAccountMapper = elderAccountMapper;
    this.elderAccountLogMapper = elderAccountLogMapper;
    this.serviceItemMapper = serviceItemMapper;
    this.serviceBookingMapper = serviceBookingMapper;
    this.productMapper = productMapper;
    this.inventoryBatchMapper = inventoryBatchMapper;
    this.storeOrderMapper = storeOrderMapper;
    this.orderItemMapper = orderItemMapper;
    this.storeOrderService = storeOrderService;
    this.visitService = visitService;
    this.careTaskService = careTaskService;
    this.orgMapper = orgMapper;
    this.familyPortalStateMapper = familyPortalStateMapper;
    this.familyRechargeOrderMapper = familyRechargeOrderMapper;
    this.familyPortalProperties = familyPortalProperties;
    this.familyWechatNotifyService = familyWechatNotifyService;
    this.passwordEncoder = passwordEncoder;
    this.objectMapper = objectMapper;
    this.restTemplate = new RestTemplate();
  }

  @Override
  public FamilyPortalModels.HomeDashboardResponse getHomeDashboard(Long orgId, Long familyUserId, Long elderId) {
    List<ElderFamily> relations = listBoundRelations(orgId, familyUserId);
    List<Long> elderIds = extractElderIds(relations);
    if (elderIds.isEmpty()) {
      throw new IllegalArgumentException("请先绑定老人信息");
    }

    Long targetElderId = resolveTargetElderId(elderIds, elderId);
    Map<Long, ElderProfile> elderMap = loadElderMap(orgId, elderIds);
    Map<Long, String> roomTextMap = loadRoomTextMap(orgId, elderMap.values());
    Map<Long, String> locationTextMap = loadLocationTextMap(orgId, elderIds, roomTextMap);
    Map<Long, String> relationTextMap = relations.stream()
        .collect(Collectors.toMap(ElderFamily::getElderId, item -> defaultText(item.getRelation(), "家属"), (a, b) -> a));

    FamilyPortalModels.HomeDashboardResponse response = new FamilyPortalModels.HomeDashboardResponse();
    LocalDateTime latestHealthTime = null;

    for (Long id : elderIds) {
      ElderProfile elder = elderMap.get(id);
      if (elder == null) {
        continue;
      }
      FamilyPortalModels.ElderCard card = new FamilyPortalModels.ElderCard();
      card.setElderId(elder.getId());
      card.setElderName(defaultText(elder.getFullName(), "未命名老人"));
      card.setAge(calculateAge(elder.getBirthDate()));
      card.setGender(toGenderText(elder.getGender()));
      card.setRoomNo(defaultText(roomTextMap.get(elder.getId()), "房间待分配"));
      card.setCareLevel(defaultText(elder.getCareLevel(), "待评估"));
      String status = resolveElderStatusText(orgId, elder.getId(), elder.getStatus());
      card.setStatus(status);
      card.setStatusType(resolveStatusType(status));
      card.setCheckinDays(calcCheckinDays(elder.getAdmissionDate()));
      HealthOverview overview = buildHealthOverview(orgId, elder.getId());
      card.setHealthOverview(overview.overviewText);
      card.setLatestUpdateTime(overview.latestTimeText);
      card.setSpecialTag(resolveSpecialTag(elder, status));
      card.setDynamicPreview(resolveDynamicPreview(orgId, elder.getId()));
      card.setRelation(defaultText(relationTextMap.get(elder.getId()), "家属"));
      card.setLocationText(defaultText(locationTextMap.get(elder.getId()), "院内"));
      response.getElders().add(card);
      if (overview.latestTime != null && (latestHealthTime == null || overview.latestTime.isAfter(latestHealthTime))) {
        latestHealthTime = overview.latestTime;
      }
    }

    response.getElders().sort(Comparator.comparing(FamilyPortalModels.ElderCard::getElderId));
    response.setHealthSummary(buildHealthSummary(orgId, targetElderId, latestHealthTime));
    response.setSchedules(listTodaySchedules(orgId, familyUserId, targetElderId).stream().limit(4).toList());
    List<FamilyPortalModels.MealSummary> mealDays = listMealCalendar(orgId);
    response.setMeal(mealDays.isEmpty() ? defaultMealSummary() : mealDays.get(0));
    response.setNotices(listMessages(orgId, familyUserId, 1, 3));
    response.setFocusEvents(buildFocusEvents(response));
    return response;
  }

  @Override
  public FamilyPortalModels.WeeklyBriefResponse getWeeklyBrief(Long orgId, Long familyUserId, Long elderId) {
    List<Long> elderIds = extractElderIds(listBoundRelations(orgId, familyUserId));
    Long targetElderId = resolveTargetElderId(elderIds, elderId);
    ElderProfile elder = elderMapper.selectById(targetElderId);

    LocalDate endDate = LocalDate.now();
    LocalDate startDate = endDate.minusDays(6);
    WeeklySnapshot snapshot = collectWeeklySnapshot(orgId, familyUserId, targetElderId, startDate, endDate, true);

    FamilyPortalModels.HealthTrendResponse trend = getHealthTrend(orgId, familyUserId, targetElderId, "7d");
    FamilyPortalModels.VitalSnapshot latest = trend.getLatest() == null ? new FamilyPortalModels.VitalSnapshot() : trend.getLatest();

    FamilyPortalModels.WeeklyBriefResponse response = new FamilyPortalModels.WeeklyBriefResponse();
    response.setElderId(targetElderId);
    response.setElderName(elder == null ? "未命名老人" : defaultText(elder.getFullName(), "未命名老人"));
    response.setWeekRange(startDate.format(DATE_FMT) + " ~ " + endDate.format(DATE_FMT));
    response.setOverallLevel(snapshot.abnormalCount > 0 ? "warning" : "normal");
    response.setOverallText(snapshot.abnormalCount > 0
        ? "本周存在异常指标，护理团队已持续跟进，请关注后续提醒"
        : "本周整体状态平稳，护理与活动安排执行有序");
    response.setHealthCheckCount((int) snapshot.healthCheckCount);
    response.setAbnormalCount((int) snapshot.abnormalCount);
    response.setNursingLogCount((int) snapshot.nursingLogCount);
    response.setActivityCount((int) snapshot.activityCount);
    response.setCommunicationCount((int) snapshot.communicationCount);
    response.setUnreadMessageCount(snapshot.unreadMessageCount);

    int latestSbp = safeInt(latest.getSbp());
    int latestDbp = safeInt(latest.getDbp());
    int latestHr = safeInt(latest.getHr());
    double latestTemp = safeDouble(latest.getTemp());
    double latestSugar = safeDouble(latest.getSugar());

    response.getVitalBadges().add(buildVitalBadge("血压",
        (latestSbp <= 0 || latestDbp <= 0) ? "--" : (latestSbp + "/" + latestDbp + " mmHg"),
        (latestSbp <= 0 || latestDbp <= 0) ? "待记录"
            : ((latestSbp >= 140 || latestDbp >= 90) ? "需关注" : "正常")));
    response.getVitalBadges().add(buildVitalBadge("心率",
        latestHr <= 0 ? "--" : (latestHr + " 次/分"),
        latestHr <= 0 ? "待记录" : ((latestHr > 100 || latestHr < 50) ? "需关注" : "正常")));
    response.getVitalBadges().add(buildVitalBadge("体温",
        latestTemp <= 0 ? "--" : String.format(Locale.ROOT, "%.1f ℃", latestTemp),
        latestTemp <= 0 ? "待记录" : (latestTemp > 37.3D ? "需关注" : "正常")));
    response.getVitalBadges().add(buildVitalBadge("血糖",
        latestSugar <= 0 ? "--" : String.format(Locale.ROOT, "%.1f mmol/L", latestSugar),
        latestSugar <= 0 ? "待记录" : (latestSugar > 7.8D ? "需关注" : "正常")));

    response.getHighlights().add("本周累计健康检测 " + snapshot.healthCheckCount + " 次，护理日志 " + snapshot.nursingLogCount + " 条");
    response.getHighlights().add(snapshot.activityCount > 0
        ? "本周参与活动 " + snapshot.activityCount + " 场，精神互动保持积极"
        : "本周暂无活动记录，建议关注精神文化活动参与情况");
    response.getHighlights().add("家属端本周沟通 " + snapshot.communicationCount + " 次，未读提醒 " + snapshot.unreadMessageCount + " 条");

    if (snapshot.abnormalCount > 0) {
      response.getSuggestions().add("建议在未来 48 小时内重点关注血压/血糖复测动态");
    }
    if (snapshot.unreadMessageCount > 0) {
      response.getSuggestions().add("当前有 " + snapshot.unreadMessageCount + " 条未读提醒，建议先处理紧急类消息");
    }
    if (snapshot.communicationCount == 0) {
      response.getSuggestions().add("建议每周至少进行 1 次护理沟通，及时同步老人状态");
    }
    if (response.getSuggestions().isEmpty()) {
      response.getSuggestions().add("本周状态整体稳定，建议保持当前沟通与护理节奏");
    }
    return response;
  }

  @Override
  public List<FamilyPortalModels.WeeklyBriefDigestItem> listWeeklyBriefHistory(Long orgId, Long familyUserId, Long elderId,
      Integer weeks) {
    List<Long> elderIds = extractElderIds(listBoundRelations(orgId, familyUserId));
    Long targetElderId = resolveTargetElderId(elderIds, elderId);
    int totalWeeks = weeks == null ? 8 : Math.max(1, Math.min(weeks, 12));

    List<FamilyPortalModels.WeeklyBriefDigestItem> history = new ArrayList<>();
    LocalDate anchor = LocalDate.now();
    for (int i = 0; i < totalWeeks; i++) {
      LocalDate endDate = anchor.minusDays(i * 7L);
      LocalDate startDate = endDate.minusDays(6);
      WeeklySnapshot snapshot = collectWeeklySnapshot(orgId, familyUserId, targetElderId, startDate, endDate, i == 0);
      FamilyPortalModels.WeeklyBriefDigestItem item = new FamilyPortalModels.WeeklyBriefDigestItem();
      item.setWeekRange(startDate.format(DATE_FMT) + " ~ " + endDate.format(DATE_FMT));
      item.setOverallLevel(snapshot.abnormalCount > 0 ? "warning" : "normal");
      item.setOverallText(snapshot.abnormalCount > 0 ? "本周存在异常指标" : "本周整体平稳");
      item.setHealthCheckCount((int) snapshot.healthCheckCount);
      item.setAbnormalCount((int) snapshot.abnormalCount);
      item.setNursingLogCount((int) snapshot.nursingLogCount);
      item.setActivityCount((int) snapshot.activityCount);
      item.setCommunicationCount((int) snapshot.communicationCount);
      item.setQuickText(buildWeeklyQuickText(snapshot));
      history.add(item);
    }
    return history;
  }

  @Override
  public List<FamilyPortalModels.MessageItem> listMessages(Long orgId, Long familyUserId, int pageNo, int pageSize) {
    return listMessages(orgId, familyUserId, pageNo, pageSize, null, null, null);
  }

  @Override
  public List<FamilyPortalModels.MessageItem> listMessages(Long orgId, Long familyUserId, int pageNo, int pageSize,
      String level, String type, Boolean unreadOnly) {
    List<Long> elderIds = extractElderIds(listBoundRelations(orgId, familyUserId));
    String primaryElderName = resolvePrimaryElderName(orgId, elderIds);
    Set<Long> readSet = loadLongSetState(orgId, familyUserId, STATE_MESSAGE_READ);
    int safePageNo = Math.max(pageNo, 1);
    int safePageSize = Math.max(pageSize, 1);
    boolean hasFilter = (level != null && !level.isBlank())
        || (type != null && !type.isBlank())
        || Boolean.TRUE.equals(unreadOnly);
    int fetchPageNo = hasFilter ? 1 : safePageNo;
    int fetchPageSize = hasFilter ? Math.min(Math.max(safePageNo * safePageSize, 200), 500) : safePageSize;
    boolean needMaskPhone = shouldMaskSensitive(orgId, familyUserId);
    String emergencyPhone = maskPhone(resolveEmergencyPhone(orgId), needMaskPhone);

    Page<OaNotice> page = oaNoticeMapper.selectPage(new Page<>(fetchPageNo, fetchPageSize),
        Wrappers.lambdaQuery(OaNotice.class)
            .eq(OaNotice::getIsDeleted, 0)
            .eq(OaNotice::getOrgId, orgId)
            .eq(OaNotice::getStatus, "PUBLISHED")
            .orderByDesc(OaNotice::getPublishTime)
            .orderByDesc(OaNotice::getCreateTime));

    List<FamilyPortalModels.MessageItem> items = new ArrayList<>();
    for (OaNotice notice : page.getRecords()) {
      FamilyPortalModels.MessageItem item = new FamilyPortalModels.MessageItem();
      item.setId(notice.getId());
      item.setLevel(resolveMessageLevel(notice.getTitle(), notice.getContent()));
      item.setLevelText(resolveMessageLevelText(item.getLevel()));
      item.setType(resolveMessageType(notice.getTitle(), notice.getContent()));
      item.setTitle(defaultText(notice.getTitle(), "系统通知"));
      item.setContent(defaultText(notice.getContent(), "暂无详情"));
      item.setElderName(primaryElderName);
      item.setTime(formatFriendlyDateTime(notice.getPublishTime() == null ? notice.getCreateTime() : notice.getPublishTime()));
      item.setUnread(!readSet.contains(notice.getId()));
      item.setPhone(emergencyPhone);
      item.setTimeline(buildMessageTimeline(notice));
      items.add(item);
    }
    List<FamilyPortalModels.MessageItem> filtered = filterMessageItems(items, level, type, unreadOnly);
    if (filtered.isEmpty() && !hasFilter) {
      FamilyPortalModels.MessageItem fallback = new FamilyPortalModels.MessageItem();
      fallback.setId(0L);
      fallback.setLevel("normal");
      fallback.setLevelText("三级提醒");
      fallback.setType("系统公告");
      fallback.setTitle("暂无最新通知");
      fallback.setContent("当前暂无需要处理的消息，系统将继续监测老人状态。");
      fallback.setElderName(primaryElderName);
      fallback.setTime(formatFriendlyDateTime(LocalDateTime.now()));
      fallback.setUnread(false);
      fallback.setPhone(emergencyPhone);
      fallback.setTimeline(List.of("系统状态正常", "如有异常将第一时间推送"));
      filtered.add(fallback);
    }
    if (hasFilter) {
      return paginateList(filtered, safePageNo, safePageSize);
    }
    return filtered;
  }

  @Override
  public FamilyPortalModels.MessageSummaryResponse getMessageSummary(Long orgId, Long familyUserId) {
    List<FamilyPortalModels.MessageItem> messages = listMessages(orgId, familyUserId, 1, 200);
    List<FamilyPortalModels.MessageItem> effective = messages.stream()
        .filter(item -> item.getId() != null && item.getId() > 0)
        .toList();

    FamilyPortalModels.MessageSummaryResponse response = new FamilyPortalModels.MessageSummaryResponse();
    response.setTotalCount(effective.size());
    response.setUnreadCount((int) effective.stream().filter(item -> Boolean.TRUE.equals(item.getUnread())).count());
    response.setUrgentUnreadCount((int) effective.stream()
        .filter(item -> "urgent".equalsIgnoreCase(defaultText(item.getLevel(), "")) && Boolean.TRUE.equals(item.getUnread()))
        .count());
    response.setWarningUnreadCount((int) effective.stream()
        .filter(item -> "warning".equalsIgnoreCase(defaultText(item.getLevel(), "")) && Boolean.TRUE.equals(item.getUnread()))
        .count());
    response.setNormalUnreadCount((int) effective.stream()
        .filter(item -> "normal".equalsIgnoreCase(defaultText(item.getLevel(), "")) && Boolean.TRUE.equals(item.getUnread()))
        .count());

    Map<String, List<FamilyPortalModels.MessageItem>> byType = effective.stream()
        .collect(Collectors.groupingBy(item -> defaultText(item.getType(), "系统公告"), LinkedHashMap::new, Collectors.toList()));
    List<FamilyPortalModels.MessageTypeSummaryItem> typeSummary = new ArrayList<>();
    byType.forEach((type, list) -> {
      FamilyPortalModels.MessageTypeSummaryItem item = new FamilyPortalModels.MessageTypeSummaryItem();
      item.setType(type);
      item.setCount(list.size());
      item.setUnreadCount((int) list.stream().filter(msg -> Boolean.TRUE.equals(msg.getUnread())).count());
      typeSummary.add(item);
    });
    typeSummary.sort(Comparator
        .comparing(FamilyPortalModels.MessageTypeSummaryItem::getUnreadCount, Comparator.nullsLast(Comparator.reverseOrder()))
        .thenComparing(FamilyPortalModels.MessageTypeSummaryItem::getCount, Comparator.nullsLast(Comparator.reverseOrder())));
    response.setTypeSummary(typeSummary);

    List<FamilyPortalModels.MessageItem> urgentItems = effective.stream()
        .filter(item -> "urgent".equalsIgnoreCase(defaultText(item.getLevel(), "")))
        .sorted(Comparator.comparing(FamilyPortalModels.MessageItem::getTime, Comparator.nullsLast(Comparator.reverseOrder())))
        .limit(3)
        .toList();
    if (urgentItems.isEmpty()) {
      urgentItems = effective.stream()
          .filter(item -> "warning".equalsIgnoreCase(defaultText(item.getLevel(), "")))
          .limit(3)
          .toList();
    }
    response.setUrgentItems(urgentItems);
    return response;
  }

  @Override
  public FamilyPortalModels.MessageItem getMessageDetail(Long orgId, Long familyUserId, Long messageId) {
    List<FamilyPortalModels.MessageItem> list = listMessages(orgId, familyUserId, 1, 50);
    return list.stream()
        .filter(item -> Objects.equals(item.getId(), messageId))
        .findFirst()
        .orElse(list.get(0));
  }

  @Override
  public boolean markMessageRead(Long orgId, Long familyUserId, Long messageId) {
    upsertState(orgId, familyUserId, STATE_MESSAGE_READ, String.valueOf(messageId),
        Map.of("messageId", messageId, "read", true));
    return true;
  }

  @Override
  public boolean markAllMessagesRead(Long orgId, Long familyUserId) {
    List<OaNotice> notices = oaNoticeMapper.selectList(
        Wrappers.lambdaQuery(OaNotice.class)
            .eq(OaNotice::getIsDeleted, 0)
            .eq(OaNotice::getOrgId, orgId)
            .eq(OaNotice::getStatus, "PUBLISHED")
            .orderByDesc(OaNotice::getPublishTime)
            .orderByDesc(OaNotice::getCreateTime)
            .last("LIMIT 200"));
    for (OaNotice notice : notices) {
      if (notice.getId() == null) {
        continue;
      }
      upsertState(orgId, familyUserId, STATE_MESSAGE_READ, String.valueOf(notice.getId()),
          Map.of("messageId", notice.getId(), "read", true));
    }
    return true;
  }

  @Override
  public FamilyPortalModels.HealthTrendResponse getHealthTrend(Long orgId, Long familyUserId, Long elderId, String range) {
    List<Long> elderIds = extractElderIds(listBoundRelations(orgId, familyUserId));
    Long targetElderId = resolveTargetElderId(elderIds, elderId);
    int days = resolveRangeDays(range);
    LocalDate startDate = LocalDate.now().minusDays(days - 1L);
    if (!hasFullSensitiveScope(orgId, familyUserId, targetElderId)) {
      FamilyPortalModels.HealthTrendResponse limited = new FamilyPortalModels.HealthTrendResponse();
      limited.setRange(normalizeRange(range));
      limited.setTrend(buildDefaultTrend(days));
      FamilyPortalModels.VitalSnapshot latest = new FamilyPortalModels.VitalSnapshot();
      latest.setSbp(0);
      latest.setDbp(0);
      latest.setHr(0);
      latest.setTemp(0D);
      latest.setSugar(0D);
      limited.setLatest(latest);
      limited.setRiskTips(List.of("当前权限范围仅支持摘要查看，如需完整趋势请联系主绑定家属授权。"));
      return limited;
    }

    List<HealthDataRecord> records = healthDataRecordMapper.selectList(
        Wrappers.lambdaQuery(HealthDataRecord.class)
            .eq(HealthDataRecord::getIsDeleted, 0)
            .eq(HealthDataRecord::getOrgId, orgId)
            .eq(HealthDataRecord::getElderId, targetElderId)
            .ge(HealthDataRecord::getMeasuredAt, startDate.atStartOfDay())
            .orderByDesc(HealthDataRecord::getMeasuredAt)
            .last("LIMIT 500"));

    Map<LocalDate, Map<String, String>> dayMetricValues = new HashMap<>();
    for (HealthDataRecord record : records) {
      LocalDate date = record.getMeasuredAt() == null ? null : record.getMeasuredAt().toLocalDate();
      String metric = normalizeMetricType(record.getDataType());
      if (date == null || metric == null) {
        continue;
      }
      dayMetricValues.computeIfAbsent(date, k -> new HashMap<>()).putIfAbsent(metric, record.getDataValue());
    }

    FamilyPortalModels.HealthTrendResponse response = new FamilyPortalModels.HealthTrendResponse();
    response.setRange(normalizeRange(range));

    FamilyPortalModels.VitalSnapshot latest = new FamilyPortalModels.VitalSnapshot();
    List<FamilyPortalModels.HealthTrendPoint> trend = new ArrayList<>();

    for (int i = 0; i < days; i++) {
      LocalDate day = startDate.plusDays(i);
      Map<String, String> values = dayMetricValues.getOrDefault(day, Map.of());
      FamilyPortalModels.HealthTrendPoint point = new FamilyPortalModels.HealthTrendPoint();
      point.setDate(day.format(DateTimeFormatter.ofPattern("MM-dd")));
      int[] bp = parseBloodPressure(values.get(METRIC_BP));
      point.setSbp(bp[0]);
      point.setDbp(bp[1]);
      point.setHr(parseInteger(values.get(METRIC_HR), 0));
      point.setTemp(parseDouble(values.get(METRIC_TEMP), 0D));
      point.setSugar(parseDouble(values.get(METRIC_SUGAR), 0D));
      trend.add(point);
    }

    if (trend.stream().allMatch(item -> safeInt(item.getSbp()) == 0)) {
      trend = buildDefaultTrend(days);
    }

    FamilyPortalModels.HealthTrendPoint latestPoint = trend.get(trend.size() - 1);
    latest.setSbp(latestPoint.getSbp());
    latest.setDbp(latestPoint.getDbp());
    latest.setHr(latestPoint.getHr());
    latest.setTemp(latestPoint.getTemp());
    latest.setSugar(latestPoint.getSugar());

    response.setTrend(trend);
    response.setLatest(latest);
    response.setRiskTips(buildRiskTips(latest));
    return response;
  }

  @Override
  public List<FamilyPortalModels.MedicalRecordItem> listMedicalRecords(Long orgId, Long familyUserId, Long elderId) {
    List<Long> elderIds = ensureBoundElderIds(extractElderIds(listBoundRelations(orgId, familyUserId)));
    Long targetElderId = elderId == null ? null : resolveTargetElderId(elderIds, elderId);
    if (targetElderId != null && !hasFullSensitiveScope(orgId, familyUserId, targetElderId)) {
      FamilyPortalModels.MedicalRecordItem limited = new FamilyPortalModels.MedicalRecordItem();
      limited.setId(0L);
      limited.setDate(LocalDate.now().format(DATE_FMT));
      limited.setHospital("访问受限");
      limited.setReason("当前账号暂无完整就医档案权限");
      limited.setDiagnosis("请联系主绑定家属或机构管理员授权后查看");
      limited.setAdvice("已启用隐私保护策略");
      limited.setMeds("--");
      return List.of(limited);
    }

    List<ElderMedicalOutingRecord> records = elderMedicalOutingRecordMapper.selectList(
        Wrappers.lambdaQuery(ElderMedicalOutingRecord.class)
            .eq(ElderMedicalOutingRecord::getIsDeleted, 0)
            .eq(ElderMedicalOutingRecord::getOrgId, orgId)
            .in(ElderMedicalOutingRecord::getElderId, elderIds)
            .eq(targetElderId != null, ElderMedicalOutingRecord::getElderId, targetElderId)
            .orderByDesc(ElderMedicalOutingRecord::getOutingDate)
            .orderByDesc(ElderMedicalOutingRecord::getUpdateTime)
            .last("LIMIT 20"));

    return records.stream().map(record -> {
      FamilyPortalModels.MedicalRecordItem item = new FamilyPortalModels.MedicalRecordItem();
      item.setId(record.getId());
      item.setDate(record.getOutingDate() == null ? "" : record.getOutingDate().format(DATE_FMT));
      item.setHospital(defaultText(record.getHospitalName(), "院内就诊"));
      item.setReason(defaultText(record.getReason(), "常规复查"));
      item.setDiagnosis(defaultText(record.getDiagnosis(), "待补充"));
      item.setAdvice(defaultText(record.getRemark(), "按医嘱持续观察"));
      item.setMeds("用药记录待同步");
      return item;
    }).toList();
  }

  @Override
  public List<FamilyPortalModels.AssessmentReportItem> listAssessmentReports(Long orgId, Long familyUserId, Long elderId) {
    List<Long> elderIds = ensureBoundElderIds(extractElderIds(listBoundRelations(orgId, familyUserId)));
    Long targetElderId = elderId == null ? null : resolveTargetElderId(elderIds, elderId);
    String orgName = resolveOrgName(orgId);
    boolean fullScope = targetElderId == null || hasFullSensitiveScope(orgId, familyUserId, targetElderId);

    List<AssessmentRecord> records = assessmentRecordMapper.selectList(
        Wrappers.lambdaQuery(AssessmentRecord.class)
            .eq(AssessmentRecord::getIsDeleted, 0)
            .eq(AssessmentRecord::getOrgId, orgId)
            .in(AssessmentRecord::getElderId, elderIds)
            .eq(targetElderId != null, AssessmentRecord::getElderId, targetElderId)
            .orderByDesc(AssessmentRecord::getAssessmentDate)
            .orderByDesc(AssessmentRecord::getUpdateTime)
            .last("LIMIT 20"));

    return records.stream().map(record -> {
      FamilyPortalModels.AssessmentReportItem item = new FamilyPortalModels.AssessmentReportItem();
      item.setId(record.getId());
      item.setReportName(defaultText(record.getAssessmentType(), "综合评估") + "报告");
      item.setDate(record.getAssessmentDate() == null ? "" : record.getAssessmentDate().format(DATE_FMT));
      item.setOrgName(orgName);
      item.setSummary(defaultText(record.getResultSummary(), defaultText(record.getSuggestion(), "暂无评估摘要")));
      item.setScore(record.getScore() == null ? "--" : record.getScore().stripTrailingZeros().toPlainString());
      item.setRisk(defaultText(record.getLevelCode(), "--"));
      item.setType(defaultText(record.getAssessmentType(), "综合评估"));
      item.setFileName(defaultText(record.getReportFileName(), "assessment-" + record.getId() + ".pdf"));
      item.setFileUrl(fullScope ? defaultText(record.getReportFileUrl(), "/uploads/assessment/" + item.getFileName()) : "");
      if (!fullScope) {
        item.setSummary("当前账号仅可查看报告摘要，请完成授权后查看PDF全文。");
      }
      return item;
    }).toList();
  }

  @Override
  public List<FamilyPortalModels.ScheduleItem> listTodaySchedules(Long orgId, Long familyUserId, Long elderId) {
    List<Long> elderIds = extractElderIds(listBoundRelations(orgId, familyUserId));
    Long targetElderId = resolveTargetElderId(elderIds, elderId);

    List<CareTaskTodayItem> tasks = careTaskService.getTodayTasks(orgId, null, targetElderId, LocalDate.now());
    List<FamilyPortalModels.ScheduleItem> result = new ArrayList<>();
    for (CareTaskTodayItem task : tasks) {
      FamilyPortalModels.ScheduleItem item = new FamilyPortalModels.ScheduleItem();
      item.setId(task.getTaskDailyId());
      item.setTime(task.getPlanTime() == null ? "--:--" : task.getPlanTime().format(HHMM_FMT));
      item.setName(defaultText(task.getTaskName(), "护理任务"));
      item.setType("护理");
      item.setStatus(mapTaskStatus(task.getStatus()));
      item.setOwner(defaultText(task.getStaffName(), "护理组"));
      item.setDescription(buildScheduleDescription(task));
      item.setDurationMinutes(resolveScheduleDuration(task.getTaskName()));
      item.setPrecautions(resolveSchedulePrecautions(task.getTaskName()));
      item.setFitNote(resolveScheduleFitNote(task.getTaskName(), task.getCareLevel()));
      result.add(item);
    }
    result.sort(Comparator.comparing(FamilyPortalModels.ScheduleItem::getTime, Comparator.nullsLast(String::compareTo)));
    return result;
  }

  @Override
  public List<FamilyPortalModels.MealSummary> listMealCalendar(Long orgId) {
    List<MealPlan> mealPlans = mealPlanMapper.selectList(
        Wrappers.lambdaQuery(MealPlan.class)
            .eq(MealPlan::getIsDeleted, 0)
            .eq(MealPlan::getOrgId, orgId)
            .orderByDesc(MealPlan::getPlanDate)
            .last("LIMIT 120"));

    Map<LocalDate, FamilyPortalModels.MealSummary> grouped = new LinkedHashMap<>();
    for (MealPlan plan : mealPlans) {
      if (plan.getPlanDate() == null) {
        continue;
      }
      FamilyPortalModels.MealSummary summary = grouped.computeIfAbsent(plan.getPlanDate(), date -> {
        FamilyPortalModels.MealSummary item = new FamilyPortalModels.MealSummary();
        item.setDate(date.format(DATE_FMT));
        item.setReview("机构已安排当日营养餐，请家属放心。");
        item.setEatingStatus("进食情况正常，护理团队持续观察。");
        item.setPhotos(new ArrayList<>());
        return item;
      });
      if (plan.getRemark() != null && !plan.getRemark().isBlank()) {
        summary.setReview(plan.getRemark().trim());
      }
      if (plan.getRemark() != null && plan.getRemark().contains("进食")) {
        summary.setEatingStatus(plan.getRemark().trim());
      }
      String menu = defaultText(plan.getMenu(), "待更新");
      if (isBreakfast(plan.getMealType())) {
        summary.getBreakfast().add(menu);
      } else if (isLunch(plan.getMealType())) {
        summary.getLunch().add(menu);
      } else if (isDinner(plan.getMealType())) {
        summary.getDinner().add(menu);
      }
      addMealTags(summary.getTags(), menu + " " + defaultText(plan.getRemark(), ""));
      summary.getPhotos().addAll(extractPhotoUrls(defaultText(plan.getRemark(), "") + "\n" + defaultText(plan.getMenu(), "")));
    }

    List<FamilyPortalModels.MealSummary> list = new ArrayList<>(grouped.values());
    list.sort(Comparator.comparing(FamilyPortalModels.MealSummary::getDate).reversed());
    if (list.isEmpty()) {
      list.add(defaultMealSummary());
    }
    for (FamilyPortalModels.MealSummary summary : list) {
      if (summary.getBreakfast().isEmpty()) {
        summary.getBreakfast().add("待更新");
      }
      if (summary.getLunch().isEmpty()) {
        summary.getLunch().add("待更新");
      }
      if (summary.getDinner().isEmpty()) {
        summary.getDinner().add("待更新");
      }
      if (summary.getTags().isEmpty()) {
        summary.getTags().add("均衡膳食");
      }
      summary.setPhotos(uniqueList(summary.getPhotos()));
    }
    return list;
  }

  @Override
  public List<FamilyPortalModels.CareLogDay> listCareLogs(Long orgId, Long familyUserId, Long elderId) {
    List<Long> elderIds = ensureBoundElderIds(extractElderIds(listBoundRelations(orgId, familyUserId)));
    Long targetElderId = elderId == null ? null : resolveTargetElderId(elderIds, elderId);

    List<HealthNursingLog> logs = healthNursingLogMapper.selectList(
        Wrappers.lambdaQuery(HealthNursingLog.class)
            .eq(HealthNursingLog::getIsDeleted, 0)
            .eq(HealthNursingLog::getOrgId, orgId)
            .in(HealthNursingLog::getElderId, elderIds)
            .eq(targetElderId != null, HealthNursingLog::getElderId, targetElderId)
            .orderByDesc(HealthNursingLog::getLogTime)
            .last("LIMIT 120"));

    Map<String, FamilyPortalModels.CareLogDay> grouped = new LinkedHashMap<>();
    for (HealthNursingLog log : logs) {
      String date = log.getLogTime() == null ? LocalDate.now().format(DATE_FMT) : log.getLogTime().toLocalDate().format(DATE_FMT);
      FamilyPortalModels.CareLogDay day = grouped.computeIfAbsent(date, key -> {
        FamilyPortalModels.CareLogDay item = new FamilyPortalModels.CareLogDay();
        item.setDate(key);
        return item;
      });
      FamilyPortalModels.CareLogItem item = new FamilyPortalModels.CareLogItem();
      item.setTime(log.getLogTime() == null ? "--:--" : log.getLogTime().format(HHMM_FMT));
      item.setProject(defaultText(log.getLogType(), "护理记录"));
      item.setStaff(defaultText(log.getStaffName(), "护理组"));
      item.setStatus(defaultText(log.getStatus(), "已记录"));
      item.setNote(defaultText(log.getContent(), defaultText(log.getRemark(), "护理已执行")));
      item.setPhotos(extractPhotoUrls(defaultText(log.getContent(), "") + "\n" + defaultText(log.getRemark(), "")));
      day.getItems().add(item);
    }
    return new ArrayList<>(grouped.values());
  }

  @Override
  public List<FamilyPortalModels.ActivityAlbumItem> listActivityAlbums(Long orgId, Long familyUserId, int pageNo, int pageSize) {
    Set<Long> liked = loadLongSetState(orgId, familyUserId, STATE_ALBUM_LIKE);
    String elderName = resolvePrimaryElderName(orgId, extractElderIds(listBoundRelations(orgId, familyUserId)));

    Page<OaAlbum> page = oaAlbumMapper.selectPage(new Page<>(pageNo, pageSize),
        Wrappers.lambdaQuery(OaAlbum.class)
            .eq(OaAlbum::getIsDeleted, 0)
            .eq(OaAlbum::getOrgId, orgId)
            .orderByDesc(OaAlbum::getShootDate)
            .orderByDesc(OaAlbum::getUpdateTime));

    List<FamilyPortalModels.ActivityAlbumItem> items = new ArrayList<>();
    for (OaAlbum album : page.getRecords()) {
      FamilyPortalModels.ActivityAlbumItem item = new FamilyPortalModels.ActivityAlbumItem();
      item.setId(album.getId());
      item.setTitle(defaultText(album.getTitle(), "院内活动纪实"));
      LocalDate shootDate = album.getShootDate();
      item.setDate(shootDate == null ? formatFriendlyDateTime(album.getUpdateTime()) : shootDate.format(DATE_FMT));
      item.setElderName(elderName);
      item.setDescription(defaultText(album.getRemark(), defaultText(album.getCategory(), "活动记录")));
      item.setMediaType(resolveAlbumMediaType(album));
      item.setCoverText((album.getPhotoCount() == null ? 0 : album.getPhotoCount()) + " 张照片");
      item.setCoverUrl(defaultText(album.getCoverUrl(), firstPhotoFromAlbum(album)));
      int baseLikes = (album.getPhotoCount() == null ? 0 : album.getPhotoCount()) + 12;
      boolean likedFlag = liked.contains(album.getId());
      item.setLikes(baseLikes + (likedFlag ? 1 : 0));
      item.setComments(countAlbumComments(orgId, album.getId()));
      item.setLiked(likedFlag);
      items.add(item);
    }
    return items;
  }

  @Override
  public FamilyPortalModels.ActivityAlbumItem toggleAlbumLike(Long orgId, Long familyUserId, Long albumId) {
    Set<Long> liked = loadLongSetState(orgId, familyUserId, STATE_ALBUM_LIKE);
    if (liked.contains(albumId)) {
      removeState(orgId, familyUserId, STATE_ALBUM_LIKE, String.valueOf(albumId));
    } else {
      upsertState(orgId, familyUserId, STATE_ALBUM_LIKE, String.valueOf(albumId),
          Map.of("albumId", albumId, "liked", true));
    }
    return listActivityAlbums(orgId, familyUserId, 1, 50).stream()
        .filter(item -> Objects.equals(item.getId(), albumId))
        .findFirst()
        .orElse(null);
  }

  @Override
  public List<FamilyPortalModels.ActivityAlbumCommentItem> listActivityAlbumComments(
      Long orgId, Long familyUserId, Long albumId) {
    if (albumId == null || albumId <= 0) {
      return List.of();
    }
    FamilyUser familyUser = familyUserMapper.selectById(familyUserId);
    String myName = familyUser == null
        ? "家属"
        : defaultText(familyUser.getRealName(), defaultText(familyUser.getPhone(), "家属"));

    List<FamilyPortalState> rows = listStateRows(orgId, familyUserId, STATE_ALBUM_COMMENT,
        String.valueOf(albumId), 200);
    List<FamilyPortalModels.ActivityAlbumCommentItem> comments = rows.stream()
        .map(row -> toAlbumCommentItem(row, myName))
        .filter(Objects::nonNull)
        .sorted(Comparator.comparing(FamilyPortalModels.ActivityAlbumCommentItem::getTime,
            Comparator.nullsLast(Comparator.reverseOrder())))
        .toList();
    if (!comments.isEmpty()) {
      return comments;
    }
    FamilyPortalModels.ActivityAlbumCommentItem sample = new FamilyPortalModels.ActivityAlbumCommentItem();
    sample.setId(1L);
    sample.setAlbumId(albumId);
    sample.setCommenter("家属服务团队");
    sample.setContent("活动记录已同步，欢迎留言互动。");
    sample.setTime(formatFriendlyDateTime(LocalDateTime.now().minusHours(2)));
    sample.setMine(false);
    return List.of(sample);
  }

  @Override
  @Transactional
  public FamilyPortalModels.ActivityAlbumCommentItem addActivityAlbumComment(Long orgId, Long familyUserId, Long albumId,
      FamilyPortalModels.ActivityAlbumCommentCreateRequest request) {
    if (albumId == null || albumId <= 0) {
      throw new IllegalArgumentException("活动相册不存在");
    }
    String content = defaultText(request == null ? null : request.getContent(), "");
    if (content.isBlank()) {
      throw new IllegalArgumentException("评论内容不能为空");
    }

    FamilyUser familyUser = familyUserMapper.selectById(familyUserId);
    String commenter = familyUser == null
        ? "家属"
        : defaultText(familyUser.getRealName(), defaultText(familyUser.getPhone(), "家属"));
    long commentId = System.currentTimeMillis();
    String time = formatFriendlyDateTime(LocalDateTime.now());
    upsertState(orgId, familyUserId, STATE_ALBUM_COMMENT, String.valueOf(albumId), String.valueOf(commentId), Map.of(
        "id", commentId,
        "albumId", albumId,
        "commenter", commenter,
        "content", content.trim(),
        "time", time,
        "mine", true));

    FamilyPortalModels.ActivityAlbumCommentItem item = new FamilyPortalModels.ActivityAlbumCommentItem();
    item.setId(commentId);
    item.setAlbumId(albumId);
    item.setCommenter(commenter);
    item.setContent(content.trim());
    item.setTime(time);
    item.setMine(true);
    return item;
  }

  @Override
  public List<FamilyPortalModels.OutingRecordItem> listOutingRecords(Long orgId, Long familyUserId, Long elderId) {
    List<Long> elderIds = ensureBoundElderIds(extractElderIds(listBoundRelations(orgId, familyUserId)));
    Long targetElderId = elderId == null ? null : resolveTargetElderId(elderIds, elderId);

    List<FamilyPortalModels.OutingRecordItem> items = new ArrayList<>();

    List<ElderOutingRecord> outings = elderOutingRecordMapper.selectList(
        Wrappers.lambdaQuery(ElderOutingRecord.class)
            .eq(ElderOutingRecord::getIsDeleted, 0)
            .eq(ElderOutingRecord::getOrgId, orgId)
            .in(ElderOutingRecord::getElderId, elderIds)
            .eq(targetElderId != null, ElderOutingRecord::getElderId, targetElderId)
            .orderByDesc(ElderOutingRecord::getOutingDate)
            .last("LIMIT 30"));

    for (ElderOutingRecord record : outings) {
      FamilyPortalModels.OutingRecordItem item = new FamilyPortalModels.OutingRecordItem();
      item.setId(record.getId());
      item.setElderName(defaultText(record.getElderName(), "未命名老人"));
      item.setStartTime(record.getOutingDate() == null ? "" : record.getOutingDate().format(DATE_FMT));
      item.setReason(defaultText(record.getReason(), "外出"));
      item.setCompanion(defaultText(record.getCompanion(), "护理人员"));
      item.setDestination("院外活动");
      item.setReturnTime(record.getActualReturnTime() == null ? "待返院" : record.getActualReturnTime().format(DATETIME_FMT));
      item.setStatus("OUT".equalsIgnoreCase(record.getStatus()) ? "外出中" : "已返院");
      items.add(item);
    }

    List<ElderMedicalOutingRecord> medicalOutings = elderMedicalOutingRecordMapper.selectList(
        Wrappers.lambdaQuery(ElderMedicalOutingRecord.class)
            .eq(ElderMedicalOutingRecord::getIsDeleted, 0)
            .eq(ElderMedicalOutingRecord::getOrgId, orgId)
            .in(ElderMedicalOutingRecord::getElderId, elderIds)
            .eq(targetElderId != null, ElderMedicalOutingRecord::getElderId, targetElderId)
            .orderByDesc(ElderMedicalOutingRecord::getOutingDate)
            .last("LIMIT 30"));

    for (ElderMedicalOutingRecord record : medicalOutings) {
      FamilyPortalModels.OutingRecordItem item = new FamilyPortalModels.OutingRecordItem();
      item.setId(record.getId());
      item.setElderName(defaultText(record.getElderName(), "未命名老人"));
      item.setStartTime(record.getOutingDate() == null ? "" : record.getOutingDate().format(DATE_FMT));
      item.setReason(defaultText(record.getReason(), "就医复查"));
      item.setCompanion(defaultText(record.getCompanion(), "责任护士"));
      item.setDestination(defaultText(record.getHospitalName(), "医院") + " " + defaultText(record.getDepartment(), ""));
      item.setReturnTime(record.getActualReturnTime() == null ? "待返院" : record.getActualReturnTime().format(DATETIME_FMT));
      item.setStatus("OUT".equalsIgnoreCase(record.getStatus()) ? "外出中" : "已返院");
      items.add(item);
    }

    items.sort(Comparator.comparing(FamilyPortalModels.OutingRecordItem::getStartTime, Comparator.nullsLast(String::compareTo)).reversed());
    return items;
  }

  @Override
  public List<FamilyPortalModels.EmergencyContactItem> listEmergencyContacts(Long orgId, Long familyUserId) {
    Org org = orgMapper.selectById(orgId);
    List<FamilyPortalModels.EmergencyContactItem> contacts = new ArrayList<>();
    boolean needMask = shouldMaskSensitive(orgId, familyUserId);

    FamilyPortalModels.EmergencyContactItem nurse = new FamilyPortalModels.EmergencyContactItem();
    nurse.setId(1L);
    nurse.setRole("责任护士");
    nurse.setName(defaultText(org == null ? null : org.getContactName(), "值班护士"));
    nurse.setPhone(maskPhone(defaultText(org == null ? null : org.getContactPhone(), "13800138000"), needMask));
    nurse.setDuty("白班 08:00-20:00");
    contacts.add(nurse);

    FamilyPortalModels.EmergencyContactItem manager = new FamilyPortalModels.EmergencyContactItem();
    manager.setId(2L);
    manager.setRole("生活管家");
    manager.setName("院务管家");
    manager.setPhone(maskPhone(defaultText(org == null ? null : org.getContactPhone(), "13800138001"), needMask));
    manager.setDuty("全时段支持");
    contacts.add(manager);

    FamilyPortalModels.EmergencyContactItem service = new FamilyPortalModels.EmergencyContactItem();
    service.setId(3L);
    service.setRole("客服中心");
    service.setName(defaultText(org == null ? null : org.getOrgName(), "机构客服"));
    service.setPhone(maskPhone(defaultText(org == null ? null : org.getContactPhone(), "4008009000"), needMask));
    service.setDuty("7x24 在线");
    contacts.add(service);

    return contacts;
  }

  @Override
  public FamilyPortalModels.PaymentSummaryResponse getPaymentSummary(Long orgId, Long familyUserId, Long elderId, String month) {
    List<Long> elderIds = extractElderIds(listBoundRelations(orgId, familyUserId));
    Long targetElderId = resolveTargetElderId(elderIds, elderId);
    String billMonth = (month == null || month.isBlank()) ? currentBillMonth() : month;

    ElderProfile elder = elderMapper.selectById(targetElderId);
    BillMonthly monthly = billMonthlyMapper.selectOne(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(BillMonthly::getOrgId, orgId)
            .eq(BillMonthly::getElderId, targetElderId)
            .eq(BillMonthly::getBillMonth, billMonth)
            .last("LIMIT 1"));

    FamilyPortalModels.PaymentSummaryResponse response = new FamilyPortalModels.PaymentSummaryResponse();
    response.setElderName(elder == null ? "" : elder.getFullName());
    response.setMonth(billMonth);
    response.setTotal(monthly == null ? BigDecimal.ZERO : safeDecimal(monthly.getTotalAmount()));
    response.setPaid(monthly == null ? BigDecimal.ZERO : safeDecimal(monthly.getPaidAmount()));
    response.setOutstanding(monthly == null ? BigDecimal.ZERO : safeDecimal(monthly.getOutstandingAmount()));

    ElderAccount account = elderAccountMapper.selectOne(
        Wrappers.lambdaQuery(ElderAccount.class)
            .eq(ElderAccount::getIsDeleted, 0)
            .eq(ElderAccount::getOrgId, orgId)
            .eq(ElderAccount::getElderId, targetElderId)
            .last("LIMIT 1"));
    response.setAccountBalance(account == null ? BigDecimal.ZERO : safeDecimal(account.getBalance()));
    String stateKey = "elder:" + targetElderId;
    boolean defaultAutoPay = account != null && Integer.valueOf(1).equals(account.getStatus());
    response.setAutoPayEnabled(readBooleanState(orgId, familyUserId, STATE_AUTO_PAY, stateKey, defaultAutoPay));

    BillDetailResponse detail = billService.getBillDetail(targetElderId, billMonth);
    if (detail.getItems() != null) {
      for (BillItemDetail billItem : detail.getItems()) {
        FamilyPortalModels.PaymentDetailItem item = new FamilyPortalModels.PaymentDetailItem();
        item.setItem(defaultText(billItem.getItemName(), billItem.getItemType()));
        item.setAmount(safeDecimal(billItem.getAmount()));
        response.getDetails().add(item);
      }
    }
    if (response.getDetails().isEmpty()) {
      FamilyPortalModels.PaymentDetailItem item = new FamilyPortalModels.PaymentDetailItem();
      item.setItem("月度费用");
      item.setAmount(response.getTotal());
      response.getDetails().add(item);
    }
    return response;
  }

  @Override
  public FamilyPortalModels.PaymentGuardResponse getPaymentGuard(Long orgId, Long familyUserId, Long elderId) {
    FamilyPortalModels.PaymentSummaryResponse summary = getPaymentSummary(orgId, familyUserId, elderId, currentBillMonth());
    List<FamilyPortalModels.RechargeOrderItem> orders = listRechargeOrders(orgId, familyUserId, elderId, 1, 20);

    FamilyPortalModels.PaymentGuardResponse response = new FamilyPortalModels.PaymentGuardResponse();
    response.setElderName(summary.getElderName());
    response.setAccountBalance(safeDecimal(summary.getAccountBalance()));
    response.setOutstanding(safeDecimal(summary.getOutstanding()));
    response.setRecentOrders(orders.stream().limit(8).toList());

    int paid = 0;
    int pending = 0;
    int closed = 0;
    int failed = 0;
    String lastPaidTime = "";
    for (FamilyPortalModels.RechargeOrderItem order : orders) {
      String status = defaultText(order.getStatus(), "");
      if (ORDER_STATUS_PAID.equalsIgnoreCase(status)) {
        paid++;
        if (lastPaidTime.isBlank() && order.getPaidTime() != null && !order.getPaidTime().isBlank()) {
          lastPaidTime = order.getPaidTime();
        }
      } else if (ORDER_STATUS_PREPAY_CREATED.equalsIgnoreCase(status)) {
        pending++;
      } else if (ORDER_STATUS_CLOSED.equalsIgnoreCase(status)) {
        closed++;
      } else if (ORDER_STATUS_FAILED.equalsIgnoreCase(status)) {
        failed++;
      }
    }
    response.setPaidOrderCount(paid);
    response.setPendingOrderCount(pending);
    response.setClosedOrderCount(closed);
    response.setFailedOrderCount(failed);
    response.setLastPaidTime(lastPaidTime);
    response.setTips(buildPaymentGuardTips(response, summary.getAutoPayEnabled()));
    return response;
  }

  @Override
  public List<FamilyPortalModels.PaymentHistoryItem> listPaymentHistory(Long orgId, Long familyUserId, Long elderId) {
    List<Long> elderIds = extractElderIds(listBoundRelations(orgId, familyUserId));
    Long targetElderId = resolveTargetElderId(elderIds, elderId);
    List<BillMonthly> bills = billMonthlyMapper.selectList(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(BillMonthly::getOrgId, orgId)
            .eq(BillMonthly::getElderId, targetElderId)
            .orderByDesc(BillMonthly::getBillMonth)
            .last("LIMIT 12"));

    return bills.stream().map(bill -> {
      FamilyPortalModels.PaymentHistoryItem item = new FamilyPortalModels.PaymentHistoryItem();
      item.setMonth(defaultText(bill.getBillMonth(), ""));
      item.setTotal(safeDecimal(bill.getTotalAmount()));
      item.setPaid(safeDecimal(bill.getPaidAmount()));
      item.setStatus(mapBillStatus(bill.getStatus()));
      return item;
    }).toList();
  }

  @Override
  @Transactional
  public FamilyPortalModels.PaymentRechargeResponse recharge(Long orgId, Long familyUserId,
      FamilyPortalModels.PaymentRechargeRequest request) {
    List<Long> elderIds = extractElderIds(listBoundRelations(orgId, familyUserId));
    Long targetElderId = resolveTargetElderId(elderIds, request.getElderId());
    ElderAccount account = getOrCreateElderAccount(orgId, targetElderId, familyUserId);

    BigDecimal amount = safeDecimal(request.getAmount());
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("充值金额必须大于0");
    }

    BigDecimal newBalance = safeDecimal(account.getBalance()).add(amount);
    account.setBalance(newBalance);
    elderAccountMapper.updateById(account);

    ElderAccountLog log = new ElderAccountLog();
    log.setTenantId(orgId);
    log.setOrgId(orgId);
    log.setElderId(targetElderId);
    log.setAccountId(account.getId());
    log.setAmount(amount);
    log.setBalanceAfter(newBalance);
    log.setDirection("CREDIT");
    log.setSourceType("FAMILY_RECHARGE");
    log.setRemark(buildRechargeRemark(request));
    log.setCreatedBy(familyUserId);
    elderAccountLogMapper.insert(log);

    FamilyPortalModels.PaymentRechargeResponse response = new FamilyPortalModels.PaymentRechargeResponse();
    response.setLogId(log.getId());
    response.setOutTradeNo("FAM" + System.currentTimeMillis() + defaultText(String.valueOf(log.getId()), ""));
    response.setRechargeAmount(amount);
    response.setAccountBalance(newBalance);
    response.setMessage("充值成功，余额已更新");

    String elderName = resolveElderName(targetElderId);
    notifyFamilySafe(orgId, familyUserId, targetElderId, "PAYMENT_RECHARGE_MANUAL", "normal",
        "充值入账成功",
        "已为" + elderName + "充值 ¥" + amount.stripTrailingZeros().toPlainString()
            + "，当前余额 ¥" + newBalance.stripTrailingZeros().toPlainString(),
        "pages/payment/index",
        Map.of(
            "elderName", elderName,
            "amount", amount.stripTrailingZeros().toPlainString(),
            "balance", newBalance.stripTrailingZeros().toPlainString()));
    return response;
  }

  @Override
  @Transactional
  public FamilyPortalModels.WechatRechargePrepayResponse createWechatRechargePrepay(Long orgId, Long familyUserId,
      FamilyPortalModels.WechatRechargePrepayRequest request) {
    FamilyPortalProperties.WechatPay wechatPay = familyPortalProperties.getWechatPay();
    if (wechatPay == null || !wechatPay.isEnabled()) {
      throw new IllegalStateException("微信支付未启用，请联系机构管理员配置");
    }

    List<Long> elderIds = ensureBoundElderIds(extractElderIds(listBoundRelations(orgId, familyUserId)));
    Long targetElderId = resolveTargetElderId(elderIds, request.getElderId());
    BigDecimal amount = safeDecimal(request.getAmount());
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("充值金额必须大于0");
    }

    String payerOpenId = resolvePayerOpenId(wechatPay, request);
    persistFamilyOpenId(orgId, familyUserId, payerOpenId);
    String outTradeNo = buildWechatOutTradeNo();
    FamilyRechargeOrder order = new FamilyRechargeOrder();
    order.setTenantId(orgId);
    order.setOrgId(orgId);
    order.setFamilyUserId(familyUserId);
    order.setElderId(targetElderId);
    order.setOutTradeNo(outTradeNo);
    order.setChannel("WECHAT_JSAPI");
    order.setStatus(ORDER_STATUS_INIT);
    order.setAmount(amount);
    order.setCurrency("CNY");
    order.setPayerOpenId(payerOpenId);
    order.setRemark(defaultText(request.getRemark(), "家属端微信充值"));
    order.setCreatedBy(familyUserId);
    familyRechargeOrderMapper.insert(order);

    try {
      int amountFen = toFen(amount);
      Map<String, Object> amountObj = new LinkedHashMap<>();
      amountObj.put("total", amountFen);
      amountObj.put("currency", "CNY");

      Map<String, Object> payerObj = new LinkedHashMap<>();
      payerObj.put("openid", payerOpenId);

      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("appid", requireText(wechatPay.getAppId(), "未配置微信支付 appId"));
      payload.put("mchid", requireText(wechatPay.getMerchantId(), "未配置微信支付商户号"));
      payload.put("description", defaultText(request.getDescription(), "家属端账户充值"));
      payload.put("out_trade_no", outTradeNo);
      payload.put("notify_url", requireText(wechatPay.getNotifyUrl(), "未配置微信支付回调地址"));
      payload.put("attach", "orgId=" + orgId + ";familyUserId=" + familyUserId + ";elderId=" + targetElderId);
      payload.put("amount", amountObj);
      payload.put("payer", payerObj);

      String body = writeJson(payload);
      JsonNode prepayResp = requestWechatApi(wechatPay, HttpMethod.POST, wechatPay.getPrepayUrl(), body);
      String prepayId = defaultText(prepayResp.path("prepay_id").asText(""), null);
      if (prepayId == null) {
        throw new IllegalStateException("微信预下单失败：" + defaultText(prepayResp.path("message").asText(""), "未返回prepay_id"));
      }

      order.setPrepayId(prepayId);
      order.setStatus(ORDER_STATUS_PREPAY_CREATED);
      familyRechargeOrderMapper.updateById(order);

      String nonceStr = randomNonce();
      String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
      String payPackage = "prepay_id=" + prepayId;
      String paySign = signJsapiPay(wechatPay, timeStamp, nonceStr, payPackage);

      FamilyPortalModels.WechatRechargePrepayResponse response = new FamilyPortalModels.WechatRechargePrepayResponse();
      response.setOutTradeNo(outTradeNo);
      response.setRechargeAmount(amount);
      response.setAmountFen(amountFen);
      response.setAppId(wechatPay.getAppId());
      response.setTimeStamp(timeStamp);
      response.setNonceStr(nonceStr);
      response.setPayPackage(payPackage);
      response.setSignType("RSA");
      response.setPaySign(paySign);
      response.setStatus(ORDER_STATUS_PREPAY_CREATED);
      response.setMessage("预下单成功");
      return response;
    } catch (Exception ex) {
      order.setStatus(ORDER_STATUS_FAILED);
      order.setRemark(defaultText(order.getRemark(), "") + " | " + defaultText(ex.getMessage(), "预下单失败"));
      familyRechargeOrderMapper.updateById(order);
      createRechargeAbnormalTodo(order, defaultText(ex.getMessage(), "微信预下单失败"));
      throw ex instanceof RuntimeException ? (RuntimeException) ex : new IllegalStateException("微信预下单失败", ex);
    }
  }

  @Override
  @Transactional
  public FamilyPortalModels.WechatNotifyBindResponse bindWechatNotifyOpenId(Long orgId, Long familyUserId,
      FamilyPortalModels.WechatNotifyBindRequest request) {
    FamilyPortalModels.WechatNotifyBindRequest bindRequest = request == null
        ? new FamilyPortalModels.WechatNotifyBindRequest()
        : request;
    String openId = defaultText(bindRequest.getOpenId(), null);
    if (openId == null) {
      String loginCode = defaultText(bindRequest.getLoginCode(), null);
      if (loginCode == null) {
        throw new IllegalArgumentException("缺少微信登录凭证 loginCode");
      }
      openId = resolveOpenIdByLoginCode(resolveWechatNotifyAppId(), resolveWechatNotifyAppSecret(), loginCode);
    }

    persistFamilyOpenId(orgId, familyUserId, openId);
    FamilyPortalModels.WechatNotifyBindResponse response = new FamilyPortalModels.WechatNotifyBindResponse();
    response.setBound(true);
    response.setOpenId(openId);
    response.setMessage("微信通知接收绑定成功");
    return response;
  }

  @Override
  public List<FamilyPortalModels.RechargeOrderItem> listRechargeOrders(Long orgId, Long familyUserId, Long elderId,
      int pageNo, int pageSize) {
    List<Long> elderIds = ensureBoundElderIds(extractElderIds(listBoundRelations(orgId, familyUserId)));
    Long targetElderId = elderId == null ? null : resolveTargetElderId(elderIds, elderId);

    Page<FamilyRechargeOrder> page = familyRechargeOrderMapper.selectPage(new Page<>(Math.max(pageNo, 1), Math.max(pageSize, 1)),
        Wrappers.lambdaQuery(FamilyRechargeOrder.class)
            .eq(FamilyRechargeOrder::getIsDeleted, 0)
            .eq(FamilyRechargeOrder::getOrgId, orgId)
            .eq(FamilyRechargeOrder::getFamilyUserId, familyUserId)
            .in(FamilyRechargeOrder::getElderId, elderIds)
            .eq(targetElderId != null, FamilyRechargeOrder::getElderId, targetElderId)
            .orderByDesc(FamilyRechargeOrder::getCreateTime));

    Map<Long, ElderProfile> elderMap = loadElderMap(orgId, elderIds);
    List<FamilyPortalModels.RechargeOrderItem> items = page.getRecords().stream()
        .map(order -> toRechargeOrderItem(order, elderMap.get(order.getElderId())))
        .toList();
    return items;
  }

  @Override
  @Transactional
  public FamilyPortalModels.RechargeOrderItem getRechargeOrder(Long orgId, Long familyUserId, String outTradeNo) {
    if (outTradeNo == null || outTradeNo.isBlank()) {
      return null;
    }
    FamilyRechargeOrder order = familyRechargeOrderMapper.selectOne(
        Wrappers.lambdaQuery(FamilyRechargeOrder.class)
            .eq(FamilyRechargeOrder::getIsDeleted, 0)
            .eq(FamilyRechargeOrder::getOrgId, orgId)
            .eq(FamilyRechargeOrder::getFamilyUserId, familyUserId)
            .eq(FamilyRechargeOrder::getOutTradeNo, outTradeNo.trim())
            .last("LIMIT 1"));
    if (order == null) {
      return null;
    }

    if (!ORDER_STATUS_PAID.equalsIgnoreCase(order.getStatus())) {
      syncWechatOrderStatus(order);
      order = familyRechargeOrderMapper.selectById(order.getId());
    }

    ElderProfile elder = elderMapper.selectById(order.getElderId());
    return toRechargeOrderItem(order, elder);
  }

  @Override
  @Transactional
  public boolean handleWechatPayNotify(String payload, Map<String, String> headers) {
    if (payload == null || payload.isBlank()) {
      return false;
    }
    try {
      JsonNode root = objectMapper.readTree(payload);
      JsonNode biz = resolveWechatNotifyBiz(root);
      if (biz == null || biz.isMissingNode()) {
        return false;
      }

      String outTradeNo = defaultText(biz.path("out_trade_no").asText(""), null);
      if (outTradeNo == null) {
        return false;
      }
      FamilyRechargeOrder order = familyRechargeOrderMapper.selectOne(
          Wrappers.lambdaQuery(FamilyRechargeOrder.class)
              .eq(FamilyRechargeOrder::getIsDeleted, 0)
              .eq(FamilyRechargeOrder::getOutTradeNo, outTradeNo)
              .last("LIMIT 1"));
      if (order == null) {
        return true;
      }

      if (!verifyWechatNotifyHeaders(headers, payload)) {
        order.setStatus(ORDER_STATUS_FAILED);
        order.setNotifyPayload(payload);
        order.setRemark("微信回调签名校验失败");
        familyRechargeOrderMapper.updateById(order);
        createRechargeAbnormalTodo(order, "签名校验失败，系统拒绝入账");
        return false;
      }
      if (isWechatNotifyReplay(headers, payload)) {
        return true;
      }

      String tradeState = defaultText(biz.path("trade_state").asText(""), "UNKNOWN");
      String wxTransactionId = defaultText(biz.path("transaction_id").asText(""), null);
      int paidFen = biz.path("amount").path("total").asInt(-1);
      LocalDateTime paidAt = parseWechatTime(biz.path("success_time").asText(null));

      if ("SUCCESS".equalsIgnoreCase(tradeState)) {
        applyRechargePaid(order, wxTransactionId, paidFen, paidAt, payload);
        return true;
      }

      if ("CLOSED".equalsIgnoreCase(tradeState) || "REVOKED".equalsIgnoreCase(tradeState) || "PAYERROR".equalsIgnoreCase(tradeState)) {
        if (!ORDER_STATUS_PAID.equalsIgnoreCase(order.getStatus())) {
          order.setStatus(ORDER_STATUS_CLOSED);
          order.setNotifyPayload(payload);
          order.setRemark("微信支付状态:" + tradeState);
          familyRechargeOrderMapper.updateById(order);
          createRechargeAbnormalTodo(order, "微信返回关闭状态：" + tradeState);
        }
      }
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  @Override
  public boolean updateAutoPay(Long orgId, Long familyUserId, Long elderId, boolean enabled,
      Boolean authorizeConfirmed) {
    List<Long> elderIds = extractElderIds(listBoundRelations(orgId, familyUserId));
    if (elderIds.isEmpty()) {
      throw new IllegalArgumentException("请先绑定老人信息");
    }
    Long targetElderId = resolveTargetElderId(elderIds, elderId);
    String stateKey = "elder:" + targetElderId;
    Map<String, Object> current = readStateMap(orgId, familyUserId, STATE_AUTO_PAY, stateKey);
    boolean existedAuthorized = readBoolean(current, "authorized", false);
    String authorizedAt = defaultText(readText(current, "authorizedAt"), "");
    if (enabled && !existedAuthorized && !Boolean.TRUE.equals(authorizeConfirmed)) {
      throw new IllegalArgumentException("开启自动扣费前请先确认授权");
    }
    if (enabled && !existedAuthorized) {
      existedAuthorized = true;
      authorizedAt = LocalDateTime.now().format(DATETIME_FMT);
    }
    Map<String, Object> state = new LinkedHashMap<>();
    state.put("enabled", enabled);
    state.put("authorized", existedAuthorized);
    state.put("authorizedAt", authorizedAt);
    state.put("updatedAt", LocalDateTime.now().format(DATETIME_FMT));
    upsertState(orgId, familyUserId, STATE_AUTO_PAY, stateKey, state);
    return enabled;
  }

  @Override
  @Transactional
  public int executeAutoPaySettlements() {
    FamilyPortalProperties.Recharge recharge = familyPortalProperties.getRecharge();
    if (recharge == null || !recharge.isAutoPaySettleEnabled()) {
      return 0;
    }
    int batchSize = recharge.getAutoPaySettleBatchSize() <= 0 ? 200 : recharge.getAutoPaySettleBatchSize();
    List<FamilyPortalState> states = familyPortalStateMapper.selectList(
        Wrappers.lambdaQuery(FamilyPortalState.class)
            .eq(FamilyPortalState::getIsDeleted, 0)
            .eq(FamilyPortalState::getCategory, STATE_AUTO_PAY)
            .orderByDesc(FamilyPortalState::getUpdateTime)
            .last("LIMIT " + Math.max(batchSize, 1)));
    if (states.isEmpty()) {
      return 0;
    }

    int settledCount = 0;
    for (FamilyPortalState state : states) {
      if (state == null || state.getOrgId() == null || state.getFamilyUserId() == null) {
        continue;
      }
      Long elderId = parseElderIdFromAutoPayState(state.getBizKey());
      if (elderId == null) {
        continue;
      }
      Map<String, Object> value = parseStateValueMap(state.getValueJson());
      if (!readBoolean(value, "enabled", false) || !readBoolean(value, "authorized", false)) {
        continue;
      }
      settledCount += settleAutoPayForElder(state.getOrgId(), state.getFamilyUserId(), elderId);
    }
    return settledCount;
  }

  @Override
  @Transactional
  public int closeExpiredRechargeOrders() {
    FamilyPortalProperties.Recharge recharge = familyPortalProperties.getRecharge();
    if (recharge == null || !recharge.isAutoCloseEnabled()) {
      return 0;
    }
    int batchSize = recharge.getAutoCloseBatchSize() <= 0 ? 200 : recharge.getAutoCloseBatchSize();
    int timeoutMinutes = recharge.getPrepayTimeoutMinutes() <= 0 ? 30 : recharge.getPrepayTimeoutMinutes();
    LocalDateTime expireBefore = LocalDateTime.now().minusMinutes(timeoutMinutes);

    List<FamilyRechargeOrder> staleOrders = familyRechargeOrderMapper.selectList(
        Wrappers.lambdaQuery(FamilyRechargeOrder.class)
            .eq(FamilyRechargeOrder::getIsDeleted, 0)
            .eq(FamilyRechargeOrder::getStatus, ORDER_STATUS_PREPAY_CREATED)
            .le(FamilyRechargeOrder::getCreateTime, expireBefore)
            .orderByAsc(FamilyRechargeOrder::getCreateTime)
            .last("LIMIT " + Math.max(batchSize, 1)));
    if (staleOrders.isEmpty()) {
      return 0;
    }

    int closed = 0;
    for (FamilyRechargeOrder order : staleOrders) {
      syncWechatOrderStatus(order);
      FamilyRechargeOrder latest = familyRechargeOrderMapper.selectById(order.getId());
      if (latest == null || ORDER_STATUS_PAID.equalsIgnoreCase(latest.getStatus())
          || ORDER_STATUS_CLOSED.equalsIgnoreCase(latest.getStatus())) {
        continue;
      }
      int updated = familyRechargeOrderMapper.update(null, Wrappers.lambdaUpdate(FamilyRechargeOrder.class)
          .set(FamilyRechargeOrder::getStatus, ORDER_STATUS_CLOSED)
          .set(FamilyRechargeOrder::getRemark, "超时自动关闭（" + timeoutMinutes + "分钟未支付）")
          .eq(FamilyRechargeOrder::getId, latest.getId())
          .eq(FamilyRechargeOrder::getIsDeleted, 0)
          .eq(FamilyRechargeOrder::getStatus, ORDER_STATUS_PREPAY_CREATED));
      if (updated > 0) {
        closed += updated;
        latest.setStatus(ORDER_STATUS_CLOSED);
        createRechargeAbnormalTodo(latest, "订单超时自动关闭，请家属重新发起充值");
      }
    }
    return closed;
  }

  @Override
  public List<FamilyPortalModels.ServiceCatalogItem> listServiceCatalog(Long orgId) {
    List<ServiceItem> list = serviceItemMapper.selectList(
        Wrappers.lambdaQuery(ServiceItem.class)
            .eq(ServiceItem::getIsDeleted, 0)
            .eq(ServiceItem::getOrgId, orgId)
            .eq(ServiceItem::getEnabled, 1)
            .orderByDesc(ServiceItem::getUpdateTime)
            .last("LIMIT 80"));

    return list.stream().map(item -> {
      FamilyPortalModels.ServiceCatalogItem model = new FamilyPortalModels.ServiceCatalogItem();
      model.setId(item.getId());
      model.setCategory(defaultText(item.getCategory(), "护理服务"));
      model.setName(defaultText(item.getName(), "标准服务"));
      model.setPrice(resolveServicePrice(item));
      model.setUnit("次");
      model.setDesc(defaultText(item.getRemark(), "机构标准化服务项目"));
      return model;
    }).toList();
  }

  @Override
  public List<FamilyPortalModels.ServiceOrderItem> listServiceOrders(Long orgId, Long familyUserId, Long elderId) {
    List<Long> elderIds = ensureBoundElderIds(extractElderIds(listBoundRelations(orgId, familyUserId)));
    Long targetElderId = elderId == null ? null : resolveTargetElderId(elderIds, elderId);

    List<ServiceBooking> bookings = serviceBookingMapper.selectList(
        Wrappers.lambdaQuery(ServiceBooking.class)
            .eq(ServiceBooking::getIsDeleted, 0)
            .eq(ServiceBooking::getOrgId, orgId)
            .in(ServiceBooking::getElderId, elderIds)
            .eq(targetElderId != null, ServiceBooking::getElderId, targetElderId)
            .orderByDesc(ServiceBooking::getBookingTime)
            .last("LIMIT 30"));

    Set<Long> serviceIds = bookings.stream()
        .map(ServiceBooking::getServiceItemId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());

    Map<Long, ServiceItem> serviceMap = serviceIds.isEmpty() ? Map.of() : serviceItemMapper.selectBatchIds(serviceIds)
        .stream().collect(Collectors.toMap(ServiceItem::getId, item -> item, (a, b) -> a));

    return bookings.stream().map(booking -> {
      ServiceItem service = serviceMap.get(booking.getServiceItemId());
      FamilyPortalModels.ServiceOrderItem item = new FamilyPortalModels.ServiceOrderItem();
      item.setId(booking.getId());
      item.setName(service == null ? "服务预定" : defaultText(service.getName(), "服务预定"));
      item.setAmount(resolveServicePrice(service));
      item.setStatus(defaultText(booking.getStatus(), "BOOKED"));
      item.setDate(booking.getBookingTime() == null ? "" : booking.getBookingTime().toLocalDate().format(DATE_FMT));
      return item;
    }).toList();
  }

  @Override
  @Transactional
  public FamilyPortalModels.ServiceOrderItem createServiceOrder(Long orgId, Long familyUserId,
      FamilyPortalModels.ServiceOrderCreateRequest request) {
    List<Long> elderIds = extractElderIds(listBoundRelations(orgId, familyUserId));
    Long targetElderId = resolveTargetElderId(elderIds, request.getElderId());

    ServiceItem serviceItem = serviceItemMapper.selectById(request.getServiceId());
    if (serviceItem == null || !Objects.equals(serviceItem.getOrgId(), orgId) || Integer.valueOf(1).equals(serviceItem.getIsDeleted())) {
      throw new IllegalArgumentException("服务项不存在");
    }

    ServiceBooking booking = new ServiceBooking();
    booking.setTenantId(orgId);
    booking.setOrgId(orgId);
    booking.setElderId(targetElderId);
    booking.setServiceItemId(serviceItem.getId());
    booking.setBookingTime(LocalDateTime.now().plusDays(1));
    booking.setExpectedDuration(serviceItem.getDefaultDuration() == null ? 60 : serviceItem.getDefaultDuration());
    booking.setSource("FAMILY_APP");
    booking.setStatus("BOOKED");
    booking.setRemark(request.getRemark());
    booking.setCreatedBy(familyUserId);
    serviceBookingMapper.insert(booking);

    FamilyPortalModels.ServiceOrderItem response = new FamilyPortalModels.ServiceOrderItem();
    response.setId(booking.getId());
    response.setName(defaultText(serviceItem.getName(), "服务预定"));
    response.setAmount(resolveServicePrice(serviceItem));
    response.setStatus(defaultText(booking.getStatus(), "BOOKED"));
    response.setDate(booking.getBookingTime().toLocalDate().format(DATE_FMT));
    return response;
  }

  @Override
  public List<FamilyPortalModels.MallProductItem> listMallProducts(Long orgId, Long familyUserId, String keyword,
      String category, int pageNo, int pageSize) {
    ensureBoundElderIds(extractElderIds(listBoundRelations(orgId, familyUserId)));
    int safePageNo = Math.max(pageNo, 1);
    int safePageSize = Math.min(Math.max(pageSize, 1), 100);

    var wrapper = Wrappers.lambdaQuery(Product.class)
        .eq(Product::getIsDeleted, 0)
        .eq(Product::getOrgId, orgId)
        .eq(Product::getStatus, 1)
        .eq(Product::getMallEnabled, 1);
    if (hasText(category)) {
      wrapper.eq(Product::getCategory, category.trim());
    }
    if (hasText(keyword)) {
      String kw = keyword.trim();
      wrapper.and(w -> w.like(Product::getProductName, kw)
          .or().like(Product::getProductCode, kw));
    }
    wrapper.orderByDesc(Product::getUpdateTime).orderByDesc(Product::getCreateTime);

    Page<Product> page = productMapper.selectPage(new Page<>(safePageNo, safePageSize), wrapper);
    List<Long> productIds = page.getRecords().stream()
        .map(Product::getId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<Long, Integer> stockMap = new HashMap<>();
    if (!productIds.isEmpty()) {
      List<InventoryBatch> batches = inventoryBatchMapper.selectList(
          Wrappers.lambdaQuery(InventoryBatch.class)
              .eq(InventoryBatch::getIsDeleted, 0)
              .eq(InventoryBatch::getOrgId, orgId)
              .in(InventoryBatch::getProductId, productIds));
      for (InventoryBatch batch : batches) {
        if (batch == null || batch.getProductId() == null) {
          continue;
        }
        stockMap.merge(batch.getProductId(), safeInt(batch.getQuantity()), Integer::sum);
      }
    }

    return page.getRecords().stream().map(product -> {
      FamilyPortalModels.MallProductItem item = new FamilyPortalModels.MallProductItem();
      item.setId(product.getId());
      item.setProductCode(defaultText(product.getProductCode(), ""));
      item.setProductName(defaultText(product.getProductName(), "未命名商品"));
      item.setCategory(defaultText(product.getCategory(), "未分类"));
      item.setUnit(defaultText(product.getUnit(), "件"));
      item.setPrice(safeDecimal(product.getPrice()));
      item.setPointsPrice(safeInt(product.getPointsPrice()));
      int stock = stockMap.getOrDefault(product.getId(), 0);
      item.setCurrentStock(stock);
      int safetyStock = safeInt(product.getSafetyStock());
      if (stock <= 0) {
        item.setStockStatus("OUT");
        item.setStatusText("库存不足");
      } else if (safetyStock > 0 && stock <= safetyStock) {
        item.setStockStatus("LOW");
        item.setStatusText("库存紧张");
      } else {
        item.setStockStatus("OK");
        item.setStatusText("可下单");
      }
      item.setBusinessDomain(defaultText(product.getBusinessDomain(), "BOTH"));
      item.setItemType(defaultText(product.getItemType(), "CONSUMABLE"));
      return item;
    }).toList();
  }

  @Override
  public FamilyPortalModels.MallOrderPreviewResponse previewMallOrder(Long orgId, Long familyUserId,
      FamilyPortalModels.MallOrderPreviewRequest request) {
    Long targetElderId = resolveTargetElderId(
        ensureBoundElderIds(extractElderIds(listBoundRelations(orgId, familyUserId))),
        request.getElderId());
    OrderPreviewRequest payload = new OrderPreviewRequest();
    payload.setElderId(targetElderId);
    payload.setProductId(request.getProductId());
    payload.setQty(request.getQty());
    OrderPreviewResponse preview = storeOrderService.preview(payload);
    return toMallOrderPreview(preview);
  }

  @Override
  @Transactional
  public FamilyPortalModels.MallOrderSubmitResponse submitMallOrder(Long orgId, Long familyUserId,
      FamilyPortalModels.MallOrderSubmitRequest request) {
    Long targetElderId = resolveTargetElderId(
        ensureBoundElderIds(extractElderIds(listBoundRelations(orgId, familyUserId))),
        request.getElderId());
    OrderPreviewRequest payload = new OrderPreviewRequest();
    payload.setElderId(targetElderId);
    payload.setProductId(request.getProductId());
    payload.setQty(request.getQty());
    OrderSubmitResponse submit = storeOrderService.submit(payload);
    return toMallOrderSubmit(submit);
  }

  @Override
  public List<FamilyPortalModels.MallOrderItem> listMallOrders(Long orgId, Long familyUserId, Long elderId, int pageNo,
      int pageSize) {
    List<Long> elderIds = ensureBoundElderIds(extractElderIds(listBoundRelations(orgId, familyUserId)));
    Long targetElderId = elderId == null ? null : resolveTargetElderId(elderIds, elderId);
    int safePageNo = Math.max(pageNo, 1);
    int safePageSize = Math.min(Math.max(pageSize, 1), 100);

    Page<StoreOrder> page = storeOrderMapper.selectPage(new Page<>(safePageNo, safePageSize),
        Wrappers.lambdaQuery(StoreOrder.class)
            .eq(StoreOrder::getIsDeleted, 0)
            .eq(StoreOrder::getOrgId, orgId)
            .in(StoreOrder::getElderId, elderIds)
            .eq(targetElderId != null, StoreOrder::getElderId, targetElderId)
            .orderByDesc(StoreOrder::getCreateTime));

    List<StoreOrder> orders = page.getRecords();
    if (orders.isEmpty()) {
      return List.of();
    }
    List<Long> orderIds = orders.stream()
        .map(StoreOrder::getId)
        .filter(Objects::nonNull)
        .toList();
    Map<Long, ElderProfile> elderMap = loadElderMap(orgId, elderIds);
    Map<Long, List<OrderItem>> orderItemMap = orderItemMapper.selectList(
            Wrappers.lambdaQuery(OrderItem.class)
                .eq(OrderItem::getIsDeleted, 0)
                .eq(OrderItem::getOrgId, orgId)
                .in(OrderItem::getOrderId, orderIds)
                .orderByAsc(OrderItem::getCreateTime))
        .stream()
        .collect(Collectors.groupingBy(OrderItem::getOrderId));

    return orders.stream().map(order -> {
      List<OrderItem> lines = orderItemMap.getOrDefault(order.getId(), List.of());
      OrderItem first = lines.isEmpty() ? null : lines.get(0);
      int quantity = lines.stream()
          .map(OrderItem::getQuantity)
          .filter(Objects::nonNull)
          .mapToInt(Integer::intValue)
          .sum();
      String productName = first == null ? "商城商品"
          : defaultText(defaultText(first.getProductNameSnapshot(), first.getProductName()), "商城商品");
      if (lines.size() > 1) {
        productName = productName + " 等" + lines.size() + "件";
      }
      FamilyPortalModels.MallOrderItem item = new FamilyPortalModels.MallOrderItem();
      item.setOrderId(order.getId());
      item.setOrderNo(defaultText(order.getOrderNo(), ""));
      item.setElderId(order.getElderId());
      ElderProfile elder = elderMap.get(order.getElderId());
      item.setElderName(elder == null ? "老人" : defaultText(elder.getFullName(), "老人"));
      item.setProductId(first == null ? null : first.getProductId());
      item.setProductName(productName);
      item.setQuantity(quantity > 0 ? quantity : 1);
      item.setUnitPrice(first == null ? safeDecimal(order.getTotalAmount()) : safeDecimal(first.getUnitPrice()));
      item.setTotalAmount(safeDecimal(order.getTotalAmount()));
      item.setPointsUsed(safeInt(order.getPointsUsed()));
      item.setOrderStatus(order.getOrderStatus());
      item.setOrderStatusText(resolveMallOrderStatusText(order.getOrderStatus()));
      item.setPayStatus(order.getPayStatus());
      item.setPayStatusText(resolveMallPayStatusText(order.getPayStatus()));
      item.setCreateTime(order.getCreateTime() == null ? "" : order.getCreateTime().format(DATETIME_FMT));
      item.setPayTime(order.getPayTime() == null ? "" : order.getPayTime().format(DATETIME_FMT));
      item.setCanCancel(canCancelMallOrder(order));
      item.setCancelHint(resolveCancelMallOrderHint(order));
      item.setCanRefund(canRefundMallOrder(order));
      item.setRefundHint(resolveRefundMallOrderHint(order));
      return item;
    }).toList();
  }

  @Override
  public FamilyPortalModels.MallOrderDetailResponse getMallOrderDetail(Long orgId, Long familyUserId, Long orderId) {
    StoreOrder order = loadAuthorizedMallOrder(orgId, familyUserId, orderId);
    OrderDetailResponse detail = storeOrderService.getOrderDetail(order.getId());

    FamilyPortalModels.MallOrderDetailResponse response = new FamilyPortalModels.MallOrderDetailResponse();
    FamilyPortalModels.MallOrderItem summary = toMallOrderSummary(order);
    if (detail != null && hasText(detail.getElderName())) {
      summary.setElderName(detail.getElderName().trim());
    }

    if (detail != null && detail.getItems() != null) {
      List<FamilyPortalModels.MallOrderLineItem> items = detail.getItems().stream().map(line -> {
        FamilyPortalModels.MallOrderLineItem item = new FamilyPortalModels.MallOrderLineItem();
        item.setProductId(line.getProductId());
        item.setProductName(defaultText(line.getProductName(), "商城商品"));
        item.setQuantity(line.getQuantity());
        item.setUnitPrice(safeDecimal(line.getUnitPrice()));
        item.setAmount(safeDecimal(line.getAmount()));
        return item;
      }).toList();
      response.setItems(items);
      if (!items.isEmpty()) {
        FamilyPortalModels.MallOrderLineItem first = items.get(0);
        int qty = items.stream()
            .map(FamilyPortalModels.MallOrderLineItem::getQuantity)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .sum();
        summary.setProductId(first.getProductId());
        summary.setQuantity(qty > 0 ? qty : 1);
        summary.setUnitPrice(safeDecimal(first.getUnitPrice()));
        String productName = defaultText(first.getProductName(), "商城商品");
        if (items.size() > 1) {
          productName = productName + " 等" + items.size() + "件";
        }
        summary.setProductName(productName);
      }
    }
    response.setSummary(summary);

    if (detail != null && detail.getRiskReasons() != null) {
      response.setRiskReasons(detail.getRiskReasons().stream().map(reason -> {
        FamilyPortalModels.MallOrderRiskItem item = new FamilyPortalModels.MallOrderRiskItem();
        item.setDiseaseName(defaultText(reason.getDiseaseName(), ""));
        item.setTagCode(defaultText(reason.getTagCode(), ""));
        item.setTagName(defaultText(reason.getTagName(), ""));
        return item;
      }).toList());
    }

    if (detail != null && detail.getFifoLogs() != null) {
      response.setFifoLogs(detail.getFifoLogs().stream().map(log -> {
        FamilyPortalModels.MallOrderFifoItem item = new FamilyPortalModels.MallOrderFifoItem();
        item.setBatchNo(defaultText(log.getBatchNo(), ""));
        item.setQuantity(log.getQuantity());
        item.setExpireDate(defaultText(log.getExpireDate(), ""));
        return item;
      }).toList());
    }
    return response;
  }

  @Override
  @Transactional
  public FamilyPortalModels.MallOrderActionResponse cancelMallOrder(Long orgId, Long familyUserId, Long orderId,
      FamilyPortalModels.MallOrderActionRequest request) {
    StoreOrder order = loadAuthorizedMallOrder(orgId, familyUserId, orderId);
    if (!canCancelMallOrder(order)) {
      throw new IllegalArgumentException(resolveCancelMallOrderHint(order));
    }
    OrderCancelRequest payload = new OrderCancelRequest();
    payload.setOrderId(order.getId());
    storeOrderService.cancel(payload);
    StoreOrder latest = storeOrderMapper.selectById(order.getId());
    return toMallOrderActionResponse(latest == null ? order : latest, "CANCEL", "订单已取消");
  }

  @Override
  @Transactional
  public FamilyPortalModels.MallOrderActionResponse refundMallOrder(Long orgId, Long familyUserId, Long orderId,
      FamilyPortalModels.MallOrderActionRequest request) {
    StoreOrder order = loadAuthorizedMallOrder(orgId, familyUserId, orderId);
    if (!canRefundMallOrder(order)) {
      throw new IllegalArgumentException(resolveRefundMallOrderHint(order));
    }
    OrderRefundRequest payload = new OrderRefundRequest();
    payload.setOrderId(order.getId());
    payload.setReason(defaultText(request == null ? null : request.getReason(), "家属端申请退款"));
    storeOrderService.refund(payload);
    StoreOrder latest = storeOrderMapper.selectById(order.getId());
    return toMallOrderActionResponse(latest == null ? order : latest, "REFUND", "退款申请已处理");
  }

  @Override
  @Transactional
  public void submitFeedback(Long orgId, Long familyUserId, FamilyPortalModels.FeedbackRequest request) {
    FamilyUser familyUser = familyUserMapper.selectById(familyUserId);
    String feedbackType = normalizeFeedbackType(request == null ? null : request.getFeedbackType());
    OaSuggestion suggestion = new OaSuggestion();
    suggestion.setTenantId(orgId);
    suggestion.setOrgId(orgId);
    String title = defaultText(request.getServiceType(),
        "COMPLAINT".equals(feedbackType) ? "投诉建议" : "服务反馈");
    String scoreText = request.getScore() == null ? "未评分" : request.getScore() + "星";
    suggestion.setContent("[" + feedbackType + "][" + title + "] 评分:" + scoreText + "\n"
        + defaultText(request.getContent(), "").trim());
    suggestion.setProposerName(familyUser == null ? "家属" : defaultText(familyUser.getRealName(), familyUser.getPhone()));
    suggestion.setContact(defaultText(request.getContact(), familyUser == null ? "" : familyUser.getPhone()));
    suggestion.setStatus("PENDING");
    suggestion.setCreatedBy(familyUserId);
    oaSuggestionMapper.insert(suggestion);
  }

  @Override
  public List<FamilyPortalModels.FeedbackRecordItem> listFeedbackRecords(Long orgId, Long familyUserId,
      int pageNo, int pageSize) {
    int safePageNo = Math.max(pageNo, 1);
    int safePageSize = Math.max(pageSize, 1);
    Page<OaSuggestion> page = oaSuggestionMapper.selectPage(new Page<>(safePageNo, safePageSize),
        Wrappers.lambdaQuery(OaSuggestion.class)
            .eq(OaSuggestion::getIsDeleted, 0)
            .eq(OaSuggestion::getOrgId, orgId)
            .eq(OaSuggestion::getCreatedBy, familyUserId)
            .orderByDesc(OaSuggestion::getCreateTime));
    return page.getRecords().stream().map(this::toFeedbackRecordItem).toList();
  }

  @Override
  @Transactional
  public VisitBookingResponse bookVideoVisit(Long orgId, Long familyUserId, FamilyPortalModels.VideoBookRequest request) {
    List<Long> elderIds = extractElderIds(listBoundRelations(orgId, familyUserId));
    Long targetElderId = resolveTargetElderId(elderIds, request.getElderId());

    VisitBookRequest bookRequest = new VisitBookRequest();
    bookRequest.setOrgId(orgId);
    bookRequest.setFamilyUserId(familyUserId);
    bookRequest.setElderId(targetElderId);
    bookRequest.setVisitTime(parseDateTime(request.getBookingTime()));
    bookRequest.setVisitTimeSlot("视频探视");
    bookRequest.setVisitorCount(1);
    bookRequest.setRemark(defaultText(request.getRemark(), "家属预约视频探视"));
    return visitService.book(bookRequest);
  }

  @Override
  @Transactional
  public FamilyPortalModels.BindRelationItem bindElder(Long orgId, Long familyUserId,
      FamilyPortalModels.BindCreateRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("绑定请求不能为空");
    }
    Long requestedElderId = request.getElderId();
    String idCardNo = normalizeIdCardNo(request.getElderIdCardNo());
    if (requestedElderId == null && !hasText(idCardNo)) {
      throw new IllegalArgumentException("请填写老人身份证号");
    }

    ElderProfile elder = null;
    if (requestedElderId != null) {
      elder = elderMapper.selectOne(
          Wrappers.lambdaQuery(ElderProfile.class)
              .eq(ElderProfile::getIsDeleted, 0)
              .eq(ElderProfile::getOrgId, orgId)
              .eq(ElderProfile::getId, requestedElderId)
              .last("LIMIT 1"));
    }
    if (elder == null && hasText(idCardNo)) {
      elder = elderMapper.selectOne(
          Wrappers.lambdaQuery(ElderProfile.class)
              .eq(ElderProfile::getIsDeleted, 0)
              .eq(ElderProfile::getOrgId, orgId)
              .apply("UPPER(id_card_no) = {0}", idCardNo)
              .last("LIMIT 1"));
    }
    if (elder == null) {
      throw new IllegalArgumentException("老人不存在或不属于当前机构");
    }
    Long targetElderId = elder.getId();

    boolean switchToPrimary = Integer.valueOf(1).equals(request.getIsPrimary());
    if (switchToPrimary) {
      elderFamilyMapper.update(null, Wrappers.lambdaUpdate(ElderFamily.class)
          .set(ElderFamily::getIsPrimary, 0)
          .eq(ElderFamily::getIsDeleted, 0)
          .eq(ElderFamily::getOrgId, orgId)
          .eq(ElderFamily::getFamilyUserId, familyUserId));
    }

    ElderFamily relation = elderFamilyMapper.selectOne(
        Wrappers.lambdaQuery(ElderFamily.class)
            .eq(ElderFamily::getIsDeleted, 0)
            .eq(ElderFamily::getOrgId, orgId)
            .eq(ElderFamily::getFamilyUserId, familyUserId)
            .eq(ElderFamily::getElderId, targetElderId)
            .last("LIMIT 1"));

    String relationText = request.getRelation() == null ? null : defaultText(request.getRelation(), "家属");
    String remark = request.getRemark() == null ? null : request.getRemark().trim();
    if (relation == null) {
      relation = new ElderFamily();
      relation.setOrgId(orgId);
      relation.setFamilyUserId(familyUserId);
      relation.setElderId(targetElderId);
      relation.setRelation(defaultText(relationText, "家属"));
      relation.setIsPrimary(switchToPrimary ? 1 : 0);
      relation.setRemark(remark);
      elderFamilyMapper.insert(relation);
    } else {
      if (relationText != null) {
        relation.setRelation(relationText);
      }
      if (request.getIsPrimary() != null) {
        relation.setIsPrimary(switchToPrimary ? 1 : 0);
      }
      if (request.getRemark() != null) {
        relation.setRemark(remark);
      }
      elderFamilyMapper.updateById(relation);
    }
    return toBindRelationItem(relation, elder);
  }

  @Override
  public List<FamilyPortalModels.BindRelationItem> listBindings(Long orgId, Long familyUserId) {
    List<ElderFamily> relations = listBoundRelations(orgId, familyUserId);
    Map<Long, ElderProfile> elderMap = loadElderMap(orgId, extractElderIds(relations));
    List<FamilyPortalModels.BindRelationItem> list = new ArrayList<>();
    for (ElderFamily relation : relations) {
      list.add(toBindRelationItem(relation, elderMap.get(relation.getElderId())));
    }
    return list;
  }

  @Override
  @Transactional
  public void removeBinding(Long orgId, Long familyUserId, Long elderId) {
    List<ElderFamily> list = elderFamilyMapper.selectList(
        Wrappers.lambdaQuery(ElderFamily.class)
            .eq(ElderFamily::getIsDeleted, 0)
            .eq(ElderFamily::getOrgId, orgId)
            .eq(ElderFamily::getFamilyUserId, familyUserId)
            .eq(ElderFamily::getElderId, elderId));
    for (ElderFamily relation : list) {
      relation.setIsDeleted(1);
      elderFamilyMapper.updateById(relation);
    }
  }

  @Override
  public FamilyPortalModels.ProfileResponse getProfile(Long orgId, Long familyUserId) {
    FamilyUser user = familyUserMapper.selectOne(
        Wrappers.lambdaQuery(FamilyUser.class)
            .eq(FamilyUser::getIsDeleted, 0)
            .eq(FamilyUser::getOrgId, orgId)
            .eq(FamilyUser::getId, familyUserId)
            .last("LIMIT 1"));
    Map<String, Object> extra = readStateMap(orgId, familyUserId, STATE_PROFILE, "default");

    FamilyPortalModels.ProfileResponse profile = new FamilyPortalModels.ProfileResponse();
    profile.setRealName(user == null ? "家属" : defaultText(user.getRealName(), defaultText(user.getPhone(), "家属")));
    profile.setPhone(maskIfNeeded(orgId, familyUserId, user == null ? "" : defaultText(user.getPhone(), "")));
    profile.setAddress(defaultText(readText(extra, "address"), "地址待完善"));
    profile.setPreferredContact(defaultText(readText(extra, "preferredContact"), "微信消息"));
    return profile;
  }

  @Override
  @Transactional
  public FamilyPortalModels.ProfileResponse updateProfile(Long orgId, Long familyUserId,
      FamilyPortalModels.ProfileUpdateRequest request) {
    FamilyUser user = familyUserMapper.selectById(familyUserId);
    if (user != null && Objects.equals(user.getOrgId(), orgId)) {
      if (request.getRealName() != null && !request.getRealName().isBlank()) {
        user.setRealName(request.getRealName().trim());
      }
      if (request.getPhone() != null && !request.getPhone().isBlank()) {
        user.setPhone(request.getPhone().trim());
      }
      familyUserMapper.updateById(user);
    }

    Map<String, Object> extra = new HashMap<>(readStateMap(orgId, familyUserId, STATE_PROFILE, "default"));
    if (request.getAddress() != null) {
      extra.put("address", request.getAddress().trim());
    }
    if (request.getPreferredContact() != null) {
      extra.put("preferredContact", request.getPreferredContact().trim());
    }
    upsertState(orgId, familyUserId, STATE_PROFILE, "default", extra);
    return getProfile(orgId, familyUserId);
  }

  @Override
  public FamilyPortalModels.NotificationSettingsResponse getNotificationSettings(Long orgId, Long familyUserId) {
    Map<String, Object> map = readStateMap(orgId, familyUserId, STATE_NOTIFICATION, "default");
    FamilyPortalModels.NotificationSettingsResponse setting = new FamilyPortalModels.NotificationSettingsResponse();
    setting.setHealthAlert(readBoolean(map, "healthAlert", true));
    setting.setPaymentAlert(readBoolean(map, "paymentAlert", true));
    setting.setActivityAlert(readBoolean(map, "activityAlert", false));
    setting.setUrgentAlert(readBoolean(map, "urgentAlert", true));
    setting.setSmsFallback(readBoolean(map, "smsFallback", true));
    setting.setSubscribeTemplateIds(resolveSubscribeTemplateIds());
    setting.setSubscribeAuthorized(readBoolean(map, "subscribeAuthorized", false));
    setting.setSubscribeAuthorizedTime(defaultText(readText(map, "subscribeAuthorizedTime"), ""));
    return setting;
  }

  @Override
  public FamilyPortalModels.NotificationSettingsResponse updateNotificationSettings(Long orgId, Long familyUserId,
      FamilyPortalModels.NotificationSettingsUpdateRequest request) {
    FamilyPortalModels.NotificationSettingsResponse setting = getNotificationSettings(orgId, familyUserId);
    if (request.getHealthAlert() != null) setting.setHealthAlert(request.getHealthAlert());
    if (request.getPaymentAlert() != null) setting.setPaymentAlert(request.getPaymentAlert());
    if (request.getActivityAlert() != null) setting.setActivityAlert(request.getActivityAlert());
    if (request.getUrgentAlert() != null) setting.setUrgentAlert(request.getUrgentAlert());
    if (request.getSmsFallback() != null) setting.setSmsFallback(request.getSmsFallback());
    if (request.getSubscribeAuthorized() != null) {
      setting.setSubscribeAuthorized(request.getSubscribeAuthorized());
      if (Boolean.TRUE.equals(request.getSubscribeAuthorized())) {
        setting.setSubscribeAuthorizedTime(LocalDateTime.now().format(DATETIME_FMT));
      }
    }
    Map<String, Object> state = new LinkedHashMap<>();
    state.put("healthAlert", setting.getHealthAlert());
    state.put("paymentAlert", setting.getPaymentAlert());
    state.put("activityAlert", setting.getActivityAlert());
    state.put("urgentAlert", setting.getUrgentAlert());
    state.put("smsFallback", setting.getSmsFallback());
    state.put("subscribeAuthorized", setting.getSubscribeAuthorized());
    state.put("subscribeAuthorizedTime", defaultText(setting.getSubscribeAuthorizedTime(), ""));
    upsertState(orgId, familyUserId, STATE_NOTIFICATION, "default", state);
    return setting;
  }

  @Override
  public FamilyPortalModels.CapabilityStatusResponse getCapabilityStatus(Long orgId, Long familyUserId) {
    FamilyPortalProperties.SmsCode smsCode = familyPortalProperties.getSmsCode();
    FamilyPortalProperties.WechatPay wechatPay = familyPortalProperties.getWechatPay();
    FamilyPortalProperties.WechatNotify wechatNotify = familyPortalProperties.getWechatNotify();
    Map<String, Object> security = readStateMap(orgId, familyUserId, STATE_SECURITY, "default");
    FamilyUser familyUser = familyUserMapper.selectOne(
        Wrappers.lambdaQuery(FamilyUser.class)
            .eq(FamilyUser::getIsDeleted, 0)
            .eq(FamilyUser::getOrgId, orgId)
            .eq(FamilyUser::getId, familyUserId)
            .last("LIMIT 1"));

    boolean smsEnabled = smsCode != null && smsCode.isEnabled();
    String smsProvider = defaultText(smsCode == null ? null : smsCode.getProvider(), "mock");
    boolean smsDebugReturnCode = smsCode != null && smsCode.isDebugReturnCode();
    boolean wechatPayEnabled = wechatPay != null && wechatPay.isEnabled();
    boolean wechatNotifyEnabled = wechatNotify != null && wechatNotify.isEnabled();
    boolean wechatNotifyBound = familyUser != null && hasText(defaultText(familyUser.getOpenId(), null));
    boolean securityPasswordEnabled = true;
    boolean legacyApiEnabled = familyPortalProperties.isLegacyApiEnabled();
    String legacyApiSunsetDate = defaultText(familyPortalProperties.getLegacyApiSunsetDate(), "");
    boolean legacyApiSunsetReached = isLegacySunsetReached(legacyApiSunsetDate);

    FamilyPortalModels.CapabilityStatusResponse response = new FamilyPortalModels.CapabilityStatusResponse();
    response.setSmsEnabled(smsEnabled);
    response.setSmsProvider(smsProvider);
    response.setSmsDebugReturnCode(smsDebugReturnCode);
    response.setWechatPayEnabled(wechatPayEnabled);
    response.setWechatNotifyEnabled(wechatNotifyEnabled);
    response.setWechatNotifyBound(wechatNotifyBound);
    response.setSecurityPasswordEnabled(securityPasswordEnabled);
    response.setLegacyApiEnabled(legacyApiEnabled);
    response.setLegacyApiSunsetDate(legacyApiSunsetDate);
    response.setLegacyApiSunsetReached(legacyApiSunsetReached);
    response.setCheckedAt(LocalDateTime.now().format(DATETIME_FMT));

    response.getItems().add(buildCapabilityItem(
        "SMS_CODE",
        "短信验证码",
        !smsEnabled ? "OFF" : ("mock".equalsIgnoreCase(smsProvider) ? "MOCK" : "READY"),
        !smsEnabled
            ? "短信验证码未启用，家属无法完成真实登录校验"
            : ("mock".equalsIgnoreCase(smsProvider)
                ? "当前为 MOCK 通道，建议切换阿里云短信用于真实投递"
                : "短信验证码通道可用，支持登录与敏感信息校验"),
        "/pages/settings-security/index"));
    response.getItems().add(buildCapabilityItem(
        "WECHAT_NOTIFY",
        "微信通知投递",
        !wechatNotifyEnabled ? "OFF" : (wechatNotifyBound ? "READY" : "BIND_REQUIRED"),
        !wechatNotifyEnabled
            ? "微信通知未启用，建议配置模板消息并开启服务通知"
            : (wechatNotifyBound
                ? "通知链路已可用，紧急消息将优先触达家属"
                : "家属尚未绑定 openId，请在小程序内授权通知接收"),
        "/pages/notification-settings/index"));
    response.getItems().add(buildCapabilityItem(
        "WECHAT_PAY",
        "微信充值支付",
        wechatPayEnabled ? "READY" : "OFF",
        wechatPayEnabled
            ? "微信预下单与支付回调链路已启用"
            : "微信支付未启用，当前仅可使用业务模拟记账充值",
        "/pages/payment/index"));
    response.getItems().add(buildCapabilityItem(
        "SECURITY_PASSWORD",
        "密码二次校验",
        securityPasswordEnabled ? "READY" : "OPTIONAL",
        securityPasswordEnabled
            ? "敏感数据默认需二次输入密码，未设独立密码时将校验登录密码"
            : "尚未开启密码二次校验，请尽快开启",
        "/pages/settings-security/index"));
    response.getItems().add(buildCapabilityItem(
        "LEGACY_API",
        "旧接口下线状态",
        (!legacyApiEnabled || legacyApiSunsetReached) ? "OFFLINE" : "DEPRECATED",
        (!legacyApiEnabled || legacyApiSunsetReached)
            ? "旧接口 bindElder/my-elders 已下线，请使用 family 聚合接口"
            : "旧接口仍兼容但已废弃，请尽快迁移到 /api/family/bindings",
        ""));
    return response;
  }

  @Override
  public FamilyPortalModels.SecuritySettingsResponse getSecuritySettings(Long orgId, Long familyUserId) {
    Map<String, Object> map = readStateMap(orgId, familyUserId, STATE_SECURITY, "default");
    FamilyPortalModels.SecuritySettingsResponse response = new FamilyPortalModels.SecuritySettingsResponse();
    response.setVerifyHealthData(readBoolean(map, "verifyHealthData", true));
    response.setVerifyMedicalRecords(readBoolean(map, "verifyMedicalRecords", true));
    response.setVerifyReports(readBoolean(map, "verifyReports", true));
    response.setVerifyWithSmsCode(false);
    response.setVerifyWithPassword(true);
    response.setHasIndependentPassword(hasSecurityPasswordConfigured(map));
    response.setMaskSensitiveData(readBoolean(map, "maskSensitiveData", true));
    response.setVisibleScope(defaultText(readText(map, "visibleScope"), "仅子女可查看完整健康数据"));
    return response;
  }

  @Override
  public FamilyPortalModels.SecuritySettingsResponse updateSecuritySettings(Long orgId, Long familyUserId,
      FamilyPortalModels.SecuritySettingsUpdateRequest request) {
    FamilyPortalModels.SecuritySettingsResponse current = getSecuritySettings(orgId, familyUserId);
    if (request.getVerifyHealthData() != null) current.setVerifyHealthData(request.getVerifyHealthData());
    if (request.getVerifyMedicalRecords() != null) current.setVerifyMedicalRecords(request.getVerifyMedicalRecords());
    if (request.getVerifyReports() != null) current.setVerifyReports(request.getVerifyReports());
    if (request.getMaskSensitiveData() != null) current.setMaskSensitiveData(request.getMaskSensitiveData());
    if (request.getVisibleScope() != null) current.setVisibleScope(request.getVisibleScope().trim());
    current.setVerifyWithSmsCode(false);
    current.setVerifyWithPassword(true);
    Map<String, Object> existing = new LinkedHashMap<>(readStateMap(orgId, familyUserId, STATE_SECURITY, "default"));
    existing.put("verifyHealthData", current.getVerifyHealthData());
    existing.put("verifyMedicalRecords", current.getVerifyMedicalRecords());
    existing.put("verifyReports", current.getVerifyReports());
    existing.put("verifyWithSmsCode", false);
    existing.put("verifyWithPassword", true);
    existing.put("maskSensitiveData", current.getMaskSensitiveData());
    existing.put("visibleScope", defaultText(current.getVisibleScope(), "仅子女可查看完整健康数据"));
    upsertState(orgId, familyUserId, STATE_SECURITY, "default", existing);
    current.setHasIndependentPassword(hasSecurityPasswordConfigured(existing));
    return current;
  }

  private FamilyPortalModels.CapabilityStatusItem buildCapabilityItem(String code, String title, String status,
      String hint, String actionPath) {
    FamilyPortalModels.CapabilityStatusItem item = new FamilyPortalModels.CapabilityStatusItem();
    item.setCode(code);
    item.setTitle(title);
    item.setStatus(status);
    item.setHint(hint);
    item.setActionPath(defaultText(actionPath, ""));
    return item;
  }

  @Override
  @Transactional
  public boolean setSecurityPassword(Long orgId, Long familyUserId, FamilyPortalModels.SecurityPasswordSetRequest request) {
    String password = defaultText(request == null ? null : request.getPassword(), "");
    if (password.length() < 6) {
      throw new IllegalArgumentException("密码长度不能少于6位");
    }
    Map<String, Object> current = new LinkedHashMap<>(readStateMap(orgId, familyUserId, STATE_SECURITY, "default"));
    String salt = randomSalt();
    String hash = hashSecurityPassword(password, salt);
    current.put("passwordSalt", salt);
    current.put("passwordHash", hash);
    current.put("passwordUpdatedAt", LocalDateTime.now().format(DATETIME_FMT));
    current.put("verifyWithPassword", true);
    upsertState(orgId, familyUserId, STATE_SECURITY, "default", current);
    return true;
  }

  @Override
  public FamilyPortalModels.SecurityPasswordVerifyResponse verifySecurityPassword(
      Long orgId, Long familyUserId, FamilyPortalModels.SecurityPasswordVerifyRequest request) {
    FamilyPortalModels.SecurityPasswordVerifyResponse response = new FamilyPortalModels.SecurityPasswordVerifyResponse();
    String password = defaultText(request == null ? null : request.getPassword(), "");
    Map<String, Object> current = readStateMap(orgId, familyUserId, STATE_SECURITY, "default");
    String salt = defaultText(readText(current, "passwordSalt"), null);
    String expected = defaultText(readText(current, "passwordHash"), null);
    if (salt != null && expected != null) {
      String candidate = hashSecurityPassword(password, salt);
      boolean passed = Objects.equals(expected, candidate);
      response.setPassed(passed);
      response.setMessage(passed ? "密码验证通过" : "密码错误，请重试");
      return response;
    }

    FamilyUser familyUser = familyUserMapper.selectOne(
        Wrappers.lambdaQuery(FamilyUser.class)
            .eq(FamilyUser::getIsDeleted, 0)
            .eq(FamilyUser::getOrgId, orgId)
            .eq(FamilyUser::getId, familyUserId)
            .last("LIMIT 1"));
    if (familyUser == null || !hasText(familyUser.getPasswordHash())) {
      response.setPassed(false);
      response.setMessage("账号密码未设置，请联系管理员");
      return response;
    }
    boolean passed = passwordEncoder.matches(password, familyUser.getPasswordHash());
    response.setPassed(passed);
    response.setMessage(passed ? "登录密码验证通过" : "登录密码错误，请重试");
    return response;
  }

  @Override
  public List<FamilyPortalModels.AffectionMomentItem> listAffectionMoments(Long orgId, Long familyUserId) {
    List<FamilyPortalState> states = listStateRows(orgId, familyUserId, STATE_AFFECTION, 100);
    List<FamilyPortalModels.AffectionMomentItem> list = states.stream()
        .map(this::toAffectionItem)
        .filter(Objects::nonNull)
        .toList();
    if (!list.isEmpty()) {
      return list;
    }
    FamilyPortalModels.AffectionMomentItem sample = new FamilyPortalModels.AffectionMomentItem();
    sample.setId(1L);
    sample.setType("voice");
    sample.setTitle("语音留言");
    sample.setTime(formatFriendlyDateTime(LocalDateTime.now().minusHours(2)));
    sample.setDesc("妈妈我想你了，周末来看你。");
    return List.of(sample);
  }

  @Override
  public List<String> listAffectionTemplates() {
    return List.of("生日祝福", "春节问候", "中秋团圆", "母亲节祝福");
  }

  @Override
  public void addAffectionMoment(Long orgId, Long familyUserId, FamilyPortalModels.AffectionMomentCreateRequest request) {
    long id = System.currentTimeMillis();
    Map<String, Object> state = new LinkedHashMap<>();
    state.put("id", id);
    state.put("type", request.getType());
    state.put("title", request.getTitle());
    state.put("time", formatFriendlyDateTime(LocalDateTime.now()));
    state.put("desc", request.getDesc());
    if (defaultText(request.getMediaType(), null) != null) {
      state.put("mediaType", request.getMediaType().trim());
    }
    if (defaultText(request.getMediaUrl(), null) != null) {
      state.put("mediaUrl", request.getMediaUrl().trim());
    }
    if (defaultText(request.getMediaName(), null) != null) {
      state.put("mediaName", request.getMediaName().trim());
    }
    upsertState(orgId, familyUserId, STATE_AFFECTION, String.valueOf(id), state);
  }

  @Override
  public List<FamilyPortalModels.CommunicationMessageItem> listCommunicationMessages(Long orgId, Long familyUserId,
      int pageNo, int pageSize) {
    if (pageNo < 1) {
      pageNo = 1;
    }
    if (pageSize < 1) {
      pageSize = 20;
    }
    Page<FamilyPortalState> page = familyPortalStateMapper.selectPage(new Page<>(pageNo, pageSize),
        Wrappers.lambdaQuery(FamilyPortalState.class)
            .eq(FamilyPortalState::getIsDeleted, 0)
            .eq(FamilyPortalState::getOrgId, orgId)
            .eq(FamilyPortalState::getFamilyUserId, familyUserId)
            .eq(FamilyPortalState::getCategory, STATE_COMMUNICATION)
            .orderByDesc(FamilyPortalState::getUpdateTime));
    return page.getRecords().stream()
        .map(this::toCommunicationMessageItem)
        .filter(Objects::nonNull)
        .toList();
  }

  @Override
  @Transactional
  public FamilyPortalModels.CommunicationMessageItem sendCommunicationMessage(Long orgId, Long familyUserId,
      FamilyPortalModels.CommunicationMessageCreateRequest request) {
    long id = System.currentTimeMillis();
    String targetRole = request.getTargetRole().trim();
    String msgType = request.getMsgType().trim();
    String content = defaultText(request.getContent(), "");
    String mediaUrl = defaultText(request.getMediaUrl(), null);
    String mediaName = defaultText(request.getMediaName(), null);
    Integer mediaDurationSec = request.getMediaDurationSec();
    String transcript = defaultText(request.getTranscript(), null);
    if (content.isBlank() && mediaUrl == null) {
      throw new IllegalArgumentException("消息内容不能为空");
    }
    FamilyUser familyUser = familyUserMapper.selectById(familyUserId);
    String sender = familyUser == null ? "家属" : defaultText(familyUser.getRealName(), defaultText(familyUser.getPhone(), "家属"));
    String time = formatFriendlyDateTime(LocalDateTime.now());

    Map<String, Object> state = new LinkedHashMap<>();
    state.put("id", id);
    state.put("targetRole", targetRole);
    state.put("msgType", msgType);
    state.put("content", content);
    state.put("time", time);
    state.put("sender", sender);
    state.put("status", "已发送");
    if (mediaUrl != null) {
      state.put("mediaUrl", mediaUrl);
    }
    if (mediaName != null) {
      state.put("mediaName", mediaName);
    }
    if (mediaDurationSec != null && mediaDurationSec > 0) {
      state.put("mediaDurationSec", mediaDurationSec);
    }
    if (transcript != null) {
      state.put("transcript", transcript);
    }
    upsertState(orgId, familyUserId, STATE_COMMUNICATION, String.valueOf(id), state);

    OaSuggestion ticket = new OaSuggestion();
    ticket.setTenantId(orgId);
    ticket.setOrgId(orgId);
    String ticketContent = "[家属沟通][" + targetRole + "][" + msgType + "] " + content;
    if (mediaUrl != null) {
      ticketContent += " | mediaUrl=" + mediaUrl;
    }
    if (mediaDurationSec != null && mediaDurationSec > 0) {
      ticketContent += " | duration=" + mediaDurationSec + "s";
    }
    ticket.setContent(ticketContent);
    ticket.setProposerName(sender);
    ticket.setContact(familyUser == null ? "" : defaultText(familyUser.getPhone(), ""));
    ticket.setStatus("PENDING");
    ticket.setCreatedBy(familyUserId);
    oaSuggestionMapper.insert(ticket);

    FamilyPortalModels.CommunicationMessageItem response = new FamilyPortalModels.CommunicationMessageItem();
    response.setId(id);
    response.setTargetRole(targetRole);
    response.setMsgType(msgType);
    response.setContent(content);
    response.setMediaUrl(mediaUrl);
    response.setMediaName(mediaName);
    response.setMediaDurationSec(mediaDurationSec);
    response.setTranscript(transcript);
    response.setTime(time);
    response.setSender(sender);
    response.setStatus("已发送");

    if ("voice".equalsIgnoreCase(msgType)) {
      notifyFamilySafe(orgId, familyUserId, null, "COMMUNICATION_VOICE_SENT", "normal",
          "语音留言已送达护工端",
          "已转交 " + targetRole + " 协助播放语音留言",
          "pages/communication/index",
          Map.of(
              "role", targetRole,
              "mediaName", defaultText(mediaName, "语音留言"),
              "duration", mediaDurationSec == null ? "" : (mediaDurationSec + "秒")));
    }
    return response;
  }

  @Override
  public List<FamilyPortalModels.CommunicationTemplateItem> listCommunicationTemplates(Long orgId) {
    List<FamilyPortalModels.CommunicationTemplateItem> items = new ArrayList<>();
    items.add(buildCommunicationTemplate("tpl-health-recheck", "血压复测反馈", "责任护士", "text",
        "请反馈今日午后血压复测结果，辛苦了。", "健康关注"));
    items.add(buildCommunicationTemplate("tpl-meal-followup", "进食情况确认", "责任护工", "text",
        "请协助确认今天午餐进食情况和是否需要额外协助。", "膳食关注"));
    items.add(buildCommunicationTemplate("tpl-video-assist", "协助探视预约", "生活管家", "video",
        "我想预约探视时段，请协助老人按预约时间参与。", "探视预约"));
    items.add(buildCommunicationTemplate("tpl-voice-greeting", "语音问候播放", "责任护工", "voice",
        "请协助播放家属语音问候，感谢。", "亲情互动"));
    items.add(buildCommunicationTemplate("tpl-payment-question", "账单咨询", "客服中心", "service",
        "请协助说明本月账单明细及待缴项目，谢谢。", "费用咨询"));
    return items;
  }

  @Override
  public List<FamilyPortalModels.HelpFaqItem> listHelpFaqs() {
    List<FamilyPortalModels.HelpFaqItem> items = new ArrayList<>();
    items.add(faq("绑定与家庭", "如何绑定多位老人？", "进入“我的家庭”后可重复添加绑定，并设置主要联系人。"));
    items.add(faq("提醒机制", "紧急事件如何第一时间通知？", "建议开启“紧急通知”和“短信兜底提醒”，系统将双通道触达。"));
    items.add(faq("缴费与服务", "可以线上充值和购买服务吗？", "支持在线充值、服务增购和订单查询，账单变更会同步提醒。"));
    items.add(faq("隐私安全", "健康档案如何保护？", "可配置健康档案、就医记录、评估报告查看时二次验证。"));
    return items;
  }

  @Override
  @Transactional
  public FamilyPortalModels.AiAssessmentGenerateResponse generateAiAssessmentReports(
      Long orgId, Long familyUserId, FamilyPortalModels.AiAssessmentGenerateRequest request) {
    List<Long> elderIds = ensureBoundElderIds(extractElderIds(listBoundRelations(orgId, familyUserId)));
    Long targetElderId = resolveTargetElderId(elderIds, request == null ? null : request.getElderId());
    ElderProfile elder = elderMapper.selectById(targetElderId);
    String elderName = elder == null ? "未命名老人" : defaultText(elder.getFullName(), "未命名老人");

    List<String> targets = resolveAiReportTypes(request == null ? null : request.getReportType());
    List<String> generated = new ArrayList<>();
    for (String type : targets) {
      AssessmentRecord record = buildAiAssessmentRecord(orgId, familyUserId, targetElderId, elderName, type);
      assessmentRecordMapper.insert(record);
      generated.add(type);
    }

    FamilyPortalModels.AiAssessmentGenerateResponse response = new FamilyPortalModels.AiAssessmentGenerateResponse();
    response.setElderId(targetElderId);
    response.setGeneratedTypes(generated);
    response.setGeneratedCount(generated.size());
    response.setMessage(generated.isEmpty() ? "未生成新评估" : "AI评估已生成并归档");
    return response;
  }

  private WeeklySnapshot collectWeeklySnapshot(Long orgId, Long familyUserId, Long elderId,
      LocalDate startDate, LocalDate endDate, boolean includeUnread) {
    LocalDateTime startDateTime = startDate.atStartOfDay();
    LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

    long healthCheckCount = healthDataRecordMapper.selectCount(
        Wrappers.lambdaQuery(HealthDataRecord.class)
            .eq(HealthDataRecord::getIsDeleted, 0)
            .eq(HealthDataRecord::getOrgId, orgId)
            .eq(HealthDataRecord::getElderId, elderId)
            .ge(HealthDataRecord::getMeasuredAt, startDateTime)
            .lt(HealthDataRecord::getMeasuredAt, endDateTime));
    long abnormalCount = healthDataRecordMapper.selectCount(
        Wrappers.lambdaQuery(HealthDataRecord.class)
            .eq(HealthDataRecord::getIsDeleted, 0)
            .eq(HealthDataRecord::getOrgId, orgId)
            .eq(HealthDataRecord::getElderId, elderId)
            .eq(HealthDataRecord::getAbnormalFlag, 1)
            .ge(HealthDataRecord::getMeasuredAt, startDateTime)
            .lt(HealthDataRecord::getMeasuredAt, endDateTime));
    long nursingLogCount = healthNursingLogMapper.selectCount(
        Wrappers.lambdaQuery(HealthNursingLog.class)
            .eq(HealthNursingLog::getIsDeleted, 0)
            .eq(HealthNursingLog::getOrgId, orgId)
            .eq(HealthNursingLog::getElderId, elderId)
            .ge(HealthNursingLog::getLogTime, startDateTime)
            .lt(HealthNursingLog::getLogTime, endDateTime));
    long activityCount = oaAlbumMapper.selectCount(
        Wrappers.lambdaQuery(OaAlbum.class)
            .eq(OaAlbum::getIsDeleted, 0)
            .eq(OaAlbum::getOrgId, orgId)
            .ge(OaAlbum::getShootDate, startDate)
            .le(OaAlbum::getShootDate, endDate));
    long communicationCount = familyPortalStateMapper.selectCount(
        Wrappers.lambdaQuery(FamilyPortalState.class)
            .eq(FamilyPortalState::getIsDeleted, 0)
            .eq(FamilyPortalState::getOrgId, orgId)
            .eq(FamilyPortalState::getFamilyUserId, familyUserId)
            .eq(FamilyPortalState::getCategory, STATE_COMMUNICATION)
            .ge(FamilyPortalState::getUpdateTime, startDateTime)
            .lt(FamilyPortalState::getUpdateTime, endDateTime));
    int unreadMessageCount = 0;
    if (includeUnread) {
      unreadMessageCount = (int) listMessages(orgId, familyUserId, 1, 50).stream()
          .filter(item -> Boolean.TRUE.equals(item.getUnread()))
          .count();
    }
    return new WeeklySnapshot(healthCheckCount, abnormalCount, nursingLogCount, activityCount, communicationCount, unreadMessageCount);
  }

  private String buildWeeklyQuickText(WeeklySnapshot snapshot) {
    if (snapshot == null) {
      return "周报数据待生成";
    }
    if (snapshot.abnormalCount > 0) {
      return "异常 " + snapshot.abnormalCount + " 项，建议优先关注健康复测";
    }
    if (snapshot.healthCheckCount == 0) {
      return "本周暂无健康检测记录，建议确认设备与排班";
    }
    return "健康检测 " + snapshot.healthCheckCount + " 次，护理记录 " + snapshot.nursingLogCount + " 条";
  }

  @Override
  public FamilyPortalModels.TodoCenterResponse getTodoCenter(Long orgId, Long familyUserId, Long elderId) {
    FamilyPortalModels.TodoCenterResponse response = new FamilyPortalModels.TodoCenterResponse();
    List<FamilyPortalModels.TodoItem> rawItems = new ArrayList<>();

    List<FamilyPortalModels.MessageItem> messages = listMessages(orgId, familyUserId, 1, 20);
    for (FamilyPortalModels.MessageItem message : messages) {
      if (!Boolean.TRUE.equals(message.getUnread())) {
        continue;
      }
      String level = defaultText(message.getLevel(), "normal");
      String priority = "urgent".equals(level) ? "URGENT" : ("warning".equals(level) ? "HIGH" : "NORMAL");
      rawItems.add(buildTodoItem(
          "MESSAGE",
          priority,
          defaultText(message.getTitle(), "待查看消息"),
          defaultText(message.getContent(), ""),
          "/pages/message-detail/index?id=" + message.getId(),
          defaultText(message.getTime(), ""),
          "待处理"));
      if (rawItems.size() >= 3) {
        break;
      }
    }

    FamilyPortalModels.PaymentGuardResponse guard = getPaymentGuard(orgId, familyUserId, elderId);
    if (safeDecimal(guard.getOutstanding()).compareTo(BigDecimal.ZERO) > 0) {
      rawItems.add(buildTodoItem(
          "PAYMENT",
          "HIGH",
          "本月账单待缴费",
          "当前仍有 ¥" + safeDecimal(guard.getOutstanding()) + " 未结清，建议尽快处理",
          "/pages/payment/index",
          "今日内",
          "待处理"));
    }
    if (safeInt(guard.getPendingOrderCount()) > 0) {
      rawItems.add(buildTodoItem(
          "PAYMENT",
          "HIGH",
          "存在待支付充值单",
          "有 " + safeInt(guard.getPendingOrderCount()) + " 笔订单待完成，可进入订单详情继续支付",
          "/pages/payment-guard/index",
          "尽快",
          "待处理"));
    }

    List<FamilyPortalModels.ScheduleItem> schedules = listTodaySchedules(orgId, familyUserId, elderId);
    int completedSchedules = 0;
    for (FamilyPortalModels.ScheduleItem schedule : schedules) {
      if ("已完成".equals(defaultText(schedule.getStatus(), ""))) {
        completedSchedules++;
        continue;
      }
      rawItems.add(buildTodoItem(
          "SCHEDULE",
          "进行中".equals(schedule.getStatus()) ? "HIGH" : "NORMAL",
          defaultText(schedule.getName(), "护理任务"),
          "执行人：" + defaultText(schedule.getOwner(), "护理组"),
          "/pages/schedule/index",
          defaultText(schedule.getTime(), ""),
          defaultText(schedule.getStatus(), "待开始")));
      if (rawItems.size() >= 10) {
        break;
      }
    }

    FamilyPortalModels.SecuritySettingsResponse security = getSecuritySettings(orgId, familyUserId);
    if (!Boolean.TRUE.equals(security.getVerifyWithSmsCode()) || !Boolean.TRUE.equals(security.getVerifyReports())) {
      rawItems.add(buildTodoItem(
          "SECURITY",
          "NORMAL",
          "建议开启二次验证",
          "当前敏感信息保护策略可进一步加强，建议开启验证码校验",
          "/pages/settings-security/index",
          "",
          "建议优化"));
    }

    List<FamilyPortalModels.TodoItem> items = rawItems.stream()
        .map(item -> applyTodoItemActionState(orgId, familyUserId, item))
        .collect(Collectors.toCollection(ArrayList::new));
    items.sort(Comparator.comparingInt(item -> todoPriorityRank(item.getPriority())));
    if (items.size() > 12) {
      items = new ArrayList<>(items.subList(0, 12));
    }

    int urgentCount = (int) items.stream()
        .filter(item -> "URGENT".equals(item.getPriority()) && !Boolean.TRUE.equals(item.getDone()))
        .count();
    int dueTodayCount = (int) items.stream()
        .filter(item -> !Boolean.TRUE.equals(item.getDone())
            && !Boolean.TRUE.equals(item.getSnoozed())
            && !"建议优化".equals(defaultText(item.getStatus(), "")))
        .count();

    FamilyPortalModels.TodoCenterSummary summary = new FamilyPortalModels.TodoCenterSummary();
    summary.setUrgentCount(urgentCount);
    summary.setDueTodayCount(dueTodayCount);
    summary.setCompletedHintCount(completedSchedules);

    response.setSummary(summary);
    response.setItems(items);
    response.setRefreshTime(formatFriendlyDateTime(LocalDateTime.now()));
    return response;
  }

  @Override
  @Transactional
  public FamilyPortalModels.TodoActionResponse handleTodoAction(Long orgId, Long familyUserId,
      FamilyPortalModels.TodoActionRequest request) {
    String todoKey = requireText(request.getTodoKey(), "todoKey不能为空");
    String action = requireText(request.getAction(), "action不能为空").toUpperCase(Locale.ROOT);
    LocalDateTime now = LocalDateTime.now();

    FamilyPortalModels.TodoActionResponse response = new FamilyPortalModels.TodoActionResponse();
    response.setTodoKey(todoKey);
    response.setAction(action);

    if ("DONE".equals(action)) {
      upsertState(orgId, familyUserId, STATE_TODO_ACTION, todoKey, Map.of(
          "todoKey", todoKey,
          "action", action,
          "doneAt", now.format(DATETIME_FMT)));
      response.setStatus("DONE");
      response.setMessage("该事项已标记为完成");
      return response;
    }

    if ("SNOOZE".equals(action)) {
      int minutes = request.getSnoozeMinutes() == null ? 120 : Math.max(5, Math.min(request.getSnoozeMinutes(), 24 * 60));
      LocalDateTime remindAt = now.plusMinutes(minutes);
      upsertState(orgId, familyUserId, STATE_TODO_ACTION, todoKey, Map.of(
          "todoKey", todoKey,
          "action", action,
          "snoozeMinutes", minutes,
          "snoozeUntil", remindAt.format(DATETIME_FMT)));
      response.setStatus("SNOOZED");
      response.setMessage("已稍后提醒 " + minutes + " 分钟");
      response.setNextRemindTime(remindAt.format(DATETIME_FMT));
      return response;
    }

    if ("UNDO".equals(action)) {
      removeState(orgId, familyUserId, STATE_TODO_ACTION, todoKey);
      response.setStatus("TODO");
      response.setMessage("已恢复为待处理");
      return response;
    }

    throw new IllegalArgumentException("不支持的待办动作：" + action);
  }

  private String resolvePayerOpenId(FamilyPortalProperties.WechatPay wechatPay,
      FamilyPortalModels.WechatRechargePrepayRequest request) {
    String openId = defaultText(request.getPayerOpenId(), null);
    if (openId != null) {
      return openId;
    }
    String loginCode = defaultText(request.getLoginCode(), null);
    if (loginCode == null) {
      throw new IllegalArgumentException("微信充值缺少 payerOpenId 或 loginCode");
    }
    String appId = requireText(wechatPay.getAppId(), "未配置微信支付 appId");
    String appSecret = requireText(wechatPay.getAppSecret(), "未配置微信支付 appSecret");
    return resolveOpenIdByLoginCode(appId, appSecret, loginCode);
  }

  private String resolveWechatNotifyAppId() {
    FamilyPortalProperties.WechatNotify notify = familyPortalProperties.getWechatNotify();
    if (notify != null && defaultText(notify.getAppId(), null) != null) {
      return notify.getAppId().trim();
    }
    FamilyPortalProperties.WechatPay pay = familyPortalProperties.getWechatPay();
    return requireText(pay == null ? null : pay.getAppId(), "未配置微信通知 appId");
  }

  private String resolveWechatNotifyAppSecret() {
    FamilyPortalProperties.WechatNotify notify = familyPortalProperties.getWechatNotify();
    if (notify != null && defaultText(notify.getAppSecret(), null) != null) {
      return notify.getAppSecret().trim();
    }
    FamilyPortalProperties.WechatPay pay = familyPortalProperties.getWechatPay();
    return requireText(pay == null ? null : pay.getAppSecret(), "未配置微信通知 appSecret");
  }

  private String resolveOpenIdByLoginCode(String appId, String appSecret, String loginCode) {
    String url = UriComponentsBuilder
        .fromHttpUrl("https://api.weixin.qq.com/sns/jscode2session")
        .queryParam("appid", appId)
        .queryParam("secret", appSecret)
        .queryParam("js_code", loginCode)
        .queryParam("grant_type", "authorization_code")
        .toUriString();
    ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
    JsonNode node = readJsonNode(response.getBody());
    String resolved = defaultText(node.path("openid").asText(""), null);
    if (resolved == null) {
      throw new IllegalStateException("获取微信openId失败：" + defaultText(node.path("errmsg").asText(""), "未知错误"));
    }
    return resolved;
  }

  private void persistFamilyOpenId(Long orgId, Long familyUserId, String openId) {
    if (orgId == null || familyUserId == null || openId == null || openId.isBlank()) {
      return;
    }
    FamilyUser familyUser = familyUserMapper.selectById(familyUserId);
    if (familyUser == null || Integer.valueOf(1).equals(familyUser.getIsDeleted())
        || !Objects.equals(orgId, familyUser.getOrgId())) {
      return;
    }
    if (Objects.equals(defaultText(familyUser.getOpenId(), null), openId.trim())) {
      return;
    }
    familyUser.setOpenId(openId.trim());
    familyUserMapper.updateById(familyUser);
  }

  private JsonNode requestWechatApi(FamilyPortalProperties.WechatPay wechatPay, HttpMethod method, String url, String body) {
    try {
      URI uri = URI.create(requireText(url, "未配置微信支付接口地址"));
      String canonicalPath = canonicalPath(uri);
      String nonceStr = randomNonce();
      String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
      String payload = body == null ? "" : body;
      String message = method.name() + "\n" + canonicalPath + "\n" + timestamp + "\n" + nonceStr + "\n" + payload + "\n";
      String signature = signByPrivateKey(requireText(wechatPay.getPrivateKeyPem(), "未配置微信支付私钥"), message);
      String authorization = "WECHATPAY2-SHA256-RSA2048 "
          + "mchid=\"" + requireText(wechatPay.getMerchantId(), "未配置微信商户号") + "\","
          + "nonce_str=\"" + nonceStr + "\","
          + "timestamp=\"" + timestamp + "\","
          + "serial_no=\"" + requireText(wechatPay.getMerchantSerialNo(), "未配置微信商户证书序列号") + "\","
          + "signature=\"" + signature + "\"";

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setAccept(List.of(MediaType.APPLICATION_JSON));
      headers.set("Authorization", authorization);
      headers.set("User-Agent", "smartcare-family-wechat-pay/1.0");

      ResponseEntity<String> response = restTemplate.exchange(uri, method, new HttpEntity<>(payload, headers), String.class);
      return readJsonNode(response.getBody());
    } catch (Exception ex) {
      throw ex instanceof RuntimeException ? (RuntimeException) ex : new IllegalStateException("请求微信支付接口失败", ex);
    }
  }

  private void syncWechatOrderStatus(FamilyRechargeOrder order) {
    if (order == null || ORDER_STATUS_PAID.equalsIgnoreCase(order.getStatus())) {
      return;
    }
    FamilyPortalProperties.WechatPay wechatPay = familyPortalProperties.getWechatPay();
    if (wechatPay == null || !wechatPay.isEnabled()) {
      return;
    }
    try {
      String queryUrl = UriComponentsBuilder
          .fromUriString(requireText(wechatPay.getQueryUrlTemplate(), "未配置微信支付查询接口地址"))
          .buildAndExpand(Map.of(
              "outTradeNo", order.getOutTradeNo(),
              "mchId", requireText(wechatPay.getMerchantId(), "未配置微信商户号")))
          .toUriString();
      JsonNode queryResp = requestWechatApi(wechatPay, HttpMethod.GET, queryUrl, "");
      String tradeState = defaultText(queryResp.path("trade_state").asText(""), "UNKNOWN");
      String wxTransactionId = defaultText(queryResp.path("transaction_id").asText(""), null);
      int paidFen = queryResp.path("amount").path("total").asInt(-1);
      LocalDateTime paidAt = parseWechatTime(queryResp.path("success_time").asText(null));
      if ("SUCCESS".equalsIgnoreCase(tradeState)) {
        applyRechargePaid(order, wxTransactionId, paidFen, paidAt, writeJson(queryResp));
        return;
      }
      if ("CLOSED".equalsIgnoreCase(tradeState) || "REVOKED".equalsIgnoreCase(tradeState) || "PAYERROR".equalsIgnoreCase(tradeState)) {
        order.setStatus(ORDER_STATUS_CLOSED);
        order.setRemark("微信查询状态:" + tradeState);
        familyRechargeOrderMapper.updateById(order);
        createRechargeAbnormalTodo(order, "微信订单状态为 " + tradeState + "，请联系家属确认后续充值");
      }
    } catch (Exception ignore) {
      // 查询失败时不阻塞前端，保留当前状态。
    }
  }

  private void applyRechargePaid(FamilyRechargeOrder order, String wxTransactionId, int paidFen, LocalDateTime paidAt, String payload) {
    if (order == null) {
      return;
    }
    LocalDateTime paidTime = paidAt == null ? LocalDateTime.now() : paidAt;
    String finalWxTransactionId = defaultText(wxTransactionId, order.getWxTransactionId());
    BigDecimal paidAmount = paidFen >= 0 ? BigDecimal.valueOf(paidFen).movePointLeft(2) : order.getAmount();
    if (order.getAmount() != null && paidAmount.compareTo(order.getAmount()) != 0) {
      throw new IllegalStateException("支付金额校验失败，订单金额=" + order.getAmount() + "，回调金额=" + paidAmount);
    }

    int claimed = familyRechargeOrderMapper.update(null, Wrappers.lambdaUpdate(FamilyRechargeOrder.class)
        .set(FamilyRechargeOrder::getStatus, ORDER_STATUS_PAID)
        .set(FamilyRechargeOrder::getWxTransactionId, finalWxTransactionId)
        .set(FamilyRechargeOrder::getPaidAt, paidTime)
        .set(FamilyRechargeOrder::getNotifyPayload, payload)
        .eq(FamilyRechargeOrder::getId, order.getId())
        .eq(FamilyRechargeOrder::getIsDeleted, 0)
        .ne(FamilyRechargeOrder::getStatus, ORDER_STATUS_PAID));
    if (claimed <= 0) {
      FamilyRechargeOrder latest = familyRechargeOrderMapper.selectById(order.getId());
      if (latest != null && ORDER_STATUS_PAID.equalsIgnoreCase(latest.getStatus())) {
        if (payload != null && !payload.isBlank()) {
          latest.setNotifyPayload(payload);
          latest.setWxTransactionId(defaultText(finalWxTransactionId, latest.getWxTransactionId()));
          if (latest.getPaidAt() == null) {
            latest.setPaidAt(paidTime);
          }
          familyRechargeOrderMapper.updateById(latest);
        }
      }
      return;
    }

    Long existingCredit = elderAccountLogMapper.selectCount(
        Wrappers.lambdaQuery(ElderAccountLog.class)
            .eq(ElderAccountLog::getIsDeleted, 0)
            .eq(ElderAccountLog::getOrgId, order.getOrgId())
            .eq(ElderAccountLog::getSourceType, "FAMILY_WECHAT_RECHARGE")
            .eq(ElderAccountLog::getSourceId, order.getId()));
    if (existingCredit != null && existingCredit > 0) {
      return;
    }

    ElderAccount account = getOrCreateElderAccount(order.getOrgId(), order.getElderId(), order.getFamilyUserId());
    BigDecimal newBalance = safeDecimal(account.getBalance()).add(paidAmount);
    account.setBalance(newBalance);
    elderAccountMapper.updateById(account);

    ElderAccountLog log = new ElderAccountLog();
    log.setTenantId(order.getOrgId());
    log.setOrgId(order.getOrgId());
    log.setElderId(order.getElderId());
    log.setAccountId(account.getId());
    log.setAmount(paidAmount);
    log.setBalanceAfter(newBalance);
    log.setDirection("CREDIT");
    log.setSourceType("FAMILY_WECHAT_RECHARGE");
    log.setSourceId(order.getId());
    log.setRemark("微信充值到账，订单号:" + order.getOutTradeNo());
    log.setCreatedBy(order.getFamilyUserId());
    elderAccountLogMapper.insert(log);

    String elderName = resolveElderName(order.getElderId());
    notifyFamilySafe(order.getOrgId(), order.getFamilyUserId(), order.getElderId(),
        "PAYMENT_RECHARGE_SUCCESS", "normal",
        "微信充值到账",
        "订单" + defaultText(order.getOutTradeNo(), "") + "已支付成功，" + elderName + "账户入账 ¥"
            + paidAmount.stripTrailingZeros().toPlainString(),
        "pages/payment-guard/index",
        Map.of(
            "elderName", elderName,
            "amount", paidAmount.stripTrailingZeros().toPlainString(),
            "outTradeNo", defaultText(order.getOutTradeNo(), "")));
  }

  private FamilyPortalModels.RechargeOrderItem toRechargeOrderItem(FamilyRechargeOrder order, ElderProfile elder) {
    if (order == null) {
      return null;
    }
    FamilyPortalModels.RechargeOrderItem item = new FamilyPortalModels.RechargeOrderItem();
    item.setOutTradeNo(order.getOutTradeNo());
    item.setElderId(order.getElderId());
    item.setElderName(elder == null ? "未命名老人" : defaultText(elder.getFullName(), "未命名老人"));
    item.setAmount(safeDecimal(order.getAmount()));
    item.setChannel(defaultText(order.getChannel(), "WECHAT_JSAPI"));
    item.setStatus(defaultText(order.getStatus(), ORDER_STATUS_INIT));
    item.setStatusText(rechargeStatusText(item.getStatus()));
    item.setPrepayId(order.getPrepayId());
    item.setWxTransactionId(order.getWxTransactionId());
    item.setCreateTime(order.getCreateTime() == null ? "" : order.getCreateTime().format(DATETIME_FMT));
    item.setPaidTime(order.getPaidAt() == null ? "" : order.getPaidAt().format(DATETIME_FMT));
    item.setRemark(defaultText(order.getRemark(), ""));
    return item;
  }

  private String rechargeStatusText(String status) {
    if (ORDER_STATUS_PAID.equalsIgnoreCase(status)) return "已支付";
    if (ORDER_STATUS_PREPAY_CREATED.equalsIgnoreCase(status)) return "待支付";
    if (ORDER_STATUS_CLOSED.equalsIgnoreCase(status)) return "已关闭";
    if (ORDER_STATUS_FAILED.equalsIgnoreCase(status)) return "支付失败";
    return "待创建";
  }

  private List<String> buildPaymentGuardTips(FamilyPortalModels.PaymentGuardResponse guard, Boolean autoPayEnabled) {
    List<String> tips = new ArrayList<>();
    if (safeDecimal(guard.getOutstanding()).compareTo(BigDecimal.ZERO) > 0) {
      tips.add("本月仍有待缴账单，建议优先处理避免影响服务安排");
    } else {
      tips.add("本月账单已结清，支付状态稳定");
    }
    if (safeInt(guard.getPendingOrderCount()) > 0) {
      tips.add("存在待支付充值单，请在 30 分钟内完成支付以免超时关闭");
    }
    if (safeInt(guard.getFailedOrderCount()) > 0 || safeInt(guard.getClosedOrderCount()) > 0) {
      tips.add("近期有异常充值记录，可在订单详情查看原因并重新发起");
    }
    if (!Boolean.TRUE.equals(autoPayEnabled)) {
      tips.add("可开启自动扣费提醒，降低忘记缴费风险");
    }
    if (tips.isEmpty()) {
      tips.add("支付链路运行正常，建议保持当前设置");
    }
    return tips;
  }

  private String resolveElderName(Long elderId) {
    if (elderId == null) {
      return "老人";
    }
    ElderProfile elder = elderMapper.selectById(elderId);
    return elder == null ? "老人" : defaultText(elder.getFullName(), "老人");
  }

  private void notifyFamilySafe(Long orgId, Long familyUserId, Long elderId, String eventType, String level,
      String title, String content, String pagePath, Map<String, String> placeholders) {
    if (orgId == null || familyUserId == null) {
      return;
    }
    try {
      FamilyNotifyCommand command = new FamilyNotifyCommand();
      command.setOrgId(orgId);
      command.setFamilyUserId(familyUserId);
      command.setElderId(elderId);
      command.setEventType(eventType);
      command.setLevel(level);
      command.setTitle(title);
      command.setContent(content);
      command.setPagePath(pagePath);
      command.setCreatedBy(familyUserId);
      if (placeholders != null && !placeholders.isEmpty()) {
        command.getPlaceholders().putAll(placeholders);
      }
      familyWechatNotifyService.notifyFamily(command);
    } catch (Exception ignored) {
      // 通知失败不影响主业务
    }
  }

  private FamilyPortalModels.TodoItem buildTodoItem(String type, String priority, String title,
      String description, String actionPath, String dueTime, String status) {
    FamilyPortalModels.TodoItem item = new FamilyPortalModels.TodoItem();
    item.setType(defaultText(type, "GENERAL"));
    item.setPriority(defaultText(priority, "NORMAL"));
    item.setTitle(defaultText(title, "待处理事项"));
    item.setDescription(defaultText(description, ""));
    item.setActionPath(defaultText(actionPath, ""));
    item.setDueTime(defaultText(dueTime, ""));
    item.setStatus(defaultText(status, "待处理"));
    item.setTodoKey(buildTodoKey(item));
    item.setActionable(!"建议优化".equals(item.getStatus()));
    item.setDone(false);
    item.setSnoozed(false);
    return item;
  }

  private List<FamilyPortalModels.MessageItem> filterMessageItems(List<FamilyPortalModels.MessageItem> items,
      String level, String type, Boolean unreadOnly) {
    if (items == null || items.isEmpty()) {
      return new ArrayList<>();
    }
    String normalizedLevel = level == null ? null : level.trim().toLowerCase(Locale.ROOT);
    String normalizedType = type == null ? null : type.trim();
    boolean onlyUnread = Boolean.TRUE.equals(unreadOnly);
    return items.stream()
        .filter(item -> {
          if (normalizedLevel == null || normalizedLevel.isBlank()) {
            return true;
          }
          return normalizedLevel.equals(defaultText(item.getLevel(), "").toLowerCase(Locale.ROOT));
        })
        .filter(item -> {
          if (normalizedType == null || normalizedType.isBlank()) {
            return true;
          }
          return normalizedType.equalsIgnoreCase(defaultText(item.getType(), ""));
        })
        .filter(item -> !onlyUnread || Boolean.TRUE.equals(item.getUnread()))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private <T> List<T> paginateList(List<T> list, int pageNo, int pageSize) {
    if (list == null || list.isEmpty()) {
      return List.of();
    }
    int safePageNo = Math.max(pageNo, 1);
    int safePageSize = Math.max(pageSize, 1);
    int from = (safePageNo - 1) * safePageSize;
    if (from >= list.size()) {
      return List.of();
    }
    int to = Math.min(from + safePageSize, list.size());
    return new ArrayList<>(list.subList(from, to));
  }

  private String buildTodoKey(FamilyPortalModels.TodoItem item) {
    String raw = defaultText(item == null ? null : item.getType(), "GENERAL") + "|"
        + defaultText(item == null ? null : item.getTitle(), "待处理事项") + "|"
        + defaultText(item == null ? null : item.getActionPath(), "") + "|"
        + defaultText(item == null ? null : item.getDescription(), "");
    String digest = UUID.nameUUIDFromBytes(raw.getBytes(StandardCharsets.UTF_8))
        .toString()
        .replace("-", "")
        .substring(0, 16);
    return "TODO-" + digest.toUpperCase(Locale.ROOT);
  }

  private FamilyPortalModels.TodoItem applyTodoItemActionState(Long orgId, Long familyUserId,
      FamilyPortalModels.TodoItem item) {
    if (item == null) {
      return null;
    }
    String todoKey = defaultText(item.getTodoKey(), buildTodoKey(item));
    item.setTodoKey(todoKey);
    item.setDone(false);
    item.setSnoozed(false);

    if ("建议优化".equals(defaultText(item.getStatus(), ""))) {
      item.setActionable(false);
      return item;
    }

    Map<String, Object> state = readStateMap(orgId, familyUserId, STATE_TODO_ACTION, todoKey);
    String action = defaultText(readText(state, "action"), "").toUpperCase(Locale.ROOT);
    if ("DONE".equals(action)) {
      item.setDone(true);
      item.setActionable(false);
      item.setStatus("已处理");
      return item;
    }
    if ("SNOOZE".equals(action)) {
      LocalDateTime snoozeUntil = parseDateTimeNullable(readText(state, "snoozeUntil"));
      if (snoozeUntil != null && snoozeUntil.isAfter(LocalDateTime.now())) {
        item.setSnoozed(true);
        item.setActionable(false);
        item.setStatus("稍后提醒");
        item.setDueTime(snoozeUntil.format(DATETIME_FMT));
        return item;
      }
      removeState(orgId, familyUserId, STATE_TODO_ACTION, todoKey);
    }
    item.setActionable(true);
    return item;
  }

  private int todoPriorityRank(String priority) {
    String value = defaultText(priority, "NORMAL").toUpperCase(Locale.ROOT);
    if ("URGENT".equals(value)) {
      return 0;
    }
    if ("HIGH".equals(value)) {
      return 1;
    }
    return 2;
  }

  private void createRechargeAbnormalTodo(FamilyRechargeOrder order, String reason) {
    FamilyPortalProperties.Recharge recharge = familyPortalProperties.getRecharge();
    if (order == null || order.getOrgId() == null || order.getFamilyUserId() == null
        || recharge == null || !recharge.isCreateAbnormalTodo()) {
      return;
    }
    String bizKey = defaultText(order.getOutTradeNo(), "unknown") + ":" + defaultText(order.getStatus(), "UNKNOWN");
    if (findState(order.getOrgId(), order.getFamilyUserId(), STATE_PAYMENT_ALERT, bizKey) != null) {
      return;
    }

    ElderProfile elder = order.getElderId() == null ? null : elderMapper.selectById(order.getElderId());
    String elderName = elder == null ? "未命名老人" : defaultText(elder.getFullName(), "未命名老人");

    OaTodo todo = new OaTodo();
    todo.setTenantId(order.getTenantId() == null ? order.getOrgId() : order.getTenantId());
    todo.setOrgId(order.getOrgId());
    todo.setTitle("充值异常跟进 - " + elderName);
    todo.setContent("订单号：" + order.getOutTradeNo()
        + "；金额：" + safeDecimal(order.getAmount())
        + "；状态：" + defaultText(order.getStatus(), "UNKNOWN")
        + "；原因：" + defaultText(reason, defaultText(order.getRemark(), "待人工确认")));
    todo.setDueTime(LocalDateTime.now().plusHours(2));
    todo.setStatus("OPEN");
    todo.setAssigneeId(null);
    todo.setAssigneeName("财务/客服跟进");
    todo.setCreatedBy(0L);
    oaTodoMapper.insert(todo);

    upsertState(order.getOrgId(), order.getFamilyUserId(), STATE_PAYMENT_ALERT, bizKey, Map.of(
        "todoId", todo.getId(),
        "outTradeNo", defaultText(order.getOutTradeNo(), ""),
        "status", defaultText(order.getStatus(), "UNKNOWN"),
        "reason", defaultText(reason, ""),
        "createdAt", formatFriendlyDateTime(LocalDateTime.now())));

    notifyFamilySafe(order.getOrgId(), order.getFamilyUserId(), order.getElderId(),
        "PAYMENT_RECHARGE_ABNORMAL", "urgent",
        "充值状态需关注",
        "订单" + defaultText(order.getOutTradeNo(), "") + "状态为" + defaultText(order.getStatus(), "UNKNOWN")
            + "，原因：" + defaultText(reason, "待人工确认"),
        "pages/payment-guard/index",
        Map.of(
            "elderName", elderName,
            "outTradeNo", defaultText(order.getOutTradeNo(), ""),
            "status", defaultText(order.getStatus(), "UNKNOWN"),
            "reason", defaultText(reason, "")));
  }

  private JsonNode resolveWechatNotifyBiz(JsonNode root) {
    if (root == null || root.isMissingNode()) {
      return null;
    }
    if (root.hasNonNull("out_trade_no")) {
      return root;
    }
    JsonNode resource = root.path("resource");
    if (resource.isObject() && resource.hasNonNull("ciphertext")) {
      String plain = decryptWechatResource(
          resource.path("associated_data").asText(""),
          resource.path("nonce").asText(""),
          resource.path("ciphertext").asText(""));
      return readJsonNode(plain);
    }
    return root.path("resource");
  }

  private boolean verifyWechatNotifyHeaders(Map<String, String> headers, String payload) {
    FamilyPortalProperties.WechatPay wechatPay = familyPortalProperties.getWechatPay();
    if (wechatPay == null || wechatPay.isSkipNotifySignatureVerify()) {
      return true;
    }

    Map<String, String> normalized = normalizeHeaders(headers);
    String signature = defaultText(normalized.get("wechatpay-signature"), null);
    String timestamp = defaultText(normalized.get("wechatpay-timestamp"), null);
    String nonce = defaultText(normalized.get("wechatpay-nonce"), null);
    String serialNo = defaultText(normalized.get("wechatpay-serial"), null);
    if (signature == null || timestamp == null || nonce == null) {
      return false;
    }

    long allowedSkewSeconds = wechatPay.getNotifyAllowedSkewSeconds() <= 0
        ? 300 : wechatPay.getNotifyAllowedSkewSeconds();
    if (!isWechatNotifyTimestampValid(timestamp, allowedSkewSeconds)) {
      return false;
    }

    PublicKey platformPublicKey;
    try {
      platformPublicKey = resolveWechatPlatformPublicKey(wechatPay, serialNo);
    } catch (Exception ex) {
      return false;
    }
    if (platformPublicKey == null) {
      return false;
    }

    String message = timestamp + "\n" + nonce + "\n" + defaultText(payload, "") + "\n";
    return verifyRsaSignature(platformPublicKey, message, signature);
  }

  private boolean isWechatNotifyReplay(Map<String, String> headers, String payload) {
    String replayKey = buildWechatNotifyReplayKey(headers, payload);
    if (replayKey == null) {
      return false;
    }
    long now = System.currentTimeMillis();
    cleanupWechatNotifyReplayCache(now);
    Long previous = wechatNotifyReplayCache.putIfAbsent(replayKey, now);
    if (previous == null) {
      return false;
    }
    if (now - previous > WECHAT_NOTIFY_REPLAY_TTL.toMillis()) {
      wechatNotifyReplayCache.put(replayKey, now);
      return false;
    }
    return true;
  }

  private String buildWechatNotifyReplayKey(Map<String, String> headers, String payload) {
    Map<String, String> normalized = normalizeHeaders(headers);
    String signature = defaultText(normalized.get("wechatpay-signature"), null);
    String timestamp = defaultText(normalized.get("wechatpay-timestamp"), null);
    String nonce = defaultText(normalized.get("wechatpay-nonce"), null);
    String digest = sha256Hex(defaultText(payload, ""));
    if (signature == null || timestamp == null || nonce == null) {
      return "fallback|" + digest;
    }
    return timestamp + "|" + nonce + "|" + signature + "|" + digest;
  }

  private void cleanupWechatNotifyReplayCache(long nowMillis) {
    long ttlMillis = WECHAT_NOTIFY_REPLAY_TTL.toMillis();
    wechatNotifyReplayCache.entrySet().removeIf(entry -> nowMillis - entry.getValue() > ttlMillis);
    if (wechatNotifyReplayCache.size() <= WECHAT_NOTIFY_REPLAY_CACHE_MAX_SIZE) {
      return;
    }
    List<Map.Entry<String, Long>> entries = new ArrayList<>(wechatNotifyReplayCache.entrySet());
    entries.sort(Comparator.comparingLong(Map.Entry::getValue));
    int overflow = wechatNotifyReplayCache.size() - WECHAT_NOTIFY_REPLAY_CACHE_MAX_SIZE;
    for (int i = 0; i < overflow && i < entries.size(); i++) {
      wechatNotifyReplayCache.remove(entries.get(i).getKey());
    }
  }

  private Map<String, String> normalizeHeaders(Map<String, String> headers) {
    if (headers == null || headers.isEmpty()) {
      return Map.of();
    }
    Map<String, String> normalized = new HashMap<>();
    headers.forEach((k, v) -> {
      if (k != null && v != null) {
        normalized.put(k.toLowerCase(Locale.ROOT), v);
      }
    });
    return normalized;
  }

  private boolean isWechatNotifyTimestampValid(String timestamp, long allowedSkewSeconds) {
    Long seconds = parseLongQuietly(timestamp);
    if (seconds == null || seconds <= 0) {
      return false;
    }
    long now = System.currentTimeMillis() / 1000;
    return Math.abs(now - seconds) <= Math.max(allowedSkewSeconds, 60L);
  }

  private PublicKey resolveWechatPlatformPublicKey(FamilyPortalProperties.WechatPay wechatPay, String serialNo) {
    PublicKey configured = loadConfiguredPlatformPublicKey(wechatPay, serialNo);
    if (configured != null) {
      return configured;
    }

    LocalDateTime now = LocalDateTime.now();
    boolean shouldRefresh = wechatPlatformCertSyncedAt == null
        || Duration.between(wechatPlatformCertSyncedAt, now).compareTo(WECHAT_PLATFORM_CERT_CACHE_TTL) >= 0
        || (serialNo != null && !wechatPlatformCertCache.containsKey(serialNo.toUpperCase(Locale.ROOT)));
    if (shouldRefresh) {
      refreshWechatPlatformCertificateCache(wechatPay);
    }
    return selectPlatformPublicKeyFromCache(serialNo);
  }

  private PublicKey loadConfiguredPlatformPublicKey(FamilyPortalProperties.WechatPay wechatPay, String serialNo) {
    String certificatePem = defaultText(wechatPay.getPlatformCertificatePem(), null);
    if (certificatePem == null) {
      return null;
    }
    X509Certificate certificate = parseX509Certificate(certificatePem);
    String configuredSerial = defaultText(wechatPay.getPlatformSerialNo(), null);
    String certSerial = certificate.getSerialNumber().toString(16).toUpperCase(Locale.ROOT);
    if (serialNo != null) {
      String normalizedHeaderSerial = serialNo.toUpperCase(Locale.ROOT);
      if (configuredSerial != null && !normalizedHeaderSerial.equals(configuredSerial.toUpperCase(Locale.ROOT))) {
        return null;
      }
      if (configuredSerial == null && !normalizedHeaderSerial.equals(certSerial)) {
        return null;
      }
    }
    return certificate.getPublicKey();
  }

  private void refreshWechatPlatformCertificateCache(FamilyPortalProperties.WechatPay wechatPay) {
    String certificatesUrl = requireText(wechatPay.getCertificatesUrl(), "未配置微信平台证书下载地址");
    JsonNode response = requestWechatApi(wechatPay, HttpMethod.GET, certificatesUrl, "");
    JsonNode data = response.path("data");
    if (!data.isArray() || data.size() == 0) {
      throw new IllegalStateException("微信平台证书拉取失败：返回为空");
    }

    Map<String, PlatformCertificateHolder> refreshed = new HashMap<>();
    for (JsonNode item : data) {
      String serialNo = defaultText(item.path("serial_no").asText(""), null);
      if (serialNo == null) {
        continue;
      }
      String certificatePem = defaultText(item.path("certificate").asText(""), null);
      if (certificatePem == null) {
        JsonNode encryptCertificate = item.path("encrypt_certificate");
        if (encryptCertificate.isObject() && encryptCertificate.hasNonNull("ciphertext")) {
          certificatePem = decryptWechatResource(
              encryptCertificate.path("associated_data").asText(""),
              encryptCertificate.path("nonce").asText(""),
              encryptCertificate.path("ciphertext").asText(""));
        }
      }
      if (certificatePem == null) {
        continue;
      }
      X509Certificate certificate = parseX509Certificate(certificatePem);
      LocalDateTime expireAt = parseWechatTime(item.path("expire_time").asText(null));
      refreshed.put(serialNo.toUpperCase(Locale.ROOT), new PlatformCertificateHolder(certificate.getPublicKey(), expireAt));
    }

    if (refreshed.isEmpty()) {
      throw new IllegalStateException("微信平台证书解析失败");
    }
    wechatPlatformCertCache.clear();
    wechatPlatformCertCache.putAll(refreshed);
    wechatPlatformCertSyncedAt = LocalDateTime.now();
  }

  private PublicKey selectPlatformPublicKeyFromCache(String serialNo) {
    LocalDateTime now = LocalDateTime.now();
    if (serialNo != null) {
      PlatformCertificateHolder holder = wechatPlatformCertCache.get(serialNo.toUpperCase(Locale.ROOT));
      if (holder == null) {
        return null;
      }
      if (holder.expireAt != null && now.isAfter(holder.expireAt)) {
        wechatPlatformCertCache.remove(serialNo.toUpperCase(Locale.ROOT));
        return null;
      }
      return holder.publicKey;
    }
    for (PlatformCertificateHolder holder : wechatPlatformCertCache.values()) {
      if (holder.expireAt != null && now.isAfter(holder.expireAt)) {
        continue;
      }
      return holder.publicKey;
    }
    return null;
  }

  private boolean verifyRsaSignature(PublicKey publicKey, String message, String signatureBase64) {
    try {
      Signature signature = Signature.getInstance("SHA256withRSA");
      signature.initVerify(publicKey);
      signature.update(message.getBytes(StandardCharsets.UTF_8));
      return signature.verify(Base64.getDecoder().decode(signatureBase64));
    } catch (Exception ex) {
      return false;
    }
  }

  private String decryptWechatResource(String associatedData, String nonce, String ciphertext) {
    String apiV3Key = requireText(familyPortalProperties.getWechatPay().getApiV3Key(), "未配置微信支付 APIv3 Key");
    if (apiV3Key.length() != 32) {
      throw new IllegalStateException("微信支付 APIv3 Key 长度必须为32位");
    }
    try {
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      SecretKeySpec keySpec = new SecretKeySpec(apiV3Key.getBytes(StandardCharsets.UTF_8), "AES");
      GCMParameterSpec spec = new GCMParameterSpec(128, nonce.getBytes(StandardCharsets.UTF_8));
      cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);
      if (associatedData != null && !associatedData.isBlank()) {
        cipher.updateAAD(associatedData.getBytes(StandardCharsets.UTF_8));
      }
      byte[] plainBytes = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
      return new String(plainBytes, StandardCharsets.UTF_8);
    } catch (Exception ex) {
      throw new IllegalStateException("微信支付回调解密失败", ex);
    }
  }

  private String canonicalPath(URI uri) {
    if (uri == null) {
      return "/";
    }
    String path = uri.getRawPath();
    if (path == null || path.isBlank()) {
      path = "/";
    }
    if (uri.getRawQuery() != null && !uri.getRawQuery().isBlank()) {
      return path + "?" + uri.getRawQuery();
    }
    return path;
  }

  private String signJsapiPay(FamilyPortalProperties.WechatPay wechatPay, String timeStamp, String nonceStr, String payPackage) {
    String appId = requireText(wechatPay.getAppId(), "未配置微信支付 appId");
    String message = appId + "\n" + timeStamp + "\n" + nonceStr + "\n" + payPackage + "\n";
    return signByPrivateKey(requireText(wechatPay.getPrivateKeyPem(), "未配置微信支付私钥"), message);
  }

  private String signByPrivateKey(String privateKeyPem, String message) {
    try {
      PrivateKey privateKey = parsePrivateKey(privateKeyPem);
      Signature signature = Signature.getInstance("SHA256withRSA");
      signature.initSign(privateKey);
      signature.update(message.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(signature.sign());
    } catch (Exception ex) {
      throw new IllegalStateException("签名失败", ex);
    }
  }

  private X509Certificate parseX509Certificate(String certificatePem) {
    String normalized = requireText(certificatePem, "微信平台证书为空").replace("\\n", "\n");
    String cleaned = normalized
        .replace("-----BEGIN CERTIFICATE-----", "")
        .replace("-----END CERTIFICATE-----", "")
        .replaceAll("\\s+", "");
    try {
      byte[] certBytes = Base64.getDecoder().decode(cleaned);
      CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
      return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certBytes));
    } catch (Exception ex) {
      throw new IllegalStateException("解析微信平台证书失败", ex);
    }
  }

  private PrivateKey parsePrivateKey(String privateKeyPem) {
    String normalized = requireText(privateKeyPem, "未配置微信支付私钥").replace("\\n", "\n");
    String cleaned = normalized
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
        .replaceAll("\\s+", "");
    try {
      byte[] keyBytes = Base64.getDecoder().decode(cleaned);
      PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
      return KeyFactory.getInstance("RSA").generatePrivate(spec);
    } catch (Exception ex) {
      throw new IllegalStateException("解析微信支付私钥失败", ex);
    }
  }

  private String randomNonce() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  private String buildWechatOutTradeNo() {
    return "FMWX" + System.currentTimeMillis() + randomNonce().substring(0, 10).toUpperCase(Locale.ROOT);
  }

  private int toFen(BigDecimal amount) {
    return amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).intValue();
  }

  private String requireText(String text, String error) {
    if (text == null || text.isBlank()) {
      throw new IllegalStateException(error);
    }
    return text.trim();
  }

  private JsonNode readJsonNode(String json) {
    if (json == null || json.isBlank()) {
      return objectMapper.createObjectNode();
    }
    try {
      return objectMapper.readTree(json);
    } catch (Exception ex) {
      throw new IllegalStateException("解析JSON失败", ex);
    }
  }

  private String writeJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (Exception ex) {
      throw new IllegalStateException("序列化JSON失败", ex);
    }
  }

  private LocalDateTime parseWechatTime(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return java.time.OffsetDateTime.parse(value).toLocalDateTime();
    } catch (Exception ignore) {
      return parseDateTime(value);
    }
  }

  private FamilyPortalModels.MallOrderPreviewResponse toMallOrderPreview(OrderPreviewResponse preview) {
    FamilyPortalModels.MallOrderPreviewResponse response = new FamilyPortalModels.MallOrderPreviewResponse();
    if (preview == null) {
      response.setAllowed(false);
      response.setStatus("ERROR");
      response.setMessage("预检失败，请稍后重试");
      return response;
    }
    response.setAllowed(preview.isAllowed());
    response.setStatus(defaultText(preview.getStatus(), ""));
    response.setMessage(defaultText(preview.getMessage(), preview.isAllowed() ? "可下单" : "当前不可下单"));
    response.setElderId(preview.getElderId());
    response.setProductId(preview.getProductId());
    response.setProductName(defaultText(preview.getProductName(), ""));
    response.setQty(preview.getQty());
    response.setPointsRequired(preview.getPointsRequired());
    List<String> reasons = preview.getReasons() == null ? List.of() : preview.getReasons().stream()
        .map(this::toMallForbiddenReasonText)
        .filter(this::hasText)
        .toList();
    response.setReasons(reasons);
    return response;
  }

  private FamilyPortalModels.MallOrderSubmitResponse toMallOrderSubmit(OrderSubmitResponse submit) {
    FamilyPortalModels.MallOrderSubmitResponse response = new FamilyPortalModels.MallOrderSubmitResponse();
    if (submit == null) {
      response.setAllowed(false);
      response.setStatus("ERROR");
      response.setMessage("下单失败，请稍后重试");
      return response;
    }
    response.setAllowed(submit.isAllowed());
    response.setStatus(defaultText(submit.getStatus(), ""));
    response.setMessage(defaultText(submit.getMessage(), submit.isAllowed() ? "下单成功" : "下单失败"));
    response.setOrderId(submit.getOrderId());
    response.setOrderNo(defaultText(submit.getOrderNo(), ""));
    response.setPointsDeducted(submit.getPointsDeducted());
    response.setBalanceAfter(submit.getBalanceAfter());
    response.setPreview(toMallOrderPreview(submit.getPreview()));
    return response;
  }

  private String toMallForbiddenReasonText(ForbiddenReason reason) {
    if (reason == null) {
      return "";
    }
    String disease = defaultText(reason.getDiseaseName(), "");
    String tag = defaultText(reason.getTagName(), "");
    if (!hasText(disease) && !hasText(tag)) {
      return "";
    }
    if (!hasText(disease)) {
      return "命中禁忌标签：" + tag;
    }
    if (!hasText(tag)) {
      return "慢病限制：" + disease;
    }
    return disease + "（禁忌：" + tag + "）";
  }

  private String resolveMallOrderStatusText(Integer status) {
    if (status == null) {
      return "状态未知";
    }
    return switch (status) {
      case 1 -> "待处理";
      case 2 -> "已支付";
      case 3 -> "已完成";
      case 4 -> "已取消";
      case 5 -> "已退款";
      default -> "处理中";
    };
  }

  private String resolveMallPayStatusText(Integer status) {
    if (status == null) {
      return "待支付";
    }
    return switch (status) {
      case 1 -> "已支付";
      case 2 -> "已退款";
      default -> "待支付";
    };
  }

  private FamilyPortalModels.MallOrderItem toMallOrderSummary(StoreOrder order) {
    FamilyPortalModels.MallOrderItem item = new FamilyPortalModels.MallOrderItem();
    if (order == null) {
      item.setCanCancel(false);
      item.setCancelHint(resolveCancelMallOrderHint(null));
      item.setCanRefund(false);
      item.setRefundHint(resolveRefundMallOrderHint(null));
      return item;
    }
    item.setOrderId(order.getId());
    item.setOrderNo(defaultText(order.getOrderNo(), ""));
    item.setElderId(order.getElderId());
    ElderProfile elder = order.getElderId() == null ? null : elderMapper.selectOne(
        Wrappers.lambdaQuery(ElderProfile.class)
            .eq(ElderProfile::getIsDeleted, 0)
            .eq(ElderProfile::getOrgId, order.getOrgId())
            .eq(ElderProfile::getId, order.getElderId())
            .last("LIMIT 1"));
    item.setElderName(elder == null ? "老人" : defaultText(elder.getFullName(), "老人"));
    item.setProductName("商城商品");
    item.setQuantity(1);
    item.setUnitPrice(safeDecimal(order.getTotalAmount()));
    item.setTotalAmount(safeDecimal(order.getTotalAmount()));
    item.setPointsUsed(safeInt(order.getPointsUsed()));
    item.setOrderStatus(order.getOrderStatus());
    item.setOrderStatusText(resolveMallOrderStatusText(order.getOrderStatus()));
    item.setPayStatus(order.getPayStatus());
    item.setPayStatusText(resolveMallPayStatusText(order.getPayStatus()));
    item.setCreateTime(order.getCreateTime() == null ? "" : order.getCreateTime().format(DATETIME_FMT));
    item.setPayTime(order.getPayTime() == null ? "" : order.getPayTime().format(DATETIME_FMT));
    item.setCanCancel(canCancelMallOrder(order));
    item.setCancelHint(resolveCancelMallOrderHint(order));
    item.setCanRefund(canRefundMallOrder(order));
    item.setRefundHint(resolveRefundMallOrderHint(order));
    return item;
  }

  private FamilyPortalModels.MallOrderActionResponse toMallOrderActionResponse(StoreOrder order, String action, String message) {
    FamilyPortalModels.MallOrderActionResponse response = new FamilyPortalModels.MallOrderActionResponse();
    response.setOrderId(order == null ? null : order.getId());
    response.setAction(defaultText(action, ""));
    response.setSuccess(Boolean.TRUE);
    response.setMessage(defaultText(message, "操作成功"));
    response.setOrderStatus(order == null ? null : order.getOrderStatus());
    response.setOrderStatusText(resolveMallOrderStatusText(order == null ? null : order.getOrderStatus()));
    response.setPayStatus(order == null ? null : order.getPayStatus());
    response.setPayStatusText(resolveMallPayStatusText(order == null ? null : order.getPayStatus()));
    response.setCanCancel(canCancelMallOrder(order));
    response.setCancelHint(resolveCancelMallOrderHint(order));
    response.setCanRefund(canRefundMallOrder(order));
    response.setRefundHint(resolveRefundMallOrderHint(order));
    return response;
  }

  private String resolveCancelMallOrderHint(StoreOrder order) {
    if (order == null || order.getIsDeleted() != null && order.getIsDeleted() == 1) {
      return "订单不存在或已失效";
    }
    int status = safeInt(order.getOrderStatus());
    return switch (status) {
      case 1, 2 -> "可取消";
      case 3 -> "订单已完成，请改为申请退款";
      case 4 -> "订单已取消，无法重复取消";
      case 5 -> "订单已退款，无法取消";
      default -> "当前订单状态不可取消";
    };
  }

  private String resolveRefundMallOrderHint(StoreOrder order) {
    if (order == null || order.getIsDeleted() != null && order.getIsDeleted() == 1) {
      return "订单不存在或已失效";
    }
    int status = safeInt(order.getOrderStatus());
    if (status == 4) {
      return "订单已取消，无需退款";
    }
    if (status == 5) {
      return "订单已退款，无需重复提交";
    }
    int payStatus = safeInt(order.getPayStatus());
    if (payStatus == 1 || status == 3) {
      return "可申请退款";
    }
    if (status == 1 && payStatus == 0) {
      return "订单尚未支付，建议先取消订单";
    }
    return "当前订单状态不可退款";
  }

  private boolean canCancelMallOrder(StoreOrder order) {
    if (order == null || order.getIsDeleted() != null && order.getIsDeleted() == 1) {
      return false;
    }
    int status = safeInt(order.getOrderStatus());
    return status == 1 || status == 2;
  }

  private boolean canRefundMallOrder(StoreOrder order) {
    if (order == null || order.getIsDeleted() != null && order.getIsDeleted() == 1) {
      return false;
    }
    int status = safeInt(order.getOrderStatus());
    if (status == 4 || status == 5) {
      return false;
    }
    int payStatus = safeInt(order.getPayStatus());
    return payStatus == 1 || status == 3;
  }

  private StoreOrder loadAuthorizedMallOrder(Long orgId, Long familyUserId, Long orderId) {
    if (orderId == null || orderId <= 0) {
      throw new IllegalArgumentException("订单ID不能为空");
    }
    List<Long> elderIds = ensureBoundElderIds(extractElderIds(listBoundRelations(orgId, familyUserId)));
    StoreOrder order = storeOrderMapper.selectOne(
        Wrappers.lambdaQuery(StoreOrder.class)
            .eq(StoreOrder::getIsDeleted, 0)
            .eq(StoreOrder::getOrgId, orgId)
            .eq(StoreOrder::getId, orderId)
            .in(StoreOrder::getElderId, elderIds)
            .last("LIMIT 1"));
    if (order == null) {
      throw new IllegalArgumentException("订单不存在或无权限访问");
    }
    return order;
  }

  private List<ElderFamily> listBoundRelations(Long orgId, Long familyUserId) {
    return elderFamilyMapper.selectList(
        Wrappers.lambdaQuery(ElderFamily.class)
            .eq(ElderFamily::getIsDeleted, 0)
            .eq(ElderFamily::getOrgId, orgId)
            .eq(ElderFamily::getFamilyUserId, familyUserId)
            .orderByDesc(ElderFamily::getIsPrimary)
            .orderByDesc(ElderFamily::getUpdateTime));
  }

  private List<Long> extractElderIds(List<ElderFamily> relations) {
    return relations.stream()
        .map(ElderFamily::getElderId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
  }

  private Long resolveTargetElderId(List<Long> elderIds, Long elderId) {
    ensureBoundElderIds(elderIds);
    if (elderId == null) {
      return elderIds.get(0);
    }
    if (!elderIds.contains(elderId)) {
      throw new IllegalArgumentException("无权限查看该老人信息");
    }
    return elderId;
  }

  private List<Long> ensureBoundElderIds(List<Long> elderIds) {
    if (elderIds == null || elderIds.isEmpty()) {
      throw new IllegalArgumentException("请先绑定老人信息");
    }
    return elderIds;
  }

  private Map<Long, ElderProfile> loadElderMap(Long orgId, Collection<Long> elderIds) {
    if (elderIds == null || elderIds.isEmpty()) {
      return Map.of();
    }
    return elderMapper.selectList(
            Wrappers.lambdaQuery(ElderProfile.class)
                .eq(ElderProfile::getIsDeleted, 0)
                .eq(ElderProfile::getOrgId, orgId)
                .in(ElderProfile::getId, elderIds))
        .stream()
        .collect(Collectors.toMap(ElderProfile::getId, item -> item, (a, b) -> a));
  }

  private Map<Long, String> loadRoomTextMap(Long orgId, Collection<ElderProfile> elders) {
    if (elders == null || elders.isEmpty()) {
      return Map.of();
    }
    List<Long> bedIds = elders.stream()
        .map(ElderProfile::getBedId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    if (bedIds.isEmpty()) {
      return Map.of();
    }
    Map<Long, Bed> bedMap = bedMapper.selectList(
            Wrappers.lambdaQuery(Bed.class)
                .eq(Bed::getIsDeleted, 0)
                .eq(Bed::getOrgId, orgId)
                .in(Bed::getId, bedIds))
        .stream()
        .collect(Collectors.toMap(Bed::getId, item -> item, (a, b) -> a));

    List<Long> roomIds = bedMap.values().stream()
        .map(Bed::getRoomId)
        .filter(Objects::nonNull)
        .distinct()
        .toList();

    Map<Long, Room> roomMap = roomIds.isEmpty() ? Map.of() : roomMapper.selectList(
            Wrappers.lambdaQuery(Room.class)
                .eq(Room::getIsDeleted, 0)
                .eq(Room::getOrgId, orgId)
                .in(Room::getId, roomIds))
        .stream().collect(Collectors.toMap(Room::getId, item -> item, (a, b) -> a));

    Map<Long, String> result = new HashMap<>();
    for (ElderProfile elder : elders) {
      if (elder.getBedId() == null) {
        continue;
      }
      Bed bed = bedMap.get(elder.getBedId());
      Room room = bed == null ? null : roomMap.get(bed.getRoomId());
      if (room != null) {
        result.put(elder.getId(), defaultText(room.getBuilding(), "") + defaultText(room.getFloorNo(), "")
            + defaultText(room.getRoomNo(), "") + " " + defaultText(bed.getBedNo(), ""));
      }
    }
    return result;
  }

  private Map<Long, String> loadLocationTextMap(Long orgId, List<Long> elderIds, Map<Long, String> roomTextMap) {
    Map<Long, String> result = new HashMap<>(roomTextMap);

    List<ElderMedicalOutingRecord> medicalOuts = elderMedicalOutingRecordMapper.selectList(
        Wrappers.lambdaQuery(ElderMedicalOutingRecord.class)
            .eq(ElderMedicalOutingRecord::getIsDeleted, 0)
            .eq(ElderMedicalOutingRecord::getOrgId, orgId)
            .in(ElderMedicalOutingRecord::getElderId, elderIds)
            .eq(ElderMedicalOutingRecord::getStatus, "OUT"));
    for (ElderMedicalOutingRecord record : medicalOuts) {
      result.put(record.getElderId(), defaultText(record.getHospitalName(), "医院") + " " + defaultText(record.getDepartment(), ""));
    }

    List<ElderOutingRecord> outs = elderOutingRecordMapper.selectList(
        Wrappers.lambdaQuery(ElderOutingRecord.class)
            .eq(ElderOutingRecord::getIsDeleted, 0)
            .eq(ElderOutingRecord::getOrgId, orgId)
            .in(ElderOutingRecord::getElderId, elderIds)
            .eq(ElderOutingRecord::getStatus, "OUT"));
    for (ElderOutingRecord record : outs) {
      result.putIfAbsent(record.getElderId(), "院外活动");
    }
    return result;
  }

  private FamilyPortalModels.HealthSummary buildHealthSummary(Long orgId, Long elderId, LocalDateTime latestTime) {
    List<HealthDataRecord> records = healthDataRecordMapper.selectList(
        Wrappers.lambdaQuery(HealthDataRecord.class)
            .eq(HealthDataRecord::getIsDeleted, 0)
            .eq(HealthDataRecord::getOrgId, orgId)
            .eq(HealthDataRecord::getElderId, elderId)
            .orderByDesc(HealthDataRecord::getMeasuredAt)
            .last("LIMIT 120"));

    Map<String, List<HealthDataRecord>> grouped = new HashMap<>();
    for (HealthDataRecord record : records) {
      String metric = normalizeMetricType(record.getDataType());
      if (metric == null) {
        continue;
      }
      grouped.computeIfAbsent(metric, k -> new ArrayList<>()).add(record);
    }

    long abnormalCount = healthDataRecordMapper.selectCount(
        Wrappers.lambdaQuery(HealthDataRecord.class)
            .eq(HealthDataRecord::getIsDeleted, 0)
            .eq(HealthDataRecord::getOrgId, orgId)
            .eq(HealthDataRecord::getElderId, elderId)
            .eq(HealthDataRecord::getAbnormalFlag, 1)
            .ge(HealthDataRecord::getMeasuredAt, LocalDate.now().atStartOfDay()));

    FamilyPortalModels.HealthSummary summary = new FamilyPortalModels.HealthSummary();
    summary.setLevel(abnormalCount > 0 ? "warning" : "normal");
    summary.setStateText(abnormalCount > 0
        ? "今日有 " + abnormalCount + " 项指标需关注"
        : "今日状态平稳，晨检已完成");
    summary.setUpdateTime(latestTime == null ? "--:--" : latestTime.format(HHMM_FMT));
    summary.getMetrics().add(buildMetric("体温", METRIC_TEMP, grouped.get(METRIC_TEMP)));
    summary.getMetrics().add(buildMetric("血压", METRIC_BP, grouped.get(METRIC_BP)));
    summary.getMetrics().add(buildMetric("心率", METRIC_HR, grouped.get(METRIC_HR)));
    summary.getMetrics().add(buildMetric("血糖", METRIC_SUGAR, grouped.get(METRIC_SUGAR)));
    summary.setSuggestion(abnormalCount > 0
        ? "院方已安排持续观察，请家属关注后续提醒。"
        : "今日状态整体稳定，请家属放心。");
    return summary;
  }

  private FamilyPortalModels.HealthMetric buildMetric(String name, String metricType, List<HealthDataRecord> records) {
    FamilyPortalModels.HealthMetric metric = new FamilyPortalModels.HealthMetric();
    metric.setName(name);
    if (records == null || records.isEmpty()) {
      metric.setValue("--");
      metric.setUnit(resolveMetricUnit(metricType));
      metric.setStatus("待记录");
      metric.setTrend("暂无趋势");
      return metric;
    }
    HealthDataRecord current = records.get(0);
    HealthDataRecord previous = records.size() > 1 ? records.get(1) : null;
    metric.setValue(defaultText(current.getDataValue(), "--"));
    metric.setUnit(resolveMetricUnit(metricType));
    metric.setStatus(resolveMetricStatus(metricType, current.getDataValue(), current.getAbnormalFlag()));
    metric.setTrend(resolveMetricTrend(metricType, current.getDataValue(), previous == null ? null : previous.getDataValue()));
    return metric;
  }

  private List<String> buildFocusEvents(FamilyPortalModels.HomeDashboardResponse dashboard) {
    List<String> events = new ArrayList<>();
    for (FamilyPortalModels.ScheduleItem item : dashboard.getSchedules()) {
      if ("待开始".equals(item.getStatus()) || "进行中".equals(item.getStatus())) {
        events.add(item.getTime() + " " + item.getName() + "（" + item.getStatus() + "）");
      }
      if (events.size() >= 3) {
        break;
      }
    }
    if (events.isEmpty()) {
      events.add("今日护理计划已排班，系统持续跟踪执行进度");
    }
    return events;
  }

  private String resolveElderStatusText(Long orgId, Long elderId, Integer elderStatus) {
    boolean medicalOut = elderMedicalOutingRecordMapper.selectCount(
        Wrappers.lambdaQuery(ElderMedicalOutingRecord.class)
            .eq(ElderMedicalOutingRecord::getIsDeleted, 0)
            .eq(ElderMedicalOutingRecord::getOrgId, orgId)
            .eq(ElderMedicalOutingRecord::getElderId, elderId)
            .eq(ElderMedicalOutingRecord::getStatus, "OUT")) > 0;
    if (medicalOut) {
      return "就医中";
    }

    boolean out = elderOutingRecordMapper.selectCount(
        Wrappers.lambdaQuery(ElderOutingRecord.class)
            .eq(ElderOutingRecord::getIsDeleted, 0)
            .eq(ElderOutingRecord::getOrgId, orgId)
            .eq(ElderOutingRecord::getElderId, elderId)
            .eq(ElderOutingRecord::getStatus, "OUT")) > 0;
    if (out) {
      return "外出";
    }
    if (elderStatus == null) {
      return "在院";
    }
    if (elderStatus == 1) {
      return "在院";
    }
    if (elderStatus == 2) {
      return "外出";
    }
    if (elderStatus == 3) {
      return "离院";
    }
    return "在院";
  }

  private String resolveStatusType(String status) {
    if ("在院".equals(status)) {
      return "normal";
    }
    if ("外出".equals(status)) {
      return "active";
    }
    if ("就医中".equals(status)) {
      return "warning";
    }
    return "danger";
  }

  private String resolveSpecialTag(ElderProfile elder, String status) {
    if (elder == null) {
      return "重点关注";
    }
    if ("就医中".equals(status)) {
      return "今日复查";
    }
    if (elder.getRiskPrecommit() != null && !elder.getRiskPrecommit().isBlank()) {
      return elder.getRiskPrecommit();
    }
    if (elder.getCareLevel() != null && elder.getCareLevel().contains("三")) {
      return "重点护理";
    }
    return "日常观察";
  }

  private String resolveDynamicPreview(Long orgId, Long elderId) {
    HealthNursingLog log = healthNursingLogMapper.selectOne(
        Wrappers.lambdaQuery(HealthNursingLog.class)
            .eq(HealthNursingLog::getIsDeleted, 0)
            .eq(HealthNursingLog::getOrgId, orgId)
            .eq(HealthNursingLog::getElderId, elderId)
            .orderByDesc(HealthNursingLog::getLogTime)
            .last("LIMIT 1"));
    if (log != null && log.getContent() != null && !log.getContent().isBlank()) {
      return log.getContent();
    }

    HealthDataRecord record = healthDataRecordMapper.selectOne(
        Wrappers.lambdaQuery(HealthDataRecord.class)
            .eq(HealthDataRecord::getIsDeleted, 0)
            .eq(HealthDataRecord::getOrgId, orgId)
            .eq(HealthDataRecord::getElderId, elderId)
            .orderByDesc(HealthDataRecord::getMeasuredAt)
            .last("LIMIT 1"));
    if (record != null) {
      return defaultText(record.getDataType(), "健康检测") + "：" + defaultText(record.getDataValue(), "已完成");
    }
    return "今日暂无新增动态";
  }

  private HealthOverview buildHealthOverview(Long orgId, Long elderId) {
    List<HealthDataRecord> records = healthDataRecordMapper.selectList(
        Wrappers.lambdaQuery(HealthDataRecord.class)
            .eq(HealthDataRecord::getIsDeleted, 0)
            .eq(HealthDataRecord::getOrgId, orgId)
            .eq(HealthDataRecord::getElderId, elderId)
            .orderByDesc(HealthDataRecord::getMeasuredAt)
            .last("LIMIT 10"));
    HealthOverview overview = new HealthOverview();
    if (records.isEmpty()) {
      overview.overviewText = "待更新";
      overview.latestTimeText = "--:--";
      return overview;
    }
    long abnormal = records.stream().filter(item -> Integer.valueOf(1).equals(item.getAbnormalFlag())).count();
    overview.overviewText = abnormal > 0 ? "需关注" : "平稳";
    LocalDateTime latest = records.get(0).getMeasuredAt();
    overview.latestTime = latest;
    overview.latestTimeText = latest == null ? "--:--" : latest.format(HHMM_FMT);
    return overview;
  }

  private String resolvePrimaryElderName(Long orgId, List<Long> elderIds) {
    if (elderIds == null || elderIds.isEmpty()) {
      return "未绑定老人";
    }
    ElderProfile elder = elderMapper.selectOne(
        Wrappers.lambdaQuery(ElderProfile.class)
            .eq(ElderProfile::getIsDeleted, 0)
            .eq(ElderProfile::getOrgId, orgId)
            .eq(ElderProfile::getId, elderIds.get(0))
            .last("LIMIT 1"));
    return elder == null ? "未命名老人" : defaultText(elder.getFullName(), "未命名老人");
  }

  private List<String> buildMessageTimeline(OaNotice notice) {
    LocalDateTime publish = notice.getPublishTime() == null ? notice.getCreateTime() : notice.getPublishTime();
    String time = publish == null ? "--:--" : publish.format(HHMM_FMT);
    return List.of(
        time + " 消息已生成",
        time + " 护理团队已收到提醒",
        "如需协助可点击下方联系按钮"
    );
  }

  private String resolveMessageLevel(String title, String content) {
    String text = (defaultText(title, "") + " " + defaultText(content, "")).toLowerCase(Locale.ROOT);
    if (text.contains("紧急") || text.contains("异常") || text.contains("告警")) {
      return "urgent";
    }
    if (text.contains("余额") || text.contains("账单") || text.contains("提醒") || text.contains("复查")) {
      return "warning";
    }
    return "normal";
  }

  private String resolveMessageLevelText(String level) {
    if ("urgent".equals(level)) {
      return "一级提醒";
    }
    if ("warning".equals(level)) {
      return "二级提醒";
    }
    return "三级提醒";
  }

  private String resolveMessageType(String title, String content) {
    String text = (defaultText(title, "") + " " + defaultText(content, "")).toLowerCase(Locale.ROOT);
    if (text.contains("紧急") || text.contains("异常")) return "紧急事件";
    if (text.contains("余额") || text.contains("账单")) return "费用提醒";
    if (text.contains("活动")) return "活动通知";
    if (text.contains("健康") || text.contains("血压") || text.contains("血糖")) return "健康提醒";
    return "系统公告";
  }

  private String resolveEmergencyPhone(Long orgId) {
    Org org = orgMapper.selectById(orgId);
    return defaultText(org == null ? null : org.getContactPhone(), "13800138000");
  }

  private String normalizeMetricType(String dataType) {
    String text = defaultText(dataType, "").toLowerCase(Locale.ROOT);
    if (text.contains("血压") || text.contains("bp") || text.contains("pressure")) {
      return METRIC_BP;
    }
    if (text.contains("体温") || text.contains("temp")) {
      return METRIC_TEMP;
    }
    if (text.contains("心率") || text.contains("heart")) {
      return METRIC_HR;
    }
    if (text.contains("血糖") || text.contains("glucose") || text.contains("sugar")) {
      return METRIC_SUGAR;
    }
    return null;
  }

  private String resolveMetricUnit(String metricType) {
    if (METRIC_BP.equals(metricType)) return "mmHg";
    if (METRIC_TEMP.equals(metricType)) return "℃";
    if (METRIC_HR.equals(metricType)) return "次/分";
    if (METRIC_SUGAR.equals(metricType)) return "mmol/L";
    return "";
  }

  private String resolveMetricStatus(String metricType, String value, Integer abnormalFlag) {
    if (Integer.valueOf(1).equals(abnormalFlag)) {
      return "需关注";
    }
    if (METRIC_BP.equals(metricType)) {
      int[] bp = parseBloodPressure(value);
      return bp[0] >= 140 || bp[1] >= 90 ? "轻度偏高" : "正常";
    }
    if (METRIC_TEMP.equals(metricType)) {
      return parseDouble(value, 0D) > 37.3 ? "偏高" : "正常";
    }
    if (METRIC_HR.equals(metricType)) {
      int hr = parseInteger(value, 0);
      return (hr > 100 || hr < 50) ? "异常" : "正常";
    }
    if (METRIC_SUGAR.equals(metricType)) {
      return parseDouble(value, 0D) > 7.8 ? "偏高" : "正常";
    }
    return "正常";
  }

  private String resolveMetricTrend(String metricType, String current, String previous) {
    if (previous == null || previous.isBlank()) {
      return "暂无对比";
    }
    if (METRIC_BP.equals(metricType)) {
      int[] c = parseBloodPressure(current);
      int[] p = parseBloodPressure(previous);
      int diff = c[0] - p[0];
      if (diff > 0) return "较昨日↑";
      if (diff < 0) return "较昨日↓";
      return "与昨日持平";
    }
    double c = parseDouble(current, 0D);
    double p = parseDouble(previous, 0D);
    if (c > p) return "较昨日↑";
    if (c < p) return "较昨日↓";
    return "与昨日持平";
  }

  private int resolveRangeDays(String range) {
    String normalized = normalizeRange(range);
    if ("30d".equals(normalized)) return 30;
    if ("90d".equals(normalized)) return 90;
    return 7;
  }

  private String normalizeRange(String range) {
    if ("30d".equalsIgnoreCase(defaultText(range, ""))) return "30d";
    if ("90d".equalsIgnoreCase(defaultText(range, ""))) return "90d";
    return "7d";
  }

  private List<FamilyPortalModels.HealthTrendPoint> buildDefaultTrend(int days) {
    List<FamilyPortalModels.HealthTrendPoint> list = new ArrayList<>();
    LocalDate start = LocalDate.now().minusDays(days - 1L);
    for (int i = 0; i < days; i++) {
      LocalDate day = start.plusDays(i);
      FamilyPortalModels.HealthTrendPoint point = new FamilyPortalModels.HealthTrendPoint();
      point.setDate(day.format(DateTimeFormatter.ofPattern("MM-dd")));
      point.setSbp(136 + (i % 3));
      point.setDbp(84 + (i % 2));
      point.setHr(76 + (i % 3));
      point.setTemp(36.5D);
      point.setSugar(6.2D);
      list.add(point);
    }
    return list;
  }

  private List<String> buildRiskTips(FamilyPortalModels.VitalSnapshot latest) {
    List<String> tips = new ArrayList<>();
    if (safeInt(latest.getSbp()) >= 140 || safeInt(latest.getDbp()) >= 90) {
      tips.add("血压有上升趋势，建议午后复测并控制盐分摄入");
    }
    if (safeDouble(latest.getSugar()) > 7.8D) {
      tips.add("血糖偏高，建议关注饮食结构与进食节奏");
    }
    if (tips.isEmpty()) {
      tips.add("当前生命体征整体平稳，保持现有护理方案");
    }
    tips.add("如连续两次异常，系统将触发紧急提醒并通知责任护士");
    return tips;
  }

  private String mapTaskStatus(String rawStatus) {
    String status = defaultText(rawStatus, "").toUpperCase(Locale.ROOT);
    if (status.contains("DONE") || status.contains("COMPLETED") || status.contains("FINISHED") || status.contains("已")) {
      return "已完成";
    }
    if (status.contains("DOING") || status.contains("IN_PROGRESS") || status.contains("EXEC")) {
      return "进行中";
    }
    return "待开始";
  }

  private boolean isBreakfast(String mealType) {
    String text = defaultText(mealType, "").toLowerCase(Locale.ROOT);
    return text.contains("早") || text.contains("breakfast") || text.equals("b");
  }

  private boolean isLunch(String mealType) {
    String text = defaultText(mealType, "").toLowerCase(Locale.ROOT);
    return text.contains("午") || text.contains("lunch") || text.equals("l");
  }

  private boolean isDinner(String mealType) {
    String text = defaultText(mealType, "").toLowerCase(Locale.ROOT);
    return text.contains("晚") || text.contains("dinner") || text.equals("d");
  }

  private void addMealTags(List<String> tags, String text) {
    if (tags == null) {
      return;
    }
    if (text.contains("低糖")) tags.add("低糖餐");
    if (text.contains("低盐")) tags.add("低盐餐");
    if (text.contains("软食")) tags.add("软食");
    if (text.contains("流食")) tags.add("流食");
    if (text.contains("加餐")) tags.add("营养加餐");
    if (text.contains("高蛋白")) tags.add("高蛋白");
    Set<String> unique = new LinkedHashSet<>(tags);
    tags.clear();
    tags.addAll(unique);
  }

  private String buildScheduleDescription(CareTaskTodayItem task) {
    String taskName = defaultText(task == null ? null : task.getTaskName(), "护理任务");
    if (taskName.contains("测量") || taskName.contains("血压") || taskName.contains("体温")) {
      return "执行常规生命体征检测并记录结果，异常值将触发护士复核。";
    }
    if (taskName.contains("康复")) {
      return "根据康复计划开展训练，关注老人耐受度与执行反馈。";
    }
    if (taskName.contains("活动")) {
      return "参与院内活动，提升情绪与社交互动。";
    }
    if (taskName.contains("护理")) {
      return "按护理等级执行标准护理流程，并同步服务记录。";
    }
    return "按当日排班执行标准照护流程。";
  }

  private int resolveScheduleDuration(String taskName) {
    String text = defaultText(taskName, "");
    if (text.contains("测量") || text.contains("血压") || text.contains("体温")) {
      return 10;
    }
    if (text.contains("康复")) {
      return 30;
    }
    if (text.contains("活动")) {
      return 45;
    }
    if (text.contains("护理")) {
      return 20;
    }
    return 15;
  }

  private String resolveSchedulePrecautions(String taskName) {
    String text = defaultText(taskName, "");
    if (text.contains("康复")) {
      return "训练过程中如出现头晕或乏力将立即暂停并复评。";
    }
    if (text.contains("活动")) {
      return "注意活动强度与情绪状态，必要时护工全程陪同。";
    }
    if (text.contains("测量") || text.contains("血压")) {
      return "测量前保持安静休息 5 分钟，避免结果偏差。";
    }
    return "按护理规范执行，异常情况及时上报。";
  }

  private String resolveScheduleFitNote(String taskName, String careLevel) {
    String level = defaultText(careLevel, "常规护理");
    return "已按" + level + "匹配照护方案，当前任务为“" + defaultText(taskName, "护理任务") + "”。";
  }

  private List<String> extractPhotoUrls(String rawText) {
    String text = defaultText(rawText, "");
    if (text.isBlank()) {
      return new ArrayList<>();
    }
    Matcher matcher = URL_PATTERN.matcher(text);
    List<String> urls = new ArrayList<>();
    while (matcher.find()) {
      String url = defaultText(matcher.group(), null);
      if (url != null && (url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png")
          || url.endsWith(".webp") || url.endsWith(".gif") || url.contains("image") || url.contains("photo"))) {
        urls.add(url);
      }
    }
    return uniqueList(urls);
  }

  private <T> List<T> uniqueList(List<T> list) {
    if (list == null || list.isEmpty()) {
      return new ArrayList<>();
    }
    LinkedHashSet<T> set = new LinkedHashSet<>();
    for (T item : list) {
      if (item != null) {
        set.add(item);
      }
    }
    return new ArrayList<>(set);
  }

  private String resolveAlbumMediaType(OaAlbum album) {
    String text = (defaultText(album == null ? null : album.getRemark(), "") + " "
        + defaultText(album == null ? null : album.getCategory(), "")).toLowerCase(Locale.ROOT);
    if (text.contains("视频") || text.contains("video") || text.contains(".mp4")) {
      return "video";
    }
    return "photo";
  }

  private String firstPhotoFromAlbum(OaAlbum album) {
    if (album == null) {
      return null;
    }
    List<String> urls = extractPhotoUrls(defaultText(album.getPhotosJson(), "") + "\n" + defaultText(album.getRemark(), ""));
    return urls.isEmpty() ? null : urls.get(0);
  }

  private int countAlbumComments(Long orgId, Long albumId) {
    if (orgId == null || albumId == null) {
      return 0;
    }
    Long count = familyPortalStateMapper.selectCount(
        Wrappers.lambdaQuery(FamilyPortalState.class)
            .eq(FamilyPortalState::getIsDeleted, 0)
            .eq(FamilyPortalState::getOrgId, orgId)
            .eq(FamilyPortalState::getCategory, STATE_ALBUM_COMMENT)
            .likeRight(FamilyPortalState::getBizKey, albumId + ":"));
    return count == null ? 0 : count.intValue();
  }

  private FamilyPortalModels.ActivityAlbumCommentItem toAlbumCommentItem(FamilyPortalState row, String myName) {
    if (row == null || row.getValueJson() == null || row.getValueJson().isBlank()) {
      return null;
    }
    try {
      JsonNode node = objectMapper.readTree(row.getValueJson());
      FamilyPortalModels.ActivityAlbumCommentItem item = new FamilyPortalModels.ActivityAlbumCommentItem();
      item.setId(node.path("id").asLong(row.getId() == null ? 0L : row.getId()));
      item.setAlbumId(node.path("albumId").asLong(0L));
      item.setCommenter(defaultText(node.path("commenter").asText(""), "家属"));
      item.setContent(defaultText(node.path("content").asText(""), ""));
      item.setTime(defaultText(node.path("time").asText(""), formatFriendlyDateTime(row.getUpdateTime())));
      boolean mine = node.path("mine").isBoolean()
          ? node.path("mine").asBoolean()
          : Objects.equals(defaultText(myName, ""), item.getCommenter());
      item.setMine(mine);
      return item;
    } catch (Exception ex) {
      return null;
    }
  }

  private boolean shouldMaskSensitive(Long orgId, Long familyUserId) {
    Map<String, Object> map = readStateMap(orgId, familyUserId, STATE_SECURITY, "default");
    return readBoolean(map, "maskSensitiveData", true);
  }

  private String maskPhone(String phone, boolean needMask) {
    String text = defaultText(phone, "");
    if (!needMask || text.isBlank()) {
      return text;
    }
    String digits = text.replaceAll("\\D", "");
    if (digits.isBlank()) {
      return text;
    }
    if (digits.length() >= 11) {
      return digits.substring(0, 3) + "****" + digits.substring(digits.length() - 4);
    }
    if (digits.length() >= 7) {
      return digits.substring(0, 2) + "***" + digits.substring(digits.length() - 2);
    }
    return "***";
  }

  private String maskIfNeeded(Long orgId, Long familyUserId, String value) {
    if (!shouldMaskSensitive(orgId, familyUserId)) {
      return value;
    }
    return maskPhone(value, true);
  }

  private boolean hasFullSensitiveScope(Long orgId, Long familyUserId, Long elderId) {
    if (orgId == null || familyUserId == null || elderId == null) {
      return true;
    }
    Map<String, Object> map = readStateMap(orgId, familyUserId, STATE_SECURITY, "default");
    String scope = defaultText(readText(map, "visibleScope"), "仅子女可查看完整健康数据");
    if (scope.contains("全部授权")) {
      return true;
    }
    ElderFamily relation = elderFamilyMapper.selectOne(
        Wrappers.lambdaQuery(ElderFamily.class)
            .eq(ElderFamily::getIsDeleted, 0)
            .eq(ElderFamily::getOrgId, orgId)
            .eq(ElderFamily::getFamilyUserId, familyUserId)
            .eq(ElderFamily::getElderId, elderId)
            .last("LIMIT 1"));
    String relationText = defaultText(relation == null ? null : relation.getRelation(), "");
    if (scope.contains("仅子女")) {
      return relationText.contains("子") || relationText.contains("女儿") || relationText.contains("儿子");
    }
    if (scope.contains("直系家属")) {
      return relationText.contains("子") || relationText.contains("女") || relationText.contains("父")
          || relationText.contains("母") || relationText.contains("妻") || relationText.contains("夫");
    }
    return true;
  }

  private List<String> resolveSubscribeTemplateIds() {
    FamilyPortalProperties.WechatNotify notify = familyPortalProperties.getWechatNotify();
    String raw = notify == null ? "" : defaultText(notify.getTemplateId(), "");
    if (raw.isBlank()) {
      return new ArrayList<>();
    }
    String[] parts = raw.split("[,;\\s]+");
    List<String> ids = new ArrayList<>();
    for (String part : parts) {
      String id = defaultText(part, null);
      if (id != null) {
        ids.add(id);
      }
    }
    return uniqueList(ids);
  }

  private boolean hasSecurityPasswordConfigured(Map<String, Object> map) {
    return defaultText(readText(map, "passwordHash"), null) != null
        && defaultText(readText(map, "passwordSalt"), null) != null;
  }

  private String randomSalt() {
    byte[] bytes = new byte[16];
    new SecureRandom().nextBytes(bytes);
    return Base64.getEncoder().encodeToString(bytes);
  }

  private String hashSecurityPassword(String password, String salt) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      digest.update(defaultText(salt, "").getBytes(StandardCharsets.UTF_8));
      byte[] hashed = digest.digest(defaultText(password, "").getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hashed);
    } catch (Exception ex) {
      throw new IllegalStateException("密码加密失败", ex);
    }
  }

  private String sha256Hex(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashed = digest.digest(defaultText(value, "").getBytes(StandardCharsets.UTF_8));
      char[] output = new char[hashed.length * 2];
      char[] hex = "0123456789abcdef".toCharArray();
      int i = 0;
      for (byte b : hashed) {
        output[i++] = hex[(b >> 4) & 0x0F];
        output[i++] = hex[b & 0x0F];
      }
      return new String(output);
    } catch (Exception ex) {
      throw new IllegalStateException("计算回调摘要失败", ex);
    }
  }

  private String normalizeFeedbackType(String feedbackType) {
    String value = defaultText(feedbackType, "EVALUATION").toUpperCase(Locale.ROOT);
    if ("COMPLAINT".equals(value)) {
      return "COMPLAINT";
    }
    return "EVALUATION";
  }

  private FamilyPortalModels.FeedbackRecordItem toFeedbackRecordItem(OaSuggestion suggestion) {
    FamilyPortalModels.FeedbackRecordItem item = new FamilyPortalModels.FeedbackRecordItem();
    item.setId(suggestion.getId());
    String content = defaultText(suggestion.getContent(), "");
    item.setFeedbackType(content.contains("[COMPLAINT]") ? "COMPLAINT" : "EVALUATION");
    item.setServiceType(resolveFeedbackServiceType(content));
    item.setScore(resolveFeedbackScore(content));
    item.setContent(stripFeedbackPrefix(content));
    item.setStatus(defaultText(suggestion.getStatus(), "PENDING"));
    item.setReply(resolveFeedbackReply(suggestion));
    item.setCreateTime(suggestion.getCreateTime() == null ? "" : suggestion.getCreateTime().format(DATETIME_FMT));
    item.setUpdateTime(suggestion.getUpdateTime() == null ? "" : suggestion.getUpdateTime().format(DATETIME_FMT));
    return item;
  }

  private String resolveFeedbackServiceType(String content) {
    String text = defaultText(content, "");
    Matcher matcher = Pattern.compile("\\[(.+?)\\]").matcher(text);
    List<String> tokens = new ArrayList<>();
    while (matcher.find()) {
      tokens.add(defaultText(matcher.group(1), ""));
    }
    if (tokens.size() >= 2) {
      return tokens.get(1);
    }
    if (tokens.size() == 1 && !"EVALUATION".equalsIgnoreCase(tokens.get(0))
        && !"COMPLAINT".equalsIgnoreCase(tokens.get(0))) {
      return tokens.get(0);
    }
    return "服务反馈";
  }

  private Integer resolveFeedbackScore(String content) {
    String text = defaultText(content, "");
    Matcher matcher = Pattern.compile("评分:([0-9])星").matcher(text);
    if (matcher.find()) {
      return parseInteger(matcher.group(1), 0);
    }
    return null;
  }

  private String stripFeedbackPrefix(String content) {
    String text = defaultText(content, "");
    if (text.contains("\n")) {
      return text.substring(text.indexOf('\n') + 1).trim();
    }
    return text;
  }

  private String resolveFeedbackReply(OaSuggestion suggestion) {
    String status = defaultText(suggestion == null ? null : suggestion.getStatus(), "PENDING");
    if ("DONE".equalsIgnoreCase(status) || "RESOLVED".equalsIgnoreCase(status)) {
      return "机构已处理完成，感谢您的反馈。";
    }
    if ("PROCESSING".equalsIgnoreCase(status)) {
      return "机构正在处理中，预计 24 小时内反馈结果。";
    }
    return "已受理，服务团队会尽快跟进。";
  }

  private List<String> resolveAiReportTypes(String reportType) {
    String value = defaultText(reportType, "ALL").toUpperCase(Locale.ROOT);
    if ("TCM".equals(value)) {
      return List.of("TCM");
    }
    if ("CARDIO".equals(value)) {
      return List.of("CARDIO");
    }
    return List.of("TCM", "CARDIO");
  }

  private AssessmentRecord buildAiAssessmentRecord(Long orgId, Long familyUserId, Long elderId, String elderName,
      String type) {
    FamilyPortalModels.VitalSnapshot latest = getHealthTrend(orgId, familyUserId, elderId, "7d").getLatest();
    int sbp = safeInt(latest == null ? null : latest.getSbp());
    int dbp = safeInt(latest == null ? null : latest.getDbp());
    int hr = safeInt(latest == null ? null : latest.getHr());
    double sugar = safeDouble(latest == null ? null : latest.getSugar());
    int baseScore = 85;
    if (sbp >= 140 || dbp >= 90) {
      baseScore -= 8;
    }
    if (hr > 100 || hr < 50) {
      baseScore -= 5;
    }
    if (sugar > 7.8D) {
      baseScore -= 6;
    }
    if (baseScore < 40) {
      baseScore = 40;
    }

    AssessmentRecord record = new AssessmentRecord();
    record.setTenantId(orgId);
    record.setOrgId(orgId);
    record.setElderId(elderId);
    record.setElderName(elderName);
    record.setAssessmentType("TCM".equals(type) ? "AI中医体质评估" : "心血管功能评估");
    record.setAssessmentDate(LocalDate.now());
    record.setScore(BigDecimal.valueOf(baseScore));
    record.setLevelCode(baseScore >= 80 ? "LOW" : (baseScore >= 60 ? "MEDIUM" : "HIGH"));
    record.setStatus("DONE");
    record.setSource("FAMILY_AI");
    record.setAssessorName("AI评估引擎");
    record.setCreatedBy(familyUserId);
    if ("TCM".equals(type)) {
      record.setResultSummary(buildTcmSummary(baseScore, sbp, dbp, sugar));
      record.setSuggestion("建议保持规律作息与温和运动，持续观察饮食结构。");
    } else {
      record.setResultSummary(buildCardioSummary(baseScore, sbp, dbp, hr));
      record.setSuggestion("建议维持低盐饮食，规律监测血压并按医嘱复查。");
    }
    record.setDetailJson(writeJson(Map.of(
        "sbp", sbp,
        "dbp", dbp,
        "hr", hr,
        "sugar", sugar,
        "score", baseScore,
        "generatedAt", LocalDateTime.now().format(DATETIME_FMT),
        "type", type)));
    return record;
  }

  private String buildTcmSummary(int score, int sbp, int dbp, double sugar) {
    String constitution = score >= 80 ? "平和偏气虚" : (score >= 60 ? "气虚体质" : "痰湿偏盛体质");
    return "体质判定：" + constitution + "；血压 " + sbp + "/" + dbp + " mmHg，血糖 "
        + String.format(Locale.ROOT, "%.1f", sugar) + " mmol/L。";
  }

  private String buildCardioSummary(int score, int sbp, int dbp, int hr) {
    String risk = score >= 80 ? "低风险" : (score >= 60 ? "中等风险" : "较高风险");
    return "心血管综合评分 " + score + " 分（" + risk + "），当前血压 " + sbp + "/" + dbp
        + " mmHg，心率 " + hr + " 次/分。";
  }

  private Long parseElderIdFromAutoPayState(String bizKey) {
    String text = defaultText(bizKey, "");
    if (!text.startsWith("elder:")) {
      return null;
    }
    return parseLongQuietly(text.substring("elder:".length()));
  }

  private int settleAutoPayForElder(Long orgId, Long familyUserId, Long elderId) {
    ElderAccount account = getOrCreateElderAccount(orgId, elderId, familyUserId);
    BigDecimal balance = safeDecimal(account.getBalance());
    if (balance.compareTo(BigDecimal.ZERO) <= 0) {
      return 0;
    }
    List<BillMonthly> bills = billMonthlyMapper.selectList(
        Wrappers.lambdaQuery(BillMonthly.class)
            .eq(BillMonthly::getIsDeleted, 0)
            .eq(BillMonthly::getOrgId, orgId)
            .eq(BillMonthly::getElderId, elderId)
            .gt(BillMonthly::getOutstandingAmount, BigDecimal.ZERO)
            .orderByAsc(BillMonthly::getBillMonth)
            .last("LIMIT 12"));
    if (bills.isEmpty()) {
      return 0;
    }

    int settled = 0;
    for (BillMonthly bill : bills) {
      if (balance.compareTo(BigDecimal.ZERO) <= 0) {
        break;
      }
      BigDecimal outstanding = safeDecimal(bill.getOutstandingAmount());
      if (outstanding.compareTo(BigDecimal.ZERO) <= 0) {
        continue;
      }
      BigDecimal payAmount = balance.min(outstanding);
      if (payAmount.compareTo(BigDecimal.ZERO) <= 0) {
        continue;
      }
      BigDecimal newPaid = safeDecimal(bill.getPaidAmount()).add(payAmount);
      BigDecimal newOutstanding = outstanding.subtract(payAmount);
      bill.setPaidAmount(newPaid);
      bill.setOutstandingAmount(newOutstanding);
      bill.setStatus(newOutstanding.compareTo(BigDecimal.ZERO) == 0 ? 2 : 1);
      billMonthlyMapper.updateById(bill);

      balance = balance.subtract(payAmount);
      ElderAccountLog log = new ElderAccountLog();
      log.setTenantId(orgId);
      log.setOrgId(orgId);
      log.setElderId(elderId);
      log.setAccountId(account.getId());
      log.setAmount(payAmount);
      log.setBalanceAfter(balance);
      log.setDirection("DEBIT");
      log.setSourceType("FAMILY_AUTO_PAY_SETTLE");
      log.setSourceId(bill.getId());
      log.setRemark("自动扣费结算，账单月:" + defaultText(bill.getBillMonth(), ""));
      log.setCreatedBy(familyUserId);
      elderAccountLogMapper.insert(log);
      settled++;
    }

    if (settled > 0) {
      account.setBalance(balance);
      elderAccountMapper.updateById(account);
      String elderName = resolveElderName(elderId);
      notifyFamilySafe(orgId, familyUserId, elderId, "PAYMENT_AUTO_SETTLED", "normal",
          "自动扣费已执行",
          "已自动结算 " + elderName + " 的待缴账单，当前余额 ¥" + balance.stripTrailingZeros().toPlainString(),
          "pages/payment/index",
          Map.of(
              "elderName", elderName,
              "balance", balance.stripTrailingZeros().toPlainString()));
    }
    return settled;
  }

  private FamilyPortalModels.MealSummary defaultMealSummary() {
    FamilyPortalModels.MealSummary meal = new FamilyPortalModels.MealSummary();
    meal.setDate(LocalDate.now().format(DATE_FMT));
    meal.setReview("今日膳食计划已生成，营养搭配均衡。");
    meal.setTags(new ArrayList<>(List.of("均衡膳食")));
    meal.setBreakfast(new ArrayList<>(List.of("小米粥", "鸡蛋羹")));
    meal.setLunch(new ArrayList<>(List.of("米饭", "时蔬")));
    meal.setDinner(new ArrayList<>(List.of("面条", "青菜汤")));
    meal.setEatingStatus("进食情况正常，护理团队持续跟进。");
    meal.setPhotos(new ArrayList<>());
    return meal;
  }

  private int[] parseBloodPressure(String value) {
    String text = defaultText(value, "");
    if (text.contains("/")) {
      String[] parts = text.split("/");
      if (parts.length >= 2) {
        return new int[] { parseInteger(parts[0], 0), parseInteger(parts[1], 0) };
      }
    }
    return new int[] { 0, 0 };
  }

  private Integer parseInteger(String value, int fallback) {
    if (value == null || value.isBlank()) {
      return fallback;
    }
    String digits = value.replaceAll("[^0-9-]", "");
    if (digits.isBlank()) {
      return fallback;
    }
    try {
      return Integer.parseInt(digits);
    } catch (NumberFormatException ex) {
      return fallback;
    }
  }

  private Double parseDouble(String value, double fallback) {
    if (value == null || value.isBlank()) {
      return fallback;
    }
    String num = value.replaceAll("[^0-9.\\-]", "");
    if (num.isBlank()) {
      return fallback;
    }
    try {
      return Double.parseDouble(num);
    } catch (NumberFormatException ex) {
      return fallback;
    }
  }

  private BigDecimal resolveServicePrice(ServiceItem item) {
    if (item == null) {
      return BigDecimal.ZERO;
    }
    if (item.getDefaultPoints() != null && item.getDefaultPoints() > 0) {
      return BigDecimal.valueOf(item.getDefaultPoints());
    }
    if (item.getDefaultDuration() != null && item.getDefaultDuration() > 0) {
      return BigDecimal.valueOf(item.getDefaultDuration())
          .divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP);
    }
    return BigDecimal.valueOf(99);
  }

  private String mapBillStatus(Integer status) {
    if (status == null || status == 0) return "待缴费";
    if (status == 1) return "部分缴费";
    if (status == 2) return "已结清";
    if (status == 9) return "已作废";
    return "待处理";
  }

  private String currentBillMonth() {
    LocalDate now = LocalDate.now();
    return now.getYear() + "-" + String.format("%02d", now.getMonthValue());
  }

  private String resolveOrgName(Long orgId) {
    Org org = orgMapper.selectById(orgId);
    return defaultText(org == null ? null : org.getOrgName(), "机构评估中心");
  }

  private int calculateAge(LocalDate birthDate) {
    if (birthDate == null) {
      return 0;
    }
    int age = Period.between(birthDate, LocalDate.now()).getYears();
    return Math.max(age, 0);
  }

  private String toGenderText(Integer gender) {
    if (gender == null) return "未知";
    if (gender == 1) return "男";
    if (gender == 2) return "女";
    return "未知";
  }

  private Integer calcCheckinDays(LocalDate admissionDate) {
    if (admissionDate == null) {
      return 0;
    }
    long days = Duration.between(admissionDate.atStartOfDay(), LocalDate.now().atStartOfDay()).toDays();
    return (int) Math.max(days, 0);
  }

  private String formatFriendlyDateTime(LocalDateTime time) {
    if (time == null) {
      return "";
    }
    LocalDate today = LocalDate.now();
    if (time.toLocalDate().isEqual(today)) {
      return "今天 " + time.format(HHMM_FMT);
    }
    return time.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
  }

  private LocalDateTime parseDateTime(String value) {
    if (value == null || value.isBlank()) {
      return LocalDateTime.now().plusHours(1);
    }
    String trimmed = value.trim();
    try {
      return LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    } catch (DateTimeParseException ignore) {
      try {
        return LocalDateTime.parse(trimmed, DATETIME_FMT);
      } catch (DateTimeParseException e) {
        return LocalDateTime.now().plusHours(1);
      }
    }
  }

  private LocalDateTime parseDateTimeNullable(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String trimmed = value.trim();
    try {
      return LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    } catch (DateTimeParseException ignore) {
      try {
        return LocalDateTime.parse(trimmed, DATETIME_FMT);
      } catch (DateTimeParseException e) {
        return null;
      }
    }
  }

  private boolean isLegacySunsetReached(String legacyApiSunsetDate) {
    String configured = defaultText(legacyApiSunsetDate, null);
    if (configured == null) {
      return false;
    }
    try {
      return !LocalDate.now().isBefore(LocalDate.parse(configured));
    } catch (DateTimeParseException ex) {
      return false;
    }
  }

  private BigDecimal safeDecimal(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  private String defaultText(String value, String fallback) {
    if (value == null || value.isBlank()) {
      return fallback;
    }
    return value.trim();
  }

  private String normalizeIdCardNo(String value) {
    if (!hasText(value)) {
      return null;
    }
    String normalized = value.trim().replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    if (!normalized.matches("(^\\d{15}$)|(^\\d{17}[0-9X]$)")) {
      throw new IllegalArgumentException("身份证号格式不正确");
    }
    return normalized;
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

  private int safeInt(Integer value) {
    return value == null ? 0 : value;
  }

  private double safeDouble(Double value) {
    return value == null ? 0D : value;
  }

  private String buildRechargeRemark(FamilyPortalModels.PaymentRechargeRequest request) {
    String method = defaultText(request.getMethod(), "微信支付");
    String memo = defaultText(request.getRemark(), "家属端在线充值");
    return "充值方式:" + method + "|充值时间:" + LocalDateTime.now().format(DATETIME_FMT) + "|备注:" + memo;
  }

  private ElderAccount getOrCreateElderAccount(Long orgId, Long elderId, Long operatorId) {
    ElderAccount account = elderAccountMapper.selectOne(
        Wrappers.lambdaQuery(ElderAccount.class)
            .eq(ElderAccount::getIsDeleted, 0)
            .eq(ElderAccount::getOrgId, orgId)
            .eq(ElderAccount::getElderId, elderId)
            .last("LIMIT 1"));
    if (account != null) {
      return account;
    }
    ElderAccount created = new ElderAccount();
    created.setTenantId(orgId);
    created.setOrgId(orgId);
    created.setElderId(elderId);
    created.setBalance(BigDecimal.ZERO);
    created.setCreditLimit(BigDecimal.ZERO);
    created.setWarnThreshold(BigDecimal.ZERO);
    created.setStatus(1);
    created.setCreatedBy(operatorId);
    elderAccountMapper.insert(created);
    return created;
  }

  private Set<Long> loadLongSetState(Long orgId, Long familyUserId, String category) {
    return listStateRows(orgId, familyUserId, category, 500).stream()
        .map(FamilyPortalState::getBizKey)
        .filter(Objects::nonNull)
        .map(this::parseLongQuietly)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  private Map<String, Object> readStateMap(Long orgId, Long familyUserId, String category, String bizKey) {
    FamilyPortalState row = findState(orgId, familyUserId, category, bizKey);
    return parseStateValueMap(row == null ? null : row.getValueJson());
  }

  private Map<String, Object> parseStateValueMap(String valueJson) {
    if (valueJson == null || valueJson.isBlank()) {
      return Map.of();
    }
    try {
      return objectMapper.readValue(valueJson, new TypeReference<Map<String, Object>>() {});
    } catch (Exception ex) {
      return Map.of();
    }
  }

  private boolean readBooleanState(Long orgId, Long familyUserId, String category, String bizKey, boolean fallback) {
    Map<String, Object> map = readStateMap(orgId, familyUserId, category, bizKey);
    return readBoolean(map, "enabled", fallback);
  }

  private List<FamilyPortalState> listStateRows(Long orgId, Long familyUserId, String category, int limit) {
    int size = Math.max(limit, 1);
    Page<FamilyPortalState> page = familyPortalStateMapper.selectPage(new Page<>(1, size),
        Wrappers.lambdaQuery(FamilyPortalState.class)
            .eq(FamilyPortalState::getIsDeleted, 0)
            .eq(FamilyPortalState::getOrgId, orgId)
            .eq(FamilyPortalState::getFamilyUserId, familyUserId)
            .eq(FamilyPortalState::getCategory, category)
            .orderByDesc(FamilyPortalState::getUpdateTime));
    return page.getRecords();
  }

  private List<FamilyPortalState> listStateRows(Long orgId, Long familyUserId, String category,
      String bizKeyPrefix, int limit) {
    int size = Math.max(limit, 1);
    String prefix = defaultText(bizKeyPrefix, "");
    Page<FamilyPortalState> page = familyPortalStateMapper.selectPage(new Page<>(1, size),
        Wrappers.lambdaQuery(FamilyPortalState.class)
            .eq(FamilyPortalState::getIsDeleted, 0)
            .eq(FamilyPortalState::getOrgId, orgId)
            .eq(FamilyPortalState::getFamilyUserId, familyUserId)
            .eq(FamilyPortalState::getCategory, category)
            .likeRight(!prefix.isBlank(), FamilyPortalState::getBizKey, prefix + ":")
            .orderByDesc(FamilyPortalState::getUpdateTime));
    return page.getRecords();
  }

  private FamilyPortalState findState(Long orgId, Long familyUserId, String category, String bizKey) {
    return familyPortalStateMapper.selectOne(
        Wrappers.lambdaQuery(FamilyPortalState.class)
            .eq(FamilyPortalState::getIsDeleted, 0)
            .eq(FamilyPortalState::getOrgId, orgId)
            .eq(FamilyPortalState::getFamilyUserId, familyUserId)
            .eq(FamilyPortalState::getCategory, category)
            .eq(FamilyPortalState::getBizKey, defaultText(bizKey, "default"))
            .last("LIMIT 1"));
  }

  private void upsertState(Long orgId, Long familyUserId, String category, String bizKey, Object value) {
    String finalKey = defaultText(bizKey, "default");
    FamilyPortalState row = findState(orgId, familyUserId, category, finalKey);
    String json;
    try {
      json = objectMapper.writeValueAsString(value);
    } catch (Exception ex) {
      throw new IllegalStateException("状态序列化失败", ex);
    }
    if (row == null) {
      FamilyPortalState created = new FamilyPortalState();
      created.setTenantId(orgId);
      created.setOrgId(orgId);
      created.setFamilyUserId(familyUserId);
      created.setCategory(category);
      created.setBizKey(finalKey);
      created.setValueJson(json);
      created.setStatus("ACTIVE");
      created.setCreatedBy(familyUserId);
      familyPortalStateMapper.insert(created);
      return;
    }
    row.setValueJson(json);
    row.setStatus("ACTIVE");
    familyPortalStateMapper.updateById(row);
  }

  private void upsertState(Long orgId, Long familyUserId, String category, String bizKey, String bizSubKey, Object value) {
    String key = defaultText(bizKey, "default") + ":" + defaultText(bizSubKey, "default");
    upsertState(orgId, familyUserId, category, key, value);
  }

  private void removeState(Long orgId, Long familyUserId, String category, String bizKey) {
    FamilyPortalState row = findState(orgId, familyUserId, category, bizKey);
    if (row != null) {
      row.setIsDeleted(1);
      familyPortalStateMapper.updateById(row);
    }
  }

  private Long parseLongQuietly(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return Long.parseLong(value.trim());
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  private boolean readBoolean(Map<String, Object> map, String key, boolean fallback) {
    if (map == null || map.isEmpty() || key == null) {
      return fallback;
    }
    Object value = map.get(key);
    if (value instanceof Boolean bool) {
      return bool;
    }
    if (value instanceof Number num) {
      return num.intValue() != 0;
    }
    if (value instanceof String text && !text.isBlank()) {
      return "1".equals(text) || "true".equalsIgnoreCase(text.trim());
    }
    return fallback;
  }

  private String readText(Map<String, Object> map, String key) {
    if (map == null || map.isEmpty() || key == null) {
      return null;
    }
    Object value = map.get(key);
    if (value == null) {
      return null;
    }
    return String.valueOf(value);
  }

  private FamilyPortalModels.AffectionMomentItem toAffectionItem(FamilyPortalState row) {
    if (row == null || row.getValueJson() == null || row.getValueJson().isBlank()) {
      return null;
    }
    try {
      JsonNode node = objectMapper.readTree(row.getValueJson());
      FamilyPortalModels.AffectionMomentItem item = new FamilyPortalModels.AffectionMomentItem();
      item.setId(node.path("id").asLong(row.getId() == null ? 0L : row.getId()));
      item.setType(defaultText(node.path("type").asText(""), "text"));
      item.setTitle(defaultText(node.path("title").asText(""), "亲情互动"));
      item.setTime(defaultText(node.path("time").asText(""), formatFriendlyDateTime(row.getUpdateTime())));
      item.setDesc(defaultText(node.path("desc").asText(""), ""));
      item.setMediaType(defaultText(node.path("mediaType").asText(""), null));
      item.setMediaUrl(defaultText(node.path("mediaUrl").asText(""), null));
      item.setMediaName(defaultText(node.path("mediaName").asText(""), null));
      return item;
    } catch (Exception ex) {
      return null;
    }
  }

  private FamilyPortalModels.CommunicationMessageItem toCommunicationMessageItem(FamilyPortalState row) {
    if (row == null || row.getValueJson() == null || row.getValueJson().isBlank()) {
      return null;
    }
    try {
      JsonNode node = objectMapper.readTree(row.getValueJson());
      FamilyPortalModels.CommunicationMessageItem item = new FamilyPortalModels.CommunicationMessageItem();
      item.setId(node.path("id").asLong(row.getId() == null ? 0L : row.getId()));
      item.setTargetRole(defaultText(node.path("targetRole").asText(""), "客服中心"));
      item.setMsgType(defaultText(node.path("msgType").asText(""), "text"));
      item.setContent(defaultText(node.path("content").asText(""), ""));
      item.setMediaUrl(defaultText(node.path("mediaUrl").asText(""), null));
      item.setMediaName(defaultText(node.path("mediaName").asText(""), null));
      item.setMediaDurationSec(node.path("mediaDurationSec").isNumber() ? node.path("mediaDurationSec").asInt() : null);
      item.setTranscript(defaultText(node.path("transcript").asText(""), null));
      item.setTime(defaultText(node.path("time").asText(""), formatFriendlyDateTime(row.getUpdateTime())));
      item.setSender(defaultText(node.path("sender").asText(""), "家属"));
      item.setStatus(defaultText(node.path("status").asText(""), "已发送"));
      return item;
    } catch (Exception ex) {
      return null;
    }
  }

  private FamilyPortalModels.VitalBadge buildVitalBadge(String name, String value, String status) {
    FamilyPortalModels.VitalBadge badge = new FamilyPortalModels.VitalBadge();
    badge.setName(name);
    badge.setValue(value);
    badge.setStatus(status);
    return badge;
  }

  private FamilyPortalModels.CommunicationTemplateItem buildCommunicationTemplate(String id, String title,
      String targetRole, String msgType, String content, String scene) {
    FamilyPortalModels.CommunicationTemplateItem item = new FamilyPortalModels.CommunicationTemplateItem();
    item.setId(id);
    item.setTitle(title);
    item.setTargetRole(targetRole);
    item.setMsgType(msgType);
    item.setContent(content);
    item.setScene(scene);
    return item;
  }

  private FamilyPortalModels.HelpFaqItem faq(String category, String question, String answer) {
    FamilyPortalModels.HelpFaqItem item = new FamilyPortalModels.HelpFaqItem();
    item.setCategory(category);
    item.setQuestion(question);
    item.setAnswer(answer);
    return item;
  }

  private FamilyPortalModels.BindRelationItem toBindRelationItem(ElderFamily relation, ElderProfile elder) {
    FamilyPortalModels.BindRelationItem item = new FamilyPortalModels.BindRelationItem();
    item.setElderId(relation.getElderId());
    item.setElderName(elder == null ? "未命名老人" : defaultText(elder.getFullName(), "未命名老人"));
    item.setRelation(defaultText(relation.getRelation(), "家属"));
    item.setIsPrimary(Integer.valueOf(1).equals(relation.getIsPrimary()));
    return item;
  }

  private static class PlatformCertificateHolder {
    private final PublicKey publicKey;
    private final LocalDateTime expireAt;

    private PlatformCertificateHolder(PublicKey publicKey, LocalDateTime expireAt) {
      this.publicKey = publicKey;
      this.expireAt = expireAt;
    }
  }

  private static class HealthOverview {
    private String overviewText;
    private String latestTimeText;
    private LocalDateTime latestTime;
  }

  private static class WeeklySnapshot {
    private final long healthCheckCount;
    private final long abnormalCount;
    private final long nursingLogCount;
    private final long activityCount;
    private final long communicationCount;
    private final int unreadMessageCount;

    private WeeklySnapshot(long healthCheckCount, long abnormalCount, long nursingLogCount, long activityCount,
        long communicationCount, int unreadMessageCount) {
      this.healthCheckCount = healthCheckCount;
      this.abnormalCount = abnormalCount;
      this.nursingLogCount = nursingLogCount;
      this.activityCount = activityCount;
      this.communicationCount = communicationCount;
      this.unreadMessageCount = unreadMessageCount;
    }
  }
}
