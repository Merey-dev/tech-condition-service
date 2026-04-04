package condition.dao.entity;

import kz.kus.sa.tech.condition.dao.entity.AbdAddressEntity;
import kz.kus.sa.tech.condition.util.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Акт разграничение балансовой принадлежности
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "act_of_delineations", schema = Constants.SCHEMA_NAME)
public class ActOfDelineationEntity extends kz.kus.sa.tech.condition.dao.entity.AbstractAuditingEntity {

//    /** ЗАЯВЛЕНИЕ */
//    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    @JoinColumn(name = "act_of_delineation_renewals_id", nullable = false)
//    private ActOfDelineationRenewalEntity actOfDelineationRenewal;

    /** Лист исполнения ЗАЯВЛЕНИЯ */
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "act_of_delineation_renewal_executions_id", nullable = false)
    private kz.kus.sa.tech.condition.dao.entity.ActOfDelineationRenewalExecutionEntity actOfDelineationRenewalExecution;


    /** Рег.номер Акта */
    @Column(name = "registration_number", nullable = false)
    private String registrationNumber;

    /** Дата составления Акта */
    @Column(name = "preparation_datetime", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime preparationDatetime;


    /** Город */
    @Column(name = "city")
    private String city;

    /** Организация поставщик */
    @Column(name = "provider_id", nullable = false)
    private UUID providerId;

    /** Подписант */
    @Column(name = "signer_id")
    private UUID signerId;

    /** ФИО подписанта */
    @Column(name = "signer_full_name")
    private String signerFullName;

    /** Должность подписанта (рус.) */
    @Column(name = "signer_position_ru")
    private String signerPositionRu;

    /** Должность подписанта (каз.) */
    @Column(name = "signer_position_kk")
    private String signerPositionKk;

    /** Адреса объектов */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "act_of_delineations_id")
    @ToString.Exclude
    private List<AbdAddressEntity> objectAbdAddresses;

    /** Электрифицированные установки */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "act_of_delineations_id")
    @ToString.Exclude
    private List<kz.kus.sa.tech.condition.dao.entity.ElectrifiedInstallationEntity> electrifiedInstallations;


    /** СИСТЕМНЫЕ ПОЛЯ
     * <p>
     * Внутренний регистрационный номер */
    @Column(name = "internal_registration_number", nullable = false)
    private Long internalRegistrationNumber;
}
