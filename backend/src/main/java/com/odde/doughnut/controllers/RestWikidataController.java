package com.odde.doughnut.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odde.doughnut.testability.TestabilitySettings;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wikidata")
public class RestWikidataController {
  private final TestabilitySettings testabilitySettings;

  public RestWikidataController(TestabilitySettings testabilitySettings) {
    this.testabilitySettings = testabilitySettings;
  }

  @GetMapping("/{term}")
  public Map<String, Object> entity(@PathVariable("term") String term)
      throws IOException, InterruptedException {
    if (term.equalsIgnoreCase("tdd")) {
      String entityId = "Q950250";
      HttpResponse<String> response = apiRequest(entityId, HttpRequest.Builder::GET);
      return new ObjectMapper().readValue(response.body(), new TypeReference<>() {});
    }
    return new HashMap<>();
  }

  private HttpResponse<String> apiRequest(
      String entityId, Function<HttpRequest.Builder, HttpRequest.Builder> callback)
      throws IOException, InterruptedException {
    final HttpRequest.Builder builder =
        HttpRequest.newBuilder(
            URI.create(testabilitySettings.getWikidataUrl() + entityId + ".json"));
    final HttpRequest.Builder builderWithRequest = callback.apply(builder);
    HttpRequest request = builderWithRequest.setHeader("Content-Type", "application/json").build();
    HttpResponse.BodyHandler<String> bodyHandler =
        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
    return HttpClient.newBuilder().build().send(request, bodyHandler);
  }
}
