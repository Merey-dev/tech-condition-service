package kz.kus.sa.tech.condition.dao.entity;

import com.vladmihalcea.hibernate.type.array.ListArrayType;
import kz.kus.sa.registry.enums.SignType;
import kz.kus.sa.tech.condition.dao.entity.embedded.ExternalFileEmbedded;
import kz.kus.sa.tech.condition.dao.entity.embedded.ExternalSubdivisionEmbedded;
import kz.kus.sa.tech.condition.dao.entity.embedded.ExternalUserEmbedded;
import kz.kus.sa.tech.condition.enums.AbdAddressDecisionStatus;
import kz.kus.sa.tech.condition.util.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Переоформления Акта разграничения по каждому адресу
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "act_of_delineation_renewal_abd_address_decisions", schema = Constants.SCHEMA_NAME,
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_aodrad_renewal_address",
                        columnNames = {"act_of_delineation_renewal_id", "abd_address_id"}
                )
        }
)
@TypeDef(name = "list-array", typeClass = ListArrayType.class)
@Where(clause = "deleted_datetime is null")
public class ActOfDelineationRenewalAbdAddressDecisionEntity extends AbstractAuditingEntity {

    /** Заявление-родитель */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "act_of_delineation_renewal_id", nullable = false, foreignKey = @ForeignKey(name = "fk_aodrad_renewal"))
    private ActOfDelineationRenewalEntity actOfDelineationRenewal;

    /** Адрес объекта */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "abd_address_id", nullable = false, foreignKey = @ForeignKey(name = "fk_aodrad_abd_address"))
    private AbdAddressEntity objectAbdAddress;

    /** Сформированный по данному адресу акт */
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "act_of_delineation_id", foreignKey = @ForeignKey(name = "fk_aodrad_act"))
    private ActOfDelineationEntity actOfDelineation;

    /** Статус */
    @Column(name = "status_code")
    private String statusCode = AbdAddressDecisionStatus.ASSIGNED.getCode();

    /** Причина доработки */
    @Column(name = "revision_reason", columnDefinition = "TEXT")
    private String revisionReason;

    /** Назначено на исполнителя */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "assigned_executor_id")),
            @AttributeOverride(name = "fullName", column = @Column(name = "assigned_executor_full_name")),
            @AttributeOverride(name = "providerId", column = @Column(name = "assigned_executor_provider_id")),
            @AttributeOverride(name = "providerName", column = @Column(name = "assigned_executor_provider_name")),
            @AttributeOverride(name = "division", column = @Column(name = "assigned_executor_division")),
            @AttributeOverride(name = "position", column = @Column(name = "assigned_executor_position"))
    })
    private ExternalUserEmbedded assignedExecutor;

    /** Назначено на департамент */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "assigned_subdivision_id")),
            @AttributeOverride(name = "kk", column = @Column(name = "assigned_subdivision_kk")),
            @AttributeOverride(name = "ru", column = @Column(name = "assigned_subdivision_ru")),
            @AttributeOverride(name = "en", column = @Column(name = "assigned_subdivision_en")),
            @AttributeOverride(name = "qq", column = @Column(name = "assigned_subdivision_qq"))
    })
    private ExternalSubdivisionEmbedded assignedSubdivision;

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

    /** Дата исполнения */
    @Column(name = "executed_datetime", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime executedDatetime;

    /** Руководитель (согласование) */
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

    /** Дата согласования руководителя */
    @Column(name = "manager_approved_datetime", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime managerApprovedDatetime;

    /** Директор (подписант со стороны поставщика) */
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

    /** Дата подписания поставщиком */
    @Column(name = "provider_signed_datetime", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime providerSignedDatetime;

    /** Тип подписи потребителя */
    @Enumerated(EnumType.STRING)
    @Column(name = "consumer_signed_type", length = 50)
    private SignType consumerSignedType;

    /** Дата подписания потребителем */
    @Column(name = "consumer_signed_datetime", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime consumerSignedDatetime;

    /** Файл подписи потребителя */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "consumer_sign_file_id")),
            @AttributeOverride(name = "originName", column = @Column(name = "consumer_sign_file_name")),
            @AttributeOverride(name = "size", column = @Column(name = "consumer_sign_file_size")),
            @AttributeOverride(name = "uploadedDatetime", column = @Column(name = "consumer_sign_file_datetime", columnDefinition = "TIMESTAMP WITH TIME ZONE"))
    })
    private ExternalFileEmbedded consumerSignFile;
}
