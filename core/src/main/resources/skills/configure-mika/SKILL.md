---
name: configure-mika
display: Modifier les paramètres de Gangou
description: Voir et modifier les réglages de Gangou.
---

# Paramètres de Gangou

## Voir ce qui est modifiable
Appelle d'abord `settings_list()` pour connaître la liste exacte des paramètres disponibles, leurs valeurs actuelles et les choix autorisés. N'invente jamais de nom de paramètre.

## Avant de modifier
Explique brièvement à l'utilisateur ce que tu vas changer avant d'appliquer un réglage important, puis utilise `settings_set(key, value)` avec une valeur autorisée par `settings_list()`.

## Ce qui ne peut pas être modifié ici
La gestion des modèles, des clés API et la suppression complète des données utilisateur doivent être effectuées directement par l'utilisateur dans l'écran Paramètres de Gangou.
