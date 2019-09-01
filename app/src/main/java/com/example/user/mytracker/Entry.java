package com.example.user.mytracker;

public class Entry {
    private String name;
    private String amount;
    private String date;
    private String dateTime; // serves as unique identifier

    // rawEntry format: 'Fried Rice = 6000 = 31-08-2019 = 22-57-24'
    public Entry(String rawEntry) {
        String[] output = rawEntry.split(" = ");
        name = output[0];
        amount = output[1];
        date = output[2];
        dateTime = output[2] + "," + output[3]; // comma to separate date and time
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAmount() {
        return amount;
    }

    public String getFormattedAmount() {
        return formatAmount(amount);
    }

    public static String formatAmount(String amountToFormat) {
        return String.format("%,d", Long.parseLong(amountToFormat)) + " KRW";
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public String getDateTime() {
        return dateTime;
    }

    public boolean isEquals(String rawEntry) {
        String[] output = rawEntry.split(" = ");
        String rawEntryDateTime = output[2] + "," + output[3];
        return this.dateTime.equals(rawEntryDateTime);
    }

    public String getRawEntryString() {
        String[] output = dateTime.split(",");
        return name + " = " + amount + " = " + output[0] + " = " + output[1];
    }
}
