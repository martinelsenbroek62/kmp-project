package com.nseindia.mc.model;

/**
 * The Penalty type enumeration (MTR, KMP)
 */
public enum PenaltyType {
  // The MTR penalty type
  MTR("MTR"),

  // The KMP Penalty type
  KMP("KMP");

  public final String value;

  /**
   * Creates a new instance of this enum with the given string value
   * @param value
   */
  private PenaltyType(String value) {
    this.value = value;
  }
}