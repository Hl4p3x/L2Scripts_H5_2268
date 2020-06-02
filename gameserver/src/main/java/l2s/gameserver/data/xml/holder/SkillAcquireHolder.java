package l2s.gameserver.data.xml.holder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.actor.instances.player.SubClass;
import l2s.gameserver.model.base.AcquireType;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.SubUnit;
import l2s.gameserver.utils.MulticlassUtils;


/**
 * @author: VISTALL
 * @date:  20:55/30.11.2010
 */
public final class SkillAcquireHolder extends AbstractHolder
{
	private static final SkillAcquireHolder _instance = new SkillAcquireHolder();

	public static SkillAcquireHolder getInstance()
	{
		return _instance;
	}

	// классовые зависимости
	private TIntObjectMap<Set<SkillLearn>> _normalSkillTree = new TIntObjectHashMap<Set<SkillLearn>>();
	private TIntObjectMap<Set<SkillLearn>> _generalSkillTree = new TIntObjectHashMap<Set<SkillLearn>>();
	private TIntObjectMap<Set<SkillLearn>> _transferSkillTree = new TIntObjectHashMap<Set<SkillLearn>>();
	// мультикласс
	private TIntObjectMap<Set<SkillLearn>> _multiclassCheckSkillTree = new TIntObjectHashMap<Set<SkillLearn>>();
	private TIntObjectMap<TIntObjectMap<Set<SkillLearn>>> _multiclassLearnSkillTree = new TIntObjectHashMap<TIntObjectMap<Set<SkillLearn>>>();
	// расовые зависимости
	private TIntObjectMap<Set<SkillLearn>> _fishingSkillTree = new TIntObjectHashMap<Set<SkillLearn>>();
	private TIntObjectMap<Set<SkillLearn>> _transformationSkillTree = new TIntObjectHashMap<Set<SkillLearn>>();
	// без зависимостей
	private Set<SkillLearn> _certificationSkillTree = new HashSet<SkillLearn>();
	private Set<SkillLearn> _rebornSkillTree = new HashSet<SkillLearn>();
	private Set<SkillLearn> _collectionSkillTree = new HashSet<SkillLearn>();
	private Set<SkillLearn> _pledgeSkillTree = new HashSet<SkillLearn>();
	private Set<SkillLearn> _subUnitSkillTree = new HashSet<SkillLearn>();
	private Set<SkillLearn> _gmSkillTree = new HashSet<SkillLearn>();

	public int getMinLevelForNewSkill(Player player, ClassId classId, AcquireType type)
	{
		Set<SkillLearn> skills;
		switch(type)
		{
			case NORMAL:
				skills = _normalSkillTree.get(player.getActiveClassId());
				if(skills == null)
				{
					info("skill tree for class " + player.getActiveClassId() + " is not defined !");
					return 0;
				}
				break;
			case TRANSFORMATION:
				skills = _transformationSkillTree.get(player.getRace().ordinal());
				if(skills == null)
				{
					info("skill tree for race " + player.getRace().ordinal() + " is not defined !");
					return 0;
				}
				break;
			case FISHING:
				skills = _fishingSkillTree.get(player.getRace().ordinal());
				if(skills == null)
				{
					info("skill tree for race " + player.getRace().ordinal() + " is not defined !");
					return 0;
				}
				break;
			case GENERAL:
				skills = _generalSkillTree.get(player.getActiveClassId());
				if(skills == null)
				{
					info("skill tree for class " + player.getActiveClassId() + " is not defined !");
					return 0;
				}
				break;
			case MULTICLASS:
				if(Config.MULTICLASS_SYSTEM_ENABLED)
				{
					if(classId != null)
					{
						TIntObjectMap<Set<SkillLearn>> map = _multiclassLearnSkillTree.get(player.getActiveClassId());
						if(map == null)
						{
							info("Skill tree for learn multiclass " + player.getActiveClassId() + " is not defined !");
							return 0;
						}

						skills = map.get(classId.getId());
						if(skills == null)
						{
							info("Skill tree for learn multiclass " + player.getActiveClassId() + ":" + classId.getId() + " is not defined !");
							return 0;
						}
					}
					else
					{
						skills = _multiclassCheckSkillTree.get(player.getActiveClassId());
						if(skills == null)
						{
							info("Skill tree for check multiclass " + player.getActiveClassId() + " is not defined !");
							return 0;
						}
					}
				}
				else
					return 0;
				break;
			default:
				return 0;
		}
		int minlevel = 0;
		for(SkillLearn temp : skills)
		{
			if(temp.getMinLevel() > player.getLevel())
			{
				if(minlevel == 0 || temp.getMinLevel() < minlevel)
					minlevel = temp.getMinLevel();
			}
		}
		return minlevel;
	}

