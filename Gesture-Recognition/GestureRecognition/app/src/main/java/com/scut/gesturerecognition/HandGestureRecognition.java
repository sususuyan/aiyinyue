package com.scut.gesturerecognition;

import com.google.mediapipe.formats.proto.LandmarkProto;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

enum Gestures {
    FIVE,
    FOUR,
    THREE,
    ONE,
    YEAH,
    ROCK,
    SPIDERMAN,
    FIST,
    OK,
    FINGER_HEART,
    THUMBS_UP,
    UNKNOWN
}

public class HandGestureRecognition {
    static boolean isRightHand(double point1, double point2){
            return point1 <  point2 ;
    }
    static boolean fingerIsOpen(double pseudoFixKeyPoint, double point1, double point2){
            return point1 < pseudoFixKeyPoint && point2 < pseudoFixKeyPoint;
    }
    static boolean thumbIsOpen(LandmarkProto.NormalizedLandmarkList landmarks){
            return (isRightHand(landmarks.getLandmark(2).getX(), landmarks.getLandmark(17).getX()) && fingerIsOpen(landmarks.getLandmark(2).getX(), landmarks.getLandmark(3).getX(), landmarks.getLandmark(4).getX()))//右手情况
                    ||(!isRightHand(landmarks.getLandmark(2).getX(), landmarks.getLandmark(17).getX()) && !fingerIsOpen(landmarks.getLandmark(2).getX(),landmarks.getLandmark(3).getX(), landmarks.getLandmark(4).getX())); //左手情况
    }
    static boolean firstFingerIsOpen(LandmarkProto.NormalizedLandmarkList landmarks){
        return  fingerIsOpen(landmarks.getLandmark(6).getY(), landmarks.getLandmark(7).getY(), landmarks.getLandmark(8).getY());
    }
    static boolean secondFingerIsOpen(LandmarkProto.NormalizedLandmarkList landmarks){
        return  fingerIsOpen(landmarks.getLandmark(10).getY(), landmarks.getLandmark(11).getY(), landmarks.getLandmark(12).getY());
    }
    static boolean thirdFingerIsOpen(LandmarkProto.NormalizedLandmarkList landmarks){
        return  fingerIsOpen(landmarks.getLandmark(14).getY(), landmarks.getLandmark(15).getY(), landmarks.getLandmark(16).getY());
    }
    static boolean fourthFingerIsOpen(LandmarkProto.NormalizedLandmarkList landmarks){
        return  fingerIsOpen(landmarks.getLandmark(18).getY(), landmarks.getLandmark(19).getY(), landmarks.getLandmark(20).getY());
    }
    static double getEuclideanDistanceAB(double aX, double aY, double bX, double bY){
        return  sqrt(pow(aX - bX, 2) + pow(aY - bY, 2));
    }
    static boolean isThumbNearFirstFinger(LandmarkProto.NormalizedLandmark point1, LandmarkProto.NormalizedLandmark point2){
        return  getEuclideanDistanceAB(point1.getX(), point1.getY(), point2.getX(), point2.getY()) < 0.1;
    }
    static com.scut.gesturerecognition.Gestures handGestureRecognition(LandmarkProto.NormalizedLandmarkList landmarks){
        if (landmarks.getLandmarkCount() == 0) return com.scut.gesturerecognition.Gestures.UNKNOWN;
        boolean thumbIsOpen = com.scut.gesturerecognition.HandGestureRecognition.thumbIsOpen(landmarks);
        boolean firstFingerIsOpen =
                com.scut.gesturerecognition.HandGestureRecognition.firstFingerIsOpen(landmarks);
        boolean secondFingerIsOpen =
                com.scut.gesturerecognition.HandGestureRecognition.secondFingerIsOpen(landmarks);
        boolean thirdFingerIsOpen =
                com.scut.gesturerecognition.HandGestureRecognition.thirdFingerIsOpen(landmarks);
        boolean fourthFingerIsOpen =
                com.scut.gesturerecognition.HandGestureRecognition.fourthFingerIsOpen(landmarks);
        if (thumbIsOpen &&
                firstFingerIsOpen &&
                secondFingerIsOpen &&
                thirdFingerIsOpen &&
                fourthFingerIsOpen)
            return com.scut.gesturerecognition.Gestures.FIVE;
        else if (!thumbIsOpen &&
                firstFingerIsOpen &&
                secondFingerIsOpen &&
                thirdFingerIsOpen &&
                fourthFingerIsOpen)
            return com.scut.gesturerecognition.Gestures.FOUR;
        else if (!thumbIsOpen &&
                firstFingerIsOpen &&
                secondFingerIsOpen &&
                thirdFingerIsOpen &&
                !fourthFingerIsOpen)
            return com.scut.gesturerecognition.Gestures.THREE;
        else if (!thumbIsOpen &&
                firstFingerIsOpen &&
                !secondFingerIsOpen &&
                !thirdFingerIsOpen &&
                !fourthFingerIsOpen)
            return com.scut.gesturerecognition.Gestures.ONE;
        else if (!thumbIsOpen &&
                firstFingerIsOpen &&
                secondFingerIsOpen &&
                !thirdFingerIsOpen &&
                !fourthFingerIsOpen)
            return com.scut.gesturerecognition.Gestures.YEAH;
        else if (!thumbIsOpen &&
                firstFingerIsOpen &&
                !secondFingerIsOpen &&
                !thirdFingerIsOpen &&
                fourthFingerIsOpen)
            return com.scut.gesturerecognition.Gestures.ROCK;
        else if (thumbIsOpen &&
                firstFingerIsOpen &&
                !secondFingerIsOpen &&
                !thirdFingerIsOpen &&
                fourthFingerIsOpen)
            return com.scut.gesturerecognition.Gestures.SPIDERMAN;
        else if (!thumbIsOpen &&
                !firstFingerIsOpen &&
                !secondFingerIsOpen &&
                !thirdFingerIsOpen &&
                !fourthFingerIsOpen)
            return com.scut.gesturerecognition.Gestures.FIST;
        else if (!firstFingerIsOpen &&
                secondFingerIsOpen &&
                thirdFingerIsOpen &&
                fourthFingerIsOpen &&
                isThumbNearFirstFinger(landmarks.getLandmark(4), landmarks.getLandmark(8)))
            return com.scut.gesturerecognition.Gestures.OK;
        else if (thumbIsOpen &&
                firstFingerIsOpen &&
                !secondFingerIsOpen &&
                !thirdFingerIsOpen &&
                !fourthFingerIsOpen &&
                (landmarks.getLandmark(3).getZ() > landmarks.getLandmark(7).getZ()) &&
                (isThumbNearFirstFinger(landmarks.getLandmark(4), landmarks.getLandmark(7))
                        ||isThumbNearFirstFinger(landmarks.getLandmark(3), landmarks.getLandmark(7))
                        ||isThumbNearFirstFinger(landmarks.getLandmark(3), landmarks.getLandmark(6))))
            return com.scut.gesturerecognition.Gestures.FINGER_HEART;
        else if (!firstFingerIsOpen &&
                !secondFingerIsOpen &&
                !thirdFingerIsOpen &&
                !fourthFingerIsOpen &&
                (landmarks.getLandmark(2).getY()>landmarks.getLandmark(3).getY() && landmarks.getLandmark(3).getY()>landmarks.getLandmark(4).getY()))
            return com.scut.gesturerecognition.Gestures.THUMBS_UP;
        else
            return com.scut.gesturerecognition.Gestures.UNKNOWN;
    }
}
