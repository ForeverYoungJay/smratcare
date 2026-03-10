package com.zhiyangyun.care.family.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

public final class FamilyPortalModels {
  private FamilyPortalModels() {}

  @Data
  public static class HomeDashboardResponse {
    private List<ElderCard> elders = new ArrayList<>();
    private HealthSummary healthSummary;
    private List<ScheduleItem> schedules = new ArrayList<>();
    private MealSummary meal;
    private List<MessageItem> notices = new ArrayList<>();
    private List<String> focusEvents = new ArrayList<>();
  }

  @Data
  public static class WeeklyBriefResponse {
    private Long elderId;
    private String elderName;
    private String weekRange;
    private String overallLevel;
    private String overallText;
    private Integer healthCheckCount;
    private Integer abnormalCount;
    private Integer nursingLogCount;
    private Integer activityCount;
    private Integer communicationCount;
    private Integer unreadMessageCount;
    private List<VitalBadge> vitalBadges = new ArrayList<>();
    private List<String> highlights = new ArrayList<>();
    private List<String> suggestions = new ArrayList<>();
  }

  @Data
  public static class WeeklyBriefDigestItem {
    private String weekRange;
    private String overallLevel;
    private String overallText;
    private Integer healthCheckCount;
    private Integer abnormalCount;
    private Integer nursingLogCount;
    private Integer activityCount;
    private Integer communicationCount;
    private String quickText;
  }

  @Data
  public static class VitalBadge {
    private String name;
    private String value;
    private String status;
  }

  @Data
  public static class ElderCard {
    private Long elderId;
    private String elderName;
    private Integer age;
    private String gender;
    private String roomNo;
    private String careLevel;
    private String status;
    private String statusType;
    private Integer checkinDays;
    private String healthOverview;
    private String latestUpdateTime;
    private String specialTag;
    private String dynamicPreview;
    private String relation;
    private String locationText;
  }

  @Data
  public static class HealthSummary {
    private String stateText;
    private String level;
    private String updateTime;
    private List<HealthMetric> metrics = new ArrayList<>();
    private String suggestion;
  }

  @Data
  public static class HealthMetric {
    private String name;
    private String value;
    private String unit;
    private String status;
    private String trend;
  }

  @Data
  public static class ScheduleItem {
    private Long id;
    private String time;
    private String name;
    private String type;
    private String status;
    private String owner;
    private String description;
    private Integer durationMinutes;
    private String precautions;
    private String fitNote;
  }

  @Data
  public static class MealSummary {
    private String date;
    private String review;
    private List<String> tags = new ArrayList<>();
    private List<String> breakfast = new ArrayList<>();
    private List<String> lunch = new ArrayList<>();
    private List<String> dinner = new ArrayList<>();
    private String eatingStatus;
    private List<String> photos = new ArrayList<>();
  }

  @Data
  public static class MessageItem {
    private Long id;
    private String level;
    private String levelText;
    private String type;
    private String title;
    private String content;
    private Long elderId;
    private String elderName;
    private String time;
    private Boolean unread;
    private String phone;
    private List<String> timeline = new ArrayList<>();
  }

  @Data
  public static class MessageSummaryResponse {
    private Integer totalCount;
    private Integer unreadCount;
    private Integer urgentUnreadCount;
    private Integer warningUnreadCount;
    private Integer normalUnreadCount;
    private List<MessageTypeSummaryItem> typeSummary = new ArrayList<>();
    private List<MessageItem> urgentItems = new ArrayList<>();
  }

  @Data
  public static class MessageTypeSummaryItem {
    private String type;
    private Integer count;
    private Integer unreadCount;
  }

  @Data
  public static class HealthTrendResponse {
    private String range;
    private List<HealthTrendPoint> trend = new ArrayList<>();
    private VitalSnapshot latest;
    private List<String> riskTips = new ArrayList<>();
  }

  @Data
  public static class HealthTrendPoint {
    private String date;
    private Integer sbp;
    private Integer dbp;
    private Integer hr;
    private Double temp;
    private Double sugar;
  }

  @Data
  public static class VitalSnapshot {
    private Integer sbp;
    private Integer dbp;
    private Integer hr;
    private Double temp;
    private Double sugar;
  }

  @Data
  public static class MedicalRecordItem {
    private Long id;
    private String date;
    private String hospital;
    private String reason;
    private String diagnosis;
    private String advice;
    private String meds;
  }

  @Data
  public static class AssessmentReportItem {
    private Long id;
    private String reportName;
    private String date;
    private String orgName;
    private String summary;
    private String score;
    private String risk;
    private String type;
    private String fileName;
    private String fileUrl;
  }

  @Data
  public static class CareLogDay {
    private String date;
    private List<CareLogItem> items = new ArrayList<>();
  }

