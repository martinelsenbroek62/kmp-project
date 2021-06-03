package com.nseindia.mc.controller.member;

import java.util.List;

import com.nseindia.mc.controller.dto.MemberInfoItem;
import com.nseindia.mc.service.member.MemberService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** This controller handles the requests related to member management */
@RestController
@RequestMapping("/mc-kmp/v1")
public class MemberController {

  /**
   * The member service to use to access members data in the backend
   */
  @Autowired
  MemberService memberService;

  /**
   * Gets all members information from the backend
   *
   * @return The ResponseEntity instance with the List of members informtation items as body
   */
  @GetMapping("/members")
  public ResponseEntity<List<MemberInfoItem>> getAllMembersInformation() {
    return ResponseEntity.ok(
      memberService.getAllMembersInformation());
  }
}
