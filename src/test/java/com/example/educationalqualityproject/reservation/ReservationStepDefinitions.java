package com.example.educationalqualityproject.reservation;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ReservationStepDefinitions {

    private ReservationService service;
    private ReservationService.Resource resource;
    private ReservationService.Actor requester;
    private ReservationService.Actor responsible;
    private ReservationService.Actor approver;
    private ReservationService.Reservation reservation;
    private Optional<ReservationService.Reservation> reservationAttempt;

    @Before
    public void setUp() {
        service = new ReservationService(Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC));
    }

    @Given("a restricted resource requested by {string} as requester")
    public void a_restricted_resource_requested_by_as_requester(String requesterId) {
        resource = new ReservationService.Resource("room-1", true);
        requester = new ReservationService.Actor(requesterId, ReservationService.Role.SOLICITANTE);
        reservation = service.request(resource, requester);
    }

    @Given("a responsible actor {string}")
    public void a_responsible_actor(String responsibleId) {
        responsible = new ReservationService.Actor(responsibleId, ReservationService.Role.RESPONSAVEL);
    }

    @When("the responsible approves it")
    public void the_responsible_approves_it() {
        service.approve(reservation, responsible);
    }

    @Then("the reservation status is {string}")
    public void the_reservation_status_is(String statusName) {
        assertEquals(ReservationService.Status.valueOf(statusName.toUpperCase()), reservation.status());
    }

    @Given("a reservation in {string} status")
    public void a_reservation_in_status(String statusName) {
        resource = new ReservationService.Resource("room-1", true);
        requester = new ReservationService.Actor("user-1", ReservationService.Role.SOLICITANTE);
        responsible = new ReservationService.Actor("user-2", ReservationService.Role.RESPONSAVEL);
        reservation = service.request(resource, requester);
        if ("approved".equalsIgnoreCase(statusName)) {
            service.approve(reservation, responsible);
        }
    }

    @Given("a reservation awaiting approval with requester {string} and approver {string}")
    public void a_reservation_awaiting_approval_with_requester_and_approver(String requesterId, String approverId) {
        resource = new ReservationService.Resource("room-1", true);
        requester = new ReservationService.Actor(requesterId, ReservationService.Role.RESPONSAVEL);
        approver = new ReservationService.Actor(approverId, ReservationService.Role.RESPONSAVEL);
        reservation = service.request(resource, requester);
    }

    @When("the same user tries to approve it")
    public void the_same_user_tries_to_approve_it() {
        service.approve(reservation, requester);
    }

    @Given("a resource with maintenance from {string} to {string}")
    public void a_resource_with_maintenance_from_to(String startsAtText, String endsAtText) {
        Instant startsAt = Instant.parse(startsAtText);
        Instant endsAt = Instant.parse(endsAtText);
        resource = new ReservationService.Resource(
                "room-1",
                false,
                List.of(new ReservationService.MaintenancePeriod(startsAt, endsAt))
        );
        requester = new ReservationService.Actor("user-1", ReservationService.Role.SOLICITANTE);
    }

    @When("the requester tries to create a reservation from {string} to {string}")
    public void the_requester_tries_to_create_a_reservation_from_to(String startsAtText, String endsAtText) {
        Instant startsAt = Instant.parse(startsAtText);
        Instant endsAt = Instant.parse(endsAtText);
        reservationAttempt = service.request(resource, requester, startsAt, endsAt);
        reservation = reservationAttempt.orElse(null);
    }

    @Then("the reservation is rejected")
    public void the_reservation_is_rejected() {
        assertNull(reservation);
    }

    @When("the reservation is started")
    public void the_reservation_is_started() {
        service.startUse(reservation);
    }

    @When("the reservation is completed")
    public void the_reservation_is_completed() {
        service.complete(reservation);
    }

    @Then("the status remains {string}")
    public void the_status_remains(String statusName) {
        assertEquals(ReservationService.Status.valueOf(statusName.toUpperCase()), reservation.status());
    }
}
