package com.wangzan101.mika.core.cap.canvas

import com.wangzan101.mika.core.capability.Capability
import com.wangzan101.mika.core.capability.CapabilityStatus
import com.wangzan101.mika.core.capability.PromptEnv
import com.wangzan101.mika.core.tool.AgentTool
import com.wangzan101.mika.core.tool.Args
import com.wangzan101.mika.core.tool.Risk
import com.wangzan101.mika.core.tool.ToolContext
import com.wangzan101.mika.core.tool.ToolDef
import com.wangzan101.mika.core.tool.ToolExecution
import com.wangzan101.mika.core.tool.Verb
import com.wangzan101.mika.core.tool.failCode
import com.wangzan101.mika.core.tool.ok
import com.wangzan101.mika.core.tool.str
import kotlinx.serialization.json.put

const val CANVAS_CAP = "canvas"

interface CanvasPort {
    suspend fun setUi(html: String, title: String?, summary: String?): String
    suspend fun getUi(): String
    suspend fun evalJs(js: String): String
    suspend fun resetCanvas(): String
    suspend fun manageSpaces(action: String, name: String?): String
    suspend fun inspectVisual(): String
}

object CanvasPortRegistry {
    @Volatile
    var port: CanvasPort? = null
}

private fun requireCanvas(): CanvasPort? = CanvasPortRegistry.port

internal object CanvasSetUiTool : AgentTool {
    override val capability = CANVAS_CAP
    override val verb = Verb.SettingsSet
    override val risk = Risk.Write
    override val def = ToolDef(
        "canvas_set_ui",
        "Render or update the live Gangou Blank Canvas workspace with complete self-contained HTML/CSS/JS (OS, AI harness, dashboard, studio, or custom app). Automatically saves an Undo snapshot.",
        mapOf(
            "html" to str("Complete self-contained HTML/CSS/JS document to render full-screen on the Gangou canvas."),
            "title" to str("Short workspace name (e.g. 'Mon OS', 'Agent Harness', 'Studio')."),
            "summary" to str("Brief description of what was built or changed."),
        ),
        listOf("html"),
    )

    override suspend fun execute(a: Args, ctx: ToolContext): ToolExecution {
        val port = requireCanvas() ?: return failCode("canvas_unavailable")
        val html = a.str("html", max = 250000).orEmpty()
        if (html.isBlank()) return failCode("missing_html")
        val res = runCatching {
            port.setUi(html, a.str("title", max = 120), a.str("summary", max = 300))
        }.getOrElse { return failCode("canvas_error") }
        return ok { put("result", res) }
    }
}

internal object CanvasGetUiTool : AgentTool {
    override val capability = CANVAS_CAP
    override val verb = Verb.ReadPage
    override val risk = Risk.Read
    override val def = ToolDef(
        "canvas_get_ui",
        "Inspect the current Gangou canvas workspace (title, whether blank, visible elements, and current HTML source) before modifying or evolving it.",
        emptyMap(),
        emptyList(),
    )

    override suspend fun execute(a: Args, ctx: ToolContext): ToolExecution {
        val port = requireCanvas() ?: return failCode("canvas_unavailable")
        val res = runCatching { port.getUi() }.getOrElse { return failCode("canvas_error") }
        return ok { put("state", res) }
    }
}
internal object CanvasInspectVisualTool : AgentTool {
    override val capability = CANVAS_CAP
    override val verb = Verb.ReadPage
    override val risk = Risk.Read
    override val def = ToolDef(
        "canvas_inspect_visual",
        "Inspect the live rendered layout of the Gangou canvas on the phone screen (viewport size, horizontal overflow, clipped/offscreen elements, contrast/visibility) to catch and fix any UI flaw.",
        emptyMap(),
        emptyList(),
    )

    override suspend fun execute(a: Args, ctx: ToolContext): ToolExecution {
        val port = requireCanvas() ?: return failCode("canvas_unavailable")
        val res = runCatching { port.inspectVisual() }.getOrElse { return failCode("canvas_error") }
        return ok { put("layout", res) }
    }
}

internal object CanvasEvalTool : AgentTool {
    override val capability = CANVAS_CAP
    override val verb = Verb.EvaluateJs
    override val risk = Risk.Write
    override val def = ToolDef(
        "canvas_eval",
        "Run JavaScript live inside the current Gangou canvas workspace to update live data, click controls, or read state.",
        mapOf("js" to str("JavaScript expression or IIFE to run inside the active canvas.")),
        listOf("js"),
    )

    override suspend fun execute(a: Args, ctx: ToolContext): ToolExecution {
        val port = requireCanvas() ?: return failCode("canvas_unavailable")
        val js = a.str("js", max = 20000).orEmpty()
        if (js.isBlank()) return failCode("missing_js")
        val res = runCatching { port.evalJs(js) }.getOrElse { return failCode("canvas_error") }
        return ok { put("result", res) }
    }
}

internal object CanvasResetTool : AgentTool {
    override val capability = CANVAS_CAP
    override val verb = Verb.SettingsSet
    override val risk = Risk.Write
    override val def = ToolDef(
        "canvas_reset",
        "Clear the current workspace and return Gangou to a pure Blank Canvas.",
        emptyMap(),
        emptyList(),
    )

    override suspend fun execute(a: Args, ctx: ToolContext): ToolExecution {
        val port = requireCanvas() ?: return failCode("canvas_unavailable")
        val res = runCatching { port.resetCanvas() }.getOrElse { return failCode("canvas_error") }
        return ok { put("result", res) }
    }
}

