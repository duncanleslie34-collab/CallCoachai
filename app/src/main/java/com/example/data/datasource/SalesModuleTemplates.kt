package com.example.data.datasource

import com.example.data.model.*

data class SalesModuleTemplate(
    val templateId: String,
    val templateName: String,
    val telecomOffer: String,
    val description: String,
    val suggestedCategory: ScenarioCategory = ScenarioCategory.SALES_AND_UPGRADE,
    val suggestedDifficulty: DifficultyLevel = DifficultyLevel.INTERMEDIATE,
    val personaName: String,
    val personaAge: Int,
    val personaOccupation: String,
    val emotionalState: String,
    val initialAnger: Int,
    val reasonForCalling: String,
    val desiredOutcome: String,
    val scenarioBrief: String,
    val scriptTips: List<String>,
    val objectives: List<CallObjective>,
    val debriefQuestions: List<DebriefQuestion>
)

object SalesModuleTemplates {

    val templates: List<SalesModuleTemplate> = listOf(
        SalesModuleTemplate(
            templateId = "tmpl_mobile_bogo",
            templateName = "Spectrum Mobile BOGO Cross-Sell",
            telecomOffer = "Spectrum Mobile Unlimited (Buy 1 Line, Get 1 Free for 12 Mos)",
            description = "Service-to-Sales pivot from internet customer support into wireless cross-sell with $1,200/yr savings pitch.",
            personaName = "Derek Campbell",
            personaAge = 38,
            personaOccupation = "Accountant & Homeowner",
            emotionalState = "Content & Cost-Conscious",
            initialAnger = 20,
            reasonForCalling = "Checking account bill date after resolving routine modem reset.",
            desiredOutcome = "Verify billing schedule, and open to legitimate ways to reduce household telecom expenses.",
            scenarioBrief = "Derek is a loyal Spectrum Internet customer paying $175/mo with AT&T for 2 mobile lines. The agent must resolve his inquiry, use a permission-based bridge, uncover his wireless bill, and pitch the Spectrum Mobile BOGO offer.",
            scriptTips = listOf(
                "Permission Bridge: 'Now that your internet connection is verified, while I have your account pulled up, may I ask which wireless carrier you use for your cell phones?'",
                "Needs Discovery: 'How much are they billing you each month for those two smartphone lines?'",
                "Value Pitch: 'Because you have Spectrum Internet, you qualify for Spectrum Mobile Unlimited on nationwide 5G for just $29.99/mo, with your second line free for a full year. That saves you over $1,200 annually.'",
                "Assumptive Close: 'We can activate your phones immediately over eSIM so you keep your current numbers and devices with zero activation fee. Shall we start that line setup?'"
            ),
            objectives = listOf(
                CallObjective("obj_t1_1", "Permission-Based Transition", "Ask customer permission before transitioning from service to wireless savings.", true),
                CallObjective("obj_t1_2", "Uncover Current Carrier Spend", "Probe exact monthly cost and line count with competitor wireless provider.", true),
                CallObjective("obj_t1_3", "Pitch BOGO Unlimited Offer", "Explain $29.99/mo rate with second line free for 12 months, calculating $1,200+ annual savings.", true),
                CallObjective("obj_t1_4", "Assumptive eSIM Close", "Close with immediate eSIM activation and keeping current phone numbers.", true)
            ),
            debriefQuestions = listOf(
                DebriefQuestion("dq_t1_1", "How smooth was the agent's permission bridge into wireless discovery?", "Candidate should ask permission rather than immediately pitching products."),
                DebriefQuestion("dq_t1_2", "Did the agent quantify annual dollar savings ($1,200/yr) or just state line fees?", "Look for total value framing.")
            )
        ),
        SalesModuleTemplate(
            templateId = "tmpl_gig_speed",
            templateName = "Spectrum Internet Gig Speed Upsell",
            telecomOffer = "Internet Gig (1,000 Mbps) + Advanced WiFi 6 Mesh Pods",
            description = "Upselling buffering customers to Gigabit speeds by diagnosing household device saturation.",
            personaName = "Marcus Brody",
            personaAge = 29,
            personaOccupation = "Software Engineer & Heavy Streamer",
            emotionalState = "Frustrated by Evening Latency",
            initialAnger = 45,
            reasonForCalling = "Inquiring why his 300 Mbps internet connection slows down during evening peak hours.",
            desiredOutcome = "Eliminate evening buffering during video calls and gaming without massive budget increase.",
            scenarioBrief = "Marcus has 300 Mbps base internet. In the evening, his family connects 18+ smart home devices, two 4K video streams, and a remote work station. The agent must diagnose bandwidth choke and upgrade him to Spectrum Gig.",
            scriptTips = listOf(
                "Device Count Diagnosis: 'Between smart TVs, work laptops, security cameras, and smartphones, how many devices are actively connected to your WiFi at night?'",
                "Bandwidth Explanation: 'Your 300 Mbps speed is shared among all 18 devices simultaneously. When multiple 4K streams run, each device competes for bandwidth, causing latency spikes.'",
                "Value Presentation: 'Spectrum Internet Gig delivers 1,000 Mbps downstream plus enhanced upstream bandwidth. For just $20 more a month—less than 66 cents a day—every device gets dedicated high-speed throughput.'",
                "Zero-Friction Close: 'I can provision the Gig speed upgrade to your modem right now from my system with no technician visit required. Can I activate that for you today?'"
            ),
            objectives = listOf(
                CallObjective("obj_t2_1", "Diagnose Device Saturation", "Count total household devices consuming concurrent bandwidth.", true),
                CallObjective("obj_t2_2", "Differentiate Base vs Gig Speed", "Explain how 1 Gbps downstream eliminates contention among multiple simultaneous 4K streams.", true),
                CallObjective("obj_t2_3", "Overcome $20 Price Objection", "Break down the price to 66 cents per day for lag-free performance.", true)
            ),
            debriefQuestions = listOf(
                DebriefQuestion("dq_t2_1", "Did the agent diagnose device saturation before quoting the speed upgrade?", "Assess consultative technical selling."),
                DebriefQuestion("dq_t2_2", "How did the agent justify the cost to the customer?", "Check for daily value breakdown.")
            )
        ),
        SalesModuleTemplate(
            templateId = "tmpl_contract_buyout",
            templateName = "Spectrum Contract Buyout Switcher",
            telecomOffer = "Spectrum Mobile Multi-Line Contract Buyout (Up to $500/Line)",
            description = "Overcoming competitor device installment penalties to win 3+ mobile port-ins.",
            personaName = "Valerie Vance",
            personaAge = 42,
            personaOccupation = "Nurse Practitioner & Family Manager",
            emotionalState = "Interested but Hesitant",
            initialAnger = 30,
            reasonForCalling = "Asked about mobile rates, but says she cannot switch because she owes $800 on family iPhones with Verizon.",
            desiredOutcome = "Find out how to switch family phones to cut her $230/mo bill without paying $800 out of pocket.",
            scenarioBrief = "Valerie wants to switch 3 family lines to Spectrum Mobile, but is paralyzed by remaining carrier device installment payments. The agent must explain the Spectrum Contract Buyout reimbursement program and guide the switch.",
            scriptTips = listOf(
                "Acknowledge the Barrier: 'I understand completely. Paying off hundreds of dollars on existing phone contracts stops many families from saving money.'",
                "Introduce Contract Buyout: 'That is exactly why Spectrum offers our Contract Buyout program: we will pay off up to $500 per device when you bring 3 or more lines to Spectrum Mobile.'",
                "Calculate Net Savings: 'Right now you pay $230/mo. Three lines on Spectrum Mobile Unlimited cost just $89.97/mo. That is $140/mo in savings—over $1,680 a year back in your pocket.'",
                "Reassure on Process: 'You submit your final carrier bill online, Spectrum reimburses your device balance, and your family keeps your existing phones and phone numbers.'"
            ),
            objectives = listOf(
                CallObjective("obj_t3_1", "Explain Contract Buyout Reimbursement", "Detail how Spectrum reimburses up to $500 per device for 3+ lines.", true),
                CallObjective("obj_t3_2", "Calculate Multi-Line Savings", "Demonstrate $140/mo ($1,680/yr) net reduction in household bills.", true),
                CallObjective("obj_t3_3", "Address Phone Number Retention", "Assure customer that numbers and phones are retained seamlessly.", true)
            ),
            debriefQuestions = listOf(
                DebriefQuestion("dq_t3_1", "How clearly did the agent explain the buyout submission and reimbursement process?", "Evaluate transparency on carrier final bills."),
                DebriefQuestion("dq_t3_2", "Did the agent build sufficient urgency to initiate the port-in today?", "Check for consultative closing.")
            )
        ),
        SalesModuleTemplate(
            templateId = "tmpl_tv_stream",
            templateName = "Spectrum TV Stream & Entertainment Bundle",
            telecomOffer = "Spectrum TV Stream ($39.99/mo) with Disney+ Basic Included",
            description = "Consolidating fragmented app subscriptions into Spectrum TV Stream with zero equipment fees.",
            personaName = "Lucas Morales",
            personaAge = 35,
            personaOccupation = "Digital Media Manager",
            emotionalState = "Skeptical of Traditional Cable",
            initialAnger = 20,
            reasonForCalling = "Calling to inquire about high-speed internet promos, emphasizes that he 'cut the cord' on cable years ago.",
            desiredOutcome = "High-speed internet with access to live sports and Disney+ without needing a rental cable box.",
            scenarioBrief = "Lucas is a cord-cutter who pays for 4 separate apps (Disney+, ESPN+, local news app, live sports app) costing him $65/mo. The agent must position Spectrum TV Stream with Disney+ Basic included for $39.99/mo.",
            scriptTips = listOf(
                "Validate Cord-Cutting: 'I respect that! Most people don't want clunky cable boxes or 2-year contracts anymore.'",
                "Uncover App Fragmentation: 'Which apps are you paying for separately right now to get your live news, Disney+, and sports?'",
                "Position TV Stream: 'Spectrum TV Stream gives you top broadcast networks, live sports, plus Disney+ Basic is bundled in at no extra charge on your smart TV app for just $39.99/mo.'",
                "Zero-Box Pitch: 'No cable boxes, no technician visit, no broadcast surcharges. You stream right through the Spectrum app on Apple TV, Roku, or your smart TV.'"
            ),
            objectives = listOf(
                CallObjective("obj_t4_1", "Uncover Total App Subscription Costs", "Calculate customer's fragmented app expenses.", true),
                CallObjective("obj_t4_2", "Position App-Based TV Stream", "Highlight that TV Stream requires zero cable boxes or equipment fees.", true),
                CallObjective("obj_t4_3", "Highlight Disney+ Included", "Emphasize included Disney+ Basic and live local sports.", true)
            ),
            debriefQuestions = listOf(
                DebriefQuestion("dq_t4_1", "How did the agent validate the customer's cord-cutting preference while still selling TV Stream?", "Look for no-cable-box positioning.")
            )
        )
    )
}
