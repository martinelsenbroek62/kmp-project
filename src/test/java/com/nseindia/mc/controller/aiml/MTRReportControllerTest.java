package com.nseindia.mc.controller.aiml;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

import com.nseindia.mc.controller.dto.*;
import com.nseindia.mc.controller.dto.GenerateApprovalNoteAnnexureRequest.Option;
import com.nseindia.mc.exception.BaseServiceException;
import com.nseindia.mc.model.MTRDailyFile;
import com.nseindia.mc.service.mtrReport.MtrFileValidateService;
import com.nseindia.mc.service.mtrReport.MtrReportService;

import com.nseindia.mc.service.penalty.PenaltyService;
import org.apache.tools.ant.util.ReflectUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Containing Test for MTRReportController. */

@ExtendWith(MockitoExtension.class)
public class MTRReportControllerTest {

    @InjectMocks
    private MTRReportController controller;

    @Mock private MtrReportService mtrReportService;

    @Mock private PenaltyService penaltyService;

    @Mock private MtrFileValidateService validateService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }


/**
     * Get main request options.
     *
     * @throws Exception
     *//*

    @Test
    public void getMainRequestOptions_success() throws Exception {
        MainRequestOption option = new MainRequestOption("MTR Submission");
        when(mtrReportService.getMainRequestOptions()).thenReturn(Arrays.asList(option));

        controller.getMainRequestOptions();
        verify(mtrReportService).getMainRequestOptions();
    }

    */
/**
     * Get upload cutoff period.
     *
     * @throws Exception
     *//*

    @Test
    public void getUploadCutOffPeriod_success() throws Exception {
        UploadCutOffPeriod uploadCutOffPeriod = new UploadCutOffPeriod("9:00 AM", "3:00 PM");
        when(mtrReportService.getUploadCutOffPeriod()).thenReturn(uploadCutOffPeriod);
        controller.getUploadCutOffPeriod();
        verify(mtrReportService).getUploadCutOffPeriod();
    }


    */
/**
     * Get member upload status.
     *
     * @throws Exception
     *//*

    @Test
    public void getMemberUploadStatus_success() throws Exception {
        MemberUploadStatus memberUploadStatus = new MemberUploadStatus();
        memberUploadStatus.setMemberCode("MEM01");
        when(mtrReportService.getMemberUploadStatus(1)).thenReturn(memberUploadStatus);
        controller.getMemberUploadStatus(1);
        verify(mtrReportService).getMemberUploadStatus(1);
    }

    */
/**
     * New member on board.
     * @throws Exception
     *//*

    @Test
    public void newMembersOnboard_success() throws Exception{
        controller.newMembersOnboard(1, true);
        verify(mtrReportService).newMembersOnboard(1,true);
    }

    */
/**
     * Download mtr files.
     * @throws Exception
     *//*

    @Test
    public void downloadFile_failure() throws Exception{
        MTRDailyFile dailyFile = new MTRDailyFile();
        File tmp = new File("tmp.txt");
        tmp.createNewFile();
        dailyFile.setDmsResIndex(tmp.getAbsolutePath());
        when(mtrReportService.getMtrFileById(1)).thenReturn(dailyFile);

        try {
            controller.downloadFile("1");
        } catch (BaseServiceException e) {
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getHttpStatus());
        }
        tmp.deleteOnExit();
    }

    @Test
    public void downloadMtrFile_failure() throws Exception{
        MTRDailyFile dailyFile = new MTRDailyFile();
        dailyFile.setDmsDocIndex("not_existed_file");
        when(mtrReportService.getMtrFileById(1)).thenReturn(dailyFile);

        try {
            controller.downloadMtrFile(1, false);
        } catch (BaseServiceException e) {
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getHttpStatus());
        }
    }

    */
