package ir.ngra.drivertrafficcontroller.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ModelResponcePrimery {

    @SerializedName("messages")
    ArrayList<ModelMessage> messages;

    public ArrayList<ModelMessage> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<ModelMessage> messages) {
        this.messages = messages;
    }
}
