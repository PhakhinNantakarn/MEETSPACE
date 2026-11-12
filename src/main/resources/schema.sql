/* 
 * Table: room_image
 * Stores room images in binary format.
 * ตาราง: room_image
 * ใช้สำหรับเก็บรูปภาพของห้องประชุมในรูปแบบไบนารี (BLOB)
 */
CREATE TABLE IF NOT EXISTS room_image (
    id VARCHAR(36) PRIMARY KEY, -- Unique image ID / รหัสรูปภาพ
    content BLOB NOT NULL       -- Binary image data / ข้อมูลรูปภาพแบบไบนารี
);

/* 
 * Table: app_user
 * Stores user account information, roles, and credentials.
 * ตาราง: app_user
 * ใช้สำหรับเก็บข้อมูลบัญชีผู้ใช้งาน บทบาท (Role) และรหัสผ่าน
 */
CREATE TABLE IF NOT EXISTS app_user (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(160) NOT NULL UNIQUE,       -- User email (must be unique) / อีเมลผู้ใช้ (ห้ามซ้ำ)
    name VARCHAR(100) NOT NULL,               -- Full name / ชื่อ-นามสกุล
    password_hash VARCHAR(100) NOT NULL,      -- Hashed password / รหัสผ่านที่เข้ารหัสแล้ว
    role VARCHAR(10) NOT NULL,                -- User role (e.g., admin, member) / บทบาทผู้ใช้
    self_registered BOOLEAN NOT NULL DEFAULT FALSE -- Flag for self-registration / บ่งบอกว่าผู้ใช้สมัครสมาชิกเองหรือไม่
);

/* 
 * Table: room
 * Stores room details, settings, capacity, and physical attributes.
 * ตาราง: room
 * ใช้สำหรับเก็บรายละเอียดห้องประชุม การตั้งค่า ความจุ และคุณลักษณะต่างๆ ของห้อง
 */
CREATE TABLE IF NOT EXISTS room (
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,               -- Room name (English) / ชื่อห้องประชุม (ภาษาอังกฤษ)
    name_th VARCHAR(100) NOT NULL,            -- Room name (Thai) / ชื่อห้องประชุม (ภาษาไทย)
    location VARCHAR(120) NOT NULL,           -- Location (English) / สถานที่ตั้ง (ภาษาอังกฤษ)
    location_th VARCHAR(120) NOT NULL,        -- Location (Thai) / สถานที่ตั้ง (ภาษาไทย)
    capacity INT NOT NULL,                    -- Maximum number of people / ความจุสูงสุดของห้อง
    approval BOOLEAN NOT NULL DEFAULT FALSE,  -- Requires admin approval / ต้องได้รับการอนุมัติจากแอดมินหรือไม่
    active BOOLEAN NOT NULL DEFAULT TRUE,     -- Is room currently available / ห้องยังเปิดใช้งานอยู่หรือไม่
    amenities VARCHAR(300) NOT NULL DEFAULT '', -- List of room amenities / สิ่งอำนวยความสะดวกในห้อง
    color VARCHAR(20) NOT NULL DEFAULT 'indigo',-- UI display color / สีสำหรับแสดงผลในหน้าจอ
    image_url VARCHAR(300) NOT NULL DEFAULT ''  -- URL to the room's image / ลิงก์รูปภาพของห้อง
);

/* 
 * Table: booking
 * Stores room booking transactions, including time, participants, and status.
 * ตาราง: booking
 * ใช้สำหรับเก็บรายการจองห้องประชุม รวมถึงเวลา จำนวนผู้เข้าร่วม และสถานะ
 */
