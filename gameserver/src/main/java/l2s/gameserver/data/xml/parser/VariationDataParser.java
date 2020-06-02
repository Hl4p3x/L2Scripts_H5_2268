package l2s.gameserver.data.xml.parser;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;

import org.dom4j.Element;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import l2s.commons.data.xml.AbstractParser;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.OptionDataHolder;
import l2s.gameserver.data.xml.holder.VariationDataHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.templates.item.WeaponFightType;
import l2s.gameserver.templates.item.support.variation.VariationCategory;
import l2s.gameserver.templates.item.support.variation.VariationFee;
import l2s.gameserver.templates.item.support.variation.VariationGroup;
import l2s.gameserver.templates.item.support.variation.VariationInfo;
import l2s.gameserver.templates.item.support.variation.VariationOption;
import l2s.gameserver.templates.item.support.variation.VariationStone;

/**
 * @author Bonux
 */
public final class VariationDataParser extends AbstractParser<VariationDataHolder>
{
	private static VariationDataParser _instance = new VariationDataParser();

	public static VariationDataParser getInstance()
	{
		return _instance;
	}

	private VariationDataParser()
	{
		super(VariationDataHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/variationdata/");
	}

	@Override
	public boolean isIgnored(File f)
	{
		return false;
	}

	@Override
	public String getDTDFileName()
	{
		return "variationdata.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> iterator = rootElement.elementIterator("weapon"); iterator.hasNext();)
		{
			Element element = iterator.next();

			WeaponFightType type = WeaponFightType.valueOf(element.attributeValue("type").toUpperCase());

			for(Iterator<Element> stoneIterator = element.elementIterator("stone"); stoneIterator.hasNext();)
			{
				Element stoneElement = stoneIterator.next();

				int stoneId = Integer.parseInt(stoneElement.attributeValue("id"));
				VariationStone stone = new VariationStone(stoneId);

				for(Iterator<Element> variationIterator = stoneElement.elementIterator("variation"); variationIterator.hasNext();)
				{
					Element variationElement = variationIterator.next();

					int variationId = Integer.parseInt(variationElement.attributeValue("id"));
					VariationInfo variation = new VariationInfo(variationId);

					for(Iterator<Element> categoryIterator = variationElement.elementIterator("category"); categoryIterator.hasNext();)
					{
						Element categoryElement = categoryIterator.next();

						double probability = Double.parseDouble(categoryElement.attributeValue("probability"));
						VariationCategory category = new VariationCategory(probability);

						for(Iterator<Element> optionIterator = categoryElement.elementIterator("option"); optionIterator.hasNext();)
						{
							Element optionElement = optionIterator.next();

							int optionId = Integer.parseInt(optionElement.attributeValue("id"));
							if(OptionDataHolder.getInstance().getTemplate(optionId) == null)
							{
								warn("Cannot find option ID: " + optionId + " for variation ID: " + variationId);
								continue;
							}

							double chance = Double.parseDouble(optionElement.attributeValue("chance"));

							category.addOption(new VariationOption(optionId, chance));
						}

						variation.addCategory(category);
					}

					stone.addVariation(variation);
				}

				getHolder().addStone(type, stone);
			}
		}

		for(Iterator<Element> iterator = rootElement.elementIterator("group"); iterator.hasNext();)
		{
			Element element = iterator.next();

			int groupId = Integer.parseInt(element.attributeValue("id"));
			VariationGroup group = new VariationGroup(groupId);

			for(Iterator<Element> feeIterator = element.elementIterator("fee"); feeIterator.hasNext();)
			{
				Element feeElement = feeIterator.next();

				int stoneId = Integer.parseInt(feeElement.attributeValue("stone_id"));
				int feeItemId = Integer.parseInt(feeElement.attributeValue("fee_item_id"));
				long feeItemCount = Long.parseLong(feeElement.attributeValue("fee_item_count"));
				long cancelFee = Long.parseLong(feeElement.attributeValue("cancel_fee"));

				group.addFee(new VariationFee(stoneId, feeItemId, feeItemCount, cancelFee));

			}
			getHolder().addGroup(group);
		}
	}

	@Override
	protected void afterParseActions()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT augmentation_id FROM items LIMIT 1;");
			statement.executeQuery();
		}
		catch(Exception e)
		{
			return;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}

		final TIntIntMap augmentations = new TIntIntHashMap();

		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT object_id, augmentation_id FROM items WHERE augmentation_id != 0;");
			rset = statement.executeQuery();
			while(rset.next())
				augmentations.put(rset.getInt("object_id"), rset.getInt("augmentation_id"));
		}
		catch(Exception e)
		{
			_log.error("VariationDataParser.afterParseActions():" + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE items SET augmentation_id = 0;");
			statement.executeUpdate();
			DbUtils.closeQuietly(statement);

			statement = con.prepareStatement("ALTER TABLE items CHANGE `augmentation_id` `variation_stone_id` int(7) NOT NULL;");
			statement.execute();
			DbUtils.closeQuietly(statement);

			statement = con.prepareStatement("ALTER TABLE items ADD COLUMN `variation1_id` int(7) NOT NULL AFTER `variation_stone_id`;");
			statement.execute();
			DbUtils.closeQuietly(statement);

			statement = con.prepareStatement("UPDATE items SET variation1_id = 0;");
			statement.executeUpdate();
			DbUtils.closeQuietly(statement);

			statement = con.prepareStatement("ALTER TABLE items ADD COLUMN `variation2_id` int(7) NOT NULL AFTER `variation1_id`;");
			statement.execute();
			DbUtils.closeQuietly(statement);

			statement = con.prepareStatement("UPDATE items SET variation2_id = 0;");
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.error("VariationDataParser.afterParseActions():" + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		if(augmentations.isEmpty())
			return;

		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			for(TIntIntIterator iterator = augmentations.iterator(); iterator.hasNext();)
			{
				iterator.advance();

				int variation1 = 0x0000FFFF & iterator.value();
				int variation2 = iterator.value() >> 16;

				statement = con.prepareStatement("UPDATE items SET variation_stone_id=?, variation1_id=?, variation2_id=? WHERE object_id=?");
				statement.setInt(1, 8723);
				statement.setInt(2, variation1);
				statement.setInt(3, variation2);
				statement.setInt(4, iterator.key());
				statement.executeUpdate();
				DbUtils.closeQuietly(statement);
			}
		}
		catch(Exception e)
		{
			_log.error("VariationDataParser.afterParseActions():" + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
}
