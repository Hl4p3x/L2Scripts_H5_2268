package ai;
 
import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.scripts.Functions;

 
public class BladeOfSplendor extends RndTeleportFighter
{
	private static final int[] CLONES = { 21525 };

	private boolean _firstTimeAttacked = true;

	public BladeOfSplendor(NpcInstance actor)
	{
		super(actor);
		this.AI_TASK_ATTACK_DELAY = 1000;
		this.AI_TASK_ACTIVE_DELAY = 100000;
	}

	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if (actor == null)
			return;
		if ((!actor.isDead()) && (this._firstTimeAttacked))
		{
			this._firstTimeAttacked = false;
			Functions.npcSay(actor, "Now I Know Why You Wanna Hate Me");
			for (int bro : CLONES)
				try
			{
					MonsterInstance npc = (MonsterInstance) NpcHolder.getInstance().getTemplate(bro).getNewInstance();
					npc.setSpawnedLoc(((MonsterInstance)actor).getMinionPosition());
					npc.setReflection(actor.getReflection());
					npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp(), true);
					npc.spawnMe(npc.getSpawnedLoc());
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(Rnd.get(1, 1000)));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		super.onEvtAttacked(attacker, damage);
	}

	protected void onEvtDead(Player killer)
	{
		this._firstTimeAttacked = true;
		super.onEvtDead(killer);
	}
}