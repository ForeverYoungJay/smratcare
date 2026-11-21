package com.zhiyangyun.care.fire.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhiyangyun.care.auth.entity.StaffAccount;
import com.zhiyangyun.care.auth.mapper.StaffMapper;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.common.web.ResourceNotFoundException;
import com.zhiyangyun.care.fire.entity.FireVideoAccessLog;
import com.zhiyangyun.care.fire.entity.FireVideoAccessRequest;
import com.zhiyangyun.care.fire.mapper.FireVideoAccessLogMapper;
import com.zhiyangyun.care.fire.mapper.FireVideoAccessRequestMapper;
import com.zhiyangyun.care.fire.service.FireRules;
import com.zhiyangyun.care.fire.service.FireVideoLiveGateway;
import com.zhiyangyun.care.logistics.entity.LogisticsCamera;
import com.zhiyangyun.care.logistics.entity.LogisticsVideoRecording;
import com.zhiyangyun.care.logistics.mapper.LogisticsCameraMapper;
import com.zhiyangyun.care.logistics.mapper.LogisticsVideoRecordingMapper;
import com.zhiyangyun.care.logistics.service.VideoStorage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 录像调阅：申请（写明事由和时间段）→ 主管审批（不能批自己的）→ 申请人在有效期内查看；每一步写审计日志。
 * 实时画面通过 FireVideoLiveGateway 接入，未接入时返回占位说明。
 */