  @Data
  public static class CareLogItem {
    private String time;
    private String project;
    private String staff;
    private String status;
    private String note;
    private List<String> photos = new ArrayList<>();
  }

  @Data
  public static class ActivityAlbumItem {
    private Long id;
    private String title;
    private String date;
    private String elderName;
    private String description;
    private String mediaType;
    private String coverText;
    private String coverUrl;
    private Integer likes;
    private Integer comments;
    private Boolean liked;
  }

  @Data
  public static class ActivityAlbumCommentItem {
    private Long id;
    private Long albumId;
    private String commenter;
    private String content;
    private String time;
    private Boolean mine;
  }

  @Data
  public static class OutingRecordItem {
    private Long id;
    private String elderName;
    private String startTime;
    private String reason;
    private String companion;
    private String destination;
    private String returnTime;
    private String status;
  }

  @Data
  public static class EmergencyContactItem {
    private Long id;
    private String role;
    private String name;
    private String phone;
    private String duty;
  }

  @Data
  public static class PaymentSummaryResponse {
    private String elderName;
    private String month;
    private BigDecimal total;
    private BigDecimal paid;
    private BigDecimal outstanding;
    private BigDecimal accountBalance;
    private Boolean autoPayEnabled;
    private List<PaymentDetailItem> details = new ArrayList<>();
  }

  @Data
  public static class PaymentGuardResponse {
    private String elderName;
    private BigDecimal accountBalance;
    private BigDecimal outstanding;
    private Integer paidOrderCount;
    private Integer pendingOrderCount;
    private Integer closedOrderCount;
    private Integer failedOrderCount;
    private String lastPaidTime;
    private List<RechargeOrderItem> recentOrders = new ArrayList<>();
    private List<String> tips = new ArrayList<>();
  }

  @Data
  public static class PaymentDetailItem {
    private String item;
    private BigDecimal amount;
  }

  @Data
  public static class PaymentHistoryItem {
    private String month;
    private BigDecimal total;
    private BigDecimal paid;
    private String status;
  }

  @Data
  public static class PaymentRechargeResponse {
    private Long logId;
    private String outTradeNo;
    private BigDecimal rechargeAmount;
    private BigDecimal accountBalance;
    private String message;
  }

  @Data
  public static class WechatRechargePrepayRequest {
    private Long elderId;
    @NotNull
    @Min(1)
    private BigDecimal amount;
    private String description;
    private String remark;
    private String payerOpenId;
    private String loginCode;
  }

  @Data
  public static class WechatRechargePrepayResponse {
    private String outTradeNo;
    private BigDecimal rechargeAmount;
    private Integer amountFen;
    private String appId;
    private String timeStamp;
    private String nonceStr;
    private String payPackage;
    private String signType;
    private String paySign;
    private String status;
    private String message;
  }

  @Data
  public static class WechatNotifyBindRequest {
    @Size(max = 80)
    private String loginCode;
    @Size(max = 80)
    private String openId;
  }

  @Data
  public static class WechatNotifyBindResponse {
    private Boolean bound;
    private String openId;
    private String message;
  }

  @Data
  public static class RechargeOrderItem {
    private String outTradeNo;
    private Long elderId;
    private String elderName;
    private BigDecimal amount;
    private String channel;
    private String status;
    private String statusText;
    private String prepayId;
    private String wxTransactionId;
    private String createTime;
    private String paidTime;
    private String remark;
  }

  @Data
  public static class ServiceCatalogItem {
    private Long id;
    private String category;
    private String name;
    private BigDecimal price;
    private String unit;
    private String desc;
  }

  @Data
  public static class ServiceOrderItem {
    private Long id;
    private String name;
    private BigDecimal amount;
    private String status;
    private String date;
  }

  @Data
  public static class MallProductItem {
    private Long id;
    private String productCode;
    private String productName;
    private String category;
    private String unit;
    private BigDecimal price;
    private Integer pointsPrice;
    private Integer currentStock;
    private String stockStatus;
    private String statusText;
    private String businessDomain;
    private String itemType;
  }

  @Data
  public static class MallOrderItem {
    private Long orderId;
    private String orderNo;
    private Long elderId;
    private String elderName;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private Integer pointsUsed;
    private Integer orderStatus;
    private String orderStatusText;
    private Integer payStatus;
    private String payStatusText;
    private String createTime;
    private String payTime;
    private Boolean canCancel;
    private String cancelHint;
    private Boolean canRefund;
    private String refundHint;
  }

  @Data
  public static class MallOrderLineItem {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
  }

  @Data
  public static class MallOrderRiskItem {
    private String diseaseName;
    private String tagCode;
    private String tagName;
  }

  @Data
  public static class MallOrderFifoItem {
    private String batchNo;
    private Integer quantity;
    private String expireDate;
  }

