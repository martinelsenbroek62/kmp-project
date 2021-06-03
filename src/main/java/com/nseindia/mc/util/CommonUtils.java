package com.nseindia.mc.util;

import com.nseindia.mc.exception.BaseServiceException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CommonUtils {
  private static final SimpleDateFormat FULL_MONTH = new SimpleDateFormat("MMMM yyyy", Locale.US);
  private static final DateTimeFormatter FULL_MONTH_NEW = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US);
  private static final SimpleDateFormat ABBR_MONTH = new SimpleDateFormat("MMM yyyy", Locale.US);
  private static final DateTimeFormatter ABBR_MONTH_NEW = DateTimeFormatter.ofPattern("MMM yyyy", Locale.US);
  private static final DateTimeFormatter NUMBERED_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter FULL_MONTH_DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
  private static final DateTimeFormatter REPORT_FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  
  public static final String getFilePath(String resFileDirectory, String fileName) {
	  File path = new File(System.getProperty("user.dir")+ File.separator +resFileDirectory);
	  if(!path.isDirectory()) path.mkdirs();
	  
	  return path+ File.separator+ fileName;
  }

  /**
   * Get previous quarter name.
   *
   * @return the previous quarter name
   */
  public static final String previousQuarterName() {
    LocalDate now = LocalDate.now();
    int previousQuarter = now.get(IsoFields.QUARTER_OF_YEAR) - 1;
    int year = now.getYear();
    if (previousQuarter == 0) {
      year--;
      previousQuarter = 4;
    }
    return String.format("%dQ%d", year, previousQuarter);
  }

  public static final String previousQuarter(final String quarter) {
    String[] yearAndQuarter = quarter.split("Q");
    if (yearAndQuarter[1].equals("1")) {
      return String.format("%dQ%d", Integer.parseInt(yearAndQuarter[0]) - 1, 4);
    }
    return String.format("%sQ%d", yearAndQuarter[0], Integer.parseInt(yearAndQuarter[1]) - 1);
  }

  /**
   * Convert string flag to boolean.
   *
   * @param flag
   * @return true if flag = 'Y' or 'y'
   */
  public static final boolean convertFlag(final String flag) {
    return StringUtils.containsIgnoreCase(flag, "Y");
  }

  /**
   * Convert boolean to string flag.
   *
   * @param flag
   * @return 'Y' if flag is true
   */
  public static final String convertFlag(final boolean flag) {
    if (flag) {
      return "Y";
    }
    return "N";
  }

  public static final String convertToAccept(final String flag) {
    if ("A".equalsIgnoreCase(flag)) {
      return "accepted";
    } else if ("R".equalsIgnoreCase(flag)) {
      return "rejected";
    }
    return null;
  }

  public static final String convertToAcceptFlag(final String accept) {
    if ("accepted".equalsIgnoreCase(accept)) {
      return "A";
    } else if ("rejected".equalsIgnoreCase(accept)) {
      return "R";
    }
    return null;
  }

  /**
   * Get month and year for a date.
   *
   * @param date
   * @return month and year string
   */
  public static final String getMonthAndYear(final Date date) {
    return FULL_MONTH.format(date);
  }

  public static final String getFullMonthAndYear(final LocalDate date) {
    return date.format(FULL_MONTH_NEW);
  }

  public static final String getFullMonthAndYear(final int year, final int month) {
    LocalDate date = LocalDate.of(year, month, 1);
    return date.format(FULL_MONTH_NEW);
  }

  public static final String getAbbrMonthAndYear(final int year, final int month) {
    LocalDate date = LocalDate.of(year, month, 1);
    return date.format(ABBR_MONTH_NEW);
  }

  public static final String getAbbrMonthAndYear(final LocalDate date) {
    return date.format(ABBR_MONTH_NEW);
  }

  public static final LocalDate getDate(String dateStr) {
    return LocalDate.parse(dateStr, NUMBERED_DATE_FORMAT);
  }

  public static final String getNumberedDateStr(LocalDate date) {
    return date.format(NUMBERED_DATE_FORMAT);
  }

  public static final String getDatabaseDateStr(LocalDate date) {
    return date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
  }
  
  public static final String getDatabaseDateStr(LocalDateTime date) {
	  return date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
  }

  public static final String getFullMonthDateStr(LocalDate date) {
    return date.format(FULL_MONTH_DATE_FORMAT);
  }

  public static final String getReportFileDateStr(LocalDate date) {
    return date.format(REPORT_FILE_DATE_FORMAT);
  }

  public static final String getParamMapValue(Map<String, String[]> paramMap, String key) {
    String[] values = paramMap.get(key);
    if (values != null && values.length > 0) {
      return values[0];
    }

    return null;
  }

  public static final Long getParamMapValueAsLong(Map<String, String[]> paramMap, String key) {
    String value = getParamMapValue(paramMap, key);

    return value == null ? null : Long.parseLong(value);
  }

  /**
   * Get a range string for two date.
   *
   * @param start
   * @param end
   * @return the range string
   */
  public static final String getDateRangeString(final Date start, final Date end) {
    return String.join("-", ABBR_MONTH.format(start), ABBR_MONTH.format(end));
  }

  /**
   * To get the rules file path from classpath.
   *
   * @param fileName name of the rules file
   * @return path of the decision table
   */
  public static String getRulesFilePath(String fileName) {
    try {
      return ResourceUtils.getFile("classpath:./rules/" + fileName)
          .getPath()
          .replaceAll("\\\\", "/");
    } catch (FileNotFoundException e) {
    }
    return "";
  }

  public static final void copyPropertiesWithoutNull(Object src, Object target) {
    BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
  }

  private static String[] getNullPropertyNames(Object source) {
    final BeanWrapper src = new BeanWrapperImpl(source);
    return Arrays.stream(src.getPropertyDescriptors())
        .filter(pd -> src.getPropertyValue(pd.getName()) == null)
        .map(pd -> pd.getName())
        .distinct()
        .toArray(String[]::new);
  }

  public static List<LocalDate> getBusinessDaysForYearMonth(int year, int month) {
    LocalDate firstCalendarDay = LocalDate.of(year, month, 1);
    LocalDate lastCalendarDay = firstCalendarDay.with(TemporalAdjusters.lastDayOfMonth());
    List<LocalDate> businessDates = getBusinessDaysInclusive(firstCalendarDay, lastCalendarDay);
    return businessDates;
  }

  public static List<LocalDate> getBusinessDaysInclusive(LocalDate start, LocalDate end) {
    List<LocalDate> res = new ArrayList<>();
    while (start.isBefore(end) || start.isEqual(end)) {
      if (!start.getDayOfWeek().equals(DayOfWeek.SATURDAY)
          && !start.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
        res.add(start);
      }
      start = start.plusDays(1);
    }
    return res;
  }

  public static LocalDate getLastBusinessDay(LocalDate today) {
    LocalDate res = today.plusDays(-1);
    while (res.getDayOfWeek().equals(DayOfWeek.SATURDAY)
        || res.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
      res = res.plusDays(-1);
    }
    return res;
  }

  public static boolean isBusinessDay(LocalDate today) {
    return !today.getDayOfWeek().equals(DayOfWeek.SATURDAY) && !today.getDayOfWeek().equals(DayOfWeek.SUNDAY);
  }

  public static LocalDate getFirstBusinessDayOfMonth(LocalDate date) {
    date = date.with(TemporalAdjusters.firstDayOfMonth());
    while (date.getDayOfWeek().equals(DayOfWeek.SATURDAY) || date.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
      date = date.plusDays(1);
    }
    return date;
  }

  public static String getFirstBusinessDayOfMonthISOStr(LocalDate date) {
    return getDatabaseDateStr(getFirstBusinessDayOfMonth(date));
  }

  public static LocalDate getFirstBusinessDayOfMonth(String year, String month) {
    LocalDate date = LocalDate.parse(year + "-" + month + "-01");
    return getFirstBusinessDayOfMonth(date);
  }

  public static String getLastBusinessDayOfMonthISOStr(LocalDate date) {
      return getDatabaseDateStr(getLastBusinessDayOfMonth(date));
  }
  public static LocalDate getLastBusinessDayOfMonth(LocalDate date) {
    date = date.with(TemporalAdjusters.lastDayOfMonth());
    while (date.getDayOfWeek().equals(DayOfWeek.SATURDAY) || date.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
      date = date.minusDays(1);
    }
    return date;
  }
  public static LocalDate getLastBusinessDayOfMonth(String year, String month) {
    LocalDate date = LocalDate.parse(year + "-" + month + "-01").with(TemporalAdjusters.lastDayOfMonth());
    return getLastBusinessDayOfMonth(date);
  }

  /**
   * Checks if the current day is the first weekday of month.
   *
   * @return true if it is, else false
   */
  public static Boolean isFirstWeekDayOfMonth() {
    Calendar cal = Calendar.getInstance();
    int currentDate = cal.get(Calendar.DAY_OF_MONTH);
    for (int i = 0; i < 3; i++) {
      cal.set(Calendar.DAY_OF_MONTH, i + 1);
      int weekDay = cal.get(Calendar.DAY_OF_WEEK);
      if (weekDay >= 2 && weekDay <= 6) {
        return currentDate == (i + 1);
      }
    }
    return false;
  }

  /**
   * Add file to zip stream.
   *
   * @param filePath the file path.
   * @param zipOut the zip stream.
   * @return false if file not exist, true if everything works fine
   * @throws IOException if any IO exception occurs.
   */
  public static boolean addFileToZip(String fileName, String filePath, ZipOutputStream zipOut) throws IOException {
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(filePath);
    } catch (FileNotFoundException e) {
      // file is missing
      return false;
    }

    zipOut.putNextEntry(new ZipEntry(fileName));

    byte[] bytes = new byte[1024];
    int length;
    while ((length = fis.read(bytes)) >= 0) {
      zipOut.write(bytes, 0, length);
    }
    fis.close();
    zipOut.closeEntry();
    return true;
  }

  public static <T> String objectToJson(T object) {
    try {
      return OBJECT_MAPPER.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> List<T> jsonToObjectList(String json) {
    if (json == null) {
      return new ArrayList<>();
    }
    try {
      return OBJECT_MAPPER.readValue(json, new TypeReference<>() {});
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Handles the errors when external services are unavailable
   * 
   * @param response The response from the service
   * @param errorMessage The error message in case of failure
   * @return The response body
   */
  public static Object handleServiceErrors(ResponseEntity<?> response, String errorMessage) {
    if(response.getStatusCode() == HttpStatus.OK) {
      return response.getBody();
    } else {
      throw new BaseServiceException(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
