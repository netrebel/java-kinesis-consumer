package com.example.javakinesisconsumer.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.kinesis.AmazonKinesisAsync;
import com.amazonaws.services.kinesis.AmazonKinesisAsyncClientBuilder;
import com.amazonaws.services.kinesis.producer.KinesisProducerConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Class containing Beans to be instantiated only if executing locally (non-dev, non-prod). {@code @ConditionalOnProperty}
 * annotation will instantiate the beans in this class only if localstack is configured. These Beans configure the Kinesis
 * Spring Cloud Stream Binder to use localstack.
 *
 * <p>
 * Localstack provides a locally deployed mock of many AWS cloud services, including Kinesis. For more information
 * {@see https://github.com/localstack/localstack}.
 */
@Configuration
@ConditionalOnProperty(
      value = "cloud.aws.localstack.enabled",
      havingValue = "true"
)
public class KinesisConfigLocal {

    private final String host;
    private final int port;
    private final String region;

    public KinesisConfigLocal(
          @Value("${cloud.aws.localstack.host}") String host,
          @Value("${cloud.aws.localstack.port}") int port,
          @Value("${cloud.aws.localstack.region}") String region
    ) {
        this.host = host;
        this.port = port;
        this.region = region;
    }

    @Bean
    public AmazonKinesisAsync kinesisClientAsync(AWSCredentialsProvider awsCredentialsProvider) {
        return AmazonKinesisAsyncClientBuilder.standard()
                                              .withCredentials(awsCredentialsProvider)
                                              .withEndpointConfiguration(new EndpointConfiguration(
                                                    "http://" + host + ":" + port,
                                                    region

                                              ))
                                              .build();
    }

    @Bean
    public AmazonCloudWatch cloudWatchClient(AWSCredentialsProvider awsCredentialsProvider) {
        return AmazonCloudWatchClientBuilder.standard().withCredentials(awsCredentialsProvider)
                                            .withEndpointConfiguration(new EndpointConfiguration(
                                                  "http://" + host + ":" + port,
                                                  region

                                            )).build();
    }
}
