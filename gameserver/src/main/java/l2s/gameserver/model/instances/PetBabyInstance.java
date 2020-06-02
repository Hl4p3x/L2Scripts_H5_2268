package l2s.gameserver.model.instances;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.string.StringArrayUtils;
import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Effect;
import l2s.gameserver.model.EffectList;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.tables.PetDataTable;
import l2s.gameserver.templates.npc.NpcTemplate;

public final class PetBabyInstance extends PetInstance
{
	private static final long serialVersionUID = 1L;

	private static final Logger _log = LoggerFactory.getLogger(PetBabyInstance.class);

	private Future<?> _actionTask;
	private boolean _buffEnabled = true;

	private final TIntObjectMap<List<Skill>> _activeSkills = new TIntObjectHashMap<List<Skill>>();
	private final TIntObjectMap<List<Skill>> _buffSkills = new TIntObjectHashMap<List<Skill>>();
	//private final TIntObjectMap<List<Skill>> _mergedBuffSkills = new TIntObjectHashMap<List<Skill>>();

	public PetBabyInstance(int objectId, NpcTemplate template, Player owner, ItemInstance control, int _currentLevel, long exp)
	{
		super(objectId, template, owner, control, _currentLevel, exp);
		parseSkills();
	}

	public PetBabyInstance(int objectId, NpcTemplate template, Player owner, ItemInstance control)
	{
		super(objectId, template, owner, control);
		parseSkills();
	}

	private void parseSkills()
	{
		for(int step = 0; step < 10; step++)
		{
			List<Skill> skills = _activeSkills.get(step);
			for(int buff = 1; buff <= 30; buff++)
			{
				String data = getTemplate().getAIParams().getString("step" + step + "_skill" + (buff > 9 ? "" : "0") + buff, null);
				if(data == null)
					continue;

				if(skills == null)
				{
					skills = new ArrayList<Skill>();
					_activeSkills.put(step, skills);
				}

				int[][] skillsData = StringArrayUtils.stringToIntArray2X(data, ";", "-");
				for(int[] skillData : skillsData)
				{
					int skillLevel = skillData.length > 1 ? skillData[1] : 1;
					skills.add(SkillHolder.getInstance().getSkill(skillData[0], skillLevel));
				}
			}

			skills = _buffSkills.get(step);
			for(int buff = 1; buff < 30; buff++)
			{
				String data = getTemplate().getAIParams().getString("step" + step + "_buff" + (buff > 9 ? "" : "0") + buff, null);
				if(data == null)
					continue;

				if(skills == null)
				{
					skills = new ArrayList<Skill>();
					_buffSkills.put(step, skills);
				}

				int[][] skillsData = StringArrayUtils.stringToIntArray2X(data, ";", "-");
				for(int[] skillData : skillsData)
				{
					int skillLevel = skillData.length > 1 ? skillData[1] : 1;
					skills.add(SkillHolder.getInstance().getSkill(skillData[0], skillLevel));
				}
			}

			/*skills = _mergedBuffSkills.get(step);
			for(int buff = 1; buff < 30; buff++)
			{
				String data = getTemplate().getAIParams().getString("step" + step + "_merged_buff" + (buff > 9 ? "" : "0") + buff, null);
				if(data == null)
					continue;

				if(skills == null)
				{
					skills = new ArrayList<Skill>();
					_mergedBuffSkills.put(step, skills);
				}

				int[][] skillsData = StringArrayUtils.stringToIntArray2X(data, ";", "-");
				for(int[] skillData : skillsData)
				{
					int skillLevel = skillData.length > 1 ? skillData[1] : 1;
					skills.add(SkillHolder.getInstance().getSkill(skillData[0], skillLevel));
				}
			}*/
		}
	}

	// heal
	private static final int HealTrick = 4717;
	private static final int GreaterHealTrick = 4718;
	private static final int GreaterHeal = 5195;
	private static final int BattleHeal = 5590;
	private static final int Recharge = 5200;

	class ActionTask extends RunnableImpl
	{
		@Override
		public void runImpl() throws Exception
		{
			Skill skill = onActionTask();
			_actionTask = ThreadPoolManager.getInstance().schedule(new ActionTask(), skill == null ? 1000 : skill.getHitTime() * 333 / Math.max(getMAtkSpd(), 1) - 100);
		}
	}

