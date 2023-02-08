package com.example.javakinesisconsumer.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.auth.WebIdentityTokenCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.AmazonKinesisAsync;
import com.amazonaws.services.kinesis.AmazonKinesisAsyncClientBuilder;
import com.amazonaws.services.kinesis.producer.KinesisProducerConfiguration;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * <p>
 * Class containing Beans to be instantiated only if executing in a cloud environment (dev, production).
 * If consuming from a different AWS account, this requires permissions to be assumed via IAM Role Chaining. For me details on Role chaining
 * {@see https://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles_terms-and-concepts.html}
 * <p>
 * {@code @ConditionalOnProperty} annotation will instantiate the beans in this class only if localstack is not enabled.
 * These Beans configure the Kinesis Spring Cloud Stream Binder to use IAM Role Chaining to assume permissions needed to
 * publish to Kinesis.
 *
 * <p>
 * The first IAM Role is loaded via the WebIdentityTokenCredentialsProvider AWSCredentialsProvider implementation. This
 * requires the following environment variables on the pod, which are loaded via the configured Deployment Service Account:
 *
 * <p>
 * Environment:
 * <p>
 * - AWS_ROLE_ARN: arn:aws:iam::AWS_ACCOUNT_NUMBER:role/AWS_IAM_ROLE_NAME
 * <p>
 * - AWS_WEB_IDENTITY_TOKEN_FILE:  /var/run/secrets/eks.amazonaws.com/serviceaccount/token
 * <p>
 * The IAM Role is loaded via the AWS Security Token Service (STS) API. This requires the cloud.aws.arn application
 * property to be set.
 */
@Log4j2
@Configuration
@ConditionalOnProperty(
      value = "cloud.aws.localstack.enabled",
      havingValue = "false"
)
public class KinesisConfig {

    private final String assumeRoleArn;
    private final String hostname;
    private final String region = Regions.US_WEST_2.getName();

    @Autowired
    public KinesisConfig(
          @Value("${cloud.aws.arn}") String assumeRoleArn,
          @Value("${app.meta.hostname}") String hostname
    ) {
        this.assumeRoleArn = assumeRoleArn;
        this.hostname = hostname;
    }

    /**
     * Create an AWSCredentialsProvider which has the necessary IAM Role Chaining to gain Kinesis permissions.
     *
     * <p>
     * This will first assume the IAM Role associated with the configured Service Account from the manifest file. Then, it
     * will assume the second cross-account IAM Role, which is configured in application properties.
     *
     * @return AWSCredentialsProvider to be used during Kinesis Client instantiation
     */
    @Bean
    @Primary
    public AWSCredentialsProvider getAwsCredentialProvider() {
        WebIdentityTokenCredentialsProvider webIdentityTokenCredentialsProvider = WebIdentityTokenCredentialsProvider
              .builder()
              .build();
        AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder
              .standard()
              .withRegion(region)
              .withCredentials(webIdentityTokenCredentialsProvider)
              .build();

        // the Role Session Name cannot be null. Create unique session name with current pod name and current time suffix
        String roleSessionName = hostname + "-" + System.currentTimeMillis();
        log.info("Hostname {} is assuming role: {}", hostname, assumeRoleArn);
        return new STSAssumeRoleSessionCredentialsProvider
              .Builder(assumeRoleArn, roleSessionName)
              .withStsClient(stsClient)
              .build();
    }

    @Bean
    public AmazonKinesisAsync getKinesisClientAsync(final AWSCredentialsProvider awsCredentialsProvider) {
        return AmazonKinesisAsyncClientBuilder
              .standard()
              .withCredentials(awsCredentialsProvider)
              .withRegion(region)
              .build();
    }

    @Bean
    public KinesisProducerConfiguration kinesisProducerConfiguration(final AWSCredentialsProvider awsCredentialsProvider) {
        KinesisProducerConfiguration config = new KinesisProducerConfiguration();
        config.setCredentialsProvider(awsCredentialsProvider);
        config.setRegion(region);
        return config;
    }
}
