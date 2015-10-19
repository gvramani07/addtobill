package com.mantralabsglobal.addtobill.service;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.mantralabsglobal.addtobill.exception.AccountExistsException;
import com.mantralabsglobal.addtobill.exception.MerchantDoesNotExistException;
import com.mantralabsglobal.addtobill.model.Account;
import com.mantralabsglobal.addtobill.model.AccountBalance;
import com.mantralabsglobal.addtobill.model.CreditAccount;
import com.mantralabsglobal.addtobill.model.Merchant;
import com.mantralabsglobal.addtobill.model.User;

import com.mantralabsglobal.addtobill.repository.UserRepository;

@Service
public class AdminService extends BaseService{

	@Autowired
	private UserRepository userRepository;
	
	public User getUserByEmail(String email) {
		return userRepository.findOneByEmail(email);
	}
	
	public User getUserById(String userId) {
		return userRepository.findOne(userId);
	}


	public List<Account> getUserAccounts(String userId) {
		return accountRepository.findAllByOwnerId(userId);
	}
	
	public Account createMerchantAccount(String merchantId, String currency) throws AccountExistsException, MerchantDoesNotExistException {
		Assert.notNull(merchantId, "Invalid merchant Id");
		Assert.hasText(currency, "Invalid currency");
		Merchant m = merchantRepository.findOne(merchantId);
		if(m != null)
		{
			Account acct = accountRepository.findOneByOwnerIdAndCurrency(merchantId, currency);
			if(acct == null)
			{
				CreditAccount account = new CreditAccount();
				account.setOwnerId(m.getMerchantId());
				
				account.setLowerLimit(0);
				account.setUpperLimit(Double.MAX_VALUE);
				
				account.setAccountBalance(new AccountBalance());
				account.getAccountBalance().setBalanceUpdateDate(new Date().getTime());
				
				account.setCurrency(currency);
				
				return accountRepository.save(account);
			}
			else
			{
				throw new AccountExistsException();
			}
			
		}
		else
		{
			throw new MerchantDoesNotExistException();
		}
	}

	public User updateUser(User user) {
		Assert.notNull(user);
		Assert.hasText(user.getUserId());
		Assert.hasText(user.getEmail());
		Assert.hasText(user.getPassword());
		Assert.notEmpty(user.getRoles());
		return userRepository.save(user);
	}

	public Merchant getMerchant(String merchantId) {
		return merchantRepository.findOne(merchantId);
	}


	public Merchant createMerchant(Merchant merchant) throws AccountExistsException, MerchantDoesNotExistException {
		Assert.notNull(merchant);
		
		Assert.hasText(merchant.getDefaultCurrency(), "Default currency not specified");
		Assert.hasText(merchant.getDisplayName(), "Display name not speciied");
		Assert.hasText(merchant.getMerchantName(), "Merchant name not specified");
		Assert.hasText(merchant.getSupportEmail(), "Support Email not specified");
		Assert.hasText(merchant.getSupportPhone(), "Support phone not specified");
		Assert.hasText(merchant.getBusinessUrl(), "Business URL not specified");
		Assert.hasText(merchant.getEmail(), "Email not spscified");
		
		Merchant m = merchantRepository.findOneByBusinessUrl(merchant.getBusinessUrl());
		Assert.isNull(m, "Duplicate business url");
		m = merchantRepository.findOneByMerchantName(merchant.getMerchantName());
		Assert.isNull(m, "Duplicate business name");
		
		m = merchantRepository.findOneByDisplayName(merchant.getDisplayName());
		Assert.isNull(m, "Duplicate display name");
		
		merchant.setChargesEnabled(false);
		merchant.setTransfersEnabled(false);
		merchant.setSecretKey(ObjectId.get().toString());
		merchant = merchantRepository.save(merchant);
		
		createMerchantAccount(merchant.getMerchantId(), merchant.getDefaultCurrency());
		
		return merchant;
	}

	public Merchant updateMerchant(Merchant merchant) {
		Assert.notNull(merchant);
		Merchant m = merchantRepository.findOne(merchant.getMerchantId());
		Assert.notNull(m, "Merchant does not exist");
		
		if(StringUtils.isNotEmpty(merchant.getBusinessPrimaryColor()))
			m.setBusinessPrimaryColor(merchant.getBusinessPrimaryColor());
		if(StringUtils.isNotEmpty(merchant.getBusinessUrl()))
			m.setBusinessUrl(merchant.getBusinessUrl());
		if(StringUtils.isNotEmpty(merchant.getDefaultCurrency()))
			m.setDefaultCurrency(merchant.getDefaultCurrency());
		if(StringUtils.isNotEmpty(merchant.getDisplayName()))
			m.setDisplayName(merchant.getDisplayName());
		if(StringUtils.isNotEmpty(merchant.getEmail()))
			m.setEmail(merchant.getEmail());
		if(StringUtils.isNotEmpty(merchant.getMerchantName()))
			m.setMerchantName(merchant.getMerchantName());
		if(StringUtils.isNotEmpty(merchant.getSecretKey()))
			m.setSecretKey(merchant.getSecretKey());
		if(StringUtils.isNotEmpty(merchant.getSupportEmail()))
			m.setSupportEmail(merchant.getSupportEmail());
		if(StringUtils.isNotEmpty(merchant.getSupportPhone()))
			m.setSupportPhone(merchant.getSupportPhone());
		if(StringUtils.isNotEmpty(merchant.getSupportUrl()))
			m.setSupportUrl(merchant.getSupportUrl());
		
		m = merchantRepository.save(m);
		return m;
	}
}
