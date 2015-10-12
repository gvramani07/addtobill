package com.mantralabsglobal.addtobill.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidTokenException  extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6801679757672581575L;

}