	public Collection<SkillLearn> getAvailableSkills(Player player, AcquireType type)
	{
		return getAvailableSkills(player, null, type, null);
	}

	public Collection<SkillLearn> getAvailableSkills(Player player, AcquireType type, SubUnit subUnit)
	{
		return getAvailableSkills(player, null, type, subUnit);
	}

	public Collection<SkillLearn> getAvailableSkills(Player player, ClassId classId, AcquireType type, SubUnit subUnit)
	{
		Collection<SkillLearn> skills;
		switch(type)
		{
			case NORMAL:
				skills = _normalSkillTree.get(player.getActiveClassId());
				if(skills == null)
				{
					info("skill tree for class " + player.getActiveClassId() + " is not defined !");
					return Collections.emptyList();
				}
				return getAvaliableList(skills, player.getAllSkillsArray(), player.getLevel());
			case COLLECTION:
				skills = _collectionSkillTree;
				if(skills == null)
				{
					info("skill tree for class " + player.getActiveClassId() + " is not defined !");
					return Collections.emptyList();
				}
				return getAvaliableList(skills, player.getAllSkillsArray(), player.getLevel());
			case TRANSFORMATION:
				skills = _transformationSkillTree.get(player.getRace().ordinal());
				if(skills == null)
				{
					info("skill tree for race " + player.getRace().ordinal() + " is not defined !");
					return Collections.emptyList();
				}
				return getAvaliableList(skills, player.getAllSkillsArray(), player.getLevel());
			case TRANSFER_EVA_SAINTS:
			case TRANSFER_SHILLIEN_SAINTS:
			case TRANSFER_CARDINAL:
				skills = _transferSkillTree.get(type.transferClassId());
				if(skills == null)
				{
					info("skill tree for class " + type.transferClassId() + " is not defined !");
					return Collections.emptyList();
				}
				if(player == null)
					return skills;
				else
				{
					Map<Integer, SkillLearn> skillLearnMap = new TreeMap<Integer, SkillLearn>();
					for(SkillLearn temp : skills)
						if(temp.getMinLevel() <= player.getLevel())
						{
							int knownLevel = player.getSkillLevel(temp.getId());
							if(knownLevel == -1)
								skillLearnMap.put(temp.getId(), temp);
						}

					return skillLearnMap.values();
				}
			case FISHING:
				skills = _fishingSkillTree.get(player.getRace().ordinal());
				if(skills == null)
				{
					info("skill tree for race " + player.getRace().ordinal() + " is not defined !");
					return Collections.emptyList();
				}
				return getAvaliableList(skills, player.getAllSkillsArray(), player.getLevel());
			case CLAN:
				skills = _pledgeSkillTree;
				Collection<Skill> skls = player.getClan().getSkills(); //TODO [VISTALL] придумать другой способ

				return getAvaliableList(skills, skls.toArray(new Skill[skls.size()]), player.getClan().getLevel());
			case SUB_UNIT:
				skills = _subUnitSkillTree;
				Collection<Skill> st = subUnit.getSkills(); //TODO [VISTALL] придумать другой способ

				return getAvaliableList(skills, st.toArray(new Skill[st.size()]), player.getClan().getLevel());
			case CERTIFICATION:
				skills = _certificationSkillTree;
				if(player == null)
					return skills;
				else
					return getAvaliableList(skills, player.getAllSkillsArray(), player.getLevel());
			case REBORN:
				skills = _rebornSkillTree;
				if(player == null)
					return skills;
				else
					return getAvaliableList(skills, player.getAllSkillsArray(), player.getLevel());	
			case GM:
				skills = _gmSkillTree;
				if(player == null)
					return skills;
				else
					return getAvaliableList(skills, player.getAllSkillsArray(), player.getLevel());
			case GENERAL:
				skills = _generalSkillTree.get(player.getActiveClassId());
				if(skills == null)
				{
					info("skill tree for class " + player.getActiveClassId() + " is not defined !");
					return Collections.emptyList();
				}
				return getAvaliableList(skills, player.getAllSkillsArray(), player.getLevel());
			case MULTICLASS:
				if(Config.MULTICLASS_SYSTEM_ENABLED)
				{
					if(classId != null)
					{
						TIntObjectMap<Set<SkillLearn>> map = _multiclassLearnSkillTree.get(player.getActiveClassId());
						if(map == null)
						{
							info("Skill tree for learn multiclass " + player.getActiveClassId() + " is not defined !");
							return Collections.emptyList();
						}

						skills = map.get(classId.getId());
						if(skills == null)
						{
							info("Skill tree for learn multiclass " + player.getActiveClassId() + ":" + classId.getId() + " is not defined !");
							return Collections.emptyList();
						}
					}
					else
					{
						skills = _multiclassCheckSkillTree.get(player.getActiveClassId());
						if(skills == null)
						{
							info("Skill tree for check multiclass " + player.getActiveClassId() + " is not defined !");
							return Collections.emptyList();
						}
					}
					return getAvaliableList(skills, player.getAllSkillsArray(), player.getLevel());
				}
				else
					return Collections.emptyList();
			default:
				return Collections.emptyList();
		}
	}

