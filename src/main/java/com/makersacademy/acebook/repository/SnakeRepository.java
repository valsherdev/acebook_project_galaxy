package com.makersacademy.acebook.repository;

import com.makersacademy.acebook.model.Snake;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SnakeRepository
        extends JpaRepository<Snake, Long> {

    List<Snake>
    findAllByOrderByScoreDescCreatedAtAsc(
            Pageable pageable
    );
}