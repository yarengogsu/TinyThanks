package com.example.tinythanks;

/**
 * Represents the visual state of the weekly flower.
 */
public enum FlowerState {
    BUD,        // small / closed flower (few entries)
    PARTIAL,    // half-open flower
    FULL,       // fully opened flower
    WILTED      // user marked this week as "difficult"
}
