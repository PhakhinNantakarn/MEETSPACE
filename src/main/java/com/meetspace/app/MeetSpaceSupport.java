package com.meetspace.app;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Shared database access and models used by the feature-specific controllers.
 * This is not itself a web controller; it intentionally has no endpoint mappings.
 * 
 * คลาสที่ใช้แชร์การเข้าถึงฐานข้อมูลและโมเดลข้อมูลสำหรับคอนโทรลเลอร์ต่างๆ 
 * คลาสนี้ไม่ใช่ Web Controller ในตัวมันเอง จึงไม่มีการกำหนด Endpoint
 */
abstract class MeetSpaceSupport {
    protected static final ZoneId BANGKOK = ZoneId.of("Asia/Bangkok");
    private static final Set<String> ACTIVE_STATUSES = Set.of("pending", "confirmed");
    protected final JdbcTemplate jdbc;

    /**
     * Constructs the MeetSpaceSupport with the provided JdbcTemplate.
     * สร้างอินสแตนซ์ของ MeetSpaceSupport โดยกำหนดค่า JdbcTemplate สำหรับใช้งาน
     *
     * @param jdbc - The JdbcTemplate instance for database access / อินสแตนซ์ JdbcTemplate สำหรับเข้าถึงฐานข้อมูล
     */
    protected MeetSpaceSupport(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Retrieves the currently logged-in user from the HTTP session.
     * ดึงข้อมูลผู้ใช้ปัจจุบันที่เข้าสู่ระบบจาก HTTP เซสชัน
     *
     * @param request - The incoming HTTP request / คำขอ HTTP ที่ส่งเข้ามา
     * @return The current User object, or null if not logged in / ออบเจ็กต์ของผู้ใช้ปัจจุบัน หรือ null หากไม่ได้เข้าสู่ระบบ
     */
    protected User current(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object uid = session.getAttribute("uid");
        return uid == null ? null : findUserById(uid.toString());
    }

    /**
     * Checks if a booking's scheduled time is in the future relative to the current time in Bangkok.
     * ตรวจสอบว่าช่วงเวลาของการจองเป็นเวลาในอนาคตหรือไม่ โดยอิงตามเวลาปัจจุบันของกรุงเทพมหานคร
     *
     * @param booking - The booking to check / ข้อมูลการจองที่ต้องการตรวจสอบ
     * @return True if the booking is in the future, false otherwise / คืนค่า True หากการจองอยู่ในอนาคต หรือ False หากไม่ใช่
     */
    protected boolean isFuture(Booking booking) {
        ZonedDateTime now = ZonedDateTime.now(BANGKOK);
        LocalDate date = LocalDate.parse(booking.date);
        if (date.isAfter(now.toLocalDate())) return true;
        String currentTime = now.toLocalTime().withSecond(0).withNano(0).toString().substring(0, 5);
        return date.equals(now.toLocalDate()) && booking.end.compareTo(currentTime) > 0;
    }

    /**
     * Checks if the given booking status is considered active (e.g., pending or confirmed).
     * ตรวจสอบว่าสถานะการจองที่ระบุถือเป็นสถานะที่กำลังใช้งานอยู่หรือไม่ (เช่น pending หรือ confirmed)
     *
     * @param status - The booking status string / สตริงระบุสถานะการจอง
     * @return True if active, false otherwise / คืนค่า True หากกำลังใช้งาน หรือ False หากไม่ใช่
     */
    protected static boolean isActive(String status) {
        return ACTIVE_STATUSES.contains(status);
    }

    /**
     * Retrieves a list of all registered users, excluding the 'walk-in' identifier.
     * ดึงข้อมูลผู้ใช้ที่ลงทะเบียนในระบบทั้งหมด ยกเว้นผู้ใช้ประเภท 'walk-in'
     *
     * @return A list of User objects / ลิสต์ของออบเจ็กต์ผู้ใช้ทั้งหมด
     */
    protected List<User> allUsers() {
        return jdbc.query("SELECT * FROM app_user WHERE id <> 'walk-in' ORDER BY name", this::mapUser);
    }

    /**
     * Finds a specific user by their unique ID.
     * ค้นหาข้อมูลผู้ใช้ที่เจาะจงจากรหัสประจำตัว (ID)
     *
     * @param id - The user ID / รหัสผู้ใช้
     * @return The User object if found, or null / ออบเจ็กต์ผู้ใช้หากพบข้อมูล หรือ null หากไม่พบ
     */
    protected User findUserById(String id) {
        return first(jdbc.query("SELECT * FROM app_user WHERE id = ?", this::mapUser, id));
    }

    /**
     * Finds a specific user by their email address (case-insensitive).
     * ค้นหาข้อมูลผู้ใช้จากที่อยู่อีเมล (ไม่สนใจตัวพิมพ์เล็ก-ใหญ่)
     *
     * @param email - The user email / อีเมลของผู้ใช้
     * @return The User object if found, or null / ออบเจ็กต์ผู้ใช้หากพบข้อมูล หรือ null หากไม่พบ
     */
    protected User findUserByEmail(String email) {
        return first(jdbc.query("SELECT * FROM app_user WHERE LOWER(email) = LOWER(?)", this::mapUser, email));
    }

    /**
     * Maps a row from a database ResultSet to a User object.
     * แปลงข้อมูลหนึ่งแถวจาก ResultSet ของฐานข้อมูลให้เป็นออบเจ็กต์ User
     *
     * @param rs - The ResultSet / ชุดข้อมูลผลลัพธ์จากฐานข้อมูล
     * @param row - The current row number / หมายเลขแถวปัจจุบัน
     * @return A mapped User object / ออบเจ็กต์ User ที่ถูกสร้างขึ้น
     * @throws SQLException - If a database access error occurs / หากเกิดข้อผิดพลาดในการเข้าถึงฐานข้อมูล
     */
    private User mapUser(ResultSet rs, int row) throws SQLException {
        return new User(rs.getString("id"), rs.getString("email"), rs.getString("name"),
                rs.getString("password_hash"), rs.getString("role"), rs.getBoolean("self_registered"));
    }

    /**
     * Retrieves a list of all rooms in the system.
     * ดึงข้อมูลห้องประชุมทั้งหมดในระบบ
     *
     * @return A list of Room objects / ลิสต์ของออบเจ็กต์ห้องประชุมทั้งหมด
     */
    protected List<Room> allRooms() {
        return jdbc.query("SELECT * FROM room ORDER BY id", this::mapRoom);
    }

    /**
     * Finds a specific room by its ID.
     * ค้นหาห้องประชุมจากรหัสห้อง
     *
     * @param id - The room ID / รหัสห้องประชุม
     * @return The Room object if found, or null / ออบเจ็กต์ห้องประชุมหากพบข้อมูล หรือ null หากไม่พบ
     */
    protected Room findRoom(String id) {
        return first(jdbc.query("SELECT * FROM room WHERE id = ?", this::mapRoom, id));
    }

    /**
     * Maps a row from a database ResultSet to a Room object.
     * แปลงข้อมูลหนึ่งแถวจาก ResultSet ของฐานข้อมูลให้เป็นออบเจ็กต์ Room
     *
     * @param rs - The ResultSet / ชุดข้อมูลผลลัพธ์จากฐานข้อมูล
     * @param row - The current row number / หมายเลขแถวปัจจุบัน
     * @return A mapped Room object / ออบเจ็กต์ Room ที่ถูกสร้างขึ้น
     * @throws SQLException - If a database access error occurs / หากเกิดข้อผิดพลาดในการเข้าถึงฐานข้อมูล
     */
    private Room mapRoom(ResultSet rs, int row) throws SQLException {
        return new Room(rs.getString("id"), rs.getString("name"), rs.getString("name_th"),
                rs.getString("location"), rs.getString("location_th"), rs.getInt("capacity"),
                rs.getBoolean("approval"), rs.getBoolean("active"), rs.getString("amenities"),
                rs.getString("color"), rs.getString("image_url"), rs.getBoolean("deleted"));
    }

    /**
     * Inserts a new room record into the database.
     * เพิ่มข้อมูลห้องประชุมใหม่ลงในฐานข้อมูล
     *
     * @param room - The Room object to insert / ออบเจ็กต์ห้องประชุมที่ต้องการเพิ่ม
     */
    protected void insertRoom(Room room) {
        jdbc.update("""
                INSERT INTO room (id, name, name_th, location, location_th, capacity, approval,
                                  active, amenities, color, image_url)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, room.id, room.name, room.nameTh, room.location, room.locationTh, room.capacity,
                room.approval, room.active, room.amenities, room.color, room.image);
    }

    /**
     * Updates an existing room record in the database.
     * อัปเดตข้อมูลห้องประชุมที่มีอยู่ในฐานข้อมูล
     *
     * @param room - The Room object containing updated details / ออบเจ็กต์ห้องประชุมที่มีข้อมูลอัปเดตแล้ว
     */
    protected void updateRoom(Room room) {
        jdbc.update("""
                UPDATE room SET name = ?, name_th = ?, location = ?, location_th = ?, capacity = ?,
                                approval = ?, active = ?, amenities = ?, color = ?, image_url = ?
                WHERE id = ?
                """, room.name, room.nameTh, room.location, room.locationTh, room.capacity,
                room.approval, room.active, room.amenities, room.color, room.image, room.id);
    }

    /**
     * Retrieves a list of all bookings, ordered by creation time descending.
     * ดึงข้อมูลการจองทั้งหมดในระบบ โดยเรียงลำดับจากเวลาที่สร้างล่าสุดไปเก่าสุด
     *
     * @return A list of Booking objects / ลิสต์ของออบเจ็กต์การจองทั้งหมด
     */
    protected List<Booking> allBookings() {
        return jdbc.query("SELECT * FROM booking ORDER BY created_at DESC", this::mapBooking);
    }

    /**
     * Finds a specific booking by its ID.
     * ค้นหาข้อมูลการจองจากรหัสการจอง
     *
     * @param id - The booking ID / รหัสการจอง
     * @return The Booking object if found, or null / ออบเจ็กต์การจองหากพบข้อมูล หรือ null หากไม่พบ
     */
    protected Booking findBooking(String id) {
        return first(jdbc.query("SELECT * FROM booking WHERE id = ?", this::mapBooking, id));
    }

    /**
     * Maps a row from a database ResultSet to a Booking object.
     * แปลงข้อมูลหนึ่งแถวจาก ResultSet ของฐานข้อมูลให้เป็นออบเจ็กต์ Booking
     *
     * @param rs - The ResultSet / ชุดข้อมูลผลลัพธ์จากฐานข้อมูล
     * @param row - The current row number / หมายเลขแถวปัจจุบัน
     * @return A mapped Booking object / ออบเจ็กต์ Booking ที่ถูกสร้างขึ้น
     * @throws SQLException - If a database access error occurs / หากเกิดข้อผิดพลาดในการเข้าถึงฐานข้อมูล
     */
    private Booking mapBooking(ResultSet rs, int row) throws SQLException {
        return new Booking(rs.getString("id"), rs.getString("room_id"), rs.getString("user_id"),
                rs.getString("title"), rs.getString("booking_date"), rs.getString("start_time"),
                rs.getString("end_time"), rs.getInt("participants"), rs.getString("status"),
                rs.getString("series_id"), rs.getString("request_key"), rs.getString("created_at"),
                rs.getString("updated_at"), rs.getString("reason"));
    }

    /**
     * Inserts a new booking record into the database.
     * บันทึกข้อมูลการจองใหม่ลงในฐานข้อมูล
     *
     * @param booking - The Booking object to insert / ออบเจ็กต์การจองที่ต้องการบันทึก
     */
    protected void insertBooking(Booking booking) {
        jdbc.update("""
                INSERT INTO booking (id, room_id, user_id, title, booking_date, start_time, end_time,
                                     participants, status, series_id, request_key, created_at, updated_at, reason)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, booking.id, booking.rid, booking.uid, booking.title, booking.date, booking.start,
                booking.end, booking.participants, booking.status, booking.series, booking.request,
                booking.created, booking.updated, booking.reason);
    }

    /**
     * Retrieves all system events/notifications associated with a specific user.
     * ดึงข้อมูลเหตุการณ์/การแจ้งเตือนทั้งหมดที่เกี่ยวข้องกับผู้ใช้ที่ระบุ
     *
     * @param userId - The user ID / รหัสผู้ใช้
     * @return A list of Event objects / ลิสต์ของออบเจ็กต์เหตุการณ์ทั้งหมด
     */
    protected List<Event> eventsFor(String userId) {
        return jdbc.query("SELECT * FROM booking_event WHERE user_id = ? ORDER BY event_at DESC",
                this::mapEvent, userId);
    }

    /**
     * Maps a row from a database ResultSet to an Event object.
     * แปลงข้อมูลหนึ่งแถวจาก ResultSet ของฐานข้อมูลให้เป็นออบเจ็กต์ Event
     *
     * @param rs - The ResultSet / ชุดข้อมูลผลลัพธ์จากฐานข้อมูล
     * @param row - The current row number / หมายเลขแถวปัจจุบัน
     * @return A mapped Event object / ออบเจ็กต์ Event ที่ถูกสร้างขึ้น
     * @throws SQLException - If a database access error occurs / หากเกิดข้อผิดพลาดในการเข้าถึงฐานข้อมูล
     */
    private Event mapEvent(ResultSet rs, int row) throws SQLException {
        return new Event(rs.getString("id"), rs.getString("user_id"), rs.getString("booking_id"),
                rs.getString("kind"), rs.getString("event_at"), rs.getString("actor"));
    }

    /**
     * Inserts a new booking event record into the database.
     * เพิ่มข้อมูลเหตุการณ์ที่เกี่ยวกับการจองใหม่ลงในฐานข้อมูล
     *
     * @param event - The Event object to insert / ออบเจ็กต์เหตุการณ์ที่ต้องการบันทึก
     */
    protected void insertEvent(Event event) {
        jdbc.update("""
                INSERT INTO booking_event (id, user_id, booking_id, kind, event_at, actor)
                VALUES (?, ?, ?, ?, ?, ?)
                """, event.id, event.uid, event.bid, event.kind, event.at, event.actor);
    }

    /**
     * Returns the first element of a list, or null if the list is empty.
     * คืนค่าองค์ประกอบตัวแรกของลิสต์ หรือคืนค่า null หากลิสต์นั้นว่างเปล่า
     *
     * @param values - The list to extract from / ลิสต์ข้อมูลที่ต้องการดึงค่า
     * @param <T> - The type of elements in the list / ชนิดของข้อมูลในลิสต์
     * @return The first element, or null / ค่าข้อมูลตัวแรก หรือ null
     */
    private static <T> T first(List<T> values) {
        return values.isEmpty() ? null : values.get(0);
    }

    /**
     * Safely extracts a string value from a map given a specific key.
     * ดึงค่าสตริงจาก Map อย่างปลอดภัย โดยไม่ทำให้เกิด Error หากค่าที่ได้เป็น null
     *
     * @param body - The map containing data / Map ที่บรรจุข้อมูล
     * @param key - The key to look up / คีย์ที่ต้องการดึงข้อมูล
     * @return The string representation of the value, or an empty string if null / ค่าข้อมูลในรูปแบบสตริง หรือสตริงว่างเปล่าหากค่าเป็น null
     */
    protected static String str(Map<String, Object> body, String key) {
        Object value = body.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    /**
     * Safely converts an object to an integer, using a fallback value if parsing fails.
     * แปลงค่าออบเจ็กต์เป็นตัวเลขจำนวนเต็ม (Integer) อย่างปลอดภัย หากแปลงไม่ได้จะใช้ค่าเริ่มต้นที่กำหนดไว้แทน
     *
     * @param value - The object to convert / ออบเจ็กต์ที่ต้องการแปลง
     * @param fallback - The fallback integer value / ค่าเริ่มต้นในกรณีที่การแปลงล้มเหลว
     * @return The parsed integer or the fallback value / ค่าตัวเลขจำนวนเต็มที่แปลงได้ หรือค่าเริ่มต้น
     */
    protected static int intValue(Object value, int fallback) {
        try {
            return value instanceof Number number ? number.intValue() : Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    /**
     * Evaluates an object and converts it to a boolean value. Supports 'true', 'on', and '1'.
     * ประเมินและแปลงค่าออบเจ็กต์ให้เป็นค่าความจริง (Boolean) รองรับค่า 'true', 'on' และ '1'
     *
     * @param value - The object to evaluate / ออบเจ็กต์ที่ต้องการประเมิน
     * @return True if the value represents truth, false otherwise / คืนค่า True หากค่าตรงกับเงื่อนไขความจริง หรือ False หากไม่ใช่
     */
    protected static boolean bool(Object value) {
        return value instanceof Boolean bool
                ? bool
                : value != null && Set.of("true", "on", "1").contains(String.valueOf(value).toLowerCase(Locale.ROOT));
    }

    /**
     * Constructs a ResponseEntity containing a standard error code.
     * สร้างออบเจ็กต์ ResponseEntity เพื่อใช้ตอบกลับข้อผิดพลาดมาตรฐานไปยังไคลเอนต์
     *
     * @param status - The HTTP status to return / รหัสสถานะ HTTP ที่ต้องการตอบกลับ
     * @param code - The internal error code string / สตริงรหัสข้อผิดพลาดภายในระบบ
     * @return A ResponseEntity with the error map / ออบเจ็กต์ ResponseEntity ที่บรรจุ Map ของข้อผิดพลาด
     */
    protected static ResponseEntity> error(HttpStatus status, String code) {
        return ResponseEntity.status(status).body(Map.of("error", code));
    }

    static class User {
        final String id;
        final String email;
        final String name;
        final String hash;
        final String role;
        final boolean selfRegistered;

        User(String id, String email, String name, String hash, String role, boolean selfRegistered) {
            this.id = id;
            this.email = email;
            this.name = name;
            this.hash = hash;
            this.role = role;
            this.selfRegistered = selfRegistered;
        }

        /**
         * Converts the user details to a map intended for public/frontend consumption, excluding sensitive data.
         * แปลงรายละเอียดของผู้ใช้เป็น Map สำหรับส่งให้ฝั่ง Frontend หรือสาธารณะ โดยจะตัดข้อมูลที่ละเอียดอ่อนออก
         *
         * @return A map of public user properties / Map ของคุณสมบัติผู้ใช้ที่เปิดเผยได้
         */
        Map<String, Object> publicMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", id);
            map.put("email", email);
            map.put("name", name);
            map.put("role", role);
            map.put("self_registered", selfRegistered ? 1 : 0);
            return map;
        }
    }

    record Room(String id, String name, String nameTh, String location, String locationTh,
                int capacity, boolean approval, boolean active, String amenities, String color,
                String image, boolean deleted) {
        Room(String id, String name, String nameTh, String location, String locationTh,
             int capacity, boolean approval, boolean active, String amenities, String color,
             String image) {
            this(id, name, nameTh, location, locationTh, capacity, approval, active, amenities,
                    color, image, false);
        }

        /**
         * Converts the room record into a key-value map.
         * แปลงข้อมูลห้องประชุมเป็นรูปแบบ Key-Value Map
         *
         * @return A map of the room properties / Map ของคุณสมบัติห้องประชุม
         */
        Map<String, Object> map() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", id);
            map.put("name", name);
            map.put("name_th", nameTh);
            map.put("location", location);
            map.put("location_th", locationTh);
            map.put("capacity", capacity);
            map.put("approval", approval ? 1 : 0);
            map.put("active", active ? 1 : 0);
            map.put("amenities", amenities);
            map.put("color", color);
            map.put("image", image);
            map.put("deleted", deleted ? 1 : 0);
            return map;
        }
    }

    static class Booking {
        final String id;
        final String rid;
        final String uid;
        final String title;
        final String date;
        final String start;
        final String end;
        final int participants;
        final String status;
        final String series;
        final String request;
        final String created;
        final String updated;
        final String reason;

        Booking(String id, String rid, String uid, String title, String date, String start, String end,
                int participants, String status, String series, String request, String created,
                String updated, String reason) {
            this.id = id;
            this.rid = rid;
            this.uid = uid;
            this.title = title;
            this.date = date;
            this.start = start;
            this.end = end;
            this.participants = participants;
            this.status = status;
            this.series = series;
            this.request = request;
            this.created = created;
            this.updated = updated;
            this.reason = reason;
        }

        /**
         * Converts the booking instance into a comprehensive key-value map.
         * แปลงข้อมูลการจองทั้งหมดให้เป็นรูปแบบ Key-Value Map ที่สมบูรณ์
         *
         * @return A map containing all booking properties / Map ที่บรรจุข้อมูลการจองทั้งหมด
         */
        Map<String, Object> map() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", id);
            map.put("rid", rid);
            map.put("uid", uid);
            map.put("title", title);
            map.put("date", date);
            map.put("start", start);
            map.put("end", end);
            map.put("participants", participants);
            map.put("status", status);
            map.put("series", series);
            map.put("request", request);
            map.put("created", created);
            map.put("updated", updated);
            map.put("reason", reason);
            return map;
        }

        /**
         * Converts the booking into a restricted map that only shows availability info (hiding personal data).
         * แปลงข้อมูลการจองเป็น Map แบบจำกัดสิทธิ์ ซึ่งจะแสดงเฉพาะข้อมูลสถานะว่าง/ไม่ว่างของห้อง (ซ่อนข้อมูลส่วนตัวผู้จอง)
         *
         * @return A map containing limited booking availability / Map ที่แสดงเฉพาะข้อมูลช่วงเวลาที่มีการจองเท่านั้น
         */
        Map<String, Object> availabilityMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("rid", rid);
            map.put("date", date);
            map.put("start", start);
            map.put("end", end);
            map.put("status", status);
            return map;
        }
    }

    record Event(String id, String uid, String bid, String kind, String at, String actor) {
        
        /**
         * Converts the event record into a key-value map.
         * แปลงข้อมูลเหตุการณ์/การแจ้งเตือนเป็นรูปแบบ Key-Value Map
         *
         * @return A map of the event properties / Map ของคุณสมบัติเหตุการณ์
         */
        Map<String, Object> map() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", id);
            map.put("uid", uid);
            map.put("bid", bid);
            map.put("kind", kind);
            map.put("at", at);
            map.put("actor", actor);
            return map;
        }
    }
}