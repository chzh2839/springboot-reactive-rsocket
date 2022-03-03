package com.example.reactiversocketserver.service;

import com.example.reactiversocketserver.repository.ItemRepository;
import org.springframework.stereotype.Service;

@Service
public class RSocketService {
    private final ItemRepository repository;

    public RSocketService(ItemRepository repository) {
        this.repository = repository;
    }
}
