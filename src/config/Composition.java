package config;


import Utils.Utilities;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import static processing.core.PApplet.*;


public class Composition {
    // Text
    public static final String SECTION_MARK = "//";
    public static final String PARAGRAPH_MARK = "/";
    public static final String SECTION_IDENTIFIER = "§";
    public static final String PARAGRAPH_IDENTIFIER = "¶";
    public static final String FONT_PATH = Utilities.getCurrentDirectory().resolve("Data").resolve("LinLibertine_R.otf").toString();
    public static final int MAX_FONT_SIZE = 300;
    public static final float MIN_FONT_SIZE_PER = .02f;
    public static final float LEADING = .20f;
    public static final float TOP_MARGIN = 0;
    public static final int BOTTOM_MARGIN = 20;
    public static final float INC_TYPEFACE = 1f;

    // Composition
    public final static int BACKGROUND = 255;
    public final static float HEIGHT_RATIO = 1.4f;
    public final static float MARGIN_FACTOR = .03f;
    public final static float SIZE_FOOTER_PER = 0.017f;
    public final static String LOGO_PATH = Utilities.getCurrentDirectory().resolve("Data").resolve("logo_fcm_vector.svg").toString();
    public final static int DEFAULT_WIDTH = 1016;
    public final static int DEFAULT_HEIGHT = 306;
    public final static int TITLE_CHAR_LIMIT = 80;

    // global
    public final static String URL = "www.fcm.org.pt/retratos/";
    public final static String CODES_QUEUE_FILE = Utilities.getCurrentDirectory().resolve("Data").resolve("codes_queue.txt").toString();
    //public final static String SAVE_DIR = "C:\\xampp\\htdocs\\public\\portraits\\";
    public final static String SAVE_DIR = "D:\\PHOTOMATON\\portraits\\";
    public final static int APP_BACKGROUND = 200;

    public static void export (Logger logger) {
        try {
            String path =
                    Utils.Utilities.getCurrentDirectory() +
                            "\\exports\\" + "composition_param_" +
                            year() + "_" + month()+ "_" + day() + "_" +
                            hour() + "_" + minute() + "_" + second() + ".txt";

            PrintWriter export = createWriter(new File(path));

            Field[] fields = Composition.class.getDeclaredFields();

            for (Field f : fields) {
                String s = f.getType() + " " + f.getName() + " : " + f.get(Composition.class).toString();
                export.println(s);
            }

            export.close();

        } catch (Exception e) {
            logger.log(Level.WARNING, "I cannot export the composition param: "+e.fillInStackTrace());
        }
    }
}
