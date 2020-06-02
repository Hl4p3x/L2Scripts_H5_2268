package l2s.gameserver.model.base;

/**
 * Author: VISTALL
 * Date:  11:53/01.12.2010
 */
public enum AcquireType
{
	/*0*/NORMAL,
	/*1*/FISHING,
	/*2*/CLAN,
	/*3*/SUB_UNIT,
	/*4*/TRANSFORMATION,
	/*5*/CERTIFICATION,
	/*6*/COLLECTION,
	/*7*/TRANSFER_CARDINAL,
	/*8*/TRANSFER_EVA_SAINTS,
	/*9*/REBORN,
	/*10*/TRANSFER_SHILLIEN_SAINTS,
	/*11*/GENERAL,
	/*12*/GM,
	/*13*/MULTICLASS;

	public static final AcquireType[] VALUES = AcquireType.values();

	public static AcquireType transferType(int classId)
	{
		switch (classId)
		{
			case 97:
				return TRANSFER_CARDINAL;
			case 105:
				return TRANSFER_EVA_SAINTS;
			case 112:
				return TRANSFER_SHILLIEN_SAINTS;
		}

		return null;
	}

	public int transferClassId()
	{
		switch (this)
		{
			case TRANSFER_CARDINAL:
				return 97;
			case TRANSFER_EVA_SAINTS:
				return 105;
			case TRANSFER_SHILLIEN_SAINTS:
				return 112;
		}

		return 0;
	}
}
