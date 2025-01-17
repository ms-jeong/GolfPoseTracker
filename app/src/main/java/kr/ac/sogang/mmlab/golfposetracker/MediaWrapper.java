package kr.ac.sogang.mmlab.golfposetracker;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;

import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.concurrent.ExecutionException;


import static org.opencv.core.Core.bitwise_and;
import static org.opencv.core.Core.bitwise_or;
import static org.opencv.core.Core.split;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2RGB;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.drawContours;
import static org.opencv.imgproc.Imgproc.findContours;
import static org.opencv.imgproc.Imgproc.threshold;
import static org.opencv.core.Core.absdiff;
import static org.opencv.core.Core.bitwise_not;
import static org.opencv.videoio.Videoio.CAP_PROP_FRAME_COUNT;
import static org.opencv.videoio.Videoio.CAP_PROP_FRAME_HEIGHT;
import static org.opencv.videoio.Videoio.CAP_PROP_FRAME_WIDTH;
import static org.opencv.videoio.Videoio.CAP_PROP_FPS;
import static org.opencv.videoio.Videoio.CAP_PROP_FOURCC;


public class MediaWrapper {
    private VideoCapture cap;
    private String videoPath;
    private String modifiedVideoName;
    private String modifiedImageName;
    private Mat outputImage;

    private Mat motions;
    private Mat motions_mask;
    public Bitmap startBitmap;

    private int smpRate;
    private int refIntv;

    private VideoWriter swingVideoWriter = null;
    private String modifiedVideoPath;

    private List<Mat> frames;
    int scanFlag = 0;
    double c;

    /********************************************/
    /*for v3*/
    private Deque<Mat> frameBuf;
    private Deque<Integer> indice;
    private Deque<Mat> prev;
    private Deque<Integer> prev_indice;
    private int frameCnt;

    /********************************************/


    boolean VideoOpen(String selectedVideoPath) {
        videoPath = selectedVideoPath;
        cap = new VideoCapture(videoPath);

        for (int i = 0; i <= 45; i++) {
            Log.e("INFO", "iiii" + String.valueOf(cap.get(i)));
        }

        Log.e("INFO", String.valueOf(c));
        Log.e("INFO", "is Opend : " + String.valueOf(cap.isOpened()));
        Log.e("INFO", String.valueOf(cap.get(Videoio.CAP_PROP_FRAME_COUNT)));
        Log.e("INFO", String.valueOf(cap.get(Videoio.CAP_PROP_FRAME_WIDTH)));
        Log.e("INFO", String.valueOf(cap.get(CAP_PROP_FRAME_HEIGHT)));
        Log.e("INFO", String.valueOf(cap.get(CAP_PROP_FPS)));
        Log.e("INFO", String.valueOf(cap.get(CAP_PROP_FOURCC)));
        if (cap.isOpened()) {
            Log.e("INFO", "open vid");
            return true;
        } else {
            Log.e("INFO", "Fail to open vid");
            return false;
        }
        /*
        if (cap.isOpened()) {
            Mat start = new Mat();
            cap.read(start);
            startFrame = Bitmap.createBitmap(start.cols(), start.rows(), Bitmap.Config.RGB_565);
            mFrame = Bitmap.createBitmap(start.cols(), start.rows(), Bitmap.Config.RGB_565);
            Utils.matToBitmap(start, startFrame);
            Log.e("INFO", "READ First Frame");
            return true;
        } else {
            Log.e("INFO", "READ First Frame ... fail");
            return false;
        }*/

    }

    Mat GetImageFromVideo() {
        Mat frame = new Mat();
        cap.read(frame);
        if (!frame.empty()) {
            cvtColor(frame, frame, COLOR_BGR2RGB);
            return frame;
        } else {
            return null;
        }
    }

    void SetThresholds(double t1, double t2) {
        smpRate = (int) t1;
        refIntv = (int) t2;
    }

    double GetThreshold1() {
        return smpRate;
    }

    double Getref_intv() {
        return refIntv;

    }

    void SetImage(Mat image) {
        outputImage = image;
    }

    Mat GetImage() {
        return outputImage;
    }

    void SetModifiedVideoName(String videoName) {
        modifiedVideoName = videoName;
    }

