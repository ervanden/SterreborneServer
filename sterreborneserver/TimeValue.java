package sterreborneserver;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class TimeValue extends GregorianCalendar {

    public Boolean on;
    public Boolean once;

    public TimeValue() { // returns an object corresponding to now
        super();
        LocalDateTime now = LocalDateTime.now();
        this.set(now.getYear(),
                now.getMonth().getValue() - 1, // Calendar works with months 0-11
                now.getDayOfMonth(),
                now.getHour(),
                now.getMinute(),
                now.getSecond());
        on = null;
    }

    public TimeValue(TimeValue t) {   // copy constructor
        super();
        this.set(
                t.get(Calendar.YEAR),
                t.get(Calendar.MONTH),
                t.get(Calendar.DAY_OF_MONTH),
                t.get(Calendar.HOUR_OF_DAY),
                t.get(Calendar.MINUTE),
                t.get(Calendar.SECOND)
        );

    }

    public Integer year() {
        return get(Calendar.YEAR);
    }

    public Integer month() {
        return get(Calendar.MONTH) + 1;
    }

    public Integer day() {
        return get(Calendar.DAY_OF_MONTH);
    }

    public String dayName() {
        String[] days = {"SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
        return days[get(Calendar.DAY_OF_WEEK) - 1];
    }

    public String dayShortName() {
        return dayName().substring(0, 3);
    }

    public Integer hour() {
        return get(Calendar.HOUR_OF_DAY);
    }

    public Integer minute() {
        return get(Calendar.MINUTE);
    }

    public Integer second() {
        return get(Calendar.SECOND);
    }

    public String dateName() {
        return this.dayName() + " "
                + this.day() + "/" + this.month() + "/" + this.year() + " "
                + this.hour() + ":" + this.minute() + ":" + this.second();
    }

    public String timeValueName() {
        return this.dateName() + " " + this.on + " " + this.once;
    }

    public boolean isSameDateAs(TimeValue t) {
        return (this.year().intValue() == t.year().intValue())
                && (this.month().intValue() == t.month().intValue())
                && (this.day().intValue() == t.day().intValue());
    }

    public int isSecondsLaterThan(TimeValue t) {
        return (hour() * 3600 + minute() * 60 + second())
                - (t.hour() * 3600 + t.minute() * 60 + t.second());
    }

    public String asString() {
        return dayName()
                + " " + year()
                + " " + month()
                + " " + day()
                + " " + hour()
                + " " + minute()
                + " " + on
                + " " + once;

    }

    String color() {
        if (on && once) return "darkred";
        if (!on && once) return "darkblue";
        if (on && !once) return "red";
        if (!on && !once) return "blue";
        return "?";
    }

    public String asJSONString() {
      return "{"+
              "\"messageID\":\"CS\", " +
              "\"day\":\""+dayName()+"\","+
              "\"hour\":\""+hour()+"\","+
              "\"minute\":\""+minute()+"\","+
              "\"color\":\""+color()+"\""+
              "}";
    }

    static public TimeValue stringToTimeValue(String s) {
        String[] tokens = s.split(" ");
        String dayName = tokens[0];
        String year = tokens[1];
        String month = tokens[2];
        String day = tokens[3];
        String hour = tokens[4];
        String minute = tokens[5];
        String cyclic = tokens[6];
        String once = tokens[7];
        int second = 0;
        TimeValue t = new TimeValue();
        t.set(Integer.parseInt(year),
                Integer.parseInt(month) - 1,
                Integer.parseInt(day),
                Integer.parseInt(hour),
                Integer.parseInt(minute),
                second);
        t.on = Boolean.parseBoolean(cyclic);
        t.once = Boolean.parseBoolean(once);
        return t;

    }
}
