package com.nseindia.mc.value;

public enum PenaltyFileType {
  ApprovalNote("Approval Note"),
  AnnexureFile("Annexure File"),
  PenaltyLetters("Penalty Letters"),
  PenaltyReversalLetter("Penalty Reversal Letter"),
  PenaltyInspectionFile("Penalty Inspection File"),
  PenaltyResolutionAgendaMinutes("Penalty Resolution Agenda Minutes"),
  All("All");
  String code;

  PenaltyFileType(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