	private Collection<SkillLearn> getAvaliableList(Collection<SkillLearn> skillLearns, Skill[] skills, int level)
	{
		Map<Integer, SkillLearn> skillLearnMap = new TreeMap<Integer, SkillLearn>();
		for(SkillLearn temp : skillLearns)
		{
			if(temp.getMinLevel() <= level)
			{
				boolean knownSkill = false;
				for(int j = 0; j < skills.length && !knownSkill; j++)
					if(skills[j].getId() == temp.getId())
					{
						knownSkill = true;
						if(skills[j].getLevel() == temp.getLevel() - 1)
							skillLearnMap.put(temp.getId(), temp);
					}
				if(!knownSkill && temp.getLevel() == 1)
					skillLearnMap.put(temp.getId(), temp);
			}
		}
		return skillLearnMap.values();
	}

	public Collection<SkillLearn> getAvailableMaxLvlSkills(Player player, AcquireType type)
	{
		return getAvailableMaxLvlSkills(player, null, type, null);
	}

	public Collection<SkillLearn> getAvailableMaxLvlSkills(Player player, AcquireType type, SubUnit subUnit)
	{
		return getAvailableMaxLvlSkills(player, null, type, subUnit);
	}

	public Collection<SkillLearn> getAvailableMaxLvlSkills(Player player, ClassId classId, AcquireType type, SubUnit subUnit)
	{
		Collection<SkillLearn> skills;
		switch(type)
		{
			case NORMAL:
				skills = _normalSkillTree.get(player.getActiveClassId());
				if(skills == null)
				{
					info("skill tree for class " + player.getActiveClassId() + " is not defined !");
					return Collections.emptyList();
				}
				return getAvaliableMaxLvlSkillList(skills, player.getAllSkillsArray(), player.getLevel());
			case COLLECTION:
				skills = _collectionSkillTree;
				if(skills == null)
				{
					info("skill tree for class " + player.getActiveClassId() + " is not defined !");
					return Collections.emptyList();
				}
				return getAvaliableMaxLvlSkillList(skills, player.getAllSkillsArray(), player.getLevel());
			case TRANSFORMATION:
				skills = _transformationSkillTree.get(player.getRace().ordinal());
				if(skills == null)
				{
					info("skill tree for race " + player.getRace().ordinal() + " is not defined !");
					return Collections.emptyList();
				}
				return getAvaliableMaxLvlSkillList(skills, player.getAllSkillsArray(), player.getLevel());
			case TRANSFER_EVA_SAINTS:
			case TRANSFER_SHILLIEN_SAINTS:
			case TRANSFER_CARDINAL:
				skills = _transferSkillTree.get(type.transferClassId());
				if(skills == null)
				{
					info("skill tree for class " + type.transferClassId() + " is not defined !");
					return Collections.emptyList();
				}
				if(player == null)
					return skills;
				else
				{
					Map<Integer, SkillLearn> skillLearnMap = new TreeMap<Integer, SkillLearn>();
					for(SkillLearn temp : skills)
						if(temp.getMinLevel() <= player.getLevel())
						{
							int knownLevel = player.getSkillLevel(temp.getId());
							if(knownLevel == -1)
								skillLearnMap.put(temp.getId(), temp);
						}

					return skillLearnMap.values();
				}
			case FISHING:
				skills = _fishingSkillTree.get(player.getRace().ordinal());
				if(skills == null)
				{
					info("skill tree for race " + player.getRace().ordinal() + " is not defined !");
					return Collections.emptyList();
				}
				return getAvaliableMaxLvlSkillList(skills, player.getAllSkillsArray(), player.getLevel());
			case CLAN:
				skills = _pledgeSkillTree;
				Collection<Skill> skls = player.getClan().getSkills(); //TODO [VISTALL] придумать другой способ

				return getAvaliableMaxLvlSkillList(skills, skls.toArray(new Skill[skls.size()]), player.getClan().getLevel());
			case SUB_UNIT:
				skills = _subUnitSkillTree;
				Collection<Skill> st = subUnit.getSkills(); //TODO [VISTALL] придумать другой способ

				return getAvaliableMaxLvlSkillList(skills, st.toArray(new Skill[st.size()]), player.getClan().getLevel());
			case CERTIFICATION:
				skills = _certificationSkillTree;
				if(player == null)
					return skills;
				else
					return getAvaliableMaxLvlSkillList(skills, player.getAllSkillsArray(), player.getLevel());
			case REBORN:
				skills = _rebornSkillTree;
				if(player == null)
					return skills;
				else
					return getAvaliableMaxLvlSkillList(skills, player.getAllSkillsArray(), player.getLevel());	
			case GM:
				skills = _gmSkillTree;
				if(player == null)
					return skills;
				else
					return getAvaliableMaxLvlSkillList(skills, player.getAllSkillsArray(), player.getLevel());
			case GENERAL:
				skills = _generalSkillTree.get(player.getActiveClassId());
				if(skills == null)
				{
					info("skill tree for class " + player.getActiveClassId() + " is not defined !");
					return Collections.emptyList();
				}
				return getAvaliableMaxLvlSkillList(skills, player.getAllSkillsArray(), player.getLevel());
			case MULTICLASS:
				if(Config.MULTICLASS_SYSTEM_ENABLED)
				{
					if(classId != null)
					{
						TIntObjectMap<Set<SkillLearn>> map = _multiclassLearnSkillTree.get(player.getActiveClassId());
						if(map == null)
						{
							info("Skill tree for learn multiclass " + player.getActiveClassId() + " is not defined !");
							return Collections.emptyList();
						}

						skills = map.get(classId.getId());
						if(skills == null)
						{
							info("Skill tree for learn multiclass " + player.getActiveClassId() + ":" + classId.getId() + " is not defined !");
							return Collections.emptyList();
						}
					}
					else
					{
						skills = _multiclassCheckSkillTree.get(player.getActiveClassId());
						if(skills == null)
						{
							info("Skill tree for check multiclass " + player.getActiveClassId() + " is not defined !");
							return Collections.emptyList();
						}
					}
					return getAvaliableMaxLvlSkillList(skills, player.getAllSkillsArray(), player.getLevel());
				}
				else
					return Collections.emptyList();
			default:
				return Collections.emptyList();
		}
	}

