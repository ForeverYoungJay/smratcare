package com.zhiyangyun.care.elder.model;

public class BedReleaseResult {
  private Long previousBedId;
  private Long releasedBedId;
  private int closedRelationCount;

  public Long getPreviousBedId() {
    return previousBedId;
  }

  public void setPreviousBedId(Long previousBedId) {
    this.previousBedId = previousBedId;
  }

  public Long getReleasedBedId() {
    return releasedBedId;
  }

  public void setReleasedBedId(Long releasedBedId) {
    this.releasedBedId = releasedBedId;
  }

  public int getClosedRelationCount() {
    return closedRelationCount;
  }

  public void setClosedRelationCount(int closedRelationCount) {
    this.closedRelationCount = closedRelationCount;
  }

  public boolean hasReleasedBed() {
    return releasedBedId != null;
  }
}
