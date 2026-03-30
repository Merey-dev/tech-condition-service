package kz.kus.sa.tech.condition.dao.entity;

import kz.kus.sa.registry.enums.Event;
import kz.kus.sa.registry.enums.RegistryType;
import kz.kus.sa.tech.condition.dao.entity.embedded.ExternalUserEmbedded;
import kz.kus.sa.tech.condition.util.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * История действий по заявке по выдаче ТУ
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "histories", schema = Constants.SCHEMA_NAME)
@Where(clause = "deleted_datetime is null")
public class HistoryEntity extends AbstractAuditingEntity {

    /** Тип услуги */
    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private RegistryType registryType;

    /** Тип события */
    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private Event event;

    /** Дата / время события */
    @Column(name = "event_datetime", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime eventDatetime;

    /** Причина доработки */
    @Column(name = "revision_reason", columnDefinition = "text")
    private String revisionReason;

    /** Комментарий */
    @Column(name = "comment", columnDefinition = "text")
    private String comment;

    /** Статус */
    @Column(name = "status", nullable = false)
    private String status;

    /** Статус исполнения */
    @Column(name = "execution_status")
    private String executionStatus;

    /** Список назначений */
    @Type(type = "list-array")
    @Column(name = "assignees", columnDefinition = "uuid[]")
    private List<UUID> assignees;


    /** Текущий пользователь */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "current_user_id")),
            @AttributeOverride(name = "fullName", column = @Column(name = "current_user_full_name")),
            @AttributeOverride(name = "providerId", column = @Column(name = "current_user_provider_id")),
            @AttributeOverride(name = "providerName", column = @Column(name = "current_user_provider_name")),
            @AttributeOverride(name = "division", column = @Column(name = "current_user_division")),
            @AttributeOverride(name = "position", column = @Column(name = "current_user_position"))
    })
    private ExternalUserEmbedded currentUser;


    /** Кем назначено */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "assigned_by_user_id")),
            @AttributeOverride(name = "fullName", column = @Column(name = "assigned_by_user_full_name")),
            @AttributeOverride(name = "providerId", column = @Column(name = "assigned_by_user_provider_id")),
            @AttributeOverride(name = "providerName", column = @Column(name = "assigned_by_user_provider_name")),
            @AttributeOverride(name = "division", column = @Column(name = "assigned_by_user_division")),
            @AttributeOverride(name = "position", column = @Column(name = "assigned_by_user_position"))
    })
    private ExternalUserEmbedded assignedByUser;


    /** Назначено на организацию */
    @Column(name = "assigned_provider_id")
    private UUID assignedProviderId;

    @Column(name = "assigned_provider_full_name")
    private String assignedProviderFullName;


    /** Назначено на департамент */
    @Column(name = "assigned_division_id")
    private UUID assignedDivisionId;

    @Column(name = "assigned_division_full_name")
    private String assignedDivisionFullName;


    /** Назначено на исполнителя */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "assigned_executor_id")),
            @AttributeOverride(name = "fullName", column = @Column(name = "assigned_executor_full_name")),
            @AttributeOverride(name = "providerId", column = @Column(name = "assigned_executor_provider_id")),
            @AttributeOverride(name = "providerName", column = @Column(name = "assigned_executor_provider_name")),
            @AttributeOverride(name = "division", column = @Column(name = "assigned_executor_division")),
            @AttributeOverride(name = "position", column = @Column(name = "assigned_executor_position"))
    })
    private ExternalUserEmbedded assignedExecutor;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tech_conditions_id")
    @ToString.Exclude
    private TechConditionEntity techCondition;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tech_condition_executions_id")
    @ToString.Exclude
    private TechConditionExecutionEntity techConditionExecution;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "act_of_delineation_renewals_id")
    @ToString.Exclude
    private ActOfDelineationRenewalEntity actOfDelineationRenewal;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "act_of_delineation_renewal_executions_id")
    @ToString.Exclude
    private ActOfDelineationRenewalExecutionEntity actOfDelineationRenewalExecution;
}
