package com.yura.repository;

import com.yura.entity.Title;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface TitleRepository extends JpaRepository<Title, Integer> {
	Optional<Title> findByName(String name);
}