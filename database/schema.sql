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
  CONSTRAINT fk_slot_centre FOREIGN KEY (centre_id) REFERENCES procurement_centre(id)
);

CREATE TABLE booking (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  farmer_id BIGINT NOT NULL,
  centre_id BIGINT NOT NULL,
  crop_id BIGINT NOT NULL,
  slot_id BIGINT NOT NULL,
  token_number VARCHAR(20) NOT NULL UNIQUE,
  quantity_kg INT NOT NULL,
  status VARCHAR(30) NOT NULL,
  created_at DATETIME NOT NULL,
  procurement_amount DECIMAL(12,2),
  payment_status VARCHAR(30) NOT NULL,
  CONSTRAINT fk_booking_farmer FOREIGN KEY (farmer_id) REFERENCES farmers(id),
  CONSTRAINT fk_booking_centre FOREIGN KEY (centre_id) REFERENCES procurement_centre(id),
  CONSTRAINT fk_booking_crop FOREIGN KEY (crop_id) REFERENCES crop(id),
  CONSTRAINT fk_booking_slot FOREIGN KEY (slot_id) REFERENCES slot(id)
);

CREATE TABLE notification (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  message VARCHAR(255) NOT NULL,
  read_flag BOOLEAN DEFAULT FALSE,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id)
);
