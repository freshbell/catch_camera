#include <jni.h>
#include <opencv2/opencv.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>

using namespace std;
using namespace cv;

void BlendingPixel(Mat& background, Mat& foreground, Point2i location) {

    for (int x = 0; x < background.cols; ++x) {
        for (int y = 0; y < background.rows; ++y) {
            double opacity = ((double) foreground.data[y * foreground.step +
                                                            x * foreground.channels() + 3]) / 255.;
            for (int c = 0; opacity > 0 && c < background.channels(); ++c) {
                unsigned char foregroundPx = foreground.data[y * foreground.step +
                                                               x * foreground.channels() + c];
                unsigned char backgroundPx = background.data[y * background.step +
                                                           x * background.channels() + c];
                background.data[y * background.step + background.channels() * x + c] =
                        backgroundPx * (1. - opacity) + foregroundPx * opacity;
            }
        }
    }
}

void BlendingPyojeok(Mat& background, Mat& foreground, Point2i location) {
    int fx, fy;
    for (int x = location.x; x < location.x+100; ++x) {
        fx = x - location.x;
        if(x<0 || x > background.cols) continue;
        for (int y = location.y; y < location.y+100; ++y) {
            fy = y - location.y;
            if(y<0 || y > background.rows) continue;
            double opacity = ((double) foreground.data[fy * foreground.step +
                                                       fx * foreground.channels() + 3]) / 255.;
            for (int c = 0; opacity > 0 && c < background.channels(); ++c) {
                unsigned char foregroundPx = foreground.data[fy * foreground.step +
                                                             fx * foreground.channels() + c];
                unsigned char backgroundPx = background.data[y * background.step +
                                                             x * background.channels() + c];
                background.data[y * background.step + background.channels() * x + c] =
                        backgroundPx * (1. - opacity) + foregroundPx * opacity;
            }
        }
    }
}


extern "C"
JNIEXPORT int JNICALL
Java_com_work_catch_1camera_MainActivity_ConvertRGBtoGray(JNIEnv *env, jobject instance,
                                                          jlong matAddrInput,
                                                          jlong matAddrResult,
                                                          jint check,
                                                          jlong matGreen,
                                                          jlong matYellow,
                                                          jlong matPyojeok){

    Mat &mat_green = *(Mat *)matGreen;
    Mat &mat_yellow = *(Mat *)matYellow;
    Mat &matInput = *(Mat *) matAddrInput;
    Mat &matResult = *(Mat *) matAddrResult;
    Mat &mat_pyojeok = *(Mat *)matPyojeok;
    Mat joonggan;
    MatExpr zFillMatrix = Mat::zeros(matInput.size(), CV_8UC1);

//    vector<Point2f> corners;
//    goodFeaturesToTrack(matInput,corners,200,0.01,0);

    int cols_num = matInput.cols;
    int rows_num = matInput.rows;
    int gumchul = 0;
    int max = -999;
    int x, y;

    cvtColor(matInput, joonggan, COLOR_RGBA2RGB);

    //circle(matResult,Point(matInput.cols/2,matInput.rows/2),10,Scalar(10,10,10),10);
   if(check == -1 || check == 2) {
       for (int i = 0; i < matInput.cols; i++){
           for (int j = 0; j < matInput.rows; j++) {
               if (max < matInput.at<uchar>(j,i) && 200 < matInput.at<uchar>(j,i)){ // 이 부분 숫자를 바꿔야 합니다. 밝기값이 최대 255이므로 그보다 작은 숫자로 바꿔야 합니다.
                   max = matInput.at<uchar>(j,i);
                   x = i;
                   y = j;
                   gumchul = 2;
               }
           }
       }

       Scalar scalar;
       //putText(matResult,"finding...",Point(cols_num/2-200,rows_num/2-200),3,5.0,Scalar(96,255,33),2);
        if (gumchul == 2) {
            // 빨간색
            Mat bgr[3];
            split(joonggan,bgr);
            Mat R[] = {bgr[0],zFillMatrix,zFillMatrix};
            merge(R, 3, bgr[0]);
            joonggan = bgr[0] + 30;
            bgr->release();

            resize(mat_pyojeok,mat_pyojeok,Size(100,100));
            BlendingPyojeok(joonggan,mat_pyojeok,Point(x-50,y-50));
            /*
            circle(joonggan,Point(x,y),30,Scalar(255,244,33),6);
            circle(joonggan,Point(x,y),20,Scalar(255,244,33),4);

            line(joonggan,Point(x + 10,y),Point(x + 40,y),Scalar(255,244,33),3);
            line(joonggan,Point(x - 10,y),Point(x - 40,y),Scalar(255,244,33),3);
            line(joonggan,Point(x,y + 10),Point(x,y + 40),Scalar(255,244,33),3);
            line(joonggan,Point(x,y - 10),Point(x,y - 40),Scalar(255,244,33),3);
*/
            scalar = Scalar(96,255,33);

            BlendingPixel(joonggan,mat_yellow, Point(0,0));
        }
        else {
            BlendingPixel(joonggan,mat_green, Point(0,0));
            scalar = Scalar(96,255,33);
        }
/*
        rectangle(joonggan,Point(cols_num/2 - 100,rows_num/2 - 50),Point(cols_num/2 - 100 + 100,rows_num/2 -  50),scalar,3);
        rectangle(joonggan,Point(cols_num/2 - 100,rows_num/2 - 50),Point(cols_num/2 - 100,rows_num/2 -  50 + 100),scalar,3);

        rectangle(joonggan,Point(cols_num/2 + 100,rows_num/2 -  50),Point(cols_num/2 + 100 - 100,rows_num/2 -  50),scalar,3);
        rectangle(joonggan,Point(cols_num/2 + 100,rows_num/2 -  50),Point(cols_num/2 + 100,rows_num/2 -  50 + 100),scalar,3);

        rectangle(joonggan,Point(cols_num/2 - 100,rows_num/2 +  50),Point(cols_num/2 - 100 + 100,rows_num/2 +  50),scalar,3);
        rectangle(joonggan,Point(cols_num/2 - 100,rows_num/2 +  50),Point(cols_num/2 - 100,rows_num/2 +  50 - 100),scalar,3);

        rectangle(joonggan,Point(cols_num/2 + 100,rows_num/2 +  50),Point(cols_num/2 + 100 - 100,rows_num/2 +  50),scalar,3);
        rectangle(joonggan,Point(cols_num/2 + 100,rows_num/2 +  50),Point(cols_num/2 + 100,rows_num/2 +  50 - 100),scalar,3);
*/
   }

   matResult = joonggan;

   joonggan.release();

   return gumchul;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_work_catch_1camera_MainActivity_draw(JNIEnv *env, jobject thiz,
                                              jlong mat_addr_input,
                                              jlong mat_addr_result) {
    //Mat image = Mat::zeros(400,400,CV_8UC3);

    //circle(image,Point(200,200),32.0,Scalar(0,0,255),5,8);
   // imshow("Image",image);

    //waitKey(0);
    // TODO: implement draw()
}