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
 * Категория по надежности электроснабжения заявки на выдачу ТУ
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "tech_condition_reliability_categories", schema = Constants.SCHEMA_NAME)
@Where(clause = "deleted_datetime is null")
public class TechConditionReliabilityCategoryEntity extends AbstractAuditingEntity {

    /** Справочник: Категория по надежности электроснабжения */
    @Column(name = "reliability_category_type_code")
    private String reliabilityCategoryTypeCode;

    /** кВт */
    @Column(name = "kwt")
    private Double kwt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tech_conditions_id")
    @ToString.Exclude
    private TechConditionEntity techCondition;
}
