package com.exception;

import static lombok.AccessLevel.*;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class State {
	public static void state(boolean expression, ErrorCode errorCode) {
		if (!expression) {
			throw new ServiceException(errorCode);
		}
	}

}
