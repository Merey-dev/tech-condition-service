package kz.kus.sa.tech.condition.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.array.ListArrayType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import kz.kus.commons.enums.ConsumerType;
import kz.kus.sa.registry.dto.common.DocumentTypeDto;
import kz.kus.sa.registry.enums.*;
import kz.kus.sa.tech.condition.dao.entity.embedded.*;
import kz.kus.sa.tech.condition.util.Constants;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Заявление на выдачу ТУ
 */
@Getter
@Setter
@Entity
@Table(name = "tech_conditions", schema = Constants.SCHEMA_NAME)
@TypeDef(name = "list-array", typeClass = ListArrayType.class)
@TypeDef(name = "jsonb",typeClass = JsonBinaryType.class)
public class TechConditionEntity extends AbstractAuditingEntity {

    /** Заявления */
    @Column(name = "statement_id", nullable = false)
    private UUID statementId;

    /** Рег.номер заявления */
    @Column(name = "statement_registration_number")
    private String statementRegistrationNumber;

    /** Дата регистраций */
    @Column(name = "statement_registration_datetime", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime statementRegistrationDatetime;


    /** № Технического условия */
    @Column(name = "tech_condition_number")
    private String techConditionNumber;

    /** Дата выдачи ТУ */
    @Column(name = "tech_condition_date")
    private LocalDate techConditionDate;

    /** Дата подачи заявления */
    @Column(name = "application_datetime")
    private OffsetDateTime applicationDatetime;

    /** Срок исполнения */
    @Column(name = "deadline_datetime")
    private LocalDateTime deadlineDatetime;

    /** Номер ранее выданного ТУ */
    @Column(name = "previous_registration_number")
    private String previousRegistrationNumber;


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


    /** Тип представителя */
    @Enumerated(EnumType.STRING)
    @Column(name = "representative_person_type")
    private RepresentativePersonType representativePersonType;

    /** Представитель */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "firstName", column = @Column(name = "representative_first_name")),
            @AttributeOverride(name = "lastName", column = @Column(name = "representative_last_name")),
            @AttributeOverride(name = "fatherName", column = @Column(name = "representative_father_name")),
            @AttributeOverride(name = "phone", column = @Column(name = "representative_phone"))
    })
    private RepresentativePersonEmbedded representativePerson;


    /** Документы, прилагаемые к заявлению */
    @Type(type = "jsonb")
    @Column(name = "documents_with_type_codes", columnDefinition = "jsonb")
    private List<DocumentTypeDto> documentsWithTypeCodes;


    /** Адреса объектов из АБД */
    @OneToMany(mappedBy = "techCondition", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<AbdAddressEntity> objectAbdAddresses;

    /** Список пересечений */
    @OneToMany(mappedBy = "techCondition", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<IntersectionEntity> intersections;


    /**
     * ПОДПИСЬ ПОТРЕБИТЕЛЯ
     * <p>
     * Тип подписи
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "consumer_signed_type", length = 50)
    private SignType signType;

    /** Дата / время подписания */
    @Column(name = "consumer_signed_datetime", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime consumerSignedDatetime;


    /** Отметить как бумажный */
    @Column(name = "is_paper")
    private Boolean isPaper;

    /** Обращение по доверенности */
    @Column(name = "is_by_attorney")
    private Boolean isByAttorney;

    /** Файл доверенности */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "attorney_file_id")),
            @AttributeOverride(name = "originName", column = @Column(name = "attorney_file_name")),
            @AttributeOverride(name = "size", column = @Column(name = "attorney_file_size")),
            @AttributeOverride(name = "uploadedDatetime", column = @Column(name = "attorney_file_datetime", columnDefinition = "TIMESTAMP WITH TIME ZONE"))
    })
    private ExternalFileEmbedded attorneyFile;


    /** Справочник: Необходимость выдачи ТУ */
    @Column(name = "service_type_code")
    private String serviceTypeCode;

    /** Справочник: Причина выдачи ТУ */
    @Column(name = "tech_condition_reason_code")
    private String techConditionReasonCode;

    /** Место подключения */
    @Column(name = "connection_point")
    private String connectionPoint;

    /** Код точки подключения */
    @Column(name = "connection_point_code")
    private UUID connectionPointCode;

    /** Требуемая мощность, кВт */
    @Column(name = "required_power")
    private Double requiredPower;

    /** Имеющаяся мощность, кВт */
    @Column(name = "available_power")
    private Double availablePower;

    /** Справочник: Уровень напряжения в кВт (номинальное напряжение присоединяемой установки) */
    @Column(name = "voltage_level_code")
    private String voltageLevelCode;

    /** Наличие субпотребителей */
    @Column(name = "has_sub_consumers")
    private Boolean hasSubConsumers;

    /** Справочник: Характер потребления */
    @Column(name = "consumption_type_code")
    private String consumptionTypeCode;


    /** ОПРОСНЫЙ ЛИСТ
     * <p>
     * Срок строительства по нормам */
    @Column(name = "completion_of_construction")
    private String completionOfConstruction;

    /** Количество квартир (номеров, кабинетов) */
    @Column(name = "number_of_apartments")
    private Integer numberOfApartments;

    /** Справочник: Характер нагрузки (фаза) */
    @Column(name = "electrical_load_type_code")
    private String electricalLoadTypeCode;

    /** Существующая максимальная нагрузка */
    @Column(name = "existing_maximum_load")
    private String existingMaximumLoad;


    /** Суб.потребители */
    @Transient
    @JsonIgnore
    @OneToMany(mappedBy = "techCondition", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<TechConditionSubConsumerEntity> subConsumers;

    /** Максимальная нагрузка после ввода в эксплуатацию по годам
     * (нарастающим итогом с учетом существующей нагрузки) */
    @Transient
    @JsonIgnore
    @OneToMany(mappedBy = "techCondition", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<TechConditionMaximumLoadEntity> maximumLoads;

    /** Предполагаемое оборудование к установке */
    @Transient
    @JsonIgnore
    @OneToMany(mappedBy = "techCondition", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<TechConditionPlannedEquipmentEntity> plannedEquipments;

    /** Разрешенная по договору мощность трансформаторов */
    @Transient
    @JsonIgnore
    @OneToMany(mappedBy = "techCondition", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<TechConditionContractualCapacityOfTransformerEntity> contractualCapacityOfTransformers;

    /** Категории по надежности электроснабжения */
    @Transient
    @JsonIgnore
    @OneToMany(mappedBy = "techCondition", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<TechConditionReliabilityCategoryEntity> reliabilityCategories;


    /** Имеет открытые листы исполнения */
    @Column(name = "has_active_executions")
    private Boolean hasActiveExecutions = false;

    /** Имеет параллельное исполнение */
    @Column(name = "has_parallel_executions")
    private Boolean hasParallelExecutions = false;

    /** Является государственным органом */
    @Column(name = "gov_agency")
    private Boolean govAgency = false;


//    /** Тип решения */
//    @Enumerated(EnumType.STRING)
//    @Column(name = "decision_type", length = 50)
//    private TechConditionExecutionDecisionType decisionType;
//
//    /** Если Техническая рекомендация
//     * <p>
//     * Точки подключения */
//    @Column(name = "connection_points", columnDefinition = "text")
//    private String connectionPoints;
//
//    /** Предусмотреть */
//    @Column(name = "consider", columnDefinition = "text")
//    private String consider;
//
//    /** В ТП(ПС) установлен трансформатор */
//    @Column(name = "installed_transformer")
//    private String installedTransformer;
//
//    /** Максимальная загрузка трансформатора, % */
//    @Column(name = "maximum_transformer_load")
//    private String maximumTransformerLoad;
//
//    /** Наличие места установки ПУ в ТП */
//    @Column(name = "exists_place_install_metering_device")
//    private Boolean existsPlaceInstallMeteringDevice;
//
//    /** Напряжение в точке присоединения */
//    @Column(name = "connection_point_voltage")
//    private String connectionPointVoltage;
//
//    /** Уровень напряжения в точке подключения */
//    @Column(name = "connection_point_voltage_level")
//    private String connectionPointVoltageLevel;
//
//    /** Для подключения необходимо */
//    @Column(name = "required_for_connection")
//    private String requiredForConnection;
//
//    /** Требования к организации учета электроэнергии */
//    @Column(name = "requirements_for_organization_electricity_metering")
//    private String requirementsForOrganizationElectricityMetering;
//
//
//    /** Если Мотивированный отказ
//     * <p>
//     * Справочник: Причина мотивированных отказов */
//    @Column(name = "refusal_reason_code")
//    private String refusalReasonCode;
//
//    /** Причина отказа */
//    @Column(name = "reason_for_refusal_ru", columnDefinition = "text")
//    private String reasonForRefusalRu;
//
//    /** Причина отказа */
//    @Column(name = "reason_for_refusal_kk", columnDefinition = "text")
//    private String reasonForRefusalKk;
//
//    /** Дата отказа */
//    @Column(name = "reason_for_refusal_datetime")
//    private OffsetDateTime reasonForRefusalDatetime;
//
//    /** Внутренний регистрационный номер */
//    @Column(name = "reason_for_refusal_internal_registration_number")
//    private Long reasonForRefusalInternalRegistrationNumber;
//
//    /** Рег.номер мотивированного отказа */
//    @Column(name = "reason_for_refusal_registration_number")
//    private String reasonForRefusalRegistrationNumber;
//
//    /** Номер мотивированного отказа (ручной) */
//    @Column(name = "refusal_number")
//    private String refusalNumber;


    /** ВОЗВРАТ ПОТРЕБИТЕЛЮ
     * <p>
     * Комментарий возврата потребителю */
    @Column(name = "return_to_customer_comment", columnDefinition = "TEXT")
    private String returnToCustomerComment;

    /** Дата возврата потребителю на доработку */
    @Column(name = "return_to_customer_date", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime returnToCustomerDate;


    /** ФАЙЛ ОТКАЗА ПОТРЕБИТЕЛЯ */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "consumer_refusal_file_id")),
            @AttributeOverride(name = "originName", column = @Column(name = "consumer_refusal_file_name")),
            @AttributeOverride(name = "size", column = @Column(name = "consumer_refusal_file_size")),
            @AttributeOverride(name = "uploadedDatetime", column = @Column(name = "consumer_refusal_file_datetime", columnDefinition = "TIMESTAMP WITH TIME ZONE"))
    })
    private ExternalFileEmbedded consumerRefusalFile;


//    /** ДАННЫЕ ФОРМИРОВАНИЯ ТУ */
//    @OneToMany(mappedBy = "techCondition", cascade = CascadeType.ALL)
//    private List<TechConditionProjectEntity> projects;


    /** ДАННЫЕ ПО "КАРАГАНДЫ ЖАРЫК" */
    @Embedded
    private KzharyqDataEmbedded kzharyqData;


    /** СИСТЕМНЫЕ ПОЛЯ
     * <p>
     * Статус */
    @Column(name = "status_code", nullable = false)
    private String statusCode = Status.DRAFT.getCode();

    /** Системный источник */
    @Enumerated(EnumType.STRING)
    @Column(name = "source")
    private Source source;

    /** Текущие исполнители */
    @Type(type = "list-array")
    @Column(name = "assignees", columnDefinition = "uuid[]")
    private List<UUID> assignees;

    /** Связанные пользователи */
    @Type(type = "list-array")
    @Column(name = "related_users", columnDefinition = "uuid[]")
    private List<UUID> relatedUsers;

    /** Кем назначено */
    @Column(name = "assigned_by")
    private UUID assignedBy;

    /** Организация поставщик */
    @Column(name = "provider_id")
    private UUID providerId;

    /** Инициатор (Абонентский отдел) */
    @Column(name = "initiator_id")
    private UUID initiatorId;

    /** Текущий пользователь */
    @Column(name = "current_user_id")
    private UUID currentUserId;

    /** Копия */
    @Column(name = "copy")
    private String copy;

    /** Регистрация вне рабочего времени */
    @Column(name = "off_hours")
    private boolean offHours;


    /** Структурное подразделение */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "subdivision_id")),
            @AttributeOverride(name = "kk", column = @Column(name = "subdivision_kk")),
            @AttributeOverride(name = "ru", column = @Column(name = "subdivision_ru")),
            @AttributeOverride(name = "en", column = @Column(name = "subdivision_en")),
            @AttributeOverride(name = "qq", column = @Column(name = "subdivision_qq"))
    })
    private ExternalSubdivisionEmbedded subdivision;


    /** Текущий исполнитель */
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


    /** ПОДПИСЬ РУКОВОДИТЕЛЯ
     * <p>
     * Дата / время подписания руководителя */
    @Column(name = "manager_signed_datetime", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime managerSignedDatetime;

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


    /** ПОДПИСЬ ДИРЕКТОРА
     * <p>
     * Дата / время подписания директора */
    @Column(name = "director_signed_datetime", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime directorSignedDatetime;

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
}
