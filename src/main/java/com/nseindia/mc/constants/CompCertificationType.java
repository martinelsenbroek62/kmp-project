package com.nseindia.mc.constants;

import java.util.stream.Stream;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
public enum CompCertificationType {
  NISM_CERTIFICATE("NISM Certificate"),
  NISM_PROVISIONAL("NISM Provisional certificate"),
  NISM_CPE("NISM CPE Certificate");
  @JsonValue public final String certificationType;
  CompCertificationType(String compCertificateType) {
    this.certificationType = compCertificateType;
  }
  @JsonCreator
  public static CompCertificationType decode(final String code) {
    return Stream.of(CompCertificationType.values())
        .filter(targetEnum -> targetEnum.certificationType.equals(code))
        .findFirst()
        .orElse(null);
  }
}
