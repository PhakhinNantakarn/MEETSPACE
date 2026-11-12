package com.meetspace.app;

import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import javax.imageio.ImageIO;
import java.io.*;
import java.util.*;

/** Stores validated raster images in the same persistent database as rooms. */
@RestController
public class RoomImages {
    private final JdbcTemplate jdbc;
    private static final Set<String> BUILTIN = Set.of("", "/images/board-room.jpg",
            "/images/collaboration-studio.png", "/images/focus-room.jpg",
            "/images/inspire-room.png", "/images/team-studio.jpg", "/images/workshop-room.jpg");
    public RoomImages(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    static boolean allowed(String path, JdbcTemplate jdbc) {
        if (BUILTIN.contains(path)) return true;
        if (!path.matches("/api/room-images/[a-f0-9-]{36}")) return false;
        return jdbc.queryForObject("SELECT COUNT(*) FROM room_image WHERE id = ?",
                Integer.class, path.substring("/api/room-images/".length())) == 1;
    }

    static String save(String data, JdbcTemplate jdbc) {
        if (data.length() > 4200000 || !data.matches("(?s)^data:image/(png|jpeg);base64,.*"))
            throw new IllegalArgumentException();
        try {
            byte[] raw = Base64.getDecoder().decode(data.substring(data.indexOf(',') + 1));
            if (raw.length > 3 * 1024 * 1024) throw new IllegalArgumentException();
            try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(raw))) {
                var readers = ImageIO.getImageReaders(input);
                if (!readers.hasNext()) throw new IllegalArgumentException();
                var reader = readers.next();
                try {
                    reader.setInput(input);
                    String format = reader.getFormatName();
                    if (!format.equalsIgnoreCase("png") && !format.equalsIgnoreCase("jpeg"))
                        throw new IllegalArgumentException();
                    int w = reader.getWidth(0), h = reader.getHeight(0);
                    if (w < 1 || h < 1 || w > 6000 || h > 6000 || (long) w*h > 16000000)
                        throw new IllegalArgumentException();
                    var image = reader.read(0);
                    var output = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", output);
                    String id = UUID.randomUUID().toString();
                    jdbc.update("INSERT INTO room_image (id, content) VALUES (?, ?)", id, output.toByteArray());
                    return "/api/room-images/" + id;
                } finally { reader.dispose(); }
            }
        } catch (IOException | RuntimeException ex) { throw new IllegalArgumentException("Invalid image", ex); }
    }

    @GetMapping("/api/room-images/{id}")
    public ResponseEntity<byte[]> image(@PathVariable String id) {
        var images = jdbc.query("SELECT content FROM room_image WHERE id = ?",
                (rs, n) -> rs.getBytes(1), id);
        if (images.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG)
                .header("X-Content-Type-Options", "nosniff").body(images.get(0));
    }
}
