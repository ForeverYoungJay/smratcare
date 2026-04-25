-- 清理功能房下误生成的历史床位，并将功能房容量回正为 0

UPDATE room r
SET r.capacity = 0,
    r.update_time = NOW()
WHERE r.is_deleted = 0
  AND EXISTS (
    SELECT 1
    FROM base_data_item item
    WHERE item.is_deleted = 0
      AND item.tenant_id = r.tenant_id
      AND item.config_group = 'ADMISSION_ROOM_TYPE'
      AND (
        CONVERT(item.item_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(r.room_type USING utf8mb4) COLLATE utf8mb4_unicode_ci
        OR CONVERT(item.item_name USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(r.room_type USING utf8mb4) COLLATE utf8mb4_unicode_ci
      )
      AND (
        (JSON_VALID(item.remark) = 1 AND CONVERT(JSON_UNQUOTE(JSON_EXTRACT(item.remark, '$.roomKind')) USING utf8mb4) COLLATE utf8mb4_unicode_ci = 'FUNCTIONAL')
        OR (JSON_VALID(item.remark) = 1 AND JSON_EXTRACT(item.remark, '$.defaultCapacity') = 0)
      )
  );

UPDATE bed b
JOIN room r
  ON r.id = b.room_id
 AND r.is_deleted = 0
SET b.is_deleted = 1,
    b.update_time = NOW()
WHERE b.is_deleted = 0
  AND b.elder_id IS NULL
  AND NOT EXISTS (
    SELECT 1
    FROM elder_bed_relation rel
    WHERE rel.is_deleted = 0
      AND rel.active_flag = 1
      AND rel.bed_id = b.id
  )
  AND EXISTS (
    SELECT 1
    FROM base_data_item item
    WHERE item.is_deleted = 0
      AND item.tenant_id = r.tenant_id
      AND item.config_group = 'ADMISSION_ROOM_TYPE'
      AND (
        CONVERT(item.item_code USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(r.room_type USING utf8mb4) COLLATE utf8mb4_unicode_ci
        OR CONVERT(item.item_name USING utf8mb4) COLLATE utf8mb4_unicode_ci = CONVERT(r.room_type USING utf8mb4) COLLATE utf8mb4_unicode_ci
      )
      AND (
        (JSON_VALID(item.remark) = 1 AND CONVERT(JSON_UNQUOTE(JSON_EXTRACT(item.remark, '$.roomKind')) USING utf8mb4) COLLATE utf8mb4_unicode_ci = 'FUNCTIONAL')
        OR (JSON_VALID(item.remark) = 1 AND JSON_EXTRACT(item.remark, '$.defaultCapacity') = 0)
      )
  );
