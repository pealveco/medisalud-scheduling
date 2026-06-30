package co.com.medisalud.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import java.time.Clock;

@Configuration
@ComponentScan(basePackages = "co.com.medisalud.usecase",
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "^.+UseCase$")
        },
        useDefaultFilters = false)
public class UseCasesConfig {

    /**
     * Provides the application clock used by use cases that depend on the current date-time.
     *
     * @return system clock for runtime execution
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
