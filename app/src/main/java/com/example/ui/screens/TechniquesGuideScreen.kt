package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechniquesGuideScreen(
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("HEAT Model", "CARP Method", "Retention Ladder", "Spectrum Sales", "Forbidden Phrases")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("De-escalation, Retention & Sales Guide", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Spectrum contact center protocols & service-to-sales playbooks", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("guide_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    0 -> item { HeatModelContent() }
                    1 -> item { CarpMethodContent() }
                    2 -> item { RetentionLadderContent() }
                    3 -> item { SpectrumSalesContent() }
                    4 -> item { ForbiddenPhrasesContent() }
                }
            }
        }
    }
}

@Composable
fun HeatModelContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("The H.E.A.T. De-escalation Protocol", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CyanGlow)
        Text(
            "The primary framework used by global contact centers to defuse aggressive or emotionally charged callers within the first 60 seconds.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ProtocolCard(
            letter = "H",
            title = "Hear Them Out",
            subtitle = "Active listening without interrupting",
            description = "Let the caller vent their initial frustration for 30–45 seconds uninterrupted. Silence during venting demonstrates respect and prevents secondary anger spikes.",
            example = "✓ Allow customer to finish their complete explanation before speaking."
        )

        ProtocolCard(
            letter = "E",
            title = "Empathize Sincerity",
            subtitle = "Validate emotion before facts",
            description = "Acknowledge the caller's feeling, not just the logistics. Do not judge or minimize the emotional impact.",
            example = "✓ 'I completely understand how alarming a surprise charge is after a long flight.'"
        )

        ProtocolCard(
            letter = "A",
            title = "Apologize Sincerely",
            subtitle = "Apologize for the experience",
            description = "You do not need to admit legal fault to apologize for the customer's distress and inconvenience.",
            example = "✓ 'I am truly sorry for the runaround you've encountered today.'"
        )

        ProtocolCard(
            letter = "T",
            title = "Take Ownership & Action",
            subtitle = "Personal accountability",
            description = "Use 'I' instead of 'we' or 'company'. Outline concrete next steps and set clear time expectations.",
            example = "✓ 'I am personally taking responsibility to fix this right now while we are on the line.'"
        )
    }
}

@Composable
fun CarpMethodContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("The C.A.R.P. Control Method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
        Text(
            "Designed for repetitive or circular callers who keep shouting or repeating grievances without allowing solutions.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ProtocolCard(
            letter = "C",
            title = "Control the Call",
            subtitle = "Calm, firm vocal pacing",
            description = "Lower your vocal pitch and slow your cadence. Callers naturally mirror the lower tempo of an agent who refuses to match their frantic energy.",
            example = "✓ Speak at 80% normal speed with steady, grounded breathing."
        )

        ProtocolCard(
            letter = "A",
            title = "Acknowledge & Summarize",
            subtitle = "Prove you heard every fact",
            description = "Repeat the core facts back to the customer so they know repeating themselves is no longer necessary.",
            example = "✓ 'To verify I have everything correct: your unit stopped functioning on Tuesday, and the replacement was delayed.'"
        )

        ProtocolCard(
            letter = "R",
            title = "Refocus on What We Can Do",
            subtitle = "Pivot away from past errors",
            description = "Do not dwell on past failures or debate who made the error. Move the conversation to immediate resolutions.",
            example = "✓ 'While we cannot change yesterday's delivery scan, here is what we can authorize right now.'"
        )

        ProtocolCard(
            letter = "P",
            title = "Problem-Solve Collaboratively",
            subtitle = "Present options with autonomy",
            description = "Give angry callers a sense of agency by presenting 2 acceptable options instead of an ultimatum.",
            example = "✓ 'Would you prefer an immediate dispatch courier, or an account credit for local pickup?'"
        )
    }
}

@Composable
fun RetentionLadderContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("The 5-Step Customer Retention Ladder", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AmberWarning)
        Text(
            "Never offer discounts on the first turn! Follow this sequence to maximize churn prevention without eroding revenue.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        RetentionStepCard(1, "Acknowledge Loyalty Tenure", "Thank them for their years of business before mentioning any cancellation terms.")
        RetentionStepCard(2, "Probe Root Churn Trigger", "Is it price, lack of usage, feature complexity, or competitor promo? Ask open questions.")
        RetentionStepCard(3, "Re-Anchor Differentiating Value", "Remind them of unique features, reliability, and bundled services they use weekly.")
        RetentionStepCard(4, "Present Alternative Tier First", "Offer a right-sized plan or feature pause before offering cash discounts.")
        RetentionStepCard(5, "Execute Loyalty Concession & Lock-In", "Provide an authorized loyalty rate tied to an annual commitment or price guarantee.")
    }
}

