package com.nseindia.mc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nseindia.mc.model.MemberMaster;

/**
 * The spring data repository for storing the member master entity
 */
public interface MemberMasterRepository extends JpaRepository<MemberMaster, Long> {

	Optional<MemberMaster> findFirstByMemCdOrMemName(String memberCode, String memberName);

	/**
	 * Find the email of member.
	 *
	 * @param id
	 * @return the email of member
	 */
	@Query(value = "SELECT T.EMAIL_ID FROM TBL_MEMBER_MASTER T WHERE T.MEM_ID= ?", nativeQuery = true)
	Optional<String> findEmail(Long id);

	/**
	 * Gets the member name by pan number
	 * 
	 * @param pan The pan number to search by
	 * @return The name of the matched member
	 */
	@Query(value = "SELECT T.MEM_NAME FROM TBL_MEMBER_MASTER T WHERE T.MEM_PAN_NUM= ?", nativeQuery = true)
	Optional<String> getMemberNameByPan(String pan);

	@Query(value = "SELECT T.EMAIL_ID FROM TBL_MEMBER_MASTER T WHERE T.MEM_ID IN :ids", nativeQuery = true)
	Optional<List<String>> findAllEmail(List<Long> ids);
}
