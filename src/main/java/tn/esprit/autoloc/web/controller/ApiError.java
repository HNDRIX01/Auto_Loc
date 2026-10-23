package tn.esprit.autoloc.web.controller;

import java.time.LocalDateTime;

public record ApiError(int statut, String message, LocalDateTime horodatage) {
}
