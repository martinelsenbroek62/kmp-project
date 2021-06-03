package com.nseindia.mc.controller.kmp;

import com.nseindia.mc.controller.dto.*;
import com.nseindia.mc.model.KMPAction;
import com.nseindia.mc.service.kmp.KMPService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static reactor.core.publisher.Mono.when;

@ExtendWith(MockitoExtension.class)
class KMPControllerTest {

    @InjectMocks
    private KMPController kmpController;

    @Mock
    private KMPService kmpService;

    @Test
    void listKmpMembers() {
        List<KeyManagementPersonnelDto> kmps = new ArrayList<>();
        kmps.add(new KeyManagementPersonnelDto());
        kmps.get(0).setAction("action");
        doReturn(kmps).when(kmpService).listKmpMembers(eq(1L), eq("selected"), eq("from"), eq("to"), eq("pan"), eq(true));
        ResponseEntity<List<KeyManagementPersonnelDto>> actual = kmpController.listKmpMembers(1L, "selected", "from", "to", "pan", true);
        assertEquals(kmps, actual.getBody());
    }

    @Test
    void getConfigData() {
        KmpConfigDataDto configData = new KmpConfigDataDto();
        configData.setAdditionalDesignationInfoTip("additionalInfo");
        doReturn(configData).when(kmpService).getConfigData();
        ResponseEntity<KmpConfigDataDto> actual = kmpController.getConfigData();
        assertEquals(configData, actual.getBody());
    }

    @Test
    void getMembersByRole() {
        List<RoleBasedKMPDataDto> members = new ArrayList<>();
        members.add(new RoleBasedKMPDataDto(1L, "name", "pan", 1L, "mobile", "phone", "email"));
        doReturn(members).when(kmpService).getMembersByRole(eq(1L), eq("Director"));
        ResponseEntity<List<RoleBasedKMPDataDto>> actual = kmpController.getMembersByRole(1L, "Director");
        assertEquals(members, actual.getBody());
    }

    @Test
    void createKMP() {
        CreateKMPRequestBody request = new CreateKMPRequestBody();
        ResponseEntity<CommonMessageDto> actual = kmpController.createKMP(1L, request);
        verify(kmpService).createKMP(eq(1L), eq(request));
        assertEquals(new CommonMessageDto(HttpStatus.OK.value(), "KMP details successfully saved"), actual.getBody());
    }

    @Test
    void updateKMPs() {
        UpdateKMPsRequestBody request = new UpdateKMPsRequestBody();
        ResponseEntity<CommonMessageDto> actual = kmpController.updateKMPs(1L, request);
        verify(kmpService).updateKMPs(eq(1L), eq(request));
        assertEquals(new CommonMessageDto(HttpStatus.OK.value(), "KMP details successfully updated"), actual.getBody());
    }

    @Test
    void validatePan() {
        PanValidationResponse response = PanValidationResponse
                .builder()
                .panStatus(PanValidationStatus.INVALID)
                .build();
        doReturn(response).when(kmpService).validatePan(eq(1L), eq("pan"), eq("name"), eq(false));
        ResponseEntity<PanValidationResponse> actual = kmpController.validatePan(1L, "pan", "name", false);
        assertEquals(response, actual.getBody());

        response = PanValidationResponse
                .builder()
                .panStatus(PanValidationStatus.EXISTING_AND_VALID)
                .build();
        doReturn(response).when(kmpService).validatePan(eq(1L), eq("pan"), eq("name"), eq(false));
        actual = kmpController.validatePan(1L, "pan", "name", false);
        assertEquals(response, actual.getBody());
    }

    @Test
    void deleteKMPDetails() {
        DeleteKMPDetailsRequestBody request = new DeleteKMPDetailsRequestBody();
        ResponseEntity<CommonMessageDto> actual = kmpController.deleteKMPDetails(1L, request);
        verify(kmpService).deleteKMPDetails(eq(1L), eq(request));
        assertEquals(new CommonMessageDto(HttpStatus.OK.value(), "KMP details successfully deleted"), actual.getBody());
    }

    @Test
    void sendOtp() {
        doReturn(new OtpSendResponse(HttpStatus.OK.value(), "ÓTP sent successfully.", 1L)).when(kmpService)
                .sendOtp(any(), any(), any(), any());
        ResponseEntity<OtpSendResponse> sendOtp = kmpController.sendOTP(1L, "verifierId", "verifierName", "1234567");
        verify(kmpService).sendOtp(eq(1L), eq("verifierId"), eq("verifierName"), eq("1234567"));
        assertEquals(new OtpSendResponse(HttpStatus.OK.value(), "ÓTP sent successfully.", 1L), sendOtp.getBody());
    }

    @Test
    void validateOtp() {
        doReturn(new CommonMessageDto(HttpStatus.OK.value(), "ÓTP validated successfully.")).when(kmpService)
                .validateOtp(any(), any(), any(), any(), any(), any());
        ResponseEntity<CommonMessageDto> validateOTP = kmpController.validateOTP(1L, 2L, "verifierId", "verifierName", "1234567", "591827");
        verify(kmpService).validateOtp(eq(1L), eq(2L), eq("verifierId"), eq("verifierName"), eq("1234567"), eq("591827"));
        assertEquals(new CommonMessageDto(HttpStatus.OK.value(), "KMP details successfully deleted"), validateOTP.getBody());
    }
}