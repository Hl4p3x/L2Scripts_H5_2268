package l2s.gameserver.model.actor.instances.player;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.base.ClassType;
import l2s.gameserver.model.base.Race;

public final class SubClassInfo
{
	private static final Set<ClassId> MAIN_SUBCLASS_SET;
	private static final Set<ClassId> KAMAEL_SUBCLASS_SET;

	private static final Set<ClassId> BANNED_SUBCLASSES = EnumSet.of(ClassId.OVERLORD, ClassId.WARSMITH);
	private static final Set<ClassId> SUBCLASS_SET_1 = EnumSet.of(ClassId.DARK_AVENGER, ClassId.PALADIN, ClassId.TEMPLE_KNIGHT, ClassId.SHILLEN_KNIGHT);
	private static final Set<ClassId> SUBCLASS_SET_2 = EnumSet.of(ClassId.TREASURE_HUNTER, ClassId.ABYSS_WALKER, ClassId.PLAIN_WALKER);
	private static final Set<ClassId> SUBCLASS_SET_3 = EnumSet.of(ClassId.HAWKEYE, ClassId.SILVER_RANGER, ClassId.PHANTOM_RANGER);
	private static final Set<ClassId> SUBCLASS_SET_4 = EnumSet.of(ClassId.WARLOCK, ClassId.ELEMENTAL_SUMMONER, ClassId.PHANTOM_SUMMONER);
	private static final Set<ClassId> SUBCLASS_SET_5 = EnumSet.of(ClassId.SORCERER, ClassId.SPELLSINGER, ClassId.SPELLHOWLER);
	private static final Set<ClassId> SUBCLASS_SET_6 = EnumSet.of(ClassId.INSPECTOR);

