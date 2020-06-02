package l2s.gameserver.data.string;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.utils.Language;
import l2s.gameserver.utils.SkillUtils;

/**
 * Author: VISTALL
 * Date:  19:27/29.12.2010
 */
public final class SkillDescHolder extends AbstractHolder
{
	private static final SkillDescHolder _instance = new SkillDescHolder();

	private final Map<Language, TIntObjectMap<String>> _skillDescs = new HashMap<Language, TIntObjectMap<String>>();

	public static SkillDescHolder getInstance()
	{
		return _instance;
	}

	private SkillDescHolder()
	{
		//
	}

	public String getSkillDesc(Language lang, int hashCode)
	{
		TIntObjectMap<String> skillDescs = _skillDescs.get(lang);
		String desc = skillDescs.get(hashCode);
		if(desc == null)
		{
			if(lang == Language.ENGLISH)
			{
				skillDescs = _skillDescs.get(Language.RUSSIAN);
				desc = skillDescs.get(hashCode);
			}
			else
			{
				skillDescs = _skillDescs.get(Language.ENGLISH);
				desc = skillDescs.get(hashCode);
			}
		}
		return desc;
	}

	public String getSkillDesc(Player player, int hashCode)
	{
		Language lang = player == null ? Config.DEFAULT_LANG : player.getLanguage();
		return getSkillDesc(lang, hashCode);
	}

	public String getSkillDesc(Language lang, Skill skill)
	{
		return getSkillDesc(lang, skill.hashCode());
	}

	public String getSkillDesc(Player player, Skill skill)
	{
		return getSkillDesc(player, skill.hashCode());
	}

	public String getSkillDesc(Language lang, int id, int level)
	{
		return getSkillDesc(lang, SkillUtils.generateSkillHashCode(id, level));
	}

	public String getSkillDesc(Player player, int id, int level)
	{
		return getSkillDesc(player, SkillUtils.generateSkillHashCode(id, level));
	}

	public void load()
	{
		for(Language lang : Language.VALUES)
		{
			_skillDescs.put(lang, new TIntObjectHashMap<String>());

			if(!Config.AVAILABLE_LANGUAGES.contains(lang))
				continue;

			File file = new File(Config.DATAPACK_ROOT, "data/string/skilldesc/" + lang.getShortName() + ".txt");
			if(!file.exists())
			{
				// Проверяем только английский и русский, потому что они являються базовыми.
				if(lang == Language.ENGLISH || lang == Language.RUSSIAN)
					warn("Not find file: " + file.getAbsolutePath());
			}
			else
			{
				LineNumberReader reader = null;
				try
				{
					reader = new LineNumberReader(new FileReader(file));
					String line = null;
					while((line = reader.readLine()) != null)
					{
						StringTokenizer token = new StringTokenizer(line, "\t");
						if(token.countTokens() < 2)
						{
							error("Error on line: " + line + "; file: " + file.getName());
							continue;
						}

						int id = Integer.parseInt(token.nextToken());
						int level = Integer.parseInt(token.nextToken());
						int hashCode = SkillUtils.generateSkillHashCode(id, level);
						String value = token.hasMoreTokens() ? token.nextToken() : "";

						_skillDescs.get(lang).put(hashCode, value);
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
						reader.close();
					}
					catch(Exception e)
					{
						//
					}
				}
			}
		}

		log();
	}

	public void reload()
	{
		clear();
		load();
	}

	@Override
	public void log()
	{
		for(Map.Entry<Language, TIntObjectMap<String>> entry : _skillDescs.entrySet())
		{
			if(!Config.AVAILABLE_LANGUAGES.contains(entry.getKey()))
				continue;
			info("load skill descs: " + entry.getValue().size() + " for lang: " + entry.getKey());
		}
	}

	@Override
	public int size()
	{
		return _skillDescs.size();
	}

	@Override
	public void clear()
	{
		_skillDescs.clear();
	}
}
