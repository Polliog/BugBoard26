package it.unina.bugboard26.service;

import it.unina.bugboard26.model.Issue;
import it.unina.bugboard26.model.User;
import it.unina.bugboard26.model.enums.GlobalRole;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

    public boolean canCreateIssue(User user) {
        return user.getRole() != GlobalRole.EXTERNAL;
    }

    public boolean canModifyIssue(User user, Issue issue) {
        if (user.getRole() == GlobalRole.ADMIN) return true;
        return user.getRole() == GlobalRole.USER
                && issue.getAssignedTo() != null
                && issue.getAssignedTo().getId().equals(user.getId());
    }

    public boolean canChangeStatus(User user, Issue issue) {
        return canModifyIssue(user, issue);
    }

    public boolean canArchive(User user) {
        return user.getRole() == GlobalRole.ADMIN;
    }

    public boolean canComment(User user) {
        return user.getRole() != GlobalRole.EXTERNAL;
    }

    public boolean canManageUsers(User user) {
        return user.getRole() == GlobalRole.ADMIN;
    }

    public boolean canDeleteIssue(User user) {
        return user.getRole() == GlobalRole.ADMIN;
    }
}