	private static final EnumMap<ClassId, Set<ClassId>> SUBCLASS_SET_MAP = new EnumMap<ClassId, Set<ClassId>>(ClassId.class);
	static
	{
		KAMAEL_SUBCLASS_SET = getSet(Race.KAMAEL, ClassLevel.SECOND);

		MAIN_SUBCLASS_SET = getSet(null, ClassLevel.SECOND);
		MAIN_SUBCLASS_SET.removeAll(BANNED_SUBCLASSES);
		MAIN_SUBCLASS_SET.removeAll(KAMAEL_SUBCLASS_SET);

		SUBCLASS_SET_MAP.put(ClassId.DARK_AVENGER, SUBCLASS_SET_1);
		SUBCLASS_SET_MAP.put(ClassId.HELL_KNIGHT, SUBCLASS_SET_1);
		SUBCLASS_SET_MAP.put(ClassId.PALADIN, SUBCLASS_SET_1);
		SUBCLASS_SET_MAP.put(ClassId.PHOENIX_KNIGHT, SUBCLASS_SET_1);
		SUBCLASS_SET_MAP.put(ClassId.TEMPLE_KNIGHT, SUBCLASS_SET_1);
		SUBCLASS_SET_MAP.put(ClassId.EVAS_TEMPLAR, SUBCLASS_SET_1);
		SUBCLASS_SET_MAP.put(ClassId.SHILLEN_KNIGHT, SUBCLASS_SET_1);
		SUBCLASS_SET_MAP.put(ClassId.SHILLIEN_TEMPLAR, SUBCLASS_SET_1);

		SUBCLASS_SET_MAP.put(ClassId.TREASURE_HUNTER, SUBCLASS_SET_2);
		SUBCLASS_SET_MAP.put(ClassId.ADVENTURER, SUBCLASS_SET_2);
		SUBCLASS_SET_MAP.put(ClassId.ABYSS_WALKER, SUBCLASS_SET_2);
		SUBCLASS_SET_MAP.put(ClassId.GHOST_HUNTER, SUBCLASS_SET_2);
		SUBCLASS_SET_MAP.put(ClassId.PLAIN_WALKER, SUBCLASS_SET_2);
		SUBCLASS_SET_MAP.put(ClassId.WIND_RIDER, SUBCLASS_SET_2);

		SUBCLASS_SET_MAP.put(ClassId.HAWKEYE, SUBCLASS_SET_3);
		SUBCLASS_SET_MAP.put(ClassId.SAGITTARIUS, SUBCLASS_SET_3);
		SUBCLASS_SET_MAP.put(ClassId.SILVER_RANGER, SUBCLASS_SET_3);
		SUBCLASS_SET_MAP.put(ClassId.MOONLIGHT_SENTINEL, SUBCLASS_SET_3);
		SUBCLASS_SET_MAP.put(ClassId.PHANTOM_RANGER, SUBCLASS_SET_3);
		SUBCLASS_SET_MAP.put(ClassId.GHOST_SENTINEL, SUBCLASS_SET_3);

		SUBCLASS_SET_MAP.put(ClassId.WARLOCK, SUBCLASS_SET_4);
		SUBCLASS_SET_MAP.put(ClassId.ARCANA_LORD, SUBCLASS_SET_4);
		SUBCLASS_SET_MAP.put(ClassId.ELEMENTAL_SUMMONER, SUBCLASS_SET_4);
		SUBCLASS_SET_MAP.put(ClassId.ELEMENTAL_MASTER, SUBCLASS_SET_4);
		SUBCLASS_SET_MAP.put(ClassId.PHANTOM_SUMMONER, SUBCLASS_SET_4);
		SUBCLASS_SET_MAP.put(ClassId.SPECTRAL_MASTER, SUBCLASS_SET_4);

		SUBCLASS_SET_MAP.put(ClassId.SORCERER, SUBCLASS_SET_5);
		SUBCLASS_SET_MAP.put(ClassId.ARCHMAGE, SUBCLASS_SET_5);
		SUBCLASS_SET_MAP.put(ClassId.SPELLSINGER, SUBCLASS_SET_5);
		SUBCLASS_SET_MAP.put(ClassId.MYSTIC_MUSE, SUBCLASS_SET_5);
		SUBCLASS_SET_MAP.put(ClassId.SPELLHOWLER, SUBCLASS_SET_5);
		SUBCLASS_SET_MAP.put(ClassId.STORM_SCREAMER, SUBCLASS_SET_5);

		SUBCLASS_SET_MAP.put(ClassId.DOOMBRINGER, SUBCLASS_SET_6);
		SUBCLASS_SET_MAP.put(ClassId.M_SOUL_HOUND, SUBCLASS_SET_6);
		SUBCLASS_SET_MAP.put(ClassId.F_SOUL_HOUND, SUBCLASS_SET_6);
		SUBCLASS_SET_MAP.put(ClassId.TRICKSTER, SUBCLASS_SET_6);

		SUBCLASS_SET_MAP.put(ClassId.DUELIST, EnumSet.of(ClassId.GLADIATOR));
		SUBCLASS_SET_MAP.put(ClassId.DREADNOUGHT, EnumSet.of(ClassId.WARLORD));
		SUBCLASS_SET_MAP.put(ClassId.SOULTAKER, EnumSet.of(ClassId.NECROMANCER));
		SUBCLASS_SET_MAP.put(ClassId.CARDINAL, EnumSet.of(ClassId.BISHOP));
		SUBCLASS_SET_MAP.put(ClassId.HIEROPHANT, EnumSet.of(ClassId.PROPHET));
		SUBCLASS_SET_MAP.put(ClassId.SWORD_MUSE, EnumSet.of(ClassId.SWORDSINGER));
		SUBCLASS_SET_MAP.put(ClassId.EVAS_SAINT, EnumSet.of(ClassId.ELDER));
		SUBCLASS_SET_MAP.put(ClassId.SPECTRAL_DANCER, EnumSet.of(ClassId.BLADEDANCER));
		SUBCLASS_SET_MAP.put(ClassId.TITAN, EnumSet.of(ClassId.DESTROYER));
		SUBCLASS_SET_MAP.put(ClassId.GRAND_KHAVATARI, EnumSet.of(ClassId.TYRANT));
		SUBCLASS_SET_MAP.put(ClassId.DOMINATOR, EnumSet.of(ClassId.OVERLORD));
		SUBCLASS_SET_MAP.put(ClassId.DOOMCRYER, EnumSet.of(ClassId.WARCRYER));
	}

