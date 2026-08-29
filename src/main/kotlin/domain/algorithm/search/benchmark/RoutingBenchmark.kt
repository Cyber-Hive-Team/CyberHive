package org.example.domain.algorithm.benchmark

import org.example.domain.algorithm.search.BidirectionalBfsRouter
import org.example.domain.algorithm.search.BreadthFirstSearchRouter
import org.example.domain.algorithm.search.EvaluatingWarehouseGraph
import org.example.domain.algorithm.search.WarehouseGraph
import org.example.domain.model.Warehouse

class RoutingBenchmark(
    private val graph: WarehouseGraph
) {

    fun compare(
        start: Warehouse,
        destination: Warehouse
    ): BenchmarkResult {
        val bfsGraph = EvaluatingWarehouseGraph(graph)
        val bidirectionalGraph = EvaluatingWarehouseGraph(graph)
        val bfs = runBfs(bfsGraph, start, destination)
        val bidirectional =
            runBidirectional(
                bidirectionalGraph,
                start,
                destination
            )

        return BenchmarkResult(
            bfsPath = bfs.path,
            bidirectionalPath = bidirectional.path,
            bfsEvaluated = bfsGraph.getEvaluatedCount(),
            bidirectionalEvaluated =
                bidirectionalGraph.getEvaluatedCount(),
            bfsTime = bfs.time,
            bidirectionalTime = bidirectional.time
        )
    }

    private fun runBfs(
        graph: EvaluatingWarehouseGraph,
        start: Warehouse,
        destination: Warehouse
    ): TimedPath {
        val router = BreadthFirstSearchRouter(graph)
        val startTime = System.nanoTime()
        val result = router.findPath(start, destination)

        return TimedPath(
            path = result.path,
            time = System.nanoTime() - startTime
        )
    }

    private fun runBidirectional(
        graph: EvaluatingWarehouseGraph,
        start: Warehouse,
        destination: Warehouse
    ): TimedPath {
        val router = BidirectionalBfsRouter(graph)
        val startTime = System.nanoTime()
        val result = router.findPath(start, destination)

        return TimedPath(
            path = result.path,
            time = System.nanoTime() - startTime
        )
    }

    fun validate(
        result: BenchmarkResult,
        start: Warehouse,
        destination: Warehouse
    ): Boolean {
        if (!hasPaths(result)) return false

        return isValidPath(
            result.bfsPath,
            start,
            destination
        ) &&
                isValidPath(
                    result.bidirectionalPath,
                    start,
                    destination
                ) &&
                sameHopCount(result)
    }

    private fun hasPaths(
        result: BenchmarkResult
    ): Boolean =
        result.bfsPath.isNotEmpty() &&
                result.bidirectionalPath.isNotEmpty()

    private fun isValidPath(
        path: List<Warehouse>,
        start: Warehouse,
        destination: Warehouse
    ): Boolean =
        path.first() == start &&
                path.last() == destination

    private fun sameHopCount(
        result: BenchmarkResult
    ): Boolean =
        result.bfsPath.size ==
                result.bidirectionalPath.size
}

private data class TimedPath(
    val path: List<Warehouse>,
    val time: Long
)