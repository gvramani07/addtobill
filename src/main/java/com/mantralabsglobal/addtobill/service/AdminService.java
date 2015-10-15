package com.mantralabsglobal.addtobill.service;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.mantralabsglobal.addtobill.exception.AccountExistsException;
import com.mantralabsglobal.addtobill.exception.InsufficientBalanceException;
import com.mantralabsglobal.addtobill.exception.InvalidRequestException;
import com.mantralabsglobal.addtobill.exception.MerchantDoesNotExistException;
import com.mantralabsglobal.addtobill.exception.UserAccountNotSetup;
import com.mantralabsglobal.addtobill.exception.UserExistsException;
import com.mantralabsglobal.addtobill.model.Account;
import com.mantralabsglobal.addtobill.model.AccountBalance;
import com.mantralabsglobal.addtobill.model.CreditAccount;
import com.mantralabsglobal.addtobill.model.Merchant;
import com.mantralabsglobal.addtobill.model.User;

import com.mantralabsglobal.addtobill.repository.UserRepository;
import com.mantralabsglobal.addtobill.requestModel.UserToken;
import com.mantralabsglobal.addtobill.responseModel.UserTokenResponse;

@Service
public class AdminService extends BaseService{

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserAccountService accountService;
	
	public User getUserByEmail(String email) {
		return userRepository.findOneByEmail(email);
	}
	
	public User getUserById(String userId) {
		return userRepository.findOne(userId);
	}


	public List<Account> getUserAccounts(String userId) {
		return accountRepository.findAllByOwnerId(userId);
	}
	
	public Account createUserAccount(String  userId, String currency ) throws UserExistsException, InvalidRequestException {
		Assert.notNull(userId);
		User savedUser = userRepository.findOne(userId);
		if(savedUser== null)
		{
			throw new InvalidRequestException("Invalid user Id");
		}
		else if(accountService.accountExists(savedUser, currency)){
			throw new InvalidRequestException("Account already exists ");
		}
		else{
			return accountService.createAccount(savedUser, currency);
		}
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

	public UserTokenResponse generateUserAuthToken(UserToken userToken) throws Exception {
		Assert.notNull(userToken);
		Assert.hasText(userToken.getCurrency());
		Assert.hasText(userToken.getMerchantId());
		Assert.hasText(userToken.getUserId());
		Assert.isTrue(userToken.getAmount()>0);
		
		User user = userRepository.findOne(userToken.getUserId());
		Merchant merchant = merchantRepository.findOne(userToken.getMerchantId());
		if(user == null || merchant == null)
			throw new InvalidRequestException("Invalid userid and/or merchantid");
		
		Account userAccount = accountRepository.findOneByOwnerIdAndCurrency(user.getUserId(), userToken.getCurrency());
		if(userAccount == null)
			throw new UserAccountNotSetup();
		
		double newBalance = userAccount.getAccountBalance().getRunningBalance() + userToken.getAmount();
		if(newBalance > userAccount.getUpperLimit() || newBalance < userAccount.getLowerLimit())
			throw new InsufficientBalanceException();
		
		UserToken userToken2 = new UserToken();
		userToken2.setAmount(userToken.getAmount());
		userToken2.setCurrency(userToken.getCurrency());
		userToken2.setMerchantId(userToken.getMerchantId());
		userToken2.setUserId(userToken.getUserId());
		userToken2.setExpiry(DateUtils.addMinutes(new Date(), 3).getTime());
		String token = generateToken(userToken2);
		UserTokenResponse response = new UserTokenResponse();
		response.setToken(token);
		return response;
	}
}
