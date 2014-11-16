package com.bitcoin_payment_gateway;

import org.bitcoinj.core.*;
import org.bitcoinj.store.BlockStore;

import javax.annotation.Nullable;
import java.util.List;

public class BitcoinNetworkEventListener implements PeerEventListener {
    private PeerGroup vPeerGroup;
    private BlockChain bc;
    private BlockStore bs;
    private boolean displayEvents;
    public BitcoinNetworkEventListener(PeerGroup vPeerGroup, BlockChain bc, BlockStore bs, boolean displayEvents){
        super();
        this.bc = bc;
        this.bs = bs;
        this.displayEvents = displayEvents;
        this.vPeerGroup = vPeerGroup;
    }
    @Override
    public void onBlocksDownloaded(Peer peer, Block block, int blocksLeft) {
        if(!displayEvents) return;
        Logger.l("Downloaded block #" + block.getHash() + " (remaining=" + blocksLeft + ")");
    }

    @Override
    public void onChainDownloadStarted(Peer peer, int blocksLeft) {
        if(!displayEvents) return;
        Logger.l("Blockchain download has started (remaining=" + blocksLeft + ")");
    }

    @Override
    public void onPeerConnected(Peer peer, int peerCount) {
        if(!displayEvents) return;
        Logger.l("Connected to peer " + peer.toString() + " (peers=" + peerCount + ")");
    }

    @Override
    public void onPeerDisconnected(Peer peer, int peerCount) {
        if(!displayEvents) return;
        Logger.l("Peer" + peer.toString() + " disconnected (peers=" + peerCount + ")");
    }

    @Override
    public Message onPreMessageReceived(Peer peer, Message m) {
        return m;
    }

    @Override
    public void onTransaction(Peer peer, Transaction t) {
        if(!displayEvents) return;
        Logger.l("TX: " + t.getHash().toString());
    }

    @Nullable
    @Override
    public List<Message> getData(Peer peer, GetDataMessage m) {
        return null;
    }
}