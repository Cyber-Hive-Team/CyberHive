package org.example.data.circuitbreaker

enum class CircuitBreakerState {
    CLOSED,
    OPEN,
    HALF_OPEN
}
