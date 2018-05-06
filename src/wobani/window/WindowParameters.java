package wobani.window;

import java.util.*;
import wobani.toolbox.annotations.*;

/**
 * Parameters for initializing the window.
 */
public class WindowParameters {

    /**
     * Determines whether the window is resizable.
     */
    private boolean resizable = true;
    /**
     * Determines whether the window is visible at start.
     */
    private boolean visibleAtStart = true;
    /**
     * Determines whether the window is decorated.
     */
    private boolean decorated = true;
    /**
     * Determines whether the window get focus at start.
     */
    private boolean focusedAtStart = true;
    /**
     * Determines whether the window is always on top.
     */
    private boolean alwaysOnTop = false;
    /**
     * Determines whether the window is maximized at start.
     */
    private boolean maximizedAtStart = false;
    /**
     * The window's title at start.
     */
    private String titleAtStart = "Wobani Engine";

    /**
     * Determines whether the window is resizable.
     *
     * @return true if the window is resizable, false otherwise
     */
    public boolean isResizable() {
        return resizable;
    }

    /**
     * Sets whether or not the window should be resizable.
     *
     * @param resizable resizable
     */
    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    /**
     * Determines whether the window is visible at start.
     *
     * @return true if the window is visible at start, false otherwise
     */
    public boolean isVisibleAtStart() {
        return visibleAtStart;
    }

    /**
     * Sets whether or not the window should be visible at start.
     *
     * @param visibleAtStart visible at start
     */
    public void setVisibleAtStart(boolean visibleAtStart) {
        this.visibleAtStart = visibleAtStart;
    }

    /**
     * Determines whether the window is decorated.
     *
     * @return true if the window is decorated, false otherwise
     */
    public boolean isDecorated() {
        return decorated;
    }

    /**
     * Sets whether or not the window should be decorated.
     *
     * @param decorated decorated
     */
    public void setDecorated(boolean decorated) {
        this.decorated = decorated;
    }

    /**
     * Determines whether the window get focus at start.
     *
     * @return true if the window get focus at start, false otherwise
     */
    public boolean isFocusedAtStart() {
        return focusedAtStart;
    }

    /**
     * Sets whether or not the window should get focus at start.
     *
     * @param focusedAtStart focus at start
     */
    public void setFocusedAtStart(boolean focusedAtStart) {
        this.focusedAtStart = focusedAtStart;
    }

    /**
     * Determines whether the window is always on top.
     *
     * @return true if the window is always on top, false otherwise
     */
    public boolean isAlwaysOnTop() {
        return alwaysOnTop;
    }

    /**
     * Sets whether or not the window should be always on top.
     *
     * @param alwaysOnTop always on top
     */
    public void setAlwaysOnTop(boolean alwaysOnTop) {
        this.alwaysOnTop = alwaysOnTop;
    }

    /**
     * Determines whether the window is maximized at start.
     *
     * @return true if the window is maximized at start, false otherwise
     */
    public boolean isMaximizedAtStart() {
        return maximizedAtStart;
    }

    /**
     * Sets whether or not the window should be maximized at start.
     *
     * @param maximizedAtStart maximized at start
     */
    public void setMaximizedAtStart(boolean maximizedAtStart) {
        this.maximizedAtStart = maximizedAtStart;
    }

    /**
     * Returns the window's title at start.
     *
     * @return the window's title at start
     */
    @NotNull
    public String getTitleAtStart() {
        return titleAtStart;
    }

    /**
     * Sets the window's title at start to the given value.
     *
     * @param titleAtStart title at start
     *
     * @throws NullPointerException title can't be null
     */
    public void setTitleAtStart(@NotNull String titleAtStart) {
        if (titleAtStart == null) {
            throw new NullPointerException();
        }
        this.titleAtStart = titleAtStart;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + (this.resizable ? 1 : 0);
        hash = 17 * hash + (this.visibleAtStart ? 1 : 0);
        hash = 17 * hash + (this.decorated ? 1 : 0);
        hash = 17 * hash + (this.focusedAtStart ? 1 : 0);
        hash = 17 * hash + (this.alwaysOnTop ? 1 : 0);
        hash = 17 * hash + (this.maximizedAtStart ? 1 : 0);
        hash = 17 * hash + Objects.hashCode(this.titleAtStart);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WindowParameters other = (WindowParameters) obj;
        if (this.resizable != other.resizable) {
            return false;
        }
        if (this.visibleAtStart != other.visibleAtStart) {
            return false;
        }
        if (this.decorated != other.decorated) {
            return false;
        }
        if (this.focusedAtStart != other.focusedAtStart) {
            return false;
        }
        if (this.alwaysOnTop != other.alwaysOnTop) {
            return false;
        }
        if (this.maximizedAtStart != other.maximizedAtStart) {
            return false;
        }
        if (!Objects.equals(this.titleAtStart, other.titleAtStart)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder()
                .append("WindowParameters(")
                .append(" resizable: ").append(resizable)
                .append(", visible at start: ").append(visibleAtStart)
                .append(", decorated: ").append(decorated)
                .append(", focused at start: ").append(focusedAtStart)
                .append(", always on top: ").append(alwaysOnTop)
                .append(", maximized at Start: ").append(maximizedAtStart)
                .append(", title at start: ").append(titleAtStart)
                .append(")");
        return res.toString();
    }

}
