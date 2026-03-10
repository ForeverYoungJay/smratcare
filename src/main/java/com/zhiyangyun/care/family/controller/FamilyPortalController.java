package com.zhiyangyun.care.family.controller;

import com.zhiyangyun.care.auth.model.FamilySmsCodeSendResponse;
import com.zhiyangyun.care.auth.model.FamilySmsCodeVerifyResponse;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.family.model.FamilyPortalModels;
import com.zhiyangyun.care.family.service.FamilyPortalService;
import com.zhiyangyun.care.family.service.FamilySmsCodeService;
import com.zhiyangyun.care.visit.model.VisitBookingResponse;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/family")
public class FamilyPortalController {
  private final FamilyPortalService familyPortalService;
  private final FamilySmsCodeService familySmsCodeService;

  public FamilyPortalController(FamilyPortalService familyPortalService,
      FamilySmsCodeService familySmsCodeService) {
    this.familyPortalService = familyPortalService;
    this.familySmsCodeService = familySmsCodeService;
  }

  @GetMapping("/dashboard/home")
  public Result<FamilyPortalModels.HomeDashboardResponse> home(@RequestParam(required = false) Long elderId) {
    return Result.ok(familyPortalService.getHomeDashboard(AuthContext.getOrgId(), AuthContext.getStaffId(), elderId));
  }

  @GetMapping("/dashboard/weekly-brief")
  public Result<FamilyPortalModels.WeeklyBriefResponse> weeklyBrief(@RequestParam(required = false) Long elderId) {
    return Result.ok(familyPortalService.getWeeklyBrief(AuthContext.getOrgId(), AuthContext.getStaffId(), elderId));
  }

  @GetMapping("/dashboard/weekly-brief/history")
  public Result<List<FamilyPortalModels.WeeklyBriefDigestItem>> weeklyBriefHistory(
      @RequestParam(required = false) Long elderId,
      @RequestParam(defaultValue = "8") Integer weeks) {
    return Result.ok(familyPortalService.listWeeklyBriefHistory(
        AuthContext.getOrgId(), AuthContext.getStaffId(), elderId, weeks));
  }

  @GetMapping("/messages/page")
  public Result<List<FamilyPortalModels.MessageItem>> messages(
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize,
      @RequestParam(required = false) String level,
      @RequestParam(required = false) String type,
      @RequestParam(required = false) Boolean unreadOnly) {
    return Result.ok(familyPortalService.listMessages(
        AuthContext.getOrgId(), AuthContext.getStaffId(), pageNo, pageSize, level, type, unreadOnly));
  }

  @GetMapping("/messages/summary")
  public Result<FamilyPortalModels.MessageSummaryResponse> messageSummary() {
    return Result.ok(familyPortalService.getMessageSummary(AuthContext.getOrgId(), AuthContext.getStaffId()));
  }

  @GetMapping("/messages/{id}")
  public Result<FamilyPortalModels.MessageItem> messageDetail(@PathVariable Long id) {
    return Result.ok(familyPortalService.getMessageDetail(AuthContext.getOrgId(), AuthContext.getStaffId(), id));
  }

  @PostMapping("/messages/{id}/read")
  public Result<Boolean> markMessageRead(@PathVariable Long id) {
    return Result.ok(familyPortalService.markMessageRead(AuthContext.getOrgId(), AuthContext.getStaffId(), id));
  }

  @PostMapping("/messages/read-all")
  public Result<Boolean> markAllMessagesRead() {
    return Result.ok(familyPortalService.markAllMessagesRead(AuthContext.getOrgId(), AuthContext.getStaffId()));
  }

  @GetMapping("/health/trend")
  public Result<FamilyPortalModels.HealthTrendResponse> healthTrend(
      @RequestParam(required = false) Long elderId,
      @RequestParam(defaultValue = "7d") String range) {
    return Result.ok(familyPortalService.getHealthTrend(AuthContext.getOrgId(), AuthContext.getStaffId(), elderId, range));
  }

  @GetMapping("/medical-records")
  public Result<List<FamilyPortalModels.MedicalRecordItem>> medicalRecords(
      @RequestParam(required = false) Long elderId) {
    return Result.ok(familyPortalService.listMedicalRecords(AuthContext.getOrgId(), AuthContext.getStaffId(), elderId));
  }

  @GetMapping("/assessment-reports")
  public Result<List<FamilyPortalModels.AssessmentReportItem>> assessmentReports(
      @RequestParam(required = false) Long elderId) {
    return Result.ok(familyPortalService.listAssessmentReports(AuthContext.getOrgId(), AuthContext.getStaffId(), elderId));
  }

