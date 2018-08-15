package de.tuberlin.inet.sdwn.core.ctl;

import de.tuberlin.inet.sdwn.core.api.Sdwn80211MgmtFrameListener;

import java.util.ArrayList;
import java.util.List;

public class MgmtFrameListenerList {


    List<MgmtFrameListenerListEntry> list = new ArrayList<>();

    public void addListener(Sdwn80211MgmtFrameListener listener, int priority) throws IllegalArgumentException {

        MgmtFrameListenerListEntry entry = new MgmtFrameListenerListEntry(priority, listener);

        synchronized (this) {
            if (list.contains(entry)) {
                throw new IllegalArgumentException("Listener already registered");
            }

            for (MgmtFrameListenerListEntry e : list) {
                if (e.priority == priority) {
                    throw new IllegalArgumentException(String.format("Listener with priority %d already registered", priority));
                }
            }

            list.add(entry);
        }
    }

    public void removeListener(Sdwn80211MgmtFrameListener listener) {
        synchronized (this) {
            MgmtFrameListenerListEntry dummy = new MgmtFrameListenerListEntry(0, listener);
            list.remove(dummy);
        }
    }

    class MgmtFrameListenerListEntry implements Comparable<MgmtFrameListenerListEntry> {

        int priority;
        Sdwn80211MgmtFrameListener listener;

        MgmtFrameListenerListEntry(int priority, Sdwn80211MgmtFrameListener listener) {
            this.priority = priority;
            this.listener = listener;
        }

        @Override
        public int compareTo(MgmtFrameListenerListEntry o) {
            return o.priority - priority;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof MgmtFrameListenerListEntry)) {
                return false;
            }

            MgmtFrameListenerListEntry other = (MgmtFrameListenerListEntry) o;
            return other.listener.equals(listener);
        }

    }
}
