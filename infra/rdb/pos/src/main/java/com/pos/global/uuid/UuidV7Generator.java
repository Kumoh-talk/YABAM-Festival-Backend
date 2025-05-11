package com.pos.global.uuid;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import com.uuid.UuidUtils;

public class UuidV7Generator extends SequenceStyleGenerator {
	@Override
	public Object generate(SharedSessionContractImplementor session, Object object) {
		return UuidUtils.randomV7();
	}
}
