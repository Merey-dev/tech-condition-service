package kz.kus.sa.tech.condition.dao.specification;

import kz.kus.sa.registry.enums.Source;
import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionProjectEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TechConditionExecutionSpecification {

    private TechConditionExecutionSpecification() {
    }

    public static Specification<TechConditionExecutionEntity> isNotDeleted() {
        return (root, cq, cb) -> cb.and(cb.isNull(root.get("deletedDatetime")));
    }

    //todo refactor
    public static Specification<TechConditionExecutionEntity> bySearchText(String searchText) {
        if (StringUtils.isBlank(searchText))
            return null;
        return (root, cq, cb) -> {
            Join<TechConditionExecutionEntity, TechConditionEntity> techCondition = root.join("techCondition", JoinType.INNER);
            Join<TechConditionEntity, TechConditionProjectEntity> project = techCondition.join("project", JoinType.LEFT);
            return cb.or(
                    cb.like(techCondition.get("consumerIinBin"), "%" + searchText + "%"),
                    cb.like(cb.lower(techCondition.get("consumerFullName")), "%" + searchText.toLowerCase() + "%"),
                    cb.like(cb.lower(techCondition.join("objectAbdAddress").get("locationKk")), "%" + searchText.toLowerCase() + "%"),
                    cb.like(cb.lower(techCondition.join("objectAbdAddress").get("locationRu")), "%" + searchText.toLowerCase() + "%"),
                    cb.like(cb.lower(project.get("connectionPointsRu")), "%" + searchText.toLowerCase() + "%"),
                    cb.like(cb.lower(project.get("connectionPointsKk")), "%" + searchText.toLowerCase() + "%"),
                    cb.like(cb.lower(techCondition.get("registrationNumber")), "%" + searchText.toLowerCase() + "%"),
                    cb.like(cb.lower(techCondition.get("reasonForRefusalRegistrationNumber")), "%" + searchText.toLowerCase() + "%"),
                    cb.like(cb.lower(project.get("registrationNumber")), "%" + searchText.toLowerCase() + "%"),
                    cb.like(techCondition.get("requiredPower").as(String.class), "%" + searchText + "%")
            );
        };
    }

    public static Specification<TechConditionExecutionEntity> sourceNotCrm() {
        return (root, cq, cb) -> {
            Join<TechConditionExecutionEntity, TechConditionEntity> techCondition = root.join("techCondition", JoinType.INNER);
            return cb.and(cb.notEqual(techCondition.get("source"), Source.CRM));
        };
    }

    public static Specification<TechConditionExecutionEntity> bySource(Source source) {
        if (Objects.isNull(source))
            return null;
        return (root, cq, cb) -> {
            Join<TechConditionExecutionEntity, TechConditionEntity> techCondition = root.join("techCondition", JoinType.INNER);
            return cb.equal(techCondition.get("source"), source);
        };
    }

    public static Specification<TechConditionExecutionEntity> byStatuses(List<String> statuses) {
        if (Objects.isNull(statuses))
            return null;
        return (root, cq, cb) -> cb.and(root.get("statusCode").in(statuses));
    }

    public static Specification<TechConditionExecutionEntity> byStatementStatuses(List<String> statuses) {
        if (Objects.isNull(statuses))
            return null;
        return (root, cq, cb) -> {
            Join<TechConditionExecutionEntity, TechConditionEntity> techCondition = root.join("techCondition", JoinType.INNER);
            return techCondition.get("statusCode").in(statuses);
        };
    }

    public static Specification<TechConditionExecutionEntity> byStatementDatetimeGreaterThanOrEqualTo(LocalDate dateFrom) {
        if (Objects.isNull(dateFrom))
            return null;
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("statementDatetime"),
                        OffsetDateTime.of(dateFrom, LocalTime.MIN, ZoneOffset.UTC)));
    }

    public static Specification<TechConditionExecutionEntity> byStatementDatetimeLessThanOrEqualTo(LocalDate dateTo) {
        if (Objects.isNull(dateTo))
            return null;
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.lessThan(root.get("statementDatetime"),
                        OffsetDateTime.of(dateTo, LocalTime.MAX, ZoneOffset.UTC)));
    }

    public static Specification<TechConditionExecutionEntity> byUserId(UUID userId, UUID currentUserId) {
        if (Objects.isNull(userId))
            return (root, cq, cb) -> cb.equal(cb.literal(currentUserId),
                    cb.function("any", UUID.class, root.get("relatedUsers")));
        return (root, cq, cb) -> cb.equal(cb.literal(userId),
                cb.function("any", UUID.class, root.get("assignees")));
    }

    public static Specification<TechConditionExecutionEntity> byUserIdForAdmin(UUID userId) {
        if (Objects.isNull(userId))
            return null;
        return (root, cq, cb) -> cb.equal(cb.literal(userId),
                cb.function("any", UUID.class, root.get("assignees")));
    }

    public static Specification<TechConditionExecutionEntity> byProviderId(UUID providerId) {
        if (Objects.isNull(providerId))
            return null;
        return (root, cq, cb) -> {
            Join<TechConditionExecutionEntity, TechConditionEntity> techCondition = root.join("techCondition", JoinType.INNER);
            return cb.equal(techCondition.get("providerId"), providerId);
        };
    }
}
