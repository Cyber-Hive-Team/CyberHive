package org.example.data.datasource.local.csv

import java.io.File

internal fun replaceCsvFile(
    filePath: String,
    content: String
) {
    val targetFile = File(filePath)
    val tempFile = File("$filePath.tmp")

    tempFile.writeText(content)
    tempFile.copyTo(targetFile, overwrite = true)
    tempFile.delete()
}
