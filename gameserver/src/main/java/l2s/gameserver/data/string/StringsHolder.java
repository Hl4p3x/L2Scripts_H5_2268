package l2s.gameserver.data.string;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Files;
import l2s.gameserver.utils.Language;

/**
 * Author: VISTALL
 * Date:  19:27/29.12.2010
 */
public final class StringsHolder extends AbstractHolder
{
	private static final Pattern LINE_PATTERN = Pattern.compile("^(((?!=).)+)=(.*?)$");

	private static final StringsHolder _instance = new StringsHolder();

	private final Map<Language, Map<String, String>> _strings = new HashMap<Language, Map<String, String>>();

	public static StringsHolder getInstance()
	{
		return _instance;
	}

	private StringsHolder()
	{
		//
	}

	public String getString(String name, Player player)
	{
		Language lang = player == null ? Config.DEFAULT_LANG : player.getLanguage();
		return getString(name, lang);
	}
	
	public String getString(Player player, String name)
	{
		Language lang = player == null ? Config.DEFAULT_LANG : player.getLanguage();
		return getString(name, lang);
	}

	public String getString(String address, Language lang)
	{
		Map<String, String> strings = _strings.get(lang);
		String value = strings.get(address);
		if(value == null)
		{
			Language secondLang = lang;
			do
			{
				if(secondLang == secondLang.getSecondLanguage())
					break;

				secondLang = secondLang.getSecondLanguage();
				strings = _strings.get(secondLang);
				value = strings.get(address);
			}
			while(value == null);

			if(value == null)
			{
				for(Language l : Language.VALUES)
				{
					strings = _strings.get(secondLang);
					if((value = strings.get(address)) != null)
						break;
				}
			}
		}

		if(value == null)
		{
			warn("String for '" + lang + "' language: '" + address + "' not found.");
			return "";
		}
		return value;
	}

	public void load()
	{
		for(Language lang : Language.VALUES)
		{
			_strings.put(lang, new HashMap<String, String>());

			if(!Config.AVAILABLE_LANGUAGES.contains(lang))
				continue;

			File file = new File(Config.DATAPACK_ROOT, "data/string/strings/" + lang.getShortName() + ".properties");
			if(!file.exists())
				warn("Not find file: " + file.getAbsolutePath());
			else
				loadFile(file, lang);

			file = new File(Config.DATAPACK_ROOT, "data/string/strings/" + lang.getShortName() + "/");
			if(file.exists() && file.isDirectory())
			{
				for(File f : file.listFiles())
				{
					if(f.getName().matches("^(.+)\\.properties$"))
						loadFile(f, lang);
				}
			}
		}
		log();
	}

	private void loadFile(File file, Language lang)
	{
		Scanner scanner = null;
		try
		{
			String content = Files.readFile(file);
			scanner = new Scanner(content);
			int i = 0;
			String line;
			while(scanner.hasNextLine())
			{
				i++;
				line = scanner.nextLine();
				if(line.startsWith("#"))
					continue;

				Matcher m = LINE_PATTERN.matcher(line);
				if(m.find())
				{
					String name = m.group(1);
					String value = m.group(3);

					_strings.get(lang).put(name, value);
				}
				else
					error("Error on line #: " + i + "; file: " + file.getName());
			}
		}
		catch(Exception e)
		{
			error("Exception: " + e, e);
		}
		finally
		{
			try
			{
				scanner.close();
			}
			catch(Exception e)
			{
				//
			}
		}
	}

	public void reload()
	{
		clear();
		load();
	}

	@Override
	public void log()
	{
		for(Map.Entry<Language, Map<String, String>> entry : _strings.entrySet())
		{
			if(!Config.AVAILABLE_LANGUAGES.contains(entry.getKey()))
				continue;
			info("load strings: " + entry.getValue().size() + " for lang: " + entry.getKey());
		}
	}

	@Override
	public int size()
	{
		return _strings.size();
	}

	@Override
	public void clear()
	{
		_strings.clear();
	}
}
