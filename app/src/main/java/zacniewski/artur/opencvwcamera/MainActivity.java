package zacniewski.artur.opencvwcamera;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements
        CameraBridgeViewBase.CvCameraViewListener2 {

    private CameraBridgeViewBase mOpenCvCameraView;
    private static final String TAG = "OCVSample::Activity";
    private Mat mIntermediateMat;

    public static final int VIEW_MODE_RGBA      = 0;
    public static final int VIEW_MODE_CANNY     = 2;
    public static final int VIEW_MODE_SOBEL     = 4;
    public static final int VIEW_MODE_ZOOM      = 5;
    public static final int VIEW_MODE_PIXELIZE  = 6;

    private MenuItem mItemPreviewRGBA;
    private MenuItem mItemPreviewCanny;
    private MenuItem mItemPreviewSobel;
    private MenuItem mItemPreviewZoom;
    private MenuItem mItemPreviewPixelize;
    public static int viewMode = VIEW_MODE_RGBA;
    private Size mSize0;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;

            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0,
                    this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mItemPreviewRGBA = menu.add("Preview RGBA");
        mItemPreviewCanny = menu.add("Canny");
        mItemPreviewSobel = menu.add("Sobel");
        mItemPreviewZoom = menu.add("Zoom");
        mItemPreviewPixelize = menu.add("Pixelize");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemPreviewRGBA)
            viewMode = VIEW_MODE_RGBA;
        else if (item == mItemPreviewCanny)
            viewMode = VIEW_MODE_CANNY;
        else if (item == mItemPreviewSobel)
            viewMode = VIEW_MODE_SOBEL;
        else if (item == mItemPreviewZoom)
            viewMode = VIEW_MODE_ZOOM;
        else if (item == mItemPreviewPixelize)
            viewMode = VIEW_MODE_PIXELIZE;
        return true;
    }
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewBase)
                findViewById(R.id.image_manipulations_activity_surface_view);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
        }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat rgba = inputFrame.rgba();
        Size sizeRgba = rgba.size();
        Mat rgbaInnerWindow;
        int rows = (int) sizeRgba.height;
        int cols = (int) sizeRgba.width;
        int left = cols / 8;
        int top = rows / 8;
        int width = cols * 3 / 4;
        int height = rows * 3 / 4;
        switch (MainActivity.viewMode) {
            case MainActivity.VIEW_MODE_RGBA:
                break;

            case MainActivity.VIEW_MODE_CANNY:
            rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
                Imgproc.Canny(rgbaInnerWindow, mIntermediateMat, 80, 90);
                Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow,
                        Imgproc.COLOR_GRAY2BGRA, 4);
                rgbaInnerWindow.release();
                break;

            case MainActivity.VIEW_MODE_SOBEL:
                Mat gray = inputFrame.gray();
                Mat grayInnerWindow = gray.submat(top, top + height, left,
                        left + width);
                rgbaInnerWindow = rgba.submat(top, top + height, left,
                        left + width);
                Imgproc.Sobel(grayInnerWindow, mIntermediateMat,
                        CvType.CV_8U, 1, 1);
                Core.convertScaleAbs(mIntermediateMat, mIntermediateMat,
                        10, 0);
                Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow,
                        Imgproc.COLOR_GRAY2BGRA, 4);
                grayInnerWindow.release();
                rgbaInnerWindow.release();
                break;
            case MainActivity.VIEW_MODE_ZOOM:
                Mat zoomCorner = rgba.submat(0, rows / 2 - rows / 10, 0,
                        cols / 2 - cols / 10);
                Mat mZoomWindow = rgba.submat(rows / 2 - 9 * rows / 100, rows / 2 +
                        9 * rows / 100, cols / 2 - 9 * cols / 100, cols / 2 + 9 * cols / 100);
                Imgproc.resize(mZoomWindow, zoomCorner, zoomCorner.size(),
                        0, 0, Imgproc.INTER_LINEAR_EXACT);
                Size wsize = mZoomWindow.size();
                Imgproc.rectangle(mZoomWindow, new Point(1, 1), new
                        Point(wsize.width - 2, wsize.height - 2), new Scalar(255, 0, 0, 255), 2);

                zoomCorner.release();
                mZoomWindow.release();
                break;

            case MainActivity.VIEW_MODE_PIXELIZE:
                rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
                Imgproc.Canny(rgbaInnerWindow, mIntermediateMat, 80, 90);
                rgbaInnerWindow.setTo(new Scalar(0, 0, 0, 255), mIntermediateMat);
                Core.convertScaleAbs(rgbaInnerWindow, mIntermediateMat, 1./16, 0);
                Core.convertScaleAbs(mIntermediateMat, rgbaInnerWindow, 16, 0);
                rgbaInnerWindow.release();
                break;
        }
        return rgba;
    }
    public void onCameraViewStopped() {
        if (mIntermediateMat != null)
            mIntermediateMat.release();
        mIntermediateMat = null;
    }

    public void onCameraViewStarted(int width, int height) {
        mIntermediateMat = new Mat();
    }

}


