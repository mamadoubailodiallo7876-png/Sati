package com.wangzan101.mika.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.wangzan101.mika.core.cap.canvas.CanvasPort
import com.wangzan101.mika.core.cap.canvas.CanvasPortRegistry
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.coroutines.resume
import kotlin.math.roundToInt

object GangouCanvasState {
    var currentHtml by mutableStateOf("")
    var currentTitle by mutableStateOf("Toile Blanche")
    var lastSummary by mutableStateOf("")
    var renderVersion by mutableIntStateOf(0)
    private val undoStack = ArrayDeque<Pair<String, String>>()
    private val redoStack = ArrayDeque<Pair<String, String>>()
    var canUndo by mutableStateOf(false)
    var canRedo by mutableStateOf(false)
    var webViewRef: WebView? = null

    private fun rootDir(ctx: Context): File = File(ctx.filesDir, "gangou_canvas").apply { if (!exists()) mkdirs() }
    private fun currentFile(ctx: Context): File = File(rootDir(ctx), "current.html")
    private fun metaFile(ctx: Context): File = File(rootDir(ctx), "meta.json")
    private fun spacesDir(ctx: Context): File = File(rootDir(ctx), "spaces").apply { if (!exists()) mkdirs() }

    fun loadFromDisk(ctx: Context) {
        if (renderVersion > 0) return
        runCatching {
            val f = currentFile(ctx)
            if (f.exists()) currentHtml = f.readText(Charsets.UTF_8)
            val m = metaFile(ctx)
            if (m.exists()) {
                val obj = JSONObject(m.readText(Charsets.UTF_8))
                currentTitle = obj.optString("title", "Toile Blanche")
                lastSummary = obj.optString("summary", "")
            }
            renderVersion++
        }
    }

    private fun persist(ctx: Context) {
        runCatching {
            currentFile(ctx).writeText(currentHtml, Charsets.UTF_8)
            metaFile(ctx).writeText(JSONObject().apply {
                put("title", currentTitle)
                put("summary", lastSummary)
            }.toString(), Charsets.UTF_8)
        }
    }

    fun applyUi(ctx: Context, html: String, title: String?, summary: String?) {
        if (currentHtml.isNotBlank()) {
            undoStack.addLast(currentTitle to currentHtml)
            if (undoStack.size > 20) undoStack.removeFirst()
        }
        redoStack.clear()
        currentHtml = html
        currentTitle = title?.takeIf { it.isNotBlank() } ?: "Espace Gangou"
        lastSummary = summary.orEmpty()
        canUndo = undoStack.isNotEmpty()
        canRedo = false
        persist(ctx)
        renderVersion++
    }

    fun reset(ctx: Context) {
        if (currentHtml.isNotBlank()) {
            undoStack.addLast(currentTitle to currentHtml)
            if (undoStack.size > 20) undoStack.removeFirst()
        }
        redoStack.clear()
        currentHtml = ""
        currentTitle = "Toile Blanche"
        lastSummary = ""
        canUndo = undoStack.isNotEmpty()
        canRedo = false
        persist(ctx)
        renderVersion++
    }

    fun undo(ctx: Context) {
        if (undoStack.isEmpty()) return
        redoStack.addLast(currentTitle to currentHtml)
        val prev = undoStack.removeLast()
        currentTitle = prev.first
        currentHtml = prev.second
        canUndo = undoStack.isNotEmpty()
        canRedo = redoStack.isNotEmpty()
        persist(ctx)
        renderVersion++
    }

    fun redo(ctx: Context) {
        if (redoStack.isEmpty()) return
        undoStack.addLast(currentTitle to currentHtml)
        val next = redoStack.removeLast()
        currentTitle = next.first
        currentHtml = next.second
        canUndo = undoStack.isNotEmpty()
        canRedo = redoStack.isNotEmpty()
        persist(ctx)
        renderVersion++
    }

    fun listSpaces(ctx: Context): List<String> =
        spacesDir(ctx).listFiles()?.filter { it.extension == "html" }?.map { it.nameWithoutExtension }?.sorted() ?: emptyList()

    fun saveSpace(ctx: Context, name: String): String {
        val safe = name.trim().replace(Regex("[^a-zA-Z0-9._ -]"), "_").ifBlank { "espace" }
        File(spacesDir(ctx), "$safe.html").writeText(currentHtml, Charsets.UTF_8)
        return "Espace '$safe' sauvegardé"
    }