	private Collection<SkillLearn> getAvaliableMaxLvlSkillList(Collection<SkillLearn> skillLearns, Skill[] skills, int level)
	{
		Map<Integer, SkillLearn> skillLearnMap = new TreeMap<Integer, SkillLearn>();
		for(SkillLearn temp : skillLearns)
		{
			int skillId = temp.getId();
			if(!skillLearnMap.containsKey(skillId) || temp.getLevel() > skillLearnMap.get(skillId).getLevel())
				skillLearnMap.put(skillId, temp);
		}

		for(Skill skill : skills)
		{
			int skillId = skill.getId();
			if(!skillLearnMap.containsKey(skillId))
				continue;

			SkillLearn temp = skillLearnMap.get(skillId);
			if(temp == null)
				continue;

			if(temp.getLevel() <= skill.getLevel())
				skillLearnMap.remove(skillId);
		}

		return skillLearnMap.values();
	}

	public Collection<SkillLearn> getAcquirableSkillListByClass(Player player)
	{
		Map<Integer, SkillLearn> skillListMap = new TreeMap<Integer, SkillLearn>();

		Collection<SkillLearn> skills = _normalSkillTree.get(player.getActiveClassId());
		Collection<SkillLearn> currentLvlSkills = getAvaliableList(skills, player.getAllSkillsArray(), player.getLevel());
		for(SkillLearn temp : currentLvlSkills)
		{
			if(!temp.isFreeAutoGet(AcquireType.NORMAL))
				skillListMap.put(temp.getId(), temp);
		}

		Collection<SkillLearn> nextLvlsSkills = getAvaliableList(skills, player.getAllSkillsArray(), player.getMaxLevel());
		for(SkillLearn temp : nextLvlsSkills)
		{
			if(!temp.isFreeAutoGet(AcquireType.NORMAL) && !skillListMap.containsKey(temp.getId()))
				skillListMap.put(temp.getId(), temp);
		}

		return skillListMap.values();
	}

	public SkillLearn getSkillLearn(Player player, int id, int level, AcquireType type)
	{
		return getSkillLearn(player, null, id, level, type);
	}

