package com.q4magic.roleLookup.serrviceImpl;

import com.q4magic.common.dto.RoleLookupDto;
import com.q4magic.common.models.RoleLookup;
import com.q4magic.common.repository.RoleLookupRepository;
import com.q4magic.roleLookup.serrvice.RoleLookupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service(value = "RoleLookupService")
public class RoleLookupServiceImpl implements RoleLookupService {

    @Autowired
    private RoleLookupRepository roleLookupRepository;

    @Override
    public List<RoleLookupDto> getAllRoles() {
        try {
            List<RoleLookup> roleLookups = this.roleLookupRepository.findFirstFour();
            List<RoleLookupDto> roleLookupDtoList = new ArrayList<>();

            if (!roleLookups.isEmpty()) {
                for (RoleLookup roleLookup : roleLookups) {
                    roleLookupDtoList.add(this.getRoleById(roleLookup.getId()));
                }
            }
            return roleLookupDtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RoleLookupDto getRoleById(Integer roleId) {
        try {
            RoleLookup roleLookup = this.roleLookupRepository.findById(roleId).orElseThrow(() -> new RuntimeException("Role not found"));
            RoleLookupDto roleLookupDto = new RoleLookupDto();
            roleLookupDto.setId(roleLookup.getId());
            roleLookupDto.setRoleType(roleLookup.getRoleType());
            roleLookupDto.setRole(roleLookup.getRole());
            return roleLookupDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RoleLookupDto createRole(RoleLookupDto roleLookupDto) {
        try {
            RoleLookup roleLookup = new RoleLookup();
            roleLookup.setRoleType(roleLookupDto.getRoleType());
            roleLookup.setRole(roleLookupDto.getRole());
            this.roleLookupRepository.save(roleLookup);
            return roleLookupDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RoleLookupDto updateRole(Integer roleId, RoleLookupDto roleLookupDto) {
        try {
            RoleLookup roleLookup = this.roleLookupRepository.findById(roleId).orElseThrow(() -> new RuntimeException("Role not found"));
            roleLookup.setRoleType(roleLookupDto.getRoleType());
            roleLookup.setRole(roleLookupDto.getRole());
            this.roleLookupRepository.save(roleLookup);
            return roleLookupDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteRole(Integer roleId) {
        try {
            RoleLookup roleLookup = this.roleLookupRepository.findById(roleId).orElseThrow(() -> new RuntimeException("Role not found"));
            this.roleLookupRepository.delete(roleLookup);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
