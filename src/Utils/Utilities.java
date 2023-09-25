package Utils;


import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Utilities {

	/**
	 * Get current working directory.
	 */
	public static Path getCurrentDirectory() {
		final boolean running_as_jar = Utilities.class.getResource(Utilities.class.getSimpleName() + ".class").toString().startsWith("jar:");
		String path;
		if (running_as_jar) {
			path = new File(Utilities.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParentFile().toPath().toString();
		} else {
			path = System.getProperty("user.dir");
		}
		try {
			path = URLDecoder.decode(path, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return Paths.get(path);
	}

	/**
	 * Resize the given dimensions to fit outside another dimensions.
	 *
	 * @param resize_w
	 * @param resize_h
	 * @param fit_w
	 * @param fit_h
	 * @return
	 */
	public static float[] resizeToFitOutside(float resize_w, float resize_h, float fit_w, float fit_h) {
		final float aspect_ratio_resize = resize_w / resize_h;
		final float aspect_ratio_fit = fit_w / fit_h;
		float x, y;
		if (aspect_ratio_fit >= aspect_ratio_resize) {
			x = fit_w;
			y = fit_w / aspect_ratio_resize;
		} else {
			x = fit_h * aspect_ratio_resize;
			y = fit_h;
		}
		return new float[]{x, y};
	}

	/**
	 * Resize the given dimensions to fit inside another dimensions.
	 *
	 * @param resize_w
	 * @param resize_h
	 * @param fit_w
	 * @param fit_h
	 * @return
	 */
	public static float[] resizeToFitInside(float resize_w, float resize_h, float fit_w, float fit_h) {
		float aspect_ratio_resize = resize_w / resize_h;
		float aspect_ratio_fit = fit_w / fit_h;
		float x, y;
		if (aspect_ratio_fit >= aspect_ratio_resize) {
			x = fit_h * aspect_ratio_resize;
			y = fit_h;
		} else {
			x = fit_w;
			y = fit_w / aspect_ratio_resize;
		}
		return new float[]{x, y};
	}

	public static String getCurrDir() {
		String path;
		boolean running_as_jar = Utilities.class.getResource(Utilities.class.getSimpleName() + ".class").toString().startsWith("jar:");
		if (running_as_jar) {
			path = new File(Utilities.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParentFile().toPath().toString();
		} else {
			path = System.getProperty("user.dir");
		}
		try {
			path = URLDecoder.decode(path, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return path;
	}

}
