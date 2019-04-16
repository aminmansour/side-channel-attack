package com.example.side_channel_attack;

import android.net.TrafficStats;
import android.os.SystemClock;

public class PacketCatcher {
    private int PERIOD_SEARCH_ID_COUNTER = 0;

    public boolean checkPeriodForUpload(
            long MS_LENGTH_IN_PERIOD,
            long UBOUND_OF_TX_EVENTS_PER_PERIOD,
            long LBOUND_OF_TX_EVENTS_PER_PERIOD,
            long UBOUND_BYTE_CHANGE,
            long LBOUND_BYTE_CHANGE){

        int id = ++PERIOD_SEARCH_ID_COUNTER;
        long periodStart = System.currentTimeMillis();
        long periodEnd = periodStart + MS_LENGTH_IN_PERIOD;
        long history = TrafficStats.getTotalTxBytes();
        long lastAccessed = periodStart;
        int occurrences = 0;
        while (id == PERIOD_SEARCH_ID_COUNTER && System.currentTimeMillis() <= periodEnd) {
            long totalTxBytes = TrafficStats.getTotalTxBytes();
            String info = "";
            if(totalTxBytes > LBOUND_BYTE_CHANGE + history &&
                    totalTxBytes < UBOUND_BYTE_CHANGE + history) {
                occurrences++;
                lastAccessed = System.currentTimeMillis();
            }

            if (totalTxBytes - history > 300) {
                info += ("\tReceived: " + TrafficStats.getTotalRxBytes() + " bytes / " + TrafficStats.getTotalRxPackets() + " packets\n");
                info += ("\tTransmitted: " + totalTxBytes + " bytes / " + TrafficStats.getTotalTxPackets() + " packets\n");
                System.out.println("code " + SystemClock.currentThreadTimeMillis() + "old : " + history + "new : " + totalTxBytes + " diff: " + (totalTxBytes - history) + " " + info);
            }

            if (occurrences >= LBOUND_OF_TX_EVENTS_PER_PERIOD && (System.currentTimeMillis() - lastAccessed) >= 4000) {
                System.out.println("Hello");
                break;
            }
            history = totalTxBytes;
        }

        return occurrences >= LBOUND_OF_TX_EVENTS_PER_PERIOD &&
                occurrences <= UBOUND_OF_TX_EVENTS_PER_PERIOD;
    }

    public boolean checkPeriodForDownload(
            long MS_LENGTH_IN_PERIOD,
            long UBOUND_OF_RX_EVENTS_PER_PERIOD,
            long LBOUND_OF_RX_EVENTS_PER_PERIOD,
            long UBOUND_BYTE_CHANGE,
            long LBOUND_BYTE_CHANGE){

        int id = ++PERIOD_SEARCH_ID_COUNTER;
        long periodEnd = System.currentTimeMillis() + MS_LENGTH_IN_PERIOD;
        long history = TrafficStats.getTotalRxBytes();
        int occurrences = 0;
        while ( id == PERIOD_SEARCH_ID_COUNTER && System.currentTimeMillis() <= periodEnd) {

            long totalRxBytes = TrafficStats.getTotalRxBytes();
            String info = "";
            if( totalRxBytes > LBOUND_BYTE_CHANGE + history &&
                    totalRxBytes < UBOUND_BYTE_CHANGE + history ) {
                occurrences++;
                info += ("\tReceived: " + TrafficStats.getTotalRxBytes() + " bytes / " + TrafficStats.getTotalRxPackets() + " packets\n");
                info += ("\tTransmitted: " + totalRxBytes + " bytes / " + TrafficStats.getTotalTxPackets() + " packets\n");
                System.out.println("code " + SystemClock.currentThreadTimeMillis() +"old : "+history+ "new : " + totalRxBytes +" diff: "+(totalRxBytes-history)+" "+ info);
            }
            System.out.println("enter");
            history = totalRxBytes;
        }

        return System.currentTimeMillis() > periodEnd &&
                occurrences >= LBOUND_OF_RX_EVENTS_PER_PERIOD &&
                occurrences <= UBOUND_OF_RX_EVENTS_PER_PERIOD;
    }

