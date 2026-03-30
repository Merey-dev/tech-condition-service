package kz.kus.sa.tech.condition.service.address;

import kz.kus.sa.tech.condition.dao.entity.AbdAddressEntity;

import java.util.List;
import java.util.UUID;

public interface AbdAddressService {

    void saveList(List<AbdAddressEntity> oldList, List<AbdAddressEntity> newList, Object object);

    List<AbdAddressEntity> getByTechConditionId(UUID techConditionId);

    List<AbdAddressEntity> getByTechConditionExecutionId(UUID techConditionExecutionId);

    List<AbdAddressEntity> getByTechConditionProjectId(UUID techConditionProjectId);

    List<AbdAddressEntity> getByActOfDelineationRenewalId(UUID actOfDelineationRenewalId);

    List<AbdAddressEntity> getByActOfDelineationRenewalExecutionId(UUID actOfDelineationRenewalExecutionId);

    List<AbdAddressEntity> getByActOfDelineationId(UUID actOfDelineationId);

    void deleteDatetime(List<AbdAddressEntity> list);
}
