CREATE DATABASE booking_system;
USE booking_system;

CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('Customer', 'Specialist', 'Admin') NOT NULL,
    avatar VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE specialists (
    user_id VARCHAR(36) PRIMARY KEY,
    bio TEXT,
    price DECIMAL(10,2) DEFAULT 0.00,
    status ENUM('Active', 'Inactive') DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_specialists_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE expertise (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE specialist_expertise (
    specialist_id VARCHAR(36),
    expertise_id VARCHAR(36),

    PRIMARY KEY (specialist_id, expertise_id),

    CONSTRAINT fk_se_specialist
        FOREIGN KEY (specialist_id) REFERENCES specialists(user_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_se_expertise
        FOREIGN KEY (expertise_id) REFERENCES expertise(id)
        ON DELETE CASCADE
);

CREATE TABLE slots (
    id VARCHAR(36) PRIMARY KEY,
    specialist_id VARCHAR(36) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_slots_specialist
        FOREIGN KEY (specialist_id) REFERENCES specialists(user_id)
        ON DELETE CASCADE
);

CREATE INDEX idx_slots_specialist_time 
ON slots(specialist_id, start_time);

CREATE TABLE bookings (
    id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    specialist_id VARCHAR(36) NOT NULL,
    slot_id VARCHAR(36) NOT NULL,
    note TEXT,
    status ENUM(
        'Pending',
        'Confirmed',
        'Cancelled',
        'Rescheduled',
        'Completed',
        'Rejected'
    ) DEFAULT 'Pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_booking_customer
        FOREIGN KEY (customer_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_booking_specialist
        FOREIGN KEY (specialist_id) REFERENCES specialists(user_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_booking_slot
        FOREIGN KEY (slot_id) REFERENCES slots(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_booking_customer ON bookings(customer_id);
CREATE INDEX idx_booking_specialist ON bookings(specialist_id);
CREATE INDEX idx_booking_slot ON bookings(slot_id);

CREATE TABLE booking_history (
    id VARCHAR(36) PRIMARY KEY,
    booking_id VARCHAR(36) NOT NULL,
    status ENUM(
        'Pending',
        'Confirmed',
        'Cancelled',
        'Rescheduled',
        'Completed',
        'Rejected'
    ),
    reason TEXT,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_history_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(id)
        ON DELETE CASCADE
);

CREATE TABLE pricing (
                         id VARCHAR(36) PRIMARY KEY,  -- UUID 主键
                         specialist_id VARCHAR(36) NOT NULL,  -- 外键关联 specialists
                         duration INT NOT NULL,  -- 时长（分钟）
                         type VARCHAR(20) NOT NULL,  -- 服务类型，如 online
                         amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,  -- 金额
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  -- 更新时间
                         currency VARCHAR(10) DEFAULT 'CNY',  -- 货币单位，默认人民币
                         detail TEXT,  -- 描述
                         CONSTRAINT fk_pricing_specialist
                             FOREIGN KEY (specialist_id) REFERENCES specialists(user_id)
                                 ON DELETE CASCADE
);

-- 可以加索引提升查询效率
CREATE INDEX idx_pricing_specialist ON pricing(specialist_id);
CREATE INDEX idx_pricing_type_duration ON pricing(type, duration);