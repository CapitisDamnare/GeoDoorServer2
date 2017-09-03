package com.tapsi;

public class Client {

    private int _id;
    private String _name;
    private boolean _allowed;

    public Client() {
    }

    public Client(String _name) {
        this._name = _name;
        this._allowed = false;
    }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public boolean is_allowed() {
        return _allowed;
    }

    public void set_allowed(boolean _allowed) {
        this._allowed = _allowed;
    }
}