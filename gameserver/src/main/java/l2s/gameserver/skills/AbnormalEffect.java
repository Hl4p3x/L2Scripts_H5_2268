package l2s.gameserver.skills;

import java.util.NoSuchElementException;

public enum AbnormalEffect
{
	/*0*/NONE("null", 0x0),
	/*1*/DOT_BLEEDING("bleeding", 0x000001),
	/*2*/DOT_POISON("poison", 0x000002),
	/*3*/DOT_FIRE("redcircle", 0x000004),
	/*4*/DOT_WATER("ice", 0x000008),
	/*5*/DOT_WIND("affraid", 0x000010),
	/*6*/DOT_SOIL("confused", 0x000020),
	/*7*/STUN("stun", 0x000040),
	/*8*/SLEEP("sleep", 0x000080),
	/*9*/SILENCE("muted", 0x000100),
	/*10*/ROOT("root", 0x000200),
	/*11*/PARALYZE("hold1", 0x000400),
	/*12*/FLESH_STONE("hold2", 0x000800),
	/*13*/DOT_MP("unk13", 0x001000),
	/*14*/BIG_HEAD("bighead", 0x002000),
	/*15*/DOT_FIRE_AREA("flame", 0x004000),
	/*16*/CHANGE_TEXTURE("change_texture", 0x008000),
	/*17*/BIG_BODY("grow", 0x010000),
	/*18*/FLOATING_ROOT("floatroot", 0x020000),
	/*19*/DANCE_ROOT("dancestun", 0x040000),
	/*20*/GHOST_STUN("firerootstun", 0x080000),
	/*21*/STEALTH("shadow", 0x100000),
	/*22*/SEIZURE1("imprison1", 0x200000),
	/*23*/SEIZURE2("imprison2", 0x400000),
	/*24*/MAGIC_SQUARE("magiccircle", 0x800000),
	/*25*/FREEZING("ice2", 0x1000000),
	/*26*/SHAKE("earthquake", 0x2000000),
	/*27*/UNK_27("unk27", 0x4000000),
	/*28*/ULTIMATE_DEFENCE("invul1", 0x8000000),
	/*29*/VP_UP("vitality", 0x10000000),
	/*30*/REAL_TARGET("realtarget", 0x20000000),
	/*31*/DEATH_MARK("deathmark", 0x40000000),
	/*32*/TURN_FLEE("soulshock", 0x80000000),
	/*33*/INVINCIBILITY("invul2", 0x000001, true),
	/*34*/AIR_BATTLE_SLOW("redglow", 0x000002, true), // Мигает красный туманный шар вокруг живота персонажа.
	/*35*/AIR_BATTLE_ROOT("redglow2", 0x000004, true),  // Мигает красный туманный шар вокруг живота персонажа.
	/*36*/CHANGE_WP("baguettesword", 0x000008, true), // Багет вместо оружия.
	/*37*/CHANGE_HAIR_G("yellowafro", 0x000010, true), // Золотая афро-прическа.
	/*38*/CHANGE_HAIR_P("pinkafro", 0x000020, true), // Розовая афро-прическа.
	/*39*/CHANGE_HAIR_B("blackafro", 0x000040, true), // Черная афро-прическа.
	/*40*/UNK_40("sunk8", 0x000080, true), // unk
	/*41*/STIGMA_OF_SILEN("stigma", 0x000100, true),
	/*42*/SPEED_DOWN("stakatoroot", 0x000200, true), // Зеленые и белые линии запутуются в ногах.
	/*43*/FROZEN_PILLAR("frozenpillar", 0x000400, true),
	/*44*/CHANGE_VES_S("vesper_s", 0x000800, true), // Переодевает персонажа в золотой веспер.
	/*45*/CHANGE_VES_C("vesper_c", 0x001000, true), // Переодевает персонажа в золотой веспер.
	/*46*/CHANGE_VES_D("vesper_d", 0x002000, true), // Переодевает персонажа в белый веспер.
	/*47*/TIME_BOMB("soa_respawn", 0x004000, true), // Зеленый круговой дождь в тумане над головой персонажа.
	/*48*/MP_SHIELD("arcane_invul", 0x008000, true),
	/*49*/AIRBIND("AIRBIND", 0x010000, true),
	/*50*/CHANGEBODY("CHANGEBODY", 0x020000, true),
	/*51*/KNOCKDOWN("KNOCKDOWN", 0x040000, true),
	/*52*/NAVIT_ADVENT("nevit_system", 0x080000, true),
	/*53*/KNOCKBACK("KNOCKBACK", 0x100000, true),
	/*54*/CHANGE_7ANNIVERSARY("CHANGE_7ANNIVERSARY", 0x200000, true),
	/*55*/ON_SPOT_MOVEMENT, //unk
	/*56*/DEPORT, // Закрывает персонажа в черной туманной клетке.
	/*57*/AURA_BUFF, // С персонажа идет зеленый дым и сверкает живот.
	/*58*/AURA_BUFF_SELF, // С персонажа идет бело-желтый дым, вокруг живота идут вспышки кольцами градиентного цвета и вокруг живота летают белые кружочки. Также персонаж стоит на красном круге.
	/*59*/AURA_DEBUFF, // С персонажа мигает красный дым и сверкает живот красными полосами.
	/*60*/AURA_DEBUFF_SELF, // Персонажа окутывают красные спиральные полосы и мигает не сильно красный дым. (При активации происходит красная вспышка)
	/*61*/HURRICANE, // (При активации происходит бело-желтая вспышка)
	/*62*/HURRICANE_SELF, // Вокруг персонажа песчаная буря.
	/*63*/BLACK_MARK, // Над головой персонажа морда красного черепа.
	/*64*/BR_SOUL_AVATAR,
	/*65*/CHANGE_GRADE_B, // Переодевает персонажа в Б грейд.
	/*66*/BR_BEAM_SWORD_ONEHAND,
	/*67*/BR_BEAM_SWORD_DUAL,
	/*68*/D_NOCHAT,
	/*69*/D_HERB_POWER,
	/*70*/D_HERB_MAGIC,
	/*71*/D_TALI_DECO_P,
	/*72*/UNK_72,
	/*73*/D_TALI_DECO_C, // Рука светится желтым.
	/*74*/D_TALI_DECO_D, // Рука светится красным.
	/*75*/D_TALI_DECO_E, // Рука светится синим.
	/*76*/D_TALI_DECO_F, // Рука светится Фиолетовым.
	/*77*/D_TALI_DECO_G, // Переодевает в Топ S80, одевает плащ и диадему.
	/*78*/D_CHANGESHAPE_TRANSFORM_1, // Переодевает в NG.
	/*79*/D_CHANGESHAPE_TRANSFORM_2, // Переодевает в D.
	/*80*/D_CHANGESHAPE_TRANSFORM_3, // Переодевает в C.
	/*81*/D_CHANGESHAPE_TRANSFORM_4, // Переодевает в B.
	/*82*/D_CHANGESHAPE_TRANSFORM_5, // Переодевает в A.
	/*83*/UNK_83, // У артеас изчезает туловище, на остальных не проверялось.
	/*84*/UNK_84, // У артеас изчезает туловище, на остальных не проверялось.
	/*85*/SANTA_SUIT, // Переодевает в костюм деда мороза.
	/*86*/UNK_86, // Возле персонажа летает игральная карта.
	/*87*/UNK_87, // Переодевает в бейсбольную форму.
	/*88*/UNK_88, // Оружие персонажа превращается в свадебный букет.
	/*89*/UNK_89,	// Возле персонажа летает леденец.
	/*90*/UNK_90,	// Возле персонажа летает конфетка.
	/*91*/UNK_91,	// Возле персонажа летает печенька.
	/*92*/EMPTY_STARS,	// Над персонажом засвечивается 5 пустых звездочек.
	/*93*/ONE_STAR,	// Над персонажом засвечивается 1я звездочка.
	/*94*/TWO_STARS,	// Над персонажом засвечивается 2я звездочка.
	/*95*/THREE_STARS,	// Над персонажом засвечивается 3я звездочка.
	/*96*/FOUR_STARS,	// Над персонажом засвечивается 4я звездочка.
	/*97*/FIVE_STARS,	// Над персонажом засвечивается 5я звездочка.
	/*98*/FACEOFF,	// Песронаж стоит в бардовом круге и над головой мигают скрещенные 2 меча.
	/*99*/UNK_99,	// Под персонажем земля пытается замерзнуть и выростает перед ногами небольшая льдинка.
	/*100*/UNK_100,	// Переодевает персонажа в бронь робокопа.
	/*101*/UNK_101,	// Возле персонажа летает голова посоха Мастера Йоды.
	/*102*/UNK_102,	// Возле персонажа летает желтая нота.
	/*103*/UNK_103,	// Возле персонажа летает синяя нота.
	/*104*/UNK_104,	// Возле персонажа летает зеленая нота.
	/*105*/UNK_105,	// Возле персонажа летает лого Lineage II.
	/*106*/STOCKING_FAIRY,	// Возле персонажа летает картинка носка.
	/*107*/TREE_FAIRY,	// Возле персонажа летает картинка елки.
	/*108*/SNOWMAN_FAIRY,	// Возле персонажа летает картинка снеговика.
	/*109*/UNK_109,	// Над головой персонажа крутятся бардовые спиральки.
	/*110*/UNK_110,	// Вокруг персонажа желтый круг и персонаж стоит в красном круге.
	/*111*/UNK_111,	// Вокруг персонажа образовывается бардовая стена с синими бликами.
	/*112*/UNK_112,	// Вокруг персонажа крутятся зеленые круги в сверичном виде.
	/*113*/UNK_113,	// Над головой светится красное око.
	/*114*/STIGMA_STORM,	// Над головой светится зеленое око.
	/*115*/GREEN_SPEED_UP,	// Персонаж светиться зеленым и при беге подпрыгивает.
	/*116*/RED_SPEED_UP,	// Персонаж светиться красным и при беге подпрыгивает.
	/*117*/WIND_PROTECTION,	// Персонаж светиться в зеленый и стоит с транной агрессивной стойке.
	/*118*/LOVE,	// Начинает летать сердце над головой.
	/*119*/PERFECT_STORM,	// На уровне пояса синенький маленький круговорот воздуха.
	/*120*/UNK_120,	// Светиться синим и вокруг небольшой смерчь с листьями.
	/*121*/UNK_121,	// Темнеет персонаж с красным бликом.
	/*122*/UNK_122,
	/*123*/GREAT_GRAVITY,	// Темнеет персонаж с красной димной сверой в районе живота.
	/*124*/STEEL_MIND,	// Каждая нога окутывается зелеными полосами с звездочками и при беге подпрыгивает персонаж.
	/*125*/UNK_125,	// Вокруг персонажа ниже пояса летает черная и голубая цепь. Происходят синие вспышки.
	/*126*/OBLATE,	// Персонаж становится 2D. Сплюснутый))
	/*127*/SPALLATION,	// Персонаж оказывается в средине большого зеленого бликующего круга.
	/*128*/U_HE_ASPECT_AVE,	// С персонажа идет черный дым.
	/*129*/UNK_129,	// У артеас изчезает туловище, на остальных не проверялось.
	/*130*/UNK_130,	// У артеас изчезает туловище, на остальных не проверялось.
	/*131*/UNK_131,	// У артеас изчезает туловище, на остальных не проверялось.
	/*132*/UNK_132,	// Переодевает персонажа в бронь робокопа.
	/*133*/UNK_133,	// У артеас пропадает все тело, кроме головы, на остальных не проверялось.
	/*134*/UNK_134,	// У артеас пропадает все тело, кроме головы, на остальных не проверялось.
	/*135*/UNK_135,	// Оружие меняет на лазерное.
	/*136*/UNK_136,	// Оружие меняет на японское.
	/*137*/UNK_137,
	/*138*/UNK_138,
	/*139*/UNK_139,
	/*140*/UNK_140,
	/*141*/U_AVE_PALADIN_DEF,	// Вокруг персонажа крутятся светащиеся оранжевые щиты.
	/*142*/U_AVE_GUARDIAN_DEF,	// Вокруг персонажа крутятся светащиеся синие щиты.
	/*143*/U_REALTAR2_AVE,	// Над головой мигает красно-оранжевый оберег в виде тризуба.
	/*144*/U_AVE_DIVINITY,	// Персонажа переливается как эффект заточенной брони.
	/*145*/U_AVE_SHILPROTECTION,	// Вокруг персонажа образовывается красная сфера состоящая из шестиугольников.
	/*146*/U_EVENT_STAR_CA,	// Над персонажем мигают 5 звезд.
	/*147*/U_EVENT_STAR1_TA,	// Над персонажем появляется 1 звезда.
	/*148*/U_EVENT_STAR2_TA,	// Над персонажем появляется 2 звезда.
	/*149*/U_EVENT_STAR3_TA,	// Над персонажем появляется 3 звезда.
	/*150*/U_EVENT_STAR4_TA,	// Над персонажем появляется 4 звезда.
	/*151*/U_EVENT_STAR5_TA,	// Над персонажем появляется 5 звезда.
	/*152*/U_AVE_ABSORB_SHIELD,	// Вокруг персонажа образовывается белая сфера состоящая из шестиугольников.
	/*153*/U_KN_PHOENIX_AURA,	// Перед персонажем образовывается сферичный крест белый и исчезает.
	/*154*/U_KN_REVENGE_AURA,	// Над персоажем происходит фиолетовая дымовая вспышка и исчезает.
	/*155*/U_KN_EVAS_AURA,	// Из тела персонажа идет синий дымок, но в начале крутятся вокруг него синий цилиндр из шестиугольников.
	/*156*/U_KN_REMPLA_AURA,	// Из тела персонажа идет синий дымок, но в начале над ним появляются мечи.
	/*157*/U_AVE_LONGBOW,	// На оружии происходит белая вспышка и исчезает.
	/*158*/U_AVE_WIDESWORD,	// Оружие цилиндрично светиться радужными цветами.
	/*159*/U_AVE_BIGFIST,	// Кастеты светятся почти как геройские.
	/*160*/U_AVE_SHADOWSTEP,
	/*161*/U_TORNADO_AVE,
	/*162*/U_AVE_SNOW_SLOW,
	/*163*/U_AVE_SNOW_HOLD,
	/*164*/UNK_164,
	/*165*/U_AVE_TORNADO_SLOW,
	/*166*/U_AVE_ASTATINE_WATER,
	/*167*/U_BIGBD_CAT_NPC,
	/*168*/U_BIGBD_UNICORN_NPC,
	/*169*/U_BIGBD_DEMON_NPC,
	/*170*/U_BIGBD_CAT_PC,
	/*171*/U_BIGBD_UNICORN_PC,
	/*172*/U_BIGBD_DEMON_PC,
	/*173*/U_AVE_DRAGON_ULTIMATE(700),

