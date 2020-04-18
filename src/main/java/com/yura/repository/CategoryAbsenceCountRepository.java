package com.yura.repository;

import com.yura.entity.CategoryAbsenceCount;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryAbsenceCountRepository extends CrudRepository<CategoryAbsenceCount, Integer> {

}