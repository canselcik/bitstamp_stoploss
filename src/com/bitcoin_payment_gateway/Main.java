package com.bitcoin_payment_gateway;


import org.bitcoinj.core.ECKey;

public class Main {

    public static void main(String[] args) throws Exception {
        BitcoinNetworkProvider b = new BitcoinNetworkProvider("localhost", "inhash", "user", "kp5g6d", 1000);

        // Make sure we have at least 50 keys to watch
        while(b.getKeyCount() < 120){
            ECKey key = ECKeyUtils.getRandomKey();
            String address = ECKeyUtils.ECKeyToString(key);
            Logger.l("Importing: " + address);
            b.addKey(key, 0);
        }

        Logger.l("Following " + b.getKeyCount() + " addresses");
        b.start();
    }

}
