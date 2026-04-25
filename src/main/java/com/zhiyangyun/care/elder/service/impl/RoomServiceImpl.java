package com.zhiyangyun.care.elder.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhiyangyun.care.asset.entity.Building;
import com.zhiyangyun.care.asset.entity.Floor;
import com.zhiyangyun.care.asset.mapper.BuildingMapper;
import com.zhiyangyun.care.asset.mapper.FloorMapper;
import com.zhiyangyun.care.baseconfig.entity.BaseDataItem;
import com.zhiyangyun.care.baseconfig.mapper.BaseDataItemMapper;
import com.zhiyangyun.care.elder.entity.Room;
import com.zhiyangyun.care.elder.entity.Bed;
import com.zhiyangyun.care.elder.mapper.BedMapper;
import com.zhiyangyun.care.elder.mapper.RoomMapper;
import com.zhiyangyun.care.elder.model.RoomRequest;
import com.zhiyangyun.care.elder.model.RoomResponse;
import com.zhiyangyun.care.elder.model.RoomSortRequest;
import com.zhiyangyun.care.elder.service.RoomService;
import com.zhiyangyun.care.elder.util.QrCodeUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomServiceImpl implements RoomService {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final RoomMapper roomMapper;
  private final BedMapper bedMapper;
  private final BuildingMapper buildingMapper;
  private final FloorMapper floorMapper;
  private final BaseDataItemMapper baseDataItemMapper;

  public RoomServiceImpl(
      RoomMapper roomMapper,
      BedMapper bedMapper,
      BuildingMapper buildingMapper,
      FloorMapper floorMapper,
      BaseDataItemMapper baseDataItemMapper) {
    this.roomMapper = roomMapper;
    this.bedMapper = bedMapper;
    this.buildingMapper = buildingMapper;
    this.floorMapper = floorMapper;
    this.baseDataItemMapper = baseDataItemMapper;
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public RoomResponse create(RoomRequest request) {
    applyBuildingFloorPlaceholders(request);
    if (request.getRoomNo() == null || request.getRoomNo().isBlank()) {
      request.setRoomNo(resolveNextRoomNo(request.getTenantId(), request.getBuildingId(), request.getFloorId(), request.getFloorNo(), request.getRoomType()));
    }
    ensureRoomNoUnique(null, request.getTenantId(), request.getRoomNo());
    Integer normalizedCapacity = normalizeCapacityByRoomType(request.getTenantId(), request.getRoomType(), request.getCapacity());
    String normalizedRoomType = normalizeRoomTypeByCapacity(request.getRoomType(), normalizedCapacity);
    Room room = new Room();
    room.setTenantId(request.getTenantId());
    room.setOrgId(request.getOrgId());
    room.setBuildingId(request.getBuildingId());
    room.setFloorId(request.getFloorId());
    room.setBuilding(request.getBuilding());
    room.setFloorNo(request.getFloorNo());
    room.setRoomNo(request.getRoomNo());
    room.setRoomType(normalizedRoomType);
    room.setSortNo(normalizeSortNo(request.getSortNo(), request.getTenantId(), request.getFloorId()));
    room.setCapacity(normalizedCapacity);
    room.setStatus(request.getStatus());
    room.setRoomQrCode(request.getRoomQrCode());
    room.setRemark(request.getRemark());
    room.setCreatedBy(request.getCreatedBy());
    applyBuildingFloor(room, request.getTenantId(), request.getBuildingId(), request.getFloorId(),
        request.getBuilding(), request.getFloorNo());
    if (room.getRoomQrCode() == null || room.getRoomQrCode().isBlank()) {
      room.setRoomQrCode(QrCodeUtil.generate());
    }
    roomMapper.insert(room);
    createBedsForRoom(room, request);
    return toResponse(room);
  }

  @Override
  public RoomResponse update(Long id, RoomRequest request) {
    Room room = roomMapper.selectById(id);
    if (room == null) {
      return null;
    }
    applyBuildingFloorPlaceholders(request);
    if (request.getRoomNo() == null || request.getRoomNo().isBlank()) {
      request.setRoomNo(room.getRoomNo());
    }
    ensureRoomNoUnique(id, request.getTenantId(), request.getRoomNo());
    Integer normalizedCapacity = normalizeCapacityByRoomType(request.getTenantId(), request.getRoomType(), request.getCapacity());
    String normalizedRoomType = normalizeRoomTypeByCapacity(request.getRoomType(), normalizedCapacity);
    ensureCapacityNotLessThanOccupied(id, request.getTenantId(), normalizedCapacity);
    room.setTenantId(request.getTenantId());
    room.setOrgId(request.getOrgId());
    room.setBuildingId(request.getBuildingId());
    room.setFloorId(request.getFloorId());
    room.setBuilding(request.getBuilding());
    room.setFloorNo(request.getFloorNo());
    room.setRoomNo(request.getRoomNo());
    room.setRoomType(normalizedRoomType);
    room.setSortNo(normalizeSortNo(request.getSortNo(), room.getTenantId(), request.getFloorId(), room.getSortNo()));
    room.setCapacity(normalizedCapacity);
    room.setStatus(request.getStatus());
    room.setRoomQrCode(request.getRoomQrCode());
    room.setRemark(request.getRemark());
    applyBuildingFloor(room, request.getTenantId(), request.getBuildingId(), request.getFloorId(),
        request.getBuilding(), request.getFloorNo());
    roomMapper.updateById(room);
    return toResponse(room);
  }

  @Override
  public RoomResponse get(Long id, Long tenantId) {
    Room room = roomMapper.selectById(id);
    if (room == null || (tenantId != null && !tenantId.equals(room.getTenantId()))) {
      return null;
    }
    return room == null ? null : toResponse(room);
  }

  @Override
  public IPage<RoomResponse> page(Long tenantId, long pageNo, long pageSize,
      String keyword, String roomNo, String building, String floorNo, Long buildingId, Long floorId, String roomType, Integer status) {
    var wrapper = Wrappers.lambdaQuery(Room.class)
        .eq(Room::getIsDeleted, 0)
        .eq(tenantId != null, Room::getTenantId, tenantId);
    if (status != null) {
      wrapper.eq(Room::getStatus, status);
    }
    if (roomNo != null && !roomNo.isBlank()) {
      wrapper.like(Room::getRoomNo, roomNo);
    }
    if (building != null && !building.isBlank()) {
      wrapper.like(Room::getBuilding, building);
    }
    if (floorNo != null && !floorNo.isBlank()) {
      wrapper.like(Room::getFloorNo, floorNo);
    }
    if (buildingId != null) {
      wrapper.eq(Room::getBuildingId, buildingId);
    }
    if (floorId != null) {
      wrapper.eq(Room::getFloorId, floorId);
    }
    if (roomType != null && !roomType.isBlank()) {
      wrapper.eq(Room::getRoomType, roomType);
    }
    if (keyword != null && !keyword.isBlank()) {
      wrapper.and(w -> w.like(Room::getRoomNo, keyword)
          .or().like(Room::getBuilding, keyword)
          .or().like(Room::getFloorNo, keyword));
    }
    IPage<Room> page = roomMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    return page.convert(this::toResponse);
  }

  @Override
  public java.util.List<RoomResponse> list(Long tenantId) {
    var wrapper = Wrappers.lambdaQuery(Room.class)
        .eq(Room::getIsDeleted, 0)
        .eq(tenantId != null, Room::getTenantId, tenantId);
    return roomMapper.selectList(wrapper).stream()
        .sorted(this::compareRoomOrder)
        .map(this::toResponse)
        .toList();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void delete(Long id, Long tenantId) {
    Room room = roomMapper.selectById(id);
    if (room == null || Integer.valueOf(1).equals(room.getIsDeleted())) {
      throw new IllegalArgumentException("房间不存在或已删除");
    }
    if (tenantId == null || !tenantId.equals(room.getTenantId())) {
      throw new IllegalArgumentException("无权限删除该房间");
    }
    List<Bed> beds = bedMapper.selectList(Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(Bed::getTenantId, room.getTenantId())
        .eq(Bed::getRoomId, room.getId()));
    boolean hasOccupiedBed = beds.stream().anyMatch(bed ->
        bed.getElderId() != null || Integer.valueOf(2).equals(bed.getStatus()));
    if (hasOccupiedBed) {
      throw new IllegalArgumentException("当前房间下存在已入住床位，请先办理退床或换床");
    }
    for (Bed bed : beds) {
      bed.setIsDeleted(1);
      bedMapper.updateById(bed);
    }
    room.setRoomNo(buildDeletedRoomNo(room));
    room.setIsDeleted(1);
    roomMapper.updateById(room);
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void sort(Long tenantId, RoomSortRequest request) {
    if (tenantId == null || request == null || request.getFloorId() == null) {
      throw new IllegalArgumentException("缺少楼层信息");
    }
    List<Long> requestedRoomIds = request.getRoomIds() == null
        ? List.of()
        : request.getRoomIds().stream().filter(Objects::nonNull).distinct().toList();
    if (requestedRoomIds.isEmpty()) {
      throw new IllegalArgumentException("缺少房间排序数据");
    }
    List<Room> floorRooms = roomMapper.selectList(Wrappers.lambdaQuery(Room.class)
        .eq(Room::getIsDeleted, 0)
        .eq(Room::getTenantId, tenantId)
        .eq(Room::getFloorId, request.getFloorId()));
    if (floorRooms.isEmpty()) {
      throw new IllegalArgumentException("当前楼层暂无房间");
    }
    Map<Long, Room> roomMap = floorRooms.stream().collect(Collectors.toMap(Room::getId, Function.identity()));
    List<Long> missingRoomIds = requestedRoomIds.stream().filter(id -> !roomMap.containsKey(id)).toList();
    if (!missingRoomIds.isEmpty()) {
      throw new IllegalArgumentException("存在无效房间，无法保存排序");
    }
    Set<Long> appendedIds = new HashSet<>(requestedRoomIds);
    List<Room> orderedRooms = new ArrayList<>();
    requestedRoomIds.forEach(id -> orderedRooms.add(roomMap.get(id)));
    floorRooms.stream()
        .filter(room -> !appendedIds.contains(room.getId()))
        .sorted(this::compareRoomOrder)
        .forEach(orderedRooms::add);
    int nextSortNo = 10;
    for (Room room : orderedRooms) {
      room.setSortNo(nextSortNo);
      roomMapper.updateById(room);
      nextSortNo += 10;
    }
  }

  private RoomResponse toResponse(Room room) {
    RoomResponse response = new RoomResponse();
    response.setId(room.getId());
    response.setTenantId(room.getTenantId());
    response.setOrgId(room.getOrgId());
    response.setBuildingId(room.getBuildingId());
    response.setFloorId(room.getFloorId());
    response.setBuilding(room.getBuilding());
    response.setFloorNo(room.getFloorNo());
    response.setRoomNo(room.getRoomNo());
    response.setRoomType(room.getRoomType());
    response.setSortNo(room.getSortNo());
    response.setCapacity(room.getCapacity());
    response.setStatus(room.getStatus());
    response.setRoomQrCode(room.getRoomQrCode());
    response.setRemark(room.getRemark());
    return response;
  }

  private void ensureRoomNoUnique(Long id, Long tenantId, String roomNo) {
    if (tenantId == null || roomNo == null || roomNo.isBlank()) {
      return;
    }
    var wrapper = Wrappers.lambdaQuery(Room.class)
        .eq(Room::getIsDeleted, 0)
        .eq(Room::getTenantId, tenantId)
        .eq(Room::getRoomNo, roomNo);
    if (id != null) {
      wrapper.ne(Room::getId, id);
    }
    if (roomMapper.selectCount(wrapper) > 0) {
      throw new IllegalArgumentException("房间号已存在");
    }
  }

  private void ensureCapacityNotLessThanOccupied(Long roomId, Long tenantId, Integer capacity) {
    if (roomId == null || tenantId == null || capacity == null) {
      return;
    }
    long occupiedCount = bedMapper.selectCount(Wrappers.lambdaQuery(Bed.class)
        .eq(Bed::getIsDeleted, 0)
        .eq(Bed::getTenantId, tenantId)
        .eq(Bed::getRoomId, roomId)
        .isNotNull(Bed::getElderId));
    if (capacity < occupiedCount) {
      throw new IllegalArgumentException("房间容量不能小于当前入住床位数");
    }
  }

  private Integer normalizeCapacityByRoomType(Long tenantId, String roomType, Integer fallbackCapacity) {
    Integer inferred = inferCapacityByRoomType(tenantId, roomType);
    if (inferred != null) {
      return inferred;
    }
    return fallbackCapacity == null ? 1 : fallbackCapacity;
  }

  private Integer inferCapacityByRoomType(Long tenantId, String roomType) {
    if (roomType == null || roomType.isBlank()) {
      return null;
    }
    RoomTypeMeta roomTypeMeta = resolveRoomTypeMeta(tenantId, roomType);
    if (roomTypeMeta.defaultCapacity() != null) {
      return roomTypeMeta.defaultCapacity();
    }
    if (roomTypeMeta.functional()) {
      return 0;
    }
    String normalized = roomType.trim().toUpperCase();
    if ("1".equals(normalized)
        || normalized.contains("SINGLE")
        || normalized.contains("ROOM_SINGLE")
        || roomType.contains("单人")) {
      return 1;
    }
    if ("2".equals(normalized)
        || normalized.contains("DOUBLE")
        || normalized.contains("ROOM_DOUBLE")
        || roomType.contains("双人")) {
      return 2;
    }
    if ("3".equals(normalized)
        || normalized.contains("TRIPLE")
        || normalized.contains("ROOM_TRIPLE")
        || roomType.contains("三人")) {
      return 3;
    }
    if (normalized.contains("NURSING")
        || normalized.contains("STATION")
        || normalized.contains("WATER")
        || normalized.contains("LAUNDRY")
        || normalized.contains("TOILET")
        || normalized.contains("WC")
        || roomType.contains("护理站")
        || roomType.contains("开水房")
        || roomType.contains("洗衣房")
        || roomType.contains("卫生间")
        || roomType.contains("厕所")
        || roomType.contains("浴室")
        || roomType.contains("治疗室")
        || roomType.contains("库房")
        || roomType.contains("活动室")
        || roomType.contains("餐厅")
        || normalized.contains("BATH")
        || normalized.contains("TREATMENT")
        || normalized.contains("STORAGE")
        || normalized.contains("ACTIVITY")
        || normalized.contains("DINING")) {
      return 0;
    }
    return null;
  }

  private RoomTypeMeta resolveRoomTypeMeta(Long tenantId, String roomType) {
    if (tenantId == null || roomType == null || roomType.isBlank()) {
      return RoomTypeMeta.EMPTY;
    }
    String raw = roomType.trim();
    String normalized = raw.toUpperCase();
    BaseDataItem item = baseDataItemMapper.selectOne(Wrappers.lambdaQuery(BaseDataItem.class)
        .eq(BaseDataItem::getIsDeleted, 0)
        .eq(BaseDataItem::getTenantId, tenantId)
        .eq(BaseDataItem::getConfigGroup, "ADMISSION_ROOM_TYPE")
        .and(wrapper -> wrapper.eq(BaseDataItem::getItemCode, raw)
            .or()
            .eq(BaseDataItem::getItemCode, normalized)
            .or()
            .eq(BaseDataItem::getItemName, raw))
        .last("LIMIT 1"));
    if (item == null || item.getRemark() == null || item.getRemark().isBlank()) {
      return RoomTypeMeta.EMPTY;
    }
    try {
      JsonNode node = OBJECT_MAPPER.readTree(item.getRemark());
      Integer defaultCapacity = node.hasNonNull("defaultCapacity") ? node.get("defaultCapacity").asInt() : null;
      String roomKind = node.path("roomKind").asText("");
      boolean functional = "FUNCTIONAL".equalsIgnoreCase(roomKind) || Integer.valueOf(0).equals(defaultCapacity);
      return new RoomTypeMeta(defaultCapacity, functional);
    } catch (Exception ex) {
      return RoomTypeMeta.EMPTY;
    }
  }

  private String normalizeRoomTypeByCapacity(String roomType, Integer capacity) {
    if (roomType != null && !roomType.isBlank()) {
      return roomType;
    }
    if (capacity == null) {
      return roomType;
    }
    if (capacity == 1) {
      return "ROOM_SINGLE";
    }
    if (capacity == 2) {
      return "ROOM_DOUBLE";
    }
    if (capacity == 3) {
      return "ROOM_TRIPLE";
    }
    return roomType;
  }

  private void applyBuildingFloor(Room room, Long tenantId, Long buildingId, Long floorId,
                                  String fallbackBuilding, String fallbackFloorNo) {
    if (buildingId != null) {
      Building building = buildingMapper.selectById(buildingId);
      if (building != null && building.getTenantId() == null && tenantId != null) {
        if (building.getOrgId() == null || tenantId.equals(building.getOrgId())) {
          building.setTenantId(tenantId);
          buildingMapper.updateById(building);
        }
      }
      if (building == null
          || (tenantId != null && !tenantId.equals(building.getTenantId()))
          || Integer.valueOf(1).equals(building.getIsDeleted())) {
        throw new IllegalArgumentException("楼栋不存在或无权限");
      }
      room.setBuilding(building.getName());
    } else {
      room.setBuilding(fallbackBuilding);
    }
    if (floorId != null) {
      Floor floor = floorMapper.selectById(floorId);
      if (floor != null && floor.getTenantId() == null && tenantId != null) {
        if (floor.getOrgId() == null || tenantId.equals(floor.getOrgId())) {
          floor.setTenantId(tenantId);
          floorMapper.updateById(floor);
        }
      }
      if (floor == null
          || (tenantId != null && !tenantId.equals(floor.getTenantId()))
          || Integer.valueOf(1).equals(floor.getIsDeleted())) {
        throw new IllegalArgumentException("楼层不存在或无权限");
      }
      if (buildingId != null && floor.getBuildingId() != null && !Objects.equals(buildingId, floor.getBuildingId())) {
        throw new IllegalArgumentException("楼层不属于当前楼栋");
      }
      room.setFloorNo(floor.getFloorNo());
    } else {
      room.setFloorNo(fallbackFloorNo);
    }
  }

  private void applyBuildingFloorPlaceholders(RoomRequest request) {
    if (request == null) {
      return;
    }
    if (request.getFloorNo() == null || request.getFloorNo().isBlank()) {
      Floor floor = request.getFloorId() == null ? null : floorMapper.selectById(request.getFloorId());
      if (floor != null) {
        request.setFloorNo(floor.getFloorNo());
      }
    }
  }

  private String resolveNextRoomNo(Long tenantId, Long buildingId, Long floorId, String floorNo, String roomType) {
    String prefixDigits = floorNo == null ? "" : floorNo.replaceAll("[^0-9]", "");
    if (prefixDigits.isBlank()) {
      prefixDigits = "1";
    }
    final String buildingPrefix = resolveBuildingPrefix(buildingId);
    final String functionalCode = resolveFunctionalRoomCode(roomType);
    final String roomPrefix = buildingPrefix + prefixDigits + (functionalCode == null ? "" : functionalCode);
    List<Room> rooms = roomMapper.selectList(Wrappers.lambdaQuery(Room.class)
        .eq(Room::getIsDeleted, 0)
        .eq(Room::getTenantId, tenantId)
        .eq(Room::getFloorId, floorId));
    int nextSeq = rooms.stream()
        .map(Room::getRoomNo)
        .filter(Objects::nonNull)
        .map(String::trim)
        .map(String::toUpperCase)
        .filter(value -> value.startsWith(roomPrefix) && value.length() > roomPrefix.length())
        .map(value -> value.substring(roomPrefix.length()))
        .filter(value -> !value.isBlank() && value.chars().allMatch(Character::isDigit))
        .mapToInt(Integer::parseInt)
        .max()
        .orElse(0) + 1;
    return roomPrefix + (functionalCode == null ? String.format("%02d", nextSeq) : nextSeq);
  }

  private String resolveFunctionalRoomCode(String roomType) {
    if (roomType == null || roomType.isBlank()) {
      return null;
    }
    String normalized = roomType.trim().toUpperCase();
    if (normalized.contains("NURSING") || normalized.contains("STATION") || roomType.contains("护理站")) {
      return "N";
    }
    if (normalized.contains("WATER") || roomType.contains("开水房")) {
      return "W";
    }
    if (normalized.contains("LAUNDRY") || roomType.contains("洗衣房")) {
      return "L";
    }
    if (normalized.contains("TOILET") || normalized.contains("WC") || roomType.contains("卫生间") || roomType.contains("厕所")) {
      return "T";
    }
    if (normalized.contains("BATH") || roomType.contains("浴室") || roomType.contains("沐浴")) {
      return "B";
    }
    return null;
  }

  private String resolveBuildingPrefix(Long buildingId) {
    if (buildingId == null) {
      return "A";
    }
    Building building = buildingMapper.selectById(buildingId);
    if (building == null) {
      return "A";
    }
    String normalizedCode = normalizeBuildingCode(building.getCode());
    if (normalizedCode != null) {
      return normalizedCode;
    }
    String seed = firstNonBlank(building.getName());
    if (seed == null || seed.isBlank()) {
      return "A";
    }
    for (int i = 0; i < seed.length(); i++) {
      char current = seed.charAt(i);
      if (Character.isLetterOrDigit(current)) {
        return String.valueOf(Character.toUpperCase(current));
      }
    }
    return String.valueOf(Character.toUpperCase(seed.charAt(0)));
  }

  private String normalizeBuildingCode(String code) {
    if (code == null || code.isBlank()) {
      return null;
    }
    String normalized = code.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
    return normalized.isBlank() ? null : normalized;
  }

  private String buildDeletedRoomNo(Room room) {
    if (room == null || room.getId() == null) {
      return "DEL";
    }
    return "DEL" + room.getId();
  }

  private void createBedsForRoom(Room room, RoomRequest request) {
    int capacity = room == null || room.getCapacity() == null ? 0 : room.getCapacity();
    if (capacity <= 0 || room.getId() == null) {
      return;
    }
    for (int i = 1; i <= capacity; i++) {
      Bed bed = new Bed();
      bed.setTenantId(room.getTenantId());
      bed.setOrgId(room.getOrgId());
      bed.setRoomId(room.getId());
      bed.setBedNo(buildInitialBedNo(room.getRoomNo(), i));
      bed.setBedQrCode(QrCodeUtil.generate());
      bed.setStatus(1);
      bed.setCreatedBy(request.getCreatedBy());
      bedMapper.insert(bed);
    }
  }

  private String buildInitialBedNo(String roomNo, int index) {
    String normalizedRoomNo = firstNonBlank(roomNo, "ROOM");
    return normalizedRoomNo + "-" + indexToAlpha(index);
  }

  private String indexToAlpha(int index) {
    int number = Math.max(index, 1);
    StringBuilder builder = new StringBuilder();
    while (number > 0) {
      int mod = (number - 1) % 26;
      builder.insert(0, (char) ('A' + mod));
      number = (number - 1) / 26;
    }
    return builder.toString();
  }

  private Integer normalizeSortNo(Integer requestedSortNo, Long tenantId, Long floorId) {
    return normalizeSortNo(requestedSortNo, tenantId, floorId, null);
  }

  private Integer normalizeSortNo(Integer requestedSortNo, Long tenantId, Long floorId, Integer currentSortNo) {
    if (requestedSortNo != null && requestedSortNo > 0) {
      return requestedSortNo;
    }
    if (currentSortNo != null && currentSortNo > 0) {
      return currentSortNo;
    }
    return resolveNextSortNo(tenantId, floorId);
  }

  private Integer resolveNextSortNo(Long tenantId, Long floorId) {
    if (tenantId == null || floorId == null) {
      return 0;
    }
    List<Room> floorRooms = roomMapper.selectList(Wrappers.lambdaQuery(Room.class)
        .eq(Room::getIsDeleted, 0)
        .eq(Room::getTenantId, tenantId)
        .eq(Room::getFloorId, floorId));
    boolean hasCustomSort = floorRooms.stream()
        .map(Room::getSortNo)
        .filter(Objects::nonNull)
        .anyMatch(sortNo -> sortNo > 0);
    if (!hasCustomSort) {
      return 0;
    }
    return floorRooms.stream()
        .map(Room::getSortNo)
        .filter(Objects::nonNull)
        .filter(sortNo -> sortNo > 0)
        .max(Integer::compareTo)
        .orElse(0) + 10;
  }

  private int compareRoomOrder(Room left, Room right) {
    Integer leftSortNo = left == null ? null : left.getSortNo();
    Integer rightSortNo = right == null ? null : right.getSortNo();
    boolean leftSorted = leftSortNo != null && leftSortNo > 0;
    boolean rightSorted = rightSortNo != null && rightSortNo > 0;
    if (leftSorted && rightSorted) {
      int sortCompare = Integer.compare(leftSortNo, rightSortNo);
      if (sortCompare != 0) {
        return sortCompare;
      }
    } else if (leftSorted != rightSorted) {
      return leftSorted ? -1 : 1;
    }
    return String.valueOf(left == null ? "" : left.getRoomNo())
        .compareToIgnoreCase(String.valueOf(right == null ? "" : right.getRoomNo()));
  }

  private String firstNonBlank(String... values) {
    if (values == null) {
      return null;
    }
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value.trim();
      }
    }
    return null;
  }

  private record RoomTypeMeta(Integer defaultCapacity, boolean functional) {
    private static final RoomTypeMeta EMPTY = new RoomTypeMeta(null, false);
  }
}
