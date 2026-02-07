package com.zhiyangyun.care.elder.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.auth.security.AuthContext;
import com.zhiyangyun.care.elder.model.RoomRequest;
import com.zhiyangyun.care.elder.model.RoomResponse;
import com.zhiyangyun.care.elder.service.RoomService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/room")
public class RoomController {
  private final RoomService roomService;

  public RoomController(RoomService roomService) {
    this.roomService = roomService;
  }

  @PostMapping
  public Result<RoomResponse> create(@Valid @RequestBody RoomRequest request) {
    request.setOrgId(AuthContext.getOrgId());
    return Result.ok(roomService.create(request));
  }

  @PutMapping("/{id}")
  public Result<RoomResponse> update(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {
    request.setOrgId(AuthContext.getOrgId());
    return Result.ok(roomService.update(id, request));
  }

  @GetMapping("/{id}")
  public Result<RoomResponse> get(@PathVariable Long id) {
    return Result.ok(roomService.get(id));
  }

  @GetMapping
  public Result<IPage<RoomResponse>> page(
      @RequestParam(defaultValue = "1") long pageNo,
      @RequestParam(defaultValue = "20") long pageSize,
      @RequestParam(required = false) String keyword) {
    return Result.ok(roomService.page(AuthContext.getOrgId(), pageNo, pageSize, keyword));
  }

  @GetMapping("/list")
  public Result<java.util.List<RoomResponse>> list() {
    return Result.ok(roomService.list(AuthContext.getOrgId()));
  }

  @DeleteMapping("/{id}")
  public Result<Void> delete(@PathVariable Long id) {
    roomService.delete(id);
    return Result.ok(null);
  }
}
