package domain.sorting

import org.example.domain.model.Package

private const val START_INDEX = 0
private const val INDEX_STEP = 1
private const val MIDDLE_DIVISOR = 2

fun sortCargoQueueDescendingByWeight(cargoQueue: MutableList<Package>) {
    if (cargoQueue.isEmpty()) return
    val lastPackageIndex = cargoQueue.lastIndex
    quickSort(cargoQueue, START_INDEX, lastPackageIndex)
}

private fun quickSort(cargoQueue: MutableList<Package>, lowIndex: Int, highIndex: Int) {
    if (lowIndex >= highIndex) return
    val pivotIndex = medianOfThree(cargoQueue, lowIndex, highIndex)
    val pivotWeight = cargoQueue[pivotIndex].weight
    val equalWeightRange = partition(cargoQueue, lowIndex, highIndex, pivotWeight)
    val indexBeforeEqualWeight = equalWeightRange.first - INDEX_STEP
    val indexAfterEqualWeight = equalWeightRange.last + INDEX_STEP
    quickSort(cargoQueue, lowIndex, indexBeforeEqualWeight)
    quickSort(cargoQueue, indexAfterEqualWeight, highIndex)
}

private fun medianOfThree(cargoQueue: MutableList<Package>, lowIndex: Int, highIndex: Int): Int {
    val midIndex = (lowIndex + highIndex) / MIDDLE_DIVISOR
    val lowWeight = cargoQueue[lowIndex].weight
    val midWeight = cargoQueue[midIndex].weight
    val highWeight = cargoQueue[highIndex].weight
    return when {
        (lowWeight in midWeight..highWeight) ||
                (lowWeight in highWeight..midWeight) -> lowIndex

        (midWeight in lowWeight..highWeight) ||
                (midWeight in highWeight..lowWeight) -> midIndex

        else -> highIndex
    }
}

private fun partition(
    cargoQueue: MutableList<Package>, lowIndex: Int, highIndex: Int, pivotWeight: Double
): IntRange {
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
    if (fromIndex == toIndex) return
    val packageToMove = cargoQueue[fromIndex]
    val firstIndexToShift = toIndex + INDEX_STEP
    for (currentIndex in fromIndex downTo firstIndexToShift) {
        val previousIndex = currentIndex - INDEX_STEP
        cargoQueue[currentIndex] = cargoQueue[previousIndex]
    }
    cargoQueue[toIndex] = packageToMove
}
