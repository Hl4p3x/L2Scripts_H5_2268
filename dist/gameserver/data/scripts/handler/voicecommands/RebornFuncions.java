package handler.voicecommands;

import java.util.Collection;
import java.util.StringTokenizer;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SkillAcquireHolder;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.base.AcquireType;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.ExAcquirableSkillListByClass;
import l2s.gameserver.scripts.ScriptFile;
import l2s.gameserver.utils.ItemFunctions;
import services.RebornSystem;

/**
 * @author Iqman
 * @reworked by Bonux
 */
public class RebornFuncions implements IVoicedCommandHandler, ScriptFile
{
	private static final String[] COMMANDS_LIST = new String[] { "reborn", "skills", "cancel" };

	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		if(Config.ALLOWED_REBORN_COUNT <= 0)
			return false;

		if(command.equalsIgnoreCase("reborn"))
		{
			if(Config.CHANGE_CLASS_ON_REBORN)
			{
				StringTokenizer st = new StringTokenizer(args);
				if(st.hasMoreTokens())
				{
					try
					{
						RebornSystem.reborn(player, ClassId.VALUES[Integer.parseInt(st.nextToken())]);
					}
					catch(Exception e)
					{
						return false;
					}
					return true;
				}
			}

			RebornSystem.tryReborn(player);
			return true;
		}
		else if(command.equalsIgnoreCase("skills"))
		{
			Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, AcquireType.REBORN);

			ExAcquirableSkillListByClass asl = new ExAcquirableSkillListByClass(AcquireType.REBORN, skills.size());
			for(SkillLearn s : skills)
				asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getCost(), 0);

			player.sendPacket(asl);
			player.sendActionFailed();
			return true;
		}
		else if(command.equalsIgnoreCase("cancel"))
		{
			if(!player.isBaseClassActive())
				return true;

			for(int[] rebornReward : Config.REBORN_REWARD_ITEMS)
				ItemFunctions.deleteItemsEverywhere(player, rebornReward[0]);

			for(SkillLearn learn : SkillAcquireHolder.getInstance().getAvailableSkills(null, AcquireType.REBORN))
				player.removeSkill(learn.getId(), true);

			int rebornLevel = player.getVarInt(RebornSystem.REBORN_LEVEL);
			for(int[] rebornReward : Config.REBORN_REWARD_ITEMS)
			{
				int itemId = rebornReward[0];
				int itemCount = rebornReward.length >= 2 ? rebornReward[1] : 1;
				ItemFunctions.addItem(player, itemId, itemCount * rebornLevel, "Reborn remove skills");
			}

			player.sendMessage(new CustomMessage("scripts.handler.voicecommands.RebornFuncions.1", player));
			return true;
		}
		return false;
	}

	@Override
	public void onLoad()
	{
		if(Config.ALLOWED_REBORN_COUNT > 0)
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
		return COMMANDS_LIST;
	}
}