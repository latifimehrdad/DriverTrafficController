package ir.ngra.drivertrafficcontroller.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;

import ir.ngra.drivertrafficcontroller.R;
import ir.ngra.drivertrafficcontroller.models.ModelMessage;
import ir.ngra.drivertrafficcontroller.models.ModelResponcePrimery;
import retrofit2.Response;


public class StaticFunctions {

    public static String GetAuthorization(Context context) {//______________________________________ Start GetAuthorization
        String Authorization = "Bearer ";
        SharedPreferences prefs = context.getSharedPreferences("dtrafficcontrollertoken", 0);
        if (prefs != null) {
            String access_token = prefs.getString("accesstoken", null);
            if (access_token != null)
                Authorization = Authorization + access_token;
        }
        return Authorization;
    }//_____________________________________________________________________________________________ End GetAuthorization


    public static TextWatcher TextChangeForChangeBack(EditText editText) {//________________________ Satart TextChangeForChangeBack

        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editText.setBackgroundResource(R.drawable.edit_back);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

    }//_____________________________________________________________________________________________ End TextChangeForChangeBack


//
//    public static View.OnKeyListener SetBackClickAndGoHome(Boolean execute) {//_____________________ Start SetBackClickAndGoHome
//        return new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//
//                if (event.getAction() != KeyEvent.ACTION_DOWN)
//                    return true;
//
//                if (keyCode != 4) {
//                    return false;
//                }
//                if (execute)
//                    MainActivity1.FragmentMessage.onNext("Main");
//                return true;
//            }
//        };
//    }//_____________________________________________________________________________________________ End SetBackClickAndGoHome


    public static String CheckResponse(Response response, Boolean Authorization) {//________________ Start CheckResponse
        if (response.body() != null)
            return null;
        else {
            if (Authorization) {
                try {
                    String Messages = "";
                    Gson gson = new Gson();
                    ModelResponcePrimery messages = gson.fromJson(
                            response.errorBody().string(),
                            ModelResponcePrimery.class);

                    if (messages.getMessages().size() == 0)
                        Messages = "No Message";
                    else {
                        for (ModelMessage message : messages.getMessages()) {
                            Messages = Messages + message.getMessage();
                        }
                    }
                    return Messages;
                } catch (Exception ex) {
                    return "Failure";
                }
            } else {
                return GetErrorMessage(response);
            }
        }

    }//_____________________________________________________________________________________________ End CheckResponse


    public static String GetErrorMessage(Response response) {//_____________________________________ Start GetErrorٍMessage
        try {
            JSONObject jObjError = new JSONObject(response.errorBody().string());
            JSONArray jsonArray = jObjError.getJSONArray("messages");
            String message = "";
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject temp = new JSONObject(jsonArray.get(i).toString());
                message = message + temp.getString("message");
                message = message + "\n";
            }
            return message;
        } catch (Exception ex) {
            return "Failure";
        }
    }//_____________________________________________________________________________________________ End GetErrorٍMessage


//    public static String GetMessage(Response<ModelResponcePrimery> response) {//____________________ Start GetMessage
//        try {
//            ArrayList<ModelMessage> modelMessages = response.body().getMessages();
//            String message = "";
//            for (int i = 0; i < modelMessages.size(); i++) {
//                message = message + modelMessages.get(i).getMessage();
//                message = message + "\n";
//            }
//            return message;
//        } catch (Exception ex) {
//            return "Failure";
//        }
//    }//_____________________________________________________________________________________________ End GetMessage


}
