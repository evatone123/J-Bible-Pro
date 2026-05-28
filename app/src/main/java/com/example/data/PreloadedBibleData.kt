package com.example.data

object PreloadedBibleData {

    fun getPreloadedPlans(): List<ReadingPlan> {
        return listOf(
            ReadingPlan(
                planId = "daily_wisdom",
                title = "Daily Wisdom Challenge",
                description = "Deepen your understanding and walk in peace with key highlights from the Book of Proverbs over 5 days.",
                totalDays = 5,
                versesPerDayJson = """
                    [
                        {"day": 1, "passage": "Proverbs 3:1-2", "notes": "Set your heart on guiding virtues."},
                        {"day": 2, "passage": "Proverbs 3:3-4", "notes": "Let mercy and truth never leave you."},
                        {"day": 3, "passage": "Proverbs 3:5-6", "notes": "Trust in the LORD with all your heart."},
                        {"day": 4, "passage": "Proverbs 3:7-8", "notes": "Fear the LORD and depart from evil."},
                        {"day": 5, "passage": "Proverbs 3:9-10", "notes": "Honor the LORD with all your wealth."}
                    ]
                """.trimIndent()
            ),
            ReadingPlan(
                planId = "life_of_jesus",
                title = "The Life & Teachings of Jesus",
                description = "Explore the incarnation, purpose, and foundational teaching of Jesus Christ in John and Matthew over 5 days.",
                totalDays = 5,
                versesPerDayJson = """
                    [
                        {"day": 1, "passage": "John 1:1-5", "notes": "The Word in the beginning."},
                        {"day": 2, "passage": "John 1:14-18", "notes": "The Word became flesh."},
                        {"day": 3, "passage": "Matthew 5:1-6", "notes": "Blessings of the Beatitudes part 1."},
                        {"day": 4, "passage": "Matthew 5:7-12", "notes": "Blessings of the Beatitudes part 2."},
                        {"day": 5, "passage": "Matthew 5:13-16", "notes": "Being Salt and Light to the world."}
                    ]
                """.trimIndent()
            ),
            ReadingPlan(
                planId = "creation_comfort",
                title = "Creation & Comfort",
                description = "Ponder the majestic creation of the world and find restful hope in standard comfort Psalms over 5 days.",
                totalDays = 5,
                versesPerDayJson = """
                    [
                        {"day": 1, "passage": "Genesis 1:1-5", "notes": "In the beginning: creation of light."},
                        {"day": 2, "passage": "Genesis 1:26-31", "notes": "Creation of humanity in His image."},
                        {"day": 3, "passage": "Psalms 23:1-3", "notes": "The LORD is my Shepherd: restoration."},
                        {"day": 4, "passage": "Psalms 23:4-6", "notes": "I will fear no evil: goodness and mercy."},
                        {"day": 5, "passage": "Psalms 121:1-8", "notes": "My help comes from the LORD: He watches over you."}
                    ]
                """.trimIndent()
            )
        )
    }

