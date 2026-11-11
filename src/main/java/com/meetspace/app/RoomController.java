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

/** Administration of meeting rooms and initial room records. */
@RestController
@RequestMapping("/api")
public class RoomController extends MeetSpaceSupport {
    public RoomController(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @PostConstruct
    void seedRooms() {
        List<Room> standardRooms = List.of(
                new Room("MS-101", "Inspire Room", "ห้องอินสไปร์", "Floor 1 · North Wing",
                        "ชั้น 1 · ฝั่งเหนือ", 6, false, true, "display,wifi,whiteboard", "indigo",
                        "/images/inspire-room.png"),
                new Room("MS-204", "Collaboration Studio", "สตูดิโอทำงานร่วมกัน", "Floor 2 · Central Hub",
                        "ชั้น 2 · ศูนย์กลาง", 12, true, true, "display,wifi,video", "teal",
                        "/images/collaboration-studio.png"),
                new Room("MS-307", "Innovation Lab", "ห้องนวัตกรรม", "Floor 3 · East Wing",
                        "ชั้น 3 · ฝั่งตะวันออก", 20, false, true, "display,wifi,whiteboard", "blue",
                        "/images/board-room.jpg"),
                new Room("MS-410", "Skyline Boardroom", "ห้องประชุมสกายไลน์", "Floor 4 · West Wing",
                        "ชั้น 4 · ฝั่งตะวันตก", 16, false, true, "display,wifi,video", "violet",
                        "/images/board-room.jpg"));

        String[] legacyIds = {"A101", "B201", "C301", "D102"};
        for (int i = 0; i < standardRooms.size(); i++) {
            migrateLegacyRoom(legacyIds[i], standardRooms.get(i));
            seedRoom(standardRooms.get(i));
        }
        jdbc.update("UPDATE room SET approval = FALSE");
    }
    private void migrateLegacyRoom(String legacyId, Room replacement) {
        Integer legacyCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM room WHERE id = ?", Integer.class, legacyId);
        if (legacyCount == null || legacyCount == 0) return;

        Integer replacementCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM room WHERE id = ?", Integer.class, replacement.id());
        if (replacementCount == null || replacementCount == 0) insertRoom(replacement);

        jdbc.update("UPDATE booking SET room_id = ? WHERE room_id = ?", replacement.id(), legacyId);
        jdbc.update("DELETE FROM room WHERE id = ?", legacyId);
    }

    private void seedRoom(Room room) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM room WHERE id = ?", Integer.class, room.id());
        if (count != null && count == 0) insertRoom(room);
    }
    @PostMapping("/room")
    @Transactional
    public ResponseEntity<?> room(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User admin = current(request);
        if (admin == null) return error(HttpStatus.UNAUTHORIZED, "signIn");
        if (!"admin".equals(admin.role)) return error(HttpStatus.FORBIDDEN, "forbidden");

        String id = str(body, "id").trim();
        if (id.isBlank()) id = nextRoomId();
        if (!id.matches("[A-Za-z0-9_-]{1,20}")) return error(HttpStatus.BAD_REQUEST, "invalidInput");

        String name = str(body, "name").trim();
        String nameTh = str(body, "name_th").trim();
        String location = str(body, "location").trim();
        String locationTh = str(body, "location_th").trim();
        int capacity = intValue(body.get("capacity"), 0);
        boolean approval = false;
        boolean active = bool(body.get("active"));
        String amenities = str(body, "amenities");
        String color = str(body, "color");
        if (name.isBlank() || name.length() > 100 || nameTh.isBlank() || nameTh.length() > 100
                || location.isBlank() || location.length() > 120 || locationTh.isBlank()
                || locationTh.length() > 120 || capacity < 1 || capacity > 1000) {
            return error(HttpStatus.BAD_REQUEST, "invalidInput");
        }
        if (!Set.of("indigo", "teal", "blue", "violet").contains(color)) color = "indigo";

        Room old = findRoom(id);
        if (old != null && old.deleted()) return error(HttpStatus.NOT_FOUND, "notFound");
        String roomId = id;
        if (old != null && (!active || capacity < old.capacity())) {
            List<Booking> future = allBookings().stream()
                    .filter(b -> b.rid.equals(roomId) && isActive(b.status) && isFuture(b))
                    .toList();
            boolean affects = !future.isEmpty() && (!active || future.stream().anyMatch(b -> b.participants > capacity));
            if (affects) return error(HttpStatus.CONFLICT, "roomHasBookings");
        }

        String image = old == null ? "" : old.image();
        if (body.containsKey("image")) {
            image = str(body, "image");
            if (!RoomImages.allowed(image, jdbc)) return error(HttpStatus.BAD_REQUEST, "invalidImage");
        }
        if (!str(body, "imageData").isBlank()) {
            try { image = RoomImages.save(str(body, "imageData"), jdbc); }
            catch (IllegalArgumentException ex) { return error(HttpStatus.BAD_REQUEST, "invalidImage"); }
        }
        Room saved = new Room(id, name, nameTh, location, locationTh, capacity, approval, active,
                amenities, color, image);
        if (old == null) insertRoom(saved); else updateRoom(saved);
        return ResponseEntity.ok(Map.of("ok", true, "id", id));
    }

    /**
     * Deletes a room from the active catalogue. The row is kept with deleted = TRUE so past
     * reservations still show the room name; the room can no longer be listed, edited or booked.
     */
    @PostMapping("/room-delete")
    @Transactional
    public ResponseEntity<?> deleteRoom(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User admin = current(request);
        if (admin == null) return error(HttpStatus.UNAUTHORIZED, "signIn");
        if (!"admin".equals(admin.role)) return error(HttpStatus.FORBIDDEN, "forbidden");

        String id = str(body, "id").trim();
        jdbc.queryForList("SELECT id FROM room WHERE id = ? FOR UPDATE", id);
        Room room = findRoom(id);
        if (room == null || room.deleted()) return error(HttpStatus.NOT_FOUND, "notFound");
        boolean hasUpcoming = allBookings().stream()
                .anyMatch(b -> b.rid.equals(id) && isActive(b.status) && isFuture(b));
        if (hasUpcoming) return error(HttpStatus.CONFLICT, "roomHasUpcoming");

        jdbc.update("UPDATE room SET deleted = TRUE, active = FALSE WHERE id = ?", id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    private String nextRoomId() {
        int number = allRooms().size() + 1;
        String candidate;
        do {
            candidate = "R" + String.format("%03d", number++);
        } while (findRoom(candidate) != null);
        return candidate;
    }

}