  @GetMapping("/schedules/today")
  public Result<List<FamilyPortalModels.ScheduleItem>> schedules(
      @RequestParam(required = false) Long elderId) {
    return Result.ok(familyPortalService.listTodaySchedules(AuthContext.getOrgId(), AuthContext.getStaffId(), elderId));
  }

  @GetMapping("/meals/calendar")
  public Result<List<FamilyPortalModels.MealSummary>> mealCalendar() {
    return Result.ok(familyPortalService.listMealCalendar(AuthContext.getOrgId()));
  }

  @GetMapping("/care-logs")
  public Result<List<FamilyPortalModels.CareLogDay>> careLogs(
      @RequestParam(required = false) Long elderId) {
    return Result.ok(familyPortalService.listCareLogs(AuthContext.getOrgId(), AuthContext.getStaffId(), elderId));
  }

  @GetMapping("/activity-albums")
  public Result<List<FamilyPortalModels.ActivityAlbumItem>> activityAlbums(
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize) {
    return Result.ok(familyPortalService.listActivityAlbums(AuthContext.getOrgId(), AuthContext.getStaffId(), pageNo, pageSize));
  }

  @PostMapping("/activity-albums/{id}/like")
  public Result<FamilyPortalModels.ActivityAlbumItem> toggleAlbumLike(@PathVariable Long id) {
    return Result.ok(familyPortalService.toggleAlbumLike(AuthContext.getOrgId(), AuthContext.getStaffId(), id));
  }

  @GetMapping("/activity-albums/{id}/comments")
  public Result<List<FamilyPortalModels.ActivityAlbumCommentItem>> activityAlbumComments(@PathVariable Long id) {
    return Result.ok(familyPortalService.listActivityAlbumComments(
        AuthContext.getOrgId(), AuthContext.getStaffId(), id));
  }

  @PostMapping("/activity-albums/{id}/comments")
  public Result<FamilyPortalModels.ActivityAlbumCommentItem> addActivityAlbumComment(@PathVariable Long id,
      @Valid @RequestBody FamilyPortalModels.ActivityAlbumCommentCreateRequest request) {
    return Result.ok(familyPortalService.addActivityAlbumComment(
        AuthContext.getOrgId(), AuthContext.getStaffId(), id, request));
  }

  @GetMapping("/outing-records")
  public Result<List<FamilyPortalModels.OutingRecordItem>> outingRecords(
      @RequestParam(required = false) Long elderId) {
    return Result.ok(familyPortalService.listOutingRecords(AuthContext.getOrgId(), AuthContext.getStaffId(), elderId));
  }

  @GetMapping("/emergency-contacts")
  public Result<List<FamilyPortalModels.EmergencyContactItem>> emergencyContacts() {
    return Result.ok(familyPortalService.listEmergencyContacts(AuthContext.getOrgId(), AuthContext.getStaffId()));
  }

  @GetMapping("/payment/summary")
  public Result<FamilyPortalModels.PaymentSummaryResponse> paymentSummary(
      @RequestParam(required = false) Long elderId,
      @RequestParam(required = false) String month) {
    return Result.ok(familyPortalService.getPaymentSummary(AuthContext.getOrgId(), AuthContext.getStaffId(), elderId, month));
  }

  @GetMapping("/payment/guard")
  public Result<FamilyPortalModels.PaymentGuardResponse> paymentGuard(
      @RequestParam(required = false) Long elderId) {
    return Result.ok(familyPortalService.getPaymentGuard(AuthContext.getOrgId(), AuthContext.getStaffId(), elderId));
  }

  @GetMapping("/payment/history")
  public Result<List<FamilyPortalModels.PaymentHistoryItem>> paymentHistory(
      @RequestParam(required = false) Long elderId) {
    return Result.ok(familyPortalService.listPaymentHistory(AuthContext.getOrgId(), AuthContext.getStaffId(), elderId));
  }

  @PostMapping("/payment/recharge")
  public Result<FamilyPortalModels.PaymentRechargeResponse> recharge(
      @Valid @RequestBody FamilyPortalModels.PaymentRechargeRequest request) {
    return Result.ok(familyPortalService.recharge(AuthContext.getOrgId(), AuthContext.getStaffId(), request));
  }

  @PostMapping("/payment/wechat/prepay")
  public Result<FamilyPortalModels.WechatRechargePrepayResponse> createWechatRechargePrepay(
      @Valid @RequestBody FamilyPortalModels.WechatRechargePrepayRequest request) {
    return Result.ok(familyPortalService.createWechatRechargePrepay(
        AuthContext.getOrgId(), AuthContext.getStaffId(), request));
  }

