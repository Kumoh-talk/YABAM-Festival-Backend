package com.application.global.config.swagger;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

@Component
@Profile("!test && !prod")
public class ApiSuccessResponseHandler {

	private static final String APPLICATION_JSON = "application/json";
	private static final String IS_SUCCESS = "true";

	public void handleApiSuccessResponse(Operation operation, HandlerMethod handlerMethod) {
		ApiResponseExplanations apiResponseExplanations
			= handlerMethod.getMethodAnnotation(ApiResponseExplanations.class);

		if (apiResponseExplanations == null) {
			return;
		}

		ApiSuccessResponseExplanation apiSuccessResponseExplanation = apiResponseExplanations.success();

		if (apiSuccessResponseExplanation != null) {
			ApiResponses responses = operation.getResponses();
			responses.remove("200");

			Schema<?> responseSchema = new Schema<>()
				.addProperty("success",
					new Schema<>().type("string").example(IS_SUCCESS))
				.addProperty("data",
					apiSuccessResponseExplanation.responseClass()
						.isAssignableFrom(ApiSuccessResponseExplanation.EmptyClass.class)
						? new Schema<>().type("object")
						: new Schema<>().$ref(
						"#/components/schemas/" + apiSuccessResponseExplanation.responseClass().getSimpleName())
				);

			ApiResponse apiResponse = new ApiResponse()
				.description(apiSuccessResponseExplanation.description())
				.content(new Content()
					.addMediaType(APPLICATION_JSON, new MediaType().schema(responseSchema))
				);

			responses.addApiResponse(String.valueOf(apiSuccessResponseExplanation.status().value()), apiResponse);
		}
	}
}
