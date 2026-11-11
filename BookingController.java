package com.meetspace.app;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ตัวควบคุมการจองห้องประชุม
 *
 * รับผิดชอบ 3 เรื่องหลัก:
 *   1. จองห้อง (ผู้ใช้ทั่วไป และแบบ walk-in หน้าเคาน์เตอร์)
 *   2. ตรวจว่าห้องว่างหรือไม่ (ห้ามจองเวลาซ้อนกัน)
 *   3. จัดการการจองที่มีอยู่: แอดมินอนุมัติ/ปฏิเสธ และผู้จองหรือแอดมินยกเลิก
 *
 * หมายเหตุ: error ที่ส่งกลับ (เช่น "signIn", "conflict") เป็นรหัสที่หน้าเว็บใช้แปลงเป็นข้อความ
 * ห้ามแก้ชื่อรหัสพวกนี้ ไม่อย่างนั้นหน้าเว็บจะแสดงข้อความผิด
 */
@RestController
@RequestMapping("/api")
public class BookingController extends MeetSpaceSupport {

    // ===== กฎการจอง =====
    /** เวลาเปิดให้จองเร็วที่สุด */
    private static final String BOOKING_OPEN_TIME = "08:00";
    /** เวลาปิดให้จอง (การจองต้องจบไม่เกินเวลานี้) */
    private static final String BOOKING_CLOSE_TIME = "20:00";
    /** จองได้นานที่สุดครั้งละ 240 นาที (4 ชั่วโมง) */
    private static final int BOOKING_MAX_MINUTES = 240;
    /** จองซ้ำรายสัปดาห์ได้สูงสุด 12 สัปดาห์ */
    private static final int BOOKING_MAX_REPEAT = 12;

    // ===== ค่าที่ระบบยังไม่เปิดให้ผู้ใช้กรอกเอง (ล็อกค่าไว้ก่อน) =====
    // เก็บเป็นค่าคงที่ไว้ เผื่อในอนาคตเปิดให้ผู้ใช้กรอกเอง จะได้ใช้การตรวจสอบด้านล่างต่อได้เลย
    /** ชื่อการจองเริ่มต้น */
    private static final String DEFAULT_TITLE = "Room reservation";
    /** จำนวนผู้เข้าร่วม (ตอนนี้ล็อกไว้ที่ 1 คน) */
    private static final int DEFAULT_PARTICIPANTS = 1;
    /** จำนวนสัปดาห์ที่จองซ้ำ (ตอนนี้ล็อกไว้ที่ 1 = ไม่จองซ้ำ) */
    private static final int DEFAULT_REPEAT = 1;
    /** ถ้าบางสัปดาห์ชน จะข้ามสัปดาห์นั้นแล้วจองที่เหลือไหม (ตอนนี้ปิดไว้ = ถ้าชนแม้วันเดียวจะไม่จองเลย) */
    private static final boolean DEFAULT_SKIP_CONFLICTS = false;

    public BookingController(JdbcTemplate jdbc) {
        super(jdbc);
    }

    // =====================================================================
    //  จองห้อง
    // =====================================================================

