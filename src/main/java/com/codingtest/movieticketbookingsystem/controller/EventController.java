package com.codingtest.movieticketbookingsystem.controller;

import com.codingtest.movieticketbookingsystem.entity.Event;
import com.codingtest.movieticketbookingsystem.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
class EventController {

    @Autowired private EventService service;

    @PostMapping
    public Event create(@RequestBody Event e) { return service.create(e); }

    @GetMapping
    public List<Event> all() { return service.getAll(); }

    @PutMapping("/{id}")
    public Event update(@PathVariable Long id, @RequestBody Event e) {
        return service.update(id, e);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "Deleted";
    }
}