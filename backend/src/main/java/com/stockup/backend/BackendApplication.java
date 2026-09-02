package com.stockup.backend;

import com.stockup.backend.common.config.properties.AppProperties;
import com.stockup.backend.common.config.properties.EmailProperties;
import com.stockup.backend.domain.bharosa.BharosaWeights;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties({AppProperties.class, EmailProperties.class, BharosaWeights.class})
public class BackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