	@Override
	public List<Skill> getActiveSkills()
	{
		for(int step = getSteep(); step >= 0; step--)
		{
			List<Skill> skills = _activeSkills.get(step);
			if(skills != null)
				return skills;
		}
		return Collections.emptyList();
	}

	@Override
	public int getActiveSkillLevel(int skillId)
	{
		for(Skill skill : getActiveSkills())
		{
			if(skill.getId() == skillId)
				return skill.getLevel();
		}
		return super.getActiveSkillLevel(skillId);
	}

	public List<Skill> getBuffs()
	{
		for(int step = getSteep(); step >= 0; step--)
		{
			List<Skill> skills = _buffSkills.get(step);
			if(skills != null)
				return skills;
		}
		return Collections.emptyList();
	}

	private Skill getHealSkill(int hpPercent)
	{
		if(PetDataTable.isImprovedBabyPet(getNpcId()))
		{
			if(hpPercent < 90)
			{
				if(hpPercent < 33)
					return SkillHolder.getInstance().getSkill(BattleHeal, Math.min(getSteep(), SkillHolder.getInstance().getMaxLevel(BattleHeal)));
				else if(getNpcId() != PetDataTable.IMPROVED_BABY_KOOKABURRA_ID)
					return SkillHolder.getInstance().getSkill(GreaterHeal, Math.min(getSteep(), SkillHolder.getInstance().getMaxLevel(GreaterHeal)));
			}
		}
		else if(PetDataTable.isBabyPet(getNpcId()))
		{
			if(hpPercent < 90)
			{
				if(hpPercent < 33)
					return SkillHolder.getInstance().getSkill(GreaterHealTrick, Math.min(getSteep(), SkillHolder.getInstance().getMaxLevel(GreaterHealTrick)));
				else
					return SkillHolder.getInstance().getSkill(HealTrick, Math.min(getSteep(), SkillHolder.getInstance().getMaxLevel(HealTrick)));
			}
		}
		else
		{
			switch(getNpcId())
			{
				case PetDataTable.WHITE_WEASEL_ID:
				case PetDataTable.TOY_KNIGHT_ID:
					if(hpPercent < 70)
					{
						if(hpPercent < 30)
							return SkillHolder.getInstance().getSkill(BattleHeal, Math.min(getSteep(), SkillHolder.getInstance().getMaxLevel(BattleHeal)));
						else
							return SkillHolder.getInstance().getSkill(GreaterHeal, Math.min(getSteep(), SkillHolder.getInstance().getMaxLevel(GreaterHeal)));
					}
					break;
				case PetDataTable.FAIRY_PRINCESS_ID:
				case PetDataTable.SPIRIT_SHAMAN_ID:
					if(hpPercent < 30)
						return SkillHolder.getInstance().getSkill(BattleHeal, Math.min(getSteep(), SkillHolder.getInstance().getMaxLevel(BattleHeal)));
					break;
			}
		}
		return null;
	}

	private Skill getManaHealSkill(int mpPercent)
	{
		switch(getNpcId())
		{
			case PetDataTable.IMPROVED_BABY_KOOKABURRA_ID:
				if(mpPercent < 66)
				{
					Skill skill = SkillHolder.getInstance().getSkill(Recharge, 1);
					return SkillHolder.getInstance().getSkill(Recharge, Math.min(getSteep(), SkillHolder.getInstance().getMaxLevel(Recharge)));
				}
				break;
			case PetDataTable.FAIRY_PRINCESS_ID:
			case PetDataTable.SPIRIT_SHAMAN_ID:
				if(mpPercent < 50)
				{
					Skill skill = SkillHolder.getInstance().getSkill(Recharge, 1);
					return SkillHolder.getInstance().getSkill(Recharge, Math.min(getSteep(), SkillHolder.getInstance().getMaxLevel(Recharge)));
				}
				break;
		}
		return null;
	}

