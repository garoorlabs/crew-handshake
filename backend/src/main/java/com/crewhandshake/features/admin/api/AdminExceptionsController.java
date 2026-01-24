package com.crewhandshake.features.admin.api;

import com.crewhandshake.features.foreman.api.ExceptionResolveRequest;
import com.crewhandshake.features.foreman.api.ExceptionResponse;
import com.crewhandshake.features.foreman.service.ExceptionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/admin/exceptions")
public class AdminExceptionsController {
  private final ExceptionService exceptionService;

  public AdminExceptionsController(ExceptionService exceptionService) {
    this.exceptionService = exceptionService;
  }

  @GetMapping
  public List<ExceptionResponse> listExceptions(@RequestParam String date, @RequestParam UUID crewId) {
    return exceptionService.getExceptions(date, crewId);
  }

  @PostMapping("/{id}/resolve")
  public ExceptionResponse resolve(@PathVariable("id") UUID exceptionId, @Valid @RequestBody ExceptionResolveRequest request) {
    return exceptionService.resolveException(exceptionId, request);
  }
}
