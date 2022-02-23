package com.odde.doughnut.controllers;

import com.odde.doughnut.controllers.currentUser.CurrentUserFetcher;
import com.odde.doughnut.entities.Comment;
import com.odde.doughnut.entities.Note;
import com.odde.doughnut.entities.User;
import com.odde.doughnut.exceptions.NoAccessRightException;
import com.odde.doughnut.factoryServices.ModelFactoryService;
import com.odde.doughnut.models.UserModel;
import com.odde.doughnut.testability.TestabilitySettings;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class RestCommentController {
    private final ModelFactoryService modelFactoryService;
    private final CurrentUserFetcher currentUserFetcher;
    @Resource(name = "testabilitySettings")
    private final TestabilitySettings testabilitySettings;

    public RestCommentController(ModelFactoryService modelFactoryService, CurrentUserFetcher currentUserFetcher, TestabilitySettings testabilitySettings) {
        this.modelFactoryService = modelFactoryService;
        this.currentUserFetcher = currentUserFetcher;
        this.testabilitySettings = testabilitySettings;
    }

    @GetMapping("/{note}")
    public List<Comment> noteComments(@PathVariable("note") Note note) {

        return modelFactoryService.commentRepository.getCommentsByNote(note);
    }

    private Comment createComment(Note note, User user, boolean isRead) {
        Comment comment = new Comment();
        comment.setNote(note);
        comment.setUser(user);
        comment.setContent("Comment 1");
        comment.setRead(isRead);
        return comment;
    }

    @PostMapping(value = "/{comment}/delete")
    @Transactional
    public Integer deleteComment(@PathVariable("comment") Comment comment) throws NoAccessRightException {
        currentUserFetcher.getUser().getAuthorization().assertAuthorization(comment);
        modelFactoryService.toCommentModel(comment).destroy(testabilitySettings.getCurrentUTCTimestamp());
        modelFactoryService.entityManager.flush();
        return comment.getId();
    }

    @PostMapping(value = "/{comment}")
    @Transactional
    public String editComment(@PathVariable("comment") Comment comment) throws NoAccessRightException {
        currentUserFetcher.getUser().getAuthorization().assertAuthorization(comment);
        modelFactoryService.toCommentModel(comment).edit();
        modelFactoryService.entityManager.flush();
        return comment.getContent();
    }
    
    @PostMapping(value = "/{note}/add")
    @Transactional
    public Comment addComment(@PathVariable("note") Note note) throws NoAccessRightException {
        final UserModel userModel = currentUserFetcher.getUser();
        User user = userModel.getEntity();
        Comment comment = createComment(note, user, true);
        return comment;
    }
}
