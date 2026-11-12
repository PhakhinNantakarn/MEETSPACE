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

@RestController
@RequestMapping("/api")
public class ApiController {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final ZoneId BANGKOK = ZoneId.of("Asia/Bangkok");
    private static final Set<String> ACTIVE_STATUSES = Set.of("pending", "confirmed");

    private final JdbcTemplate jdbc;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public ApiController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostConstruct
    void seed() {
        if (findUserById("walk-in") == null) {
            jdbc.update("INSERT INTO app_user (id,email,name,password_hash,role,self_registered) VALUES (?,?,?,?,?,?)",
                    "walk-in", "walk-in@meetspace.invalid", "Walk-in kiosk", "!", "user", false);
        }
        if (findUserById("workspace-admin") == null) {
            jdbc.update("""
                    INSERT INTO app_user (id, email, name, password_hash, role, self_registered)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """, "workspace-admin", "admin@meetspace.local", "Workspace Admin",
                    encoder.encode("admin123"), "admin", false);
        }

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
                "SELECT COUNT(*) FROM room WHERE id = ?", Integer.class, replacement.id);
        if (replacementCount == null || replacementCount == 0) insertRoom(replacement);

        jdbc.update("UPDATE booking SET room_id = ? WHERE room_id = ?", replacement.id, legacyId);
        jdbc.update("DELETE FROM room WHERE id = ?", legacyId);
    }

    private void seedRoom(Room room) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM room WHERE id = ?", Integer.class, room.id);
        if (count != null && count == 0) insertRoom(room);
    }

    @GetMapping("/session")
    public Map<String, Object> session(HttpServletRequest request) {
        User user = current(request);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("user", user == null ? null : user.publicMap());
        out.put("setupRequired", false);
        out.put("setupAllowed", false);
        out.put("setupEmail", "admin@meetspace.local");
        out.put("ownerSignedIn", false);
        return out;
    }

    @GetMapping({"/data", "/walk-in/data"})
    public ResponseEntity<?> data(HttpServletRequest request) {
        User user = request.getRequestURI().endsWith("/walk-in/data") ? null : current(request);
        List<Map<String, Object>> roomList = allRooms().stream().map(Room::map).toList();
        List<Booking> storedBookings = allBookings();

        List<Map<String, Object>> bookingList;
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

        List<Map<String, Object>> people;
        if (user == null) {
            people = List.of();
        } else if ("admin".equals(user.role)) {
            people = allUsers().stream().map(User::publicMap).toList();
        } else {
            people = List.of(user.publicMap());
        }

        List<Map<String, Object>> notes = user == null
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

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String name = str(body, "name").trim();
        String email = str(body, "email").trim().toLowerCase(Locale.ROOT);
        String password = str(body, "password");
        if (name.isBlank() || name.length() > 100 || !EMAIL.matcher(email).matches()) {
            return error(HttpStatus.BAD_REQUEST, "invalidInput");
        }
        ResponseEntity<?> passError = passwordError(password);
        if (passError != null) return passError;
        if (findUserByEmail(email) != null) return error(HttpStatus.CONFLICT, "emailExists");

        String id = UUID.randomUUID().toString();
        jdbc.update("""
                INSERT INTO app_user (id, email, name, password_hash, role, self_registered)
                VALUES (?, ?, ?, ?, ?, ?)
                """, id, email, name, encoder.encode(password), "user", true);
        User user = findUserById(id);
        request.getSession(true).setAttribute("uid", id);
        return ResponseEntity.ok(Map.of("ok", true, "user", user.publicMap()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String email = str(body, "email").trim().toLowerCase(Locale.ROOT);
        String password = str(body, "password");
        User user = findUserByEmail(email);
        if (user == null || !encoder.matches(password, user.hash)) {
            return error(HttpStatus.UNAUTHORIZED, "invalidLogin");
        }
        request.getSession(true).setAttribute("uid", user.id);
        return ResponseEntity.ok(Map.of("ok", true, "user", user.publicMap()));
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return Map.of("ok", true);
    }

    @PostMapping("/password")
    @Transactional
    public ResponseEntity<?> changePassword(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = current(request);
        if (user == null) return error(HttpStatus.UNAUTHORIZED, "signIn");
        String current = str(body, "current");
        String password = str(body, "password");
        if (!encoder.matches(current, user.hash)) return error(HttpStatus.BAD_REQUEST, "invalidLogin");
        ResponseEntity<?> passError = passwordError(password);
        if (passError != null) return passError;

        jdbc.update("UPDATE app_user SET password_hash = ? WHERE id = ?", encoder.encode(password), user.id);
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/user")
    @Transactional
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User admin = current(request);
        if (admin == null) return error(HttpStatus.UNAUTHORIZED, "signIn");
        if (!"admin".equals(admin.role)) return error(HttpStatus.FORBIDDEN, "forbidden");

        String name = str(body, "name").trim();
        String email = str(body, "email").trim().toLowerCase(Locale.ROOT);
        String password = str(body, "password");
        String role = "admin".equals(str(body, "role")) ? "admin" : "user";
        if (name.isBlank() || name.length() > 100 || !EMAIL.matcher(email).matches()) {
            return error(HttpStatus.BAD_REQUEST, "invalidInput");
        }
        ResponseEntity<?> passError = passwordError(password);
        if (passError != null) return passError;
        if (findUserByEmail(email) != null) return error(HttpStatus.CONFLICT, "emailExists");

        jdbc.update("""
                INSERT INTO app_user (id, email, name, password_hash, role, self_registered)
                VALUES (?, ?, ?, ?, ?, ?)
                """, UUID.randomUUID().toString(), email, name, encoder.encode(password), role, false);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/user-role")
    @Transactional
    public ResponseEntity<?> userRole(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User admin = current(request);
        if (admin == null) return error(HttpStatus.UNAUTHORIZED, "signIn");
        if (!"admin".equals(admin.role)) return error(HttpStatus.FORBIDDEN, "forbidden");

        String id = str(body, "id");
        User target = findUserById(id);
        if (target == null) return error(HttpStatus.NOT_FOUND, "notFound");
        if (target.id.equals(admin.id) || target.id.equals("workspace-admin")) {
            return error(HttpStatus.FORBIDDEN, "protectedAccount");
        }
        String previousRole = str(body, "previousRole");
        if (!previousRole.isBlank() && !previousRole.equals(target.role)) {
            return error(HttpStatus.CONFLICT, "accountChanged");
        }
        String role = "admin".equals(str(body, "role")) ? "admin" : "user";
        jdbc.update("UPDATE app_user SET role = ? WHERE id = ?", role, id);
        return ResponseEntity.ok(Map.of("ok", true));
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
        if (old != null && old.deleted) return error(HttpStatus.NOT_FOUND, "notFound");
        String roomId = id;
        if (old != null && (!active || capacity < old.capacity)) {
            List<Booking> future = allBookings().stream()
                    .filter(b -> b.rid.equals(roomId) && isActive(b.status) && isFuture(b))
                    .toList();
            boolean affects = !future.isEmpty() && (!active || future.stream().anyMatch(b -> b.participants > capacity));
            if (affects) return error(HttpStatus.CONFLICT, "roomHasBookings");
        }

        String image = old == null ? "" : old.image;
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
        if (room == null || room.deleted) return error(HttpStatus.NOT_FOUND, "notFound");
        boolean hasUpcoming = allBookings().stream()
                .anyMatch(b -> b.rid.equals(id) && isActive(b.status) && isFuture(b));
        if (hasUpcoming) return error(HttpStatus.CONFLICT, "roomHasUpcoming");

        jdbc.update("UPDATE room SET deleted = TRUE, active = FALSE WHERE id = ?", id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping({"/book", "/walk-in/book"})
    @Transactional
    public synchronized ResponseEntity<?> book(@RequestBody Map<String, Object> body,
                                                HttpServletRequest request) {
        boolean walkIn = request.getRequestURI().endsWith("/walk-in/book");
        User user = walkIn ? findUserById("walk-in") : current(request);
        if (user == null) return error(HttpStatus.UNAUTHORIZED, "signIn");
        String rid = str(body, "rid");
        jdbc.queryForList("SELECT id FROM room WHERE id = ? FOR UPDATE", rid);
        Room room = findRoom(rid);
        if (room == null || room.deleted) return error(HttpStatus.NOT_FOUND, "notFound");
        if (!room.active) return error(HttpStatus.CONFLICT, "unavailable");

        String title = "Room reservation";
        String identity = walkIn ? str(body, "booker").trim() : user.name;
        if (identity.isBlank() || identity.length() > 100) return error(HttpStatus.BAD_REQUEST, "invalidBooker");
        String date = str(body, "date");
        String start = str(body, "start");
        String end = str(body, "end");
        int participants = 1;
        int repeat = 1;
        boolean skip = false;
        String requestKey = str(body, "key").trim();

        if (title.isBlank() || title.length() > 160) return error(HttpStatus.BAD_REQUEST, "invalidTitle");
        if (participants < 1) return error(HttpStatus.BAD_REQUEST, "invalidParticipants");
        if (participants > room.capacity) {
            return ResponseEntity.badRequest().body(Map.of("error", "capacity", "capacity", room.capacity));
        }
        if (repeat < 1 || repeat > 12) return error(HttpStatus.BAD_REQUEST, "invalidRepeat");
        if (!validSlot(start) || !validSlot(end) || start.compareTo(end) >= 0) {
            return error(HttpStatus.BAD_REQUEST, "invalidTime");
        }
        if (start.compareTo("08:00") < 0 || end.compareTo("20:00") > 0
                || Duration.between(LocalTime.parse(start), LocalTime.parse(end)).toMinutes() > 240) {
            return error(HttpStatus.BAD_REQUEST, "invalidTime");
        }
        if (requestKey.length() > 80) return error(HttpStatus.BAD_REQUEST, "invalidInput");
        if (requestKey.isBlank()) requestKey = UUID.randomUUID().toString();
        Integer duplicate = jdbc.queryForObject(
                "SELECT COUNT(*) FROM booking WHERE user_id = ? AND request_key = ?",
                Integer.class, user.id, requestKey);
        if (duplicate != null && duplicate > 0) return error(HttpStatus.CONFLICT, "duplicate");

        LocalDate first;
        try {
            first = LocalDate.parse(date);
        } catch (DateTimeParseException ex) {
            return error(HttpStatus.BAD_REQUEST, "invalidDate");
        }
        ZonedDateTime now = ZonedDateTime.now(BANGKOK);
        LocalDate max = now.toLocalDate().plusYears(1);
        if (first.plusWeeks(repeat - 1L).isAfter(max)) return error(HttpStatus.BAD_REQUEST, "tooFar");
        String currentTime = now.toLocalTime().withSecond(0).withNano(0).toString().substring(0, 5);
        if (first.isBefore(now.toLocalDate())
                || (first.equals(now.toLocalDate()) && start.compareTo(currentTime) <= 0)) {
            return error(HttpStatus.BAD_REQUEST, "past");
        }

        List<LocalDate> requested = new ArrayList<>();
        for (int i = 0; i < repeat; i++) requested.add(first.plusWeeks(i));
        List<String> skipped = new ArrayList<>();
        List<LocalDate> free = new ArrayList<>();
        for (LocalDate requestedDate : requested) {
            if (hasConflict(rid, requestedDate.toString(), start, end)) {
                skipped.add(requestedDate.toString());
            } else {
                free.add(requestedDate);
            }
        }
        if ((!skipped.isEmpty() && !skip) || free.isEmpty()) {
            return error(HttpStatus.CONFLICT, "conflict");
        }

        String series = repeat > 1 ? UUID.randomUUID().toString() : null;
        List<Map<String, Object>> made = new ArrayList<>();
        for (LocalDate freeDate : free) {
            String id = UUID.randomUUID().toString();
            String nowIso = Instant.now().toString();
            String status = "confirmed";
            Booking booking = new Booking(id, rid, user.id, title, freeDate.toString(), start, end,
                    participants, status, series, requestKey, nowIso, nowIso, "");
            insertBooking(booking);
            jdbc.update("UPDATE booking SET booker = ? WHERE id = ?", identity, id);
            insertEvent(new Event(UUID.randomUUID().toString(), user.id, id,
                    "confirmed", nowIso, user.name));
            made.add(booking.map());
        }
        return ResponseEntity.ok(Map.of("bookings", made, "skipped", skipped));
    }

    @PostMapping("/reservation")
    @Transactional
    public ResponseEntity<?> reservation(@RequestBody Map<String, Object> body,
                                         HttpServletRequest request) {
        User user = current(request);
        if (user == null) return error(HttpStatus.UNAUTHORIZED, "signIn");
        String id = str(body, "id");
        String action = str(body, "action");
        Booking booking = findBooking(id);
        if (booking == null) return error(HttpStatus.NOT_FOUND, "notFound");

        if ("approve".equals(action) || "reject".equals(action)) {
            if (!"admin".equals(user.role)) return error(HttpStatus.FORBIDDEN, "forbidden");
            if (!"pending".equals(booking.status)) return error(HttpStatus.CONFLICT, "changed");
            String reason = "reject".equals(action) ? str(body, "reason").trim() : "";
            if (reason.length() > 300 || ("reject".equals(action) && reason.isBlank())) {
                return error(HttpStatus.BAD_REQUEST, "invalidInput");
            }

            String status = "approve".equals(action) ? "confirmed" : "rejected";
            String updated = Instant.now().toString();
            int changed = jdbc.update("""
                    UPDATE booking SET status = ?, reason = ?, updated_at = ?
                    WHERE id = ? AND status = 'pending'
                    """, status, reason, updated, id);
            if (changed == 0) return error(HttpStatus.CONFLICT, "changed");
            insertEvent(new Event(UUID.randomUUID().toString(), booking.uid, booking.id,
                    "approve".equals(action) ? "confirmed" : "rejected", updated, user.name));
            return ResponseEntity.ok(Map.of("ok", true));
        }

        if ("cancel".equals(action)) {
            if (!booking.uid.equals(user.id) && !"admin".equals(user.role)) {
                return error(HttpStatus.FORBIDDEN, "forbidden");
            }
            // FR-09: only an active reservation that has not yet ended can be cancelled.
            if (!isActive(booking.status)) return error(HttpStatus.CONFLICT, "changed");
            if (!isFuture(booking)) return error(HttpStatus.CONFLICT, "alreadyEnded");
            boolean series = bool(body.get("series"));
            List<Booking> targets = allBookings().stream()
                    .filter(item -> item.id.equals(id)
                            || (series && booking.series != null && booking.series.equals(item.series) && isFuture(item)))
                    .toList();
            for (Booking target : targets) {
                if (isActive(target.status)) {
                    String updated = Instant.now().toString();
                    jdbc.update("UPDATE booking SET status = 'cancelled', updated_at = ? WHERE id = ?",
                            updated, target.id);
                    insertEvent(new Event(UUID.randomUUID().toString(), target.uid, target.id,
                            "cancelled", updated, user.name));
                }
            }
            return ResponseEntity.ok(Map.of("ok", true));
        }
        return error(HttpStatus.BAD_REQUEST, "invalidInput");
    }

    private User current(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object uid = session.getAttribute("uid");
        return uid == null ? null : findUserById(uid.toString());
    }

    private ResponseEntity<?> passwordError(String password) {
        int length = password.codePointCount(0, password.length());
        if (length < 8 || length > 12) return error(HttpStatus.BAD_REQUEST, "passwordLength");
        return null;
    }

    private boolean hasConflict(String roomId, String date, String start, String end) {
        Integer count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM booking
                WHERE room_id = ? AND booking_date = ?
                  AND status IN ('pending', 'confirmed')
                  AND start_time < ? AND end_time > ?
                """, Integer.class, roomId, date, end, start);
        return count != null && count > 0;
    }

    private boolean isFuture(Booking booking) {
        ZonedDateTime now = ZonedDateTime.now(BANGKOK);
        LocalDate date = LocalDate.parse(booking.date);
        if (date.isAfter(now.toLocalDate())) return true;
        String currentTime = now.toLocalTime().withSecond(0).withNano(0).toString().substring(0, 5);
        return date.equals(now.toLocalDate()) && booking.end.compareTo(currentTime) > 0;
    }

    private Map<String, Object> bookingDetails(Booking booking) {
        Map<String, Object> result = booking.map();
        String identity = jdbc.queryForObject("SELECT booker FROM booking WHERE id = ?", String.class, booking.id);
        User owner = findUserById(booking.uid);
        result.put("booker", identity == null || identity.isBlank()
                ? (owner == null ? "Walk-in" : owner.name) : identity);
        result.put("source", booking.uid.equals("walk-in") ? "Walk-in" : "Member");
        return result;
    }

    private static boolean isActive(String status) {
        return ACTIVE_STATUSES.contains(status);
    }

    private static boolean validSlot(String value) {
        if (value == null || !value.matches("(?:[01]\\d|2[0-3]):[0-5]\\d")) return false;
        return Integer.parseInt(value.substring(3)) % 30 == 0;
    }

    private String nextRoomId() {
        int number = allRooms().size() + 1;
        String candidate;
        do {
            candidate = "R" + String.format("%03d", number++);
        } while (findRoom(candidate) != null);
        return candidate;
    }

    private List<User> allUsers() {
        return jdbc.query("SELECT * FROM app_user WHERE id <> 'walk-in' ORDER BY name", this::mapUser);
    }

    private User findUserById(String id) {
        return first(jdbc.query("SELECT * FROM app_user WHERE id = ?", this::mapUser, id));
    }

    private User findUserByEmail(String email) {
        return first(jdbc.query("SELECT * FROM app_user WHERE LOWER(email) = LOWER(?)", this::mapUser, email));
    }

    private User mapUser(ResultSet rs, int row) throws SQLException {
        return new User(rs.getString("id"), rs.getString("email"), rs.getString("name"),
                rs.getString("password_hash"), rs.getString("role"), rs.getBoolean("self_registered"));
    }

    private List<Room> allRooms() {
        return jdbc.query("SELECT * FROM room ORDER BY id", this::mapRoom);
    }

    private Room findRoom(String id) {
        return first(jdbc.query("SELECT * FROM room WHERE id = ?", this::mapRoom, id));
    }

    private Room mapRoom(ResultSet rs, int row) throws SQLException {
        return new Room(rs.getString("id"), rs.getString("name"), rs.getString("name_th"),
                rs.getString("location"), rs.getString("location_th"), rs.getInt("capacity"),
                rs.getBoolean("approval"), rs.getBoolean("active"), rs.getString("amenities"),
                rs.getString("color"), rs.getString("image_url"), rs.getBoolean("deleted"));
    }

    private void insertRoom(Room room) {
        jdbc.update("""
                INSERT INTO room (id, name, name_th, location, location_th, capacity, approval,
                                  active, amenities, color, image_url)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, room.id, room.name, room.nameTh, room.location, room.locationTh, room.capacity,
                room.approval, room.active, room.amenities, room.color, room.image);
    }

    private void updateRoom(Room room) {
        jdbc.update("""
                UPDATE room SET name = ?, name_th = ?, location = ?, location_th = ?, capacity = ?,
                                approval = ?, active = ?, amenities = ?, color = ?, image_url = ?
                WHERE id = ?
                """, room.name, room.nameTh, room.location, room.locationTh, room.capacity,
                room.approval, room.active, room.amenities, room.color, room.image, room.id);
    }

    private List<Booking> allBookings() {
        return jdbc.query("SELECT * FROM booking ORDER BY created_at DESC", this::mapBooking);
    }

    private Booking findBooking(String id) {
        return first(jdbc.query("SELECT * FROM booking WHERE id = ?", this::mapBooking, id));
    }

    private Booking mapBooking(ResultSet rs, int row) throws SQLException {
        return new Booking(rs.getString("id"), rs.getString("room_id"), rs.getString("user_id"),
                rs.getString("title"), rs.getString("booking_date"), rs.getString("start_time"),
                rs.getString("end_time"), rs.getInt("participants"), rs.getString("status"),
                rs.getString("series_id"), rs.getString("request_key"), rs.getString("created_at"),
                rs.getString("updated_at"), rs.getString("reason"));
    }

    private void insertBooking(Booking booking) {
        jdbc.update("""
                INSERT INTO booking (id, room_id, user_id, title, booking_date, start_time, end_time,
                                     participants, status, series_id, request_key, created_at, updated_at, reason)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, booking.id, booking.rid, booking.uid, booking.title, booking.date, booking.start,
                booking.end, booking.participants, booking.status, booking.series, booking.request,
                booking.created, booking.updated, booking.reason);
    }

    private List<Event> eventsFor(String userId) {
        return jdbc.query("SELECT * FROM booking_event WHERE user_id = ? ORDER BY event_at DESC",
                this::mapEvent, userId);
    }

    private Event mapEvent(ResultSet rs, int row) throws SQLException {
        return new Event(rs.getString("id"), rs.getString("user_id"), rs.getString("booking_id"),
                rs.getString("kind"), rs.getString("event_at"), rs.getString("actor"));
    }

    private void insertEvent(Event event) {
        jdbc.update("""
                INSERT INTO booking_event (id, user_id, booking_id, kind, event_at, actor)
                VALUES (?, ?, ?, ?, ?, ?)
                """, event.id, event.uid, event.bid, event.kind, event.at, event.actor);
    }

    private static <T> T first(List<T> values) {
        return values.isEmpty() ? null : values.get(0);
    }

    private static String str(Map<String, Object> body, String key) {
        Object value = body.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private static int intValue(Object value, int fallback) {
        try {
            return value instanceof Number number ? number.intValue() : Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static boolean bool(Object value) {
        return value instanceof Boolean bool
                ? bool
                : value != null && Set.of("true", "on", "1").contains(String.valueOf(value).toLowerCase(Locale.ROOT));
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus status, String code) {
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
