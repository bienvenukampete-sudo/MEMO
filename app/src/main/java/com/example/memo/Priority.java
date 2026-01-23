package com.example.memo;

import android.graphics.Color;

public enum Priority {
    HIGH("Haute", Color.parseColor("#EF5350")),
    MEDIUM("Moyenne", Color.parseColor("#FF9800")),
    LOW("Basse", Color.parseColor("#66BB6A"));

    private final String label;
    private final int color;

    Priority(String label, int color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public int getColor() {
        return color;
    }
}
