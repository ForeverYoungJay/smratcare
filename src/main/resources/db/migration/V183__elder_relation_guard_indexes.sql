-- 长者床位关系热点查询与并发保护索引

CREATE INDEX IF NOT EXISTS idx_elder_bed_rel_org_elder_active_deleted
    ON elder_bed_relation (org_id, elder_id, active_flag, is_deleted);

CREATE INDEX IF NOT EXISTS idx_elder_bed_rel_org_bed_active_deleted
    ON elder_bed_relation (org_id, bed_id, active_flag, is_deleted);

CREATE INDEX IF NOT EXISTS idx_elder_bed_rel_tenant_elder_active_deleted
    ON elder_bed_relation (tenant_id, elder_id, active_flag, is_deleted);

CREATE INDEX IF NOT EXISTS idx_elder_bed_rel_tenant_bed_active_deleted
    ON elder_bed_relation (tenant_id, bed_id, active_flag, is_deleted);