@Composable
fun ForbiddenPhrasesContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Forbidden Triggers vs. Golden Phrases", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CrimsonEscalated)
        Text(
            "Replacing high-friction words with collaborative alternatives directly reduces customer escalation rates by up to 60%.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        PhraseComparisonCard(
            forbidden = "“You need to calm down.”",
            golden = "“I can hear how frustrated you are, and I am here to help you get this resolved.”"
        )
        PhraseComparisonCard(
            forbidden = "“It's company policy.”",
            golden = "“The way our system is configured requires X, but let's see how we can work around this together.”"
        )
        PhraseComparisonCard(
            forbidden = "“That's not my department / I have to transfer you.”",
            golden = "“I am going to consult our specialist while keeping you on the line so you don't have to repeat yourself.”"
        )
        PhraseComparisonCard(
            forbidden = "“You should have read the fine print.”",
            golden = "“I understand how easily that detail can be missed, and I appreciate you clarifying it.”"
        )
        PhraseComparisonCard(
            forbidden = "“There is nothing I can do.”",
            golden = "“While I cannot authorize option A, here are two concrete options I can put in place for you.”"
        )
    }
}

@Composable
fun ProtocolCard(
    letter: String,
    title: String,
    subtitle: String,
    description: String,
    example: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(letter, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(description, style = MaterialTheme.typography.bodyMedium)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = example,
                    style = MaterialTheme.typography.bodySmall,
                    color = EmeraldSuccess,
                    modifier = Modifier.padding(8.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun RetentionStepCard(step: Int, title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(CyanGlow),
                contentAlignment = Alignment.Center
            ) {
                Text("$step", fontWeight = FontWeight.Bold, color = Slate950, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun PhraseComparisonCard(forbidden: String, golden: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Close, contentDescription = "Avoid", tint = CrimsonEscalated, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(forbidden, color = CrimsonEscalated, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Check, contentDescription = "Use", tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(golden, color = EmeraldSuccess, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun SpectrumSalesContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Spectrum Service-to-Sales (4P Framework)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = IndigoLight)
        Text(
            "The proven framework for transitioning inbound customer care or technical support calls into valuable Spectrum Mobile, Gig Internet, and TV Stream additions.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ProtocolCard(
            letter = "1",
            title = "Permission-Based Bridge",
            subtitle = "Never hard pitch without permission",
            description = "Once the primary reason for calling is resolved, ask permission before reviewing additional account benefits. This preserves trust.",
            example = "✓ 'Now that your WiFi is running smoothly, while I have your account verified, may I ask which wireless carrier you use for your cell phones?'"
        )

        ProtocolCard(
            letter = "2",
            title = "Probe Household Telecom Spend",
            subtitle = "Uncover device counts and monthly costs",
            description = "Ask open-ended discovery questions to pinpoint pain points: cell phone bill totals, household device counts, or separate streaming subscriptions.",
            example = "✓ 'How many smartphone lines do you have in the home, and about how much are they billing you each month?'"
        )

        ProtocolCard(
            letter = "3",
            title = "Position Spectrum Products",
            subtitle = "Translate features into dollar savings",
            description = "Present Spectrum One, Spectrum Mobile Unlimited ($29.99/mo BOGO), or Gig Internet (1,000 Mbps). Emphasize zero contracts and leading nationwide 5G network parity.",
            example = "✓ 'With Spectrum Mobile, you get the exact same nationwide 5G network as Verizon, but for just $29.99 with a free second line, putting over $1,200 back in your pocket each year.'"
        )

        ProtocolCard(
            letter = "4",
            title = "Propose & Assumptive Close",
            subtitle = "Close with instant eSIM or delivery",
            description = "Ask for the sale by proposing the next immediate step rather than an open-ended 'do you want to buy?'. Mention instant eSIM activation and zero activation fees.",
            example = "✓ 'I can activate your line via eSIM right now so you can keep your existing phone and number, or ship free SIM cards to your door by Wednesday. Which is easier for you?'"
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text("Spectrum Objection Rebuttal Playbook", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CyanGlow)

        PhraseComparisonCard(
            forbidden = "Objection: “I'm happy with Verizon / AT&T right now.”",
            golden = "Rebuttal: “I completely understand! We actually run on the exact same leading nationwide 5G network, but as a Spectrum Internet subscriber, you qualify for $29.99/mo Unlimited with BOGO, saving you $1,200/yr for the same coverage.”"
        )

        PhraseComparisonCard(
            forbidden = "Objection: “I still owe $400 on my phones with my current carrier.”",
            golden = "Rebuttal: “That is exactly why Spectrum has our Contract Buyout program—we pay off up to $500 per device when you switch, wiping out that balance so you can start saving immediately.”"
        )

        PhraseComparisonCard(
            forbidden = "Objection: “I don't think I need Gig speed, $20/mo is too much.”",
            golden = "Rebuttal: “With 18 devices online in your household, that $20 breaks down to just 66 cents a day to guarantee zero buffering during your work video calls and evening 4K streams.”"
        )
    }
}
