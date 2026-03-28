/* -------------------------------------------------------
   Copyright (c) [2026] MantaBots
   All rights reserved
   -------------------------------------------------------
   Alliance management class
   ------------------------------------------------------- */
package org.firstinspires.ftc.core.configuration;

// Solverslib includes
import com.seattlesolvers.solverslib.gamepad.GamepadEx;

// Panels includes
import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.configurables.annotations.IgnoreConfigurable;
import com.bylazar.configurables.PanelsConfigurables;

@Configurable
public class Alliance {

    @IgnoreConfigurable
    static final  int               sFeedbackDefaultDuration = 100;

    static public Alliance.Color    COLOR = Alliance.Color.NONE;

    // Registered alliance colors with their values
    public enum Color {

        BLUE(0.0),
        RED(1.0),
        NONE(2.0);

        final double mValue;

        Color(double value)         { mValue = value; }
        public Double getValue()    { return mValue; }
    }

    // Members
    Color                           mColor;

    // Alliance singleton
    @IgnoreConfigurable
    public static Alliance INSTANCE = new Alliance();

    // Private constructor
    private Alliance()              { mColor = Color.NONE; }

    // Alliance accessors
    public  Color               getColor()     {
        update();
        return mColor;
    }
    public  void                setColor(Color color)  {
        mColor = color;
        COLOR = color;
        PanelsConfigurables.INSTANCE.refreshClass(this);
    }
    public  void                reset()                { setColor(Color.NONE); }
    private void                update()               { if(COLOR != mColor) { mColor = COLOR; } }

    // Alliance feedbacks
    public  void                feedback(GamepadEx gamepad, int duration) {
        update();
        if(mColor == Color.BLUE)      { gamepad.gamepad.setLedColor(0,0,1, duration); }
        else if(mColor == Color.RED)  { gamepad.gamepad.setLedColor(1,0,0, duration); }
        else  { gamepad.gamepad.setLedColor(0,0,0, duration); }
    }
    public   void               feedback(GamepadEx gamepad) { feedback(gamepad,sFeedbackDefaultDuration);}

}