	public SkillLearn getSkillLearn(Player player, ClassId classId, int id, int level, AcquireType type)
	{
		Set<SkillLearn> skills;
		switch(type)
		{
			case NORMAL:
				skills = _normalSkillTree.get(player.getActiveClassId());
				break;
			case COLLECTION:
				skills = _collectionSkillTree;
				break;
			case TRANSFORMATION:
				skills = _transformationSkillTree.get(player.getRace().ordinal());
				break;
			case TRANSFER_CARDINAL:
			case TRANSFER_SHILLIEN_SAINTS:
			case TRANSFER_EVA_SAINTS:
				skills = _transferSkillTree.get(player.getActiveClassId());
				break;
			case FISHING:
				skills = _fishingSkillTree.get(player.getRace().ordinal());
				break;
			case CLAN:
				skills = _pledgeSkillTree;
				break;
			case SUB_UNIT:
				skills = _subUnitSkillTree;
				break;
			case CERTIFICATION:
				skills = _certificationSkillTree;
				break;
			case REBORN:
				skills = _rebornSkillTree;
				break;	
			case GM:
				skills = _gmSkillTree;
				break;
			case GENERAL:
				skills = _generalSkillTree.get(player.getActiveClassId());
				break;
			case MULTICLASS:
				if(Config.MULTICLASS_SYSTEM_ENABLED)
				{
					if(classId != null)
					{
						TIntObjectMap<Set<SkillLearn>> map = _multiclassLearnSkillTree.get(player.getActiveClassId());
						if(map == null)
							return null;

						skills = map.get(classId.getId());
					}
					else
						skills = _multiclassCheckSkillTree.get(player.getActiveClassId());
				}
				else
					return null;
				break;
			default:
				return null;
		}

		if(skills == null)
			return null;

		for(SkillLearn temp : skills)
			if(temp.getLevel() == level && temp.getId() == id)
				return temp;

		return null;
	}

	public boolean isSkillPossible(Player player, Skill skill, AcquireType type)
	{
		return isSkillPossible(player, null, skill, type, false);
	}

	public boolean isSkillPossible(Player player, ClassId classId, Skill skill, AcquireType type, boolean withMulticlass)
	{
		Clan clan = null;
		Set<SkillLearn> skills;
		switch(type)
		{
			case NORMAL:
				skills = new HashSet<SkillLearn>();
				if(withMulticlass && (Config.ALT_ENABLE_MULTI_PROFESSION || Config.TRANS_SUBCLASS_SKILL_TO_MAIN && !player.isSubClassActive()))
				{
					for(SubClass subClass : player.getSubClassList())
						skills.addAll(_normalSkillTree.get(subClass.getClassId()));
				}
				else
					skills.addAll(_normalSkillTree.get(player.getActiveClassId()));
				break;
			case COLLECTION:
				skills = _collectionSkillTree;
				break;
			case TRANSFORMATION:
				skills = _transformationSkillTree.get(player.getRace().ordinal());
				break;
			case FISHING:
				skills = _fishingSkillTree.get(player.getRace().ordinal());
				break;
			case TRANSFER_CARDINAL:
			case TRANSFER_EVA_SAINTS:
			case TRANSFER_SHILLIEN_SAINTS:
				int transferId = type.transferClassId();
				if(player.getActiveClassId() != transferId)
					return false;

				skills = _transferSkillTree.get(transferId);
				break;
			case CLAN:
				clan = player.getClan();
				if(clan == null)
					return false;
				skills = _pledgeSkillTree;
				break;
			case SUB_UNIT:
				clan = player.getClan();
				if(clan == null)
					return false;

				skills = _subUnitSkillTree;
				break;
			case CERTIFICATION:
				skills = _certificationSkillTree;
				break;
			case REBORN:
				skills = _rebornSkillTree;
				break;
			case GM:
				if(!player.isGM())
					return false;
				skills = _gmSkillTree;
				break;
			case GENERAL:
				skills = _generalSkillTree.get(player.getActiveClassId());
				break;
			case MULTICLASS:
				if(Config.MULTICLASS_SYSTEM_ENABLED)
				{
					if(classId != null)
					{
						TIntObjectMap<Set<SkillLearn>> map = _multiclassLearnSkillTree.get(player.getActiveClassId());
						if(map == null)
							return false;

						skills = map.get(classId.getId());
					}
					else
						skills = _multiclassCheckSkillTree.get(player.getActiveClassId());
				}
				else
					return false;
				break;
			default:
				return false;
		}

		return isSkillPossible(skills, skill);
	}

	private boolean isSkillPossible(Collection<SkillLearn> skills, Skill skill)
	{
		for(SkillLearn learn : skills)
			if(learn.getId() == skill.getId() && learn.getLevel() <= skill.getLevel())
				return true;
		return false;
	}

	public boolean isSkillPossible(Player player, Skill skill)
	{
		return isSkillPossible(player, null, skill, false);
	}

	public boolean isSkillPossible(Player player, ClassId classId, Skill skill, boolean withMulticlass)
	{
		for(AcquireType aq : AcquireType.VALUES)
			if(isSkillPossible(player, classId, skill, aq, withMulticlass))
				return true;

		return false;
	}

