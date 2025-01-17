package com.odde.doughnut.factoryServices.quizFacotries.factories;

import com.odde.doughnut.entities.*;
import com.odde.doughnut.factoryServices.quizFacotries.QuizQuestionFactory;
import com.odde.doughnut.factoryServices.quizFacotries.QuizQuestionServant;
import java.util.List;

public class LinkSourceWithinSameLinkTypeQuizFactory
    implements QuizQuestionFactory, QuestionOptionsFactory {
  protected final Link link;
  private final QuizQuestionServant servant;
  protected final Note answerNote;
  private List<Link> cachedFillingOptions = null;

  public LinkSourceWithinSameLinkTypeQuizFactory(Thing thing, QuizQuestionServant servant) {
    this.link = thing.getLink();
    this.servant = servant;
    this.answerNote = link.getSourceNote();
  }

  @Override
  public List<Link> generateFillingOptions() {
    if (cachedFillingOptions == null) {
      cachedFillingOptions =
          servant.chooseLinkFromCohortAvoidSiblingsOfSameLinkType(link, answerNote);
    }
    return cachedFillingOptions;
  }

  @Override
  public Link generateAnswer() {
    return link;
  }
}
