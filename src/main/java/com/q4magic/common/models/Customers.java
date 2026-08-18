package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

@Entity
@Table(name = "customers")
@Setter
@Getter
@NoArgsConstructor
public class Customers implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cus_id", unique = true, nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auth_id ", referencedColumnName = "id")
    private AuthIDetails authIDetails;

    @Column(name = "username")
    private String username;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "password")
    private String password;

    @Column(name = "login_preference")
    private String loginPreference;

    @Column(name = "account_owner")
    private String accountOwner;

    @Column(name = "title")
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id ", referencedColumnName = "role_id")
    private RoleLookup role;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "address1", columnDefinition = "TEXT")
    private String address1;

    @Column(name = "address2")
    private String address2;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "country")
    private String country;

    @Column(name = "zipcode")
    private Integer zipCode;

    @Column(name = "quota")
    private String quota;

    @Column(name = "start_eval_period")
    @Temporal(TemporalType.DATE)
    private Date startEvalPeriod;

    @Column(name = "end_eval_period")
    @Temporal(TemporalType.DATE)
    private Date endEvalPeriod;

    @Column(name = "eval_period")
    @Temporal(TemporalType.DATE)
    private Date evalPeriod;

    @Column(name = "calendar_year_type")
    private String calendarYearType;

    @Column(name = "question1")
    private String question1;

    @Column(name = "answer1")
    private String answer1;

    @Column(name = "question2")
    private String question2;

    @Column(name = "answer2")
    private String answer2;

    @Column(name = "question3")
    private String question3;

    @Column(name = "answer3")
    private String answer3;

    @Column(name = "billing_address1", columnDefinition = "TEXT")
    private String billingAddress1;

    @Column(name = "billing_address2")
    private String billingAddress2;

    @Column(name = "billing_city")
    private String billingCity;

    @Column(name = "billing_state")
    private String billingState;

    @Column(name = "billing_country")
    private String billingCountry;

    @Column(name = "billing_zipcode")
    private Integer billingZipcode;

    @Column(name = "billing_phone")
    private String billingPhone;

    @Column(name = "date_registered")
    @Temporal(TemporalType.DATE)
    private Date dateRegistered;

    @Column(name = "bill_date")
    @Temporal(TemporalType.DATE)
    private Date billDate;

    @Column(name = "billing_address_same_as_primary")
    private Boolean billingAddressSameAsPrimary;

    @Column(name = "payment_profile_id")
    private String authorizeCustomerPaymentProfileId;

    @Column(name = "customer_profile_id")
    private String authorizeCustomerProfileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "cus_id")
    private Customers customers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_user_type", referencedColumnName = "id")
    private SubUserType subUserType;

    @Column(name = "google_calendar_access_token", columnDefinition = "text")
    private String googleCalendarAccessToken;

    @Column(name = "google_calendar_refresh_token", columnDefinition = "text")
    private String googleCalendarRefreshToken;

    @Column(name = "google_calendar_sync_time")
    private String googleCalendarSyncTime;

    @Column(name = "google_calendar_email")
    private String googleCalendarEmail;

    @Column(name = "outlook_calendar_access_token", columnDefinition = "text")
    private String outlookCalendarAccessToken;

    @Column(name = "outlook_calendar_refresh_token", columnDefinition = "text")
    private String outlookCalendarRefreshToken;

    @Column(name = "outlook_calendar_sync_time")
    private String outlookCalendarSyncTime;

    @Column(name = "outlook_calendar_email")
    private String outlookCalendarEmail;

    @Column(name = "salesforce_access_token")
    private String salesforceAccessToken;

    @Column(name = "salesforce_instance_url")
    private String salesforceInstanceUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", referencedColumnName = "sub_id")
    private SubscriptionRates subscriptionRates;

    @Column(name = "time_zone")
    private String timeZone;

    @Column(name = "web_conference", columnDefinition = "text")
    private String webConference;

    @Column(name = "email_notification", columnDefinition = "text")
    private String emailNotification;

    @Column(name = "default_calendar")
    private String defaultCalendar;

    @Column(name = "meeting_quota")
    private String meetingQuota;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_to", referencedColumnName = "cus_id")
    private Customers reportTo;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getUsername() {
        return this.username;
    }
}
