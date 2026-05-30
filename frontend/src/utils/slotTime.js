/** Utilitaires dates / plages horaires pour les créneaux */

export function startOfWeekMonday(date) {
  const d = new Date(date);
  const day = d.getDay();
  const diff = day === 0 ? -6 : 1 - day;
  d.setDate(d.getDate() + diff);
  d.setHours(0, 0, 0, 0);
  return d;
}

export function addDays(date, days) {
  const d = new Date(date);
  d.setDate(d.getDate() + days);
  return d;
}

export function dateKey(date) {
  const d = date instanceof Date ? date : new Date(date);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
}

export function isSameDay(a, b) {
  if (!a || !b) return false;
  return dateKey(a) === dateKey(b);
}

export function formatTime(dateString) {
  return new Date(dateString).toLocaleTimeString("fr-FR", {
    hour: "2-digit",
    minute: "2-digit",
  });
}

/** Affiche une plage horaire (ex. 10:00 – 11:00) pour un créneau de début. */
export function formatSlotPlage(creneau, dureeMinutes) {
  const start = new Date(creneau.dateHeure);
  const total = dureeMinutes || creneau.dureeMinutes || 30;
  const slotDur = creneau.dureeMinutes || 30;
  const endMs = start.getTime() + Math.max(total, slotDur) * 60000;
  const end = new Date(endMs);
  if (total <= slotDur) {
    return formatTime(creneau.dateHeure);
  }
  return `${formatTime(creneau.dateHeure)} – ${formatTime(end.toISOString())}`;
}

export function buildAvailableDateSet(creneauxDisponibles) {
  const set = new Set();
  (creneauxDisponibles || []).forEach((c) => {
    set.add(c.dateHeure.slice(0, 10));
  });
  return set;
}

export function isDateAvailable(date, availableDateSet) {
  return availableDateSet.has(dateKey(date));
}

const MONTH_NAMES = [
  "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
  "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre",
];

export function formatMonthYear(date) {
  return `${MONTH_NAMES[date.getMonth()]} ${date.getFullYear()}`;
}

export function formatWeekRange(weekStart) {
  const end = addDays(weekStart, 6);
  if (weekStart.getMonth() === end.getMonth()) {
    return `${weekStart.getDate()} – ${end.getDate()} ${MONTH_NAMES[weekStart.getMonth()]} ${weekStart.getFullYear()}`;
  }
  return `${weekStart.toLocaleDateString("fr-FR", { day: "numeric", month: "short" })} – ${end.toLocaleDateString("fr-FR", { day: "numeric", month: "short", year: "numeric" })}`;
}

export { MONTH_NAMES };
