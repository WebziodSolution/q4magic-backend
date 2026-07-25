package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
@Table(name="calendar")
public class Calendar {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "id", unique=true, nullable=false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cus_id", referencedColumnName = "cus_id")
    private Customers customers;

    @Column(name="cal_title")
    private String calTitle;

    @Column(name="cal_description", columnDefinition = "text")
    private String calDescription;

    @Column(name="location",columnDefinition = "text")
    private String location;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="cal_start_date_time")
    private Date calStartDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="cal_end_date_time")
    private Date calEndDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="cal_created_date_time")
    private Date calCreatedDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="cal_updated_date_time")
    private Date calUpdatedDateTime;

    @Builder.Default
    @Column(name="cal_all_day", columnDefinition = "String default false")
    private String calAllDay = "false";

    @Column(name="cal_time_zone")
    private String calTimeZone;

    @Column(name="cal_attendees", columnDefinition = "text")
    private String calAttendees;

    @Builder.Default
    @Column(name="cal_aet_id", columnDefinition = "long default 0")
    private Long calAetId = 0L;

    @Builder.Default
    @Column(name="cal_notification", columnDefinition = "char default N")
    private String calNotification = "N";

    @Column(name="cal_numbers", columnDefinition = "text")
    private String calNumbers;

    @Column(name="cal_type")
    private String calType;

    @Builder.Default
    @Column(name="cal_event_reminder", columnDefinition = "varchar default event")
    private String calEventReminder = "event";

    @Column(name="cal_reminder_subject")
    private String calReminderSubject;

    @Column(name="cal_reminder_type")
    private String calReminderType;

    @Builder.Default
    @Column(name="cal_mypage_id", columnDefinition = "Long default 0")
    private Long calMyPageId = 0L;

    @Builder.Default
    @Column(name="cal_sms_sst_id", columnDefinition = "Long default 0")
    private Long calSmsSstId = 0L;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="cal_schedule_date_time")
    private Date calScheduleDateTime;

    @Builder.Default
    @Column(name="cal_parent_id", columnDefinition = "Long default 0")
    private Integer calParentId = 0;

    @Column(name="cal_repeat_every")
    private int calRepeatEvery;

    @Column(name="cal_repeat_type")
    private String calRepeatType;

    @Column(name="cal_repeat_every_type")
    private String calRepeatEveryType;

    @Column(name="cal_repeat_day_name")
    private String calRepeatDayName;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="cal_repeat_end_date")
    private Date calRepeatEndDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="cal_repeat_date")
    private Date calRepeatDate;

    @Column(name="cal_repeat_selected_option")
    private int calRepeatSelectedOption;
}



//package com.q4magic.common.models;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.util.Date;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@ToString
//@Builder
//@Entity
//@Table(name="calendar")
//public class Calendar {
//    @Id
//    @GeneratedValue(strategy=GenerationType.IDENTITY)
//    @Column(name = "id", unique=true, nullable=false)
//    private Integer id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "cus_id", referencedColumnName = "cus_id")
//    private Customers customers;
//
//    @Column(name="cal_title")
//    private String calTitle;
//
//    @Column(name="cal_description", columnDefinition = "text")
//    private String calDescription;
//
//    @Temporal(TemporalType.TIMESTAMP)
//    @Column(name="cal_start_date_time")
//    private Date calStartDateTime;
//
//    @Temporal(TemporalType.TIMESTAMP)
//    @Column(name="cal_end_date_time")
//    private Date calEndDateTime;
//
//    @Temporal(TemporalType.TIMESTAMP)
//    @Column(name="cal_created_date_time")
//    private Date calCreatedDateTime;
//
//    @Temporal(TemporalType.TIMESTAMP)
//    @Column(name="cal_updated_date_time")
//    private Date calUpdatedDateTime;
//
//    @Builder.Default
//    @Column(name="cal_all_day", columnDefinition = "String default false")
//    private String calAllDay = "false";
//
//    @Column(name="cal_time_zone")
//    private String calTimeZone;
//
//    @Column(name="cal_attendees", columnDefinition = "text")
//    private String calAttendees;
//
//    @Builder.Default
//    @Column(name="cal_aet_id", columnDefinition = "long default 0")
//    private Long calAetId = 0L;
//
//    @Builder.Default
//    @Column(name="cal_notification", columnDefinition = "char default N")
//    private String calNotification = "N";
//
//    @Column(name="cal_numbers", columnDefinition = "text")
//    private String calNumbers;
//
//    @Column(name="cal_type")
//    private String calType;
//
//    @Builder.Default
//    @Column(name="cal_event_reminder", columnDefinition = "varchar default event")
//    private String calEventReminder = "event";
//
//    @Column(name="cal_reminder_subject")
//    private String calReminderSubject;
//
//    @Column(name="cal_reminder_type")
//    private String calReminderType;
//
//    @Builder.Default
//    @Column(name="cal_mypage_id", columnDefinition = "Long default 0")
//    private Long calMyPageId = 0L;
//
//    @Builder.Default
//    @Column(name="cal_sms_sst_id", columnDefinition = "Long default 0")
//    private Long calSmsSstId = 0L;
//
//    @Temporal(TemporalType.TIMESTAMP)
//    @Column(name="cal_schedule_date_time")
//    private Date calScheduleDateTime;
//
//    @Builder.Default
//    @Column(name="cal_parent_id", columnDefinition = "Long default 0")
//    private Integer calParentId = 0;
//
//    @Column(name="cal_repeat_every")
//    private int calRepeatEvery;
//
//    @Column(name="cal_repeat_type")
//    private String calRepeatType;
//
//    @Column(name="cal_repeat_every_type")
//    private String calRepeatEveryType;
//
//    @Column(name="cal_repeat_day_name")
//    private String calRepeatDayName;
//
//    @Temporal(TemporalType.TIMESTAMP)
//    @Column(name="cal_repeat_end_date")
//    private Date calRepeatEndDate;
//
//    @Temporal(TemporalType.TIMESTAMP)
//    @Column(name="cal_repeat_date")
//    private Date calRepeatDate;
//
//    @Column(name="cal_repeat_selected_option")
//    private int calRepeatSelectedOption;
//}
