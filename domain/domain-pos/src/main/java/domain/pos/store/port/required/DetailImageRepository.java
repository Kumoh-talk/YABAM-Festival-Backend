package domain.pos.store.port.required;

import org.springframework.stereotype.Repository;

import domain.pos.store.entity.DetailImages;

@Repository
public interface DetailImageRepository {
	DetailImages findByStoreId(Long queryStoreId);

	void save(DetailImages detailImages);
}