    String GetModifiedVideoName() { return modifiedVideoName;  }

    void SetModifiedImageName(String imageName) {
        modifiedImageName = imageName;
    }

    String GetModifiedImageName() {
        return modifiedImageName;
    }

    boolean SaveImage(Mat image) {
        Mat saveImage = new Mat(image.rows(), image.cols(), CvType.CV_8U, new Scalar(3));
        cvtColor(image, saveImage, Imgproc.COLOR_BGR2RGB, 3);
        Imgcodecs.imwrite(GetModifiedImageName(), saveImage);
        return true;
    }

    boolean GenerateVideo(Mat frame) {
        try {
            Log.e("VideoWriter", "" + GetModifiedVideoName() + " / " + String.valueOf(frame.rows()) + " / " + String.valueOf(frame.cols()));
            swingVideoWriter = new VideoWriter(GetModifiedVideoName(), VideoWriter.fourcc('M', 'J', 'P', 'G'), 30.0, new Size(frame.cols(), frame.rows()), true);
            Log.e("INFO", "is Opened : " + String.valueOf(swingVideoWriter.isOpened()));
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    boolean GetGeneratedVideo() {
        if (swingVideoWriter != null && swingVideoWriter.isOpened()) {
            Log.e("VIDEO WRITE", "GET VID");
            return true;
        } else {
            Log.e("VIDEO WRITE", "GET VID Fail");
            return false;
        }
    }

    boolean InsertFrameInVideo(Mat frame) {
        try {
            Mat saveFrame = new Mat(frame.rows(), frame.cols(), CvType.CV_8U, new Scalar(3));
            cvtColor(frame, saveFrame, Imgproc.COLOR_BGR2RGB, 3);

            swingVideoWriter.write(saveFrame);
            saveFrame = null;
            return true;
        } catch (Exception e) {

            return false;
        }
    }

    Mat GenerateFrame(Mat srcImage) {
        return srcImage;
    }

    void swingVideoRelease() {
        Log.e("VIDEO WRITE", "open ? " + swingVideoWriter.isOpened() + " wr " + String.valueOf(swingVideoWriter));
        swingVideoWriter.release();
        swingVideoWriter = null;
    }


    /********************************************/
    /********************************************/
    /********************************************/
    int GetLastFrameIdx() {
        return (int) cap.get(CAP_PROP_FRAME_COUNT) - 1;
    }

    boolean GenerateVideo() {
        try {
            Mat frame = frames.get(0);
            swingVideoWriter = new VideoWriter(GetModifiedVideoName(), VideoWriter.fourcc('M', 'J', 'P', 'G'), 30.0, new Size(frame.cols(), frame.rows()), true);
            //swingVideoWriter = new VideoWriter(GetModifiedVideoName(), VideoWriter.fourcc('M', 'P', '4', 'v'), 30.0, new Size(frame.cols(), frame.rows()), true);
            motions = new Mat(new Size(frame.cols(), frame.rows()), CvType.CV_8UC(3), Scalar.all(0));
            motions_mask = new Mat(new Size(frame.cols(), frame.rows()), CvType.CV_8U, Scalar.all(0));
            Log.e("VideoWriter", "" + GetModifiedVideoName() + " / " + String.valueOf(frame.rows()) + " / " + String.valueOf(frame.cols()));
            Log.e("VideoWriter", "writer is Opened : " + swingVideoWriter.isOpened());

            return true;
        } catch (Exception e) {
            return false;
        }

    }

    int ScanAllFrames() {
        int scanFlag = 0;
        Mat srcFrame;
        frames = new ArrayList<>();
        while (true) {
            srcFrame = GetImageFromVideo();
            if (srcFrame != null) {
                frames.add(srcFrame);
                scanFlag++;
            } else {
                break;
            }

        }
        return scanFlag;
    }

    private Mat GrayBlur(Mat m) {
        Mat g = new Mat();
        cvtColor(m, g, COLOR_BGR2GRAY);
        GaussianBlur(g, g, new Size(5.0, 5.0), 0);
        return g;
    }

    private Mat GetDifference(Mat m1, Mat m2) {
        Mat dif = new Mat();
        absdiff(m1, m2, dif);
        threshold(dif, dif, 5, 255, THRESH_BINARY);
        return dif;
    }

    Mat GenerateFrame(int idx, boolean flag) {
        Mat curr = frames.get(idx);
        Mat m_frame = curr.clone();
        if (flag) {
            Log.e("REF", "ref " + (idx - refIntv) + "/" + idx + "/" + (idx + refIntv));
            Mat prev = frames.get(idx - refIntv);
            Mat next = frames.get(idx + refIntv);

            Mat p_gray = GrayBlur(prev);
            Mat c_gray = GrayBlur(curr);
            Mat n_gray = GrayBlur(next);

            Mat pc_thr = GetDifference(p_gray, c_gray);
            Mat pn_thr = GetDifference(p_gray, n_gray);
            Mat cn_thr = GetDifference(c_gray, n_gray);

            Mat pcn_thr = new Mat();
            bitwise_or(pc_thr, cn_thr, pcn_thr);
            bitwise_or(pcn_thr, pn_thr, pcn_thr);
            Mat c_stick = new Mat();
            Mat not_pnthr = new Mat();
            bitwise_not(pn_thr, not_pnthr);
            bitwise_and(pcn_thr, not_pnthr, c_stick);
            List<MatOfPoint> cntrs = new ArrayList<>();
            findContours(c_stick, cntrs, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
            Mat action = new Mat(new Size(c_stick.cols(), c_stick.rows()), CvType.CV_8U, Scalar.all(0));
            for (int i = 0; i < cntrs.size(); i++) {
                double area = Imgproc.contourArea(cntrs.get(i));
                if (area > 100) {
                    drawContours(action, cntrs, i, new Scalar(255), -1);
                }
            }
            curr.copyTo(motions, action);
            bitwise_or(motions_mask, action, motions_mask);

            p_gray.release();
            c_gray.release();
            n_gray.release();
            pc_thr.release();
            pn_thr.release();
            cn_thr.release();
            pcn_thr.release();
            c_stick.release();
            not_pnthr.release();
            cntrs.clear();

            for (int i = idx - 2 * refIntv; i < idx - refIntv; i++) {

                Mat frame = frames.get(i);
                if (!frame.empty()) {
                    frame.release();
                    Log.e("Free", "free " + i);
                }
            }
        }
        motions.copyTo(m_frame, motions_mask);

        return m_frame;
    }

    boolean InsertFrameInVideo(int idx, boolean flag) {
        try {
            Mat frame = GenerateFrame(idx, flag);
            swingVideoWriter.write(frame);
            //Utils.matToBitmap(frame,mFrame);
            frame.release();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    void SetModifiedVideoName(String path, String videoName) {
        modifiedVideoName = path + videoName;
    }

    /********************************************/
    /********************************************/
    /********************************************/
    int GetframeCnt() {
        return frameCnt;
    }

    void init_vid() throws Exception {
        Mat frame = GetImageFromVideo();

        frameBuf = new ArrayDeque<>();
        indice = new ArrayDeque<>();
        prev = new ArrayDeque<>();
        prev_indice = new ArrayDeque<>();
        frameCnt = 1;

        swingVideoWriter = new VideoWriter(GetModifiedVideoName(), VideoWriter.fourcc('M', 'J', 'P', 'G'), 30.0, new Size(frame.cols(), frame.rows()), true);
        motions = new Mat(new Size(frame.cols(), frame.rows()), CvType.CV_8UC(3), Scalar.all(0));
        motions_mask = new Mat(new Size(frame.cols(), frame.rows()), CvType.CV_8U, Scalar.all(0));
        //Utils.matToBitmap(frame,startBitmap);

        //System.out.println("success to create Video : "+GetModifiedVideoName());
        Log.e("MSG", "success to create Video : " + GetModifiedVideoName());

    }

    void release_vid() {
        try {
            cap.release();
            frameBuf.clear();
            indice.clear();
            prev.clear();
            prev_indice.clear();
            frameCnt = 0;
            swingVideoWriter.release();
            motions.release();
            motions_mask.release();
        } catch (Exception e) {

        }
    }

    private boolean hasRef(int idx) {
        return idx > refIntv;
    }

    private boolean isSample(int idx) {
        return (idx % smpRate == 0) && hasRef(idx);
    }

    private int isPrev(int idx) {
        return isSample(idx + refIntv) ? idx + refIntv : -1;
    }

    private int isNext(int idx) {
        return isSample(idx - refIntv) ? idx - refIntv : -1;
    }

    boolean GetFrame() {
        Mat frame = GetImageFromVideo();
        if (frame!=null&&!frame.empty()) {
            frameBuf.addLast(frame);
            indice.addLast(frameCnt);
            frameCnt++;
            return true;
        } else {
            return false;
        }
    }
    int GetFrameBufSize(){
        return frameBuf.size();
    }

    private void update_motion(Mat prev, Mat curr, Mat next) {
        Mat p_gray = GrayBlur(prev);
        Mat c_gray = GrayBlur(curr);
        Mat n_gray = GrayBlur(next);

        Mat pc_thr = GetDifference(p_gray, c_gray);
        Mat pn_thr = GetDifference(p_gray, n_gray);
        Mat cn_thr = GetDifference(c_gray, n_gray);


        Mat pcn_thr = new Mat();
        bitwise_or(pc_thr, cn_thr, pcn_thr);
        bitwise_or(pcn_thr, pn_thr, pcn_thr);
        Mat c_stick = new Mat();
        Mat not_pnthr = new Mat();
        bitwise_not(pn_thr, not_pnthr);
        bitwise_and(pcn_thr, not_pnthr, c_stick);


        List<MatOfPoint> cntrs = new ArrayList<>();

        Imgproc.findContours(c_stick, cntrs, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat action = new Mat(new Size(c_stick.cols(), c_stick.rows()), CvType.CV_8U, Scalar.all(0));
        for (int i = 0; i < cntrs.size(); i++) {
            double area = Imgproc.contourArea(cntrs.get(i));
            if (area > 100) {
                Imgproc.drawContours(action, cntrs, i, new Scalar(255), -1);
            }
        }
        curr.copyTo(motions, action);
        bitwise_or(motions_mask, action, motions_mask);

        action.release();
        cntrs.clear();
        not_pnthr.release();
        c_stick.release();
        pcn_thr.release();
        pc_thr.release();
        cn_thr.release();
        pn_thr.release();
        p_gray.release();
        c_gray.release();
        n_gray.release();
    }

    Mat generate_frame() throws Exception{
        int idx = indice.removeFirst();
        Mat frame = frameBuf.removeFirst();
        Mat m_frame = frame.clone();

        //System.out.println("idx : " + String.valueOf(idx) + "/ Sample : " + isSample(idx) + "/ Prev : " + isPrev(idx) + "/ Next : " + isNext(idx));
        //Log.e("MSG", "idx : " + String.valueOf(idx) + "/ Sample : " + isSample(idx) + "/ Prev : " + isPrev(idx) + "/ Next : " + isNext(idx));
        if (isPrev(idx) != -1) {
            prev.addLast(frame);
            prev_indice.addLast(idx);
        }
        if (isSample(idx)) {
            int prev_idx = prev_indice.removeFirst();
            Mat prev_im = prev.removeFirst();
            while (indice.size() < refIntv && GetFrame()) ;
            if (GetFrameBufSize()>=refIntv) {
                int next_idx = indice.peekLast();
                Mat next_im = frameBuf.peekLast();
                if (prev_idx == idx - refIntv && next_idx == idx + refIntv) {
                    //System.out.println("update motion... " + prev_idx + "/" + idx + "/" + next_idx);
                    //Log.e("MSG", "update motion... " + prev_idx + "/" + idx + "/" + next_idx);
                    update_motion(prev_im, frame, next_im);
                    prev_im.release();
                    //Log.e("MSG", "free prev : " + prev_idx);
                }
            }
        }
        motions.copyTo(m_frame, motions_mask);
        //System.out.println("apply motion... " + idx);
        Log.e("MSG", "apply motion... " + idx);
        swingVideoWriter.write(m_frame);

        if (isPrev(idx) == -1) {
            frame.release();
            Log.e("MSG", "free curr : " + idx);
        }
        return m_frame;
    }

}
