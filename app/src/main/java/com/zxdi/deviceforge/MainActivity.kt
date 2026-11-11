package com.zxdi.deviceforge

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import java.util.UUID
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val PERMISSION_REQUEST_CODE = 100

    private val proxyList = listOf(
        "103.152.112.12:8080",
        "180.250.110.45:3128",
        "202.65.139.15:80"
    )

    private lateinit var currentImei: String
    private lateinit var currentAndroidId: String
    private lateinit var currentGaid: String
    private lateinit var currentBtMac: String
    private lateinit var currentWifiMac: String
    private lateinit var currentModel: String
    private lateinit var currentBrand: String

    private val firstNames = listOf("Zidan", "Rian", "Dimas", "Fajar", "Galih", "Bayu", "Satria", "Raka", "Aldi", "Rehan")
    private val lastNames = listOf("Gamer", "Cyber", "Android", "Pwn", "Xploit", "Zxdi", "Blaster", "Phantom", "Slayer", "Vortex")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val btnRefreshProfile = Button(this).apply {
            text = "🔁 Auto-Replace All (Buat Identitas HP Baru)"
            setOnClickListener {
                generateNewVirtualDevice()
                applyAndReload()
                Toast.makeText(this@MainActivity, "Identitas HP & Proxy Berhasil Diganti Total!", Toast.LENGTH_SHORT).show()
            }
        }
        layout.addView(btnRefreshProfile)

        webView = WebView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }
        layout.addView(webView)

        setContentView(layout)
        requestDevicePermissions()
    }

    private fun requestDevicePermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE
        )
        val listPermissionsNeeded = ArrayList<String>()
        for (p in permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            initializeEngine()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            initializeEngine()
        }
    }

    private fun initializeEngine() {
        generateNewVirtualDevice()
        setupWebViewSettings()
        applyAndReload()
    }

    private fun generateNewVirtualDevice() {
        currentImei = generateFakeIMEI()
        currentAndroidId = UUID.randomUUID().toString().replace("-", "").substring(0, 16)
        currentGaid = UUID.randomUUID().toString()
        currentBtMac = generateRandomMAC()
        currentWifiMac = generateRandomMAC()
        
        val deviceModels = listOf(
            Pair("samsung", "SM-S928B"),
            Pair("google", "Pixel 8 Pro"),
            Pair("xiaomi", "Redmi Note 13 Pro"),
            Pair("oppo", "CPH2551")
        )
        val selectedDevice = deviceModels.random()
        currentBrand = selectedDevice.first
        currentModel = selectedDevice.second
    }

    private fun setupWebViewSettings() {
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.databaseEnabled = true
        webSettings.loadsImagesAutomatically = true
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webSettings.userAgentString = "Mozilla/5.0 (Linux; Android 14; $currentModel) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"
    }

    private fun applyAndReload() {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            val selectedProxy = proxyList.random()
            val proxyConfig = ProxyConfig.Builder().addProxyRule(selectedProxy).build()
            ProxyController.getInstance().setProxyOverride(proxyConfig, {}, {})
        }

        webView.clearCache(true)
        webView.clearHistory()

        val randFirstName = firstNames.random()
        val randLastName = lastNames.random()
        val randUsername = (randFirstName + randLastName + Random.nextInt(100, 9999)).lowercase()
        val randPassword = "Pass" + UUID.randomUUID().toString().substring(0, 8) + "99!"

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                val spoofingScript = """
                    Object.defineProperty(navigator, 'webdriver', { get: () => undefined });
                    window.navigator.chrome = { runtime: {} };
                    Object.defineProperty(navigator, 'hardwareConcurrency', { get: () => 8 });
                    Object.defineProperty(navigator, 'deviceMemory', { get: () => 8 });
                    localStorage.setItem('virtual_imei', '$currentImei');
                    localStorage.setItem('virtual_android_id', '$currentAndroidId');
                    localStorage.setItem('virtual_gaid', '$currentGaid');
                """
                webView.evaluateJavascript(spoofingScript, null)

                if (url != null && url.contains("signup")) {
                    val autoFillScript = """
                        setTimeout(function() {
                            let fName = document.querySelector("input[name='firstName']");
                            let lName = document.querySelector("input[name='lastName']");
                            
                            if (fName && !fName.value) {
                                fName.value = "$randFirstName";
                                fName.dispatchEvent(new Event('input', { bubbles: true }));
                            }
                            if (lName && !lName.value) {
                                lName.value = "$randLastName";
                                lName.dispatchEvent(new Event('input', { bubbles: true }));
                            }
                        }, 1200);
                    """
                    webView.evaluateJavascript(autoFillScript, null)
                }
            }
        }

        webView.loadUrl("https://accounts.google.com/signup/v2/createaccount?theme=glif&flowName=GlifWebSignIn&flowEntry=SignUp")
    }

    private fun generateFakeIMEI(): String {
        val r = Random(System.currentTimeMillis())
        val tac = "35" + String.format("%06d", r.nextInt(999999))
        val snr = String.format("%06d", r.nextInt(999999))
        val imeiBase = tac + snr
        var sum = 0
        for (i in imeiBase.indices) {
            var num = Character.getNumericValue(imeiBase[i])
            if (i % 2 == 1) {
                num *= 2
                if (num > 9) num -= 9
            }
            sum += num
        }
        val checksum = (10 - (sum % 10)) % 10
        return imeiBase + checksum
    }

    private fun generateRandomMAC(): String {
        val hexChars = "0123456789ABCDEF"
        val mac = StringBuilder()
        for (i in 0..5) {
            if (i > 0) mac.append(":")
            mac.append(hexChars[Random.nextInt(hexChars.length)])
            mac.append(hexChars[Random.nextInt(hexChars.length)])
        }
        return mac.toString()
    }
}
