package com.zhiyangyun.care.common.web;

/**
 * 分页参数护栏：对外部传入的 pageNo/pageSize 做钳制，避免非法值或超大 pageSize 导致的全表加载与 OOM。
 *
 * <p>用法：在 Service 的分页方法入口处调用
 * {@code pageNo = PageGuard.pageNo(pageNo); pageSize = PageGuard.pageSize(pageSize);}
 */
public final class PageGuard {
  /** 单页最大条数上限，超过则钳制到该值。 */
  public static final long MAX_PAGE_SIZE = 200L;
  /** pageSize 缺省/非法时使用的默认值。 */
  public static final long DEFAULT_PAGE_SIZE = 20L;

  private PageGuard() {}

  /** 规整页码：非正数一律回落到第 1 页。 */
  public static long pageNo(long pageNo) {
    return pageNo <= 0 ? 1L : pageNo;
  }

  /** 规整每页条数：非正数回落默认值，超过上限则钳制到 {@link #MAX_PAGE_SIZE}。 */
  public static long pageSize(long pageSize) {
    if (pageSize <= 0) {
      return DEFAULT_PAGE_SIZE;
    }
    return Math.min(pageSize, MAX_PAGE_SIZE);
  }

  /** 使用自定义上限规整每页条数（用于个别允许更大批量的场景）。 */
  public static long pageSize(long pageSize, long maxPageSize) {
    if (pageSize <= 0) {
      return Math.min(DEFAULT_PAGE_SIZE, maxPageSize);
    }
    return Math.min(pageSize, maxPageSize);
  }
}