/**
     * Get member details.
     * @throws Exception
     *//*

    @Test
    public void getMemberDetails_success() throws Exception{
        MemberDetailsDto memberDetailsDto = new MemberDetailsDto();

        when(mtrReportService.getMemberDetails(1,"M","C")).thenReturn(memberDetailsDto);

        ResponseEntity<MemberDetailsDto> response = controller.getMemberDetails(1, "M", "C");
        assertEquals(memberDetailsDto, response.getBody());
        verify(mtrReportService).getMemberDetails(1,"M","C");
    }

    */
/**
     * List MTR Daily Files for member.
     * @throws Exception
     *//*

    @Test
    public void listMtrDailyFilesMember_success() throws Exception{
        MTRMemberDailyFileDetailsDto dailyFileDetailsDto = new MTRMemberDailyFileDetailsDto();
        List<MTRMemberDailyFileDetailsDto> list = new ArrayList<>();
        list.add(dailyFileDetailsDto);
        when(mtrReportService.listMtrDailyFilesMember(1L,"M",
                LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE).atStartOfDay(),
                LocalTime.MAX.atDate(LocalDate.parse("2021-01-06",DateTimeFormatter.ISO_DATE)))).thenReturn(list);

        ResponseEntity<List<MTRMemberDailyFileDetailsDto>> actual = controller.listMtrDailyFilesMember(
                1L,
                "M",
                LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE),
                LocalDate.parse("2021-01-06",DateTimeFormatter.ISO_DATE)
        );
        assertEquals(
                list,
                actual.getBody()
        );
        verify(mtrReportService).listMtrDailyFilesMember(1L,"M",
                LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE).atStartOfDay(),
                LocalTime.MAX.atDate(LocalDate.parse("2021-01-06",DateTimeFormatter.ISO_DATE)));
    }

    */
/**
     * List MTR daily files for NSE.
     * @throws Exception
     *//*

    @Test
    public void listMtrDailyFilesNSE_success() throws Exception{
        MTRDailyFileDetailsDto dailyFileDetailsDto = new MTRDailyFileDetailsDto();
        List<MTRDailyFileDetailsDto> list = new ArrayList<>();
        list.add(dailyFileDetailsDto);
        when(mtrReportService.listMtrDailyFilesNse(1L,
                LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE))).thenReturn(list);

        ResponseEntity<List<MTRDailyFileDetailsDto>> actual = controller.listMtrDailyFilesNse(1L, 1L, LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE));
        assertEquals(
                list,
                actual.getBody()
        );
        verify(mtrReportService).listMtrDailyFilesNse(1L,
                LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE));
    }

    */
/**
     * List Ho Maker.
     * @throws Exception
     *//*

    @Test
    public void listHoMakerMtr_success() throws Exception{
        HoMakerDto hoMakerDto = new HoMakerDto();
        List<HoMakerDto> list = new ArrayList<>();
        list.add(hoMakerDto);
        when(mtrReportService.listHoMakerMtr()).thenReturn(list);

        ResponseEntity<List<HoMakerDto>> actual = controller.listHoMakerMtr(1L);
        assertEquals(list, actual.getBody());
        verify(mtrReportService).listHoMakerMtr();
    }

    */
/**
     * Assign HO Maker.
     * @throws Exception
     *//*

    @Test
    public void assignHoMakerMtr_success() throws Exception{
        AssignHoMakerRequest request = new AssignHoMakerRequest();
        request.setHoMakerId(1L);
        doNothing().when(mtrReportService).assignHoMaker(request.getHoMakerId());
        controller.assignHoMakerMtr(1L, request);
        verify(mtrReportService).assignHoMaker(request.getHoMakerId());
    }


    */
/**
     * Download MTR file to NSE.
     * @throws Exception
     *//*

    @Test
    public void downloadMtrFileNse_success() throws Exception{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        when(mtrReportService.downloadMtrFiles(1L
                ,LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE)
                ,LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE)
                ,new ArrayList<String>())).thenReturn(bos);

        controller.downloadMtrFileNse(1, 1L, LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE), LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE), new ArrayList<>());
        verify(mtrReportService).downloadMtrFiles(1L
                ,LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE)
                ,LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE)
                ,new ArrayList<String>());
    }

    */
