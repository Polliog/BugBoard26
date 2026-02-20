package it.unina.bugboard26.service;

import it.unina.bugboard26.model.Issue;
import it.unina.bugboard26.model.User;
import it.unina.bugboard26.model.enums.GlobalRole;
import org.springframework.stereotype.Service;

/**
 * Logica permessi centralizzata.
 * Controlla i diritti degli utenti in base al ruolo globale.
 */
@Service
public class PermissionService {

    /**
     * RF02 - Solo ADMIN e USER possono creare issue.
     */
    public boolean canCreateIssue(User user) {
        return user.getRole() != GlobalRole.EXTERNAL;
    }

    /**
     * RF06/RF09 - ADMIN full access, USER solo sue issue assegnate.
     */
    public boolean canModifyIssue(User user, Issue issue) {
        if (user.getRole() == GlobalRole.ADMIN) return true;
        return user.getRole() == GlobalRole.USER
                && issue.getAssignedTo() != null
                && issue.getAssignedTo().getId().equals(user.getId());
    }

    /**
     * RF06 - Alias semantico per cambio stato.
     */
    public boolean canChangeStatus(User user, Issue issue) {
        return canModifyIssue(user, issue);
    }

    /**
     * RF13 - Solo ADMIN archivia.
     */
    public boolean canArchive(User user) {
        return user.getRole() == GlobalRole.ADMIN;
    }

    /**
     * RF15 - EXTERNAL non puo commentare.
     */
    public boolean canComment(User user) {
        return user.getRole() != GlobalRole.EXTERNAL;
    }

    /**
     * RF01 - Solo ADMIN gestisce utenti.
     */
    public boolean canManageUsers(User user) {
        return user.getRole() == GlobalRole.ADMIN;
    }
}
