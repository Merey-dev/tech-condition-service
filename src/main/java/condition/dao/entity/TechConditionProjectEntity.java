package condition.dao.entity;

import kz.kus.commons.enums.ConsumerType;
import kz.kus.sa.registry.enums.Source;
import kz.kus.sa.tech.condition.dao.entity.AbdAddressEntity;
import kz.kus.sa.tech.condition.dao.entity.AbstractAuditingEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.dao.entity.embedded.ExternalFileEmbedded;
import kz.kus.sa.tech.condition.enums.ProjectStatus;
import kz.kus.sa.tech.condition.util.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Проект выдачи ТУ
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "tech_condition_projects", schema = Constants.SCHEMA_NAME)
@Where(clause = "deleted_datetime is null")
public class TechConditionProjectEntity extends AbstractAuditingEntity {

    /** Рег.номер */
    @Column(name = "registration_number")
    private String registrationNumber;


    /** Тип потребителя */
    @Enumerated(EnumType.STRING)
    @Column(name = "consumer_type", nullable = false)
    private ConsumerType consumerType;

    /** ИИН/БИН потребителя */
    @Column(name = "consumer_iin_bin", nullable = false)
    private String consumerIinBin;

    /** Наименование потребителя рус. */
    @Column(name = "consumer_full_name_ru", nullable = false)
    private String consumerFullNameRu;

    /** Наименование потребителя каз. */
    @Column(name = "consumer_full_name_kk", nullable = false)
    private String consumerFullNameKk;


    /** Адреса объектов */
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_condition_projects_id")
    @ToString.Exclude
    private List<AbdAddressEntity> objectAbdAddresses;


    /** Место подключения */
    @Column(name = "connection_point")
    private String connectionPoint;

    /** Точки подключения */
    @Column(name = "connection_points_ru")
    private String connectionPointsRu;

    /** Точки подключения */
    @Column(name = "connection_points_kk")
    private String connectionPointsKk;

    /** Код точки подключения */
    @Column(name = "connection_point_code")
    private UUID connectionPointCode;

    /** Код точки учета */
    @Column(name = "metering_point_code")
    private UUID meteringPointCode;

    /** Справочник: Необходимость выдачи ТУ */
    @Column(name = "service_type_code")
    private String serviceTypeCode;

    /** Заявленная мощность (кВт) */
    @Column(name = "required_power")
    private Double requiredPower;

    /** Заявленная мощность (кВт, старое значение) */
    @Column(name = "old_required_power")
    private Double oldRequiredPower;

    /** Справочник: Уровень напряжения в кВт (номинальное напряжение присоединяемой установки) */
    @Column(name = "voltage_level_code")
    private String voltageLevelCode;

    /** Справочник: Характер нагрузки */
    @Column(name = "electrical_load_type_code")
    private String electricalLoadTypeCode;

    /** Справочник: Характер потребления */
    @Column(name = "consumption_type_code")
    private String consumptionTypeCode;

    /** Граница раздела */
    @Column(name = "form_section_border_ru")
    private String sectionBorderRu;

    /** Граница раздела */
    @Column(name = "form_section_border_kk")
    private String sectionBorderKk;

    /** Разрешенный коэффициент мощности */
    @Column(name = "form_allowed_power_factor")
    private String allowedPowerFactor;

    /** Технические требования */
    @Column(name = "form_technical_requirements_ru", columnDefinition = "text")
    private String technicalRequirementsRu;

    /** Технические требования */
    @Column(name = "form_technical_requirements_kk", columnDefinition = "text")
    private String technicalRequirementsKk;

    /** Технические требования - Требования системного оператора */
    @Column(name = "form_system_operator_requirements_ru")
    private String systemOperatorRequirementsRu;

    /** Технические требования - Требования системного оператора */
    @Column(name = "form_system_operator_requirements_kk")
    private String systemOperatorRequirementsKk;

