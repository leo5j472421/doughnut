package com.odde.doughnut.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "subscription")
public class SubscriptionEntity {
    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;

    @Column(name = "daily_target_of_new_notes")
    @Getter
    @Setter
    private Integer dailyTargetOfNewNotes = 5;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @Getter @Setter private UserEntity userEntity;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "note_id", referencedColumnName = "id")
    @Getter @Setter private NoteEntity noteEntity;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "notebook_id", referencedColumnName = "id")
    @Getter @Setter private NotebookEntity notebookEntity;

    public String getTitle() {
        return noteEntity.getTitle();
    }

    public NoteContentEntity getNoteContent() {
        return noteEntity.getNoteContent();
    }
}
