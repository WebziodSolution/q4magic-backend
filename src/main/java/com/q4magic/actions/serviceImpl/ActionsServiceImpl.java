package com.q4magic.actions.serviceImpl;

import com.q4magic.actions.service.ActionService;
import com.q4magic.common.dto.ActionsDto;
import com.q4magic.common.models.Actions;
import com.q4magic.common.repository.ActionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service(value = "actionService")
public class ActionsServiceImpl implements ActionService {

    @Autowired
    private ActionsRepository actionsRepository;

    @Override
    public List<ActionsDto> getAllActions() {
        try {
            List<Actions> actions = this.actionsRepository.findAll();
            List<ActionsDto> actionDtos = new ArrayList<>();
            if (!actions.isEmpty()) {
                for (Actions actions1 : actions) {
                    ActionsDto actionDto = new ActionsDto();
                    actionDto.setId(actions1.getId());
                    actionDto.setName(actions1.getName());
                    actionDtos.add(actionDto);
                }
            }
            return actionDtos;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
