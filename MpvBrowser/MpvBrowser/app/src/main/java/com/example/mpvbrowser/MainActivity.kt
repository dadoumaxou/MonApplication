package com.example.mpvbrowser

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast

/**
 * Navigateur web minimal.
 * Toute URL pointant vers un fichier vidéo (extension connue) ou tout
 * téléchargement de type vidéo est automatiquement envoyé vers MPV
 * (package "is.xyz.mpv" — mpv-android) au lieu d'être chargé dans la page.
 * Un appui long sur un lien propose aussi "Ouvrir avec MPV".
 * Le bouton "MPV" en haut envoie l'URL actuellement affichée vers MPV
 * (utile pour les pages de streaming où le lecteur intégré du site ne
 * convient pas).
 */
class MainActivity : Activity() {

    private lateinit var webView: WebView
    private lateinit var addressBar: EditText
    private lateinit var progressBar: ProgressBar

    private val videoExtensions = listOf(
        ".mp4", ".mkv", ".avi", ".mov", ".webm", ".flv", ".m3u8", ".ts", ".wmv"
    )

    // Package de mpv-android. Si tu utilises un fork différent, change cette valeur.
    private val mpvPackage = "is.xyz.mpv"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        addressBar = findViewById(R.id.addressBar)
        progressBar = findViewById(R.id.progressBar)

        val btnBack: ImageButton = findViewById(R.id.btnBack)
        val btnForward: ImageButton = findViewById(R.id.btnForward)
        val btnReload: ImageButton = findViewById(R.id.btnReload)
        val btnMpv: Button = findViewById(R.id.btnMpv)

        setupWebView()

        btnBack.setOnClickListener { if (webView.canGoBack()) webView.goBack() }
        btnForward.setOnClickListener { if (webView.canGoForward()) webView.goForward() }
        btnReload.setOnClickListener { webView.reload() }
        btnMpv.setOnClickListener { openInMpv(webView.url) }

        addressBar.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                loadFromAddressBar()
                true
            } else {
                false
            }
        }

        webView.loadUrl("https://www.google.com")
    }

    private fun setupWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.visibility = if (newProgress in 1..99) View.VISIBLE else View.GONE
                progressBar.progress = newProgress
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                if (isVideoUrl(url)) {
                    openInMpv(url)
                    return true
                }
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                addressBar.setText(url)
            }
        }

        // Beaucoup de sites déclenchent un "téléchargement" pour les vidéos
        // plutôt qu'une simple navigation : on l'intercepte aussi.
        webView.setDownloadListener { url, _, _, mimeType, _ ->
            if (isVideoUrl(url) || (mimeType != null && mimeType.startsWith("video"))) {
                openInMpv(url)
            } else {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this, "Impossible d'ouvrir ce lien", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Appui long sur un lien -> proposer "Ouvrir avec MPV"
        webView.setOnLongClickListener {
            val result = webView.hitTestResult
            if (result.type == WebView.HitTestResult.SRC_ANCHOR_TYPE ||
                result.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE
            ) {
                val linkUrl = result.extra
                if (linkUrl != null) {
                    AlertDialog.Builder(this)
                        .setTitle(linkUrl)
                        .setItems(arrayOf("Ouvrir dans le navigateur", "Ouvrir avec MPV")) { _, which ->
                            when (which) {
                                0 -> webView.loadUrl(linkUrl)
                                1 -> openInMpv(linkUrl)
                            }
                        }
                        .show()
                    return@setOnLongClickListener true
                }
            }
            false
        }
    }

    private fun isVideoUrl(url: String): Boolean {
        val clean = url.substringBefore("?").lowercase()
        return videoExtensions.any { clean.endsWith(it) }
    }

    private fun openInMpv(url: String?) {
        if (url.isNullOrBlank()) {
            Toast.makeText(this, "Aucune URL à ouvrir", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(url), "video/*")
        intent.setPackage(mpvPackage)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "MPV n'est pas installé sur cet appareil", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadFromAddressBar() {
        var input = addressBar.text.toString().trim()
        if (input.isEmpty()) return

        val looksLikeUrl = input.contains(".") && !input.contains(" ")
        if (looksLikeUrl) {
            if (!input.startsWith("http://") && !input.startsWith("https://")) {
                input = "https://$input"
            }
            webView.loadUrl(input)
        } else {
            webView.loadUrl("https://www.google.com/search?q=" + Uri.encode(input))
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
