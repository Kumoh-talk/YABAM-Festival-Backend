package com.application.presentation.image.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Presigned URL 응답")
@Builder
public record PresignedUrlResponse(
	@Schema(description = "Presigned URL", example = "https://kumoh-talk-bucket.s3.ap-northeast-2.amazonaws.com/board/15/image/54599f59-1d5b-4167-b9f7-96f84d3c452d/example.jpg")
	String presignedUrl
) {
	public static PresignedUrlResponse from(String presignedUrl) {
		return PresignedUrlResponse.builder()
			.presignedUrl(presignedUrl)
			.build();
	}
}