    /**
     * จองห้องประชุม
     *
     * ใช้ได้ 2 แบบ:
     *   - /api/book         : ผู้ใช้ที่ล็อกอินแล้วจองเอง (ชื่อผู้จอง = ชื่อผู้ใช้)
     *   - /api/walk-in/book : จองแบบ walk-in (ใช้บัญชีกลาง "walk-in" และกรอกชื่อผู้จองเอง)
     *
     * ข้อมูลที่รับ (JSON): rid = รหัสห้อง, date = วันที่ (yyyy-MM-dd),
     *                    start / end = เวลาเริ่ม/จบ (HH:mm ทีละ 30 นาที),
     *                    booker = ชื่อผู้จอง (เฉพาะ walk-in), key = รหัสกันกดจองซ้ำ (ไม่บังคับ)
     *
     * มี synchronized + ล็อกแถวห้อง (FOR UPDATE) เพื่อกันคนสองคนจองช่วงเวลาเดียวกันพร้อมกัน
     */
    @PostMapping({"/book", "/walk-in/book"})
    @Transactional
    public synchronized ResponseEntity<?> book(@RequestBody Map<String, Object> body,
                                                HttpServletRequest request) {
        // 1) หาว่าใครเป็นคนจอง
        boolean walkIn = request.getRequestURI().endsWith("/walk-in/book");
        User user = walkIn ? findUserById("walk-in") : current(request);
        if (user == null) return error(HttpStatus.UNAUTHORIZED, "signIn");

        // 2) ล็อกแถวห้องในฐานข้อมูล แล้วตรวจว่าห้องมีอยู่และเปิดให้จอง
        String roomId = str(body, "rid");
        jdbc.queryForList("SELECT id FROM room WHERE id = ? FOR UPDATE", roomId);
        Room room = findRoom(roomId);
        if (room == null || room.deleted()) return error(HttpStatus.NOT_FOUND, "notFound");
        if (!room.active()) return error(HttpStatus.CONFLICT, "unavailable");

        // 3) อ่านข้อมูลที่ส่งมา
        String title = DEFAULT_TITLE;
        String bookerName = walkIn ? str(body, "booker").trim() : user.name;
        if (bookerName.isBlank() || bookerName.length() > 100) return error(HttpStatus.BAD_REQUEST, "invalidBooker");
        String date = str(body, "date");
        String start = str(body, "start");
        String end = str(body, "end");
        int participants = DEFAULT_PARTICIPANTS;
        int repeat = DEFAULT_REPEAT;
        boolean skipConflicts = DEFAULT_SKIP_CONFLICTS;
        String requestKey = str(body, "key").trim();

        // 4) ตรวจข้อมูลทั่วไป: ชื่อการจอง, จำนวนคน, ความจุห้อง, จำนวนสัปดาห์
        if (title.isBlank() || title.length() > 160) return error(HttpStatus.BAD_REQUEST, "invalidTitle");
        if (participants < 1) return error(HttpStatus.BAD_REQUEST, "invalidParticipants");
        if (participants > room.capacity()) {
            return ResponseEntity.badRequest().body(Map.of("error", "capacity", "capacity", room.capacity()));
        }
        if (repeat < 1 || repeat > BOOKING_MAX_REPEAT) return error(HttpStatus.BAD_REQUEST, "invalidRepeat");

        // 5) ตรวจเวลา: รูปแบบถูก, อยู่ในเวลาทำการ, ไม่นานเกินกำหนด
        if (!isValidTimeRange(start, end)) return error(HttpStatus.BAD_REQUEST, "invalidTime");

        // 6) กันกดจองซ้ำ: ถ้า key นี้เคยใช้จองไปแล้ว ไม่ต้องจองอีก
        if (requestKey.length() > 80) return error(HttpStatus.BAD_REQUEST, "invalidInput");
        if (requestKey.isBlank()) requestKey = UUID.randomUUID().toString();
        Integer duplicate = jdbc.queryForObject(
                "SELECT COUNT(*) FROM booking WHERE user_id = ? AND request_key = ?",
                Integer.class, user.id, requestKey);
        if (duplicate != null && duplicate > 0) return error(HttpStatus.CONFLICT, "duplicate");

        // 7) ตรวจวันที่: รูปแบบถูก, ไม่เกิน 1 ปีล่วงหน้า, ไม่ใช่เวลาที่ผ่านไปแล้ว
        LocalDate firstDate;
        try {
            firstDate = LocalDate.parse(date);
        } catch (DateTimeParseException ex) {
            return error(HttpStatus.BAD_REQUEST, "invalidDate");
        }
        ZonedDateTime now = ZonedDateTime.now(BANGKOK);
        LocalDate maxDate = now.toLocalDate().plusYears(1);
        if (firstDate.plusWeeks(repeat - 1L).isAfter(maxDate)) return error(HttpStatus.BAD_REQUEST, "tooFar");
        String currentTime = now.toLocalTime().withSecond(0).withNano(0).toString().substring(0, 5);
        if (firstDate.isBefore(now.toLocalDate())
                || (firstDate.equals(now.toLocalDate()) && start.compareTo(currentTime) <= 0)) {
            return error(HttpStatus.BAD_REQUEST, "past");
        }

        // 8) แยกว่าแต่ละสัปดาห์ห้องว่างหรือชนกับการจองอื่น
        List<LocalDate> freeDates = new ArrayList<>();
        List<String> skippedDates = new ArrayList<>();
        splitFreeAndConflictDates(roomId, firstDate, repeat, start, end, freeDates, skippedDates);
        if ((!skippedDates.isEmpty() && !skipConflicts) || freeDates.isEmpty()) {
            return error(HttpStatus.CONFLICT, "conflict");
        }

        // 9) บันทึกการจองลงฐานข้อมูล แล้วส่งผลกลับ
        List<Map<String, Object>> createdBookings = saveBookings(freeDates, roomId, user, title,
                start, end, participants, repeat, requestKey, bookerName);
        return ResponseEntity.ok(Map.of("bookings", createdBookings, "skipped", skippedDates));
    }