  @Data
  public static class MallOrderDetailResponse {
    private MallOrderItem summary;
    private List<MallOrderLineItem> items = new ArrayList<>();
    private List<MallOrderRiskItem> riskReasons = new ArrayList<>();
    private List<MallOrderFifoItem> fifoLogs = new ArrayList<>();
  }

  @Data
  public static class MallOrderActionRequest {
    @Size(max = 120)
    private String reason;
  }

  @Data
  public static class MallOrderActionResponse {
    private Long orderId;
    private String action;
    private Boolean success;
    private String message;
    private Integer orderStatus;
    private String orderStatusText;
    private Integer payStatus;
    private String payStatusText;
    private Boolean canCancel;
    private String cancelHint;
    private Boolean canRefund;
    private String refundHint;
  }

  @Data
  public static class MallOrderPreviewRequest {
    @NotNull
    private Long productId;
    @Min(1)
    @NotNull
    private Integer qty;
    private Long elderId;
  }

  @Data
  public static class MallOrderSubmitRequest {
    @NotNull
    private Long productId;
    @Min(1)
    @NotNull
    private Integer qty;
    private Long elderId;
  }

  @Data
  public static class MallOrderPreviewResponse {
    private Boolean allowed;
    private String status;
    private String message;
    private Long elderId;
    private Long productId;
    private String productName;
    private Integer qty;
    private Integer pointsRequired;
    private List<String> reasons = new ArrayList<>();
  }

  @Data
  public static class MallOrderSubmitResponse {
    private Boolean allowed;
    private String status;
    private String message;
    private Long orderId;
    private String orderNo;
    private Integer pointsDeducted;
    private Integer balanceAfter;
    private MallOrderPreviewResponse preview;
  }

  @Data
  public static class BindRelationItem {
    private Long elderId;
    private String elderName;
    private String relation;
    private Boolean isPrimary;
  }

  @Data
  public static class BindCreateRequest {
    private Long elderId;
    @Size(max = 32)
    private String elderIdCardNo;
    private String relation;
    @Min(0)
    @Max(1)
    private Integer isPrimary;
    private String remark;
  }

  @Data
  public static class ProfileResponse {
    private String realName;
    private String phone;
    private String address;
    private String preferredContact;
  }

  @Data
  public static class NotificationSettingsResponse {
    private Boolean healthAlert;
    private Boolean paymentAlert;
    private Boolean activityAlert;
    private Boolean urgentAlert;
    private Boolean smsFallback;
    private List<String> subscribeTemplateIds = new ArrayList<>();
    private Boolean subscribeAuthorized;
    private String subscribeAuthorizedTime;
  }

  @Data
  public static class CapabilityStatusResponse {
    private Boolean smsEnabled;
    private String smsProvider;
    private Boolean smsDebugReturnCode;
    private Boolean wechatPayEnabled;
    private Boolean wechatNotifyEnabled;
    private Boolean wechatNotifyBound;
    private Boolean securityPasswordEnabled;
    private Boolean legacyApiEnabled;
    private String legacyApiSunsetDate;
    private Boolean legacyApiSunsetReached;
    private String checkedAt;
    private List<CapabilityStatusItem> items = new ArrayList<>();
  }

  @Data
  public static class CapabilityStatusItem {
    private String code;
    private String title;
    private String status;
    private String hint;
    private String actionPath;
  }

  @Data
  public static class AffectionMomentItem {
    private Long id;
    private String type;
    private String title;
    private String time;
    private String desc;
    private String mediaType;
    private String mediaUrl;
    private String mediaName;
  }

  @Data
  public static class SecuritySettingsResponse {
    private Boolean verifyHealthData;
    private Boolean verifyMedicalRecords;
    private Boolean verifyReports;
    private Boolean verifyWithSmsCode;
    private Boolean verifyWithPassword;
    private Boolean hasIndependentPassword;
    private Boolean maskSensitiveData;
    private String visibleScope;
  }

  @Data
  public static class CommunicationMessageItem {
    private Long id;
    private String targetRole;
    private String msgType;
    private String content;
    private String mediaUrl;
    private String mediaName;
    private Integer mediaDurationSec;
    private String transcript;
    private String time;
    private String sender;
    private String status;
  }

  @Data
  public static class CommunicationTemplateItem {
    private String id;
    private String title;
    private String targetRole;
    private String msgType;
    private String content;
    private String scene;
  }

  @Data
  public static class HelpFaqItem {
    private String category;
    private String question;
    private String answer;
  }

  @Data
  public static class FeedbackRecordItem {
    private Long id;
    private String feedbackType;
    private String serviceType;
    private Integer score;
    private String content;
    private String status;
    private String reply;
    private String createTime;
    private String updateTime;
  }

