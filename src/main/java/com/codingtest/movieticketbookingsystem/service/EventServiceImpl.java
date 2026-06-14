package com.codingtest.movieticketbookingsystem.service;

import com.codingtest.movieticketbookingsystem.entity.Event;
import com.codingtest.movieticketbookingsystem.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventServiceImpl implements EventService{

    @Autowired
    private EventRepository repo;

    public Event create(Event e) {
        return repo.save(e);
    }
    public List<Event> getAll() {
        return repo.findAll();
    }
    public Event get(Long id) {
        return repo.findById(id).orElseThrow();
    }

    public Event update(Long id, Event e) {
        Event ev = get(id);
        ev.setName(e.getName());
        ev.setLocation(e.getLocation());
        ev.setEventDate(e.getEventDate());
        ev.setTotalSeats(e.getTotalSeats());
        return repo.save(ev);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}