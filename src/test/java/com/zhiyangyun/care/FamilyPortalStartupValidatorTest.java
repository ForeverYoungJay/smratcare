package com.zhiyangyun.care;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zhiyangyun.care.common.file.FileStorageProperties;
import com.zhiyangyun.care.family.config.FamilyPortalProperties;
import com.zhiyangyun.care.family.config.FamilyPortalStartupValidator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.mock.env.MockEnvironment;

class FamilyPortalStartupValidatorTest {

  @Test
  void production_rejects_mock_sms_provider() {
    FamilyPortalProperties familyProperties = new FamilyPortalProperties();
    familyProperties.getSmsCode().setEnabled(true);
    familyProperties.getSmsCode().setProvider("mock");

    FileStorageProperties fileStorageProperties = new FileStorageProperties();
    fileStorageProperties.setProvider("oss");
    fileStorageProperties.getOss().setEndpoint("oss-cn-hangzhou.aliyuncs.com");
    fileStorageProperties.getOss().setBucket("smartcare");
    fileStorageProperties.getOss().setAccessKeyId("ak");
    fileStorageProperties.getOss().setAccessKeySecret("sk");

    MockEnvironment environment = new MockEnvironment();
    environment.setActiveProfiles("prod");

    FamilyPortalStartupValidator validator = new FamilyPortalStartupValidator(
        familyProperties, fileStorageProperties, environment);

    IllegalStateException error = assertThrows(IllegalStateException.class,
        () -> validator.run(new DefaultApplicationArguments(new String[0])));
    org.junit.jupiter.api.Assertions.assertTrue(error.getMessage().contains("sms-code provider"));
  }

  @Test
  void non_prod_allows_local_storage_and_mock_sms() {
    FamilyPortalProperties familyProperties = new FamilyPortalProperties();
    familyProperties.getSmsCode().setEnabled(true);
    familyProperties.getSmsCode().setProvider("mock");

    FileStorageProperties fileStorageProperties = new FileStorageProperties();
    fileStorageProperties.setProvider("local");

    MockEnvironment environment = new MockEnvironment();
    environment.setActiveProfiles("dev");

    FamilyPortalStartupValidator validator = new FamilyPortalStartupValidator(
        familyProperties, fileStorageProperties, environment);

    assertDoesNotThrow(() -> validator.run(new DefaultApplicationArguments(new String[0])));
  }
}
