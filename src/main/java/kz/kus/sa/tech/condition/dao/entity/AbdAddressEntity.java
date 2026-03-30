package kz.kus.sa.tech.condition.dao.entity;

import kz.kus.sa.tech.condition.dao.entity.embedded.ExternalFileEmbedded;
import kz.kus.sa.tech.condition.util.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.List;

/**
 * Адрес объекта
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "abd_address", schema = Constants.SCHEMA_NAME)
@Where(clause = "deleted_datetime is null")
public class AbdAddressEntity extends AbstractAuditingEntity {

    /** Собственник */
    @Column(name = "owner")
    private Boolean owner;
    
    /** Код типа объекта (справочник) */
    @Column(name = "object_type_code")
    private String objectTypeCode;
    
    /** Кадастровый номер */
    @Column(name = "address_cadastral_number")
    private String cadastralNumber;
    
    /** Наименование объекта */
    @Column(name = "address_end_use_kk")
    private String endUseKk;
    @Column(name = "address_end_use_ru")
    private String endUseRu;
    
    /** Правоустанавливающие документы на объект */
    @Column(name = "address_document_kk")
    private String documentKk;
    @Column(name = "address_document_ru")
    private String documentRu;
    
    /** Этажность */
    @Column(name = "address_storeys")
    private String storeys;
    
    /** Площадь */
    @Column(name = "address_total_area")
    private String totalArea;
    
    /** Адрес */
    @Column(name = "address_location_kk", columnDefinition = "text")
    private String locationKk;
    @Column(name = "address_location_ru", columnDefinition = "text")
    private String locationRu;
    
    /** Код РКА из АР сервиса */
    @Column(name = "address_ar_rca_code")
    private String arRcaCode;

    /** Признак наличии ПУ */
    @Column(name = "meter_device_availability")
    private Boolean meterDeviceAvailability;

    /** Тип владения (property_type) */
    @Column(name = "property_type_code")
    private String propertyTypeCode;

    /** Площадь (жилая) */
    @Column(name = "living_area")
    private String livingArea;

    /** Объект в составе кондоминиума */
    @Column(name = "within_condominium")
    private Boolean withinCondominium;

    /** Объект является кондоминиумом */
    @Column(name = "is_condominium")
    private Boolean isCondominium;

    /** Добавлено вручную */
    @Column(name = "manually_added")
    private Boolean manuallyAdded;

    /** Тип объекта недвижимости */
    @Column(name = "object_type_id_kk")
    private String objectTypeIdKk;
    @Column(name = "object_type_id_ru")
    private String objectTypeIdRu;

    /** Точки присоединения */
    @Column(name = "connection_points")
    private String connectionPoints;

    /** Мощность, кВт */
    @Column(name = "power")
    private Double power;

    /** Схема раздела границ */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "border_demarcation_scheme_file_id")),
            @AttributeOverride(name = "originName", column = @Column(name = "border_demarcation_scheme_file_name")),
            @AttributeOverride(name = "size", column = @Column(name = "border_demarcation_scheme_file_size")),
            @AttributeOverride(name = "uploadedDatetime", column = @Column(name = "border_demarcation_scheme_file_datetime", columnDefinition = "TIMESTAMP WITH TIME ZONE"))
    })
    private ExternalFileEmbedded borderDemarcationSchemeFile;

    /** Требуемая мощность, кВт */
    @Column(name = "required_power")
    private Double requiredPower;

    /** Уровень напряжения */
    @Column(name = "voltage_level_code")
    private String voltageLevelCode;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_conditions_id", referencedColumnName = "id", table = "abd_address", foreignKey = @ForeignKey(name = "fk_abd_address_tech_conditions"))
    private TechConditionEntity techCondition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_condition_executions_id", referencedColumnName = "id", table = "abd_address", foreignKey = @ForeignKey(name = "fk_abd_address_tech_condition_executions"))
    private TechConditionExecutionEntity techConditionExecution;

    @OneToMany(mappedBy = "abdAddress", fetch = FetchType.LAZY)
    private List<TechConditionExecutionAbdAddressDecisionEntity> executionDecisions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_condition_projects_id", referencedColumnName = "id", table = "abd_address", foreignKey = @ForeignKey(name = "fk_abd_address_tech_condition_projects"))
    private TechConditionProjectEntity techConditionProject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "act_of_delineation_renewals_id", referencedColumnName = "id", table = "abd_address", foreignKey = @ForeignKey(name = "fk_abd_address_act_of_delineation_renewals"))
    private ActOfDelineationRenewalEntity actOfDelineationRenewal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "act_of_delineation_renewal_executions_id", referencedColumnName = "id", table = "abd_address", foreignKey = @ForeignKey(name = "fk_abd_address_act_of_delineation_renewal_executions"))
    private ActOfDelineationRenewalExecutionEntity actOfDelineationRenewalExecution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "act_of_delineations_id", referencedColumnName = "id", table = "abd_address", foreignKey = @ForeignKey(name = "fk_abd_address_act_of_delineations"))
    private ActOfDelineationEntity actOfDelineation;
}
