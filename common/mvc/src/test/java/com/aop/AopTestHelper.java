package com.aop;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Transactional;

import com.annotation.AssignUserPassport;
import com.annotation.DeadlockRetry;
import com.vo.UserPassport;

public class AopTestHelper {
	@Autowired
	private DataSource dataSource;

	private int deadLockRetryFlag = 0;
	public String firstTransaction = "1";
	public String secondTransaction = "2";

	@AssignUserPassport
	public UserPassport assignUserPassport(UserPassport userPassport) {
		return userPassport;
	}

	@DeadlockRetry
	@Transactional
	public int deadlockRetry() {
		Connection conn = DataSourceUtils.getConnection(dataSource);
		String txId = Integer.toHexString(System.identityHashCode(conn));

		if (deadLockRetryFlag == 0) {
			deadLockRetryFlag = 1;
			firstTransaction = txId;
			// TODO : 서비스-인프라 통합 테스트에서 Deadlock 시에 의도한 예외가 던져지는지 테스트
			throw new CannotAcquireLockException("Deadlock!");
		}
		secondTransaction = txId;
		return deadLockRetryFlag = 0;
	}

}
