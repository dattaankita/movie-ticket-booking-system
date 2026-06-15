package com.codingtest.movieticketbookingsystem.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationUtils {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    private PaginationUtils() {
    }

    public static Pageable of(Integer page, Integer size) {
        return of(page, size, Sort.unsorted());
    }

    public static Pageable of(Integer page, Integer size, Sort sort) {
        int resolvedPage = page != null && page >= 0 ? page : 0;
        int resolvedSize = size != null && size > 0 ? Math.min(size, MAX_SIZE) : DEFAULT_SIZE;
        return PageRequest.of(resolvedPage, resolvedSize, sort);
    }
}
