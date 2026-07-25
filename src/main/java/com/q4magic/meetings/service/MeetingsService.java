package com.q4magic.meetings.service;

import com.q4magic.common.dto.MeetingsDto;

import java.util.List;

public interface MeetingsService {
    List<MeetingsDto> getAllMeetingsByOppId(Integer oppId,String cusTimeZone);

    void createMeeting(MeetingsDto meetingsDto);

    void deleteMeeting(Integer meetingId);

    void deleteMeetingByCalendar(Integer calendarId);

}