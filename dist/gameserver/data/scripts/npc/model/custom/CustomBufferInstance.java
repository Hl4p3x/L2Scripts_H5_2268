package npc.model.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Effect;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Language;

/**
 * @author Bonux
 */
public class CustomBufferInstance extends NpcInstance
{
	private static class BuffSet
	{
		private String[] _name = new String[Language.VALUES.length];
		private Map<Integer, Skill> _buffs = new LinkedHashMap<Integer, Skill>();

		public String getName(Player player)
		{
			if(player.isLangRus())
				return _name[Language.RUSSIAN.ordinal()];
			return _name[Language.ENGLISH.ordinal()];
		}

		public void setName(Language lang, String name)
		{
			_name[lang.ordinal()] = name;
		}

		public Skill[] getBuffs()
		{
			return _buffs.values().toArray(new Skill[_buffs.size()]);
		}

		public boolean addBuff(int skillId)
		{
			if(!BUFFS_MAP.containsKey(skillId))
				return false;

			int skillLvl = BUFFS_MAP.get(skillId);
			Skill skill = SkillHolder.getInstance().getSkill(skillId, skillLvl > 0 ? skillLvl : 1);
			if(skill == null)
			{
				_log.warn("Cannot found skill ID[" + skillId + "] LEVEL[" + skillLvl + "] for buff!");
				return false;
			}

			if(skillLvl <= 0)
			{
				skill = SkillHolder.getInstance().getSkill(skillId, skill.getBaseLevel());
				if(skill == null)
				{
					_log.warn("Cannot found skill ID[" + skillId + "] LEVEL[" + skill.getBaseLevel() + "] for buff!");
					return false;
				}
			}

			return _buffs.put(skill.getId(), skill) == null;
		}

		public void removeBuff(int skillId)
		{
			_buffs.remove(skillId);
		}

		public boolean containsBuff(int skillId)
		{
			return _buffs.containsKey(skillId);
		}
	}

	private static TIntIntMap BUFFS_MAP = new TIntIntHashMap();
	static
	{
		//	[Баффы бойцу]
		BUFFS_MAP.put(1068, 3);	// Могущество 3 ур   
		BUFFS_MAP.put(1086, 2);	// Ускорение 2 ур   
		BUFFS_MAP.put(1077, 3);	// Фокусировка 3 ур   
		BUFFS_MAP.put(1242, 3);	// Шепот Смерти 3 ур  
		BUFFS_MAP.put(1240, 3);	// Наведение 3 ур 
		BUFFS_MAP.put(1043, 1);	// Святое Оружие 1 ур   
		BUFFS_MAP.put(1268, 4);	// Гнев Вампира 4 ур  
		BUFFS_MAP.put(1040, 3);	// Щит 3 ур 
		BUFFS_MAP.put(1036, 2);	// Магический барьер 2 ур 
		BUFFS_MAP.put(1204, 2);	// Легкая походка 2 ур  
		BUFFS_MAP.put(1045, 6);	// Благословение тела 6 ур 
		BUFFS_MAP.put(1048, 6);	// Благословение духа 6 ур  

		//	[Бафы Магу]
		BUFFS_MAP.put(1085, 3);	// Проницательность 3 ур  
		BUFFS_MAP.put(1059, 3);	// Воодушевление 3 ур  
		BUFFS_MAP.put(1303, 2);	// Дикая Магия 2 ур  
		BUFFS_MAP.put(1078, 6);	// Концентрация 6 ур 
		BUFFS_MAP.put(1397, 3);	// Чистота 3 ур 
		BUFFS_MAP.put(1040, 3);	// Щит 3 ур 
		BUFFS_MAP.put(1036, 2);	// Магический барьер 2 ур 
		BUFFS_MAP.put(1204, 2);	// Легкая походка 2 ур  
		BUFFS_MAP.put(1045, 6);	// Благословение тела 6 ур 
		BUFFS_MAP.put(1048, 6);	// Благословение духа 6 ур  

		//	[Танцы бойцу]
		BUFFS_MAP.put(271, 2);	// Танец воителя 2 ур 
		BUFFS_MAP.put(275, 1);	// Танец ярости 1 ур   
		BUFFS_MAP.put(274, 2);	// Танец огня 2 ур 
		BUFFS_MAP.put(272, 1);	// Танец вдохновения 1 ур 
		BUFFS_MAP.put(310, 1);	// Танец вампира 1 ур 
		BUFFS_MAP.put(277, 1);	// Танец света 1 ур 
		BUFFS_MAP.put(269, 1); // Песня охотника 1 ур 
		BUFFS_MAP.put(364, 1); // Песня чемпиона 1 ур 
		BUFFS_MAP.put(264, 2); // Песня земли 2 ур 
		BUFFS_MAP.put(267, 2); // Песня отражения 2 ур 
		BUFFS_MAP.put(304, 1); // Песня жизненной силы 1 ур 
		BUFFS_MAP.put(268, 1); // Песня ветра 1 ур 
		BUFFS_MAP.put(349, 1); // Песня возрождения  1 ур 
		BUFFS_MAP.put(265, 1); // Песня жизни 1 ур 
		BUFFS_MAP.put(266, 2); // Песня воды 2 ур 

		//	[Танцы Магу]
		BUFFS_MAP.put(276, 1); // Танец концентрации 1 ур 
		BUFFS_MAP.put(273, 2); // Танец мистика  2 ур   
		BUFFS_MAP.put(365, 1); // Танец сирены 1 ур 
		BUFFS_MAP.put(264, 2); // Песня земли 2 ур 
		BUFFS_MAP.put(267, 2); // Песня отражения 2 ур 
		BUFFS_MAP.put(304, 1); // Песня жизненной силы 1 ур 
		BUFFS_MAP.put(268, 1); // Песня ветра 1 ур 
		BUFFS_MAP.put(349, 1); // Песня возрождения  1 ур 
		BUFFS_MAP.put(265, 1); // Песня жизни 1 ур 
		BUFFS_MAP.put(266, 2); // Песня воды 2 ур 

		//	[Премиум Баф]
		BUFFS_MAP.put(1035, 4); // Ментальный щит  4 ур 
		BUFFS_MAP.put(1362, 1); // Напев Духа  1 ур 
		BUFFS_MAP.put(1033, 3); // Сопротивление Яду 3 ур 
		BUFFS_MAP.put(1259, 4); // Сопротивление Оглушению 4 ур 
		BUFFS_MAP.put(1044, 3); // Регенерация  3 ур 
		BUFFS_MAP.put(1087, 3); // Проворство  3 ур 
		BUFFS_MAP.put(1243, 6); // Благословение щита 6 ур 
		BUFFS_MAP.put(1304, 5); // Усиленная блокировка 5 ур 
		BUFFS_MAP.put(1416, 1); // Кулак Паагрио 1 ур 
		BUFFS_MAP.put(1284, 3); // Напев мщения 3 ур 
		BUFFS_MAP.put(1461, 1); // Напев охраны 1 ур 
		BUFFS_MAP.put(1542, 1); // Ответный крит. удар 1 ур. 
		BUFFS_MAP.put(1392, 3); // Сопротивление Святости 3 ур 
		BUFFS_MAP.put(1393, 3); // Сопротивление к Тьме 3 ур 
		BUFFS_MAP.put(311, 2); // Танец защиты 2 ур 
		BUFFS_MAP.put(305, 1); // Песня мщения 1 ур 

		//	[Выборочно]

		//	[Великое Могущество]
		BUFFS_MAP.put(1388, 3); // Великое Могущество 3 ур 

		//	[Великий Щит]
		BUFFS_MAP.put(1389, 3); // Великий Щит 3 ур 

		//	[Огонь]
		BUFFS_MAP.put(1356, 1); // Пророчество Огня 1 ур 

		//	[Вода]
		BUFFS_MAP.put(1355, 1); // Пророчество Воды 1 ур 

		//	[Ветер]
		BUFFS_MAP.put(1357, 1); // Пророчество Ветра 1 ур 

		//	[Победы]
		BUFFS_MAP.put(1363, 1); // Напев Победы 1 ур 

		//	[Паагрио]
		BUFFS_MAP.put(1414, 1); // Победа Паагрио 1 ур 

		//	[Дух берсерка]
		BUFFS_MAP.put(1062, 2); // Дух Берсерка 2 ур 
	}

