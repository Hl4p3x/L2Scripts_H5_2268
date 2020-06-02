package l2s.gameserver.model.base;

import l2s.gameserver.data.xml.holder.ClassDataHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.templates.player.ClassData;

public enum ClassId
{
	/*Human Fighter 1st and 2nd class list*/
	/*0*/HUMAN_FIGHTER(ClassType.FIGHTER, Race.HUMAN, null, ClassLevel.NONE, null),
	/*1*/WARRIOR(ClassType.FIGHTER, Race.HUMAN, HUMAN_FIGHTER, ClassLevel.FIRST, null),
	/*2*/GLADIATOR(ClassType.FIGHTER, Race.HUMAN, WARRIOR, ClassLevel.SECOND, ClassType2.Warrior),
	/*3*/WARLORD(ClassType.FIGHTER, Race.HUMAN, WARRIOR, ClassLevel.SECOND, ClassType2.Warrior),
	/*4*/KNIGHT(ClassType.FIGHTER, Race.HUMAN, HUMAN_FIGHTER, ClassLevel.FIRST, null),
	/*5*/PALADIN(ClassType.FIGHTER, Race.HUMAN, KNIGHT, ClassLevel.SECOND, ClassType2.Knight),
	/*6*/DARK_AVENGER(ClassType.FIGHTER, Race.HUMAN, KNIGHT, ClassLevel.SECOND, ClassType2.Knight),
	/*7*/ROGUE(ClassType.FIGHTER, Race.HUMAN, HUMAN_FIGHTER, ClassLevel.FIRST, null),
	/*8*/TREASURE_HUNTER(ClassType.FIGHTER, Race.HUMAN, ROGUE, ClassLevel.SECOND, ClassType2.Rogue),
	/*9*/HAWKEYE(ClassType.FIGHTER, Race.HUMAN, ROGUE, ClassLevel.SECOND, ClassType2.Rogue),

	/*Human Mage 1st and 2nd class list*/
	/*10*/HUMAN_MAGE(ClassType.MYSTIC, Race.HUMAN, null, ClassLevel.NONE, null),
	/*11*/WIZARD(ClassType.MYSTIC, Race.HUMAN, HUMAN_MAGE, ClassLevel.FIRST, null),
	/*12*/SORCERER(ClassType.MYSTIC, Race.HUMAN, WIZARD, ClassLevel.SECOND, ClassType2.Wizard),
	/*13*/NECROMANCER(ClassType.MYSTIC, Race.HUMAN, WIZARD, ClassLevel.SECOND, ClassType2.Wizard),
	/*14*/WARLOCK(ClassType.MYSTIC, Race.HUMAN, WIZARD, ClassLevel.SECOND, ClassType2.Summoner),
	/*15*/CLERIC(ClassType.PRIEST, Race.HUMAN, HUMAN_MAGE, ClassLevel.FIRST, null),
	/*16*/BISHOP(ClassType.PRIEST, Race.HUMAN, CLERIC, ClassLevel.SECOND, ClassType2.Healer),
	/*17*/PROPHET(ClassType.PRIEST, Race.HUMAN, CLERIC, ClassLevel.SECOND, ClassType2.Enchanter),

	/*Elven Fighter 1st and 2nd class list*/
	/*18*/ELVEN_FIGHTER(ClassType.FIGHTER, Race.ELF, null, ClassLevel.NONE, null),
	/*19*/ELVEN_KNIGHT(ClassType.FIGHTER, Race.ELF, ELVEN_FIGHTER, ClassLevel.FIRST, null),
	/*20*/TEMPLE_KNIGHT(ClassType.FIGHTER, Race.ELF, ELVEN_KNIGHT, ClassLevel.SECOND, ClassType2.Knight),
	/*21*/SWORDSINGER(ClassType.FIGHTER, Race.ELF, ELVEN_KNIGHT, ClassLevel.SECOND, ClassType2.Enchanter),
	/*22*/ELVEN_SCOUT(ClassType.FIGHTER, Race.ELF, ELVEN_FIGHTER, ClassLevel.FIRST, null),
	/*23*/PLAIN_WALKER(ClassType.FIGHTER, Race.ELF, ELVEN_SCOUT, ClassLevel.SECOND, ClassType2.Rogue),
	/*24*/SILVER_RANGER(ClassType.FIGHTER, Race.ELF, ELVEN_SCOUT, ClassLevel.SECOND, ClassType2.Rogue),

