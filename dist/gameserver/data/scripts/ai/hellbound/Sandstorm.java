package ai.hellbound;

import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.Location;

/**
 * AI NPC Sandstorm (ID: 32350)
 * - Используют randomWalk
 * - Все время бегают
 * - Если находят чара в радиусе 200, то на него сперва используют стан - 5435, затем "пинок" - 5494
 * - Цепляют даже тех, кто находится в режиме SilentMove
 * - Никогда и никого не атакуют
 * @author SYS & Diamond
 */
public class Sandstorm extends DefaultAI
{
	private static final int AGGRO_RANGE = 200;
	private static final Skill SKILL1 = SkillHolder.getInstance().getSkill(5435, 1);
	private static final Skill SKILL2 = SkillHolder.getInstance().getSkill(5494, 1);
	private long lastThrow = 0;

	public Sandstorm(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(lastThrow + 5000 < System.currentTimeMillis())
			for(Playable target : World.getAroundPlayables(actor, AGGRO_RANGE, AGGRO_RANGE))
				if(target != null && !target.isAlikeDead() && !target.isInvul() && target.isVisible() && GeoEngine.canSeeTarget(actor, target))
				{
					actor.doCast(SKILL1, target, true);
					actor.doCast(SKILL2, target, true);
					lastThrow = System.currentTimeMillis();
					break;
				}

		return super.thinkActive();
	}

	@Override
	protected void thinkAttack()
	{}

	@Override
	protected void onIntentionAttack(Creature target)
	{}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{}

	@Override
	protected void onEvtAggression(Creature attacker, int aggro)
	{}

	@Override
	protected void onEvtClanAttacked(Creature attacked_member, Creature attacker, int damage)
	{}

	@Override
	protected boolean randomWalk()
	{
		NpcInstance actor = getActor();
		Location sloc = actor.getSpawnedLoc();

		Location pos = Location.findPointToStay(actor, sloc, 150, 300); 
		if(GeoEngine.canMoveToCoord(actor.getX(), actor.getY(), actor.getZ(), pos.x, pos.y, pos.z, actor.getGeoIndex()))
		{
			actor.setRunning();
			addTaskMove(pos, false, false);
		}

		return true;
	}
}