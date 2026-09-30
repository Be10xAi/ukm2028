package com.tech10x.ukm.serviceimpl;

import com.tech10x.ukm.entity.AuditAction;
import com.tech10x.ukm.entity.AuditLog;
import com.tech10x.ukm.entity.Users;
import com.tech10x.ukm.repository.AuditLogRepository;
import com.tech10x.ukm.repositoryproxy.UserRepository;
import com.tech10x.ukm.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    /** Design doc: sensitive fields must never appear in AUDIT_LOG's old/new value JSON. */
    private static final Set<String> FORBIDDEN_KEYS =
            Set.of("password", "bankaccountno", "bankaccountnumber", "idproofnumber", "otp", "token", "secret");

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void record(String actorUserId, String entityName, String entityId, AuditAction action,
                       Map<String, String> oldValue, Map<String, String> newValue) {
        Users user = actorUserId == null ? null : userRepository.findByUserId(actorUserId).orElse(null);
        auditLogRepository.save(AuditLog.builder()
                .user(user)
                .entityName(entityName)
                .entityId(entityId)
                .action(action)
                .oldValue(toJson(oldValue))
                .newValue(toJson(newValue))
                .build());
    }

    private static String toJson(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> e : new TreeMap<>(values).entrySet()) {
            String normalisedKey = e.getKey().toLowerCase(Locale.ROOT).replace("_", "");
            if (FORBIDDEN_KEYS.contains(normalisedKey)) {
                throw new IllegalArgumentException("Sensitive field must not be written to the audit log: " + e.getKey());
            }
            if (!first) {
                json.append(',');
            }
            first = false;
            json.append('"').append(escape(e.getKey())).append("\":");
            json.append(e.getValue() == null ? "null" : "\"" + escape(e.getValue()) + "\"");
        }
        return json.append('}').toString();
    }

    private static String escape(String s) {
        StringBuilder out = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }
}
