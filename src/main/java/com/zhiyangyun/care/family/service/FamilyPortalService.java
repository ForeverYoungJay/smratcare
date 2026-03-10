package com.zhiyangyun.care.family.service;

import com.zhiyangyun.care.family.model.FamilyPortalModels;
import java.util.List;
import java.util.Map;

public interface FamilyPortalService {
  FamilyPortalModels.HomeDashboardResponse getHomeDashboard(Long orgId, Long familyUserId, Long elderId);

  FamilyPortalModels.WeeklyBriefResponse getWeeklyBrief(Long orgId, Long familyUserId, Long elderId);

  List<FamilyPortalModels.WeeklyBriefDigestItem> listWeeklyBriefHistory(Long orgId, Long familyUserId, Long elderId, Integer weeks);

  List<FamilyPortalModels.MessageItem> listMessages(Long orgId, Long familyUserId, int pageNo, int pageSize);

  List<FamilyPortalModels.MessageItem> listMessages(Long orgId, Long familyUserId, int pageNo, int pageSize,
      String level, String type, Boolean unreadOnly);

  FamilyPortalModels.MessageSummaryResponse getMessageSummary(Long orgId, Long familyUserId);

  FamilyPortalModels.MessageItem getMessageDetail(Long orgId, Long familyUserId, Long messageId);

  boolean markMessageRead(Long orgId, Long familyUserId, Long messageId);

  boolean markAllMessagesRead(Long orgId, Long familyUserId);

  FamilyPortalModels.HealthTrendResponse getHealthTrend(Long orgId, Long familyUserId, Long elderId, String range);

  List<FamilyPortalModels.MedicalRecordItem> listMedicalRecords(Long orgId, Long familyUserId, Long elderId);

  List<FamilyPortalModels.AssessmentReportItem> listAssessmentReports(Long orgId, Long familyUserId, Long elderId);

  List<FamilyPortalModels.ScheduleItem> listTodaySchedules(Long orgId, Long familyUserId, Long elderId);

  List<FamilyPortalModels.MealSummary> listMealCalendar(Long orgId);

  List<FamilyPortalModels.CareLogDay> listCareLogs(Long orgId, Long familyUserId, Long elderId);

  List<FamilyPortalModels.ActivityAlbumItem> listActivityAlbums(Long orgId, Long familyUserId, int pageNo, int pageSize);

  FamilyPortalModels.ActivityAlbumItem toggleAlbumLike(Long orgId, Long familyUserId, Long albumId);

  List<FamilyPortalModels.ActivityAlbumCommentItem> listActivityAlbumComments(Long orgId, Long familyUserId, Long albumId);

  FamilyPortalModels.ActivityAlbumCommentItem addActivityAlbumComment(Long orgId, Long familyUserId, Long albumId,
      FamilyPortalModels.ActivityAlbumCommentCreateRequest request);

  List<FamilyPortalModels.OutingRecordItem> listOutingRecords(Long orgId, Long familyUserId, Long elderId);

  List<FamilyPortalModels.EmergencyContactItem> listEmergencyContacts(Long orgId, Long familyUserId);

  FamilyPortalModels.PaymentSummaryResponse getPaymentSummary(Long orgId, Long familyUserId, Long elderId, String month);

  FamilyPortalModels.PaymentGuardResponse getPaymentGuard(Long orgId, Long familyUserId, Long elderId);

  List<FamilyPortalModels.PaymentHistoryItem> listPaymentHistory(Long orgId, Long familyUserId, Long elderId);

  FamilyPortalModels.PaymentRechargeResponse recharge(Long orgId, Long familyUserId,
      FamilyPortalModels.PaymentRechargeRequest request);

  FamilyPortalModels.WechatRechargePrepayResponse createWechatRechargePrepay(Long orgId, Long familyUserId,
      FamilyPortalModels.WechatRechargePrepayRequest request);

  FamilyPortalModels.WechatNotifyBindResponse bindWechatNotifyOpenId(Long orgId, Long familyUserId,
      FamilyPortalModels.WechatNotifyBindRequest request);

  List<FamilyPortalModels.RechargeOrderItem> listRechargeOrders(Long orgId, Long familyUserId, Long elderId,
      int pageNo, int pageSize);

  FamilyPortalModels.RechargeOrderItem getRechargeOrder(Long orgId, Long familyUserId, String outTradeNo);

  boolean handleWechatPayNotify(String payload, Map<String, String> headers);

  int closeExpiredRechargeOrders();

  boolean updateAutoPay(Long orgId, Long familyUserId, Long elderId, boolean enabled, Boolean authorizeConfirmed);

  int executeAutoPaySettlements();

  List<FamilyPortalModels.ServiceCatalogItem> listServiceCatalog(Long orgId);

  List<FamilyPortalModels.ServiceOrderItem> listServiceOrders(Long orgId, Long familyUserId, Long elderId);

