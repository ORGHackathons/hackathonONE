package com.sentimentapi.repositories;

import com.sentimentapi.entities.CommentEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

// Repositório JPA responsável por acessar os comentários
// Contém uma query personalizada para buscar
// os últimos comentários cadastrados no banco
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    @Query(
            value = "SELECT c FROM CommentEntity c " +
                    "ORDER BY c.id DESC"
    )
    List<CommentEntity> buscarPorUltimos(Pageable pageable);
}

