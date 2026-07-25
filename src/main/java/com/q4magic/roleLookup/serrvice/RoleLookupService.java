package com.q4magic.roleLookup.serrvice;

import com.q4magic.common.dto.RoleLookupDto;

import java.util.List;

public interface RoleLookupService {
    List<RoleLookupDto> getAllRoles();
    RoleLookupDto getRoleById(Integer roleId);
    RoleLookupDto createRole(RoleLookupDto roleLookupDto);
    RoleLookupDto updateRole(Integer roleId, RoleLookupDto roleLookupDto);
    void deleteRole(Integer roleId);
}
