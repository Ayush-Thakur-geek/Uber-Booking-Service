package com.uber.UberBookingService.repositories;

import com.uber.UberEntityService.models.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    Optional<Driver> findDriverById(long id);

    Optional<Driver> findById(Optional<Long> driverId);

    @Transactional
    @Modifying
    @Query("UPDATE Driver d SET d.isAvailable = false WHERE d.id = :id")
    int updateDriverStatus(@Param("id") long id);

}
