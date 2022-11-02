package com.csse.transshift.models;

public class ReadWriteUserDetails {

    public String dob, mobile;
    public String balance = "1320";

    public ReadWriteUserDetails() {
    }

    public ReadWriteUserDetails(String textDoB, String textMobile, String balance) {
        this.dob = textDoB;
        this.mobile = textMobile;
        this.balance = balance;
    }
}
