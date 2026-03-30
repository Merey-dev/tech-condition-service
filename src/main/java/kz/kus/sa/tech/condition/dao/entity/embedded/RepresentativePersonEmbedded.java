package kz.kus.sa.tech.condition.dao.entity.embedded;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Данные представителя
 */
@Data
@Embeddable
public class RepresentativePersonEmbedded {

    /** Имя представителя */
    @Column(name = "representative_first_name")
    private String firstName;

    /** Фамилия представителя */
    @Column(name = "representative_last_name")
    private String lastName;

    /** Отчество представителя */
    @Column(name = "representative_father_name")
    private String fatherName;

    /** Номер телефона заявителя */
    @Column(name = "representative_phone")
    private String phone;
}
