package com.kthakare.aiscanner.ocr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InvoiceLayoutParserTest {
    @Test
    fun forceStoreStylePutsInvoiceAndAddressAboveItems() {
        val text = """
            FORCESTORE
            ForceStore
            6146 Honey Bluff Parkway
            Calder, Michigan, 49628-7978
            United States
            INVOICE #000000005
            Order #000000007
            Order date        Jun 10, 2019, 11:34:45 AM
            Shipping Method
            Flat Rate - Fixed
            Payment Method
            Check / Money order
            Items        Qty        Subtotal
            Luma 7" Analog Watch        3        ${'$'}129.00
            SKU: 24-WG09
            Fusion Backpack        1        ${'$'}59.00
            SKU: 24-MB02
            Helios Endurance Tank        1        ${'$'}32.00
            SKU: MT04-M-Blue
            Subtotal        ${'$'}220.00
            Shipping & Handling        ${'$'}25.00
            Discount        -${'$'}18.20
            Tax        ${'$'}16.65
            Grand Total        ${'$'}243.45
        """.trimIndent()

        val layout = InvoiceLayoutParser.parse(text, pageNumber = 1)
        assertEquals("000000005", layout.headerFields.first { it.key == "Invoice number" }.value)
        assertTrue(layout.headerFields.any { it.key == "Address" && it.value.contains("6146 Honey Bluff") })
        val table = layout.table!!
        assertEquals(listOf("Items", "Qty", "Subtotal"), table.columns)
        assertEquals(3, table.rows.size)
        assertEquals("3", table.rows[0][1])
        assertEquals("$129.00", table.rows[0][2])
        assertTrue(table.rows[0][0].contains("SKU: 24-WG09"))
        assertEquals("$220.00", layout.footerFields.first { it.key.equals("Subtotal", true) }.value)
        assertEquals("$243.45", layout.footerFields.first { it.key.equals("Grand Total", true) }.value)
    }

    @Test
    fun fiveColumnInvoiceKeepsItemDescriptionQtyAndTotals() {
        val text = """
            Sample Client Limited
            100 Sample Street
            Invoice#        000031
            Date        15/06/2011
            Amount Due        £153.00
            Item        Description        Unit Cost        Quantity        Line total
            Sample Product        This is a sample product.        100.00        1        100.00
            Test product        this is a test product.        10.00        1        10.00
            Product 1        This is product 1.        10.00        5        50.00
            Subtotal        170.00
            Discount - 10%        -17.00
            Total        £153.00
        """.trimIndent()

        val layout = InvoiceLayoutParser.parse(text, pageNumber = 1)
        assertEquals("000031", layout.headerFields.first { it.key == "Invoice number" }.value)
        val table = layout.table!!
        assertEquals(5, table.columns.size)
        assertEquals("Sample Product", table.rows[0][0])
        assertEquals("1", table.rows[0][3])
        assertEquals("100.00", table.rows[0][4])
        assertTrue(layout.footerFields.any { it.key.equals("Total", true) })
    }
}
