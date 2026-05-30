import React, { useMemo, useState } from "react";
import { ChevronLeft, ChevronRight, Calendar, Clock } from "lucide-react";
import {
  startOfWeekMonday,
  addDays,
  dateKey,
  isSameDay,
  formatSlotPlage,
  buildAvailableDateSet,
  isDateAvailable,
  formatMonthYear,
  formatWeekRange,
} from "../utils/slotTime";

const WEEKDAYS = ["Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"];

/**
 * Calendrier semaine / mois pour choisir un créneau avec plages horaires.
 */
const SlotCalendar = ({
  creneauxDisponibles = [],
  slots = [],
  selectedDate,
  onSelectDate,
  selectedSlot,
  onSelectSlot,
  dureeMinutes,
  loading = false,
  weekSlots = {},
  onWeekChange,
  minDate = new Date(),
}) => {
  const [view, setView] = useState("month");
  const [cursorMonth, setCursorMonth] = useState(() => {
    const base = selectedDate || new Date();
    return new Date(base.getFullYear(), base.getMonth(), 1);
  });
  const [weekStart, setWeekStart] = useState(() =>
    startOfWeekMonday(selectedDate || new Date())
  );

  const availableDates = useMemo(
    () => buildAvailableDateSet(creneauxDisponibles),
    [creneauxDisponibles]
  );

  const minDay = useMemo(() => {
    const m = new Date(minDate);
    m.setHours(0, 0, 0, 0);
    return m;
  }, [minDate]);

  const monthDays = useMemo(() => {
    const year = cursorMonth.getFullYear();
    const month = cursorMonth.getMonth();
    const first = new Date(year, month, 1);
    const startPad = (first.getDay() + 6) % 7;
    const days = [];
    const start = addDays(first, -startPad);
    for (let i = 0; i < 42; i++) {
      days.push(addDays(start, i));
    }
    return days;
  }, [cursorMonth]);

  const weekDays = useMemo(() => {
    const days = [];
    for (let i = 0; i < 7; i++) {
      days.push(addDays(weekStart, i));
    }
    return days;
  }, [weekStart]);

  const handleSelectDay = (day) => {
    if (day < minDay) return;
    if (!isDateAvailable(day, availableDates)) return;
    onSelectDate(day);
    onSelectSlot?.(null);
  };

  const goPrev = () => {
    if (view === "month") {
      setCursorMonth(new Date(cursorMonth.getFullYear(), cursorMonth.getMonth() - 1, 1));
    } else {
      const next = addDays(weekStart, -7);
      setWeekStart(next);
      onWeekChange?.(next);
    }
  };

  const goNext = () => {
    if (view === "month") {
      setCursorMonth(new Date(cursorMonth.getFullYear(), cursorMonth.getMonth() + 1, 1));
    } else {
      const next = addDays(weekStart, 7);
      setWeekStart(next);
      onWeekChange?.(next);
    }
  };

  const switchView = (nextView) => {
    setView(nextView);
    if (nextView === "week") {
      const ws = startOfWeekMonday(selectedDate || new Date());
      setWeekStart(ws);
      onWeekChange?.(ws);
    }
  };

  const renderDayButton = (day, inMonth = true) => {
    const available = isDateAvailable(day, availableDates);
    const selected = isSameDay(day, selectedDate);
    const past = day < minDay;
    const disabled = past || !available;

    return (
      <button
        key={dateKey(day)}
        type="button"
        disabled={disabled}
        onClick={() => handleSelectDay(day)}
        className={[
          "relative aspect-square rounded-lg text-sm font-medium transition-colors",
          !inMonth && view === "month" ? "text-gray-300" : "",
          disabled ? "text-gray-300 cursor-not-allowed" : "hover:bg-primary-50",
          available && !past ? "text-gray-900" : "",
          selected ? "bg-primary-600 text-white hover:bg-primary-700" : "",
          available && !selected && !past ? "ring-1 ring-primary-200 bg-primary-50/50" : "",
        ]
          .filter(Boolean)
          .join(" ")}
      >
        {day.getDate()}
        {available && !past && !selected && (
          <span className="absolute bottom-1 left-1/2 -translate-x-1/2 w-1 h-1 rounded-full bg-primary-500" />
        )}
      </button>
    );
  };

  const renderSlotChip = (creneau) => {
    const selected = selectedSlot?.id === creneau.id;
    const label = formatSlotPlage(creneau, dureeMinutes);
    return (
      <button
        key={creneau.id}
        type="button"
        onClick={() => onSelectSlot?.(creneau)}
        className={`w-full text-left px-2 py-1.5 rounded-md text-xs sm:text-sm border transition-colors ${
          selected
            ? "border-primary-600 bg-primary-100 text-primary-800 font-medium"
            : "border-gray-200 hover:border-primary-400 hover:bg-primary-50"
        }`}
      >
        {label}
      </button>
    );
  };

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div className="inline-flex rounded-lg border border-gray-200 p-0.5 bg-gray-50">
          <button
            type="button"
            onClick={() => switchView("month")}
            className={`px-3 py-1.5 text-sm rounded-md ${
              view === "month" ? "bg-white shadow-sm text-primary-700 font-medium" : "text-gray-600"
            }`}
          >
            Mois
          </button>
          <button
            type="button"
            onClick={() => switchView("week")}
            className={`px-3 py-1.5 text-sm rounded-md ${
              view === "week" ? "bg-white shadow-sm text-primary-700 font-medium" : "text-gray-600"
            }`}
          >
            Semaine
          </button>
        </div>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={goPrev}
            className="p-1.5 rounded-md border border-gray-200 hover:bg-gray-50"
            aria-label="Période précédente"
          >
            <ChevronLeft className="h-5 w-5" />
          </button>
          <span className="text-sm font-medium text-gray-800 min-w-[10rem] text-center">
            {view === "month" ? formatMonthYear(cursorMonth) : formatWeekRange(weekStart)}
          </span>
          <button
            type="button"
            onClick={goNext}
            className="p-1.5 rounded-md border border-gray-200 hover:bg-gray-50"
            aria-label="Période suivante"
          >
            <ChevronRight className="h-5 w-5" />
          </button>
        </div>
      </div>

      {view === "month" ? (
        <>
          <div className="grid grid-cols-7 gap-1 mb-1">
            {WEEKDAYS.map((wd) => (
              <div key={wd} className="text-center text-xs font-semibold text-gray-500 py-1">
                {wd}
              </div>
            ))}
          </div>
          <div className="grid grid-cols-7 gap-1">
            {monthDays.map((day) =>
              renderDayButton(day, day.getMonth() === cursorMonth.getMonth())
            )}
          </div>
        </>
      ) : (
        <div className="grid grid-cols-2 sm:grid-cols-4 lg:grid-cols-7 gap-2">
          {weekDays.map((day) => {
            const key = dateKey(day);
            const daySlots = weekSlots[key] || (isSameDay(day, selectedDate) ? slots : []);
            const available = isDateAvailable(day, availableDates);
            const past = day < minDay;

            return (
              <div
                key={key}
                className={`border rounded-lg p-2 min-h-[8rem] ${
                  isSameDay(day, selectedDate) ? "border-primary-500 bg-primary-50/30" : "border-gray-200"
                } ${past || !available ? "opacity-50" : ""}`}
              >
                <button
                  type="button"
                  disabled={past || !available}
                  onClick={() => handleSelectDay(day)}
                  className={`w-full text-left mb-2 pb-1 border-b ${
                    isSameDay(day, selectedDate) ? "border-primary-300" : "border-gray-100"
                  }`}
                >
                  <div className="text-xs text-gray-500 uppercase">
                    {WEEKDAYS[(day.getDay() + 6) % 7]}
                  </div>
                  <div className="text-lg font-semibold text-gray-900">{day.getDate()}</div>
                </button>
                <div className="space-y-1 max-h-36 overflow-y-auto">
                  {loading && isSameDay(day, selectedDate) ? (
                    <p className="text-xs text-gray-400">…</p>
                  ) : daySlots.length > 0 ? (
                    daySlots.map(renderSlotChip)
                  ) : available && !past ? (
                    <p className="text-xs text-gray-400">—</p>
                  ) : null}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {view === "month" && selectedDate && (
        <div className="border-t pt-4">
          <h4 className="text-sm font-medium text-gray-700 mb-2 flex items-center">
            <Clock className="h-4 w-4 mr-1 text-primary-600" />
            Créneaux le{" "}
            {selectedDate.toLocaleDateString("fr-FR", {
              weekday: "long",
              day: "numeric",
              month: "long",
            })}
          </h4>
          {loading ? (
            <p className="text-gray-500 text-sm py-2">Chargement des horaires…</p>
          ) : slots.length === 0 ? (
            <p className="text-gray-500 text-sm">Aucun créneau pour cette date.</p>
          ) : (
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-2">
              {slots.map(renderSlotChip)}
            </div>
          )}
        </div>
      )}

      {selectedSlot && (
        <p className="text-sm text-primary-700 bg-primary-50 border border-primary-100 rounded-lg px-3 py-2 flex items-center">
          <Calendar className="h-4 w-4 mr-2 shrink-0" />
          Sélection : {formatSlotPlage(selectedSlot, dureeMinutes)}
        </p>
      )}

      <p className="text-xs text-gray-500">
        Les jours avec un point sont disponibles. Les horaires affichent la plage complète de la prestation.
      </p>
    </div>
  );
};

export default SlotCalendar;
