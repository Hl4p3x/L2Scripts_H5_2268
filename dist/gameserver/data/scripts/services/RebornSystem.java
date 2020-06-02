package services;

import l2s.commons.map.hash.TIntStringHashMap;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.listener.actor.player.OnLevelChangeListener;
import l2s.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ConfirmDlgPacket;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.scripts.ScriptFile;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author Iqman
 * @reworked by Bonux
 */
public final class RebornSystem implements ScriptFile
{
	public static final String REBORN_LEVEL = "@reborn_level";

	private final RebornListenes _rebornListenes = new RebornListenes();

	public class RebornListenes implements OnPlayerEnterListener, OnLevelChangeListener
	{
		@Override
		public void onPlayerEnter(Player player)
		{
			callReborn(player);
		}

		@Override
		public void onLevelChange(Player player, int oldLvl, int newLvl)
		{
			callReborn(player);
		}
	}

	public static boolean tryReborn(Player player)
	{
		String msg = callReborn(player);
		if(msg != null)
		{
			player.sendMessage(msg);
			return false;
		}
		return true;
	}

	private static String callReborn(Player player)
	{
		if(!player.isBaseClassActive())
			return new CustomMessage("scripts.services.RebornSystem.1", player).toString();

		if(player.getLevel() < Config.ALT_MAX_LEVEL)
			return new CustomMessage("scripts.services.RebornSystem.2", player).toString();

		int rebornLevel = player.getVarInt(REBORN_LEVEL);
		if(rebornLevel >= Config.ALLOWED_REBORN_COUNT)
			return new CustomMessage("scripts.services.RebornSystem.3", player).toString();

		String dlg;
		if(Config.ALLOWED_REBORN_COUNT == 1)
			dlg = new CustomMessage("scripts.services.RebornSystem.4", player).toString();
		else if(rebornLevel == 0)
			dlg = new CustomMessage("scripts.services.RebornSystem.5", player).toString();
		else if((Config.ALLOWED_REBORN_COUNT - rebornLevel) == 1)
			dlg = new CustomMessage("scripts.services.RebornSystem.6", player).toString();
		else
			dlg = new CustomMessage("scripts.services.RebornSystem.7", player).toString();

		player.ask(new ConfirmDlgPacket(SystemMsg.S1, 0).addString(dlg), new OnAnswerListener()
		{
			@Override
			public void sayYes()
			{
				reborn(player, null);
			}

			@Override
			public void sayNo()
			{
				//
			}
		});
		return null;
	}

	public static void reborn(Player player, ClassId classId)
	{
		if(!player.isBaseClassActive())
			return;

		if(player.getLevel() < Config.ALT_MAX_LEVEL)
			return;

		int rebornLevel = player.getVarInt(REBORN_LEVEL);
		if(rebornLevel >= Config.ALLOWED_REBORN_COUNT)
			return;

		if(classId == null)
		{
			if(Config.CHANGE_CLASS_ON_REBORN)
			{
				TIntStringHashMap tpls = HtmCache.getInstance().getTemplates("custom/reborn.htm", player);
				String html = tpls.get(0);

				StringBuilder content = new StringBuilder();

				for(ClassId c : ClassId.VALUES)
				{
					if(c.isDummy())
						continue;

					if(!c.isOfLevel(ClassLevel.NONE))
						continue;

					String tempClassButton = tpls.get(1);
					tempClassButton = tempClassButton.replace("<?class_id?>", String.valueOf(c.getId()));
					tempClassButton = tempClassButton.replace("<?button_name?>", c.getName(player));
					content.append(tempClassButton);
				}

				html = html.replace("<?content?>", content.toString());

				HtmlUtils.sendHtm(player, html);
				return;
			}

			ClassId currClassId = player.getClassId();
			for(ClassId c : ClassId.VALUES)
			{
				if(c.isDummy())
					continue;

				if(!c.isOfLevel(ClassLevel.NONE))
					continue;

				if(!currClassId.equalsOrChildOf(c))
					continue;

				classId = c;
				break;
			}
		}

		if(classId == null)
			return;

		player.setClassId(classId.getId(), true, false);
		player.broadcastUserInfo(true);
		player.addExpAndSp(Experience.LEVEL[Math.max(1, Math.min(Config.ALT_MAX_LEVEL - 1, Config.REBORN_START_LEVEL))] - player.getExp(), 0);

		for(int[] rebornReward : Config.REBORN_REWARD_ITEMS)
		{
			int itemId = rebornReward[0];
			int itemCount = rebornReward.length >= 2 ? rebornReward[1] : 1;
			ItemFunctions.addItem(player, itemId, itemCount, "Reborn reward");
		}

		player.setVar(REBORN_LEVEL, ++rebornLevel);

		if(rebornLevel == Config.ALLOWED_REBORN_COUNT)
		{
			int[] lastRebornReward = Rnd.get(Config.LAST_REBORN_RANDOM_REWARD_ITEMS);
			if(lastRebornReward != null && lastRebornReward.length >= 1)
			{
				int itemId = lastRebornReward[0];
				int itemCount = lastRebornReward.length >= 2 ? lastRebornReward[1] : 1;
				ItemFunctions.addItem(player, itemId, itemCount, "Last reborn reward");
			}
		}
		player.sendMessage(new CustomMessage("scripts.services.RebornSystem.8", player));
		player.broadcastPacket(new SocialActionPacket(player.getObjectId(), SocialActionPacket.LEVEL_UP));
	}

	@Override
	public void onLoad()
	{
		if(Config.ALLOWED_REBORN_COUNT > 0)
			CharListenerList.addGlobal(_rebornListenes);
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
}