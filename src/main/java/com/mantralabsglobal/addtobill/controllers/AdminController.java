package com.mantralabsglobal.addtobill.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mantralabsglobal.addtobill.exception.ResourceNotFoundException;
import com.mantralabsglobal.addtobill.exception.UserExistsException;
import com.mantralabsglobal.addtobill.model.Account;
import com.mantralabsglobal.addtobill.model.User;
import com.mantralabsglobal.addtobill.service.AdminService;

@RestController
public class AdminController {

	@Autowired
	private AdminService adminService;
	
	
	@RequestMapping(value="admin/user", method=RequestMethod.PUT)
	public User updateUser(@RequestBody User user) throws UserExistsException{
		return adminService.updateUser(user);
	}
	
	@RequestMapping(value="admin/accounts", method=RequestMethod.GET)
	public List<Account> getUserAccounts(@RequestParam(value="id") String userId) throws ResourceNotFoundException{
		return adminService.getUserAccounts(userId);
	}

	@RequestMapping(value="admin/account", method=RequestMethod.GET)
	public Account getAccount(@RequestParam(value="id") String accountId) throws ResourceNotFoundException{
		Account acct = adminService.getAccountDetails(accountId);
		if(acct != null)
			return acct;
		throw new ResourceNotFoundException(); 
	}
	
	public AdminService getAdminService() {
		return adminService;
	}

	public void setAdminService(AdminService adminService) {
		this.adminService = adminService;
	}
}
