package com.example.data.remote

import com.example.data.model.ChronologyEvent
import com.example.data.model.MilestoneType
import com.example.data.model.NewsArticle
import com.example.data.model.NewsCategory
import com.example.data.model.NewsSource
import com.example.data.model.TopicStory

object InitialNewsData {

    fun getDefaultSources(): List<NewsSource> {
        return listOf(
            // --- МИР / ГЛОБАЛЬНЫЕ СМИ (20 источников) ---
            NewsSource("bbc_world", "BBC World News", "https://bbc.com/news/world", "https://feeds.bbci.co.uk/news/world/rss.xml", NewsCategory.WORLD, "Великобритания", "🇬🇧", "EN", isEnabled = true, isFavorite = true),
            NewsSource("cnn_world", "CNN International", "https://cnn.com/world", "http://rss.cnn.com/rss/edition_world.rss", NewsCategory.WORLD, "США", "🇺🇸", "EN", isEnabled = true, isFavorite = true),
            NewsSource("reuters_world", "Reuters World", "https://reuters.com/world", "https://www.reutersagency.com/feed/?best-topics=world&post_type=best", NewsCategory.WORLD, "Великобритания", "🇬🇧", "EN", isEnabled = true, isFavorite = true),
            NewsSource("nyt_world", "The New York Times", "https://nytimes.com/section/world", "https://rss.nytimes.com/services/xml/rss/nyt/World.xml", NewsCategory.WORLD, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("guardian_world", "The Guardian", "https://theguardian.com/world", "https://www.theguardian.com/world/rss", NewsCategory.WORLD, "Великобритания", "🇬🇧", "EN", isEnabled = true),
            NewsSource("dw_world", "Deutsche Welle", "https://dw.com", "https://rss.dw.com/rdf/rss-en-all", NewsCategory.WORLD, "Германия", "🇩🇪", "EN", isEnabled = true),
            NewsSource("france24", "France 24", "https://france24.com/en", "https://www.france24.com/en/rss", NewsCategory.WORLD, "Франция", "🇫🇷", "EN", isEnabled = true),
            NewsSource("aljazeera", "Al Jazeera English", "https://aljazeera.com", "https://www.aljazeera.com/xml/rss/all.xml", NewsCategory.WORLD, "Катар", "🇶🇦", "EN", isEnabled = true),
            NewsSource("nhk_world", "NHK World-Japan", "https://www3.nhk.or.jp/nhkworld", "https://www3.nhk.or.jp/nhkworld/en/news/rss/index.xml", NewsCategory.WORLD, "Япония", "🇯🇵", "EN", isEnabled = true),
            NewsSource("cna_singapore", "CNA (Channel NewsAsia)", "https://channelnewsasia.com", "https://www.channelnewsasia.com/api/v1/rss-outbound-feed?_format=xml", NewsCategory.WORLD, "Сингапур", "🇸🇬", "EN", isEnabled = true),
            NewsSource("abc_au", "ABC News Australia", "https://abc.net.au/news", "https://www.abc.net.au/news/feed/51120/rss.xml", NewsCategory.WORLD, "Австралия", "🇦🇺", "EN", isEnabled = true),
            NewsSource("cbc_ca", "CBC News World", "https://cbc.ca/news/world", "https://www.cbc.ca/cmlink/rss-world", NewsCategory.WORLD, "Канада", "🇨🇦", "EN", isEnabled = true),
            NewsSource("lenta", "Лента.ру Главное", "https://lenta.ru", "https://lenta.ru/rss/news", NewsCategory.WORLD, "Россия", "🇷🇺", "RU", isEnabled = true, isFavorite = true),
            NewsSource("bbc_ru", "BBC Русская служба", "https://bbc.com/russian", "https://feeds.bbci.co.uk/russian/rss.xml", NewsCategory.WORLD, "Великобритания", "🇬🇧", "RU", isEnabled = true),
            NewsSource("kommersant", "Коммерсантъ", "https://kommersant.ru", "https://www.kommersant.ru/RSS/news.xml", NewsCategory.WORLD, "Россия", "🇷🇺", "RU", isEnabled = true),
            NewsSource("npr_news", "NPR News", "https://npr.org", "https://feeds.npr.org/1004/rss.xml", NewsCategory.WORLD, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("time_mag", "TIME Magazine", "https://time.com", "https://time.com/feed/", NewsCategory.WORLD, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("el_pais", "El País", "https://elpais.com", "https://feeds.elpais.com/mrss-s/pages/ep/site/elpais.com/portada", NewsCategory.WORLD, "Испания", "🇪🇸", "ES", isEnabled = true),
            NewsSource("spiegel", "Der Spiegel", "https://spiegel.de/international", "https://www.spiegel.de/international/index.rss", NewsCategory.WORLD, "Германия", "🇩🇪", "EN", isEnabled = true),
            NewsSource("swissinfo", "SWI swissinfo.ch", "https://swissinfo.ch", "https://www.swissinfo.ch/eng/rss", NewsCategory.WORLD, "Швейцария", "🇨🇭", "EN", isEnabled = true),

            // --- ТЕХНОЛОГИИ & IT (15 источников) ---
            NewsSource("habr", "Хабр IT & Dev", "https://habr.com", "https://habr.com/ru/rss/hub/all/all/?fl=ru", NewsCategory.TECH, "Россия", "🇷🇺", "RU", isEnabled = true, isFavorite = true),
            NewsSource("techcrunch", "TechCrunch", "https://techcrunch.com", "https://techcrunch.com/feed/", NewsCategory.TECH, "США", "🇺🇸", "EN", isEnabled = true, isFavorite = true),
            NewsSource("the_verge", "The Verge", "https://theverge.com", "https://www.theverge.com/rss/index.xml", NewsCategory.TECH, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("wired", "Wired", "https://wired.com", "https://www.wired.com/feed/rss", NewsCategory.TECH, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("ars_technica", "Ars Technica", "https://arstechnica.com", "https://feeds.arstechnica.com/arstechnica/index", NewsCategory.TECH, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("3dnews", "3DNews Железо & IT", "https://3dnews.ru", "https://3dnews.ru/news/rss/", NewsCategory.TECH, "Россия", "🇷🇺", "RU", isEnabled = true),
            NewsSource("engadget", "Engadget", "https://engadget.com", "https://www.engadget.com/rss.xml", NewsCategory.TECH, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("mit_tech_review", "MIT Technology Review", "https://technologyreview.com", "https://www.technologyreview.com/feed/", NewsCategory.TECH, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("zdnet", "ZDNet", "https://zdnet.com", "https://www.zdnet.com/news/rss.xml", NewsCategory.TECH, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("nine_to_five_mac", "9to5Mac", "https://9to5mac.com", "https://9to5mac.com/feed/", NewsCategory.TECH, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("android_police", "Android Police", "https://androidpolice.com", "https://www.androidpolice.com/feed/", NewsCategory.TECH, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("hacker_news", "Hacker News", "https://news.ycombinator.com", "https://news.ycombinator.com/rss", NewsCategory.TECH, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("cnet_tech", "CNET Tech", "https://cnet.com", "https://www.cnet.com/rss/news/", NewsCategory.TECH, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("toms_hardware", "Tom's Hardware", "https://tomshardware.com", "https://www.tomshardware.com/feeds/all", NewsCategory.TECH, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("venturebeat", "VentureBeat AI", "https://venturebeat.com", "https://venturebeat.com/feed/", NewsCategory.TECH, "США", "🇺🇸", "EN", isEnabled = true),

            // --- БИЗНЕС & ЭКОНОМИКА (10 источников) ---
            NewsSource("wsj_biz", "Wall Street Journal", "https://wsj.com", "https://feeds.a.dj.com/rss/WSJcomUSBusiness.xml", NewsCategory.BUSINESS, "США", "🇺🇸", "EN", isEnabled = true, isFavorite = true),
            NewsSource("ft_biz", "Financial Times", "https://ft.com", "https://www.ft.com/rss/home/uk", NewsCategory.BUSINESS, "Великобритания", "🇬🇧", "EN", isEnabled = true),
            NewsSource("cnbc_biz", "CNBC Markets", "https://cnbc.com", "https://search.cnbc.com/rs/search/view.html?partnerId=2000&keywords=markets&sort=date", NewsCategory.BUSINESS, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("forbes_biz", "Forbes Business", "https://forbes.com", "https://www.forbes.com/business/feed/", NewsCategory.BUSINESS, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("marketwatch", "MarketWatch", "https://marketwatch.com", "https://feeds.content.dowjones.io/public/rss/mw_topstories", NewsCategory.BUSINESS, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("economist", "The Economist", "https://economist.com", "https://www.economist.com/finance-and-economics/rss.xml", NewsCategory.BUSINESS, "Великобритания", "🇬🇧", "EN", isEnabled = true),
            NewsSource("rbc_biz", "РБК Экономика", "https://rbc.ru", "https://rssexport.rbc.ru/rbcnews/news/20/full.rss", NewsCategory.BUSINESS, "Россия", "🇷🇺", "RU", isEnabled = true),
            NewsSource("yahoo_finance", "Yahoo Finance", "https://finance.yahoo.com", "https://finance.yahoo.com/news/rssindex", NewsCategory.BUSINESS, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("quartz", "Quartz Economy", "https://qz.com", "https://qz.com/feed", NewsCategory.BUSINESS, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("bloomberg_market", "Bloomberg Markets", "https://bloomberg.com", "https://feeds.bloomberg.com/markets/news.rss", NewsCategory.BUSINESS, "США", "🇺🇸", "EN", isEnabled = true),

            // --- НАУКА & КОСМОС (8 источников) ---
            NewsSource("nasa", "NASA Breaking News", "https://nasa.gov", "https://www.nasa.gov/news-release/feed/", NewsCategory.SCIENCE, "США", "🇺🇸", "EN", isEnabled = true, isFavorite = true),
            NewsSource("naked_science", "Naked Science", "https://naked-science.ru", "https://naked-science.ru/feed", NewsCategory.SCIENCE, "Россия", "🇷🇺", "RU", isEnabled = true, isFavorite = true),
            NewsSource("space_com", "Space.com", "https://space.com", "https://www.space.com/feeds/all", NewsCategory.SCIENCE, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("sci_american", "Scientific American", "https://scientificamerican.com", "http://rss.sciam.com/ScientificAmerican-Global", NewsCategory.SCIENCE, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("new_scientist", "New Scientist", "https://newscientist.com", "https://www.newscientist.com/feed/home/", NewsCategory.SCIENCE, "Великобритания", "🇬🇧", "EN", isEnabled = true),
            NewsSource("science_daily", "ScienceDaily", "https://sciencedaily.com", "https://www.sciencedaily.com/rss/all.xml", NewsCategory.SCIENCE, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("phys_org", "Phys.org", "https://phys.org", "https://phys.org/rss-feed/", NewsCategory.SCIENCE, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("esa_space", "European Space Agency", "https://esa.int", "https://www.esa.int/rssfeed/Our_Activities/Space_Science", NewsCategory.SCIENCE, "ЕС", "🇪🇺", "EN", isEnabled = true),

            // --- ИГРЫ & МЕДИА (8 источников) ---
            NewsSource("stopgame", "StopGame Новости Игр", "https://stopgame.ru", "https://stopgame.ru/rss/rss_news.xml", NewsCategory.GAMES, "Россия", "🇷🇺", "RU", isEnabled = true, isFavorite = true),
            NewsSource("ign", "IGN Entertainment", "https://ign.com", "https://feeds.feedburner.com/ign/all", NewsCategory.GAMES, "США", "🇺🇸", "EN", isEnabled = true, isFavorite = true),
            NewsSource("gamespot", "GameSpot", "https://gamespot.com", "https://www.gamespot.com/feeds/mashup/", NewsCategory.GAMES, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("kotaku", "Kotaku", "https://kotaku.com", "https://kotaku.com/rss", NewsCategory.GAMES, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("polygon", "Polygon", "https://polygon.com", "https://www.polygon.com/rss/index.xml", NewsCategory.GAMES, "США", "🇺🇸", "EN", isEnabled = true),
            NewsSource("pc_gamer", "PC Gamer", "https://pcgamer.com", "https://www.pcgamer.com/rss/", NewsCategory.GAMES, "Великобритания", "🇬🇧", "EN", isEnabled = true),
            NewsSource("eurogamer", "Eurogamer", "https://eurogamer.net", "https://www.eurogamer.net/feed", NewsCategory.GAMES, "Великобритания", "🇬🇧", "EN", isEnabled = true),
            NewsSource("dtf", "DTF Игры & Кино", "https://dtf.ru", "https://dtf.ru/rss/all", NewsCategory.GAMES, "Россия", "🇷🇺", "RU", isEnabled = true)
        )
    }

    fun getDefaultTopics(): List<TopicStory> {
        val now = System.currentTimeMillis()
        return listOf(
            TopicStory(
                id = "topic_quantum",
                title = "Квантовая революция: архитектура кубитов и сверхбыстрые процессоры",
                summary = "Хроника разработки устойчивых к помехам квантовых микросхем нового поколения и их коммерческого тестирования",
                category = NewsCategory.TECH,
                isPinned = true,
                articleIds = listOf("breaking_01", "tech_02"),
                createdAt = now - 3 * 24 * 3600 * 1000,
                updatedAt = now - 3 * 60 * 1000,
                events = listOf(
                    ChronologyEvent(
                        id = "ev_q1",
                        topicId = "topic_quantum",
                        title = "Теоретическое обоснование топологических кубитов",
                        summary = "Международная группа физиков опубликовала математическую модель снижения электромагнитного шума.",
                        timestamp = now - 2 * 24 * 3600 * 1000,
                        sourceName = "Хабр Наука",
                        milestoneType = MilestoneType.START,
                        articleId = null
                    ),
                    ChronologyEvent(
                        id = "ev_q2",
                        topicId = "topic_quantum",
                        title = "Синтез графеновой подложки и литография чипа",
                        summary = "Инженеры изготовили первые рабочие ячейки с квантовой когерентностью при криогенных температурах.",
                        timestamp = now - 24 * 3600 * 1000,
                        sourceName = "3DNews",
                        milestoneType = MilestoneType.DEVELOPMENT,
                        articleId = "tech_02"
                    ),
                    ChronologyEvent(
                        id = "ev_q3",
                        topicId = "topic_quantum",
                        title = "Прорыв: сверхбыстрый квантовый процессор",
                        summary = "Представлен рабочий кристалл, снизивший уровень ошибок на порядок и готовый к первым индустриальным тестам.",
                        timestamp = now - 3 * 60 * 1000,
                        sourceName = "Хабр Наука",
                        milestoneType = MilestoneType.BREAKING,
                        articleId = "breaking_01"
                    )
                )
            ),
            TopicStory(
                id = "topic_space",
                title = "Миссия телескопа «Джеймс Уэбб»: поиск биомаркеров в глубоком космосе",
                summary = "Развитие астрофизических открытий экзопланет и атмосферных аномалий телескопом Джеймс Уэбб",
                category = NewsCategory.SCIENCE,
                isPinned = false,
                articleIds = listOf("breaking_02", "science_02"),
                createdAt = now - 7 * 24 * 3600 * 1000,
                updatedAt = now - 12 * 60 * 1000,
                events = listOf(
                    ChronologyEvent(
                        id = "ev_s1",
                        topicId = "topic_space",
                        title = "Калибровка инфракрасного спектрометра NIRSpec",
                        summary = "Орбитальная обсерватория завершила тестовые экспозиции в созвездии Льва.",
                        timestamp = now - 5 * 24 * 3600 * 1000,
                        sourceName = "Naked Science",
                        milestoneType = MilestoneType.START,
                        articleId = null
                    ),
                    ChronologyEvent(
                        id = "ev_s2",
                        topicId = "topic_space",
                        title = "Фиксация спектральных полос метана и CO2",
                        summary = "Компьютерная обработка транзитных кривых подтвердила наличие плотной водородной оболочки.",
                        timestamp = now - 2 * 24 * 3600 * 1000,
                        sourceName = "NASA Bulletin",
                        milestoneType = MilestoneType.DEVELOPMENT,
                        articleId = null
                    ),
                    ChronologyEvent(
                        id = "ev_s3",
                        topicId = "topic_space",
                        title = "Обнаружение водяного пара в обитаемой зоне K2-18b",
                        summary = "Официальное подтверждение данных спектроскопии: на экзопланете может существовать теплый жидкий океан.",
                        timestamp = now - 12 * 60 * 1000,
                        sourceName = "Naked Science",
                        milestoneType = MilestoneType.BREAKING,
                        articleId = "breaking_02"
                    )
                )
            ),
            TopicStory(
                id = "topic_climate",
                title = "Глобальная трансформация энергетики и климатические соглашения",
                summary = "Хронология межгосударственных инициатив по переходу на возобновляемые ресурсы и модернизацию сетей",
                category = NewsCategory.WORLD,
                isPinned = false,
                articleIds = listOf("world_01", "biz_01"),
                createdAt = now - 10 * 24 * 3600 * 1000,
                updatedAt = now - 45 * 60 * 1000,
                events = listOf(
                    ChronologyEvent(
                        id = "ev_c1",
                        topicId = "topic_climate",
                        title = "Открытие международного энергетического форума",
                        summary = "Делегации более 100 стран начали обсуждение декарбонизации промышленности.",
                        timestamp = now - 6 * 24 * 3600 * 1000,
                        sourceName = "BBC Мир",
                        milestoneType = MilestoneType.START,
                        articleId = null
                    ),
                    ChronologyEvent(
                        id = "ev_c2",
                        topicId = "topic_climate",
                        title = "Перераспределение суверенных фондов и золотых резервов",
                        summary = "Мировые центробанки диверсифицируют активы в условиях структурной перестройки мировой торговли.",
                        timestamp = now - 45 * 60 * 1000,
                        sourceName = "Лента Финансы",
                        milestoneType = MilestoneType.DEVELOPMENT,
                        articleId = "biz_01"
                    ),
                    ChronologyEvent(
                        id = "ev_c3",
                        topicId = "topic_climate",
                        title = "Подписание декларации об удвоении мощностей ВИЭ к 2030 году",
                        summary = "Более 70 государств одобрили масштабный инвестиционный пакет для зеленой энергетики.",
                        timestamp = now - 65 * 60 * 1000,
                        sourceName = "BBC Мир",
                        milestoneType = MilestoneType.MILESTONE,
                        articleId = "world_01"
                    )
                )
            )
        )
    }

    fun getInitialArticles(): List<NewsArticle> {
        val now = System.currentTimeMillis()
        return listOf(
            NewsArticle(
                id = "breaking_01",
                title = "Квантовые вычисления: исследователи представили сверхбыстрый процессор нового поколения",
                description = "Новая архитектура кубитов позволила снизить уровень шума на порядок и ускорить решение комплексных задач оптимизации в тысячи раз.",
                content = "Международная исследовательская группа совершила ключевой прорыв в области квантовых технологий. Разработанный чип использует топологические кубиты, устойчивые к внешним электромагнитным помехам. Это открывает дорогу к моделированию сложнейших молекул для создания новых лекарств и сверхпроводников при комнатной температуре. Первые коммерческие тесты начнутся уже в следующем квартале.",
                link = "https://habr.com/ru/news/quantum-breakthrough/",
                sourceName = "Хабр Наука",
                sourceUrl = "https://habr.com",
                imageUrl = "https://images.unsplash.com/photo-1635070041078-e363dbe005cb?w=800&auto=format&fit=crop&q=80",
                category = NewsCategory.TECH,
                publishedAt = now - 3 * 60 * 1000, // 3 minutes ago
                isBreaking = true,
                readTimeMinutes = 3,
                topicId = "topic_quantum"
            ),
            NewsArticle(
                id = "breaking_02",
                title = "Космический телескоп «Джеймс Уэбб» зафиксировал следы водяного пара в атмосфере экзопланеты",
                description = "Планета находится в обитаемой зоне своей звезды, а спектральный анализ показал присутствие метана и углекислого газа.",
                content = "Астрофизики опубликовали сенсационные данные спектроскопии экзопланеты K2-18b. Телескоп «Джеймс Уэбб» обнаружил явные сигнатуры водяного пара и углеродсодержащих соединений. Планета относится к классу субнептунов, обладающих водородной атмосферой и, возможно, океаном жидкой воды. Учёные готовят серию дополнительных наблюдений в инфракрасном диапазоне.",
                link = "https://naked-science.ru/article/astronomy/webb-exoplanet",
                sourceName = "Naked Science",
                sourceUrl = "https://naked-science.ru",
                imageUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&auto=format&fit=crop&q=80",
                category = NewsCategory.SCIENCE,
                publishedAt = now - 12 * 60 * 1000, // 12 minutes ago
                isBreaking = true,
                readTimeMinutes = 4,
                topicId = "topic_space"
            ),
            NewsArticle(
                id = "tech_01",
                title = "Вышла новая версия языка Kotlin с ускоренным компилятором K2 и новыми возможностями Compose",
                description = "Обновление ускоряет сборку проектов на 40% и добавляет глубокую оптимизацию генерации кода для мобильных платформ.",
                content = "Команда разработчиков объявила о масштабном обновлении экосистемы Kotlin. Компилятор K2 стал доступен по умолчанию, обеспечивая значительный прирост производительности на многопоточных сборочных серверах. В Jetpack Compose улучшено инкрементальное обновление UI, управление ресурсами и интеграция с мультиплатформенными библиотеками.",
                link = "https://habr.com/ru/post/kotlin-update/",
                sourceName = "Хабр Разработка",
                sourceUrl = "https://habr.com",
                imageUrl = "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800&auto=format&fit=crop&q=80",
                category = NewsCategory.TECH,
                publishedAt = now - 28 * 60 * 1000,
                isBreaking = false,
                readTimeMinutes = 2,
                topicId = null
            ),
            NewsArticle(
                id = "biz_01",
                title = "Мировые центробанки наращивают золотые резервы на фоне трансформации торговых расчётов",
                description = "Доля золота в международных резервах достигла рекордных значений за последние 15 лет благодаря диверсификации активов.",
                content = "Аналитики инвестиционных банков отмечают продолжающийся тренд на дедолларизацию расчетов между развивающимися экономиками. Центральные банки за последние 6 месяцев закупили свыше 400 тонн драгоценного металла. Одновременно растут объемы взаиморасчетов в национальных валютах и цифровых финансовых активах.",
                link = "https://lenta.ru/rubrics/economics/",
                sourceName = "Лента Финансы",
                sourceUrl = "https://lenta.ru",
                imageUrl = "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=800&auto=format&fit=crop&q=80",
                category = NewsCategory.BUSINESS,
                publishedAt = now - 45 * 60 * 1000,
                isBreaking = false,
                readTimeMinutes = 3,
                topicId = "topic_climate"
            ),
            NewsArticle(
                id = "world_01",
                title = "Саммит по климатической энергетике: принят глобальный план перехода на возобновляемые источники",
                description = "Страны договорились удвоить инвестиции в ветровые и солнечные электростанции до 2030 года.",
                content = "Более 70 государств подписали рамочное соглашение о поддержке инноваций в зеленой энергетике. Основной акцент сделан на разработке промышленных накопителей энергии на основе натрий-ионных аккумуляторов и модернизации магистральных электросетей для снижения потерь при передаче.",
                link = "https://bbc.com/russian/news",
                sourceName = "BBC Мир",
                sourceUrl = "https://bbc.com",
                imageUrl = "https://images.unsplash.com/photo-1466611653911-95081537e5b7?w=800&auto=format&fit=crop&q=80",
                category = NewsCategory.WORLD,
                publishedAt = now - 65 * 60 * 1000,
                isBreaking = false,
                readTimeMinutes = 4,
                topicId = "topic_climate"
            ),
            NewsArticle(
                id = "games_01",
                title = "Анонсировано продолжение культовой ролевой саги на движке Unreal Engine 5.5",
                description = "Разработчики показали первый геймплейный трейлер с фотореалистичной физикой разрушений и симуляцией живого открытого мира.",
                content = "Студия представила 15-минутный ролик с демонстрацией революционных технологий освещения Lumen и геометрии Nanite нового поколения. Игроков ждет бесшовный открытый мир размером более 120 квадратных километров без экранов загрузки, динамическая смена погодных условий и продвинутый искусственный интеллект спутников.",
                link = "https://stopgame.ru/news",
                sourceName = "StopGame",
                sourceUrl = "https://stopgame.ru",
                imageUrl = "https://images.unsplash.com/photo-1538481199705-c710c4e965fc?w=800&auto=format&fit=crop&q=80",
                category = NewsCategory.GAMES,
                publishedAt = now - 85 * 60 * 1000,
                isBreaking = false,
                readTimeMinutes = 2,
                topicId = null
            ),
            NewsArticle(
                id = "science_02",
                title = "Биологи расшифровали механизм регенерации нейронов у глубоководных организмов",
                description = "Открытие специфических белков-шаперонов может стать основой для терапии нейродегенеративных заболеваний человека.",
                content = "Ученые Института молекулярной генетики исследовали свойства клеток медуз и гидр, способных восстанавливать утраченные участки нервной ткани. Эксперименты показали, что активация аналогичного сигнального пути в культуре клеток млекопитающих стимулирует рост аксонов на 300%.",
                link = "https://naked-science.ru/article/biology/neuron-regeneration",
                sourceName = "Naked Science",
                sourceUrl = "https://naked-science.ru",
                imageUrl = "https://images.unsplash.com/photo-1507668077129-56e32842fceb?w=800&auto=format&fit=crop&q=80",
                category = NewsCategory.SCIENCE,
                publishedAt = now - 110 * 60 * 1000,
                isBreaking = false,
                readTimeMinutes = 5,
                topicId = "topic_space"
            ),
            NewsArticle(
                id = "tech_02",
                title = "Представлены графеновые аккумуляторы с зарядкой от 0 до 80% всего за 6 минут",
                description = "Технология выдерживает более 3000 циклов зарядки без потери ёмкости и безопасна при высоких температурах.",
                content = "Инженеры продемонстрировали готовые к серийному производству ячейки для электромобилей и потребительской электроники. Использование графеновой решетки исключает образование дендритов, вызывающих короткие замыкания в традиционных литиевых аккумуляторах.",
                link = "https://3dnews.ru/news",
                sourceName = "3DNews",
                sourceUrl = "https://3dnews.ru",
                imageUrl = "https://images.unsplash.com/photo-1558441719-7e50284ab527?w=800&auto=format&fit=crop&q=80",
                category = NewsCategory.TECH,
                publishedAt = now - 140 * 60 * 1000,
                isBreaking = false,
                readTimeMinutes = 3,
                topicId = "topic_quantum"
            )
        )
    }
}
