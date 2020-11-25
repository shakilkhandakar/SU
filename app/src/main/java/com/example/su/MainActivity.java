package com.example.su;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class MainActivity extends AppCompatActivity {
    ProgressBar progressBar;
    SwipeRefreshLayout refreshLayout;
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        progressBar = findViewById(R.id.progressBar);
        refreshLayout = findViewById(R.id.swipeId);
        webView = findViewById(R.id.webViewID);

        refreshLayout.setColorSchemeColors(Color.YELLOW, Color.BLUE, Color.GREEN);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.reload();
            }
        });
        checkConnection();
        load();
    }

    public void checkConnection() {
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        assert manager != null;
        NetworkInfo network = manager.getActiveNetworkInfo();
        if (network != null) {
            if (network.getType() == ConnectivityManager.TYPE_WIFI || network.getType() == ConnectivityManager.TYPE_MOBILE) {

            } else {
                alertDialog();
            }
        } else {
            alertDialog();
        }
    }

    public void alertDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setIcon(R.drawable.no_internet_icon);
        alert.setTitle("Connection Failed !");
        alert.setView(LayoutInflater.from(this).inflate(R.layout.connection_error, null));
        alert.setCancelable(false);
        alert.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alert.setNeutralButton("Refresh", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkConnection();
                load();
            }
        });
        alert.create().show();
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void load() {
        webView.loadUrl("http://www.su.edu.bd/");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(newProgress);
                setTitle("Loading.....");
                if (newProgress == 100) {
                    progressBar.setVisibility(View.GONE);
                    setTitle(view.getTitle());
                }
                super.onProgressChanged(view, newProgress);
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                refreshLayout.setRefreshing(false);
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(true);
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        ws.setLoadsImagesAutomatically(true);
        ws.setCacheMode(WebSettings.LOAD_NO_CACHE);
        ws.setSupportZoom(true);
        ws.setBuiltInZoomControls(false);

        webView.setDownloadListener(new DownloadListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onDownloadStart(final String url, final String userAgent, final String contentDisposition, final String mimetype, long contentLength) {
                Dexter.withContext(MainActivity.this).withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                                request.setMimeType(mimetype);
                                String cookies = CookieManager.getInstance().getCookie(url);
                                request.addRequestHeader("cookie", cookies);
                                request.addRequestHeader("userAgent", userAgent);
                                request.setDescription("Downloading File.....");
                                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));
                                request.allowScanningByMediaScanner();
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                                        URLUtil.guessFileName(url, contentDisposition, mimetype));
                                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                downloadManager.enqueue(request);
                                Toast.makeText(MainActivity.this, "Downloading File", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        }).check();

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setIcon(R.drawable.ic_baseline_home_24);
            alert.setTitle("EXIT");
            alert.setMessage("Do you want to Exit ?");
            alert.setNeutralButton("No", null);
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finishAffinity();
                }
            });
            alert.create().show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back:
                onBackPressed();
                break;
            case R.id.forWord:
                if (webView.canGoForward()) {
                    webView.goForward();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
