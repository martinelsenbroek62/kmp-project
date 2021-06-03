package com.nseindia.mc.controller.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.text.StringSubstitutor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MTRFileValidationResult {
	
	private List<LocalDate> missedDates = new ArrayList<>();
	
	private int maxBatchNo;
	
	private List<MTRDailyFileErrorDto> dailyFileErrors = new ArrayList<>();
	private List<LineValidationResult> controlRecords = new ArrayList<>();
	private List<LineValidationResult> summaryRecords = new ArrayList<>();
	private List<LineValidationResult> detailRecords = new ArrayList<>();
	private List<LineValidationResult> collateralSummaryRecords = new ArrayList<>();
	private List<LineValidationResult> collateralScripsDetailRecords = new ArrayList<>();
	
	private LineValidationResult declaration;
	private String responseFileName;
	private String responseFilePath;
	private String errorCodeFileName;
	private String errorCodeFilePath;
	private boolean failed;
	private List<MTRValidateRecordSummary> recordSummaryList;

	public void generateErrorMessage(final Map<String, String> messageTemplates) {
		dailyFileErrors.forEach(errorDto -> {
			String template = messageTemplates.getOrDefault(errorDto.getErrorCode(), errorDto.getErrorCode());
			errorDto.setErrorMessage(new StringSubstitutor(errorDto.getParameters(), "<%", ">").replace(template));
		});
		generateErrorMessage(controlRecords, messageTemplates);
		generateErrorMessage(summaryRecords, messageTemplates);
		generateErrorMessage(detailRecords, messageTemplates);
		generateErrorMessage(collateralSummaryRecords, messageTemplates);
		generateErrorMessage(collateralScripsDetailRecords, messageTemplates);
	}

	private static void generateErrorMessage(final List<LineValidationResult> lrs, final Map<String, String> messageTemplates) {
		lrs.forEach(lr -> lr.getErrors().forEach(e -> {
			String template = messageTemplates.getOrDefault(e.getCode(), e.getCode());
			e.setMessage(new StringSubstitutor(e.getParameters(), "<%", ">").replace(template));
		}));
	}
	
	public static String generateErrorMessage(final MTRDailyFileErrorDto dailyFileError, final Map<String, String> messageTemplates) {
		String template = messageTemplates.getOrDefault(dailyFileError.getErrorCode(), dailyFileError.getErrorCode());
		return new StringSubstitutor(dailyFileError.getParameters(), "<%", ">").replace(template);
	}
}
