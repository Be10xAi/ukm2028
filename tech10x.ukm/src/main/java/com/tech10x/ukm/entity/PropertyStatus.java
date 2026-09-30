package com.tech10x.ukm.entity;

/** PROPERTY.status - DRAFT until an admin publishes it; SUSPENDED replaces hard deletes. */
public enum PropertyStatus {
    DRAFT,
    LIVE,
    SUSPENDED
}
