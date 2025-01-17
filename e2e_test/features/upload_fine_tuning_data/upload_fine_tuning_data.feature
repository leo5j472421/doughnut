@usingMockedOpenAiService
Feature: Upload fine tuning data

# TODO: download first?

  @ignore
  Scenario Outline: Block upload fine tuning data
    Given I have <positive_count> positive feedbacks and <negative_count> negative feedbacks
    When I upload the feedbacks
    Then I should see the error message "Positive feedback cannot be less than 10."

    Examples:
      | positive_count | negative_count |
      | 9              | 0              |
      | 9              | 1              |
      | 0              | 10             |
      | 9              | 10             |

  @ignore
  Scenario: Open AI fail
    Given Open AI is not ready
    Given I have 10 positive feedbacks and 1 negative feedbacks
    When I upload the feedbacks
    Then I should see the error message "Something wrong with Open AI service."

  @ignore
  Scenario Outline: Upload fine tuning data to Open AI service
    Given I have <positive_count> positive feedbacks and <negative_count> negative feedbacks
    When I upload the feedbacks
    Then I should see the success message "Upload successfully."
    Then Open AI service should receive the uploaded file.

    Examples:
      | positive_count | negative_count |
      | 10             | 0              |
      | 11             | 10             |
