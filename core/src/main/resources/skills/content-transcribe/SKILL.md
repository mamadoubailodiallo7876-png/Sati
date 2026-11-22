---
name: content-transcribe
display: Mettre du contenu en texte
description: "Mettre des vidéos, podcasts et articles en texte : résumé, chapitrage, citations ou réécriture en article"
aliases: ["résumer une vidéo", "organiser un podcast", "résumé d'article", "extraire les sous-titres"]
apps: ["bilibili", "xiaohongshu"]
sites: ["youtube.com", "xiaoyuzhoufm.com"]
terms: ["résumé", "synthèse", "transcription", "sous-titres", "podcast", "chapitres", "points clés", "citations", "script"]
---
## Point d'entrée

Demandez d'abord le format souhaité : transcription intégrale, résumé, chapitrage, citations ou réécriture en article. Si l'utilisateur ne précise rien, rédigez un résumé de cinq à dix phrases.

Récupérez le texte là où se trouve le contenu : pour un article, lisez le corps de la page web ; pour une vidéo, récupérez les sous-titres ou la transcription dans la description ; pour un podcast, vérifiez si la page de l'épisode propose une transcription ou des notes d'épisode. Cet appareil ne dispose pas de reconnaissance vocale : s'il n'y a que de l'audio, demandez à l'utilisateur de fournir une transcription ou un fichier de sous-titres.

## Étapes

1. **Récupérer le texte intégral** : utilisez `web_read_page` pour lire le corps de la page ; les fichiers de sous-titres ou de transcription envoyés par l'utilisateur se trouvent dans `/workspace/inbox/`, lisez-les avec `read_file`.
2. **Lire par segments** : si le contenu est long, lisez-le en plusieurs parties avec un léger chevauchement entre elles, puis supprimez les doublons et vérifiez qu'aucun passage ne manque du début à la fin.
3. **Rédiger selon le format demandé** : consultez `references/transcript-output-formats.md` avant de rédiger pour voir les consignes et exemples de chaque format. La structure d'un résumé est la suivante : une ou deux phrases de présentation générale ; trois à huit thèmes par ordre d'apparition, chacun accompagné de son horodatage approximatif de début et de deux ou trois phrases de contenu ; puis une liste des idées marquantes, chiffres et livres ou œuvres mentionnés.
4. **Rédiger dans la langue du contenu original**, sauf demande contraire de l'utilisateur.
5. **Relire** : une fois la rédaction terminée, comparez avec la source pour vérifier l'ordre, l'absence de thèmes oubliés, et que les horodatages et citations figurent bien dans le texte d'origine.

## Points de vigilance

- Les sous-titres ne contiennent que le texte parlé : les graphiques, démonstrations et textes affichés à l'écran ne sont pas accessibles, n'écrivez donc pas comme si vous aviez vu l'image.
- Les sous-titres générés automatiquement comportent souvent des fautes ou des coupures erronées : vérifiez les noms propres, noms de personnes et chiffres à l'aide du contexte ou de la description.
- Les horodatages sont estimés d'après la position des sous-titres : indiquez par exemple « vers la 23e minute », sans précision à la seconde près.
- Signalez les passages inaudibles ou sans sous-titres au lieu de les inventer.

## Critères de réussite (vérification)

Le résultat couvre tous les segments du contenu original ; chaque citation et chaque horodatage peut être retrouvé dans les sous-titres ou le texte récupéré.

## Quand s'arrêter et demander à l'utilisateur

Si le contenu nécessite une connexion ou un paiement, ou s'il n'y a que de l'audio sans aucune transcription, indiquez à l'utilisateur ce qui manque et demandez-lui de le fournir.