CREATE TABLE IF NOT EXISTS booking (
    id VARCHAR(36) PRIMARY KEY,
    room_id VARCHAR(20) NOT NULL,             -- Room being booked / รหัสห้องที่ถูกจอง
    user_id VARCHAR(36) NOT NULL,             -- User who made the booking / รหัสผู้จอง
    title VARCHAR(160) NOT NULL,              -- Booking title/topic / หัวข้อหรือชื่อการจอง
    booking_date VARCHAR(10) NOT NULL,        -- Date of booking (YYYY-MM-DD) / วันที่จอง 
    start_time VARCHAR(5) NOT NULL,           -- Start time (HH:MM) / เวลาเริ่ม
    end_time VARCHAR(5) NOT NULL,             -- End time (HH:MM) / เวลาสิ้นสุด
    participants INT NOT NULL,                -- Number of attendees / จำนวนผู้เข้าร่วม
    status VARCHAR(20) NOT NULL,              -- Booking status (pending, confirmed, etc.) / สถานะการจอง
    series_id VARCHAR(36),                    -- ID for recurring bookings / รหัสสำหรับการจองแบบต่อเนื่อง (ซีรีส์)
    request_key VARCHAR(80) NOT NULL,         -- Idempotency key to prevent duplicates / คีย์ป้องกันการกดจองซ้ำ
    created_at VARCHAR(40) NOT NULL,          -- Creation timestamp / เวลาที่สร้างรายการ
    updated_at VARCHAR(40) NOT NULL,          -- Last update timestamp / เวลาที่อัปเดตล่าสุด
    reason VARCHAR(300) NOT NULL DEFAULT '',  -- Rejection/Cancellation reason / เหตุผลการปฏิเสธหรือยกเลิก
    CONSTRAINT fk_booking_room FOREIGN KEY (room_id) REFERENCES room(id),
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES app_user(id),
    CONSTRAINT uq_booking_request UNIQUE (user_id, request_key, booking_date) -- Prevents duplicate bookings from the same user on the same day with the same key
);

/* 
 * Schema Updates (Alterations)
 * การปรับปรุงโครงสร้างตาราง
 */
-- Soft delete flag for rooms / เพิ่มฟิลด์สำหรับซ่อนห้องประชุมที่ถูกลบ (Soft Delete)
ALTER TABLE room ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Store exact booker name for walk-ins or overrides / เพิ่มฟิลด์เก็บชื่อผู้จอง (มีประโยชน์สำหรับเคส Walk-in)
ALTER TABLE booking ADD COLUMN IF NOT EXISTS booker VARCHAR(100) NOT NULL DEFAULT '';

/* 
 * Table: booking_event
 * Stores notification and audit events related to bookings (e.g., created, approved, rejected).
 * ตาราง: booking_event
 * ใช้สำหรับเก็บประวัติเหตุการณ์และการแจ้งเตือนที่เกี่ยวกับการจอง (เช่น สร้างการจอง, อนุมัติ, ปฏิเสธ)
 */
CREATE TABLE IF NOT EXISTS booking_event (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,             -- Target user for the event / ผู้ใช้ที่เกี่ยวข้องกับเหตุการณ์
    booking_id VARCHAR(36) NOT NULL,          -- Related booking ID / รหัสการจองที่เกี่ยวข้อง
    kind VARCHAR(30) NOT NULL,                -- Type of event / ประเภทของเหตุการณ์
    event_at VARCHAR(40) NOT NULL,            -- Timestamp of the event / เวลาที่เกิดเหตุการณ์
    actor VARCHAR(100) NOT NULL,              -- Person who triggered the event / ชื่อผู้กระทำการ (เช่น แอดมิน)
    CONSTRAINT fk_event_user FOREIGN KEY (user_id) REFERENCES app_user(id),
    CONSTRAINT fk_event_booking FOREIGN KEY (booking_id) REFERENCES booking(id)
);

/* 
 * Database Indices
 * Indexes for query performance optimization.
 * ดัชนีฐานข้อมูลสำหรับการเพิ่มประสิทธิภาพในการค้นหา
 */
-- Optimize availability lookups / เพิ่มความเร็วในการค้นหาคิวว่างของห้องในแต่ละวัน
CREATE INDEX IF NOT EXISTS idx_booking_room_date ON booking(room_id, booking_date);

-- Optimize user booking history lookups / เพิ่มความเร็วในการดูประวัติการจองของผู้ใช้
CREATE INDEX IF NOT EXISTS idx_booking_user ON booking(user_id);

-- Optimize notifications queries for users / เพิ่มความเร็วในการดึงการแจ้งเตือนของผู้ใช้
CREATE INDEX IF NOT EXISTS idx_event_user ON booking_event(user_id);