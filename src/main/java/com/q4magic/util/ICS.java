package com.q4magic.util;

import java.io.FileOutputStream;
import java.net.URI;
import java.util.UUID;

import com.q4magic.common.dto.ICSEventDto;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.*;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;

public class ICS {
    public static void generateICSFile(ICSEventDto icsEventDto, String filePath, String siteNameBigCom) throws Exception {
        Calendar calendar = new Calendar();
//        calendar.getProperties().add(new ProdId("-//My calendar//iCal4j 3.0//EN"));
        calendar.getProperties().add(new ProdId(siteNameBigCom));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);

        // Create a timezone
        TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
        TimeZone timezone = registry.getTimeZone(icsEventDto.getTimeZone());

        Uid uid = new Uid(UUID.randomUUID().toString());

        VEvent event = new VEvent(new DateTime(icsEventDto.getStartDate(), timezone), new DateTime(icsEventDto.getEndDate(), timezone), icsEventDto.getSummary());

//        event.getProperties().add(new Location("Event location"));
        if(!icsEventDto.getDescription().equals("")) {
            event.getProperties().add(new Description(icsEventDto.getDescription()));
        }
        event.getProperties().add(uid);

        Organizer organizer = new Organizer(URI.create("mailto:"+icsEventDto.getReplyToAdd()));
        organizer.getParameters().add(new Cn(icsEventDto.getMemberName()));
        event.getProperties().add(organizer);

        if(!icsEventDto.getAttendees().isEmpty()) {
            for (String emailAddress: icsEventDto.getAttendees()) {
//                event.getProperties().add(new Attendee("mailto:"+emailAddress));
                Attendee attendee = new Attendee(URI.create("mailto:"+emailAddress));
                attendee.getParameters().add(CuType.INDIVIDUAL);
                attendee.getParameters().add(Role.REQ_PARTICIPANT);
                attendee.getParameters().add(PartStat.NEEDS_ACTION);
                attendee.getParameters().add(new Cn(emailAddress));
                attendee.getParameters().add(new XParameter("X-NUM-GUESTS", "0"));
                event.getProperties().add(attendee);
            }
        }
        calendar.getComponents().add(event);
        FileOutputStream fout = new FileOutputStream(filePath);
        CalendarOutputter outputter = new CalendarOutputter();
        outputter.output(calendar, fout);
    }
}
