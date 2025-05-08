package com.application.global.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(name = "GlobalSliceResponse", description = "no-offset 방식 페이지 응답")
public class GlobalSliceResponse<T> {
	@Schema(description = "다음 페이지 존재 여부", example = "true")
	protected boolean nextPage;
	@Schema(description = "한 페이지에 게시물 갯수", example = "10")
	protected int pageSize;
	@Schema(description = "페이징 내용")
	protected List<T> pageContents;

	@Builder
	public GlobalSliceResponse(boolean nextPage, int pageSize, List<T> pageContents) {
		this.nextPage = nextPage;
		this.pageSize = pageSize;
		this.pageContents = pageContents;
	}

	public static <T> GlobalSliceResponse<T> from(
		Slice<T> sliceDTO) {
		return GlobalSliceResponse.<T>builder()
			.nextPage(sliceDTO.hasNext())
			.pageSize(sliceDTO.getSize())
			.pageContents(sliceDTO.getContent())
			.build();
	}
}
