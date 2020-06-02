package npc.model.birthday;

import java.util.concurrent.Future;

import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author VISTALL
 * @date 22:21/28.08.2011
 */
public class BirthDayCakeInstance extends NpcInstance
{
	private static final Skill SKILL = SkillHolder.getInstance().getSkill(22035, 1);

	private class CastTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			for(Player player : World.getAroundPlayers(BirthDayCakeInstance.this, 500, 100))
			{
				if(player.getEffectList().getEffectsBySkill(SKILL) != null)
					continue;

				SKILL.getEffects(BirthDayCakeInstance.this, player, false, false);
			}
		}
	}

	private Future<?> _castTask;

	public BirthDayCakeInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setTargetable(false);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();

		_castTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new CastTask(), 1000L, 1000L);
	}

	@Override
	public void onDespawn()
	{
		super.onDespawn();

		_castTask.cancel(false);
		_castTask = null;
	}
}
