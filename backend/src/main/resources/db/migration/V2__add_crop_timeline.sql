-- Crop timeline items table
CREATE TABLE crop_timeline_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    crop_id         UUID REFERENCES crops(id) ON DELETE CASCADE,
    stage           VARCHAR(100) NOT NULL,
    estimated_date  DATE NOT NULL,
    description     TEXT,
    completed       BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_crop_timeline_crop_id ON crop_timeline_items(crop_id);
CREATE INDEX idx_crop_timeline_estimated_date ON crop_timeline_items(estimated_date);
