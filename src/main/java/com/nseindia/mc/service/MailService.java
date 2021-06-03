package com.nseindia.mc.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.nseindia.mc.config.CutoffOpenDataEmailConfig;
import com.nseindia.mc.config.CutoffOpenNilEmailConfig;
import com.nseindia.mc.config.DataSubmissionEmailConfig;
import com.nseindia.mc.config.FifteenthDayAutoEmailConfig;
import com.nseindia.mc.config.FirstWeekdayEmailAIMLConfig;
import com.nseindia.mc.config.FirstWeekdayEmailConfig;
import com.nseindia.mc.config.FirstWeekdayEmailNilAimlConfig;
import com.nseindia.mc.config.IBESReportEmailConfig;
import com.nseindia.mc.config.NilSubmissionEmailConfig;
import com.nseindia.mc.config.NotificationConfig;
import com.nseindia.mc.config.PenaltyLetterEmailConfig;
import com.nseindia.mc.config.PenaltyReversalLetterEmailConfig;
import com.nseindia.mc.config.PublishMrgReportEmailConfig;
import com.nseindia.mc.config.SeventhDayAutoEmailConfig;
import com.nseindia.mc.config.SignoffEmailConfig;
import com.nseindia.mc.controller.dto.EmailBody;
import com.nseindia.mc.controller.dto.EmailRequestVO;
import com.nseindia.mc.controller.dto.EmailResponse;
import com.nseindia.mc.exception.BaseServiceException;
import com.nseindia.mc.model.PenaltyLetterType;
import com.nseindia.mc.proxy.NotificationServiceProxy;
import com.nseindia.mc.util.CommonUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MailService {
  @Autowired private DataSubmissionEmailConfig dataSubmissionEmailConfig;
  @Autowired private NilSubmissionEmailConfig nilSubmissionEmailConfig;
  @Autowired private IBESReportEmailConfig ibesReportEmailConfig;
  @Autowired private FirstWeekdayEmailAIMLConfig firstWeekdayEmailAIMLConfig;
  @Autowired private SeventhDayAutoEmailConfig seventhDayAutoEmailConfig;
  @Autowired private FifteenthDayAutoEmailConfig fifteenthDayAutoEmailConfig;
  @Autowired private FirstWeekdayEmailNilAimlConfig firstWeekdayEmailNilAimlConfig;
  @Autowired private NotificationServiceProxy notificationServiceProxy;
  @Autowired private NotificationConfig notificationConfig;

  @Autowired private CutoffOpenNilEmailConfig cutoffOpenNilEmailConfig;
  @Autowired private CutoffOpenDataEmailConfig cutoffOpenDataEmailConfig;
  @Autowired private FirstWeekdayEmailConfig firstWeekdayEmailConfig;
  @Autowired private SignoffEmailConfig signoffEmailConfig;
  @Autowired private PublishMrgReportEmailConfig publishMrgReportEmailConfig;
  @Autowired private PenaltyLetterEmailConfig penaltyLetterEmailConfig;
  @Autowired private PenaltyReversalLetterEmailConfig penaltyReversalLetterEmailConfig;

  /**
   * Send after successful data submission email.
   *
   * @param memberEmail
   * @param quarterStart
   * @param quarterEnd
   */
  @Async
  public void sendEmailAfterSuccessfulDataSubmission(
      String memberEmail, Date quarterStart, Date quarterEnd) {
    try {
      List<String> to = new ArrayList<>();
      to.add(memberEmail);

      this.emailNotification(
          EmailBody.builder()
              .bulkId(notificationConfig.getBulkId())
              .templateId(notificationConfig.getTemplateId())
              .from(dataSubmissionEmailConfig.getFrom())
              .intermediateReport(notificationConfig.getIntermediateReport())
              .subject(
                  String.format(
                      dataSubmissionEmailConfig.getSubject(),
                      CommonUtils.getMonthAndYear(quarterEnd)))
              .html(
                  String.format(
                      dataSubmissionEmailConfig.getBody(),
                      CommonUtils.getDateRangeString(quarterStart, quarterEnd)))
              .to(to)
              .build());
    } catch (Exception e) {
      log.error("send email after successful data submission fail", e);
    }
  }

  /**
   * Send after successful nil submission email.
   *
   * @param memberEmail
   * @param quarterStart
   * @param quarterEnd
   */
  @Async
  public void sendEmailAfterSuccessfulNilSubmission(
      String memberEmail, Date quarterStart, Date quarterEnd) {
    try {
      List<String> to = new ArrayList<>();
      to.add(memberEmail);

      this.emailNotification(
          EmailBody.builder()
              .bulkId(notificationConfig.getBulkId())
              .templateId(notificationConfig.getTemplateId())
              .from(nilSubmissionEmailConfig.getFrom())
              .intermediateReport(notificationConfig.getIntermediateReport())
              .subject(
                  String.format(
                      nilSubmissionEmailConfig.getSubject(),
                      CommonUtils.getMonthAndYear(quarterEnd)))
              .html(
                  String.format(
                      nilSubmissionEmailConfig.getBody(),
                      CommonUtils.getDateRangeString(quarterStart, quarterEnd)))
              .to(to)
              .build());
    } catch (Exception e) {
      log.error("send email after successful nil submission fail", e);
    }
  }

  /**
   * Send report to ibes.
   *
   * @param quarter
   * @param report
   */
  public void sendIBESReportEmail(String quarter, ByteArrayResource report) {
    try {
      List<String> to = new ArrayList<>();
      to.add(ibesReportEmailConfig.getTo());

      String fileName = quarter.concat(ibesReportEmailConfig.getFileExtension());
      InputStream inputStream = new ByteArrayInputStream(report.getByteArray());
      MultipartFile multipartFile =
          new MockMultipartFile(fileName, fileName, "multipart/form-data", inputStream);

      this.emailNotificationWithAttachment(
          EmailBody.builder()
              .bulkId(notificationConfig.getBulkId())
              .from(ibesReportEmailConfig.getFrom())
              .intermediateReport(notificationConfig.getIntermediateReport())
              .subject(String.format(ibesReportEmailConfig.getSubject(), quarter))
              .text(ibesReportEmailConfig.getBody())
              .replyTo(notificationConfig.getReplyTo())
              .to(to)
              .build(),
          multipartFile);

    } catch (Exception e) {
      log.error("send email after successful IBES Report fail", e);
      throw new BaseServiceException(
          "send email after successful IBES Report fail", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Notification Email Functionality
   *
   * @param requestEmailBody Email Body
   * @return Email Response
   */
  protected List<EmailResponse> emailNotification(EmailBody requestEmailBody) {
    try {
      EmailRequestVO emailRequest =
          EmailRequestVO.builder()
              .invocationType(notificationConfig.getInvocationType())
              .funcOrServiceID(notificationConfig.getFuncOrServiceID())
              .funcOrServiceName(notificationConfig.getFuncOrServiceName())
              .requestEmailBody(requestEmailBody)
              .build();
      return notificationServiceProxy.sendEmail(emailRequest);
    } catch (Exception e) {
      log.error("Exception occurred while sending email ", e);
      throw new BaseServiceException(
          "Exception occurred while sending email ", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Notification Email Functionality with attachment
   *
   * @param requestEmailBody Email Body
   * @param multipartFile Email attachment
   * @return true if the email has been sent successfully
   */
  protected boolean emailNotificationWithAttachment(
      EmailBody requestEmailBody, MultipartFile multipartFile) {
    EmailRequestVO emailRequest =
        EmailRequestVO.builder()
            .invocationType(notificationConfig.getInvocationType())
            .funcOrServiceID(notificationConfig.getFuncOrServiceID())
            .funcOrServiceName(notificationConfig.getFuncOrServiceName())
            .requestEmailBody(requestEmailBody)
            .build();

    List<EmailResponse> emailResponses = null;
    try {
      emailResponses =
          notificationServiceProxy.sendOTPViaEmailWithAttachment(emailRequest, multipartFile);
    } catch (Exception e) {
      log.error("Exception occurred while sending email with attachment ", e);
      throw new BaseServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    if (!CollectionUtils.isEmpty(emailResponses)) {
      EmailResponse emailResponse = emailResponses.get(0);
      if (StringUtils.isEmpty(emailResponse.getErrorCode())
          && StringUtils.isEmpty(emailResponse.getErrorMessage())) {
        // Sent successfully
        return true;
      }

      log.error(
          String.format(
              "Failed to send email with attachment: %s", emailResponse.getErrorMessage()));
      return false;
    }

    throw new BaseServiceException(
        "Failed to send email with attachment: no response from notification-service",
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  public void sendMTRDailyFileAutoEmail(
      String toEmail, String ccEmail, String subject, String body) {
    try {
      List<String> to = new ArrayList<>();
      to.add(toEmail);

      this.emailNotification(
          EmailBody.builder()
              .bulkId(notificationConfig.getBulkId())
              .templateId(notificationConfig.getTemplateId())
              .from(dataSubmissionEmailConfig.getFrom())
              .intermediateReport(notificationConfig.getIntermediateReport())
              .subject(subject)
              .html(body)
              .to(to)
              .build());
    } catch (Exception e) {
      log.error("send MTR daily file auto email fail", e);
    }
  }

  /**
   * Send email when cutoff window open.
   *
   * @param toEmail the to email.
   */
  public void sendCutOffWindowsAutoEmail(
      String toEmail, String uploadCutOffEndTime, boolean isNilSubmission) {
    try {
      String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
      List<String> to = new ArrayList<>();
      to.add(toEmail);

      this.emailNotification(
          EmailBody.builder()
              .bulkId(notificationConfig.getBulkId())
              .templateId(notificationConfig.getTemplateId())
              .from(
                  isNilSubmission
                      ? cutoffOpenNilEmailConfig.getFrom()
                      : cutoffOpenDataEmailConfig.getFrom())
              .intermediateReport(notificationConfig.getIntermediateReport())
              .subject(
                  isNilSubmission
                      ? String.format(cutoffOpenNilEmailConfig.getSubject(), today)
                      : String.format(cutoffOpenDataEmailConfig.getSubject(), today))
              .html(
                  isNilSubmission
                      ? String.format(
                          cutoffOpenNilEmailConfig.getBody(), today, uploadCutOffEndTime)
                      : String.format(cutoffOpenDataEmailConfig.getBody(), uploadCutOffEndTime))
              .to(to)
              .build());
    } catch (Exception e) {
      log.error("send auto email when cutoff windows open fail", e);
    }
  }

  /** Send auto email when first weekday of month. */
  public void sendFirstWeekDayOfMonthAutoEmail() {

    LocalDate today = LocalDate.now();
    LocalDate lastMonth = today.minusMonths(1);
    sendFirstWeekDayOfMonthAutoEmail(lastMonth.getYear(), lastMonth.getMonthValue());
  }

  public void sendFirstWeekDayOfMonthAutoEmail(int year, int month) {

    try {
      List<String> to = new ArrayList<>();
      to.add(firstWeekdayEmailConfig.getTo());

      this.emailNotification(
          EmailBody.builder()
              .bulkId(notificationConfig.getBulkId())
              .templateId(notificationConfig.getTemplateId())
              .from(firstWeekdayEmailConfig.getFrom())
              .intermediateReport(notificationConfig.getIntermediateReport())
              .subject(firstWeekdayEmailConfig.getSubject())
              .html(
                  String.format(
                      firstWeekdayEmailConfig.getBody(), CommonUtils.getAbbrMonthAndYear(year, month)))
              .to(to)
              .build());
    } catch (Exception e) {
      log.error("send auto email when cutoff windows open fail", e);
    }
  }

  /** Send auto email when first weekday of month. */
  public void sendFirstWeekDayOfMonthAIMLAutoEmail(String email, Date quarterEnd) {

    try {
      List<String> to = new ArrayList<>();
      to.add(email);

      this.emailNotification(
          EmailBody.builder()
              .bulkId(notificationConfig.getBulkId())
              .templateId(notificationConfig.getTemplateId())
              .from(firstWeekdayEmailAIMLConfig.getFrom())
              .intermediateReport(notificationConfig.getIntermediateReport())
              .subject(
                  String.format(
                      firstWeekdayEmailAIMLConfig.getSubject(),
                      CommonUtils.getMonthAndYear(quarterEnd)))
              .html(
                  String.format(
                      firstWeekdayEmailAIMLConfig.getBody(),
                      LocalDate.now().minusMonths(1).getMonth()))
              .to(to)
              .build());
    } catch (Exception e) {
      log.error("send auto email when cutoff windows open fail", e);
    }
  }

  /** Send auto email when first weekday of month. */
  public void sendSeventhDayOfMonthAIMLAutoEmail(String email, Date quarterEnd) {

    try {
      List<String> to = new ArrayList<>();
      to.add(email);

      this.emailNotification(
          EmailBody.builder()
              .bulkId(notificationConfig.getBulkId())
              .templateId(notificationConfig.getTemplateId())
              .from(seventhDayAutoEmailConfig.getFrom())
              .intermediateReport(notificationConfig.getIntermediateReport())
              .subject(
                  String.format(
                      seventhDayAutoEmailConfig.getSubject(),
                      CommonUtils.getMonthAndYear(quarterEnd)))
              .html(
                  String.format(
                      seventhDayAutoEmailConfig.getBody(),
                      LocalDate.now().minusMonths(1).getMonth()))
              .to(to)
              .build());
    } catch (Exception e) {
      log.error("send auto email when cutoff windows open fail", e);
    }
  }

  /** Send auto email when first weekday of month. */
  public void sendFifteenthDayOfMonthAIMLAutoEmail(String email, Date quarterEnd) {

    try {
      List<String> to = new ArrayList<>();
      to.add(email);

      this.emailNotification(
          EmailBody.builder()
              .bulkId(notificationConfig.getBulkId())
              .templateId(notificationConfig.getTemplateId())
              .from(fifteenthDayAutoEmailConfig.getFrom())
              .intermediateReport(notificationConfig.getIntermediateReport())
              .subject(
                  String.format(
                      fifteenthDayAutoEmailConfig.getSubject(),
                      CommonUtils.getMonthAndYear(quarterEnd)))
              .html(
                  String.format(
                      fifteenthDayAutoEmailConfig.getBody(),
                      LocalDate.now().minusMonths(1).getMonth()))
              .to(to)
              .build());
    } catch (Exception e) {
      log.error("send auto email when cutoff windows open fail", e);
    }
  }

  /** Send auto email when first weekday of month. */
  public void sendFirstNilWeekDayOfMonthAIMLAutoEmail(String email, Date quarterEnd) {

    try {
      List<String> to = new ArrayList<>();
      to.add(email);

      this.emailNotification(
          EmailBody.builder()
              .bulkId(notificationConfig.getBulkId())
              .templateId(notificationConfig.getTemplateId())
              .from(firstWeekdayEmailNilAimlConfig.getFrom())
              .intermediateReport(notificationConfig.getIntermediateReport())
              .subject(
                  String.format(
                      firstWeekdayEmailNilAimlConfig.getSubject(),
                      CommonUtils.getMonthAndYear(quarterEnd)))
              .html(
                  String.format(
                      firstWeekdayEmailNilAimlConfig.getBody(),
                      LocalDate.now().minusMonths(1).getMonth()))
              .to(to)
              .build());
    } catch (Exception e) {
      log.error("send auto email when cutoff windows open fail", e);
    }
  }

  /** Send daily sign Off Email */
  public void sendDailySignOffEmail() {
    LocalDate currentReportingDate = LocalDate.now();
    sendDailySignOffEmail(currentReportingDate);
  }

  public void sendDailySignOffEmail(LocalDate currentReportingDate) {

    try {
      List<String> to = new ArrayList<>();
      to.add(signoffEmailConfig.getTo());

      this.emailNotification(
          EmailBody.builder()
              .bulkId(notificationConfig.getBulkId())
              .templateId(notificationConfig.getTemplateId())
              .from(signoffEmailConfig.getFrom())
              .intermediateReport(notificationConfig.getIntermediateReport())
              .subject(signoffEmailConfig.getSubject())
              .html(
                  String.format(
                      signoffEmailConfig.getBody(),
                      currentReportingDate.format(DateTimeFormatter.ISO_DATE)))
              .to(to)
              .build());
    } catch (Exception e) {
      log.error("send auto email when sign off fail", e);
    }
  }

  /**
   * Send publish To NSE web Email
   *
   * @param attachment
   * @return true if sending email successfully
   */
  public boolean sendPublishToNSEWebEmail(File attachment) {
    try {
      List<String> to = new ArrayList<>();
      to.add(publishMrgReportEmailConfig.getTo());

      InputStream inputStream = new FileInputStream(attachment);
      MultipartFile multipartFile =
          new MockMultipartFile(
              attachment.getName(), attachment.getName(), "multipart/form-data", inputStream);

      return this.emailNotificationWithAttachment(
          EmailBody.builder()
              .bulkId(notificationConfig.getBulkId())
              .from(publishMrgReportEmailConfig.getFrom())
              .intermediateReport(notificationConfig.getIntermediateReport())
              .subject(publishMrgReportEmailConfig.getSubject())
              .text(
                  String.format(
                      publishMrgReportEmailConfig.getBody(),
                      LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)))
              .replyTo(publishMrgReportEmailConfig.getFrom())
              .to(to)
              .build(),
          multipartFile);
    } catch (Exception ex) {
      log.error("send auto email when mrg trading report published fail", ex);

      // This method is expected to be run in cron jon, so just return false quietly
      return false;
    }
  }

  public boolean sendPenaltyLetterEmail(
      String toEmail, PenaltyLetterType penaltyLetterType, String monthStr, File attachment) {
    List<String> to = new ArrayList<>();
    to.add(toEmail);

    try {
      InputStream inputStream = new FileInputStream(attachment);
      MultipartFile multipartFile =
          new MockMultipartFile(
              attachment.getName(), attachment.getName(), "multipart/form-data", inputStream);

      EmailBody emailBody =
          EmailBody.builder()
              .bulkId(notificationConfig.getBulkId())
              .intermediateReport(notificationConfig.getIntermediateReport())
              .replyTo(publishMrgReportEmailConfig.getFrom())
              .to(to)
              .build();

      switch (penaltyLetterType) {
        case PENALTY_LETTER:
          emailBody.setFrom(penaltyLetterEmailConfig.getFrom());
          emailBody.setSubject(String.format(penaltyLetterEmailConfig.getSubject(), monthStr));
          emailBody.setHtml(String.format(penaltyLetterEmailConfig.getBody(), monthStr));
          break;
        case PENALTY_REVERSAL_LETTER:
          emailBody.setFrom(penaltyReversalLetterEmailConfig.getFrom());
          emailBody.setSubject(
              String.format(penaltyReversalLetterEmailConfig.getSubject(), monthStr));
          emailBody.setHtml(String.format(penaltyReversalLetterEmailConfig.getBody(), monthStr));
          break;
      }

      return this.emailNotificationWithAttachment(emailBody, multipartFile);
    } catch (IOException ex) {
      throw new BaseServiceException(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