    // =====================================================================
    //  จัดการการจองที่มีอยู่ (อนุมัติ / ปฏิเสธ / ยกเลิก)
    // =====================================================================

    /**
     * จัดการการจองที่มีอยู่แล้ว
     *
     * ข้อมูลที่รับ (JSON): id = รหัสการจอง, action = "approve" | "reject" | "cancel",
     *                    reason = เหตุผล (บังคับกรอกตอน reject),
     *                    series = true ถ้าต้องการยกเลิกทั้งชุดที่จองซ้ำรายสัปดาห์
     */
    @PostMapping("/reservation")
    @Transactional
    public ResponseEntity<?> reservation(@RequestBody Map<String, Object> body,
                                         HttpServletRequest request) {
        // ต้องล็อกอินก่อน และการจองต้องมีอยู่จริง
        User user = current(request);
        if (user == null) return error(HttpStatus.UNAUTHORIZED, "signIn");
        String id = str(body, "id");
        String action = str(body, "action");
        Booking booking = findBooking(id);
        if (booking == null) return error(HttpStatus.NOT_FOUND, "notFound");

        if ("approve".equals(action) || "reject".equals(action)) {
            return approveOrReject(body, user, booking, action);
        }
        if ("cancel".equals(action)) {
            return cancel(body, user, booking);
        }
        return error(HttpStatus.BAD_REQUEST, "invalidInput");
    }

