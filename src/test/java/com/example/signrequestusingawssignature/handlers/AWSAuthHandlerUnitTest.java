package com.example.signrequestusingawssignature.handlers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.time.Instant;
import java.util.Calendar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.amazonaws.DefaultRequest;
import com.amazonaws.Request;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.example.signrequestusingawssignature.config.ConfigProperties;
import com.example.signrequestusingawssignature.pojo.aws.AWSToken;


@ExtendWith(SpringExtension.class)
public class AWSAuthHandlerUnitTest {
	
	@InjectMocks
	private AWSAuthHandler testAWSAuthHandler;
	
	@Mock
	private AWSSecurityTokenService awsSecurityTokenService;
	
	@Mock
	private ConfigProperties configPropertiesMock;
	
	private static final String  PAYLOAD = "{\r\n"
			+ "    \"name\": \"Sweta Sharma\",\r\n"
			+ "}";
	
	@Test
	public void testShouldCreateNewTokenOnAppLoad() throws Exception {
		ReflectionTestUtils.setField(testAWSAuthHandler, "tokenExpiration", Instant.now());
		assertTrue(testAWSAuthHandler.shouldCreateNewToken());
	}
	
	@Test
	public void testShouldCreateNewTokenWithInExpiryTime() throws Exception {
		ReflectionTestUtils.setField(testAWSAuthHandler, "tokenExpiration", Instant.now().plusSeconds(3600));
		assertFalse(testAWSAuthHandler.shouldCreateNewToken());
	}
	
	@Test
	public void testShouldCreateNewTokenAfterExpiry() throws Exception {
		ReflectionTestUtils.setField(testAWSAuthHandler, "tokenExpiration", Instant.now().minusSeconds(120));
		assertTrue(testAWSAuthHandler.shouldCreateNewToken());
	}
	
	@Test
	public void testGetAWSSTSCredentials() throws Exception {
		AssumeRoleResult mockedAssumeRoleResult = Mockito.mock(AssumeRoleResult.class);
		Credentials sessionCredentials = getSessionCredentials();
		
		Mockito.doReturn(mockedAssumeRoleResult).when(awsSecurityTokenService).assumeRole(Mockito.any());
		Mockito.doReturn(sessionCredentials).when(mockedAssumeRoleResult).getCredentials();
		Credentials expected = testAWSAuthHandler.getAWSSTSCredentials();
		assertNotNull(expected);
	}
	
	@Test
	public void testGetAWSCredentials() throws Exception {
		Credentials sessionCredentials = getSessionCredentials();
		BasicSessionCredentials expected = testAWSAuthHandler.getAWSCredentials(sessionCredentials);
		assertNotNull(expected);
	}
	
	@Test
	public void testSetHttpHeaders() throws Exception {
		AWSToken awsToken = new AWSToken();
		awsToken.setAuthorization("Credential=ASIAXKLFTLKUM47J3EDL/20220213/us-west-2/service-api/aws4_request, SignedHeaders=content-type;host;x-amz-content-sha256;x-amz-date;x-amz-security-token, Signature=98fa606a4806e9b64e87e3e429932605ef1718c721ae6cbb15d09c5d9f86a566");
		awsToken.setDate(Calendar.getInstance().getTime().toString());
		awsToken.setToken("FwoGZXIvYXdzEPT//////////wEaDFNgYNYx/nIfPqPQ9yK0AbnuQdO/RLRBbMma+pfbJH0Xzw/UvYgU00seccV8Es6RsL1hvr4YY5ZkyyT9EyJ7fHCfH8eW/3HEeomzyVzsbeT1TXoetL6re7uy4tlciWlI856pUe+ZvNACCbgz4dxEVZD2Lij0fLxo6fvytkD7bSELaNBMgDdzymS8kZRmBprbXuUAbTm55QG/NxNNoeoanHJ3SE2RbX0GjtX79Y1OTXXuqHxOB+Di9e5al0pVymxnhHdvoSiF05OQBjItqoXZ5XDNJFi8b5BemRcnnl8z1xkEh5hFgi7VqnB2aY7j6Lku+bBdcemTZm9I");
		assertNotNull(testAWSAuthHandler.setHttpHeaders(awsToken));
	}
		
	@Test
	public void testSetAwsRequestGETParameter() throws Exception {		
		ReflectionTestUtils.setField(testAWSAuthHandler, "emailServiceConfigProperties", configPropertiesMock);
		Mockito.when(configPropertiesMock.getServiceEndpoint()).thenReturn("test");
		assertNotNull(testAWSAuthHandler.setAwsRequestGETParameter("/status/7deaed5e-3080-45ec-89ba-403977d60c0c"));
	}
	
	@Test
	public void testSetAWSRequestPOSTParameter() throws Exception {		
		ReflectionTestUtils.setField(testAWSAuthHandler, "emailServiceConfigProperties", configPropertiesMock);
		Mockito.when(configPropertiesMock.getServiceEndpoint()).thenReturn("test");
		assertNotNull(testAWSAuthHandler.setAWSRequestPOSTParameter(PAYLOAD));
	}
	
	@Test
	public void testSignRequest() throws Exception {
		Request<Void> requestAws = new DefaultRequest<Void>("sts");
		requestAws.setHttpMethod(HttpMethodName.GET);
		requestAws.setEndpoint(URI.create("https://dev.example.com/api"));
		requestAws.setResourcePath("/status/7deaed5e-3080-45ec-89ba-403977d60c0c");
		
		Credentials sessionCredentials = getSessionCredentials();
		BasicSessionCredentials awsCredentials = new BasicSessionCredentials(
				sessionCredentials.getAccessKeyId(),
				sessionCredentials.getSecretAccessKey(),
				sessionCredentials.getSessionToken());
		assertDoesNotThrow(() -> testAWSAuthHandler.signRequest(awsCredentials, requestAws));  ;
	}
	
	private static Credentials getSessionCredentials() {
		Credentials sessionCredentials = new Credentials();
		sessionCredentials.setAccessKeyId("AccessKeyId");
		sessionCredentials.setSecretAccessKey("SecretAccessKey");
		sessionCredentials.setSessionToken("FwoGZXIvYXdzEPT//////////wEaDFNgYNYx/nIfPqPQ9yK0AbnuQdO/RLRBbMma+pfbJH0Xzw/UvYgU00seccV8Es6RsL1hvr4YY5ZkyyT9EyJ7fHCfH8eW/3HEeomzyVzsbeT1TXoetL6re7uy4tlciWlI856pUe+ZvNACCbgz4dxEVZD2Lij0fLxo6fvytkD7bSELaNBMgDdzymS8kZRmBprbXuUAbTm55QG/NxNNoeoanHJ3SE2RbX0GjtX79Y1OTXXuqHxOB+Di9e5al0pVymxnhHdvoSiF05OQBjItqoXZ5XDNJFi8b5BemRcnnl8z1xkEh5hFgi7VqnB2aY7j6Lku+bBdcemTZm9I");
		sessionCredentials.setExpiration(Calendar.getInstance().getTime());
		return sessionCredentials;
	}
}
