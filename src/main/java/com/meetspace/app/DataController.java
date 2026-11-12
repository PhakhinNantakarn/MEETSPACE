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
 * Controller for managing public room data, privacy-aware booking records, and user notifications.
 * คอนโทรลเลอร์สำหรับจัดการข้อมูลห้องประชุมสาธารณะ ข้อมูลการจองที่รักษาสิทธิส่วนบุคคล และการแจ้งเตือนของผู้ใช้
 */
@RestController
@RequestMapping("/api")
public class DataController extends MeetSpaceSupport {

    /**
     * Constructs the DataController with the required JdbcTemplate for database operations.
     * สร้าง DataController โดยรับค่า JdbcTemplate เพื่อใช้ในการจัดการฐานข้อมูล
     *
     * @param jdbc - The JdbcTemplate instance for database access / อินสแตนซ์ JdbcTemplate สำหรับการเข้าถึงฐานข้อมูล
     */
    public DataController(JdbcTemplate jdbc) {
        super(jdbc);
    }

    /**
     * Retrieves the main dashboard data including rooms, bookings, people, and notifications. 
     * The returned data is filtered and sanitized based on the current user's role (admin, member, or walk-in).
     * 
     * ดึงข้อมูลหลักสำหรับหน้าแดชบอร์ด ซึ่งประกอบด้วยข้อมูลห้องประชุม การจอง ข้อมูลบุคคล และการแจ้งเตือน 
     * โดยข้อมูลที่ส่งกลับจะถูกกรองและจำกัดสิทธิ์การมองเห็นตามบทบาทของผู้ใช้งานปัจจุบัน (แอดมิน, สมาชิก, หรือ Walk-in)
     *
     * @param request - The HTTP request used to identify the route and current user session / คำขอ HTTP ที่ใช้ระบุเส้นทางและเซสชันของผู้ใช้ปัจจุบัน
     * @return ResponseEntity containing a map of all required frontend data / ResponseEntity ที่บรรจุ Map ของข้อมูลทั้งหมดที่ต้องใช้ในหน้าบ้าน (Frontend)
     */
    @GetMapping({"/data", "/walk-in/data"})
    public ResponseEntity<?> data(HttpServletRequest request) {
        User user = request.getRequestURI().endsWith("/walk-in/data") ? null : current(request);
        List> roomList = allRooms().stream().map(Room::map).toList();
        List<Booking> storedBookings = allBookings();

        List> bookingList;
        if (user == null) {
            bookingList = storedBookings.stream()
                    .filter(b -> isActive(b.status))
                    .map(Booking::availabilityMap)
                    .toList();
        } else if ("admin".equals(user.role)) {
            bookingList = storedBookings.stream().map(this::bookingDetails).toList();
        } else {
            // Members receive their own full records plus anonymous occupied slots for everyone else.
            bookingList = storedBookings.stream()
                    .filter(b -> b.uid.equals(user.id) || isActive(b.status))
                    .map(b -> b.uid.equals(user.id) ? b.map() : b.availabilityMap())
                    .toList();
        }

        List> people;
        if (user == null) {
            people = List.of();
        } else if ("admin".equals(user.role)) {
            people = allUsers().stream().map(User::publicMap).toList();
        } else {
            people = List.of(user.publicMap());
        }

        List> notes = user == null
                ? List.of()
                : eventsFor(user.id).stream().map(Event::map).toList();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("user", user == null ? null : user.publicMap());
        out.put("rooms", roomList);
        out.put("bookings", bookingList);
        out.put("people", people);
        out.put("notifications", notes);
        out.put("time", ZonedDateTime.now(BANGKOK).toString());
        return ResponseEntity.ok(out);
    }

    /**
     * Enriches a booking object with additional administrative details, such as the exact booker's name 
     * and the source of the booking (Member or Walk-in).
     * 
     * เพิ่มข้อมูลรายละเอียดเพิ่มเติมสำหรับการจองในมุมมองแอดมิน เช่น ระบุชื่อผู้จองที่แน่ชัด 
     * และแหล่งที่มาของการจอง (สมาชิกระบบ หรือ Walk-in)
     *
     * @param booking - The original booking object to extract details from / ออบเจ็กต์การจองเริ่มต้นที่ต้องการดึงรายละเอียด
     * @return A map containing the enriched booking details / Map ที่บรรจุข้อมูลรายละเอียดการจองที่ถูกเพิ่มข้อมูลส่วนขยายแล้ว
     */
    private Map<String, Object> bookingDetails(Booking booking) {
        Map<String, Object> result = booking.map();
        String identity = jdbc.queryForObject("SELECT booker FROM booking WHERE id = ?", String.class, booking.id);
        User owner = findUserById(booking.uid);
        result.put("booker", identity == null || identity.isBlank()
                ? (owner == null ? "Walk-in" : owner.name) : identity);
        result.put("source", booking.uid.equals("walk-in") ? "Walk-in" : "Member");
        return result;
    }

}