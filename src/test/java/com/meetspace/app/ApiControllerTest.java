package com.meetspace.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:meetspace-test;DB_CLOSE_DELAY=-1",
        "spring.sql.init.mode=always",
        "spring.h2.console.enabled=false"
})
@AutoConfigureMockMvc
class ApiControllerTest {
    private static final ZoneId BANGKOK = ZoneId.of("Asia/Bangkok");

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void cleanUserData() {
        jdbc.update("DELETE FROM booking_event");
        jdbc.update("DELETE FROM booking");
        jdbc.update("DELETE FROM app_user WHERE id NOT IN ('workspace-admin', 'walk-in')");
    }

    @Test
    void passwordMustBeBetweenEightAndTwelveCharacters() throws Exception {
        register("seven", "1234567")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("passwordLength"));

        register("eight", "12345678")
                .andExpect(status().isOk());

        register("twelve", "123456789012")
                .andExpect(status().isOk());

        register("thirteen", "1234567890123")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("passwordLength"));
    }

    @Test
    void guestsCanBrowseButCannotBook() throws Exception {
        mvc.perform(get("/api/data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user").doesNotExist())
                .andExpect(jsonPath("$.rooms.length()").value(4))
                .andExpect(jsonPath("$.rooms[0].id").value("MS-101"))
                .andExpect(jsonPath("$.rooms[0].name").value("Inspire Room"))
                .andExpect(jsonPath("$.rooms[0].image").value("/images/inspire-room.png"))
                .andExpect(jsonPath("$.rooms[1].id").value("MS-204"))
                .andExpect(jsonPath("$.rooms[1].name").value("Collaboration Studio"))
                .andExpect(jsonPath("$.rooms[1].image").value("/images/collaboration-studio.png"))
                .andExpect(jsonPath("$.rooms[2].name").value("Innovation Lab"))
                .andExpect(jsonPath("$.rooms[2].image").value("/images/board-room.jpg"))
                .andExpect(jsonPath("$.rooms[3].name").value("Skyline Boardroom"))
                .andExpect(jsonPath("$.rooms[3].capacity").value(16));

        mvc.perform(post("/api/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(bookingRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("signIn"));
    }

    @Test
    void registrationBookingPrivacyAndImmediateConfirmationWorkTogether() throws Exception {
        MvcResult registration = register("member", "pass1234")
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession memberSession = session(registration);

        MvcResult created = mvc.perform(post("/api/book")
                        .session(memberSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(bookingRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookings[0].status").value("confirmed"))
                .andReturn();

        JsonNode response = json.readTree(created.getResponse().getContentAsString());
        String bookingId = response.path("bookings").get(0).path("id").asText();

        mvc.perform(get("/api/data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookings[0].rid").value("MS-204"))
                .andExpect(jsonPath("$.bookings[0].title").doesNotExist())
                .andExpect(jsonPath("$.bookings[0].uid").doesNotExist());

        mvc.perform(get("/api/data").session(memberSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookings[0].title").value("Room reservation"));

        MvcResult adminLogin = mvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "email", "admin@meetspace.local",
                                "password", "admin123"))))
                .andExpect(status().isOk())
                .andReturn();

        mvc.perform(post("/api/reservation")
                        .session(session(adminLogin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "id", bookingId,
                                "action", "approve"))))
                .andExpect(status().isConflict());

        mvc.perform(get("/api/data").session(memberSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookings[0].status").value("confirmed"));
    }

    @Test
    void walkInBooksWithoutSessionAndRejectsConflicts() throws Exception {
        var body = bookingRequest();
        var made = mvc.perform(post("/api/walk-in/book").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookings.length()").value(1))
                .andExpect(jsonPath("$.bookings[0].status").value("confirmed"))
                .andReturn();
        org.junit.jupiter.api.Assertions.assertNull(made.getRequest().getSession(false));
        mvc.perform(get("/api/data"))
                .andExpect(jsonPath("$.user").doesNotExist())
                .andExpect(jsonPath("$.bookings[0].title").doesNotExist())
                .andExpect(jsonPath("$.bookings[0].id").doesNotExist());
        mvc.perform(post("/api/walk-in/book").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(bookingRequest())))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.error").value("conflict"));
        mvc.perform(post("/api/walk-in/book").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.error").value("duplicate"));
    }

    @Test
    void removedRepeatAndParticipantsCannotAffectBooking() throws Exception {
        var body = new java.util.HashMap<>(bookingRequest());
        body.put("repeat", 12);
        body.put("participants", 9999);
        mvc.perform(post("/api/walk-in/book").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.bookings.length()").value(1));
    }

    @Test
    void onlyAdminCanSaveValidatedRoomPhotos() throws Exception {
        var body = new java.util.HashMap<String, Object>();
        body.put("id", "MS-101");
        body.put("name", "Inspire Room");
        body.put("name_th", "ห้องอินสไปร์");
        body.put("location", "Floor 1 · North Wing");
        body.put("location_th", "ชั้น 1 · ฝั่งเหนือ");
        body.put("capacity", 6);
        body.put("active", true);
        body.put("image", "/images/inspire-room.png");
        mvc.perform(post("/api/room").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body))).andExpect(status().isUnauthorized());
        var login = mvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", "admin@meetspace.local", "password", "admin123"))))
                .andExpect(status().isOk()).andReturn();
        var admin = session(login);
        mvc.perform(get("/api/walk-in/data").session(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user").doesNotExist())
                .andExpect(jsonPath("$.people.length()").value(0))
                .andExpect(jsonPath("$.notifications.length()").value(0));
        body.put("imageData", "data:image/png;base64,bm90IGFuIGltYWdl");
        mvc.perform(post("/api/room").session(admin).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("invalidImage"));
        var output = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB), "png", output);
        body.put("imageData", "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(output.toByteArray()));
        mvc.perform(post("/api/room").session(admin).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body))).andExpect(status().isOk());
        String path = jdbc.queryForObject("SELECT image_url FROM room WHERE id='MS-101'", String.class);
        mvc.perform(get(path)).andExpect(status().isOk());
        body.remove("imageData");
        body.put("image", "https://example.com/untrusted.svg");
        mvc.perform(post("/api/room").session(admin).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body))).andExpect(status().isBadRequest());
        body.put("image", "/images/inspire-room.png");
        mvc.perform(post("/api/room").session(admin).contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body))).andExpect(status().isOk());
    }

    @Test
    void cancellationIsLimitedToOwnerAndActiveReservations() throws Exception {
        MockHttpSession owner = session(register("owner", "pass1234").andExpect(status().isOk()).andReturn());
        MockHttpSession other = session(register("other", "pass1234").andExpect(status().isOk()).andReturn());
        MvcResult created = mvc.perform(post("/api/book").session(owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(bookingRequest())))
                .andExpect(status().isOk()).andReturn();
        String id = json.readTree(created.getResponse().getContentAsString())
                .path("bookings").get(0).path("id").asText();
        String cancel = json.writeValueAsString(Map.of("id", id, "action", "cancel"));

        mvc.perform(post("/api/reservation").session(other)
                        .contentType(MediaType.APPLICATION_JSON).content(cancel))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/reservation").session(owner)
                        .contentType(MediaType.APPLICATION_JSON).content(cancel))
                .andExpect(status().isOk());
        mvc.perform(post("/api/reservation").session(owner)
                        .contentType(MediaType.APPLICATION_JSON).content(cancel))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("changed"));

        jdbc.update("""
                INSERT INTO booking (id, room_id, user_id, title, booking_date, start_time, end_time,
                                     participants, status, request_key, created_at, updated_at)
                SELECT 'past-booking', room_id, user_id, title, ?, '08:00', '09:00', 1, 'confirmed',
                       'past-key', created_at, updated_at FROM booking WHERE id = ?
                """, LocalDate.now(BANGKOK).minusDays(1).toString(), id);
        mvc.perform(post("/api/reservation").session(owner)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("id", "past-booking", "action", "cancel"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("alreadyEnded"));
    }

    @Test
    void adminCanDeleteRoomWithoutUpcomingReservationsAndHistoryIsKept() throws Exception {
        MockHttpSession admin = session(mvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "email", "admin@meetspace.local", "password", "admin123"))))
                .andExpect(status().isOk()).andReturn());
        try {
            var room = new java.util.HashMap<String, Object>(Map.of(
                    "id", "DEL-1", "name", "Temp Room", "name_th", "ห้องชั่วคราว",
                    "location", "Floor 9", "location_th", "ชั้น 9", "capacity", 4, "active", true));
            mvc.perform(post("/api/room").session(admin).contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(room))).andExpect(status().isOk());

            var booking = new java.util.HashMap<>(bookingRequest());
            booking.put("rid", "DEL-1");
            MvcResult made = mvc.perform(post("/api/book").session(admin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(booking)))
                    .andExpect(status().isOk()).andReturn();
            String bookingId = json.readTree(made.getResponse().getContentAsString())
                    .path("bookings").get(0).path("id").asText();
            String delete = json.writeValueAsString(Map.of("id", "DEL-1"));

            mvc.perform(post("/api/room-delete").contentType(MediaType.APPLICATION_JSON).content(delete))
                    .andExpect(status().isUnauthorized());
            mvc.perform(post("/api/room-delete").session(admin)
                            .contentType(MediaType.APPLICATION_JSON).content(delete))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("roomHasUpcoming"));

            mvc.perform(post("/api/reservation").session(admin).contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(Map.of("id", bookingId, "action", "cancel"))))
                    .andExpect(status().isOk());
            mvc.perform(post("/api/room-delete").session(admin)
                            .contentType(MediaType.APPLICATION_JSON).content(delete))
                    .andExpect(status().isOk());

            booking.put("key", UUID.randomUUID().toString());
            mvc.perform(post("/api/book").session(admin).contentType(MediaType.APPLICATION_JSON)
                            .content(json.writeValueAsString(booking)))
                    .andExpect(status().isNotFound());
            mvc.perform(post("/api/room").session(admin).contentType(MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(room))).andExpect(status().isNotFound());
            mvc.perform(get("/api/data").session(admin))
                    .andExpect(jsonPath("$.rooms[?(@.id == 'DEL-1')].deleted").value(1))
                    .andExpect(jsonPath("$.bookings[?(@.id == '" + bookingId + "')].status").value("cancelled"));
        } finally {
            jdbc.update("DELETE FROM booking_event WHERE booking_id IN (SELECT id FROM booking WHERE room_id = 'DEL-1')");
            jdbc.update("DELETE FROM booking WHERE room_id = 'DEL-1'");
            jdbc.update("DELETE FROM room WHERE id = 'DEL-1'");
        }
    }

    private org.springframework.test.web.servlet.ResultActions register(String label, String password)
            throws Exception {
        return mvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of(
                        "name", "Test " + label,
                        "email", label + "." + UUID.randomUUID() + "@example.com",
                        "password", password))));
    }

    private Map<String, Object> bookingRequest() {
        return Map.of(
                "rid", "MS-204",
                "title", "Project planning",
                "booker", "Employee 123",
                "date", LocalDate.now(BANGKOK).plusDays(1).toString(),
                "start", "10:00",
                "end", "11:00",
                "key", UUID.randomUUID().toString());
    }

    private MockHttpSession session(MvcResult result) {
        HttpSession session = result.getRequest().getSession(false);
        return (MockHttpSession) session;
    }
}
