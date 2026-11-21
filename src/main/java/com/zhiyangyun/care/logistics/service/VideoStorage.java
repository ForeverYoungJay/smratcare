package com.zhiyangyun.care.logistics.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.zhiyangyun.care.logistics.entity.LogisticsVideoRecording;
import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/** 私有录像空间独立于公开的 /uploads 文件服务。 */
@Component
@ConfigurationProperties(prefix = "app.logistics.video")
@Data
public class VideoStorage {
  private String localRoot = System.getProperty("user.home") + "/smartcare-video-private";
  private long maxSegmentBytes = 10 * 1024 * 1024;
  private String ossEndpoint;
  private String ossBucket;
  private String ossAccessKeyId;
  private String ossAccessKeySecret;

  public boolean configured(String provider) {
    return switch (provider) {
      case "LOCAL" -> localRoot != null && !localRoot.isBlank();
      case "OSS" -> present(ossEndpoint) && present(ossBucket) && present(ossAccessKeyId) && present(ossAccessKeySecret);
      default -> false;
    };
  }

  public record Stored(String key, long size, String sha256) {}

  public Stored put(Long orgId, Long cameraId, String provider, MultipartFile file) {
    if (!configured(provider)) throw new IllegalArgumentException("录像存储尚未配置，请联系管理员");
    if (file == null || file.isEmpty() || file.getSize() > maxSegmentBytes) {
      throw new IllegalArgumentException("录像分片为空或超出服务端分片大小上限");
    }
    String key = orgId + "/" + cameraId + "/" + UUID.randomUUID() + ".mp4";
    Path temp = null;
    try {
      temp = Files.createTempFile("smartcare-video-", ".mp4");
      file.transferTo(temp);
      // 不信任扩展名和 Content-Type；网关必须提交可独立播放的 MP4。
      try (InputStream input = Files.newInputStream(temp)) {
        byte[] header = input.readNBytes(12);
        if (header.length < 12 || header[4] != 'f' || header[5] != 't' || header[6] != 'y' || header[7] != 'p') {
          throw new IllegalArgumentException("仅接收具有 ftyp 文件头的 MP4 录像分片");
        }
      }
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      try (InputStream input = Files.newInputStream(temp)) {
        byte[] buffer = new byte[8192];
        int count;
        while ((count = input.read(buffer)) != -1) digest.update(buffer, 0, count);
      }
      long size = Files.size(temp);
      if ("LOCAL".equals(provider)) {
        Path target = localPath(key);
        Files.createDirectories(target.getParent());
        Files.copy(temp, target);
      } else {
        OSS client = oss();
        try {
          if (client.getBucketAcl(ossBucket).getCannedACL() != com.aliyun.oss.model.CannedAccessControlList.Private) {
            throw new IllegalArgumentException("录像 Bucket 必须设置为私有权限");
          }
          ObjectMetadata metadata = new ObjectMetadata();
          metadata.setContentType("video/mp4");
          metadata.setContentLength(size);
          metadata.setObjectAcl(com.aliyun.oss.model.CannedAccessControlList.Private);
          try (InputStream input = Files.newInputStream(temp)) {
            client.putObject(ossBucket, "video/" + key, input, metadata);
          }
        } finally { client.shutdown(); }
      }
      return new Stored(key, size, HexFormat.of().formatHex(digest.digest()));
    } catch (IllegalArgumentException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new IllegalStateException("录像写入失败，请检查私有存储配置和可用空间", ex);
    } finally {
      if (temp != null) try { Files.deleteIfExists(temp); } catch (IOException ignored) { /* 临时目录由运维清理 */ }
    }
  }

  public FileSystemResource localResource(LogisticsVideoRecording recording) {
    Path path = localPath(recording.getObjectKey());
    if (!Files.isRegularFile(path)) throw new IllegalArgumentException("录像文件不存在，请检查存储设备");
    return new FileSystemResource(path);
  }

  public String signedUrl(LogisticsVideoRecording recording) {
    OSS client = oss();
    try {
      return client.generatePresignedUrl(ossBucket, "video/" + recording.getObjectKey(),
          new java.util.Date(System.currentTimeMillis() + 300_000)).toString();
    } finally { client.shutdown(); }
  }

  public void delete(String provider, String key) {
    try {
      if ("LOCAL".equals(provider)) Files.deleteIfExists(localPath(key));
      else if ("OSS".equals(provider)) {
        OSS client = oss();
        try { client.deleteObject(ossBucket, "video/" + key); } finally { client.shutdown(); }
      } else throw new IllegalArgumentException("未知录像存储类型");
    } catch (IOException ex) { throw new IllegalStateException("录像清理失败", ex); }
  }

  private Path localPath(String key) {
    Path root = Path.of(localRoot).toAbsolutePath().normalize();
    Path target = root.resolve(key).normalize();
    if (!target.startsWith(root)) throw new IllegalArgumentException("录像路径非法");
    return target;
  }

  private OSS oss() {
    if (!configured("OSS")) throw new IllegalArgumentException("未配置录像专用 OSS");
    return new OSSClientBuilder().build(ossEndpoint, ossAccessKeyId, ossAccessKeySecret);
  }

  private boolean present(String value) { return value != null && !value.isBlank(); }
}