    /** Технические требования - Файл требования системного оператора */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "system_operator_requirements_id")),
            @AttributeOverride(name = "originName", column = @Column(name = "system_operator_requirements_name")),
            @AttributeOverride(name = "size", column = @Column(name = "system_operator_requirements_size")),
            @AttributeOverride(name = "uploadedDatetime", column = @Column(name = "system_operator_requirements_datetime", columnDefinition = "TIMESTAMP WITH TIME ZONE"))
    })
    private ExternalFileEmbedded systemOperatorRequirementFile;

    /** Технические требования - Согласование с собственником сетей */
    @Column(name = "form_agreement_with_owner_ru", length = 256)
    private String agreementWithOwnerRu;

    /** Технические требования - Согласование с собственником сетей */
    @Column(name = "form_agreement_with_owner_kk", length = 256)
    private String agreementWithOwnerKk;

    /** Технические требования - Аннулирование ранее выданных ТУ */
    @Column(name = "form_cancellation_of_previous")
    private String cancellationOfPrevious;

    /** Усиление сети */
    @Column(name = "form_conductivity_enhancement_ru", columnDefinition = "text")
    private String conductivityEnhancementRu;

    /** Усиление сети */
    @Column(name = "form_conductivity_enhancement_kk", columnDefinition = "text")
    private String conductivityEnhancementKk;

    /** Справочник: Причина выдачи ТУ */
    @Column(name = "tech_condition_reason_code")
    private String techConditionReasonCode;

    /** Срок действия ТУ */
    @Column(name = "form_tech_condition_validity")
    private LocalDate techConditionValidity;

    /** Организация КУ с применением АСКУЭ */
    @Column(name = "form_organization_with_AECAS_ru", columnDefinition = "text")
    private String organizationWithAECASRu;

    /** Организация КУ с применением АСКУЭ */
    @Column(name = "form_organization_with_AECAS_kk", columnDefinition = "text")
    private String organizationWithAECASKk;

    /** Оснащение устройствами РЗА */
    @Column(name = "form_equipping_with_RPD_ru", columnDefinition = "text")
    private String equippingWithRPDRu;

    /** Оснащение устройствами РЗА */
    @Column(name = "form_equipping_with_RPD_kk", columnDefinition = "text")
    private String equippingWithRPDKk;

    /** Оснащение устройствами, диспетчерского управления */
    @Column(name = "form_equipping_with_SCD_ru", columnDefinition = "text")
    private String equippingWithSCDRu;

    /** Оснащение устройствами, диспетчерского управления */
    @Column(name = "form_equipping_with_SCD_kk", columnDefinition = "text")
    private String equippingWithSCDKk;

    /** Компенсации реактивной мощности */
    @Column(name = "form_reactive_power_compensation_ru", columnDefinition = "text")
    private String reactivePowerCompensationRu;

    /** Компенсации реактивной мощности */
    @Column(name = "form_reactive_power_compensation_kk", columnDefinition = "text")
    private String reactivePowerCompensationKk;

    /** Комментарий */
    @Column(name = "comment_ru", columnDefinition = "text")
    private String commentRu;

    /** Комментарий */
    @Column(name = "comment_kk", columnDefinition = "text")
    private String commentKk;

    /** Код местности ОЖТ */
    @Column(name = "ojt_locality_code")
    private String ojtLocalityCode;


    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "tech_conditions_id")
    @ToString.Exclude
    private TechConditionEntity techCondition;

//    @ManyToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "tech_condition_executions_id")
//    @ToString.Exclude
//    private TechConditionExecutionEntity techConditionExecution;


    /** СИСТЕМНЫЕ ПОЛЯ
     * <p>
     *  Внутренний регистрационный номер */
    @Column(name = "internal_registration_number")
    private Long internalRegistrationNumber;

    /** Статус */
    @Column(name = "status_code")
    private String statusCode = ProjectStatus.DRAFT.getCode();

    /** Системный источник */
    @Enumerated(EnumType.STRING)
    @Column(name = "source")
    private Source source;

    /** ID поставщика */
    @Column(name = "provider_id")
    private UUID providerId;
}
