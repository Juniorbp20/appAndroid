package com.example.gestiondecompras;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.gestiondecompras.utils.DateUtils;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class DateUtilsTest {

    @Test
    public void formatDateForDisplay_formateaYmanejaNull() {
        assertEquals("Sin fecha", DateUtils.formatDateForDisplay(null));
        assertEquals("15/08/2026", DateUtils.formatDateForDisplay(DateUtils.createDate(15, 8, 2026)));
    }

    @Test
    public void formatDateForDB_formatoISO() {
        assertNull(DateUtils.formatDateForDB(null));
        assertEquals("2026-08-15", DateUtils.formatDateForDB(DateUtils.createDate(15, 8, 2026)));
    }

    @Test
    public void formatDateTimeForDisplay_incluyeHora() {
        assertEquals("Sin fecha", DateUtils.formatDateTimeForDisplay(null));
        String out = DateUtils.formatDateTimeForDisplay(DateUtils.createDate(15, 8, 2026));
        assertTrue(out.startsWith("15/08/2026 "));
    }

    @Test
    public void formatMonthYear_noVacio() {
        assertEquals("", DateUtils.formatMonthYear(null));
        assertNotNull(DateUtils.formatMonthYear(DateUtils.createDate(15, 8, 2026)));
    }

    @Test
    public void parseDisplayDate_roundtrip() {
        assertNull(DateUtils.parseDisplayDate(null));
        assertNull(DateUtils.parseDisplayDate("fecha invalida"));
        Date d = DateUtils.parseDisplayDate("15/08/2026");
        assertNotNull(d);
        assertEquals("15/08/2026", DateUtils.formatDateForDisplay(d));
    }

    @Test
    public void isToday_yIsTomorrow() {
        assertFalse(DateUtils.isToday(null));
        assertTrue(DateUtils.isToday(DateUtils.getToday()));
        assertFalse(DateUtils.isTomorrow(DateUtils.getToday()));
        assertTrue(DateUtils.isTomorrow(DateUtils.getTomorrow()));
    }

    @Test
    public void addDays_sumaDias() {
        Date start = DateUtils.createDate(1, 6, 2026);
        assertEquals("06/06/2026", DateUtils.formatDateForDisplay(DateUtils.addDays(start, 5)));
        assertEquals("26/05/2026", DateUtils.formatDateForDisplay(DateUtils.addDays(start, -6)));
    }

    @Test
    public void getDaysDifference_diasEntreFechas() {
        Date start = DateUtils.createDate(1, 6, 2026);
        Date end = DateUtils.createDate(10, 6, 2026);
        assertEquals(9, DateUtils.getDaysDifference(start, end));
        assertEquals(-9, DateUtils.getDaysDifference(end, start));
        assertEquals(0, DateUtils.getDaysDifference(null, end));
        assertEquals(0, DateUtils.getDaysDifference(start, null));
    }

    @Test
    public void getStartAndEndOfDay_limites() {
        Date d = DateUtils.createDate(10, 4, 2026);
        Date start = DateUtils.getStartOfDay(d);
        Date end = DateUtils.getEndOfDay(d);
        Calendar c = Calendar.getInstance();
        c.setTime(start);
        assertEquals(0, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, c.get(Calendar.MINUTE));
        c.setTime(end);
        assertEquals(23, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(59, c.get(Calendar.MINUTE));
        assertNull(DateUtils.getStartOfDay(null));
        assertNull(DateUtils.getEndOfDay(null));
    }

    @Test
    public void isSameDay_comparaDia() {
        assertTrue(DateUtils.isSameDay(
                DateUtils.createDate(15, 8, 2026),
                DateUtils.addDays(DateUtils.createDate(15, 8, 2026), 0)));
        assertFalse(DateUtils.isSameDay(
                DateUtils.createDate(15, 8, 2026),
                DateUtils.createDate(16, 8, 2026)));
        assertFalse(DateUtils.isSameDay(null, DateUtils.getToday()));
    }

    @Test
    public void getDayName_nombresEnEspanol() {
        assertEquals("Domingo", DateUtils.getDayName(DateUtils.createDate(9, 8, 2026)));
        assertEquals("Lunes", DateUtils.getDayName(DateUtils.createDate(10, 8, 2026)));
        assertEquals("", DateUtils.getDayName(null));
    }

    @Test
    public void isPastDue_yIsFuture() {
        assertFalse(DateUtils.isPastDue(null));
        assertTrue(DateUtils.isPastDue(DateUtils.addDays(new Date(), -2)));
        assertFalse(DateUtils.isFuture(DateUtils.addDays(new Date(), -2)));
        assertTrue(DateUtils.isFuture(DateUtils.addDays(new Date(), 2)));
    }
}