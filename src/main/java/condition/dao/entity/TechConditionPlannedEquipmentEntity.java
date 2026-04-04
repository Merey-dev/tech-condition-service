package condition.dao.entity;

import kz.kus.sa.tech.condition.dao.entity.AbstractAuditingEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.util.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * Предполагаемое оборудование к установке
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "tech_condition_planned_equipments", schema = Constants.SCHEMA_NAME)
@Where(clause = "deleted_datetime is null")
public class TechConditionPlannedEquipmentEntity extends AbstractAuditingEntity {

    /** Справочник: Тип оборудования */
    @Column(name = "equipment_type_code")
    private String equipmentTypeCode;

    /** Количество */
    @Column(name = "count")
    private Integer count;

    /** Единичная мощность */
    @Column(name = "unit_power")
    private Double unitPower;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tech_conditions_id")
    @ToString.Exclude
    private TechConditionEntity techCondition;
}
