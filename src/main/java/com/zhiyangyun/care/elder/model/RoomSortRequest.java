package com.zhiyangyun.care.elder.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class RoomSortRequest {
  @NotNull
  private Long floorId;

  @NotEmpty
  private List<Long> roomIds;
}