	private static final Logger _log = LoggerFactory.getLogger(CustomBufferInstance.class);

	private static final long serialVersionUID = 1L;

	private static final int BUFF_COST_ITEM_ID = 57; // Adena
	private static final int BUFF_COST_ITEM_COUNT = 10000;

	private static final int BUFFS_PER_PAGE = 10;

	private static final int MAX_PERSONAL_SETS = 5;

	private static final String DATABASE_VAR = "@personal_buff_set";

	private static final TIntObjectMap<TIntObjectMap<BuffSet>> BUFF_SETS = new TIntObjectHashMap<TIntObjectMap<BuffSet>>();
	static
	{
		TIntObjectHashMap<BuffSet> buffSets = new TIntObjectHashMap<BuffSet>();
		BUFF_SETS.put(0, buffSets);

		BuffSet buffSet;
		int[] skillIds;

		// [Все]
		/*buffSet = new BuffSet();
		skillIds = BUFFS_MAP.keys();
		for(int skillId : skillIds)
			buffSet.addBuff(skillId);

		buffSet.setName(Language.ENGLISH, "All");
		buffSet.setName(Language.RUSSIAN, "Все");
		buffSets.put(0, buffSet);*/

		//	[Баффы бойцу]
		buffSet = new BuffSet();
		skillIds = new int[]{ 1068, 1086, 1077, 1242, 1240, 1043, 1268, 1040, 1036, 1204, 1045, 1048 };
		for(int skillId : skillIds)
			buffSet.addBuff(skillId);

		buffSet.setName(Language.ENGLISH, "Fighter buffs");
		buffSet.setName(Language.RUSSIAN, "Баффы бойцу");
		buffSets.put(1, buffSet);

		//	[Бафы Магу]
		buffSet = new BuffSet();
		skillIds = new int[]{ 1085, 1059, 1303, 1078, 1397, 1040, 1036, 1204, 1045, 1048 };
		for(int skillId : skillIds)
			buffSet.addBuff(skillId);

		buffSet.setName(Language.ENGLISH, "Mystic buffs");
		buffSet.setName(Language.RUSSIAN, "Бафы Магу");
		buffSets.put(2, buffSet);

		//	[Танцы бойцу]
		buffSet = new BuffSet();
		skillIds = new int[]{ 271, 275, 274, 272, 310, 277, 269, 364, 264, 267, 304, 268, 349, 265, 266 };
		for(int skillId : skillIds)
			buffSet.addBuff(skillId);

		buffSet.setName(Language.ENGLISH, "Fighter dances");
		buffSet.setName(Language.RUSSIAN, "Танцы бойцу");
		buffSets.put(3, buffSet);

		//	[Танцы Магу]
		buffSet = new BuffSet();
		skillIds = new int[]{ 276, 273, 365, 264, 267, 304, 268, 349, 265, 266 };
		for(int skillId : skillIds)
			buffSet.addBuff(skillId);

		buffSet.setName(Language.ENGLISH, "Mystic dances");
		buffSet.setName(Language.RUSSIAN, "Танцы Магу");
		buffSets.put(4, buffSet);

		//	[Премиум Баф]
		buffSet = new BuffSet();
		skillIds = new int[]{ 1035, 1362, 1033, 1259, 1044, 1087, 1243, 1304, 1416, 1284, 1461, 1542, 1392, 1393, 311, 305 };
		for(int skillId : skillIds)
			buffSet.addBuff(skillId);

		buffSet.setName(Language.ENGLISH, "Premium buffs");
		buffSet.setName(Language.RUSSIAN, "Премиум Баф");
		buffSets.put(5, buffSet);

		/*//	[Напев Победы]
		buffSet = new BuffSet();
		skillIds = new int[]{ 1363 };
		for(int skillId : skillIds)
			buffSet.addBuff(skillId);

		buffSet.setName(Language.ENGLISH, "Chant of Victory");
		buffSet.setName(Language.RUSSIAN, "Напев Победы");
		buffSets.put(6, buffSet);

		//	[Великий Щит]
		buffSet = new BuffSet();
		skillIds = new int[]{ 1389 };
		for(int skillId : skillIds)
			buffSet.addBuff(skillId);

		buffSet.setName(Language.ENGLISH, "Greater Shield");
		buffSet.setName(Language.RUSSIAN, "Великий Щит");
		buffSets.put(7, buffSet);

		//	[Великое Могущество]
		buffSet = new BuffSet();
		skillIds = new int[]{ 1388 };
		for(int skillId : skillIds)
			buffSet.addBuff(skillId);

		buffSet.setName(Language.ENGLISH, "Greater Might");
		buffSet.setName(Language.RUSSIAN, "Великое Могущество");
		buffSets.put(8, buffSet);

		//	[Дух берсерка]
		buffSet = new BuffSet();
		skillIds = new int[]{ 1062 };
		for(int skillId : skillIds)
			buffSet.addBuff(skillId);

		buffSet.setName(Language.ENGLISH, "Berserker Spirit");
		buffSet.setName(Language.RUSSIAN, "Дух Берсерка");
		buffSets.put(9, buffSet);*/

		/*
		//	[Выборочно]

		//	[Великое Могущество]
		buffSet = new BuffSet();
		skillIds = new int[]{ 1388 };
		for(int skillId : skillIds)
			buffSet.addBuff(skillId);

		buffSet.setName(Language.ENGLISH, "Greater Might");
		buffSet.setName(Language.RUSSIAN, "Великое Могущество");
		buffSets.put(6, buffSet);

		//	[Великий Щит]
		buffSet = new BuffSet();
		skillIds = new int[]{ 1389 };
		for(int skillId : skillIds)
			buffSet.addBuff(skillId);

		buffSet.setName(Language.ENGLISH, "Greater Shield");
		buffSet.setName(Language.RUSSIAN, "Великий Щит");
		buffSets.put(7, buffSet);

		//	[Огонь]
		buffSet = new BuffSet();
		skillIds = new int[]{ 1356 };
		for(int skillId : skillIds)
			buffSet.addBuff(skillId);

		buffSet.setName(Language.ENGLISH, "Fire");
		buffSet.setName(Language.RUSSIAN, "Огонь");
		buffSets.put(8, buffSet);

		//	[Вода]
		buffSet = new BuffSet();
		skillIds = new int[]{ 1355 };
		for(int skillId : skillIds)
			buffSet.addBuff(skillId);

		buffSet.setName(Language.ENGLISH, "Water");
		buffSet.setName(Language.RUSSIAN, "Вода");
		buffSets.put(9, buffSet);

		//	[Ветер]
		buffSet = new BuffSet();
		skillIds = new int[]{ 1357 };
		for(int skillId : skillIds)
			buffSet.addBuff(skillId);

		buffSet.setName(Language.ENGLISH, "Wind");
		buffSet.setName(Language.RUSSIAN, "Ветер");
		buffSets.put(10, buffSet);

		//	[Победы]
		buffSet = new BuffSet();
		skillIds = new int[]{ 1363 };
		for(int skillId : skillIds)
			buffSet.addBuff(skillId);

		buffSet.setName(Language.ENGLISH, "Victory");
		buffSet.setName(Language.RUSSIAN, "Победы");
		buffSets.put(11, buffSet);

		//	[Паагрио]
		buffSet = new BuffSet();
		skillIds = new int[]{ 1414 };
		for(int skillId : skillIds)
			buffSet.addBuff(skillId);

		buffSet.setName(Language.ENGLISH, "Paagrio");
		buffSet.setName(Language.RUSSIAN, "Паагрио");
		buffSets.put(12, buffSet);
		*/
	}

