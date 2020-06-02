package ai.SkyshadowMeadow;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.scripts.Functions;

/**
 * @author PaInKiLlEr
 *         - AI для моба Sel Mahum Recruit (22780) и Sel Mahum Recruit (22782) и Sel Mahum Soldier (22783) и Sel Mahum Recruit (22784) и Sel Mahum Soldier (22785).
 *         - При атаке ругается в чат с шансом 20%, агрит главного моба.
 *         - AI проверен и работает.
 */
public class SelMahumRecruit extends Fighter
{
	private long _wait_timeout = System.currentTimeMillis() + 180000;
	private List<NpcInstance> _arm = new ArrayList<NpcInstance>();
	private boolean _firstTimeAttacked = true;
	public static final NpcString[] _text = {NpcString.SCHOOL1, NpcString.SCHOOL2};

	public SelMahumRecruit(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return true;

		if(_wait_timeout < System.currentTimeMillis())
		{
			_wait_timeout = (System.currentTimeMillis() + Rnd.get(150, 200) * 1000);
			actor.broadcastPacket(new SocialActionPacket(actor.getObjectId(), 1));
		}

		if(_arm == null || _arm.isEmpty())
		{
			for(NpcInstance npc : getActor().getAroundNpc(750, 750))
			{
				if(npc != null && (npc.getNpcId() == 22775 || npc.getNpcId() == 22776 || npc.getNpcId() == 22778 || npc.getNpcId() == 22780 || npc.getNpcId() == 22782 || npc.getNpcId() == 22783 || npc.getNpcId() == 22784 || npc.getNpcId() == 22785))
					_arm.add(npc);
			}
		}
		return true;
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return;

		for(NpcInstance npc : _arm)
		{
			npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Rnd.get(1, 100));

			if(npc.isDead())
			{
				if(Rnd.chance(20))
				{
					if(_firstTimeAttacked)
					{
						_firstTimeAttacked = false;
						Functions.npcSay(actor, _text[Rnd.get(_text.length)]);
					}
				}
				actor.moveToLocation(actor.getSpawnedLoc(), 0, true);
			}
		}

		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		_firstTimeAttacked = true;
		super.onEvtDead(killer);
	}
}