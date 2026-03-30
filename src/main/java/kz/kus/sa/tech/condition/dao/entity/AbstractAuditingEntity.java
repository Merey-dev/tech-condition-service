package kz.kus.sa.tech.condition.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Базовая модель
 */
@Data
@MappedSuperclass
@JsonIgnoreProperties(ignoreUnknown = true)
public class AbstractAuditingEntity {

    /** Системный ID */
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    /** Дата создания */
    @CreatedDate
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false)
    private OffsetDateTime createdDatetime = OffsetDateTime.now();

    /** Дата последнего изменения */
    @LastModifiedDate
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE", nullable = false)
    private OffsetDateTime lastModifiedDatetime = OffsetDateTime.now();

    /** Дата удаления */
    @Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime deletedDatetime;
}
