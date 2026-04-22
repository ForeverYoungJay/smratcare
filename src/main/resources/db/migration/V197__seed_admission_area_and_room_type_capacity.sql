-- 补齐区域默认项，并为房间类型写入默认床位数元数据

UPDATE base_data_item
SET remark = JSON_OBJECT('text', '', 'defaultCapacity', 1),
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND item_code = 'ROOM_SINGLE'
  AND is_deleted = 0;

UPDATE base_data_item
SET remark = JSON_OBJECT('text', '', 'defaultCapacity', 2),
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND item_code IN ('ROOM_DOUBLE', 'ROOM_CARE', 'ROOM_SUITE')
  AND is_deleted = 0;

UPDATE base_data_item
SET remark = JSON_OBJECT('text', '', 'defaultCapacity', 3),
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND item_code = 'ROOM_TRIPLE'
  AND is_deleted = 0;

UPDATE base_data_item
SET remark = JSON_OBJECT('text', '', 'defaultCapacity', 0),
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

INSERT INTO base_data_item (
  id,
  tenant_id,
  org_id,
  config_group,
  item_code,
  item_name,
  status,
  sort_no,
  remark,
  created_by,
  create_time,
  update_time,
  is_deleted
)
SELECT UUID_SHORT(),
       org.id,
       org.id,
       'ADMISSION_AREA',
       preset.item_code,
       preset.item_name,
       1,
       preset.sort_no,
       NULL,
       NULL,
       NOW(),
       NOW(),
       0
FROM org
JOIN (
  SELECT 'AREA_A' AS item_code, 'A区' AS item_name, 10 AS sort_no
  UNION ALL SELECT 'AREA_B', 'B区', 20
  UNION ALL SELECT 'AREA_C', 'C区', 30
) preset
LEFT JOIN base_data_item existing_code
  ON existing_code.tenant_id = org.id
 AND existing_code.config_group = 'ADMISSION_AREA'
 AND existing_code.item_code = preset.item_code
LEFT JOIN base_data_item existing_name
  ON existing_name.tenant_id = org.id
 AND existing_name.config_group = 'ADMISSION_AREA'
 AND existing_name.item_name = preset.item_name
WHERE org.is_deleted = 0
  AND existing_code.id IS NULL
  AND existing_name.id IS NULL;
