package vn.edu.vaa.classmanagerdemo.utils;

import android.os.SystemClock;
import android.view.View;

public class DebounceClickListener implements View.OnClickListener {
    private static final long DEBOUNCE_MS = 700;
    private long lastClickTime = 0;
    private final View.OnClickListener wrapped;

    public DebounceClickListener(View.OnClickListener wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void onClick(View v) {
        long now = SystemClock.elapsedRealtime();
        if (now - lastClickTime < DEBOUNCE_MS) return;
        lastClickTime = now;
        wrapped.onClick(v);
    }

    // Shorthand static factory
    public static View.OnClickListener wrap(View.OnClickListener listener) {
        return new DebounceClickListener(listener);
    }
}
