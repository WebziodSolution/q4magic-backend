package com.q4magic.common.googleCalendar.service;

import com.q4magic.common.dto.googleCalendar.GoogleAuthUrlResponse;
import com.q4magic.common.dto.googleCalendar.GoogleCalendarDto;
import com.q4magic.common.dto.googleCalendar.GoogleCalendarEventDto;
import org.json.JSONObject;

import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface GoogleCalendarService {

    GoogleAuthUrlResponse getAuthorizationUrl(Integer customerId);

    void handleOAuthCallback(String code, String state);

    void getEmail(Integer userId);

    String refreshToken(String refreshToken);

    List<GoogleCalendarEventDto> listEvents(Integer customerId,
            Instant timeMin,
            Instant timeMax);

    Map<String, Object> getUserTimezone(Integer cusId);

    String getUserCalendarTimezone(String accessToken);

    Map<String, Object> saveEvent(Integer cusId, GoogleCalendarDto googleCalendarDto);

    void deleteEvent(Integer cusId, String eventId);

    JSONObject getGoogleLastUpdatedTime(String accessToken);

    List<Integer> downloadGoogleCalendarToLocalData(Integer cusId, List<Integer> calIdList, String accessToken,
            String currentDate);

    void uploadLocalDataToGoogleCalendar(Integer cusId, String googleTimeZone, List<Integer> calIdList,
            String currentDate) throws ParseException;

    Map<String, Object> revoke(Integer userId);
}