  @PostMapping("/wechat/notify/bind-openid")
  public Result<FamilyPortalModels.WechatNotifyBindResponse> bindWechatNotifyOpenId(
      @RequestBody(required = false) FamilyPortalModels.WechatNotifyBindRequest request) {
    return Result.ok(familyPortalService.bindWechatNotifyOpenId(
        AuthContext.getOrgId(), AuthContext.getStaffId(), request));
  }

  @GetMapping("/payment/recharge-orders")
  public Result<List<FamilyPortalModels.RechargeOrderItem>> rechargeOrders(
      @RequestParam(required = false) Long elderId,
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize) {
    return Result.ok(familyPortalService.listRechargeOrders(
        AuthContext.getOrgId(), AuthContext.getStaffId(), elderId, pageNo, pageSize));
  }

  @GetMapping("/payment/recharge-orders/{outTradeNo}")
  public Result<FamilyPortalModels.RechargeOrderItem> rechargeOrder(@PathVariable String outTradeNo) {
    return Result.ok(familyPortalService.getRechargeOrder(
        AuthContext.getOrgId(), AuthContext.getStaffId(), outTradeNo));
  }

  @PutMapping("/payment/auto-pay")
  public Result<Boolean> updateAutoPay(@Valid @RequestBody FamilyPortalModels.AutoPayUpdateRequest request) {
    return Result.ok(familyPortalService.updateAutoPay(
        AuthContext.getOrgId(), AuthContext.getStaffId(), request.getElderId(),
        request.getEnabled(), request.getAuthorizeConfirmed()));
  }

  @GetMapping("/service/catalog")
  public Result<List<FamilyPortalModels.ServiceCatalogItem>> serviceCatalog() {
    return Result.ok(familyPortalService.listServiceCatalog(AuthContext.getOrgId()));
  }

  @GetMapping("/service/orders")
  public Result<List<FamilyPortalModels.ServiceOrderItem>> serviceOrders(
      @RequestParam(required = false) Long elderId) {
    return Result.ok(familyPortalService.listServiceOrders(AuthContext.getOrgId(), AuthContext.getStaffId(), elderId));
  }

  @PostMapping("/service/orders")
  public Result<FamilyPortalModels.ServiceOrderItem> createServiceOrder(
      @Valid @RequestBody FamilyPortalModels.ServiceOrderCreateRequest request) {
    return Result.ok(familyPortalService.createServiceOrder(AuthContext.getOrgId(), AuthContext.getStaffId(), request));
  }

  @GetMapping("/mall/products")
  public Result<List<FamilyPortalModels.MallProductItem>> mallProducts(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String category,
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize) {
    return Result.ok(familyPortalService.listMallProducts(
        AuthContext.getOrgId(), AuthContext.getStaffId(), keyword, category, pageNo, pageSize));
  }

  @PostMapping("/mall/orders/preview")
  public Result<FamilyPortalModels.MallOrderPreviewResponse> previewMallOrder(
      @Valid @RequestBody FamilyPortalModels.MallOrderPreviewRequest request) {
    return Result.ok(familyPortalService.previewMallOrder(
        AuthContext.getOrgId(), AuthContext.getStaffId(), request));
  }

  @PostMapping("/mall/orders/submit")
  public Result<FamilyPortalModels.MallOrderSubmitResponse> submitMallOrder(
      @Valid @RequestBody FamilyPortalModels.MallOrderSubmitRequest request) {
    return Result.ok(familyPortalService.submitMallOrder(
        AuthContext.getOrgId(), AuthContext.getStaffId(), request));
  }

  @GetMapping("/mall/orders")
  public Result<List<FamilyPortalModels.MallOrderItem>> mallOrders(
      @RequestParam(required = false) Long elderId,
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize) {
    return Result.ok(familyPortalService.listMallOrders(
        AuthContext.getOrgId(), AuthContext.getStaffId(), elderId, pageNo, pageSize));
  }

  @PostMapping("/feedback")
  public Result<Void> submitFeedback(@Valid @RequestBody FamilyPortalModels.FeedbackRequest request) {
    familyPortalService.submitFeedback(AuthContext.getOrgId(), AuthContext.getStaffId(), request);
    return Result.ok(null);
  }

  @GetMapping("/feedback/records")
  public Result<List<FamilyPortalModels.FeedbackRecordItem>> feedbackRecords(
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize) {
    return Result.ok(familyPortalService.listFeedbackRecords(
        AuthContext.getOrgId(), AuthContext.getStaffId(), pageNo, pageSize));
  }

