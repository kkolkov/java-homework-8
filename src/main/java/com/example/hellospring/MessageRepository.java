package com.example.hellospring;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findTop30ByOrderByCreatedAtDescIdDesc();

    @EntityGraph(attributePaths = "author")
    List<Message> findFirst30ByOrderByCreatedAtDescIdDesc();

    @EntityGraph(attributePaths = "author")
    Optional<Message> findWithAuthorById(Long id);
}
