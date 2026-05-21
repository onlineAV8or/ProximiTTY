package com.example.proximitty.phone

import com.example.proximitty.shared.UiNode

data class UiTemplate(val name: String, val node: UiNode, val json: String)

object UiTemplates {

    val WELCOME = UiNode(
        type = "column",
        padding = 64,
        spacing = 16,
        alignment = "center",
        background = "#0F172A",
        children = listOf(
            UiNode(type = "text", text = "Hello from Phone!", size = 64, bold = true, color = "#FFFFFF", align = "center"),
            UiNode(type = "text", text = "This UI was sent over Nearby Connections", size = 22, color = "#94A3B8", align = "center"),
            UiNode(type = "spacer", size = 16),
            UiNode(type = "text", text = ":)", size = 80, color = "#60A5FA", align = "center"),
        ),
    )

    val NOTIFICATION = UiNode(
        type = "box",
        padding = 80,
        background = "#1E293B",
        alignment = "center",
        children = listOf(
            UiNode(
                type = "card",
                padding = 32,
                cornerRadius = 24,
                background = "#FFFFFF",
                child = UiNode(
                    type = "column",
                    spacing = 12,
                    children = listOf(
                        UiNode(type = "text", text = "📬  New Notification", size = 28, bold = true, color = "#1565C0"),
                        UiNode(type = "text", text = "From: Phone", size = 18, color = "#475569"),
                        UiNode(type = "spacer", size = 8),
                        UiNode(type = "text", text = "Notification from app on phone", size = 20, color = "#0F172A"),
                    ),
                ),
            ),
        ),
    )

    val STATS = UiNode(
        type = "column",
        padding = 40,
        spacing = 24,
        alignment = "center",
        background = "#F8FAFC",
        children = listOf(
            UiNode(type = "text", text = "Today's Stats", size = 36, bold = true, color = "#0F172A", align = "center"),
            UiNode(
                type = "row",
                spacing = 20,
                alignment = "center",
                children = listOf(
                    statCard("42", "Messages", "#22C55E"),
                    statCard("7",  "Alerts",   "#F97316"),
                    statCard("198", "Steps",   "#3B82F6"),
                ),
            ),
        ),
    )

    private fun statCard(value: String, label: String, color: String) = UiNode(
        type = "card",
        padding = 32,
        cornerRadius = 20,
        background = "#FFFFFF",
        child = UiNode(
            type = "column",
            alignment = "center",
            spacing = 4,
            children = listOf(
                UiNode(type = "text", text = value, size = 64, bold = true, color = color),
                UiNode(type = "text", text = label, size = 18, color = "#64748B"),
            ),
        ),
    )

    val ALL: List<UiTemplate> = listOf(
        UiTemplate("Welcome",      WELCOME,      pretty(WELCOME)),
        UiTemplate("Notification", NOTIFICATION, pretty(NOTIFICATION)),
        UiTemplate("Stats",        STATS,        pretty(STATS)),
    )

    private fun pretty(node: UiNode): String =
        com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(node)
}
