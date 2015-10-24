package com.mantralabsglobal.addtobill.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.mantralabsglobal.addtobill.crypto.DesEncryptor;
import com.mantralabsglobal.addtobill.model.Account;
import com.mantralabsglobal.addtobill.model.Merchant;
import com.mantralabsglobal.addtobill.model.User;
import com.mantralabsglobal.addtobill.repository.AccountRepository;
import com.mantralabsglobal.addtobill.repository.ChargeRepository;
import com.mantralabsglobal.addtobill.repository.MerchantRepository;
import com.mantralabsglobal.addtobill.repository.TransactionRepository;
import com.mantralabsglobal.addtobill.repository.UserMerchantRepository;
import com.mantralabsglobal.addtobill.repository.UserRepository;
import com.mantralabsglobal.addtobill.requestModel.UserToken;

public abstract class BaseService {
	
	@Autowired
	protected AccountRepository accountRepository;
	@Autowired
	protected UserRepository userRepository;
	@Autowired
	protected ChargeRepository chargeRepository;
	@Autowired
	protected MerchantRepository merchantRepository;
	@Autowired
	protected TransactionRepository transactionRepository;
	@Autowired
	protected UserMerchantRepository userMerchantRepository;
	@Autowired
	protected DesEncryptor encrypter;
	
	protected <T> T clone(T object, Class<T> type) {
		Gson gson = new Gson();
		return gson.fromJson(gson.toJson(object), type);
	}

	
	public Account getAccountDetails(String accountId){
		return accountRepository.findOne(accountId);
	}


	public MerchantRepository getMerchantRepository() {
		return merchantRepository;
	}


	public void setMerchantRepository(MerchantRepository merchantRepository) {
		this.merchantRepository = merchantRepository;
	}

	protected String getUserIdByEmail(String email) {
		return userRepository.findOneByEmail(email).getUserId();
	}
	
	/*protected Transaction getUserTransaction(String userId, String transactionId) {
		Transaction transaction = transactionRepository.findOne(transactionId);
		UserAccount userAccount = accountRepository.findOneByUserIdAndCurrency(userId, transaction.getCurrency());
		return userAccount.getAccountId().equals(transaction.getTransactionAccountId())?transaction:null;
	}*/
	
	protected UserToken resolveToken(String token) throws Exception {
		String decryptedToken = encrypter.decrypt(token);
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(decryptedToken, UserToken.class);
	}
	
	protected String generateToken(UserToken userToken) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(userToken);
		return encrypter.encrypt(json);
	}
	
	protected User getLoggedInUser(){
		String userName = SecurityContextHolder.getContext().getAuthentication().getName();
		return userRepository.findOneByEmail(userName);
	}
	

	protected Merchant getLoggedInMerchant() {
		String merchantId = SecurityContextHolder.getContext().getAuthentication().getName();
		return merchantRepository.findOne(merchantId);
	}

}
