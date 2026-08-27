CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(80) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL,
  display_name VARCHAR(120) NOT NULL
);

CREATE TABLE farmers (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  farmer_code VARCHAR(40) NOT NULL UNIQUE,
  mobile VARCHAR(20) NOT NULL UNIQUE,
  address VARCHAR(255),
  village VARCHAR(80),
  district VARCHAR(80),
  state VARCHAR(80),
  CONSTRAINT fk_farmer_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE procurement_centre (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(120) NOT NULL,
  location VARCHAR(120),
  address VARCHAR(255),
  working_hours VARCHAR(80),
  daily_capacity INT NOT NULL
);

CREATE TABLE crop (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(80) NOT NULL,
  rate_per_kg DECIMAL(10,2) NOT NULL
);

CREATE TABLE slot (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  centre_id BIGINT NOT NULL,
  slot_date DATE NOT NULL,
  time_range VARCHAR(40) NOT NULL,
  capacity INT NOT NULL,
  open_flag BOOLEAN DEFAULT TRUE,
  CONSTRAINT fk_slot_centre FOREIGN KEY (centre_id) REFERENCES procurement_centre(id)
);

CREATE UNIQUE INDEX uk_slot_centre_date_time ON slot(centre_id, slot_date, time_range);
CREATE INDEX idx_slot_centre_date ON slot(centre_id, slot_date);

CREATE TABLE booking (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  farmer_id BIGINT NOT NULL,
  centre_id BIGINT NOT NULL,
  crop_id BIGINT NOT NULL,
  slot_id BIGINT NOT NULL,
  business_date DATE NOT NULL,
  token_sequence INT NOT NULL,
  token_number VARCHAR(40) NOT NULL UNIQUE,
  quantity_kg INT NOT NULL,
  status VARCHAR(30) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME,
  called_at DATETIME,
  arrived_at DATETIME,
  verification_started_at DATETIME,
  procurement_started_at DATETIME,
  completed_at DATETIME,
  cancelled_at DATETIME,
  cancellation_reason VARCHAR(255),
  weighed_quantity_kg INT DEFAULT 0,
  accepted_quantity_kg INT DEFAULT 0,
  rate_per_kg DECIMAL(10,2),
  procurement_amount DECIMAL(12,2),
  payment_reference VARCHAR(120),
  payment_updated_at DATETIME,
  payment_status VARCHAR(30) NOT NULL,
  CONSTRAINT fk_booking_farmer FOREIGN KEY (farmer_id) REFERENCES farmers(id),
  CONSTRAINT fk_booking_centre FOREIGN KEY (centre_id) REFERENCES procurement_centre(id),
  CONSTRAINT fk_booking_crop FOREIGN KEY (crop_id) REFERENCES crop(id),
  CONSTRAINT fk_booking_slot FOREIGN KEY (slot_id) REFERENCES slot(id)
);

CREATE INDEX idx_booking_centre_date_status_created ON booking(centre_id, business_date, status, created_at);
CREATE INDEX idx_booking_farmer_status ON booking(farmer_id, status);
CREATE INDEX idx_booking_slot_date ON booking(slot_id, business_date);

CREATE TABLE booking_token_sequence (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  centre_id BIGINT NOT NULL,
  business_date DATE NOT NULL,
  next_value INT NOT NULL,
  CONSTRAINT uk_token_sequence_centre_date UNIQUE (centre_id, business_date),
  CONSTRAINT fk_token_sequence_centre FOREIGN KEY (centre_id) REFERENCES procurement_centre(id)
);

CREATE TABLE procurement_counter (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  centre_id BIGINT NOT NULL,
  name VARCHAR(120) NOT NULL,
  active_flag BOOLEAN DEFAULT TRUE,
  current_booking_id BIGINT,
  officer_id BIGINT,
  CONSTRAINT uk_counter_centre_name UNIQUE (centre_id, name),
  CONSTRAINT fk_counter_centre FOREIGN KEY (centre_id) REFERENCES procurement_centre(id),
  CONSTRAINT fk_counter_booking FOREIGN KEY (current_booking_id) REFERENCES booking(id),
  CONSTRAINT fk_counter_officer FOREIGN KEY (officer_id) REFERENCES users(id)
);

CREATE TABLE notification (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  message VARCHAR(255) NOT NULL,
  type VARCHAR(80),
  read_flag BOOLEAN DEFAULT FALSE,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_notification_user_created ON notification(user_id, created_at);

CREATE TABLE audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  action VARCHAR(80) NOT NULL,
  entity_type VARCHAR(80) NOT NULL,
  entity_id VARCHAR(80) NOT NULL,
  old_state VARCHAR(2000),
  new_state VARCHAR(2000),
  request_metadata VARCHAR(255),
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_audit_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_created ON audit_log(created_at);
