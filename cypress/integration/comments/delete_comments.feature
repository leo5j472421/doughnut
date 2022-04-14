Feature:
  User can delete comment which leave by himself

  @featureToggle
  @ignore
  Scenario: I delete comment
    Given I've logged in as an existing user
    And there is a note and some comments of current user
      | content |
      | hello   |
      | world   |
    When I delete comment "hello" under Note 'A'
    Then Note 'A' only have one comment 'world'
