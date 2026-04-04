package condition.dao.entity;

import com.vladmihalcea.hibernate.type.array.ListArrayType;
import kz.kus.sa.tech.condition.dao.entity.AbstractAuditingEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionEntity;
import kz.kus.sa.tech.condition.dao.entity.TechConditionExecutionEntity;
import kz.kus.sa.tech.condition.util.Constants;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.List;

/**
 * Список пересечений
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "intersection", schema = Constants.SCHEMA_NAME)
@TypeDef(name = "list-array", typeClass = ListArrayType.class)
@Where(clause = "deleted_datetime is null")
public class IntersectionEntity extends AbstractAuditingEntity {

    /** Собственник объекта */
    @Column(name = "owner")
    private Boolean owner;

    /** Код типа объекта (справочник) */
    @Column(name = "object_type_code", columnDefinition = "varchar(30)")
    private String objectTypeCode;

    /** Рка код из сервиса АР первого объекта */
    @Column(name = "first_ar_rca_code", columnDefinition = "varchar(30)")
    private String firstArRcaCode;

    /** Полный адрес из АР кодов первого объекта */
    @Type(type = "list-array")
    @Column(name = "first_full_ar_rca_codes", columnDefinition = "varchar(30)[]")
    private List<String> firstFullArRcaCodes;

    /** Адрес первого объекта (текст, русский) */
    @Column(name = "first_location_ru")
    private String firstLocationRu;

    /** Адрес первого объекта (текст, казахский) */
    @Column(name = "first_location_kk")
    private String firstLocationKk;

    /** Рка код из сервиса АР второго объекта */
    @Column(name = "second_ar_rca_code", columnDefinition = "varchar(30)")
    private String secondArRcaCode;

    /** Полный адрес из АР кодов второго объекта */
    @Type(type = "list-array")
    @Column(name = "second_full_ar_rca_codes", columnDefinition = "varchar(30)[]")
    private List<String> secondFullArRcaCodes;

    /** Адрес второго объекта (текст, русский) */
    @Column(name = "second_location_ru")
    private String secondLocationRu;

    /** Адрес второго объекта (текст, казахский) */
    @Column(name = "second_location_kk")
    private String secondLocationKk;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_conditions_id", referencedColumnName = "id", table = "intersection", foreignKey = @ForeignKey(name = "fk_intersection_tech_conditions"))
    private TechConditionEntity techCondition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_condition_executions_id", referencedColumnName = "id", table = "intersection", foreignKey = @ForeignKey(name = "fk_intersection_tech_condition_executions"))
    private TechConditionExecutionEntity techConditionExecution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_condition_projects_id", referencedColumnName = "id", table = "intersection", foreignKey = @ForeignKey(name = "fk_intersection_tech_condition_projects"))
    private kz.kus.sa.tech.condition.dao.entity.TechConditionProjectEntity techConditionProject;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "act_of_delineations_id", referencedColumnName = "id", table = "intersection", foreignKey = @ForeignKey(name = "fk_intersection_act_of_delineations"))
//    private ActOfDelineationEntity actOfDelineation;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "archive_documents_id", referencedColumnName = "id", table = "intersection", foreignKey = @ForeignKey(name = "fk_intersection_archive_documents"))
//    private ArchiveDocumentEntity archiveDocument;

}