	public List<SkillLearn> getSkillLearnListByItemId(Player player, int itemId, boolean multiclass)
	{
		List<SkillLearn> l = new ArrayList<SkillLearn>(1);
		
		for(SkillLearn learning : _collectionSkillTree)
		{
			if(learning.isClicked() && learning.getItemId() == itemId)
				l.add(learning);
		}

		if(!l.isEmpty())
			return l;

		Set<SkillLearn> learns = _normalSkillTree.get(player.getActiveClassId());
		if(learns != null)
		{
			for(SkillLearn $i : learns)
			{
				if($i.getItemId() == itemId)
					l.add($i);
			}

			if(!l.isEmpty())
				return l;
		}

		learns = _generalSkillTree.get(player.getActiveClassId());
		if(learns != null)
		{
			for(SkillLearn $i : learns)
			{
				if($i.getItemId() == itemId)
					l.add($i);
			}

			if(!l.isEmpty())
				return l;
		}

		if(multiclass)
		{
			TIntObjectMap<Set<SkillLearn>> map = _multiclassLearnSkillTree.get(player.getActiveClassId());
			if(map == null)
				return Collections.emptyList();

			for(Set<SkillLearn> temp : map.valueCollection())
			{
				for(SkillLearn $i : temp)
				{
					if($i.getItemId() == itemId)
						l.add($i);
				}

				if(!l.isEmpty())
					return l;
			}
		}
		return Collections.emptyList();
	}

	public List<SkillLearn> getAllNormalSkillTreeWithForgottenScrolls()
	{
		List<SkillLearn> a = new ArrayList<SkillLearn>();
		for(TIntObjectIterator<Set<SkillLearn>> i = _normalSkillTree.iterator(); i.hasNext();)
		{
			i.advance();
			for(SkillLearn learn : i.value())
				if(learn.getItemId() > 0 && learn.isClicked())
					a.add(learn);
		}

		for(TIntObjectIterator<Set<SkillLearn>> i = _generalSkillTree.iterator(); i.hasNext();)
		{
			i.advance();
			for(SkillLearn learn : i.value())
				if(learn.getItemId() > 0 && learn.isClicked())
					a.add(learn);
		}

		for(SkillLearn learning : _collectionSkillTree)
		{
			if(learning.getItemId() > 0 && learning.isClicked())
				a.add(learning);
		}		

		return a;
	}

	public void addAllNormalSkillLearns(int classId, Set<SkillLearn> s)
	{
		Set<SkillLearn> set = _normalSkillTree.get(classId);
		if(set == null)
		{
			set = new HashSet<SkillLearn>();
			_normalSkillTree.put(classId, set);
		}
		set.addAll(s);
	}

