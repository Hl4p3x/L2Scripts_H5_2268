package l2s.gameserver.data.xml.holder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.napile.primitive.Containers;
import org.napile.primitive.lists.IntList;
import org.napile.primitive.lists.impl.ArrayIntList;

import l2s.commons.data.xml.AbstractHolder;
import l2s.commons.util.Rnd;
import l2s.gameserver.model.Player;
import l2s.gameserver.templates.item.ArmorTemplate;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.ItemTemplate.Grade;
import l2s.gameserver.templates.item.WeaponTemplate;

/**
 LIO 
 29.01.2016
 */
public class FakeItemHolder extends AbstractHolder
{
	private static class ClassWeaponAndArmor
	{
		private final int classId;
		private final List<WeaponTemplate.WeaponType> weaponTypes = new ArrayList<>();
		private final List<ArmorTemplate.ArmorType> armorTypes = new ArrayList<>();

		public ClassWeaponAndArmor(int classId, String weaponTypes, String armorTypes)
		{
			this.classId = classId;
			for(String s : weaponTypes.split(";"))
			{
				this.weaponTypes.add(WeaponTemplate.WeaponType.valueOf(s));
			}
			for(String s : armorTypes.split(";"))
			{
				this.armorTypes.add(ArmorTemplate.ArmorType.valueOf(s));
			}
		}

		public WeaponTemplate.WeaponType getRandomWeaponType()
		{
			return weaponTypes.get(Rnd.get(weaponTypes.size()));
		}

		public ArmorTemplate.ArmorType getRandomArmorType()
		{
			return armorTypes.get(Rnd.get(armorTypes.size()));
		}
	}

	private static FakeItemHolder ourInstance = new FakeItemHolder();

	public static FakeItemHolder getInstance()
	{
		return ourInstance;
	}

	private final Map<Grade, Map<WeaponTemplate.WeaponType, IntList>> weapons = new HashMap<>();
	private final Map<Grade, Map<ArmorTemplate.ArmorType, IntList>> armors = new HashMap<>();
	private final Map<Grade, List<IntList>> accessorys = new HashMap<>();
	private final Map<Integer, ClassWeaponAndArmor> classWeaponAndArmors = new HashMap<>();
	private final IntList _hairAccessories = new ArrayIntList();
	private final IntList _cloaks = new ArrayIntList();

	public void addWeapons(Grade grade, IntList list)
	{
		Map<WeaponTemplate.WeaponType, IntList> map = new HashMap<>();
		for(int itemId : list.toArray())
		{
			ItemTemplate template = ItemHolder.getInstance().getTemplate(itemId);
			if(template == null || !template.isWeapon())
			{
				//TODO Log
				continue;
			}
			WeaponTemplate.WeaponType weaponType = ((WeaponTemplate) template).getItemType();
			if(template.isMagicWeapon())
			{
				weaponType = WeaponTemplate.WeaponType.MAGIC;
			}
			if(map.get(weaponType) == null)
			{
				map.put(weaponType, new ArrayIntList());
			}
			map.get(weaponType).add(itemId);
		}
		weapons.put(grade, map);
	}

	public void addArmors(Grade grade, Map<ArmorTemplate.ArmorType, IntList> map)
	{
		armors.put(grade, map);
	}

	public void addAccessorys(Grade grade, List<IntList> list)
	{
		accessorys.put(grade, list);
	}

	public void addClassWeaponAndArmors(int classId, String weaponTypes, String armorTypes)
	{
		classWeaponAndArmors.put(classId, new ClassWeaponAndArmor(classId, weaponTypes, armorTypes));
	}

	public void addHairAccessories(IntList list)
	{
		_hairAccessories.addAll(list);
	}

	public IntList getHairAccessories()
	{
		return _hairAccessories;
	}

	public void addCloaks(IntList list)
	{
		_cloaks.addAll(list);
	}

	public IntList getCloaks()
	{
		return _cloaks;
	}

	public IntList getRandomItems(Player player, String type, int expertiseIndex)
	{
		Grade grade = Grade.values()[expertiseIndex];

		switch(type)
		{
			case "Accessory":
			{
				List<IntList> packs = accessorys.get(grade);
				return packs.get(Rnd.get(packs.size()));
			}
			case "Armor":
			{
				try
				{
					ClassWeaponAndArmor classWeaponAndArmor = classWeaponAndArmors.get(player.getClassId().getId());
					return armors.get(grade).get(classWeaponAndArmor.getRandomArmorType());
				}
				catch(Exception e)
				{
					System.out.println(player.getClassId().getId());
				}
				break;
			}
			case "Weapon":
			{
				ClassWeaponAndArmor classWeaponAndArmor = classWeaponAndArmors.get(player.getClassId().getId());
				IntList weaponIds = new ArrayIntList();
				while(weaponIds.isEmpty())
				{
					IntList list = weapons.get(grade).get(classWeaponAndArmor.getRandomWeaponType());
					if(list != null)
					{
						weaponIds.add(list.get(list.size() - 1));
					}
				}
				return weaponIds;
			}
		}
		return Containers.EMPTY_INT_LIST;
	}

	@Override
	public void log()
	{
		info("loaded fake items.");
	}

	@Override
	public int size()
	{
		return 0;
	}

	@Override
	public void clear()
	{
		//
	}
}