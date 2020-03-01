package it.niedermann.nextcloud.deck.ui.exception;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.nextcloud.android.sso.helper.VersionCheckHelper;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

import it.niedermann.nextcloud.deck.R;
import it.niedermann.nextcloud.deck.databinding.ActivityExceptionBinding;

public class ExceptionActivity extends AppCompatActivity {


    private ActivityExceptionBinding binding;
    public static final String KEY_THROWABLE = "T";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        binding = ActivityExceptionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        super.onCreate(savedInstanceState);

        binding.copy.setOnClickListener((v) -> copyStacktraceToClipboard());
        binding.close.setOnClickListener((v) -> finish());

        Throwable throwable = ((Throwable) getIntent().getSerializableExtra(KEY_THROWABLE));
        throwable.printStackTrace();
        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.error);
        binding.message.setText(throwable.getMessage());


        String debugInfo = "";

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            debugInfo += "App Version: " + pInfo.versionName;
            debugInfo += "\nApp Version Code: " + pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            debugInfo += "\nApp Version: " + e.getMessage();
            e.printStackTrace();
        }

        try {
            debugInfo += "\nFiles App Version Code: " + VersionCheckHelper.getNextcloudFilesVersionCode(this);
        } catch (PackageManager.NameNotFoundException e) {
            debugInfo += "\nFiles App Version Code: " + e.getMessage();
            e.printStackTrace();
        }

        debugInfo += "\n\n---\n";
        debugInfo += "\nOS Version: " + System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
        debugInfo += "\nOS API Level: " + android.os.Build.VERSION.SDK_INT;
        debugInfo += "\nDevice: " + android.os.Build.DEVICE;
        debugInfo += "\nModel (and Product): " + android.os.Build.MODEL + " (" + android.os.Build.PRODUCT + ")";

        debugInfo += "\n\n---";

        binding.stacktrace.setText(debugInfo + "\n\n" + getStacktraceOf(throwable));
    }

    private String getStacktraceOf(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private void copyStacktraceToClipboard() {
        final android.content.ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(getString(R.string.simple_exception), "```\n" + binding.stacktrace.getText() + "\n```");
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
    }
}
