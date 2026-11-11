// ตัวช่วยเลือก element และ escape ข้อความก่อนนำไปแสดงใน HTML
const $ = (s) => document.querySelector(s);
const esc = (s) =>
  String(s ?? '').replace(
    /[&<>"']/g,
    (c) =>
      ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[
        c
      ],
  );
// ข้อความสองภาษาที่หน้าเว็บใช้ โดยเลือกภาษาตามค่าที่บันทึกไว้
const dictionary = {
  workspace: ['Workspace', 'พื้นที่ทำงาน'],
  recommended: ['Recommended rooms', 'ห้องแนะนำ'],
  recSearch: ['Find a room…', 'ค้นหาห้อง…'],
  recCount: ['Room {n} of {total}', 'ห้อง {n} จาก {total}'],
  recPrev: ['Previous room', 'ห้องก่อนหน้า'],
  recNext: ['Next room', 'ห้องถัดไป'],
  recEmpty: ['No rooms match your search', 'ไม่พบห้องที่ตรงกับการค้นหา'],
  recIntro: [
    'A quick look at the spaces in your workspace.',
    'ชมห้องประชุมในพื้นที่ทำงานของคุณ',
  ],
  manageCrumb: ['Manage', 'จัดการ'],
  overview: ['Overview', 'ภาพรวม'],
  dashboard: ['Dashboard', 'แดชบอร์ด'],
  rooms: ['Meeting rooms', 'ห้องประชุม'],
  my: ['My reservations', 'รายการจองของฉัน'],
  history: ['Reservation history', 'ประวัติการจอง'],
  notifications: ['Notifications', 'การแจ้งเตือน'],
  admin: ['Administration', 'ผู้ดูแลระบบ'],
  settings: ['Settings', 'การตั้งค่า'],
  menu: ['WORKSPACE', 'พื้นที่ทำงาน'],
  manage: ['MANAGE', 'จัดการ'],
  logout: ['Sign out', 'ออกจากระบบ'],
  collapse: ['Collapse navigation', 'ย่อเมนู'],
  expand: ['Expand navigation', 'ขยายเมนู'],
  toggleMenu: ['Toggle navigation', 'เปิดหรือปิดเมนู'],
  welcome: ['Your workspace, in sync.', 'ทุกการประชุม ลงตัวได้ง่าย ๆ'],
  dashboardIntro: [
    'A little less planning. A little more working together.',
    'จัดการห้องประชุมและติดตามการจองของคุณได้ในที่เดียว',
  ],
  newReservation: ['New reservation', 'จองห้องประชุม'],
  totalRooms: ['Total meeting rooms', 'ห้องประชุมทั้งหมด'],
  availableNow: ['Available now', 'ว่างขณะนี้'],
  upcomingCount: ['My upcoming reservations', 'การจองที่กำลังจะถึง'],
  pendingCount: ['Pending reservations', 'รายการรออนุมัติ'],
  pendingApprovals: ['Pending approvals', 'รอการอนุมัติ'],
  acrossWorkspace: ['Across your workspace', 'ในพื้นที่ทำงานของคุณ'],
  readyToBook: ['Ready for your next meeting', 'พร้อมสำหรับการประชุม'],
  onYourCalendar: ['Coming up next', 'กำลังจะมาถึง'],
  awaitingReview: ['Awaiting a decision', 'รอการพิจารณา'],
  upcoming: ['Upcoming reservations', 'การจองที่กำลังจะถึง'],
  viewAll: ['View all', 'ดูทั้งหมด'],
  findRoom: ['Find your next meeting space', 'ค้นหาห้องสำหรับการประชุม'],
  findRoomIntro: [
    'The right space for every conversation.',
    'เลือกพื้นที่ที่เหมาะกับทุกการประชุม',
  ],
  explore: ['Explore rooms', 'ดูห้องประชุม'],
  todayOverview: ['Workspace at a glance', 'ภาพรวมพื้นที่ทำงาน'],
  available: ['Available', 'ว่าง'],
  pending: ['Pending approval', 'รออนุมัติ'],
  reserved: ['Reserved', 'ถูกจอง'],
  confirmed: ['Confirmed', 'อนุมัติแล้ว'],
  unavailable: ['Unavailable', 'ไม่พร้อมใช้งาน'],
  closed: ['Outside opening hours', 'นอกเวลาเปิดใช้งาน'],
  cancelled: ['Cancelled', 'ยกเลิกแล้ว'],
  rejected: ['Rejected', 'ไม่อนุมัติ'],
  ended: ['Completed', 'เสร็จสิ้น'],
  people: ['people', 'คน'],
  seats: ['Capacity', 'ความจุ'],
  book: ['Reserve', 'จองห้อง'],
  schedule: ['View schedule', 'ดูตาราง'],
  until: ['Until', 'ถึง'],
  from: ['From', 'ตั้งแต่'],
  openHours: ['Open 08:00–20:00', 'เปิด 08:00–20:00'],
  allDay: ['Available all day', 'ว่างตลอดวัน'],
  dayFull: ['No free slots', 'ไม่มีช่วงเวลาว่าง'],
  roomHint: ['Plan ahead, meet better.', 'วางแผนล่วงหน้า ประชุมได้ลงตัว'],
  roomHintBody: [
    'Book a recurring meeting once. We’ll check every date for conflicts.',
    'จองการประชุมประจำได้ในครั้งเดียว พร้อมตรวจสอบเวลาซ้ำทุกวัน',
  ],
  bookRecurring: ['Plan a meeting', 'วางแผนการประชุม'],
  localTime: [
    'All times are in Bangkok (UTC+7).',
    'เวลาทั้งหมดเป็นเวลาประเทศไทย (UTC+7)',
  ],
  searchRooms: ['Search rooms or locations', 'ค้นหาห้องหรือสถานที่'],
  participants: ['Participants', 'จำนวนผู้เข้าร่วม'],
  date: ['Date', 'วันที่'],
  status: ['Status', 'สถานะ'],
  allStatus: ['All statuses', 'ทุกสถานะ'],
  all: ['All', 'ทั้งหมด'],
  noRooms: ['No rooms match your search', 'ไม่พบห้องที่ตรงกับการค้นหา'],
  noRoomsBody: [
    'Try another date, capacity or search term.',
    'ลองเปลี่ยนวันที่ จำนวนผู้เข้าร่วม หรือคำค้นหา',
  ],
  clearFilters: ['Clear filters', 'ล้างตัวกรอง'],
  noReservations: ['You don’t have any reservations yet.', 'คุณยังไม่มีรายการจอง'],
  noUpcoming: ['No upcoming reservations', 'ไม่มีการจองที่กำลังจะถึง'],
  noUpcomingBody: [
    'Find a space and make time to connect.',
    'เลือกห้องและกำหนดเวลาประชุมครั้งต่อไป',
  ],
  noHistory: [
    'Your reservation history will appear here.',
    'ประวัติการจองของคุณจะแสดงที่นี่',
  ],
  noHistoryBody: [
    'Completed, cancelled and rejected reservations stay here for reference.',
    'ดูรายการที่เสร็จสิ้น ยกเลิก หรือไม่ได้รับการอนุมัติย้อนหลังได้ที่นี่',
  ],
  room: ['Room', 'ห้อง'],
  meeting: ['Meeting', 'การประชุม'],
  time: ['Time', 'เวลา'],
  action: ['Action', 'การจัดการ'],
  details: ['Details', 'รายละเอียด'],
  cancel: ['Cancel', 'ยกเลิก'],
  close: ['Close', 'ปิด'],
  back: ['Back', 'ย้อนกลับ'],
  next: ['Review reservation', 'ตรวจสอบการจอง'],
  confirm: ['Confirm reservation', 'ยืนยันการจอง'],
  saving: ['Saving…', 'กำลังบันทึก…'],
  loading: ['Loading…', 'กำลังโหลด…'],
  retry: ['Try again', 'ลองอีกครั้ง'],
  title: ['Meeting title', 'หัวข้อการประชุม'],
  titlePlaceholder: ['e.g. Weekly team check-in', 'เช่น การประชุมทีมประจำสัปดาห์'],
  start: ['Start time', 'เวลาเริ่ม'],
  end: ['End time', 'เวลาสิ้นสุด'],
  repeat: ['Repeat weekly', 'จองซ้ำทุกสัปดาห์'],
  once: ['Does not repeat', 'ไม่จองซ้ำ'],
  weeks: ['weeks', 'สัปดาห์'],
  skip: ['Skip dates with conflicts', 'ข้ามวันที่มีเวลาซ้ำ'],
  approvalNote: [
    'This room requires administrator approval. Your time will be held while the request is reviewed.',
    'ห้องนี้ต้องได้รับการอนุมัติ ระบบจะกันเวลาไว้ระหว่างรอพิจารณา',
  ],
  autoNote: [
    'This room is confirmed immediately when your reservation is submitted.',
    'ห้องนี้จะได้รับการยืนยันเมื่อส่งรายการจองสำเร็จ',
  ],
  bookStep: ['Choose details', 'เลือกรายละเอียด'],
  reviewStep: ['Review & confirm', 'ตรวจสอบและยืนยัน'],
  summary: ['Reservation summary', 'สรุปการจอง'],
  checkDetails: [
    'Check your details before confirming.',
    'ตรวจสอบรายละเอียดก่อนยืนยัน',
  ],
  submitted: ['Reservation submitted successfully.', 'ส่งรายการจองสำเร็จ'],
  savedBody: [
    'Your reservation is saved. You can track it in My reservations.',
    'บันทึกการจองแล้ว ติดตามสถานะได้ในรายการจองของฉัน',
  ],
  viewReservations: ['View my reservations', 'ดูรายการจองของฉัน'],
  conflict: [
    'This time overlaps another reservation. Please choose a different time.',
    'ช่วงเวลานี้ซ้ำกับรายการจองอื่น กรุณาเลือกเวลาใหม่',
  ],
  capacity: [
    'This room can accommodate a maximum of {capacity} people.',
    'ห้องนี้รองรับผู้เข้าร่วมได้สูงสุด {capacity} คน',
  ],
  invalidParticipants: [
    'Enter a whole number of participants greater than zero.',
    'กรุณาระบุจำนวนผู้เข้าร่วมเป็นจำนวนเต็มมากกว่าศูนย์',
  ],
  invalidTitle: [
    'Enter a meeting title (up to 160 characters).',
    'กรุณาระบุหัวข้อการประชุมไม่เกิน 160 ตัวอักษร',
  ],
  invalidDate: ['Choose a valid date.', 'กรุณาเลือกวันที่ที่ถูกต้อง'],
  invalidTime: [
    'Choose a valid time between 08:00 and 20:00, in 30-minute intervals. The end must be after the start.',
    'เลือกเวลาระหว่าง 08:00–20:00 ทีละ 30 นาที โดยเวลาสิ้นสุดต้องอยู่หลังเวลาเริ่ม',
  ],
  past: ['Reservations must be in the future.', 'กรุณาเลือกวันและเวลาในอนาคต'],
  tooFar: [
    'Reservations can be made up to one year ahead.',
    'จองล่วงหน้าได้ไม่เกินหนึ่งปี',
  ],
  invalidRepeat: [
    'Choose between 1 and 16 occurrences.',
    'เลือกจำนวนครั้งระหว่าง 1 ถึง 16 ครั้ง',
  ],
  unavailableError: ['This room is no longer available.', 'ห้องนี้ไม่พร้อมใช้งานแล้ว'],
  serviceUnavailable: [
    'We couldn’t reach the service. Your entries are kept. Please try again.',
    'ไม่สามารถเชื่อมต่อระบบได้ ข้อมูลที่กรอกยังอยู่ กรุณาลองอีกครั้ง',
  ],
  invalidInput: ['Please check the required fields.', 'กรุณาตรวจสอบข้อมูลที่จำเป็น'],
  changed: [
    'This reservation has changed. Refresh and try again.',
    'รายการจองนี้มีการเปลี่ยนแปลง กรุณาโหลดใหม่แล้วลองอีกครั้ง',
  ],
  forbidden: [
    'You don’t have permission to perform this action.',
    'คุณไม่มีสิทธิ์ดำเนินการนี้',
  ],
  notFound: ['This item could not be found.', 'ไม่พบรายการนี้'],
  signIn: ['Please sign in to continue.', 'กรุณาเข้าสู่ระบบเพื่อดำเนินการต่อ'],
  duplicate: [
    'This submission has already been used. Please start a new reservation.',
    'คำขอนี้ถูกส่งแล้ว กรุณาเริ่มการจองใหม่',
  ],
  cancelTitle: ['Cancel this reservation?', 'ยกเลิกรายการจองนี้หรือไม่'],
  cancelBody: [
    'The room will become available for other people. This action cannot be undone.',
    'ห้องจะเปิดให้ผู้อื่นจอง การยกเลิกนี้ไม่สามารถย้อนกลับได้',
  ],
  keep: ['Keep reservation', 'เก็บรายการจองไว้'],
  cancelSeries: [
    'Cancel all upcoming meetings in this series',
    'ยกเลิกการจองในชุดนี้ที่ยังไม่สิ้นสุดทั้งหมด',
  ],
  cancelSuccess: ['Reservation cancelled.', 'ยกเลิกรายการจองแล้ว'],
  approve: ['Approve', 'อนุมัติ'],
  reject: ['Reject', 'ไม่อนุมัติ'],
  approveSuccess: ['Reservation approved.', 'อนุมัติรายการจองแล้ว'],
  rejectSuccess: ['Reservation rejected.', 'ไม่อนุมัติรายการจองแล้ว'],
  rejectTitle: ['Reject reservation', 'ไม่อนุมัติรายการจอง'],
  reason: ['Reason', 'เหตุผล'],
  reasonHint: [
    'Explain why this request cannot be approved.',
    'ระบุเหตุผลที่ไม่สามารถอนุมัติคำขอนี้ได้',
  ],
  adminIntro: [
    'Manage requests, rooms and workspace access.',
    'จัดการคำขอ ห้องประชุม และสิทธิ์เข้าใช้งาน',
  ],
  requests: ['Reservation requests', 'คำขอจองห้อง'],
  roomManagement: ['Room management', 'จัดการห้องประชุม'],
  team: ['Team access', 'สมาชิกและสิทธิ์'],
  addRoom: ['Add meeting room', 'เพิ่มห้องประชุม'],
  edit: ['Edit', 'แก้ไข'],
  editRoom: ['Edit meeting room', 'แก้ไขห้องประชุม'],
  roomName: ['Room name · English', 'ชื่อห้อง · ภาษาอังกฤษ'],
  roomNameTh: ['Room name · Thai', 'ชื่อห้อง · ภาษาไทย'],
  location: ['Location · English', 'สถานที่ · ภาษาอังกฤษ'],
  locationTh: ['Location · Thai', 'สถานที่ · ภาษาไทย'],
  requireApproval: [
    'Require approval for new reservations',
    'ต้องอนุมัติรายการจองใหม่',
  ],
  activeRoom: ['Room is available for reservations', 'เปิดให้จองห้องนี้'],
  deactivateHint: [
    'Turn off to deactivate. Rooms with upcoming reservations must be cleared first. History is preserved.',
    'ปิดเพื่อระงับการจอง ต้องจัดการรายการที่ยังไม่สิ้นสุดก่อน ประวัติจะยังคงอยู่',
  ],
  roomHasBookings: [
    'This change affects upcoming reservations. Cancel those reservations first or keep sufficient capacity.',
    'การเปลี่ยนแปลงนี้กระทบรายการจองที่ยังไม่สิ้นสุด กรุณายกเลิกรายการเหล่านั้นก่อนหรือคงความจุที่เพียงพอ',
  ],
  saveRoom: ['Save room', 'บันทึกห้อง'],
  roomSaved: ['Meeting room saved successfully.', 'บันทึกห้องประชุมสำเร็จ'],
  amenities: ['Amenities', 'อุปกรณ์'],
  display: ['Display', 'จอแสดงผล'],
  wifi: ['Wi-Fi', 'ไวไฟ'],
  whiteboard: ['Whiteboard', 'ไวท์บอร์ด'],
  projector: ['Projector', 'โปรเจกเตอร์'],
  video: ['Video conferencing', 'ระบบประชุมทางไกล'],
  audio: ['Audio system', 'ระบบเสียง'],
  computers: ['Computers', 'คอมพิวเตอร์'],
  color: ['Room color', 'สีประจำห้อง'],
  indigo: ['Indigo', 'คราม'],
  teal: ['Teal', 'เขียวน้ำทะเล'],
  blue: ['Blue', 'น้ำเงิน'],
  violet: ['Violet', 'ม่วง'],
  recent: ['Recent reservations', 'รายการจองล่าสุด'],
  usage: ['Room usage today', 'การใช้ห้องวันนี้'],
  usageHint: [
    'Confirmed hours as a share of opening hours.',
    'ชั่วโมงที่อนุมัติแล้วเทียบกับเวลาเปิดใช้งาน',
  ],
  noRequests: ['All caught up', 'ไม่มีคำขอค้างอยู่'],
  noRequestsBody: [
    'New reservation requests will appear here.',
    'คำขอจองห้องใหม่จะแสดงที่นี่',
  ],
  requester: ['Requested by', 'ผู้ขอจอง'],
  reference: ['Reference', 'เลขอ้างอิง'],
  created: ['Submitted', 'วันที่ส่งคำขอ'],
  updated: ['Last updated', 'อัปเดตล่าสุด'],
  reviewHistory: ['Decision history', 'ประวัติการพิจารณา'],
  newUser: ['Add team member', 'เพิ่มสมาชิก'],
  name: ['Full name', 'ชื่อและนามสกุล'],
  email: ['Email address', 'อีเมล'],
  password: ['Password', 'รหัสผ่าน'],
  role: ['Role', 'บทบาท'],
  user: ['Member', 'สมาชิก'],
  administrator: ['Administrator', 'ผู้ดูแลระบบ'],
  createUser: ['Create account', 'สร้างบัญชี'],
  userCreated: [
    'Team member added. Share their credentials securely.',
    'เพิ่มสมาชิกแล้ว กรุณาส่งข้อมูลเข้าสู่ระบบอย่างปลอดภัย',
  ],
  passwordLength: [
    'Use a password between 8 and 12 characters.',
    'ใช้รหัสผ่าน 8 ถึง 12 ตัวอักษร',
  ],
  invalidEmail: ['Enter a valid email address.', 'กรุณาระบุอีเมลที่ถูกต้อง'],
  emailExists: [
    'An account with this email already exists.',
    'มีบัญชีที่ใช้อีเมลนี้แล้ว',
  ],
  noNotifications: ['You’re all caught up', 'ไม่มีการแจ้งเตือนใหม่'],
  notificationsHint: [
    'Reservation updates will appear here.',
    'การอัปเดตรายการจองจะแสดงที่นี่',
  ],
  submittedEvent: ['Reservation submitted', 'ส่งรายการจองแล้ว'],
  confirmedEvent: ['Reservation approved', 'อนุมัติรายการจองแล้ว'],
  rejectedEvent: ['Reservation rejected', 'ไม่อนุมัติรายการจองแล้ว'],
  cancelledEvent: ['Reservation cancelled', 'ยกเลิกรายการจองแล้ว'],
  preferences: ['Your preferences', 'การตั้งค่าของคุณ'],
  language: ['Language', 'ภาษา'],
  languageHint: [
    'Choose the language used throughout MeetSpace.',
    'เลือกภาษาที่ใช้ในระบบ MeetSpace',
  ],
  theme: ['Appearance', 'รูปแบบการแสดงผล'],
  themeHint: [
    'Choose a comfortable theme for your workspace.',
    'เลือกรูปแบบที่เหมาะกับการใช้งานของคุณ',
  ],
  light: ['Light', 'สว่าง'],
  dark: ['Dark', 'มืด'],
  security: ['Account security', 'ความปลอดภัยของบัญชี'],
  currentPassword: ['Current password', 'รหัสผ่านปัจจุบัน'],
  newPassword: ['New password', 'รหัสผ่านใหม่'],
  changePassword: ['Change password', 'เปลี่ยนรหัสผ่าน'],
  passwordChanged: [
    'Password changed. Please sign in again.',
    'เปลี่ยนรหัสผ่านแล้ว กรุณาเข้าสู่ระบบอีกครั้ง',
  ],
  loginTitle: ['Welcome back.', 'ยินดีต้อนรับกลับมา'],
  loginBody: [
    'Sign in to your MeetSpace workspace.',
    'เข้าสู่พื้นที่ทำงาน MeetSpace ของคุณ',
  ],
  login: ['Sign in', 'เข้าสู่ระบบ'],
  remember: ['Remember me for 30 days', 'จดจำฉันเป็นเวลา 30 วัน'],
  loginHelp: [
    'Need an account? Contact your workspace administrator.',
    'หากต้องการบัญชี กรุณาติดต่อผู้ดูแลระบบ',
  ],
  invalidLogin: ['The email or password is incorrect.', 'อีเมลหรือรหัสผ่านไม่ถูกต้อง'],
  rateLimit: [
    'Too many sign-in attempts. Try again in 15 minutes.',
    'พยายามเข้าสู่ระบบหลายครั้งเกินไป กรุณาลองอีกครั้งใน 15 นาที',
  ],
  loginEyebrow: ['A SPACE FOR EVERY IDEA', 'พื้นที่สำหรับทุกความคิด'],
  loginHeadline: [
    'Good meetings start with the right space.',
    'การประชุมที่ดี เริ่มต้นจากพื้นที่ที่ใช่',
  ],
  loginArtBody: [
    'Find a room. Bring your team together. Make your next meeting count.',
    'เลือกห้อง นัดหมายทีม แล้วสร้างสรรค์การประชุมที่มีความหมาย',
  ],
  liveAvailability: ['Live availability', 'สถานะห้องแบบปัจจุบัน'],
  smartBooking: ['Simple reservations', 'จองได้สะดวก'],
  workspacePlan: ['MEETING SPACE · FLOOR PLAN', 'พื้นที่ประชุม · แผนผัง'],
  diagramNote: ['Illustrative room layout', 'ตัวอย่างแผนผังห้อง'],
  secureAccess: ['Secure workspace access', 'เข้าสู่พื้นที่ทำงานอย่างปลอดภัย'],
  account: ['Account', 'บัญชีผู้ใช้'],
  refresh: ['Refresh', 'โหลดใหม่'],
  occurrences: ['occurrences', 'ครั้ง'],
  skipped: ['Skipped', 'ข้ามแล้ว'],
  free: ['Free', 'ว่าง'],
  held: ['Held', 'กันเวลาไว้'],
  chooseDate: ['Choose a date to see the schedule.', 'เลือกวันที่เพื่อดูตาราง'],
  noAvailable: [
    'There are currently no available meeting rooms.',
    'ขณะนี้ไม่มีห้องประชุมว่าง',
  ],
  noAvailableBody: [
    'Browse the rooms to find a later time.',
    'ดูตารางห้องประชุมเพื่อเลือกเวลาอื่น',
  ],
  required: ['Required', 'จำเป็น'],
  deactivated: ['Deactivated', 'ระงับใช้งาน'],
  approval: ['Approval', 'การอนุมัติ'],
  automatic: ['Automatic', 'อัตโนมัติ'],
  manual: ['Administrator review', 'ผู้ดูแลพิจารณา'],
  capacityLabel: ['Maximum capacity', 'ความจุสูงสุด'],
  allReservations: ['All reservations', 'รายการจองทั้งหมด'],
  clear: ['Clear', 'ล้าง'],
  refreshError: ['Unable to load your workspace', 'ไม่สามารถโหลดพื้นที่ทำงานได้'],
  bookingsCount: ['reservations', 'รายการจอง'],
  roomCount: ['rooms', 'ห้อง'],
  duration: ['Duration', 'ระยะเวลา'],
  hours: ['hours', 'ชั่วโมง'],
  savedDates: ['Reserved dates', 'วันที่จอง'],
  confirmReject: ['Confirm rejection', 'ยืนยันไม่อนุมัติ'],
  countNote: [
    'Counts reflect the current time. Pending totals include future requests.',
    'จำนวนห้องอ้างอิงเวลาปัจจุบัน รายการรออนุมัติรวมคำขอในอนาคต',
  ],
  noEmail: [
    'Updates appear in MeetSpace. Email and Google Calendar are not connected.',
    'ติดตามผลได้ใน MeetSpace ขณะนี้ยังไม่ได้เชื่อมต่ออีเมลและ Google Calendar',
  ],
  meetspace: ['MeetSpace', 'MeetSpace'],
};
dictionary.availabilityTimeline = [
  'Room availability from 08:00 to 20:00',
  'เวลาว่างของห้องตั้งแต่ 08:00 ถึง 20:00',
];
dictionary.dailyAvailability = ['Daily availability', 'เวลาว่างรายวัน'];
dictionary.guestNoteTitle = ['Browsing as guest', 'กำลังใช้งานแบบผู้เยี่ยมชม'];
dictionary.guestNoteBody = [
  'Browse rooms and live availability. Sign in only when you are ready to make a reservation.',
  'ดูห้องและเวลาว่างได้ทันที เข้าสู่ระบบเมื่อคุณพร้อมจองห้อง',
];
dictionary.guestSubtitle = ['Browsing as guest', 'กำลังใช้งานแบบผู้เยี่ยมชม'];
dictionary.invalidTime = [
  'Choose a 30-minute time slot between 08:00 and 20:00. A reservation can be up to 4 hours.',
  'เลือกเวลาทีละ 30 นาที ระหว่าง 08:00–20:00 และจองต่อเนื่องได้ไม่เกิน 4 ชั่วโมง',
];
dictionary.invalidRepeat = [
  'Choose between 1 and 12 occurrences.',
  'เลือกจำนวนครั้งระหว่าง 1 ถึง 12 ครั้ง',
];
// อ่านและเก็บค่าตั้งค่าของผู้ใช้ เช่น ภาษาและธีม
function pref(k, fallback) {
  try {
    return localStorage.getItem(k) || fallback;
  } catch {
    return fallback;
  }
}
Object.assign(dictionary, {
  bookingManagement: ['Bookings', 'จัดการการจอง'],
  booker: ['Booker name or employee ID', 'ชื่อผู้จอง หรือเลขบัตรพนักงาน'],
  invalidBooker: [
    'Enter a name or employee ID (max 100 characters).',
    'กรุณาระบุชื่อหรือเลขพนักงานไม่เกิน 100 ตัวอักษร',
  ],
  walkin: ['Walk-in booking', 'จองหน้าห้อง (Walk-in)'],
  walkinIntro: [
    'Choose a room and book immediately. No sign-in required.',
    'เลือกห้องและจองได้ทันที ไม่ต้องเข้าสู่ระบบ',
  ],
  roomPhoto: ['Room photo', 'รูปห้อง'],
  uploadPhoto: [
    'Upload PNG or JPG (max 3 MB)',
    'อัปโหลด PNG หรือ JPG (ไม่เกิน 3 MB)',
  ],
  invalidImage: [
    'Choose a valid PNG or JPG, up to 3 MB and 16 megapixels.',
    'เลือกรูป PNG หรือ JPG ไม่เกิน 3 MB และ 16 ล้านพิกเซล',
  ],
  noPhoto: ['No photo', 'ไม่มีรูป'],
  walkinSaved: [
    'Your room is booked. Save this reference before closing.',
    'จองห้องสำเร็จ กรุณาจดเลขอ้างอิงก่อนปิดหน้านี้',
  ],
  roomHintBody: [
    'Choose a room and reserve your preferred time.',
    'เลือกห้องและจองเวลาที่ต้องการ',
  ],
  guestNoteBody: [
    'Browse rooms, or use Walk-in to book without an account.',
    'ดูห้องประชุม หรือใช้หน้า Walk-in เพื่อจองโดยไม่ต้องมีบัญชี',
  ],
  adminIntro: [
    'Manage rooms, photos and workspace access.',
    'จัดการห้อง รูปภาพ และสิทธิ์เข้าใช้งาน',
  ],
});
// สถานะหลักของหน้าเว็บและข้อมูลที่ใช้ระหว่างการทำงาน
const S = {
  lang: pref('meetspace.lang', 'en'),
  theme: pref('meetspace.theme', 'light'),
  view: window.location.hash === '#walkin' ? 'walkin' : 'dashboard',
  user: null,
  rooms: [],
  bookings: [],
  people: [],
  notifications: [],
  time: null,
  filter: { q: '', capacity: '', date: '', status: 'all' },
  bookingFilter: { date: '', room: '' },
  tab: 'roomManagement',
  loading: false,
  error: null,
  mobile: false,
  collapsed: false,
  authScreen: false,
  authMode: 'login',
  afterLoginView: null,
  afterLoginBook: null,
};
const t = (k, vars = {}) => {
  let s = dictionary[k]?.[S.lang === 'th' ? 1 : 0] || k;
  for (const [key, value] of Object.entries(vars))
    s = s.replaceAll('{' + key + '}', String(value));
  return s;
};
dictionary.setupRequired = [
  'Workspace setup is not complete. The site owner must configure the administrator account before sign-in is available.',
  'ยังตั้งค่าพื้นที่ทำงานไม่เสร็จ เจ้าของเว็บไซต์ต้องตั้งค่าบัญชีผู้ดูแลระบบก่อนเข้าสู่ระบบ',
];
Object.assign(dictionary, {
  setupTitle: ['Set up your workspace', 'ตั้งค่าพื้นที่ทำงานของคุณ'],
  setupIntro: [
    'Create your administrator account to start using MeetSpace.',
    'สร้างบัญชีผู้ดูแลระบบเพื่อเริ่มใช้งาน MeetSpace',
  ],
  // ข้อความสองภาษาสำหรับยืนยันเจ้าของและตั้งค่าผู้ดูแลระบบคนแรก
  // ส่วนนี้กำหนดข้อความบนหน้าเว็บ ไม่ใช่โค้ดตรวจสอบตัวตนโดยตรง
  ownerSignIn: ['Verify ownership with ChatGPT', 'ยืนยันเจ้าของเว็บไซต์ด้วย ChatGPT'],
  ownerSetupHelp: [
    'Only the site owner can create the first administrator. Sign in with the ChatGPT account that owns this site, then choose your password.',
    'เฉพาะเจ้าของเว็บไซต์เท่านั้นที่สร้างผู้ดูแลคนแรกได้ กรุณาเข้าสู่ระบบด้วยบัญชี ChatGPT ที่เป็นเจ้าของเว็บไซต์ แล้วตั้งรหัสผ่านของคุณ',
  ],
  wrongOwner: [
    'This ChatGPT account is not the site owner. Switch to the account used to create this site.',
    'บัญชี ChatGPT นี้ไม่ใช่เจ้าของเว็บไซต์ กรุณาเปลี่ยนเป็นบัญชีที่ใช้สร้างเว็บไซต์นี้',
  ],
  switchAccount: ['Switch ChatGPT account', 'เปลี่ยนบัญชี ChatGPT'],
  confirmPassword: ['Confirm password', 'ยืนยันรหัสผ่าน'],
  finishSetup: ['Create account and sign in', 'สร้างบัญชีและเข้าสู่ระบบ'],
  passwordMismatch: ['The passwords do not match.', 'รหัสผ่านทั้งสองช่องไม่ตรงกัน'],
  setupComplete: [
    'An administrator account already exists. Refresh the page and sign in.',
    'มีบัญชีผู้ดูแลระบบแล้ว กรุณาโหลดหน้าใหม่แล้วเข้าสู่ระบบ',
  ],
  setupSecure: [
    'Your password is stored as a secure hash. You only need to complete this setup once.',
    'รหัสผ่านจะถูกจัดเก็บเป็นค่าแฮชที่ปลอดภัย คุณต้องตั้งค่านี้เพียงครั้งเดียว',
  ],
});
Object.assign(dictionary, {
  signUp: ['Create an account', 'สมัครสมาชิก'],
  registerTitle: ['Join your workspace.', 'สมัครเข้าใช้งาน MeetSpace'],
  registerIntro: [
    'Use your email address and create a MeetSpace password. Gmail addresses are welcome.',
    'ใช้อีเมลของคุณ เช่น Gmail และตั้งรหัสผ่านสำหรับ MeetSpace',
  ],
  createAndEnter: ['Create account and get started', 'สมัครและเข้าใช้งานทันที'],
  alreadyMember: ['Already have an account? Sign in', 'มีบัญชีแล้ว? เข้าสู่ระบบ'],
  noAccount: ['New here? Create an account', 'ยังไม่มีบัญชี? สมัครสมาชิก'],
  memberAccess: [
    'You can reserve rooms as soon as you sign up. An administrator can grant additional permissions later.',
    'สมัครแล้วจองห้องได้ทันทีในฐานะสมาชิก ผู้ดูแลสามารถเพิ่มสิทธิ์ให้ภายหลังได้',
  ],
  registered: [
    'Your account is ready. Welcome to MeetSpace.',
    'สมัครสมาชิกสำเร็จ ยินดีต้อนรับสู่ MeetSpace',
  ],
  manageRole: ['Manage permissions', 'จัดการสิทธิ์'],
  saveRole: ['Save permissions', 'บันทึกสิทธิ์'],
  roleUpdated: ['Account permissions updated.', 'อัปเดตสิทธิ์บัญชีแล้ว'],
  roleHelp: [
    'Members can reserve rooms. Administrators can manage all rooms, reservations and team accounts.',
    'สมาชิกจองห้องได้ ส่วนผู้ดูแลระบบจัดการห้อง การจอง และบัญชีสมาชิกทั้งหมดได้',
  ],
  selfRegistered: [
    'Self-registered · email not verified',
    'สมัครด้วยตนเอง · อีเมลยังไม่ยืนยัน',
  ],
  verifyBeforeAdmin: [
    'This email was entered during registration and has not been verified. Confirm the person’s identity before granting administrator access.',
    'อีเมลนี้กรอกตอนสมัครและยังไม่ได้ยืนยันความเป็นเจ้าของ กรุณาตรวจสอบว่าเป็นบุคคลที่คุณต้องการก่อนให้สิทธิ์ผู้ดูแลระบบ',
  ],
  protectedAccount: [
    'You cannot change your own permissions or the workspace owner’s permissions.',
    'ไม่สามารถเปลี่ยนสิทธิ์ของตัวเองหรือเจ้าของพื้นที่ทำงานได้',
  ],
  accountChanged: [
    'These permissions have changed. Refresh and try again.',
    'สิทธิ์บัญชีนี้มีการเปลี่ยนแปลง กรุณาโหลดใหม่แล้วลองอีกครั้ง',
  ],
  confirmed: ['Confirmed', 'ยืนยันแล้ว'],
  confirmedEvent: ['Reservation confirmed', 'ยืนยันรายการจองแล้ว'],
  sourceMember: ['Member booking', 'จองโดยสมาชิก'],
  sourceWalkin: ['Walk-in booking', 'จองหน้าห้อง (Walk-in)'],
  alreadyEnded: [
    'This reservation has already ended and cannot be cancelled.',
    'รายการจองนี้สิ้นสุดแล้ว ไม่สามารถยกเลิกได้',
  ],
  walkinBookings: ['Walk-in bookings', 'การจองแบบ Walk-in'],
  memberBookings: ['Member bookings', 'การจองผ่านระบบสมาชิก'],
  bookingDate: ['Reservation date', 'วันที่ใช้ห้อง'],
  allRooms: ['All rooms', 'ทุกห้อง'],
  deletedRoom: ['deleted', 'ลบแล้ว'],
  showingDate: [
    'Showing reservations on {date}.',
    'แสดงรายการจองของวันที่ {date}',
  ],
  showingAllDates: [
    'Showing reservations on all dates. Pick a date to see one day.',
    'แสดงรายการจองทุกวัน เลือกวันที่ในปฏิทินเพื่อดูเฉพาะวันนั้น',
  ],
  noBookingsFound: ['No reservations', 'ไม่มีรายการจอง'],
  noBookingsFoundBody: [
    'No reservations match the selected date or room.',
    'ไม่มีรายการจองที่ตรงกับวันที่หรือห้องที่เลือก',
  ],
  deleteRoom: ['Delete', 'ลบห้อง'],
  keepRoom: ['Keep room', 'เก็บห้องไว้'],
  deleteRoomTitle: ['Delete this meeting room?', 'ลบห้องประชุมนี้หรือไม่'],
  deleteRoomBody: [
    'The room will be removed from all room lists and can no longer be reserved. Past reservation history is kept. This action cannot be undone.',
    'ห้องจะถูกนำออกจากรายการห้องทั้งหมดและไม่สามารถจองได้อีก ประวัติการจองเดิมยังเก็บไว้ การลบนี้ไม่สามารถย้อนกลับได้',
  ],
  roomDeleted: ['Meeting room deleted.', 'ลบห้องประชุมแล้ว'],
  roomHasUpcoming: [
    'This room still has upcoming reservations. Cancel them before deleting the room.',
    'ห้องนี้ยังมีการจองที่ยังไม่ถึงเวลา กรุณายกเลิกการจองเหล่านั้นก่อนลบห้อง',
  ],
  noHistoryBody: [
    'Completed and cancelled reservations stay here for reference.',
    'ดูรายการที่เสร็จสิ้นหรือยกเลิกแล้วย้อนหลังได้ที่นี่',
  ],
  countNote: ['Counts reflect the current time.', 'จำนวนห้องอ้างอิงเวลาปัจจุบัน'],
  workspaceNotReady: [
    'The owner must finish workspace setup before registration opens.',
    'เจ้าของต้องตั้งค่าพื้นที่ทำงานให้เสร็จก่อนเปิดรับสมัคร',
  ],
  signupRateLimit: [
    'Too many registration attempts. Please try again in one hour.',
    'สมัครหลายครั้งเกินไป กรุณาลองอีกครั้งในหนึ่งชั่วโมง',
  ],
  continueAsGuest: ['Back to public site', 'กลับไปดูเว็บไซต์'],
  guest: ['Guest', 'ผู้เยี่ยมชม'],
});
// สร้างฟอร์มสมัครสมาชิก ตั้งค่าพื้นที่ทำงาน และจัดการทีม
function registerForm() {
  return `<form id="register-form"><label class="field">${t('name')}<input name="name" autocomplete="name" required maxlength="100"></label><label class="field">${t('email')}<input name="email" type="email" autocomplete="username" placeholder="name@gmail.com" required maxlength="160"></label><label class="field">${t('password')}<input name="password" type="password" autocomplete="new-password" required minlength="8" maxlength="12"><small>${t('passwordLength')}</small></label><label class="field">${t('confirmPassword')}<input name="confirm" type="password" autocomplete="new-password" required minlength="8" maxlength="12"></label><p class="notice">${t('memberAccess')}</p><div class="notice error" id="form-error" role="alert" tabindex="-1"></div><button class="btn primary full" type="submit">${t('createAndEnter')}${icon('arrow')}</button></form>`;
}
function teamView() {
  return `<div class="panel"><div class="panel-head"><h2>${t('team')}</h2>${button('newUser', 'newUser', 'primary', 'plus')}</div><div class="table-wrap"><table class="table"><thead><tr><th>${t('name')}</th><th>${t('email')}</th><th>${t('role')}</th><th>${t('action')}</th></tr></thead><tbody>${S.people.map((u) => `<tr><td>${esc(u.name)}</td><td>${esc(u.email)}${u.self_registered ? `<small>${t('selfRegistered')}</small>` : ''}</td><td>${t(u.role === 'admin' ? 'administrator' : 'user')}</td><td>${u.id !== S.user.id && u.id !== 'workspace-admin' ? button('manageRole', 'manageRole', 'compact', 'shield', `data-id="${u.id}"`) : '—'}</td></tr>`).join('')}</tbody></table></div></div>`;
}
function roleForm(id) {
  const user = S.people.find((u) => u.id === id);
  if (!user) return;
  showDialog(
    'manageRole',
    `<form id="role-form" data-id="${user.id}" data-previous-role="${user.role}"><div class="modal-body"><h3>${esc(user.name)}</h3><p class="muted small" style="margin-bottom:20px">${esc(user.email)}</p><label class="field">${t('role')}<select name="role"><option value="user" ${user.role === 'user' ? 'selected' : ''}>${t('user')}</option><option value="admin" ${user.role === 'admin' ? 'selected' : ''}>${t('administrator')}</option></select><small>${t('roleHelp')}</small></label>${user.self_registered ? `<div class="notice">${t('verifyBeforeAdmin')}</div>` : ''}${errorBox()}</div><div class="modal-foot">${button('cancel', 'close')}<button class="btn primary" type="submit">${t('saveRole')}</button></div></form>`,
  );
}
function setupForm() {
  // หากระบบอนุญาตให้ตั้งค่า จะแสดงฟอร์มสร้างบัญชีผู้ดูแล
  // หากยังไม่อนุญาต จะแสดงคำแนะนำและลิงก์เข้าสู่ระบบหรือเปลี่ยนบัญชี ChatGPT
  return S.setupAllowed
    ? `<form id="setup-form"><label class="field">${t('name')}<input name="name" autocomplete="name" required maxlength="100"></label><label class="field">${t('email')}<input name="email" type="email" readonly value="${esc(S.setupEmail)}" autocomplete="username"></label><label class="field">${t('password')}<input name="password" type="password" autocomplete="new-password" required minlength="8" maxlength="12"><small>${t('passwordLength')}</small></label><label class="field">${t('confirmPassword')}<input name="confirm" type="password" autocomplete="new-password" required minlength="8" maxlength="12"></label><div id="form-error" class="notice error" tabindex="-1" role="alert"></div><button type="submit" class="btn primary full">${t('finishSetup')}${icon('arrow')}</button><p class="small muted" style="margin-top:18px">${t('setupSecure')}</p></form>`
    : `<div class="notice">${t(S.ownerSignedIn ? 'wrongOwner' : 'ownerSetupHelp')}</div><a class="btn primary full" style="margin-top:22px" href="${S.ownerSignedIn ? '/signout-with-chatgpt?return_to=%2F' : '/signin-with-chatgpt?return_to=%2F'}" target="_top">${t(S.ownerSignedIn ? 'switchAccount' : 'ownerSignIn')}${icon('arrow')}</a>`;
}
// ข้อมูล path ของ SVG และตัวช่วยสร้าง icon, button, badge
const paths = {
  grid: 'M3 3h7v7H3z M14 3h7v7h-7z M3 14h7v7H3z M14 14h7v7h-7z',
  room: 'M4 21V4l12-2v19 M16 6h4v15 M2 21h20 M12 12h.01',
  calendar: 'M4 5h16v16H4z M7 2v6 M17 2v6 M4 10h16 M8 14h2 M14 14h2 M8 18h2',
  clock: 'M12 8v5l3 2 M22 12a10 10 0 1 1-20 0 10 10 0 0 1 20 0',
  history: 'M3 11a9 9 0 1 1 2 7 M3 4v7h7 M12 7v5l3 2',
  bell: 'M5 17h14l-2-3V9a5 5 0 0 0-10 0v5z M10 21h4',
  shield: 'M12 2l8 3v7c0 5-8 10-8 10S4 17 4 12V5z M8 12l3 3 5-6',
  settings:
    'M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8 M9 2h6l1 4 4 1 2 5-3 3v5l-5 2-3-3H6l-4-4 2-4V6z',
  logout: 'M9 3H4v18h5 M12 12h10 M18 8l4 4-4 4',
  chevron: 'M9 5l7 7-7 7',
  plus: 'M12 5v14 M5 12h14',
  users:
    'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2 M9 3a4 4 0 1 0 0 8 4 4 0 0 0 0-8 M17 4a4 4 0 0 1 0 7 M22 21v-2a4 4 0 0 0-3-4',
  pin: 'M20 10c0 6-8 12-8 12S4 16 4 10a8 8 0 1 1 16 0 M15 10a3 3 0 1 1-6 0 3 3 0 0 1 6 0',
  display: 'M3 3h18v13H3z M8 21h8 M12 16v5',
  wifi: 'M2 8a16 16 0 0 1 20 0 M5 12a11 11 0 0 1 14 0 M8 16a6 6 0 0 1 8 0 M12 20h.01',
  video: 'M2 6h13v12H2z M15 10l7-4v12l-7-4',
  whiteboard: 'M3 3h18v13H3z M12 16v6 M7 22l5-6 5 6',
  projector: 'M3 5h18v12H3z M12 17v5 M8 22h8',
  audio: 'M4 9h4l5-5v16l-5-5H4z M17 8a6 6 0 0 1 0 8 M20 5a10 10 0 0 1 0 14',
  computers: 'M3 3h18v12H3z M2 19h20 M5 15l-3 4v2h20v-2l-3-4',
  check: 'M5 12l4 4L19 6',
  x: 'M6 6l12 12 M18 6L6 18',
  spark: 'M12 2l3 7 7 3-7 3-3 7-3-7-7-3 7-3z',
  arrow: 'M4 12h16 M14 6l6 6-6 6',
  menu: 'M4 6h16 M4 12h16 M4 18h16',
  left: 'M15 5l-7 7 7 7',
  search: 'M10 3a7 7 0 1 0 0 14 7 7 0 0 0 0-14 M15 15l6 6',
  edit: 'M15 4l5 5 M4 20l4-1L21 6l-4-4L4 15z',
  moon: 'M20 15A9 9 0 0 1 9 3a9 9 0 1 0 11 12',
  sun: 'M12 7a5 5 0 1 0 0 10 5 5 0 0 0 0-10 M12 1v2 M12 21v2 M1 12h2 M21 12h2 M4 4l2 2 M18 18l2 2 M4 20l2-2 M18 6l2-2',
  info: 'M12 11v6 M12 7h.01 M22 12a10 10 0 1 1-20 0 10 10 0 0 1 20 0',
  refresh:
    'M20 11a8 8 0 0 0-14-6L3 8 M3 2v6h6 M4 13a8 8 0 0 0 14 6l3-3 M21 22v-6h-6',
};
const icon = (name) =>
  `<svg class="icon" viewBox="0 0 24 24" aria-hidden="true"><path d="${paths[name] || paths.room}"/></svg>`;
const button = (label, action, kind = '', ico = '', extra = '') =>
  `<button type="button" class="btn ${kind}" data-action="${action}" ${extra}>${ico ? icon(ico) : ''}<span>${t(label)}</span></button>`;
const badge = (status) => `<span class="badge ${status}">${t(status)}</span>`;
// จัดรูปแบบวันเวลาและคำนวณเวลาท้องถิ่นของพื้นที่ทำงาน
const locale = () => (S.lang === 'th' ? 'th-TH' : 'en-GB');
const dateLabel = (d) =>
  new Intl.DateTimeFormat(locale(), {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    timeZone: 'Asia/Bangkok',
  }).format(new Date(d + 'T12:00:00+07:00'));
const instant = (s) =>
  new Intl.DateTimeFormat(locale(), {
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
    timeZone: 'Asia/Bangkok',
  }).format(new Date(s));
const mins = (t) => Number(t.slice(0, 2)) * 60 + Number(t.slice(3));
const tm = (m) =>
  `${String(Math.floor(m / 60)).padStart(2, '0')}:${String(m % 60).padStart(2, '0')}`;
function timeNow() {
  const v = Object.fromEntries(
    new Intl.DateTimeFormat('en-CA', {
      timeZone: 'Asia/Bangkok',
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hourCycle: 'h23',
    })
      .formatToParts(new Date())
      .map((x) => [x.type, x.value]),
  );
  return {
    date: `${v.year}-${v.month}-${v.day}`,
    time: `${v.hour}:${v.minute}`,
  };
}
const active = (b) => ['pending', 'confirmed'].includes(b.status);
const future = (b) => {
  const n = timeNow();
  return b.date > n.date || (b.date === n.date && b.end > n.time);
};
const shownStatus = (b) =>
  ['cancelled', 'rejected'].includes(b.status) ? 'cancelled' : 'confirmed';
const room = (id) => (S.allRooms || S.rooms).find((r) => r.id === id);
// Deleted rooms stay in allRooms so past reservations keep their names, but are hidden from every room list.
// เตรียมข้อมูลจากเซิร์ฟเวอร์และช่วยค้นหาห้อง/การจองของผู้ใช้
function applyData(data) {
  Object.assign(S, data);
  if (data.rooms) {
    S.allRooms = data.rooms;
    S.rooms = data.rooms.filter((r) => !r.deleted);
  }
}
const rn = (r) => (r ? (S.lang === 'th' ? r.name_th : r.name) : '—');
const location = (r) => (S.lang === 'th' ? r.location_th : r.location);
const mine = () =>
  S.user ? S.bookings.filter((b) => b.uid === S.user.id) : [];
function roomStatus(r, date = timeNow().date) {
  if (!r.active) return 'unavailable';
  const n = timeNow();
  if (date !== n.date) {
    const day = S.bookings.filter(
      (b) => b.rid === r.id && b.date === date && active(b),
    );
    return day.reduce((n, b) => n + mins(b.end) - mins(b.start), 0) < 720
      ? 'available'
      : 'reserved';
  }
  if (n.time < '08:00' || n.time >= '20:00') return 'closed';
  return S.bookings.some(
    (b) =>
      b.rid === r.id &&
      b.date === date &&
      active(b) &&
      b.start <= n.time &&
      b.end > n.time,
  )
    ? 'reserved'
    : 'available';
}
function availability(r, date = timeNow().date) {
  if (!r.active) return t('unavailable');
  const n = timeNow();
  let m =
    date === n.date ? Math.max(480, Math.ceil(mins(n.time) / 30) * 30) : 480;
  const bookings = S.bookings.filter(
    (b) => b.rid === r.id && b.date === date && active(b),
  );
  while (
    m < 1200 &&
    bookings.some((b) => mins(b.start) < m + 30 && mins(b.end) > m)
  )
    m += 30;
  if (m >= 1200) return date === n.date ? t('openHours') : t('dayFull');
  const next = bookings
    .filter((b) => mins(b.start) > m)
    .sort((a, b) => a.start.localeCompare(b.start))[0];
  return `${tm(m)} – ${next ? next.start : '20:00'}`;
}
function floorplan() {
  return `<svg class="floorplan" viewBox="0 0 260 125" role="img" aria-label="${t('diagramNote')}"><path d="M38 99V17h184v88H64 M38 99h26 M64 99v-23 M64 76a23 23 0 0 0-23 23"/><rect x="99" y="37" width="69" height="46" rx="11" class="tabletop"/><rect x="107" y="24" width="18" height="8" rx="3"/><rect x="144" y="24" width="18" height="8" rx="3"/><rect x="107" y="89" width="18" height="8" rx="3"/><rect x="144" y="89" width="18" height="8" rx="3"/><rect x="84" y="49" width="9" height="21" rx="3"/><rect x="175" y="49" width="9" height="21" rx="3"/><path d="M206 40v40 M210 44v32"/></svg>`;
}
function roomTimeline(r, date) {
  const bookings = S.bookings.filter(
    (b) => b.rid === r.id && b.date === date && active(b),
  );
  const slots = Array.from({ length: 24 }, (_, i) => {
    const m = 480 + i * 30,
      b = bookings.find(
        (item) => mins(item.start) < m + 30 && mins(item.end) > m,
      );
    return `<i class="${b ? 'busy' : 'free'}" title="${tm(m)}–${tm(m + 30)} · ${t(b ? 'reserved' : 'free')}"></i>`;
  }).join('');
  return `<div class="room-timeline" role="img" aria-label="${t('availabilityTimeline')}"><div class="timeline-head"><strong>${t('dailyAvailability')}</strong><span class="timeline-legend"><span><b class="free"></b>${t('available')}</span><span><b class="busy"></b>${t('reserved')}</span></span></div><div class="timeline-slots">${slots}</div><div class="timeline-labels"><span>08:00</span><span>12:00</span><span>16:00</span><span>20:00</span></div></div>`;
}
function langSelect() {
  return `<select class="lang" aria-label="${t('language')}" data-setting="lang"><option value="en" ${S.lang === 'en' ? 'selected' : ''}>EN · English</option><option value="th" ${S.lang === 'th' ? 'selected' : ''}>TH · ไทย</option></select>`;
}
const logo = () =>
  `<div class="logo"><img src="/favicon.svg" alt=""><span>MeetSpace</span></div>`;
// เรียก API ของระบบ พร้อมแปลงคำตอบและข้อผิดพลาดให้อยู่ในรูปแบบเดียวกัน
async function api(path, body) {
  try {
    const res = await fetch('/api/' + path, {
      method: body ? 'POST' : 'GET',
      headers: body ? { 'Content-Type': 'application/json' } : {},
      body: body ? JSON.stringify(body) : undefined,
    });
    let data;
    try {
      data = await res.json();
    } catch {
      throw { error: 'serviceUnavailable' };
    }
    if (!res.ok) throw data;
    return data;
  } catch (e) {
    if (e.error) throw e;
    throw { error: 'serviceUnavailable' };
  }
}
function message(e) {
  return t(
    e.error === 'unavailable'
      ? 'unavailableError'
      : e.error || 'serviceUnavailable',
    e,
  );
}
function toast(k, bad = false) {
  const el = document.createElement('div');
  el.className = 'toast' + (bad ? ' bad' : '');
  el.innerHTML =
    icon(bad ? 'info' : 'check') +
    `<span>${esc(dictionary[k] ? t(k) : k)}</span>`;
  $('#toasts').append(el);
  setTimeout(() => el.remove(), 5000);
}
function formError(e) {
  const box = $('#form-error');
  if (box) {
    box.textContent = message(e);
    box.hidden = false;
    box.focus();
  } else toast(message(e), true);
}
function setBusy(form, busy) {
  form.querySelectorAll('button[type=submit]').forEach((b) => {
    if (busy) {
      b.dataset.original = b.innerHTML;
      b.innerHTML = `<span class="spinner"></span>${t('saving')}`;
    } else b.innerHTML = b.dataset.original || b.innerHTML;
    b.disabled = busy;
  });
  form.setAttribute('aria-busy', busy);
}
// โหลดข้อมูลล่าสุดจากเซิร์ฟเวอร์แล้ววาดหน้าปัจจุบันใหม่
async function refresh(show = false) {
  if (show) {
    S.loading = true;
    render();
  }
  try {
    applyData(await api(S.view === 'walkin' ? 'walk-in/data' : 'data'));
    S.error = null;
  } catch (e) {
    if (e.error === 'signIn') {
      S.user = null;
      if ($('#dialog').open) closeDialog();
    } else S.error = e;
  }
  S.loading = false;
  render();
}
// ตรวจสิทธิ์และควบคุมการเปลี่ยนหน้าของระบบ
const protectedViews = new Set([
  'my',
  'history',
  'notifications',
  'settings',
  'admin',
]);
function requireSignIn(view = 'dashboard', bookId = null) {
  S.afterLoginView = view;
  S.afterLoginBook = bookId;
  S.authMode = 'login';
  S.authScreen = true;
  S.mobile = false;
  render();
  window.scrollTo(0, 0);
}
async function navigate(view) {
  const kioskChange = S.view === 'walkin' || view === 'walkin';
  if (!S.user && protectedViews.has(view)) {
    requireSignIn(view);
    return;
  }
  if (S.view !== view) S.filter.date = '';
  S.view = view;
  window.history.replaceState(
    null,
    '',
    view === 'walkin' ? '#walkin' : window.location.pathname,
  );
  S.mobile = false;
  if (kioskChange) await refresh(true);
  else render();
  window.scrollTo(0, 0);
}
// สร้างหน้าจอเข้าสู่ระบบและโครงหน้าหลักพร้อมเมนูนำทาง
function loginView() {
  return `<div class="login"><section class="login-left"><div class="login-top">${logo()}<div class="login-tools">${langSelect()}<button type="button" class="btn icon-only" data-action="theme" aria-label="${t(S.theme === 'light' ? 'dark' : 'light')}" title="${t(S.theme === 'light' ? 'dark' : 'light')}">${icon(S.theme === 'light' ? 'moon' : 'sun')}</button></div></div><main class="login-form" id="main"><h1>${t(S.setupRequired ? 'setupTitle' : S.authMode === 'register' ? 'registerTitle' : 'loginTitle')}</h1><p>${t(S.setupRequired ? 'setupIntro' : S.authMode === 'register' ? 'registerIntro' : 'loginBody')}</p>${S.setupRequired ? setupForm() : S.authMode === 'register' ? registerForm() : `<form id="login-form"> <label class="field">${t('email')}<input type="email" name="email" autocomplete="username" required maxlength="160" placeholder="name@company.com"></label><label class="field">${t('password')}<input type="password" name="password" autocomplete="current-password" required minlength="8" maxlength="12"></label><label class="check"><input type="checkbox" name="remember">${t('remember')}</label><div class="notice error" id="form-error" tabindex="-1" role="alert"></div><button type="submit" class="btn primary full">${t('login')}${icon('arrow')}</button></form>`}${!S.setupRequired ? button(S.authMode === 'register' ? 'alreadyMember' : 'noAccount', S.authMode === 'register' ? 'showLogin' : 'showRegister', 'text full') + button('continueAsGuest', 'guestHome', 'text full') : ''}</main><div class="login-foot">© ${new Date().getFullYear()} MeetSpace · ${t('secureAccess')}</div></section><aside class="login-art"><div class="art-content"><span class="eyebrow">${t('loginEyebrow')}</span><h2>${t('loginHeadline')}</h2><p>${t('loginArtBody')}</p><div class="workspace-diagram"><div class="row between"><span class="eyebrow">${t('workspacePlan')}</span>${icon('room')}</div>${floorplan()}<div class="row between"><span>${t('diagramNote')}</span>${icon('users')}</div></div></div><div class="art-bottom">MEETSPACE / ${t('workspace')}</div></aside></div>`;
}
function shell(content) {
  if (S.view === 'walkin')
    return `<div class="kiosk"><header class="topbar">${logo()}<div class="spacer"></div>${langSelect()}</header><main id="main">${content}<p class="footer-note">${t('localTime')}</p></main></div>`;
  const links = [
    ['dashboard', 'grid'],
    ['rooms', 'room'],
    ['my', 'calendar'],
    ['history', 'history'],
    ['notifications', 'bell'],
  ];
  const manage = S.user
    ? `${S.user.role === 'admin' ? `<button data-nav="admin" class="${S.view === 'admin' ? 'active' : ''}">${icon('shield')}<span>${t('admin')}</span></button>` : ''}<button data-nav="settings" class="${S.view === 'settings' ? 'active' : ''}">${icon('settings')}<span>${t('settings')}</span></button>`
    : `<button data-nav="settings">${icon('settings')}<span>${t('settings')}</span></button>`;
  const sideNote = S.user
    ? `<div class="workspace-note">${icon('clock')}<p class="small"><strong>${t('openHours')}</strong></p><p class="small muted">${t('localTime')}</p></div>`
    : `<div class="workspace-note guest-note">${icon('info')}<p class="small"><strong>${t('guestNoteTitle')}</strong></p><p class="small muted">${t('guestNoteBody')}</p></div>`;
  const profile = S.user
    ? `<div class="profile"><div class="avatar">${esc(S.user.name.slice(0, 2).toUpperCase())}</div><div class="identity"><strong title="${esc(S.user.name)}">${esc(S.user.name)}</strong><small class="muted">${t(S.user.role === 'admin' ? 'administrator' : 'user')}</small></div><button class="btn text icon-only" data-action="logout" aria-label="${t('logout')}" title="${t('logout')}">${icon('logout')}</button></div>`
    : `<div class="profile"><div class="avatar">GU</div><div class="identity"><strong>${t('guest')}</strong><small class="muted">${t('guestSubtitle')}</small></div><button class="btn text icon-only" data-action="showLogin" aria-label="${t('login')}" title="${t('login')}">${icon('arrow')}</button></div>`;
  const classes = [
    'shell',
    `view-${S.view}`,
    !S.user ? 'guest-shell' : '',
    S.mobile ? 'mobile-open' : '',
    S.collapsed ? 'collapsed' : '',
  ]
    .filter(Boolean)
    .join(' ');
  return `<div class="${classes}"><aside class="sidebar"><div class="sidebar-head"><button class="btn icon-only collapse-btn desktop-only" data-action="collapse" aria-label="${t(S.collapsed ? 'expand' : 'collapse')}" title="${t(S.collapsed ? 'expand' : 'collapse')}">${icon('menu')}</button>${logo()}</div><div class="nav-label">${t('menu')}</div><nav class="nav" aria-label="${t('menu')}">${links.map(([name, ico]) => `<button data-nav="${name}" class="${S.view === name ? 'active' : ''}" ${S.view === name ? 'aria-current="page"' : ''} title="${t(name)}">${icon(ico)}<span>${t(name)}</span></button>`).join('')}</nav><div class="nav-label" style="margin-top:30px">${t('manage')}</div><nav class="nav" aria-label="${t('manage')}">${manage}</nav><div class="sidebar-bottom">${sideNote}${profile}</div></aside><div><header class="topbar"><button class="btn icon-only mobile-menu" data-action="menu" aria-label="${t('toggleMenu')}">${icon('menu')}</button><div class="breadcrumb"><span>${t(['admin', 'settings'].includes(S.view) ? 'manageCrumb' : 'workspace')} &nbsp; / &nbsp; </span><b>${t(S.view)}</b></div><div class="spacer"></div><span class="date">${dateLabel(timeNow().date)}</span>${langSelect()}<button class="btn icon-only" data-action="theme" aria-label="${t(S.theme === 'light' ? 'dark' : 'light')}">${icon(S.theme === 'light' ? 'moon' : 'sun')}</button>${S.user ? `<button class="btn icon-only" data-nav="notifications" aria-label="${t('notifications')}">${icon('bell')}</button>` : `<button class="btn primary compact" data-action="showLogin"><span>${t('login')}</span></button>`}</header><main id="main">${content}<p class="footer-note">${t('localTime')}</p></main></div></div>`;
}
// สร้างส่วนหัวและเนื้อหาของหน้าต่าง ๆ เช่น ห้องและการจอง
function heading(title, desc, action = '') {
  return `<div class="page-heading"><div><h1>${t(title)}</h1>${desc ? `<p>${t(desc)}</p>` : ''}</div>${action}</div>`;
}
function empty(title, body, action = '') {
  return `<div class="empty"><div class="empty-icon">${icon('calendar')}</div><h3>${t(title)}</h3><p>${t(body)}</p>${action}</div>`;
}
function stats(admin = false) {
  const n = timeNow();
  const values = [
    ['totalRooms', S.rooms.length, 'room', 'acrossWorkspace', ''],
    [
      'availableNow',
      S.rooms.filter((r) => roomStatus(r) === 'available').length,
      'room',
      'readyToBook',
      'green',
    ],
    [
      admin ? 'reserved' : 'upcomingCount',
      admin
        ? S.rooms.filter((r) => roomStatus(r) === 'reserved').length
        : mine().filter((b) => active(b) && future(b)).length,
      'calendar',
      'onYourCalendar',
      '',
    ],
  ];
  return `<div class="stats">${values
    .slice(0, 3)
    .map(
      ([label, value, ico, hint, color]) =>
        `<div class="stat ${color}"><div class="row between"><span class="stat-icon">${icon(ico)}</span></div><div class="stat-label">${t(label)}</div><div class="stat-bottom"><strong>${value}</strong><span class="hint">${t(hint)}</span></div></div>`,
    )
    .join('')}</div>`;
}
function roomCard(r) {
  const date = ['rooms', 'walkin'].includes(S.view)
    ? S.filter.date || timeNow().date
    : timeNow().date;
  const liveStatus = roomStatus(r, date),
    status = liveStatus;
  const photo = r.image
    ? `<img class="room-photo" src="${esc(r.image)}" alt="${esc(rn(r))}" loading="lazy" onerror="this.remove()"><span class="room-photo-shade"></span>`
    : floorplan();
  return `<article class="room-card ${!r.active ? 'inactive' : 'clickable'}" ${r.active ? `data-card-book="${esc(r.id)}"` : ''}><div class="room-visual ${r.color} ${r.image ? 'has-photo' : ''}"><span class="room-code">${esc(r.id)}</span>${badge(status)}${photo}</div><div class="room-content"><h3>${esc(rn(r))}</h3><div class="location">${icon('pin')}${esc(location(r))}</div><div class="room-meta"><span class="row" style="gap:7px">${icon('users')}${r.capacity} ${t('people')}</span><div class="amenities">${r.amenities
    .split(',')
    .filter(Boolean)
    .slice(0, 3)
    .map((a) => `<span title="${t(a)}" aria-label="${t(a)}">${icon(a)}</span>`)
    .join(
      '',
    )}</div></div>${roomTimeline(r, date)}<div class="room-footer"><span class="muted">${availability(r, date)}</span>${button(r.active ? 'book' : 'unavailable', 'book', '', r.active ? 'arrow' : '', `data-id="${r.id}" ${!r.active ? 'disabled' : ''}`)}</div></div></article>`;
}
function upcomingList(bookings) {
  if (!bookings.length)
    return empty(
      'noUpcoming',
      'noUpcomingBody',
      button('explore', 'rooms', 'primary', 'plus'),
    );
  return bookings
    .map((b) => {
      const d = new Date(b.date + 'T12:00:00+07:00');
      return `<div class="upcoming-item"><div class="date-tile">${new Intl.DateTimeFormat(locale(), { month: 'short', timeZone: 'Asia/Bangkok' }).format(d)}<strong>${d.getUTCDate()}</strong></div><div><button class="btn text compact title" style="padding:0;min-height:0;text-align:left;white-space:normal" data-action="details" data-id="${b.id}">${esc(b.title)}</button><div class="sub">${esc(rn(room(b.rid)))} · ${b.start} – ${b.end}</div></div>${badge(shownStatus(b))}</div>`;
    })
    .join('');
}
function dashboard() {
  const n = timeNow(),
    available = S.rooms.filter((r) => roomStatus(r) === 'available'),
    counts = ['available', 'reserved', 'unavailable', 'closed'].map((k) => [
      k,
      S.rooms.filter((r) => roomStatus(r) === k).length,
    ]);
  return (
    heading(
      'welcome',
      'dashboardIntro',
      button('newReservation', 'rooms', 'primary', 'plus'),
    ) +
    stats() +
    `<div class="dashboard-main"><section>${recSection()}<div class="section-head"><h2>${t('history')}</h2>${button('viewAll', 'history', 'text compact', 'arrow')}</div><div class="panel">${reservationTable(
      mine()
        .filter((b) => !active(b) || !future(b))
        .slice(-3)
        .reverse(),
      false,
      true,
    )}</div></section></div>`
  );
}
function filters() {
  return `<div class="filterbar"><label class="search">${t('searchRooms')}<input type="search" data-filter="q" value="${esc(S.filter.q)}" placeholder="${t('searchRooms')}"></label><label>${t('date')}<input type="date" data-filter="date" min="${timeNow().date}" value="${S.filter.date || timeNow().date}"></label>${button('clear', 'clearFilters', 'filter-clear', 'refresh')}</div>`;
}
function roomsView() {
  const f = S.filter,
    rooms = S.rooms.filter(
      (r) =>
        (rn(r) + ' ' + location(r) + ' ' + r.id)
          .toLowerCase()
          .includes(f.q.toLowerCase()) &&
        r.capacity >= Number(f.capacity || 0) &&
        (f.status === 'all' ||
          roomStatus(r, f.date || timeNow().date) === f.status),
    );
  return (
    heading(
      S.view === 'walkin' ? 'walkin' : 'rooms',
      S.view === 'walkin' ? 'walkinIntro' : 'findRoomIntro',
    ) +
    filters() +
    `<div class="row between small muted" style="margin-bottom:15px"><span>${rooms.length} ${t('roomCount')}</span><span>${t('openHours')}</span></div><div class="room-grid">${rooms.map(roomCard).join('')}</div>${!rooms.length ? empty('noRooms', 'noRoomsBody', button('clearFilters', 'clearFilters', 'primary')) : ''}`
  );
}
function reservationTable(bookings, admin = false, history = false) {
  if (!bookings.length)
    return empty(
      history ? 'noHistory' : admin ? 'noRequests' : 'noReservations',
      history ? 'noHistoryBody' : admin ? 'noRequestsBody' : 'noUpcomingBody',
      admin || history ? '' : button('explore', 'rooms', 'primary'),
    );
  return `<div class="table-wrap"><table class="table"><thead><tr><th>${t('room')} / ${t('meeting')}</th><th>${t('date')} / ${t('time')}</th><th>${t('action')}</th></tr></thead><tbody>${bookings.map((b) => `<tr><td><strong>${esc(rn(room(b.rid)))}</strong><small>${esc(b.title)}</small>${admin ? `<small>${esc(b.booker || S.people.find((u) => u.id === b.uid)?.name || 'Walk-in')} · ${t(b.source === 'Walk-in' || b.uid === 'walk-in' ? 'sourceWalkin' : 'sourceMember')}</small>` : ''}<div style="margin-top:6px">${badge(shownStatus(b))}</div></td><td style="white-space:nowrap">${dateLabel(b.date)}<small>${b.start} – ${b.end}</small></td><td><div class="actions">${button('details', 'details', '', '', `data-id="${b.id}"`)}${active(b) && future(b) ? button('cancel', 'cancel', 'danger', '', `data-id="${b.id}"`) : ''}</div></td></tr>`).join('')}</tbody></table></div>`;
}
function reservationsView(history = false) {
  const list = mine().filter((b) =>
    history ? !active(b) || !future(b) : active(b) && future(b),
  );
  return (
    heading(
      history ? 'history' : 'my',
      '',
      button('newReservation', 'rooms', 'primary', 'plus'),
    ) +
    `<div class="panel">${reservationTable(history ? list.reverse() : list, false, history)}</div>`
  );
}
function isWalkIn(b) {
  return b.source === 'Walk-in' || b.uid === 'walk-in';
}
// Admin Bookings tab: filter by date (calendar) and room, then split walk-in and member reservations side by side.
function bookingManagementView() {
  const f = S.bookingFilter,
    rooms = S.allRooms || S.rooms;
  const list = S.bookings
    .filter(
      (b) => (!f.date || b.date === f.date) && (!f.room || b.rid === f.room),
    )
    .sort(
      f.date
        ? (a, b) => a.start.localeCompare(b.start) || a.rid.localeCompare(b.rid)
        : (a, b) => (b.created || '').localeCompare(a.created || ''),
    );
  const side = (key, items) =>
    `<div class="panel"><div class="panel-head"><h2>${t(key)}</h2><span class="muted small">${items.length} ${t('bookingsCount')}</span></div>${items.length ? reservationTable(items, true, true) : empty('noBookingsFound', 'noBookingsFoundBody')}</div>`;
  const filters = `<div class="filterbar booking-filters"><label>${t('bookingDate')}<input type="date" data-bfilter="date" value="${esc(f.date)}"></label><label>${t('room')}<select data-bfilter="room"><option value="">${t('allRooms')}</option>${rooms.map((r) => `<option value="${esc(r.id)}" ${f.room === r.id ? 'selected' : ''}>${esc(rn(r))} · ${esc(r.id)}${r.deleted ? ' (' + t('deletedRoom') + ')' : ''}</option>`).join('')}</select></label>${button('clear', 'clearBookingFilters', 'text compact')}${button('refresh', 'refresh', 'compact', 'refresh')}</div>`;
  const hint = `<p class="small muted" style="margin:-10px 0 18px">${f.date ? t('showingDate', { date: dateLabel(f.date) }) : t('showingAllDates')}</p>`;
  return (
    filters +
    hint +
    `<div class="booking-split">${side('walkinBookings', list.filter(isWalkIn))}${side(
      'memberBookings',
      list.filter((b) => !isWalkIn(b)),
    )}</div>`
  );
}
// สร้างหน้าผู้ดูแล รวมการจอง ห้อง ทีม และสถิติการใช้งาน
function adminView() {
  if (S.user.role !== 'admin') return empty('forbidden', '');
  let content = '';
  if (S.tab === 'bookingManagement') content = bookingManagementView();
  if (S.tab === 'roomManagement')
    content = `<div class="panel"><div class="panel-head"><h2>${t('roomManagement')}</h2>${button('addRoom', 'addRoom', 'primary', 'plus')}</div><div class="table-wrap"><table class="table"><thead><tr><th>${t('room')}</th><th>${t('seats')}</th><th>${t('action')}</th></tr></thead><tbody>${S.rooms.map((r) => `<tr><td><strong>${esc(rn(r))}</strong><small>${esc(location(r))} · ${esc(r.id)}</small></td><td>${r.capacity} ${t('people')}</td><td><div class="actions">${button('edit', 'editRoom', 'compact', 'edit', `data-id="${r.id}"`)}${button('deleteRoom', 'deleteRoom', 'compact danger', 'x', `data-id="${r.id}"`)}</div></td></tr>`).join('')}</tbody></table></div></div>`;
  if (S.tab === 'team') content = teamView();
  return (
    heading('admin', 'adminIntro') +
    stats(true) +
    `<div class="tabs" role="tablist">${['bookingManagement', 'roomManagement', 'team'].map((k) => `<button role="tab" aria-selected="${S.tab === k}" data-tab="${k}" class="${S.tab === k ? 'active' : ''}">${t(k)}</button>`).join('')}</div>${content}<div class="section-head"><div><h2>${t('usage')}</h2><p>${t('usageHint')}</p></div></div><div class="panel panel-body">${S.rooms
      .map((r) => {
        const used = S.bookings
          .filter(
            (b) =>
              b.rid === r.id &&
              b.date === timeNow().date &&
              b.status === 'confirmed',
          )
          .reduce((n, b) => n + mins(b.end) - mins(b.start), 0);
        return `<div class="usage-row"><div class="row between"><span>${esc(rn(r))}</span><b>${(used / 60).toFixed(1)} ${t('hours')}</b></div><div class="bar"><i style="width:${Math.min(100, (used / 720) * 100)}%"></i></div></div>`;
      })
      .join('')}</div>`
  );
}
function notificationsView() {
  return (
    heading('notifications', '') +
    `<div class="panel">${
      S.notifications.length
        ? S.notifications
            .map((n) => {
              const b = S.bookings.find((x) => x.id === n.bid);
              return `<div class="notification">${icon(n.kind === 'confirmed' ? 'check' : 'x')}<div><p><strong>${t((n.kind === 'confirmed' ? 'confirmed' : 'cancelled') + 'Event')}</strong></p><p>${esc(b?.title || '')} · ${esc(rn(room(b?.rid)))}</p><small>${instant(n.at)}</small></div><div class="spacer"></div>${b ? button('details', 'details', 'compact', '', `data-id="${b.id}"`) : ''}</div>`;
            })
            .join('')
        : empty('noNotifications', 'notificationsHint')
    }</div>`
  );
}
function settingsView() {
  return (
    heading('settings', 'preferences') +
    `<div class="settings stack"><div class="panel panel-body"><div class="settings-row"><div><h3>${t('language')}</h3><p>${t('languageHint')}</p></div>${langSelect()}</div><div class="settings-row"><div><h3>${t('theme')}</h3><p>${t('themeHint')}</p></div><select class="lang" data-setting="theme" aria-label="${t('theme')}">${['light', 'dark'].map((k) => `<option ${S.theme === k ? 'selected' : ''} value="${k}">${t(k)}</option>`).join('')}</select></div><div class="settings-row"><div><h3>${t('account')}</h3><p>${esc(S.user.email)} · ${t(S.user.role === 'admin' ? 'administrator' : 'user')}</p></div>${button('logout', 'logout', '', 'logout')}</div></div><div class="panel"><div class="panel-head"><h2>${t('security')}</h2></div><form class="panel-body" id="password-form"><label class="field">${t('currentPassword')}<input type="password" name="current" autocomplete="current-password" required minlength="8" maxlength="12"></label><label class="field">${t('newPassword')}<input type="password" name="password" autocomplete="new-password" required minlength="8" maxlength="12"><small>${t('passwordLength')}</small></label><div id="form-error" class="notice error" tabindex="-1" role="alert"></div><button class="btn primary" type="submit" style="margin-top:16px">${t('changePassword')}</button></form></div><div class="notice">${t('noEmail')}</div></div>`
  );
}
// เลือกหน้าตามสถานะ แล้วนำเนื้อหาไปแสดงในแอป
function render() {
  document.documentElement.lang = S.lang;
  document.body.classList.toggle('dark', S.theme === 'dark');
  if (!S.user && S.authScreen) {
    $('#app').innerHTML = loginView();
    return;
  }
  if (!S.user && protectedViews.has(S.view)) S.view = 'dashboard';
  let content;
  if (S.loading)
    content =
      heading(S.view, '') +
      `<div class="skeletons" aria-label="${t('loading')}"><div class="skeleton"></div><div class="skeleton"></div><div class="skeleton"></div></div>`;
  else if (S.error)
    content =
      heading('refreshError', '') +
      `<div class="notice error" role="alert">${message(S.error)}</div>` +
      button('retry', 'refresh', 'primary');
  else
    content = (
      {
        dashboard,
        rooms: roomsView,
        walkin: roomsView,
        my: () => reservationsView(),
        history: () => reservationsView(true),
        admin: adminView,
        notifications: notificationsView,
        settings: settingsView,
      }[S.view] || dashboard
    )();
  $('#app').innerHTML = shell(content);
  recLayout();
}
// จัดการ dialog กลางหน้าจอและคืน focus เมื่อปิด
let lastFocus = null,
  draft = null,
  sending = false;
function showDialog(title, body, foot = '', subtitle = '') {
  const d = $('#dialog');
  if (!d.open) lastFocus = document.activeElement;
  d.innerHTML = `<div class="modal-head"><div><h2 id="dialog-title">${t(title)}</h2>${subtitle ? `<p>${esc(subtitle)}</p>` : ''}</div><button class="btn text icon-only" data-action="close" aria-label="${t('close')}">${icon('x')}</button></div>${body}${foot ? `<div class="modal-foot">${foot}</div>` : ''}`;
  d.setAttribute('aria-labelledby', 'dialog-title');
  if (!d.open) d.showModal();
  else d.querySelector('input,button')?.focus();
}
function closeDialog() {
  if (sending) return;
  $('#dialog').close();
  draft = null;
  lastFocus?.focus();
}
const errorBox = () =>
  '<div class="notice error" id="form-error" role="alert" tabindex="-1"></div>';
// ช่วยจองซ้ำรายสัปดาห์ ตรวจเวลาชน และสร้างขั้นตอนการจอง
function dateSeries(date, count) {
  return Array.from({ length: count }, (_, i) => {
    const d = new Date(date + 'T12:00:00Z');
    d.setUTCDate(d.getUTCDate() + 7 * i);
    return d.toISOString().slice(0, 10);
  });
}
const conflict = (rid, date, start, end) =>
  S.bookings.some(
    (b) =>
      b.rid === rid &&
      b.date === date &&
      active(b) &&
      b.start < end &&
      b.end > start,
  );
function openBooking(id) {
  if (!S.user && S.view !== 'walkin') {
    requireSignIn('rooms', id);
    return;
  }
  const r = room(id),
    n = timeNow();
  let date = n.date;
  let start = 480;
  if (date === n.date)
    start = Math.max(480, Math.ceil((mins(n.time) + 1) / 30) * 30);
  if (start > 1140) {
    const d = new Date(n.date + 'T12:00:00Z');
    d.setUTCDate(d.getUTCDate() + 1);
    date = d.toISOString().slice(0, 10);
    start = 540;
  }
  draft = {
    rid: id,
    date,
    start: tm(start),
    end: tm(start + 60),
    title: '',
    repeat: 1,
    skip: false,
    key: crypto.randomUUID(),
  };
  bookingForm();
}
function bookingForm() {
  const r = room(draft.rid);
  const options = (value, end = false) =>
    Array.from({ length: 24 }, (_, i) => 480 + i * 30 + (end ? 30 : 0))
      .map(
        (m) =>
          `<option value="${tm(m)}" ${tm(m) === value ? 'selected' : ''}>${tm(m)}</option>`,
      )
      .join('');
  showDialog(
    'newReservation',
    `<form id="booking-form"><div class="modal-body"><div class="steps"><b>1</b>${t('bookStep')}<i></i><span>2 · ${t('reviewStep')}</span></div><div class="row between" style="margin-bottom:18px"><div><h3>${esc(rn(r))}</h3><p class="small muted">${esc(location(r))}</p></div><span class="badge available">${r.capacity} ${t('people')}</span></div>${S.view === 'walkin' ? `<label class="field">${t('booker')}<input name="booker" required maxlength="100" autocomplete="off" value="${esc(draft.booker || '')}"></label>` : ''}<label class="field">${t('date')}<input name="date" type="date" required min="${timeNow().date}" value="${draft.date}"></label><div id="booking-schedule"></div><div class="fields"><label class="field">${t('start')}<select name="start">${options(draft.start)}</select></label><label class="field">${t('end')}<select name="end">${options(draft.end, true)}</select></label></div><div class="notice">${t('autoNote')}</div><div id="repeat-preview"></div>${errorBox()}</div><div class="modal-foot">${button('cancel', 'close')}<button class="btn primary" type="submit">${t('next')}${icon('arrow')}</button></div></form>`,
  );
  updateBookingVisuals();
}
function captureDraft() {
  const form = $('#booking-form');
  if (!form) return;
  const f = new FormData(form);
  Object.assign(draft, Object.fromEntries(f), { repeat: 1, skip: false });
}
function updateBookingVisuals() {
  captureDraft();
  if (!draft.date) return;
  $('#booking-schedule').innerHTML = `<div class="schedule">${Array.from(
    { length: 24 },
    (_, i) => {
      const m = 480 + i * 30,
        b = S.bookings.find(
          (b) =>
            b.rid === draft.rid &&
            b.date === draft.date &&
            active(b) &&
            mins(b.start) < m + 30 &&
            mins(b.end) > m,
        );
      return `<span class="${b ? 'busy' : m >= mins(draft.start) && m < mins(draft.end) ? 'selected' : ''}" title="${tm(m)}–${tm(m + 30)} · ${t(b ? 'reserved' : 'free')}"></span>`;
    },
  ).join(
    '',
  )}</div><div class="schedule-labels"><span>08:00</span><span>12:00</span><span>16:00</span><span>20:00</span></div>`;
  try {
    $('#repeat-preview').innerHTML =
      draft.repeat > 1
        ? `<div class="preview-list">${dateSeries(draft.date, draft.repeat)
            .map(
              (date) =>
                `<div><span>${dateLabel(date)}</span>${badge(conflict(draft.rid, date, draft.start, draft.end) ? 'reserved' : 'available')}</div>`,
            )
            .join('')}</div>`
        : '';
  } catch {}
}
function validateDraft() {
  const r = room(draft.rid),
    n = timeNow();
  if (
    S.view === 'walkin' &&
    (!draft.booker?.trim() || draft.booker.length > 100)
  )
    return { error: 'invalidBooker' };
  if (!draft.date) return { error: 'invalidDate' };
  if (draft.start >= draft.end || mins(draft.end) - mins(draft.start) > 240)
    return { error: 'invalidTime' };
  if (draft.repeat < 1 || draft.repeat > 12) return { error: 'invalidRepeat' };
  if (draft.date < n.date || (draft.date === n.date && draft.start <= n.time))
    return { error: 'past' };
  const conflicts = dateSeries(draft.date, draft.repeat).filter((date) =>
    conflict(draft.rid, date, draft.start, draft.end),
  );
  if ((conflicts.length && !draft.skip) || conflicts.length === draft.repeat)
    return { error: 'conflict' };
  return null;
}
function reviewBooking() {
  captureDraft();
  const error = validateDraft();
  if (error) {
    formError(error);
    return;
  }
  const r = room(draft.rid),
    dates = dateSeries(draft.date, draft.repeat),
    free = dates.filter(
      (date) => !conflict(draft.rid, date, draft.start, draft.end),
    );
  showDialog(
    'summary',
    `<form id="confirm-form"><div class="modal-body"><div class="steps"><span>1 · ${t('bookStep')}</span><i></i><b>2</b>${t('reviewStep')}</div><p class="muted small">${t('checkDetails')}</p><dl class="summary"><dt>${t('room')}</dt><dd><strong>${esc(rn(r))}</strong></dd>${S.view === 'walkin' ? `<dt>${t('booker')}</dt><dd>${esc(draft.booker)}</dd>` : ''}<dt>${t('time')}</dt><dd>${draft.start} – ${draft.end}</dd><dt>${t('savedDates')}</dt><dd>${free.map(dateLabel).join('<br>')}</dd>${free.length < dates.length ? `<dt>${t('skipped')}</dt><dd>${dates.length - free.length}</dd>` : ''}</dl><div class="notice" style="margin-top:20px">${t('noEmail')}</div>${errorBox()}</div><div class="modal-foot">${button('back', 'bookingBack')}<button class="btn primary" type="submit">${icon('check')}${t('confirm')}</button></div></form>`,
  );
}
// แสดงรายละเอียด/ยกเลิกการจอง และฟอร์มจัดการห้องกับผู้ใช้
function details(id) {
  const b = S.bookings.find((x) => x.id === id),
    r = room(b.rid),
    u = S.people.find((x) => x.id === b.uid) || S.user;
  showDialog(
    'details',
    `<div class="modal-body">${badge(shownStatus(b))}<h3 style="margin-top:16px">${esc(b.title)}</h3><dl class="summary"><dt>${t('room')}</dt><dd>${esc(rn(r))}</dd><dt>${t('date')}</dt><dd>${dateLabel(b.date)}</dd><dt>${t('time')}</dt><dd>${b.start} – ${b.end}</dd><dt>${t('requester')}</dt><dd>${esc(u.name)}</dd><dt>${t('reference')}</dt><dd>${b.id.slice(0, 8).toUpperCase()}</dd><dt>${t('created')}</dt><dd>${instant(b.created)}</dd><dt>${t('updated')}</dt><dd>${instant(b.updated)}</dd></dl></div>`,
    button('close', 'close'),
  );
}
function cancelDialog(id) {
  const b = S.bookings.find((x) => x.id === id);
  showDialog(
    'cancelTitle',
    `<form id="cancel-form" data-id="${id}"><div class="modal-body"><p>${t('cancelBody')}</p><p style="margin:15px 0"><strong>${esc(b.title)}</strong><br><span class="muted small">${esc(rn(room(b.rid)))} · ${dateLabel(b.date)} · ${b.start} – ${b.end}</span></p>${b.series ? `<label class="check"><input type="checkbox" name="series">${t('cancelSeries')}</label>` : ''}${errorBox()}</div><div class="modal-foot">${button('keep', 'close')}<button class="btn danger" type="submit">${t('cancel')}</button></div></form>`,
  );
}
function roomForm(id) {
  const r = room(id) || {
    name: '',
    name_th: '',
    location: '',
    location_th: '',
    capacity: 10,
    active: 1,
    approval: 0,
    amenities: 'display,wifi',
    color: 'indigo',
  };
  showDialog(
    id ? 'editRoom' : 'addRoom',
    `<form id="room-form" data-id="${id || ''}"><div class="modal-body"><div class="fields">${[
      ['roomName', 'name'],
      ['roomNameTh', 'name_th'],
      ['location', 'location'],
      ['locationTh', 'location_th'],
    ]
      .map(
        ([label, key]) =>
          `<label class="field">${t(label)}<input name="${key}" required maxlength="100" value="${esc(r[key])}"></label>`,
      )
      .join(
        '',
      )}</div><div class="fields"><label class="field">${t('capacityLabel')}<input name="capacity" type="number" min="1" max="1000" step="1" required value="${r.capacity}"></label><label class="field">${t('color')}<select name="color">${['indigo', 'teal', 'blue', 'violet'].map((k) => `<option value="${k}" ${r.color === k ? 'selected' : ''}>${t(k)}</option>`).join('')}</select></label></div><fieldset style="border:1px solid var(--line);border-radius:9px;margin:0 0 20px;padding:14px"><legend class="small">${t('amenities')}</legend><div class="fields">${['display', 'wifi', 'whiteboard', 'projector', 'video', 'audio', 'computers'].map((k) => `<label class="check"><input type="checkbox" name="amenities" value="${k}" ${r.amenities.split(',').includes(k) ? 'checked' : ''}>${t(k)}</label>`).join('')}</div></fieldset><label class="field">${t('roomPhoto')}<select name="image" id="room-image-select">${[...new Set(['', r.image || '', ...S.rooms.map((x) => x.image).filter(Boolean), '/images/inspire-room.png', '/images/collaboration-studio.png', '/images/board-room.jpg', '/images/focus-room.jpg', '/images/team-studio.jpg', '/images/workshop-room.jpg'])].map((path, i) => `<option value="${esc(path)}" ${(r.image || '') === path ? 'selected' : ''}>${path ? esc(path.startsWith('/images/') ? path.split('/').pop() : t('roomPhoto') + ' ' + i) : t('noPhoto')}</option>`).join('')}</select></label><label class="field">${t('uploadPhoto')}<input type="file" name="photo" accept="image/png,image/jpeg"></label><img id="room-image-preview" src="${esc(r.image || '')}" alt="${t('roomPhoto')}" style="width:100%;max-height:220px;object-fit:cover;border-radius:12px;margin-bottom:16px;${r.image ? '' : 'display:none'}"><label class="check"><input name="active" type="checkbox" ${r.active ? 'checked' : ''}>${t('activeRoom')}</label><p class="small muted" style="margin:8px 0 15px">${t('deactivateHint')}</p>${errorBox()}</div><div class="modal-foot">${button('cancel', 'close')}<button class="btn primary" type="submit">${t('saveRoom')}</button></div></form>`,
  );
}
function deleteRoomDialog(id) {
  const r = room(id);
  if (!r) return;
  showDialog(
    'deleteRoomTitle',
    `<form id="delete-room-form" data-id="${esc(r.id)}"><div class="modal-body"><p>${t('deleteRoomBody')}</p><p style="margin:15px 0"><strong>${esc(rn(r))}</strong><br><span class="muted small">${esc(location(r))} · ${esc(r.id)}</span></p>${errorBox()}</div><div class="modal-foot">${button('keepRoom', 'close')}<button class="btn danger" type="submit">${t('deleteRoom')}</button></div></form>`,
  );
}
function userForm() {
  showDialog(
    'newUser',
    `<form id="user-form"><div class="modal-body"><label class="field">${t('name')}<input name="name" required maxlength="100" autocomplete="off"></label><label class="field">${t('email')}<input name="email" type="email" required maxlength="160" autocomplete="off"></label><label class="field">${t('password')}<input name="password" type="password" required minlength="8" maxlength="12" autocomplete="new-password"><small>${t('passwordLength')}</small></label><label class="field">${t('role')}<select name="role"><option value="user">${t('user')}</option><option value="admin">${t('administrator')}</option></select></label>${errorBox()}</div><div class="modal-foot">${button('cancel', 'close')}<button class="btn primary" type="submit">${t('createUser')}</button></div></form>`,
  );
}
// รวม event handler สำหรับเมนู ปุ่ม และการเปลี่ยนค่าตัวกรอง
document.addEventListener('click', async (e) => {
  const b = e.target.closest('button');
  if (!b || b.disabled) return;
  if (b.dataset.nav) {
    navigate(b.dataset.nav);
    return;
  }
  if (b.dataset.tab) {
    S.tab = b.dataset.tab;
    render();
    return;
  }
  const a = b.dataset.action,
    id = b.dataset.id;
  if (!a) return;
  if (['rooms', 'my', 'history'].includes(a)) {
    navigate(a);
    return;
  }
  if (a === 'showRegister') {
    S.authMode = 'register';
    S.authScreen = true;
    render();
  }
  if (a === 'showLogin') {
    S.authMode = 'login';
    S.authScreen = true;
    render();
  }
  if (a === 'guestHome') {
    S.authScreen = false;
    S.afterLoginView = null;
    S.afterLoginBook = null;
    S.view = 'dashboard';
    render();
  }
  if (a === 'manageRole') roleForm(id);
  if (a === 'close') closeDialog();
  if (a === 'menu') {
    S.mobile = !S.mobile;
    render();
  }
  if (a === 'collapse') {
    S.collapsed = !S.collapsed;
    render();
  }
  if (a === 'theme') {
    S.theme = S.theme === 'light' ? 'dark' : 'light';
    savePref('theme', S.theme);
    render();
  }
  if (a === 'clearBookingFilters') {
    S.bookingFilter = { date: '', room: '' };
    render();
  }
  if (a === 'clearFilters') {
    S.filter = { q: '', capacity: '', date: '', status: 'all' };
    render();
  }
  if (a === 'refresh') await refresh(true);
  if (a === 'logout') {
    try {
      await api('logout', {});
      S.user = null;
      S.authScreen = false;
      S.view = 'dashboard';
      await refresh();
    } catch (e) {
      toast(message(e), true);
    }
  }
  if (a === 'book') openBooking(id);
  if (a === 'bookingBack') bookingForm();
  if (a === 'details') details(id);
  if (a === 'cancel') cancelDialog(id);
  if (a === 'addRoom') roomForm();
  if (a === 'editRoom') roomForm(id);
  if (a === 'deleteRoom') deleteRoomDialog(id);
  if (a === 'newUser') userForm();
});
function savePref(k, v) {
  try {
    localStorage.setItem('meetspace.' + k, v);
  } catch {}
}
document.addEventListener('change', (e) => {
  const el = e.target;
  if (el.dataset.setting) {
    const k = el.dataset.setting;
    S[k] = el.value;
    savePref(k, el.value);
    render();
  }
  if (el.dataset.filter) {
    S.filter[el.dataset.filter] = el.value;
    render();
  }
  if (el.dataset.bfilter) {
    S.bookingFilter[el.dataset.bfilter] = el.value;
    render();
  }
  if (el.closest('#booking-form')) updateBookingVisuals();
});
// ส่งฟอร์มเข้าสู่ระบบ สมัครสมาชิก จอง และจัดการข้อมูลต่าง ๆ
document.addEventListener('submit', async (e) => {
  const form = e.target;
  e.preventDefault();
  if (form.id === 'booking-form') {
    reviewBooking();
    return;
  }
  if (sending) return;
  const f = new FormData(form);
  const data = Object.fromEntries(f);
  sending = true;
  setBusy(form, true);
  try {
    if (form.id === 'setup-form') {
      if (data.password !== data.confirm) throw { error: 'passwordMismatch' };
      await api('setup', data);
      applyData(await api('data'));
      S.setupRequired = false;
      S.error = null;
      S.view = 'dashboard';
      render();
    }
    if (form.id === 'register-form') {
      if (data.password !== data.confirm) throw { error: 'passwordMismatch' };
      await api('register', data);
      applyData(await api('data'));
      const next = S.afterLoginView || 'dashboard',
        bookId = S.afterLoginBook;
      S.authMode = 'login';
      S.authScreen = false;
      S.afterLoginView = null;
      S.afterLoginBook = null;
      S.error = null;
      S.view = next;
      render();
      if (bookId) setTimeout(() => openBooking(bookId), 0);
      toast('registered');
    }
    if (form.id === 'role-form') {
      await api('user-role', {
        id: form.dataset.id,
        previousRole: form.dataset.previousRole,
        role: data.role,
      });
      sending = false;
      closeDialog();
      toast('roleUpdated');
      await refresh();
    }
    if (form.id === 'login-form') {
      await api('login', { ...data, remember: f.has('remember') });
      applyData(await api('data'));
      const next = S.afterLoginView || 'dashboard',
        bookId = S.afterLoginBook;
      S.authScreen = false;
      S.afterLoginView = null;
      S.afterLoginBook = null;
      S.error = null;
      S.view = next;
      render();
      if (bookId) setTimeout(() => openBooking(bookId), 0);
    }
    if (form.id === 'confirm-form') {
      const walkIn = S.view === 'walkin';
      const result = await api(walkIn ? 'walk-in/book' : 'book', draft);
      sending = false;
      closeDialog();
      await refresh();
      showDialog(
        'submitted',
        `<div class="modal-body" style="text-align:center"><div class="checkmark">${icon('check')}</div><h3>${t('submitted')}</h3><p class="muted small" style="margin-top:10px">${t(walkIn ? 'walkinSaved' : 'savedBody')}</p><p class="small">${t('reference')}: <strong>${esc(result.bookings[0].id)}</strong></p><p class="small" style="margin-top:12px">${result.bookings.length} ${t('bookingsCount')}${result.skipped?.length ? ' · ' + result.skipped.length + ' ' + t('skipped') : ''}</p></div>`,
        button('close', 'close') +
          (walkIn ? '' : button('viewReservations', 'done', 'primary')),
      );
      toast('submitted');
    }
    if (form.id === 'cancel-form') {
      await api('reservation', {
        id: form.dataset.id,
        action: 'cancel',
        series: f.has('series'),
      });
      sending = false;
      closeDialog();
      toast('cancelSuccess');
      await refresh();
    }
    if (form.id === 'room-form') {
      const photo = f.get('photo');
      let imageData = '';
      if (photo?.size) {
        if (
          photo.size > 3 * 1024 * 1024 ||
          !['image/png', 'image/jpeg'].includes(photo.type)
        )
          throw { error: 'invalidImage' };
        imageData = await readPhoto(photo);
      }
      delete data.photo;
      await api('room', {
        ...data,
        imageData,
        id: form.dataset.id || undefined,
        capacity: Number(data.capacity),
        active: f.has('active'),
        approval: false,
        amenities: f.getAll('amenities').join(','),
      });
      sending = false;
      closeDialog();
      toast('roomSaved');
      await refresh();
    }
    if (form.id === 'delete-room-form') {
      await api('room-delete', { id: form.dataset.id });
      sending = false;
      closeDialog();
      toast('roomDeleted');
      await refresh();
    }
    if (form.id === 'user-form') {
      await api('user', data);
      sending = false;
      closeDialog();
      toast('userCreated');
      await refresh();
    }
    if (form.id === 'password-form') {
      await api('password', data);
      S.user = null;
      render();
      toast('passwordChanged');
    }
  } catch (e) {
    formError(e);
  } finally {
    sending = false;
    setBusy(form, false);
  }
});
document.addEventListener('click', (e) => {
  if (e.target.closest('[data-action="done"]')) {
    closeDialog();
    navigate('my');
  }
});
$('#dialog').addEventListener('cancel', (e) => {
  e.preventDefault();
  closeDialog();
});
$('#dialog').addEventListener('click', (e) => {
  if (e.target === $('#dialog')) {
    const r = e.target.getBoundingClientRect();
    if (
      e.clientX < r.left ||
      e.clientX > r.right ||
      e.clientY < r.top ||
      e.clientY > r.bottom
    )
      closeDialog();
  }
});
document.addEventListener(
  'invalid',
  (e) => {
    const el = e.target;
    if (el.closest('#booking-form') && el.name === 'participants') {
      e.preventDefault();
      formError({
        error: el.validity.rangeOverflow ? 'capacity' : 'invalidParticipants',
        capacity: room(draft.rid).capacity,
      });
    }
  },
  true,
);
// อ่านรูปห้องที่อัปโหลดและแสดงตัวอย่างก่อนบันทึก
function readPhoto(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result);
    reader.onerror = () => reject({ error: 'invalidImage' });
    reader.readAsDataURL(file);
  });
}
document.addEventListener('change', async (e) => {
  if (!e.target.closest('#room-form')) return;
  const preview = $('#room-image-preview');
  if (e.target.name === 'image') {
    $('#room-form [name=photo]').value = '';
    preview.src = e.target.value;
    preview.style.display = e.target.value ? 'block' : 'none';
  }
  if (e.target.name === 'photo') {
    const file = e.target.files[0];
    if (!file) return;
    if (
      file.size > 3 * 1024 * 1024 ||
      !['image/png', 'image/jpeg'].includes(file.type)
    ) {
      e.target.value = '';
      formError({ error: 'invalidImage' });
      return;
    }
    preview.src = await readPhoto(file);
    preview.style.display = 'block';
  }
});
// รีเฟรชข้อมูลเป็นระยะและโหลด session เมื่อเปิดแอป
setInterval(() => {
  if (
    !$('#dialog').open &&
    !document.hidden &&
    S.view !== 'settings' &&
    !document.activeElement.matches('input,select,textarea')
  )
    refresh();
}, 60000);
try {
  const session = await api('session');
  Object.assign(S, session);
  await refresh(true);
} catch (e) {
  S.error = e;
  render();
}
addEventListener('click', (e) => {
  if (e.target.closest('button,a,input,select,textarea')) return;
  const c = e.target.closest('.room-card[data-card-book]');
  if (!c) return;
  const b = c.querySelector('button[data-action="book"]:not(:disabled)');
  if (b) b.click();
});

