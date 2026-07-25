package com.q4magic.calendar.calendarAppointmentEventType.service;


import com.q4magic.common.dto.CalendarAppointmentEventTypeDto;

import java.util.List;

public interface CalendarAppointmentEventTypeService {
    List<CalendarAppointmentEventTypeDto> getAllAppointmentEventTypeByUserId(Integer cusId);

    CalendarAppointmentEventTypeDto getAppointmentEventType(Integer id);

    void saveAppointmentEventType(Integer id, CalendarAppointmentEventTypeDto calendarAppointmentEventTypeDto);

    void deleteAppointmentEventType(Integer id);
}
