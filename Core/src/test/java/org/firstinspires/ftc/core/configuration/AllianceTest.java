/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Unit tests for Alliance class
   ------------------------------------------------------- */
package org.firstinspires.ftc.core.configuration;

// System includes
import java.lang.reflect.Field;
import java.lang.reflect.Method;

// Junit includes
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

// Mockito includes
import static org.mockito.Mockito.mock;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

// FTC Controller includes
import com.qualcomm.robotcore.hardware.Gamepad;

// Solvers lib includes
import com.seattlesolvers.solverslib.gamepad.GamepadEx;

// Panels includes
import com.bylazar.configurables.PanelsConfigurables;

@RunWith(MockitoJUnitRunner.class)
public class AllianceTest {

    // Gamepad is compiled for the Android runtime — Byte Buddy cannot instrument it.
    // We use a plain subclass that records setLedColor() calls instead.
    static class TestGamepad extends Gamepad {
        double lastR, lastG, lastB;
        int    lastDuration;
        boolean called = false;

        @Override
        public void setLedColor(double r, double g, double b, int duration) {
            lastR = r; lastG = g; lastB = b; lastDuration = duration;
            called = true;
        }
    }

    @Mock GamepadEx  mMockGamepadEx;

    TestGamepad mTestGamepad;

    // ── Setup ────────────────────────────────────────────────────────────────

    @BeforeClass
    public static void mockPanelsConfigurables() throws Exception {
        // sun.misc.Unsafe bypasses the final constraint on the Kotlin object INSTANCE.
        // Class.forName avoids a direct import of this internal JDK API.
        Class<?> unsafeClass    = Class.forName("sun.misc.Unsafe");
        Field    theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
        theUnsafeField.setAccessible(true);
        Object   unsafe         = theUnsafeField.get(null);

        Field  instanceField = PanelsConfigurables.class.getDeclaredField("INSTANCE");
        Method staticBase    = unsafeClass.getMethod("staticFieldBase",   Field.class);
        Method staticOffset  = unsafeClass.getMethod("staticFieldOffset", Field.class);
        Method putObject     = unsafeClass.getMethod("putObject", Object.class, long.class, Object.class);

        Object base   = staticBase.invoke(unsafe, instanceField);
        long   offset = (long) staticOffset.invoke(unsafe, instanceField);
        putObject.invoke(unsafe, base, offset, mock(PanelsConfigurables.class));
    }

    @Before
    public void setUp() {
        mTestGamepad = new TestGamepad();
        mMockGamepadEx.gamepad = mTestGamepad;
        Alliance.COLOR = Alliance.Color.NONE;
        Alliance.INSTANCE.reset();
    }

    // ── Singleton ────────────────────────────────────────────────────────────

    @Test
    public void singletonAlwaysReturnsSameInstance() {
        assertSame(Alliance.INSTANCE, Alliance.INSTANCE);
    }

    // ── Initial state ────────────────────────────────────────────────────────

    @Test
    public void initialColorIsNone() {
        assertEquals(Alliance.Color.NONE, Alliance.INSTANCE.getColor());
    }

    @Test
    public void initialStaticColorIsNone() {
        assertEquals(Alliance.Color.NONE, Alliance.COLOR);
    }

    // ── setColor ─────────────────────────────────────────────────────────────

    @Test
    public void setColorToBlue() {
        Alliance.INSTANCE.setColor(Alliance.Color.BLUE);
        assertEquals(Alliance.Color.BLUE, Alliance.INSTANCE.getColor());
    }

    @Test
    public void setColorToRed() {
        Alliance.INSTANCE.setColor(Alliance.Color.RED);
        assertEquals(Alliance.Color.RED, Alliance.INSTANCE.getColor());
    }

    @Test
    public void setColorUpdatesStaticField() {
        Alliance.INSTANCE.setColor(Alliance.Color.BLUE);
        assertEquals(Alliance.Color.BLUE, Alliance.COLOR);
    }

    // ── reset ────────────────────────────────────────────────────────────────

    @Test
    public void resetRestoresToNone() {
        Alliance.INSTANCE.setColor(Alliance.Color.RED);
        Alliance.INSTANCE.reset();
        assertEquals(Alliance.Color.NONE, Alliance.INSTANCE.getColor());
    }

