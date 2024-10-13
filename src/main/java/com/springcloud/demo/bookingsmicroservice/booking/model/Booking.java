package com.springcloud.demo.bookingsmicroservice.booking.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @CreationTimestamp()
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "receipt_url")
    private String receiptUrl;

    @Column(name = "check_in")
    private LocalDateTime checkIn;

    @Column(name = "check_out")
    private LocalDateTime checkOut;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "room_id")
    private String roomId;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private Integer rating;

    private String review;
}
