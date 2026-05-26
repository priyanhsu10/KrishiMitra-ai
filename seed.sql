-- ------------------------------------------------------------------
-- KrishiMitra Test Data – Seed for quick API validation
-- ------------------------------------------------------------------
-- The UUIDs are explicitly specified so that foreign-key relations
-- are deterministic and can be queried directly from the API.
-- ------------------------------------------------------------------

-- -------------------------------------------------
-- 1️⃣ Farmers
-- -------------------------------------------------
INSERT INTO farmers (id, mobile, name, language, village, state, fcm_token)
VALUES
  ('11111111-1111-1111-1111-111111111111', '9876543210', 'Ramesh Patil', 'marathi', 'Pune Rural', 'Maharashtra', 'fcm_test_token_123'),
  ('22222222-2222-2222-2222-222222222222', '9123456789', 'Sunita Sharma', 'hindi', 'Nagpur', 'Maharashtra', NULL);

-- -------------------------------------------------
-- 2️⃣ Farms
-- -------------------------------------------------
INSERT INTO farms (id, farmer_id, name, latitude, longitude, area_acres, soil_type)
VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'Main Farm', 18.5204, 73.8567, 5.50, 'black'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222', 'Green Farm', 21.1702, 78.0308, 10.00, 'loamy');

-- -------------------------------------------------
-- 3️⃣ Crops
-- -------------------------------------------------
INSERT INTO crops (id, farm_id, crop_type, sowing_date, stage)
VALUES
  ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'cotton', '2025-01-15', 'germination'),
  ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'soybean', '2025-02-20', 'vegetative');

-- -------------------------------------------------
-- 4️⃣ Advisories
-- -------------------------------------------------
INSERT INTO advisories (id, crop_id, farmer_id, alert_type,
                       message_en, message_mr, message_hi,
                       priority, is_read, fcm_sent)
VALUES
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'cccccccc-cccc-cccc-cccc-cccccccccccc', '11111111-1111-1111-1111-111111111111', 'weather',
   'Rain expected next week', 'पुढील आठवड्यात पाऊस अपेक्षित', 'आगामी सप्ताह में बारिश अपेक्षित', 'high', false, false),

  ('ffffffff-ffff-ffff-ffff-ffffffffffff', 'dddddddd-dddd-dddd-dddd-dddddddddddd', '22222222-2222-2222-2222-222222222222', 'disease',
   'Cotton leaf spot disease', 'कॉटन लीफ स्पॉट रोग', 'कॉटन लिफ़ स्पॉट रोग', 'medium', false, false);

-- -------------------------------------------------
-- 5️⃣ Disease Reports
-- -------------------------------------------------
INSERT INTO disease_reports (id, farmer_id, crop_id, image_url,
                            diagnosis, diagnosis_mr, confidence,
                            remedy_en, remedy_mr, severity)
VALUES
  ('gggggggg-gggg-gggg-gggg-gggggggggggg', '11111111-1111-1111-1111-111111111111', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'http://example.com/image1.jpg',
   'Cotton Leaf Spot', 'कॉटन लीफ स्पॉट', 0.85,
   'Apply 2% copper sulfate', '2% कॉपर सल्फेट लावा', 'high'),

  ('hhhhhhhh-hhhh-hhhh-hhhh-hhhhhhhhhhhh', '22222222-2222-2222-2222-222222222222', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 'http://example.com/image2.jpg',
   'Soybean Septoria brown spot', 'सोयाबीन सॅप्टोरिया ब्राउन स्पॉट', 0.78,
   'Use 0.5% sulfuric acid', '0.5% सल्फ्युरिक अॅसिड वापरा', 'medium');