    public boolean scan(long BYTE_HISTORY, long MS_LENGTH_IN_PERIOD) {

        int id = ++PERIOD_SEARCH_ID_COUNTER;
        long periodStart = System.currentTimeMillis();
        long periodEnd = System.currentTimeMillis() + MS_LENGTH_IN_PERIOD;
        long lastAccessed = periodStart;
        long history = BYTE_HISTORY;
        long periodRecordingPoint = periodStart + 3000;
        int recordedInitiations = 0;

        int occurrences10002000 = 0;
        int occurrences20003000 = 0;
        int occurrences30004000 = 0;
        int occurrences40005000 = 0;
        int occurrences50006000 = 0;
        int occurrences60007000 = 0;
        int occurrences70008000 = 0;
        int occurrences80009000 = 0;
        int occurrences900010000 = 0;
        int occurrences100000 = 0;
        while (id == PERIOD_SEARCH_ID_COUNTER && System.currentTimeMillis() <= periodEnd) {
            if (System.currentTimeMillis() > periodRecordingPoint) {
                long totalTxBytes = TrafficStats.getTotalTxBytes();
                if (totalTxBytes > 400 + history && totalTxBytes < 2000 + history) {
                    occurrences10002000++;
                    recordedInitiations++;
                    System.out.println("h1 " + (totalTxBytes - history));
                    lastAccessed = System.currentTimeMillis();

                }
                if (totalTxBytes > 2000 + history && totalTxBytes < 3000 + history) {
                    occurrences20003000++;
                    recordedInitiations++;
                    System.out.println("h2 " + (totalTxBytes - history));
                    lastAccessed = System.currentTimeMillis();
                }

                if (totalTxBytes > 3000 + history && totalTxBytes < 4000 + history) {
                    occurrences30004000++;
                    recordedInitiations++;
                    System.out.println("h3 " + (totalTxBytes - history));
                    lastAccessed = System.currentTimeMillis();
                }

                if (totalTxBytes > 4000 + history && totalTxBytes < 5000 + history) {
                    occurrences40005000++;
                    recordedInitiations++;
                    System.out.println("h4 " + (totalTxBytes - history));
                    lastAccessed = System.currentTimeMillis();
                }
                if (totalTxBytes > 5000 + history && totalTxBytes < 6000 + history) {
                    occurrences50006000++;
                    recordedInitiations++;
                    System.out.println("h5 " + (totalTxBytes - history));
                    lastAccessed = System.currentTimeMillis();
                }

                if (totalTxBytes > 6000 + history && totalTxBytes < 7000 + history) {
                    occurrences60007000++;
                    recordedInitiations++;
                    System.out.println("h6 " + (totalTxBytes - history));
                    lastAccessed = System.currentTimeMillis();
                }
                if (totalTxBytes > 7000 + history && totalTxBytes < 8000 + history) {
                    occurrences70008000++;
                    recordedInitiations++;
                    System.out.println("h7 " + (totalTxBytes - history));
                    lastAccessed = System.currentTimeMillis();
                }
                if (totalTxBytes > 8000 + history && totalTxBytes < 9000 + history) {
                    occurrences80009000++;
                    recordedInitiations++;
                    System.out.println("h8 " + (totalTxBytes - history));
                    lastAccessed = System.currentTimeMillis();
                }
                if (totalTxBytes > 9000 + history && totalTxBytes < 10000 + history) {
                    occurrences900010000++;
                    System.out.println("h9 " + (totalTxBytes - history));
                    lastAccessed = System.currentTimeMillis();
                }
                if (totalTxBytes > 10000 + history) {
                    occurrences100000++;
                    recordedInitiations++;
                    long l = totalTxBytes - history;

                    System.out.println("h10 " + l);
                    lastAccessed = System.currentTimeMillis();
                }

                if (recordedInitiations >= 3 && (System.currentTimeMillis() - lastAccessed) >= 4000) {
                    break;
                }

                history = totalTxBytes;
            }
        }
//        return occurrences10002000 >= 7 && occurrences10002000 <= 13 ||
//                occurrences20003000 >= 4 && occurrences20003000 <= 7 ||
//                occurrences30004000 >= 3 && occurrences30004000 <= 5 ||
//                occurrences40005000 >= 2 && occurrences40005000 <= 5 ||
//                occurrences50006000 >= 1 && occurrences50006000 <= 3;
        return false;

    }

}
