package com.library.user.infrastructure.persistence.repository;

import com.library.user.infrastructure.persistence.entity.OutboxEventEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataOutboxJpaRepository extends JpaRepository<OutboxEventEntity, Long> {

    List<OutboxEventEntity> findTop50ByStatusOrderByIdAsc(String status);
}
