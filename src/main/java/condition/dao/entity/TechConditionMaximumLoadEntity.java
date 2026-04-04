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
 * Максимальная нагрузка после ввода в эксплуатацию по годам
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "tech_condition_maximum_loads", schema = Constants.SCHEMA_NAME)
@Where(clause = "deleted_datetime is null")
public class TechConditionMaximumLoadEntity extends AbstractAuditingEntity {

    /** Период */
    @Column(name = "period")
    private String period;

    /** Максимальная нагрузка */
    @Column(name = "load")
    private Double load;

    /** Справочник: Категория по надежности электроснабжения */
    @Column(name = "reliability_category_type_code")
    private String reliabilityCategoryTypeCode;

    /** из указанной макс. нагрузки относятся к электроприемникам, кВт */
    @Column(name = "electrical_receivers_load_kw")
    private Double electricalReceiversLoadKw;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tech_conditions_id")
    @ToString.Exclude
    private TechConditionEntity techCondition;
}
