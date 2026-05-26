-- Farmers table
CREATE TABLE farmers (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mobile      VARCHAR(15) UNIQUE NOT NULL,
    name        VARCHAR(100),
    language    VARCHAR(20) DEFAULT 'marathi',
    village     VARCHAR(100),
    state       VARCHAR(100),
    fcm_token   VARCHAR(500),
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_farmers_mobile ON farmers(mobile);
CREATE INDEX idx_farmers_fcm_token ON farmers(fcm_token);

-- Farms table
CREATE TABLE farms (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farmer_id   UUID REFERENCES farmers(id) ON DELETE CASCADE,
    name        VARCHAR(100),
    latitude    DECIMAL(10,6),
    longitude   DECIMAL(10,6),
    area_acres  DECIMAL(6,2),
    soil_type   VARCHAR(50),
    created_at  TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_farms_farmer_id ON farms(farmer_id);

-- Crops table
CREATE TABLE crops (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id      UUID REFERENCES farms(id) ON DELETE CASCADE,
    crop_type    VARCHAR(100),
    sowing_date  DATE,
    stage        VARCHAR(50),
    created_at   TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_crops_farm_id ON crops(farm_id);

-- Advisories table
CREATE TABLE advisories (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    crop_id      UUID REFERENCES crops(id),
    farmer_id    UUID REFERENCES farmers(id),
    alert_type   VARCHAR(50),
    message_en   TEXT,
    message_mr   TEXT,
    message_hi   TEXT,
    priority     VARCHAR(10) DEFAULT 'medium',
    is_read      BOOLEAN DEFAULT FALSE,
    fcm_sent     BOOLEAN DEFAULT FALSE,
    created_at   TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_advisories_farmer_id ON advisories(farmer_id);
CREATE INDEX idx_advisories_is_read ON advisories(is_read);
CREATE INDEX idx_advisories_created_at ON advisories(created_at DESC);

-- Disease reports table
CREATE TABLE disease_reports (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farmer_id     UUID REFERENCES farmers(id),
    crop_id       UUID REFERENCES crops(id),
    image_url     VARCHAR(500),
    diagnosis     TEXT,
    diagnosis_mr  TEXT,
    confidence    DECIMAL(5,2),
    remedy_en     TEXT,
    remedy_mr     TEXT,
    severity      VARCHAR(20),
    created_at    TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_disease_reports_farmer_id ON disease_reports(farmer_id);
CREATE INDEX idx_disease_reports_crop_id ON disease_reports(crop_id);
