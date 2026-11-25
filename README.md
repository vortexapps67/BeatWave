<div align="center">

<img src="https://private-user-images.githubusercontent.com/267700146/677091356-94db79e3-95bf-4a47-8133-ef2e785227fb.png?jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3OTU2Mzc1ODIsIm5iZiI6MTc5NTYzNzI4MiwicGF0aCI6Ii8yNjc3MDAxNDYvNjc3MDkxMzU2LTk0ZGI3OWUzLTk1YmYtNGE0Ny04MTMzLWVmMmU3ODUyMjdmYi5wbmc_WC1BbXotQWxnb3JpdGhtPUFXUzQtSE1BQy1TSEEyNTYmWC1BbXotQ3JlZGVudGlhbD1BS0lBVkNPRFlMU0E1M1BRSzRaQSUyRjIwMjYxMTI1JTJGdXMtZWFzdC0xJTJGczMlMkZhd3M0X3JlcXVlc3QmWC1BbXotRGF0ZT0yMDI2MTEyNVQyMDA4MDJaJlgtQW16LUV4cGlyZXM9MzAwJlgtQW16LVNpZ25hdHVyZT05YTdlN2I3MzcyMDNlZmE3YjlhMTFjZDdiODM5ODRkZGI3MDFiYWQ0MTIxNzM1ZWI2ZWE3NzQxODU0ZjY1M2FmJlgtQW16LVNpZ25lZEhlYWRlcnM9aG9zdCZyZXNwb25zZS1jb250ZW50LXR5cGU9aW1hZ2UlMkZwbmcifQ.mHDiQYqUGV_eoO-Qg8Cjzpf-ihndio5M_MWUFQGiBd8" alt="Logo BeatWave" width="132" />

# BEATWAVE

**Lecteur musical open-source au design Liquid Glass pour Android**