	public static Set<ClassId> getAvailableSubClasses(Player player, Race npcRace, ClassType npcTeachType, boolean isNew)
	{
		Set<ClassId> availSubs = null;
		Race race = player.getRace();
		if(race == Race.KAMAEL)
		{
			availSubs = EnumSet.copyOf(KAMAEL_SUBCLASS_SET);
		}
		else
		{
			ClassId classId = player.getClassId();
			if(classId.isOfLevel(ClassLevel.SECOND) || classId.isOfLevel(ClassLevel.THIRD))
			{
				availSubs = EnumSet.copyOf(MAIN_SUBCLASS_SET);

				availSubs.removeAll(BANNED_SUBCLASSES);
				availSubs.remove(classId);

				switch(race)
				{
					case ELF:
						availSubs.removeAll(getSet(Race.DARKELF, ClassLevel.SECOND));
						break;
					case DARKELF:
						availSubs.removeAll(getSet(Race.ELF, ClassLevel.SECOND));
						break;
				}

				Set<ClassId> unavailableClasses = SUBCLASS_SET_MAP.get(classId);

				if(unavailableClasses != null)
					availSubs.removeAll(unavailableClasses);
			}
		}

		final int charClassId = player.getBaseClassId();

		ClassId currClass = ClassId.VALUES[charClassId];

		if(availSubs == null)
			return Collections.emptySet();

		// Из списка сабов удаляем мейн класс игрока
		availSubs.remove(currClass);

		for(ClassId availSub : availSubs)
		{
			// Удаляем из списка возможных сабов, уже взятые сабы и их предков
			for(SubClass subClass : player.getSubClassList())
			{
				if(availSub.getId() == subClass.getClassId())
				{
					availSubs.remove(availSub);
					continue;
				}

				// Удаляем из возможных сабов их родителей, если таковые есть у чара
				ClassId parent = ClassId.VALUES[availSub.ordinal()].getParent(player.getSex());
				if(parent != null && parent.getId() == subClass.getClassId())
				{
					availSubs.remove(availSub);
					continue;
				}

				// Удаляем из возможных сабов родителей текущих сабклассов, иначе если взять саб berserker
				// и довести до 3ей профы - doombringer, игроку будет предложен berserker вновь (дежавю)
				ClassId subParent = ClassId.VALUES[subClass.getClassId()].getParent(player.getSex());
				if(subParent != null && subParent.getId() == availSub.getId())
					availSubs.remove(availSub);
			}

			if(npcRace != null && npcTeachType != null)
			{
				if(!availSub.isOfRace(Race.HUMAN) && !availSub.isOfRace(Race.ELF))
				{
					if(!availSub.isOfRace(npcRace))
						availSubs.remove(availSub);
				}
				else if(!availSub.isOfType(npcTeachType))
					availSubs.remove(availSub);
			}

			// Особенности саб классов камаэль
			if(availSub.isOfRace(Race.KAMAEL))
			{
				// Для Soulbreaker-а и SoulHound не предлагаем Soulbreaker-а другого пола
				if((currClass == ClassId.M_SOUL_HOUND || currClass == ClassId.F_SOUL_HOUND || currClass == ClassId.F_SOUL_BREAKER || currClass == ClassId.M_SOUL_BREAKER) && (availSub == ClassId.F_SOUL_BREAKER || availSub == ClassId.M_SOUL_BREAKER))
					availSubs.remove(availSub);

				// Для Berserker(doombringer) и Arbalester(trickster) предлагаем Soulbreaker-а только своего пола
				if(currClass == ClassId.BERSERKER || currClass == ClassId.DOOMBRINGER || currClass == ClassId.ARBALESTER || currClass == ClassId.TRICKSTER)
					if(player.getSex() == 1 && availSub == ClassId.M_SOUL_BREAKER || player.getSex() == 0 && availSub == ClassId.F_SOUL_BREAKER)
						availSubs.remove(availSub);

				// Inspector доступен, только когда вкачаны 2 возможных первых саба камаэль(+ мейн класс)
				if(availSub == ClassId.INSPECTOR && player.getSubClassList().size() < (isNew ? 3 : 4))
					availSubs.remove(availSub);
			}
	
			if(availSub == ClassId.DARK_AVENGER || availSub == ClassId.PALADIN || availSub == ClassId.TEMPLE_KNIGHT || availSub == ClassId.SHILLEN_KNIGHT)
				if(currClass == ClassId.DARK_AVENGER || currClass == ClassId.HELL_KNIGHT || currClass == ClassId.PALADIN || currClass == ClassId.PHOENIX_KNIGHT || currClass == ClassId.TEMPLE_KNIGHT || currClass == ClassId.EVAS_TEMPLAR || currClass == ClassId.SHILLEN_KNIGHT || currClass == ClassId.SHILLIEN_TEMPLAR)
					availSubs.remove(availSub);
		}
		return availSubs;
	}

	public static EnumSet<ClassId> getSet(Race race, ClassLevel level)
	{
		EnumSet<ClassId> allOf = EnumSet.noneOf(ClassId.class);

		for(ClassId classId : EnumSet.allOf(ClassId.class))
			if(race == null || classId.isOfRace(race))
				if(level == null || classId.isOfLevel(level))
					allOf.add(classId);

		return allOf;
	}

	/**
	 * Проверяет принципиальную совместимость двух сабов.
	 */
	public static boolean areClassesComportable(ClassId c1, ClassId c2)
	{
		if(c1.isOfRace(Race.KAMAEL) != c2.isOfRace(Race.KAMAEL))
			return false; // камаэли только с камаэлями
		if(c1.isOfRace(Race.ELF) && c2.isOfRace(Race.DARKELF) || c1.isOfRace(Race.DARKELF) && c2.isOfRace(Race.ELF))
			return false; // эльфы несовместимы с темными
		if(c1 == ClassId.OVERLORD || c1 == ClassId.WARSMITH || c2 == ClassId.OVERLORD || c2 == ClassId.WARSMITH)
			return false; // эти вообще
		if(SUBCLASS_SET_MAP.get(c1) == SUBCLASS_SET_MAP.get(c2))
			return false; // однотипные
		return true;
	}
}