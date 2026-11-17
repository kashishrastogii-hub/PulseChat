package com.fullstack.chatapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** Fail loudly on a misconfigured production deployment instead of running with a demo secret. */
@Component
public class ProductionConfigValidator implements ApplicationRunner {
    private final Environment environment;
    private final String jwtSecret;

    public ProductionConfigValidator(Environment environment, @Value("${app.jwtSecret}") String jwtSecret) {
        this.environment = environment;
        this.jwtSecret = jwtSecret;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!environment.matchesProfiles("prod")) {
            return;
        }
        if (jwtSecret == null || jwtSecret.isBlank()
                || jwtSecret.contains("pulsechat=local=development=secret")
                || jwtSecret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET must be set to a unique value of at least 32 characters in prod");
        }
    }
}
