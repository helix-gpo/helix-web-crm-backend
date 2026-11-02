-- hibernate generated these with default lengths, too small for serialized events
ALTER TABLE event_publication
    MODIFY serialized_event VARCHAR(4000) NOT NULL,
    MODIFY event_type VARCHAR(512) NOT NULL,
    MODIFY listener_id VARCHAR(512) NOT NULL;