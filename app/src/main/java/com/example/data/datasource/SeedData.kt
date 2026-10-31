package com.example.data.datasource

import com.example.data.model.*

object SeedData {

    val trainees = listOf(
        TraineeProfile(
            id = "trainee_1",
            name = "Alex Rivera",
            email = "alex.rivera@enterprise-support.com",
            role = "New Hire Agent",
            department = "Tier-1 Customer Escalations",
            hireWeeksAgo = 1,
            avatarColorHex = 0xFF2563EB
        ),
        TraineeProfile(
            id = "trainee_2",
            name = "Jordan Smith",
            email = "jordan.s@enterprise-support.com",
            role = "Customer Retention Specialist",
            department = "Retention & Renewals",
            hireWeeksAgo = 2,
            avatarColorHex = 0xFF059669
        ),
        TraineeProfile(
            id = "trainee_3",
            name = "Taylor Chen",
            email = "t.chen@enterprise-support.com",
            role = "Support Associate",
            department = "Billing & Account Care",
            hireWeeksAgo = 3,
            avatarColorHex = 0xFF7C3AED
        ),
        TraineeProfile(
            id = "trainee_4",
            name = "Samira Khan",
            email = "samira.k@enterprise-support.com",
            role = "Technical Support Agent",
            department = "Broadband & Hardware",
            hireWeeksAgo = 4,
            avatarColorHex = 0xFFD97706
        )
    )