  @PostMapping("/visit/video/book")
  public Result<VisitBookingResponse> bookVideoVisit(@Valid @RequestBody FamilyPortalModels.VideoBookRequest request) {
    return Result.ok(familyPortalService.bookVideoVisit(AuthContext.getOrgId(), AuthContext.getStaffId(), request));
  }

  @PostMapping("/bindings")
  public Result<FamilyPortalModels.BindRelationItem> bindElder(
      @Valid @RequestBody FamilyPortalModels.BindCreateRequest request) {
    return Result.ok(familyPortalService.bindElder(AuthContext.getOrgId(), AuthContext.getStaffId(), request));
  }

  @GetMapping("/bindings")
  public Result<List<FamilyPortalModels.BindRelationItem>> bindings() {
    return Result.ok(familyPortalService.listBindings(AuthContext.getOrgId(), AuthContext.getStaffId()));
  }

  @DeleteMapping("/bindings/{elderId}")
  public Result<Void> removeBinding(@PathVariable Long elderId) {
    familyPortalService.removeBinding(AuthContext.getOrgId(), AuthContext.getStaffId(), elderId);
    return Result.ok(null);
  }

  @GetMapping("/profile")
  public Result<FamilyPortalModels.ProfileResponse> profile() {
    return Result.ok(familyPortalService.getProfile(AuthContext.getOrgId(), AuthContext.getStaffId()));
  }

  @PutMapping("/profile")
  public Result<FamilyPortalModels.ProfileResponse> updateProfile(
      @RequestBody FamilyPortalModels.ProfileUpdateRequest request) {
    return Result.ok(familyPortalService.updateProfile(AuthContext.getOrgId(), AuthContext.getStaffId(), request));
  }

  @GetMapping("/notification-settings")
  public Result<FamilyPortalModels.NotificationSettingsResponse> notificationSettings() {
    return Result.ok(familyPortalService.getNotificationSettings(AuthContext.getOrgId(), AuthContext.getStaffId()));
  }

  @PutMapping("/notification-settings")
  public Result<FamilyPortalModels.NotificationSettingsResponse> updateNotificationSettings(
      @RequestBody FamilyPortalModels.NotificationSettingsUpdateRequest request) {
    return Result.ok(familyPortalService.updateNotificationSettings(AuthContext.getOrgId(), AuthContext.getStaffId(), request));
  }

  @GetMapping("/capabilities/status")
  public Result<FamilyPortalModels.CapabilityStatusResponse> capabilityStatus() {
    return Result.ok(familyPortalService.getCapabilityStatus(AuthContext.getOrgId(), AuthContext.getStaffId()));
  }

  @GetMapping("/security-settings")
  public Result<FamilyPortalModels.SecuritySettingsResponse> securitySettings() {
    return Result.ok(familyPortalService.getSecuritySettings(AuthContext.getOrgId(), AuthContext.getStaffId()));
  }

  @PutMapping("/security-settings")
  public Result<FamilyPortalModels.SecuritySettingsResponse> updateSecuritySettings(
      @RequestBody FamilyPortalModels.SecuritySettingsUpdateRequest request) {
    return Result.ok(familyPortalService.updateSecuritySettings(AuthContext.getOrgId(), AuthContext.getStaffId(), request));
  }

  @PostMapping("/security/sms-code/send")
  public Result<FamilySmsCodeSendResponse> sendSecuritySmsCode(
      @RequestBody(required = false) FamilyPortalModels.SecuritySmsCodeSendRequest request,
      HttpServletRequest httpServletRequest) {
    String scene = request == null ? null : request.getScene();
    return Result.ok(familySmsCodeService.sendCodeForFamilyUser(
        AuthContext.getOrgId(), AuthContext.getStaffId(), scene, resolveClientIp(httpServletRequest)));
  }

  @PostMapping("/security/sms-code/verify")
  public Result<FamilySmsCodeVerifyResponse> verifySecuritySmsCode(
      @Valid @RequestBody FamilyPortalModels.SecuritySmsCodeVerifyRequest request,
      HttpServletRequest httpServletRequest) {
    return Result.ok(familySmsCodeService.verifyCodeForFamilyUser(
        AuthContext.getOrgId(), AuthContext.getStaffId(), request.getScene(), request.getVerifyCode(), true,
        resolveClientIp(httpServletRequest)));
  }