	//TODO: Подобрать.
	/*174*/BR_POWER_OF_EVA(0),
	/*175*/VP_KEEP(29),

	/*176*/UNK_176,
	/*177*/UNK_177,
	/*178*/UNK_178,
	/*179*/UNK_179,
	/*180*/UNK_180,
	/*181*/UNK_181,
	/*182*/UNK_182,
	/*183*/UNK_183,

	// event effects
	/*184*/E_AFRO_1(/*TODO:*/37, "afrobaguette1", 0x000001, false, true),
	/*185*/E_AFRO_2(/*TODO:*/38, "afrobaguette2", 0x000002, false, true),
	/*186*/E_AFRO_3(/*TODO:*/39, "afrobaguette3", 0x000004, false, true),
	/*187*/E_EVASWRATH(/*TODO:*/0, "evaswrath", 0x000008, false, true),
	/*188*/E_HEADPHONE(/*TODO:*/0, "headphone", 0x000010, false, true),
	/*189*/E_VESPER_1(/*TODO:*/44, "vesper1", 0x000020, false, true),
	/*190*/E_VESPER_2(/*TODO:*/45, "vesper2", 0x000040, false, true),
	/*191*/E_VESPER_3(/*TODO:*/46, "vesper3", 0x000080, false, true);

	public static final AbnormalEffect[] VALUES = values();