    @Test
    public void resetUpdatesStaticField() {
        Alliance.INSTANCE.setColor(Alliance.Color.BLUE);
        Alliance.INSTANCE.reset();
        assertEquals(Alliance.Color.NONE, Alliance.COLOR);
    }

    // ── update() — external COLOR change synced via getColor() ───────────────

    @Test
    public void getColorSyncsFromExternalStaticChange() {
        Alliance.COLOR = Alliance.Color.RED;
        assertEquals(Alliance.Color.RED, Alliance.INSTANCE.getColor());
    }

    @Test
    public void getColorSyncsFromNoneAfterBlue() {
        Alliance.INSTANCE.setColor(Alliance.Color.BLUE);
        Alliance.COLOR = Alliance.Color.NONE;
        assertEquals(Alliance.Color.NONE, Alliance.INSTANCE.getColor());
    }

    // ── Color enum values ────────────────────────────────────────────────────

    @Test
    public void blueEnumValue() {
        assertEquals(0.0, Alliance.Color.BLUE.getValue(), 0.0);
    }

    @Test
    public void redEnumValue() {
        assertEquals(1.0, Alliance.Color.RED.getValue(), 0.0);
    }

    @Test
    public void noneEnumValue() {
        assertEquals(2.0, Alliance.Color.NONE.getValue(), 0.0);
    }

    // ── feedback ─────────────────────────────────────────────────────────────

    @Test
    public void feedbackBlueCallsCorrectLedColor() {
        Alliance.INSTANCE.setColor(Alliance.Color.BLUE);
        Alliance.INSTANCE.feedback(mMockGamepadEx);
        assertEquals(0.0, mTestGamepad.lastR, 0.0);
        assertEquals(0.0, mTestGamepad.lastG, 0.0);
        assertEquals(1.0, mTestGamepad.lastB, 0.0);
        assertEquals(Alliance.sFeedbackDefaultDuration, mTestGamepad.lastDuration);
    }

    @Test
    public void feedbackRedCallsCorrectLedColor() {
        Alliance.INSTANCE.setColor(Alliance.Color.RED);
        Alliance.INSTANCE.feedback(mMockGamepadEx);
        assertEquals(1.0, mTestGamepad.lastR, 0.0);
        assertEquals(0.0, mTestGamepad.lastG, 0.0);
        assertEquals(0.0, mTestGamepad.lastB, 0.0);
        assertEquals(Alliance.sFeedbackDefaultDuration, mTestGamepad.lastDuration);
    }

    @Test
    public void feedbackNoneCallsCorrectLedColor() {
        Alliance.INSTANCE.feedback(mMockGamepadEx);
        assertEquals(0.0, mTestGamepad.lastR, 0.0);
        assertEquals(0.0, mTestGamepad.lastG, 0.0);
        assertEquals(0.0, mTestGamepad.lastB, 0.0);
        assertEquals(Alliance.sFeedbackDefaultDuration, mTestGamepad.lastDuration);
    }

    @Test
    public void feedbackWithCustomDuration() {
        Alliance.INSTANCE.setColor(Alliance.Color.BLUE);
        Alliance.INSTANCE.feedback(mMockGamepadEx, 500);
        assertEquals(0.0, mTestGamepad.lastR, 0.0);
        assertEquals(0.0, mTestGamepad.lastG, 0.0);
        assertEquals(1.0, mTestGamepad.lastB, 0.0);
        assertEquals(500, mTestGamepad.lastDuration);
    }

    @Test
    public void feedbackDefaultDurationIs100() {
        assertEquals(100, Alliance.sFeedbackDefaultDuration);
    }

    @Test
    public void feedbackSyncsColorFromStaticFieldBeforeLed() {
        // COLOR changed externally — feedback must reflect it without explicit setColor
        Alliance.COLOR = Alliance.Color.RED;
        Alliance.INSTANCE.feedback(mMockGamepadEx);
        assertEquals(1.0, mTestGamepad.lastR, 0.0);
        assertEquals(0.0, mTestGamepad.lastG, 0.0);
        assertEquals(0.0, mTestGamepad.lastB, 0.0);
    }
}