	/*Elven Mage 1st and 2nd class list*/
	/*25*/ELVEN_MAGE(ClassType.MYSTIC, Race.ELF, null, ClassLevel.NONE, null),
	/*26*/ELVEN_WIZARD(ClassType.MYSTIC, Race.ELF, ELVEN_MAGE, ClassLevel.FIRST, null),
	/*27*/SPELLSINGER(ClassType.MYSTIC, Race.ELF, ELVEN_WIZARD, ClassLevel.SECOND, ClassType2.Wizard),
	/*28*/ELEMENTAL_SUMMONER(ClassType.MYSTIC, Race.ELF, ELVEN_WIZARD, ClassLevel.SECOND, ClassType2.Summoner),
	/*29*/ORACLE(ClassType.PRIEST, Race.ELF, ELVEN_MAGE, ClassLevel.FIRST, null),
	/*30*/ELDER(ClassType.PRIEST, Race.ELF, ORACLE, ClassLevel.SECOND, ClassType2.Healer),

	/*Darkelf Fighter 1st and 2nd class list*/
	/*31*/DARK_FIGHTER(ClassType.FIGHTER, Race.DARKELF, null, ClassLevel.NONE, null),
	/*32*/PALUS_KNIGHT(ClassType.FIGHTER, Race.DARKELF, DARK_FIGHTER, ClassLevel.FIRST, null),
	/*33*/SHILLEN_KNIGHT(ClassType.FIGHTER, Race.DARKELF, PALUS_KNIGHT, ClassLevel.SECOND, ClassType2.Knight),
	/*34*/BLADEDANCER(ClassType.FIGHTER, Race.DARKELF, PALUS_KNIGHT, ClassLevel.SECOND, ClassType2.Enchanter),
	/*35*/ASSASIN(ClassType.FIGHTER, Race.DARKELF, DARK_FIGHTER, ClassLevel.FIRST, null),
	/*36*/ABYSS_WALKER(ClassType.FIGHTER, Race.DARKELF, ASSASIN, ClassLevel.SECOND, ClassType2.Rogue),
	/*37*/PHANTOM_RANGER(ClassType.FIGHTER, Race.DARKELF, ASSASIN, ClassLevel.SECOND, ClassType2.Rogue),

	/*Darkelf Mage 1st and 2nd class list*/
	/*38*/DARK_MAGE(ClassType.MYSTIC, Race.DARKELF, null, ClassLevel.NONE, null),
	/*39*/DARK_WIZARD(ClassType.MYSTIC, Race.DARKELF, DARK_MAGE, ClassLevel.FIRST, null),
	/*40*/SPELLHOWLER(ClassType.MYSTIC, Race.DARKELF, DARK_WIZARD, ClassLevel.SECOND, ClassType2.Wizard),
	/*41*/PHANTOM_SUMMONER(ClassType.MYSTIC, Race.DARKELF, DARK_WIZARD, ClassLevel.SECOND, ClassType2.Summoner),
	/*42*/SHILLEN_ORACLE(ClassType.PRIEST, Race.DARKELF, DARK_MAGE, ClassLevel.FIRST, null),
	/*43*/SHILLEN_ELDER(ClassType.PRIEST, Race.DARKELF, SHILLEN_ORACLE, ClassLevel.SECOND, ClassType2.Healer),

