package com.example.flashmind.core.data.repository

import com.example.flashmind.core.database.entity.CardEntity
import com.example.flashmind.core.database.entity.DeckEntity
import java.time.Instant
import java.time.temporal.ChronoUnit

internal object SampleData {
    fun decks(): List<DeckEntity> = listOf(
        DeckEntity(
            id = "deck-daily",
            title = "Tu vung giao tiep hang ngay",
            description = "40 tu va cum tu co nghia tieng Viet de dung trong sinh hoat va hoi thoai co ban.",
        ),
        DeckEntity(
            id = "deck-academic",
            title = "Tu vung hoc thuat",
            description = "20 tu hoc thuat thong dung cho IELTS, doc hieu va viet hoc thuat.",
        ),
        DeckEntity(
            id = "deck-travel",
            title = "Tu vung du lich",
            description = "20 tu va cum tu can thiet khi di san bay, khach san va di chuyen.",
        ),
        DeckEntity(
            id = "deck-work",
            title = "Tu vung cong so",
            description = "20 tu vung dung trong email, hop va giao tiep noi cong so.",
        ),
        DeckEntity(
            id = "deck-phrasal",
            title = "Phrasal verbs pho bien",
            description = "20 phrasal verbs thong dung kem nghia tieng Viet va cau vi du ngan.",
        ),
    )

