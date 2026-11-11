package com.helix.gpo.web_crm.project.internal;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectTest {

    private static final LocalDate START = LocalDate.of(2026, 1, 1);
    private static final LocalDate END = LocalDate.of(2026, 6, 30);

    private Project project(ProjectStatus status) {
        return Project.builder()
                .tenantId(UUID.randomUUID())
                .title("Testprojekt")
                .startDate(START)
                .endDate(END)
                .status(status)
                .build();
    }

    private void updateDates(Project project, LocalDate start, LocalDate end) {
        project.updateDetails("Testprojekt", null, null, List.of(), List.of(), start, end);
    }

    @Test
    void endBeforeStartIsRejected() {
        Project project = project(ProjectStatus.IN_PROGRESS);
        assertThatThrownBy(() -> updateDates(project, END, START))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void milestoneOutsideProjectPeriodIsRejected() {
        Project project = project(ProjectStatus.IN_PROGRESS);
        assertThatThrownBy(() -> project.addMilestone("Go-Live", null, END.plusDays(1), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Go-Live");
    }

    @Test
    void shorteningPeriodThatExcludesMilestoneIsRejected() {
        Project project = project(ProjectStatus.IN_PROGRESS);
        project.addMilestone("Go-Live", null, LocalDate.of(2026, 6, 1), null);

        assertThatThrownBy(() -> updateDates(project, START, LocalDate.of(2026, 5, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Go-Live");
    }

    @Test
    void completingWithOpenMilestonesIsRejected() {
        Project project = project(ProjectStatus.IN_PROGRESS);
        project.addMilestone("Konzept", null, null, null);

        assertThatThrownBy(() -> project.changeStatus(ProjectStatus.COMPLETED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Konzept");
    }

    @Test
    void completingWithAllMilestonesDoneWorks() {
        Project project = project(ProjectStatus.IN_PROGRESS);
        Milestone milestone = project.addMilestone("Konzept", null, null, null);
        project.changeMilestoneStatus(milestone, MilestoneStatus.DONE);

        project.changeStatus(ProjectStatus.COMPLETED);

        assertThat(project.getStatus()).isEqualTo(ProjectStatus.COMPLETED);
    }

    @Test
    void leadCannotJumpToCompleted() {
        Project project = project(ProjectStatus.LEAD);
        assertThatThrownBy(() -> project.changeStatus(ProjectStatus.COMPLETED))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void closedProjectLocksMilestonesUntilReopened() {
        Project project = project(ProjectStatus.IN_PROGRESS);
        Milestone milestone = project.addMilestone("Konzept", null, null, null);
        project.changeStatus(ProjectStatus.CANCELLED);

        assertThatThrownBy(() -> project.changeMilestoneStatus(milestone, MilestoneStatus.DONE))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> project.addMilestone("Neu", null, null, null))
                .isInstanceOf(IllegalStateException.class);

        project.changeStatus(ProjectStatus.IN_PROGRESS);
        project.changeMilestoneStatus(milestone, MilestoneStatus.DONE);

        assertThat(milestone.getStatus()).isEqualTo(MilestoneStatus.DONE);
    }
}
