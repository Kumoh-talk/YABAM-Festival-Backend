package domain.pos.table.port.provided;

import org.springframework.validation.annotation.Validated;

import com.vo.UserPassport;

import domain.pos.table.entity.Table;
import domain.pos.table.entity.TableInfoRequest;

public interface TableCommand {
	Table createTable(UserPassport passport, Long storeId, @Validated TableInfoRequest request);
}
