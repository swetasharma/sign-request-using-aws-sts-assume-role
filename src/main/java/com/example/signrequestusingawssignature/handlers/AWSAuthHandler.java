package com.example.signrequestusingawssignature.handlers;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.DefaultRequest;
import com.amazonaws.Request;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.example.signrequestusingawssignature.config.ConfigProperties;
import com.example.signrequestusingawssignature.constants.ServiceConstant;
import com.example.signrequestusingawssignature.pojo.aws.AWSToken;

@Service
public class AWSAuthHandler {
	private static Logger logger = LoggerFactory.getLogger(AWSAuthHandler.class);
	
	private static Instant tokenExpiration = Instant.now();
	private static Credentials sessionCredentials = new Credentials();
	
	@Autowired
	private AWSSecurityTokenService awsSecurityTokenService;
	
	@Autowired
	private ConfigProperties configProperties;
		
	public Credentials getEmailServiceApiAuthorization() throws Exception {
		try {
			if(shouldCreateNewToken()) {
				sessionCredentials = getAWSSTSCredentials();
				tokenExpiration = sessionCredentials.getExpiration().toInstant();
				logger.info("Token Expiration time is " + tokenExpiration + " Current time is "+ Instant.now());
			}
		}
		catch(AmazonServiceException e) {
			logger.error("An error while creating credentials : " + sessionCredentials, e);
		}
		catch(SdkClientException e) {
			logger.error("An error while creating credentials : " + sessionCredentials, e);
		}	
		return sessionCredentials;
	}
	
	public boolean shouldCreateNewToken() {
		Instant currentTimestamp = Instant.now(); 
		Duration differenceInTime = Duration.between(currentTimestamp, tokenExpiration);

		if (currentTimestamp.compareTo(tokenExpiration) > 0 || differenceInTime.getSeconds() < 120) {
			logger.info("Getting new AWS Credentials");
			return true;
		}
		logger.info("Using existing valid credentials");
		return false;
	}
	
	public Credentials getAWSSTSCredentials() throws Exception {
		AssumeRoleRequest roleRequest = new AssumeRoleRequest()
		                .withRoleArn(configProperties.getRoleARN())
		                .withRoleSessionName(configProperties.getRoleSessionName());
		
		AssumeRoleResult roleResponse = awsSecurityTokenService.assumeRole(roleRequest);
		
		Credentials sessionCredentials = roleResponse.getCredentials();
		logger.info("Credentials generated for transcational Email: {} " + sessionCredentials);
		
		return sessionCredentials;
	}
	
	public void signRequest(BasicSessionCredentials awsCredentials, Request<Void> requestAws) {
		AWS4Signer signer = new AWS4Signer();
	    signer.setServiceName(ServiceConstant.SERVICE_NAME);
	    signer.setRegionName(ServiceConstant.AWS_REGION);
	    signer.sign(requestAws, awsCredentials);
	}

	public BasicSessionCredentials getAWSCredentials(Credentials sessionCredentials) {
		BasicSessionCredentials awsCredentials = new BasicSessionCredentials(
		sessionCredentials.getAccessKeyId(),
		sessionCredentials.getSecretAccessKey(),
		sessionCredentials.getSessionToken());
		return awsCredentials;
	}
	
	public Request<Void> setAWSRequestPOSTParameter(String payload) {
		Request<Void> requestAws = new DefaultRequest<Void>("sts");
		requestAws.setHttpMethod(HttpMethodName.POST);
		requestAws.setEndpoint(URI.create(configProperties.getServiceEndpoint()));
		requestAws.addHeader("Content-Type", "application/json");
		requestAws.setResourcePath(ServiceConstant.SEND_TRANSACTIONAL_EMAIL);
		requestAws.setContent(new ByteArrayInputStream(payload.getBytes()));		
		return requestAws;
	}
	
	public Request<Void> setAwsRequestGETParameter(String setStatusPath) {
		Request<Void> requestAws = new DefaultRequest<Void>("sts");
		requestAws.setHttpMethod(HttpMethodName.GET);
		requestAws.setEndpoint(URI.create(configProperties.getServiceEndpoint()));
		requestAws.setResourcePath(setStatusPath);
		return requestAws;
	}
	
	public AWSToken setAWSTokenHeaders(Map<String, String> headers) {
		AWSToken awsToken = new AWSToken();
	    for (Map.Entry<String, String> entry : headers.entrySet()) {
	        if (entry.getKey().equals("X-Amz-Security-Token")) {
	        	awsToken.setToken( entry.getValue());
	        }
	        if (entry.getKey().equals("X-Amz-Date")) {
	        	awsToken.setDate(entry.getValue());
	        }
	        if (entry.getKey().equals("Authorization")) {
	            awsToken.setAuthorization(entry.getValue());
	        }
	    }
		return awsToken;
	}
	
	public HttpHeaders setHttpHeaders(AWSToken awsToken) {
		HttpHeaders httpHeaders = new HttpHeaders();		
		httpHeaders.add("Content-Type", "application/json");
		httpHeaders.add("X-Amz-Date", awsToken.getDate());
		httpHeaders.add("X-Amz-Security-Token", awsToken.getToken());
		httpHeaders.add("Authorization", awsToken.getAuthorization());
		return httpHeaders;
	}
}
