package com.odde.doughnut.services;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import java.util.stream.Collectors;
import retrofit2.HttpException;

public class AiAdvisorService {
  private final OpenAiService service;

  public AiAdvisorService(OpenAiService openAiService) {
    service = openAiService;
  }

  private String getOpenAiResponse(String prompt) {
    CompletionRequest completionRequest =
        CompletionRequest.builder().prompt(prompt).model("text-davinci-003").echo(true).build();
    var choices = service.createCompletion(completionRequest).getChoices();
    return choices.stream().map(CompletionChoice::getText).collect(Collectors.joining(""));
  }

  public String getSuggestedDescription(String item) {
    try {
      String prompt = "Tell me about " + item + ".";
      return getOpenAiResponse(prompt).replace(prompt, "").trim();
    } catch (HttpException e) {
      return "";
    }
  }
}