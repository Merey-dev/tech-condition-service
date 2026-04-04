package condition.dao.specification;

import kz.kus.sa.tech.condition.dao.entity.TechConditionProjectEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.JoinType;
import java.util.List;
import java.util.Objects;

public class TechConditionProjectSpecification {

    private TechConditionProjectSpecification() {
    }

    public static Specification<TechConditionProjectEntity> isNotDeleted() {
        return (root, cq, cb) -> cb.and(cb.isNull(root.get("deletedDatetime")));
    }

    public static Specification<TechConditionProjectEntity> byStatuses(List<String> statuses) {
        if (Objects.isNull(statuses))
            return null;
        return (root, cq, cb) -> cb.and(root.get("statusCode").in(statuses));
    }

    public static Specification<TechConditionProjectEntity> byIdentifier(String identifier) {
        if (StringUtils.isBlank(identifier))
            return null;
        return (root, cq, cb) -> cb.equal(root.get("consumerIinBin"), identifier);
    }

    public static Specification<TechConditionProjectEntity> byCadastral(String cadastral) {
        if (StringUtils.isBlank(cadastral))
            return null;
        return (root, cq, cb) -> cb.equal(root.join("objectAbdAddress", JoinType.INNER).get("cadastralNumber"), cadastral);
    }
}
