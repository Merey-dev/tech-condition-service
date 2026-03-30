package kz.kus.sa.tech.condition.dao.entity.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class KzharyqDataEmbedded {

    /** Признак отправки данных по регистрации заявления в 1с Караганды Жарык */
    @Column(name = "kzh_sent_registered")
    private Boolean kzharyqSentRegistered;

    /** Признак отправки данных по выдаче ту/мот отказза/отказа пользователя в 1с Караганды Жарык */
    @Column(name = "kzh_sent_completed")
    private Boolean kzharyqSentCompleted;
}