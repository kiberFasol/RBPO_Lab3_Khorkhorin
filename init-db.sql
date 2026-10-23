-- Инициализация БД для Лабораторной работы 3 (Вариант 22)
-- Запуск выполняется от суперпользователя postgres

SELECT 'CREATE DATABASE dormitory_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'dormitory_db')\gexec

DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'dorm_app_user') THEN
        CREATE ROLE dorm_app_user WITH LOGIN PASSWORD 'dorm_secure_pass123' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT;
    ELSE
        ALTER ROLE dorm_app_user WITH PASSWORD 'dorm_secure_pass123';
    END IF;
END
$$;

\connect dormitory_db

-- Таблицы и ограничения
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT chk_user_email_format CHECK (email LIKE '%@%'),
    CONSTRAINT chk_user_role CHECK (role IN ('STUDENT', 'ADMIN', 'STAFF'))
);

CREATE TABLE IF NOT EXISTS rooms (
    id BIGSERIAL PRIMARY KEY,
    room_number VARCHAR(50) NOT NULL UNIQUE,
    capacity INTEGER NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_room_capacity CHECK (capacity > 0)
);

CREATE TABLE IF NOT EXISTS housing_applications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    submission_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_app_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_app_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'))
);

CREATE TABLE IF NOT EXISTS residence_contracts (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_contract_application FOREIGN KEY (application_id) REFERENCES housing_applications(id) ON DELETE RESTRICT,
    CONSTRAINT fk_contract_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE RESTRICT,
    CONSTRAINT chk_contract_dates CHECK (check_out_date IS NULL OR check_in_date <= check_out_date),
    CONSTRAINT chk_contract_status CHECK (status IN ('ACTIVE', 'TERMINATED', 'EXPIRED'))
);

CREATE TABLE IF NOT EXISTS room_transfer_requests (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    target_room_id BIGINT NOT NULL,
    submission_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_transfer_contract FOREIGN KEY (contract_id) REFERENCES residence_contracts(id) ON DELETE CASCADE,
    CONSTRAINT fk_transfer_room FOREIGN KEY (target_room_id) REFERENCES rooms(id) ON DELETE RESTRICT,
    CONSTRAINT chk_transfer_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'))
);

-- Индексы
CREATE INDEX IF NOT EXISTS idx_contracts_room_status ON residence_contracts(room_id, status);
CREATE INDEX IF NOT EXISTS idx_contracts_app ON residence_contracts(application_id);
CREATE INDEX IF NOT EXISTS idx_contracts_dates ON residence_contracts(check_out_date, status);
CREATE INDEX IF NOT EXISTS idx_applications_user_status ON housing_applications(user_id, status);
CREATE INDEX IF NOT EXISTS idx_transfers_contract_status ON room_transfer_requests(contract_id, status);

-- Настройка прав: пользователю сервиса даем только DML (чтение и запись данных)
REVOKE CREATE ON SCHEMA public FROM PUBLIC;
REVOKE CREATE ON SCHEMA public FROM dorm_app_user;

GRANT CONNECT ON DATABASE dormitory_db TO dorm_app_user;
GRANT USAGE ON SCHEMA public TO dorm_app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO dorm_app_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO dorm_app_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO dorm_app_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO dorm_app_user;

-- Тестовые данные
TRUNCATE TABLE room_transfer_requests, residence_contracts, housing_applications, rooms, users RESTART IDENTITY CASCADE;

INSERT INTO users (full_name, email, role) VALUES
    ('Иванов Иван Иванович', 'ivanov@university.ru', 'STUDENT'),
    ('Петров Петр Сергеевич', 'petrov@university.ru', 'STUDENT'),
    ('Сидорова Анна Павловна', 'sidorova@university.ru', 'STUDENT'),
    ('Смирнов Алексей Игоревич', 'smirnov@university.ru', 'STUDENT'),
    ('Васильев Олег Петрович', 'vasiliev@university.ru', 'ADMIN');

INSERT INTO rooms (room_number, capacity, is_available) VALUES
    ('101', 2, TRUE),
    ('102', 1, TRUE),
    ('201', 3, TRUE),
    ('202', 2, TRUE),
    ('301-VIP', 1, TRUE);

INSERT INTO housing_applications (user_id, submission_date, status) VALUES
    (1, CURRENT_DATE - INTERVAL '10 days', 'APPROVED'),
    (2, CURRENT_DATE - INTERVAL '5 days', 'APPROVED'),
    (3, CURRENT_DATE - INTERVAL '2 days', 'PENDING'),
    (4, CURRENT_DATE - INTERVAL '1 day', 'PENDING');

INSERT INTO residence_contracts (application_id, room_id, check_in_date, check_out_date, status) VALUES
    (1, 1, CURRENT_DATE - INTERVAL '9 days', CURRENT_DATE + INTERVAL '300 days', 'ACTIVE'),
    (2, 2, CURRENT_DATE - INTERVAL '4 days', CURRENT_DATE + INTERVAL '250 days', 'ACTIVE');

-- В комнате 102 занято единственное место, поэтому закрываем доступность
UPDATE rooms SET is_available = FALSE WHERE id = 2;

INSERT INTO room_transfer_requests (contract_id, target_room_id, submission_date, status) VALUES
    (1, 3, CURRENT_DATE - INTERVAL '1 day', 'PENDING');