/**
     * List MTR penality file types.
     * @throws Exception
     *//*

    @Test
    public void listMtrPenaltyFileTypes_success() throws Exception{
        PenaltyDocDto docDto = new PenaltyDocDto();
        List<PenaltyDocDto> list = new ArrayList<>();
        list.add(docDto);
        when(mtrReportService.listMtrPenaltyFileTypes()).thenReturn(list);
        ResponseEntity<?> actual = controller.listMtrPenaltyFileTypes(1L);
        assertEquals(list, actual.getBody());
        verify(mtrReportService).listMtrPenaltyFileTypes();
    }

    */
/**
     * Validate member can make nil submission or not..
     * @throws Exception
     *//*

    @Test
    public void validateNilSubmission_success() throws Exception{

        MemberUploadStatus uploadStatus = new MemberUploadStatus();
        uploadStatus.setLastTotalAmountFunded(0.0);
        when(mtrReportService.getMemberUploadStatus(1L)).thenReturn(uploadStatus);

        ResponseEntity<?> actual = controller.validateNilSubmission(1L);
        assertEquals(HttpStatus.NO_CONTENT, actual.getStatusCode());
    }

    @Test
    public void validateNilSubmission_failure() throws Exception{

        MemberUploadStatus uploadStatus = new MemberUploadStatus();
        uploadStatus.setLastTotalAmountFunded(3.4);
        when(mtrReportService.getMemberUploadStatus(1L)).thenReturn(uploadStatus);

        try {
            ResponseEntity<?> actual = controller.validateNilSubmission(1L);
        } catch (BaseServiceException e) {
            assertEquals(HttpStatus.FORBIDDEN, e.getHttpStatus());
            assertEquals("Validations failed, please verify if you margin trading exposure is 0.", e.getMessage());
        }
    }

    */
/**
     * Submit a nil submission.
     * @throws Exception
     *//*

    @Test
    public void submitNilSubmission_success() throws Exception{
        doNothing().when(mtrReportService).submitNilSubmission(1L);

        controller.submitNilSubmission(1L);

        verify(mtrReportService).submitNilSubmission(1L);
    }

    */
/**
     * Validate mrt file.
     * @throws Exception
     *//*

    @Test
    public void validateMtrFile_success() throws Exception{

    }


    */
/**
     * Submit mtr file.
     * @throws Exception
     *//*

    @Test
    public void submiMtrFile_success() throws Exception{

    }

    */
/**
     * mock of validation response file download
     * @throws Exception
     *//*

    @Test
    public void downloadFile_success()throws Exception{
        ReflectionTestUtils.setField(controller, "resFileDirectory", "test_files");

        File tmp = new File(System.getProperty("user.dir")+ File.separator + "test_files" + File.separator + "tmp.txt");
        tmp.createNewFile();

        ResponseEntity<?> response = controller.downloadFile("tmp.txt");
        Assert.assertEquals(APPLICATION_OCTET_STREAM.toString(), response.getHeaders().get("Content-Type").get(0));
        tmp.deleteOnExit();
    }

    */
/**
     * Trigger monthly penalty calculation
     * @throws Exception
     *//*

    @Test
    public void computePenaltyMTR_success()throws Exception{
        ComputePenaltyMTRRequest request = new ComputePenaltyMTRRequest();
        request.setSubmissionYear(2021);
        request.setSubmissionMonth(1);
        CommonMessageDto dto = new CommonMessageDto(1,"MS");
        when(penaltyService.computePenaltyMTR(request.getSubmissionYear(), request.getSubmissionMonth())).thenReturn(dto);

        controller.computePenaltyJob(request.getSubmissionYear(), request.getSubmissionMonth());
        verify(penaltyService).computePenaltyMTR(request.getSubmissionYear(), request.getSubmissionMonth());
    }

    // TODO - move to PenaltyController Tests
    // /**
    //  * List computed penalty for members
    //  * @throws Exception
    //  */
    // @Test
    // public void listMemberPenalty_success() throws Exception{
    //     ListMemberPenaltyResponse response = new ListMemberPenaltyResponse();
    //     when(penaltyService.listMemberPenalty(12, 6)).thenReturn(response);
    //     ResponseEntity<ListMemberPenaltyResponse> actual = controller.listMemberPenalty(1L, 12, 6);
    //     assertEquals(response, actual.getBody());
    //     verify(penaltyService).listMemberPenalty(12, 6);
    // }

