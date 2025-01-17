/* eslint-disable  @typescript-eslint/no-explicit-any */
/* eslint @typescript-eslint/no-unused-vars: 0 */
/// <reference types="cypress" />
// @ts-check
declare namespace Cypress {
  interface Chainable<Subject = any> {
    dismissLastErrorMessage(): Chainable<any>
    addSiblingNoteButton(): Chainable<any>
    backendTimeTravelTo(day: number, hour: number): Chainable<Subject>
    backendTimeTravelRelativeToNow(hours: number): Chainable<Subject>
    cleanDBAndResetTestabilitySettings(): Chainable<Subject>
    cleanDownloadFolder(): Chainable<any>
    clickAddChildNoteButton(): Chainable<any>
    clickButtonOnCardBody(noteTopic: any, buttonTitle: any): Chainable<any>
    notePageButtonOnCurrentPage(btnTextOrTitle: any): Chainable<any>
    notePageButtonOnCurrentPageEditNote(): Chainable<any>
    clickNotePageMoreOptionsButtonOnCurrentPage(btnTextOrTitle: string): Chainable<any>
    clickLinkNob(target: string): Chainable<any>
    changeLinkType(targetNote: string, linkType: string): Chainable<any>
    clickRadioByLabel(labelText: any): Chainable<any>
    deleteNoteViaAPI(): Chainable<Subject>
    dialogDisappeared(): Chainable<any>
    expectBreadcrumb(item: string, addChildButton: boolean = true): Chainable<any>
    expectExactLinkTargets(targets: any): Chainable<any>
    expectFieldErrorMessage(field: string, message: string): Chainable<any>
    expectNoteCards(expectedCards: any): Chainable<any>
    findNoteTopic(topic: string): Chainable<any>
    findNoteDetailsOnCurrentPage(topic: string): Chainable<any>
    findCardTitle(topic: string): Chainable<any>
    findWikiAssociationButton(): Chainable<any>
    expectALinkThatOpensANewWindowWithURL(url: string): Chainable<any>
    expectAMapTo(latitude: string, longitude: string): Chainable<any>
    findUserSettingsButton(userName: string): Chainable<any>
    failure(): Chainable<any>
    featureToggle(enabled: boolean): Chainable<Subject>
    findNoteCardButton(noteTopic: string, btnTextOrTitle: string): Chainable<any>
    formField(label: string): Chainable<any>
    assignFieldValue(value: string): Chainable<any>
    fieldShouldHaveValue(value: string): Chainable<any>
    getSeededNoteIdByTitle(noteTopic: string): Chainable<Subject>
    initialReviewInSequence(reviews: any): Chainable<any>
    initialReviewNotes(noteTopics: any): Chainable<any>
    initialReviewOneNoteIfThereIs({
      review_type,
      topic,
      additional_info,
      skip,
    }: any): Chainable<any>
    inPlaceEdit(noteAttributes: any): Chainable<any>
    jumpToNotePage(noteTopic: any, forceLoadPage?: any): Chainable<any>
    loginAs(username: string): Chainable<any>
    logout(username?: string): Chainable<any>
    mock(): Chainable<Subject>
    navigateToChild(noteTopic: any): Chainable<any>
    navigateToCircle(circleName: any): Chainable<any>
    navigateToNotePage(notePath: NotePath): Chainable<any>
    noteByTitle(noteTopic: string): Chainable<any>
    openAndSubmitNoteAccessoriesFormWith(
      noteTopic: string,
      NoteAccessoriesAttributes: Record<string, string>,
    ): Chainable<any>
    openSidebar(): Chainable<any>
    pageIsNotLoading(): Chainable<any>
    randomizerAlwaysChooseLast(): Chainable<Subject>
    clearFocusedText(): Chainable<any>
    replaceFocusedTextAndEnter(test: any): Chainable<any>
    repeatReviewNotes(noteTopics: string): Chainable<any>
    goAndRepeatReviewNotes(noteTopics: string): Chainable<any>
    repeatMore(): Chainable<any>
    restore(): Chainable<Subject>
    routerPush(fallback: any, name: any, params: any): Chainable<any>
    routerToReviews(): Chainable<any>
    routerToRoot(): Chainable<any>
    routerToInitialReview(): Chainable<any>
    routerToRepeatReview(): Chainable<any>
    routerToNotebooks(noteTopic?: string): Chainable<any>
    searchNote(searchKey: any, options: any): Chainable<any>
    seedNotes(seedNotes: unknown[], externalIdentifier?: any, circleName?: any): Chainable<Subject>
    seedLink(type: string, fromNoteTopic: string, toNoteTopic: string): Chainable<Subject>
    seedCircle(circleInfo: Record<string, string>): Chainable<Subject>
    shareToBazaar(noteTopic: string): Chainable<Subject>
    shouldSeeQuizWithOptions(questionParts: any, options: any): Chainable<any>
    startSearching(): Chainable<any>
    subscribeToNotebook(notebookTitle: string, dailyLearningCount: string): Chinputainable<any>
    submitNoteFormWith(noteAttributes: any): Chainable<any>
    submitNoteFormsWith(notes: any): Chainable<any>
    submitNoteCreationFormWith(noteAttributes: any): Chainable<any>
    submitNoteCreationFormSuccessfully(noteAttributes: any): Chainable<any>
    createNotebookWith(notes: any): Chainable<any>
    testability(): Chainable<any>
    timeTravelTo(day: number, hour: number): Chainable<Subject>
    triggerException(): Chainable<Subject>
    undoLast(undoThpe: string): Chainable<any>
    unsubscribeFromNotebook(noteTopic: string): Chainable<any>
    updateCurrentUserSettingsWith(hash: Record<string, string>): Chainable<Subject>
    setServiceUrl(serviceName: string, serviceUrl: string): Chainable<any>
    yesIRemember(): Chainable<any>
  }
}
