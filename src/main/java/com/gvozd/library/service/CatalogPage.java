package com.gvozd.library.service;

import com.gvozd.library.model.Book;

import java.util.List;

public record CatalogPage(List<Book> books, String query, int page, int totalPages) {
}