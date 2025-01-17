package com.odde.doughnut.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import lombok.Getter;

@MappedSuperclass
public abstract class Thingy {

  @Id
  @Getter
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  protected Integer id;

  abstract void setThing(Thing thing);

  @JsonIgnore
  public abstract Thing getThing();
}
