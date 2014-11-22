package com.bitcoin_payment_gateway;

import org.bitcoinj.core.*;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.utils.Threading;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;

public class BitcoinNetworkProvider {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(BitcoinNetworkProvider.class);

    private BitcoinNetworkEventListener eventListener;
    private PeerGroup vPeerGroup;
    private SPVBlockStore s;
    private NetworkParameters params;
    private FollowedAddressStore fas;
    private BlockChain bc;
    private Wallet w;
    private String coldWalletAddr;

    public BitcoinNetworkProvider(String DB_HOST, String DB_NAME, String DB_USER, String DB_PASSWD,
                                  String spvBlockStorePath, NetworkParameters params, long fastCatchup, String coldWalletAddr) throws Exception {
        fas = new FollowedAddressStore(DB_HOST, DB_NAME, DB_USER, DB_PASSWD);
        s   = new SPVBlockStore(params, new File(spvBlockStorePath));
        bc  = new BlockChain(params, s);
        this.params = params;
        this.coldWalletAddr = coldWalletAddr;

        vPeerGroup = new PeerGroup(params, bc);
        vPeerGroup.setUserAgent("Satoshi", "0.9.3");
        vPeerGroup.addPeerDiscovery( new DnsDiscovery(params) );
        vPeerGroup.setMaxConnections(16);

        if(fastCatchup != 0) {
            log.info("Starting BitcoinNetworkProvider with {} fastCatchupParam", fastCatchup);
            vPeerGroup.setFastCatchupTimeSecs(fastCatchup);
        }

        // Importing addresses from DB
        w = new Wallet(params);
        ArrayList<String> addresses = fas.getAddresses();
        for(String addr : addresses){
            ECKey key = ECKeyUtils.getECKey(addr);
            if(key != null)
                w.importKey(key);
        }
        vPeerGroup.addWallet(w);

        eventListener = new BitcoinNetworkEventListener(vPeerGroup, bc, s, w, params, this.coldWalletAddr);
        bc.addListener(eventListener, Threading.THREAD_POOL);
    }


    public void start() throws Exception {
        vPeerGroup.startAsync();

        log.info("Waiting for at least 8 peers...");
        vPeerGroup.waitForPeers(8).get();

        log.info("Initiating blockchain sync");
        vPeerGroup.downloadBlockChain();
    }

    public NetworkParameters getNetworkParameters(){
        return this.params;
    }

    public boolean addKey(ECKey key, int user_id) {
        String serialized = ECKeyUtils.ECKeyToString(key, params);
        boolean res = true;
        try {
            res = res && fas.addAddress(user_id, serialized) <= 1;
        } catch (SQLException e){
            e.printStackTrace();
            res = false;
        }
        return w.importKey(key) && res;
    }

    public int getKeyCount() throws SQLException {
        ArrayList<String> addr = fas.getAddresses();
        if(fas == null || addr == null){
            log.error("Failed to get addresses from DB");
            return -1;
        }
        return addr.size();
    }

    public boolean removeKey(ECKey key){
        boolean res = true;
        try {
            String priv_key = key.getPrivateKeyEncoded(params).toString();
            res = res && fas.removeAddressByAnything(priv_key);
        } catch (SQLException e) {
            e.printStackTrace();
            res = res && false;
        }

        if(w.removeKey(key))
            return res;
        log.error("Failed to load the key into wallet");
        return false;
    }
}
