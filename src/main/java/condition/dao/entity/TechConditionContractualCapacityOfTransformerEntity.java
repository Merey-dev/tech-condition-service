package condition.dao.entity;

import kz.kus.sa.tech.condition.util.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * Разрешенная по договору мощность трансформаторов
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "tech_condition_contractual_capacity_of_transformers", schema = Constants.SCHEMA_NAME)
@Where(clause = "deleted_datetime is null")
public class TechConditionContractualCapacityOfTransformerEntity extends kz.kus.sa.tech.condition.dao.entity.AbstractAuditingEntity {

    /** ТП № */
    @Column(name = "transformer_number")
    private String transformerNumber;

    /** кВт */
    @Column(name = "kwt")
    private Double kwt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tech_conditions_id", nullable = false)
    @ToString.Exclude
    private kz.kus.sa.tech.condition.dao.entity.TechConditionEntity techCondition;
}
