package com.exception;

import lombok.Getter;

@Getter
public class VoException extends RuntimeException {
	private final String message;

	public VoException(String message) {
		super(message);
		this.message = message;
	}
}
