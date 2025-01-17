import { DataTable } from "@badeball/cypress-cucumber-preprocessor"
/// <reference types="cypress" />
/// <reference types="../support" />
// @ts-check

import { Given } from "@badeball/cypress-cucumber-preprocessor"
import start from "start"

Given("my question should not be included in the admin's fine-tuning data", () => {
  start
    .loginAsAdminAndGoToAdminDashboard()
    .suggestedQuestionsForFineTuning()
    .downloadAIQuestionTrainingData()
    .expectNumberOfRecords(0)
})

Given(
  "the admin modifies the question suggested {string} to:",
  (originalQuestionStem: string, newQuestion: DataTable) => {
    start
      .loginAsAdminAndGoToAdminDashboard()
      .suggestedQuestionsForFineTuning()
      .updateQuestionSuggestionAndChoice(originalQuestionStem, newQuestion.hashes()[0])
  },
)

Given("an admin duplicates the question {string}", (questionStem: string) => {
  start
    .loginAsAdminAndGoToAdminDashboard()
    .suggestedQuestionsForFineTuning()
    .duplicateNegativeQuestion(questionStem)
})

Given(
  "an admin can retrieve the training data for question generation containing:",
  (question: DataTable) => {
    start
      .loginAsAdminAndGoToAdminDashboard()
      .suggestedQuestionsForFineTuning()
      .downloadAIQuestionTrainingData()
      .expectExampleQuestions(question.hashes())
  },
)

Given(
  "an admin can retrieve the training data for question generation containing {int} examples",
  (numOfDownload: number) => {
    start
      .loginAsAdminAndGoToAdminDashboard()
      .suggestedQuestionsForFineTuning()
      .downloadAIQuestionTrainingData()
      .expectNumberOfRecords(numOfDownload)
  },
)

Given(
  "an admin should be able to download the training data for evaluation containing:",
  (trainingExamples: DataTable) => {
    start
      .loginAsAdminAndGoToAdminDashboard()
      .suggestedQuestionsForFineTuning()
      .downloadFeedbackForEvaluationModel()
      .expectExampleQuestions(trainingExamples.hashes())
  },
)

Given(
  "there should be {int} examples containing {string}",
  (numOfOccurrence: number, expectedString: string) => {
    start
      .assumeAdminDashboardPage()
      .suggestedQuestionsForFineTuning()
      .expectString(numOfOccurrence, expectedString)
  },
)