/**
     * Details of the cumulative member data based on whether it should be member wise or not.
     * @throws Exception
     *//*

    @Test
    public void listCumulativeDtls_success() throws Exception{
        ListCumulativeDetailsResponse response = new ListCumulativeDetailsResponse();
        when(mtrReportService.listCumulativeDtls(LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE),
                LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE),true)).thenReturn(response);

        ResponseEntity<ListCumulativeDetailsResponse> actual = controller.listCumulativeDtls(LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE),
                LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE),true);
        assertEquals(response, actual.getBody());
        verify(mtrReportService).listCumulativeDtls(LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE),
                LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE),true);
    }

    */
/**
     * Details of the cumulative member data based on whether it should be member wise or not.
     * @throws Exception
     *//*

    @Test
    public void leverageReport_totalIndeptness_success() throws Exception{
        List<TotalIndebtednessDto> tiDtoList = new ArrayList<>();
        TotalIndebtednessDto tiDto = new TotalIndebtednessDto();
        tiDtoList.add(tiDto);
        LeverageReportResponse response = new LeverageReportResponse();
        response.setTotalIndebtedness(tiDtoList);
        when(mtrReportService.leverageTotalIndebtednessReport(LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE),
                LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE))).thenReturn(response);

        ResponseEntity<LeverageReportResponse> actual = controller.leverageReport(LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE), LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE), "totalIndebtedness");
        assertEquals(response, actual.getBody());
        verify(mtrReportService).leverageTotalIndebtednessReport(LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE),
                LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE));
    }

    */
/**
     * Details of the cumulative member data based on whether it should be member wise or not.
     * @throws Exception
     *//*

    @Test
    public void leverageReport_maxAllowableExposure_success() throws Exception{
        List<MaxAllowableExposureDto> maeDtoList = new ArrayList<>();
        MaxAllowableExposureDto maeDto = new MaxAllowableExposureDto();
        maeDtoList.add(maeDto);
        LeverageReportResponse response = new LeverageReportResponse();
        response.setMaxAllowableExposure(maeDtoList);
        when(mtrReportService.leverageMaxAllowableExposureReport(LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE),
                LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE))).thenReturn(response);
        ResponseEntity<LeverageReportResponse> actual = controller.leverageReport(LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE),
                LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE), "maxAllowableExposure");
        assertEquals(response, actual.getBody());
        verify(mtrReportService).leverageMaxAllowableExposureReport(LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE),
                LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE));
    }

    */
/**
     * Details of the cumulative member data based on whether it should be member wise or not.
     * @throws Exception
     *//*

    @Test
    public void leverageReport_maxClientAllowableExposure_success() throws Exception{
        List<MaxClientAllowableExposureDto> maeDtoList = new ArrayList<>();
        MaxClientAllowableExposureDto maeDto = new MaxClientAllowableExposureDto();
        maeDtoList.add(maeDto);
        LeverageReportResponse response = new LeverageReportResponse();
        response.setMaxClientAllowableExposure(maeDtoList);
        when(mtrReportService.leverageMaxClientAllowableExposureReport(LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE),
                LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE))).thenReturn(response);
        ResponseEntity<LeverageReportResponse> actual = controller.leverageReport(LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE),
                LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE), "maxClientAllowableExposure");
        assertEquals(response, actual.getBody());
        verify(mtrReportService).leverageMaxClientAllowableExposureReport(LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE),
                LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE));
    }

    */
