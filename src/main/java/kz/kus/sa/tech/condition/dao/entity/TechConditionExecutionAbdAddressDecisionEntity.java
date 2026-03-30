package kz.kus.sa.tech.condition.dao.entity;

import kz.kus.sa.registry.enums.TechConditionExecutionDecisionType;
import kz.kus.sa.tech.condition.util.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.OffsetDateTime;
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
}