  @Data
  public static class TodoCenterResponse {
    private TodoCenterSummary summary = new TodoCenterSummary();
    private List<TodoItem> items = new ArrayList<>();
    private String refreshTime;
  }

  @Data
  public static class TodoCenterSummary {
    private Integer urgentCount;
    private Integer dueTodayCount;
    private Integer completedHintCount;
  }

  @Data
  public static class TodoItem {
    private String todoKey;
    private String type;
    private String priority;
    private String title;
    private String description;
    private String actionPath;
    private String dueTime;
    private String status;
    private Boolean actionable;
    private Boolean done;
    private Boolean snoozed;
  }

  @Data
  public static class TodoActionRequest {
    @NotBlank
    @Size(max = 120)
    private String todoKey;
    @NotBlank
    @Size(max = 20)
    private String action;
    @Min(5)
    @Max(24 * 60)
    private Integer snoozeMinutes;
  }

  @Data
  public static class TodoActionResponse {
    private String todoKey;
    private String action;
    private String status;
    private String message;
    private String nextRemindTime;
  }

  @Data
  public static class ServiceOrderCreateRequest {
    @NotNull
    private Long serviceId;
    private Long elderId;
    private String remark;
  }

  @Data
  public static class FeedbackRequest {
    private String feedbackType;
    private String serviceType;
    @Min(1)
    @Max(5)
    private Integer score;
    @NotBlank
    private String content;
    private String contact;
  }

  @Data
  public static class VideoBookRequest {
    @NotNull
    private Long elderId;
    @NotBlank
    private String bookingTime;
    private String remark;
  }

  @Data
  public static class ProfileUpdateRequest {
    private String realName;
    private String phone;
    private String address;
    private String preferredContact;
  }

  @Data
  public static class NotificationSettingsUpdateRequest {
    private Boolean healthAlert;
    private Boolean paymentAlert;
    private Boolean activityAlert;
    private Boolean urgentAlert;
    private Boolean smsFallback;
    private Boolean subscribeAuthorized;
  }

  @Data
  public static class AffectionMomentCreateRequest {
    @NotBlank
    private String type;
    @NotBlank
    private String title;
    @NotBlank
    private String desc;
    @Size(max = 20)
    private String mediaType;
    @Size(max = 1024)
    private String mediaUrl;
    @Size(max = 120)
    private String mediaName;
  }

  @Data
  public static class AutoPayUpdateRequest {
    @NotNull
    private Boolean enabled;
    private Long elderId;
    private Boolean authorizeConfirmed;
  }

  @Data
  public static class PaymentRechargeRequest {
    private Long elderId;
    @NotNull
    @Min(1)
    private BigDecimal amount;
    private String method;
    private String remark;
  }

  @Data
  public static class SecuritySettingsUpdateRequest {
    private Boolean verifyHealthData;
    private Boolean verifyMedicalRecords;
    private Boolean verifyReports;
    private Boolean verifyWithSmsCode;
    private Boolean verifyWithPassword;
    private Boolean maskSensitiveData;
    private String visibleScope;
  }

  @Data
  public static class SecuritySmsCodeSendRequest {
    @Size(max = 24)
    private String scene;
  }

  @Data
  public static class SecuritySmsCodeVerifyRequest {
    @NotBlank
    @Size(max = 10)
    private String verifyCode;
    @Size(max = 24)
    private String scene;
  }

  @Data
  public static class SecurityPasswordSetRequest {
    @NotBlank
    @Size(min = 6, max = 32)
    private String password;
  }

  @Data
  public static class SecurityPasswordVerifyRequest {
    @NotBlank
    @Size(min = 6, max = 32)
    private String password;
    @Size(max = 24)
    private String scene;
  }

  @Data
  public static class SecurityPasswordVerifyResponse {
    private Boolean passed;
    private String message;
  }

  @Data
  public static class ActivityAlbumCommentCreateRequest {
    @NotBlank
    @Size(max = 200)
    private String content;
  }

  @Data
  public static class AiAssessmentGenerateRequest {
    private Long elderId;
    @Size(max = 20)
    private String reportType;
  }

  @Data
  public static class AiAssessmentGenerateResponse {
    private Long elderId;
    private List<String> generatedTypes = new ArrayList<>();
    private Integer generatedCount;
    private String message;
  }

  @Data
  public static class CommunicationMessageCreateRequest {
    @NotBlank
    @Size(max = 20)
    private String targetRole;
    @NotBlank
    @Size(max = 20)
    private String msgType;
    @Size(max = 200)
    private String content;
    @Size(max = 1024)
    private String mediaUrl;
    @Size(max = 120)
    private String mediaName;
    @Min(1)
    @Max(600)
    private Integer mediaDurationSec;
    @Size(max = 200)
    private String transcript;
  }
}
