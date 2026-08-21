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
        val bidirectionalGraph =
            EvaluatingWarehouseGraph(graph)

        val bfsRouter =
            BreadthFirstSearchRouter(bfsGraph)

        val bidirectionalRouter =
            BidirectionalBfsRouter(
                bidirectionalGraph
            )

        val bfsStart = System.nanoTime()
        val bfsResult =
            bfsRouter.findPath(start, destination)
        val bfsTime =
            System.nanoTime() - bfsStart

        val bidirectionalStart =
            System.nanoTime()

        val bidirectionalResult =
            bidirectionalRouter.findPath(
                start,
                destination
            )

        val bidirectionalTime =
            System.nanoTime() - bidirectionalStart

        return BenchmarkResult(
            bfsPath = bfsResult.path,
            bidirectionalPath =
                bidirectionalResult.path,
            bfsEvaluated =
                bfsGraph.getEvaluatedCount(),
            bidirectionalEvaluated =
                bidirectionalGraph.getEvaluatedCount(),
            bfsTime = bfsTime,
            bidirectionalTime =
                bidirectionalTime
        )
    }

    fun validate(
        result: BenchmarkResult,
        start: Warehouse,
        destination: Warehouse
    ): Boolean {

        if (
            result.bfsPath.isEmpty() ||
            result.bidirectionalPath.isEmpty()
        ) {
            return false
        }

        val bfsValid =
            result.bfsPath.first() == start &&
                    result.bfsPath.last() == destination

        val bidirectionalValid =
            result.bidirectionalPath.first() == start &&
                    result.bidirectionalPath.last() ==
                    destination

        val sameHops =
            result.bfsPath.size ==
                    result.bidirectionalPath.size

        return bfsValid &&
                bidirectionalValid &&
                sameHops
    }
}