package com.tech10x.ukm.service;

import com.tech10x.ukm.entity.AuditAction;

import java.util.Map;

public interface AuditService {

    /**
     * Appends one row to the insert-only audit log, inside the caller's transaction so the
     * change and its audit record commit (or roll back) together.
     * Values must be non-sensitive - keys such as password / bank account / id-proof number are rejected.
     *
     * @param actorUserId the acting user's userId, or null for system actions
     */
    void record(String actorUserId, String entityName, String entityId, AuditAction action,
                Map<String, String> oldValue, Map<String, String> newValue);
}
