package com.helix.gpo.web_crm.project.internal;

import com.helix.gpo.web_crm.shared.BaseEntity;
import com.helix.gpo.web_crm.shared.Money;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "projects")
class Project extends BaseEntity {

    private static final DateTimeFormatter GERMAN_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(name = "full_description", columnDefinition = "TEXT")
    private String fullDescription;

    @ElementCollection
    @CollectionTable(name = "project_highlights", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "highlight", length = 300)
    @OrderColumn(name = "sort_order")
    @Builder.Default
    private List<String> highlights = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "project_tags", joinColumns = @JoinColumn(name = "project_id"))
    @Builder.Default
    private List<ProjectTag> tags = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.LEAD;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean visibleOnWebsite = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dueDate ASC")
    @Builder.Default
    private List<Milestone> milestones = new ArrayList<>();

    void changeStatus(ProjectStatus newStatus) {
        if (newStatus == status) {
            return;
        }
        if (!status.canChangeTo(newStatus)) {
            throw new IllegalStateException(
                    "Der Wechsel von „%s“ zu „%s“ ist nicht möglich.".formatted(status.label(), newStatus.label()));
        }
        if (newStatus == ProjectStatus.COMPLETED) {
            List<String> open = milestones.stream()
                    .filter(m -> m.getStatus() != MilestoneStatus.DONE)
                    .map(Milestone::getTitle)
                    .toList();
            if (!open.isEmpty()) {
                throw new IllegalStateException(
                        "Das Projekt kann erst abgeschlossen werden, wenn alle Meilensteine erledigt sind. Offen: "
                                + String.join(", ", open));
            }
        }
        this.status = newStatus;
    }


    public void publishOnWebsite() {
        this.visibleOnWebsite = true;
    }

    public void unpublishFromWebsite() {
        this.visibleOnWebsite = false;
    }

    void updateDetails(String title, String description, String fullDescription,
                              List<String> highlights, List<ProjectTag> tags,
                              LocalDate startDate, LocalDate endDate) {
        assertValidPeriod(startDate, endDate);
        // the new period must still contain every existing milestone
        milestones.forEach(m -> assertWithinPeriod(m.getDueDate(), startDate, endDate, m.getTitle()));

        this.title = title;
        this.description = description;
        this.fullDescription = fullDescription;
        this.highlights = highlights != null ? highlights : List.of();
        this.tags = tags != null ? tags : List.of();
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Milestone addMilestone(String title, String description, LocalDate dueDate, Money price) {
        requireOpen();
        assertWithinPeriod(dueDate, startDate, endDate, title);

        Milestone milestone = Milestone.builder()
                .project(this)
                .title(title)
                .description(description)
                .dueDate(dueDate)
                .price(price)
                .status(MilestoneStatus.PLANNED)
                .build();
        this.milestones.add(milestone);
        return milestone;
    }

    public void updateMilestone(Milestone milestone, String title, String description,
                                LocalDate dueDate, Money price, MilestoneStatus status) {
        requireOpen();
        assertWithinPeriod(dueDate, startDate, endDate, title);
        milestone.updateDetails(title, description, dueDate, price, status);
    }

    public void changeMilestoneStatus(Milestone milestone, MilestoneStatus status) {
        requireOpen();
        milestone.changeStatus(status);
    }

    public void removeMilestone(Milestone milestone) {
        requireOpen();
        this.milestones.remove(milestone);
    }

    static void assertValidPeriod(LocalDate start, LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("Das Projektende darf nicht vor dem Projektstart liegen.");
        }
    }

    private static void assertWithinPeriod(LocalDate dueDate, LocalDate start, LocalDate end, String milestoneTitle) {
        if (dueDate == null) {
            return;
        }
        if (start != null && dueDate.isBefore(start)) {
            throw new IllegalArgumentException("Meilenstein „%s“ wäre am %s fällig und damit vor dem Projektstart (%s)."
                    .formatted(milestoneTitle, GERMAN_DATE.format(dueDate), GERMAN_DATE.format(start)));
        }
        if (end != null && dueDate.isAfter(end)) {
            throw new IllegalArgumentException("Meilenstein „%s“ wäre am %s fällig und damit nach dem Projektende (%s)."
                    .formatted(milestoneTitle, GERMAN_DATE.format(dueDate), GERMAN_DATE.format(end)));
        }
    }

    private void requireOpen() {
        if (status.isClosed()) {
            throw new IllegalStateException(
                    "Das Projekt ist %s - öffne es wieder, um Meilensteine zu ändern.".formatted(status.label().toLowerCase()));
        }
    }

    public void updateNotes(String notes) {
        this.notes = notes;
    }

}
