package com.application.presentation.image.controller;

import static com.response.ResponseUtil.*;
import static com.vo.UserRole.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.application.presentation.image.api.ImageApi;
import com.application.presentation.image.dto.request.PresignedUrlRequest;
import com.authorization.AssignUserPassport;
import com.authorization.HasRole;
import com.response.ResponseBody;
import com.vo.UserPassport;

import domain.pos.image.service.ImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ImageController implements ImageApi {
	private final ImageService imageService;

	@PostMapping("/api/v1/presigned-url")
	@HasRole(userRole = ROLE_OWNER)
	@AssignUserPassport
	public ResponseEntity<ResponseBody<String>> getPresignedUrl(
		UserPassport userPassport,
		@RequestBody @Valid PresignedUrlRequest presignedUrlRequest) {
		return ResponseEntity
			.ok(createSuccessResponse(
				imageService.getPresignedUrl(userPassport, presignedUrlRequest.storeId(),
					presignedUrlRequest.imageProperty())));
	}
}
