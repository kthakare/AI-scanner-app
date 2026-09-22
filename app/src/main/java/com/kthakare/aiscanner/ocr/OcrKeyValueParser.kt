package com.kthakare.aiscanner.ocr

data class OcrField(
    val key: String,
    val value: String,
    val pageNumber: Int,
)

data class OcrExtraction(
    val fields: List<OcrField>,
    val rawText: String,
) {
    fun toCopyText(): String {
        if (fields.isEmpty()) return rawText
        val includePage = fields.any { it.pageNumber > 1 }
        return buildString {
            append("Key\tValue")
            if (includePage) append("\tPage")
            append('\n')
            fields.forEach { field ->
                append(field.key)
                append('\t')
                append(field.value.replace('\n', ' '))
                if (includePage) {
                    append('\t')
                    append(field.pageNumber)
                }
                append('\n')
            }
        }.trimEnd()
    }
}

object OcrKeyValueParser {
    private val sameLineSeparator = Regex("""^(.{1,80}?)\s*[:：=|]\s+(.+)$""")
    private val trailingLabel = Regex("""^(.{1,80}?)[:：]\s*$""")
    private val multiSpace = Regex("""\s{2,}""")

    fun parse(pageText: String, pageNumber: Int): List<OcrField> {
        val lines = pageText
            .lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.equals("(no text found)", ignoreCase = true) }

        if (lines.isEmpty()) return emptyList()

        val fields = mutableListOf<OcrField>()
        var index = 0
        var unlabeled = 0
        while (index < lines.size) {
            val line = lines[index]
            val stacked = trailingLabel.matchEntire(line)
            val sameLine = sameLineSeparator.matchEntire(line)
            when {
                stacked != null -> {
                    val key = stacked.groupValues[1].trim()
                    val next = lines.getOrNull(index + 1)
                    if (key.isNotEmpty() && next != null && trailingLabel.matchEntire(next) == null) {
                        fields += OcrField(key = key, value = next, pageNumber = pageNumber)
                        index += 2
                    } else {
                        unlabeled += 1
                        fields += OcrField(key = "Line $unlabeled", value = line, pageNumber = pageNumber)
                        index += 1
                    }
                }
                sameLine != null -> {
                    fields += OcrField(
                        key = sameLine.groupValues[1].trim(),
                        value = sameLine.groupValues[2].trim(),
                        pageNumber = pageNumber,
                    )
                    index += 1
                }
                line.contains('\t') -> {
                    val parts = line.split('\t').map { it.trim() }.filter { it.isNotEmpty() }
                    if (parts.size >= 2) {
                        fields += OcrField(key = parts.first(), value = parts.drop(1).joinToString(" "), pageNumber = pageNumber)
                    } else {
                        unlabeled += 1
                        fields += OcrField(key = "Line $unlabeled", value = line, pageNumber = pageNumber)
                    }
                    index += 1
                }
                multiSpace.containsMatchIn(line) -> {
                    val parts = line.split(multiSpace).map { it.trim() }.filter { it.isNotEmpty() }
                    if (parts.size == 2) {
                        fields += OcrField(key = parts[0], value = parts[1], pageNumber = pageNumber)
                    } else {
                        unlabeled += 1
                        fields += OcrField(key = "Line $unlabeled", value = line, pageNumber = pageNumber)
                    }
                    index += 1
                }
                else -> {
                    unlabeled += 1
                    fields += OcrField(key = "Line $unlabeled", value = line, pageNumber = pageNumber)
                    index += 1
                }
            }
        }
        return fields
    }
}
