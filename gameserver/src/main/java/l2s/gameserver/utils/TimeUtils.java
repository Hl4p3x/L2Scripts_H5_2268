package l2s.gameserver.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author VISTALL
 * @date 16:18/14.02.2011
 */
public class TimeUtils
{
	private static final SimpleDateFormat SIMPLE_FORMAT = new SimpleDateFormat("HH:mm dd.MM.yyyy");

	public static String toSimpleFormat(Calendar cal)
	{
		return SIMPLE_FORMAT.format(cal.getTime());
	}

	public static String toSimpleFormat(long cal)
	{
		return SIMPLE_FORMAT.format(cal);
	}

	public static String minutesToFullString(int period)
	{
		StringBuilder sb = new StringBuilder();

		if(period > 1440)
		{
			sb.append((period - (period % 1440)) / 1440).append(" D.");
			period = period % 1440;
		}

		if(period > 60)
		{
			if(sb.length() > 0)
			{
				sb.append(", ");
			}

			sb.append((period - (period % 60)) / 60).append(" h.");

			period = period % 60;
		}

		if(period > 0)
		{
			if(sb.length() > 0)
			{
				sb.append(", ");
			}

			sb.append(period).append(" min.");
		}
		if(sb.length() < 1)
		{
			sb.append("less than 1 minute.");
		}

		return sb.toString();
	}
}