/**
     * Details of the cumulative member data based on whether it should be member wise or not.
     * @throws Exception
     *//*

    @Test
    public void leverageReport_lenderWiseExposure_success() throws Exception{
        List<LenderWiseExposureDto> maeDtoList = new ArrayList<>();
        LenderWiseExposureDto maeDto = new LenderWiseExposureDto();
        maeDtoList.add(maeDto);
        LeverageReportResponse response = new LeverageReportResponse();
        response.setLenderWiseExposure(maeDtoList);

        when(mtrReportService.leverageLenderWiseExposureReport(LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE),
                LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE))).thenReturn(response);

        ResponseEntity<LeverageReportResponse> actual = controller.leverageReport(LocalDate.parse("2021-01-05", DateTimeFormatter.ISO_DATE), LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE), "lenderWiseExposure");
        assertEquals(response, actual.getBody());
        verify(mtrReportService).leverageLenderWiseExposureReport(LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE),
                LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE));
    }

    @Test(expected = BaseServiceException.class)
    public void leverageReport_invalidReportType_fail() throws Exception{
        controller.leverageReport(LocalDate.parse("2021-01-05", DateTimeFormatter.ISO_DATE), LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE), "invalid");
    }

    */
/**
     * Details of the cumulative member data based on whether it should be member wise or not.
     * @throws Exception
     *//*

    @Test
    public void getNonSubmissionMbrDtls_success() throws Exception{
        NonSubmissionMbrDtlsResponse response = new NonSubmissionMbrDtlsResponse();
        when(mtrReportService.getNonSubmissionMbrDtls(2020,12,"C", "N" )).thenReturn(response);

        ResponseEntity<NonSubmissionMbrDtlsResponse> actual = controller.getNonSubmissionMbrDtls(2020, 12, "C", "N");

        assertEquals(response, actual.getBody());
        verify(mtrReportService).getNonSubmissionMbrDtls(2020,12,"C", "N" );
    }

    */
/**
     * List computed penalty for members
     * @throws Exception
     *//*

    @Test
    public void generateApprovalNoteAnnexure_success() throws Exception{
        GenerateApprovalNoteAnnexureRequest request = new GenerateApprovalNoteAnnexureRequest();
        CommonMessageDto response = new CommonMessageDto(1,"MS");
        when(penaltyService.generateApprovalNoteAnnexure(request, 1L)).thenReturn(response);

        ResponseEntity<CommonMessageDto> actual = controller.generateApprovalNoteAnnexure(1, request);
        assertEquals(response, actual.getBody());
        verify(penaltyService).generateApprovalNoteAnnexure(request, 1L);

        // ONLY_APPROVAL_NOTE
        request.setOption(Option.ONLY_APPROVAL_NOTE);
        when(penaltyService.generateApprovalNoteAnnexure(request, 1L)).thenReturn(response);
        actual = controller.generateApprovalNoteAnnexure(1, request);
        assertEquals(response, actual.getBody());
        verify(penaltyService, times(2)).generateApprovalNoteAnnexure(request, 1L);

        // ONLY_ANNEXURE
        request.setOption(Option.ONLY_ANNEXURE);
        when(penaltyService.generateApprovalNoteAnnexure(request, 1L)).thenReturn(response);
        actual = controller.generateApprovalNoteAnnexure(1, request);
        assertEquals(response, actual.getBody());
        verify(penaltyService, times(3)).generateApprovalNoteAnnexure(request, 1L);
    }

    */
/**
     * Get details of member non submission records
     * @throws Exception
     *//*

    @Test
    public void downloadMemberNonSubmissionFile_success() throws Exception{
        NonSubmissionCountRequest request = new NonSubmissionCountRequest();
        NonSubmissionCountResponse response = new  NonSubmissionCountResponse(1,"MS");
        when(penaltyService.downloadMemberNonSubmissionFile(request)).thenReturn(response);

        ResponseEntity<NonSubmissionCountResponse> actual = controller.downloadMemberNonSubmissionFile(1, request);
        assertEquals(response, actual.getBody());
        verify(penaltyService).downloadMemberNonSubmissionFile(request);
    }

    */