	public void initNormalSkillLearns()
	{
		TIntObjectMap<Set<SkillLearn>> map = new TIntObjectHashMap<Set<SkillLearn>>(_normalSkillTree);

		_normalSkillTree.clear();

		for(final ClassId classId : ClassId.VALUES)
		{
			if(classId.isDummy())
				continue;

			Set<SkillLearn> skills = map.get(classId.getId());
			if(skills == null)
			{
				info("Not found NORMAL skill learn for class " + classId.getId());
				continue;
			}

			_normalSkillTree.put(classId.getId(), skills);

			ClassId secondparent = classId.getParent(1);
			if(secondparent == classId.getParent(0))
				secondparent = null;

			ClassId tempClassId = classId.getParent(0);
			while(tempClassId != null)
			{
				if(_normalSkillTree.containsKey(tempClassId.getId()))
					skills.addAll(_normalSkillTree.get(tempClassId.getId()));

				tempClassId = tempClassId.getParent(0);
				if(tempClassId == null && secondparent != null)
				{
					tempClassId = secondparent;
					secondparent = secondparent.getParent(1);
				}
			}
		}

		if(Config.MULTICLASS_SYSTEM_ENABLED)
		{
			for(ClassId classId : ClassId.VALUES)
			{
				if(classId.isDummy())
					continue;

				TIntObjectMap<Set<SkillLearn>> multiMap = new TIntObjectHashMap<Set<SkillLearn>>();
				Set<SkillLearn> multiSet = new HashSet<SkillLearn>();
				for(ClassId sameLevelClassId : ClassId.VALUES)
				{
					if(!MulticlassUtils.checkMulticlass(classId, sameLevelClassId))
						continue;

					Set<SkillLearn> skills = new HashSet<SkillLearn>();
					loop: for(SkillLearn sl : _normalSkillTree.get(sameLevelClassId.getId()))
					{

						for(SkillLearn temp : _normalSkillTree.get(classId.getId()))
						{
							if(sl.getId() == temp.getId() && sl.getLevel() == temp.getLevel())
								continue loop;
						}

						if(sl.isClicked())
						{
							skills.add(new SkillLearn(sl.getId(), sl.getLevel(), sl.getMinLevel(), sl.getCost(), sl.getItemId(), sl.getItemCount(), true, sl.getClassLevel()));
							continue;
						}

						double spModifier;
						int costItemIdBasedOnSp;
						double costItemCountModifierBasedOnSp;
						int costItemId;
						long costItemCount;
						if(sl.getClassLevel() == ClassLevel.FIRST)
						{
							spModifier = Config.MULTICLASS_SYSTEM_1ST_CLASS_SP_MODIFIER;
							costItemIdBasedOnSp = Config.MULTICLASS_SYSTEM_1ST_CLASS_COST_ITEM_ID_BASED_ON_SP;
							costItemCountModifierBasedOnSp = Config.MULTICLASS_SYSTEM_1ST_CLASS_COST_ITEM_COUNT_MODIFIER_BASED_ON_SP;
							costItemId = Config.MULTICLASS_SYSTEM_1ST_CLASS_COST_ITEM_ID;
							costItemCount = Config.MULTICLASS_SYSTEM_1ST_CLASS_COST_ITEM_COUNT;
						}
						else if(sl.getClassLevel() == ClassLevel.SECOND)
						{
							spModifier = Config.MULTICLASS_SYSTEM_2ND_CLASS_SP_MODIFIER;
							costItemIdBasedOnSp = Config.MULTICLASS_SYSTEM_2ND_CLASS_COST_ITEM_ID_BASED_ON_SP;
							costItemCountModifierBasedOnSp = Config.MULTICLASS_SYSTEM_2ND_CLASS_COST_ITEM_COUNT_MODIFIER_BASED_ON_SP;
							costItemId = Config.MULTICLASS_SYSTEM_2ND_CLASS_COST_ITEM_ID;
							costItemCount = Config.MULTICLASS_SYSTEM_2ND_CLASS_COST_ITEM_COUNT;
						}
						else if(sl.getClassLevel() == ClassLevel.THIRD)
						{
							spModifier = Config.MULTICLASS_SYSTEM_3RD_CLASS_SP_MODIFIER;
							costItemIdBasedOnSp = Config.MULTICLASS_SYSTEM_3RD_CLASS_COST_ITEM_ID_BASED_ON_SP;
							costItemCountModifierBasedOnSp = Config.MULTICLASS_SYSTEM_3RD_CLASS_COST_ITEM_COUNT_MODIFIER_BASED_ON_SP;
							costItemId = Config.MULTICLASS_SYSTEM_3RD_CLASS_COST_ITEM_ID;
							costItemCount = Config.MULTICLASS_SYSTEM_3RD_CLASS_COST_ITEM_COUNT;
						}
						else
						{
							spModifier = Config.MULTICLASS_SYSTEM_NON_CLASS_SP_MODIFIER;
							costItemIdBasedOnSp = Config.MULTICLASS_SYSTEM_NON_CLASS_COST_ITEM_ID_BASED_ON_SP;
							costItemCountModifierBasedOnSp = Config.MULTICLASS_SYSTEM_NON_CLASS_COST_ITEM_COUNT_MODIFIER_BASED_ON_SP;
							costItemId = Config.MULTICLASS_SYSTEM_NON_CLASS_COST_ITEM_ID;
							costItemCount = Config.MULTICLASS_SYSTEM_NON_CLASS_COST_ITEM_COUNT;
						}

						SkillLearn skillLearn = new SkillLearn(sl.getId(), sl.getLevel(), sl.getMinLevel(), (int) (Math.max(1, sl.getCost()) * spModifier), sl.getItemId(), sl.getItemCount(), false, sl.getClassLevel());
						if(costItemIdBasedOnSp > 0 && costItemCountModifierBasedOnSp > 0)
						{
							skillLearn.addAdditionalRequiredItem(costItemIdBasedOnSp, Math.max(1, (long) (skillLearn.getCost() * costItemCountModifierBasedOnSp)));
						}
						if(costItemId > 0 && costItemCount > 0)
						{
							skillLearn.addAdditionalRequiredItem(costItemId, costItemCount);
						}
						skills.add(skillLearn);
					}
					// TODO: Придумать алгоритм постройки цены изучения мульти-класс умений.
					multiMap.put(sameLevelClassId.getId(), skills);
					multiSet.addAll(skills);
				}
				_multiclassCheckSkillTree.put(classId.getId(), multiSet);
				_multiclassLearnSkillTree.put(classId.getId(), multiMap);
			}
		}
	}

	public void addAllGeneralSkillLearns(int classId, Set<SkillLearn> s)
	{
		Set<SkillLearn> set = _generalSkillTree.get(classId);
		if(set == null)
		{
			set = new HashSet<SkillLearn>();
			_generalSkillTree.put(classId, set);
		}
		set.addAll(s);
	}

