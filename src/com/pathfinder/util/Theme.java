package com.pathfinder.util;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

public class Theme {
    // ── Colors ────────────────────────────────────────────────────
    public static final Color BG        = new Color(0x07, 0x08, 0x0C);
    public static final Color BG2       = new Color(0x0E, 0x10, 0x18);
    public static final Color BG3       = new Color(0x14, 0x17, 0x20);
    public static final Color CARD      = new Color(0x19, 0x1D, 0x27);
    public static final Color CARD2     = new Color(0x1F, 0x23, 0x30);
    public static final Color BORDER    = new Color(0x27, 0x2C, 0x3A);
    public static final Color BORDER2   = new Color(0x32, 0x37, 0x48);
    public static final Color TEAL      = new Color(0x00, 0xD4, 0xAA);
    public static final Color TEAL2     = new Color(0x00, 0xB8, 0x91);
    public static final Color AMBER     = new Color(0xFF, 0xB3, 0x47);
    public static final Color AMBER2    = new Color(0xFF, 0x99, 0x00);
    public static final Color ROSE      = new Color(0xFF, 0x5F, 0x7E);
    public static final Color BLUE      = new Color(0x4F, 0x8E, 0xF7);
    public static final Color PURPLE    = new Color(0x9B, 0x6D, 0xFF);
    public static final Color GREEN     = new Color(0x3D, 0xD6, 0x8C);
    public static final Color TXT       = new Color(0xEE, 0xF0, 0xF6);
    public static final Color TXT2      = new Color(0x8A, 0x90, 0xA4);
    public static final Color TXT3      = new Color(0x55, 0x5C, 0x72);

    // ── Fonts ──────────────────────────────────────────────────────
    public static final String FONT_DISPLAY = "SansSerif";
    public static final String FONT_BODY    = "SansSerif";

    public static Font display(int size, int style) {
        return new Font(FONT_DISPLAY, style, size);
    }

    public static Font body(int size) {
        return new Font(FONT_BODY, Font.PLAIN, size);
    }

    public static Font body(int size, int style) {
        return new Font(FONT_BODY, style, size);
    }

    // ── Gradient paint helpers ─────────────────────────────────────
    public static GradientPaint tealPurpleGradient(int x1, int y1, int x2, int y2) {
        return new GradientPaint(x1, y1, TEAL, x2, y2, PURPLE);
    }

    public static GradientPaint amberGradient(int x1, int y1, int x2, int y2) {
        return new GradientPaint(x1, y1, AMBER, x2, y2, AMBER2);
    }

    // ── Corner radius ──────────────────────────────────────────────
    public static final int R8  = 8;
    public static final int R12 = 12;
    public static final int R16 = 16;
    public static final int R24 = 24;
}
