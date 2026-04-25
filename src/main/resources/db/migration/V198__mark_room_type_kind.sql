-- 为房间类型补充普通房/功能房元数据，避免功能房误生成床位

UPDATE base_data_item
SET remark = JSON_OBJECT('text', '', 'defaultCapacity', 1, 'roomKind', 'NORMAL'),
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND item_code = 'ROOM_SINGLE'
  AND is_deleted = 0;

UPDATE base_data_item
SET remark = JSON_OBJECT('text', '', 'defaultCapacity', 2, 'roomKind', 'NORMAL'),
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND item_code IN ('ROOM_DOUBLE', 'ROOM_CARE', 'ROOM_SUITE')
  AND is_deleted = 0;

UPDATE base_data_item
SET remark = JSON_OBJECT('text', '', 'defaultCapacity', 3, 'roomKind', 'NORMAL'),
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND item_code = 'ROOM_TRIPLE'
  AND is_deleted = 0;

UPDATE base_data_item
SET remark = JSON_OBJECT('text', '', 'defaultCapacity', 0, 'roomKind', 'FUNCTIONAL'),
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND item_code IN (
    'ROOM_NURSING_STATION',
    'ROOM_WATER',
    'ROOM_LAUNDRY',
    'ROOM_TOILET',
    'ROOM_BATH',
    'ROOM_TREATMENT',
    'ROOM_STORAGE',
    'ROOM_ACTIVITY',
    'ROOM_DINING'
  )
  AND is_deleted = 0;
