package com.odde.doughnut.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odde.doughnut.controllers.json.QuestionSuggestionCreationParams;
import com.odde.doughnut.controllers.json.QuizQuestion;
import com.odde.doughnut.controllers.json.QuizQuestionContestResult;
import com.odde.doughnut.entities.*;
import com.odde.doughnut.factoryServices.ModelFactoryService;
import com.odde.doughnut.factoryServices.quizFacotries.QuizQuestionNotPossibleException;
import com.odde.doughnut.models.TimestampOperations;
import com.odde.doughnut.models.UserModel;
import com.odde.doughnut.services.ai.MCQWithAnswer;
import com.odde.doughnut.services.ai.QuestionEvaluation;
import com.odde.doughnut.testability.MakeMe;
import com.odde.doughnut.testability.TestabilitySettings;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import io.reactivex.Single;
import java.sql.Timestamp;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {"classpath:repository.xml"})
@Transactional
class RestQuizQuestionControllerTests {
  @Mock OpenAiApi openAiApi;
  @Autowired ModelFactoryService modelFactoryService;
  @Autowired MakeMe makeMe;
  private UserModel currentUser;
  private final TestabilitySettings testabilitySettings = new TestabilitySettings();

  RestQuizQuestionController controller;

  @BeforeEach
  void setup() {
    currentUser = makeMe.aUser().toModelPlease();
    controller =
        new RestQuizQuestionController(
            openAiApi, modelFactoryService, currentUser, testabilitySettings);
  }

  RestQuizQuestionController nullUserController() {
    return new RestQuizQuestionController(
        openAiApi, modelFactoryService, makeMe.aNullUserModel(), testabilitySettings);
  }

  @Nested
  class answer {
    ReviewPoint reviewPoint;
    QuizQuestionEntity quizQuestionEntity;
    Answer answer;

    @BeforeEach
    void setup() {
      Note answerNote = makeMe.aNote().rememberSpelling().please();
      reviewPoint =
          makeMe
              .aReviewPointFor(answerNote)
              .by(currentUser)
              .forgettingCurveAndNextReviewAt(200)
              .please();
      quizQuestionEntity =
          makeMe.aQuestion().spellingQuestionOfReviewPoint(answerNote.getThing()).please();
      answer = makeMe.anAnswer().answerWithSpelling(answerNote.getTopic()).inMemoryPlease();
    }

    @Test
    void shouldValidateTheAnswerAndUpdateReviewPoint() {
      Integer oldRepetitionCount = reviewPoint.getRepetitionCount();
      AnsweredQuestion answerResult = controller.answerQuiz(quizQuestionEntity, answer);
      assertTrue(answerResult.correct);
      assertThat(reviewPoint.getRepetitionCount(), greaterThan(oldRepetitionCount));
    }

    @Test
    void shouldNoteIncreaseIndexIfRepeatImmediately() {
      testabilitySettings.timeTravelTo(reviewPoint.getLastReviewedAt());
      Integer oldForgettingCurveIndex = reviewPoint.getForgettingCurveIndex();
      controller.answerQuiz(quizQuestionEntity, answer);
      assertThat(reviewPoint.getForgettingCurveIndex(), equalTo(oldForgettingCurveIndex));
    }

    @Test
    void shouldIncreaseTheIndex() {
      testabilitySettings.timeTravelTo(reviewPoint.getNextReviewAt());
      Integer oldForgettingCurveIndex = reviewPoint.getForgettingCurveIndex();
      controller.answerQuiz(quizQuestionEntity, answer);
      assertThat(reviewPoint.getForgettingCurveIndex(), greaterThan(oldForgettingCurveIndex));
      assertThat(
          reviewPoint.getLastReviewedAt(), equalTo(testabilitySettings.getCurrentUTCTimestamp()));
    }

    @Test
    void shouldNotBeAbleToSeeNoteIDontHaveAccessTo() {
      Answer answer = new Answer();
      assertThrows(
          ResponseStatusException.class,
          () -> nullUserController().answerQuiz(quizQuestionEntity, answer));
    }

    @Nested
    class WrongAnswer {
      @BeforeEach
      void setup() {
        quizQuestionEntity =
            makeMe.aQuestion().spellingQuestionOfReviewPoint(reviewPoint.getThing()).please();
        answer = makeMe.anAnswer().answerWithSpelling("wrong").inMemoryPlease();
      }

      @Test
      void shouldValidateTheWrongAnswer() {
        testabilitySettings.timeTravelTo(reviewPoint.getNextReviewAt());
        Integer oldRepetitionCount = reviewPoint.getRepetitionCount();
        AnsweredQuestion answerResult = controller.answerQuiz(quizQuestionEntity, answer);
        assertFalse(answerResult.correct);
        assertThat(reviewPoint.getRepetitionCount(), greaterThan(oldRepetitionCount));
      }

