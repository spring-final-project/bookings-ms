package com.springcloud.demo.bookingsmicroservice.booking.repository;

import com.springcloud.demo.bookingsmicroservice.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, String>, JpaSpecificationExecutor<Booking> {

    @Query("SELECT b FROM Booking b " +
            "WHERE ((?1 BETWEEN b.checkIn AND b.checkOut) " +
            "OR (?2 BETWEEN b.checkIn AND b.checkOut)) " +
            "AND b.status = BOOKED " +
            "AND (b.userId = ?3 OR b.roomId = ?4)")
    Optional<Booking> findBookingsByRange(LocalDateTime checkIn, LocalDateTime checkOut, String userId, String roomId);
}
