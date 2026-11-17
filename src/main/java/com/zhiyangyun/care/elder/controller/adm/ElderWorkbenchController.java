package com.zhiyangyun.care.elder.controller.adm;

import com.zhiyangyun.care.auth.model.Result;
import com.zhiyangyun.care.elder.model.lifecycle.ElderWorkbenchTodoResponse;
import com.zhiyangyun.care.elder.service.ElderWorkbenchTodoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 长者工作台：待办汇总。各来源再按查看权限过滤，口径见 {@link ElderWorkbenchTodoService}。 */
@RestController
@RequestMapping("/api/elder/workbench")
public class ElderWorkbenchController {
  private final ElderWorkbenchTodoService todoService;

  public ElderWorkbenchController(ElderWorkbenchTodoService todoService) {
    this.todoService = todoService;
  }

  @PreAuthorize("@elderAuthz.canReadResidence()")
  @GetMapping("/todos")
  public Result<ElderWorkbenchTodoResponse> todos(@RequestParam(defaultValue = "mine") String scope) {
    return Result.ok(todoService.list(scope));
  }
}
