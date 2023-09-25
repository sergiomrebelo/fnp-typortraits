package config;

import Utils.Utilities;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import static processing.core.PApplet.*;

public class GUI {
    // nRows, nCols
    public static final int GRID_ROWS = 12;
    public static final int GRID_COLS = 12;
    // background colour
    public static final int BACKGROUND_COLOR = 10;
    public static final int STROKE_COLOR = 220;
    public static final int [] SECONDARY_COLOR = {182, 19, 47};
    public static final int STROKE_WEIGHT = 3;
    public static final int TEXT_COLOR = 220;
    public static final int FONT_SIZE = 34; //small device: 24
    public static final float TEXT_DESC_SCALAR = 0.8f;
    public static final int LEADING_INC = +2;
    public static final float FONT_SCALAR = 0.65f;
    public static final String FONT_PATH = Utilities.getCurrentDirectory().resolve("Data").resolve("gui-font").resolve("Inter-Medium.ttf").toString();
    public static final int TIMEOUT = 30000;
    public static final int GLOBAL_TIMEOUT = 150000;
    public static final int QR_SIZE = 300;
    public static final String PATH_IMPORT = Utilities.getCurrentDirectory().resolve("Data").resolve("import_params").toString();

    // button height in percents of text size
    public static final float BT_SIZE = 2f;
    public static final int BT_DEFAULT_WIDTH = 150;


    // pop up
    public static final int POPUP_MARGIN = 30; //small device: 15
    public static final float POPUP_LEADING = 1.2f;

    // counter
    public static final int COUNTER_FONT_SIZE = 137; //default: 137 //small decive:85

    // Main
    // welcome screen
    public static final String TITLE_FONT_PATH = Utilities.getCurrentDirectory().resolve("Data").resolve("gui-font").resolve("Inter-Bold.ttf").toString();
    public static final int TITLE_SIZE = 100;
    public static final float CODE_SIZE = 72;
    // loading shape
    public static final String LOADING_SHAPE_PATH = Utilities.getCurrentDirectory().resolve("Data").resolve("loading.svg").toString();
    // flash morphing
    public static final float MORPH_MAX = 0.5f;
    public static final float MORPH_MIN = 0.3f;

    //RGPD
    public static final String RGPD_PT =  Utilities.getCurrentDirectory().resolve("Data").resolve("rgpd_politica-de-privacidade_PT.pdf").toString();
    public static final String RGPD_EN =  Utilities.getCurrentDirectory().resolve("Data").resolve("rgpd_politica-de-privacidade_EN.pdf").toString();

    //keyboard
    public static final int KEYBOARD_MARGIN = 90;
    public static final String EMAIL_SAVE_PATH = "D:\\PHOTOMATON\\email\\";
    public static final String[] HEADERS = new String[]{"email", "code", "data", "time", "lang"};

    public static void export (Logger logger) {
        try {
            String path =
                    Utils.Utilities.getCurrentDirectory() +
                            "\\exports\\" + "gui_param_" +
                            year() + "_" + month()+ "_" + day() + "_" +
                            hour() + "_" + minute() + "_" + second() + ".txt";

            PrintWriter export = createWriter(new File(path));

            Field[] fields = GUI.class.getDeclaredFields();

            for (Field f : fields) {
                String s = f.getType() + " " + f.getName() + " : " + f.get(GUI.class).toString();
                export.println(s);
            }

            export.close();

        } catch (Exception e) {
            logger.log(Level.WARNING, "I cannot export the gui param: "+e.fillInStackTrace());
        }
    }
}
