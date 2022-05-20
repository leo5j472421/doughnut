Feature: I can interact with Wikidata API
  @external_api_dummy
  Scenario: Fetch a wikidata record from mock api service
    Given the description for "TDD" on Wikidata is "debug driven development"
    When I ask for the "TDD" entity from Wikidata
    Then I will get the payload containing "debug driven development"

  Scenario: Fetch a wikidata record from real Wikidata
    When I ask for the "TDD" entity from Wikidata
    But I will get the payload containing "test driven development"
