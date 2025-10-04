package com.response;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Getter;

@Getter
@JsonTypeName("false")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public final class FailedResponseBody extends ResponseBody<Void> {
	private final String msg;

	private final Map<String, Object> errorData;

	public FailedResponseBody(String code, String msg) {
		this.setCode(code);
		this.msg = msg;
		this.errorData = null;
	}

	public FailedResponseBody(String code, String msg, Map<String, Object> errorData) {
		this.setCode(code);
		this.msg = msg;
		this.errorData = errorData;
	}
}
