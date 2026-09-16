package org.example.domain.validator

object IdValidator {

    fun validate(
        id: String,
        prefix: String,
        entityName: String
    ): List<FieldViolation> {

        if (id.isBlank()) {
            return listOf(
                FieldViolation(
                    "id",
                    "$entityName ID cannot be blank."
                )
            )
        }

        if (!id.startsWith(prefix) && !isUuid(id)) {
            return listOf(
                FieldViolation(
                    "id",
                    "$entityName ID must start with '$prefix' or be a valid UUID."
                )
            )
        }

        return emptyList()
    }

    private fun isUuid(id: String): Boolean {
        return id.matches(
            Regex(
                "^[0-9a-fA-F]{8}-" +
                        "[0-9a-fA-F]{4}-" +
                        "[0-9a-fA-F]{4}-" +
                        "[0-9a-fA-F]{4}-" +
                        "[0-9a-fA-F]{12}$"
            )
        )
    }
}
