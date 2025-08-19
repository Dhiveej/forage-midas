package com.jpmc.midascore.repository;

import com.jpmc.midascore.entity.UserRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // optional but recommended for clarity
public interface UserRepository extends JpaRepository<UserRecord, Long> {
    UserRecord findByUsername(String username);
}