[![Download Latest Release](https://img.shields.io/badge/⬇️%20DOWNLOAD%20Latest%20Build-4CAF50?style=for-the-badge&logo=android&logoColor=white)](https://github.com/beatlabs790/BeatWave/releases/latest)
[![Previous Tag v5.0.0](https://img.shields.io/badge/Tag-v6.1.1-388E3C?style=for-the-badge)](https://github.com/beatlabs790/BeatWave/releases/latest)

[![Downloads](https://img.shields.io/github/downloads/beatlabs790/BeatWave/total?style=for-the-badge&color=blue)](https://github.com/beatlabs790/BeatWave/releases)
[![License](https://img.shields.io/github/license/beatlabs790/BeatWave?style=for-the-badge)](LICENSE)
[![Discord](https://img.shields.io/badge/Discord-Join%20Us-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.gg/spbuDTePSR)
[![Website](https://img.shields.io/badge/BeatWave-Website-8A2BE2?style=for-the-badge&logo=googlechrome&logoColor=white)](https://support-akshansh.vercel.app)

> **Android 8.0+** requis • **UPI :** `akshanshsinha67@axl` • **Site :** [support-akshansh.vercel.app](https://support-akshansh.vercel.app)

</div>

---

## 🎵 À propos de BeatWave

**BeatWave** est un lecteur musical open-source gratuit pour Android permettant de diffuser de la musique depuis YouTube Music, conçu avec **Jetpack Compose** et un moteur **Media3** ExoPlayer. Son interface repose sur un système **Liquid Glass** personnalisé — surfaces givrées et réfractives, défilement élastique façon iOS et flou progressif — en alternative aux composants Material standards.

Le projet a débuté comme un fork de [CONVX](https://github.com/cosmictaserdev-creator/Convx) et s'appuie également sur des composants issus de [vivi-music](https://github.com/vivizzz007/vivi-music) (voir la section [Crédits](#-crédits)).

---

## ✨ Fonctionnalités

### Interface Liquid Glass
* **Flou d'arrière-plan réel :** éléments vitrés (barre de navigation, boutons flottants, feuilles modales) échantillonnant et réfractant le contenu situé derrière eux plutôt qu'une simple transparence unie.
* **Animations fluides :** effet de rebond en fin de défilement, transitions floutées et indicateur de navigation élastique.
* **Material You :** palette de couleurs adaptative générée à partir de la pochette en cours de lecture.

### Lecture et streaming
* **Catalogue YouTube Music :** lecture sans publicité et lecture en arrière-plan avec contrôles complets dans les notifications et sur l'écran de verrouillage.
* **Téléchargements hors ligne :** mise en cache locale des morceaux avec gestion du stockage.
* **Qualité audio :** prise en charge audio haute qualité / sans perte et égaliseur intégré.

### Paroles et fonctions sociales
* **Paroles synchronisées :** affichage type karaoké avec mise en évidence mot par mot.
* **Discord Rich Presence :** affichage du titre en cours d'écoute sur votre profil Discord.
* **Listen Together :** synchronisation de la lecture en temps réel avec d'autres auditeurs.

### Confidentialité et mises à jour
* **Aucune télémétrie :** pas de pisteurs ni d'analytique ; bibliothèque locale et préférences conservées sur l'appareil.
* **Mise à jour intégrée :** vérification des nouvelles versions et notes de changements directement dans l'application, sans dépendre d'un store tiers.

---

## 🚗 Configuration Android Auto

Si **BeatWave** n'apparaît pas dans Android Auto :

1. Ouvrez **Android Auto** sur votre téléphone.
2. Appuyez sur le **menu burger** (trois lignes) puis allez dans **Paramètres**.
3. Faites défiler jusqu'en bas et appuyez plusieurs fois sur le **numéro de version** pour activer les paramètres développeur.
4. Appuyez sur le **menu à trois points** (`⋮`) en haut à droite.
5. Sélectionnez **Paramètres développeur**.
6. Activez **Sources inconnues**.
7. Redémarrez Android Auto puis connectez votre téléphone au véhicule.

---

## 🏗️ Architecture

Repères rapides pour les contributeurs (consultez [CONTRIBUTING.md](CONTRIBUTING.md) pour le guide complet) :

* **Interface :** Jetpack Compose, MVVM (`ui/screens` et `viewmodels`), navigation via `ui/screens/NavigationBuilder.kt`.
* **Liquid Glass :** `ui/component/GlassEffect.kt` expose `Modifier.liquidGlass(...)` à partir d'une copie intégrée de [Kyant0/backdrop](https://github.com/Kyant0/backdrop) sous `ui/component/backdrop/`. Un `Backdrop` (généralement un `rememberLayerBackdrop()` rattaché via `Modifier.layerBackdrop(...)` à un sous-arbre) capture les pixels réels, permettant aux surfaces vitrées d'échantillonner, flouter et réfracter l'arrière-plan via `drawBackdrop(...)`.
* **Lecture audio :** service ExoPlayer basé sur Media3 dans `playback/MusicService.kt`.
* **Données :** base Room (`db/`) pour la bibliothèque locale et DataStore pour les préférences (`utils/DataStore.kt`, `constants/PreferenceKeys.kt`).
* **Accès YouTube Music :** module `innertube` (client API InnerTube non officiel), séparé du module principal de l'application.
* **Mises à jour :** `vivimusic/updater/` interroge les releases GitHub et gère le téléchargement et l'installation de l'APK dans l'application (avec de légères différences selon les variantes FOSS/GMS, visibles via `BuildConfig.CAST_AVAILABLE`).

---

## 🤝 Contribuer

Les contributions sont les bienvenues : signalements de bugs, suggestions de fonctionnalités, traductions et code. Consultez [CONTRIBUTING.md](CONTRIBUTING.md) pour découvrir l'organisation du dépôt, la compilation et la liste de vérification des Pull Requests.

1. Forkez le dépôt.
2. Créez une branche dédiée (`git checkout -b feature/AmazingFeature`).
3. Validez vos modifications (`git commit -m 'Add some AmazingFeature'`).
4. Envoyez la branche sur votre fork (`git push origin feature/AmazingFeature`).
5. Ouvrez une Pull Request.

---

## 🛡️ Confidentialité et données

Chez **BeatWave**, votre musique et vos données restent les vôtres :

* **Aucune collecte de données :** aucune donnée personnelle, habitude d'utilisation ou historique d'écoute n'est collecté, stocké ou partagé.
* **100 % local :** paramètres, morceaux téléchargés et caches hors ligne restent enregistrés localement sur l'appareil.
* **Aucun suivi :** pas de traceurs cachés, d'outils d'analyse ou de services d'arrière-plan dédiés au suivi.

---

## 📜 Avertissement et licence

Ce projet n'est **ni affilié, ni financé, ni autorisé, ni approuvé, ni associé de quelque manière que ce soit** à YouTube, Google LLC ou leurs filiales. Les marques, noms commerciaux et droits de propriété intellectuelle cités appartiennent à leurs détenteurs respectifs. **BeatWave** est un projet indépendant conçu à des fins personnelles et éducatives.

Ce projet est distribué sous licence **GPL-3.0** (voir le fichier [LICENSE](LICENSE)).

---

## 💬 Communauté et support

Échangez avec d'autres utilisateurs, proposez des améliorations, signalez des problèmes et suivez les sorties :

* **Discord :** [Rejoindre la communauté](https://discord.gg/spbuDTePSR)
* **Suivi et échanges :** [Signaler un bug](https://github.com/beatlabs790/BeatWave/issues) • [Discussions](https://github.com/beatlabs790/BeatWave/discussions) • [Releases](https://github.com/beatlabs790/BeatWave/releases)

---

## 🙏 Crédits

**BeatWave** est développé et maintenu par [Vortex Apps](https://github.com/vortexapps67) ([support-akshansh.vercel.app](https://support-akshansh.vercel.app)).

### Base du projet
* **[vivi-music](https://github.com/vivizzz007/vivi-music)** par **Vividh P Ashokan** : projet d'origine dont BeatWave est issu.
* **Style lecteur Apple Music V17** (`Paramètres → Thème du lecteur`) : adapté depuis l'interface lecteur Apple Music de [vivi-music](https://github.com/vivizzz007/vivi-music) (GPL-3.0).

### Projets et bibliothèques clés
* **[Kyant0/backdrop](https://github.com/Kyant0/backdrop)** : bibliothèque de flou et de réfraction d'arrière-plan en temps réel utilisée pour Liquid Glass.
* **[Better Lyrics](https://github.com/better-lyrics/better-lyrics)** et **[SimpMusic](https://github.com/maxrave-dev/SimpMusic)** : paroles synchronisées.
* **[YouLyPlus](https://github.com/ibratabian17/YouLyPlus)** : mise en forme des paroles dans l'application.
* **[Monochrome](https://github.com/monochrome-music/monochrome)** : visualiseur animé.

<div align="center">

Merci à la communauté open-source pour les outils, bibliothèques et API qui rendent ce projet possible.

**Conçu avec ❤️ pour les amateurs de musique**

⭐ *Pensez à ajouter une étoile au dépôt si BeatWave vous plaît !*

</div>
