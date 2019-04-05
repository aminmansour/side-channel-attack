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
        int occurrences = 0;
        while ( id == PERIOD_SEARCH_ID_COUNTER && System.currentTimeMillis() <= periodEnd) {

            long totalTxBytes = TrafficStats.getTotalTxBytes();
            String info = "";
            if(totalTxBytes > LBOUND_BYTE_CHANGE + history &&
                    totalTxBytes < UBOUND_BYTE_CHANGE + history) {
                occurrences++;
                info += ("\tReceived: " + TrafficStats.getTotalRxBytes() + " bytes / " + TrafficStats.getTotalRxPackets() + " packets\n");
                info += ("\tTransmitted: " + totalTxBytes + " bytes / " + TrafficStats.getTotalTxPackets() + " packets\n");
                System.out.println("code " + SystemClock.currentThreadTimeMillis() +"old : "+history+ "new : " + totalTxBytes +" diff: "+(totalTxBytes-history)+" "+ info);
            }
            System.out.println("enter");
            history = totalTxBytes;
        }

        if (System.currentTimeMillis() > periodEnd &&
                occurrences >= LBOUND_OF_TX_EVENTS_PER_PERIOD &&
                occurrences <= UBOUND_OF_TX_EVENTS_PER_PERIOD){
            return true;
        }
        return false;
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

        if (System.currentTimeMillis() > periodEnd &&
                occurrences >= LBOUND_OF_RX_EVENTS_PER_PERIOD &&
                occurrences <= UBOUND_OF_RX_EVENTS_PER_PERIOD){
            return true;
        }
        return false;
    }
}
