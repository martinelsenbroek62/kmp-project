package com.nseindia.mc.controller.dto;

import lombok.Data;

/**
 * The pan verification response entity
 */

@Data
public class PanResponse {
  /**
   * The pan number
   */
  private String panNumber;

  /**
   * The pan holder first name
   */
  private String firstName;

  /**
   * The pan holder last name
   */
  private String lastName;

  /**
   * The pan title
   */
  private String panTitle;

  /**
   * The pan status
   */
  private String panStatus;

  /**
   * The return code
   */
  private String returnCode;

  /**
   * The printed name on card
   */
  private String namePrintedOnCard;

  /**
   * The last updated date
   */
  private String lastUpdatedDate;

  /**
   * The pan holder type
   */
  private String typeOfHolder;
}