// ============================================================
// AyurSutra — shared.js  v3
// Include on every page: <script src="shared.js"></script>
// Then call injectSharedStyles() and buildNav(activeHref)
// ============================================================

const API = 'http://localhost:8080/api/v1';

// ── Auth ─────────────────────────────────────────────────────
const Auth = {
  token:      () => localStorage.getItem('token'),
  user:       () => { try { return JSON.parse(localStorage.getItem('user') || '{}'); } catch(e) { return {}; } },
  isLoggedIn: () => !!(localStorage.getItem('token') && Auth.user().id),
  role:       () => (Auth.user().role || '').toUpperCase(),
  isPatient:  () => Auth.role() === 'PATIENT',
  isDoctor:   () => Auth.role() === 'DOCTOR',
  isAdmin:    () => Auth.role() === 'ADMIN',
  logout:     () => { localStorage.clear(); window.location.href = 'login.html'; },

  /*
   * FIX: Use window.location.replace (not href) so the browser doesn't add
   * the protected page to history.  Also blank the page immediately so the
   * user never sees a flash of protected content before redirect.
   */
  require: () => {
    if (!Auth.isLoggedIn()) {
      document.documentElement.innerHTML = '';
      window.location.replace('login.html');
      throw new Error('Unauthenticated');
    }
  },
  requireDoctor: () => {
    Auth.require();
    if (!Auth.isDoctor()) {
      document.documentElement.innerHTML = '';
      window.location.replace('profile.html');
      throw new Error('Not a doctor');
    }
  },
  requirePatient: () => {
    Auth.require();
    if (!Auth.isPatient()) {
      document.documentElement.innerHTML = '';
      window.location.replace('doctor-dashboard.html');
      throw new Error('Not a patient');
    }
  }
};

// ── Fetch Wrappers ───────────────────────────────────────────
async function apiFetch(path, options = {}) {
  const res = await fetch(`${API}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${Auth.token()}`,
      ...(options.headers || {})
    }
  });
  return res.json();
}

