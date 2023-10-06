package config;

import Utils.Utilities;
import approach6.Portraitor;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import static processing.core.PApplet.*;

public class Portraits {
    public final static int INPUT_IMAGE_RESIZE = 1000;
    // [min, max]
    public static int THRESHOLD_BRIGHTNESS_MIN = 80;

    public static int THRESHOLD_BRIGHTNESS_MAX = 220; // background color
    // [DEPTH MAX, BRIGHTNESS DIVISION TOLERANCE, BRIGHTNESS THRESHOLD FILTER]
    public static int QUADTREE_SETUP_DEPTH_MAX = 8;
    public static int QUADTREE_SETUP_BRIGHTNESS_DIVISION_TOLERANCE = 5; //10
    public static int QUADTREE_SETUP_BRIGHTNESS_THRESHOLD_FILTER = 220; //220


    public static int [] FONT_SIZES =  {6, 12, 16, 18, 24, 28, 32, 36}; //{8, 12, 16, 18, 24, 22, 32, 42, 48, 56, 64, 72, 80};
    public static float  ELE_MENTS_ANGLE_MAX = (float) Math.toRadians(90);

    // [ATTEMPTS, MIN_AREA_ON_INK_BLACK, OUTLINE]
    public static float PLACEMENT_BLACK_ATTEMPTS = 15;
    public static float PLACEMENT_BLACK_MIN_AREA_ON_INK_BLACK = 0.7F; // 0 more darker => 1 light
    public static float PLACEMENT_BLACK_OUTLINE = 1;


    // [ATTEMPTS, MIN_AREA_ON_INK_BLACK, OUTLINE]
    public static float PLACEMENT_WHITE_ATTEMPTS = 1;
    public static float PLACEMENT_WHITE_MIN_AREA_ON_INK_WHITE = 1;
    public static float PLACEMENT_WHITE_OUTLINE = 6;

    public final static String CHARACTERS = "abcdefghijlmnopqrstuvxz"; // ABCDEFGHIJLMNOPQRSTUVXZ abcdefghijlmnopqrstuvxz 0123456789 .,:;!?-()
    public final static String FONT_PATH = Utilities.getCurrentDirectory().resolve("Data").resolve("LinLibertine_R.otf").toString();

    // capturer
    public final static int OPENCV_SIZE = 2; //default: 4
    public final static float ASPECT_RATIO_FRAMING = 1; //default: 4 / 5
    public final static boolean ROTATE_CAMERA = true;
    public final static int ROTATE_FLAG = 1;

    // only look to the first line of file the first line
    public static void importFontSizes (String path, Logger logger) {
        String data = "";
        try {
            File paramsFile = new File(path);
            Scanner reader = new Scanner(paramsFile);
            while (reader.hasNextLine()) {
                data += reader.nextLine();
                break;
            }
            reader.close();
            String[] params = data.split(",");
            int [] fontsizes = new int [params.length];
            for (int i=0; i<fontsizes.length; i++) {
                fontsizes[i] = parseInt(params[i]);
            }
            FONT_SIZES = fontsizes;
            logger.log(Level.INFO, "font size on portraits updated for " + params.toString());
        } catch (Exception e) {
            logger.log(Level.WARNING, "I cannot import the font size params on portraits: "+e.fillInStackTrace());
        }
    }

    // only look to thye first line of file
    public static void importParam (String path, Logger logger) {
        String data = "";
        try {
            File paramsFile = new File(path);
            Scanner reader = new Scanner(paramsFile);
            while (reader.hasNextLine()) {
                data += reader.nextLine();
                break;
            }
            reader.close();

            String[] params = data.split(",");
            for (int i=0; i<params.length; i++) {
                switch (i) {
                    case 0:
                        THRESHOLD_BRIGHTNESS_MIN = parseInt(params[i]);
                        break;
                    case 1:
                        THRESHOLD_BRIGHTNESS_MAX = parseInt(params[i]);
                        break;
                    case 2:
                        QUADTREE_SETUP_DEPTH_MAX = parseInt(params[i]);
                        break;
                    case 3:
                        QUADTREE_SETUP_BRIGHTNESS_DIVISION_TOLERANCE = parseInt(params[i]);
                        break;
                    case 4:
                        QUADTREE_SETUP_BRIGHTNESS_THRESHOLD_FILTER = parseInt(params[i]);
                        break;
                    case 5:
                        PLACEMENT_BLACK_ATTEMPTS = parseInt(params[i]);
                        break;
                    case 6:
                        PLACEMENT_BLACK_MIN_AREA_ON_INK_BLACK = parseInt(params[i]);
                        break;
                    case 7:
                        PLACEMENT_BLACK_OUTLINE = parseInt(params[i]);
                        break;
                    case 8:
                        PLACEMENT_WHITE_ATTEMPTS = parseInt(params[i]);
                        break;
                    case 9:
                        ELE_MENTS_ANGLE_MAX = parseInt(params[i]);
                        break;
                    case 10:
                        PLACEMENT_WHITE_MIN_AREA_ON_INK_WHITE = parseInt(params[i]);
                        break;
                    case 11:
                        PLACEMENT_WHITE_OUTLINE = parseInt(params[i]);
                        break;
                    default:
                        logger.log(Level.INFO, "the portrait' param "+i+" is ignored (value: "+parseInt(params[i])+")");
                        break;
                }
            }

        } catch (Exception e) {
            logger.log(Level.WARNING, "I cannot import the portraits param: "+e.fillInStackTrace());
        }
    }

    public static void export (Logger logger) {
        try {
            String path =
                    Utils.Utilities.getCurrentDirectory() +
                            "\\exports\\" + "portraitor_param_" +
                            year() + "_" + month()+ "_" + day() + "_" +
                            hour() + "_" + minute() + "_" + second() + ".txt";

            PrintWriter export = createWriter(new File(path));

            Field[] fields = Portraits.class.getDeclaredFields();

            for (Field f : fields) {
                String s = f.getType() + " " + f.getName() + " : " + f.get(Portraits.class).toString();
                export.println(s);
            }

            String s = "FONT_SIZES: [";

            for (int fs : FONT_SIZES) {
                s += " " + fs;
            }

            s += "]";

            export.println(s);
            export.close();

        } catch (Exception e) {
            logger.log(Level.WARNING, "I cannot export the portraits param: "+e.fillInStackTrace());
        }
    }

}
