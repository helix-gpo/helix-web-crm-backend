package com.helix.gpo.web_crm.project.internal;

public enum ProjectStatus {
    LEAD("Lead"),
    IN_PROGRESS("In Arbeit"),
    ON_HOLD("Pausiert"),
    COMPLETED("Abgeschlossen"),
    CANCELLED("Abgebrochen");

    private final String label;

    ProjectStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public boolean isClosed() {
        return this == COMPLETED || this == CANCELLED;
    }

    public boolean canChangeTo(ProjectStatus target) {
        return switch (this) {
            case LEAD, ON_HOLD -> target == IN_PROGRESS || target == CANCELLED;
            case IN_PROGRESS -> target == ON_HOLD || target == COMPLETED || target == CANCELLED;
            // closed projects can only be reopened
            case COMPLETED, CANCELLED -> target == IN_PROGRESS;
        };
    }
}
