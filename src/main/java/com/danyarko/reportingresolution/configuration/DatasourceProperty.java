package com.danyarko.reportingresolution.configuration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@PropertySources({
        @PropertySource(value = "classpath:reporting-resolution.properties"),
        @PropertySource(value = "file:reporting-resolution.properties", ignoreResourceNotFound = true),
})
@ConfigurationProperties(prefix = "reporting.resolution.datasource")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class DatasourceProperty {
    private String driverClassName;
    private String dialect;
    private String url;
    private String username;
    private String password;

    private String showSql;
}
