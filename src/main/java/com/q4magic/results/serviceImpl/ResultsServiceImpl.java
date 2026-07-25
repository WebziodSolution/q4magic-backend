package com.q4magic.results.serviceImpl;

import com.q4magic.common.models.Contacts;
import com.q4magic.common.models.Customers;
import com.q4magic.common.models.Meetings;
import com.q4magic.common.models.Opportunities;
import com.q4magic.common.repository.ContactsRepository;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.common.repository.MeetingsRepository;
import com.q4magic.common.repository.OpportunitiesRepository;
import com.q4magic.results.service.ResultsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service(value = "ResultsService")
public class ResultsServiceImpl implements ResultsService {

    @Autowired
    private OpportunitiesRepository opportunitiesRepository;

    @Autowired
    private ContactsRepository contactsRepository;

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private MeetingsRepository meetingsRepository;

    @Override
    public List<Map<String, Object>> getResults(Integer userId) {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            Customers customers = this.customersRepository.findById(userId).orElseThrow(() -> new Exception("Customer not found"));
            if (customers.getRole().getRole().equals("SALES MANAGER")) {
                List<Customers> allRepCustomers = this.customersRepository.getAllSubUsersWithRepRole(userId);

                if (allRepCustomers != null && !allRepCustomers.isEmpty()) {

                    for (Customers customer : allRepCustomers) {

                        double pipelineTotal = 0.0;
                        double revTotal = 0.0;
                        // Get only this rep's active opportunities
                        List<Opportunities> opportunities =
                                this.opportunitiesRepository.findActiveOpportunities(customer.getId());

                        if (opportunities != null && !opportunities.isEmpty()) {
                            for (Opportunities opp : opportunities) {

                                if ("Pipeline".equalsIgnoreCase(opp.getStatus())) {
                                    Double dealAmount = opp.getDealAmount() != null ? opp.getDealAmount() : 0.0;
                                    pipelineTotal += dealAmount;
                                }
                                if ("Closed Won".equalsIgnoreCase(opp.getSalesStage()) && "Won".equalsIgnoreCase(opp.getStatus())) {
                                    Double dealAmount = opp.getDealAmount() != null ? opp.getDealAmount() : 0.0;
                                    revTotal += dealAmount;
                                }
                            }
                        }

                        Map<String, Object> row = new HashMap<>();
                        row.put("rep_name", customer.getFirstName() != null ? customer.getFirstName() + " " + customer.getLastName() : customer.getUsername());
                        row.put("pipelineTotal", pipelineTotal);
                        row.put("rev", revTotal);
                        result.add(row);
                    }
                }

                return result;
            } else {
                double pipelineTotal = 0.0;
                double revTotal = 0.0;
                // Get only this rep's active opportunities
                List<Opportunities> opportunities =
                        this.opportunitiesRepository.findActiveOpportunities(customers.getId());

                if (opportunities != null && !opportunities.isEmpty()) {
                    for (Opportunities opp : opportunities) {

                        if ("Pipeline".equalsIgnoreCase(opp.getStatus())) {
                            Double dealAmount = opp.getDealAmount() != null ? opp.getDealAmount() : 0.0;
                            pipelineTotal += dealAmount;
                        }
                        if ("Closed Won".equalsIgnoreCase(opp.getSalesStage()) && "Won".equalsIgnoreCase(opp.getStatus())) {
                            Double dealAmount = opp.getDealAmount() != null ? opp.getDealAmount() : 0.0;
                            revTotal += dealAmount;
                        }
                    }
                }

                Map<String, Object> row = new HashMap<>();
                row.put("rep_name", customers.getFirstName() != null ? customers.getFirstName() + " " + customers.getLastName() : customers.getUsername());
                row.put("pipelineTotal", pipelineTotal);
                row.put("rev", revTotal);
                result.add(row);
                return result;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Map<String, Object>> getActivities(Integer userId) {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            Customers customers = this.customersRepository.findById(userId).orElseThrow(() -> new Exception("Customer not found"));
            if (customers.getRole().getRole().equals("SALES MANAGER")) {
                List<Customers> allRepCustomers = this.customersRepository.getAllSubUsersWithRepRole(userId);
                if (!allRepCustomers.isEmpty()) {
                    for (Customers customer : allRepCustomers) {
                        Integer contactCount = this.contactsRepository.countNewContactByCustomerId(customer.getId());
                        Map<String, Object> data = new HashMap<>();

                        Integer newMeetingCount = null;
                        Integer oldMeetingCount = null;
                        Integer onSiteMeetingCount = null;
                        Integer virtualMeetingCount = null;

                        List<Meetings> meetings = this.meetingsRepository.findByCustomerId(customer.getId());
                        if (!meetings.isEmpty()) {
                            for (Meetings meeting : meetings) {
                                boolean hasNewContact = false;
                                if (meeting.getContactIds() != null && !meeting.getContactIds().isEmpty()) {
                                    String contactIdsStr = meeting.getContactIds();
                                    Integer[] contactIds = Arrays.stream(
                                                    contactIdsStr.replace("[", "")
                                                            .replace("]", "")
                                                            .split(",")
                                            )
                                            .map(String::trim)
                                            .map(Integer::valueOf)
                                            .toArray(Integer[]::new);

                                    for (Integer contactId : contactIds) {
                                        Contacts contacts = this.contactsRepository
                                                .findById(contactId)
                                                .orElseThrow(() -> new RuntimeException("Contact not found"));

                                        if (Boolean.TRUE.equals(contacts.getFromMailScraping())) {
                                            hasNewContact = true;
                                            break; // ✅ one true is enough
                                        }
                                    }
                                }

                                if (hasNewContact) {
                                    newMeetingCount++;
                                } else {
                                    oldMeetingCount++;
                                }

                                if (meeting.getCalendar().getLocation() != null) {
                                    onSiteMeetingCount++;
                                }else {
                                    virtualMeetingCount++;
                                }
                            }
                        }
                        data.put("rep_name", customer.getUsername());
                        data.put("contacts", contactCount);
                        data.put("newMeetingCount", newMeetingCount);
                        data.put("oldMeetingCount", oldMeetingCount);
                        data.put("onSiteMeetingCount", onSiteMeetingCount);
                        data.put("virtualMeetingCount", virtualMeetingCount);

                        result.add(data);
                    }
                }
                return result;
            } else {
                Integer contactCount = this.contactsRepository.countNewContactByCustomerId(customers.getId());
                Map<String, Object> data = new HashMap<>();

                Integer newMeetingCount = null;
                Integer oldMeetingCount = null;
                Integer onSiteMeetingCount = null;
                Integer virtualMeetingCount = null;

                List<Meetings> meetings = this.meetingsRepository.findByCustomerId(customers.getId());
                if (!meetings.isEmpty()) {
                    for (Meetings meeting : meetings) {
                        boolean hasNewContact = false;
                        if (meeting.getContactIds() != null && !meeting.getContactIds().isEmpty()) {
                            String contactIdsStr = meeting.getContactIds();
                            Integer[] contactIds = Arrays.stream(
                                            contactIdsStr.replace("[", "")
                                                    .replace("]", "")
                                                    .split(",")
                                    )
                                    .map(String::trim)
                                    .map(Integer::valueOf)
                                    .toArray(Integer[]::new);

                            for (Integer contactId : contactIds) {
                                Contacts contacts = this.contactsRepository
                                        .findById(contactId)
                                        .orElseThrow(() -> new RuntimeException("Contact not found"));

                                if (Boolean.TRUE.equals(contacts.getFromMailScraping())) {
                                    hasNewContact = true;
                                    break; // ✅ one true is enough
                                }
                            }
                        }

                        if (hasNewContact) {
                            newMeetingCount++;
                        } else {
                            oldMeetingCount++;
                        }

                        if (meeting.getCalendar().getLocation() != null) {
                            onSiteMeetingCount++;
                        }else {
                            virtualMeetingCount++;
                        }
                    }
                }
                data.put("rep_name", customers.getUsername());
                data.put("contacts", contactCount);
                data.put("newMeetingCount", newMeetingCount);
                data.put("oldMeetingCount", oldMeetingCount);
                data.put("onSiteMeetingCount", onSiteMeetingCount);
                data.put("virtualMeetingCount", virtualMeetingCount);
                result.add(data);
                return result;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
