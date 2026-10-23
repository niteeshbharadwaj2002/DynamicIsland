package com.agupta07505.smartisland.util

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectLinksTest {
    private val links = listOf(
        ProjectLinks.REPO, ProjectLinks.FEATURE_REQUEST, ProjectLinks.BUG_REPORT, ProjectLinks.ISSUES,
        ProjectLinks.FORK, ProjectLinks.LICENSE, ProjectLinks.PRIVACY, ProjectLinks.TERMS
    )

    @Test
    fun everyLinkPointsAtThisProject() {
        assertEquals("https://github.com/niteeshbharadwaj2002/NotchIQ", ProjectLinks.REPO)
        links.forEach { assertTrue(it, it.startsWith(ProjectLinks.REPO)) }
    }

    @Test
    fun theIssueFormsAreTheTemplatesTheRepoShips() {
        assertTrue(ProjectLinks.FEATURE_REQUEST.endsWith("template=feature_request.md"))
        assertTrue(ProjectLinks.BUG_REPORT.endsWith("template=bug_report.md"))
        listOf("feature_request.md", "bug_report.md").forEach {
            assertTrue(it, File("../.github/ISSUE_TEMPLATE/$it").exists())
        }
    }

    @Test
    fun theScreensNoLongerLinkUpstreamOrTelegram() {
        val root = File("src/main/java/com/agupta07505/smartisland/ui")
        listOf("sections/SupportSection.kt", "sections/AboutSection.kt", "SmartIslandHomeScreen.kt").forEach { name ->
            val text = File(root, name).readText()
            assertFalse("$name links upstream", text.contains("agupta07505/SmartIsland"))
            assertFalse("$name links Telegram", text.contains("telegram", ignoreCase = true))
        }
    }
}
