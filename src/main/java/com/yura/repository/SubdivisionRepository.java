package com.yura.repository;

import org.springframework.stereotype.Repository;
//import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yura.entity.Subdivision;

import java.util.List;

@Repository
public interface SubdivisionRepository extends JpaRepository<Subdivision, Integer> {
	
	@Query("SELECT s FROM Subdivision s WHERE s.parent is null")
    List<Subdivision> findAllOfRoot();
	
    @Query("SELECT s FROM Subdivision s WHERE s.name = :name")
    Subdivision findByName(@Param("name") String name);
    
	@Query(
		value = "select subdivisions.* \n" +
				"from subdivisions \n" +
				"where subdivisions.fk_id_subdivision = :id_parent",
		nativeQuery = true
	)
	List<Subdivision> getAllChildrenByIdParent(@Param("id_parent") int idSubdivision);
}