/**
     * Get review comments
     * @throws Exception
     *//*

    @Test
    public void getReviewCommentsMTR_success() throws Exception{
        ReviewCommentsMTRDto response = new  ReviewCommentsMTRDto();
        when(penaltyService.getReviewCommentsMTR(2020,11)).thenReturn(response);

        ResponseEntity<ReviewCommentsMTRDto> actual = controller.getReviewCommentsMTR(1, 2020, 11);
        assertEquals(response, actual.getBody());
        verify(penaltyService).getReviewCommentsMTR(2020,11);
    }

    */
/**
     * Post review comments
     * @throws Exception
     *//*

    @Test
    public void postReviewCommentsMTR_success() throws Exception{
        ReviewCommentsMTRRequest request = new ReviewCommentsMTRRequest();
        CommonMessageDto response = new  CommonMessageDto(1,"MS");
        when(penaltyService.postReviewCommentsMTR(request, 1L)).thenReturn(response);

        ResponseEntity<CommonMessageDto> actual = controller.postReviewCommentsMTR(1, request);
        assertEquals(response, actual.getBody());
        verify(penaltyService).postReviewCommentsMTR(request, 1L);
    }

    */
/**
     * Get details of the Withdrawal status of the members in the selected date range
     * @throws Exception
     *//*

    @Test
    public void getWithdrawalMbrDtls_success() throws Exception{
        WithdrawalMbrDtlsResponse response = new WithdrawalMbrDtlsResponse();
        when(penaltyService.getWithdrawalMbrDtls(LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE),
                LocalDate.parse("2021-01-06",DateTimeFormatter.ISO_DATE),
                LocalDate.parse("2021-01-07",DateTimeFormatter.ISO_DATE))).thenReturn(response);

        ResponseEntity<WithdrawalMbrDtlsResponse> actual = controller.getWithdrawalMbrDtls(LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE), LocalDate.parse("2021-01-06",DateTimeFormatter.ISO_DATE), LocalDate.parse("2021-01-07",DateTimeFormatter.ISO_DATE));

        assertEquals(response, actual.getBody());
        verify(penaltyService).getWithdrawalMbrDtls(LocalDate.parse("2021-01-05",DateTimeFormatter.ISO_DATE),
                LocalDate.parse("2021-01-06",DateTimeFormatter.ISO_DATE),
                LocalDate.parse("2021-01-07",DateTimeFormatter.ISO_DATE));
    }


    */
/**
     * The checker approves the Approval Note and Annexure File for penalty submission cycle.
     * @throws Exception
     *//*

    @Test
    public void postPenaltyApproval_success() throws Exception{
        PenaltyApprovalRequest request = new PenaltyApprovalRequest();
        CommonMessageDto response = new CommonMessageDto(1,"MS");
        when(penaltyService.postPenaltyApproval(request,1)).thenReturn(response);

        ResponseEntity<CommonMessageDto> actual = controller.postPenaltyApproval(1, request);
        assertEquals(response, actual.getBody());
        verify(penaltyService).postPenaltyApproval(request,1);
    }

    */
/**
     *To update penalty if member have any dispute.
     * @throws Exception
     *//*

    @Test
    public void putPenaltyDispute_success() throws Exception{
        PenaltyDisputeRequest request = new PenaltyDisputeRequest();
        CommonMessageDto response = new CommonMessageDto(1,"MS");
        when(penaltyService.putPenaltyDispute(request,1)).thenReturn(response);

        ResponseEntity<CommonMessageDto> actual = controller.putPenaltyDispute(1, request);
        assertEquals(response, actual.getBody());
        verify(penaltyService).putPenaltyDispute(request,1);
    }

    */
