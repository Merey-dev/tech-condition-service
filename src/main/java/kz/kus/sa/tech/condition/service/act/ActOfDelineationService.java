package kz.kus.sa.tech.condition.service.act;

import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationEntity;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationCreateDto;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationDto;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationSearchDto;
import kz.kus.sa.tech.condition.dto.act.ActOfDelineationUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ActOfDelineationService {

    Page<ActOfDelineationDto> getAll(ActOfDelineationSearchDto search, Pageable pageable);

    ActOfDelineationDto create(ActOfDelineationCreateDto dto);

    ActOfDelineationDto update(UUID id, ActOfDelineationUpdateDto dto);

    ActOfDelineationDto getById(UUID id);

    ActOfDelineationEntity findByRenewalExecutionId(UUID renewalExecutionId);
}
