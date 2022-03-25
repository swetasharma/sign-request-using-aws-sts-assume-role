package com.example.signrequestusingawssignature.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;

@Configuration
public class CloudConfig {
	@Value("${aws.clientRegion}")
	private String clientRegion;
	
	@Value("${aws.region}")
	private String awsRegion;
	
	@Value("${aws.accessKeyId}")
	private String awsAccessKeyId;
	
	@Value("${aws.awsSecretKeyId}")
	private String awsSecretKeyId;
	
	@Bean
	public AWSSecurityTokenService stsClient() {
		AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
            .withCredentials(stsTokenAWSCredentials())
            .withRegion(clientRegion)
            .build();
		return stsClient;
	}

	@Bean
	public AWSCredentialsProvider stsTokenAWSCredentials() {
		return new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsAccessKeyId, awsSecretKeyId));
	}
}