package com.example.javakinesisconsumer.config;

import static com.amazonaws.SDKGlobalConfiguration.AWS_ROLE_ARN_ENV_VAR;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


/**
 * DynamoDb related beans for SDK version 1.x.
 */
@Slf4j
@EnableConfigurationProperties(com.example.javakinesisconsumer.config.DynamoDbProperties.class)
@Configuration
public class DynamoDbConfiguration {

    private final DynamoDbProperties dbProperties;

    DynamoDbConfiguration(DynamoDbProperties dbProperties) {
        this.dbProperties = dbProperties;
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "amazonDynamoDbConfig")
    AmazonDynamoDB amazonDynamoDbConfig(CredProviderDevelopment localCredProvider) {

        AWSCredentialsProvider awsCredentialsProvider = localCredProvider.credProvider();
        AmazonDynamoDB amazonDynamoDb =
              AmazonDynamoDBClientBuilder.standard()
                                         .withEndpointConfiguration(
                                               new AwsClientBuilder.EndpointConfiguration(
                                                     dbProperties.getEndpoint(),
                                                     dbProperties.getRegion()))
                                         .withCredentials(awsCredentialsProvider)
                                         .build();

        // the following provides debug info for problems with credentials
        // only log first 5 characters of access keys >= 10 characters
        String accessKeyId = awsCredentialsProvider.getCredentials().getAWSAccessKeyId();
        accessKeyId = (accessKeyId == null)
              ? "none"
              : (accessKeyId.length() < 10)
                    ? "too short" : (accessKeyId.substring(0, 5) + "...");
        String roleArn = System.getenv(AWS_ROLE_ARN_ENV_VAR);
        roleArn = (roleArn == null) ? "none" : roleArn;
        log.info("AWS Credentials: provider [{}], access key [{}], role [{}]",
              awsCredentialsProvider.getCredentials(), accessKeyId, roleArn);

        log.info("Connecting to DynamoDB with {}", dbProperties);
        return amazonDynamoDb;
    }
}
