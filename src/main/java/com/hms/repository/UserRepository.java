package com.hms.repository;

import com.hms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	@Query("""
			    SELECT DISTINCT u
			    FROM User u
			    LEFT JOIN FETCH u.roles
			    WHERE u.email = :email
			""")
	Optional<User> findByEmailWithRoles(@Param("email") String email);

	Optional<User> findByMobileNumber(String mobileNumber);

	@Query("""
			    SELECT
			        u.id AS id,
			        u.email AS email,
			        u.firstName AS firstName,
			        u.lastName AS lastName,
			        u.mobileNumber AS mobileNumber,
			        u.enabled AS enabled,
			        u.passwordChangeRequired AS passwordChangeRequired,
			        r.name AS roleName
			    FROM User u
			    LEFT JOIN u.roles r
			    ORDER BY u.id
			""")
	List<UserProjection> findAllUserProjections();

	@Query("""
			    SELECT
			        u.id AS id,
			        u.email AS email,
			        u.firstName AS firstName,
			        u.lastName AS lastName,
			        u.mobileNumber AS mobileNumber,
			        u.enabled AS enabled,
			        u.passwordChangeRequired AS passwordChangeRequired,
			        r.name AS roleName
			    FROM User u
			    JOIN u.roles r
			    WHERE r.name = 'PATIENT'
			    ORDER BY u.id
			""")
	List<UserProjection> findAllPatientProjections();

	@Query("""
			    SELECT
			        u.id AS id,
			        u.email AS email,
			        u.firstName AS firstName,
			        u.lastName AS lastName,
			        u.mobileNumber AS mobileNumber,
			        u.enabled AS enabled,
			        u.passwordChangeRequired AS passwordChangeRequired,
			        r.name AS roleName
			    FROM User u
			    JOIN u.roles r
			    WHERE u.id = :id
			      AND r.name = 'PATIENT'
			""")
	List<UserProjection> findPatientProjectionById(@Param("id") Long id);

	@Query("""
			    SELECT
			        u.id AS id,
			        u.email AS email,
			        u.firstName AS firstName,
			        u.lastName AS lastName,
			        u.mobileNumber AS mobileNumber,
			        u.enabled AS enabled,
			        u.passwordChangeRequired AS passwordChangeRequired,
			        r.name AS roleName
			    FROM User u
			    JOIN u.roles r
			    WHERE u.mobileNumber = :mobile
			      AND r.name = 'PATIENT'
			""")
	List<UserProjection> findPatientProjectionByMobile(
			@Param("mobile") String mobile);

}