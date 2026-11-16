package com.gvozd.library.service;

import com.gvozd.library.dao.BookDao;
import com.gvozd.library.exception.NotFoundException;
import com.gvozd.library.exception.ValidationException;
import com.gvozd.library.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Year;
import java.util.List;
import java.util.regex.Pattern;

public class BookService {

    private static final Logger LOG = LoggerFactory.getLogger(BookService.class);

    private static final int MAX_TITLE_LENGTH = 300;
    private static final int MIN_YEAR = 1000;
    private static final int MAX_COPIES = 1000;
    private static final Pattern ISBN_PATTERN = Pattern.compile("^(\\d{13}|\\d{9}[\\dX])$");

    private record BookData(String title, String isbn, Integer year, int copies,
                            List<String> authors) {}

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

    public Book getBook(long id) {
        return bookDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Книга не найдена"));
    }

    public Book addBook(String title, String isbn, String publishYear, String totalCopies,
                        String authors) {
        BookData data = validate(title, isbn, publishYear, totalCopies, authors);

        if (data.isbn() != null && bookDao.existsByIsbn(data.isbn())) {
            throw new ValidationException("Книга с таким ISBN уже есть в каталоге");
        }

        Book saved = bookDao.save(new Book(null, data.title(), data.isbn(), data.year(),
                data.copies(), data.copies()), data.authors());
        LOG.info("Book added: id={}, copies={}, authors={}",
                saved.getId(), saved.getTotalCopies(), data.authors().size());
        return saved;
    }

    public Book updateBook(long id, String title, String isbn, String publishYear,
                           String totalCopies, String authors) {
        Book book = getBook(id);
        BookData data = validate(title, isbn, publishYear, totalCopies, authors);

        int borrowed = book.getTotalCopies() - book.getAvailableCopies();
        if (data.copies() < borrowed) {
            throw new ValidationException(
                    "Нельзя указать меньше экземпляров, чем сейчас на руках: " + borrowed);
        }

        if (data.isbn() != null) {
            bookDao.findByIsbn(data.isbn())
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> {
                        throw new ValidationException("Книга с таким ISBN уже есть в каталоге");
                    });
        }

        book.setTitle(data.title());
        book.setIsbn(data.isbn());
        book.setPublishYear(data.year());
        book.setTotalCopies(data.copies());
        book.setAvailableCopies(data.copies() - borrowed);

        bookDao.update(book, data.authors());
        LOG.info("Book updated: id={}, total={}, available={}, authors={}",
                book.getId(), book.getTotalCopies(), book.getAvailableCopies(),
                data.authors().size());
        return book;
    }

    public void deleteBook(long id) {
        getBook(id);

        if (bookDao.hasLoans(id)) {
            throw new ValidationException(
                    "Нельзя удалить книгу: по ней есть выдачи. История выдач должна сохраниться");
        }
        bookDao.delete(id);
        LOG.info("Book deleted: id={}", id);
    }

    private BookData validate(String title, String isbn, String publishYear,
                              String totalCopies, String authors) {
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
        List<String> authorNames = AuthorNames.parse(authors);

        return new BookData(trimmedTitle, normalizedIsbn, year, copies, authorNames);
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