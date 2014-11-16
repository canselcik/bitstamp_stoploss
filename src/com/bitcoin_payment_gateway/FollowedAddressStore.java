package com.bitcoin_payment_gateway;

import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;


public class FollowedAddressStore {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(FollowedAddressStore.class);

    private Connection conn;
    public FollowedAddressStore(String host, String dbName, String user, String pass) throws SQLException {
        String connString = "jdbc:postgresql://" + host + "/" + dbName;
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", pass);

        conn = DriverManager.getConnection(connString, props);
    }

    public int addAddress(int userId, String addressInformation) throws SQLException {
        String[] components = addressInformation.split(",");
        if(components.length != 3){
            log.error("A parsing error occured while adding address into DB ({})", addressInformation);
            return 0;
        }
        String address = components[0];
        String priv_key = components[1];
        String export_priv_key = components[2];

        conn.setAutoCommit(true);
        String query = "INSERT INTO active_addresses (user_id, address, priv_key, export_priv_key) VALUES (?, ?, ?, ?);";
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setFetchSize(1);
        pst.setInt(1, userId);
        pst.setString(2, address);
        pst.setString(3, priv_key);
        pst.setString(4, export_priv_key);

        int updated_rows = pst.executeUpdate();
        pst.close();
        return updated_rows;
    }

    public boolean removeAddressByAnything(String anything) throws SQLException {
        if(anything == null)
            return false;

        conn.setAutoCommit(true);
        PreparedStatement pst = conn.prepareStatement("DELETE FROM active_addresses WHERE address=? OR priv_key=? OR export_priv_key=?;");
        pst.setFetchSize(1);
        pst.setString(1, anything);
        pst.setString(2, anything);
        pst.setString(3, anything);

        boolean result = pst.executeUpdate() > 0;
        pst.close();
        return result;
    }

    public ArrayList<String> getAddresses() throws SQLException {
        ArrayList<String> addr = new ArrayList<String>();
        if(conn == null) {
            log.error("~~ Cannot connect to the database ~~");
            return addr;
        }

        conn.setAutoCommit(false);
        Statement st = conn.createStatement();
        st.setFetchSize(50);

        ResultSet rs = st.executeQuery("SELECT user_id, address, priv_key, export_priv_key FROM active_addresses;");
        while(rs.next()){
            String address = rs.getString(2);
            String priv_key = rs.getString(3);
            String export_priv_key = rs.getString(4);
            addr.add(address + "," + priv_key + "," + export_priv_key);
        }

        rs.close();
        st.close();
        return addr;
    }
}