      @Test
      void shouldNotChangeTheLastReviewedAtTime() {
        testabilitySettings.timeTravelTo(reviewPoint.getNextReviewAt());
        Timestamp lastReviewedAt = reviewPoint.getLastReviewedAt();
        Integer oldForgettingCurveIndex = reviewPoint.getForgettingCurveIndex();
        controller.answerQuiz(quizQuestionEntity, answer);
        assertThat(reviewPoint.getForgettingCurveIndex(), lessThan(oldForgettingCurveIndex));
        assertThat(reviewPoint.getLastReviewedAt(), equalTo(lastReviewedAt));
      }

      @Test
      void shouldRepeatTheNextDay() {
        controller.answerQuiz(quizQuestionEntity, answer);
        assertThat(
            reviewPoint.getNextReviewAt(),
            lessThan(
                TimestampOperations.addHoursToTimestamp(
                    testabilitySettings.getCurrentUTCTimestamp(), 25)));
      }
    }
  }

  @Nested
  class SuggestQuestionForFineTuning {
    QuizQuestionEntity quizQuestionEntity;
    MCQWithAnswer mcqWithAnswer;
    Note note;

    QuestionSuggestionCreationParams suggestionWithPositiveFeedback =
        new QuestionSuggestionCreationParams("this is a comment", true);

    QuestionSuggestionCreationParams suggestionWithNegativeFeedback =
        new QuestionSuggestionCreationParams("this is a comment", false);

    @BeforeEach
    void setup() throws QuizQuestionNotPossibleException {
      note = makeMe.aNote().creatorAndOwner(currentUser).please();
      mcqWithAnswer = makeMe.aMCQWithAnswer().please();
      quizQuestionEntity =
          makeMe.aQuestion().ofAIGeneratedQuestion(mcqWithAnswer, note.getThing()).please();
    }

    @Test
    void suggestQuestionWithAPositiveFeedback() {

      SuggestedQuestionForFineTuning suggestedQuestionForFineTuning =
          controller.suggestQuestionForFineTuning(
              quizQuestionEntity, suggestionWithPositiveFeedback);
      assert suggestedQuestionForFineTuning != null;
      assertEquals(
          quizQuestionEntity.getMcqWithAnswer().toJsonString(),
          suggestedQuestionForFineTuning.getPreservedQuestion().toJsonString());
      assertEquals("this is a comment", suggestedQuestionForFineTuning.getComment());
      assertTrue(suggestedQuestionForFineTuning.isPositiveFeedback(), "Incorrect Feedback");
      assertEquals("0", suggestedQuestionForFineTuning.getRealCorrectAnswers());
    }

    @Test
    void suggestQuestionWithANegativeFeedback() {
      SuggestedQuestionForFineTuning suggestedQuestionForFineTuning =
          controller.suggestQuestionForFineTuning(
              quizQuestionEntity, suggestionWithNegativeFeedback);
      assert suggestedQuestionForFineTuning != null;
      assertEquals(
          quizQuestionEntity.getMcqWithAnswer().toJsonString(),
          suggestedQuestionForFineTuning.getPreservedQuestion().toJsonString());
      assertEquals("this is a comment", suggestedQuestionForFineTuning.getComment());
      assertFalse(suggestedQuestionForFineTuning.isPositiveFeedback(), "Incorrect Feedback");
      assertEquals("", suggestedQuestionForFineTuning.getRealCorrectAnswers());
    }

    @Test
    void suggestQuestionWithSnapshotQuestionStem() {
      var suggestedQuestionForFineTuning =
          controller.suggestQuestionForFineTuning(
              quizQuestionEntity, suggestionWithPositiveFeedback);
      assert suggestedQuestionForFineTuning != null;
      assertThat(
          suggestedQuestionForFineTuning.getPreservedQuestion().stem, equalTo(mcqWithAnswer.stem));
    }

    @Test
    void createMarkedQuestionInDatabase() {
      long oldCount = modelFactoryService.questionSuggestionForFineTuningRepository.count();
      controller.suggestQuestionForFineTuning(quizQuestionEntity, suggestionWithPositiveFeedback);
      assertThat(
          modelFactoryService.questionSuggestionForFineTuningRepository.count(),
          equalTo(oldCount + 1));
    }
  }

  @Nested
  class GenerateQuestion {
    String jsonQuestion;
    Note note;

