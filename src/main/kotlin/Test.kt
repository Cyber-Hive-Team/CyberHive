package org.example

fun main() {

    val warehouses = parseWarehouses("src/main/resources/warehouses.csv")

    println("Total warehouses: ${warehouses.size}")

    warehouses.forEach {
        println(it)
    }
}

