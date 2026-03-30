package kz.kus.sa.tech.condition.service.act.impl;

import kz.kus.sa.tech.condition.dao.entity.ElectrifiedInstallationEntity;
import kz.kus.sa.tech.condition.dao.repository.ElectrifiedInstallationRepository;
import kz.kus.sa.tech.condition.service.act.ElectrifiedInstallationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElectrifiedInstallationServiceImpl implements ElectrifiedInstallationService {

    private final ElectrifiedInstallationRepository electrifiedInstallationRepository;

    @Override
    public List<ElectrifiedInstallationEntity> getByActOfDelineationId(UUID actOfDelineationId) {
        return electrifiedInstallationRepository.findAllByActOfDelineationId(actOfDelineationId);
    }
}
