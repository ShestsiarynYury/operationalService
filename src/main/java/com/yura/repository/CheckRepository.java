/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yura.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yura.entity.CheckExpenditureByPost;
import com.yura.entity.Expenditure;
import java.util.List;
/**
 *
 * @author shyv
 */
public interface CheckRepository extends JpaRepository<Expenditure,Integer> {
    @Query(
		value = "select s.name as name,\n" +
                "exists (select subdivisions.name \n" +
                "			 from subdivisions, expenditures\n" +
                "             where subdivisions.name = expenditures.name_subdivision\n" +
                "				and subdivisions.name = s.name\n" +
                "					and expenditures.type_expenditure = \"current\"\n" +
                "						and date(expenditures.date_and_time) = current_date()) as post\n" +
                "from subdivisions as s",
		nativeQuery = true
	)
	List<CheckExpenditureByPost> checkExpenditureByPost();
}
