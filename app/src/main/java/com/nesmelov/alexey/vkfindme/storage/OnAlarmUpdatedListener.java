package com.nesmelov.alexey.vkfindme.storage;

/**
 * Alarm updated event listener.
 */
public interface OnAlarmUpdatedListener {
    /**
     * This method is called when an alarm is removed.
     *
     * @param alarmId alarm id.
     */
    void onAlarmRemoved(final int alarmId);

    /**
     * This method is called when an alarm is updated.
     *
     * @param alarmId alarm id.
     */
    void onAlarmUpdated(final int alarmId);
}
