package com.example.proximity.shared

/**
 * Serializable UI tree node. All fields nullable so a single class works
 * for every node type — Gson serializes only what's set.
 *
 * Supported `type` values:
 *   - "text"    : text, size, color, bold, align
 *   - "column"  : children, padding, spacing, alignment ("start"|"center"|"end"), background
 *   - "row"     : children, padding, spacing, alignment ("top"|"center"|"bottom"), background
 *   - "card"    : child, padding, background, cornerRadius
 *   - "box"     : children, padding, background, alignment
 *   - "spacer"  : size (dp)
 *   - "clear"   : signals TV to clear remote UI and return to status
 */
data class UiNode(
    val type: String,
    // text
    val text: String? = null,
    val size: Int? = null,        // sp for text, dp for spacer
    val color: String? = null,    // "#RRGGBB" or "#AARRGGBB"
    val bold: Boolean = false,
    val align: String? = null,
    // containers
    val children: List<UiNode>? = null,
    val child: UiNode? = null,
    val padding: Int? = null,
    val spacing: Int? = null,
    val alignment: String? = null,
    val background: String? = null,
    val cornerRadius: Int? = null,
)
