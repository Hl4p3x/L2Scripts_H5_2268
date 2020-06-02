package l2s.gameserver.utils;

import java.util.Calendar;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class LogFormatter
  extends SimpleFormatter
{
  String newline = System.getProperty("line.separator");
  
  public String format(LogRecord record)
  {
    Calendar date = Calendar.getInstance();
    date.setTimeInMillis(record.getMillis());
    String text = "[" + getDate(date, 2) + ".";
    text = text + getDate(date, 5) + ".";
    text = text + getDate(date, 1);
    text = text + " ";
    text = text + getDate(date, 11) + ":";
    text = text + getDate(date, 12) + ":";
    text = text + getDate(date, 13) + "]";
    text = text + " " + record.getMessage();
    text = text + newline;
    return text;
  }
  
  private String getDate(Calendar c, int i)
  {
    int intResult = c.get(i);
    if (i == 2) {
      intResult++;
    }
    String result = String.valueOf(intResult);
    if (result.length() == 4) {
      result = result.substring(2);
    }
    if (result.length() == 1) {
      result = "0" + result;
    }
    return result;
  }
}
