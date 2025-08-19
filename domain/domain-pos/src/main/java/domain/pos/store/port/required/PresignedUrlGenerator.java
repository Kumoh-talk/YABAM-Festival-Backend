package domain.pos.store.port.required;

public interface PresignedUrlGenerator {
	String generatePresignedUrl(String s3Path);
}
