package com.pos.table.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.exception.VoException;
import com.pos.table.vo.TableNumber;
import com.pos.table.vo.TablePointVo;

class TableEntityTest {

	@Test
	void TableEntity_생성시_테이블_번호가_양수가_아니면_실패한다() {
		Assertions.assertThrows(VoException.class, () -> {
			TableEntity.of(TableNumber.from(-1), TablePointVo.of(0, 0), true, null);
		});
	}
}
