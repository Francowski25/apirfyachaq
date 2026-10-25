package com.minero.appirfyachaq.generic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ResponseGeneric {

    public enum Type { SUCCESS, WARNING, ERROR, EXCEPTION }

    private Type type = Type.ERROR;
    private final List<String> messages = new ArrayList<>();

    public Type getType() {
        return type;
    }

    public List<String> getListMessage() {
        return Collections.unmodifiableList(messages);
    }

    public void success(String... msgs) {
        this.type = Type.SUCCESS;
        addAll(msgs);
    }

    public void warning(String... msgs) {
        this.type = Type.WARNING;
        addAll(msgs);
    }

    public void error(String... msgs) {
        this.type = Type.ERROR;
        addAll(msgs);
    }

    public void exception(String... msgs) {
        this.type = Type.EXCEPTION;
        addAll(msgs);
    }

    private void addAll(String... msgs) {
        if (msgs != null) {
            for (String m : msgs) this.messages.add(m);
        }
    }
}