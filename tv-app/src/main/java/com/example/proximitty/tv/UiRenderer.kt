package com.example.proximitty.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proximitty.shared.UiNode

@Composable
fun RenderUi(node: UiNode, modifier: Modifier = Modifier) {
    when (node.type.lowercase()) {
        "text"   -> RenderText(node, modifier)
        "column" -> RenderColumn(node, modifier)
        "row"    -> RenderRow(node, modifier)
        "card"   -> RenderCard(node, modifier)
        "box"    -> RenderBox(node, modifier)
        "spacer" -> Spacer(Modifier.size((node.size ?: 8).dp))
        "clear"  -> Unit // handled at the TvScreen level — never reaches renderer
        else     -> Text(
            "Unknown node type: ${node.type}",
            color = MaterialTheme.colorScheme.error,
        )
    }
}

// ─── Components ──────────────────────────────────────────────────────────────

@Composable
private fun RenderText(node: UiNode, modifier: Modifier) {
    Text(
        text = node.text.orEmpty(),
        fontSize = (node.size ?: 16).sp,
        color = node.color.parseColor() ?: MaterialTheme.colorScheme.onSurface,
        fontWeight = if (node.bold) FontWeight.Bold else FontWeight.Normal,
        textAlign = node.align.toTextAlign(),
        modifier = modifier,
    )
}

@Composable
private fun RenderColumn(node: UiNode, modifier: Modifier) {
    Column(
        modifier = modifier
            .background(node.background.parseColor() ?: Color.Transparent)
            .padding((node.padding ?: 0).dp),
        verticalArrangement = Arrangement.spacedBy((node.spacing ?: 0).dp),
        horizontalAlignment = when (node.alignment?.lowercase()) {
            "center" -> Alignment.CenterHorizontally
            "end"    -> Alignment.End
            else     -> Alignment.Start
        },
    ) {
        node.children?.forEach { RenderUi(it) }
    }
}

@Composable
private fun RenderRow(node: UiNode, modifier: Modifier) {
    Row(
        modifier = modifier
            .background(node.background.parseColor() ?: Color.Transparent)
            .padding((node.padding ?: 0).dp),
        horizontalArrangement = Arrangement.spacedBy((node.spacing ?: 0).dp),
        verticalAlignment = when (node.alignment?.lowercase()) {
            "center" -> Alignment.CenterVertically
            "bottom" -> Alignment.Bottom
            else     -> Alignment.Top
        },
    ) {
        node.children?.forEach { RenderUi(it) }
    }
}

@Composable
private fun RenderCard(node: UiNode, modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape((node.cornerRadius ?: 16).dp))
            .background(node.background.parseColor() ?: MaterialTheme.colorScheme.surfaceVariant)
            .padding((node.padding ?: 16).dp),
    ) {
        node.child?.let { RenderUi(it) }
    }
}

@Composable
private fun RenderBox(node: UiNode, modifier: Modifier) {
    Box(
        modifier = modifier
            .background(node.background.parseColor() ?: Color.Transparent)
            .padding((node.padding ?: 0).dp),
        contentAlignment = when (node.alignment?.lowercase()) {
            "topstart"    -> Alignment.TopStart
            "topcenter"   -> Alignment.TopCenter
            "topend"      -> Alignment.TopEnd
            "centerstart" -> Alignment.CenterStart
            "center"      -> Alignment.Center
            "centerend"   -> Alignment.CenterEnd
            "bottomstart" -> Alignment.BottomStart
            "bottomcenter" -> Alignment.BottomCenter
            "bottomend"   -> Alignment.BottomEnd
            else          -> Alignment.TopStart
        },
    ) {
        node.children?.forEach { RenderUi(it) }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun String?.parseColor(): Color? {
    if (this.isNullOrBlank()) return null
    return runCatching { Color(android.graphics.Color.parseColor(this)) }.getOrNull()
}

private fun String?.toTextAlign(): TextAlign = when (this?.lowercase()) {
    "center" -> TextAlign.Center
    "end"    -> TextAlign.End
    else     -> TextAlign.Start
}
