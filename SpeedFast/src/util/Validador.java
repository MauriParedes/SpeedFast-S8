package util;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;


public final class Validador {

    private static final int MAX_TEXTO = 100; // coincide con VARCHAR(100) de la BD

    private Validador() {}

    public static String nombre(String texto) {
        String t = obligatorio(texto, "El nombre");
        if (!t.matches("[\\p{L} .'\\-]+")) {
            throw new IllegalArgumentException("El nombre solo puede contener letras, espacios, punto, apóstrofe y guion.");
        }
        return t;
    }

    public static String direccion(String texto) {
        return obligatorio(texto, "La dirección");
    }

    public static Date fecha(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("La fecha es obligatoria (formato AAAA-MM-DD).");
        }
        try {
            return Date.valueOf(LocalDate.parse(texto.trim()));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Fecha inválida. Use el formato AAAA-MM-DD (ej: 2026-10-02).");
        }
    }

    public static Time hora(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("La hora es obligatoria (formato HH:MM o HH:MM:SS).");
        }
        try {
            return Time.valueOf(LocalTime.parse(texto.trim()));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Hora inválida. Use el formato HH:MM o HH:MM:SS (ej: 14:30).");
        }
    }

    public static <T> T seleccionado(T valor, String nombreCampo) {
        if (valor == null) {
            throw new IllegalArgumentException("Debe seleccionar " + nombreCampo + ".");
        }
        return valor;
    }

    private static String obligatorio(String texto, String campo) {
        String t = texto == null ? "" : texto.trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException(campo + " es obligatorio.");
        }
        if (t.length() > MAX_TEXTO) {
            throw new IllegalArgumentException(campo + " no puede superar los " + MAX_TEXTO + " caracteres.");
        }
        return t;
    }
}
