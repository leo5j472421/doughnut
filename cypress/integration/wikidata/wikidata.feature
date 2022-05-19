Feature: I can interact with Wikidata API
  Scenario: Fetch a wikidata record
    When I ask for the "TDD" entity from Wikidata
    Then I will get the payload containing "test driven development"
