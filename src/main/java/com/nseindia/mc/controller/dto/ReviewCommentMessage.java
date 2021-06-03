package com.nseindia.mc.controller.dto;

import lombok.Data;

@Data
public class ReviewCommentMessage {
  private Long timestamp; // unix timestamp in seconds for UTC timezone.
  private String comment;
}