    val personas: List<CustomerPersona> = listOf(
        CustomerPersona(
            id = "persona_1",
            name = "Marcus Vance",
            age = 42,
            occupation = "Regional Sales Director",
            emotionalState = "Furious & Belligerent",
            initialAngerLevel = 95,
            backstory = "Just landed from a 14-hour business trip in Europe. Received an automated SMS notification of a $180 international roaming surcharge despite believing his company plan covered EU data.",
            reasonForCalling = "Disputing $180 international data fee and threatening to cancel 25 corporate company phone lines.",
            desiredOutcome = "Immediate 100% waiver of roaming fees and a guarantee this never happens again.",
            triggerWords = listOf("policy", "fine print", "you should have checked", "contract terms", "calm down"),
            soothingWords = listOf("I understand how shocking this is", "I'm going to take personal ownership", "credit", "review the account together"),
            category = ScenarioCategory.BILLING_DISPUTE,
            avatarInitials = "MV"
        ),
        CustomerPersona(
            id = "persona_2",
            name = "Brenda Miller",
            age = 72,
            occupation = "Retired Schoolteacher",
            emotionalState = "Confused & Highly Anxious",
            initialAngerLevel = 45,
            backstory = "Her grandchildren are visiting for the weekend and she promised they could watch their favorite movies, but the television app keeps showing 'Error Code 403 - Device Limit Exceeded'.",
            reasonForCalling = "Cannot log into video streaming app on her smart TV and feels overwhelmed by tech terminology.",
            desiredOutcome = "Patience, plain-English instructions without jargon, and getting the movie to play right now.",
            triggerWords = listOf("reboot your router", "cache", "DNS", "firmware", "just do it online"),
            soothingWords = listOf("Take your time", "We will do this together step by step", "No problem at all Brenda", "I am right here with you"),
            category = ScenarioCategory.TECH_SUPPORT,
            avatarInitials = "BM"
        ),
        CustomerPersona(
            id = "persona_3",
            name = "Chloe Lin",
            age = 29,
            occupation = "Startup Co-Founder & CEO",
            emotionalState = "Panicked & Urgently Demanding",
            initialAngerLevel = 88,
            backstory = "Her company's dedicated commercial fiber line went dark 15 minutes before an all-hands seed funding pitch to Tier-1 venture capital investors.",
            reasonForCalling = "Enterprise internet outage causing catastrophic risk to business operations right now.",
            desiredOutcome = "Instant dispatch or network rerouting, explicit ETA within 10 minutes, and executive credit guarantee.",
            triggerWords = listOf("estimated window 24-48 hours", "file a ticket", "wait your turn", "unforeseen outage"),
            soothingWords = listOf("Critical priority escalated", "Emergency dispatch", "Immediate backup LTE failover activation", "I am staying on this"),
            category = ScenarioCategory.TECH_SUPPORT,
            avatarInitials = "CL"
        ),
        CustomerPersona(
            id = "persona_4",
            name = "David Sterling",
            age = 54,
            occupation = "Architectural Consultant",
            emotionalState = "Distrustful & Resigned to Churn",
            initialAngerLevel = 75,
            backstory = "Has been a loyal paying customer for 8 straight years with zero late payments. His annual renewal invoice just arrived showing a 35% stealth price increase from $59/mo to $80/mo.",
            reasonForCalling = "Calling to cancel his subscription immediately after discovering competitor 'NovaNet' offers identical service for $45/mo.",
            desiredOutcome = "Either price-match the legacy rate or process immediate penalty-free cancellation.",
            triggerWords = listOf("inflation adjustment", "standard rates", "competitor is lower quality", "early termination fee"),
            soothingWords = listOf("8 years of loyalty", "valued member", "loyalty legacy rate locked in", "personalized discount"),
            category = ScenarioCategory.RETENTION,
            avatarInitials = "DS"
        ),
        CustomerPersona(
            id = "persona_5",
            name = "Elena Rostova",
            age = 36,
            occupation = "Hospitality Manager",
            emotionalState = "Sarcastic & Thoroughly Disgusted",
            initialAngerLevel = 92,
            backstory = "Has called customer service three times over the past 48 hours for a payroll integration bug. Was transferred between departments three times and accidentally disconnected after 40 minutes on hold.",
            reasonForCalling = "Extremely frustrated by call center runaround and demands to speak to a Senior Operations Director.",
            desiredOutcome = "A sincere apology acknowledging previous agent negligence, a concrete supervisor follow-up, and immediate issue resolution.",
            triggerWords = listOf("I will transfer you", "not my department", "you need to hold", "sorry for the inconvenience"),
            soothingWords = listOf("I am not transferring you anywhere", "I am handling this personally", "You have been treated unfairly and I apologize", "I will see this through"),
            category = ScenarioCategory.DE_ESCALATION,
            avatarInitials = "ER"
        ),
        CustomerPersona(
            id = "persona_6",
            name = "Gary Hopkins",
            age = 48,
            occupation = "HVAC Contractor & Small Business Owner",
            emotionalState = "Stressed & Frantic",
            initialAngerLevel = 80,
            backstory = "Ordered an emergency furnace replacement motor guaranteed overnight delivery for an elderly client without heat in freezing weather. Tracking says 'Delayed in transit - pending warehouse scan'.",
            reasonForCalling = "Package delay putting an elderly resident at freeze risk and threatening Gary's business reputation.",
            desiredOutcome = "Local depot courier override or immediate same-day local supplier replacement pickup authorization.",
            triggerWords = listOf("weather exception", "beyond our control", "check back tomorrow", "standard 3-5 days"),
            soothingWords = listOf("Priority emergency hot-shot delivery", "Local warehouse pickup voucher", "I recognize the human urgency", "Let's find the closest part in stock"),
            category = ScenarioCategory.DE_ESCALATION,
            avatarInitials = "GH"
        ),
        CustomerPersona(
            id = "persona_7",
            name = "Hannah Washington",
            age = 26,
            occupation = "Freelance Graphic Designer",
            emotionalState = "Defensive & Annoyed",
            initialAngerLevel = 65,
            backstory = "Signed up for a 14-day free trial software license. Forgot to cancel 2 days ago and was billed the annual upfront fee of $249. Claims she never used the product after day 1.",
            reasonForCalling = "Disputing annual charge, demanding full reversal, claiming subscription email notice was deceptive.",
            desiredOutcome = "Immediate refund of the $249 charge and account cancellation confirmation.",
            triggerWords = listOf("non-refundable", "auto-renewal terms", "you agreed at signup", "no exceptions"),
            soothingWords = listOf("I can see you haven't utilized the software", "Full courtesy refund", "I understand how easy it is to forget trial dates", "Processed right away"),
            category = ScenarioCategory.BILLING_DISPUTE,
            avatarInitials = "HW"
        ),
        CustomerPersona(
            id = "persona_8",
            name = "Ian Gallagher",
            age = 39,
            occupation = "Automotive Mechanic",
            emotionalState = "Indignant & Suspicious",
            initialAngerLevel = 82,
            backstory = "Purchased a commercial diagnostic scanner for $850. Exactly 4 days after the 1-year limited warranty expired, the touchscreen controller completely failed.",
            reasonForCalling = "Demanding warranty repair exception; accuses the manufacturer of planned obsolescence and unfair build quality.",
            desiredOutcome = "Complimentary out-of-warranty repair or free certified refurbished replacement unit.",
            triggerWords = listOf("warranty is warranty", "out of warranty fee $350", "nothing we can do", "buy the new model"),
            soothingWords = listOf("4 days is a reasonable grace period", "Goodwill warranty extension", "You invested significant money in our equipment", "Let's get this repaired under courtesy coverage"),
            category = ScenarioCategory.DE_ESCALATION,
            avatarInitials = "IG"
        ),
        CustomerPersona(
            id = "persona_9",
            name = "Jasmine Patel",
            age = 31,
            occupation = "E-commerce Operations Specialist",
            emotionalState = "Overwhelmed & Skeptical",
            initialAngerLevel = 70,
            backstory = "Her team relied on the legacy order-routing dashboard. Over the weekend, the company pushed a mandatory v3.0 redesign that hid batch export shortcuts, cutting her team's throughput in half.",
            reasonForCalling = "Expressing deep dissatisfaction with product changes, threatening to transition company's 40 seats to a rival CRM.",
            desiredOutcome = "Enabling legacy layout toggle, direct coaching on new workflow shortcuts, and product team feedback submission.",
            triggerWords = listOf("the new way is better", "everyone loves the update", "get used to it", "legacy is retired"),
            soothingWords = listOf("I hear how disruptive this was to your workflow", "Let me show you the hidden shortcut", "One-month grace access toggle", "I'm escalating your workflow friction directly to our VP of Product"),
            category = ScenarioCategory.RETENTION,
            avatarInitials = "JP"
        ),
        CustomerPersona(
            id = "persona_10",
            name = "Kevin O'Connor",
            age = 58,
            occupation = "Senior Managing Director, Global Capital",
            emotionalState = "Authoritative & Demanding",
            initialAngerLevel = 90,
            backstory = "Enterprise Platinum account holder whose multi-factor authentication (2FA) token was locked following a phone replacement, locking his executive team out of compliance files.",
            reasonForCalling = "Demanding immediate emergency MFA bypass or instant biometric reset without standard 24-hour security delay.",
            desiredOutcome = "White-glove executive verification, immediate bypass authorization, and SLA breach credit.",
            triggerWords = listOf("standard security verification queue", "wait 24 hours", "no executive exceptions", "submit a ticket"),
            soothingWords = listOf("Platinum SLA Priority protocol", "Immediate live supervisor video verification", "Dedicated corporate liaison", "We will have your access unlocked within 5 minutes"),
            category = ScenarioCategory.VIP_SERVICE,
            avatarInitials = "KO"
        ),
        CustomerPersona(
            id = "persona_11",
            name = "Tyler Brooks",
            age = 34,
            occupation = "Software Consultant",
            emotionalState = "Receptive & Cost-Conscious",
            initialAngerLevel = 25,
            backstory = "Has Spectrum 300Mbps Internet at home. Called to verify a 5-minute modem restart earlier today. Pays $85/line for 2 smartphones with Verizon and has never heard of Spectrum Mobile.",
            reasonForCalling = "Checking if his home connection is stable, but mentions his total telecom spend is overwhelming.",
            desiredOutcome = "Verify connection stability, and open to legitimate ways to reduce his $170/mo Verizon wireless bill.",
            triggerWords = listOf("sign a 3-year contract", "mandatory bundle", "read the fine print"),
            soothingWords = listOf("Spectrum Mobile Unlimited", "uses the exact same nationwide 5G network", "save over $1,000 per year", "Buy One Get One Free line", "no contracts"),
            category = ScenarioCategory.SALES_AND_UPGRADE,
            avatarInitials = "TB"
        ),
        CustomerPersona(
            id = "persona_12",
            name = "Morgan Reynolds",
            age = 31,
            occupation = "Marketing Specialist & New Homeowner",
            emotionalState = "Curious & Inquisitive",
            initialAngerLevel = 15,
            backstory = "Moving into a new home in Spectrum territory this Friday. Currently has AT&T DSL at old apartment and received a Spectrum mover flyer. Also pays for Disney+ and sports streaming separately.",
            reasonForCalling = "Shopping for new home internet and TV service. Wants fast self-install and reliable speeds for remote work.",
            desiredOutcome = "Transparent bundle quote, free Disney+ inclusion, free Advanced WiFi, and installation ready for move-in day.",
            triggerWords = listOf("surprise technician fee", "equipment rental surcharge", "price jumps after 3 months"),
            soothingWords = listOf("Spectrum One Bundle", "free Advanced WiFi", "free Unlimited Mobile line for 12 months", "Disney+ Basic included", "no annual contracts"),
            category = ScenarioCategory.SALES_AND_UPGRADE,
            avatarInitials = "MR"
        ),
        CustomerPersona(
            id = "persona_13",
            name = "Zachary Cruz",
            age = 24,
            occupation = "Graphic Designer & Twitch Streamer",
            emotionalState = "Frustrated by Lag Spikes",
            initialAngerLevel = 55,
            backstory = "Has base 300 Mbps Spectrum Internet. His two roommates stream 4K movies and game simultaneously, causing severe latency spikes during his evening client video uploads.",
            reasonForCalling = "Calling tech support to complain that his internet is 'dropping speeds' in the evening and asking why ping is high.",
            desiredOutcome = "Eliminate evening buffering, achieve low latency for streaming and gaming without exorbitant costs.",
            triggerWords = listOf("standard speeds are up to", "congestion is normal", "nothing is wrong on our end"),
            soothingWords = listOf("Internet Gig upgrade", "1,000 Mbps bandwidth", "WiFi 6 Advanced WiFi Mesh Pods", "dedicated priority upstream", "only $20/mo upgrade"),
            category = ScenarioCategory.SALES_AND_UPGRADE,
            avatarInitials = "ZC"
        ),
        CustomerPersona(
            id = "persona_14",
            name = "Samantha Bell",
            age = 44,
            occupation = "Healthcare Clinic Administrator",
            emotionalState = "Cautious & Price-Sensitive",
            initialAngerLevel = 35,
            backstory = "Managing 3 phone lines on T-Mobile paying $215/mo. Wants to switch to Spectrum Mobile to save money, but is locked into $750 in remaining device payment plans.",
            reasonForCalling = "Checking Spectrum Mobile family rates, but explains she cannot switch right now because her phones are locked under carrier device installments.",
            desiredOutcome = "Learn if Spectrum will help pay off her carrier phones so her family can switch immediately.",
            triggerWords = listOf("you have to pay T-Mobile first", "not our problem", "wait until your phones are paid off"),
            soothingWords = listOf("Spectrum Contract Buyout", "we pay off up to $500 per phone", "keep your phone numbers", "eSIM instant activation", "cut your monthly bill by half"),
            category = ScenarioCategory.SALES_AND_UPGRADE,
            avatarInitials = "SB"
        )
    )