	public CustomBufferInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		StringTokenizer st = new StringTokenizer(command, "_");
		String cmd = st.nextToken();
		if(cmd.equals("buffs"))
		{
			if(!st.hasMoreTokens())
				return;

			String cmd2 = st.nextToken();
			if(cmd2.equals("view"))
			{
				if(!st.hasMoreTokens())
					return;

				int ownerId = Integer.parseInt(st.nextToken());
				TIntObjectMap<BuffSet> buffSets = BUFF_SETS.get(ownerId);
				if(buffSets == null || buffSets.isEmpty())
					return;

				if(!st.hasMoreTokens())
					return;

				int setId = Integer.parseInt(st.nextToken());
				BuffSet buffSet = buffSets.get(setId);
				if(buffSet == null)
					return;

				Skill[] buffs = buffSet.getBuffs();

				String html = HtmCache.getInstance().getHtml("custom/custom_buffer.htm", player);

				StringBuilder content = new StringBuilder();
				content.append("<table fixwidth=260 align=center><tr>");
				content.append("<td fixwidth=65>");
				content.append(HtmlUtils.htmlButton("< " + getString(7, player), "bypass -h npc_%objectId%_Chat 0", 60, 30));
				content.append("</td>");
				content.append("<td fixwidth=120 align=center>");
				content.append(buffSet.getName(player));
				content.append("</td>");
				content.append("<td fixwidth=65></td>");
				content.append("</tr></table>");

				if(BUFF_COST_ITEM_ID > 0 && BUFF_COST_ITEM_COUNT > 0)
				{
					content.append("<table fixwidth=260 align=center>");
					content.append("<tr><td align=center><img src=\"L2UI.SquareWhite\" width=200 height=1><br></td></tr>");

					if((ownerId != 0 || setId != 0) && buffs.length > 0)
					{
						content.append("<tr>");
						content.append("<td fixwidth=260 align=center><font color=63B9FF>");
						content.append(getString(9, player));
						content.append(": ");
						content.append(String.valueOf(BUFF_COST_ITEM_COUNT * buffs.length));
						content.append(" ");
						content.append(HtmlUtils.htmlItemName(BUFF_COST_ITEM_ID));
						content.append("</font></td>");
						content.append("</tr>");
					}
					content.append("</table>");
				}

				int page = 1;
				if(st.hasMoreTokens())
					page = Integer.parseInt(st.nextToken());

				if(buffs.length > 0)
					page = Math.min(page, (int) Math.ceil((double) buffs.length / BUFFS_PER_PAGE));

				if(ownerId != 0 || setId != 0)
				{
					content.append("<table fixwidth=260 align=center><tr>");
					if(buffs.length > 0)
					{
						content.append("<td align=center>");
						content.append(HtmlUtils.htmlButton(getString(4, player), "bypass -h npc_%objectId%_buffs_cast_set_" + ownerId + "_" + setId + "_self" + "_" + page, 70, 30));
						content.append("</td>");
						content.append("<td align=center>");
						content.append(HtmlUtils.htmlButton(getString(5, player), "bypass -h npc_%objectId%_buffs_cast_set_" + ownerId + "_" + setId + "_servitor" + "_" + page, 70, 30));
						content.append("</td>");
					}
					if(ownerId != 0)
					{
						if(buffs.length > 0)
							content.append("<td fixwidth=30></td>");
						content.append("<td align=center>");
						content.append(HtmlUtils.htmlButton(getString(6, player), "bypass -h npc_%objectId%_buffs_edit_" + ownerId + "_" + setId, 50, 30));
						content.append("</td>");
					}
					content.append("</tr></table>");
				}

				content.append("<br>");

				if(buffs.length > 0)
				{
					int maxCursor = Math.min(buffs.length, page * BUFFS_PER_PAGE);
					if(buffs.length > BUFFS_PER_PAGE)
					{
						content.append("<table fixwidth=120 align=center><tr>");
						content.append("<td fixwidth=30 align=center>");
						if(page > 1)
							content.append(HtmlUtils.htmlButton("<", "bypass -h npc_%objectId%_buffs_view_" + ownerId + "_" + setId + "_" + (page - 1), 30, 30));
						content.append("</td>");
						content.append("<td fixwidth=80 align=center>");
						content.append(getString(11, player));
						content.append(": ");
						content.append(page);
						content.append("</td>");
						content.append("<td fixwidth=30 align=center>");
						if(maxCursor < buffs.length)
							content.append(HtmlUtils.htmlButton(">", "bypass -h npc_%objectId%_buffs_view_" + ownerId + "_" + setId + "_" + (page + 1), 30, 30));
						content.append("</td>");
						content.append("</tr></table>");
					}
					content.append("<table align=center width=260 bgcolor=3D3D3D>");
					content.append("<tr>");
					content.append("<td width=260 align=center>");
					content.append("</td>");
					content.append("</tr>");
					content.append("</table>");
					int i = 0;
					for(int currCursor = page * BUFFS_PER_PAGE - BUFFS_PER_PAGE; currCursor < maxCursor; currCursor++)
					{
						if(i % 2 == 0)
							content.append("<table align=center width=260 bgcolor=000000>");
						else
							content.append("<table align=center width=260>");
						Skill skill = buffs[currCursor];
						content.append("<tr>");
						content.append("<td fixwidth=40 align=center><img src=\"");
						content.append(skill.getIcon());
						content.append("\" width=32 height=32></td>");
						content.append("<td>");
						content.append("<table>");
						content.append("<tr><td fixwidth=150 align=center><font color=LEVEL>");
						content.append(skill.getName(player));
						content.append("</font>&nbsp;<font color=44AAFF>(");
						content.append(getString(25, player));
						content.append(" ");
						content.append(skill.getLevel());
						content.append(")</font></td></tr>");
						content.append("<tr><td fixwidth=200 align=center>");
						content.append("<table><tr>");
						content.append("<td>");
						content.append(HtmlUtils.htmlButton(getString(4, player), "bypass -h npc_%objectId%_buffs_cast_skill_" + skill.getId() + "_self_" + ownerId + "_" + setId + "_" + page, 60, 30));
						content.append("</td><td>");
						content.append(HtmlUtils.htmlButton(getString(5, player), "bypass -h npc_%objectId%_buffs_cast_skill_" + skill.getId() + "_servitor_" + ownerId + "_" + setId + "_" + page, 60, 30));
						content.append("</td>");
						if(ownerId != 0)
						{
							content.append("<td>&nbsp;</td><td>");
							content.append(HtmlUtils.htmlButton("-", "bypass -h npc_%objectId%_buffs_delete_skill_" + ownerId + "_" + setId + "_" + skill.getId() + "_" + page, 30, 30));
							content.append("</td>");
						}
						content.append("</tr></table>");
						content.append("</td></tr>");
						content.append("</table><br>");
						content.append("</td>");
						content.append("</tr>");
						content.append("</table>");
						i++;
					}
					content.append("<table align=center width=260 bgcolor=3D3D3D>");
					content.append("<tr>");
					content.append("<td width=260 align=center>");
					content.append("</td>");
					content.append("</tr>");
					content.append("</table>");
					content.append("<br>");
				}
				html = html.replace("<?CONTENT?>", content);

				showChatWindow(player, html);
			}
			else if(cmd2.equals("cast"))
			{
				if(!st.hasMoreTokens())
					return;

				String cmd3 = st.nextToken();
				if(cmd3.equals("skill"))
				{
					if(!st.hasMoreTokens())
						return;

					int skillId = Integer.parseInt(st.nextToken());
					if(!BUFFS_MAP.containsKey(skillId))
						return;

					int skillLvl = BUFFS_MAP.get(skillId);
					Skill skill = SkillHolder.getInstance().getSkill(skillId, skillLvl > 0 ? skillLvl : 1);
					if(skill == null)
					{
						_log.warn("Cannot found skill ID[" + skillId + "] LEVEL[" + skillLvl + "] for buff!");
						return;
					}

					if(skillLvl <= 0)
					{
						skill = SkillHolder.getInstance().getSkill(skillId, skill.getBaseLevel());
						if(skill == null)
						{
							_log.warn("Cannot found skill ID[" + skillId + "] LEVEL[" + skill.getBaseLevel() + "] for buff!");
							return;
						}
					}

					if(player.isInCombat())
					{
						player.sendMessage(getString(26, player));
						return;
					}

					boolean self = true;
					if(st.hasMoreTokens())
					{
						String cmd4 = st.nextToken();
						if(cmd4.equals("servitor"))
							self = false;
					}

					if(self)
					{
						if(BUFF_COST_ITEM_ID > 0 && BUFF_COST_ITEM_COUNT > 0 && !ItemFunctions.deleteItem(player, BUFF_COST_ITEM_ID, BUFF_COST_ITEM_COUNT, true))
							player.sendMessage(getString(13, player));
						else
						{
							skill.getEffects(this, player, false, false);
							restore(player);
						}
					}
					else
					{
						Servitor servitor = player.getServitor();
						if(servitor != null)
						{
							if(BUFF_COST_ITEM_ID > 0 && BUFF_COST_ITEM_COUNT > 0 && !ItemFunctions.deleteItem(player, BUFF_COST_ITEM_ID, BUFF_COST_ITEM_COUNT, true))
								player.sendMessage(getString(13, player));
							else
							{
								skill.getEffects(this, servitor, false, false);
								restore(servitor);
							}
						}
						else
							player.sendMessage(getString(14, player));
					}

					if(st.hasMoreTokens())
					{
						int ownerId = Integer.parseInt(st.nextToken());
						if(st.hasMoreTokens())
						{
							int setId = Integer.parseInt(st.nextToken());
							if(st.hasMoreTokens())
							{
								int page = Integer.parseInt(st.nextToken());
								onBypassFeedback(player, "buffs_view_" + ownerId + "_" + setId + "_" + page);
								return;
							}
						}
					}
					showChatWindow(player, 0);
				}
				else if(cmd3.equals("set"))
				{
					if(!st.hasMoreTokens())
						return;

					int ownerId = Integer.parseInt(st.nextToken());

					TIntObjectMap<BuffSet> buffSets = BUFF_SETS.get(ownerId);
					if(buffSets == null || buffSets.isEmpty())
						return;

					if(!st.hasMoreTokens())
						return;

					int setId = Integer.parseInt(st.nextToken());

					BuffSet buffSet = buffSets.get(setId);
					if(buffSet == null)
						return;

					if(player.isInCombat())
					{
						player.sendMessage(getString(26, player));
						return;
					}

					boolean self = true;
					if(st.hasMoreTokens())
					{
						String cmd4 = st.nextToken();
						if(cmd4.equals("servitor"))
							self = false;
					}

					Skill[] buffs = buffSet.getBuffs();
					int cost = BUFF_COST_ITEM_COUNT * buffs.length;
					if(self)
					{
						if(BUFF_COST_ITEM_ID > 0 && BUFF_COST_ITEM_COUNT > 0 && !ItemFunctions.deleteItem(player, BUFF_COST_ITEM_ID, cost, true))
							player.sendMessage(getString(13, player));
						else
						{
							for(Skill skill : buffs)
								skill.getEffects(this, player, false, false);
							restore(player);
						}
					}
					else
					{
						Servitor servitor = player.getServitor();
						if(servitor != null)
						{
							if(BUFF_COST_ITEM_ID > 0 && BUFF_COST_ITEM_COUNT > 0 && !ItemFunctions.deleteItem(player, BUFF_COST_ITEM_ID, cost, true))
								player.sendMessage(getString(13, player));
							else
							{
								for(Skill skill : buffs)
									skill.getEffects(this, servitor, false, false);
								restore(servitor);
							}
						}
						else
							player.sendMessage(getString(14, player));
					}

					int page = 1;
					if(st.hasMoreTokens())
						page = Integer.parseInt(st.nextToken());

					onBypassFeedback(player, "buffs_view_" + ownerId + "_" + setId + "_" + page);
				}
			}
			else if(cmd2.equals("add"))
			{
				String cmd3 = st.nextToken();
				if(cmd3.startsWith("set "))
				{
					String setName = cmd3.substring(4);
					if(setName.isEmpty())
						return;

					BuffSet buffSet = new BuffSet();
					for(Language lang : Language.VALUES)
						buffSet.setName(lang, setName);

					TIntObjectMap<BuffSet> buffSets = BUFF_SETS.get(player.getObjectId());
					if(buffSets == null)
					{
						buffSets = new TIntObjectHashMap<BuffSet>();
						BUFF_SETS.put(player.getObjectId(), buffSets);
					}

					int setId = 0;
					for(int i = 1; i <= MAX_PERSONAL_SETS; i++)
					{
						if(!buffSets.containsKey(i))
						{
							setId = i;
							break;
						}
					}
					if(setId != 0)
					{
						buffSets.put(setId, buffSet);
						saveBuffSet(player, setId);
					}
					else
						player.sendMessage(getString(12, player));

					showChatWindow(player, 0);
				}
				else if(cmd3.equals("skill"))
				{
					if(!st.hasMoreTokens())
						return;

					int ownerId = Integer.parseInt(st.nextToken());
					if(ownerId == 0)
						return;

					TIntObjectMap<BuffSet> buffSets = BUFF_SETS.get(ownerId);
					if(buffSets == null || buffSets.isEmpty())
						return;

					if(!st.hasMoreTokens())
						return;

					int setId = Integer.parseInt(st.nextToken());
					BuffSet buffSet = buffSets.get(setId);
					if(buffSet == null)
						return;

					if(!st.hasMoreTokens())
						return;

					int skillId = Integer.parseInt(st.nextToken());
					if(buffSet.addBuff(skillId))
						saveBuffSet(player, setId);

					int page = 1;
					if(st.hasMoreTokens())
						page = Integer.parseInt(st.nextToken());

					onBypassFeedback(player, "buffs_edit_" + ownerId + "_" + setId + "_" + page);
				}
			}
			else if(cmd2.equals("edit"))
			{
				if(!st.hasMoreTokens())
					return;

				int ownerId = Integer.parseInt(st.nextToken());
				if(ownerId == 0)
					return;

				TIntObjectMap<BuffSet> buffSets = BUFF_SETS.get(ownerId);
				if(buffSets == null || buffSets.isEmpty())
					return;

				if(!st.hasMoreTokens())
					return;

				int setId = Integer.parseInt(st.nextToken());
				BuffSet buffSet = buffSets.get(setId);
				if(buffSet == null)
					return;

				String html = HtmCache.getInstance().getHtml("custom/custom_buffer.htm", player);

				StringBuilder content = new StringBuilder();
				content.append("<table fixwidth=260 align=center><tr>");
				content.append("<td fixwidth=65>");
				content.append(HtmlUtils.htmlButton("< " + getString(7, player), "bypass -h npc_%objectId%_buffs_view_" + ownerId + "_" + setId, 60, 30));
				content.append("</td>");
				content.append("<td fixwidth=120 align=center>");
				content.append(buffSet.getName(player));
				content.append("</td>");
				content.append("<td fixwidth=65></td>");
				content.append("</tr></table>");
				content.append("<table fixwidth=260 align=center>");
				content.append("<tr><td align=center><img src=\"L2UI.SquareWhite\" width=200 height=1><br></td></tr>");
				content.append("</table>");

				content.append("<table fixwidth=260 align=center><tr>");
				content.append("<td align=center>");
				content.append(HtmlUtils.htmlButton(getString(10, player), "bypass -h npc_%objectId%_buffs_delete_set_" + ownerId + "_" + setId, 80, 30));
				content.append("</td>");
				content.append("</tr></table>");

				content.append("<br>");

				List<Skill> buffsToAdd = new ArrayList<Skill>();
				for(TIntIntIterator iterator = BUFFS_MAP.iterator(); iterator.hasNext();)
				{
					iterator.advance();

					int skillId = iterator.key();
					if(buffSet.containsBuff(skillId))
						continue;

					int skillLvl = iterator.value();
					Skill skill = SkillHolder.getInstance().getSkill(skillId, skillLvl > 0 ? skillLvl : 1);
					if(skill == null)
						continue;

					if(skillLvl <= 0)
					{
						skill = SkillHolder.getInstance().getSkill(skillId, skill.getBaseLevel());
						if(skill == null)
							continue;
					}

					buffsToAdd.add(skill);
				}

				if(buffsToAdd.size() > 0)
				{
					int page = 1;
					if(st.hasMoreTokens())
						page = Integer.parseInt(st.nextToken());

					page = Math.min(page, (int) Math.ceil((double) buffsToAdd.size() / BUFFS_PER_PAGE));
					int maxCursor = Math.min(buffsToAdd.size(), page * BUFFS_PER_PAGE);
					if(buffsToAdd.size() > BUFFS_PER_PAGE)
					{
						content.append("<table fixwidth=120 align=center><tr>");
						content.append("<td fixwidth=30 align=center>");
						if(page > 1)
							content.append(HtmlUtils.htmlButton("<", "bypass -h npc_%objectId%_buffs_edit_" + ownerId + "_" + setId + "_" + (page - 1), 30, 30));
						content.append("</td>");
						content.append("<td fixwidth=80 align=center>");
						content.append(getString(11, player));
						content.append(": ");
						content.append(page);
						content.append("</td>");
						content.append("<td fixwidth=30 align=center>");
						if(maxCursor < buffsToAdd.size())
							content.append(HtmlUtils.htmlButton(">", "bypass -h npc_%objectId%_buffs_edit_" + ownerId + "_" + setId + "_" + (page + 1), 30, 30));
						content.append("</td>");
						content.append("</tr></table>");
					}
					content.append("<table align=center width=260 bgcolor=3D3D3D>");
					content.append("<tr>");
					content.append("<td width=260 align=center>");
					content.append("</td>");
					content.append("</tr>");
					content.append("</table>");
					int i = 0;
					for(int currCursor = page * BUFFS_PER_PAGE - BUFFS_PER_PAGE; currCursor < maxCursor; currCursor++)
					{
						if(i % 2 == 0)
							content.append("<table align=center width=260 bgcolor=000000>");
						else
							content.append("<table align=center width=260>");
						Skill skill = buffsToAdd.get(currCursor);
						content.append("<tr>");
						content.append("<td fixwidth=40 align=center><img src=\"");
						content.append(skill.getIcon());
						content.append("\" width=32 height=32></td>");
						content.append("<td>");
						content.append("<table>");
						content.append("<tr><td fixwidth=200 align=center><font color=LEVEL>");
						content.append(skill.getName(player));
						content.append("</font></td></tr>");
						content.append("<tr><td fixwidth=200 align=center>");
						content.append("<table><tr>");
						content.append("<td>");
						content.append(HtmlUtils.htmlButton("+", "bypass -h npc_%objectId%_buffs_add_skill_" + ownerId + "_" + setId + "_" + skill.getId() + "_" + page, 30, 30));
						content.append("</td>");
						content.append("</tr></table>");
						content.append("</td></tr>");
						content.append("</table><br>");
						content.append("</td>");
						content.append("</tr>");
						content.append("</table>");
						i++;
					}
					content.append("<table align=center width=260 bgcolor=3D3D3D>");
					content.append("<tr>");
					content.append("<td width=260 align=center>");
					content.append("</td>");
					content.append("</tr>");
					content.append("</table>");
					content.append("<br>");
				}
				html = html.replace("<?CONTENT?>", content);

				showChatWindow(player, html);
			}
			else if(cmd2.equals("delete"))
			{
				if(!st.hasMoreTokens())
					return;

				String cmd3 = st.nextToken();
				if(cmd3.equals("set"))
				{
					int ownerId = Integer.parseInt(st.nextToken());
					if(ownerId == 0)
						return;

					TIntObjectMap<BuffSet> buffSets = BUFF_SETS.get(ownerId);
					if(buffSets == null || buffSets.isEmpty())
						return;

					if(!st.hasMoreTokens())
						return;

					int setId = Integer.parseInt(st.nextToken());
					buffSets.remove(setId);
					deleteBuffSet(player, setId);

					showChatWindow(player, 0);
				}
				else if(cmd3.equals("skill"))
				{
					int ownerId = Integer.parseInt(st.nextToken());
					if(ownerId == 0)
						return;

					TIntObjectMap<BuffSet> buffSets = BUFF_SETS.get(ownerId);
					if(buffSets == null || buffSets.isEmpty())
						return;

					if(!st.hasMoreTokens())
						return;

					int setId = Integer.parseInt(st.nextToken());
					BuffSet buffSet = buffSets.get(setId);

					if(!st.hasMoreTokens())
						return;

					int skillId = Integer.parseInt(st.nextToken());
					buffSet.removeBuff(skillId);
					saveBuffSet(player, setId);

					int page = 1;
					if(st.hasMoreTokens())
						page = Integer.parseInt(st.nextToken());

					onBypassFeedback(player, "buffs_view_" + ownerId + "_" + setId + "_" + page);
				}
			}
		}
		else if(cmd.equals("restore"))
		{
			restore(player);

			Servitor servitor = player.getServitor();
			if(servitor != null)
			{
				restore(servitor);
				player.sendMessage(getString(17, player));
			}
			else
				player.sendMessage(getString(16, player));
	
			showChatWindow(player, 0);
		}
		else if(cmd.equals("clean"))
		{
			if(!st.hasMoreTokens())
				return;

			String cmd2 = st.nextToken();
			if(cmd2.equals("self"))
			{
				for(Effect effect : player.getEffectList().getAllEffects())
				{
					if(effect.isOffensive())
						continue;

					if(effect.getSkill().isToggle())
						continue;

					if(effect.getSkill().isPassive())
						continue;

					if(!effect.getSkill().isCancelable())
						continue;

					effect.exit();
				}

				player.sendMessage(getString(19, player));

				showChatWindow(player, 0);
			}
			else if(cmd2.equals("servitor"))
			{
				Servitor servitor = player.getServitor();
				if(servitor != null)
				{
					for(Effect effect : servitor.getEffectList().getAllEffects())
					{
						if(effect.isOffensive())
							continue;

						if(effect.getSkill().isToggle())
							continue;

						if(effect.getSkill().isPassive())
							continue;

						if(!effect.getSkill().isCancelable())
							continue;

						effect.exit();
					}
					player.sendMessage(getString(20, player));
				}
				else
					player.sendMessage(getString(14, player));

				showChatWindow(player, 0);
			}
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public String getHtmlPath(int npcId, int val, Player player)
	{
		String filename;
		if(val == 0)
			filename = "custom_buffer.htm";
		else
			filename = "custom_buffer-" + val + ".htm";

 		if(getTemplate().getHtmRoot() != null)
			return getTemplate().getHtmRoot() + filename + ".htm";

		String temp = "custom/" + filename + ".htm";
		if(HtmCache.getInstance().getIfExists(temp, player) != null)
			return temp;

		return super.getHtmlPath(npcId, val, player);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... replace)
	{
		if(val == 0)
		{
			String html = HtmCache.getInstance().getHtml("custom/custom_buffer.htm", player);
			StringBuilder content = new StringBuilder();
			content.append("<table align=center>");

			content.append("<tr><td align=center><font color=63B9FF>");
			content.append(getString(8, player));
			content.append(": ");
			content.append(String.valueOf(BUFF_COST_ITEM_COUNT));
			content.append(" ");
			content.append(HtmlUtils.htmlItemName(BUFF_COST_ITEM_ID));
			content.append("</font></td></tr>");
			content.append("<tr><td><img src=\"L2UI.SquareWhite\" width=200 height=1><br></td></tr>");

			TIntObjectMap<BuffSet> buffSets = BUFF_SETS.get(0);
			if(buffSets != null && !buffSets.isEmpty())
			{
				content.append("<tr><td align=center>");
				content.append(getString(3, player));
				content.append("</td></tr>");
				content.append("<tr><td></td></tr>");

				int[] keys = buffSets.keys();
				Arrays.sort(keys);
				for(int key : keys)
				{
					content.append("<tr><td align=center>");
					content.append(HtmlUtils.htmlButton(buffSets.get(key).getName(player), "bypass -h npc_%objectId%_buffs_view_0_" + key, 200, 30));
					content.append("</td></tr>");
				}
				content.append("<tr><td><br></td></tr>");
			}

			content.append("<tr><td align=center>");
			content.append(getString(1, player));
			content.append("</td></tr>");
			content.append("<tr><td></td></tr>");

			if(!BUFF_SETS.containsKey(player.getObjectId()))
				restoreBuffSets(player);

			buffSets = BUFF_SETS.get(player.getObjectId());
			if(buffSets == null || buffSets.size() < MAX_PERSONAL_SETS)
			{
				content.append("<tr><td align=center>");
				content.append("<edit var=\"name\" width=190>");
				content.append("</td></tr>");
				content.append("<tr><td></td></tr>");
				content.append("<tr><td align=center>");
				content.append(HtmlUtils.htmlButton(getString(2, player), "bypass -h npc_%objectId%_buffs_add_set $name", 200, 30));
				content.append("</td></tr>");
				if(buffSets != null && !buffSets.isEmpty())
					content.append("<tr><td></td></tr>");
			}

			if(buffSets != null && !buffSets.isEmpty())
			{
				int[] keys = buffSets.keys();
				Arrays.sort(keys);
				for(int key : keys)
				{
					content.append("<tr><td align=center>");
					content.append(HtmlUtils.htmlButton(buffSets.get(key).getName(player), "bypass -h npc_%objectId%_buffs_view_" + player.getObjectId() + "_" + key, 200, 30));
					content.append("</td></tr>");
				}
			}

			content.append("<tr><td><br></td></tr>");

			content.append("<tr><td align=center>");
			content.append(getString(18, player));
			content.append("</td></tr>");
			content.append("<tr><td align=center><table><tr>");
			content.append("<td align=center>");
			content.append(HtmlUtils.htmlButton(getString(21, player), "bypass -h npc_%objectId%_clean_self", 80, 30));
			content.append("</td>");
			content.append("<td align=center>");
			content.append(HtmlUtils.htmlButton(getString(22, player), "bypass -h npc_%objectId%_clean_servitor", 80, 30));
			content.append("</td>");
			content.append("</tr></table></td></tr>");

			content.append("<tr><td><br></td></tr>");

			content.append("<tr><td align=center>");
			content.append(HtmlUtils.htmlButton(getString(15, player), "bypass -h npc_%objectId%_restore", 200, 30));
			content.append("</td></tr>");

			content.append("</table>");
			html = html.replace("<?CONTENT?>", content);

			showChatWindow(player, html);
		}
		else
			super.showChatWindow(player, val, replace);
	}

	private static void saveBuffSet(Player player, int setId)
	{
		TIntObjectMap<BuffSet> buffSets = BUFF_SETS.get(player.getObjectId());
		if(buffSets == null || buffSets.isEmpty())
			return;

		BuffSet buffSet = buffSets.get(setId);
		if(buffSet == null)
			return;

		StringBuilder value = new StringBuilder();
		value.append(buffSet.getName(player));
		value.append(";");
		for(Skill skill : buffSet.getBuffs())
		{
			value.append(skill.getId());
			value.append(";");
		}
		player.setVar(DATABASE_VAR + "_" + setId, value.toString());
	}

	private static void deleteBuffSet(Player player, int setId)
	{
		player.unsetVar(DATABASE_VAR + "_" + setId);
	}

	private static void restoreBuffSets(Player player)
	{
		TIntObjectMap<BuffSet> buffSets = new TIntObjectHashMap<BuffSet>();
		BUFF_SETS.put(player.getObjectId(), buffSets);

		for(int i = 1; i <= MAX_PERSONAL_SETS; i++)
		{
			String value = player.getVar(DATABASE_VAR + "_" + i);
			if(value == null)
				continue;

			String[] data = value.split(";");
			if(data.length > 0)
			{
				BuffSet buffSet = new BuffSet();
				for(Language lang : Language.VALUES)
					buffSet.setName(lang, data[0]);

				if(data.length > 1)
				{
					for(int j = 1; j < data.length; j++)
						buffSet.addBuff(Integer.parseInt(data[j]));
				}
				buffSets.put(i, buffSet);
			}
		}
	}

	private static String getString(int id, Player player)
	{
		String[] strings = null;
		switch(id)
		{
			case 1:
				strings = new String[]{ "Personal sets",  "Персональные наборы"};
				break;
			case 2:
				strings = new String[]{ "Add set", "Добавить набор" };
				break;
			case 3:
				strings = new String[]{ "Default sets", "Стандартные наборы" };
				break;
			case 4:
				strings = new String[]{ "Me", "Себе" };
				break;
			case 5:
				strings = new String[]{ "Servitor", "Слуге" };
				break;
			case 6:
				strings = new String[]{ "Edit", "Редакт." };
				break;
			case 7:
				strings = new String[]{ "Back", "Назад" };
				break;
			case 8:
				strings = new String[]{ "One buff price", "Цена одного баффа" };
				break;
			case 9:
				strings = new String[]{ "Price for all buffs", "Цена за все баффы" };
				break;
			case 10:
				strings = new String[]{ "Delete", "Удалить" };
				break;
			case 11:
				strings = new String[]{ "Page", "Страница" };
				break;
			case 12:
				strings = new String[]{ "Create impossible! You have reached the maximum number of personal buff sets.", "Создание невозможно! Вы достигли максимальное количество персональных наборов баффов." };
				break;
			case 13:
				strings = new String[]{ "You do not have items! Come back later.", "У вас недостаточно предметов! Приходите позже." };
				break;
			case 14:
				strings = new String[]{ "You do not have servitors.", "У вас нету слуг." };
				break;
			case 15:
				strings = new String[]{ "Restore HP/MP/CP", "Восстановить HP/MP/CP" };
				break;
			case 16:
				strings = new String[]{ "Your HP, MP and CP have been completely restored.", "Ваше HP, MP и CP было полностью восстановлено." };
				break;
			case 17:
				strings = new String[]{ "HP, MP and CP your and your servitors have been completely restored.", "HP, MP и CP ваше и ваших слуг было полностью восстановлено." };
				break;
			case 18:
				strings = new String[]{ "Clear away buffs", "Очистить от баффов" };
				break;
			case 19:
				strings = new String[]{ "You have been successfully cleared.", "Вы были успешно очищены." };
				break;
			case 20:
				strings = new String[]{ "Your servitors have been successfully cleared.", "Ваши слуги были успешно очищены." };
				break;
			case 21:
				strings = new String[]{ "Me", "Себя" };
				break;
			case 22:
				strings = new String[]{ "Servitor", "Слугу" };
				break;
			case 23:
				strings = new String[]{ "Other", "Прочее" };
				break;
			case 24:
				strings = new String[]{ "You can not recover during a combat!", "Нельзя восстановиться во время боя!" };
				break;
			case 25:
				strings = new String[]{ "Lvl.", "Ур." };
				break;
			case 26:
				strings = new String[]{ "You can not apply the effect in combat!", "Нельзя применить эффект в состоянии боя!" };
				break;
		}

		if(strings != null)
			return player.isLangRus() ? strings[1] : strings[0];

		return "";
	}

	private void restore(Creature target)
	{
		if(target.isInCombat())
			return;

		target.setCurrentHpMp(target.getMaxHp(), target.getMaxMp());
		target.setCurrentCp(target.getMaxCp());
	}
}