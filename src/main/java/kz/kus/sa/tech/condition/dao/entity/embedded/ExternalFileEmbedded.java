package kz.kus.sa.tech.condition.dao.entity.embedded;

import lombok.Data;

import javax.persistence.Embeddable;
import java.time.OffsetDateTime;

/**
 * Загруженный внешний файл
 */
@Data
@Embeddable
public class ExternalFileEmbedded {
    /** External file id */
    private String id;
    private String originName;
    private Long size;
    private OffsetDateTime uploadedDatetime;
}
