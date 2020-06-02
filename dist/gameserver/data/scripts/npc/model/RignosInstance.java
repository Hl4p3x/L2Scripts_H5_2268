package npc.model;

import java.util.concurrent.Future;

import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author VISTALL
 * @date 17:41/30.08.2011
 */
public class RignosInstance extends NpcInstance
{
	private class EndRaceTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			_raceTask = null;
		}
	}

	private static final Skill SKILL_EVENT_TIMER = SkillHolder.getInstance().getSkill(5239, 5);
	private static final int RACE_STAMP = 10013;
	private static final int SECRET_KEY = 9694;

	private Future<?> _raceTask;

	public RignosInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(!canBypassCheck(player, this))
			return;

		if(command.equalsIgnoreCase("startRace"))
		{
			if(_raceTask != null)
				return;

			altUseSkill(SKILL_EVENT_TIMER, player);
			ItemFunctions.deleteItem(player, RACE_STAMP, ItemFunctions.getItemCount(player, RACE_STAMP), true);
			_raceTask = ThreadPoolManager.getInstance().schedule(new EndRaceTask(), 30 * 60 * 1000L);
		}
		else if(command.equalsIgnoreCase("endRace"))
		{
			if(_raceTask == null)
				return;

			long count = ItemFunctions.getItemCount(player, RACE_STAMP);
			if(count >= 4)
			{
				ItemFunctions.deleteItem(player, RACE_STAMP, count, true);
				ItemFunctions.addItem(player, SECRET_KEY, 1, true, "Give items on end race by RignosInstance");
				player.getEffectList().stopEffect(SKILL_EVENT_TIMER);
				_raceTask.cancel(false);
				_raceTask = null;
			}
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(Player player, int val, Object... arg)
	{
		if(ItemFunctions.getItemCount(player, RACE_STAMP) >= 4)
			showChatWindow(player, "race_start001a.htm");
		else if(player.getLevel() >= 78 && _raceTask == null)
			showChatWindow(player, "race_start001.htm");
		else
			showChatWindow(player, "race_start002.htm");
	}
}
