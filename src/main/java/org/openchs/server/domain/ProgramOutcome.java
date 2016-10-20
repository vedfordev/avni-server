package org.openchs.server.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ProgramOutcome")
public class ProgramOutcome extends CHSEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    @NotNull
    private String name;

    @ManyToOne
    @NotNull
    private Program program;
}