	private final int _id;
	private final int _mask;
	private final String _name;
	private final boolean _special;
	private final boolean _event;

	private AbnormalEffect()
	{
		_id = ordinal();
		_name = toString();
		_mask = 0x0;
		_special = false;
		_event = false;
	}

	private AbnormalEffect(int id)
	{
		_id = id;
		_name = toString();
		_mask = 0x0;
		_special = false;
		_event = false;
	}

	private AbnormalEffect(String name, int mask)
	{
		_id = ordinal();
		_name = name;
		_mask = mask;
		_special = false;
		_event = false;
	}

	private AbnormalEffect(String name, int mask, boolean special)
	{
		_id = ordinal();
		_name = name;
		_mask = mask;
		_special = special;
		_event = false;
	}

	private AbnormalEffect(int id, String name, int mask, boolean special, boolean event)
	{
		_id = id;
		_name = name;
		_mask = mask;
		_special = special;
		_event = event;
	}

	public final int getId()
	{
		return _id;
	}

	public final int getMask()
	{
		return _mask;
	}

	public final String getName()
	{
		return _name;
	}

	public final boolean isSpecial()
	{
		return _special;
	}

	public final boolean isEvent()
	{
		return _event;
	}

	public static AbnormalEffect getByName(String name)
	{
		for(AbnormalEffect eff : AbnormalEffect.VALUES)
		{
			if(eff.getName().equalsIgnoreCase(name))
				return eff;
		}

		for(AbnormalEffect eff : AbnormalEffect.VALUES)
		{
			if(eff.toString().equalsIgnoreCase(name))
				return eff;
		}
		throw new NoSuchElementException("AbnormalEffect not found for name: '" + name + "'.\n Please check " + AbnormalEffect.class.getCanonicalName());
	}
}