    @BeforeEach
    void setUp() {
      note = makeMe.aNote().please();
      MCQWithAnswer aiGeneratedQuestion =
          makeMe
              .aMCQWithAnswer()
              .stem("What is the first color in the rainbow?")
              .choices("red", "black", "green")
              .correctChoiceIndex(0)
              .please();
      jsonQuestion = aiGeneratedQuestion.toJsonString();
    }

    @Test
    void askWithNoteThatCannotAccess() {
      assertThrows(
          ResponseStatusException.class,
          () -> {
            RestQuizQuestionController restAiController =
                new RestQuizQuestionController(
                    openAiApi,
                    makeMe.modelFactoryService,
                    makeMe.aNullUserModel(),
                    testabilitySettings);
            restAiController.generateQuestion(note);
          });
    }

    @Test
    void createQuizQuestion() {
      mockChatCompletionForGPT3_5MessageOnly(jsonQuestion);
      QuizQuestion quizQuestion = controller.generateQuestion(note);

      Assertions.assertThat(quizQuestion.stem).contains("What is the first color in the rainbow?");
    }

    @Test
    void createQuizQuestionFailedWithGpt35WillNotTryAgain() throws JsonProcessingException {
      when(openAiApi.createChatCompletion(any()))
          .thenReturn(buildCompletionResultForFunctionCall("{\"stem\": \"\"}"));
      assertThrows(ResponseStatusException.class, () -> controller.generateQuestion(note));
      verify(openAiApi, Mockito.times(1)).createChatCompletion(any());
    }
  }

  @Nested
  class Contest {
    String jsonQuestion;
    QuizQuestionEntity quizQuestionEntity;
    QuestionEvaluation questionEvaluation = new QuestionEvaluation();

    @BeforeEach
    void setUp() {
      questionEvaluation.correctChoices = new int[] {0};
      questionEvaluation.feasibleQuestion = true;
      questionEvaluation.comment = "what a horrible question!";

      MCQWithAnswer aiGeneratedQuestion =
          makeMe
              .aMCQWithAnswer()
              .stem("What is the first color in the rainbow?")
              .choices("red", "black", "green")
              .correctChoiceIndex(0)
              .please();
      jsonQuestion = aiGeneratedQuestion.toJsonString();
      Note note = makeMe.aNote().please();
      quizQuestionEntity =
          makeMe.aQuestion().ofAIGeneratedQuestion(aiGeneratedQuestion, note.getThing()).please();
    }

    @Test
    void askWithNoteThatCannotAccess() {
      assertThrows(
          ResponseStatusException.class,
          () -> {
            RestQuizQuestionController restAiController =
                new RestQuizQuestionController(
                    openAiApi,
                    makeMe.modelFactoryService,
                    makeMe.aNullUserModel(),
                    testabilitySettings);
            restAiController.contest(quizQuestionEntity);
          });
    }

    @Test
    void rejected() throws JsonProcessingException {
      mockChatCompletionForFunctionCall(
          "evaluate_question", new ObjectMapper().writeValueAsString(questionEvaluation));
      QuizQuestionContestResult contest = controller.contest(quizQuestionEntity);
      Assertions.assertThat(contest.newQuizQuestion).isNull();
      Assertions.assertThat(contest.reason)
          .isEqualTo("This seems to be a legitimate question. Please answer it.");
    }

    @Test
    void acceptTheContest() throws JsonProcessingException {
      questionEvaluation.feasibleQuestion = false;
      mockChatCompletionForFunctionCall(
          "evaluate_question", new ObjectMapper().writeValueAsString(questionEvaluation));
      mockChatCompletionForGPT3_5MessageOnly(jsonQuestion);
      QuizQuestionContestResult contest = controller.contest(quizQuestionEntity);
      Assertions.assertThat(contest.newQuizQuestion).isNotNull();
      Assertions.assertThat(contest.reason).isEqualTo("what a horrible question!");
    }
  }

  private Single<ChatCompletionResult> buildCompletionResultForFunctionCall(String jsonString)
      throws JsonProcessingException {
    return Single.just(
        makeMe
            .openAiCompletionResult()
            .functionCall("", new ObjectMapper().readTree(jsonString))
            .please());
  }

  private void mockChatCompletionForGPT3_5MessageOnly(String result) {
    Single<ChatCompletionResult> just =
        Single.just(makeMe.openAiCompletionResult().choice(result).please());

    doReturn(just)
        .when(openAiApi)
        .createChatCompletion(argThat(request -> request.getFunctions() == null));
  }

  private void mockChatCompletionForFunctionCall(String functionName, String result)
      throws JsonProcessingException {
    doReturn(buildCompletionResultForFunctionCall(result))
        .when(openAiApi)
        .createChatCompletion(
            argThat(
                request ->
                    request.getFunctions() != null
                        && request.getFunctions().get(0).getName().equals(functionName)));
  }
}
