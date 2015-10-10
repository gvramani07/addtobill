package com.mantralabsglobal.addtobill.repository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.mantralabsglobal.addtobill.model.Account;
import com.mantralabsglobal.addtobill.model.Transaction;

public interface AccountRepository extends CrudRepository<Account, String> {

	 	@Query(value = "{ 'unbilledTransactionList.transactionId' : ?0, 'billedTransactionList.transactionId' : ?0 }", fields = "{ 'transaction' : 1 }")
	    Transaction findOneByTransactionId(String transactionId);

		@Query(value = "{ 'unbilledTransactionList.merchantReferenceId' : ?0, 'billedTransactionList.merchantReferenceId' : ?0 }", fields = "{ 'transaction' : 1 }")
	    Transaction findOneByReferenceId(String merchantReferenceId);


}