	public Skill onActionTask()
	{
		try
		{
			Player owner = getPlayer();
			if(!owner.isDead() && !owner.isInvul() && !isCastingNow())
			{
				if(getEffectList().getEffectsCountForSkill(5753) > 0) // Awakening
					return null;

				if(getEffectList().getEffectsCountForSkill(5771) > 0) // Buff Control
					return null;

				Skill skill = null;

				if(!Config.ALT_PET_HEAL_BATTLE_ONLY || owner.isInCombat())
				{
					// проверка лечения
					double curHp = owner.getCurrentHpPercents();
					if(Rnd.chance((100 - curHp) / 3))
						skill = getHealSkill((int) curHp);

					// проверка речарджа
					if(skill == null)
					{
						double curMp = owner.getCurrentMpPercents();
						if(Rnd.chance((100 - curMp) / 2))
							skill = getManaHealSkill((int) curMp);
					}

					if(skill != null && skill.checkCondition(PetBabyInstance.this, owner, false, !isFollowMode(), true))
					{
						setTarget(owner);
						getAI().Cast(skill, owner, false, !isFollowMode());
						return skill;
					}
				}

				if(owner.isInOfflineMode() || owner.getEffectList().getEffectsCountForSkill(5771) > 0)
					return null;

				outer: for(Skill buff : getBuffs())
				{
					if(getCurrentMp() < buff.getMpConsume2())
						continue;

					for(Effect ef : owner.getEffectList().getAllEffects())
					{
						if(checkEffect(ef, buff))
							continue outer;
					}

					if(buff.checkCondition(PetBabyInstance.this, owner, false, !isFollowMode(), true))
					{
						setTarget(owner);
						getAI().Cast(buff, owner, false, !isFollowMode());
						return buff;
					}
				}
			}
		}
		catch(Throwable e)
		{
			_log.warn("Pet [#" + getNpcId() + "] a buff task error has occurred: " + e);
			_log.error("", e);
		}
		return null;
	}

	/**
	 * Возвращает true если эффект для скилла уже есть и заново накладывать не надо
	 */
	private boolean checkEffect(Effect ef, Skill skill)
	{
		if(ef == null || !ef.isInUse())
			return false;
		if(ef.getSkill().getId() == skill.getId())
		{
			if(ef.getSkill().getLevel() < skill.getLevel())
				return false;
		}
		else
		{
			if(!EffectList.checkStackType(ef.getTemplate(), skill.getEffectTemplates()[0])) // такого скилла нет
				return false;
			if(ef.getStackOrder() < skill.getEffectTemplates()[0]._stackOrder) // старый слабее
				return false;
		}
		if(ef.getTimeLeft() > 10) // старый не слабее и еще не кончается - ждем
			return true;
		if(ef.getNext() != null) // старый не слабее но уже кончается - проверить рекурсией что там зашедулено
			return checkEffect(ef.getNext(), skill);
		return false;
	}

	public synchronized void stopBuffTask()
	{
		if(_actionTask != null)
		{
			_actionTask.cancel(false);
			_actionTask = null;
		}
	}

	public synchronized void startBuffTask()
	{
		if(_actionTask != null)
			stopBuffTask();

		if(_actionTask == null && !isDead())
			_actionTask = ThreadPoolManager.getInstance().schedule(new ActionTask(), 5000);
	}

	public boolean isBuffEnabled()
	{
		return _buffEnabled;
	}

	public void triggerBuff()
	{
		_buffEnabled = !_buffEnabled;
	}

	@Override
	protected void onDeath(Creature killer)
	{
		stopBuffTask();
		super.onDeath(killer);
	}

	@Override
	public void doRevive()
	{
		super.doRevive();
		startBuffTask();
	}

	@Override
	public void unSummon()
	{
		stopBuffTask();
		super.unSummon();
	}

	public int getSteep()
	{
		if(PetDataTable.isSpecialPet(getNpcId()))
		{
			if(getLevel() < 10)
				return 0;
			if(getLevel() < 20)
				return 1;
			if(getLevel() < 30)
				return 2;
			if(getLevel() < 40)
				return 3;
			if(getLevel() < 50)
				return 4;
			if(getLevel() < 60)
				return 5;
			if(getLevel() < 70)
				return 6;
			if(getLevel() >= 70)
				return 7;
		}
		else
		{
			if(getLevel() < 60)
				return 0;
			if(getLevel() < 65)
				return 1;
			if(getLevel() < 70)
				return 2;
			if(getLevel() < 75)
				return 3;
			if(getLevel() < 80)
				return 4;
			if(getLevel() >= 80)
				return 5;
		}
		return 0;
	}

	@Override
	public int getSoulshotConsumeCount()
	{
		return 1;
	}

	@Override
	public int getSpiritshotConsumeCount()
	{
		return 1;
	}

	@Override
	public boolean canConsumeSkillItem(Skill skill)
	{
		return !getBuffs().contains(skill);
	}
}