package com.deepthink.org.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.deepthink.org.modals.Item;

public interface ItemRepository extends JpaRepository<Item, Integer>{

	List<Item> findAllByOrderByIdAsc();

}
