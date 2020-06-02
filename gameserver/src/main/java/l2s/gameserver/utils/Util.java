package l2s.gameserver.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import l2s.commons.map.hash.TIntStringHashMap;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.model.entity.events.impl.AbstractFightClub;
import l2s.gameserver.model.reward.RewardList;
import l2s.gameserver.templates.item.ItemTemplate;


public class Util
{
	static final String PATTERN = "0.0000000000E00";
	static final DecimalFormat df;

	/**
	 * Форматтер для адены.<br>
	 * Locale.KOREA заставляет его фортматировать через ",".<br>
	 * Locale.FRANCE форматирует через " "<br>
	 * Для форматирования через "." убрать с аргументов Locale.FRANCE
	 */
	private static NumberFormat adenaFormatter;

	static
	{
		adenaFormatter = NumberFormat.getIntegerInstance(Locale.FRANCE);
		df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH);
		df.applyPattern(PATTERN);
		df.setPositivePrefix("+");
	}

	/**
	 * Проверяет строку на соответсвие регулярному выражению
	 * @param text Строка-источник
	 * @param template Шаблон для поиска
	 * @return true в случае соответвия строки шаблону
	 */
	public static boolean isMatchingRegexp(String text, String template)
	{
		Pattern pattern = null;
		try
		{
			pattern = Pattern.compile(template);
		}
		catch(PatternSyntaxException e) // invalid template
		{
			e.printStackTrace();
		}
		if(pattern == null)
			return false;
		Matcher regexp = pattern.matcher(text);
		return regexp.matches();
	}

	public static String formatDouble(double x, String nanString, boolean forceExponents)
	{
		if(Double.isNaN(x))
			return nanString;
		if(forceExponents)
			return df.format(x);
		if((long) x == x)
			return String.valueOf((long) x);
		return String.valueOf(x);
	}

	/**
	 * Return amount of adena formatted with " " delimiter
	 * @param amount
	 * @return String formatted adena amount
	 */
	public static String formatAdena(long amount)
	{
		return adenaFormatter.format(amount);
	}

	/**
	 * форматирует время в секундах в дни/часы/минуты/секунды
	 */
	public static String formatTime(int time)
	{
		if(time == 0)
			return "now";
		time = Math.abs(time);
		String ret = "";
		long numDays = time / 86400;
		time -= numDays * 86400;
		long numHours = time / 3600;
		time -= numHours * 3600;
		long numMins = time / 60;
		time -= numMins * 60;
		long numSeconds = time;
		if(numDays > 0)
			ret += numDays + "d ";
		if(numHours > 0)
			ret += numHours + "h ";
		if(numMins > 0)
			ret += numMins + "m ";
		if(numSeconds > 0)
			ret += numSeconds + "s";
		return ret.trim();
	}

	/**
	 * Инструмент для подсчета выпавших вещей с учетом рейтов.
	 * Возвращает 0 если шанс не прошел, либо количество если прошел.
	 * Корректно обрабатывает шансы превышающие 100%.
	 * Шанс в 1:1000000 (L2Drop.MAX_CHANCE)
	 */
	public static long rollDrop(long min, long max, double calcChance, boolean rate)
	{
		if(calcChance <= 0 || min <= 0 || max <= 0)
			return 0;
		int dropmult = 1;
		if(rate)
			calcChance *= Config.RATE_DROP_ITEMS;
		if(calcChance > RewardList.MAX_CHANCE)
			if(calcChance % RewardList.MAX_CHANCE == 0) // если кратен 100% то тупо умножаем количество
				dropmult = (int) (calcChance / RewardList.MAX_CHANCE);
			else
			{
				dropmult = (int) Math.ceil(calcChance / RewardList.MAX_CHANCE); // множитель равен шанс / 100% округление вверх
				calcChance = calcChance / dropmult; // шанс равен шанс / множитель
			}
		return Rnd.chance(calcChance / 10000.) ? Rnd.get(min * dropmult, max * dropmult) : 0;
	}

	public static int packInt(int[] a, int bits) throws Exception
	{
		int m = 32 / bits;
		if(a.length > m)
			throw new Exception("Overflow");

		int result = 0;
		int next;
		int mval = (int) Math.pow(2, bits);
		for(int i = 0; i < m; i++)
		{
			result <<= bits;
			if(a.length > i)
			{
				next = a[i];
				if(next >= mval || next < 0)
					throw new Exception("Overload, value is out of range");
			}
			else
				next = 0;
			result += next;
		}
		return result;
	}

	public static long packLong(int[] a, int bits) throws Exception
	{
		int m = 64 / bits;
		if(a.length > m)
			throw new Exception("Overflow");

		long result = 0;
		int next;
		int mval = (int) Math.pow(2, bits);
		for(int i = 0; i < m; i++)
		{
			result <<= bits;
			if(a.length > i)
			{
				next = a[i];
				if(next >= mval || next < 0)
					throw new Exception("Overload, value is out of range");
			}
			else
				next = 0;
			result += next;
		}
		return result;
	}

	public static int[] unpackInt(int a, int bits)
	{
		int m = 32 / bits;
		int mval = (int) Math.pow(2, bits);
		int[] result = new int[m];
		int next;
		for(int i = m; i > 0; i--)
		{
			next = a;
			a = a >> bits;
		result[i - 1] = next - a * mval;
		}
		return result;
	}

	public static int[] unpackLong(long a, int bits)
	{
		int m = 64 / bits;
		int mval = (int) Math.pow(2, bits);
		int[] result = new int[m];
		long next;
		for(int i = m; i > 0; i--)
		{
			next = a;
			a = a >> bits;
		result[i - 1] = (int) (next - a * mval);
		}
		return result;
	}
	public static float[] parseCommaSeparatedFloatArray(String s)
	{
		if (s.isEmpty())
			return new float[0];
		String[] tmp = s.replaceAll(",", ";").replaceAll("\\n", ";").split(";");
		float[] val = new float[tmp.length];
		for (int i = 0; i < tmp.length; i++)
			val[i] = Float.parseFloat(tmp[i]);
		return val;
	}

	public static int[] parseCommaSeparatedIntegerArray(String s)
	{
		if (s.isEmpty())
			return new int[0];
		String[] tmp = s.replaceAll(",", ";").replaceAll("\\n", ";").split(";");
		int[] val = new int[tmp.length];
		for (int i = 0; i < tmp.length; i++)
			val[i] = Integer.parseInt(tmp[i]);
		return val;
	}

	public static long[] parseCommaSeparatedLongArray(String s)
	{
		if (s.isEmpty())
			return new long[0];
		String[] tmp = s.replaceAll(",", ";").replaceAll("\\n", ";").split(";");
		long[] val = new long[tmp.length];
		for (int i = 0; i < tmp.length; i++)
			val[i] = Long.parseLong(tmp[i]);
		return val;
	}

	public static long[][] parseStringForDoubleArray(String s)
	{
		String[] temp = s.replaceAll("\\n", ";").split(";");
		long[][] val = new long[temp.length][];

		for (int i = 0; i < temp.length; i++)
			val[i] = parseCommaSeparatedLongArray(temp[i]);
		return val;
	}

	/** Just alias */
	public static String joinStrings(String glueStr, String[] strings, int startIdx, int maxCount)
	{
		return Strings.joinStrings(glueStr, strings, startIdx, maxCount);
	}

	/** Just alias */
	public static String joinStrings(String glueStr, String[] strings, int startIdx)
	{
		return Strings.joinStrings(glueStr, strings, startIdx, -1);
	}

	public static boolean isNumber(String s)
	{
		try
		{
			Double.parseDouble(s);
		}
		catch(NumberFormatException e)
		{
			return false;
		}
		return true;
	}

	public static String dumpObject(Object o, boolean simpleTypes, boolean parentFields, boolean ignoreStatics)
	{
		Class<?> cls = o.getClass();
		String val, type, result = "[" + (simpleTypes ? cls.getSimpleName() : cls.getName()) + "\n";
		Object fldObj;
		List<Field> fields = new ArrayList<Field>();
		while(cls != null)
		{
			for(Field fld : cls.getDeclaredFields())
				if(!fields.contains(fld))
				{
					if(ignoreStatics && Modifier.isStatic(fld.getModifiers()))
						continue;
					fields.add(fld);
				}
			cls = cls.getSuperclass();
			if(!parentFields)
				break;
		}

		for(Field fld : fields)
		{
			fld.setAccessible(true);
			try
			{
				fldObj = fld.get(o);
				if(fldObj == null)
					val = "NULL";
				else
					val = fldObj.toString();
			}
			catch(Throwable e)
			{
				e.printStackTrace();
				val = "<ERROR>";
			}
			type = simpleTypes ? fld.getType().getSimpleName() : fld.getType().toString();

			result += String.format("\t%s [%s] = %s;\n", fld.getName(), type, val);
		}

		result += "]\n";
		return result;
	}

	public static String getDataTime(long time, String format)
	{
		return new SimpleDateFormat(format).format(new Date(time));
	}

	public static String getDataTime(long time)
	{
		return getDataTime(time, "dd/MM/yyyy HH:mm:ss");
	}

	public static String getPriceString(int id, long count)
	{
		ItemTemplate item = ItemHolder.getInstance().getTemplate(id);
		return item==null ? "Нет такого предмета" : count + " " + item.getName();
	}

	public static String getServerIp() throws Exception
	{
		return InetAddress.getLocalHost().getHostAddress();
	}
	
	public static String getHostIp() throws Exception
	{
		return GetIP.getIpAddress();
	}
	
	public static String getServerMac() throws Exception
	{
		InetAddress address = InetAddress.getLocalHost();
		NetworkInterface ni = NetworkInterface.getByInetAddress(address);

		if (ni != null)
		{
			byte[] mac = ni.getHardwareAddress();
			if (mac != null)
			{
				StringBuffer macAddress = new StringBuffer();
				String sep = "";
				for (byte Mac : mac)
				{
					int b = Math.abs(Mac);
					String hexByte = Integer.toHexString(b);
					macAddress.append(sep).append(hexByte);
					sep = ":";
				}

				return macAddress.toString();
			}
		}

		return null;
	}

	private static Pattern _pattern = Pattern.compile("<!--TEMPLATE(\\d+)(.*?)TEMPLATE-->", Pattern.DOTALL);

	public static TIntStringHashMap parseTemplates(String html)
	{
		Matcher m = _pattern.matcher(html);
		TIntStringHashMap tpls = new TIntStringHashMap();
		while(m.find())
		{
			tpls.put(Integer.parseInt(m.group(1)), m.group(2));
			html = html.replace(m.group(0), "");
		}

		tpls.put(0, html);
		return tpls;
	}
	
	public static boolean isDigit(String text)
	{
		return (text != null) && (text.matches("[0-9]+"));
	}

	public static String getChangedEventName(AbstractFightClub event)
	{
		String eventName = event.getClass().getSimpleName();
		eventName = eventName.substring(0, eventName.length() - 5);
		return eventName;
	}
	
	public static String convertToLineagePriceFormat(double price)
	{
		if (price < 10000.0D)
			return Math.round(price) + "a";
		if (price < 1000000.0D)
			return reduceDecimals(price / 1000.0D, 1) + "k";
		if (price < 1000000000.0D)
			return reduceDecimals(price / 1000.0D / 1000.0D, 1) + "kk";
		return reduceDecimals(price / 1000.0D / 1000.0D / 1000.0D, 1) + "kkk";
	}	
	
	public static String reduceDecimals(double original, int nDecim)
	{
		return reduceDecimals(original, nDecim, false);
	}
  
	public static String reduceDecimals(double original, int nDecim, boolean round)
	{
		String decimals = "#";
		if (nDecim > 0)
		{
			decimals = decimals + ".";
			for (int i = 0; i < nDecim; i++)
			{
				decimals = decimals + "#";
			}
		}
		DecimalFormat df = new DecimalFormat(decimals);
		return df.format(round ? Math.round(original) : original).replace(",", ".");
	}

	public static String boolToString(boolean b)
	{
		return b ? "True" : "False";
	}
	
	public static boolean arrayContains(Object[] array, Object objectToLookFor)
	{
		if ((array == null) || (objectToLookFor == null))
			return false;
		for (Object objectInArray : array)
			if ((objectInArray != null) && (objectInArray.equals(objectToLookFor)))
				return true;
		return false;
	}	
}