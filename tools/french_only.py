#!/usr/bin/env python3
import os, shutil, re

for d in ["app/src/main/res/values-zh", "app/src/main/res/values-b+zh+Hant"]:
    if os.path.isdir(d):
        shutil.rmtree(d)

REPLACEMENTS = [
    # StopRules.kt - Erreurs modèle et garde-fous
    ("模型的回复还没传完,连接就在上游被断开了,重试后仍然如此。常见原因是网络不稳,或中转服务、网关的空闲超时太短(模型长时间思考时容易碰上)。请稍后再试。", "La connexion a été interrompue avant la fin de la réponse du modèle. Réseau instable ou délai trop court côté serveur. Veuillez réessayer plus tard."),
    ("连不上网络代理,或代理没有转发这个请求,重试后仍然不通。请检查代理是否开着、设置 > 网络代理 里的地址和端口是否正确;不需要代理时把它清空。", "Impossible de se connecter au proxy réseau. Vérifiez l'adresse et le port dans Paramètres > Proxy réseau, ou videz ces champs si vous n'utilisez pas de proxy."),
    ("ChatGPT 额度用完了,稍后再试。用量可以在 ChatGPT 设置 > 用量(chatgpt.com/settings/usage)查看;Plus 每 5 小时的额度和其他用 ChatGPT 登录的 App 共用。也可以先换一个模型。", "Quota ChatGPT épuisé, réessayez plus tard (consultez chatgpt.com/settings/usage) ou changez de modèle dans Paramètres > Modèle."),
    ("模型服务拒绝了请求:API Key 无效或没有权限。请在设置里检查 API Key 是否填写正确、是否有权使用该模型。", "Le service a refusé la requête : clé API invalide ou sans autorisation. Vérifiez votre clé API dans les paramètres."),
    ("控件树读不到操作位置,连续操作后画面仍无可见变化,无法确认操作是否生效,已停止重试", "Impossible de confirmer l'effet de l'opération à l'écran après plusieurs essais ; nouvelles tentatives arrêtées"),
    ("去设置 > 模型,检查模型名(要填完整的模型 ID,比如 Gemini 的模型名类似 gemini-flash-latest)或改用对应的预设。", "Vérifiez le nom complet du modèle dans Paramètres > Modèle ou utilisez un préréglage."),
    ("模型服务限流,重试后仍然请求太频繁。请稍后再试,或检查账户的调用频率限制。", "Limite de requêtes atteinte sur le service du modèle : trop de demandes. Patientez un instant ou vérifiez les limites de votre compte."),
    ("网络代理拒绝了账号或密码(HTTP 407)。请在设置 > 网络代理 里检查代理地址中的用户名和密码。", "Le proxy réseau a refusé les identifiants (HTTP 407). Vérifiez Paramètres > Proxy réseau."),
    ("模型账户额度已用完,请检查服务商账户的余额或套餐。", "Le quota du compte du modèle est épuisé. Vérifiez le solde ou le forfait de votre fournisseur."),
    ("模型账户额度已用完，请检查服务商账户的余额或套餐。", "Le quota du compte du modèle est épuisé. Vérifiez le solde ou le forfait de votre fournisseur."),
    ("模型的工具调用没有被服务端识别,原样作为文字返回了,没能执行。", "L'appel d'outil du modèle n'a pas été reconnu par le serveur et n'a pas pu être exécuté."),
    ("对话内容超出了模型的上下文长度,请换用上下文更长的模型,或把任务拆小一些。", "La conversation dépasse la taille de contexte du modèle. Choisissez un modèle avec un plus grand contexte ou réduisez la tâche."),
    ("请到设置 > 模型检查模型名和 Base URL 是否正确。", "Vérifiez le nom du modèle et la Base URL dans Paramètres > Modèle."),
    ("连接模型服务失败,重试后仍然不通。请检查网络和 Base URL。", "Échec de connexion au service du modèle. Vérifiez votre réseau et la Base URL."),
    ("模型拒绝处理该内容,请调整任务描述或换一个模型。", "Le modèle a refusé de traiter ce contenu. Ajustez la demande ou changez de modèle."),
    ("模型不接受所选的思考强度。服务端回复:", "Le modèle n'accepte pas le niveau de raisonnement choisi. Réponse serveur : "),
    ("请到设置 > 模型,把思考强度改为默认或服务端支持的档位。", "Réglez le niveau de raisonnement sur Par défaut dans Paramètres > Modèle."),
    ("这个账号不能在其他 App 里用 ChatGPT 额度(需要 Plus 或 Pro,并且所在地区和账号设置允许)。", "Ce compte ne peut pas utiliser le quota ChatGPT dans cette app (Plus ou Pro requis). "),
    ("这个账号没开通或没权限用。去设置 > 模型换一个模型,或到服务商后台开通。", " n'est pas activé pour ce compte. Changez de modèle dans Paramètres > Modèle."),
    ("不支持调用工具,Mika 干活要靠工具。去设置 > 模型换一个会调用工具的模型。", " ne gère pas l'appel d'outils requis par Gangou. Choisissez un autre modèle dans Paramètres > Modèle."),
    ("在这个服务商那里不存在。", " n'existe pas chez ce fournisseur. "),
    ("服务端回复:", "Réponse serveur : "),
    ("服务端说明:", "Détail serveur : "),
    ("可能是模型名不对,或者服务那边暂时不可用。", "Nom de modèle incorrect ou service temporairement indisponible. "),
    ("可以换一个模型。", "Vous pouvez changer de modèle."),
    ("请检查 Base URL 和接口类型是否匹配。", "Vérifiez que la Base URL et le type d'API correspondent."),
    ("任务进度已经完成,但模型没有给出结果。", "La tâche est terminée, mais le modèle n'a renvoyé aucun résultat."),
    ("模型停止了操作,没有给出回答。", "Le modèle s'est arrêté sans donner de réponse."),
    ("页面没有变化,反复读也读不出要找的内容", "La page n'a pas changé et le contenu recherché reste introuvable"),
    ("同一个操作反复执行仍没有效果", "La même action répétée reste sans effet"),
    ("操作多次后画面一直没有变化", "Aucun changement visible à l'écran après plusieurs actions"),
    ("同一条命令原样重复仍然失败", "La même commande échoue à répétition"),
    ("操作连续失败", "Échecs consécutifs de l'opération"),
    ("触发了操作保护", "Protection de sécurité déclenchée"),
    ("步上限,任务没有完成。", " étapes atteinte, tâche incomplète."),
    ("已达到 ", "Limite de "),
    ("次没有读到新数据,任务已停止。", " lectures sans nouvelle donnée, tâche arrêtée."),
    ("连续 ", "Après "),
    ("模型调用异常:", "Erreur d'appel du modèle : "),
    ("已安全停止(", "Arrêt de sécurité ("),
    ("模型服务出错(HTTP ", "Erreur du service de modèle (HTTP "),
    ("),请稍后再试。", "), veuillez réessayer plus tard."),
    (" 登录已失效,请在设置 > 模型里重新用 ", " : session expirée, reconnectez-vous avec "),
    (" 登录。", " dans Paramètres > Modèle."),
    (" 订阅档位不能用这个接口(HTTP 403)。", " : votre abonnement n'autorise pas cette API (HTTP 403). "),
    ("可以改用 ", "Utilisez une clé API "),
    (" 的 API Key,或在设置 > 模型里换一个模型。", " ou changez de modèle dans Paramètres > Modèle."),
    ("调用模型「", "Modèle « "),
    ("」时", " » : "),
    ("模型「", "Modèle « "),
    ("找不到模型「", "Modèle introuvable « "),
    ("找不到该模型,", "Modèle introuvable. "),
    ("找不到该模型", "Modèle introuvable"),
    ("当前模型", "modèle actuel"),
    # ModelError.kt
    ("API Key 无效或没有权限", "Clé API invalide ou sans autorisation"),
    ("额度已用完", "Quota épuisé"),
    ("请求太频繁,请稍后再试", "Requêtes trop fréquentes, réessayez plus tard"),
    ("对话内容超出模型上下文长度", "Conversation trop longue pour le contexte du modèle"),
    ("这个账号用不了该模型(没开通或没权限)", "Modèle non autorisé pour ce compte"),
    ("这个账号用不了模型「", "Modèle non autorisé « "),
    ("」(没开通或没权限)", " »"),
    ("该模型不支持调用工具", "Ce modèle ne gère pas les outils"),
    ("」不支持调用工具", " » ne gère pas les outils"),
    ("网络连接失败", "Échec de la connexion réseau"),
    ("连不上网络代理", "Impossible de joindre le proxy réseau"),
    ("网络代理拒绝了账号或密码", "Proxy réseau : identifiants refusés"),
    ("回复还没传完,连接就被断开了", "Connexion coupée avant la fin de la réponse"),
    ("模型拒绝处理该内容", "Contenu refusé par le modèle"),
    ("模型不接受所选的思考强度", "Niveau de raisonnement refusé par le modèle"),
    ("ChatGPT 额度用完了", "Quota ChatGPT épuisé"),
    ("这个账号不能在其他 App 里用 ChatGPT 额度", "Quota ChatGPT non autorisé sur cette app"),
    ("登录已失效,请重新登录", "Session expirée, veuillez vous reconnecter"),
    ("订阅档位不能用这个接口", "Abonnement non autorisé pour cette API"),
    ("模型返回了无法解析的内容", "Réponse du modèle illisible"),
    # Fournisseurs et UI
    ("关于 Mika", "À propos de Gangou"),
    ("智谱 Coding Plan", "Zhipu Coding Plan"),
    ("通义千问", "Qwen (Alibaba)"),
    ("硅基流动", "SiliconFlow"),
    ("火山方舟", "Volcengine Ark"),
    ("阶跃星辰", "StepFun"),
    ("腾讯混元", "Tencent Hunyuan"),
    ("百度千帆", "Baidu Qianfan"),
    ("智谱", "Zhipu GLM"),
    ("简体中文", "Chinois"),
    ("繁體中文", "Chinois trad."),
    ("模型账户余额不足", "Solde du compte insuffisant"),
    ("模型请求频率超限", "Limite de requêtes dépassée"),
    ("模型请求超时", "Délai d'attente dépassé"),
    ("模型服务暂时不可用", "Service temporairement indisponible"),
    ("模型返回为空", "Réponse vide du modèle"),
    ("请求失败", "Échec de la requête"),
    ("任务已停止", "Tâche arrêtée"),
    ("任务已完成", "Tâche terminée"),
    ("任务未完成", "Tâche non terminée"),
    ("正在思考", "Réflexion en cours"),
    ("正在执行", "Exécution en cours"),
    ("等待确认", "En attente de confirmation"),
    ("已取消", "Annulé"),
    ("未知错误", "Erreur inconnue"),
    ("请稍候", "veuillez patienter"),
    ("正在验证", "vérification en cours"),
]    