	/*Orc Fighter 1st and 2nd class list*/
	/*44*/ORC_FIGHTER(ClassType.FIGHTER, Race.ORC, null, ClassLevel.NONE, null),
	/*45*/ORC_RAIDER(ClassType.FIGHTER, Race.ORC, ORC_FIGHTER, ClassLevel.FIRST, null),
	/*46*/DESTROYER(ClassType.FIGHTER, Race.ORC, ORC_RAIDER, ClassLevel.SECOND, ClassType2.Warrior),
	/*47*/ORC_MONK(ClassType.FIGHTER, Race.ORC, ORC_FIGHTER, ClassLevel.FIRST, null),
	/*48*/TYRANT(ClassType.FIGHTER, Race.ORC, ORC_MONK, ClassLevel.SECOND, ClassType2.Warrior),

	/*Orc Mage 1st and 2nd class list*/
	/*49*/ORC_MAGE(ClassType.MYSTIC, Race.ORC, null, ClassLevel.NONE, null),
	/*50*/ORC_SHAMAN(ClassType.MYSTIC, Race.ORC, ORC_MAGE, ClassLevel.FIRST, null),
	/*51*/OVERLORD(ClassType.MYSTIC, Race.ORC, ORC_SHAMAN, ClassLevel.SECOND, ClassType2.Enchanter),
	/*52*/WARCRYER(ClassType.MYSTIC, Race.ORC, ORC_SHAMAN, ClassLevel.SECOND, ClassType2.Enchanter),

	/*Dwarf Fighter 1st and 2nd class list*/
	/*53*/DWARVEN_FIGHTER(ClassType.FIGHTER, Race.DWARF, null, ClassLevel.NONE, null),
	/*54*/SCAVENGER(ClassType.FIGHTER, Race.DWARF, DWARVEN_FIGHTER, ClassLevel.FIRST, null),
	/*55*/BOUNTY_HUNTER(ClassType.FIGHTER, Race.DWARF, SCAVENGER, ClassLevel.SECOND, ClassType2.Warrior),
	/*56*/ARTISAN(ClassType.FIGHTER, Race.DWARF, DWARVEN_FIGHTER, ClassLevel.FIRST, null),
	/*57*/WARSMITH(ClassType.FIGHTER, Race.DWARF, ARTISAN, ClassLevel.SECOND, ClassType2.Warrior),

	/*Dummy Entries*/
	/*58*/DUMMY_ENTRY_58,
	/*59*/DUMMY_ENTRY_59,
	/*60*/DUMMY_ENTRY_60,
	/*61*/DUMMY_ENTRY_61,
	/*62*/DUMMY_ENTRY_62,
	/*63*/DUMMY_ENTRY_63,
	/*64*/DUMMY_ENTRY_64,
	/*65*/DUMMY_ENTRY_65,
	/*66*/DUMMY_ENTRY_66,
	/*67*/DUMMY_ENTRY_67,
	/*68*/DUMMY_ENTRY_68,
	/*69*/DUMMY_ENTRY_69,
	/*70*/DUMMY_ENTRY_70,
	/*71*/DUMMY_ENTRY_71,
	/*72*/DUMMY_ENTRY_72,
	/*73*/DUMMY_ENTRY_73,
	/*74*/DUMMY_ENTRY_74,
	/*75*/DUMMY_ENTRY_75,
	/*76*/DUMMY_ENTRY_76,
	/*77*/DUMMY_ENTRY_77,
	/*78*/DUMMY_ENTRY_78,
	/*79*/DUMMY_ENTRY_79,
	/*80*/DUMMY_ENTRY_80,
	/*81*/DUMMY_ENTRY_81,
	/*82*/DUMMY_ENTRY_82,
	/*83*/DUMMY_ENTRY_83,
	/*84*/DUMMY_ENTRY_84,
	/*85*/DUMMY_ENTRY_85,
	/*86*/DUMMY_ENTRY_86,
	/*87*/DUMMY_ENTRY_87,