  FamilyPortalModels.ServiceOrderItem createServiceOrder(Long orgId, Long familyUserId,
      FamilyPortalModels.ServiceOrderCreateRequest request);

  List<FamilyPortalModels.MallProductItem> listMallProducts(Long orgId, Long familyUserId, String keyword, String category,
      int pageNo, int pageSize);

  FamilyPortalModels.MallOrderPreviewResponse previewMallOrder(Long orgId, Long familyUserId,
      FamilyPortalModels.MallOrderPreviewRequest request);

  FamilyPortalModels.MallOrderSubmitResponse submitMallOrder(Long orgId, Long familyUserId,
      FamilyPortalModels.MallOrderSubmitRequest request);

  List<FamilyPortalModels.MallOrderItem> listMallOrders(Long orgId, Long familyUserId, Long elderId, int pageNo, int pageSize);

  FamilyPortalModels.MallOrderDetailResponse getMallOrderDetail(Long orgId, Long familyUserId, Long orderId);

  FamilyPortalModels.MallOrderActionResponse cancelMallOrder(Long orgId, Long familyUserId, Long orderId,
      FamilyPortalModels.MallOrderActionRequest request);

  FamilyPortalModels.MallOrderActionResponse refundMallOrder(Long orgId, Long familyUserId, Long orderId,
      FamilyPortalModels.MallOrderActionRequest request);

  void submitFeedback(Long orgId, Long familyUserId, FamilyPortalModels.FeedbackRequest request);

  List<FamilyPortalModels.FeedbackRecordItem> listFeedbackRecords(Long orgId, Long familyUserId, int pageNo, int pageSize);

  com.zhiyangyun.care.visit.model.VisitBookingResponse bookVideoVisit(Long orgId, Long familyUserId,
      FamilyPortalModels.VideoBookRequest request);

  FamilyPortalModels.BindRelationItem bindElder(Long orgId, Long familyUserId,
      FamilyPortalModels.BindCreateRequest request);

  List<FamilyPortalModels.BindRelationItem> listBindings(Long orgId, Long familyUserId);

  void removeBinding(Long orgId, Long familyUserId, Long elderId);

  FamilyPortalModels.ProfileResponse getProfile(Long orgId, Long familyUserId);

  FamilyPortalModels.ProfileResponse updateProfile(Long orgId, Long familyUserId,
      FamilyPortalModels.ProfileUpdateRequest request);

  FamilyPortalModels.NotificationSettingsResponse getNotificationSettings(Long orgId, Long familyUserId);

  FamilyPortalModels.NotificationSettingsResponse updateNotificationSettings(Long orgId, Long familyUserId,
      FamilyPortalModels.NotificationSettingsUpdateRequest request);

  FamilyPortalModels.CapabilityStatusResponse getCapabilityStatus(Long orgId, Long familyUserId);

  FamilyPortalModels.SecuritySettingsResponse getSecuritySettings(Long orgId, Long familyUserId);

  FamilyPortalModels.SecuritySettingsResponse updateSecuritySettings(Long orgId, Long familyUserId,
      FamilyPortalModels.SecuritySettingsUpdateRequest request);

  boolean setSecurityPassword(Long orgId, Long familyUserId, FamilyPortalModels.SecurityPasswordSetRequest request);

  FamilyPortalModels.SecurityPasswordVerifyResponse verifySecurityPassword(Long orgId, Long familyUserId,
      FamilyPortalModels.SecurityPasswordVerifyRequest request);

  List<FamilyPortalModels.AffectionMomentItem> listAffectionMoments(Long orgId, Long familyUserId);

  List<String> listAffectionTemplates();

  void addAffectionMoment(Long orgId, Long familyUserId, FamilyPortalModels.AffectionMomentCreateRequest request);

  List<FamilyPortalModels.CommunicationMessageItem> listCommunicationMessages(Long orgId, Long familyUserId,
      int pageNo, int pageSize);

  FamilyPortalModels.CommunicationMessageItem sendCommunicationMessage(Long orgId, Long familyUserId,
      FamilyPortalModels.CommunicationMessageCreateRequest request);

  List<FamilyPortalModels.CommunicationTemplateItem> listCommunicationTemplates(Long orgId);

  List<FamilyPortalModels.HelpFaqItem> listHelpFaqs();

  FamilyPortalModels.TodoCenterResponse getTodoCenter(Long orgId, Long familyUserId, Long elderId);

  FamilyPortalModels.TodoActionResponse handleTodoAction(Long orgId, Long familyUserId,
      FamilyPortalModels.TodoActionRequest request);

  FamilyPortalModels.AiAssessmentGenerateResponse generateAiAssessmentReports(Long orgId, Long familyUserId,
      FamilyPortalModels.AiAssessmentGenerateRequest request);
}
