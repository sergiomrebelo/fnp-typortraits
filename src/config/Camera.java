package config;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import static processing.core.PApplet.*;

public class Camera {
    public static final int DEVICEID = 0;
    public static final int SIZE_W = 1920;
    public static final int SIZE_H = 1080;
    public static final int FPS = 30; //30
    public static final int INTERVAL_CHECK_CONFIG = 3600000;

    public static float EXPOSURE = -4; // min: -13; max: -1
    public static final float AUTO_EXPOSURE = 0;
    public static int GAIN = 2;
    public static int TEMPERATURE = 0;
    public static final int AUTO_WB = 0;
    public static int GAMMA = 120; //240
    public static int SHARPNESS = 20;
    public static int CONSTRAT = 10; //10
    public static int WHITE_BALANCE = 4500;
    public static int LUMA = 3;
    public static int DIGITAL_ZOOM = 100;

    public static void importParam (String path, Logger logger) {
        String data = "";
       try {
           File paramsFile = new File(path);
           Scanner reader = new Scanner(paramsFile);
           while (reader.hasNextLine()) {
               data += reader.nextLine();
           }
           reader.close();

           String[] params = data.split(",");
           for (int i=0; i<params.length; i++) {
               switch (i) {
                   case 0:
                       EXPOSURE = parseInt(params[i]);
                       break;
                   case 1:
                       GAIN = parseInt(params[i]);
                       break;
                   case 2:
                       TEMPERATURE = parseInt(params[i]);
                       break;
                   case 3:
                       GAMMA = parseInt(params[i]);
                       break;
                   case 4:
                       SHARPNESS = parseInt(params[i]);
                       break;
                   case 5:
                       CONSTRAT = parseInt(params[i]);
                       break;
                   case 6:
                       WHITE_BALANCE = parseInt(params[i]);
                       break;
                   case 7:
                       LUMA = parseInt(params[i]);
                       break;
                   case 8:
                       DIGITAL_ZOOM = parseInt(params[i]);
                       break;
                   default:
                       logger.log(Level.INFO, "the param "+i+" is ignored (value: "+parseInt(params[i])+")");
                       break;
               }
           }

       } catch (Exception e) {
           logger.log(Level.WARNING, "I cannot import the camera param: "+e.fillInStackTrace());
       }
    }

    public static void export (Logger logger) {
        try {
            String path =
                    Utils.Utilities.getCurrentDirectory() +
                            "\\exports\\" + "camera_param_" +
                            year() + "_" + month()+ "_" + day() + "_" +
                            hour() + "_" + minute() + "_" + second() + ".txt";

            PrintWriter export = createWriter(new File(path));

            Field[] fields = Camera.class.getDeclaredFields();

            for (Field f : fields) {
                String s = f.getType() + " " + f.getName() + " : " + f.get(Camera.class).toString();
                export.println(s);
            }

            export.close();

        } catch (Exception e) {
            logger.log(Level.WARNING, "I cannot export the camera param: "+e.fillInStackTrace());
        }
    }
}
