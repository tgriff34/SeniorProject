package com.example.tristangriffin.projectx;

import android.media.ExifInterface;

public class GeoLocationConverter {


    private float longitude, latitude;

    GeoLocationConverter(ExifInterface exifInterface) {
        String LATITUDE = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        String LONGITUDE = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        String LATITUDE_DIR = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
        String LONGITUDE_DIR = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

        if (LATITUDE != null && LONGITUDE != null && LATITUDE_DIR != null && LONGITUDE_DIR != null){
            if (LATITUDE_DIR.equals("N")) {
                latitude = convertToDegree(LATITUDE);
            } else {
                latitude = 0 - convertToDegree(LATITUDE);
            }

            if (LONGITUDE_DIR.equals("E")) {
                longitude = convertToDegree(LONGITUDE);
            } else {
                longitude = 0 - convertToDegree(LONGITUDE);
            }
        }
    }

    private float convertToDegree(String stringToConvert) {
        float result;
        String[] split = stringToConvert.split(",", 3);

        String[] degrees = split[0].split("/", 2);
        Double _degreeZero = Double.valueOf(degrees[0]);
        Double _degreeOne = Double.valueOf(degrees[1]);
        Double _degreeResult = _degreeZero / _degreeOne;

        String[] minutes = split[1].split("/", 2);
        Double _minuteZero = Double.valueOf(minutes[0]);
        Double _minuteOne = Double.valueOf(minutes[1]);
        Double _minuteResult= _minuteZero / _minuteOne;

        String[] seconds = split[0].split("/", 2);
        Double _secondZero = Double.valueOf(seconds[0]);
        Double _secondOne = Double.valueOf(seconds[1]);
        Double _secondResult = _secondZero / _secondOne;

        result = new Float(_degreeResult + (_minuteResult / 60) + (_secondResult / 3600));

        return result;
    }

    public float getLongitude() {
        return longitude;
    }

    public float getLatitude() {
        return latitude;
    }
}
