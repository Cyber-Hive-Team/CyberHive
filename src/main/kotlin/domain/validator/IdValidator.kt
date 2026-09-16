package org.example.domain.validator

object IdValidator {

    fun validate(
        id: String,
        prefix: String,
        entityName: String
    ): List<FieldViolation> {

        val violations = mutableListOf<FieldViolation>()

        if (id.isBlank()) {
            violations.add(
                FieldViolation(
                    "id",
                    "$entityName ID cannot be blank."
                )
            )
        }else if (!id.startsWith(prefix) && !isUuid(id)) {
            violations.add(
                FieldViolation(
                    "id",
                    "$entityName ID must start with '$prefix' or be a valid UUID."
                )
            )
        }

        return violations
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
