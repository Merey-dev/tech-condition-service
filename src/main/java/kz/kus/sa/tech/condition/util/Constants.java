package kz.kus.sa.tech.condition.util;

public interface Constants {

    String SCHEMA_NAME = "tech_condition_service";

    String PHONE_NUMBERS = "^\\+(\\d)(\\d{3})(\\d{3})(\\d{2})(\\d{2})$";
    String REGEXP_EMAIL = ".+[@].+[\\.].+";
    String DATE_FORMAT = "dd.MM.yyyy";
    String DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm:ss";

    Integer PROCESSING_DAYS = 5;
}
