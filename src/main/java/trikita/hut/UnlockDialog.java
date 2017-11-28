package trikita.hut;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;
import java.security.MessageDigest;
import java.lang.StringBuilder;
import android.util.Log;

public class UnlockDialog extends Activity {
    private Button unlockButton;
    private Button lockButton;
    private EditText password;

	public void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.prompts);
        unlockButton = (Button) findViewById(R.id.unlockButton);
        lockButton = (Button) findViewById(R.id.lockButton);
        password = (EditText) findViewById(R.id.editTextDialogUserInput);

        unlockButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!getSha1Hex(password.getText().toString())
                    .equals("ea1b37da89c9714afa6e8264911e12813c480b40")) {
                    Log.d("SHA1:",getSha1Hex(password.getText().toString()));
                    return;
                }
                Intent intent = new Intent();
                intent.setAction("hut.trikita.PARENT_UNLOCK");
                //intent.putExtra("data","Notice me senpai!");
                sendBroadcast(intent);
            }
        });
        lockButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("hut.trikita.PARENT_LOCK");
                //intent.putExtra("data","Notice me senpai!");
                sendBroadcast(intent);
            }
        });
    }

    public static String getSha1Hex(String clearString)
    {
        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(clearString.getBytes("UTF-8"));
            byte[] bytes = messageDigest.digest();
            StringBuilder buffer = new StringBuilder();
            for (byte b : bytes)
            {
                buffer.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return buffer.toString();
        }
        catch (Exception ignored)
        {
            ignored.printStackTrace();
            return null;
        }
    }
}
