package l2s.gameserver.utils;

import l2s.gameserver.Config;

/**
 * @author B0nux
 * @date 16:03/10.10.2011
 */
public enum Language
{
	// With offical client support
	KOREAN(0, "ko", "k", 1),
	ENGLISH(1, "en", "e", 1),
	JAPANESE(2, "ja", "j", 1),
	CHINESE_TW(3, "zh-tw", "tw", 4),
	CHINESE(4, "zh", "cn", 1),
	THAI(5, "th", "th", 1),
	PHILIPPINE(6, "tl", "ph", 1),
	INDONESIAN(7, "id", "id", 1),
	RUSSIAN(8, "ru", "ru", 1),
	ENGLISH_EU(9, "en-eu", "eu", 1),
	GERMAN(10, "de", "de", 1),
	FRENCH(11, "fr", "fr", 1),
	POLISH(12, "pl", "pl", 1),
	TURKISH(13, "tr", "tr", 1),
	// Custom
	PORTUGUESE(-1, "pt", "e", 1),
	SPANISH(-2, "es", "e", 1),
	ARABIC(-3, "ar", "e", 1),
	GREEK(-4, "el", "e", 1),
	GEORGIAN(-5, "ka", "e", 1),
	HUNGARIAN(-6, "hu", "e", 1),
	FINNISH(-7, "fi", "e", 1),
	UKRAINIAN(-8, "uk", "ru", 8),
	VIETNAMESE(-9, "vi", "e", 1);

	public static final Language[] VALUES = values();

	public static final String LANG_VAR = "lang@";

	private final int _id;
	private final String _shortName;
	private final String _datName;
	private final int _secondLang;

	private Language(int id, String shortName, String datName, int secondLang)
	{
		_id = id;
		_shortName = shortName;
		_datName = datName;
		_secondLang = secondLang;
	}

	public int getId()
	{
		return _id;
	}

	public String getShortName()
	{
		return _shortName;
	}

	public String getDatName()
	{
		return _datName;
	}

	public Language getSecondLanguage()
	{
		return getLanguage(_secondLang);
	}

	public boolean isCustom()
	{
		return getId() < 0;
	}

	public static Language getLanguage(int langId, Language defaultLang)
	{
		for(Language lang : VALUES)
			if(lang.getId() == langId)
				return lang;
		return defaultLang;
	}

	public static Language getLanguage(int langId)
	{
		return getLanguage(langId, Config.DEFAULT_LANG);
	}

	public static Language getLanguage(String shortName, Language defaultLang)
	{
		if(shortName != null)
		{
			for(Language lang : VALUES)
			{
				if(lang.getShortName().equalsIgnoreCase(shortName))
					return lang;
			}
		}
		return defaultLang;
	}

	public static Language getLanguage(String shortName)
	{
		return getLanguage(shortName, Config.DEFAULT_LANG);
	}
}