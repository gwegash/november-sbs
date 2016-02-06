package uk.ac.cam.november.simulation;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class DepthMap {

    public static final double MIN_DEPTH = 0.0;
    public static final double MAX_DEPTH = 100.0;

    public static final BufferedImage depthMapImage;
    private double scalex;
    private double scaley;
    private double offsetx;
    private double offsety;

    // Statically initialize the depth map image
    static {
        BufferedImage tmp = null;
        try {
            tmp = ImageIO.read(DepthMap.class.getResource("/ui/lake_depth_map.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        depthMapImage = tmp;
    }

    public DepthMap(int w, int h) throws IOException {
        scalex = (double) depthMapImage.getWidth() / (double) w;
        scaley = (double) depthMapImage.getHeight() / (double) h;
        offsetx = w / 2;
        offsety = h / 2;
    }

    /**
     * Interpolate the depth map to produce a float depth at the given
     * coordinates.
     *
     * @param x
     *            The x-coordinate at which to perform the calculation. 0 is the
     *            centre of the image.
     * @param y
     *            The y-coordinate at which to perform the calculation. 0 is the
     *            centre of the image.
     * @return a float depth in meters. The range is set by the values of
     *         MIN_DEPTH and MAX_DEPTH;
     */
    public float getDepth(float x, float y) {
        // Pixel coordinates of the point in the depth map
        double pixelx = (x * scalex) + offsetx;
        double pixely = (y * scaley) + offsety;

        // Fractional pixel values, used for interpolation
        double ix = pixelx - (int) pixelx;
        double iy = pixely - (int) pixely;

        // pixel shifts to get next values
        int shiftx = pixelx > 1 ? -1 : 0;
        int shifty = pixely > 1 ? -1 : 0;

        if (ix > 0.5) {
            shiftx = pixelx < (depthMapImage.getWidth() - 1) ? 1 : 0;
        }

        if (iy > 0.5) {
            shifty = pixely < (depthMapImage.getHeight() - 1) ? 1 : 0;
        }

        // Sum of pixel values
        double v1 = getGrayscale(depthMapImage.getRGB((int) pixelx, (int) pixely));
        double v2 = getGrayscale(depthMapImage.getRGB((int) pixelx, (int) (pixely + shifty)));
        double v3 = getGrayscale(depthMapImage.getRGB((int) (pixelx + shiftx), (int) pixely));
        double v4 = getGrayscale(depthMapImage.getRGB((int) (pixelx + shiftx), (int) (pixely + shifty)));

        double avg = (v1 + v2 + v3 + v4) / 4.0;

        double range = MAX_DEPTH - MIN_DEPTH;
        return (float) ((((255.0-avg) / 255.0) * range) + MIN_DEPTH);
    }

    /**
     * Calculate the average of the R, G and B channels of the pixel, given in
     * ARGB format.
     *
     * @param pixelColor
     *            integer color in ARGB format.
     * @return a double grayscale value.
     */
    private double getGrayscale(int pixelColor) {
        int r = (pixelColor >> 16) & 0xff;
        int g = (pixelColor >> 8) & 0xff;
        int b = pixelColor & 0xff;
        return (double) (r + g + b) / 3.0;
    }

}
