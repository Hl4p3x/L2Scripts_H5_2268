package ai.seedofdestruction;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.instancemanager.SoDManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;
import l2s.gameserver.network.l2.s2c.ExStartScenePlayer;
import l2s.gameserver.utils.Location;

/**
  * @author pchayka
  */
public class Tiat extends Fighter
{
	private static final int TIAT_TRANSFORMATION_SKILL_ID = 5974;
	private static final Skill TIAT_TRANSFORMATION_SKILL = SkillHolder.getInstance().getSkill(TIAT_TRANSFORMATION_SKILL_ID, 1);
	private boolean _notUsedTransform = true;
	private static final int TRAPS_COUNT = 4;
	private static final Location[] TRAP_LOCS = {
			new Location(-252022, 210130, -11995, 16384),
			new Location(-248782, 210130, -11995, 16384),
			new Location(-248782, 206875, -11995, 16384),
			new Location(-252022, 206875, -11995, 16384) };
	private static final long COLLAPSE_BY_INACTIVITY_INTERVAL = 10 * 60 * 1000; // 10 мин
	private long _lastAttackTime = 0;
	private static final int TRAP_NPC_ID = 18696;
	private static final int[] TIAT_MINION_IDS = { 29162, 22538, 22540, 22547, 22542, 22548 };
	private static final String[] TIAT_TEXT = { "You'll regret challenging me!", "You shall die in pain!", "I will wipe out your entire kind!" };
	private long _lastFactionNotifyTime = 0;
	private boolean _immobilized;
	private boolean _failed = false;

	public Tiat(NpcInstance actor)
	{
		super(actor);
		_immobilized = true;
		actor.startImmobilized();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, int damage)
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return;

		_lastAttackTime = System.currentTimeMillis();

		if(_notUsedTransform && actor.getCurrentHpPercents() < 50)
		{
			if(_immobilized)
			{
				_immobilized = false;
				actor.stopImmobilized();
			}
			_notUsedTransform = false;
			clearTasks();
			spawnTraps();
			actor.abortAttack(true, false);
			actor.abortCast(true, false);
			// Transform skill cast [custom: making Tiat invul while casting]
			actor.setIsInvul(true);
			actor.doCast(TIAT_TRANSFORMATION_SKILL, actor, true);
			ThreadPoolManager.getInstance().schedule(new RunnableImpl()
			{
				@Override
				public void runImpl() throws Exception
				{
					getActor().setCurrentHpMp(getActor().getMaxHp(), getActor().getMaxMp());
					getActor().setIsInvul(false);
				}
			}, TIAT_TRANSFORMATION_SKILL.getHitTime());
		}
		if(System.currentTimeMillis() - _lastFactionNotifyTime > _minFactionNotifyInterval)
		{
			_lastFactionNotifyTime = System.currentTimeMillis();
			for(NpcInstance npc : World.getAroundNpc(actor))
				if(ArrayUtils.contains(TIAT_MINION_IDS, npc.getNpcId()))
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 30000);
			if(Rnd.chance(15) && !_notUsedTransform)
				actor.broadcastPacket(new ExShowScreenMessage(NpcString.NONE, 4000, ScreenMessageAlign.MIDDLE_CENTER, false, TIAT_TEXT[Rnd.get(TIAT_TEXT.length)]));
		}
		super.onEvtAttacked(attacker, damage);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return true;

		// Коллапсируем инстанс, если Тиата не били более 10 мин
		if(!_failed && _lastAttackTime != 0 && _lastAttackTime + COLLAPSE_BY_INACTIVITY_INTERVAL < System.currentTimeMillis())
		{
			final Reflection r = actor.getReflection();
			_failed = true;

			// Показываем финальный ролик при фейле серез секунду после очистки инстанса
			ThreadPoolManager.getInstance().schedule(new RunnableImpl()
			{
				@Override
				public void runImpl() throws Exception
				{
					for(Player pl : r.getPlayers())
						pl.showQuestMovie(ExStartScenePlayer.SCENE_TIAT_FAIL);
					r.clearReflection(5, true);
				}
			}, 1000);
			return true;
		}
		return super.thinkActive();
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		_notUsedTransform = true;
		_lastAttackTime = 0;
		_lastFactionNotifyTime = 0;

		NpcInstance actor = getActor();
		SoDManager.addTiatKill();
		final Reflection r = actor.getReflection();
		r.setReenterTime(System.currentTimeMillis());
		for(NpcInstance n : r.getNpcs())
			n.deleteMe();
		// Показываем финальный ролик серез секунду после очистки инстанса
		ThreadPoolManager.getInstance().schedule(new RunnableImpl()
		{
			@Override
			public void runImpl() throws Exception
			{
				for(Player pl : r.getPlayers())
					if(pl != null)
						pl.showQuestMovie(ExStartScenePlayer.SCENE_TIAT_SUCCESS);
			}
		}, 1000);
	}

	private void spawnTraps()
	{
		NpcInstance actor = getActor();
		actor.broadcastPacket(new ExShowScreenMessage(NpcString.NONE, 5000, ScreenMessageAlign.MIDDLE_CENTER, false, "Come out, warriors. Protect Seed of Destruction."));
		for(int i = 0; i < TRAPS_COUNT; i++)
			actor.getReflection().addSpawnWithRespawn(TRAP_NPC_ID, TRAP_LOCS[i], 0, 180);
	}
}