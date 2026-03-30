package kz.kus.sa.tech.condition;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = {"kz.kus"})
public class TechConditionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TechConditionServiceApplication.class, args);
	}

}
