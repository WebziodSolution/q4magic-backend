package com.q4magic.closePlanNotes.serviceImpl;

import com.q4magic.closePlanNotes.service.ClosePlanNotesService;
import com.q4magic.common.dto.ClosePlanNotesDto;
import com.q4magic.common.models.ClosePlan;
import com.q4magic.common.models.ClosePlanNotes;
import com.q4magic.common.models.Contacts;
import com.q4magic.common.models.Customers;
import com.q4magic.common.repository.ClosePlanNotesRepository;
import com.q4magic.common.repository.ClosePlanRepository;
import com.q4magic.common.repository.ContactsRepository;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.common.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service(value = "ClosePlanNotesService")
public class ClosePlanNotesServiceImpl implements ClosePlanNotesService {

    @Autowired
    private ClosePlanNotesRepository closePlanNotesRepository;

    @Autowired
    private ClosePlanRepository closePlanRepository;

    @Autowired
    private ContactsRepository contactsRepository;

    @Autowired
    private CommonService commonService;

    @Autowired
    private CustomersRepository customersRepository;

    @Override
    public List<ClosePlanNotesDto> findLastTwoComments(Integer closePlanId, Integer contactId) {
        try {
            List<ClosePlanNotes> closePlanNotesList =
                    this.closePlanNotesRepository.findLastTwoComments(
                            closePlanId,
                            PageRequest.of(0, 2)
                    );

            List<ClosePlanNotesDto> closePlanNotesDtoList = new ArrayList<>();

            if (!closePlanNotesList.isEmpty()) {
                for (ClosePlanNotes closePlanNotes : closePlanNotesList) {
                    ClosePlanNotesDto closePlanNotesDto = new ClosePlanNotesDto();
                    closePlanNotesDto.setId(closePlanNotes.getId());
                    closePlanNotesDto.setClosePlanId(closePlanNotes.getClosePlan().getId());
                    closePlanNotesDto.setSendTo(closePlanNotes.getSendTo());
                    closePlanNotesDto.setComments(closePlanNotes.getComments());
                    closePlanNotesDto.setCreatedAt(this.commonService.convertDateToString(closePlanNotes.getCreatedAt()));
                    Integer createdById = closePlanNotes.getCreatedBy();

                    Contacts createdByContact = contactsRepository.findById(createdById).orElse(null);
                    closePlanNotesDto.setCreatedBy(createdById);

                    if (createdByContact != null) {
                        String first = createdByContact.getFirstName() != null ? createdByContact.getFirstName() : "";
                        String last = createdByContact.getLastName() != null ? createdByContact.getLastName() : "";
                        String fullName = (first + " " + last).trim();
                        closePlanNotesDto.setCreatedByName(fullName.isEmpty() ? "Unknown" : fullName);
                    } else {
                        Customers customers = customersRepository.findById(createdById).orElse(null);
                        if (customers != null) {
                            closePlanNotesDto.setCreatedByName(customers.getFirstName() +  " " + customers.getLastName());
                        }
                    }

                    closePlanNotesDtoList.add(closePlanNotesDto);

                }
            }
            return closePlanNotesDtoList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ClosePlanNotesDto> findByClosePlanIdAndContactId(Integer closePlanId, Integer contactId) {
        try {
            List<ClosePlanNotes> closePlanNotesList = this.closePlanNotesRepository.findByClosePlanIdAndContactIds(closePlanId, contactId);
            List<ClosePlanNotesDto> closePlanNotesDtoList = new ArrayList<>();
            if (!closePlanNotesList.isEmpty()) {
                for (ClosePlanNotes closePlanNotes : closePlanNotesList) {
                    ClosePlanNotesDto closePlanNotesDto = new ClosePlanNotesDto();
                    closePlanNotesDto.setId(closePlanNotes.getId());
                    closePlanNotesDto.setClosePlanId(closePlanNotes.getClosePlan().getId());
                    closePlanNotesDto.setSendTo(closePlanNotes.getSendTo());
                    closePlanNotesDto.setComments(closePlanNotes.getComments());
                    closePlanNotesDto.setCreatedAt(this.commonService.convertDateToString(closePlanNotes.getCreatedAt()));
                    Integer createdById = closePlanNotes.getCreatedBy();

                    Contacts createdByContact = contactsRepository.findById(createdById).orElse(null);
                    closePlanNotesDto.setCreatedBy(createdById);

                    if (createdByContact != null) {
                        String first = createdByContact.getFirstName() != null ? createdByContact.getFirstName() : "";
                        String last = createdByContact.getLastName() != null ? createdByContact.getLastName() : "";
                        String fullName = (first + " " + last).trim();
                        closePlanNotesDto.setCreatedByName(fullName.isEmpty() ? "Unknown" : fullName);
                    } else {
                        Customers customers = customersRepository.findById(createdById).orElse(null);
                        if (customers != null) {
                            closePlanNotesDto.setCreatedByName(customers.getFirstName() +   " " + customers.getLastName());
                        }
                    }

                    closePlanNotesDtoList.add(closePlanNotesDto);

                }
            }
            return closePlanNotesDtoList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public ClosePlanNotesDto saveComment(ClosePlanNotesDto closePlanNotesDto) {
        try {
            ClosePlanNotes closePlanNotes = new ClosePlanNotes();
            ClosePlan closePlan = this.closePlanRepository.findById(closePlanNotesDto.getClosePlanId()).orElseThrow(() -> new RuntimeException("Close plan not found"));

            closePlanNotes.setSendTo(closePlanNotesDto.getSendTo());
            closePlanNotes.setClosePlan(closePlan);
            closePlanNotes.setComments(closePlanNotesDto.getComments());
            closePlanNotes.setCreatedBy(closePlanNotesDto.getCreatedBy());
//            closePlanNotes.setCreatedAt(new Date());
            this.closePlanNotesRepository.save(closePlanNotes);
            return closePlanNotesDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
