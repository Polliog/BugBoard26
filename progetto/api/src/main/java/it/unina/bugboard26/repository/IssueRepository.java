package it.unina.bugboard26.repository;

import it.unina.bugboard26.model.Issue;
import it.unina.bugboard26.model.enums.IssuePriority;
import it.unina.bugboard26.model.enums.IssueStatus;
import it.unina.bugboard26.model.enums.IssueType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, String> {

    @Query("""
            SELECT i FROM Issue i
            WHERE (:types IS NULL OR i.type IN :types)
              AND (:statuses IS NULL OR i.status IN :statuses)
              AND (:priorities IS NULL OR i.priority IN :priorities)
              AND (:assignedToId IS NULL OR i.assignedTo.id = :assignedToId)
              AND (:archived IS NULL OR i.archived = :archived)
              AND (:search IS NULL
                   OR LOWER(i.title) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(i.description) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<Issue> findFiltered(
            @Param("types") List<IssueType> types,
            @Param("statuses") List<IssueStatus> statuses,
            @Param("priorities") List<IssuePriority> priorities,
            @Param("assignedToId") String assignedToId,
            @Param("archived") Boolean archived,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("""
            SELECT i FROM Issue i
            WHERE (:types IS NULL OR i.type IN :types)
              AND (:statuses IS NULL OR i.status IN :statuses)
              AND (:priorities IS NULL OR i.priority IN :priorities)
              AND (:assignedToId IS NULL OR i.assignedTo.id = :assignedToId)
              AND (:archived IS NULL OR i.archived = :archived)
              AND (:search IS NULL
                   OR LOWER(i.title) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(i.description) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY i.createdAt DESC
            """)
    List<Issue> findFilteredAll(
            @Param("types") List<IssueType> types,
            @Param("statuses") List<IssueStatus> statuses,
            @Param("priorities") List<IssuePriority> priorities,
            @Param("assignedToId") String assignedToId,
            @Param("archived") Boolean archived,
            @Param("search") String search
    );
}
