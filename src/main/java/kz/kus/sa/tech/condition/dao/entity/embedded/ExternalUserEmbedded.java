package kz.kus.sa.tech.condition.dao.entity.embedded;

import lombok.Data;

import javax.persistence.Embeddable;
import java.util.UUID;

/**
 * Данные о пользователе
 */
@Data
@Embeddable
public class ExternalUserEmbedded {
    private UUID id;
    private String fullName;
    private UUID providerId;
    private String providerName;
    private String division;
    private String position;
}
