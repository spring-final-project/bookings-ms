package com.springcloud.demo.bookingsmicroservice.booking.repository;

import com.springcloud.demo.bookingsmicroservice.booking.dto.FilterBookingDTO;
import com.springcloud.demo.bookingsmicroservice.booking.model.Booking;
import com.springcloud.demo.bookingsmicroservice.booking.model.BookingStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import static org.mockito.BDDMockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class BookingSpecificationTest {

    @InjectMocks
    private BookingSpecification bookingSpecification;

    FilterBookingDTO filters;
    Root<Booking> root;
    CriteriaQuery<?> query;
    CriteriaBuilder builder;

    @BeforeEach
    void setup(){
        filters = new FilterBookingDTO();
        root = mock(Root.class);
        query = mock(CriteriaQuery.class);
        builder = mock(CriteriaBuilder.class);
    }

    @Test
    void withNoFilters(){

        Predicate predicate = bookingSpecification.withFilters(filters).toPredicate(root, query, builder);

        assertThat(predicate).isNull();
    }

    @Test
    void withUserIdFilter(){
        filters.setUserId(UUID.randomUUID().toString());

        bookingSpecification.withFilters(filters).toPredicate(root, query, builder);

        verify(builder).equal(root.get("userId"), UUID.fromString(filters.getUserId()));
    }

    @Test
    void withRoomIdFilter(){
        filters.setRoomId(UUID.randomUUID().toString());

        bookingSpecification.withFilters(filters).toPredicate(root, query, builder);

        verify(builder).equal(root.get("roomId"), UUID.fromString(filters.getRoomId()));
    }

    @Test
    void withStatusFilter(){
        filters.setStatus(BookingStatus.BOOKED);

        bookingSpecification.withFilters(filters).toPredicate(root, query, builder);

        verify(builder).equal(root.get("status"), filters.getStatus());
    }

    @Test
    void withMinRatingFilter(){
        filters.setMinRating(2);

        bookingSpecification.withFilters(filters).toPredicate(root, query, builder);

        verify(builder).greaterThanOrEqualTo(root.get("rating"), filters.getMinRating());
    }

    @Test
    void withMaxRatingFilter(){
        filters.setMaxRating(4);

        bookingSpecification.withFilters(filters).toPredicate(root, query, builder);

        verify(builder).lessThanOrEqualTo(root.get("rating"), filters.getMaxRating());
    }

    @Test
    void withCheckInFilter(){
        filters.setCheckIn(OffsetDateTime.now());

        bookingSpecification.withFilters(filters).toPredicate(root, query, builder);

        verify(builder).greaterThanOrEqualTo(root.get("checkIn"), filters.getCheckIn());
    }

    @Test
    void withCheckOutFilter(){
        filters.setCheckOut(OffsetDateTime.now());

        bookingSpecification.withFilters(filters).toPredicate(root, query, builder);

        verify(builder).lessThanOrEqualTo(root.get("checkOut"), filters.getCheckOut());
    }
}