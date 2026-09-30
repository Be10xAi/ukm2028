package com.tech10x.ukm.repository;

import com.tech10x.ukm.entity.AuditLog;
import org.springframework.data.repository.Repository;

/**
 * Deliberately NOT a JpaRepository: the audit log is insert-only, so no update/delete
 * (or even read-all) methods are exposed to the rest of the application.
 */
@org.springframework.stereotype.Repository
public interface AuditLogRepository extends Repository<AuditLog, Long> {

    <S extends AuditLog> S save(S entity);
}
