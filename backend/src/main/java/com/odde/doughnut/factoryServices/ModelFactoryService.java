package com.odde.doughnut.factoryServices;

import com.odde.doughnut.controllers.json.QuizQuestion;
import com.odde.doughnut.controllers.json.SearchTerm;
import com.odde.doughnut.entities.*;
import com.odde.doughnut.entities.repositories.*;
import com.odde.doughnut.factoryServices.quizFacotries.QuizQuestionPresenter;
import com.odde.doughnut.models.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ModelFactoryService {
  @Autowired public ThingRepository thingRepository;
  @Autowired public NoteRepository noteRepository;
  @Autowired public UserRepository userRepository;
  @Autowired public BazaarNotebookRepository bazaarNotebookRepository;
  @Autowired public ReviewPointRepository reviewPointRepository;
  @Autowired public CircleRepository circleRepository;
  @Autowired public LinkRepository linkRepository;
  @Autowired public QuizQuestionRepository quizQuestionRepository;
  @Autowired public AnswerRepository answerRepository;
  @Autowired public NotesClosureRepository notesClosureRepository;
  @Autowired public NotebookRepository notebookRepository;
  @Autowired public EntityManager entityManager;
  @Autowired public FailureReportRepository failureReportRepository;

  @Autowired
  public QuestionSuggestionForFineTuningRepository questionSuggestionForFineTuningRepository;

  public NoteModel toNoteModel(Note note) {
    return new NoteModel(note, this);
  }

  public NoteMotionModel toNoteMotionModel(NoteMotion noteMotion, Note note) {
    noteMotion.setSubject(note);
    return new NoteMotionModel(noteMotion, this);
  }

  public NoteMotionModel toNoteMotionModel(Note sourceNote, Note targetNote, Boolean asFirstChild) {
    if (!asFirstChild) {
      List<Note> children = targetNote.getChildren();
      if (children.size() > 0) {
        return toNoteMotionModel(
            new NoteMotion(children.get(children.size() - 1), false), sourceNote);
      }
    }
    return toNoteMotionModel(new NoteMotion(targetNote, true), sourceNote);
  }

  public BazaarModel toBazaarModel() {
    return new BazaarModel(this);
  }

  public Optional<User> findUserById(Integer id) {
    return userRepository.findById(id);
  }

  public UserModel toUserModel(User user) {
    return new UserModel(user, this);
  }

  public ReviewPointModel toReviewPointModel(ReviewPoint reviewPoint) {
    return new ReviewPointModel(reviewPoint, this);
  }

  public ReviewPointModel toReviewPointModel(Integer reviewPointId) {
    return new ReviewPointModel(reviewPointRepository.findById(reviewPointId).get(), this);
  }

  public CircleModel toCircleModel(Circle circle) {
    return new CircleModel(circle, this);
  }

  public CircleModel findCircleByInvitationCode(String invitationCode) {
    Circle circle = circleRepository.findFirstByInvitationCode(invitationCode);
    if (circle == null) {
      return null;
    }
    return toCircleModel(circle);
  }

  public LinkModel toLinkModel(Link link) {
    return new LinkModel(link, this);
  }

  public SubscriptionModel toSubscriptionModel(Subscription sub) {
    return new SubscriptionModel(sub, this);
  }

  public Authorization toAuthorization(User entity) {
    return new Authorization(entity, this);
  }

  public SearchTermModel toSearchTermModel(User entity, SearchTerm searchTerm) {
    return new SearchTermModel(entity, noteRepository, searchTerm);
  }

  public AnswerModel toAnswerModel(Answer answer) {
    return new AnswerModel(answer, this);
  }

  public QuizQuestion toQuizQuestion(QuizQuestionEntity quizQuestionEntity, User user) {
    QuizQuestionPresenter presenter = quizQuestionEntity.buildPresenter();
    return new QuizQuestion(
        quizQuestionEntity.getId(),
        quizQuestionEntity.getQuestionType(),
        presenter.stem(),
        presenter.mainTopic(),
        quizQuestionEntity.getNotebookPosition(user),
        presenter.getOptions(this),
        presenter.pictureWithMask());
  }

  public Stream<Thing> getThingStreamAndKeepOriginalOrder(List<Integer> idList) {
    return thingRepository
        .findAllByIds(idList)
        .sorted(Comparator.comparing(v -> idList.indexOf(v.getId())));
  }

  public SuggestedQuestionForFineTuningModel toSuggestedQuestionForFineTuningService(
      SuggestedQuestionForFineTuning suggestion) {
    return new SuggestedQuestionForFineTuningModel(suggestion, this);
  }
}