    fun loadSpace(ctx: Context, name: String): String {
        val safe = name.trim().replace(Regex("[^a-zA-Z0-9._ -]"), "_")
        val f = File(spacesDir(ctx), "$safe.html")
        if (!f.exists()) return "Espace introuvable: $name"
        applyUi(ctx, f.readText(Charsets.UTF_8), safe, "Chargé depuis les espaces")
        return "Espace '$safe' chargé"
    }

    fun deleteSpace(ctx: Context, name: String): String {
        val safe = name.trim().replace(Regex("[^a-zA-Z0-9._ -]"), "_")
        val f = File(spacesDir(ctx), "$safe.html")
        return if (f.exists() && f.delete()) "Espace '$safe' supprimé" else "Espace introuvable: $name"
    }
}

fun registerAndroidCanvasPort(context: Context) {
    val appCtx = context.applicationContext
    GangouCanvasState.loadFromDisk(appCtx)
    if (CanvasPortRegistry.port != null) return
    val mainHandler = Handler(Looper.getMainLooper())

    CanvasPortRegistry.port = object : CanvasPort {
        override suspend fun setUi(html: String, title: String?, summary: String?): String {
            mainHandler.post { GangouCanvasState.applyUi(appCtx, html, title, summary) }
            return "OK: toile Gangou mise à jour (${title ?: "Espace Gangou"}, ${html.length / 1024} Ko)"
        }

        override suspend fun getUi(): String {
            val isBlank = GangouCanvasState.currentHtml.isBlank()
            return JSONObject().apply {
                put("title", GangouCanvasState.currentTitle)
                put("is_blank", isBlank)
                put("html_length", GangouCanvasState.currentHtml.length)
                put("html", GangouCanvasState.currentHtml.take(24000))
            }.toString()
        }

        override suspend fun evalJs(js: String): String {
            val res = withTimeoutOrNull(6000L) {
                suspendCancellableCoroutine<String> { cont ->
                    mainHandler.post {
                        val wv = GangouCanvasState.webViewRef
                        if (wv == null) {
                            if (cont.isActive) cont.resume("ERROR: canvas not mounted")
                        } else {
                            wv.evaluateJavascript(js) { out ->
                                if (cont.isActive) cont.resume(out ?: "null")
                            }
                        }
                    }
                }
            }
            return res ?: "ERROR: timeout"
        }

        override suspend fun resetCanvas(): String {
            mainHandler.post { GangouCanvasState.reset(appCtx) }
            return "OK: toile réinitialisée en Toile Blanche"
        }

        override suspend fun manageSpaces(action: String, name: String?): String {
            return when (action) {
                "list" -> JSONArray(GangouCanvasState.listSpaces(appCtx)).toString()
                "save" -> GangouCanvasState.saveSpace(appCtx, name ?: GangouCanvasState.currentTitle)
                "load" -> GangouCanvasState.loadSpace(appCtx, name.orEmpty())
                "delete" -> GangouCanvasState.deleteSpace(appCtx, name.orEmpty())
                else -> "Action inconnue: $action"
            }
        }
    }
}
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GangouCanvasScreen() {
    val context = LocalContext.current
    LaunchedEffect(Unit) { GangouCanvasState.loadFromDisk(context) }

    val html = GangouCanvasState.currentHtml
    val version = GangouCanvasState.renderVersion

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050811))
            .statusBarsPadding()
    ) {
        if (html.isNotBlank()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(android.graphics.Color.parseColor("#050811"))
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        @Suppress("DEPRECATION")
                        settings.databaseEnabled = true
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        settings.allowFileAccess = true
                        settings.allowContentAccess = true
                        @Suppress("DEPRECATION")
                        settings.allowUniversalAccessFromFileURLs = true
                        webViewClient = WebViewClient()
                        webChromeClient = object : WebChromeClient() {
                            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                                if (!message.isNullOrBlank()) Toast.makeText(ctx, message, Toast.LENGTH_LONG).show()
                                result?.confirm()
                                return true
                            }
                            override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                                result?.confirm()
                                return true
                            }
                            override fun onPermissionRequest(request: PermissionRequest?) {
                                request?.grant(request.resources)
                            }
                        }
                        GangouCanvasState.webViewRef = this
                        tag = -1
                    }
                },
                update = { wv ->
                    GangouCanvasState.webViewRef = wv
                    if (wv.tag != version) {
                        wv.tag = version
                        wv.loadDataWithBaseURL("https://gangou.local/", html, "text/html", "UTF-8", null)
                    }
                }
            )
        }
    }
}
