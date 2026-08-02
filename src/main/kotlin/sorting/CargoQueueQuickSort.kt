package org.example.sorting

import org.example.domain.model.Package


private const val START_INDEX = 0
private const val INDEX_STEP = 1
fun sortCargoQueueByWeightDescending(cargoQueue: MutableList<Package>) {
    val lastPackageIndex = cargoQueue.lastIndex
    quickSort(cargoQueue, START_INDEX, lastPackageIndex)
}

private fun quickSort(cargoQueue: MutableList<Package>, lowIndex: Int, highIndex: Int) {
    if (lowIndex >= highIndex) {
        return
    }

    val equalWeightRange = partition(cargoQueue, lowIndex, highIndex)
    val indexBeforeEqualWeight = equalWeightRange.first - INDEX_STEP
    val indexAfterEqualWeight = equalWeightRange.last + INDEX_STEP
    quickSort(cargoQueue, lowIndex, indexBeforeEqualWeight)
    quickSort(cargoQueue, indexAfterEqualWeight, highIndex)
}

private fun partition(cargoQueue: MutableList<Package>, lowIndex: Int, highIndex: Int): IntRange {
    val pivotWeight = cargoQueue[highIndex].weight
    var nextHeavierPackageIndex = lowIndex
    for (currentIndex in lowIndex..highIndex) {
        val currentPackage = cargoQueue[currentIndex]
        if (currentPackage.weight > pivotWeight) {
            movePackageWithShift(cargoQueue, currentIndex, nextHeavierPackageIndex)
            nextHeavierPackageIndex += INDEX_STEP
        }
    }

    val equalWeightStartIndex = nextHeavierPackageIndex
    var nextEqualWeightIndex = equalWeightStartIndex

    for (currentIndex in equalWeightStartIndex..highIndex) {
        val currentPackage = cargoQueue[currentIndex]
        if (currentPackage.weight == pivotWeight) {
            movePackageWithShift(cargoQueue, currentIndex, nextEqualWeightIndex)
            nextEqualWeightIndex += INDEX_STEP
        }
    }
    return equalWeightStartIndex until nextEqualWeightIndex
}

private fun movePackageWithShift(cargoQueue: MutableList<Package>, fromIndex: Int, toIndex: Int) {
    if (fromIndex == toIndex) {
        return
    }

    val packageToMove = cargoQueue[fromIndex]
    val firstIndexToShift = toIndex + INDEX_STEP
    for (currentIndex in fromIndex downTo firstIndexToShift) {
        val previousIndex = currentIndex - INDEX_STEP
        cargoQueue[currentIndex] = cargoQueue[previousIndex]
    }
    cargoQueue[toIndex] = packageToMove
}