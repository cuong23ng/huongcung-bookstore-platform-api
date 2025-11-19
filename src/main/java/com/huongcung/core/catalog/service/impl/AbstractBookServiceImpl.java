package com.huongcung.core.catalog.service.impl;

import com.huongcung.core.catalog.model.dto.AbstractBookDTO;
import com.huongcung.core.catalog.mapper.AbstractBookMapper;
import com.huongcung.core.catalog.repository.AbstractBookRepository;
import com.huongcung.core.catalog.service.AbstractBookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AbstractBookServiceImpl implements AbstractBookService {

    private final AbstractBookRepository abstractBookRepository;

    private final AbstractBookMapper abstractBookMapper;

    @Override
    public List<AbstractBookDTO> findAll() {
        return abstractBookMapper.toDto(abstractBookRepository.findAll());
    }

    @Override
    public AbstractBookDTO findBookByCode(String code) {
        return abstractBookMapper.toDto(abstractBookRepository.findAbstractBookEntityByCode(code));
    }

    @Override
    public List<AbstractBookDTO> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return abstractBookMapper.toDto(abstractBookRepository.findByIdIn(ids));
    }

}
