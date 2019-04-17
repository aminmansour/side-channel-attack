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

    public boolean scan(long MS_LENGTH_IN_PERIOD) {

        int id = ++PERIOD_SEARCH_ID_COUNTER;
        long periodStart = System.currentTimeMillis();
        long periodEnd = System.currentTimeMillis() + MS_LENGTH_IN_PERIOD;
        long lastAccessed = periodStart;
        long history = TrafficStats.getTotalTxBytes();
        long periodRecordingPoint = periodStart + 3000;
        int recordedInitiations = 0;

        int occurrences10002000 = 0;
        int occurrences20003000 = 0;
        ;
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

                if (totalTxBytes > 6000 + history) {
                    occurrences6000Plus++;
                    recordedInitiations++;
                    System.out.println("h6 " + (totalTxBytes - history));
                    lastAccessed = System.currentTimeMillis();
                }

                if (recordedInitiations >= 2 && (System.currentTimeMillis() - lastAccessed) >= 4000) {
                    break;
                }

                history = totalTxBytes;
            }
        }
        return (occurrences10002000 >= 4 ||
                occurrences20003000 >= 4 ||
                occurrences30004000 >= 3 ||
                occurrences40005000 >= 2 ||
                occurrences50006000 >= 2 ||
                occurrences6000Plus >= 2) && periodRecordingPoint >= 3;

    }

}
