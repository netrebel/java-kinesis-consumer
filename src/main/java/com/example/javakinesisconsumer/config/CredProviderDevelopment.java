package com.example.javakinesisconsumer.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CredProviderDevelopment {

    @Bean
    @Primary
    AWSCredentialsProvider credProvider() {
        return new ProfileCredentialsProvider();
    }
}
