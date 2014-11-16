package com.bitcoin_payment_gateway;


import java.util.logging.Level;
import java.util.logging.Logger;

import org.bitcoinj.core.ECKey;

public class Main {

    private final static java.util.logging.Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception {
        LOGGER.setLevel(Level.ALL);
        LOGGER.fine("Starting BitcoinNetworkProvider...");
        BitcoinNetworkProvider b = new BitcoinNetworkProvider("localhost", "inhash", "user", "kp5g6d", 1000);

        // Make sure we have at least 50 keys to watch
        while(b.getKeyCount() < 120){
            ECKey key = ECKeyUtils.getRandomKey();
            String address = ECKeyUtils.ECKeyToString(key);
            com.bitcoin_payment_gateway.Logger.l("Importing: " + address);
            b.addKey(key, 0);
        }

        com.bitcoin_payment_gateway.Logger.l("Following " + b.getKeyCount() + " addresses");
        b.start();
    }

}
