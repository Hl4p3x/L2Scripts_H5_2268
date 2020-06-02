package ai.SkyshadowMeadow;

import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ChangeWaitTypePacket;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.Location;

/**
 * @author PaInKiLlEr and Felixx
 *         - AI для мобов Sel Mahum Squad Leader (22786, 22787, 22788).
 *         - Когда на костре появляется Катёл (18933) то нпцы сбигаются, садятся и ставят символ еды над головой.
 *         - Когда костер загорается (18927), есть шанс 30% что они захотят спать и сбегуться к нему и появится символ сна над головой.
 *         - Перед тем как броситься питаться, кричат в чат.
 *         - Когда имеют над головой символы, статы значительно уменьшаются.
 *         - AI проверен и работает.
 */
public class SelMahumSquadLeader extends Fighter
{
	private boolean _firstTime1 = true;
	private boolean _firstTime2 = true;
	private boolean _firstTime3 = true;
	private boolean _firstTime4 = true;
	private boolean _firstTime5 = true;
	private boolean statsIsChanged = false;
	public static final NpcString[] _text = {NpcString.SCHOOL5, NpcString.SCHOOL6};

	public SelMahumSquadLeader(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return true;

		if(_def_think)
		{
			doTask();
			return true;
		}

		if( !statsIsChanged)
		{
			switch(actor.getNpcState())
			{
				case 1:
				{
					actor.doCast(SkillHolder.getInstance().getSkill(6332, 1), actor, true);
					statsIsChanged = true;
					break;
				}
				case 2:
				{
					actor.doCast(SkillHolder.getInstance().getSkill(6331, 1), actor, true);
					statsIsChanged = true;
					break;
				}
			}
		}

		if( !_firstTime2)
		{
			actor.broadcastPacket(new SocialActionPacket(getActor().getObjectId(), 2));
			actor.broadcastPacket(new ChangeWaitTypePacket(getActor(), 0));
			actor.setNpcState((byte) 1);
			_firstTime2 = true;
		}

		if( !_firstTime4)
		{
			actor.broadcastPacket(new SocialActionPacket(getActor().getObjectId(), 2));
			actor.broadcastPacket(new ChangeWaitTypePacket(getActor(), 0));
			actor.setNpcState((byte) 2);
			_firstTime4 = true;
		}

		for(NpcInstance npc : getActor().getAroundNpc(600, 600))
		{
			Location loc = Location.findPointToStay(npc, 100, 200);
			if(npc != null && npc.getNpcId() == 18933)
			{
				if(_firstTime1)
				{
					_firstTime1 = false;
					actor.setRunning();
					addTaskMove(loc, true, false);
					if(_firstTime5)
					{
						_firstTime5 = false;
						Functions.npcSay(actor, _text[Rnd.get(_text.length)]);
					}
					if(_firstTime2)
					{
						_firstTime2 = false;
						ThreadPoolManager.getInstance().schedule(new Go(), Rnd.get(20, 30) * 1000);
					}
				}
			}
		}

		for(NpcInstance npc : getActor().getAroundNpc(600, 600))
		{
			Location loc = Location.findPointToStay(npc, 100, 200);
			if(npc != null && npc.getNpcId() == 18927 && npc.getNpcState() == 1)
			{
				if(Rnd.chance(30))
				{
					if(_firstTime3)
					{
						_firstTime3 = false;
						actor.setRunning();
						addTaskMove(loc, true, false);
						if(_firstTime4)
						{
							_firstTime4 = false;
							ThreadPoolManager.getInstance().schedule(new Go(), Rnd.get(20, 30) * 1000);
						}
					}
				}
				else if(Rnd.chance(20))
				{
					actor.setNpcState((byte) 2);
					ThreadPoolManager.getInstance().schedule(new Stop(), Rnd.get(20, 30) * 1000);
				}
			}
		}

		return true;
	}

	private class Go extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			NpcInstance actor = getActor();
			Location loc = Location.findPointToStay(actor, 100, 200);

			actor.setNpcState((byte) 3);
			actor.setRunning();
			addTaskMove(loc, true, false);
			_firstTime1 = true;
			_firstTime3 = true;
		}
	}

	private class Stop extends RunnableImpl
	{
		@Override
		public void runImpl()
		{
			NpcInstance actor = getActor();
			actor.setNpcState((byte) 3);
		}
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		_firstTime1 = true;
		_firstTime2 = true;
		_firstTime3 = true;
		_firstTime4 = true;
		_firstTime5 = true;
		super.onEvtDead(killer);
	}
}