  @PostMapping("/security/password/set")
  public Result<Boolean> setSecurityPassword(@Valid @RequestBody FamilyPortalModels.SecurityPasswordSetRequest request) {
    return Result.ok(familyPortalService.setSecurityPassword(AuthContext.getOrgId(), AuthContext.getStaffId(), request));
  }

  @PostMapping("/security/password/verify")
  public Result<FamilyPortalModels.SecurityPasswordVerifyResponse> verifySecurityPassword(
      @Valid @RequestBody FamilyPortalModels.SecurityPasswordVerifyRequest request) {
    return Result.ok(familyPortalService.verifySecurityPassword(
        AuthContext.getOrgId(), AuthContext.getStaffId(), request));
  }

  @GetMapping("/affection/moments")
  public Result<List<FamilyPortalModels.AffectionMomentItem>> affectionMoments() {
    return Result.ok(familyPortalService.listAffectionMoments(AuthContext.getOrgId(), AuthContext.getStaffId()));
  }

  @PostMapping("/affection/moments")
  public Result<Void> addAffectionMoment(@Valid @RequestBody FamilyPortalModels.AffectionMomentCreateRequest request) {
    familyPortalService.addAffectionMoment(AuthContext.getOrgId(), AuthContext.getStaffId(), request);
    return Result.ok(null);
  }

  @GetMapping("/affection/templates")
  public Result<List<String>> affectionTemplates() {
    return Result.ok(familyPortalService.listAffectionTemplates());
  }

  @GetMapping("/communication/messages")
  public Result<List<FamilyPortalModels.CommunicationMessageItem>> communicationMessages(
      @RequestParam(defaultValue = "1") int pageNo,
      @RequestParam(defaultValue = "20") int pageSize) {
    return Result.ok(familyPortalService.listCommunicationMessages(
        AuthContext.getOrgId(), AuthContext.getStaffId(), pageNo, pageSize));
  }

  @PostMapping("/communication/messages")
  public Result<FamilyPortalModels.CommunicationMessageItem> sendCommunicationMessage(
      @Valid @RequestBody FamilyPortalModels.CommunicationMessageCreateRequest request) {
    return Result.ok(familyPortalService.sendCommunicationMessage(
        AuthContext.getOrgId(), AuthContext.getStaffId(), request));
  }

  @GetMapping("/communication/templates")
  public Result<List<FamilyPortalModels.CommunicationTemplateItem>> communicationTemplates() {
    return Result.ok(familyPortalService.listCommunicationTemplates(AuthContext.getOrgId()));
  }

  @GetMapping("/help/faqs")
  public Result<List<FamilyPortalModels.HelpFaqItem>> helpFaqs() {
    return Result.ok(familyPortalService.listHelpFaqs());
  }

  @PostMapping("/assessment-reports/generate-ai")
  public Result<FamilyPortalModels.AiAssessmentGenerateResponse> generateAiAssessmentReports(
      @RequestBody(required = false) FamilyPortalModels.AiAssessmentGenerateRequest request) {
    FamilyPortalModels.AiAssessmentGenerateRequest payload = request == null
        ? new FamilyPortalModels.AiAssessmentGenerateRequest()
        : request;
    return Result.ok(familyPortalService.generateAiAssessmentReports(
        AuthContext.getOrgId(), AuthContext.getStaffId(), payload));
  }

  @GetMapping("/todo-center")
  public Result<FamilyPortalModels.TodoCenterResponse> todoCenter(
      @RequestParam(required = false) Long elderId) {
    return Result.ok(familyPortalService.getTodoCenter(AuthContext.getOrgId(), AuthContext.getStaffId(), elderId));
  }

  @PostMapping("/todo-center/action")
  public Result<FamilyPortalModels.TodoActionResponse> todoCenterAction(
      @Valid @RequestBody FamilyPortalModels.TodoActionRequest request) {
    return Result.ok(familyPortalService.handleTodoAction(AuthContext.getOrgId(), AuthContext.getStaffId(), request));
  }

  private String resolveClientIp(HttpServletRequest request) {
    if (request == null) {
      return "";
    }
    String[] candidateHeaders = {
        "X-Forwarded-For",
        "X-Real-IP",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_CLIENT_IP",
        "HTTP_X_FORWARDED_FOR"
    };
    for (String header : candidateHeaders) {
      String value = request.getHeader(header);
      if (value != null && !value.isBlank() && !"unknown".equalsIgnoreCase(value.trim())) {
        return value.trim();
      }
    }
    String remoteAddr = request.getRemoteAddr();
    return remoteAddr == null ? "" : remoteAddr.trim();
  }
}