internal object CanvasSpacesTool : AgentTool {
    override val capability = CANVAS_CAP
    override val verb = Verb.Query
    override val risk = Risk.Write
    override val def = ToolDef(
        "canvas_spaces",
        "List, save, load, or delete named Gangou workspaces (e.g. 'OS', 'Harness', 'Studio').",
        mapOf(
            "action" to str("One of: 'list', 'save', 'load', 'delete'."),
            "name" to str("Workspace name (for save/load/delete)."),
        ),
        listOf("action"),
    )

    override suspend fun execute(a: Args, ctx: ToolContext): ToolExecution {
        val port = requireCanvas() ?: return failCode("canvas_unavailable")
        val action = a.str("action", max = 30)?.trim()?.lowercase().orEmpty()
        if (action.isBlank()) return failCode("missing_action")
        val res = runCatching { port.manageSpaces(action, a.str("name", max = 120)) }.getOrElse { return failCode("canvas_error") }
        return ok { put("result", res) }
    }
}

internal object CanvasGroup {
    val tools: List<AgentTool> = listOf(
        CanvasSetUiTool,
        CanvasInspectVisualTool,
        CanvasGetUiTool,
        CanvasEvalTool,
        CanvasResetTool,
        CanvasSpacesTool,
    )
}

internal class CanvasCapability : Capability {
    override val id = CANVAS_CAP
    override val folds = false
    override fun status(): CapabilityStatus =
        if (CanvasPortRegistry.port != null) CapabilityStatus.Available
        else CapabilityStatus.Unavailable("canvas unavailable")
    override fun tools(): List<AgentTool> = CanvasGroup.tools
    override fun sections(env: PromptEnv): List<String> = listOf(
        """
        ## Gangou — Toile Blanche Générative & Design System
        Tu es **Gangou**, un environnement graphique adaptatif sur Android. L'utilisateur démarre sur une toile vide et te demande de créer l'espace de travail de son choix (OS complet, Harness d'agent, studio, tableau de bord, etc.).

        ### Outils de toile disponibles
        - `canvas_set_ui(html, title, summary)` : dessine ou met à jour l'interface plein écran (sauvegarde Undo automatique).
        - `canvas_get_ui()` : lit l'interface actuelle avant de la faire évoluer.
        - `canvas_eval(js)` : exécute du JS en direct pour animer ou mettre à jour des données sans tout recharger.
        - `canvas_inspect_visual()` : vérifie le rendu réel sur l'écran du téléphone (débordement horizontal, éléments coupés, dimensions). Appelle-le après un design complexe pour t'assurer qu'aucun bouton n'est coupé.
        - `canvas_spaces(action, name)` / `canvas_reset()` : gérer plusieurs espaces nommés ou revenir à la toile vide.

        ### Exigences de Design Poussé (Obligatoire)
        Ne génère JAMAIS une page basique ou terne. Chaque interface doit ressembler à une application native ou un OS moderne soigné :
        - **Esthétique & Surfaces** : fond profond (`#050811` à `#0b1120`), cartes en verre dépoli subtil (`background: rgba(15, 23, 42, 0.78); backdrop-filter: blur(16px); border: 1px solid rgba(255, 255, 255, 0.09)`), ombres douces, accents lumineux cohérents (`#38bdf8`, `#818cf8`, `#10b981`).
        - **Typographie & Iconographie** : hiérarchie visuelle nette (titres gras `letter-spacing: -0.02em`, sous-titres doux `#94a3b8`, chiffres `font-variant-numeric: tabular-nums`), privilégie des icônes **SVG inline nettes** plutôt que des emojis bruts à répétition.
        - **Ergonomie Mobile & Micro-interactions** : `min-height: 100dvh; overflow-x: hidden;`, respect des zones sûres (`padding: env(safe-area-inset-top) 14px env(safe-area-inset-bottom)`), cibles tactiles `>= 44px`, transitions fluides (`transition: transform .15s ease, background .15s ease`) et états `:active { transform: scale(0.97); }`.
        - **Zéro données factices silencieuses** : chaque bouton affiché doit fonctionner réellement.

        ### Pont direct Toile ↔ Noyau (`window.GangouBridge` & `window.onGangouEvent`)
        - `GangouBridge.ask(prompt)` : envoie une demande à l'IA directement depuis un bouton de la toile.
        - `GangouBridge.runLinux(cmd, callbackName)` : exécute une commande shell dans le bac à sable Linux et appelle `window[callbackName]({exitCode, output, timedOut})` avec la sortie réelle (sans consommer de tokens IA !).
        - `GangouBridge.complete(prompt, callbackName, systemPrompt)` : interroge directement le modèle IA configuré (avec failover automatique) et appelle `window[callbackName]({ok, text, error})` sans modifier la toile.
- `GangouBridge.kvSet(key, value)` / `kvGet(key)` / `kvDelete(key)` / `kvList()` : mémoire clé-valeur JSON persistante sur le téléphone, conservée même si on change d'espace ou réinitialise la toile.
- `GangouBridge.undo()` / `redo()` / `reset()` / `openSettings()` / `saveSpace(nom)` / `loadSpace(nom)` / `listSpaces()`.
        - `window.onGangouEvent = (evt) => { ... }` : reçoit en direct `{type: 'status', busy: true|false}` dès que l'IA commence ou finit une tâche.
        """.trimIndent()
    )
}
