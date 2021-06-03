package com.nseindia.mc.service.member;

import java.util.List;
import java.util.stream.Collectors;

import com.nseindia.mc.controller.dto.MemberInfoItem;
import com.nseindia.mc.exception.BaseServiceException;
import com.nseindia.mc.model.MemberMaster;
import com.nseindia.mc.repository.MemberMasterRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * This service is responsible of managing the members information in the backend
 */
@Service
public class MemberService {

  /**
   * The MemberMasterRespository instance to use to get the data from the persistence
   */
  @Autowired
  MemberMasterRepository memberMasterRepository;

  /**
   * This method gets the information of all members in the persistence
   * @return
   */
  public List<MemberInfoItem> getAllMembersInformation() {
    return memberMasterRepository
      .findAll().stream()
      .map(member -> MemberInfoItem.builder()
                     .name(member.getMemName())
                     .code(member.getMemCd())
                     .type(member.getConstitutionType())
                     .id(member.getMemId()).build()).collect(Collectors.toList());
  }

  /**
   * Retrieves the member information by pan
   * 
   * @param pan The pan by which to search the member
   * @return The information of the matched member
   */
  public String getMemberNameByPan(String pan) {
    return memberMasterRepository
       .getMemberNameByPan(pan)
       .orElseThrow(() -> new BaseServiceException(String.format("Member with pan %s does not exist", pan), HttpStatus.NOT_FOUND));
  }

  /**
   * Gets the member by id
   * 
   * @param memberId The id by which to get the member
   * @return The member matching the given member id
   */
  public MemberMaster getByMemberId(Long memberId) {
    return memberMasterRepository
          .findById(memberId)
          .orElseThrow(() -> new BaseServiceException(String.format("Member with id %d does not exist", memberId) , HttpStatus.NOT_FOUND));
  }
}
