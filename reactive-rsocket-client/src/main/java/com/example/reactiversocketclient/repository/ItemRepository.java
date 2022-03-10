package com.example.reactiversocketclient.repository;

import com.example.reactiversocketclient.domain.Item;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends ReactiveCrudRepository<Item, String> {

}
