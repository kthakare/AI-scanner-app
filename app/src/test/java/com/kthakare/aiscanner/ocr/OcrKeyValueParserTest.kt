package com.kthakare.aiscanner.ocr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OcrKeyValueParserTest {
    @Test
    fun parsesColonSeparatedPairs() {
        val fields = OcrKeyValueParser.parse(
            """
            Name: Jane Doe
            Invoice #: INV-42
            Total: $18.50
            """.trimIndent(),
            pageNumber = 1,
        )
        assertEquals(
            listOf(
                OcrField("Name", "Jane Doe", 1),
                OcrField("Invoice #", "INV-42", 1),
                OcrField("Total", "$18.50", 1),
            ),
            fields,
        )
    }

    @Test
    fun parsesStackedLabelAndValue() {
        val fields = OcrKeyValueParser.parse(
            """
            Address:
            123 Main Street
            """.trimIndent(),
            pageNumber = 1,
        )
        assertEquals(listOf(OcrField("Address", "123 Main Street", 1)), fields)
    }

    @Test
    fun parsesMultiSpaceColumns() {
        val fields = OcrKeyValueParser.parse("Date        22 Sep 2026", pageNumber = 2)
        assertEquals(listOf(OcrField("Date", "22 Sep 2026", 2)), fields)
    }

    @Test
    fun parsesCommaSeparatedPairsOnOneLine() {
        val fields = OcrKeyValueParser.parse("name : abc, address: xyz", pageNumber = 1)
        assertEquals(
            listOf(
                OcrField("name", "abc", 1),
                OcrField("address", "xyz", 1),
            ),
            fields,
        )
    }

    @Test
    fun keepsCommaInsideASingleValue() {
        val fields = OcrKeyValueParser.parse("Address: 123 Main St, Apt 4", pageNumber = 1)
        assertEquals(listOf(OcrField("Address", "123 Main St, Apt 4", 1)), fields)
    }

    @Test
    fun unlabeledLinesBecomeNumberedRows() {
        val fields = OcrKeyValueParser.parse(
            """
            Corner Market
            Thank you
            """.trimIndent(),
            pageNumber = 1,
        )
        assertEquals(
            listOf(
                OcrField("Line 1", "Corner Market", 1),
                OcrField("Line 2", "Thank you", 1),
            ),
            fields,
        )
    }

    @Test
    fun copyTextIsTabSeparated() {
        val extraction = OcrExtraction(
            fields = listOf(
                OcrField("Name", "Ada", 1),
                OcrField("City", "London", 2),
            ),
            rawText = "unused",
        )
        assertEquals("Key\tValue\tPage\nName\tAda\t1\nCity\tLondon\t2", extraction.toCopyText())
        assertTrue(OcrExtraction(emptyList(), "plain").toCopyText() == "plain")
    }
}
