package com.chris.texteye;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.ImageViewState;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.opencv.core.Core.add;
import static org.opencv.imgproc.Imgproc.Canny;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.findContours;

public class WordExtractor {

    private static final String TAG = "SubimageExtractortyDD";
    private Mat baseImage;
    private Mat wordsSelectionCanvas;
    private boolean SCALE_IMAGE_DOWN_ON = true;
    private SubsamplingScaleImageView imageView, imageView2;
    private AppCompatActivity UIActivity;
    private Set<Rect> selectedWords = new HashSet<>();

    public WordExtractor(AppCompatActivity UIA, SubsamplingScaleImageView iv, SubsamplingScaleImageView iv2) {
        imageView = iv;
        imageView2 = iv2;
        UIActivity = UIA;
    }

    public String loadImage(final String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        if (SCALE_IMAGE_DOWN_ON)
            options.inSampleSize = 2;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
        if (bitmap == null) {
            String errorMessage = "The bitmat was not found. Cannot proceed.";
            Log.d(TAG, "loadImage: " + errorMessage);
            return errorMessage;
        }
        bitmap = RotateBitmap(bitmap, 90.f);
        baseImage = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC3);
        wordsSelectionCanvas = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC3);
        Utils.bitmapToMat(bitmap, baseImage);
        baseImage.copyTo(wordsSelectionCanvas);
        return null;
    }

    public void displayImage() {
        displayMat(wordsSelectionCanvas, imageView);
    }

    private static Bitmap RotateBitmap(final Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void displayOnUIThread(final SubsamplingScaleImageView iv, final Bitmap bitmap) {
        UIActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageViewState imageViewState = iv.getState();
                iv.setImage(ImageSource.bitmap(bitmap));
                if (imageViewState != null)
                    iv.setScaleAndCenter(imageViewState.getScale(), imageViewState.getCenter());
            }
        });
    }

    private void displayMat(final Mat extractedImage, final SubsamplingScaleImageView imageView) {
        Bitmap outbit = Bitmap.createBitmap(extractedImage.width(), extractedImage.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(extractedImage, outbit);
        displayOnUIThread(imageView, outbit);
    }

    public Bitmap extract(final int mouseX, final int mouseY) {

        int roiEnlargement = 0;
        int roiXShift = 0;

        Rect examinedArea = prepareExaminedArea(mouseX, mouseY, roiXShift, roiEnlargement);

        Mat examinedAreaImage = new Mat(baseImage, examinedArea);

        Mat detectedEdges = createCannyEdges(examinedAreaImage);

        List<Rect> rects = createBoundingBoxesForContours(detectedEdges);

        rects = sortRectangles(rects);

        Rect mouse = new Rect(mouseX - examinedArea.x, mouseY - examinedArea.y, 8, 8);

        rects = unionInterceptedRects(rects);
        Mat thirdImage = new Mat();
        baseImage.submat(examinedArea).copyTo(thirdImage);
        //Mat thirdImage = new Mat(baseImage.clone(), examinedArea);
        for (Rect rect : rects) {
            Imgproc.rectangle(thirdImage, rect.tl(), rect.br(), new Scalar(122, 32, 23));
            Imgproc.line(thirdImage, new Point(rect.x, rect.y + rect.height / 2), new Point(rect.x + rect.width, rect.y + rect.height / 2), new Scalar(100, 0, 200));
        }
        Imgproc.circle(thirdImage, mouse.tl(), 10, new Scalar(0, 19, 255), -1);
        Rect line = new Rect(0, mouseY - examinedArea.y - 20, examinedArea.width, 40);
        Imgproc.rectangle(thirdImage, line.tl(), line.br(), new Scalar(22, 232, 23));
//        displayMat(thirdImage, imageView2);
        thirdImage.release();

        Rect roi = checkMouseCollisionAndFindRoi(mouse, rects);

        //MainActivity.startingPoint = starting;// Only needed for small image preview.
        // If any rectangle collides with mouse click.
        if (roi != null) {
            Log.d(TAG, "extract: Roi found: " + roi);
            Mat examinedIm = new Mat();
            baseImage.submat(examinedArea).copyTo(examinedIm);
            if (roi.x < 0) roi.x = 0;
            if (roi.x + roi.width > examinedArea.width) roi.width = examinedArea.width - roi.x;
            if (roi.y < 0) roi.y = 0;
            if (roi.y + roi.height > examinedArea.height) roi.height = examinedArea.height - roi.y;

            Mat extractedImage = new Mat();
            examinedIm.submat(roi).copyTo(extractedImage);
            examinedIm.release();
//                displayMat(extractedImage, imageView3);
            Bitmap extractedWord = Bitmap.createBitmap(extractedImage.cols(),
                    extractedImage.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(extractedImage, extractedWord);
            extractedImage.release();
            Rect baseImageRoi = new Rect(roi.x + examinedArea.x, roi.y + examinedArea.y,
                    roi.width, roi.height);
            final int BORDER_SIZE = 3;
            if (selectedWords.contains(baseImageRoi)) {
                selectedWords.remove(baseImageRoi);

                Rect roiAndBorder = baseImageRoi; // Prepare ROI + borders.
                roiAndBorder.x = (roiAndBorder.x - BORDER_SIZE < 0 ? 0 : roiAndBorder.x - BORDER_SIZE);
                roiAndBorder.y = (roiAndBorder.y - BORDER_SIZE < 0 ? 0 : roiAndBorder.y - BORDER_SIZE);
                roiAndBorder.width = (roiAndBorder.width + BORDER_SIZE  * 2 > baseImage.width() ?
                        baseImage.width() : roiAndBorder.width + BORDER_SIZE * 2);
                roiAndBorder.height = (roiAndBorder.height + BORDER_SIZE * 2 > baseImage.height() ?
                        baseImage.height() : roiAndBorder.height + BORDER_SIZE * 2);

                Mat roiSubmat = baseImage.submat(roiAndBorder); // this will be placed in the image.
                Mat subView = wordsSelectionCanvas.submat(roiAndBorder); // This is place where roiSubmat will be placed.
                roiSubmat.copyTo(subView);
            } else {
                selectedWords.add(baseImageRoi);
                Imgproc.rectangle(wordsSelectionCanvas, baseImageRoi.tl(), baseImageRoi.br(),
                        new Scalar(0, 244, 244, 0), BORDER_SIZE);
            }
            displayMat(wordsSelectionCanvas, imageView);

            return extractedWord;

        } else // Miss click - no word under cursor.
        {
            Log.d(TAG, "extract: Miss click.");
            return null;
        }

    }

    private Rect prepareExaminedArea(final int mouseX, final int mouseY, final int roiXShift,
                                     final int roiEnlargement) {

        final int AREA_WIDTH = (SCALE_IMAGE_DOWN_ON ? 500 : 700);
        final int AREA_HEIGHT = (SCALE_IMAGE_DOWN_ON ? 200 : 200);

        Rect examinedArea = new Rect(mouseX + roiXShift - (AREA_WIDTH + roiEnlargement) / 2,
                mouseY - AREA_HEIGHT / 2, AREA_WIDTH + roiEnlargement, AREA_HEIGHT);
        // Check if chosen subimage does not exceed the original one.
        if (examinedArea.x < 0)
            examinedArea.x = 0;
        else if (examinedArea.x + AREA_WIDTH + roiEnlargement >= baseImage.width())
            examinedArea.x = baseImage.width() - AREA_WIDTH - roiEnlargement;

        if (examinedArea.y < 0)
            examinedArea.y = 0;
        else if (examinedArea.y + AREA_HEIGHT >= baseImage.height())
            examinedArea.y = baseImage.height() - AREA_HEIGHT;

        return examinedArea;
    }


    private List<Rect> sortRectangles(List<Rect> rects) {
        Collections.sort(rects, new Comparator<Rect>() {
            @Override
            public int compare(Rect o1, Rect o2) {
                int aXPos = o1.x;
                int bXPos = o2.x;

                return aXPos - bXPos;
            }
        });

        return rects;
    }

    private Rect checkMouseCollisionAndFindRoi(final Rect mouse, final List<Rect> rects) {
        for (Rect rect : rects) {
            if (checkCollision(rect, mouse)) {
                return rect;
            }
        }

        return null;
    }

    private boolean checkCollision(final Rect objA, final Rect objB) {
        class Borders {
            int left, right, top, bottom;

            Borders(int l, int t, int r, int b) {
                left = l;
                right = r;
                top = t;
                bottom = b;
            }
        }
        final Borders A = new Borders(objA.x, objA.y, objA.x + objA.width, objA.y + objA.height);
        final Borders B = new Borders(objB.x, objB.y, objB.x + objB.width, objB.y + objB.height);

        if (A.bottom < B.top)
            return false;
        if (A.top > B.bottom)
            return false;
        if (A.right < B.left)
            return false;
        if (A.left > B.right)
            return false;

        return true;
    }

    private boolean checkIfInTheSameLine(final int y1, final int y2) {
        if (Math.abs(y1 - y2) > 30)
            return false;
        return true;
    }

    private Rect combineRectangles(final Rect A, final Rect B) {
        int mostLeftX = Math.min(A.x, B.x);
        int mostTopY = Math.min(A.y, B.y);
        int mostRightX = Math.max(A.x + A.width, B.x + B.width);
        int mostDownY = Math.max(A.y + A.height, B.y + B.height);

        return new Rect(mostLeftX, mostTopY, mostRightX - mostLeftX, mostDownY - mostTopY);
    }

    private List<Rect> unionInterceptedRects(List<Rect> rects) {
        // Rectangles have to be sorted by x coordinate from the most left to the right.
        List<Boolean> to_remove = new ArrayList<>(Arrays.asList(new Boolean[rects.size()]));
        Collections.fill(to_remove, Boolean.FALSE);

        for (int i = 0; i < rects.size(); i++) {
            if (to_remove.get(i) == Boolean.TRUE)
                continue;
            for (int next = i + 1; next < rects.size(); next++) {
                if (to_remove.get(next) == Boolean.TRUE)
                    continue;
                if (checkCollision(rects.get(i), rects.get(next))
                        && checkIfInTheSameLine(rects.get(i).y, rects.get(next).y)) {
                    Rect combined = combineRectangles(rects.get(i), rects.get(next));
                    rects.set(i, combined);
                    to_remove.set(next, true);
                }
            }
        }

        // Delete all signed rectangles
        List<Rect> newRects = new ArrayList<>();
        for (int i = 0; i < rects.size(); i++) {
            if (!to_remove.get(i))
                newRects.add(rects.get(i));
        }

        return newRects;

    }

    private Mat createCannyEdges(final Mat extracted_part) {
        final int RED = 0, GREEN = 1, BLUE = 2;
        // Split extracted image into three colour channels.
        List<Mat> rgb = new ArrayList<>(3);
        Core.split(extracted_part, rgb);
        Imgproc.blur(rgb.get(RED), rgb.get(RED), new Size(3, 3));
        Imgproc.blur(rgb.get(GREEN), rgb.get(GREEN), new Size(3, 3));
        Imgproc.blur(rgb.get(BLUE), rgb.get(BLUE), new Size(3, 3));

        // Perform a Canny for every channel(RGB) separately.
        final int lowThreshold = 80, kernel_size = 3, ratio = 3;
        Canny(rgb.get(RED), rgb.get(RED), lowThreshold, lowThreshold * ratio, kernel_size);

        Canny(rgb.get(GREEN), rgb.get(GREEN), lowThreshold, lowThreshold * ratio, kernel_size);
        Canny(rgb.get(BLUE), rgb.get(BLUE), lowThreshold, lowThreshold * ratio, kernel_size);

        // Combined Cannies results.
        Mat detected_edges = new Mat();
        add(rgb.get(RED), rgb.get(GREEN), detected_edges);
        add(detected_edges, rgb.get(BLUE), detected_edges);

        return detected_edges;
    }


    private List<Rect> createBoundingBoxesForContours(final Mat detectedEdges) {
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        // Find only external contours - skip children.
        findContours(detectedEdges, contours, hierarchy, Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        // Create rectangles surrounding contours.
        final int BOX_EXPANSION_Y = (SCALE_IMAGE_DOWN_ON ? 3 : 5);
        final int BOX_EXPANSION_X = (SCALE_IMAGE_DOWN_ON ? 3 : 5);
        List<Rect> rects = new ArrayList<>();
        for (MatOfPoint contour : contours) {
            // Eliminate rectangles that are out of red rectangle.
            Rect boundingBox = boundingRect(contour);
            // Make boxes bigger so that they overlap and can be unioned.
            boundingBox.x -= BOX_EXPANSION_X;
            boundingBox.y -= BOX_EXPANSION_Y;
            boundingBox.width += BOX_EXPANSION_X * 2;
            boundingBox.height += BOX_EXPANSION_Y * 2;

            rects.add(boundingBox);
        }

        return rects;
    }
}

