package ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.Location;

/**
 * AI монахов в Monastery of Silence<br>
 * - агрятся на чаров с оружием в руках
 * - перед тем как броситься в атаку кричат
 *
 * @author SYS
 * fix Kekess
 */
public class MoSMonk extends Fighter
{
	public MoSMonk(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onIntentionAttack(Creature target)
	{
		NpcInstance actor = getActor();
		if(getIntention() == CtrlIntention.AI_INTENTION_ACTIVE && Rnd.chance(20))
			Functions.npcSay(actor, NpcString.YOU_CANNOT_CARRY_A_WEAPON_WITHOUT_AUTHORIZATION);
		super.onIntentionAttack(target);
	}

	@Override
	public boolean checkAggression(Creature target)
	{
		if(target.getActiveWeaponInstance() == null)
			return false;
		return super.checkAggression(target);
	}

	// for quest _457_LostandFound 
	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();
		if(actor == null)
			return;
			
		if(Rnd.chance(1))
		{
			if(Rnd.chance(10))
				spawnGumiel(actor);
		}

		super.onEvtDead(killer);
	}

	protected void spawnGumiel(NpcInstance actor)
	{
		try
		{
			NpcInstance npc = NpcHolder.getInstance().getTemplate(32759).getNewInstance();
			Location pos = Location.findPointToStay(actor.getX(), actor.getY(), actor.getZ(), 100, 120, actor.getReflection().getGeoIndex());
			npc.setSpawnedLoc(pos);
			npc.setReflection(actor.getReflection());
			npc.spawnMe(npc.getSpawnedLoc());
			Functions.npcSayCustomMessage(getActor(), "scripts.ai.MoSMonk.spawnGumiel");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}