/**
     *To list penalty letter details of every members.
     * @throws Exception
     *//*

    @Test
    public void getPenaltyLetterDetails_success() throws Exception{
        PenaltyDisputeRequest request = new PenaltyDisputeRequest();
        PenaltyLetterDetailsResponse response = new PenaltyLetterDetailsResponse();
        when(penaltyService.getPenaltyLetterDetails(2020,1,"T")).thenReturn(response);

        ResponseEntity<PenaltyLetterDetailsResponse> actual = controller.getPenaltyLetterDetails(1, 2020, 1, "T");
        assertEquals(response, actual.getBody());
        verify(penaltyService).getPenaltyLetterDetails(2020,1,"T");
    }

    */
/**
     *Generate penalty letters for members.
     * @throws Exception
     *//*

    @Test
    public void generatePenaltyLetters_success() throws Exception{
        PenaltyLettersRequest request = new PenaltyLettersRequest();
        PenaltyLetterDetailsResponse response = new PenaltyLetterDetailsResponse();
        when(penaltyService.generatePenaltyLetters(request,1)).thenReturn(response);

        ResponseEntity<PenaltyLetterDetailsResponse> actual = controller.generatePenaltyLetters(1, request);
        assertEquals(response, actual.getBody());
        verify(penaltyService).generatePenaltyLetters(request,1);
    }

    */
/**
     * Download Inspection Penalty File
     * @throws Exception
     *//*

    @Test
    public void downloadInspectionPenaltyFile_success() throws Exception{
        InspectionPenaltyFileRequest request = new InspectionPenaltyFileRequest();
        InspectionPenaltyFileResponse response = new InspectionPenaltyFileResponse(1,"MS");
        when(penaltyService.downloadInspectionPenaltyFile(request,1)).thenReturn(response);

        ResponseEntity<InspectionPenaltyFileResponse> actual = controller.downloadInspectionPenaltyFile(1, request);
        assertEquals(response, actual.getBody());
        verify(penaltyService).downloadInspectionPenaltyFile(request,1);
    }

    */
