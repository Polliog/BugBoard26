package it.unina.bugboard26.repository;

import it.unina.bugboard26.model.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LabelRepository extends JpaRepository<Label, String> {

    Optional<Label> findByName(String name);
}
