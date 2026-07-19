package org.example

import java.io.BufferedReader
import java.io.InputStreamReader

class FleetParser {
// إنشاء قائمة فارغة لتخزين البيانات
    fun parseFleet(): List<FleetRaw> {
        val fleetList = mutableListOf<FleetRaw>()
        
// قراءة ملف fleet.csv من مجلد resources
        val inputStream = javaClass.classLoader.getResourceAsStream("fleet.csv")
            ?: throw IllegalArgumentException("File fleet.csv not found in resources!")
            
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
// قراءة السطر الأول (Header) وتخطيه حتى لا يسبب خطأ في البيانات
            val header = reader.readLine() 
            
            var line: String? = reader.readLine()
            while (line != null) {
//الدوران سطر بسطر وتقسيم البيانات (الـ Loop)
                val tokens = line.split(",")
                
                if (tokens.size >= 4) {
//تحويل السطر إلى كائن (Object) وحفظه
                    val fleetRaw = FleetRaw(
                        vehicleId = tokens[0].trim(),
                        currentHubId = tokens[1].trim(),
// معالجة القيم الفارغة إذا كانت مسموحة
                        maxCapacityKg = tokens[2].trim().ifEmpty { null },
                        costPerKm = tokens[3].trim().ifEmpty { null }
                    )
                    fleetList.add(fleetRaw)
                }
                line = reader.readLine()
            }
        }
// إرجاع النتيجة النهائية
        return fleetList
    }
}
