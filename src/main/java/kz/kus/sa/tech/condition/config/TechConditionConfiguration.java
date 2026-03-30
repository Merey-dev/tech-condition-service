package kz.kus.sa.tech.condition.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@ConfigurationProperties(prefix = "tech-condition")
@Getter
@Setter
@Slf4j
public class TechConditionConfiguration {
    private Boolean async;
    private Boolean verifySignedData;
    private KafkaConfiguration kafka;

    @Getter
    @Setter
    public static class KafkaConfiguration {
        private String credentials;
        private String groupName;
        private String serverUrl;
    }

    @PostConstruct
    public void printConfig() {
        log.info("=== SERVICE CONFIGS ===");
        log.info("async = [{}]", async);
        log.info("verifySignedData = [{}]", verifySignedData);
        log.info("kafka = [{}]", kafka);
    }
}