	/*Human Fighter 3th class list*/
	/*88*/DUELIST(ClassType.FIGHTER, Race.HUMAN, GLADIATOR, ClassLevel.THIRD, ClassType2.Warrior),
	/*89*/DREADNOUGHT(ClassType.FIGHTER, Race.HUMAN, WARLORD, ClassLevel.THIRD, ClassType2.Warrior),
	/*90*/PHOENIX_KNIGHT(ClassType.FIGHTER, Race.HUMAN, PALADIN, ClassLevel.THIRD, ClassType2.Knight),
	/*91*/HELL_KNIGHT(ClassType.FIGHTER, Race.HUMAN, DARK_AVENGER, ClassLevel.THIRD, ClassType2.Knight),
	/*92*/SAGITTARIUS(ClassType.FIGHTER, Race.HUMAN, HAWKEYE, ClassLevel.THIRD, ClassType2.Rogue),
	/*93*/ADVENTURER(ClassType.FIGHTER, Race.HUMAN, TREASURE_HUNTER, ClassLevel.THIRD, ClassType2.Rogue),

	/*Human Mage 3th class list*/
	/*94*/ARCHMAGE(ClassType.MYSTIC, Race.HUMAN, SORCERER, ClassLevel.THIRD, ClassType2.Wizard),
	/*95*/SOULTAKER(ClassType.MYSTIC, Race.HUMAN, NECROMANCER, ClassLevel.THIRD, ClassType2.Wizard),
	/*96*/ARCANA_LORD(ClassType.MYSTIC, Race.HUMAN, WARLOCK, ClassLevel.THIRD, ClassType2.Summoner),
	/*97*/CARDINAL(ClassType.PRIEST, Race.HUMAN, BISHOP, ClassLevel.THIRD, ClassType2.Healer),
	/*98*/HIEROPHANT(ClassType.PRIEST, Race.HUMAN, PROPHET, ClassLevel.THIRD, ClassType2.Enchanter),

	/*Elven Fighter 3th class list*/
	/*99*/EVAS_TEMPLAR(ClassType.FIGHTER, Race.ELF, TEMPLE_KNIGHT, ClassLevel.THIRD, ClassType2.Knight),
	/*100*/SWORD_MUSE(ClassType.FIGHTER, Race.ELF, SWORDSINGER, ClassLevel.THIRD, ClassType2.Enchanter),
	/*101*/WIND_RIDER(ClassType.FIGHTER, Race.ELF, PLAIN_WALKER, ClassLevel.THIRD, ClassType2.Rogue),
	/*102*/MOONLIGHT_SENTINEL(ClassType.FIGHTER, Race.ELF, SILVER_RANGER, ClassLevel.THIRD, ClassType2.Rogue),

	/*Elven Mage 3th class list*/
	/*103*/MYSTIC_MUSE(ClassType.MYSTIC, Race.ELF, SPELLSINGER, ClassLevel.THIRD, ClassType2.Wizard),
	/*104*/ELEMENTAL_MASTER(ClassType.MYSTIC, Race.ELF, ELEMENTAL_SUMMONER, ClassLevel.THIRD, ClassType2.Summoner),
	/*105*/EVAS_SAINT(ClassType.PRIEST, Race.ELF, ELDER, ClassLevel.THIRD, ClassType2.Healer),

	/*Darkelf Fighter 3th class list*/
	/*106*/SHILLIEN_TEMPLAR(ClassType.FIGHTER, Race.DARKELF, SHILLEN_KNIGHT, ClassLevel.THIRD, ClassType2.Knight),
	/*107*/SPECTRAL_DANCER(ClassType.FIGHTER, Race.DARKELF, BLADEDANCER, ClassLevel.THIRD, ClassType2.Enchanter),
	/*108*/GHOST_HUNTER(ClassType.FIGHTER, Race.DARKELF, ABYSS_WALKER, ClassLevel.THIRD, ClassType2.Rogue),
	/*109*/GHOST_SENTINEL(ClassType.FIGHTER, Race.DARKELF, PHANTOM_RANGER, ClassLevel.THIRD, ClassType2.Rogue),

