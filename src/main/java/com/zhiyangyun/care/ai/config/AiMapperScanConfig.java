package com.zhiyangyun.care.ai.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * ai 模块 mapper 扫描。主应用 {@code CareApplication} 的 @MapperScan 为静态清单，
 * 为避免改动其他模块文件，本模块自带扫描配置（多个 @MapperScan 可叠加生效）。
 */
@Configuration
@MapperScan("com.zhiyangyun.care.ai.mapper")
public class AiMapperScanConfig {}