async function apiUpload(path, formData) {
  const res = await fetch(`${API}${path}`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${Auth.token()}` },
    body: formData
  });
  return res.json();
}

// ── Toast ─────────────────────────────────────────────────────
const Toast = {
  show(msg, type = 'success', duration = 3500) {
    const prev = document.getElementById('__ayur_toast');
    if (prev) prev.remove();
    const icons = { success: '✓', error: '✕', info: 'ℹ', warning: '⚠' };
    const t = document.createElement('div');
    t.id = '__ayur_toast';
    t.className = `ayur-toast ayur-toast-${type}`;
    t.innerHTML = `<span style="font-size:16px">${icons[type]}</span><span>${msg}</span>`;
    document.body.appendChild(t);
    requestAnimationFrame(() => t.classList.add('ayur-toast-in'));
    setTimeout(() => { t.classList.remove('ayur-toast-in'); setTimeout(() => t.remove(), 300); }, duration);
  },
  success: m => Toast.show(m, 'success'),
  error:   m => Toast.show(m, 'error'),
  info:    m => Toast.show(m, 'info'),
  warning: m => Toast.show(m, 'warning')
};

// ── Helpers ───────────────────────────────────────────────────
function getInitials(first = '', last = '') {
  return ((first[0] || '') + (last[0] || '')).toUpperCase() || '?';
}

function getAvatarColor(name = '') {
  const palette = ['#2d5016','#3d6b1f','#d4af37','#8b6914','#4a7c59','#1a3a0a','#5c4033'];
  let h = 0;
  for (const c of name) h = c.charCodeAt(0) + ((h << 5) - h);
  return palette[Math.abs(h) % palette.length];
}

function fmtDate(d, opts = {}) {
  if (!d) return '—';
  try { return new Date(d).toLocaleDateString('en-IN', { day:'numeric', month:'short', year:'numeric', ...opts }); }
  catch { return '—'; }
}

function fmtShort(d) {
  if (!d) return '—';
  try { return new Date(d).toLocaleDateString('en-IN', { day:'numeric', month:'short' }); }
  catch { return '—'; }
}

function timeAgo(d) {
  if (!d) return '—';
  const mins = Math.floor((Date.now() - new Date(d).getTime()) / 60000);
  if (mins < 1)   return 'Just now';
  if (mins < 60)  return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24)   return `${hrs}h ago`;
  return `${Math.floor(hrs / 24)}d ago`;
}

// ── State HTML builders ───────────────────────────────────────
function loadingHTML(msg = 'Loading...') {
  return `<div class="s-box"><div class="s-spin"></div><p>${msg}</p></div>`;
}
function emptyHTML(icon, title, sub = '') {
  return `<div class="s-box"><div class="s-icon">${icon}</div><h4>${title}</h4>${sub ? `<p>${sub}</p>` : ''}</div>`;
}
function errorHTML(msg = 'Something went wrong') {
  return `<div class="s-box s-err"><div class="s-icon">⚠️</div><h4>Error</h4><p>${msg}</p></div>`;
}

// ── Mock Data ─────────────────────────────────────────────────
const MockData = {
  doctorStats: { totalPatients:24, todayAppointments:6, pendingReports:3, completedThisWeek:18 },
  recentPatients: [
    { id:1, firstName:'Priya',  lastName:'Sharma', patientId:'AYR-PAT-000001', doshaType:'VATA',       createdAt: new Date(Date.now()-86400000*2).toISOString() },
    { id:2, firstName:'Arjun',  lastName:'Mehta',  patientId:'AYR-PAT-000002', doshaType:'PITTA',      createdAt: new Date(Date.now()-86400000*5).toISOString() },
    { id:3, firstName:'Sunita', lastName:'Patel',  patientId:'AYR-PAT-000003', doshaType:'KAPHA',      createdAt: new Date(Date.now()-86400000).toISOString()   },
    { id:4, firstName:'Raj',    lastName:'Kumar',  patientId:'AYR-PAT-000004', doshaType:'VATA_PITTA', createdAt: new Date(Date.now()-86400000*10).toISOString() },
  ],
  todaySchedule: [
    { time:'09:00 AM', patient:'Priya Sharma',  type:'Consultation', status:'completed' },
    { time:'10:30 AM', patient:'Arjun Mehta',   type:'Panchakarma',  status:'completed' },
    { time:'12:00 PM', patient:'Sunita Patel',  type:'Follow-up',    status:'ongoing'   },
    { time:'02:00 PM', patient:'Raj Kumar',     type:'Consultation', status:'upcoming'  },
    { time:'03:30 PM', patient:'Meera Joshi',   type:'Abhyanga',     status:'upcoming'  },
  ],
  doctors: [
    { id:1, firstName:'Dr. Anil', lastName:'Sharma', specialization:'Panchakarma Specialist', experienceYears:12, consultationFee:800,  rating:4.8, isAvailable:true,  qualification:'BAMS, MD (Ayurveda)' },
    { id:2, firstName:'Dr. Meera',lastName:'Patel',  specialization:'Kayachikitsa',           experienceYears:8,  consultationFee:600,  rating:4.6, isAvailable:true,  qualification:'BAMS, PhD' },
    { id:3, firstName:'Dr. Rajan',lastName:'Nair',   specialization:'Shalya Tantra',          experienceYears:15, consultationFee:1000, rating:4.9, isAvailable:false, qualification:'BAMS, MS (Ayu)' },
    { id:4, firstName:'Dr. Kavya',lastName:'Iyer',   specialization:'Streeroga',              experienceYears:6,  consultationFee:700,  rating:4.5, isAvailable:true,  qualification:'BAMS' },
  ]
};

// ── Navbar ────────────────────────────────────────────────────
function buildNav(activePage = '') {
  const u    = Auth.user();
  const role = Auth.role();
  const init = getInitials(u.firstName, u.lastName);
  const bg   = getAvatarColor((u.firstName||'')+(u.lastName||''));

  const patientLinks = [
    { href:'profile.html',          label:'Profile'      },
    { href:'health-dashboard.html', label:'Health'       },
    { href:'doctors-list.html',     label:'Find Doctors' },
  ];
  const doctorLinks = [
    { href:'doctor-dashboard.html', label:'Dashboard' },
    { href:'scan-patient.html',     label:'Scan QR'   },
  ];

  const links    = role === 'DOCTOR' ? doctorLinks : patientLinks;
  const homeHref = role === 'DOCTOR' ? 'doctor-dashboard.html' : 'profile.html';

  const linksHtml = links.map(l =>
    `<a href="${l.href}" class="anav-link${activePage === l.href ? ' anav-active' : ''}">${l.label}</a>`
  ).join('');

  const profileItem = role === 'PATIENT'
    ? `<a href="profile.html" class="anav-dd-item">👤 My Profile</a>
       <a href="health-dashboard.html" class="anav-dd-item">📊 Health Dashboard</a>`
    : `<a href="doctor-dashboard.html" class="anav-dd-item">🏠 Dashboard</a>`;

  const nav = document.createElement('nav');
  nav.className = 'ayur-nav';
  nav.id = '__ayurNav';
  nav.innerHTML = `
    <div class="anav-inner">
      <a href="${homeHref}" class="anav-brand">
        <span style="font-size:20px;">🍃</span>
        <span class="anav-brand-name">AyurSutra</span>
      </a>
      <div class="anav-links" id="anavLinks">${linksHtml}</div>
      <div class="anav-right">
        <button class="anav-user" onclick="__anavToggle()" id="anavUserBtn">
          <span class="anav-av" style="background:${bg}" id="anavAv">${init}</span>
          <span class="anav-uname" id="anavUname">${u.firstName || 'User'}</span>
          <span style="color:rgba(255,255,255,.35);font-size:10px;margin-left:2px;">▾</span>
        </button>
        <div class="anav-dd" id="anavDd">
          <div class="anav-dd-head">
            <span class="anav-av" style="background:${bg};width:32px;height:32px;font-size:12px;">${init}</span>
            <div>
              <div class="anav-dd-name">${(u.firstName||'')} ${(u.lastName||'')}</div>
              <div class="anav-dd-role">${role}</div>
            </div>
          </div>
          ${profileItem}
          <div class="anav-dd-sep"></div>
          <button class="anav-dd-item anav-dd-out" onclick="Auth.logout()">🚪 Sign Out</button>
        </div>
      </div>
      <button class="anav-burger" onclick="__anavBurger()" id="anavBurger">☰</button>
    </div>
    <div class="anav-mob" id="anavMob">
      ${linksHtml}
      <button class="anav-link" style="background:none;border:none;cursor:pointer;text-align:left;color:rgba(255,255,255,.55);width:100%;padding:8px 0;" onclick="Auth.logout()">🚪 Sign Out</button>
    </div>
  `;

  document.body.prepend(nav);

  document.addEventListener('click', e => {
    if (!e.target.closest('.anav-right')) {
      document.getElementById('anavDd')?.classList.remove('open');
    }
  });
}

function __anavToggle() { document.getElementById('anavDd').classList.toggle('open'); }
function __anavBurger() {
  const mob = document.getElementById('anavMob');
  const btn = document.getElementById('anavBurger');
  mob.classList.toggle('open');
  btn.textContent = mob.classList.contains('open') ? '✕' : '☰';
}

// ── Inject shared CSS ─────────────────────────────────────────
function injectSharedStyles() {
  const link2 = document.createElement('link');
  link2.rel = 'stylesheet';
  link2.href = 'https://fonts.googleapis.com/css2?family=Sora:wght@300;400;500;600;700;800&family=Lora:ital,wght@0,400;0,600;1,400&display=swap';
  document.head.appendChild(link2);

  const s = document.createElement('style');
  s.textContent = `
*, *::before, *::after { margin:0; padding:0; box-sizing:border-box; }
:root {
  --g9:#0f1a08; --g8:#1a2f0d; --g7:#2d5016; --g6:#3d6b1f; --g5:#4e8c28;
  --g3:#8bc34a; --g1:#e8f5d8; --g0:#f4fbee;
  --gold:#d4af37; --gold-l:#f0d878;
  --ink:#0f1a08; --ink2:#2c3520; --ink3:#5a6650; --ink4:#8a9680;
  --paper:#f8f6f0; --paper2:#ede9df; --paper3:#ddd8cc;
  --white:#ffffff; --red:#c0392b; --red-l:#fde8e6;
  --r8:8px; --r12:12px; --r16:16px; --r20:20px; --r24:24px;
  --sh1:0 1px 4px rgba(15,26,8,.08); --sh2:0 4px 16px rgba(15,26,8,.10); --sh3:0 8px 32px rgba(15,26,8,.14);
  --nav:58px;
  /* aliases used by some pages */
  --green-700:#2d5016; --green-600:#3d6b1f; --green-900:#0f1a08; --green-50:#f4fbee; --green-100:#e8f5d8;
  --border:#ddd8cc; --cream:#f8f6f0; --text-900:#0f1a08; --text-700:#2c3520; --text-500:#5a6650; --text-300:#8a9680;
  --radius-sm:8px; --radius-md:12px; --radius-lg:16px;
  --shadow-md:0 4px 16px rgba(15,26,8,.10); --shadow-lg:0 8px 32px rgba(15,26,8,.14);
  --white:#ffffff;
}
html { scroll-behavior:smooth; }
body { font-family:'Sora',sans-serif; background:var(--paper); color:var(--ink2); min-height:100vh; padding-top:var(--nav); }
h1,h2,h3,h4,h5 { font-family:'Lora',serif; color:var(--ink); }
a { text-decoration:none; }
button,input,select,textarea { font-family:'Sora',sans-serif; }

/* Navbar */
.ayur-nav { position:fixed; top:0; left:0; right:0; z-index:1000; height:var(--nav); background:var(--g9); border-bottom:1px solid rgba(255,255,255,.06); }
.anav-inner { height:100%; max-width:1440px; margin:0 auto; padding:0 24px; display:flex; align-items:center; gap:0; }
.anav-brand { display:flex; align-items:center; gap:9px; flex-shrink:0; margin-right:20px; }
.anav-brand-name { font-family:'Lora',serif; font-size:17px; font-weight:600; color:white; letter-spacing:.2px; white-space:nowrap; }
.anav-links { display:flex; align-items:center; gap:2px; flex:1; }
.anav-link { padding:6px 13px; font-size:13px; font-weight:500; color:rgba(255,255,255,.55); border-radius:var(--r8); transition:all .16s; white-space:nowrap; text-decoration:none; display:inline-block; }
.anav-link:hover { color:white; background:rgba(255,255,255,.08); }
.anav-active { color:white !important; background:rgba(255,255,255,.12); font-weight:600; }
.anav-right { position:relative; margin-left:auto; }
.anav-user { display:flex; align-items:center; gap:9px; padding:5px 12px 5px 5px; background:rgba(255,255,255,.08); border:1px solid rgba(255,255,255,.12); border-radius:30px; cursor:pointer; transition:all .16s; }
.anav-user:hover { background:rgba(255,255,255,.14); }
.anav-av { width:28px; height:28px; border-radius:50%; flex-shrink:0; display:flex; align-items:center; justify-content:center; color:white; font-size:11px; font-weight:700; }
.anav-uname { font-size:13px; font-weight:600; color:white; }
.anav-dd { display:none; position:absolute; top:calc(100% + 10px); right:0; background:var(--g8); border:1px solid rgba(255,255,255,.1); border-radius:var(--r12); box-shadow:var(--sh3); width:208px; overflow:hidden; }
.anav-dd.open { display:block; animation:anavFD .14s ease; }
@keyframes anavFD { from{opacity:0;transform:translateY(-6px)} to{opacity:1;transform:none} }
.anav-dd-head { display:flex; gap:10px; align-items:center; padding:13px 14px; border-bottom:1px solid rgba(255,255,255,.08); }
.anav-dd-name { font-size:13px; font-weight:600; color:white; }
.anav-dd-role { font-size:10px; color:rgba(255,255,255,.35); text-transform:uppercase; letter-spacing:.5px; margin-top:1px; }
.anav-dd-item { display:flex; align-items:center; gap:8px; width:100%; padding:9px 14px; font-size:13px; color:rgba(255,255,255,.65); background:none; border:none; cursor:pointer; transition:background .14s; text-decoration:none; }
.anav-dd-item:hover { background:rgba(255,255,255,.06); color:white; }
.anav-dd-sep { height:1px; background:rgba(255,255,255,.08); }
.anav-dd-out { color:#fc8181 !important; }
.anav-burger { display:none; background:none; border:none; color:white; font-size:20px; cursor:pointer; margin-left:auto; }
.anav-mob { display:none; position:fixed; top:var(--nav); left:0; right:0; background:var(--g9); border-bottom:1px solid rgba(255,255,255,.1); z-index:999; padding:10px 20px; flex-direction:column; gap:2px; }
.anav-mob.open { display:flex; }
.anav-mob .anav-link { color:rgba(255,255,255,.65); padding:9px 4px; }

/* Layout */
.page-wrap    { max-width:1440px; margin:0 auto; padding:28px 24px; }
.page-wrap-sm { max-width:960px;  margin:0 auto; padding:28px 24px; }

/* Cards */
.card    { background:var(--white); border:1px solid var(--paper3); border-radius:var(--r16); overflow:hidden; }
.card-p  { padding:22px; }
.card-header { display:flex; align-items:center; justify-content:space-between; padding:16px 22px; border-bottom:1px solid var(--paper3); }
.card-hd { display:flex; align-items:center; justify-content:space-between; padding:16px 22px; border-bottom:1px solid var(--paper3); }
.card-title { font-family:'Lora',serif; font-size:17px; font-weight:600; color:var(--ink); }
.card-tt { font-family:'Lora',serif; font-size:17px; font-weight:600; color:var(--ink); }

/* Buttons */
.btn { display:inline-flex; align-items:center; gap:7px; padding:9px 18px; border-radius:var(--r8); font-size:13px; font-weight:600; cursor:pointer; border:none; transition:all .18s; white-space:nowrap; font-family:'Sora',sans-serif; text-decoration:none; }
.btn-primary   { background:var(--g7); color:white; }
.btn-primary:hover   { background:var(--g8); transform:translateY(-1px); box-shadow:0 4px 12px rgba(45,80,22,.3); }
.btn-secondary { background:var(--white); color:var(--ink2); border:1.5px solid var(--paper3); }
.btn-secondary:hover { background:var(--paper); border-color:var(--g5); }
.btn-gold      { background:var(--gold); color:var(--g9); }
.btn-gold:hover      { background:var(--gold-l); }
.btn-ghost     { background:rgba(255,255,255,.1); color:white; border:1px solid rgba(255,255,255,.2); }
.btn-ghost:hover     { background:rgba(255,255,255,.18); }
.btn-danger    { background:var(--red-l); color:var(--red); border:1px solid #fca5a5; }
.btn-danger:hover    { background:var(--red); color:white; }
.btn-sm   { padding:5px 12px; font-size:12px; }
.btn-lg   { padding:12px 28px; font-size:15px; }
.btn-full { width:100%; justify-content:center; }
.btn:disabled { opacity:.55; cursor:not-allowed; transform:none !important; }

/* Badges */
.badge       { display:inline-flex; align-items:center; gap:4px; padding:3px 9px; border-radius:20px; font-size:11px; font-weight:700; }
.badge-green { background:var(--g1); color:var(--g7); }
.badge-gold  { background:#fef9e7; color:#8b6914; }
.badge-red   { background:var(--red-l); color:var(--red); }
.badge-blue  { background:#eff6ff; color:#2563eb; }
.badge-gray  { background:#f3f4f6; color:#6b7280; }

/* Stat grid */
.stat-grid { display:grid; grid-template-columns:repeat(auto-fit,minmax(200px,1fr)); gap:16px; }
.stat-card { background:var(--white); border:1px solid var(--paper3); border-radius:var(--r12); padding:20px; transition:all .2s; }
.stat-card:hover { transform:translateY(-2px); box-shadow:var(--sh2); }

/* Avatar */
.avatar    { border-radius:50%; display:flex; align-items:center; justify-content:center; color:white; font-weight:700; flex-shrink:0; }
.avatar-xs { width:28px;height:28px;font-size:10px; }
.avatar-sm { width:36px;height:36px;font-size:13px; }
.avatar-md { width:48px;height:48px;font-size:17px; }
.avatar-lg { width:68px;height:68px;font-size:24px; }
.avatar-xl { width:90px;height:90px;font-size:32px; }

/* Forms */
.form-group   { margin-bottom:14px; }
.form-label   { display:block; font-size:11px; font-weight:700; text-transform:uppercase; letter-spacing:.4px; color:var(--ink3); margin-bottom:5px; }
.form-control { width:100%; padding:10px 12px; border:1.5px solid var(--paper3); border-radius:var(--r8); font-size:14px; font-family:'Sora',sans-serif; color:var(--ink2); background:var(--white); transition:all .18s; }
.form-control:focus { outline:none; border-color:var(--g7); box-shadow:0 0 0 3px rgba(45,80,22,.1); }
.form-ctrl    { width:100%; padding:10px 12px; border:1.5px solid var(--paper3); border-radius:var(--r8); font-size:14px; font-family:'Sora',sans-serif; color:var(--ink2); background:var(--white); transition:all .18s; }
.form-ctrl:focus { outline:none; border-color:var(--g7); box-shadow:0 0 0 3px rgba(45,80,22,.1); }
.form-row     { display:grid; grid-template-columns:1fr 1fr; gap:12px; }
.form-hint    { font-size:11px; color:var(--ink4); margin-top:4px; }
.form-section { font-size:11px; font-weight:700; text-transform:uppercase; letter-spacing:.5px; color:var(--ink3); margin:16px 0 10px; border-bottom:1.5px solid var(--paper3); padding-bottom:6px; }

/* Modal */
.modal-overlay { display:none; position:fixed; inset:0; background:rgba(0,0,0,.55); z-index:2000; align-items:center; justify-content:center; padding:20px; }
.modal-overlay.open { display:flex; animation:mFade .2s; }
@keyframes mFade { from{opacity:0} to{opacity:1} }
.modal-box    { background:var(--white); border-radius:var(--r24); padding:30px; max-width:520px; width:100%; max-height:88vh; overflow-y:auto; animation:mSlide .22s ease; }
@keyframes mSlide { from{opacity:0;transform:translateY(28px)} to{opacity:1;transform:none} }
.modal-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:22px; }
.modal-head   { display:flex; justify-content:space-between; align-items:center; margin-bottom:22px; }
.modal-title  { font-family:'Lora',serif; font-size:21px; font-weight:600; }
.modal-close  { width:30px;height:30px;border-radius:50%;background:var(--paper);border:none;font-size:15px;cursor:pointer;display:flex;align-items:center;justify-content:center; }
.modal-x      { width:30px;height:30px;border-radius:50%;background:var(--paper);border:none;font-size:15px;cursor:pointer;display:flex;align-items:center;justify-content:center; }
.modal-close:hover,.modal-x:hover { background:var(--paper2); }

/* State boxes */
.s-box  { display:flex; flex-direction:column; align-items:center; justify-content:center; padding:40px 20px; gap:10px; text-align:center; }
.state-box { display:flex; flex-direction:column; align-items:center; justify-content:center; padding:40px 20px; gap:10px; text-align:center; }
.s-box h4,.state-box h4 { font-size:15px; color:var(--ink3); font-family:'Sora',sans-serif; font-weight:600; }
.s-box p,.state-box p   { font-size:13px; color:var(--ink4); }
.s-icon,.state-icon     { font-size:40px; opacity:.5; }
.s-err h4               { color:var(--red); }
.s-spin,.spinner        { width:30px;height:30px;border:3px solid var(--paper3);border-top-color:var(--g7);border-radius:50%;animation:sspin .7s linear infinite; }
@keyframes sspin { to{transform:rotate(360deg)} }

/* Toast */
.ayur-toast { position:fixed; bottom:24px; right:24px; z-index:9999; display:flex; align-items:center; gap:10px; padding:12px 18px; border-radius:var(--r8); font-size:13px; font-weight:600; box-shadow:var(--sh3); transform:translateY(80px); opacity:0; transition:all .28s ease; max-width:330px; font-family:'Sora',sans-serif; }
.ayur-toast-in { transform:none !important; opacity:1 !important; }
.ayur-toast-success { background:var(--g8); color:white; }
.ayur-toast-error   { background:#7f1d1d; color:white; }
.ayur-toast-info    { background:#1e3a5f; color:white; }
.ayur-toast-warning { background:#7c4a00; color:white; }

/* Dosha badges */
.dosha-vata        { background:#e8f0ff;color:#2d4fa0; }
.dosha-pitta       { background:#fff0e8;color:#a02d2d; }
.dosha-kapha       { background:#e8fff0;color:#2d7a3a; }
.dosha-vata_pitta  { background:#f5e8ff;color:#6b2da0; }
.dosha-pitta_kapha { background:#ffffe8;color:#7a7a2d; }
.dosha-vata_kapha  { background:#e8f8ff;color:#2d6ea0; }
.dosha-tridosha    { background:#f8e8ff;color:#8b2da0; }

/* Info rows */
.info-row { display:flex; justify-content:space-between; padding:9px 0; border-bottom:1px solid var(--paper3); font-size:13px; }
.info-row:last-child { border-bottom:none; }
.info-key { color:var(--ink4); }
.info-val { font-weight:600; color:var(--ink2); text-align:right; max-width:58%; word-break:break-word; }

/* Table */
.data-table { width:100%; border-collapse:collapse; }
.data-table th { text-align:left; padding:10px 14px; font-size:11px; font-weight:700; text-transform:uppercase; letter-spacing:.5px; color:var(--ink4); border-bottom:2px solid var(--paper3); }
.data-table td { padding:12px 14px; font-size:13px; border-bottom:1px solid var(--paper3); vertical-align:middle; }
.data-table tr:hover td { background:var(--g0); }
.data-table tr:last-child td { border-bottom:none; }

/* Responsive */
@media(max-width:768px) {
  .anav-links  { display:none; }
  .anav-burger { display:block; }
  .form-row    { grid-template-columns:1fr; }
  .stat-grid   { grid-template-columns:1fr 1fr; }
  .page-wrap,.page-wrap-sm { padding:20px 16px; }
  .modal-box   { padding:22px 18px; }
}
@media(max-width:480px) { .stat-grid { grid-template-columns:1fr; } }
  `;
  document.head.appendChild(s);
}