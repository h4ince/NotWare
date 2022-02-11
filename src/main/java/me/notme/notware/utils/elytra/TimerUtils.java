package me.notme.notware.utils.elytra;

public class TimerUtils {
    public long ms;

    public void Timer() {
        ms = 0L;
    }

    public boolean hasPassed(int ms) {
        return System.currentTimeMillis() - ms >= ms;
    }

    public void reset() {
        ms = System.currentTimeMillis();
    }
}
