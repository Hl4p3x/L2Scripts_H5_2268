package l2s.gameserver.data.xml.parser;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.dom4j.Element;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.EnchantStoneHolder;
import l2s.gameserver.templates.item.ItemTemplate.Grade;
import l2s.gameserver.templates.item.support.EnchantStone;
import l2s.gameserver.templates.item.support.EnchantType;
import l2s.gameserver.templates.item.support.FailResultType;

/**
 * @author Bonux
**/
public class EnchantStoneParser extends AbstractParser<EnchantStoneHolder>
{
	private static EnchantStoneParser _instance = new EnchantStoneParser();

	public static EnchantStoneParser getInstance()
	{
		return _instance;
	}

	private EnchantStoneParser()
	{
		super(EnchantStoneHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/enchant_stones.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "enchant_stones.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		int defaultMinEnchantLevel = 0;
		int defaultMinFullbodyEnchantLevel = 0;
		int defaultMaxEnchantLevel = 0;
		FailResultType defaultResultType  = FailResultType.CRYSTALS;

		Element defaultElement = rootElement.element("default");
		if(defaultElement != null)
		{
			defaultResultType = FailResultType.valueOf(defaultElement.attributeValue("on_fail"));
			defaultMinEnchantLevel = Integer.parseInt(defaultElement.attributeValue("min_enchant_level"));
			defaultMinFullbodyEnchantLevel = Integer.parseInt(defaultElement.attributeValue("min_fullbody_enchant_level"));
			defaultMaxEnchantLevel = Integer.parseInt(defaultElement.attributeValue("max_enchant_level"));
		}

		for(Iterator<Element> iterator = rootElement.elementIterator("enchant_stone"); iterator.hasNext();)
		{
			Element enchantStoneElement = iterator.next();
			final int itemId = Integer.parseInt(enchantStoneElement.attributeValue("id"));
			final double chance = Integer.parseInt(enchantStoneElement.attributeValue("chance"));

			final Set<Grade> gradesSet = new HashSet<Grade>();
			final String[] grades = enchantStoneElement.attributeValue("grade") == null ? new String[]{ "NONE" } : enchantStoneElement.attributeValue("grade").split(";");
			for(String grade : grades)
				gradesSet.add(Grade.valueOf(grade.toUpperCase()));

			final EnchantType type = enchantStoneElement.attributeValue("type") == null ? EnchantType.ALL : EnchantType.valueOf(enchantStoneElement.attributeValue("type"));
			final FailResultType resultType = enchantStoneElement.attributeValue("on_fail") == null ? defaultResultType : FailResultType.valueOf(enchantStoneElement.attributeValue("on_fail"));
			final int enchantDropCount = enchantStoneElement.attributeValue("enchant_drop_count") == null ? Integer.MAX_VALUE : Integer.parseInt(enchantStoneElement.attributeValue("enchant_drop_count"));
			final int minEnchantLevel = enchantStoneElement.attributeValue("min_enchant_level") == null ? defaultMinEnchantLevel : Integer.parseInt(enchantStoneElement.attributeValue("min_enchant_level"));
			final int minFullbodyEnchantLevel = enchantStoneElement.attributeValue("min_fullbody_enchant_level") == null ? Math.max(minEnchantLevel, defaultMinFullbodyEnchantLevel) : Integer.parseInt(enchantStoneElement.attributeValue("min_fullbody_enchant_level"));
			final int maxEnchantLevel = enchantStoneElement.attributeValue("max_enchant_level") == null ? defaultMaxEnchantLevel : Integer.parseInt(enchantStoneElement.attributeValue("max_enchant_level"));
			final int minEnchantStep = enchantStoneElement.attributeValue("min_enchant_step") == null ? 1 : Integer.parseInt(enchantStoneElement.attributeValue("min_enchant_step"));
			final int maxEnchantStep = enchantStoneElement.attributeValue("max_enchant_step") == null ? 1 : Integer.parseInt(enchantStoneElement.attributeValue("max_enchant_step"));

			getHolder().addEnchantStone(new EnchantStone(itemId, chance, type, gradesSet, resultType, enchantDropCount, minEnchantLevel, minFullbodyEnchantLevel, maxEnchantLevel, minEnchantStep, maxEnchantStep));
		}
	}
}
