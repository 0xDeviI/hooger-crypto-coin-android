package com.hoogercoin.models;

public class Transaction {

    public enum TransactionType {
        BUY_COIN,
        SELL_COIN
    }

    private String sender, receiver, tsId, tsTime;
    private int ts_timestamp;
    private TransactionType ts_type;
    private double hooger_price;
    private double amount;

    public Transaction(String sender, String receiver, String tsId, String tsTime, int ts_timestamp, TransactionType ts_type, double hooger_price, double amount) {
        this.sender = sender;
        this.receiver = receiver;
        this.tsId = tsId;
        this.tsTime = tsTime;
        this.ts_timestamp = ts_timestamp;
        this.ts_type = ts_type;
        this.hooger_price = hooger_price;
        this.amount = amount;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getTsId() {
        return tsId;
    }

    public void setTsId(String tsId) {
        this.tsId = tsId;
    }

    public String getTsTime() {
        return tsTime;
    }

    public void setTsTime(String tsTime) {
        this.tsTime = tsTime;
    }

    public int getTs_timestamp() {
        return ts_timestamp;
    }

    public void setTs_timestamp(int ts_timestamp) {
        this.ts_timestamp = ts_timestamp;
    }

    public TransactionType getTs_type() {
        return ts_type;
    }

    public void setTs_type(TransactionType ts_type) {
        this.ts_type = ts_type;
    }

    public double getHooger_price() {
        return hooger_price;
    }

    public void setHooger_price(double hooger_price) {
        this.hooger_price = hooger_price;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
