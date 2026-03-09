package com.zhiyangyun.care.family.sms;

import lombok.Data;

@Data
public class FamilySmsSendResult {
  private boolean success;
  private String provider;
  private String messageId;
  private String requestPayload;
  private String responsePayload;
  private String errorMessage;

  public static FamilySmsSendResult success(String provider, String messageId, String requestPayload, String responsePayload) {
    FamilySmsSendResult result = new FamilySmsSendResult();
    result.setSuccess(true);
    result.setProvider(provider);
    result.setMessageId(messageId);
    result.setRequestPayload(requestPayload);
    result.setResponsePayload(responsePayload);
    return result;
  }

  public static FamilySmsSendResult failed(String provider, String requestPayload, String responsePayload, String errorMessage) {
    FamilySmsSendResult result = new FamilySmsSendResult();
    result.setSuccess(false);
    result.setProvider(provider);
    result.setRequestPayload(requestPayload);
    result.setResponsePayload(responsePayload);
    result.setErrorMessage(errorMessage);
    return result;
  }
}
