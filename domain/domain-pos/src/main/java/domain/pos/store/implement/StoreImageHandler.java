package domain.pos.store.implement;

import org.springframework.stereotype.Component;

import domain.pos.store.port.required.PresignedUrlGenerator;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StoreImageHandler {
	private final PresignedUrlGenerator presignedUrlGenerator;

	public String generatePresignedUrl(String url) {
		return presignedUrlGenerator.generatePresignedUrl(url);
	}
}
