package condition.dao.entity;

import com.vladmihalcea.hibernate.type.array.ListArrayType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import kz.kus.commons.enums.ConsumerType;
import kz.kus.sa.registry.enums.RepresentativePersonType;
import kz.kus.sa.registry.enums.SignType;
import kz.kus.sa.registry.enums.Source;
import kz.kus.sa.registry.enums.Status;
import kz.kus.sa.tech.condition.dao.entity.AbdAddressEntity;
import kz.kus.sa.tech.condition.dao.entity.embedded.ExternalFileEmbedded;
import kz.kus.sa.tech.condition.dao.entity.embedded.ExternalSubdivisionEmbedded;
import kz.kus.sa.tech.condition.dao.entity.embedded.ExternalUserEmbedded;
import kz.kus.sa.tech.condition.dao.entity.embedded.RepresentativePersonEmbedded;
import kz.kus.sa.tech.condition.util.Constants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Заявления переоформление акта разграничения балансовой принадлежности
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "act_of_delineation_renewals", schema = Constants.SCHEMA_NAME)
@TypeDef(name = "list-array", typeClass = ListArrayType.class)
@TypeDef(name = "jsonb",typeClass = JsonBinaryType.class)
public class ActOfDelineationRenewalEntity extends kz.kus.sa.tech.condition.dao.entity.AbstractAuditingEntity {

    /** Заявления */
    @Column(name = "statement_id", nullable = false)
    private UUID statementId;

    /** Рег.номер заявления */
    @Column(name = "statement_registration_number")
    private String statementRegistrationNumber;

    /** Дата регистраций */
    @Column(name = "statement_registration_datetime", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime statementRegistrationDatetime;


    /** Тип потребителя */
    @Enumerated(EnumType.STRING)
    @Column(name = "consumer_type", nullable = false)
    private ConsumerType consumerType;

    /** ИИН/БИН потребителя */
    @Column(name = "consumer_iin_bin", nullable = false)
    private String consumerIinBin;

    /** Наименование потребителя рус. */
    @Column(name = "consumer_full_name_ru", nullable = false)
    private String consumerFullNameRu;

    /** Наименование потребителя каз. */
    @Column(name = "consumer_full_name_kk", nullable = false)
    private String consumerFullNameKk;


    /** Тип представителя */
    @Enumerated(EnumType.STRING)
    @Column(name = "representative_person_type")
    private RepresentativePersonType representativePersonType;

    /** Представитель */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "firstName", column = @Column(name = "representative_first_name")),
            @AttributeOverride(name = "lastName", column = @Column(name = "representative_last_name")),
            @AttributeOverride(name = "fatherName", column = @Column(name = "representative_father_name")),
            @AttributeOverride(name = "phone", column = @Column(name = "representative_phone"))
    })
    private RepresentativePersonEmbedded representativePerson;


    /** Адреса объектов из АБД */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "act_of_delineation_renewals_id")
    @ToString.Exclude
    private List<AbdAddressEntity> objectAbdAddresses;

    /** Ответственный за электрохозяйство */
    @Column(name = "responsible_for_electrical_equipment")
    private String responsibleForElectricalEquipment;

    /** Контактный телефон ответственного за электрохозяйство */
    @Column(name = "responsible_for_electrical_equipment_contact_phone")
    private String responsibleForElectricalEquipmentContactPhone;


    /**
     * ПОДПИСЬ ПОТРЕБИТЕЛЯ
     * <p>
     * Тип подписи
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "consumer_signed_type", length = 50)
    private SignType signType;

    /** Дата / время подписания */
    @Column(name = "consumer_signed_datetime", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime consumerSignedDatetime;

    /** Файл */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "consumer_sign_file_id")),
            @AttributeOverride(name = "originName", column = @Column(name = "consumer_sign_file_name")),
            @AttributeOverride(name = "size", column = @Column(name = "consumer_sign_file_size")),
            @AttributeOverride(name = "uploadedDatetime", column = @Column(name = "consumer_sign_file_datetime", columnDefinition = "TIMESTAMP WITH TIME ZONE"))
    })
    private ExternalFileEmbedded consumerSignFile;


    /** СИСТЕМНЫЕ ПОЛЯ
     * <p>
     * Статус */
    @Column(name = "status_code", nullable = false)
    private String statusCode = Status.DRAFT.getCode();

    /** Системный источник */
    @Enumerated(EnumType.STRING)
    @Column(name = "source")
    private Source source;

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

    /** Организация поставщик */
    @Column(name = "provider_id")
    private UUID providerId;

    /** Инициатор (Абонентский отдел) */
    @Column(name = "initiator_id")
    private UUID initiatorId;

    /** Текущий пользователь */
    @Column(name = "current_user_id")
    private UUID currentUserId;

    /** Регистрация вне рабочего времени */
    @Column(name = "off_hours")
    private boolean offHours;

    /** Причина доработки */
    @Column(name = "revision_reason", columnDefinition = "text")
    private String revisionReason;

    /** Кто вернул на доработку */
    @Column(name = "revision_reason_user_id")
    private UUID revisionReasonUserId;

    /** Время отправки на доработку */
    @Column(name = "revision_reason_datetime")
    private OffsetDateTime revisionReasonDatetime;


    /** Структурное подразделение */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "subdivision_id")),
            @AttributeOverride(name = "kk", column = @Column(name = "subdivision_kk")),
            @AttributeOverride(name = "ru", column = @Column(name = "subdivision_ru")),
            @AttributeOverride(name = "en", column = @Column(name = "subdivision_en")),
            @AttributeOverride(name = "qq", column = @Column(name = "subdivision_qq"))
    })
    private ExternalSubdivisionEmbedded subdivision;


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


    /** ПОДПИСЬ РУКОВОДИТЕЛЯ
     * <p>
     * Дата / время согласования руководителя */
    @Column(name = "manager_approved_datetime", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime managerApprovedDatetime;

    /** Руководитель */
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


    /** ПОДПИСЬ ДИРЕКТОРА
     * <p>
     * Дата / время согласования директора */
    @Column(name = "director_approved_datetime", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime directorApprovedDatetime;

    /** Директор */
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
}
