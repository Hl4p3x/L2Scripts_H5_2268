package l2s.gameserver.model;

import java.util.List;

import l2s.commons.lang.reference.HardReference;
import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.items.attachment.FlagItemAttachment;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.s2c.ExVoteSystemInfoPacket;
import l2s.gameserver.network.l2.s2c.MagicSkillLaunchedPacket;
import l2s.gameserver.network.l2.s2c.SayPacket2;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class GameObjectTasks
{
	public static class DeleteTask extends RunnableImpl
	{
		private final HardReference<? extends Creature> _ref;

		public DeleteTask(Creature c)
		{
			_ref = c.getRef();
		}

		@Override
		public void runImpl()
		{
			Creature c = _ref.get();

			if(c != null)
				c.deleteMe();
		}
	}
	
	public static class ReturnTask extends RunnableImpl
	{
		private final HardReference<? extends Creature> _ref;
		private final List<Skill> _list;
		private final List<Integer> _timeLeft;
		
		public ReturnTask(Creature c, List<Skill> list, List<Integer> timeLeft)
		{
			_ref = c.getRef();
			_list = list;
			_timeLeft = timeLeft;
		}

		@Override
		public void runImpl()
		{
			Creature c = _ref.get();

			if(c != null && _list != null && !c.isDead() && !c.isInOlympiadMode())
			{
				int i = 0;
				for(Skill skill : _list)
				{
					if(skill != null)
						skill.getEffects(c, c, false, false, _timeLeft.get(i)*1000, 1.0, false);
					i++; //out side the loop to make sure the order remains
				}
			}
		}
	}	
	// ============================ Таски для L2Player ==============================
	public static class SoulConsumeTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;

		public SoulConsumeTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			player.setConsumedSouls(player.getConsumedSouls() + 1, null);
		}
	}

	/** PvPFlagTask */
	public static class PvPFlagTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;

		public PvPFlagTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;

			long diff = Math.abs(System.currentTimeMillis() - player.getlastPvpAttack());
			if(diff > Config.PVP_TIME)
				player.stopPvPFlag();
			else if(diff > Config.PVP_TIME - 20000)
				player.updatePvPFlag(2);
			else
				player.updatePvPFlag(1);
		}
	}

	/** HourlyTask */
	public static class HourlyTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;

		public HourlyTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			// Каждый час в игре оповещаем персонажу сколько часов он играет.
			int hoursInGame = player.getHoursInGame();
			player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_BEEN_PLAYING_FOR_AN_EXTENDED_PERIOD_OF_TIME_PLEASE_CONSIDER_TAKING_A_BREAK).addNumber(hoursInGame));
			player.sendPacket(new SystemMessage(SystemMessage.YOU_OBTAINED_S1_RECOMMENDS).addNumber(player.addRecomLeft()));
		}
	}

	/** RecomBonusTask */
	public static class RecomBonusTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;

		public RecomBonusTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			player.setRecomBonusTime(0);
			player.sendPacket(new ExVoteSystemInfoPacket(player));
		}
	}

	public static class ChargeCountDownTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;

		public ChargeCountDownTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			player.setIncreasedForce(0);	
		}
	}
	
	/** WaterTask */
	public static class WaterTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;

		public WaterTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			if(player.isDead() || !player.isInWater())
			{
				player.stopWaterTask();
				return;
			}

			double reduceHp = player.getMaxHp() < 100 ? 1 : player.getMaxHp() / 100;
			player.reduceCurrentHp(reduceHp, player, null, false, false, true, false, false, false, false);
			player.sendPacket(new SystemMessage(SystemMessage.YOU_RECEIVED_S1_DAMAGE_BECAUSE_YOU_WERE_UNABLE_TO_BREATHE).addNumber((long) reduceHp));
		}
	}

	/** KickTask */
	public static class KickTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;

		public KickTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			player.setOfflineMode(false);
			player.kick();
		}
	}

	/** UnJailTask */
	public static class UnJailTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;

		private boolean _msg;

		public UnJailTask(Player player, boolean msg)
		{
			_playerRef = player.getRef();
			_msg = msg;
		}

		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
			{
				return;
			}

			player.fromJail();
			
			if(_msg)
				player.sendPacket(new SayPacket2(0, ChatType.TELL, "Administration", "You are free. Try to respect server rules!"));
		}
	}

	/** EndSitDownTask */
	public static class EndSitDownTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;

		public EndSitDownTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			player.sittingTaskLaunched = false;
			player.getAI().clearNextAction();
		}
	}

	/** EndStandUpTask */
	public static class EndStandUpTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;

		public EndStandUpTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			player.sittingTaskLaunched = false;
			player.getAI().clearNextAction();
			player.setSitting(false);
			if(!player.getAI().setNextIntention())
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}

	// ============================ Таски для L2Character ==============================

	/** AltMagicUseTask */
	public static class AltMagicUseTask extends RunnableImpl
	{
		public final Skill _skill;
		private final HardReference<? extends Creature> _charRef, _targetRef;

		public AltMagicUseTask(Creature character, Creature target, Skill skill)
		{
			_charRef = character.getRef();
			_targetRef = target.getRef();
			_skill = skill;
		}

		@Override
		public void runImpl()
		{
			Creature cha, target;
			if((cha = _charRef.get()) == null || (target = _targetRef.get()) == null)
				return;
			cha.altOnMagicUseTimer(target, _skill);
		}
	}

	/** CastEndTimeTask */
	public static class CastEndTimeTask extends RunnableImpl
	{
		private final HardReference<? extends Creature> _charRef;
		private final List<Creature> _targets;

		public CastEndTimeTask(Creature character, List<Creature> targets)
		{
			_charRef = character.getRef();
			_targets = targets;
		}

		@Override
		public void runImpl()
		{
			Creature character = _charRef.get();
			if(character == null)
				return;
			character.onCastEndTime(_targets, true);
		}
	}

	/** HitTask */
	public static class HitTask extends RunnableImpl
	{
		boolean _crit, _miss, _shld, _soulshot, _unchargeSS, _notify;
		int _damage;
		int _sAtk;
		private final HardReference<? extends Creature> _charRef, _targetRef;

		public HitTask(Creature cha, Creature target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld, boolean unchargeSS, boolean notify, int sAtk)
		{
			_charRef = cha.getRef();
			_targetRef = target.getRef();
			_damage = damage;
			_crit = crit;
			_shld = shld;
			_miss = miss;
			_soulshot = soulshot;
			_unchargeSS = unchargeSS;
			_notify = notify;
			_sAtk = sAtk;
		}

		@Override
		public void runImpl()
		{
			Creature character, target;
			if((character = _charRef.get()) == null || (target = _targetRef.get()) == null)
				return;

			if(character.isAttackAborted())
				return;

			character.onHitTimer(target, _damage, _crit, _miss, _soulshot, _shld, _unchargeSS);

			if(_notify)
				ThreadPoolManager.getInstance().schedule(new NotifyAITask(character, CtrlEvent.EVT_READY_TO_ACT), _sAtk / 2);
		}
	}

	/** Task launching the function onMagicUseTimer() */
	public static class MagicUseTask extends RunnableImpl
	{
		public boolean _forceUse;
		private final HardReference<? extends Creature> _charRef;

		public MagicUseTask(Creature cha, boolean forceUse)
		{
			_charRef = cha.getRef();
			_forceUse = forceUse;
		}

		@Override
		public void runImpl()
		{
			Creature character = _charRef.get();
			if(character == null)
				return;
			Skill castingSkill = character.getCastingSkill();
			Creature castingTarget = character.getCastingTarget();
			if(castingSkill == null || castingTarget == null)
			{
				character.clearCastVars();
				return;
			}
			character.onMagicUseTimer(castingTarget, castingSkill, _forceUse);
		}
	}

	public static class SeventeethPecentTimer extends RunnableImpl
	{
		private final HardReference<? extends Creature> _charRef;
		
		public SeventeethPecentTimer(Creature cha)
		{
			_charRef = cha.getRef();
		}

		@Override
		public void runImpl()
		{
			Creature character = _charRef.get();
			if(character == null)
				return;
			Skill castingSkill = character.getCastingSkill();
			Creature castingTarget = character.getCastingTarget();
			if(castingSkill == null || castingTarget == null)
			{
				character.clearCastVars();
				return;
			}
			if(!GeoEngine.canSeeTarget(character, castingTarget))
				character.NotPass70PercentCheck(true);
		}
	}
	
	/** MagicLaunchedTask */
	public static class MagicLaunchedTask extends RunnableImpl
	{
		public boolean _forceUse;
		private final HardReference<? extends Creature> _charRef;

		public MagicLaunchedTask(Creature cha, boolean forceUse)
		{
			_charRef = cha.getRef();
			_forceUse = forceUse;
		}

		@Override
		public void runImpl()
		{
			Creature character = _charRef.get();
			if(character == null)
				return;
			Skill castingSkill = character.getCastingSkill();
			Creature castingTarget = character.getCastingTarget();
			if(castingSkill == null || castingTarget == null)
			{
				character.clearCastVars();
				return;
			}
			List<Creature> targets = castingSkill.getTargets(character, castingTarget, _forceUse);
			character.broadcastPacket(new MagicSkillLaunchedPacket(character.getObjectId(), castingSkill.getDisplayId(), castingSkill.getDisplayLevel(), targets));
		}
	}

	/** Task of AI notification */
	public static class NotifyAITask extends RunnableImpl
	{
		private final CtrlEvent _evt;
		private final Object _agr0;
		private final Object _agr1;
		private final HardReference<? extends Creature> _charRef;

		public NotifyAITask(Creature cha, CtrlEvent evt, Object agr0, Object agr1)
		{
			_charRef = cha.getRef();
			_evt = evt;
			_agr0 = agr0;
			_agr1 = agr1;
		}

		public NotifyAITask(Creature cha, CtrlEvent evt)
		{
			this(cha, evt, null, null);
		}

		@Override
		public void runImpl()
		{
			Creature character = _charRef.get();
			if(character == null || !character.hasAI())
				return;

			character.getAI().notifyEvent(_evt, _agr0, _agr1);
		}
	}
	/** FlagKickTask */
	public static class FlagKickTask extends RunnableImpl
	{
		private final HardReference<Player> _playerRef;

		public FlagKickTask(Player player)
		{
			_playerRef = player.getRef();
		}

		@Override
		public void runImpl()
		{
			Player player = _playerRef.get();
			if(player == null)
				return;
			FlagItemAttachment attachment = player.getActiveWeaponFlagAttachment();
			if(attachment != null)
				attachment.onLogout(player); // возвращяем на место
		}
	}	
}