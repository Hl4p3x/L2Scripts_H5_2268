package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class ExBasicActionList extends L2GameServerPacket
{
	private static final int[] BASIC_ACTIONS =
	{
		0, // Переключатель Сесть/Встать. (/sit, /stand)
		1, // Переключатель Ходьба/Бег. (/walk, /run)
		2, // Атака выбранной цели (целей). Щелкните с зажатой клавишей Ctrl, чтобы принудительно атаковать. (/attack, /attackforce)
		3, // Запрос торговли с выбранным игроком. (/trade)
		4, // Выбор ближайшей цели для атаки. (/targetnext)
		5, // Подобрать предметы, расположенные рядом. (/pickup)
		6, // Переключиться на цель выбранного игрока. (/assist)
		7, // Пригласить выбранного игрока в вашу группу. (/invite)
		8, // Покинуть группу. (/leave)
		9, // Если вы лидер группы, исключить выбранного игрока (игроков) из группы. (/dismiss)
		10, // Настроить личный магазин для продажи предметов.(/vendor)
		11, // Отобразить окно "Подбор Группы" для поиска групп или членов для вашей группы. (/partymatching)
		12, // Эмоция: Поприветствовать окружающих. (/socialhello)
		13, // Эмоция: Показать, что вы или кто-то еще одержал победу!(/socialvictory)
		14, // Эмоция: Вдохновить ваших союзников (/socialcharge)
		15, // Ваш питомец либо следует за вами, либо остается на месте.
		16, // Атаковать цель.
		17, // Прервать текущее действие.
		18, // Подобрать находящиеся рядом предметы.
		19, // Убирает Питомца в инвентарь.
		20, // Использовать особое умение.
		21, // Ваши Миньоны либо следуют за вами, либо остаются на месте.
		22, // Атаковать цель.
		23, // Прервать текущее действие.
		24, // Эмоция: Ответить утвердительно. (/socialyes)
		25, // Эмоция: Ответить отрицательно. (/socialno)
		26, // Эмоция: Поклон, в знак уважения. (/socialbow)
		27, // Использовать особое умение.
		28, // Настроить личный магазин для покупки предметов. (/buy)
		29, // Эмоция: Я не понимаю, что происходит. (/socialunaware)
		30, // Эмоция: Я жду... (/socialwaiting)
		31, // Эмоция: От души посмеяться. (/sociallaugh)
		32, // Переключение между режимами атаки/движения.
		33, // Эмоция: Аплодисменты. (/socialapplause)
		34, // Эмоция: Покажите всем ваш лучший танец. (/socialdance)
		35, // Эмоция: Мне грустно. (/socialsad)
		36, // Ядовитая Газовая Атака.
		37, // Настроить личную мастерскую для создания предметов с помощью рецептов Гномов за вознаграждение. (/dwarvenmanufacture)
		38, // Переключатель оседлать/спешиться, когда вы находитесь рядом с Питомцем, которого можно оседлать. (/mount, /dismount, /mountdismount)
		39, // Атака взрывающимися трупами.
		40, // Увеличивает оценку цели (/evaluate)
		41, // Атаковать врата замка, стены или штабы выстрелом из пушки.
		42, // Возвращает урон обратно врагу.
		43, // Атаковать врага, создав бурлящий водоворот.
		44, // Атаковать врага мощным взрывом.
		45, // Восстанавливает MP призывателя.
		46, // Атаковать врага, призвав разрушительный шторм.
		47, // Одновременно повреждает врага и лечит слугу.
		48, // Атака врага выстрелом из пушки.
		49, // Атака в приступе ярости.
		50, // Выбранный член группы становится ее лидером.(/changepartyleader)
		51, // Создать предмет, используя обычный рецепт за вознаграждение.(/generalmanufacture)
		52, // Снимает узы с миньона и освобождает его.
		53, // Двигаться к цели.
		54, // Двигаться к цели.
		55, // Переключатель записи и остановки записи повторов. (/start_videorecording, /end_videorecording, /startend_videorecording)
		56, // Пригласить выбранную цель в канал команды. (/channelinvite)
		57, // Высвечивает сообщения личного магазина и личной мастерской, содержащие искомое слово. (/findprivatestore)
		58, // Вызвать другого игрока на дуэль. (/duel)
		59, // Отмена дуэли означает проигрыш. (/withdraw)
		60, // Вызвать другую группу на дуэль. (/partyduel)
		61, // Открывает личный магазин для продажи упаковок (/packagesale)
		62, // Обаятельная поза(/charm)
		63, // Запускает забавную и простую мини-игру, в которую можно поиграть в любое время. (команда: /minigame)
		64, // Открывает окно свободного телепорта, которое позволяет свободно перемещаться между локациями с телепортами. (команда: /teleportbookmark)
		65, // Сообщает о подозрительном поведении объекта, чьи действия позволяют предположить использование бот-программы.
		66, // Поза "Смущение"  (команда: /shyness)
		67, // Управление кораблем
		68, // Прекращение управления кораблем
		69, // Отправление корабля
		70, // Спуск с корабля
		71, // Поклон
		72, // Дай Пять
		73, // Танец Вдвоем
		74, // Вкл/Выкл данные о состоянии
		76, // Приглашение друга
		77, // Вкл/Выкл. Запись
		78, // Использование Знака 1
		79, // Использование Знака 2
		80, // Использование Знака 3
		81, // Использование Знака 4
		82, // Автоприцел Знаком 1
		83, // Автоприцел Знаком 2
		84, // Автоприцел Знаком 3
		85, // Автоприцел Знаком 4
		86, // Начать/прервать автоматический поиск группы
		87, // Propose
		88, // Provoke
		89, // Хвастовство
		90, // Подземелье
		1000, // Осадный Молот
		1001, // Предельный Ускоритель
		1002, // Враждебность
		1003, // Дикое Оглушение
		1004, // Дикая Защита
		1005, // Яркая Вспышка
		1006, // Исцеляющий Свет
		1007, // Благословение Королевы
		1008, // Дар Королевы
		1009, // Исцеление Королевы
		1010, // Благословение Серафима
		1011, // Дар Серафима
		1012, // Исцеление Серафима
		1013, // Проклятие Тени
		1014, // Массовое Проклятие Тени
		1015, // Жертва Тени
		1016, // Проклятый Импульс
		1017, // Проклятый Удар
		1018, // Проклятие Поглощения Энергии
		1019, // Умение Кэт 2
		1020, // Умение Мяу 2
		1021, // Умение Кая 2
		1022, // Умение Юпитера 2
		1023, // Умение Миража 2
		1024, // Умение Бекара 2
		1025, // Теневое Умение 1
		1026, // Теневое Умение 2
		1027, // Умение Гекаты
		1028, // Умение Воскрешенного 1
		1029, // Умение Воскрешенного 2
		1030, // Умение Порочного 2
		1031, // Рассечение
		1032, // Режущий Вихрь
		1033, // Кошачья Хватка
		1034, // Кнут
		1035, // Приливная Волна
		1036, // Взрыв Трупа
		1037, // Случайная Смерть
		1038, // Сила Проклятия
		1039, // Пушечное Мясо
		1040, // Большой Бум
		1041, // Укус
		1042, // Кувалда
		1043, // Волчий Рык
		1044, // Пробуждение
		1045, // Волчий Вой
		1046, // Рев Ездового Дракона
		1047, // Укус Божественного Зверя
		1048, // Оглушительная Атака Божественного Зверя
		1049, // Огненное Дыхание Божественного Зверя
		1050, // Рев Божественного Зверя
		1051, // Благословение Тела
		1052, // Благословение Духа
		1053, // Ускорение
		1054, // Проницательность
		1055, // Чистота
		1056, // Воодушевление
		1057, // Дикая Магия
		1058, // Шепот Смерти
		1059, // Фокусировка
		1060, // Наведение
		1061, // Удар Смерти
		1062, // Двойная Атака
		1063, // Вихревая Атака
		1064, // Метеоритный дождь
		1065, // Пробуждение
		1066, // Удар Молнии
		1067, // Молния
		1068, // Световая Волна
		1069, // Вспышка
		1070, // Контроль Эффекта
		1071, // Мощный удар
		1072, // Проникающая Атака
		1073, // Яростный Ветер
		1074, // Удар Копьем
		1075, // Боевой Клич
		1076, // Мощное Сокрушение
		1077, // Шаровая Молния
		1078, // Шоковая Волна
		1079, // Вой
		1080, // Прилив Феникса
		1081, // Очищение Феникса
		1082, // Пылающее Перо Феникса
		1083, // Пылающий Клюв Феникса
		1084, // Смена Режима
		1086, // Натиск Пантеры
		1087, // Темный Коготь Пантеры
		1088, // Смертоносный Коготь Пантеры
		1089, // Хвост
		1090, // Укус Ездового Дракона
		1091, // Устрашение Ездового Дракона
		1092, // Рывок Ездового Дракона
		1093, // Удар Магвена
		1094, // Легкая Походка Магвена
		1095, // Мощный Удар Магвена
		1096, // Легкая Походка Элитного Магвена
		1097, // Возвращение Магвена
		1098, // Групповое Возвращение Магвена
		1099, // Атака
		1100, // Перемещение
		1101, // Прекращение
		1102, // Отмена  призыва
		1103, // Пассивность
		1104, // Защита
		1106, // Коготь Медведя
		1107, // Топот Медведя
		1108, // Укус Кугуара
		1109, // Прыжок Кугуара
		1110, // Прикосновение Потрошителя
		1111, // Сила Потрошителя
		1113, // Львиный Рев
		1114, // Львиный Коготь
		1115, // Львиный Бросок
		1116, // Львиное Пламя
		1117, // Полет Громового Змея
		1118, // Очищение Громового Змея
		1120, // Стрельба Перьями Громового Змея
		1121, // Острые Когти Громового Змея
		1122, // Благословение Жизни
		1123, // Осадный Удар
		1124, // Агрессия Кошки
		1125, // Кошачье Оглушение
		1126, // Укус Кошки
		1127, // Атакующий Прыжок Кошки
		1128, // Прикосновение Кошки
		1129, // Сила Кошки
		1130, // Агрессия Единорога
		1131, // Оглушение Единорога
		1132, // Укус Единорога
		1133, // Атакующий Прыжок Единорога
		1134, // Прикосновение Единорога
		1135, // Сила Единорога
		1136, // Агрессия Фантома
		1137, // Фантомное Оглушение
		1138, // Укус Фантома
		1139, // Атакующий Прыжок Фантома
		1140, // Прикосновение Фантома
		1141, // Сила Фантома
		1142, // Рев Пантеры
		1143, // Стремительный Бросок Пантеры
		1144, // Атакующий Прыжок Коммандо
		1145, // Двойное Сокрушение Коммандо
		1146, // Стихийное Сокрушение
		1147, // Мощь Кошки Колдуньи
		1148, // Стремительный Бросок Лансера
		1149, // Клеймо Силы
		1150, // Массовая Заморозка
		1151, // Мощь Херувима
		1152, // Призрачный Удар Меча
		1153, // Призрачный Импульс
		1154, // Призрачный Шип
		1155, // Призрачное Разрушение
		5000, // Погладить
		5001, // Искушение Света Розы
		5002, // Запредельное Искушение
		5003, // Удар Молнии
		5004, // Молния
		5005, // Световая волна
		5006, // Контроль Эффекта
		5007, // Проникающая Атака
		5008, // Вихревая Атака
		5009, // Сокрушение
		5010, // Боевой Клич
		5011, // Мощное Сокрушение
		5012, // Шаровая Молния
		5013, // Шоковая Волна
		5014, // Воспламенение
		5015, // Смена Режима
		5016, // Усиление Кота-Рейнджера
	};

	private static final int[] BASIC_ACTIONS_HF =
	{
		0, // Переключатель Сесть/Встать. (/sit, /stand)
		1, // Переключатель Ходьба/Бег. (/walk, /run)
		2, // Атака выбранной цели (целей). Щелкните с зажатой клавишей Ctrl, чтобы принудительно атаковать. (/attack, /attackforce)
		3, // Запрос торговли с выбранным игроком. (/trade)
		4, // Выбор ближайшей цели для атаки. (/targetnext)
		5, // Подобрать предметы, расположенные рядом. (/pickup)
		6, // Переключиться на цель выбранного игрока. (/assist)
		7, // Пригласить выбранного игрока в вашу группу. (/invite)
		8, // Покинуть группу. (/leave)
		9, // Если вы лидер группы, исключить выбранного игрока (игроков) из группы. (/dismiss)
		10, // Настроить личный магазин для продажи предметов.(/vendor)
		11, // Отобразить окно "Подбор Группы" для поиска групп или членов для вашей группы. (/partymatching)
		12, // Эмоция: Поприветствовать окружающих. (/socialhello)
		13, // Эмоция: Показать, что вы или кто-то еще одержал победу!(/socialvictory)
		14, // Эмоция: Вдохновить ваших союзников (/socialcharge)
		15, // Ваш питомец либо следует за вами, либо остается на месте.
		16, // Атаковать цель.
		17, // Прервать текущее действие.
		18, // Подобрать находящиеся рядом предметы.
		19, // Убирает Питомца в инвентарь.
		20, // Использовать особое умение.
		21, // Ваши Миньоны либо следуют за вами, либо остаются на месте.
		22, // Атаковать цель.
		23, // Прервать текущее действие.
		24, // Эмоция: Ответить утвердительно. (/socialyes)
		25, // Эмоция: Ответить отрицательно. (/socialno)
		26, // Эмоция: Поклон, в знак уважения. (/socialbow)
		27, // Использовать особое умение.
		28, // Настроить личный магазин для покупки предметов. (/buy)
		29, // Эмоция: Я не понимаю, что происходит. (/socialunaware)
		30, // Эмоция: Я жду... (/socialwaiting)
		31, // Эмоция: От души посмеяться. (/sociallaugh)
		32, // Переключение между режимами атаки/движения.
		33, // Эмоция: Аплодисменты. (/socialapplause)
		34, // Эмоция: Покажите всем ваш лучший танец. (/socialdance)
		35, // Эмоция: Мне грустно. (/socialsad)
		36, // Ядовитая Газовая Атака.
		37, // Настроить личную мастерскую для создания предметов с помощью рецептов Гномов за вознаграждение. (/dwarvenmanufacture)
		38, // Переключатель оседлать/спешиться, когда вы находитесь рядом с Питомцем, которого можно оседлать. (/mount, /dismount, /mountdismount)
		39, // Атака взрывающимися трупами.
		40, // Увеличивает оценку цели (/evaluate)
		41, // Атаковать врата замка, стены или штабы выстрелом из пушки.
		42, // Возвращает урон обратно врагу.
		43, // Атаковать врага, создав бурлящий водоворот.
		44, // Атаковать врага мощным взрывом.
		45, // Восстанавливает MP призывателя.
		46, // Атаковать врага, призвав разрушительный шторм.
		47, // Одновременно повреждает врага и лечит слугу.
		48, // Атака врага выстрелом из пушки.
		49, // Атака в приступе ярости.
		50, // Выбранный член группы становится ее лидером.(/changepartyleader)
		51, // Создать предмет, используя обычный рецепт за вознаграждение.(/generalmanufacture)
		52, // Снимает узы с миньона и освобождает его.
		53, // Двигаться к цели.
		54, // Двигаться к цели.
		55, // Переключатель записи и остановки записи повторов. (/start_videorecording, /end_videorecording, /startend_videorecording)
		56, // Пригласить выбранную цель в канал команды. (/channelinvite)
		57, // Высвечивает сообщения личного магазина и личной мастерской, содержащие искомое слово. (/findprivatestore)
		58, // Вызвать другого игрока на дуэль. (/duel)
		59, // Отмена дуэли означает проигрыш. (/withdraw)
		60, // Вызвать другую группу на дуэль. (/partyduel)
		61, // Открывает личный магазин для продажи упаковок (/packagesale)
		62, // Обаятельная поза(/charm)
		63, // Запускает забавную и простую мини-игру, в которую можно поиграть в любое время. (команда: /minigame)
		64, // Открывает окно свободного телепорта, которое позволяет свободно перемещаться между локациями с телепортами. (команда: /teleportbookmark)
		65, // Сообщает о подозрительном поведении объекта, чьи действия позволяют предположить использование бот-программы.
		66, // Поза "Смущение"  (команда: /shyness)
		67, // Управление кораблем
		68, // Прекращение управления кораблем
		69, // Отправление корабля
		70, // Спуск с корабля
		71, // Поклон
		72, // Дай Пять
		73, // Танец Вдвоем
		1000, // Атаковать врата замка, стены и штабы мощным ударом.
		1001, // Безрассудная, но мощная атака, используйте ее с большой осторожностью.
		1002, // Провоцировать окружающих атаковать вас.
		1003, // Неожиданная атака, наносящая урон и оглушающая оппонента.
		1004, // Моментально значительно увеличивается Физ. Защ. и Маг. Защ. Использующий данное умение персонаж не может двигаться.
		1005, // Магическая Атака
		1006, // Восстанавливает HP питомца.
		1007, // В случае успешного применения временно увеличивает силу атаки группы и шанс на критический удар.
		1008, // Временно увеличивает Физ. Атк. и точность вашей группы.
		1009, // Есть шанс снять проклятие с членов группы.
		1010, // Временно увеличивает регенерацию MP вашей группы.
		1011, // Временно уменьшает время перезарядки заклинаний вашей команды.
		1012, // Снимает проклятие с вашей группы.
		1013, // Провокация оппонента и удар, накладывающий проклятие, уменьшающее Физ. Защ. и Маг. Защ.
		1014, // Провоцирует на атаку множество врагов и наносит удар с проклятием, понижающим их Физ. Защ. и Маг. Защ.
		1015, // Жертвует HP для регенерации HP выбранной цели.
		1016, // Обрушивает на оппонента мощную критическую атаку.
		1017, // Оглушающий взрыв, наносящий урон и ошеломляющий врага.
		1018, // Наложение смертельного проклятия, высасывающего HP врага.
		1019, // Умение №2, используемое Кэт
		1020, // Умение №2, используемое Мяу
		1021, // Умение №2, используемое Каем
		1022, // Умение №2, используемое Юпитером
		1023, // Умение №2, используемое Миражом
		1024, // Умение №2, используемое Бекаром
		1025, // Умение №2, используемое Тенью
		1026, // Умение №1, используемое Тенью
		1027, // Умение №2, используемое Гекатой
		1028, // Умение №1, используемое Воскрешенным
		1029, // Умение №2, используемое Воскрешенным
		1030, // Умение №2, используемое Порочным
		1031, // Король Кошек: Мощная режущая атака. Максимальное поражение.
		1032, // Король Кошек: Режет окружающих врагов во время вращения в воздухе. Максимальное поражение.
		1033, // Король Кошек: Обездвиживает близко стоящих врагов
		1034, // Магнус: Мощный удар задними ногами, поражающий и оглушающий врага. Максимальное поражение.
		1035, // Магнус: Обрушивает на многочисленные цели гигантские массы воды.
		1036, // Призрачный Лорд: Врывает труп, поражая рядом стоящих врагов.
		1037, // Призрачный Лорд: Клинки в каждой руке наносят разрушительные повреждения. Максимальное поражение.
		1038, // Проклятие рядом стоящих врагов, отравляющее и уменьшающее их Скор. Атк.
		1039, // Осадное Орудие: Выстреливает снаряд на короткую дистанцию. Потребляет 4 ед. Сверкающего Пороха.
		1040, // Осадное Орудие: Выстреливает снаряд на длинную дистанцию. Потребляет 5 ед. Сверкающего пороха.
		1041, // Ужасный укус врага
		1042, // Царапает врага обеими лапами. Вызывает кровотечение.
		1043, // Подавляет врагов мощным ревом
		1044, // Пробуждает тайную силу
		1045, // Понижает Физ. Атк./Маг. Атк. у стоящих рядом врагов.
		1046, // Понижает Скор. Атк./Скор. Маг. у стоящих рядом врагов.
		1047, // Ужасный укус врага
		1048, // Приносит двойные повреждения и одновременно оглушает врага.
		1049, // Выдыхает огонь в вашем направлении.
		1050, // Подавляет окружающих врагов мощным ревом.
		1051, // Временно увеличивает макс. количество HP.
		1052, // Временно увеличивает макс. количество MP.
		1053, // Временно увеличивает Скор. Атк.
		1054, // Временно увеличивает скорость чтения заклинаний.
		1055, // Временно уменьшает затраты MP выбранной цели. Потребляет Рунные камни.
		1056, // Временно увеличивает Маг. Атк.
		1057, // Временно увеличивает ранг критического удара и силу магических атак
		1058, // Временно увеличивает силу критического удара.
		1059, // Временно увеличивает шанс критического удара
		1060, // Временно увеличивает точность
		1061, // Мощная атака из засады. Можно использовать только при применении навыка "Пробуждение".
		1062, // Быстрая двойная атака
		1063, // Сильная крутящая атака наносит не только урон, но и оглушает противника.
		1064, // Падающие с неба камни наносят повреждения врагам.
		1065, // Выводит из скрытого состояния
		1066, // Атака громовыми силами
		1067, // Быстрая магическая атака врагов в поле зрения
		1068, // Атакует нескольких врагов силами молний
		1069, // Наносит сильный удар из засады. Можно использовать только при применении навыка "Пробуждение".
		1070, // Нельзя накладывать положительные эффекты на владельца. Действует 5 минут.
		1071, // Мощная атака по объекту
		1072, // Мощная проникающая атака по объекту
		1073, // Атака по врагам, разметающая их ряды как под ударом торнадо
		1074, // Атака по впереди стоящим врагам мощным броском копья
		1075, // Победный крик, повышающий собственные навыки
		1076, // Мощная атака по объекту
		1077, // Атака по впереди стоящим врагам внутренней энергией
		1078, // Атака по впереди стоящим врагам при помощи электричества
		1079, // Громкий крик, повышающий собственные навыки
		1080, // Быстро приближает к врагу и наносит урон
		1081, // Снимает отрицательные эффекты с объекта
		1082, // Откидывает пламенем
		1083, // Мощный укус, наносящий урон врагу
		1084, // Переключает между атакующим/защитным режимом
		1086, // Ограничивает количество положительных эффектов до одного
		1087, // Увеличивает темную сторону до 25
		1088, // Урезает важные навыки
		1089, // Атака по впереди стоящим врагам при помощи хвоста.
		1090, // Ужасный укус врага
		1091, // Ввергает противника в ужас и заставляет бежать с поля боя.
		1092, // Увеличивает скорость передвижения.
		1093, // Attacks the enemy with a little chance to reduce his speed .
		1094, // considerably increases the speed of the host.
		1095, // Attacks the enemy with a little chance to reduce his speed .
		1096, // Significantly increases the speed of the host , as well as members of his group .
		1097, // Leads host in Seed of Annihilation .
		1098, // Leads master and his group in the Seed of Annihilation .
		5000, // Можно погладить Рудольфа. Заполняет шкалу верности на 25%. Нельзя использовать во время перевоплощения!
		5001, // Увеличивает Макс. HP, Макс. MP и Скорость на 20%, сопротивление отрицательным эффектам на 10%. Время повторного использования: 10 мин. При использовании умения расходуется 3 Эссенции Розы. Нельзя использовать с Запредельным Искушением. Время действия: 5 мин.
		5002, // Увеличивает Макс. HP/MP/CP, Физ. Защ. и Маг. Защ. на 30%, Скорость на 20%, Физ. Атк. на 10%, Маг. Атк. на 20% и снижает расход MP на 15%.  Время повторного использования: 40 мин. При использовании умения расходуется 10 Эссенций Розы. Время действия: 20 мин.
		5003, // Обрушивает на врагов мощь грома.
		5004, // Обрушивает на стоящих вблизи врагов молниеносную магическую атаку.
		5005, // Обрушивает на окружающих врагов мощь грома.
		5006, // Не позволяет накладывать на хозяина любые эффекты. Время действия: 5 мин.
		5007, // Питомец пронзает врага в смертоносном выпаде.
		5008, // Атакует окружающих врагов.
		5009, // Вонзает меч в ряды впередистоящих врагов.
		5010, // Усиливает свои умения.
		5011, // Атакует врага мощным ударом.
		5012, // Обрушивает накопившуюся в теле энергию на ряды впередистоящих врагов.
		5013, // Обрушивает шоковую волну на впередистоящего врага.
		5014, // Значительно усиливает свои умения.
		5015, // Смена атакующего/вспомогательного состояния питомца.
		5016, // Instantly restores the master's HP by 10%. Increases the master's resistnace debuff attacks by 80%, resistance to buff-canceling attacks by 40% speed by 10, p. def by 20%, and m. def by 20% and decreases MP comsmption for all skills by 50% for 1 minute?
	};

	private static final int[] TRANSFORMATION_ACTIONS =
	{
		1, // Переключатель Ходьба/Бег. (/walk, /run)
		2, // Атака выбранной цели (целей). Щелкните с зажатой клавишей Ctrl, чтобы принудительно атаковать. (/attack, /attackforce)
		3, // Запрос торговли с выбранным игроком. (/trade)
		4, // Выбор ближайшей цели для атаки. (/targetnext)
		5, // Подобрать предметы, расположенные рядом. (/pickup)
		6, // Переключиться на цель выбранного игрока. (/assist)
		7, // Пригласить выбранного игрока в вашу группу. (/invite)
		8, // Покинуть группу. (/leave)
		9, // Если вы лидер группы, исключить выбранного игрока (игроков) из группы. (/dismiss)
		11, // Отобразить окно "Подбор Группы" для поиска групп или членов для вашей группы. (/partymatching)
		15, // Ваш питомец либо следует за вами, либо остается на месте.
		16, // Атаковать цель.
		17, // Прервать текущее действие.
		18, // Подобрать находящиеся рядом предметы.
		19, // Убирает Питомца в инвентарь.
		21, // Ваши Миньоны либо следуют за вами, либо остаются на месте.
		22, // Атаковать цель.
		23, // Прервать текущее действие.
		40, // Увеличивает оценку цели (/evaluate)
		50, // Выбранный член группы становится ее лидером.(/changepartyleader)
		52, // Снимает узы с миньона и освобождает его.
		53, // Двигаться к цели.
		54, // Двигаться к цели.
		55, // Переключатель записи и остановки записи повторов. (/start_videorecording, /end_videorecording, /startend_videorecording)
		56, // Пригласить выбранную цель в канал команды. (/channelinvite)
		57, // Высвечивает сообщения личного магазина и личной мастерской, содержащие искомое слово. (/findprivatestore)
		63, // Запускает забавную и простую мини-игру, в которую можно поиграть в любое время. (команда: /minigame)
		64, // Открывает окно свободного телепорта, которое позволяет свободно перемещаться между локациями с телепортами. (команда: /freeteleport)
		65, // report suspicious behavior of an object whose actions suggest BOT- use program.
		//65, // Сообщает о подозрительном поведении объекта, чьи действия позволяют предположить использование BOT-программы.
		67, // Управление кораблем
		68, // Прекращение управления кораблем
		69, // Отправление корабля
		70, // Спуск с корабля
		1000, // Атаковать врата замка, стены и штабы мощным ударом.
		1001, // Безрассудная, но мощная атака, используйте ее с большой осторожностью.
		1002, // Провоцировать окружающих атаковать вас.
		1003, // Неожиданная атака, наносящая урон и оглушающая оппонента.
		1004, // Моментально значительно увеличивается Физ. Защ. и Маг. Защ. Использующий данное умение персонаж не может двигаться.
		1005, // Магическая Атака
		1006, // Восстанавливает HP питомца.
		1007, // В случае успешного применения временно увеличивает силу атаки группы и шанс на критический удар.
		1008, // Временно увеличивает Физ. Атк. и точность вашей группы.
		1009, // Есть шанс снять проклятие с членов группы.
		1010, // Временно увеличивает регенерацию MP вашей группы.
		1011, // Временно уменьшает время перезарядки заклинаний вашей команды.
		1012, // Снимает проклятие с вашей группы.
		1013, // Провокация оппонента и удар, накладывающий проклятие, уменьшающее Физ. Защ. и Маг. Защ.
		1014, // Провоцирует на атаку множество врагов и наносит удар с проклятием, понижающим их Физ. Защ. и Маг. Защ.
		1015, // Жертвует HP для регенерации HP выбранной цели.
		1016, // Обрушивает на оппонента мощную критическую атаку.
		1017, // Оглушающий взрыв, наносящий урон и ошеломляющий врага.
		1018, // Наложение смертельного проклятия, высасывающего HP врага.
		1019, // Умение №2, используемое Кэт
		1020, // Умение №2, используемое Мяу
		1021, // Умение №2, используемое Каем
		1022, // Умение №2, используемое Юпитером
		1023, // Умение №2, используемое Миражом
		1024, // Умение №2, используемое Бекаром
		1025, // Умение №2, используемое Тенью
		1026, // Умение №1, используемое Тенью
		1027, // Умение №2, используемое Гекатой
		1028, // Умение №1, используемое Воскрешенным
		1029, // Умение №2, используемое Воскрешенным
		1030, // Умение №2, используемое Порочным
		1031, // Король Кошек: Мощная режущая атака. Максимальное поражение.
		1032, // Король Кошек: Режет окружающих врагов во время вращения в воздухе. Максимальное поражение.
		1033, // Король Кошек: Обездвиживает близко стоящих врагов
		1034, // Магнус: Мощный удар задними ногами, поражающий и оглушающий врага. Максимальное поражение.
		1035, // Магнус: Обрушивает на многочисленные цели гигантские массы воды.
		1036, // Призрачный Лорд: Врывает труп, поражая рядом стоящих врагов.
		1037, // Призрачный Лорд: Клинки в каждой руке наносят разрушительные повреждения. Максимальное поражение.
		1038, // Проклятие рядом стоящих врагов, отравляющее и уменьшающее их Скор. Атк.
		1039, // Осадное Орудие: Выстреливает снаряд на короткую дистанцию. Потребляет 4 ед. Сверкающего Пороха.
		1040, // Осадное Орудие: Выстреливает снаряд на длинную дистанцию. Потребляет 5 ед. Сверкающего пороха.
		1041, // Ужасный укус врага
		1042, // Царапает врага обеими лапами. Вызывает кровотечение.
		1043, // Подавляет врагов мощным ревом
		1044, // Пробуждает тайную силу
		1045, // Понижает Физ. Атк./Маг. Атк. у стоящих рядом врагов.
		1046, // Понижает Скор. Атк./Скор. Маг. у стоящих рядом врагов.
		1047, // Ужасный укус врага
		1048, // Приносит двойные повреждения и одновременно оглушает врага.
		1049, // Выдыхает огонь в вашем направлении.
		1050, // Подавляет окружающих врагов мощным ревом.
		1051, // Временно увеличивает макс. количество HP.
		1052, // Временно увеличивает макс. количество MP.
		1053, // Временно увеличивает Скор. Атк.
		1054, // Временно увеличивает скорость чтения заклинаний.
		1055, // Временно уменьшает затраты MP выбранной цели. Потребляет Рунные камни.
		1056, // Временно увеличивает Маг. Атк.
		1057, // Временно увеличивает ранг критического удара и силу магических атак
		1058, // Временно увеличивает силу критического удара.
		1059, // Временно увеличивает шанс критического удара
		1060, // Временно увеличивает точность
		1061, // Мощная атака из засады. Можно использовать только при применении навыка "Пробуждение".
		1062, // Быстрая двойная атака
		1063, // Сильная крутящая атака наносит не только урон, но и оглушает противника.
		1064, // Падающие с неба камни наносят повреждения врагам.
		1065, // Выводит из скрытого состояния
		1066, // Атака громовыми силами
		1067, // Быстрая магическая атака врагов в поле зрения
		1068, // Атакует нескольких врагов силами молний
		1069, // Наносит сильный удар из засады. Можно использовать только при применении навыка "Пробуждение".
		1070, // Нельзя накладывать положительные эффекты на владельца. Действует 5 минут.
		1071, // Мощная атака по объекту
		1072, // Мощная проникающая атака по объекту
		1073, // Атака по врагам, разметающая их ряды как под ударом торнадо
		1074, // Атака по впереди стоящим врагам мощным броском копья
		1075, // Победный крик, повышающий собственные навыки
		1076, // Мощная атака по объекту
		1077, // Атака по впереди стоящим врагам внутренней энергией
		1078, // Атака по впереди стоящим врагам при помощи электричества
		1079, // Громкий крик, повышающий собственные навыки
		1080, // Быстро приближает к врагу и наносит урон
		1081, // Снимает отрицательные эффекты с объекта
		1082, // Откидывает пламенем
		1083, // Мощный укус, наносящий урон врагу
		1084, // Переключает между атакующим/защитным режимом
		1086, // Ограничивает количество положительных эффектов до одного
		1087, // Увеличивает темную сторону до 25
		1088, // Урезает важные навыки
		1089, // Атака по впереди стоящим врагам при помощи хвоста.
		1090, // Ужасный укус врага
		1091, // Ввергает противника в ужас и заставляет бежать с поля боя.
		1092, // Увеличивает скорость передвижения.
		1093, // Attacks the enemy with a little chance to reduce his speed .
		1094, // considerably increases the speed of the host.
		1095, // Attacks the enemy with a little chance to reduce his speed .
		1096, // Significantly increases the speed of the host , as well as members of his group .
		1097, // Leads host in Seed of Annihilation .
		1098, // Leads master and his group in the Seed of Annihilation .
		5000, // Можно погладить Рудольфа. Заполняет шкалу верности на 25%. Нельзя использовать во время перевоплощения!
		5001, // Увеличивает Макс. HP, Макс. MP и Скорость на 20%, сопротивление отрицательным эффектам на 10%. Время повторного использования: 10 мин. При использовании умения расходуется 3 Эссенции Розы. Нельзя использовать с Запредельным Искушением. Время действия: 5 мин.
		5002, // Увеличивает Макс. HP/MP/CP, Физ. Защ. и Маг. Защ. на 30%, Скорость на 20%, Физ. Атк. на 10%, Маг. Атк. на 20% и снижает расход MP на 15%.  Время повторного использования: 40 мин. При использовании умения расходуется 10 Эссенций Розы. Время действия: 20 мин.
		5003, // Обрушивает на врагов мощь грома.
		5004, // Обрушивает на стоящих вблизи врагов молниеносную магическую атаку.
		5005, // Обрушивает на окружающих врагов мощь грома.
		5006, // Не позволяет накладывать на хозяина любые эффекты. Время действия: 5 мин.
		5007, // Питомец пронзает врага в смертоносном выпаде.
		5008, // Атакует окружающих врагов.
		5009, // Вонзает меч в ряды впередистоящих врагов.
		5010, // Усиливает свои умения.
		5011, // Атакует врага мощным ударом.
		5012, // Обрушивает накопившуюся в теле энергию на ряды впередистоящих врагов.
		5013, // Обрушивает шоковую волну на впередистоящего врага.
		5014, // Значительно усиливает свои умения.
		5015, // Смена атакующего/вспомогательного состояния питомца..
		5016, // Instantly restores the master's HP by 10%. Increases the master's resistnace debuff attacks by 80%, resistance to buff-canceling attacks by 40% speed by 10, p. def by 20%, and m. def by 20% and decreases MP comsmption for all skills by 50% for 1 minute?
	};

	private static final int[] TRANSFORMATION_ACTIONS_HF =
	{
		1, // Переключатель Ходьба/Бег. (/walk, /run)
		2, // Атака выбранной цели (целей). Щелкните с зажатой клавишей Ctrl, чтобы принудительно атаковать. (/attack, /attackforce)
		3, // Запрос торговли с выбранным игроком. (/trade)
		4, // Выбор ближайшей цели для атаки. (/targetnext)
		5, // Подобрать предметы, расположенные рядом. (/pickup)
		6, // Переключиться на цель выбранного игрока. (/assist)
		7, // Пригласить выбранного игрока в вашу группу. (/invite)
		8, // Покинуть группу. (/leave)
		9, // Если вы лидер группы, исключить выбранного игрока (игроков) из группы. (/dismiss)
		11, // Отобразить окно "Подбор Группы" для поиска групп или членов для вашей группы. (/partymatching)
		15, // Ваш питомец либо следует за вами, либо остается на месте.
		16, // Атаковать цель.
		17, // Прервать текущее действие.
		18, // Подобрать находящиеся рядом предметы.
		19, // Убирает Питомца в инвентарь.
		21, // Ваши Миньоны либо следуют за вами, либо остаются на месте.
		22, // Атаковать цель.
		23, // Прервать текущее действие.
		40, // Увеличивает оценку цели (/evaluate)
		50, // Выбранный член группы становится ее лидером.(/changepartyleader)
		52, // Снимает узы с миньона и освобождает его.
		53, // Двигаться к цели.
		54, // Двигаться к цели.
		55, // Переключатель записи и остановки записи повторов. (/start_videorecording, /end_videorecording, /startend_videorecording)
		56, // Пригласить выбранную цель в канал команды. (/channelinvite)
		57, // Высвечивает сообщения личного магазина и личной мастерской, содержащие искомое слово. (/findprivatestore)
		63, // Запускает забавную и простую мини-игру, в которую можно поиграть в любое время. (команда: /minigame)
		64, // Открывает окно свободного телепорта, которое позволяет свободно перемещаться между локациями с телепортами. (команда: /teleportbookmark)
		65, // Сообщает о подозрительном поведении объекта, чьи действия позволяют предположить использование бот-программы.
		67, // Управление кораблем
		68, // Прекращение управления кораблем
		69, // Отправление корабля
		70, // Спуск с корабля
		74, // Вкл/Выкл данные о состоянии
		76, // Приглашение друга
		77, // Вкл/Выкл. Запись
		78, // Использование Знака 1
		79, // Использование Знака 2
		80, // Использование Знака 3
		81, // Использование Знака 4
		82, // Автоприцел Знаком 1
		83, // Автоприцел Знаком 2
		84, // Автоприцел Знаком 3
		85, // Автоприцел Знаком 4
		86, // Начать/прервать автоматический поиск группы
		90, // Подземелье
		1000, // Осадный Молот
		1001, // Предельный Ускоритель
		1002, // Враждебность
		1003, // Дикое Оглушение
		1004, // Дикая Защита
		1005, // Яркая Вспышка
		1006, // Исцеляющий Свет
		1007, // Благословение Королевы
		1008, // Дар Королевы
		1009, // Исцеление Королевы
		1010, // Благословение Серафима
		1011, // Дар Серафима
		1012, // Исцеление Серафима
		1013, // Проклятие Тени
		1014, // Массовое Проклятие Тени
		1015, // Жертва Тени
		1016, // Проклятый Импульс
		1017, // Проклятый Удар
		1018, // Проклятие Поглощения Энергии
		1019, // Умение Кэт 2
		1020, // Умение Мяу 2
		1021, // Умение Кая 2
		1022, // Умение Юпитера 2
		1023, // Умение Миража 2
		1024, // Умение Бекара 2
		1025, // Теневое Умение 1
		1026, // Теневое Умение 2
		1027, // Умение Гекаты
		1028, // Умение Воскрешенного 1
		1029, // Умение Воскрешенного 2
		1030, // Умение Порочного 2
		1031, // Рассечение
		1032, // Режущий Вихрь
		1033, // Кошачья Хватка
		1034, // Кнут
		1035, // Приливная Волна
		1036, // Взрыв Трупа
		1037, // Случайная Смерть
		1038, // Сила Проклятия
		1039, // Пушечное Мясо
		1040, // Большой Бум
		1041, // Укус
		1042, // Кувалда
		1043, // Волчий Рык
		1044, // Пробуждение
		1045, // Волчий Вой
		1046, // Рев Ездового Дракона
		1047, // Укус Божественного Зверя
		1048, // Оглушительная Атака Божественного Зверя
		1049, // Огненное Дыхание Божественного Зверя
		1050, // Рев Божественного Зверя
		1051, // Благословение Тела
		1052, // Благословение Духа
		1053, // Ускорение
		1054, // Проницательность
		1055, // Чистота
		1056, // Воодушевление
		1057, // Дикая Магия
		1058, // Шепот Смерти
		1059, // Фокусировка
		1060, // Наведение
		1061, // Удар Смерти
		1062, // Двойная Атака
		1063, // Вихревая Атака
		1064, // Метеоритный дождь
		1065, // Пробуждение
		1066, // Удар Молнии
		1067, // Молния
		1068, // Световая Волна
		1069, // Вспышка
		1070, // Контроль Эффекта
		1071, // Мощный удар
		1072, // Проникающая Атака
		1073, // Яростный Ветер
		1074, // Удар Копьем
		1075, // Боевой Клич
		1076, // Мощное Сокрушение
		1077, // Шаровая Молния
		1078, // Шоковая Волна
		1079, // Вой
		1080, // Прилив Феникса
		1081, // Очищение Феникса
		1082, // Пылающее Перо Феникса
		1083, // Пылающий Клюв Феникса
		1084, // Смена Режима
		1086, // Натиск Пантеры
		1087, // Темный Коготь Пантеры
		1088, // Смертоносный Коготь Пантеры
		1089, // Хвост
		1090, // Укус Ездового Дракона
		1091, // Устрашение Ездового Дракона
		1092, // Рывок Ездового Дракона
		1093, // Удар Магвена
		1094, // Легкая Походка Магвена
		1095, // Мощный Удар Магвена
		1096, // Легкая Походка Элитного Магвена
		1097, // Возвращение Магвена
		1098, // Групповое Возвращение Магвена
		1099, // Атака
		1100, // Перемещение
		1101, // Прекращение
		1102, // Отмена  призыва
		1103, // Пассивность
		1104, // Защита
		1106, // Коготь Медведя
		1107, // Топот Медведя
		1108, // Укус Кугуара
		1109, // Прыжок Кугуара
		1110, // Прикосновение Потрошителя
		1111, // Сила Потрошителя
		1113, // Львиный Рев
		1114, // Львиный Коготь
		1115, // Львиный Бросок
		1116, // Львиное Пламя
		1117, // Полет Громового Змея
		1118, // Очищение Громового Змея
		1120, // Стрельба Перьями Громового Змея
		1121, // Острые Когти Громового Змея
		1122, // Благословение Жизни
		1123, // Осадный Удар
		1124, // Агрессия Кошки
		1125, // Кошачье Оглушение
		1126, // Укус Кошки
		1127, // Атакующий Прыжок Кошки
		1128, // Прикосновение Кошки
		1129, // Сила Кошки
		1130, // Агрессия Единорога
		1131, // Оглушение Единорога
		1132, // Укус Единорога
		1133, // Атакующий Прыжок Единорога
		1134, // Прикосновение Единорога
		1135, // Сила Единорога
		1136, // Агрессия Фантома
		1137, // Фантомное Оглушение
		1138, // Укус Фантома
		1139, // Атакующий Прыжок Фантома
		1140, // Прикосновение Фантома
		1141, // Сила Фантома
		1142, // Рев Пантеры
		1143, // Стремительный Бросок Пантеры
		1144, // Атакующий Прыжок Коммандо
		1145, // Двойное Сокрушение Коммандо
		1146, // Стихийное Сокрушение
		1147, // Мощь Кошки Колдуньи
		1148, // Стремительный Бросок Лансера
		1149, // Клеймо Силы
		1150, // Массовая Заморозка
		1151, // Мощь Херувима
		1152, // Призрачный Удар Меча
		1153, // Призрачный Импульс
		1154, // Призрачный Шип
		1155, // Призрачное Разрушение
		5000, // Погладить
		5001, // Искушение Света Розы
		5002, // Запредельное Искушение
		5003, // Удар Молнии
		5004, // Молния
		5005, // Световая волна
		5006, // Контроль Эффекта
		5007, // Проникающая Атака
		5008, // Вихревая Атака
		5009, // Сокрушение
		5010, // Боевой Клич
		5011, // Мощное Сокрушение
		5012, // Шаровая Молния
		5013, // Шоковая Волна
		5014, // Воспламенение
		5015, // Смена Режима
		5016, // Усиление Кота-Рейнджера
	};

	private final int[] actions;
	private final int[] actionsHF;
	
	public ExBasicActionList(Player activeChar)
	{
		actions = activeChar.getTransformation() == 0 ? BASIC_ACTIONS : TRANSFORMATION_ACTIONS;
		actionsHF = activeChar.getTransformation() == 0 ? BASIC_ACTIONS_HF : TRANSFORMATION_ACTIONS_HF;
	}
	
	@Override
	protected void writeImpl()
	{
		writeDD(actions, true);
	}
	
	@Override
	protected void writeImplHF()
	{
		writeDD(actionsHF, true);
	}
}