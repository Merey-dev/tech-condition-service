package kz.kus.sa.tech.condition.dao.entity;

import kz.kus.sa.tech.condition.util.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Where;

import javax.persistence.*;

/**
 * Акт разграничение балансовой принадлежности
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "electrified_installations", schema = Constants.SCHEMA_NAME)
@Where(clause = "deleted_datetime is null")
public class ElectrifiedInstallationEntity extends AbstractAuditingEntity {

    /** Акт разграничение балансовой принадлежности */
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "act_of_delineations_id",
            referencedColumnName = "id",
            table = "electrified_installations",
            foreignKey = @ForeignKey(name = "fk_electrified_installations_act_of_delineations"),
            nullable = false)
    private ActOfDelineationEntity actOfDelineation;

    /** Наименование электрифицированных установок (объектов) и основных источников электроснабжения */
    @Column(name = "name")
    private String name;

    /** Место нахождения установок */
    @Column(name = "location")
    private String location;

    /** Наличие резервного источника электроснабжения, принадлежность */
    @Column(name = "availability_backup_source")
    private String availabilityBackupSource;

    /** Допустимая продолжительность перерыва в электроснабжении от энергосистемы */
    @Column(name = "permissible_duration_break")
    private String permissibleDurationBreak;
}
