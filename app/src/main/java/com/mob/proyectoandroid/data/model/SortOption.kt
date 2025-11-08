package com.mob.proyectoandroid.data.model
enum class SortOption(val displayName: String) {
    DATE_CREATED("Fecha de creación"),
    DATE_MODIFIED("Última modificación"),
    TITLE_ASC("Título (A-Z)"),
    TITLE_DESC("Título (Z-A)"),
    PRIORITY_HIGH("Prioridad (Alta primero)"),
    PRIORITY_LOW("Prioridad (Baja primero)");

    companion object {
        fun fromString(value: String): SortOption {
            return values().find { it.name == value } ?: DATE_CREATED
        }
    }
}