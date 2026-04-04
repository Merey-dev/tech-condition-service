package condition.dao.specification;

import kz.kus.sa.tech.condition.dao.entity.ActOfDelineationEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.UUID;

import static org.springframework.util.CollectionUtils.isEmpty;

public class ActOfDelineationSpecification {

    private ActOfDelineationSpecification() {
    }

    public static Specification<ActOfDelineationEntity> isNotDeleted() {
        return (root, cq, cb) -> cb.and(
                cb.isNull(root.get("deletedDatetime"))
        );
    }

    public static Specification<ActOfDelineationEntity> byIdList(List<UUID> idList) {
        if (isEmpty(idList))
            return null;
        return (root, cq, cb) -> cb.and(
                root.get("id").in(idList)
        );
    }

    public static Specification<ActOfDelineationEntity> byRegistrationNumber(String registrationNumber) {
        if (StringUtils.isBlank(registrationNumber))
            return null;
        return (root, cq, cb) -> cb.or(
                cb.like(cb.lower(root.get("registrationNumber")), "%" + registrationNumber.toLowerCase() + "%")
        );
    }

    public static Specification<ActOfDelineationEntity> byIdentifierList(List<String> consumerIinBinList) {
        if (isEmpty(consumerIinBinList))
            return null;
        return (root, cq, cb) -> cb.and(
                root.join("actOfDelineationRenewalExecution")
                        .get("actOfDelineationRenewal")
                        .get("consumerIinBin")
                        .in(consumerIinBinList)
        );
    }
}
