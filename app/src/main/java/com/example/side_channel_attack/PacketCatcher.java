package com.example.side_channel_attack;

import android.net.TrafficStats;

public class PacketCatcher {
    private int PERIOD_SEARCH_ID_COUNTER = 0;


    public boolean scan(long MS_LENGTH_IN_PERIOD) {

        int id = ++PERIOD_SEARCH_ID_COUNTER;
        long periodStart = System.currentTimeMillis();
        long periodEnd = System.currentTimeMillis() + MS_LENGTH_IN_PERIOD;
        long lastAccessed = periodStart;
        long history = TrafficStats.getTotalTxBytes();
        long periodRecordingPoint = periodStart + 3000;
        int recordedInitiations = 0;
        int total = 0;

        int occurrences10002000 = 0;
        int occurrences20003000 = 0;
        int occurrences30004000 = 0;
        int occurrences40005000 = 0;
        int occurrences50006000 = 0;
        int occurrences6000Plus = 0;

        while (id == PERIOD_SEARCH_ID_COUNTER && System.currentTimeMillis() <= periodEnd) {
            if (System.currentTimeMillis() > periodRecordingPoint) {
                long totalTxBytes = TrafficStats.getTotalTxBytes();
                if (totalTxBytes > 400 + history && totalTxBytes < 2000 + history) {
                    occurrences10002000++;
                    recordedInitiations++;
                    lastAccessed = System.currentTimeMillis();

                }
                if (totalTxBytes > 2000 + history && totalTxBytes < 3000 + history) {
                    occurrences20003000++;
                    recordedInitiations++;
                    lastAccessed = System.currentTimeMillis();
                }

                if (totalTxBytes > 3000 + history && totalTxBytes < 4000 + history) {
                    occurrences30004000++;
                    recordedInitiations++;
                    lastAccessed = System.currentTimeMillis();
                }

                if (totalTxBytes > 4000 + history && totalTxBytes < 5000 + history) {
                    occurrences40005000++;
                    recordedInitiations++;
                    lastAccessed = System.currentTimeMillis();
                }
                if (totalTxBytes > 5000 + history && totalTxBytes < 6000 + history) {
                    occurrences50006000++;
                    recordedInitiations++;
                    lastAccessed = System.currentTimeMillis();
                }

                if (totalTxBytes > 6000 + history) {
                    occurrences6000Plus++;
                    recordedInitiations++;
                    lastAccessed = System.currentTimeMillis();
                }
                total += (totalTxBytes - history);

                if ((recordedInitiations >= 2 || total >= 20000) && (System.currentTimeMillis() - lastAccessed) >= 4000) {
                    break;
                }

                if (recordedInitiations == 1 && (System.currentTimeMillis() - lastAccessed) >= 4000) {
                    recordedInitiations = 0;
                }

                history = totalTxBytes;
            }
        }
        return ((occurrences10002000 >= 4 ||
                occurrences20003000 >= 4 ||
                occurrences30004000 >= 3 ||
                occurrences40005000 >= 2 ||
                occurrences50006000 >= 2) && periodRecordingPoint >= 3)
                || (total >= 20000 && occurrences6000Plus >= 1);

    }

}