    fun getPreloadedVerses(): List<BibleVerse> {
        val list = mutableListOf<BibleVerse>()

        // --- GENESIS 1 ---
        val genesisWEB = listOf(
            1 to "In the beginning, God created the heavens and the earth.",
            2 to "The earth was formless and empty. Darkness was on the surface of the deep and God’s Spirit was hovering over the surface of the waters.",
            3 to "God said, “Let there be light,” and there was light.",
            4 to "God saw the light, and saw that it was good. God divided the light from the darkness.",
            5 to "God called the light “day”, and the darkness he called “night”. There was evening and there was morning, one day.",
            6 to "God said, “Let there be an expanse in the middle of the waters, and let it divide the waters from the waters.”",
            7 to "God made the expanse, and divided the waters which were under the expanse from the waters which were above the expanse; and it was so.",
            8 to "God called the expanse “sky”. There was evening and there was morning, a second day.",
            9 to "God said, “Let the waters under the sky be gathered together to one place, and let the dry land appear;” and it was so.",
            10 to "God called the dry land “earth”, and the gathering together of the waters he called “seas”. God saw that it was good.",
            26 to "God said, “Let us make man in our image, after our likeness: and let them have dominion over the fish of the sea, and over the birds of the sky, and over the livestock, and over all the earth, and over every creeping thing that creeps on the earth.”",
            27 to "God created man in his own image. In God’s image he created him; male and female he created them.",
            28 to "God blessed them. God said to them, “Be fruitful, multiply, fill the earth, and subdue it. Have dominion over the fish of the sea, over the birds of the sky, and over every living thing that moves on the earth.”",
            31 to "God saw everything that he had made, and behold, it was very good. There was evening and there was morning, a sixth day."
        )

        val genesisKJV = listOf(
            1 to "In the beginning God created the heaven and the earth.",
            2 to "And the earth was without form, and void; and darkness was upon the face of the deep. And the Spirit of God moved upon the face of the waters.",
            3 to "And God said, Let there be light: and there was light.",
            4 to "And God saw the light, that it was good: and God divided the light from the darkness.",
            5 to "And God called the light Day, and the darkness he called Night. And the evening and the morning were the first day.",
            6 to "And God said, Let there be a firmament in the midst of the waters, and let it divide the waters from the waters.",
            7 to "And God made the firmament, and divided the waters which were under the firmament from the waters which were above the firmament: and it was so.",
            8 to "And God called the firmament Heaven. And the evening and the morning were the second day.",
            9 to "And God said, Let the waters under the heaven be gathered together unto one place, and let the dry land appear: and it was so.",
            10 to "And God called the dry land Earth; and the gathering together of the waters called he Seas: and God saw that it was good.",
            26 to "And God said, Let us make man in our image, after our likeness: and let them have dominion over the fish of the sea, and over the fowl of the air, and over the cattle, and over all the earth, and over every creeping thing that creepeth upon the earth.",
            27 to "So God created man in his own image, in the image of God created he him; male and female created he them.",
            28 to "And God blessed them, and God said unto them, Be fruitful, and multiply, and replenish the earth, and subdue it: and have dominion over the fish of the sea, and over the fowl of the air, and over every living thing that moveth upon the earth.",
            31 to "And God saw every thing that he had made, and, behold, it was very good. And the evening and the morning were the sixth day."
        )

        // Add Genesis 1 verses
        genesisWEB.forEach { (v, txt) ->
            list.add(BibleVerse(translation = "WEB", book = "Genesis", bookId = 1, chapter = 1, verse = v, text = txt))
        }
        genesisKJV.forEach { (v, txt) ->
            list.add(BibleVerse(translation = "KJV", book = "Genesis", bookId = 1, chapter = 1, verse = v, text = txt))
        }

        // --- PSALMS 23 ---
        val psalms23WEB = listOf(
            1 to "The LORD is my shepherd: I shall not lack.",
            2 to "He makes me lie down in green pastures. He leads me beside still waters.",
            3 to "He restores my soul. He guides me in the paths of righteousness for his name’s sake.",
            4 to "Even though I walk through the valley of the shadow of death, I will fear no evil, for you are with me. Your rod and your staff, they comfort me.",
            5 to "You prepare a table before me in the presence of my enemies. You anoint my head with oil. My cup runs over.",
            6 to "Surely goodness and loving kindness shall follow me all the days of my life, and I will dwell in the LORD’s house forever."
        )

        val psalms23KJV = listOf(
            1 to "The LORD is my shepherd; I shall not want.",
            2 to "He maketh me to lie down in green pastures: he leadeth me beside the still waters.",
            3 to "He restoreth my soul: he leadeth me in the paths of righteousness for his name's sake.",
            4 to "Yea, though I walk through the valley of the shadow of death, I will fear no evil: for thou art with me; thy rod and thy staff they comfort me.",
            5 to "Thou preparest a table before me in the presence of mine enemies: thou anointest my head with oil; my cup runneth over.",
            6 to "Surely goodness and mercy shall follow me all the days of my life: and I will dwell in the house of the LORD for ever."
        )

        psalms23WEB.forEach { (v, txt) ->
            list.add(BibleVerse(translation = "WEB", book = "Psalms", bookId = 19, chapter = 23, verse = v, text = txt))
        }
        psalms23KJV.forEach { (v, txt) ->
            list.add(BibleVerse(translation = "KJV", book = "Psalms", bookId = 19, chapter = 23, verse = v, text = txt))
        }

        // --- PSALMS 121 ---
        val psalms121WEB = listOf(
            1 to "I will lift up my eyes to the hills. Where does my help come from?",
            2 to "My help comes from the LORD, who made heaven and earth.",
            3 to "He will not allow your foot to be moved. He who keeps you will not slumber.",
            4 to "Behold, he who keeps Israel will neither slumber nor sleep.",
            5 to "The LORD is your keeper. The LORD is your shade on your right hand.",
            6 to "The sun will not strike you by day, nor the moon by night.",
            7 to "The LORD will keep you from all evil. He will keep your soul.",
            8 to "The LORD will keep your going out and your coming in, from this time forth, and forever more."
        )

        val psalms121KJV = listOf(
            1 to "I will lift up mine eyes unto the hills, from whence cometh my help.",
            2 to "My help cometh from the LORD, which made heaven and earth.",
            3 to "He will not suffer thy foot to be moved: he that keepeth thee will not slumber.",
            4 to "Behold, he that keepeth Israel shall neither slumber nor sleep.",
            5 to "The LORD is thy keeper: the LORD is thy shade upon thy right hand.",
            6 to "The sun shall not smite thee by day, nor the moon by night.",
            7 to "The LORD shall preserve thee from all evil: he shall preserve thy soul.",
            8 to "The LORD shall preserve thy going out and thy coming in from this time forth, and even for evermore."
        )

        psalms121WEB.forEach { (v, txt) ->
            list.add(BibleVerse(translation = "WEB", book = "Psalms", bookId = 19, chapter = 121, verse = v, text = txt))
        }
        psalms121KJV.forEach { (v, txt) ->
            list.add(BibleVerse(translation = "KJV", book = "Psalms", bookId = 19, chapter = 121, verse = v, text = txt))
        }

        // --- PROVERBS 3 ---
        val proverbs3WEB = listOf(
            1 to "My son, don’t forget my teaching, but let your heart keep my commandments;",
            2 to "for length of days, years of life, and peace, they will add to you.",
            3 to "Don’t let kindness and truth forsake you. Bind them around your neck. Write them on the tablet of your heart.",
            4 to "So you will find favor and good understanding in the sight of God and man.",
            5 to "Trust in the LORD with all your heart, and don’t lean on your own understanding.",
            6 to "In all your ways acknowledge him, and he will make your paths straight.",
            7 to "Don’t be wise in your own eyes. Fear the LORD, and depart from evil.",
            8 to "It will be body health, and nourishment to your bones.",
            9 to "Honor the LORD with your substance, and with the first fruits of all your increase;",
            10 to "so your barns will be filled with plenty, and your vats will overflow with new wine."
        )

        val proverbs3KJV = listOf(
            1 to "My son, forget not my law; but let thine heart keep my commandments:",
            2 to "For length of days, and long life, and peace, shall they add to thee.",
            3 to "Let not mercy and truth forsake thee: bind them about thy neck; write them upon the table of thine heart:",
            4 to "So shalt thou find favour and good understanding in the sight of God and man.",
            5 to "Trust in the LORD with all thine heart; and lean not unto thine own understanding.",
            6 to "In all thy ways acknowledge him, and he shall direct thy paths.",
            7 to "Be not wise in thine own eyes: fear the LORD, and depart from evil.",
            8 to "It shall be health to thy navel, and marrow to thy bones.",
            9 to "Honour the LORD with thy substance, and with the firstfruits of all thine increase:",
            10 to "So shall thy barns be filled with plenty, and thy presses shall burst out with new wine."
        )

        proverbs3WEB.forEach { (v, txt) ->
            list.add(BibleVerse(translation = "WEB", book = "Proverbs", bookId = 20, chapter = 3, verse = v, text = txt))
        }
        proverbs3KJV.forEach { (v, txt) ->
            list.add(BibleVerse(translation = "KJV", book = "Proverbs", bookId = 20, chapter = 3, verse = v, text = txt))
        }

        // --- JOHN 1 ---
        val john1WEB = listOf(
            1 to "In the beginning was the Word, and the Word was with God, and the Word was God.",
            2 to "The same was in the beginning with God.",
            3 to "All things were made through him. Without him was not anything made that has been made.",
            4 to "In him was life, and the life was the light of men.",
            5 to "The light shines in the darkness, and the darkness hasn’t overcome it.",
            14 to "The Word became flesh, and lived among us. We saw his glory, such glory as of the one and only Son of the Father, full of grace and truth.",
            16 to "From his fullness we all received grace upon grace.",
            17 to "For the law was given through Moses. Grace and truth came through Jesus Christ.",
            18 to "No one has seen God at any time. The one and only Son, who is in the bosom of the Father, he has declared him."
        )

        val john1KJV = listOf(
            1 to "In the beginning was the Word, and the Word was with God, and the Word was God.",
            2 to "The same was in the beginning with God.",
            3 to "All things were made by him; and without him was not any thing made that was made.",
            4 to "In him was life; and the life was the light of men.",
            5 to "And the light shineth in darkness; and the darkness comprehended it not.",
            14 to "And the Word was made flesh, and dwelt among us, (and we beheld his glory, the glory as of the only begotten of the Father,) full of grace and truth.",
            16 to "And of his fulness have all we received, and grace for grace.",
            17 to "For the law was given by Moses, but grace and truth came by Jesus Christ.",
            18 to "No man hath seen God at any time; the only begotten Son, which is in the bosom of the Father, he hath declared him."
        )

        john1WEB.forEach { (v, txt) ->
            list.add(BibleVerse(translation = "WEB", book = "John", bookId = 43, chapter = 1, verse = v, text = txt))
        }
        john1KJV.forEach { (v, txt) ->
            list.add(BibleVerse(translation = "KJV", book = "John", bookId = 43, chapter = 1, verse = v, text = txt))
        }

        // --- MATTHEW 5 ---
        val matthew5WEB = listOf(
            1 to "Seeing the multitudes, he went up onto the mountain. When he had sat down, his disciples came to him.",
            2 to "He opened his mouth and taught them, saying:",
            3 to "“Blessed are the poor in spirit, for theirs is the Kingdom of Heaven.",
            4 to "Blessed are those who mourn, for they shall be comforted.",
            5 to "Blessed are the gentle, for they shall inherit the earth.",
            6 to "Blessed are those who hunger and thirst after righteousness, for they shall be filled.",
            7 to "Blessed are the merciful, for they shall obtain mercy.",
            8 to "Blessed are the pure in heart, for they shall see God.",
            9 to "Blessed are the peacemakers, for they shall be called children of God.",
            10 to "Blessed are those who have been persecuted for righteousness’ sake, for theirs is the Kingdom of Heaven.",
            11 to "Blessed are you when people reproach you, persecute you, and say all kinds of evil against you falsely for my sake.",
            12 to "Rejoice, and be exceedingly glad, for great is your reward in heaven. For that is how they persecuted the prophets who were before you.",
            13 to "“You are the salt of the earth, but if the salt has lost its flavor, with what will it be salted? It is then good for nothing, but to be cast out and trodden under foot of men.",
            14 to "You are the light of the world. A city set on a hill can’t be hidden.",
            15 to "Neither do you light a lamp, and put it under a measuring basket, but on a stand; and it shines to all who are in the house.",
            16 to "Even so, let your light shine before men; that they may see your good works, and glorify your Father who is in heaven.”"
        )

        val matthew5KJV = listOf(
            1 to "And seeing the multitudes, he went up into a mountain: and when he was set, his disciples came unto him:",
            2 to "And he opened his mouth, and taught them, saying,",
            3 to "Blessed are the poor in spirit: for theirs is the kingdom of heaven.",
            4 to "Blessed are they that mourn: for they shall be comforted.",
            5 to "Blessed are the meek: for they shall inherit the earth.",
            6 to "Blessed are they which do hunger and thirst after righteousness: for they shall be filled.",
            7 to "Blessed are the merciful: for they shall obtain mercy.",
            8 to "Blessed are the pure in heart: for they shall see God.",
            9 to "Blessed are the peacemakers: for they shall be called the children of God.",
            10 to "Blessed are they which are persecuted for righteousness' sake: for theirs is the kingdom of heaven.",
            11 to "Blessed are ye, when men shall revile you, and persecute you, and shall say all manner of evil against you falsely, for my sake.",
            12 to "Rejoice, and be exceeding glad: for great is your reward in heaven: for so persecuted they the prophets which were before you.",
            13 to "Ye are the salt of the earth: but if the salt have lost his savour, wherewith shall it be salted? it is thenceforth good for nothing, but to be cast out, and to be trodden under foot of men.",
            14 to "Ye are the light of the world. A city that is set on an hill cannot be hid.",
            15 to "Neither do men light a candle, and put it under a bushel, but on a candlestick; and it giveth light unto all that are in the house.",
            16 to "Let your light so shine before men, that they may see your good works, and glorify your Father which is in heaven."
        )

        matthew5WEB.forEach { (v, txt) ->
            list.add(BibleVerse(translation = "WEB", book = "Matthew", bookId = 40, chapter = 5, verse = v, text = txt))
        }
        matthew5KJV.forEach { (v, txt) ->
            list.add(BibleVerse(translation = "KJV", book = "Matthew", bookId = 40, chapter = 5, verse = v, text = txt))
        }

        return list
    }
}
