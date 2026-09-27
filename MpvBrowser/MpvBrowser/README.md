# MPV Browser — pour Nvidia Shield

Navigateur web minimal en Kotlin. Toute vidéo détectée (lien direct .mp4/.mkv/.m3u8/etc.,
téléchargement vidéo, ou appui long sur un lien) est envoyée vers **MPV**
(mpv-android, package `is.xyz.mpv`) au lieu d'être lue dans le navigateur.
Un bouton "MPV" en haut permet aussi d'envoyer la page actuellement affichée
directement vers MPV.

## 1. Compiler l'APK

Je n'ai pas d'environnement Android SDK ici, donc ce dossier est un **projet
Android Studio complet et prêt à compiler**, pas un .apk directement. Étapes :

1. Installe [Android Studio](https://developer.android.com/studio) (gratuit).
2. `File > Open`, sélectionne ce dossier `MpvBrowser`.
3. Laisse Gradle synchroniser (première fois : télécharge les dépendances,
   ça prend quelques minutes).
4. `Build > Build Bundle(s) / APK(s) > Build APK(s)`.
5. L'APK apparaît dans `app/build/outputs/apk/debug/app-debug.apk`.

Aucune signature particulière n'est nécessaire pour un usage personnel
(APK "debug").

## 2. Installer MPV sur la Shield

Avant d'utiliser cette app, installe **mpv-android** sur ta Shield :
- Télécharge l'APK depuis [le dépôt officiel GitHub](https://github.com/mpv-android/mpv-android/releases)
  ou depuis F-Droid.
- Transfère-le sur la Shield (via `adb install`, ou une app comme
  "Downloader" / "Send files to TV" / clé USB).

Le package attendu est `is.xyz.mpv`. Si tu utilises un fork différent de
MPV avec un autre nom de package, change la valeur `mpvPackage` dans
`MainActivity.kt`.

## 3. Installer l'APK du navigateur sur la Shield

Avec adb (recommandé) :
```
adb connect <IP_DE_TA_SHIELD>:5555
adb install app-debug.apk
```

Ou transfère le fichier `.apk` sur la Shield (clé USB, Downloader, etc.) et
ouvre-le avec un gestionnaire de fichiers pour l'installer — il faut activer
"Sources inconnues" dans les paramètres de sécurité de la Shield.

## 4. Visibilité sur la page d'accueil / épingler en façade

L'app inclut une bannière et la catégorie "Leanback launcher" : une fois
installée, elle apparaît comme une vraie tuile TV (avec sa bannière) dans
la rangée **"Apps"** de la page d'accueil de la Shield, pas seulement dans
le tiroir d'applications.

Pour l'épingler dans une rangée de façade (bibliothèque / accueil) :
1. Va sur l'accueil de la Shield, repère la tuile "MPV Browser" dans la
   ligne "Apps".
2. Sélectionne-la avec la télécommande et maintiens le bouton central
   (OK/sélection) enfoncé quelques secondes, ou ouvre le menu contextuel
   (selon la version du launcher, une icône "..." apparaît).
3. Choisis **"Épingler à l'accueil"** / **"Ajouter à la ligne d'accueil"**
   (le libellé exact dépend de la version du launcher Shield/Android TV).

Si tu utilises un launcher personnalisé (type un launcher de bibliothèque
multimédia tiers), il détectera aussi l'app grâce à sa bannière et à la
catégorie leanback, et proposera généralement une option similaire pour
l'épingler.

## 5. Utilisation

- Barre d'adresse en haut : tape une URL ou une recherche.
- Bouton **MPV** : envoie la page actuellement affichée vers MPV.
- Appui long sur un lien vidéo dans une page : propose "Ouvrir avec MPV".
- Les liens pointant directement vers un fichier vidéo (.mp4, .mkv, .m3u8…)
  s'ouvrent automatiquement dans MPV.

⚠️ **Confort d'usage sur Shield** : ceci est une app "normale" (pas conçue
pour la télécommande seule). Naviguer sur le web avec juste la télécommande
Shield est possible (mode pointeur en maintenant le bouton central) mais
assez lent. Une souris/clavier Bluetooth rend l'expérience bien plus
confortable.

## Limites à connaître

- Cette app ne contourne aucune protection DRM (Netflix, Disney+, etc. ne
  fonctionneront pas mieux qu'avec un navigateur normal).
- La détection automatique fonctionne bien pour les liens vidéo directs.
  Sur les sites de streaming qui masquent l'URL réelle du flux (lecteur
  JS custom), il faudra parfois utiliser le bouton "MPV" ou l'appui long
  manuellement, voire inspecter la page pour trouver le vrai lien du flux.
