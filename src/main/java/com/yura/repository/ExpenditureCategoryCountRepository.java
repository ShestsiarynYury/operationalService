package com.yura.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yura.entity.ExpenditureCategoryCount;
import java.util.List;

@Repository
public interface ExpenditureCategoryCountRepository extends CrudRepository<ExpenditureCategoryCount, Integer> {

	@Query(
		value = "select expenditures_categories.count_category from expenditures_categories inner join expenditures on expenditures_categories.fk_id_expenditure = expenditures.id_expenditure and expenditures.type_expenditure = :type and expenditures.fk_id_subdivision in (select subdivisions.id_subdivision from subdivisions where subdivisions.name = :nameSubdivision) and expenditures_categories.fk_id_category in (select categories.id_category from categories where categories.name = :nameCategory)",
		nativeQuery = true
	)
	int getCountCategoryBySubdivisionNameAndTypeExpenditure(@Param("type") String type, @Param("nameSubdivision") String nameSubdivision, @Param("nameCategory") String nameCategory);
    
    @Query(
        value = "select expenditure_category_counts.*\n" +
                "from expenditure_category_counts\n" +
                "inner join expenditures on expenditures.id_expenditure = expenditure_category_counts.id_expenditure\n" +
                "							and expenditures.type_expenditure = 'state'\n" +
                "								and expenditures.id_expenditure = :id_expenditure",
        nativeQuery = true
    )
    List<ExpenditureCategoryCount> findListExpenditureCategoryCountForStateByIdExpenditure(@Param("id_expenditure") int idExpenditure);
    
    @Query(
        value = "select expenditure_category_counts.*\n" +
                "from expenditure_category_counts\n" +
                "inner join expenditures on expenditures.id_expenditure = expenditure_category_counts.id_expenditure\n" +
                "							and expenditures.type_expenditure = 'list'\n" +
                "								and expenditures.id_expenditure = :id_expenditure",
        nativeQuery = true
    )
    List<ExpenditureCategoryCount> findListExpenditureCategoryCountForListByIdExpenditure(@Param("id_expenditure") int idExpenditure);
    
    @Query(
        value = "select expenditure_category_counts.*\n" +
                "from expenditure_category_counts\n" +
                "inner join expenditures on expenditures.id_expenditure = expenditure_category_counts.id_expenditure\n" +
                "							and expenditures.type_expenditure = 'current'\n" +
                "								and expenditures.id_expenditure = :id_expenditure",
        nativeQuery = true
    )
    List<ExpenditureCategoryCount> findListExpenditureCategoryCountForCurrentByIdExpenditure(@Param("id_expenditure") int idExpenditure);
}