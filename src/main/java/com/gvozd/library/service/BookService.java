package com.gvozd.library.service;

import com.gvozd.library.dao.BookDao;
import com.gvozd.library.exception.ValidationException;
import com.gvozd.library.model.Book;

import java.time.Year;
import java.util.List;
import java.util.regex.Pattern;

public class BookService {

    private static final int MAX_TITLE_LENGTH = 300;
    private static final int MIN_YEAR = 1000;
    private static final int MAX_COPIES = 1000;
    private static final Pattern ISBN_PATTERN = Pattern.compile("^(\\d{13}|\\d{9}[\\dX])$");

    private final BookDao bookDao;
    private final int pageSize;

    public BookService(BookDao bookDao, int pageSize) {
        this.bookDao = bookDao;
        this.pageSize = pageSize;
    }

    public CatalogPage getCatalogPage(String query, int requestedPage) {
        String q = (query == null) ? "" : query.trim();

        long total = bookDao.count(q);
        int totalPages = (int) Math.max(1, Math.ceil((double) total / pageSize));

        int page = Math.min(Math.max(requestedPage, 1), totalPages);
        int offset = (page - 1) * pageSize;

        List<Book> books = bookDao.findPage(q, pageSize, offset);
        return new CatalogPage(books, q, page, totalPages);
    }

    public Book addBook(String title, String isbn, String publishYear, String totalCopies) {
        String trimmedTitle = (title == null) ? "" : title.trim();
        if (trimmedTitle.isEmpty() || trimmedTitle.length() > MAX_TITLE_LENGTH) {
            throw new ValidationException(
                    "Название должно содержать от 1 до " + MAX_TITLE_LENGTH + " символов");
        }

        String normalizedIsbn = normalizeIsbn(isbn);
        if (normalizedIsbn != null && !ISBN_PATTERN.matcher(normalizedIsbn).matches()) {
            throw new ValidationException("ISBN должен содержать 10 или 13 цифр");
        }

        Integer year = parseYear(publishYear);
        int copies = parseCopies(totalCopies);

        if (normalizedIsbn != null && bookDao.existsByIsbn(normalizedIsbn)) {
            throw new ValidationException("Книга с таким ISBN уже есть в каталоге");
        }

        return bookDao.save(new Book(null, trimmedTitle, normalizedIsbn, year, copies, copies));
    }

    private static String normalizeIsbn(String isbn) {
        if (isbn == null) {
            return null;
        }
        String cleaned = isbn.replace("-", "").replace(" ", "").toUpperCase();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private static Integer parseYear(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        int year;
        try {
            year = Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Год издания должен быть числом");
        }
        int maxYear = Year.now().getValue() + 1;
        if (year < MIN_YEAR || year > maxYear) {
            throw new ValidationException(
                    "Год издания должен быть от " + MIN_YEAR + " до " + maxYear);
        }
        return year;
    }

    private static int parseCopies(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Укажите количество экземпляров");
        }
        int copies;
        try {
            copies = Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Количество экземпляров должно быть числом");
        }
        if (copies < 1 || copies > MAX_COPIES) {
            throw new ValidationException(
                    "Количество экземпляров должно быть от 1 до " + MAX_COPIES);
        }
        return copies;
    }
}