package ai;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.utils.Location;

public class MCManager extends DefaultAI
{
	public MCManager(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtSpawn()
	{
		NpcInstance actor = getActor();
		if(actor == null)
		{
			return;
		}

		ThreadPoolManager.getInstance().schedule(new ScheduleStart(1, actor), 30000);
		super.onEvtSpawn();
	}

	private class ScheduleStart implements Runnable
	{
		private int _taskId;
		private NpcInstance _actor;

		public ScheduleStart(int taskId, NpcInstance actor)
		{
			_taskId = taskId;
			_actor = actor;
		}

		@Override
		public void run()
		{
			switch(_taskId)
			{
				case 1:
					_actor.sendPacket(new ExShowScreenMessage(NpcString.UGH_I_HAVE_BUTTERFLIES_IN_MY_STOMACH, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false));
					ThreadPoolManager.getInstance().schedule(new ScheduleStart(2, _actor), 1000);
					break;
				case 2:
					_actor.sendPacket(new ExShowScreenMessage(NpcString.THANK_YOU_ALL_FOR_COMING_HERE_TONIGHT, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false));
					ThreadPoolManager.getInstance().schedule(new ScheduleStart(3, _actor), 6000);
					break;
				case 3:
					_actor.sendPacket(new ExShowScreenMessage(NpcString.IT_IS_AN_HONOR_TO_HAVE_THE_SPECIAL_SHOW_TODAY, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false));
					ThreadPoolManager.getInstance().schedule(new ScheduleStart(4, _actor), 4000);
					break;
				case 4:
					_actor.sendPacket(new ExShowScreenMessage(NpcString.FANTASY_ISLE_IS_FULLY_COMMITTED_TO_YOUR_HAPPINESS, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false));
					ThreadPoolManager.getInstance().schedule(new ScheduleStart(5, _actor), 5000);
					break;
				case 5:
					_actor.sendPacket(new ExShowScreenMessage(NpcString.NOW_ID_LIKE_TO_INTRODUCE_THE_MOST_BEAUTIFUL_SINGER_IN_ADEN, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false));
					ThreadPoolManager.getInstance().schedule(new ScheduleStart(6, _actor), 3000);
					addTaskMove(new Location(-56511, -56647, -2008), true, false);
					doTask();
					break;
				case 6:
					_actor.sendPacket(new ExShowScreenMessage(NpcString.HERE_SHE_COMES, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false));
					ThreadPoolManager.getInstance().schedule(new ScheduleStart(7, _actor), 220000);
					break;
				case 7:
					_actor.sendPacket(new ExShowScreenMessage(NpcString.THANK_YOU_VERY_MUCH_LEYLA, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false));
					ThreadPoolManager.getInstance().schedule(new ScheduleStart(8, _actor), 12000);
					addTaskMove(new Location(-56698, -56430, -2008), true, false);
					doTask();
					break;
				case 8:
					_actor.sendPacket(new ExShowScreenMessage(NpcString.JUST_BACK_FROM_THEIR_WORLD_TOURPUT_YOUR_HANDS_TOGETHER_FOR_THE_FANTASY_ISLE_CIRCUS, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false));
					ThreadPoolManager.getInstance().schedule(new ScheduleStart(9, _actor), 3000);
					addTaskMove(new Location(-56511, -56647, -2008), true, false);
					doTask();
					break;
				case 9:
					_actor.sendPacket(new ExShowScreenMessage(NpcString.COME_ON__EVERYONE, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false));
					ThreadPoolManager.getInstance().schedule(new ScheduleStart(10, _actor), 102000);
					break;
				case 10:
					_actor.sendPacket(new ExShowScreenMessage(NpcString.DID_YOU_LIKE_IT_THAT_WAS_SO_AMAZING, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false));
					ThreadPoolManager.getInstance().schedule(new ScheduleStart(11, _actor), 5000);
					addTaskMove(new Location(-56698, -56430, -2008), true, false);
					doTask();
					break;
				case 11:
					_actor.sendPacket(new ExShowScreenMessage(NpcString.NOW_WE_ALSO_INVITED_INDIVIDUALS_WITH_SPECIAL_TALENTS, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false));
					ThreadPoolManager.getInstance().schedule(new ScheduleStart(12, _actor), 3000);
					break;
				case 12:
					_actor.sendPacket(new ExShowScreenMessage(NpcString.LETS_WELCOME_THE_FIRST_PERSON_HERE, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false));
					ThreadPoolManager.getInstance().schedule(new ScheduleStart(13, _actor), 3000);
					break;
				case 13:
					_actor.sendPacket(new ExShowScreenMessage(NpcString.OH__, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false));
					ThreadPoolManager.getInstance().schedule(new ScheduleStart(14, _actor), 2000);
					break;
				case 14:
					_actor.sendPacket(new ExShowScreenMessage(NpcString.OKAY_NOW_HERE_COMES_THE_NEXT_PERSON, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false));
					ThreadPoolManager.getInstance().schedule(new ScheduleStart(15, _actor), 1000);
					break;
				case 15:
					_actor.sendPacket(new ExShowScreenMessage(NpcString.OH_IT_LOOKS_LIKE_SOMETHING_GREAT_IS_GOING_TO_HAPPEN_RIGHT, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false));
					ThreadPoolManager.getInstance().schedule(new ScheduleStart(16, _actor), 2000);
					break;
				case 16:
					_actor.sendPacket(new ExShowScreenMessage(NpcString.OH_MY_, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false));
					ThreadPoolManager.getInstance().schedule(new ScheduleStart(17, _actor), 2000);
					break;
				case 17:
					_actor.sendPacket(new ExShowScreenMessage(NpcString.THATS_G_, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false));
					ThreadPoolManager.getInstance().schedule(new ScheduleStart(18, _actor), 3000);
					break;
				case 18:
					_actor.sendPacket(new ExShowScreenMessage(NpcString.NOW_THIS_IS_THE_END_OF_TODAYS_SHOW, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false));
					ThreadPoolManager.getInstance().schedule(new ScheduleStart(19, _actor), 5000);
					break;
				case 19:
					_actor.sendPacket(new ExShowScreenMessage(NpcString.HOW_WAS_IT_I_HOPE_YOU_ALL_ENJOYED_IT, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false));
					ThreadPoolManager.getInstance().schedule(new ScheduleStart(20, _actor), 10000);
					addTaskMove(new Location(-56698, -56340, -2008), true, false);
					doTask();
					break;
				case 20:
					_actor.sendPacket(new ExShowScreenMessage(NpcString.PLEASE_REMEMBER_THAT_FANTASY_ISLE_IS_ALWAYS_PLANNING_A_LOT_OF_GREAT_SHOWS_FOR_YOU, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false));
					ThreadPoolManager.getInstance().schedule(new ScheduleStart(21, _actor), 10000);
					break;
				case 21:
					_actor.sendPacket(new ExShowScreenMessage(NpcString.WELL_I_WISH_I_COULD_CONTINUE_ALL_NIGHT_LONG_BUT_THIS_IS_IT_FOR_TODAY, 8000, ExShowScreenMessage.ScreenMessageAlign.MIDDLE_CENTER, false, 1, -1, false));
					break;
			}
		}
	}
}