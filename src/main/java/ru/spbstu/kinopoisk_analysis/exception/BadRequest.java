package ru.spbstu.kinopoisk_analysis.exception;

public class BadRequest extends RuntimeException {

    public BadRequest(String message) {
        super(message);
    }
}
