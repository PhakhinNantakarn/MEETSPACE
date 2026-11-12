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

/** Member login, registration, administrator roles and sessions. */
@RestController
@RequestMapping("/api")
public class AuthController extends MeetSpaceSupport {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(JdbcTemplate jdbc) {
        super(jdbc);
    }

    @PostConstruct
    void seedDefaultUsers() {
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

    private ResponseEntity<?> passwordError(String password) {
        int length = password.codePointCount(0, password.length());
        if (length < 8 || length > 12) return error(HttpStatus.BAD_REQUEST, "passwordLength");
        return null;
    }

}
