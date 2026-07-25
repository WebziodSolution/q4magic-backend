package com.q4magic.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class CalendarDto {
    private Integer id;
    private Integer customerId;
    private Integer oppId;
    private Integer meetingId;
    private String contactIds;

    private String title;
    private String description;
    private String location;
    private String start;
    private String end;
    private Boolean allDay;
    private String calTimeZone;
    private String calAttendees;
    private String slotMember;
    private Long calAetId;

    private int slotTimeMinus;
    private List<String> contactList;

    private String displayStart;
    private String displayEnd;

    private String calType;
    private String currentDateYN;
    private String memTimeZone;

    private String calEventReminder;
    private String calReminderSubject;
    private String calReminderType;
    private Long calMyPageId;
    private Long calSmsSstId;
    private String calScheduleDateTime;

    private Integer calParentId;
    private int calRepeatEvery;
    private String calRepeatType;
    private String calRepeatEveryType;
    private String calRepeatDayName;
    private String calRepeatEndDate;
    private String calRepeatDate;
    private int calRepeatSelectedOption;

    private String editAll;
}