for base in ["app/src/main/java", "core/src/main/kotlin", "app/src/main/res"]:
    for r, _, files in os.walk(base):
        for fn in files:
            if not (fn.endswith(".kt") or fn.endswith(".xml")):
                continue
            path = os.path.join(r, fn)
            txt = open(path, "r", encoding="utf-8").read()
            new_txt = txt
            for zh, fr in REPLACEMENTS:
                if zh in new_txt:
                    new_txt = new_txt.replace(zh, fr)
            if new_txt != txt:
                open(path, "w", encoding="utf-8").write(new_txt)
def replace_mika_and_zh():
    def sub_mika(m):
        w = m.group(0)
        return "GANGOU" if w.isupper() else "Gangou"

    def clean_text(txt, do_rep, do_zh):
        nt = txt
        if do_rep:
            for zh, fr in REPLACEMENTS:
                nt = nt.replace(zh, fr)
        if do_zh:
            nt = re.sub(r"[\u4e00-\u9fff\u3000-\u303f\uff00-\uffef]+", "", nt)
        nt = re.sub(r"(?<!\.)\b(Mika|MIKA)\b(?![.\-/])", sub_mika, nt)
        return nt

    configs = [
        ("app/src/main/res", (".xml",), ("values-zh",), False, False),
        ("core/src/main/resources/prompts", (".md", ".txt", ".json"), (), True, True),
        ("core/src/main/resources/skills", (".md", ".txt", ".py", ".json"), ("configure-mika",), True, True),
    ]
    for base, exts, ex, do_rep, do_zh in configs:
        if not os.path.isdir(base):
            continue
        for r, _, files in os.walk(base):
            if any(x in r for x in ex):
                continue
            for fn in files:
                if not fn.endswith(exts):
                    continue
                p = os.path.join(r, fn)
                t = open(p, "r", encoding="utf-8").read()
                nt = clean_text(t, do_rep, do_zh)
                if nt != t:
                    open(p, "w", encoding="utf-8").write(nt)

replace_mika_and_zh()

id_path = "core/src/main/resources/prompts/system/identity.md"
if os.path.isfile(id_path):
    ident = open(id_path, "r", encoding="utf-8").read()
    rule = "\n- Langue obligatoire : répondez TOUJOURS en français dans tous vos messages, résumés et erreurs. N'utilisez jamais le chinois.\n"
    if "Langue obligatoire" not in ident:
        open(id_path, "w", encoding="utf-8").write(ident.rstrip() + rule)

print("Complete French-only replacement done.")
