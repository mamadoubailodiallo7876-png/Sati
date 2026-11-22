---
name: info-research
display: Tirer une question au clair
description: "Tirer une question au clair en ligne : explorer les pistes, lire les sources d'origine, recouper plusieurs sources, indiquer la source et la date"
aliases: ["rechercher", "vérifier", "m'aider à comprendre"]
apps: ["xiaohongshu", "zhihu", "bilibili", "weibo"]
sites: ["zhihu.com", "tieba.baidu.com", "weibo.com", "douban.com"]
terms: ["recherche", "vérifier", "comprendre", "contexte", "comparaison", "fiable", "source", "récent"]
---
## Point d'entrée

Définissez d'abord précisément l'objet de la recherche : nom complet, région, période, version ou modèle. En cas d'homonymie ou d'ambiguïté, posez la question une seule fois à l'utilisateur, tout en commençant à chercher les éléments non concernés par cette ambiguïté en attendant sa réponse. Pour les critères non précisés par l'utilisateur (comme la période), choisissez un jeu de critères courants et précisez en début de conclusion lequel vous avez retenu.

Pour démarrer, ouvrez un moteur de recherche (Baidu, Bing) avec `web_navigate`, ou utilisez la recherche interne dans l'application concernée.

## Étapes

1. **Explorer les pistes** : effectuez d'abord deux ou trois recherches assez larges pour voir quelles versions existent, quels sites en parlent et s'il existe une source officielle faisant autorité. Pour une recherche sur un site, utilisez deux à quatre mots-clés plutôt qu'une phrase entière.
2. **Trouver la source d'origine** : pour le fait sur lequel porte directement la question de l'utilisateur, remontez jusqu'au lieu de publication initial : le site de l'émetteur, une annonce officielle, un document original, le texte d'un article scientifique, ou la page même du produit ou service. Les articles de presse et reprises ne servent que de pistes : suivez les liens ou noms qu'ils citent pour retrouver le texte original.
3. **Compléter avec les retours d'expérience et les avis négatifs** : consultez les plateformes communautaires et vidéo (Xiaohongshu, Zhihu, Tieba, Weibo, Bilibili, Douban) pour les retours d'usage réels, et les plateformes de réclamations pour les problèmes. Effectuez une recherche dédiée avec des termes critiques (« problème », « réclamation », « défaut »).
4. **Recouper les informations** : vérifiez les conclusions clés dans le texte original d'au moins deux sources indépendantes. Lorsque deux versions divergent, examinez la date de publication, voyez laquelle est la plus proche des faits, et exposez clairement la divergence elle-même.
5. **Rédiger la conclusion** : indiquez après chaque conclusion sa source (site ou organisme émetteur) et la date de l'information ; distinguez clairement ce qui figure dans le texte original de ce que vous déduisez ; pour ce que vous avez cherché sans succès, précisez quels endroits ont été consultés et ce qui manque encore.

## Points de vigilance

- L'extrait affiché dans les résultats de recherche n'est pas le texte intégral et est souvent tronqué ou obsolète ; ouvrez la page avec `web_read_page` pour lire le corps du texte et vérifiez bien sur quel site vous arrivez au final.
- Un même communiqué repris par de nombreux sites donne l'impression de sources multiples alors qu'il n'y a en réalité qu'une seule origine.
- Les articles sponsorisés, tests avec liens d'achat et rumeurs anonymes ont une faible fiabilité : signalez-le si vous les utilisez.
- La date affichée sur une page peut être celle d'une republication ou d'une mise à jour, et non nécessairement celle de l'événement lui-même.
- Une recherche infructueuse, une page inaccessible ou bloquée par une fenêtre de connexion signifie simplement que vous n'avez pas pu lire l'information cette fois-ci, et non que « cela n'existe pas ».

## Critères de réussite (vérification)

Chaque conclusion peut être reliée à une page effectivement lue lors de cette session, avec indication de la source et de la date ; les conclusions clés s'appuient sur deux sources indépendantes ; les points faibles ou contradictoires sont expliqués.

## Quand s'arrêter et demander à l'utilisateur

Si le contenu à consulter nécessite une connexion, ou si l'étape suivante implique une action personnelle ou financière de l'utilisateur (réservation, achat, inscription), transmettez-lui les résultats trouvés.