	//TODO: [Bonux] Добавить гендерные различия.
	public void initGeneralSkillLearns()
	{
		TIntObjectMap<Set<SkillLearn>> map = new TIntObjectHashMap<Set<SkillLearn>>(_generalSkillTree);
		Set<SkillLearn> globalList = map.remove(-1); // Скиллы которые принадлежат любому классу.

		_generalSkillTree.clear();

		for(final ClassId classId : ClassId.VALUES)
		{
			if(classId.isDummy())
				continue;

			Set<SkillLearn> tempList = map.get(classId.getId());
			if(tempList == null)
				tempList = new HashSet<SkillLearn>();

			Set<SkillLearn> skills = new HashSet<SkillLearn>();
			_generalSkillTree.put(classId.getId(), skills);

			ClassId secondparent = classId.getParent(1);
			if(secondparent == classId.getParent(0))
				secondparent = null;

			ClassId tempClassId = classId.getParent(0);
			while(tempClassId != null)
			{
				if(_generalSkillTree.containsKey(tempClassId.getId()))
					tempList.addAll(_generalSkillTree.get(tempClassId.getId()));

				tempClassId = tempClassId.getParent(0);
				if(tempClassId == null && secondparent != null)
				{
					tempClassId = secondparent;
					secondparent = secondparent.getParent(1);
				}
			}

			tempList.addAll(globalList);

			skills.addAll(tempList);
		}
	}

	public void addAllFishingLearns(int race, Set<SkillLearn> s)
	{
		_fishingSkillTree.put(race, s);
	}

	public void addAllTransferLearns(int classId, Set<SkillLearn> s)
	{
		_transferSkillTree.put(classId, s);
	}

	public void addAllTransformationLearns(int race, Set<SkillLearn> s)
	{
		_transformationSkillTree.put(race, s);
	}

	public void addAllCertificationLearns(Set<SkillLearn> s)
	{
		_certificationSkillTree.addAll(s);
	}
	
	public void addAllRebornLearns(Set<SkillLearn> s)
	{
		_rebornSkillTree.addAll(s);
	}
	
	public void addAllCollectionLearns(Set<SkillLearn> s)
	{
		_collectionSkillTree.addAll(s);
	}

	public void addAllSubUnitLearns(Set<SkillLearn> s)
	{
		_subUnitSkillTree.addAll(s);
	}

	public void addAllPledgeLearns(Set<SkillLearn> s)
	{
		_pledgeSkillTree.addAll(s);
	}

	public void addAllGMLearns(Set<SkillLearn> s)
	{
		_gmSkillTree.addAll(s);
	}

	@Override
	public void log()
	{
		info("load " + sizeTroveMap(_normalSkillTree) + " normal learns for " + _normalSkillTree.size() + " classes.");
		info("load " + sizeTroveMap(_transferSkillTree) + " transfer learns for " + _transferSkillTree.size() + " classes.");
		info("load " + sizeTroveMap(_generalSkillTree) + " general skills learns for " + _generalSkillTree.size() + " classes.");
		//
		info("load " + sizeTroveMap(_transformationSkillTree) + " transformation learns for " + _transformationSkillTree.size() + " races.");
		info("load " + sizeTroveMap(_fishingSkillTree) + " fishing learns for " + _fishingSkillTree.size() + " races.");
		//
		info("load " + _certificationSkillTree.size() + " certification learns.");
		info("load " + _collectionSkillTree.size() + " collection learns.");
		info("load " + _pledgeSkillTree.size() + " pledge learns.");
		info("load " + _subUnitSkillTree.size() + " sub unit learns.");
		info("load " + _gmSkillTree.size() + " GM skills learns.");

		if(Config.ALLOWED_REBORN_COUNT > 0)
			info("load " + _rebornSkillTree.size() + " reborn skills learns.");
	}

	@Deprecated
	@Override
	public int size()
	{
		return 0;
	}

	@Override
	public void clear()
	{
		_normalSkillTree.clear();
		_fishingSkillTree.clear();
		_transferSkillTree.clear();
		_certificationSkillTree.clear();
		_collectionSkillTree.clear();
		_pledgeSkillTree.clear();
		_subUnitSkillTree.clear();
		_generalSkillTree.clear();
		_gmSkillTree.clear();
		_rebornSkillTree.clear();
	}

	private int sizeTroveMap(TIntObjectMap<Set<SkillLearn>> a)
	{
		int i = 0;
		for(TIntObjectIterator<Set<SkillLearn>> iterator = a.iterator(); iterator.hasNext();)
		{
			iterator.advance();
			i += iterator.value().size();
		}

		return i;
	}
}
