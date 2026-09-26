package com.tech10x.ukm.repository;

import com.tech10x.ukm.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UsersRepository extends JpaRepository<Users,Long> {

    Optional<Users> findByUserId(@Param("userId")String userId);

    Optional<Users> findByEmail(@Param("email")String email);
}
