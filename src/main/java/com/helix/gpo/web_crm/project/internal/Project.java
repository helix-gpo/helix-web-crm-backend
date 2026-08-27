package com.helix.gpo.web_crm.project.internal;

import com.helix.gpo.web_crm.shared.BaseEntity;
import com.helix.gpo.web_crm.shared.Money;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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

    // Bewusst nur die rohe ID, KEINE @ManyToOne-Relation zum tenant-Modul -
    // Modulgrenze bleibt sauber, Zugriff auf Tenant-Daten läuft über TenantApi
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 500)
    private String description;

    @Lob
    @Column(name = "full_description")
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

    @Column(name = "image_key", length = 500)
    private String imageKey;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dueDate ASC")
    @Builder.Default
    private List<Milestone> milestones = new ArrayList<>();

    public void changeStatus(ProjectStatus newStatus) {
        this.status = newStatus;
    }

    public void publishOnWebsite() {
        this.visibleOnWebsite = true;
    }

    public void unpublishFromWebsite() {
        this.visibleOnWebsite = false;
    }

    public void updateDetails(String title, String description, String fullDescription,
                              List<String> highlights, List<ProjectTag> tags,
                              LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.description = description;
        this.fullDescription = fullDescription;
        this.highlights = highlights != null ? highlights : List.of();
        this.tags = tags != null ? tags : List.of();
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Milestone addMilestone(String title, String description, LocalDate dueDate, Money price) {
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

    public void updateNotes(String notes) {
        this.notes = notes;
    }

    public void attachImage(String key) {
        this.imageKey = key;
    }

    public void removeImage() {
        this.imageKey = null;
    }

}
