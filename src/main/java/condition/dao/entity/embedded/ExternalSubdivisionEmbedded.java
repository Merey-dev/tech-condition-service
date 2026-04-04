package condition.dao.entity.embedded;

import lombok.Data;

import javax.persistence.Embeddable;
import java.util.UUID;

/**
 * Структурное подразделение
 */
@Data
@Embeddable
public class ExternalSubdivisionEmbedded {
    private UUID id;
    private String kk;
    private String ru;
    private String en;
    private String qq;
}
