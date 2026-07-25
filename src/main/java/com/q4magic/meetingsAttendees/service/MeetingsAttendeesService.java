package com.q4magic.meetingsAttendees.service;

import com.q4magic.common.dto.MeetingsAttendeesDto;

import java.util.List;

public interface MeetingsAttendeesService {
    List<MeetingsAttendeesDto> findByMeetingsId(Integer meetingsId);

    MeetingsAttendeesDto findById(Integer id);

    MeetingsAttendeesDto addMeetingAttendees(MeetingsAttendeesDto meetingsAttendeesDto);

    MeetingsAttendeesDto updateMeetingAttendees(Integer id, MeetingsAttendeesDto meetingsAttendeesDto);

    void deleteMeetingAttendees(Integer id);
}
