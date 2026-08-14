package com.helix.gpo.web_crm.shared.web;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Zentrale Übersetzung der im Code verwendeten Standard-Exceptions in
// saubere HTTP-Antworten (RFC 7807 ProblemDetail) - ohne diesen Handler
// landen z.B. IllegalStateException oder EntityNotFoundException als
// nackter 500er beim Client, mit Whitelabel-Error-Page statt JSON
@RestControllerAdvice
class GlobalExceptionHandler {

    // Fachliche Zustandsverletzungen - z.B. "Cannot issue an invoice
    // without line items", "Nur freigegebene Referenzen können..." oder
    // das neue 6er-Limit auf der Website. 409, weil der Request an sich
    // gültig war, der aktuelle Zustand die Aktion aber verhindert.
    @ExceptionHandler(IllegalStateException.class)
    ProblemDetail handleIllegalState(IllegalStateException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Aktion nicht möglich");
        return problem;
    }

    // Fehlerhafte Eingaben, die keine Bean-Validation abfängt - z.B. der
    // ungültige Token beim öffentlichen Testimonial-Submit
    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Ungültige Anfrage");
        return problem;
    }

    // Alle getOrThrow()-Stellen im Code werfen das bei unbekannter ID
    @ExceptionHandler(EntityNotFoundException.class)
    ProblemDetail handleEntityNotFound(EntityNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Nicht gefunden");
        return problem;
    }

    // Letzte Auffanglinie - sorgt zumindest für ein konsistentes JSON statt
    // der Spring-Whitelabel-Fehlerseite bei unerwarteten Fehlern. Bewusst
    // OHNE ex.getMessage() - bei echten Server-/Programmierfehlern soll
    // keine interne Exception-Detail nach außen durchsickern.
    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Es ist ein unerwarteter Fehler aufgetreten"
        );
        problem.setTitle("Serverfehler");
        return problem;
    }

}
