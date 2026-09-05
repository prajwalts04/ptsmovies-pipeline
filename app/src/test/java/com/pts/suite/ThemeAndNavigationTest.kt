package com.pts.suite

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pts.suite.ui.components.AppDestination
import com.pts.suite.ui.components.DockTabItem
import com.pts.suite.ui.theme.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeAndNavigationTest {

    @Test
    fun testPitchBlackAndEmeraldColors() {
        // Assert exact hex values
        assertEquals(Color(0xFF040404), PitchBlack)
        assertEquals(Color(0xFF0A0A0A), DarkSurface)
        assertEquals(Color(0xFF22C55E), EmeraldGreen)
        assertEquals(Color(0xFF404048), SketchBorder)
        assertEquals(Color(0xFFFFFFFF), Graphite100)
        assertEquals(Color(0xFFEAEAEA), Graphite200)
    }

    @Test
    fun testSketchShapeRadiiDefinitions() {
        val shape = SketchShape as AsymmetricSketchShape
        assertEquals(25.5.dp, shape.tlX)
        assertEquals(3.5.dp, shape.tlY)
        assertEquals(3.5.dp, shape.trX)
        assertEquals(22.5.dp, shape.trY)
        assertEquals(22.5.dp, shape.brX)
        assertEquals(3.5.dp, shape.brY)
        assertEquals(3.5.dp, shape.blX)
        assertEquals(25.5.dp, shape.blY)

        val shapeAlt = SketchShapeAlt as AsymmetricSketchShape
        assertEquals(3.5.dp, shapeAlt.tlX)
        assertEquals(25.5.dp, shapeAlt.tlY)
        assertEquals(22.5.dp, shapeAlt.trX)
        assertEquals(3.5.dp, shapeAlt.trY)

        val shapeSm = SketchShapeSm as AsymmetricSketchShape
        assertEquals(14.dp, shapeSm.tlX)
        assertEquals(3.dp, shapeSm.tlY)
    }

    @Test
    fun testDockTabItemBadgeCounting() {
        val tabWithBadge = DockTabItem(
            id = "hub",
            label = "Hub",
            icon = androidx.compose.material.icons.Icons.Default.Bolt,
            badgeCount = 5
        )
        assertEquals(5, tabWithBadge.badgeCount)
        assertEquals("hub", tabWithBadge.id)
    }

    @Test
    fun testAppDestinationEnums() {
        assertEquals("PTS Stream", AppDestination.STREAM.title)
        assertEquals("PTS Hub", AppDestination.HUB.title)
        assertEquals("PTS Files", AppDestination.FILES.title)
        assertEquals("PTS Vault (Wallet & Notes)", AppDestination.VAULT.title)
        assertEquals("PTS Mobile SSH", AppDestination.TERMINAL.title)
    }
}
