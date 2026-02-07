package com.zhiyangyun.care.auth.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import java.time.LocalDateTime;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {
  @Override
  public void insertFill(MetaObject metaObject) {
    strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
    strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    strictInsertFill(metaObject, "isDeleted", Integer.class, 0);
  }

  @Override
  public void updateFill(MetaObject metaObject) {
    strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
  }
}
