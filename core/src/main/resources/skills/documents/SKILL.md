---
name: documents
display: Traiter des documents PDF
description: Lire le texte d'un PDF, extraire par page, consulter les métadonnées, diviser et fusionner puis remettre le fichier à l'utilisateur
aliases: ["lire un PDF", "PDF en texte", "fusionner des PDF"]
terms: ["PDF", "document", "extraire le texte", "par page", "fusionner", "diviser", "numérisation"]
---
## Point d'entrée

Vérifiez d'abord où se trouve le document :
- Lien public en ligne : ouvrez-le avec `web_navigate` ; si le navigateur intégré peut l'afficher directement, il suffit de lire la page.
- Pièce jointe envoyée par l'utilisateur : dans `/workspace/inbox/`.
- Fichier téléchargé depuis une page web : dans `/workspace/downloads/`, vous pouvez consulter l'historique des téléchargements avec `web_downloads`.

## Étapes

1. Pour les PDF textuels, exécutez `scripts/extract_pdf.py` via `run_bundled_script`, en indiquant `skills/documents` pour `source`, avec un chemin de fichier relatif à `/workspace` :
   - Texte intégral : `args: ["inbox/a.pdf"]`
   - Pages précises (numérotées à partir de 1, ex. `3`, `1-5`, `1,4,7-9`) : `args: ["inbox/a.pdf", "--pages", "1-5"]`
   - Titre, auteur, nombre de pages, etc. (JSON) : `args: ["inbox/a.pdf", "--metadata"]`
   - Enregistrer d'abord les documents longs en texte : `args: ["inbox/a.pdf", "--out", "a.txt"]`, puis lire par segments avec `read_file` ou chercher des mots-clés avec `search_files`.
   - Avec mot de passe : ajoutez `--password` suivi du mot de passe.
2. Chaque page de la sortie commence par `--- page N ---` ; indiquez ce numéro de page lors des citations.
3. Pour diviser, fusionner ou extraire des pages, utilisez `run_command` pour exécuter un court script Python appelant `pypdf` ; placez les nouveaux fichiers sous `/workspace`.
4. Remettez le fichier final à l'utilisateur avec `share_file`.

## Points de vigilance

- Si le script renvoie le code de sortie 3 et indique qu'aucun texte n'est extractible, il s'agit d'un PDF numérisé (image) ; cet appareil ne dispose pas de reconnaissance de texte (OCR), vous pouvez seulement consulter les images des pages ou en informer l'utilisateur. Si seules certaines pages sont sans texte, le script les signalera séparément.
- L'ordre d'extraction des tableaux et des mises en page à plusieurs colonnes est souvent perturbé : vérifiez toujours les chiffres d'un tableau par rapport à la page d'origine avant de les citer.
- Si la plage de pages dépasse le nombre de pages du document, le script échoue immédiatement : vérifiez d'abord le nombre de pages avec `--metadata`.

## Critères de réussite (vérification)

Le texte fourni correspond bien aux pages d'origine dans la sortie du script avec les numéros de page indiqués ; le fichier remis via `share_file` a bien été envoyé et son nombre de pages ainsi que son contenu correspondent à ce qui était attendu (vous pouvez relancer `--metadata` sur le nouveau fichier pour vérifier le nombre de pages).

## Quand s'arrêter et demander à l'utilisateur

Si le document est protégé par un mot de passe que l'utilisateur n'a pas fourni, ou s'il faut que l'utilisateur décide quelles pages conserver ou dans quel ordre les fusionner, demandez-lui d'abord.
