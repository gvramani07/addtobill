package com.mantralabsglobal.addtobill.repository;

import org.springframework.data.repository.CrudRepository;

import com.mantralabsglobal.addtobill.model.Merchant;

public interface MerchantRepository extends CrudRepository<Merchant, String> {

	Merchant findByMerchantName(String merchantName);


}
