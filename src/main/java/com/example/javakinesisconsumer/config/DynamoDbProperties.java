package com.example.javakinesisconsumer.config;

import javax.validation.constraints.NotBlank;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for DynamoDB SDK version 1.x.
 */
@Getter
@Setter(AccessLevel.PACKAGE)
@ToString
@Validated
@ConfigurationProperties("amazon.dynamodb")
public class DynamoDbProperties {

    @NotBlank
    private String endpoint;
    @NotBlank
    private String region;
}
