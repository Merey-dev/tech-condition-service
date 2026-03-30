package kz.kus.sa.tech.condition.dao.repository;

import kz.kus.sa.tech.condition.dao.entity.ElectrifiedInstallationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ElectrifiedInstallationRepository extends JpaRepository<ElectrifiedInstallationEntity, UUID> {

    List<ElectrifiedInstallationEntity> findAllByActOfDelineationId(UUID actOfDelineationId);
}