    fun cards(): List<CardEntity> {
        val now = Instant.now()
        return buildList {
            addAll(
                listOf(
                    card(now, 1, "deck-daily", "actually", "thuc ra", "Actually, I prefer studying in the morning."),
                    card(now, 2, "deck-daily", "almost", "gan nhu", "I almost missed the bus this morning."),
                    card(now, 3, "deck-daily", "arrive", "den noi", "Please arrive ten minutes early."),
                    card(now, 4, "deck-daily", "borrow", "muon", "Can I borrow your pen for a minute?"),
                    card(now, 5, "deck-daily", "busy", "ban ron", "She is busy with her final project today."),
                    card(now, 6, "deck-daily", "careful", "can than", "Be careful when you cross the street."),
                    card(now, 7, "deck-daily", "choose", "chon", "You can choose any seat you like."),
                    card(now, 8, "deck-daily", "comfortable", "thoai mai", "These shoes are comfortable for walking."),
                    card(now, 9, "deck-daily", "decide", "quyet dinh", "We need to decide before Friday."),
                    card(now, 10, "deck-daily", "delicious", "ngon", "This soup is simple but delicious."),
                    card(now, 11, "deck-daily", "different", "khac nhau", "My opinion is different from yours."),
                    card(now, 12, "deck-daily", "early", "som", "I woke up early to review my notes."),
                    card(now, 13, "deck-daily", "enough", "du", "Do we have enough time to finish?"),
                    card(now, 14, "deck-daily", "friendly", "than thien", "The new neighbor is very friendly."),
                    card(now, 15, "deck-daily", "happen", "xay ra", "What happened after the meeting ended?"),
                    card(now, 16, "deck-daily", "important", "quan trong", "Sleep is important for your memory."),
                    card(now, 17, "deck-daily", "improve", "cai thien", "Reading daily will improve your vocabulary."),
                    card(now, 18, "deck-daily", "instead", "thay vao do", "Let's stay home instead of going out."),
                    card(now, 19, "deck-daily", "invite", "moi", "They invited me to dinner last night."),
                    card(now, 20, "deck-daily", "join", "tham gia", "Would you like to join our study group?"),
                    card(now, 21, "deck-daily", "lazy", "luoi bieng", "I feel lazy on rainy afternoons."),
                    card(now, 22, "deck-daily", "mention", "de cap", "He mentioned your name in class."),
                    card(now, 23, "deck-daily", "notice", "nhan ra", "Did you notice the new poster outside?"),
                    card(now, 24, "deck-daily", "offer", "de nghi", "She offered me a cup of tea."),
                    card(now, 25, "deck-daily", "perhaps", "co le", "Perhaps we should try a different method."),
                    card(now, 26, "deck-daily", "prepare", "chuan bi", "I need to prepare for tomorrow's quiz."),
                    card(now, 27, "deck-daily", "quiet", "yen lang", "The library is quiet in the evening."),
                    card(now, 28, "deck-daily", "receive", "nhan duoc", "I received your message this morning."),
                    card(now, 29, "deck-daily", "remember", "nho", "Do you remember his phone number?"),
                    card(now, 30, "deck-daily", "share", "chia se", "Please share the file with the group."),
                    card(now, 31, "deck-daily", "suddenly", "dot nhien", "It suddenly started to rain."),
                    card(now, 32, "deck-daily", "suggest", "de xuat", "I suggest taking a short break."),
                    card(now, 33, "deck-daily", "tidy", "gon gang", "Keep your desk tidy and clean."),
                    card(now, 34, "deck-daily", "useful", "huu ich", "This app is useful for daily review."),
                    card(now, 35, "deck-daily", "usual", "thuong le", "I took my usual route home."),
                    card(now, 36, "deck-daily", "worry", "lo lang", "Don't worry about small mistakes."),
                    card(now, 37, "deck-daily", "yet", "chua", "I haven't finished my homework yet."),
                    card(now, 38, "deck-daily", "agree", "dong y", "I agree with your final answer."),
                    card(now, 39, "deck-daily", "avoid", "tranh", "Try to avoid using your phone while studying."),
                    card(now, 40, "deck-daily", "support", "ho tro", "My friends always support my goals."),
                ),
            )
            addAll(
                listOf(
                    card(now, 41, "deck-academic", "analyze", "phan tich", "Students must analyze the data carefully."),
                    card(now, 42, "deck-academic", "approach", "cach tiep can", "This article suggests a new approach to language learning."),
                    card(now, 43, "deck-academic", "assess", "danh gia", "Teachers assess progress at the end of each unit."),
                    card(now, 44, "deck-academic", "assume", "gia dinh", "We cannot assume that all learners study the same way."),
                    card(now, 45, "deck-academic", "concept", "khai niem", "The concept is simple but powerful."),
                    card(now, 46, "deck-academic", "consistent", "nhat quan", "Consistent practice improves long-term memory."),
                    card(now, 47, "deck-academic", "context", "ngu canh", "Words are easier to remember in context."),
                    card(now, 48, "deck-academic", "derive", "rut ra", "Several conclusions can be derived from the results."),
                    card(now, 49, "deck-academic", "evidence", "bang chung", "The argument lacks strong evidence."),
                    card(now, 50, "deck-academic", "factor", "yeu to", "Sleep is an important factor in learning."),
                    card(now, 51, "deck-academic", "interpret", "dien giai", "Please interpret the chart in your own words."),
                    card(now, 52, "deck-academic", "maintain", "duy tri", "It is hard to maintain focus for three hours."),
                    card(now, 53, "deck-academic", "method", "phuong phap", "This method helps learners review faster."),
                    card(now, 54, "deck-academic", "outcome", "ket qua", "The outcome was better than expected."),
                    card(now, 55, "deck-academic", "precise", "chinh xac", "Use precise language in academic writing."),
                    card(now, 56, "deck-academic", "relevant", "lien quan", "Only include relevant examples in your essay."),
                    card(now, 57, "deck-academic", "significant", "dang ke", "There was a significant increase in scores."),
                    card(now, 58, "deck-academic", "theory", "ly thuyet", "The theory explains how memory works."),
                    card(now, 59, "deck-academic", "valid", "hop le", "Your conclusion is valid if the data is correct."),
                    card(now, 60, "deck-academic", "variable", "bien so", "Time is the key variable in this experiment."),
                ),
            )
            addAll(
                listOf(
                    card(now, 61, "deck-travel", "aisle seat", "ghe ngoai gan loi di", "I booked an aisle seat for the long flight."),
                    card(now, 62, "deck-travel", "baggage claim", "khu nhan hanh ly", "We waited at baggage claim for twenty minutes."),
                    card(now, 63, "deck-travel", "boarding pass", "the len may bay", "Please show your boarding pass at the gate."),
                    card(now, 64, "deck-travel", "check in", "lam thu tuc", "We need to check in two hours before departure."),
                    card(now, 65, "deck-travel", "customs", "hai quan", "The officer at customs asked a few questions."),
                    card(now, 66, "deck-travel", "departure", "gio khoi hanh", "The departure time changed this morning."),
                    card(now, 67, "deck-travel", "destination", "diem den", "Bangkok is our final destination."),
                    card(now, 68, "deck-travel", "exchange rate", "ty gia hoi doai", "Check the exchange rate before buying currency."),
                    card(now, 69, "deck-travel", "itinerary", "lich trinh", "Our itinerary includes three cities in five days."),
                    card(now, 70, "deck-travel", "journey", "hanh trinh", "The journey took almost twelve hours."),
                    card(now, 71, "deck-travel", "landmark", "dia danh noi tieng", "This temple is a famous local landmark."),
                    card(now, 72, "deck-travel", "luggage", "hanh ly", "My luggage was heavier than expected."),
                    card(now, 73, "deck-travel", "passport", "ho chieu", "Keep your passport in a safe place."),
                    card(now, 74, "deck-travel", "reservation", "dat cho", "I made a hotel reservation online."),
                    card(now, 75, "deck-travel", "return ticket", "ve khu hoi", "Some countries require a return ticket."),
                    card(now, 76, "deck-travel", "schedule", "lich trinh", "Let's check the train schedule again."),
                    card(now, 77, "deck-travel", "single room", "phong don", "I'd like to book a single room for two nights."),
                    card(now, 78, "deck-travel", "souvenir", "qua luu niem", "She bought a small souvenir for her sister."),
                    card(now, 79, "deck-travel", "tour guide", "huong dan vien", "The tour guide spoke very clearly."),
                    card(now, 80, "deck-travel", "visa", "thi thuc", "You may need a visa before entering the country."),
                ),
            )
            addAll(
                listOf(
                    card(now, 81, "deck-work", "agenda", "chuong trinh hop", "Please read the agenda before the meeting."),
                    card(now, 82, "deck-work", "assign", "phan cong", "The manager will assign tasks this afternoon."),
                    card(now, 83, "deck-work", "colleague", "dong nghiep", "My colleague helped me finish the report."),
                    card(now, 84, "deck-work", "deadline", "han chot", "The deadline for this proposal is Friday."),
                    card(now, 85, "deck-work", "delegate", "uy quyen", "Leaders should delegate work effectively."),
                    card(now, 86, "deck-work", "draft", "ban nhap", "I sent the first draft to my supervisor."),
                    card(now, 87, "deck-work", "efficient", "hieu qua", "We need a more efficient workflow."),
                    card(now, 88, "deck-work", "feedback", "phan hoi", "Thank you for your honest feedback."),
                    card(now, 89, "deck-work", "follow up", "theo doi lai", "I will follow up with the client tomorrow."),
                    card(now, 90, "deck-work", "goal", "muc tieu", "Our main goal is to improve retention."),
                    card(now, 91, "deck-work", "hire", "tuyen dung", "The company plans to hire two designers."),
                    card(now, 92, "deck-work", "issue", "van de", "We need to solve this issue quickly."),
                    card(now, 93, "deck-work", "launch", "ra mat", "The team will launch the feature next month."),
                    card(now, 94, "deck-work", "negotiate", "thuong luong", "They negotiated a better price with the supplier."),
                    card(now, 95, "deck-work", "priority", "uu tien", "Customer support is our top priority."),
                    card(now, 96, "deck-work", "proposal", "de xuat", "Her proposal was accepted by the board."),
                    card(now, 97, "deck-work", "recruit", "tuyen mo", "It is hard to recruit experienced engineers."),
                    card(now, 98, "deck-work", "strategy", "chien luoc", "The marketing strategy needs revision."),
                    card(now, 99, "deck-work", "update", "cap nhat", "Please update the spreadsheet today."),
                    card(now, 100, "deck-work", "workload", "khoi luong cong viec", "My workload is heavier this week."),
                ),
            )
            addAll(
                listOf(
                    card(now, 101, "deck-phrasal", "break down", "bi hong; suy sup", "My old laptop broke down during class."),
                    card(now, 102, "deck-phrasal", "bring up", "de cap den", "She brought up an interesting idea in the meeting."),
                    card(now, 103, "deck-phrasal", "call off", "huy bo", "They called off the trip because of the storm."),
                    card(now, 104, "deck-phrasal", "carry on", "tiep tuc", "Please carry on with your presentation."),
                    card(now, 105, "deck-phrasal", "catch up", "bat kip", "I need to catch up on my reading tonight."),
                    card(now, 106, "deck-phrasal", "come across", "tinh co gap", "I came across a useful article online."),
                    card(now, 107, "deck-phrasal", "figure out", "tim ra", "Can you figure out the answer by yourself?"),
                    card(now, 108, "deck-phrasal", "fill in", "dien vao", "Please fill in the missing information."),
                    card(now, 109, "deck-phrasal", "find out", "tim hieu ra", "I found out why the app was crashing."),
                    card(now, 110, "deck-phrasal", "get along", "hoa hop", "Do you get along with your roommates?"),
                    card(now, 111, "deck-phrasal", "give up", "tu bo", "Don't give up after one bad result."),
                    card(now, 112, "deck-phrasal", "go over", "xem lai", "Let's go over the vocabulary once more."),
                    card(now, 113, "deck-phrasal", "look after", "cham soc", "She looks after her younger brother every day."),
                    card(now, 114, "deck-phrasal", "look for", "tim kiem", "I am looking for a quieter place to study."),
                    card(now, 115, "deck-phrasal", "make up", "bịa ra; lam hoa", "He made up an excuse for being late."),
                    card(now, 116, "deck-phrasal", "pick up", "nhat len; hoc duoc", "I picked up a few new words from the podcast."),
                    card(now, 117, "deck-phrasal", "put off", "tri hoan", "Don't put off your revision until Sunday."),
                    card(now, 118, "deck-phrasal", "run out of", "het", "We ran out of time at the end of the test."),
                    card(now, 119, "deck-phrasal", "set up", "thiet lap", "He set up a study schedule for the month."),
                    card(now, 120, "deck-phrasal", "turn down", "tu choi; van nho", "She turned down the job offer politely."),
                ),
            )
        }
    }

    private fun card(
        now: Instant,
        index: Int,
        deckId: String,
        front: String,
        back: String,
        exampleSentence: String,
        pronunciation: String? = null,
    ): CardEntity {
        val isDue = index % 5 != 0
        val nextReviewAt = if (isDue) {
            now.minus((index % 3 + 1).toLong(), ChronoUnit.DAYS)
        } else {
            now.plus((index % 4 + 1).toLong(), ChronoUnit.DAYS)
        }
        return CardEntity(
            id = "card-$index",
            deckId = deckId,
            front = front,
            back = back,
            pronunciation = pronunciation,
            exampleSentence = exampleSentence,
            imageUrl = null,
            audioUrl = null,
            isStarred = index % 7 == 0,
            repetition = if (isDue) index % 3 else (index % 4) + 1,
            intervalDays = if (isDue) 0 else (index % 6) + 1,
            easeFactor = 2.5,
            nextReviewAt = nextReviewAt.toEpochMilli(),
            lastReviewedAt = if (index % 4 == 0) now.minus(1, ChronoUnit.DAYS).toEpochMilli() else null,
        )
    }
}


