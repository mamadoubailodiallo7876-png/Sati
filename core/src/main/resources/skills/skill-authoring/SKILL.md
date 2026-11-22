---
name: skill-authoring
display: Rédiger une compétence
description: À utiliser lorsque l'utilisateur souhaite créer, importer ou modifier une compétence (une méthode récurrente pour un type de tâche) : comment l'écrire, vérifier les doublons et l'enregistrer
aliases: ["créer une compétence", "nouvelle compétence", "écrire une compétence", "enregistrer une compétence", "importer une compétence", "modifier une compétence", "skill"]
terms: ["compétence", "méthode", "processus", "étapes", "modèle", "faire comme ça à l'avenir", "enregistrer en compétence", "importer", "réécrire"]
---
## Quand écrire une compétence

- Écrivez une compétence uniquement lorsque l'utilisateur demande explicitement de « créer une compétence », « enregistrer cette méthode », « importer ceci comme compétence » ou « modifier une compétence ». S'il discute simplement d'une méthode ou demande si quelque chose est faisable, répondez d'abord sans rien enregistrer de votre propre chef.
- Une compétence décrit comment réaliser un type de tâche à l'avenir, pas un fait ponctuel ni une préférence personnelle. « Je ne mange pas de coriandre » relève de la mémoire, pas d'une compétence.
- Seul `skill_save` permet d'enregistrer une compétence. Si vous n'avez pas cet outil sous la main, dites honnêtement à l'utilisateur que l'enregistrement est impossible pour le moment, sans prétendre que c'est fait.

## Vérifier d'abord ce qui existe

1. Utilisez `skill_search` une fois avec la formulation décrivant ce type de tâche.
2. Si une compétence personnelle (`personal`) correspond déjà à ce type de tâche : lisez-la intégralement avec `skill_read`, modifiez-la sur cette base, puis enregistrez-la en conservant son `name` et la `version` lue. Ne créez pas une deuxième compétence pour la même tâche.
3. Si seule une compétence intégrée correspond : les compétences intégrées ne sont pas modifiables. Si l'utilisateur souhaite une autre méthode, créez votre propre compétence sous un autre nom et indiquez à l'utilisateur laquelle sera utilisée à l'avenir.

## Clarifier avant d'écrire

Si l'utilisateur ne donne qu'une phrase vague (« crée-moi une compétence pour commander un café »), utilisez d'abord `ask_user` pour clarifier un ou deux points déterminants : dans quelle application ou sur quel site, jusqu'à quelle étape aller, et s'il y a des exigences fixes. Présentez les points clés en quelques lignes pour validation avant d'enregistrer. Si l'utilisateur a déjà décrit la méthode clairement ou collé le contenu complet, inutile de redemander.

## Comment rédiger

Le document forme un tout : un en-tête suivi de cinq sous-sections, avec exactement ces titres, pour un total de 4 000 caractères maximum.

```
---
name: order-coffee
display: Commander un café
description: À utiliser lorsque l'utilisateur veut commander un café en livraison ou à emporter
aliases: ["commander un café", "acheter un café"]
---
## Point d'entrée
## Étapes
## Erreurs fréquentes
## Critères de réussite
## Quand passer la main à l'utilisateur
```

- `name` utilise des lettres minuscules, des chiffres et des tirets pour indiquer clairement le type de tâche ; `display` est un nom court en français.
- `description` résume en une phrase « quand l'utiliser », et `aliases` regroupe les formulations probables de l'utilisateur : la réutilisation future dépend de ces deux champs.
- `Point d'entrée` indique par où commencer (quelle application, quel site web ou quelle recherche) ; `Étapes` utilise des phrases courtes numérotées, une action par étape, en nommant les boutons et les pages sans jamais écrire de coordonnées.
- Les éléments variables d'une fois sur l'autre (nom de personne, montant, adresse, texte à envoyer) s'écrivent `<paramètre>` ou « le contact indiqué par l'utilisateur », jamais en dur.
- `Erreurs fréquentes` signale les pièges et les moyens de les contourner ; `Critères de réussite` précise où vérifier le résultat une fois terminé ; `Quand passer la main à l'utilisateur` indique les moments nécessitant une connexion, un paiement, une confirmation ou un choix de l'utilisateur.
- Les accents graves ne servent qu'à encadrer les noms d'outils ; n'en mettez pas autour des noms d'applications ou des libellés de boutons. N'écrivez jamais de mots de passe, codes de vérification, numéros de carte ni informations personnelles d'autrui.
- Mieux vaut court que long : inutile d'écrire ce que le modèle sait déjà faire, concentrez-vous uniquement sur les méthodes et les pièges propres à ce type de tâche.

## Importer un contenu collé par l'utilisateur

Lorsque l'utilisateur colle une compétence, une note ou une procédure rédigée ailleurs, reformatez-la selon la structure ci-dessus : conservez la méthode elle-même, supprimez ce qui est sans rapport ici (commandes d'autres logiciels, outils ou scripts inutilisables sur téléphone) et complétez d'une phrase ou deux les sous-sections manquantes selon l'intention d'origine. N'importez jamais les identifiants ou mots de passe éventuellement présents dans le texte source.

## Enregistrer et informer l'utilisateur

1. Enregistrez avec `skill_save` : `markdown` contient l'intégralité du document, et `quote` reprend textuellement la demande actuelle de création ou de modification de l'utilisateur.
2. L'enregistrement n'est réussi que si le résultat est `ok`. Informez l'utilisateur d'après ce résultat : le nom de la compétence, s'il s'agit d'une création ou d'une mise à jour, qu'elle est visible dans « Paramètres › Mémoire et compétences », et qu'elle sera essayée en priorité la prochaine fois pour ce type de tâche.
3. En cas d'échec, corrigez selon la raison indiquée et réessayez une fois ; si cela échoue encore, expliquez honnêtement que l'enregistrement n'a pas abouti et pourquoi, sans prétendre qu'il a réussi.
4. Le renommage et la suppression ne se font pas ici : invitez l'utilisateur à supprimer l'ancienne compétence dans « Paramètres › Mémoire et compétences » ; pour changer de nom, enregistrez d'abord une nouvelle copie sous le nouveau nom, puis demandez à l'utilisateur de supprimer l'ancienne.