	/*Darkelf Mage 3th class list*/
	/*110*/STORM_SCREAMER(ClassType.MYSTIC, Race.DARKELF, SPELLHOWLER, ClassLevel.THIRD, ClassType2.Wizard),
	/*111*/SPECTRAL_MASTER(ClassType.MYSTIC, Race.DARKELF, PHANTOM_SUMMONER, ClassLevel.THIRD, ClassType2.Summoner),
	/*112*/SHILLIEN_SAINT(ClassType.PRIEST, Race.DARKELF, SHILLEN_ELDER, ClassLevel.THIRD, ClassType2.Healer),

	/*Orc Fighter 3th class list*/
	/*113*/TITAN(ClassType.FIGHTER, Race.ORC, DESTROYER, ClassLevel.THIRD, ClassType2.Warrior),
	/*114*/GRAND_KHAVATARI(ClassType.FIGHTER, Race.ORC, TYRANT, ClassLevel.THIRD, ClassType2.Warrior),

	/*Orc Mage 3th class list*/
	/*115*/DOMINATOR(ClassType.MYSTIC, Race.ORC, OVERLORD, ClassLevel.THIRD, ClassType2.Enchanter),
	/*116*/DOOMCRYER(ClassType.MYSTIC, Race.ORC, WARCRYER, ClassLevel.THIRD, ClassType2.Enchanter),

	/*Dwarf Fighter 3th class list*/
	/*117*/FORTUNE_SEEKER(ClassType.FIGHTER, Race.DWARF, BOUNTY_HUNTER, ClassLevel.THIRD, ClassType2.Warrior),
	/*118*/MAESTRO(ClassType.FIGHTER, Race.DWARF, WARSMITH, ClassLevel.THIRD, ClassType2.Warrior),

	/*Dummy Entries*/
	/*119*/DUMMY_ENTRY_119,
	/*120*/DUMMY_ENTRY_120,
	/*121*/DUMMY_ENTRY_121,
	/*122*/DUMMY_ENTRY_122,

	/*Kamael Fighter 1st, 2nd and 3th class list*/
	/*123*/KAMAEL_M_SOLDIER(ClassType.FIGHTER, Race.KAMAEL, null, ClassLevel.NONE, null),
	/*124*/KAMAEL_F_SOLDIER(ClassType.FIGHTER, Race.KAMAEL, null, ClassLevel.NONE, null),
	/*125*/TROOPER(ClassType.FIGHTER, Race.KAMAEL, KAMAEL_M_SOLDIER, ClassLevel.FIRST, null),
	/*126*/WARDER(ClassType.FIGHTER, Race.KAMAEL, KAMAEL_F_SOLDIER, ClassLevel.FIRST, null),
	/*127*/BERSERKER(ClassType.FIGHTER, Race.KAMAEL, TROOPER, ClassLevel.SECOND, ClassType2.Warrior),
	/*128*/M_SOUL_BREAKER(ClassType.FIGHTER, Race.KAMAEL, TROOPER, ClassLevel.SECOND, ClassType2.Warrior),
	/*129*/F_SOUL_BREAKER(ClassType.FIGHTER, Race.KAMAEL, WARDER, ClassLevel.SECOND, ClassType2.Warrior),
	/*130*/ARBALESTER(ClassType.FIGHTER, Race.KAMAEL, WARDER, ClassLevel.SECOND, ClassType2.Rogue),
	/*131*/DOOMBRINGER(ClassType.FIGHTER, Race.KAMAEL, BERSERKER, ClassLevel.THIRD, ClassType2.Warrior),
	/*132*/M_SOUL_HOUND(ClassType.FIGHTER, Race.KAMAEL, M_SOUL_BREAKER, ClassLevel.THIRD, ClassType2.Warrior),
	/*133*/F_SOUL_HOUND(ClassType.FIGHTER, Race.KAMAEL, F_SOUL_BREAKER, ClassLevel.THIRD, ClassType2.Warrior),
	/*134*/TRICKSTER(ClassType.FIGHTER, Race.KAMAEL, ARBALESTER, ClassLevel.THIRD, ClassType2.Rogue),
	/*135*/INSPECTOR(ClassType.FIGHTER, Race.KAMAEL, TROOPER, WARDER, ClassLevel.SECOND, ClassType2.Enchanter),
	/*136*/JUDICATOR(ClassType.FIGHTER, Race.KAMAEL, INSPECTOR, ClassLevel.THIRD, ClassType2.Enchanter);

