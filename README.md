# MeetSpace

**MeetSpace** is a web application for booking meeting rooms.
Anyone can check which rooms are free without an account. Members sign in to
reserve and manage their bookings, and administrators manage rooms and users.

---

## Features

- **Room overview**: see every room with its photo, capacity, amenities, and an
  08:00–20:00 availability timeline. No sign-in required.
- **Walk-in booking**: a public self-service screen where visitors can book a
  room without creating an account.
- **Member reservations**: book a room, view upcoming meetings, cancel bookings,
  and check reservation history and notifications.
- **Instant confirmation**: new reservations are confirmed immediately.
- **No double booking**: the system blocks any time that overlaps an existing
  reservation.
- **Privacy**: other users can see that a time slot is taken, but not who booked
  it or what the meeting is about.
- **Administration**: add, edit, or deactivate rooms, upload room photos, and
  manage user accounts and roles.
- **Thai / English** language switch and **light / dark** theme.
- **Data is saved permanently**: everything stays in the database after the
  server restarts.

---

## Tech Stack

| Part      | Technology                                    |
|-----------|-----------------------------------------------|
| Backend   | Java 17, Spring Boot 3.3.5                    |
| Database  | H2 (stored in `data/meetspace.mv.db`)         |
| Frontend  | HTML, CSS, JavaScript                         |
| Security  | BCrypt password hashing                       |
| Build     | Maven                                         |
| Testing   | JUnit 5, Node.js test runner                  |

---

## How to Run

### 1. Install requirements

- **Java 17**
- **Maven 3.9+**

### 2. Start the server

Open a terminal in the project folder (the one that contains `pom.xml`) and run:

```bash
mvn spring-boot:run
```

### 3. Open the website

Go to **http://localhost:8080**

To stop the server, press `Ctrl + C`.

> The `data` folder and database are created automatically the first time the
> app runs.

---

## How to Use

### As a guest (no account)

1. Open the home page to see all rooms and their availability.
2. Click a room to see its schedule for any date.

### Walk-in booking (no account)

1. Open **http://localhost:8080/#walkin**
2. Choose a room, date, and time, then enter your name and a meeting title.
3. Confirm. The booking is confirmed right away, and you get a reference number.

### As a member

1. Click **Sign in**, or register a new account. Passwords must be 8–12
   characters.
2. Choose a room and click **Reserve**.
3. Fill in the meeting title, date, and time, then review and confirm.
4. Go to **My reservations** to see or cancel your bookings.
5. Go to **History** and **Notifications** to see past bookings and updates.

### As an administrator

Sign in with the test admin account:

| Email                    | Password   |
|--------------------------|------------|
| `admin@meetspace.local`  | `admin123` |

Then open **Administration**, where you can:

- **Room management**: add or edit rooms, change photos, or deactivate a room.
- **Team access**: add members and change user roles.

---

## Booking Rules

| Rule              | Detail                                       |
|-------------------|----------------------------------------------|
| Opening hours     | 08:00 – 20:00 (Bangkok time, UTC+7)          |
| Time slots        | Every 30 minutes                             |
| Maximum length    | 4 hours per booking                          |
| Overlaps          | Not allowed                                  |
| Cancellation      | Only for bookings that have not ended yet    |

---

## Meeting Rooms

| ID     | Room                 | Location               | Capacity |
|--------|----------------------|------------------------|----------|
| MS-101 | Inspire Room         | Floor 1 · North Wing   | 6        |
| MS-204 | Collaboration Studio | Floor 2 · Central Hub  | 12       |
| MS-307 | Innovation Lab       | Floor 3 · East Wing    | 20       |
| MS-410 | Skyline Boardroom    | Floor 4 · West Wing    | 16       |

Administrators can upload new room photos with these limits:

- PNG or JPG only
- up to 3 MB
- up to 6000 px per side

---

## Running Tests

```bash
# Backend tests
mvn clean test

# UI tests (requires Node.js 18+)
node --test src/test/ui.test.cjs
```

The tests cover:

- guest access
- sign-in protection
- registration and password rules
- walk-in privacy
- booking conflicts
- room photos
- page rendering

---

## Project Structure
