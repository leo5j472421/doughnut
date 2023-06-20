@usingMockedOpenAiService
Feature: Question generation by AI
  As a learner, I want to use AI to generate review questions based on my note and its context.
  So that I can remember my note better and potentially get new inspiration.

  Background:
    Given I've logged in as an existing user
    And there are some notes for the current user
      | title        | description                                   |
      | Scuba Diving | The most common certification is Rescue Diver.|
    And OpenAI by default returns this question from now:
      | question                                            | option_a     | option_b   | option_c         |
      | What is the most common scuba diving certification? | Rescue Diver | Divemaster | Open Water Diver |

  Scenario Outline: testing myself with generated question for a note
    When I ask to generate a question for note "Scuba Diving"
    Then I should be asked "What is the most common scuba diving certification?"
    And the option "<option>" should be <expectedResult>
    Examples:
      | option       | expectedResult |
      | Rescue Diver | correct        |
      | Divemaster   | wrong          |

  Scenario: I should see a new question when I click 'Ask again'
    Given I ask to generate a question for note "Scuba Diving"
    And OpenAI by default returns this question from now:
      | question                | option_a | option_b | option_c  |
      | How often scuba diving? | daily    | weekly   | never     |
    When I click "Ask again"
    Then I should be asked "How often scuba diving?"
