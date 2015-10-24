package com.mantralabsglobal.addtobill.model;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

public class Transaction extends BaseEntity{

	public static final String DEBIT = "D";
	public static final String CREDIT = "C";
	
	@Id
	private String transactionId;

	@Indexed
	private String currency;
	private double amount;
	@Indexed
	private String transactionAccountId;
	private String debitCreditIndicator;
	@Indexed
	private String chargeId;
	private String paymentId;
	
	private Map<String,String> attributes;
	
	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public String getDebitCreditIndicator() {
		return debitCreditIndicator;
	}

	public void setDebitCreditIndicator(String debitCreditIndicator) {
		this.debitCreditIndicator = debitCreditIndicator;
	}
	
	public boolean isDebitTransaction(){
		return DEBIT.equals(getDebitCreditIndicator());
	}
	
	public boolean isCreditTransaction(){
		return CREDIT.equals(getDebitCreditIndicator());
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public double getSignedAmount() {
		return (isDebitTransaction()?-1:1)*getAmount();
	}

	public String getTransactionAccountId() {
		return transactionAccountId;
	}

	public void setTransactionAccountId(String transactionAccountId) {
		this.transactionAccountId = transactionAccountId;
	}

	public String getChargeId() {
		return chargeId;
	}

	public void setChargeId(String chargeId) {
		this.chargeId = chargeId;
	}

	public Map<String,String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String,String> attributes) {
		this.attributes = attributes;
	}

	public String putAttribute(String key, String value) {
		if(getAttributes() == null)
			attributes = new HashMap<>();
		return attributes.put(key, value);
	}

	public String getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}

}
