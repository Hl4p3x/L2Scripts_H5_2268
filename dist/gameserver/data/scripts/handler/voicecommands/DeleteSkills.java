package handler.voicecommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import l2s.commons.map.hash.TIntStringHashMap;
import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.SkillAcquireHolder;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.AcquireType;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.scripts.ScriptFile;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Util;

/**
 * @author Bonux
**/
public class DeleteSkills extends Functions implements IVoicedCommandHandler, ScriptFile
{
	private static class SkillComparator implements Comparator<Skill>
	{
		private static final Comparator<Skill> instance = new SkillComparator();

		public static final Comparator<Skill> getInstance()
		{
			return instance;
		}

		@Override
		public int compare(Skill o1, Skill o2)
		{
			if(o1 == null || o2 == null)
				return 0;
			if(o1.getId() == o2.getId())
				return Integer.compare(o2.getLevel(), o1.getLevel());
			return Long.compare(o2.getId(), o1.getId());
		}
	}

	private static final AcquireType[] DELETE_SKILL_TYPES = new AcquireType[]{
		AcquireType.NORMAL,
		//AcquireType.COLLECTION,
		//AcquireType.TRANSFORMATION,
		//AcquireType.TRANSFER_EVA_SAINTS,
		//AcquireType.TRANSFER_SHILLIEN_SAINTS,
		//AcquireType.TRANSFER_CARDINAL,
		//AcquireType.FISHING,
		//AcquireType.CERTIFICATION,
		//AcquireType.REBORN,
		AcquireType.MULTICLASS
	};
		
	private static final String[] COMMANDS = new String[] { "deleteskills", "deleteskill" };

	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		if(Config.DELETE_SKILL_SERVICE_ITEM_ID <= 0)
			return false;

		StringTokenizer st = new StringTokenizer(args);
		TIntStringHashMap tpls = HtmCache.getInstance().getTemplates("command/deleteskills.htm", player);
		String html = tpls.get(0);
		String message = null;
		StringBuilder content = new StringBuilder();

		int page = 1;
		if(command.equalsIgnoreCase("deleteskill"))
		{
			if(st.hasMoreTokens())
			{
				try
				{
					int skillId = Integer.parseInt(st.nextToken());
					Skill skill = player.getKnownSkill(skillId);
					if(skill != null)
					{
						boolean possible = false;
						for(AcquireType acquireType : DELETE_SKILL_TYPES)
						{
							if(SkillAcquireHolder.getInstance().isSkillPossible(player, skill, acquireType))
							{
								possible = true;
								break;
							}
						}

						if(possible)
						{
							if(Config.DELETE_SKILL_SERVICE_ITEM_COUNT <= 0 || ItemFunctions.deleteItem(player, Config.DELETE_SKILL_SERVICE_ITEM_ID, Config.DELETE_SKILL_SERVICE_ITEM_COUNT))
							{
								player.removeSkill(skill, true);
								player.sendSkillList();
								message = tpls.get(5).replace("<?skill_name?>", skill.getName(player));
							}
							else
								message = tpls.get(7);
						}
						else
							message = tpls.get(3).replace("<?skill_name?>", skill.getName(player));
					}
					else
						message = tpls.get(2);
				}
				catch(Exception e)
				{
					message = tpls.get(4);
				}

				if(st.hasMoreTokens())
				{
					try
					{
						page = Integer.parseInt(st.nextToken());
					}
					catch(Exception e)
					{
						//
					}
				}
			}
			else
				message = tpls.get(4);
		}

		if(command.equalsIgnoreCase("deleteskills"))
		{
			if(st.hasMoreTokens())
			{
				try
				{
					page = Integer.parseInt(st.nextToken());
				}
				catch(Exception e)
				{
					//
				}
			}
		}

		List<Skill> skills = new ArrayList<Skill>();
		loop: for(Skill skill : player.getAllSkills())
		{
			for(AcquireType acquireType : DELETE_SKILL_TYPES)
			{
				if(SkillAcquireHolder.getInstance().isSkillPossible(player, skill, acquireType))
				{
					skills.add(skill);
					continue loop;
				}
			}
		}

		if(skills.isEmpty())
		{
			if(message == null)
				message = tpls.get(6);
		}
		else
		{
			Collections.sort(skills, SkillComparator.getInstance());

			int maxPage = ((skills.size() - 1) / 9) + 1;
			page = Math.max(1, Math.min(page, maxPage));

			StringBuilder skillList = new StringBuilder();
			for(int i = ((page - 1) * 9); i < Math.min(skills.size(), page * 9); i++)
			{
				Skill skill = skills.get(i);
				String skillBlock = tpls.get(101);
				skillBlock = skillBlock.replace("<?bg_color?>", (i == 0 || (i % 2) == 0) ? tpls.get(102) : tpls.get(103));
				skillBlock = skillBlock.replace("<?skill_name?>", skill.getName(player));
				skillBlock = skillBlock.replace("<?skill_icon?>", skill.getIcon());
				skillBlock = skillBlock.replace("<?skill_id?>", String.valueOf(skill.getId()));
				skillBlock = skillBlock.replace("<?skill_level?>", String.valueOf(skill.getLevel()));
				skillList.append(skillBlock);
			}

			String mainBlock = tpls.get(100);
			mainBlock = mainBlock.replace("<?skill_list?>", skillList.toString());
			mainBlock = mainBlock.replace("<?prev_page_button?>", page == 1 ? "" : tpls.get(104).replace("<?prev_page?>", String.valueOf(page - 1)));
			mainBlock = mainBlock.replace("<?current_page?>", String.valueOf(page));
			mainBlock = mainBlock.replace("<?next_page_button?>", page == maxPage ? "" : tpls.get(105).replace("<?next_page?>", String.valueOf(page + 1)));
			content.append(mainBlock);
		}

		if(message != null)
			html = html.replace("<?message?>", tpls.get(1).replace("<?message_text?>", message));
		else
			html = html.replace("<?message?>", "");

		if(Config.DELETE_SKILL_SERVICE_ITEM_COUNT > 0)
		{
			String priceBlock = tpls.get(106);
			priceBlock = priceBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(Config.DELETE_SKILL_SERVICE_ITEM_ID));
			priceBlock = priceBlock.replace("<?fee_item_count?>", Util.formatAdena(Config.DELETE_SKILL_SERVICE_ITEM_COUNT));
			html = html.replace("<?price?>", priceBlock);
		}
		else
			html = html.replace("<?price?>", "");

		html = html.replace("<?content?>", content.toString());
		html = html.replace("<?current_page?>", String.valueOf(page));

		show(html, player);
		return true;
	}

	@Override
	public void onLoad()
	{
		if(Config.DELETE_SKILL_SERVICE_ITEM_ID > 0)
			VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
	}

	@Override
	public void onReload()
	{
		//
	}

	@Override
	public void onShutdown()
	{
		//
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
}