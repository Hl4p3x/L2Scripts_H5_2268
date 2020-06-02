package l2s.gameserver.templates;

import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;
import l2s.gameserver.templates.player.StatAttributes;

public class CharTemplate
{
	private final static int[] EMPTY_ATTRIBUTES = new int[6];
	
	private final StatAttributes _baseAttr;

	private final int _baseAtkRange;

	private final double _baseHpMax;
	private final double _baseCpMax;
	private final double _baseMpMax;

	/** HP Regen base */
	private final double _baseHpReg;

	/** MP Regen base */
	private final double _baseMpReg;

	/** CP Regen base */
	private final double _baseCpReg;

	private final double _basePAtk;
	private final double _baseMAtk;
	private final double _basePDef;
	private final double _baseMDef;
	private final double _basePAtkSpd;
	private final double _baseMAtkSpd;
	private final double _baseShldDef;
	private final double _baseShldRate;
	private final double _basePCritRate;
	private final double _baseRunSpd;
	private final double _baseWalkSpd;
	private final double _baseWaterRunSpd;
	private final double _baseWaterWalkSpd;

	private final int[] _baseAttributeAttack;
	private final int[] _baseAttributeDefence;
	
	private final double _collisionRadius;
	private final double _collisionHeight;

	private final WeaponType _baseAttackType;

	public CharTemplate(StatsSet set)
	{
		_baseAttr = new StatAttributes(set.getInteger("baseINT", 0), set.getInteger("baseSTR", 0), set.getInteger("baseCON", 0), set.getInteger("baseMEN", 0), set.getInteger("baseDEX", 0), set.getInteger("baseWIT", 0));
		_baseHpMax = set.getDouble("baseHpMax", 0);
		_baseCpMax = set.getDouble("baseCpMax", 0);
		_baseMpMax = set.getDouble("baseMpMax", 0);
		_baseHpReg = set.getDouble("baseHpReg", 0.01);
		_baseCpReg = set.getDouble("baseCpReg", 0.01);
		_baseMpReg = set.getDouble("baseMpReg", 0.01);
		_basePAtk = set.getDouble("basePAtk");
		_baseMAtk = set.getDouble("baseMAtk");
		_basePDef = set.getDouble("basePDef", 0);
		_baseMDef = set.getDouble("baseMDef", 0);
		_basePAtkSpd = set.getDouble("basePAtkSpd");
		_baseMAtkSpd = set.getDouble("baseMAtkSpd", 333);
		_baseShldDef = set.getDouble("baseShldDef", 0);
		_baseAtkRange = set.getInteger("baseAtkRange");
		_baseShldRate = set.getDouble("baseShldRate", 0);
		_basePCritRate = set.getDouble("basePCritRate");
		_baseRunSpd = set.getDouble("baseRunSpd");
		_baseWalkSpd = set.getDouble("baseWalkSpd");
		_baseWaterRunSpd = set.getDouble("baseWaterRunSpd", 50);
		_baseWaterWalkSpd = set.getDouble("baseWaterWalkSpd", 50);
		_baseAttributeAttack = set.getIntegerArray("baseAttributeAttack", EMPTY_ATTRIBUTES);
		_baseAttributeDefence = set.getIntegerArray("baseAttributeDefence", EMPTY_ATTRIBUTES);
		_collisionRadius = set.getDoubleArray("collision_radius", new double[] { 5 })[0];
		_collisionHeight = set.getDoubleArray("collision_height", new double[] { 5 })[0];
		_baseAttackType = WeaponType.valueOf(set.getString("baseAttackType", "FIST").toUpperCase());
	}

	public StatAttributes getBaseAttr()
	{
		return _baseAttr;
	}

	public double getBaseHpMax()
	{
		return _baseHpMax;
	}

	public double getBaseCpMax()
	{
		return _baseCpMax;
	}

	public double getBaseMpMax()
	{
		return _baseMpMax;
	}

	public double getBaseHpReg()
	{
		return _baseHpReg;
	}

	public double getBaseMpReg()
	{
		return _baseMpReg;
	}

	public double getBaseCpReg()
	{
		return _baseCpReg;
	}

	public double getBasePAtk()
	{
		return _basePAtk;
	}

	public double getBaseMAtk()
	{
		return _baseMAtk;
	}

	public double getBasePDef()
	{
		return _basePDef;
	}

	public double getBaseMDef()
	{
		return _baseMDef;
	}

	public double getBasePAtkSpd()
	{
		return _basePAtkSpd;
	}

	public double getBaseMAtkSpd()
	{
		return _baseMAtkSpd;
	}

	public double getBaseShldDef()
	{
		return _baseShldDef;
	}

	public int getBaseAtkRange()
	{
		return _baseAtkRange;
	}

	public double getBaseShldRate()
	{
		return _baseShldRate;
	}

	public double getBasePCritRate()
	{
		return _basePCritRate;
	}

	public double getBaseRunSpd()
	{
		return _baseRunSpd;
	}

	public double getBaseWalkSpd()
	{
		return _baseWalkSpd;
	}

	public double getBaseWaterRunSpd()
	{
		return _baseWaterRunSpd;
	}

	public double getBaseWaterWalkSpd()
	{
		return _baseWaterWalkSpd;
	}

	public int[] getBaseAttributeAttack()
	{
		return _baseAttributeAttack;
	}

	public int[] getBaseAttributeDefence()
	{
		return _baseAttributeDefence;
	}

	public double getCollisionRadius()
	{
		return _collisionRadius;
	}

	public double getCollisionHeight()
	{
		return _collisionHeight;
	}

	public WeaponType getBaseAttackType()
	{
		return _baseAttackType;
	}

	public int getNpcId()
	{
		return 0;
	}

	public static StatsSet getEmptyStatsSet()
	{
		StatsSet npcDat = new StatsSet();
		npcDat.set("baseSTR", 0);
		npcDat.set("baseCON", 0);
		npcDat.set("baseDEX", 0);
		npcDat.set("baseINT", 0);
		npcDat.set("baseWIT", 0);
		npcDat.set("baseMEN", 0);
		npcDat.set("baseHpMax", 0);
		npcDat.set("baseCpMax", 0);
		npcDat.set("baseMpMax", 0);
		npcDat.set("baseHpReg", 3.e-3f);
		npcDat.set("baseCpReg", 0);
		npcDat.set("baseMpReg", 3.e-3f);
		npcDat.set("basePAtk", 0);
		npcDat.set("baseMAtk", 0);
		npcDat.set("basePDef", 100);
		npcDat.set("baseMDef", 100);
		npcDat.set("basePAtkSpd", 0);
		npcDat.set("baseMAtkSpd", 0);
		npcDat.set("baseShldDef", 0);
		npcDat.set("baseAtkRange", 0);
		npcDat.set("baseShldRate", 0);
		npcDat.set("basePCritRate", 0);
		npcDat.set("baseRunSpd", 0);
		npcDat.set("baseWalkSpd", 0);
		return npcDat;
	}
}