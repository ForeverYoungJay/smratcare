-- 补齐入住相关默认类型，统一床位类型与房间类型的标准编码

UPDATE base_data_item
SET item_code = 'BED_STANDARD',
    item_name = '标准床',
    status = 1,
    sort_no = 10,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_BED_TYPE'
  AND (item_code = 'BED_STANDARD' OR item_name = '标准床');

UPDATE base_data_item
SET item_code = 'BED_CARE',
    item_name = '护理床',
    status = 1,
    sort_no = 20,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_BED_TYPE'
  AND (item_code = 'BED_CARE' OR item_name = '护理床');

UPDATE base_data_item
SET item_code = 'BED_ELECTRIC',
    item_name = '电动护理床',
    status = 1,
    sort_no = 30,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_BED_TYPE'
  AND (item_code = 'BED_ELECTRIC' OR item_name IN ('电动床', '电动护理床'));

UPDATE base_data_item
SET item_code = 'BED_ANTI_DECUBITUS',
    item_name = '防压疮床',
    status = 1,
    sort_no = 40,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_BED_TYPE'
  AND (item_code = 'BED_ANTI_DECUBITUS' OR item_name = '防压疮床');

UPDATE base_data_item
SET item_code = 'BED_MEDICAL',
    item_name = '医用床',
    status = 1,
    sort_no = 50,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_BED_TYPE'
  AND (item_code = 'BED_MEDICAL' OR item_name IN ('医用床', '医疗床'));

UPDATE base_data_item
SET item_code = 'ROOM_SINGLE',
    item_name = '单人间',
    status = 1,
    sort_no = 10,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND (item_code = 'ROOM_SINGLE' OR item_name = '单人间');

UPDATE base_data_item
SET item_code = 'ROOM_DOUBLE',
    item_name = '双人间',
    status = 1,
    sort_no = 20,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND (item_code = 'ROOM_DOUBLE' OR item_name = '双人间');

UPDATE base_data_item
SET item_code = 'ROOM_TRIPLE',
    item_name = '三人间',
    status = 1,
    sort_no = 30,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND (item_code = 'ROOM_TRIPLE' OR item_name = '三人间');

UPDATE base_data_item
SET item_code = 'ROOM_CARE',
    item_name = '护理房',
    status = 1,
    sort_no = 40,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND (item_code = 'ROOM_CARE' OR item_name = '护理房');

UPDATE base_data_item
SET item_code = 'ROOM_SUITE',
    item_name = '套间',
    status = 1,
    sort_no = 45,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND (item_code = 'ROOM_SUITE' OR item_name = '套间');

UPDATE base_data_item
SET item_code = 'ROOM_NURSING_STATION',
    item_name = '护理站',
    status = 1,
    sort_no = 50,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND (item_code = 'ROOM_NURSING_STATION' OR item_name = '护理站');

UPDATE base_data_item
SET item_code = 'ROOM_WATER',
    item_name = '开水房',
    status = 1,
    sort_no = 60,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND (item_code = 'ROOM_WATER' OR item_name = '开水房');

UPDATE base_data_item
SET item_code = 'ROOM_LAUNDRY',
    item_name = '洗衣房',
    status = 1,
    sort_no = 70,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND (item_code = 'ROOM_LAUNDRY' OR item_name = '洗衣房');

UPDATE base_data_item
SET item_code = 'ROOM_TOILET',
    item_name = '卫生间',
    status = 1,
    sort_no = 80,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND (item_code = 'ROOM_TOILET' OR item_name = '卫生间');

UPDATE base_data_item
SET item_code = 'ROOM_BATH',
    item_name = '浴室',
    status = 1,
    sort_no = 90,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND (item_code = 'ROOM_BATH' OR item_name IN ('浴室', '沐浴间'));

UPDATE base_data_item
SET item_code = 'ROOM_TREATMENT',
    item_name = '治疗室',
    status = 1,
    sort_no = 100,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND (item_code = 'ROOM_TREATMENT' OR item_name = '治疗室');

UPDATE base_data_item
SET item_code = 'ROOM_STORAGE',
    item_name = '库房',
    status = 1,
    sort_no = 110,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND (item_code = 'ROOM_STORAGE' OR item_name IN ('库房', '储物间'));

UPDATE base_data_item
SET item_code = 'ROOM_ACTIVITY',
    item_name = '活动室',
    status = 1,
    sort_no = 120,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND (item_code = 'ROOM_ACTIVITY' OR item_name = '活动室');

UPDATE base_data_item
SET item_code = 'ROOM_DINING',
    item_name = '餐厅',
    status = 1,
    sort_no = 130,
    is_deleted = 0,
    update_time = NOW()
WHERE config_group = 'ADMISSION_ROOM_TYPE'
  AND (item_code = 'ROOM_DINING' OR item_name IN ('餐厅', '餐饮间'));

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
       preset.config_group,
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
  SELECT 'ADMISSION_BED_TYPE' AS config_group, 'BED_STANDARD' AS item_code, '标准床' AS item_name, 10 AS sort_no
  UNION ALL SELECT 'ADMISSION_BED_TYPE', 'BED_CARE', '护理床', 20
  UNION ALL SELECT 'ADMISSION_BED_TYPE', 'BED_ELECTRIC', '电动护理床', 30
  UNION ALL SELECT 'ADMISSION_BED_TYPE', 'BED_ANTI_DECUBITUS', '防压疮床', 40
  UNION ALL SELECT 'ADMISSION_BED_TYPE', 'BED_MEDICAL', '医用床', 50
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_SINGLE', '单人间', 10
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_DOUBLE', '双人间', 20
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_TRIPLE', '三人间', 30
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_CARE', '护理房', 40
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_SUITE', '套间', 45
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_NURSING_STATION', '护理站', 50
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_WATER', '开水房', 60
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_LAUNDRY', '洗衣房', 70
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_TOILET', '卫生间', 80
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_BATH', '浴室', 90
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_TREATMENT', '治疗室', 100
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_STORAGE', '库房', 110
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_ACTIVITY', '活动室', 120
  UNION ALL SELECT 'ADMISSION_ROOM_TYPE', 'ROOM_DINING', '餐厅', 130
) preset
LEFT JOIN base_data_item existing_code
  ON existing_code.tenant_id = org.id
 AND existing_code.config_group = preset.config_group
 AND existing_code.item_code = preset.item_code
LEFT JOIN base_data_item existing_name
  ON existing_name.tenant_id = org.id
 AND existing_name.config_group = preset.config_group
 AND existing_name.item_name = preset.item_name
WHERE org.is_deleted = 0
  AND existing_code.id IS NULL
  AND existing_name.id IS NULL;
