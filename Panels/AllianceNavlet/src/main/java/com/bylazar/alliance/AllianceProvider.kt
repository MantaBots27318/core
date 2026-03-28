/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Panel plugin provider managing alliance display navlet
   ------------------------------------------------------- */
package com.bylazar.alliance

// Core includes
import org.firstinspires.ftc.core.configuration.Alliance


class AllianceProvider {
    var allianceColor = "None"
    internal var lastAllianceColor: String = "None"

    internal val hasChanged: Boolean
        get() = allianceColor != lastAllianceColor

    internal fun updateAllianceColor() {
        allianceColor = "NONE"

        val alliance = Alliance.INSTANCE.getColor();

        if (alliance != null) {
            allianceColor = alliance.toString()
        }
    }
}