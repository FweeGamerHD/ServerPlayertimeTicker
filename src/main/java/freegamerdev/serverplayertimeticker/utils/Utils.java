package freegamerdev.serverplayertimeticker.utils;

public class Utils {
    public static String formatSeconds(long timeInSeconds) {
        return String.format("%02d:%02d:%02d", timeInSeconds / 3600, (timeInSeconds / 60) % 60, timeInSeconds % 60);
    }

    public static double getPercent(long total, long now){
        int iTotal = Math.toIntExact(total);
        int iNow = Math.toIntExact(now);
        return (double) (((double)iNow*100)/iTotal);
    }
}
