package com.q4magic.common.outlookCalendar.service;

import com.q4magic.common.dto.outlookCalendar.OutlookCalendarDto;
import com.q4magic.common.models.Customers;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface OutlookCalendarService {
    String authorize() throws UnsupportedEncodingException;

    String refreshToken(String outlookCalendarRefreshToken, Customers customers);

    String getUserCalendarTimezone(String outlookAccessToken);

    void saveToken(Integer cusId, String code);

    String getEmail(Integer cusId);

    Map<String, Object> saveEvent(Integer cusId, OutlookCalendarDto outlookCalendarDto);

    Map<String, Object> revoke(Integer cusId);

    List<Integer> downloadOutlookCalendarToLocalData(Integer cusId, List<Integer> outlookCalIdList, String outlookAccessToken, String currentDate) throws ParseException;

    void uploadLocalDataToOutlookCalendar(Integer cusId, String outlookTimeZone, List<Integer> outlookCalIdList, String currentDate);

    void deleteEvent(Integer cusId, String eventId);

    JSONObject getOutlookLastUpdatedTime(String outlookAccessToken);
}
