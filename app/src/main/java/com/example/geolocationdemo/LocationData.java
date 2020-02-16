package com.example.geolocationdemo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationData {

        @SerializedName("address")
        @Expose
        private String address;
        @SerializedName("time")
        @Expose
        private String date;
        @SerializedName("user_id")
        @Expose
        private String userId;
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

}
