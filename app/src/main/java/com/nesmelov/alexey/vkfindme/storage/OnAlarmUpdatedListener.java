package com.nesmelov.alexey.vkfindme.storage;

public interface OnAlarmUpdatedListener {
    void onAlarmRemoved(final long alarmId);
    void onAlarmUpdated(final long alarmId);
}
