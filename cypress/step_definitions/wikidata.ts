/// <reference types="cypress" />
/// <reference types="@testing-library/cypress" />
/// <reference types="../support" />
// @ts-check

import { Given, Then, When } from "@badeball/cypress-cucumber-preprocessor"

When("I ask for the {string} entity from Wikidata", (term) => {
  cy.request(`/api/wikidata/${term}`).as("resp")
})

Then("I will get the payload containing {string}", (description) => {
  cy.get("@resp").should((contents) => {
    expect(JSON.stringify(contents.body)).contains(description)
  })
})
