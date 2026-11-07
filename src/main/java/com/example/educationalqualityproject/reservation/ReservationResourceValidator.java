package com.example.educationalqualityproject.reservation;

import com.example.educationalqualityproject.entity.ManagedResource;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationResourceValidator {

    ManagedResource validateReservationResource(String id, LocalDateTime startsAt,
                                                LocalDateTime endsAt, int quantity);

    int getAvailableMaterialQuantity(String id, LocalDateTime startsAt, LocalDateTime endsAt);

        List<ReservationStore.MaterialInterval> getMaterialReservations(
            String id, LocalDateTime startsAt, LocalDateTime endsAt);

    void validateTeacherReservation(String teacherId, String academicPeriodId,
                                    LocalDateTime startsAt, LocalDateTime endsAt);
}