// สร้างและควบคุม carousel ห้องแนะนำบนหน้า Dashboard
function recRooms() {
  const q = String(S.recQ || '')
    .trim()
    .toLowerCase();
  return S.rooms.filter(
    (r) =>
      r.active &&
      (!q ||
        [r.id, r.name, r.name_th, r.location, r.location_th].some((v) =>
          String(v || '')
            .toLowerCase()
            .includes(q),
        )),
  );
}
function recCard(r, k) {
  const photo = r.image
    ? `<img src="${esc(r.image)}" alt="" loading="lazy" draggable="false" onerror="this.remove()">`
    : `<div class="rec-plan">${floorplan()}</div>`;
  const am = String(r.amenities || '')
    .split(',')
    .filter(Boolean)
    .slice(0, 4)
    .map((a) => `<span title="${t(a)}">${icon(a)}</span>`)
    .join('');
  return `<div class="rec-card ${r.color || ''}" data-rec-k="${k}">${photo}<span class="rec-shade"></span><span class="rec-code">${esc(r.id)}</span>${badge(roomStatus(r))}<div class="rec-info"><strong>${esc(rn(r))}</strong><div class="rec-meta"><span>${icon('users')}${r.capacity} ${t('people')}</span><span>${icon('pin')}${esc(location(r))}</span></div>${am ? `<div class="rec-amen">${am}</div>` : ''}</div></div>`;
}
function recInner() {
  const list = recRooms(),
    n = list.length;
  if (!n)
    return `<div class="rec-empty">${icon('search')}<p>${t('recEmpty')}</p></div>`;
  const nav =
    n > 1
      ? `<button type="button" class="rec-nav prev" data-rec-go="-1" aria-label="${t('recPrev')}">${icon('left')}</button><button type="button" class="rec-nav next" data-rec-go="1" aria-label="${t('recNext')}">${icon('chevron')}</button>`
      : '';
  return `<div class="rec-stage" tabindex="0" aria-roledescription="carousel" aria-label="${t('recommended')}">${list.map(recCard).join('')}${nav}</div><div class="rec-dots" ${n > 1 ? '' : 'hidden'}>${list.map((_, k) => `<button type="button" data-rec-dot="${k}" aria-label="${k + 1} / ${n}"></button>`).join('')}</div><p class="rec-count" aria-live="polite"></p>`;
}
function recLayout() {
  const root = document.getElementById('rec-carousel');
  if (!root) return;
  const cards = [...root.querySelectorAll('.rec-card')],
    n = cards.length;
  if (!n) return;
  S.recIdx = (((S.recIdx | 0) % n) + n) % n;
  const i = S.recIdx;
  cards.forEach((c, k) => {
    let d = k - i;
    if (d > n / 2) d -= n;
    if (d < -n / 2) d += n;
    const pos =
      d === 0
        ? 'center'
        : d === -1 && n > 2
          ? 'left'
          : d === 1
            ? 'right'
            : d === -1
              ? 'right'
              : 'hidden';
    c.className =
      c.className.replace(/\b(center|left|right|hidden)\b/g, '').trim() +
      ' ' +
      pos;
    c.dataset.recGo = pos === 'left' ? '-1' : pos === 'right' ? String(d) : '';
    c.setAttribute('aria-hidden', pos === 'center' ? 'false' : 'true');
  });
  root.querySelectorAll('[data-rec-dot]').forEach((b, k) => {
    b.classList.toggle('on', k === i);
    b.setAttribute('aria-current', k === i ? 'true' : 'false');
  });
  const c = root.querySelector('.rec-count');
  if (c)
    c.textContent = t('recCount')
      .replace('{n}', i + 1)
      .replace('{total}', n);
}
function recSection() {
  if (!S.rooms.some((r) => r.active)) return '';
  return `<section class="rec-section"><div class="rec-head"><div><h2>${t('recommended')}</h2><p>${t('recIntro')}</p></div><div class="rec-tools"><label class="rec-search"><input id="rec-search" type="search" placeholder="${t('recSearch')}" aria-label="${t('recSearch')}" value="${esc(S.recQ || '')}">${icon('search')}</label>${button('viewAll', 'rooms', 'text compact', 'arrow')}</div></div><div id="rec-carousel" class="rec-carousel">${recInner()}</div></section>`;
}
function updateRec(rebuild) {
  const el = document.getElementById('rec-carousel');
  if (!el) return;
  if (rebuild) el.innerHTML = recInner();
  recLayout();
}
let recLock = 0;
function recMove(delta) {
  const now = Date.now();
  if (now - recLock < 140) return;
  recLock = now;
  S.recIdx = (S.recIdx | 0) + delta;
  recLayout();
}
document.addEventListener('click', (e) => {
  const go = e.target.closest('[data-rec-go]');
  if (go && go.dataset.recGo) {
    e.preventDefault();
    recMove(Number(go.dataset.recGo));
    return;
  }
  const dot = e.target.closest('[data-rec-dot]');
  if (dot) {
    S.recIdx = Number(dot.dataset.recDot);
    recLayout();
  }
});
document.addEventListener('keydown', (e) => {
  if (!e.target.closest || !e.target.closest('.rec-stage')) return;
  if (e.key === 'ArrowLeft') {
    e.preventDefault();
    recMove(-1);
  }
  if (e.key === 'ArrowRight') {
    e.preventDefault();
    recMove(1);
  }
});
document.addEventListener('input', (e) => {
  if (e.target.id === 'rec-search') {
    S.recQ = e.target.value;
    S.recIdx = 0;
    updateRec(true);
  }
});
let recSwipe = null;
document.addEventListener('pointerdown', (e) => {
  const st = e.target.closest && e.target.closest('.rec-stage');
  if (st && !e.target.closest('button'))
    recSwipe = { x: e.clientX, y: e.clientY };
});
document.addEventListener('pointerup', (e) => {
  if (!recSwipe) return;
  const dx = e.clientX - recSwipe.x,
    dy = e.clientY - recSwipe.y;
  recSwipe = null;
  if (Math.abs(dx) > 45 && Math.abs(dx) > Math.abs(dy)) {
    recLock = 0;
    recMove(dx < 0 ? 1 : -1);
  }
});
