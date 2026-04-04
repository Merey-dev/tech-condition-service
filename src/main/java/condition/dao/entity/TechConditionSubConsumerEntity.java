package condition.dao.entity;

import kz.kus.sa.tech.condition.util.Constants;
import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * Суб потребители потребителя заявки на выдачу ТУ
 */
@Getter
@Setter
@Entity
@Table(name = "tech_condition_sub_consumers", schema = Constants.SCHEMA_NAME)
@Where(clause = "deleted_datetime is null")
public class TechConditionSubConsumerEntity extends kz.kus.sa.tech.condition.dao.entity.AbstractAuditingEntity {

    /** ФИО субпотребителя / Наименование юридического лица */
    @Column(name = "full_name")
    private String fullName;

    /** Справочник: Наименование объекта */
    @Column(name = "object_type_code")
    private String objectTypeCode;

    /** Другое */
    @Column(name = "other_type")
    private String otherType;

    /** Местонахождение объекта */
    @Column(name = "object_location")
    private String objectLocation;

    /** Потребляемая мощность */
    @Column(name = "power_consumption")
    private String powerConsumption;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tech_conditions_id")
    @ToString.Exclude
    private kz.kus.sa.tech.condition.dao.entity.TechConditionEntity techCondition;
}
