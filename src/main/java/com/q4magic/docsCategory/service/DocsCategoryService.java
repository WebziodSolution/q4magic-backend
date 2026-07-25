package com.q4magic.docsCategory.service;

import com.q4magic.common.dto.DocsCategoryDto;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocsCategoryService {
    List<DocsCategoryDto> getByOppId(@Param("id") Integer id);

    DocsCategoryDto getById(Integer id);

    DocsCategoryDto save(DocsCategoryDto docsCategoryDto);

    DocsCategoryDto update(Integer id, DocsCategoryDto docsCategoryDto);

    void delete(Integer userId,Integer id);
}
