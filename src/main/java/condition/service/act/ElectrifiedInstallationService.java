package condition.service.act;

import kz.kus.sa.tech.condition.dao.entity.ElectrifiedInstallationEntity;

import java.util.List;
import java.util.UUID;

public interface ElectrifiedInstallationService {

    List<ElectrifiedInstallationEntity> getByActOfDelineationId(UUID actOfDelineationId);
}
