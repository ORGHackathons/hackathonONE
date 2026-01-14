package com.sentimentapi.repositories;

import com.sentimentapi.entities.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    // Alteramos de List para Page para suportar os metadados de paginação
    // A query DESC garante que os IDs maiores (mais recentes) venham primeiro
    @Query("SELECT c FROM CommentEntity c ORDER BY c.id DESC")
    Page<CommentEntity> buscarPorUltimos(Pageable pageable);
}

