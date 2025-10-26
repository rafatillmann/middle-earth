package org.example.interfaces;

public interface LogCallback {
    interface AddEntryCallback {
        void onComplete(Long entryId);
    }
}
