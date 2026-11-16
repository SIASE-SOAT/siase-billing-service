package br.com.fiap.siase.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;
import java.util.Optional;

@Configuration
public class AwsConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Bean
    public SqsClient sqsClient(@Value("${app.aws.region}") String region,
                               @Value("${app.aws.endpoint:}") String endpoint) {
        var builder = SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create());
        Optional.ofNullable(endpoint).filter(e -> !e.isBlank())
                .ifPresent(e -> builder.endpointOverride(URI.create(e)));
        return builder.build();
    }
}
