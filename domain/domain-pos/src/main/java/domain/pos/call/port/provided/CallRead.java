package domain.pos.call.port.provided;

import org.springframework.data.domain.Slice;

import com.vo.UserPassport;

import domain.pos.call.entity.dto.CallInfoDto;

public interface CallRead {
	Slice<CallInfoDto> getNonCompleteCalls(UserPassport ownerPassport, Long saleId,
		Long lastCallId,
		int pageSize);
}
