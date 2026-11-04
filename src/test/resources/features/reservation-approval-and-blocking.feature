Feature: Reservation approval and blocking rules

  Scenario: restricted resource is approved only by a responsible actor
    Given a restricted resource requested by "user-1" as requester
    And a responsible actor "user-2"
    When the responsible approves it
    Then the reservation status is "APROVADA"

  Scenario: same user cannot approve their own restricted reservation
    Given a reservation awaiting approval with requester "user-1" and approver "user-1"
    When the same user tries to approve it
    Then the reservation status is "SOLICITADA"

  Scenario: reservation overlapping maintenance is rejected
    Given a resource with maintenance from "2026-01-01T10:00:00Z" to "2026-01-01T10:10:00Z"
    And a responsible actor "user-2"
    When the requester tries to create a reservation from "2026-01-01T09:59:00Z" to "2026-01-01T10:00:01Z"
    Then the reservation is rejected

  Scenario: approved reservation moves through the valid lifecycle
    Given a reservation in "requested" status
    When the responsible approves it
    And the reservation is started
    And the reservation is completed
    Then the reservation status is "CONCLUIDA"

  Scenario: invalid transitions do not advance the reservation status
    Given a reservation in "requested" status
    When the reservation is started
    Then the status remains "SOLICITADA"

    Given a reservation in "approved" status
    When the reservation is completed
    Then the status remains "APROVADA"
