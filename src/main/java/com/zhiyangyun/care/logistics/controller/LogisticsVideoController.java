package com.zhiyangyun.care.logistics.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.logistics.entity.*;
import com.zhiyangyun.care.logistics.model.*;
import com.zhiyangyun.care.logistics.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Validated
@RequestMapping("/api/logistics/video")
@PreAuthorize("hasAnyRole('LOGISTICS_EMPLOYEE','LOGISTICS_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')")
public class LogisticsVideoController {
  private static final String MANAGE = "hasAnyRole('LOGISTICS_MINISTER','DIRECTOR','SYS_ADMIN','ADMIN')";
  private final LogisticsVideoService service;
  private final VideoStorage storage;

  public LogisticsVideoController(LogisticsVideoService service, VideoStorage storage) {
    this.service = service;
    this.storage = storage;
  }

  @GetMapping("/overview")
  public Result<Map<String, Object>> overview() { return Result.ok(service.overview()); }

  @GetMapping("/cameras")
  public Result<IPage<LogisticsCamera>> cameras(
      @RequestParam(defaultValue = "1") @Min(1) long pageNo,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) long pageSize,
      @RequestParam(required = false) @Size(max = 128) String keyword) {
    return Result.ok(service.cameraPage(pageNo, pageSize, keyword));
  }

  @PostMapping("/cameras") @PreAuthorize(MANAGE)
  public Result<LogisticsCamera> create(@Valid @RequestBody VideoCameraRequest request) {
    return Result.ok(service.saveCamera(null, request));
  }

  @PutMapping("/cameras/{id}") @PreAuthorize(MANAGE)
  public Result<LogisticsCamera> update(@PathVariable Long id, @Valid @RequestBody VideoCameraRequest request) {
    return Result.ok(service.saveCamera(id, request));
  }

  @GetMapping("/policy")
  public Result<LogisticsVideoPolicy> policy() { return Result.ok(service.policy()); }

  @PutMapping("/policy") @PreAuthorize(MANAGE)
  public Result<LogisticsVideoPolicy> savePolicy(@Valid @RequestBody VideoPolicyRequest request) {
    return Result.ok(service.savePolicy(request));
  }

  @GetMapping("/recordings") @PreAuthorize(MANAGE)
  public Result<IPage<LogisticsVideoRecording>> recordings(
      @RequestParam(defaultValue = "1") @Min(1) long pageNo,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) long pageSize,
      @RequestParam(required = false) Long cameraId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
      @RequestParam(required = false) Boolean held) {
    return Result.ok(service.recordingPage(pageNo, pageSize, cameraId, from, to, held));
  }

  @PostMapping(value = "/recordings", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) @PreAuthorize(MANAGE)
  public Result<LogisticsVideoRecording> ingest(
      @RequestParam Long cameraId,
      @RequestParam @Pattern(regexp = "[A-Za-z0-9_-]{1,64}") String ingestKey,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startedAt,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endedAt,
      @RequestPart MultipartFile file) {
    return Result.ok(service.ingest(cameraId, ingestKey, startedAt, endedAt, file));
  }

  @PutMapping("/recordings/{id}/hold") @PreAuthorize(MANAGE)
  public Result<LogisticsVideoRecording> hold(@PathVariable Long id, @Valid @RequestBody VideoHoldRequest request) {
    return Result.ok(service.hold(id, request));
  }

  @GetMapping("/recordings/{id}/playback") @PreAuthorize(MANAGE)
  public Result<Map<String, String>> playback(@PathVariable Long id) {
    LogisticsVideoRecording recording = service.playback(id);
    String url = "OSS".equals(recording.getProvider()) ? storage.signedUrl(recording)
        : "/api/logistics/video/recordings/" + id + "/content";
    return Result.ok(Map.of("url", url, "provider", recording.getProvider()));
  }

  @GetMapping("/recordings/{id}/content") @PreAuthorize(MANAGE)
  public ResponseEntity<Resource> content(@PathVariable Long id) {
    LogisticsVideoRecording recording = service.playback(id);
    if (!"LOCAL".equals(recording.getProvider())) throw new IllegalArgumentException("请通过私有链接播放云端录像");
    return ResponseEntity.ok().cacheControl(CacheControl.noStore())
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=recording.mp4")
        .contentType(MediaType.parseMediaType("video/mp4")).body(storage.localResource(recording));
  }
}
