import mock_services from "./mock_services"

export const questionGenerationService = () => ({
  resetAndStubAskingMCQ: (record: Record<string, string>) => {
    const reply = JSON.stringify({
      stem: record["Question Stem"],
      correctChoiceIndex: 0,
      choices: [
        record["Correct Choice"],
        record["Incorrect Choice 1"],
        record["Incorrect Choice 2"],
      ],
    })
    const messages = [{ role: "system", content: "MCQ" }]
    cy.then(async () => {
      await mock_services.openAi().restartImposter()
      await mock_services
        .openAi()
        .stubChatCompletionFunctionCallForMessageContaining(
          messages,
          "ask_single_answer_multiple_choice_question",
          reply,
        )
    })
  },
  stubEvaluationQuestion: (record: Record<string, boolean | string>) => {
    const messages = [{ role: "user", content: ".*critically check.*" }]
    cy.then(async () => {
      await mock_services
        .openAi()
        .stubChatCompletionFunctionCallForMessageContaining(
          messages,
          "evaluate_question",
          JSON.stringify(record),
        )
    })
  },
})
