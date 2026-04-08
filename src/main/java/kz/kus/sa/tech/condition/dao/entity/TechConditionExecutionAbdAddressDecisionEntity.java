package kz.kus.sa.tech.condition.dao.entity;

import com.vladmihalcea.hibernate.type.array.ListArrayType;
import kz.kus.sa.registry.enums.TechConditionExecutionDecisionType;
import kz.kus.sa.tech.condition.dao.entity.embedded.ExternalSubdivisionEmbedded;
import kz.kus.sa.tech.condition.dao.entity.embedded.ExternalUserEmbedded;
import kz.kus.sa.tech.condition.enums.AbdAddressDecisionStatus;
import kz.kus.sa.tech.condition.util.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Лист исполнения по каждому адресу
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "tech_condition_execution_abd_address_decisions", schema = Constants.SCHEMA_NAME, uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_tcead_execution_address",
                columnNames = {"tech_condition_execution_id", "abd_address_id"}
        )
})
@TypeDef(name = "list-array", typeClass = ListArrayType.class)
@Where(clause = "deleted_datetime is null")
public class TechConditionExecutionAbdAddressDecisionEntity extends AbstractAuditingEntity {

    /** Лист исполнения */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tech_condition_execution_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tcead_execution"))
    private TechConditionExecutionEntity techConditionExecution;


    /** Адреса объектов из АБД */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "abd_address_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tcead_abd_address"))
    private AbdAddressEntity objectAbdAddress;


    /** Проект ТУ, созданный по данному адресу */
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "fk_tcead_project"))
    private TechConditionProjectEntity project;


    /** РЕШЕНИЕ ПО ИСПОЛНЕНИЮ
     * <p>
     * Тип решения */
    @Enumerated(EnumType.STRING)
    @Column(name = "decision_type", nullable = false)
    private TechConditionExecutionDecisionType decisionType;


    /** Если Техническая рекомендация
     * <p>
     * Точки подключения */
    @Column(name = "connection_points", columnDefinition = "text")
    private String connectionPoints;

    /** Код точки учета */
    @Column(name = "metering_point_code")
    private UUID meteringPointCode;

    /** Предусмотреть */
    @Column(name = "consider", columnDefinition = "text")
    private String consider;

    /** В ТП(ПС) установлен трансформатор */
    @Column(name = "installed_transformer")
    private String installedTransformer;

    /** Максимальная загрузка трансформатора, % */
    @Column(name = "maximum_transformer_load")
    private String maximumTransformerLoad;

    /** Наличие места установки ПУ в ТП */
    @Column(name = "exists_place_install_metering_device")
    private Boolean existsPlaceInstallMeteringDevice;

    /** Напряжение в точке присоединения */
    @Column(name = "connection_point_voltage")
    private String connectionPointVoltage;

    /** Уровень напряжения в точке подключения */
    @Column(name = "connection_point_voltage_level")
    private String connectionPointVoltageLevel;

    /** Для подключения необходимо */
    @Column(name = "required_for_connection")
    private String requiredForConnection;

    /** Требования к организации учета электроэнергии */
    @Column(name = "requirements_for_organization_electricity_metering")
    private String requirementsForOrganizationElectricityMetering;


    /** Если Мотивированный отказ
     * <p>
     * Справочник: Причина мотивированных отказов */
    @Column(name = "refusal_reason_code")
    private String refusalReasonCode;

    /** Причина отказа */
    @Column(name = "reason_for_refusal_ru", columnDefinition = "text")
    private String reasonForRefusalRu;

    /** Причина отказа */
    @Column(name = "reason_for_refusal_kk", columnDefinition = "text")
    private String reasonForRefusalKk;

    /** Дата отказа */
    @Column(name = "reason_for_refusal_datetime")
    private OffsetDateTime reasonForRefusalDatetime;

    /** Внутренний регистрационный номер */
    @Column(name = "reason_for_refusal_internal_registration_number")
    private Long reasonForRefusalInternalRegistrationNumber;

    /** Рег.номер мотивированного отказа */
    @Column(name = "reason_for_refusal_registration_number")
    private String reasonForRefusalRegistrationNumber;

    /** Номер мотивированного отказа (ручной) */
    @Column(name = "refusal_number")
    private String refusalNumber;

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

    /** Текущие исполнители */
    @Type(type = "list-array")
    @Column(name = "assignees", columnDefinition = "uuid[]")
    private List<UUID> assignees;

    /** Статус */
    @Column(name = "status_code")
    private String statusCode = AbdAddressDecisionStatus.ASSIGNED.getCode();

    /** Причина доработки */
    @Column(name = "revision_reason", columnDefinition = "TEXT")
    private String revisionReason;

    /** Дата исполнения */
    @Column(name = "executed_datetime", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime executedDatetime;

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

    /** Дата согласования */
    @Column(name = "manager_approved_datetime", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime managerApprovedDatetime;

    /** Директор */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "director_id")),
            @AttributeOverride(name = "fullName", column = @Column(name = "director_full_name")),
            @AttributeOverride(name = "providerId", column = @Column(name = "director_provider_id")),
            @AttributeOverride(name = "providerName", column = @Column(name = "director_provider_name")),
            @AttributeOverride(name = "division", column = @Column(name = "director_division")),
            @AttributeOverride(name = "position", column = @Column(name = "director_position"))
    })
    private ExternalUserEmbedded director;

    /** Дата подписания */
    @Column(name = "director_signed_datetime", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime directorSignedDatetime;
}
