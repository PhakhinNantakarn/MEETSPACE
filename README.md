# MEETSPACE — Smart Conference Room

**MEETSPACE** is a web-based meeting room reservation system. It has a member
website and a separate walk-in kiosk.

## About the Project

### The problem

Staff who need a meeting room often don't know which rooms have the right
equipment or enough seats. The room status they see isn't reliable either: a
room can look free while someone is already using it.

Bookings are also made informally, through chat messages or verbal agreements,
and kept in different places. As a result:

- the same room gets booked twice
- people waste time looking for a room, even for urgent meetings
- no one can tell who is responsible for a booking

### Our solution

MEETSPACE keeps every booking in one shared record, whether it was made in
advance by a member or on the spot by a walk-in visitor. This means no time
slot can be booked twice.

Before booking, users can see each room's photo, equipment, capacity, and
which time slots are taken. Every valid booking is confirmed immediately.

Administrators can see who made each booking and which channel it came from,
and they can cancel bookings when needed.

---

## Team Members

| No. | Student ID | Name                        |
|-----|------------|-----------------------------|
| 1   | 6831503064 | Phakhin Nantakarn           |
| 2   | 6831503067 | Ratchata Suppattapan        |
| 3   | 6831503070 | Witthawin Tanaset           |
| 4   | 6831503072 | Siwakorn Jaronsak           |
| 5   | 6831503116 | Thanaphathr Phutthiphiriya  |

School of Applied Digital Technology, Mae Fah Luang University

---

## Features

- **Public room browsing**: see room photos, equipment, capacity, and booked
  time slots (08:00–20:00) without signing in.
- **Walk-in kiosk** (`/#walkin`): book a room without an account by entering a
  name or employee ID.
- **Member accounts**: register, sign in, sign out, and change your password.
- **Instant reservations**: each booking covers one room, one date, and one
  time range, and is confirmed right away.
- **Booking validation**: the server checks every booking against the rules
  below. It prevents overlaps and duplicate submissions.
- **My reservations**: view upcoming bookings, history, and notifications, and
  cancel bookings.
- **Administration**:
  - booking management, filtered by date and room, with walk-in and member
    bookings listed separately
  - room management: add, edit, change photo, or delete rooms
  - member and role management
- **Thai / English** interface and **light / dark** theme.
- **Privacy**: other users can see that a time slot is taken, but not who booked
  it or the meeting title.

---

## Tech Stack

| Part      | Technology                              |
|-----------|-----------------------------------------|
| Backend   | Java 17, Spring Boot 3.3.5              |
| Database  | H2 (stored in `data/meetspace.mv.db`)   |
| Frontend  | HTML, CSS, JavaScript                   |
| Security  | BCrypt password hashing                 |
| Build     | Maven                                   |
| Testing   | JUnit 5, Node.js test runner            |

---

## How to Run

### 1. Requirements

- **Java 17**
- **Maven 3.9+**

### 2. Start the server

Open a terminal in the project folder (the one that contains `pom.xml`) and run:

```bash
mvn spring-boot:run
```

### 3. Open the website

Go to **http://localhost:8080**. To stop the server, press `Ctrl + C`.

> The database is created automatically in the `data` folder the first time the
> app runs, and your data is kept after a restart.

---

## How to Use

### Guest (no account)

1. Open the home page to see all rooms and their availability.
2. Choose a date to see which time slots are free.

### Walk-in kiosk (no account)

1. Open **http://localhost:8080/#walkin**
2. Choose a room, date, and time.
3. Enter your name or employee ID and a meeting title.
4. Confirm. The booking is confirmed immediately, and a reference number is
   shown.

### Member

1. Click **Sign in**, or register a new account. Passwords must be 8–12
   characters.
2. Choose a room and click **Reserve**.
3. Enter the meeting title, date, and time, then review and confirm.
4. Use **My reservations** to view or cancel bookings.
5. Use **History** and **Notifications** to check past bookings and updates.

### Administrator

Sign in with the test admin account:

| Email                    | Password   |
|--------------------------|------------|
| `admin@meetspace.local`  | `admin123` |

Then open **Administration**, where you can:

- manage all bookings
- add or edit rooms
- manage members and their roles

---

## Booking Rules

| Rule            | Detail                                          |
|-----------------|-------------------------------------------------|
| Opening hours   | 08:00 – 20:00 (Bangkok time, UTC+7)             |
| Time slots      | Every 30 minutes                                |
| Maximum length  | 4 hours per booking                             |
| Booking window  | Must be in the future, up to 1 year ahead       |
| Overlaps        | Not allowed                                     |
| Cancellation    | Only for bookings that have not ended yet       |
| Editing         | Not supported. Cancel and book again instead.   |

---

## Meeting Rooms

| ID     | Room                 | Location               | Capacity |
|--------|----------------------|------------------------|----------|
| MS-101 | Inspire Room         | Floor 1 · North Wing   | 6        |
| MS-204 | Collaboration Studio | Floor 2 · Central Hub  | 12       |
| MS-307 | Innovation Lab       | Floor 3 · East Wing    | 20       |
| MS-410 | Skyline Boardroom    | Floor 4 · West Wing    | 16       |

Administrators can upload room photos in PNG or JPG format, up to 3 MB and
6000 px per side.

---

## Running Tests

```bash
# Backend tests
mvn clean test

# UI tests (requires Node.js 18+)
node --test src/test/ui.test.cjs
```

---

