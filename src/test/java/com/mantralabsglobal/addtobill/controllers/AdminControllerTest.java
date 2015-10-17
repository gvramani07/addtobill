package com.mantralabsglobal.addtobill.controllers;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.util.MatcherAssertionErrors.assertThat;

import java.io.IOException;
import java.util.Arrays;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mantralabsglobal.addtobill.Application;
import com.mantralabsglobal.addtobill.model.CreditAccount;
import com.mantralabsglobal.addtobill.model.DebitAccount;
import com.mantralabsglobal.addtobill.model.Merchant;
import com.mantralabsglobal.addtobill.model.User;
import com.mantralabsglobal.addtobill.repository.MerchantRepository;
import com.mantralabsglobal.addtobill.repository.UserRepository;
import com.mantralabsglobal.addtobill.requestModel.MerchantAccountRequest;
import com.mantralabsglobal.addtobill.requestModel.NewChargeRequest;
import com.mantralabsglobal.addtobill.requestModel.UserAccountRequest;
import com.mantralabsglobal.addtobill.requestModel.UserToken;
import com.mantralabsglobal.addtobill.responseModel.UserTokenResponse;
import com.mantralabsglobal.addtobill.service.AdminService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class AdminControllerTest {

	@Autowired
	private AdminService adminService;
	
	@Autowired
	private MerchantRepository merchantRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	
	 @Value("${local.server.port}")  
	 int port;
	 
	private Merchant merchant;
	private User user;
	
	 private String getBaseUrl() {
		return "http://localhost:" + port;
	 }
	private RestTemplate restTemplate = new TestRestTemplate();
		
	 
	@Before
    public void setUp() throws JsonParseException, JsonMappingException, IOException {
    	merchant = new Merchant();
		merchant.setChargesEnabled(false);
		merchant.setDefaultCurrency("inr");
		merchant.setDisplayName(ObjectId.get().toString() + " Merchant");
		merchant.setEmail("support@xyzmerchant.com");
		merchant.setMerchantName(merchant.getDisplayName());
		merchant.setSecretKey(ObjectId.get().toString());
		merchant.setTransfersEnabled(false);

		merchant = merchantRepository.save(merchant);
		
		createMerchantAccount();
    
    	user = new User();
		user.setEmail(ObjectId.get().toString() + "@atob.com");
		user.setEmailVerified(true);
		user.setPassword("secret");
		user.setRoles(Arrays.asList(User.ROLE_USER));
		
		user = userRepository.save(user);
		createUserAccount();
        
    }
	
	protected MultiValueMap<String, String> getAuthHeaders(){
		String plainCreds = "admin:admin";
		byte[] plainCredsBytes = plainCreds.getBytes();
		byte[] base64CredsBytes = Base64.encode(plainCredsBytes);
		String base64Creds = new String(base64CredsBytes);
		MultiValueMap<String, String> headers= new HttpHeaders();
		headers.add("Authorization", "Basic " + base64Creds);
		return headers;
	}
	
	
	public void createMerchantAccount() throws JsonParseException, JsonMappingException, IOException{
		
		
		MerchantAccountRequest merchantAccountRequest = new MerchantAccountRequest();
		merchantAccountRequest.setCurrency("inr");
		merchantAccountRequest.setMerchantId(merchant.getMerchantId());
		HttpEntity<MerchantAccountRequest> entity = new HttpEntity<MerchantAccountRequest>(merchantAccountRequest, getAuthHeaders());
		
		ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl() + "/admin/merchant", entity, String.class);
		assertThat( response.getStatusCode() , equalTo(HttpStatus.OK));
		ObjectMapper mapper = new ObjectMapper();
		CreditAccount acct =mapper.readValue(response.getBody(), CreditAccount.class);
		assertThat(acct.getAccountId(), notNullValue());
	}
	
	
	public void createUserAccount() throws JsonParseException, JsonMappingException, IOException{
		UserAccountRequest userAccountRequest = new UserAccountRequest();
		userAccountRequest.setCurrency("inr");
		userAccountRequest.setUserId(user.getUserId());
		HttpEntity<UserAccountRequest> entity = new HttpEntity<UserAccountRequest>(userAccountRequest, getAuthHeaders());
		
		ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl() + "/admin/user", entity, String.class);
		assertThat( response.getStatusCode() , equalTo(HttpStatus.OK));
		ObjectMapper mapper = new ObjectMapper();
		DebitAccount acct =mapper.readValue(response.getBody(), DebitAccount.class);
		assertThat(acct.getAccountId(), notNullValue());
	}
	
	@Test
	public void generateToken() throws JsonParseException, JsonMappingException, IOException{
		UserToken token = new UserToken();
		token.setAmount(200);
		token.setCurrency("inr");
		token.setMerchantId(merchant.getMerchantId());
		token.setUserId(user.getUserId());
		
		HttpEntity<UserToken> entity = new HttpEntity<UserToken>(token, getAuthHeaders());
		
		ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl() + "admin/user/authToken", entity, String.class);
		//Bad request as the merchant is not enabled for charge
		assertThat( response.getStatusCode() , equalTo(HttpStatus.BAD_REQUEST));
		
		merchant.setChargesEnabled(true);
		merchant = merchantRepository.save(merchant);
		
		response = restTemplate.postForEntity(getBaseUrl() + "admin/user/authToken", entity, String.class);
		assertThat( response.getStatusCode() , equalTo(HttpStatus.OK));
		
		
		ObjectMapper mapper = new ObjectMapper();
		UserTokenResponse acct =mapper.readValue(response.getBody(), UserTokenResponse.class);
		assertThat(acct.getToken(), notNullValue());
	}
	
	public String generateToken(UserToken token) {
		
		HttpEntity<UserToken> entity = new HttpEntity<UserToken>(token, getAuthHeaders());
		
		ResponseEntity<UserTokenResponse> response = restTemplate.postForEntity(getBaseUrl() + "admin/user/authToken", entity, UserTokenResponse.class);
		if(merchant.isChargesEnabled())
			assertThat( response.getStatusCode() , equalTo(HttpStatus.OK));
		else
		{
			assertThat( response.getStatusCode() , equalTo(HttpStatus.BAD_REQUEST));
			merchant.setChargesEnabled(true);
			merchant = merchantRepository.save(merchant);
			response = restTemplate.postForEntity(getBaseUrl() + "admin/user/authToken", entity, UserTokenResponse.class);
			assertThat( response.getStatusCode() , equalTo(HttpStatus.OK));
		}
		return response.getBody().getToken();
	}
	
	@Test
	public void createChargeRequest() throws JsonParseException, JsonMappingException, IOException{
		
		UserToken token = new UserToken();
		token.setAmount(200);
		token.setCurrency("inr");
		token.setMerchantId(merchant.getMerchantId());
		token.setUserId(user.getUserId());
		String tkn = generateToken(token);
		NewChargeRequest chargeRequest = new NewChargeRequest();
		chargeRequest.setAmount(200);
		chargeRequest.setCurrency("inr");
		chargeRequest.setMerchantId(merchant.getMerchantId());
		chargeRequest.setUserId(user.getUserId());
		chargeRequest.setToken(tkn);
		HttpEntity<NewChargeRequest> entity = new HttpEntity<NewChargeRequest>(chargeRequest, getAuthHeaders());
		
		ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl() + "/admin/charge", entity, String.class);
		assertThat( response.getStatusCode() , equalTo(HttpStatus.OK));
	}
	

	public AdminService getAdminService() {
		return adminService;
	}

	public void setAdminService(AdminService adminService) {
		this.adminService = adminService;
	}
	
}
