package domain.pos.store.repository;

public interface PresignedUrlGenerator {
	String generatePresignedUrl(String s3Path);
}
