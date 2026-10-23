package com.kthakare.aiscanner.ocr

data class OcrTable(
    val columns: List<String>,
    val rows: List<List<String>>,
    val pageNumber: Int,
)

data class InvoicePageLayout(
    val headerFields: List<OcrField>,
    val table: OcrTable?,
    val footerFields: List<OcrField>,
    val fields: List<OcrField>,
)

object InvoiceLayoutParser {
    private val money = Regex("""[$£€]?\s*-?\d{1,3}(?:,\d{3})*(?:\.\d{2})|-?\d+\.\d{2}""")
    private val tableHeader = Regex(
        """\b(items?|description|qty|quantity|unit cost|subtotal|line total)\b""",
        RegexOption.IGNORE_CASE,
    )
    private val totalLabel = Regex(
        """^(subtotal|shipping(?:\s*&\s*handling)?|discount(?:.*)?|tax|grand total|amount due|total)\b""",
        RegexOption.IGNORE_CASE,
    )
    private val invoiceNumber = Regex(
        """invoice\s*(?:number|no\.?|#)?\s*[:#]?\s*(\S+)""",
        RegexOption.IGNORE_CASE,
    )
    private val continuation = Regex("""^(sku\s*:|size\b|color\b)""", RegexOption.IGNORE_CASE)
    private val boilerplate = Regex(
        """thank you|your logo here|page \d|support@|questions about your order|please pay""",
        RegexOption.IGNORE_CASE,
    )

    fun parse(pageText: String, pageNumber: Int): InvoicePageLayout {
        val lines = pageText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) {
            return InvoicePageLayout(emptyList(), null, emptyList(), emptyList())
        }

        val headerIndex = lines.indexOfFirst { isTableHeader(it) }
        if (headerIndex < 0) {
            val fields = OcrKeyValueParser.parse(pageText, pageNumber)
            return InvoicePageLayout(
                headerFields = fields,
                table = null,
                footerFields = emptyList(),
                fields = fields,
            )
        }

        val columns = splitCells(lines[headerIndex]).ifEmpty { listOf("Items", "Qty", "Amount") }
        val headerLines = lines.take(headerIndex)
        val afterHeader = lines.drop(headerIndex + 1)
        val footerStart = afterHeader.indexOfFirst { isTotalLine(it) }.let { if (it < 0) afterHeader.size else it }
        val itemLines = afterHeader.take(footerStart)
        val totalLines = afterHeader.drop(footerStart).filterNot { boilerplate.containsMatchIn(it) }

