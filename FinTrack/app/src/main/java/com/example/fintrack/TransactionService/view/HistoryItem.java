package com.example.fintrack.TransactionService.view;

import com.example.fintrack.TransactionService.data.entity.TransactionEntity;

public class HistoryItem {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_TX = 1;

    public int type;
    public String header;
    public TransactionEntity tx;

    public HistoryItem() {}

    // header item
    public static HistoryItem header(String text) {
        HistoryItem i = new HistoryItem();
        i.type = TYPE_HEADER;
        i.header = text;
        return i;
    }

    public static HistoryItem tx(TransactionEntity tx) {
        HistoryItem i = new HistoryItem();
        i.type = TYPE_TX;
        i.tx = tx;
        return i;
    }

    public HistoryItem(String header){
        this.type = TYPE_HEADER;
        this.header = header;
    }

    public HistoryItem(TransactionEntity tx){
        this.type = TYPE_TX;
        this.tx = tx;
    }
}