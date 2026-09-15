package mg.itu.tatitra_app.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

/** Routes liste → détail → confirmation (J7). */
class DestinationsTatitraTest {

    @Test
    fun detailSignalement_construitLaRoute() {
        assertEquals(
            "signalement/abc-123",
            DestinationsTatitra.detailSignalement("abc-123")
        )
    }

    @Test
    fun confirmationResolution_construitLaRoute() {
        assertEquals(
            "signalement/abc-123/confirmation",
            DestinationsTatitra.confirmationResolution("abc-123")
        )
    }

    @Test
    fun routesFixees_restentStables() {
        assertEquals("accueil", DestinationsTatitra.ACCUEIL)
        assertEquals("signalements", DestinationsTatitra.SIGNALEMENTS)
        assertEquals("signalement/{idLocal}", DestinationsTatitra.DETAIL_SIGNALEMENT)
        assertEquals(
            "signalement/{idLocal}/confirmation",
            DestinationsTatitra.CONFIRMATION_RESOLUTION
        )
    }
}
