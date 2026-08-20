package org.example.domain.algorithm.benchmark

import org.example.domain.model.Warehouse

private const val NANOSECONDS_PER_MILLISECOND = 1_000_000.0

class BenchmarkReporter {

    fun printResults(result: BenchmarkResult) {
        printAlgorithm(
            "Standard BFS",
            result.bfsPath,
            result.bfsEvaluated,
            result.bfsTime
        )

        printAlgorithm(
            "Bidirectional BFS",
            result.bidirectionalPath,
            result.bidirectionalEvaluated,
            result.bidirectionalTime
        )

        val saved =
            result.bfsEvaluated -
                    result.bidirectionalEvaluated

        println("\n--- Efficiency ---")
        println("Warehouses saved: $saved")
    }

    private fun printAlgorithm(
        name: String,
        path: List<Warehouse>,
        evaluated: Int,
        time: Long
    ) {
        println("\n--- $name ---")
        println("Path: ${path.joinToString(" -> ") { it.id }}")
        println("Hops: ${path.size - 1}")
        println("Warehouses evaluated: $evaluated")
        println(
            "Execution time: " +
                    "${time / NANOSECONDS_PER_MILLISECOND} ms"
        )
    }

    fun printValidation(valid: Boolean) {
        println("\n--- Validation ---")
        println(
            if (valid) {
                "Both algorithms produced valid shortest-hop paths."
            } else {
                "Validation failed."
            }
        )
    }
}