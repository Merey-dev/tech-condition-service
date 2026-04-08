package kz.kus.sa.tech.condition.dao.entity;

import com.vladmihalcea.hibernate.type.array.ListArrayType;
import kz.kus.sa.tech.condition.dao.entity.embedded.ExternalSubdivisionEmbedded;
import kz.kus.sa.tech.condition.dao.entity.embedded.ExternalUserEmbedded;
import kz.kus.sa.tech.condition.enums.ExecutionStatus;
import kz.kus.sa.tech.condition.enums.TechConditionExecutionType;
import kz.kus.sa.tech.condition.util.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Лист исполнения
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "tech_condition_executions", schema = Constants.SCHEMA_NAME)
@TypeDef(name = "list-array", typeClass = ListArrayType.class)
public class TechConditionExecutionEntity extends AbstractAuditingEntity {

    /** Тип листа исполнения */
    @Enumerated(EnumType.STRING)
    @Column(name = "execution_type", length = 50)
    private TechConditionExecutionType executionType;

    /** Параллельное исполнение */
    @Column(name = "is_parallel")
    private Boolean isParallel;


    /** Адреса объектов из АБД */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "tech_condition_executions_id")
    @ToString.Exclude
    private List<AbdAddressEntity> objectAbdAddresses;


    /** Лист исполнения по каждому адресу */
    @OneToMany(mappedBy = "techConditionExecution", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TechConditionExecutionAbdAddressDecisionEntity> abdAddressDecisions;


    /** Причина доработки */
    @Column(name = "revision_reason", columnDefinition = "text")
    private String revisionReason;

//    /** Быстрый отказ */
//    @Column(name = "quick_refusal")
//    private boolean quickRefusal;

//    /** Дата / время исполнения */
//    @Column(name = "executed_datetime", columnDefinition = "TIMESTAMP WITH TIME ZONE")
//    private OffsetDateTime executedDatetime;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tech_conditions_id", nullable = false)
    @ToString.Exclude
    private TechConditionEntity techCondition;


    /** ДАННЫЕ ФОРМИРОВАНИЯ ТУ */
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "project_id")
    private TechConditionProjectEntity project;


    /** СИСТЕМНЫЕ ПОЛЯ
     * <p>
     * Статус */
    @Column(name = "status_code", nullable = false)
    private String statusCode = ExecutionStatus.ASSIGNED.getCode();

    /** Текущие исполнители */
    @Type(type = "list-array")
    @Column(name = "assignees", columnDefinition = "uuid[]", nullable = false)
    private List<UUID> assignees;

    /** Связанные пользователи */
    @Type(type = "list-array")
    @Column(name = "related_users", columnDefinition = "uuid[]",  nullable = false)
    private List<UUID> relatedUsers;

    /** Кем назначено */
    @Column(name = "assigned_by")
    private UUID assignedBy;

    /** Инициатор */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "initiator_id")),
            @AttributeOverride(name = "fullName", column = @Column(name = "initiator_full_name")),
            @AttributeOverride(name = "providerId", column = @Column(name = "initiator_provider_id")),
            @AttributeOverride(name = "providerName", column = @Column(name = "initiator_provider_name")),
            @AttributeOverride(name = "division", column = @Column(name = "initiator_division")),
            @AttributeOverride(name = "position", column = @Column(name = "initiator_position"))
    })
    private ExternalUserEmbedded initiator;

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

    /** Назначено на департамент */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "assigned_subdivision_id")),
            @AttributeOverride(name = "kk", column = @Column(name = "assigned_subdivision_kk")),
            @AttributeOverride(name = "ru", column = @Column(name = "assigned_subdivision_ru")),
            @AttributeOverride(name = "en", column = @Column(name = "assigned_subdivision_en")),
            @AttributeOverride(name = "qq", column = @Column(name = "assigned_subdivision_qq"))
    })
    private ExternalSubdivisionEmbedded assignedSubdivision;

    /** Исполнитель */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "executor_id")),
            @AttributeOverride(name = "fullName", column = @Column(name = "executor_full_name")),
            @AttributeOverride(name = "providerId", column = @Column(name = "executor_provider_id")),
            @AttributeOverride(name = "providerName", column = @Column(name = "executor_provider_name")),
            @AttributeOverride(name = "division", column = @Column(name = "executor_division")),
            @AttributeOverride(name = "position", column = @Column(name = "executor_position"))
    })
    private ExternalUserEmbedded executor;

    /** Текущий владелец */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "owner_id")),
            @AttributeOverride(name = "fullName", column = @Column(name = "owner_full_name")),
            @AttributeOverride(name = "providerId", column = @Column(name = "owner_provider_id")),
            @AttributeOverride(name = "providerName", column = @Column(name = "owner_provider_name")),
            @AttributeOverride(name = "division", column = @Column(name = "owner_division")),
            @AttributeOverride(name = "position", column = @Column(name = "owner_position"))
    })
    private ExternalUserEmbedded owner;


    /** СОГЛАСОВАНИЕ РУКОВОДИТЕЛЯ
     * <p>
     * Дата / время согласования руководителя */
    @Column(name = "manager_approved_datetime", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime managerApprovedDatetime;

    /** Руководитель */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "manager_id")),
            @AttributeOverride(name = "fullName", column = @Column(name = "manager_full_name")),
            @AttributeOverride(name = "providerId", column = @Column(name = "manager_provider_id")),
            @AttributeOverride(name = "providerName", column = @Column(name = "manager_provider_name")),
            @AttributeOverride(name = "division", column = @Column(name = "manager_division")),
            @AttributeOverride(name = "position", column = @Column(name = "manager_position"))
    })
    private ExternalUserEmbedded manager;
}
