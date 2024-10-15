package com.springcloud.demo.bookingsmicroservice.booking.repository;

import com.springcloud.demo.bookingsmicroservice.booking.dto.FilterBookingDTO;
import com.springcloud.demo.bookingsmicroservice.booking.model.Booking;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class BookingSpecification {

    public Specification<Booking> withFilters(FilterBookingDTO filters){
        return ((root, _, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(filters.getUserId() != null){
                predicates.add(builder.equal(root.get("userId"), UUID.fromString(filters.getUserId())));
            }
            if(filters.getRoomId() != null){
                predicates.add(builder.equal(root.get("roomId"), UUID.fromString(filters.getRoomId())));
            }
            if(filters.getStatus() != null){
                predicates.add(builder.equal(root.get("status"), filters.getStatus()));
            }
            if(filters.getMinRating() != null){
                predicates.add(builder.greaterThanOrEqualTo(root.get("rating"), filters.getMinRating()));
            }
            if(filters.getMaxRating() != null){
                predicates.add(builder.lessThanOrEqualTo(root.get("rating"), filters.getMaxRating()));
            }
            if(filters.getCheckIn() != null){
                predicates.add(builder.greaterThanOrEqualTo(root.get("checkIn"), filters.getCheckIn()));
            }
            if(filters.getCheckOut() != null){
                predicates.add(builder.lessThanOrEqualTo(root.get("checkOut"), filters.getCheckOut()));
            }

            Predicate[] arrayPredicates = predicates.toArray(new Predicate[0]);

            return builder.and(arrayPredicates);
        });
    }
}
