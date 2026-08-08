package com.helix.gpo.web_crm.project.internal;

import com.helix.gpo.web_crm.shared.BaseEntity;
import com.helix.gpo.web_crm.shared.Money;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "milestones")
class Milestone extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_milestone_project"))
    private Project project;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Embedded
    private Money price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MilestoneStatus status = MilestoneStatus.PLANNED;

    public void complete() {
        this.status = MilestoneStatus.DONE;
    }

    public void updatePrice(Money price) {
        this.price = price;
    }

}
