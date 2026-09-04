package com.project.souklab.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Provides a standardized application-wide {@link Clock} bean configured to UTC.
 * Ensures consistent server-side timestamp generation across environments and
 * enables deterministic, time-travel unit and integration testing.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