    /** แอดมินอนุมัติหรือปฏิเสธการจองที่ยังรอพิจารณา (pending) */
    private ResponseEntity<?> approveOrReject(Map<String, Object> body, User user,
                                              Booking booking, String action) {
        // เฉพาะแอดมิน และการจองต้องยังเป็น pending อยู่
        if (!"admin".equals(user.role)) return error(HttpStatus.FORBIDDEN, "forbidden");
        if (!"pending".equals(booking.status)) return error(HttpStatus.CONFLICT, "changed");

        // ถ้าปฏิเสธ ต้องกรอกเหตุผล (ไม่เกิน 300 ตัวอักษร)
        boolean isReject = "reject".equals(action);
        String reason = isReject ? str(body, "reason").trim() : "";
        if (reason.length() > 300 || (isReject && reason.isBlank())) {
            return error(HttpStatus.BAD_REQUEST, "invalidInput");
        }

        // อัปเดตสถานะ (เงื่อนไข status = 'pending' กันกรณีแอดมินอีกคนเปลี่ยนไปก่อนแล้ว)
        String newStatus = isReject ? "rejected" : "confirmed";
        String updatedAt = Instant.now().toString();
        int changed = jdbc.update("""
                UPDATE booking SET status = ?, reason = ?, updated_at = ?
                WHERE id = ? AND status = 'pending'
                """, newStatus, reason, updatedAt, booking.id);
        if (changed == 0) return error(HttpStatus.CONFLICT, "changed");

        // บันทึกประวัติ (event) ว่าใครเปลี่ยนสถานะ
        insertEvent(new Event(UUID.randomUUID().toString(), booking.uid, booking.id,
                newStatus, updatedAt, user.name));
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /** ยกเลิกการจอง (เจ้าของการจองหรือแอดมินเท่านั้น) */
    private ResponseEntity<?> cancel(Map<String, Object> body, User user, Booking booking) {
        if (!booking.uid.equals(user.id) && !"admin".equals(user.role)) {
            return error(HttpStatus.FORBIDDEN, "forbidden");
        }
        // FR-09: ยกเลิกได้เฉพาะการจองที่ยังใช้งานอยู่ และยังไม่ถึงเวลาจบ
        if (!isActive(booking.status)) return error(HttpStatus.CONFLICT, "changed");
        if (!isFuture(booking)) return error(HttpStatus.CONFLICT, "alreadyEnded");

        // หาการจองที่จะยกเลิก: ตัวที่เลือก + (ถ้า series = true) การจองในชุดเดียวกันที่ยังไม่ถึงเวลา
        boolean wholeSeries = bool(body.get("series"));
        List<Booking> targets = allBookings().stream()
                .filter(item -> item.id.equals(booking.id)
                        || (wholeSeries && booking.series != null
                            && booking.series.equals(item.series) && isFuture(item)))
                .toList();

        for (Booking target : targets) {
            if (isActive(target.status)) {
                String updatedAt = Instant.now().toString();
                jdbc.update("UPDATE booking SET status = 'cancelled', updated_at = ? WHERE id = ?",
                        updatedAt, target.id);
                insertEvent(new Event(UUID.randomUUID().toString(), target.uid, target.id,
                        "cancelled", updatedAt, user.name));
            }
        }
        return ResponseEntity.ok(Map.of("ok", true));
    }

    // =====================================================================
    //  เมธอดช่วย (ใช้ภายในคลาสนี้เท่านั้น)
    // =====================================================================

    /**
     * ตรวจช่วงเวลาที่จอง:
     *   - start และ end ต้องเป็นรูปแบบ HH:mm ทีละ 30 นาที และ start ต้องมาก่อน end
     *   - ต้องอยู่ในช่วง 08:00–20:00
     *   - ต้องไม่นานเกิน 4 ชั่วโมง
     */
    private static boolean isValidTimeRange(String start, String end) {
        if (!validSlot(start) || !validSlot(end) || start.compareTo(end) >= 0) return false;
        if (start.compareTo(BOOKING_OPEN_TIME) < 0 || end.compareTo(BOOKING_CLOSE_TIME) > 0) return false;
        long minutes = Duration.between(LocalTime.parse(start), LocalTime.parse(end)).toMinutes();
        return minutes <= BOOKING_MAX_MINUTES;
    }

    /**
     * ไล่ดูทีละสัปดาห์ (เริ่มจาก firstDate จำนวน repeat สัปดาห์)
     * วันที่ห้องว่างจะใส่ใน freeDates ส่วนวันที่ชนกับการจองอื่นจะใส่ใน skippedDates
     */
    private void splitFreeAndConflictDates(String roomId, LocalDate firstDate, int repeat,
                                           String start, String end,
                                           List<LocalDate> freeDates, List<String> skippedDates) {
        for (int week = 0; week < repeat; week++) {
            LocalDate date = firstDate.plusWeeks(week);
            if (hasConflict(roomId, date.toString(), start, end)) {
                skippedDates.add(date.toString());
            } else {
                freeDates.add(date);
            }
        }
    }

    /**
     * บันทึกการจองทุกวันใน freeDates ลงฐานข้อมูล พร้อมบันทึกประวัติ (event)
     * ถ้าจองซ้ำหลายสัปดาห์ ทุกการจองจะได้รหัสชุด (series) เดียวกัน เพื่อให้ยกเลิกทั้งชุดได้
     */
    private List<Map<String, Object>> saveBookings(List<LocalDate> freeDates, String roomId, User user,
                                                   String title, String start, String end,
                                                   int participants, int repeat,
                                                   String requestKey, String bookerName) {
        String series = repeat > 1 ? UUID.randomUUID().toString() : null;
        List<Map<String, Object>> createdBookings = new ArrayList<>();
        for (LocalDate date : freeDates) {
            String id = UUID.randomUUID().toString();
            String nowIso = Instant.now().toString();
            String status = "confirmed";
            Booking booking = new Booking(id, roomId, user.id, title, date.toString(), start, end,
                    participants, status, series, requestKey, nowIso, nowIso, "");
            insertBooking(booking);
            jdbc.update("UPDATE booking SET booker = ? WHERE id = ?", bookerName, id);
            insertEvent(new Event(UUID.randomUUID().toString(), user.id, id,
                    "confirmed", nowIso, user.name));
            createdBookings.add(booking.map());
        }
        return createdBookings;
    }

    /**
     * ตรวจว่าห้องนี้ วันนี้ มีการจอง (pending หรือ confirmed) ที่เวลาซ้อนกับช่วง start–end หรือไม่
     * เงื่อนไขซ้อนกัน: การจองเดิมเริ่มก่อนเราจบ และจบหลังเราเริ่ม
     */
    private boolean hasConflict(String roomId, String date, String start, String end) {
        Integer count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM booking
                WHERE room_id = ? AND booking_date = ?
                  AND status IN ('pending', 'confirmed')
                  AND start_time < ? AND end_time > ?
                """, Integer.class, roomId, date, end, start);
        return count != null && count > 0;
    }

    /** ตรวจรูปแบบเวลา HH:mm (00:00–23:59) และต้องลงที่นาทีที่ 00 หรือ 30 เท่านั้น */
    private static boolean validSlot(String value) {
        if (value == null || !value.matches("(?:[01]\\d|2[0-3]):[0-5]\\d")) return false;
        return Integer.parseInt(value.substring(3)) % 30 == 0;
    }

}
