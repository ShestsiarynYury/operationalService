package com.yura.repository;

import java.time.LocalDate;
import java.util.List;

import com.yura.entity.Expenditure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenditureRepository extends JpaRepository<Expenditure, Integer> {

    @Query(
        value = "select expenditures.* from expenditures where expenditures.fk_id_subdivision in (select subdivisions.id_subdivision from subdivisions where subdivisions.name = :subdivisionName)",
        nativeQuery = true
    )
    List<Expenditure> findExpenditure(@Param("subdivisionName") String subdivisionName);
    
    @Query(
        value = "select * from expenditures where expenditures.type_expenditure = 'state' and expenditures.name_subdivision in (select subdivisions.name from subdivisions where subdivisions.name = :subdivisionName)",
        nativeQuery = true
    )
    Expenditure findTypeStateExpenditure(@Param("subdivisionName") String subdivisionName);

    @Query(
        value = "select * from expenditures where expenditures.type_expenditure = 'list' and expenditures.name_subdivision in (select subdivisions.name from subdivisions where subdivisions.name = :subdivisionName)",
        nativeQuery = true
    )
    Expenditure findTypeListExpenditure(@Param("subdivisionName") String subdivisionName);


    @Query(
        value = "select * from expenditures where expenditures.type_expenditure = 'current' and date(expenditures.date_and_time) = :date and expenditures.name_subdivision in (select subdivisions.name from subdivisions where subdivisions.name = :subdivisionName)",
        nativeQuery = true)
    Expenditure findTypeCurrentExpenditure(@Param("subdivisionName") String subdivisionName, @Param("date") String date);

    @Query(
        value = "select expenditures.* from expenditures where date(expenditures.date_and_time) = curdate() and expenditures.type_expenditure = 'current'",
        nativeQuery = true)
    List<Expenditure> findAllExpenditureTypeCurrentByCurrentDate();
    
    @Query(
        value = "select expenditures.* from expenditures where expenditures.type_expenditure = 'state'",
        nativeQuery = true)
    List<Expenditure> findAllExpenditureTypeState();
    
    @Query(
        value = "select expenditures.* from expenditures where expenditures.type_expenditure = 'list'",
        nativeQuery = true)
    List<Expenditure> findAllExpenditureTypeList();

    @Query(
        value = "select expenditures.* from expenditures where expenditures.name_subdivision = :nameSubdivision",
        nativeQuery = true)
    List<Expenditure> findAllExpenditureByNameSubdivision(@Param("nameSubdivision") String nameSubdivision);
    
    @Query(
        value = "select max(expenditures.id_expenditure)\n" +
                "from expenditures",
        nativeQuery = true
    )
    int getMaxId();

}