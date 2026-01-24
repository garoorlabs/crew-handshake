package com.crewhandshake.common.errors;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(ErrorContractTestController.class)
class ErrorContractTest {
  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void validationErrorsReturnFieldErrors() {
    ResponseEntity<ApiErrorResponse> response = restTemplate.postForEntity(
        "/api/v1/public/test-errors/validation",
        Map.of("name", ""),
        ApiErrorResponse.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().errorCode()).isEqualTo(ApiErrorCode.VALIDATION_ERROR.name());
    assertThat(response.getBody().fieldErrors()).containsKey("name");
  }
}
