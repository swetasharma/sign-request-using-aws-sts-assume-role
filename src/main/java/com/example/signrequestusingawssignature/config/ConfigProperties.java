package com.example.signrequestusingawssignature.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@Data
public class ConfigProperties {

	@Value("${aws.clientRegion}")
	private String clientRegion;
	
	@Value("${aws.roleARN}")
	private String roleARN;
	
	@Value("${aws.roleSessionName}")
	private String roleSessionName;
	
	@Value("${api.proxy.host}")
	private String serviceEndpoint;

}
