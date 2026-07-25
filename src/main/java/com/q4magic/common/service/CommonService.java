package com.q4magic.common.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface CommonService {
    String formatDueLongZoned(String isoDate, ZoneId zoneId);

    Date convertStringToDate(String dateStr);

    String convertDateToString(Date date);

    Map<String, Object> uploadFiles(MultipartFile[] files, Integer LoginUserId, String folderName);

    String updateFileLocation(String image, Integer loginUserId, String tempFolderName, String newFolderName);

    void deleteDirectoryRecursively(File directory);

    boolean sendEmail(String toEmail, String subject, String body, boolean isHtml);

    boolean sendEmailWithAttachment(String toEmail, String subject, String body, boolean isHtml, String filename, String filepath);

    List<String> getDomainMX(String domain);

    String dbToDdTimeZone(String startDate, String timeZone) throws ParseException;

    String dbDateTime(String date) throws ParseException;

    Date convertDate(String date) throws ParseException;

    String convertEventTimeZoneToUserDB(String date, String fromTimeZone, String toTimeZone) throws ParseException;

    String convertDateTimeToTimeZone(String date) throws ParseException;

    String dbDate(String date) throws ParseException;

    String dateObjectToDbDateTime(Date date) throws ParseException;

    LocalDateTime covertLocalDateTime(String date) throws ParseException;

    Map<String, Integer> getSplitDate(String sourcedate) throws ParseException;

    String convertDateTimeObjectToDBDateTime(String date) throws ParseException;

    LocalDateTime changeDate(LocalDateTime eventDate, int day);

    String getDateTime(int year, Month month, int day, int week);

    String getLastWeek(int year, Month month, int day) throws ParseException;

    String convertEventTimeZoneToUser(String date, String fromTimeZone, String toTimeZone) throws ParseException;

    String dateObjectToDisplayDate(Date date) throws ParseException;

    int convertDateTimeObjectToReturnYear(String date) throws ParseException;

    int convertDateTimeObjectToReturnMonth(String date) throws ParseException;

    Map<String, Integer> getSplitDateOnlyWeek(String sourcedate) throws ParseException;

    String getDate(String day, int week, int month, int year) throws ParseException;

    int dateCampare(String dateOne, String dateTwo) throws ParseException;

    String dateObjectToDbDate(Date date) throws ParseException;

    Date addOneDays(Date date, int hours);

    Date convertDateOnly(String date) throws ParseException;

    String displayDateTime(String date) throws ParseException;

    String convertTimeZoneToDbDate(String date) throws ParseException;

    String changeTimeZoneName(String name);

    String nl2br(String text);

    String dbDateToDisplayDateTime(String date) throws ParseException;

    String dayName(String date) throws ParseException;

    String lastCharaters(String input, int no);

    String remainingTime(int minutes, String firstTime, String secondTime) throws ParseException;

    List<String> slotList(int minutes, String firstTime, String secondTime) throws ParseException;

    String currentTime(String timeZone) throws ParseException;

    boolean checkBigFirstTime(String firstTime, String secondTime) throws ParseException;

    String displayDbDateTimeToTime(String date) throws ParseException;

    List<String> removeBookSlot(int minutes, List<String> slotList, String startTime, String endTime);

    String convertDateTimeToTime(String date) throws ParseException;

    String br2nl(String html);

    String ucWords(String str);
}