	public static final ClassId[] VALUES = values();

	public static ClassId valueOf(int id)
	{
		if(id < 0 || id >= VALUES.length)
			return null;

		ClassId result = VALUES[id];
		if(result != null && !result.isDummy())
			return result;

		return null;
	}

	private final Race _race;
	private final ClassId _parent;
	private final ClassId _parent2;
	private final ClassLevel _level;
	private final ClassType _type;
	private final ClassType2 _type2;
	private final boolean _isDummy;

	private ClassId()
	{
		this(null, null, null, null, null, null, true);
	}

	private ClassId(ClassType classType, Race race, ClassId parent, ClassLevel level, ClassType2 classType2)
	{
		this(classType, race, parent, null, level, classType2);
	}

	private ClassId(ClassType classType, Race race, ClassId parent, ClassId parent2, ClassLevel level, ClassType2 classType2)
	{
		this(classType, race, parent, parent2, level, classType2, false);
	}

	private ClassId(ClassType classType, Race race, ClassId parent, ClassId parent2, ClassLevel level, ClassType2 classType2, boolean isDummy)
	{
		_type = classType;
		_race = race;
		_parent = parent;
		_parent2 = parent2;
		_level = level;
		_type2 = classType2;
		_isDummy = isDummy;
	}

	public final int getId()
	{
		return ordinal();
	}

	public final Race getRace()
	{
		return _race;
	}

	public final boolean isOfRace(Race race)
	{
		return _race == race;
	}

	public final ClassLevel getClassLevel()
	{
		return _level;
	}

	public final boolean isOfLevel(ClassLevel level)
	{
		return _level == level;
	}

	public ClassType getType()
	{
		return _type;
	}

	public final boolean isOfType(ClassType type)
	{
		return _type == type;
	}

	public ClassType2 getType2()
	{
		return _type2;
	}

	public boolean isMage()
	{
		return _type.isMagician();
	}

	public final boolean isDummy()
	{
		return _isDummy;
	}

	public boolean isHealer()
	{
		return _type.isHealer();
	}
	
	public final boolean childOf(ClassId cid)
	{
		if(_parent == null)
			return false;

		if(_parent == cid || _parent2 == cid)
			return true;

		return _parent.childOf(cid);

	}

	public final boolean equalsOrChildOf(ClassId cid)
	{
		return this == cid || childOf(cid);
	}

	public final ClassId getParent(int sex)
	{
		return sex == 0 || _parent2 == null ? _parent : _parent2;
	}

	public ClassData getClassData()
	{
		return ClassDataHolder.getInstance().getClassData(getId());
	}

	public double getBaseCp(int level)
	{
		return getClassData().getLvlUpData(level).getCP();
	}

	public double getBaseHp(int level)
	{
		return getClassData().getLvlUpData(level).getHP();
	}

	public double getBaseMp(int level)
	{
		return getClassData().getLvlUpData(level).getMP();
	}

	public final String getName(Player player)
	{
		return new CustomMessage("l2s.gameserver.model.base.ClassId.name." + getId(), player).toString();
	}
}