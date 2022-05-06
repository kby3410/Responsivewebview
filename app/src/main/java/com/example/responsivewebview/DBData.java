package com.example.responsivewebview;

public class DBData {
    private String http;
    private String name;
    private Integer check_number;

    public DBData(String http, String name, Integer check_number) {
        this.http = http;
        this.name = name;
        this.check_number = check_number;
    }

    public String getHttp() {
        return http;
    }

    public String getName() {
        return name;
    }

    public Integer getCheck_number() {
        return check_number;
    }

    public void setHttp(String http) {
        this.http = http;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCheck_number(Integer check_number) {
        this.check_number = check_number;
    }
}
