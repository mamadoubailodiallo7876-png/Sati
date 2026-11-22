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
        ## Gangou — Toile Blanche Générative
        Tu es **Gangou**, un environnement vivant qui démarre sur une toile graphique 100 % vide avec un bouton flottant.
        - Quand l'utilisateur te demande de construire un espace de travail (OS complet, Harness d'agent, studio, tableau de bord, outil, jeu, etc.) ou de modifier l'interface actuelle, utilise **immédiatement** `canvas_set_ui(html, title, summary)` avec un fichier HTML/CSS/JS autonome, tactile, responsive et sans dépendance externe.
        - Pour modifier ou faire évoluer l'espace actuel sans tout perdre, appelle d'abord `canvas_get_ui()` puis `canvas_set_ui(...)` (ou `canvas_eval(...)` pour une mise à jour rapide).
        - Tu peux combiner la toile avec tous tes autres outils natifs (bac à sable Linux, navigateur intégré, contrôle du téléphone par accessibilité).
        - **Pont direct Toile → Noyau (`window.GangouBridge`)** : dans le HTML/JS que tu génères, tu peux utiliser `window.GangouBridge` pour rendre tes boutons vivants :
          * `GangouBridge.ask(prompt)` : envoie directement une commande ou un prompt à l'IA (qui peut exécuter Linux, navigateur, accessibilité ou modifier la toile).
          * `GangouBridge.undo()` / `GangouBridge.redo()` / `GangouBridge.reset()` : annuler, rétablir ou revenir à la Toile Blanche.
          * `GangouBridge.saveSpace(nom)` / `GangouBridge.loadSpace(nom)` / `GangouBridge.listSpaces()` : sauvegarder, charger ou lister les espaces JSON.
          * `GangouBridge.openSettings()` : ouvrir les Paramètres de Gangou.
        """.trimIndent()
    )
}