/**
     * Send penalty letters to members via e-mail.
     * @throws Exception
     *//*

    @Test
    public void sendPenaltyLetterEmail_success() throws Exception{
        PenaltyLetterEmailRequest request = new PenaltyLetterEmailRequest();
        CommonMessageDto response = new CommonMessageDto(1,"MS");
        when(penaltyService.sendPenaltyLetterEmail(request,1)).thenReturn(response);

        ResponseEntity<CommonMessageDto> actual = controller.sendPenaltyLetterEmail(1, request);
        assertEquals(response, actual.getBody());
        verify(penaltyService).sendPenaltyLetterEmail(request,1);
    }

    @Test
    public void validateMtrFile() {
        MockMultipartFile file = new MockMultipartFile("name", "hello".getBytes());
        controller.validateMtrFile(1L, file);
        verify(validateService).validateMtrFile(eq(file), eq(1L));
    }

    @Test
    public void submitMtrFile() {
        controller.submitMtrFile(1L, 1L);
        verify(validateService).submitMtrFile(1L, 1L);
    }

    @Test
    public void generatePenaltyLetters() {
        PenaltyLettersRequest request = new PenaltyLettersRequest();
        controller.generatePenaltyLetters(1L, request);
        verify(penaltyService).generatePenaltyLetters(request, 1L);
    }

    @Test
    public void downloadInspectionPenaltyFile() {
        InspectionPenaltyFileRequest request = new InspectionPenaltyFileRequest();
        controller.downloadInspectionPenaltyFile(1L, request);
        verify(penaltyService).downloadInspectionPenaltyFile(request, 1L);
    }

    @Test
    public void sendPenaltyLetterEmail() {
        PenaltyLetterEmailRequest request = new PenaltyLetterEmailRequest();
        controller.sendPenaltyLetterEmail(1L, request);
        verify(penaltyService).sendPenaltyLetterEmail(request, 1L);
    }

    @Test
    public void downloadMtrPenaltyFiles() {
        doReturn(new ByteArrayOutputStream(2)).when(penaltyService).downloadMtrPenaltyFiles(2020, 1, new ArrayList<>(), 1L);
        ResponseEntity<?> actual = controller.downloadMtrPenaltyFiles(1L, 2020, 1, new ArrayList<>());
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(new ArrayList<>(){{
            add("attachment; filename=\"mtr_penalty_files.zip\"");
        }}, actual.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(APPLICATION_OCTET_STREAM.toString(), actual.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        verify(penaltyService).downloadMtrPenaltyFiles(2020, 1, new ArrayList<>(), 1L);
    }

    @Test
    public void downloadMtrFile() throws IOException {

        File tmp = new File("tmp.txt");
        tmp.createNewFile();

        MTRDailyFile dailyFile = new MTRDailyFile();
        dailyFile.setDmsResIndex(tmp.getPath());
        dailyFile.setDmsDocIndex(tmp.getPath());
        dailyFile.setDailyFileName("file");

        doReturn(dailyFile).when(mtrReportService).getMtrFileById(1L);
        ResponseEntity<?> actual = controller.downloadMtrFile(1L, true);
        assertEquals(HttpStatus.OK, actual.getStatusCode());

        dailyFile.setDmsResIndex("resIndex");
        dailyFile.setDmsDocIndex("docIndex");
        dailyFile.setDailyFileName("file");

        doReturn(dailyFile).when(mtrReportService).getMtrFileById(1L);

        try {
            controller.downloadMtrFile(1L, true);
        } catch (BaseServiceException e) {
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getHttpStatus());
        }
    }

    @Test
    public void downloadMtrFileValidationResponse() {
        doReturn(new ByteArrayOutputStream(2)).when(validateService).downloadMtrFileValidationResponse(1L, 1L);
        ResponseEntity<?> actual = controller.downloadMtrFileValidationResponse(1L, 1L);
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(new ArrayList<>(){{
            add("attachment; filename=\"mtr_file_validation_response.zip\"");
        }}, actual.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(APPLICATION_OCTET_STREAM.toString(), actual.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        verify(validateService).downloadMtrFileValidationResponse(1L, 1L);
    }

    @Test
    public void downloadPenaltyLetters() {
        doReturn(new ByteArrayOutputStream(2)).when(penaltyService).downloadPenaltyLetters(1L);
        ResponseEntity<?> actual = controller.downloadPenaltyLetters(1L);
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertEquals(new ArrayList<>(){{
            add("attachment; filename=\"penalty_files.zip\"");
        }}, actual.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(APPLICATION_OCTET_STREAM.toString(), actual.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
        verify(penaltyService).downloadPenaltyLetters(1L);
    }

    @Test
    public void processDailyNonSubmissionRecords() {
        ResponseEntity<CommonMessageDto> actual = controller.processDailyNonSubmissionRecords();
        assertEquals(new CommonMessageDto(200, "done"), actual.getBody());
        verify(mtrReportService).processDailyNonSubmissionRecords();
    }

    @Test
    public void generateMarginTradingReport_success() {
        ResponseEntity<CommonMessageDto> actual = controller.generateMarginTradingReport();
        assertEquals(new CommonMessageDto(200, "done"), actual.getBody());
        verify(mtrReportService).generateMarginTradingReport();
    }

    @Test
    public void publishNSEWeb_success() {
        ResponseEntity<CommonMessageDto> actual = controller.publishNSEWeb();
        assertEquals(new CommonMessageDto(200, "done"), actual.getBody());
        verify(mtrReportService).publishNSEWeb();
    }

    @Test
    public void getSubmissionCycles_success() {
        List<SubmissionCycleDto> mock = mock(List.class);
        when(penaltyService.getSubmissionCycles()).thenReturn(mock);
        ResponseEntity<List<SubmissionCycleDto>> actual = controller.getSubmissionCycles();
        assertEquals(mock, actual.getBody());
        verify(penaltyService).getSubmissionCycles();
    }
    */
}
