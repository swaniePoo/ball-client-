package me.dinozoid.strife.util.system;

import java.util.concurrent.TimeUnit;

public class DateUtil {

    public static String getFormattedTime(long time) {
//        long days = TimeUnit.MILLISECONDS.toDays(value);
//        value -= TimeUnit.DAYS.toMillis(days);
//        long hours = TimeUnit.MILLISECONDS.toHours(value);
//        value -= TimeUnit.HOURS.toMillis(hours);
//        long minutes = TimeUnit.MILLISECONDS.toMinutes(value);
//        value -= TimeUnit.MINUTES.toMillis(minutes);
//        long seconds = TimeUnit.MILLISECONDS.toSeconds(value);
//        return String.format("%d hours, %d min, %d sec", hours, minutes, seconds);
        time /= 1000;
        int seconds = (int) (time % 60);
        int minutes = (int) (time / 60 % 60);
        int hours = (int) (time / 60 / 60 % 60);
        return String.format("%sh %sm %ss", hours, minutes, seconds);
    }

}
