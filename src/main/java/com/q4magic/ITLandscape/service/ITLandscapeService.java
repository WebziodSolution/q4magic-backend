package com.q4magic.ITLandscape.service;

import com.q4magic.common.dto.ITLandscapeDto;
import com.q4magic.common.models.ITLandscape;

import java.util.List;

public interface ITLandscapeService {
    List<ITLandscapeDto> getAllITLandscapesByOppId(String oppId);

    ITLandscapeDto getITLandscapeById(Integer id);

    ITLandscapeDto createITLandscape(ITLandscapeDto itLandscape,Boolean syncSalesforce);

    ITLandscapeDto updateITLandscape(Integer id, ITLandscapeDto itLandscape,Boolean syncSalesforce);

    void deleteITLandscape(Integer id,Boolean syncSalesforce);
}