        val headerFields = parseHeader(headerLines, pageNumber)
        val table = OcrTable(
            columns = columns,
            rows = parseItemRows(itemLines, columns.size),
            pageNumber = pageNumber,
        )
        val footerFields = parseTotals(totalLines, pageNumber)
        val fields = buildList {
            addAll(headerFields)
            table.rows.forEachIndexed { index, row ->
                add(
                    OcrField(
                        key = row.firstOrNull().orEmpty().ifBlank { "Item ${index + 1}" },
                        value = row.drop(1).filter { it.isNotBlank() }.joinToString(" · "),
                        pageNumber = pageNumber,
                    ),
                )
            }
            addAll(footerFields)
        }
        return InvoicePageLayout(headerFields, table, footerFields, fields)
    }

    fun isTableHeader(line: String): Boolean {
        val matches = tableHeader.findAll(line.lowercase()).map { it.value.lowercase() }.toSet()
        val hasItem = matches.any { it.startsWith("item") }
        val hasQty = matches.any { it == "qty" || it == "quantity" }
        val hasAmount = matches.any { it in setOf("subtotal", "line total", "unit cost") }
        return (hasItem && hasQty) || (hasItem && hasAmount) || (hasQty && hasAmount)
    }

    private fun isTotalLine(line: String): Boolean {
        val normalized = line.trim().lowercase()
        if (normalized.contains("line total")) return false
        return totalLabel.containsMatchIn(normalized)
    }

    private val orderNumber = Regex(
        """order\s*(?:number|no\.?|#)?\s*[:#]?\s*([#]?\d+)""",
        RegexOption.IGNORE_CASE,
    )

    private fun parseHeader(lines: List<String>, pageNumber: Int): List<OcrField> {
        val fields = mutableListOf<OcrField>()
        val remaining = mutableListOf<String>()
        for (line in lines) {
            if (boilerplate.containsMatchIn(line)) continue
            val invoice = invoiceNumber.find(line)
            val order = orderNumber.find(line)
            when {
                invoice != null && invoice.groupValues[1].isNotBlank() -> {
                    fields += OcrField("Invoice number", invoice.groupValues[1].trim(), pageNumber)
                    val rest = line.replace(invoice.value, "").trim().trim(',', '-', ':')
                    if (rest.isNotEmpty()) remaining += rest
                }
                order != null && !line.contains("date", ignoreCase = true) -> {
                    fields += OcrField("Order", order.groupValues[1].trim(), pageNumber)
                    val rest = line.replace(order.value, "").trim().trim(',', '-', ':')
                    if (rest.isNotEmpty()) remaining += rest
                }
                else -> remaining += line
            }
        }
        val parsed = OcrKeyValueParser.parse(remaining.joinToString("\n"), pageNumber)
        fields += parsed.filterNot { it.key.startsWith("Line ") }
        val unlabeled = parsed.filter { it.key.startsWith("Line ") }.map { it.value }
        if (unlabeled.isNotEmpty()) {
            val key = if (fields.any { it.key.contains("address", ignoreCase = true) }) "Bill to" else "Address"
            fields.add(0, OcrField(key, unlabeled.joinToString("\n"), pageNumber))
        }
        return sortHeader(fields.distinctBy { it.key.lowercase() to it.value })
    }

    private fun sortHeader(fields: List<OcrField>): List<OcrField> {
        val rank = listOf("invoice", "order", "date", "amount due", "bill", "customer", "address", "shipping", "payment")
        return fields.sortedBy { field ->
            val key = field.key.lowercase()
            rank.indexOfFirst { key.contains(it) }.let { if (it < 0) rank.size else it }
        }
    }

    private fun parseItemRows(lines: List<String>, columnCount: Int): List<List<String>> {
        val rows = mutableListOf<MutableList<String>>()
        for (line in lines) {
            if (boilerplate.containsMatchIn(line)) continue
            if (continuation.containsMatchIn(line) && rows.isNotEmpty()) {
                rows.last()[0] = listOf(rows.last()[0], line).filter { it.isNotBlank() }.joinToString("\n")
                continue
            }
            val row = parseItemRow(line, columnCount)
            if (row != null) {
                rows += row.toMutableList()
            } else if (rows.isNotEmpty()) {
                rows.last()[0] = listOf(rows.last()[0], line).filter { it.isNotBlank() }.joinToString("\n")
            }
        }
        return rows
    }

    internal fun parseItemRow(line: String, columnCount: Int): List<String>? {
        val cells = splitCells(line)
        if (cells.size == columnCount) return cells
        val amounts = money.findAll(line).toList()
        if (amounts.isEmpty()) return null
        val last = amounts.last()
        val amount = last.value.trim()
        val before = line.substring(0, last.range.first).trim()
        val qtyMatch = Regex("""^(.*?)(?:\s{2,}|\s+)(\d+(?:\.\d+)?)$""").matchEntire(before)
        if (columnCount <= 3 && qtyMatch != null) {
            return padCells(
                listOf(qtyMatch.groupValues[1].trim(), qtyMatch.groupValues[2], amount),
                columnCount,
            )
        }
        if (columnCount >= 5 && amounts.size >= 2) {
            val unit = amounts[amounts.size - 2]
            val left = line.substring(0, unit.range.first).trim()
            val qtyAndName = Regex("""^(.*?)(?:\s{2,}|\s+)(\d+(?:\.\d+)?)$""").matchEntire(left)
            if (qtyAndName != null) {
                val nameParts = splitCells(qtyAndName.groupValues[1].trim())
                val item = nameParts.firstOrNull().orEmpty()
                val description = nameParts.drop(1).joinToString(" ")
                return padCells(
                    listOf(item, description, unit.value.trim(), qtyAndName.groupValues[2], amount),
                    columnCount,
                )
            }
        }
        if (qtyMatch != null) {
            val row = MutableList(columnCount) { "" }
            row[0] = qtyMatch.groupValues[1].trim()
            if (columnCount >= 2) row[columnCount - 2] = qtyMatch.groupValues[2]
            row[columnCount - 1] = amount
            return row
        }
        if (cells.size >= 2) return padCells(cells, columnCount)
        return null
    }

    private fun parseTotals(lines: List<String>, pageNumber: Int): List<OcrField> {
        val fields = mutableListOf<OcrField>()
        for (line in lines) {
            if (!isTotalLine(line) && money.find(line) == null) continue
            val parsed = OcrKeyValueParser.parse(line, pageNumber).filterNot { it.key.startsWith("Line ") }
            if (parsed.isNotEmpty()) {
                fields += parsed
                continue
            }
            val amount = money.findAll(line).lastOrNull()?.value?.trim() ?: continue
            val key = line.replace(amount, "").trim().trim(':', '-')
            if (key.isNotEmpty()) fields += OcrField(key, amount, pageNumber)
        }
        return fields
    }

    private fun splitCells(line: String): List<String> {
        if (line.contains('\t')) {
            return line.split('\t').map { it.trim() }.filter { it.isNotEmpty() }
        }
        return line.split(Regex("""\s{2,}""")).map { it.trim() }.filter { it.isNotEmpty() }
    }

    private fun padCells(parts: List<String>, count: Int): List<String> {
        if (parts.size == count) return parts
        if (parts.size > count) {
            return parts.take(count - 1) + parts.drop(count - 1).joinToString(" ")
        }
        return parts + List(count - parts.size) { "" }
    }
}
