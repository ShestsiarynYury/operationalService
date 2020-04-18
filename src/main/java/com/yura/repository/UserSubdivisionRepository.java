package com.yura.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yura.entity.UserSubdivision;

@Repository
public interface UserSubdivisionRepository extends CrudRepository<UserSubdivision, Integer> {
	
	@Override
	UserSubdivision save(UserSubdivision userSubdivision);
	
	@Query(
		value = "delete users_subdivisions.*\n" +
				"from users_subdivisions\n" +
				"where users_subdivisions.id_user_subdivision = :idUserSubdivisionQ",
		nativeQuery = true
	)
	void customDeleteById(@Param("idUserSubdivisionQ") int idUserSubdivision);
    
    @Query(
		value = "select count(subdivisions.id_subdivision)\n" +
                "from subdivisions inner join users_subdivisions on subdivisions.id_subdivision = users_subdivisions.fk_id_subdivision\n" +
					"inner join users on users.id_user = users_subdivisions.fk_id_user\n" +
						"and users.id_User = :idUserQ\n" +
                        "and subdivisions.name = :subdivisionNameQ;",
		nativeQuery = true
	)
    int findSubdivisionCanShow(@Param("subdivisionNameQ") String subdivisionName, @Param("idUserQ") int idUser);
    
    @Query(
		value = "select count(subdivisions.id_subdivision)\n" +
                "from subdivisions inner join users_subdivisions on subdivisions.id_subdivision = users_subdivisions.fk_id_subdivision\n" +
					"inner join users on users.id_user = users_subdivisions.fk_id_user\n" +
						"and users.id_User = :idUserQ\n" +
                        "and subdivisions.name = :subdivisionNameQ\n" +
                        "and users_subdivisions.is_edit = 1;",
		nativeQuery = true
	)
    int findSubdivisionCanShowAndEdit(@Param("subdivisionNameQ") String subdivisionName, @Param("idUserQ") int idUser);
    
}