@RestController
@RequestMapping("/api/fire/video")
@PreAuthorize("hasAnyRole('LOGISTICS_EMPLOYEE','LOGISTICS_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
public class FireVideoAccessController {
  private final FireVideoAccessRequestMapper requests;
  private final FireVideoAccessLogMapper logs;
  private final LogisticsCameraMapper cameras;
  private final LogisticsVideoRecordingMapper recordings;
  private final VideoStorage storage;
  private final StaffMapper staffMapper;
  private final FireVideoLiveGateway liveGateway;

  public FireVideoAccessController(FireVideoAccessRequestMapper requests, FireVideoAccessLogMapper logs,
      LogisticsCameraMapper cameras, LogisticsVideoRecordingMapper recordings, VideoStorage storage,
      StaffMapper staffMapper, FireVideoLiveGateway liveGateway) {
    this.requests = requests;
    this.logs = logs;
    this.cameras = cameras;
    this.recordings = recordings;
    this.storage = storage;
    this.staffMapper = staffMapper;
    this.liveGateway = liveGateway;
  }

  @GetMapping("/live/{cameraId}")
  public Result<FireVideoLiveGateway.LiveStream> live(@PathVariable Long cameraId) {
    return Result.ok(liveGateway.open(camera(cameraId)));
  }

  /** 员工只看自己的申请；主管看全部（用于审批） */
  @GetMapping("/access-requests")
  public Result<IPage<FireVideoAccessRequest>> page(@RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize, @RequestParam(required = false) String status) {
    boolean manager = AuthContext.isMinisterOrHigher();
    return Result.ok(requests.selectPage(new Page<>(pageNo, pageSize), Wrappers.lambdaQuery(FireVideoAccessRequest.class)
        .eq(FireVideoAccessRequest::getOrgId, org()).eq(FireVideoAccessRequest::getIsDeleted, 0)
        .eq(!manager, FireVideoAccessRequest::getApplicantId, AuthContext.getStaffId())
        .eq(status != null && !status.isBlank(), FireVideoAccessRequest::getStatus, status)
        .orderByDesc(FireVideoAccessRequest::getCreateTime)));
  }

  public record AccessRequest(Long cameraId, LocalDateTime rangeStart, LocalDateTime rangeEnd, String reason) {}

  @PostMapping("/access-requests")
  public Result<FireVideoAccessRequest> apply(@RequestBody AccessRequest body) {
    LocalDateTime now = LocalDateTime.now();
    FireRules.requireValidVideoRequest(body.rangeStart(), body.rangeEnd(), body.reason(), now);
    LogisticsCamera camera = camera(body.cameraId());
    FireVideoAccessRequest request = new FireVideoAccessRequest();
    request.setOrgId(org());
    request.setCameraId(camera.getId());
    request.setCameraName(camera.getCameraName());
    request.setRangeStart(body.rangeStart());
    request.setRangeEnd(body.rangeEnd());
    request.setReason(body.reason().trim());
    request.setApplicantId(AuthContext.getStaffId());
    request.setApplicantName(currentName());
    request.setStatus("PENDING");
    request.setCreateTime(now);
    request.setUpdateTime(now);
    request.setIsDeleted(0);
    requests.insert(request);
    log(request, null, "REQUEST");
    return Result.ok(request);
  }

  public record Decision(boolean approve, String note) {}

  @PostMapping("/access-requests/{id}/decide")
  @PreAuthorize("hasAnyRole('LOGISTICS_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  @Transactional
  public Result<FireVideoAccessRequest> decide(@PathVariable Long id, @RequestBody Decision body) {
    FireVideoAccessRequest request = owned(id);
    FireRules.decideVideoRequest(request, AuthContext.getStaffId(), body.approve(), body.note(), LocalDateTime.now());
    request.setApproverName(currentName());
    request.setUpdateTime(LocalDateTime.now());
    requests.updateById(request);
    log(request, null, body.approve() ? "APPROVE" : "REJECT");
    return Result.ok(request);
  }

  /** 批准范围内的录像片段 */
  @GetMapping("/access-requests/{id}/recordings")
  public Result<List<LogisticsVideoRecording>> recordings(@PathVariable Long id) {
    FireVideoAccessRequest request = owned(id);
    FireRules.requireCanView(request, AuthContext.getStaffId(), LocalDateTime.now());
    log(request, null, "LIST");
    return Result.ok(recordings.selectList(Wrappers.lambdaQuery(LogisticsVideoRecording.class)
        .eq(LogisticsVideoRecording::getOrgId, org()).eq(LogisticsVideoRecording::getCameraId, request.getCameraId())
        .eq(LogisticsVideoRecording::getStatus, "AVAILABLE")
        .lt(LogisticsVideoRecording::getStartedAt, request.getRangeEnd()).gt(LogisticsVideoRecording::getEndedAt, request.getRangeStart())
        .orderByAsc(LogisticsVideoRecording::getStartedAt)));
  }

  @GetMapping("/access-requests/{id}/recordings/{recordingId}/playback")
  public Result<Map<String, String>> playback(@PathVariable Long id, @PathVariable Long recordingId) {
    LogisticsVideoRecording recording = viewable(id, recordingId);
    String url = "OSS".equals(recording.getProvider()) ? storage.signedUrl(recording)
        : "/api/fire/video/access-requests/" + id + "/recordings/" + recordingId + "/content";
    return Result.ok(Map.of("url", url, "provider", recording.getProvider()));
  }

  @GetMapping("/access-requests/{id}/recordings/{recordingId}/content")
  public ResponseEntity<Resource> content(@PathVariable Long id, @PathVariable Long recordingId) {
    LogisticsVideoRecording recording = viewable(id, recordingId);
    if (!"LOCAL".equals(recording.getProvider())) throw new IllegalArgumentException("请通过私有链接播放云端录像");
    return ResponseEntity.ok().cacheControl(CacheControl.noStore())
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=recording.mp4")
        .contentType(MediaType.parseMediaType("video/mp4")).body(storage.localResource(recording));
  }

  @GetMapping("/access-requests/{id}/logs")
  @PreAuthorize("hasAnyRole('LOGISTICS_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
  public Result<List<FireVideoAccessLog>> auditLogs(@PathVariable Long id) {
    owned(id);
    return Result.ok(logs.selectList(Wrappers.lambdaQuery(FireVideoAccessLog.class)
        .eq(FireVideoAccessLog::getOrgId, org()).eq(FireVideoAccessLog::getRequestId, id)
        .orderByAsc(FireVideoAccessLog::getCreateTime)));
  }

  private LogisticsVideoRecording viewable(Long id, Long recordingId) {
    FireVideoAccessRequest request = owned(id);
    FireRules.requireCanView(request, AuthContext.getStaffId(), LocalDateTime.now());
    LogisticsVideoRecording recording = recordings.selectById(recordingId);
    if (recording == null || !Objects.equals(recording.getOrgId(), org()) || !"AVAILABLE".equals(recording.getStatus())
        || !FireRules.recordingCovered(request, recording.getCameraId(), recording.getStartedAt(), recording.getEndedAt())) {
      throw new IllegalArgumentException("该录像不在批准的调阅范围内");
    }
    log(request, recordingId, "VIEW");
    return recording;
  }

  private void log(FireVideoAccessRequest request, Long recordingId, String action) {
    FireVideoAccessLog entry = new FireVideoAccessLog();
    entry.setOrgId(request.getOrgId());
    entry.setRequestId(request.getId());
    entry.setRecordingId(recordingId);
    entry.setAction(action);
    entry.setStaffId(AuthContext.getStaffId());
    entry.setStaffName(currentName());
    entry.setCreateTime(LocalDateTime.now());
    logs.insert(entry);
  }

  private FireVideoAccessRequest owned(Long id) {
    FireVideoAccessRequest request = requests.selectById(id);
    if (request == null || Integer.valueOf(1).equals(request.getIsDeleted()) || !Objects.equals(request.getOrgId(), org())) {
      throw new ResourceNotFoundException("调阅申请不存在或无权访问");
    }
    return request;
  }

  private LogisticsCamera camera(Long cameraId) {
    LogisticsCamera camera = cameraId == null ? null : cameras.selectById(cameraId);
    if (camera == null || !Objects.equals(camera.getOrgId(), org())) throw new ResourceNotFoundException("摄像头不存在或无权访问");
    return camera;
  }

  private Long org() {
    Long orgId = AuthContext.getOrgId();
    if (orgId == null) throw new IllegalArgumentException("当前账号未绑定机构");
    return orgId;
  }

  private String currentName() {
    StaffAccount staff = AuthContext.getStaffId() == null ? null : staffMapper.selectById(AuthContext.getStaffId());
    return staff == null ? AuthContext.getUsername() : staff.getRealName();
  }
}
