package com.orange.datasync.chat;

import com.orange.webcom.sdk.DataSnapshot;
import com.orange.webcom.sdk.WebcomException;

import java.util.Map;

public class Message {
    public String name;
    public String text;
    public String snapName;

    public Message(){}

    @SuppressWarnings("unchecked")
    public Message(DataSnapshot snapData) throws WebcomException {
        Map<String, String> m = (Map<String, String>) snapData.value();
        name = m.get("name");
        text = m.get("text");
        snapName = snapData.name();
    }

    public Message(String name, String text) {
        this.name = name;
        this.text = text;
    }

    public Message(String name, String snapName, String text) {
        this.name = name;
        this.snapName = snapName;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getName() {
        return name;
    }

    public Message setName(String name) {
        this.name = name;
        return this;
    }

    public Message setSnapName(String snapName) {
        this.snapName = snapName;
        return this;
    }

    public Message setText(String text) {
        this.text = text;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Message) {
            return snapName.equals(((Message) o).snapName);
        }
        return super.equals(o);
    }
}
