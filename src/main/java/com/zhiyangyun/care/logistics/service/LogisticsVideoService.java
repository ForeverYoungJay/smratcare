package com.zhiyangyun.care.logistics.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.audit.service.AuditLogService;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.common.web.ResourceNotFoundException;
import com.zhiyangyun.care.logistics.entity.*;
import com.zhiyangyun.care.logistics.mapper.*;
import com.zhiyangyun.care.logistics.model.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LogisticsVideoService {
  private final LogisticsCameraMapper cameras;
  private final LogisticsVideoPolicyMapper policies;
  private final LogisticsVideoRecordingMapper recordings;
  private final VideoStorage storage;
  private final AuditLogService audit;

  public LogisticsVideoService(LogisticsCameraMapper cameras, LogisticsVideoPolicyMapper policies,
      LogisticsVideoRecordingMapper recordings, VideoStorage storage, AuditLogService audit) {
    this.cameras = cameras;
    this.policies = policies;
    this.recordings = recordings;
    this.storage = storage;
    this.audit = audit;
  }

  public Long org() {
    Long id = AuthContext.getOrgId();
    if (id == null) throw new AccessDeniedException("录像操作必须具有机构上下文");
    return id;
  }

  public LogisticsVideoPolicy policy() { return policy(org(), false); }

  private LogisticsVideoPolicy policy(Long orgId, boolean lock) {
    var query = Wrappers.lambdaQuery(LogisticsVideoPolicy.class).eq(LogisticsVideoPolicy::getOrgId, orgId);
    if (lock) query.last("FOR UPDATE");
    LogisticsVideoPolicy policy = policies.selectOne(query);
    if (policy == null) {
      policy = new LogisticsVideoPolicy();
      policy.setOrgId(orgId);
      policy.setProvider("UNDECIDED");
      policy.setRetentionDays(90);
      policy.setAutoCleanup(false);
    }
    return policy;
  }

  @Transactional
  public LogisticsVideoPolicy savePolicy(VideoPolicyRequest request) {
    Long orgId = org();
    LogisticsVideoPolicy policy = policy(orgId, true);
    if (request.getRetentionDays() < 90) throw new IllegalArgumentException("录像至少保留90天");
    if (request.getAutoCleanup() && "UNDECIDED".equals(request.getProvider())) {
      throw new IllegalArgumentException("请先确定存储方式再开启自动清理");
    }
    if (request.getAutoCleanup() && !storage.configured(request.getProvider())) {
      throw new IllegalArgumentException("存储尚未配置，不能开启自动清理");
    }
    policy.setProvider(request.getProvider());
    policy.setRetentionDays(request.getRetentionDays());
    policy.setAutoCleanup(request.getAutoCleanup());
    policy.setBasis(request.getBasis());
    if (policy.getId() == null) policies.insert(policy); else policies.updateById(policy);
    // 延长策略同时保护既有录像；缩短策略不提前删除此前承诺保留的录像。
    recordings.update(null, Wrappers.<LogisticsVideoRecording>update()
        .eq("org_id", orgId).eq("status", "AVAILABLE")
        .setSql("expires_at = GREATEST(expires_at, TIMESTAMPADD(DAY, "
            + request.getRetentionDays() + ", ended_at))"));
    log(orgId, "VIDEO_POLICY_UPDATE", policy.getId(), "存储=" + policy.getProvider()
        + "，留存=" + policy.getRetentionDays() + "天，自动清理=" + policy.getAutoCleanup());
    return policy;
  }

  public IPage<LogisticsCamera> cameraPage(long pageNo, long pageSize, String keyword) {
    var query = Wrappers.lambdaQuery(LogisticsCamera.class).eq(LogisticsCamera::getOrgId, org());
    if (keyword != null && !keyword.isBlank()) query.and(q -> q.like(LogisticsCamera::getCameraCode, keyword)
        .or().like(LogisticsCamera::getCameraName, keyword).or().like(LogisticsCamera::getLocation, keyword));
    return cameras.selectPage(new Page<>(pageNo, pageSize), query.orderByDesc(LogisticsCamera::getCreateTime));
  }

  @Transactional
  public LogisticsCamera saveCamera(Long id, VideoCameraRequest request) {
    Long orgId = org();
    LogisticsCamera camera = id == null ? new LogisticsCamera() : camera(id, orgId);
    camera.setOrgId(orgId);
    camera.setCameraCode(request.getCameraCode().trim());
    camera.setCameraName(request.getCameraName().trim());
    camera.setLocation(request.getLocation().trim());
    camera.setChannelRef(request.getChannelRef().trim());
    camera.setProtocol(request.getProtocol());
    camera.setResponsibleName(request.getResponsibleName());
    camera.setEnabled(request.getEnabled());
    camera.setBitrateMbps(request.getBitrateMbps());
    if (id == null) cameras.insert(camera); else cameras.updateById(camera);
    log(orgId, "VIDEO_CAMERA_SAVE", camera.getId(), camera.getCameraCode());
    return camera;
  }

  private LogisticsCamera camera(Long id, Long orgId) {
    LogisticsCamera camera = cameras.selectOne(Wrappers.lambdaQuery(LogisticsCamera.class)
        .eq(LogisticsCamera::getId, id).eq(LogisticsCamera::getOrgId, orgId));
    if (camera == null) throw new ResourceNotFoundException("摄像头不存在或无权访问");
    return camera;
  }

  public IPage<LogisticsVideoRecording> recordingPage(long pageNo, long pageSize, Long cameraId,
      LocalDateTime from, LocalDateTime to, Boolean held) {
    if (from != null && to != null && !from.isBefore(to)) throw new IllegalArgumentException("结束时间必须晚于开始时间");
    return recordings.selectPage(new Page<>(pageNo, pageSize), Wrappers.lambdaQuery(LogisticsVideoRecording.class)
        .eq(LogisticsVideoRecording::getOrgId, org())
        .eq(cameraId != null, LogisticsVideoRecording::getCameraId, cameraId)
        .eq(held != null, LogisticsVideoRecording::getHeld, held)
        .gt(from != null, LogisticsVideoRecording::getEndedAt, from)
        .lt(to != null, LogisticsVideoRecording::getStartedAt, to)
        .orderByDesc(LogisticsVideoRecording::getStartedAt));
  }

  public LogisticsVideoRecording recording(Long id, boolean lock) {
    var query = Wrappers.lambdaQuery(LogisticsVideoRecording.class)
        .eq(LogisticsVideoRecording::getOrgId, org()).eq(LogisticsVideoRecording::getId, id);
    if (lock) query.last("FOR UPDATE");
    LogisticsVideoRecording recording = recordings.selectOne(query);
    if (recording == null) throw new ResourceNotFoundException("录像不存在或无权访问");
    return recording;
  }

  public LogisticsVideoRecording playback(Long id) {
    LogisticsVideoRecording recording = recording(id, false);
    if (!"AVAILABLE".equals(recording.getStatus())) throw new IllegalArgumentException("录像已清理，无法播放");
    log(org(), "VIDEO_PLAYBACK", id, "查看录像");
    return recording;
  }

  @Transactional
  public LogisticsVideoRecording hold(Long id, VideoHoldRequest request) {
    LogisticsVideoRecording recording = recording(id, true);
    if (!"AVAILABLE".equals(recording.getStatus())) throw new IllegalArgumentException("已清理的录像不能保全");
    recording.setHeld(request.getHeld());
    recording.setHoldReason(request.getReason().trim());
    recordings.updateById(recording);
    log(org(), request.getHeld() ? "VIDEO_HOLD" : "VIDEO_RELEASE", id, request.getReason());
    return recording;
  }

  @Transactional
  public LogisticsVideoRecording ingest(Long cameraId, String ingestKey, LocalDateTime startedAt,
      LocalDateTime endedAt, MultipartFile file) {
    Long orgId = org();
    if (!startedAt.isBefore(endedAt) || endedAt.isAfter(LocalDateTime.now().plusMinutes(5))
        || java.time.Duration.between(startedAt, endedAt).compareTo(java.time.Duration.ofMinutes(10)) > 0) {
      throw new IllegalArgumentException("录像时间不合法：单片不超过10分钟，结束时间不能超出当前时间5分钟");
    }
    LogisticsCamera camera = camera(cameraId, orgId);
    if (!Integer.valueOf(1).equals(camera.getEnabled())) throw new IllegalArgumentException("摄像头已停用");
    LogisticsVideoPolicy policy = policy(orgId, true);
    if (!storage.configured(policy.getProvider())) throw new IllegalArgumentException("请先确定并配置录像存储");
    LogisticsVideoRecording existing = recordings.selectOne(Wrappers.lambdaQuery(LogisticsVideoRecording.class)
        .eq(LogisticsVideoRecording::getOrgId, orgId).eq(LogisticsVideoRecording::getCameraId, cameraId)
        .eq(LogisticsVideoRecording::getIngestKey, ingestKey));
    if (existing != null) return existing;
    VideoStorage.Stored stored = storage.put(orgId, cameraId, policy.getProvider(), file);
    // 数据库提交失败时补偿文件写入；清理失败的孤儿文件由巡检按 objectKey 对账。
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override public void afterCompletion(int status) {
        if (status != STATUS_COMMITTED) storage.delete(policy.getProvider(), stored.key());
      }
    });
    LogisticsVideoRecording recording = new LogisticsVideoRecording();
    recording.setOrgId(orgId);
    recording.setCameraId(cameraId);
    recording.setIngestKey(ingestKey);
    recording.setProvider(policy.getProvider());
    recording.setObjectKey(stored.key());
    recording.setSizeBytes(stored.size());
    recording.setSha256(stored.sha256());
    recording.setStartedAt(startedAt);
    recording.setEndedAt(endedAt);
    recording.setExpiresAt(endedAt.plusDays(policy.getRetentionDays()));
    recording.setHeld(false);
    recording.setStatus("AVAILABLE");
    recordings.insert(recording);
    log(orgId, "VIDEO_INGEST", recording.getId(), "摄像头=" + cameraId + "，SHA256=" + stored.sha256());
    return recording;
  }

  public Map<String, Object> overview() {
    Long orgId = org();
    LogisticsVideoPolicy policy = policy(orgId, false);
    List<LogisticsCamera> cameraList = cameras.selectList(Wrappers.lambdaQuery(LogisticsCamera.class)
        .eq(LogisticsCamera::getOrgId, orgId));
    List<Map<String, Object>> health = new ArrayList<>();
    BigDecimal mbps = BigDecimal.ZERO;
    int missing = 0;
    for (LogisticsCamera camera : cameraList) {
      if (!Integer.valueOf(1).equals(camera.getEnabled())) continue;
      mbps = mbps.add(camera.getBitrateMbps());
      LogisticsVideoRecording latest = recordings.selectOne(Wrappers.lambdaQuery(LogisticsVideoRecording.class)
          .eq(LogisticsVideoRecording::getOrgId, orgId).eq(LogisticsVideoRecording::getCameraId, camera.getId())
          .eq(LogisticsVideoRecording::getStatus, "AVAILABLE")
          .orderByDesc(LogisticsVideoRecording::getEndedAt).last("LIMIT 1"));
      boolean stale = latest == null || latest.getEndedAt().isBefore(LocalDateTime.now().minusMinutes(15));
      if (stale) missing++;
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("cameraId", camera.getId());
      item.put("cameraName", camera.getCameraName());
      item.put("state", latest == null ? "NOT_CONNECTED" : stale ? "STALE" : "RECEIVING");
      item.put("lastRecordedAt", latest == null ? null : latest.getEndedAt());
      health.add(item);
    }
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("cameraCount", cameraList.size());
    result.put("enabledCount", health.size());
    result.put("missingCount", missing);
    result.put("cameraHealth", health);
    result.put("policy", policy);
    result.put("storageConfigured", storage.configured(policy.getProvider()));
    result.put("maxSegmentBytes", storage.getMaxSegmentBytes());
    // 十进制 TB，含20%容量余量；不含 RAID、保全及备份副本。
    result.put("estimatedTb", mbps.multiply(BigDecimal.valueOf(86400L * policy.getRetentionDays()))
        .multiply(BigDecimal.valueOf(1.2)).divide(BigDecimal.valueOf(8_000_000), 2, RoundingMode.UP));
    result.put("uploadMbps", mbps);
    result.put("checkedAt", LocalDateTime.now());
    return result;
  }

  @Transactional
  public int cleanup(Long orgId) {
    LogisticsVideoPolicy policy = policy(orgId, true);
    if (!Boolean.TRUE.equals(policy.getAutoCleanup())) return 0;
    LocalDateTime now = LocalDateTime.now();
    List<LogisticsVideoRecording> expired = recordings.selectList(Wrappers.lambdaQuery(LogisticsVideoRecording.class)
        .eq(LogisticsVideoRecording::getOrgId, orgId).eq(LogisticsVideoRecording::getStatus, "AVAILABLE")
        .eq(LogisticsVideoRecording::getHeld, false).le(LogisticsVideoRecording::getExpiresAt, now)
        .le(LogisticsVideoRecording::getEndedAt, now.minusDays(Math.max(90, policy.getRetentionDays())))
        .orderByAsc(LogisticsVideoRecording::getExpiresAt).last("LIMIT 100 FOR UPDATE"));
    for (LogisticsVideoRecording recording : expired) {
      storage.delete(recording.getProvider(), recording.getObjectKey());
      recording.setStatus("DELETED");
      recordings.updateById(recording);
      log(orgId, "VIDEO_EXPIRED_DELETE", recording.getId(), "到期清理；未保全");
    }
    return expired.size();
  }

  private void log(Long orgId, String action, Long id, String detail) {
    audit.record(orgId, orgId, AuthContext.getStaffId(), AuthContext.getUsername(), action, "LOGISTICS_VIDEO", id, detail);
  }
}
