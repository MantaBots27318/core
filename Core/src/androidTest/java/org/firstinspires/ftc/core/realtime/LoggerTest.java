/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Realtime logger test
   ------------------------------------------------------- */
package realtime;

/* System includes */
import android.util.Log;

/* Qualcomm includes */
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

/* Panels includes */
import com.bylazar.telemetry.PanelsTelemetry;

// Core includes
import org.firstinspires.ftc.core.realtime.Logger;

@TeleOp(name = "LoggerTest", group = "Test")
public class LoggerTest extends LinearOpMode {

    private enum Suite {
        NONE,
        CONSTRUCTOR
    }

    Suite   mCurrentSuite;
    Suite   mNextSuite;

    Logger  mLogger;

    public void runOpMode() {

        telemetry.clear();
        telemetry.addLine("----- LOGGER TESTS -----");
        telemetry.update();
        PanelsTelemetry.INSTANCE.getFtcTelemetry().clear();
        PanelsTelemetry.INSTANCE.getFtcTelemetry().addLine("LOGGER TESTS");
        PanelsTelemetry.INSTANCE.getFtcTelemetry().update();
        Log.i("test","LOGGER TESTS");
        mCurrentSuite = Suite.NONE;
        mNextSuite = Suite.CONSTRUCTOR;

        waitForStart();

        while (opModeIsActive()) {

            if(mNextSuite != mCurrentSuite) {
                this.launch(mNextSuite);
                mNextSuite = mCurrentSuite;
            }

            sleep(200);
        }

    }

    private void launch(Suite suite) {
        if(suite == Suite.CONSTRUCTOR) { this.constructorTest(); }
        else {
            telemetry.addLine("Unknown suite " + suite);
            PanelsTelemetry.INSTANCE.getFtcTelemetry().addLine("Unknown suite " + suite);
        }
    }

    private void constructorTest() {

        telemetry.addLine("--> Constructor test\n");
        PanelsTelemetry.INSTANCE.getFtcTelemetry().addLine("--> Constructor test");
        Log.i("LoggerTest...rTest:0073","----> Constructor test");

        telemetry.addLine("----> Driver station only");
        PanelsTelemetry.INSTANCE.getFtcTelemetry().addLine("----> Driver station only");
        Log.i("LoggerTest...rTest:0077","----> Driver station only");
                
        mLogger = new Logger(telemetry, null, false);
        for (Logger.Target target : Logger.Target.values()) {
            mLogger.error(target, "Error shall only appear on driver station");
            mLogger.warning(target, "Warning shall only appear on driver station");
            mLogger.metric(target, "Target","DriverStation");
            mLogger.info(target, "Info shall only appear on driver station");
            mLogger.debug(target, "Debug shall only appear on driver station");
            mLogger.verbose(target, "Trace shall only appear on driver station");
        }
        mLogger.stop();

        telemetry.addLine("----> Dashboard only");
        PanelsTelemetry.INSTANCE.getFtcTelemetry().addLine("----> Dashboard only");
        Log.i("LoggerTest...rTest:0092","----> Dashboard station only\n");

        mLogger = new Logger(null, PanelsTelemetry.INSTANCE.getTelemetry(),false);
        for (Logger.Target target : Logger.Target.values()) {
            mLogger.error(target, "Error line shall only appear on dashboard");
            mLogger.warning(target, "Warning shall only appear on dashboard");
            mLogger.metric(target, "Target","Dashboard");
            mLogger.info(target, "Info shall only appear on dashboard");
            mLogger.debug(target, "Debug shall only appear on dashboard");
            mLogger.verbose(target, "Trace shall only appear on dashboard");
        }
        mLogger.stop();

        telemetry.addLine("----> System only");
        PanelsTelemetry.INSTANCE.getFtcTelemetry().addLine("----> System only");
        Log.i("LoggerTest...rTest:0107","----> System only");

        mLogger = new Logger(null, null,true);
        for (Logger.Target target : Logger.Target.values()) {
            mLogger.error(target, "Error line shall only appear in system");
            mLogger.warning(target, "Warning line shall only appear in system");
            mLogger.metric(target, "Target","System");
            mLogger.info(target, "Info shall only appear in system");
            mLogger.debug(target, "Debug shall only appear in system");
            mLogger.verbose(target, "Trace shall only appear in system");
        }
        mLogger.stop();

        telemetry.addLine("----> Nowhere");
        PanelsTelemetry.INSTANCE.getFtcTelemetry().addLine("----> Nowhere");
        Log.i("LoggerTest...rTest:0122","----> Nowhere");

        mLogger = new Logger(null, null, false);
        for (Logger.Target target : Logger.Target.values()) {
            mLogger.error(target, "Error line shall not appear");
            mLogger.warning(target, "Warning line shall not appear");
            mLogger.metric(target, "Target","None");
            mLogger.info(target, "Info line shall not appear");
            mLogger.debug(target, "Debug line shall not appear");
            mLogger.verbose(target, "Trace line shall not appear");
        }
        mLogger.stop();

        telemetry.addLine("----> Driver station, system and dashboard");
        PanelsTelemetry.INSTANCE.getFtcTelemetry().addLine("----> Driver station and dashboard");
        Log.i("LoggerTest...rTest:0137","----> Driver station, system and dashboard");

        mLogger = new Logger(telemetry, PanelsTelemetry.INSTANCE.getTelemetry(),true);
        for (Logger.Target target : Logger.Target.values()) {
            mLogger.error(target, "Error line shall appear on driver station, system and dashboard");
            mLogger.warning(target, "Warning line shall appear on driver station, system and dashboard");
            mLogger.metric(target, "Target","All");
            mLogger.info(target, "Info line shall appear on driver station, system and dashboard");
            mLogger.debug(target, "Debug line shall appear on driver station, system and dashboard");
            mLogger.verbose(target, "Trace line shall appear on driver station, system and dashboard");
        }
        mLogger.stop();

        for (Logger.Target target : Logger.Target.values()) {
            mLogger.update(target);
        }


    }



}