    val defaultScenarios: List<CallScenario> = listOf(
        CallScenario(
            id = "scen_1",
            title = "Surprise International Data Surcharge",
            category = ScenarioCategory.BILLING_DISPUTE,
            difficulty = DifficultyLevel.ADVANCED,
            persona = personas[0], // Marcus Vance
            scenarioBrief = "Marcus Vance is enraged by a $180 roaming surcharge received upon returning from a business trip. He threatens to cancel all 25 company lines unless refunded immediately.",
            targetObjectives = listOf(
                CallObjective("obj_1", "De-escalate High Rage", "Lower customer anger from 95% to below 40% using sincere empathy and personal accountability.", true),
                CallObjective("obj_2", "Avoid Trigger Phrases", "Do not cite 'fine print', 'contract policy', or tell the customer to 'calm down'.", true),
                CallObjective("obj_3", "Retain 25 Corporate Lines", "Probe customer travel patterns and configure automatic RoamEasy protection to secure renewal.", true)
            ),
            debriefQuestions = listOf(
                DebriefQuestion("dq_1", "What specific empathy statement did you use to defuse Marcus's initial rage?", "Look for emotional validation rather than policy defensive answers."),
                DebriefQuestion("dq_2", "How did you balance company fiscal policy with customer lifetime value (LTV)?", "Candidate should explain why a $180 fee waiver protects a high-value account.")
            )
        ),
        CallScenario(
            id = "scen_2",
            title = "8-Year Loyalty Price Shock Churn Threat",
            category = ScenarioCategory.RETENTION,
            difficulty = DifficultyLevel.INTERMEDIATE,
            persona = personas[3], // David Sterling
            scenarioBrief = "David Sterling has been a customer for 8 years and received a 35% renewal price jump. He is armed with competitor NovaNet's $45/mo promotional rate.",
            targetObjectives = listOf(
                CallObjective("obj_4", "Validate Loyalty Tenure", "Acknowledge David's 8 years of loyalty before discussing any numbers.", true),
                CallObjective("obj_5", "Execute Retention Value Ladder", "Highlight bundled features and reliability advantages before offering a custom loyalty price match.", true),
                CallObjective("obj_6", "Secure Long-term Contract", "Transition him into an annual price-lock retention agreement.", false)
            ),
            debriefQuestions = listOf(
                DebriefQuestion("dq_3", "Did you immediately cave on price, or did you reinforce unique value first?", "Agent should explore feature usage before discounting."),
                DebriefQuestion("dq_4", "What words showed David his 8 years of loyalty genuinely matter?", "Assess authentic appreciation vs canned scripts.")
            )
        ),
        CallScenario(
            id = "scen_3",
            title = "The Repeated Transfer Meltdown",
            category = ScenarioCategory.DE_ESCALATION,
            difficulty = DifficultyLevel.ADVANCED,
            persona = personas[4], // Elena Rostova
            scenarioBrief = "Elena Rostova was transferred 3 times and hung up on after 40 minutes on hold. She is sarcastic, furious, and demands a supervisor on the first sentence.",
            targetObjectives = listOf(
                CallObjective("obj_7", "Take Unconditional Ownership", "Assure Elena immediately that she will not be transferred and you will personally resolve her issue.", true),
                CallObjective("obj_8", "Sincere Apology for Company Failure", "Offer a genuine apology for the lost time and runaround without blaming other agents.", true),
                CallObjective("obj_9", "Solve Root Issue Directly", "Investigate and resolve the payroll synchronization glitch during the call.", true)
            ),
            debriefQuestions = listOf(
                DebriefQuestion("dq_5", "How did you prevent an immediate demand for escalation to a supervisor?", "Agent should demonstrate competence and radical ownership."),
                DebriefQuestion("dq_6", "What is the danger of saying 'I apologize for the previous agent's mistake'?", "Discuss presenting a unified company front.")
            )
        ),
        CallScenario(
            id = "scen_4",
            title = "Senior Citizen Digital Jargon Overwhelm",
            category = ScenarioCategory.TECH_SUPPORT,
            difficulty = DifficultyLevel.BEGINNER,
            persona = personas[1], // Brenda Miller
            scenarioBrief = "72-year-old Brenda Miller cannot open the TV app for her visiting grandkids. She feels confused, anxious, and intimidated by modern technology.",
            targetObjectives = listOf(
                CallObjective("obj_10", "Zero Tech Jargon", "Avoid words like router, cache, DNS, reboot, and firmware.", true),
                CallObjective("obj_11", "Step-by-Step Pacing", "Give only one single instruction at a time and wait for Brenda's confirmation.", true),
                CallObjective("obj_12", "Reassurance & Encouragement", "Keep tone exceptionally warm, warmhearted, and patient throughout.", true)
            ),
            debriefQuestions = listOf(
                DebriefQuestion("dq_7", "How did you simplify complex technical concepts into everyday terms?", "Evaluate metaphors used like 'unplug the little black box for 10 seconds'."),
                DebriefQuestion("dq_8", "Did you check for understanding after each instruction?", "Observe micro-confirmations.")
            )
        ),
        CallScenario(
            id = "scen_5",
            title = "Emergency Logistics Delay For Vulnerable Client",
            category = ScenarioCategory.DE_ESCALATION,
            difficulty = DifficultyLevel.ADVANCED,
            persona = personas[5], // Gary Hopkins
            scenarioBrief = "Gary Hopkins needs an emergency furnace repair motor for an elderly resident in freezing temperatures. His guaranteed overnight package is delayed.",
            targetObjectives = listOf(
                CallObjective("obj_13", "Recognize Real-World Urgency", "Immediately validate the safety and freezing weather threat.", true),
                CallObjective("obj_14", "Creative Problem Solving", "Locate alternative local supply or authorize urgent local vendor pickup instead of reciting tracking status.", true)
            ),
            debriefQuestions = listOf(
                DebriefQuestion("dq_9", "Why is reading a tracking screen unacceptable in high-stakes logistics?", "Emphasize outcome-driven agency.")
            )
        ),
        CallScenario(
            id = "scen_6",
            title = "Expired Warranty Good-Will Appeal",
            category = ScenarioCategory.DE_ESCALATION,
            difficulty = DifficultyLevel.INTERMEDIATE,
            persona = personas[7], // Ian Gallagher
            scenarioBrief = "Ian Gallagher's $850 diagnostic scanner broke 4 days after the 1-year warranty ended. He is angry and feels taken advantage of.",
            targetObjectives = listOf(
                CallObjective("obj_15", "Acknowledge Proximity to Warranty", "Validate that 4 days past warranty is understandably frustrating for an $850 tool.", true),
                CallObjective("obj_16", "Deliver Courtesy Exception", "Present a manufacturer goodwill repair coverage program.", true)
            ),
            debriefQuestions = listOf(
                DebriefQuestion("dq_10", "How did you avoid sounding like a robotic rule enforcer?", "Evaluate conversational empathy.")
            )
        ),
        CallScenario(
            id = "scen_7",
            title = "Accidental Subscription Auto-Renewal Dispute",
            category = ScenarioCategory.BILLING_DISPUTE,
            difficulty = DifficultyLevel.BEGINNER,
            persona = personas[6], // Hannah Washington
            scenarioBrief = "Hannah Washington forgot to cancel a 14-day trial and was billed $249 upfront for an annual plan. She is defensive and wants her money back.",
            targetObjectives = listOf(
                CallObjective("obj_17", "Normalize the Mistake", "Reassure Hannah without shaming her for missing the 14-day cancellation window.", true),
                CallObjective("obj_18", "Full Courtesy Reversal", "Process the refund cleanly and offer a free educational starter tier.", true)
            ),
            debriefQuestions = listOf(
                DebriefQuestion("dq_11", "Why is empathy more effective than proving the customer signed the agreement?", "Customer psychology & brand preservation.")
            )
        ),
        CallScenario(
            id = "scen_8",
            title = "Enterprise Critical Pitch Fiber Outage",
            category = ScenarioCategory.TECH_SUPPORT,
            difficulty = DifficultyLevel.ADVANCED,
            persona = personas[2], // Chloe Lin
            scenarioBrief = "Chloe Lin's corporate fiber connection is down 15 minutes before an investor pitch. Every second matters.",
            targetObjectives = listOf(
                CallObjective("obj_19", "Instant Emergency Response", "Engage urgent triage protocol without routine verification delays.", true),
                CallObjective("obj_20", "Activate Backup LTE Failover", "Guide instant switch to cellular backup line within 3 minutes.", true)
            ),
            debriefQuestions = listOf(
                DebriefQuestion("dq_12", "How did you match the caller's tempo while remaining calm?", "Pacing and urgency calibration.")
            )
        ),
        CallScenario(
            id = "scen_9",
            title = "Software Redesign Disruption & Team Churn",
            category = ScenarioCategory.RETENTION,
            difficulty = DifficultyLevel.INTERMEDIATE,
            persona = personas[8], // Jasmine Patel
            scenarioBrief = "Jasmine Patel is threatening to move her 40-seat operations team after a v3.0 redesign removed key workflow shortcuts.",
            targetObjectives = listOf(
                CallObjective("obj_21", "Hear Operational Pain", "Listen without defending the UI design choices.", true),
                CallObjective("obj_22", "Provide Actionable Workaround", "Enable legacy shortcut setting and schedule personalized trainer demo.", true)
            ),
            debriefQuestions = listOf(
                DebriefQuestion("dq_13", "How did you prevent the customer from feeling ignored by product development?", "Feedback escalation techniques.")
            )
        ),
        CallScenario(
            id = "scen_10",
            title = "Executive Platinum MFA Lockout",
            category = ScenarioCategory.VIP_SERVICE,
            difficulty = DifficultyLevel.ADVANCED,
            persona = personas[9], // Kevin O'Connor
            scenarioBrief = "Senior Managing Director Kevin O'Connor is locked out of 2FA before a global board meeting. Demands instant executive bypass.",
            targetObjectives = listOf(
                CallObjective("obj_23", "Recognize VIP Tier", "Acknowledge Platinum status immediately.", true),
                CallObjective("obj_24", "Secure Live Video Authentication", "Execute expedited visual authentication safely without compromising security policy.", true)
            ),
            debriefQuestions = listOf(
                DebriefQuestion("dq_14", "How do you deliver white-glove service without violating security controls?", "Compliance vs VIP accommodation.")
            )
        ),
        CallScenario(
            id = "scen_sales_1",
            title = "The Service-to-Sales Spectrum Mobile Pivot",
            category = ScenarioCategory.SALES_AND_UPGRADE,
            difficulty = DifficultyLevel.INTERMEDIATE,
            persona = personas[10], // Tyler Brooks
            scenarioBrief = "Tyler called to verify modem connection stability after a brief restart. After reassuring him, the agent must ask permission to review telecom expenses, uncover his $170/mo Verizon bill, and pitch Spectrum Mobile Unlimited ($29.99/mo BOGO).",
            targetObjectives = listOf(
                CallObjective("obj_s1", "Seamless Service Resolution", "Confirm modem health and ensure Tyler has zero remaining technical questions first.", true),
                CallObjective("obj_s2", "Permission-Based Pivot", "Ask permission to check household mobile savings before pitching.", true),
                CallObjective("obj_s3", "Present Spectrum Mobile BOGO", "Highlight $29.99/line rate on nationwide 5G with Buy-One-Get-One-Free for 12 months, calculating $1,200 annual savings.", true),
                CallObjective("obj_s4", "Overcome Network Hesitation", "Clarify that Spectrum Mobile operates on the exact same leading 5G network as Verizon.", true)
            ),
            debriefQuestions = listOf(
                DebriefQuestion("dq_s1", "How did you transition from technical inquiry into a sales conversation without feeling pushy?", "Look for permission-based bridge: 'While I have your account pulled up...'"),
                DebriefQuestion("dq_s2", "Did you quantify annual household dollar savings or just recite monthly line fees?", "Best practice: Anchor $1,200/yr savings.")
            )
        ),
        CallScenario(
            id = "scen_sales_2",
            title = "Inbound Mover's Triple Play Concierge Bundle",
            category = ScenarioCategory.SALES_AND_UPGRADE,
            difficulty = DifficultyLevel.BEGINNER,
            persona = personas[11], // Morgan Reynolds
            scenarioBrief = "Morgan is moving into a new townhome and comparing Spectrum against AT&T DSL. Pitch the Spectrum One Bundle (500Mbps Ultra + Free Advanced WiFi + Free Mobile Line) plus Spectrum TV Stream with Disney+ Basic included.",
            targetObjectives = listOf(
                CallObjective("obj_s5", "Probe Household Technology Needs", "Ask about remote work demands, streaming habits, and number of connected devices.", true),
                CallObjective("obj_s6", "Position Spectrum One Value", "Explain that Spectrum One includes free Advanced WiFi and an Unlimited Mobile line for 12 months with no annual contracts.", true),
                CallObjective("obj_s7", "Highlight Bundled Entertainment", "Mention that Disney+ Basic and sports programming are included in Spectrum TV Select/Stream.", true),
                CallObjective("obj_s8", "Assumptive Close on Move-In", "Close by securing the self-installation kit delivery for move-in day.", true)
            ),
            debriefQuestions = listOf(
                DebriefQuestion("dq_s3", "What open discovery questions did you ask about Morgan's moving timeline?", "Assessing consultative needs discovery."),
                DebriefQuestion("dq_s4", "How did you differentiate Spectrum from competitor contracts and equipment fees?", "Verify emphasis on zero contracts and free included WiFi.")
            )
        ),
        CallScenario(
            id = "scen_sales_3",
            title = "Internet Gig & WiFi 6 Advanced Pods Upgrade",
            category = ScenarioCategory.SALES_AND_UPGRADE,
            difficulty = DifficultyLevel.ADVANCED,
            persona = personas[12], // Zachary Cruz
            scenarioBrief = "Zachary Cruz experiences buffering while streaming gameplay in the evening due to 18 smart home devices and roommates. Diagnose bandwidth exhaustion and upgrade him to Spectrum Internet Gig (1,000 Mbps) with Advanced WiFi Mesh Pods.",
            targetObjectives = listOf(
                CallObjective("obj_s9", "Uncover Total Device Saturation", "Count total household devices consuming concurrent bandwidth.", true),
                CallObjective("obj_s10", "Differentiate Base vs Gig Speeds", "Explain how 1 Gbps downstream + enhanced upstream eliminates congestion and packet drops.", true),
                CallObjective("obj_s11", "Overcome $20/mo Price Objection", "Break down the $20 upgrade to less than $0.66 a day for lag-free performance.", true)
            ),
            debriefQuestions = listOf(
                DebriefQuestion("dq_s5", "Did you explain the difference between speed at the modem vs WiFi coverage in the room?", "Evaluate explanation of WiFi 6 pods."),
                DebriefQuestion("dq_s6", "How did you justify the cost to an emotional, lag-frustrated gamer?", "Look for value-per-day framing.")
            )
        ),
        CallScenario(
            id = "scen_sales_4",
            title = "Spectrum Mobile Contract Buyout Switching",
            category = ScenarioCategory.SALES_AND_UPGRADE,
            difficulty = DifficultyLevel.INTERMEDIATE,
            persona = personas[13], // Samantha Bell
            scenarioBrief = "Samantha wants to switch 3 family phones from T-Mobile to Spectrum Mobile to cut her $215/mo bill in half, but hesitates because she owes $750 in carrier phone installments. Pitch the Spectrum Contract Buyout program (up to $500/line).",
            targetObjectives = listOf(
                CallObjective("obj_s12", "Explain Contract Buyout Reimbursement", "Detail how Spectrum pays off up to $500 per device when switching 3+ lines.", true),
                CallObjective("obj_s13", "Keep Phone Numbers & eSIM", "Reassure Samantha that numbers are ported instantly and phones activate over eSIM today.", true),
                CallObjective("obj_s14", "Calculate Multi-Line Savings", "Show total net savings: $215/mo down to $89.97/mo for 3 lines ($1,500/year savings).", true)
            ),
            debriefQuestions = listOf(
                DebriefQuestion("dq_s7", "How clearly did you explain the Contract Buyout reimbursement steps?", "Ensuring transparent customer expectations."),
                DebriefQuestion("dq_s8", "Did you build urgency to complete the port-in today?", "Closing technique review.")
            )
        )
    )
}
