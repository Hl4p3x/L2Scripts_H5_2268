package l2s.gameserver.data.xml.parser;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.dom4j.Element;

import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SkillAcquireHolder;
import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;

/**
 * @author: VISTALL
 * @date:  20:55/30.11.2010
 */
public final class SkillAcquireParser extends AbstractParser<SkillAcquireHolder>
{
	private static final SkillAcquireParser _instance = new SkillAcquireParser();

	public static SkillAcquireParser getInstance()
	{
		return _instance;
	}

	protected SkillAcquireParser()
	{
		super(SkillAcquireHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/skill_tree/");
	}

	@Override
	public boolean isIgnored(File b)
	{
		return false;
	}

	@Override
	public String getDTDFileName()
	{
		return "tree.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> iterator = rootElement.elementIterator("certification_skill_tree"); iterator.hasNext();)
			getHolder().addAllCertificationLearns(parseSkillLearn(iterator.next()));

		for(Iterator<Element> iterator = rootElement.elementIterator("sub_unit_skill_tree"); iterator.hasNext();)
			getHolder().addAllSubUnitLearns(parseSkillLearn(iterator.next()));

		for(Iterator<Element> iterator = rootElement.elementIterator("pledge_skill_tree"); iterator.hasNext();)
			getHolder().addAllPledgeLearns(parseSkillLearn(iterator.next()));

		for(Iterator<Element> iterator = rootElement.elementIterator("collection_skill_tree"); iterator.hasNext();)
			getHolder().addAllCollectionLearns(parseSkillLearn(iterator.next()));

		for(Iterator<Element> iterator = rootElement.elementIterator("gm_skill_tree"); iterator.hasNext();)
			getHolder().addAllGMLearns(parseSkillLearn(iterator.next()));

		for(Iterator<Element> iterator = rootElement.elementIterator("fishing_skill_tree"); iterator.hasNext();)
		{
			Element nxt = iterator.next();
			for(Iterator<Element> classIterator = nxt.elementIterator("race"); classIterator.hasNext();)
			{
				Element classElement = classIterator.next();
				int race = Integer.parseInt(classElement.attributeValue("id"));
				Set<SkillLearn> learns = parseSkillLearn(classElement);
				getHolder().addAllFishingLearns(race, learns);
			}
		}

		for(Iterator<Element> iterator = rootElement.elementIterator("transfer_skill_tree"); iterator.hasNext();)
		{
			Element nxt = iterator.next();
			for(Iterator<Element> classIterator = nxt.elementIterator("class"); classIterator.hasNext();)
			{
				Element classElement = classIterator.next();
				int classId = Integer.parseInt(classElement.attributeValue("id"));
				Set<SkillLearn> learns = parseSkillLearn(classElement, ClassId.VALUES[classId].getClassLevel());
				getHolder().addAllTransferLearns(classId, learns);
			}
		}

		for(Iterator<Element> iterator = rootElement.elementIterator("normal_skill_tree"); iterator.hasNext();)
		{
			Element nxt = iterator.next();
			for(Iterator<Element> classIterator = nxt.elementIterator("class"); classIterator.hasNext();)
			{
				Element classElement = classIterator.next();
				if(classElement.attributeValue("id") != null)
				{
					int classId = Integer.parseInt(classElement.attributeValue("id"));
					Set<SkillLearn> learns = parseSkillLearn(classElement, ClassId.VALUES[classId].getClassLevel());
					getHolder().addAllNormalSkillLearns(classId, learns);
				}
				if(classElement.attributeValue("level") != null)
				{
					ClassLevel classLevel = ClassLevel.valueOf(classElement.attributeValue("level").toUpperCase());
					Set<SkillLearn> learns = parseSkillLearn(classElement, classLevel);
					for(ClassId classId : ClassId.VALUES)
					{
						if(classId.isOfLevel(classLevel))
							getHolder().addAllNormalSkillLearns(classId.getId(), learns);
					}
				}
			}
		}

		for(Iterator<Element> iterator = rootElement.elementIterator("general_skill_tree"); iterator.hasNext();)
		{
			Element nxt = iterator.next();
			getHolder().addAllGeneralSkillLearns(-1, parseSkillLearn(nxt)); // Парсим скиллы которые принадлежат любому классу.
			for(Iterator<Element> classIterator = nxt.elementIterator("class"); classIterator.hasNext();)
			{
				Element classElement = classIterator.next();
				if(classElement.attributeValue("id") != null)
				{
					int classId = Integer.parseInt(classElement.attributeValue("id"));
					Set<SkillLearn> learns = parseSkillLearn(classElement, ClassId.VALUES[classId].getClassLevel());
					getHolder().addAllGeneralSkillLearns(classId, learns);
				}
				if(classElement.attributeValue("level") != null)
				{
					ClassLevel classLevel = ClassLevel.valueOf(classElement.attributeValue("level").toUpperCase());
					Set<SkillLearn> learns = parseSkillLearn(classElement, classLevel);
					for(ClassId classId : ClassId.VALUES)
					{
						if(classId.isOfLevel(classLevel))
							getHolder().addAllGeneralSkillLearns(classId.getId(), learns);
					}
				}
			}
		}

		if(Config.ALLOWED_REBORN_COUNT > 0)
		{
			for(Iterator<Element> iterator = rootElement.elementIterator("reborn_skill_tree"); iterator.hasNext();)
				getHolder().addAllRebornLearns(parseSkillLearn(iterator.next()));
		}
		
		for(Iterator<Element> iterator = rootElement.elementIterator("transformation_skill_tree"); iterator.hasNext();)
		{
			Element nxt = iterator.next();
			for(Iterator<Element> classIterator = nxt.elementIterator("race"); classIterator.hasNext();)
			{
				Element classElement = classIterator.next();
				int race = Integer.parseInt(classElement.attributeValue("id"));
				Set<SkillLearn> learns = parseSkillLearn(classElement);
				getHolder().addAllTransformationLearns(race, learns);
			}
		}
	}

	@Override
	protected void afterParseActions()
	{
		getHolder().initNormalSkillLearns();
		getHolder().initGeneralSkillLearns();
	}

	private Set<SkillLearn> parseSkillLearn(Element tree, ClassLevel classLevel)
	{
		Set<SkillLearn> skillLearns = new HashSet<SkillLearn>();
		for(Iterator<Element> iterator = tree.elementIterator("skill"); iterator.hasNext();)
		{
			Element element = iterator.next();

			int id = Integer.parseInt(element.attributeValue("id"));
			int level = Integer.parseInt(element.attributeValue("level"));
			int cost = element.attributeValue("cost") == null ? 0 : Integer.parseInt(element.attributeValue("cost"));
			int min_level = element.attributeValue("min_level") == null ? 1 : Integer.parseInt(element.attributeValue("min_level"));
			int item_id = element.attributeValue("item_id") == null ? 0 : Integer.parseInt(element.attributeValue("item_id"));
			long item_count = element.attributeValue("item_count") == null ? 1 : Long.parseLong(element.attributeValue("item_count"));
			boolean clicked = element.attributeValue("clicked") != null && Boolean.parseBoolean(element.attributeValue("clicked"));

			skillLearns.add(new SkillLearn(id, level, min_level, cost, item_id, item_count, clicked, classLevel));
		}

		return skillLearns;
	}

	private Set<SkillLearn> parseSkillLearn(Element tree)
	{
		return parseSkillLearn(tree, ClassLevel.NONE);
	}
}
