package com.codingtest.movieticketbookingsystem.service;

import com.codingtest.movieticketbookingsystem.entity.Event;

import java.util.List;

public interface EventService {

    void delete(Long id);

    Event update(Long id, Event e);

    List<Event> getAll();

    Event create(Event e);
}
