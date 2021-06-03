package com.nseindia.mc.controller.aiml;

import com.nseindia.mc.model.UserMemCom;
import com.nseindia.mc.repository.UserMemComRepository;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@NoArgsConstructor
public class UserMemComController {
  @Autowired
  private UserMemComRepository userMemComRepository;

  /**
   * Get the users from the TBL_USER_MEM_COM table.  It's used to load the checker list.
   * This is the temporary API for development purpose, most likely it will be replaced with correct integration
   *
   * @return List of UserMemCom
   */
  @GetMapping("users")
  public ResponseEntity<List<UserMemCom>> getAllUsers() {

    return ResponseEntity.ok(
        userMemComRepository.findAll()
    );
  }
}
