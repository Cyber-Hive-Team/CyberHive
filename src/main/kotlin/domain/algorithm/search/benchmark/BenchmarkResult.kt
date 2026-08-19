package org.example.domain.algorithm.benchmark

import org.example.domain.model.Warehouse

data class BenchmarkResult(
    val bfsPath: List<Warehouse>,
    val bidirectionalPath: List<Warehouse>,
    val bfsEvaluated: Int,
    val bidirectionalEvaluated: Int,
    val bfsTime: Long,
    val bidirectionalTime: Long
)