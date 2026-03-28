/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Alliance management test
   ------------------------------------------------------- */
package configuration;

// Qualcomm includes
import com.bylazar.telemetry.PanelsTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

// Panels includes
import com.bylazar.gamepad.PanelsGamepad;

// Solverslib includes
import com.seattlesolvers.solverslib.command.CommandOpMode;
import com.seattlesolvers.solverslib.gamepad.GamepadKeys;

// Core includes
import org.firstinspires.ftc.core.configuration.Alliance;
import org.firstinspires.ftc.core.control.GamepadCombinedEx;

@TeleOp(name="AllianceTest",group="Test")
public class AllianceTest extends CommandOpMode {


    GamepadCombinedEx  mGamepad1;
    GamepadCombinedEx  mGamepad2;

    @Override
    public void initialize() {

        mGamepad1 = new GamepadCombinedEx(gamepad1, PanelsGamepad.INSTANCE.getFirstManager());
        mGamepad2 = new GamepadCombinedEx(gamepad2, PanelsGamepad.INSTANCE.getSecondManager());

    }


    @Override
    public void initialize_loop() {
        super.initialize_loop();

        mGamepad1.readButtons();
        mGamepad2.readButtons();

        PanelsTelemetry.INSTANCE.getFtcTelemetry().addData("X",mGamepad1.getButton(GamepadKeys.Button.X));
        PanelsTelemetry.INSTANCE.getFtcTelemetry().addData("B",mGamepad1.getButton(GamepadKeys.Button.B));


        if(mGamepad1.wasJustPressed(GamepadKeys.Button.X)) {
            PanelsTelemetry.INSTANCE.getFtcTelemetry().addData("ALLIANCE","BLUE");
            Alliance.INSTANCE.setColor(Alliance.Color.BLUE);
        }
        if(mGamepad1.wasJustPressed(GamepadKeys.Button.B)) {
            PanelsTelemetry.INSTANCE.getFtcTelemetry().addData("ALLIANCE","RED");
            Alliance.INSTANCE.setColor(Alliance.Color.RED);
        }

        PanelsTelemetry.INSTANCE.getFtcTelemetry().update();

        Alliance.INSTANCE.feedback(mGamepad1);
        Alliance.INSTANCE.feedback(mGamepad2);

    }
}
